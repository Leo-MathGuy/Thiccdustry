package mindustry.world.draw;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;

public class DrawFlame extends DrawBlock {
    public Color flameColor = Color.valueOf("ffc999");
    public TextureRegion top;
    public float lightRadius = 60f, lightAlpha = 0.65f, lightSinScl = 10f, lightSinMag = 5;
    public float flameRadius = 3f, flameRadiusIn = 1.9f, flameRadiusScl = 5f, flameRadiusMag = 2f,
            flameRadiusInMag = 1f;

    public float offsetX = 0;
    public float offsetY = 0;
    public int teamCol = -1;
    public boolean always = false;

    public DrawFlame() {
    }

    public DrawFlame(Color flameColor) {
        this.flameColor = flameColor;
    }

    public DrawFlame(Color flameColor, float offX, float offY) {
        this.flameColor = flameColor;
        this.offsetX = offX;
        this.offsetY = offY;
    }

    public DrawFlame(int teamCol, float offX, float offY, boolean always) {
        this.teamCol = teamCol;
        this.offsetX = offX;
        this.offsetY = offY;
        this.always = always;
    }

    @Override
    public void load(Block block) {
        top = Core.atlas.find(block.name + "-top");
        block.clipSize = Math.max(block.clipSize, (lightRadius + lightSinMag) * 2f * block.size);
    }

    @Override
    public void draw(Building build) {
        if ((build.warmup() > 0f && flameColor.a > 0.001f) || always) {

            Color col = (3 > teamCol && teamCol >= 0) ? build.team().palette[teamCol] : flameColor;

            float g = 0.3f;
            float r = 0.06f;
            float cr = Mathf.random(0.1f);

            float x = offsetX + build.x;
            float y = offsetY + build.y;

            Draw.z(Layer.block + 0.01f);

            Draw.alpha(always ? 1 : build.warmup());

            Draw.alpha(
                    ((1f - g) + Mathf.absin(Time.time, 8f, g) + Mathf.random(r) - r) * (always ? 1 : build.warmup()));

            Draw.rect(top, x, y);
            Draw.tint(col);
            Fill.circle(x, y, flameRadius + Mathf.absin(Time.time, flameRadiusScl, flameRadiusMag) + cr);
            Draw.color(1f, 1f, 1f, (always ? 1 : build.warmup()));
            Fill.circle(x, y, flameRadiusIn + Mathf.absin(Time.time, flameRadiusScl, flameRadiusInMag) + cr);

            Draw.color();
        }
    }

    @Override
    public void drawLight(Building build) {
        Drawf.light(build.x + this.offsetX, build.y + this.offsetY,
                (lightRadius + Mathf.absin(lightSinScl, lightSinMag)) * (always ? 1 : build.warmup())
                        * build.block.size,
                ((3 > teamCol && teamCol >= 0) ? build.team().palette[teamCol] : flameColor),
                lightAlpha);
    }
}
