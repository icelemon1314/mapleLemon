package client.inventory;

/**
 * 武器类型
 *
 * @author 7
 */
public enum MapleWeaponType {

    没有武器(1.43F, 20, 0),
    单手剑(1.2F, 20, 30),
    单手斧(1.2F, 20, 31),
    单手钝器(1.2F, 20, 32),
    短刀(1.3F, 20, 33),
    手杖(1.3F, 15, 36),
    短杖(1.2F, 25, 37),
    长杖(1.2F, 25, 38),
    双手斧(1.34F, 20, 40),
    双手剑(1.34F, 20, 41),
    双手钝器(1.34F, 20, 42),
    枪(1.49F, 20, 43),
    矛(1.49F, 20, 44),
    弓(1.3F, 15, 45),
    弩(1.35F, 15, 46),
    拳套(1.75F, 15, 47),
    指节(1.7F, 20, 48),
    大剑(1.49F, 15, 56),
    太刀(1.34F, 15, 57),;

    private final float damageMultiplier;
    private final int baseMastery;
    private final int weaponType;

    private MapleWeaponType(float maxDamageMultiplier, int baseMastery, int weaponType) {
        this.damageMultiplier = maxDamageMultiplier;
        this.baseMastery = baseMastery;
        this.weaponType = weaponType;
    }

    public float getMaxDamageMultiplier() {
        return this.damageMultiplier;
    }

    public int getBaseMastery() {
        return this.baseMastery;
    }

    public int getWeaponType() {
        return this.weaponType;
    }
}
