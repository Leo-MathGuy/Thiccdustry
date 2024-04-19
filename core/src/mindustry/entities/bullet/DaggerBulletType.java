package mindustry.entities.bullet;

import arc.Core;

public class DaggerBulletType extends BasicBulletType {
    @Override
    public void load() {
        super.load();

        backRegion = Core.atlas.find("blank");
    }

    public DaggerBulletType(float speed, float damage, String bulletSprite) {
        super(speed, damage);
        this.sprite = bulletSprite;
    }
}
