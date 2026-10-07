package outerspace.boss;

/**
 * Data for one boss: name, toughness, damage and how many attack mechanics it
 * unlocks. Higher tiers add patterns to every phase, so later acts' bosses are
 * not just tankier but play differently.
 */
public final class BossData {

    private final String name;
    private final int hp;
    private final int bulletDamage;
    private final int contactDamage;
    private final double bulletSpeed;
    /** 1 = base patterns; 2 adds spirals; 3 adds a rotating ring curtain. */
    private final int tier;
    private final int scoreValue;

    public BossData(String name, int hp, int bulletDamage, int contactDamage,
            double bulletSpeed, int tier, int scoreValue) {
        this.name = name;
        this.hp = hp;
        this.bulletDamage = bulletDamage;
        this.contactDamage = contactDamage;
        this.bulletSpeed = bulletSpeed;
        this.tier = tier;
        this.scoreValue = scoreValue;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getBulletDamage() {
        return bulletDamage;
    }

    public int getContactDamage() {
        return contactDamage;
    }

    public double getBulletSpeed() {
        return bulletSpeed;
    }

    public int getTier() {
        return tier;
    }

    public int getScoreValue() {
        return scoreValue;
    }
}
