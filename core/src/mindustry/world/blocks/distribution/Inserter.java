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

public class Inserter extends Block {
    public @Load("@-top-1") TextureRegion topRegion1;
    public @Load("@-top-2") TextureRegion topRegion2;
    public @Load("@-arrow") TextureRegion arrowRegion;
    public @Load("@-arm") TextureRegion arm;
    public @Load("@-claw-c") TextureRegion clawC;
    public @Load("@-claw-c-l") TextureRegion clawC_L;
    public @Load("@-claw-o") TextureRegion clawO;

    public float speed = 1f;
    public boolean allowCoreUnload = false;

    public Inserter(String name) {
        super(name);

        group = BlockGroup.transportation;
        update = true;
        solid = true;
        hasItems = true;
        configurable = false;
        saveConfig = true;
        rotate = true;
        itemCapacity = 0;
        noUpdateDisabled = true;
        unloadable = false;
        isDuct = false;
        envDisabled = Env.none;
        clearOnDoubleTap = true;
        priority = TargetPriority.transport;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.speed, 60f / speed, StatUnit.itemsSecond);
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("items");
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[] { region, arrowRegion, topRegion1 };
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(plan.rotation % 2 == 0 ? topRegion1 : topRegion2, plan.drawx(), plan.drawy());
        Draw.rect(arrowRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
    }

    public class InserterBuild extends Building {
        public float armRotation = 180f;

        public Item haveItem = null;
        public boolean aimingOut = true;
        public int offset = 0;

        private void takeItem() {
            Building front = front(), back = back();

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

                    if (back.items.has(itemC) && frontAcc) {
                        if (first) {
                            first = false;
                            min = new ItemStack(itemC, front.items.get(itemC));
                            preferred = min.copy();
                            continue;
                        }
                        if (front.items.get(itemC) < min.amount) {
                            min.set(itemC, front.items.get(itemC));
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
            } else {
                item = preferred.item;
            }

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

            if (Math.abs(offs.y) <= 1f)
                return -1;

            return offs.y > 0 ? 1 : 0;
        }

        private void putItem() {
            Building front = front();

            if (front != null) {
                if (front instanceof ConveyorBuild cb) {
                    if (cb.blendbits != 1) {
                        Vec2 offs = cb.getRelOffset(this).rotate(cb.rotation * -90);
                        offs.add(1f, 0);

                        int closerBelt = getFurtherBelt(cb);

                        var belt1 = cb.belts[closerBelt];
                        var belt2 = cb.belts[1 - closerBelt];

                        var res = checkTargetBelts(belt1, belt2, offs.x);

                        if (res != -1) {
                            (res == 0 ? belt1 : belt2).insertAt(haveItem, offs.x);
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                    haveItem = null;
                    aimingOut = true;
                } else {
                    front.handleItem(this, haveItem);
                    haveItem = null;
                    aimingOut = true;
                }
            }
        }

        private void approachArm(float angle) {
            armRotation = Mathf.approachDelta(armRotation, angle, speed);
        }

        @Override
        public void updateTile() {
            Building front = front(), back = back();

            var basicReqs = front != null && back != null && back.items != null && front.team == team
                    && back.team == team;

            var coreReq = (allowCoreUnload || !(back instanceof CoreBuild
                    || (back instanceof StorageBuild sb && sb.linkedCore != null)));

            var itemseq = content.items();
            int itemc = itemseq.size;

            if (aimingOut) {
                // AIMING OUT
                if (armRotation == 180f) {
                    if (basicReqs && back.canUnload() && coreReq) {
                        takeItem();
                    }
                } else {
                    approachArm(180f);
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

            Draw.rect(block.region, x, y);
            Draw.rect(rotation % 2 == 0 ? topRegion1 : topRegion2, x, y);
            Draw.rect(arrowRegion, x, y, rotdeg());

            Draw.z(Layer.block + 0.03f);
            Draw.rect(arm, x, y, rotation * 90f + armRotation);
            Draw.z(Layer.block + 0.02f);

            Tmp.v1.trns(rotation * 90, tilesize / 2f).rotate(armRotation);

            var armHeightC = 0.25f;
            var armLenC = 1.25f;

            if (haveItem != null && front() instanceof ConveyorBuild cb && armRotation <= 90f
                    && cb.blendbits != 1 && cb.belts[getFurtherBelt(cb)]
                            .hasSpace(cb.getRelOffset(this).rotate(cb.rotation * -90).add(1f, 0).x)) {
                Draw.rect(clawC_L, Tmp.v1.x + x, Tmp.v1.y + y, rotation * 90f - armRotation);
                armHeightC = 0.5f;
                armLenC = 1.5f;
            } else {
                Draw.rect(haveItem != null ? clawC : clawO, Tmp.v1.x + x, Tmp.v1.y + y, rotation * 90f - armRotation);
            }
            if (haveItem != null) {
                Draw.z(Layer.block + 0.01f);
                Tmp.v1.trns(rotation * 90f, tilesize * armLenC * Mathf.cosDeg(armRotation),
                        tilesize * -armHeightC * Mathf.sinDeg(armRotation));
                Draw.rect(haveItem.fullIcon, Tmp.v1.x + x, Tmp.v1.y + y, itemSize / 2f, itemSize / 2f);
            }
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
    }
}
