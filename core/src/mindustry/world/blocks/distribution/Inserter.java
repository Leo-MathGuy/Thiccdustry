package mindustry.world.blocks.distribution;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.*;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.Items;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.logic.LAccess;
import mindustry.logic.Senseable;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.distribution.Conveyor.ConveyorBuild;
import mindustry.world.blocks.distribution.Conveyor.ConveyorBuild.ConveyorBelt;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.blocks.storage.StorageBlock.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class Inserter extends Block implements Senseable {
    public @Load("inserter") TextureRegion baseRegion;
    public @Load("@-top-1") TextureRegion topRegion1;
    public @Load("@-top-2") TextureRegion topRegion2;
    public @Load("@-arm") TextureRegion arm;

    public @Load("inserter-arrow") TextureRegion arrowRegion;
    public @Load("inserter-claw-c") TextureRegion clawC;
    public @Load("inserter-claw-c-l") TextureRegion clawC_L;
    public @Load("inserter-claw-c-m") TextureRegion clawC_M;
    public @Load("inserter-claw-o") TextureRegion clawO;

    public float speed = 1f;
    public boolean allowCoreUnload = false;
    public float[] overdrives;
    public float basePower;

    public Inserter(String name) {
        super(name);

        group = BlockGroup.transportation;
        update = true;
        solid = true;
        hasItems = true;
        configurable = true;
        saveConfig = true;
        rotate = true;
        itemCapacity = 1;
        noUpdateDisabled = true;
        unloadable = false;
        isDuct = false;
        envDisabled = Env.none;
        clearOnDoubleTap = true;
        allowConfigInventory = true;
        priority = TargetPriority.transport;

        config(Integer.class, (InserterBuild tile, Integer i) -> {
            if (!configurable)
                return;

            if (tile.curOverdrive == i)
                return;
            tile.curOverdrive = i < 0 || i >= overdrives.length ? -1 : i;
        });
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.speed, 600f / speed, StatUnit.itemsSecond);
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("items");
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { baseRegion, arrowRegion, topRegion1 };
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(baseRegion, plan.drawx(), plan.drawy());
        Draw.rect(plan.rotation % 2 == 0 ? topRegion1 : topRegion2, plan.drawx(), plan.drawy());
        Draw.rect(arrowRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
    }

    public class InserterBuild extends Building {
        public float armRotation = 180f;

        public Item haveItem = null;
        public boolean aimingOut = true;
        public int offset = 0;
        public float fullHeat = 0f;
        private float maxRot = 180f;
        public int curOverdrive = 0;

        private void takeItem() {
            Building front = front(), back = back();

            if (front == null || back == null) {
                return;
            }

            var itemc = content.items().size;
            var itemseq = content.items();

            ItemStack min = new ItemStack(Items.copper, -1);
            ItemStack preferred = new ItemStack(Items.copper, -1);
            boolean first = true;

            if (back.block instanceof GenericCrafter gc) {
                var outputI = gc.outputItems;

                for (int i = 0; i < outputI.length; i++) {
                    var item = outputI[(i + offset) % outputI.length].item;
                    boolean frontAcc = front.acceptItem(this, item) || front instanceof ConveyorBuild;

                    if (back.items.has(item) && frontAcc) {
                        if (first) {
                            first = false;
                            min = new ItemStack(item, back.items.get(item));
                            preferred = min.copy();
                            continue;
                        }
                        if (back.items.get(item) < min.amount) {
                            min.set(item, back.items.get(item));
                        }
                    }
                }
            } else {
                for (int i = 0; i < itemc; i++) {
                    var item = (i + offset) % itemc;
                    var itemC = itemseq.get(item);
                    boolean frontAcc = front.acceptItem(this, itemseq.get(item)) || front instanceof ConveyorBuild;
                    boolean backHas = back.items.has(itemC);

                    if (back instanceof ConveyorBuild cb) {

                    }

                    if (backHas && frontAcc) {
                        if (front.items != null) {
                            if (first) {
                                first = false;
                                min = new ItemStack(itemC, front.items.get(itemC));
                                preferred = min.copy();
                                continue;
                            }
                            if (front.items.get(itemC) < min.amount) {
                                min.set(itemC, front.items.get(itemC));
                            }
                        } else {
                            preferred = new ItemStack(itemC, 0);
                            min = preferred.copy();
                        }
                    }
                }
            }

            if (preferred.amount == -1) {
                return;
            }

            Item item;

            if (preferred.amount - 2 > min.amount || min.amount < 2) {
                item = min.item;
                offset--;
            } else {
                item = preferred.item;
            }

            items.add(item, 1);
            back.items.remove(item, 1);
            back.itemTaken(item);
            offset++;
            offset %= itemc;
            haveItem = item;
            aimingOut = false;
        }

        private int checkTargetBelts(ConveyorBelt b1, ConveyorBelt b2, float x) {
            if (b1.hasSpace(x)) {
                return 0;
            } else if (b2.hasSpace(x)) {
                return 1;
            } else {
                return -1;
            }
        }

        private int getFurtherBelt(ConveyorBuild cb) {
            Vec2 offs = cb.getRelOffset(this).rotate(cb.rotation * -90);

            var y = offs.y;

            if (Math.abs(offs.y) <= 1f)
                return -1;

            return offs.y > 0 ? 1 : 0;
        }

        private void putItem() {
            Building front = front();

            if (haveItem == null) {
                aimingOut = false;
                return;
            }

            if (front != null) {
                if (front instanceof ConveyorBuild cb) {
                    if (cb.blendbits != 1) {
                        Vec2 offs = cb.getRelOffset(this).rotate(cb.rotation * -90);
                        offs.add(1f, 0);
                        int fartherBelt = getFurtherBelt(cb);

                        boolean oneBelt = false;

                        if (fartherBelt == -1) {
                            oneBelt = true;
                            fartherBelt = Mathf.round(-offs.y + 0.5f);
                        }

                        var belt1 = cb.belts[fartherBelt];
                        var belt2 = cb.belts[1 - fartherBelt];

                        var res = checkTargetBelts(belt1, belt2, offs.x);

                        if (res == 0 && !oneBelt) {
                            belt1.insertAt(haveItem, offs.x);
                        } else {
                            if (oneBelt && belt1.hasSpace(1f)) {
                                belt1.insertAt(haveItem, 1f);
                            } else {
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                } else {
                    if (front.acceptItem(this, haveItem)) {
                        front.handleItem(this, haveItem);
                    } else {
                        if (fullHeat < 1f) {
                            fullHeat = Mathf.approachDelta(fullHeat, 1f, 1f / 400f);
                            return;
                        }
                    }
                }
                items.remove(haveItem, 1);
                haveItem = null;
                fullHeat = 0f;
                aimingOut = true;
            }
        }

        private void approachArm(float angle) {
            armRotation = Mathf.approachDelta(armRotation, angle,
                    speed * (curOverdrive == 0 ? 1f : overdrives[curOverdrive]));
        }

        @Override
        public void updateTile() {
            Building front = front(), back = back();

            if (!aimingOut && haveItem == null) {
                aimingOut = true;
            }

            var basicReqs = front != null && back != null && back.items != null && front.team == team
                    && back.team == team;

            var coreReq = (allowCoreUnload || !(back instanceof CoreBuild
                    || (back instanceof StorageBuild sb && sb.linkedCore != null)));

            var itemseq = content.items();
            int itemc = itemseq.size;

            if (aimingOut) {
                // AIMING OUT
                if (armRotation == maxRot) {
                    if (basicReqs && (back.canUnload() || back instanceof ConveyorBuild) && coreReq) {
                        takeItem();
                    }
                } else {
                    approachArm(maxRot);
                }
            } else {
                // AIMING IN
                if (armRotation == 0f) {
                    putItem();
                } else {
                    approachArm(0f);
                }
            }
        }

        @Override
        public void draw() {
            Draw.z(Layer.block);

            Draw.rect(baseRegion, x, y);
            Draw.rect(rotation % 2 == 0 ? topRegion1 : topRegion2, x, y);
            Draw.rect(arrowRegion, x, y, rotdeg());

            Draw.z(Layer.block + 0.03f);
            Draw.rect(arm, x, y, rotation * 90f + armRotation);
            Draw.z(Layer.block + 0.02f);

            Tmp.v1.trns(rotation * 90, tilesize / 2f).rotate(armRotation);

            var armHeightC = 0.25f;
            var armLenC = 1.25f;

            boolean drawLong = false;
            boolean drawMed = false;

            if (front() instanceof ConveyorBuild cb) {
                float rel = cb.getRelOffset(this).rotate(cb.rotation * -90).y;
                if (Math.abs(rel) > 1 && cb.blendbits != 1) {
                    if (armRotation <= 60f) {
                        drawLong = true;
                    } else if (armRotation <= 120f) {
                        drawMed = true;
                    }
                }
            }

            if (drawLong || drawMed) {
                Draw.rect(drawLong ? clawC_L : clawC_M, Tmp.v1.x + x, Tmp.v1.y + y, rotation * 90f - armRotation);
                armHeightC = 1f * (drawLong ? 1f : 0.666f);
                armLenC = 2f * (drawLong ? 1f : 0.8333f);
            } else {
                Draw.rect(haveItem != null ? clawC : clawO, Tmp.v1.x + x, Tmp.v1.y + y, rotation * 90f - armRotation);
            }
            if (haveItem != null) {
                Draw.z(Layer.block + 0.011f);
                var other = fullHeat > 0.5f ? 1f - (fullHeat - 0.5f) * 2f : 1f;

                Draw.color(1f, other, other);

                Tmp.v1.trns(rotation * 90f, tilesize * armLenC * Mathf.cosDeg(armRotation),
                        tilesize * -armHeightC * Mathf.sinDeg(armRotation));
                Draw.rect(haveItem.fullIcon, Tmp.v1.x + x, Tmp.v1.y + y, itemSize / 2f, itemSize / 2f);
                Draw.color();
            }
        }

        /*
         * @Override
         * public void buildConfiguration(Table table) {
         * Seq<Float> ods = Seq.with(overdrives).map(f -> Float.valueOf(f));
         * 
         * if (ods.any()) {
         * ItemSelection.buildTable(Inserter.this, table, units,
         * () -> currentPlan == -1 ? null : plans.get(currentPlan).unit,
         * unit -> configure(plans.indexOf(u -> u.unit == unit)), selectionRows,
         * selectionColumns);
         * } else {
         * table.table(Styles.black3, t -> t.add("@none").color(Color.lightGray));
         * }
         * }
         */

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            return (haveItem == null) ? 1 : 0;
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source) {
            haveItem = item;
            aimingOut = false;
            items.add(item, 1);
        }

        @Override
        public int removeStack(Item item, int amount) {
            haveItem = null;
            aimingOut = true;
            items.remove(item, 1);
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(haveItem == null ? -1 : haveItem.id);
            write.f(armRotation);
            write.bool(aimingOut);
            write.s(offset);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int id = read.s();
            haveItem = id == -1 ? null : content.items().get(id);
            armRotation = read.f();
            aimingOut = read.bool();
            offset = read.s();
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress)
                return armRotation / maxRot;
            return super.sense(sensor);
        }
    }
}
