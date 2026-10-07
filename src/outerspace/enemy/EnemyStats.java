package outerspace.enemy;

/**
 * Numbers that make an enemy easy or hard: toughness, damage, firing, speed,
 * size and score. Acts build these (normal and elite variants), so difficulty
 * scales through data instead of new enemy classes.
 */
public final class EnemyStats {

    private final int hp;
    private final int contactDamage;
    private final int bulletDamage;
    private final double bulletSpeed;
    private final int fireIntervalMs;
    /** Bullets per volley; above 1 they fan out around the aim direction. */
    private final int shots;
    private final int speed;
    private final int displayWidth;
    private final int scoreValue;

    public EnemyStats(int hp, int contactDamage, int bulletDamage, double bulletSpeed,
            int fireIntervalMs, int shots, int speed, int displayWidth, int scoreValue) {
        this.hp = hp;
        this.contactDamage = contactDamage;
        this.bulletDamage = bulletDamage;
        this.bulletSpeed = bulletSpeed;
        this.fireIntervalMs = fireIntervalMs;
        this.shots = shots;
        this.speed = speed;
        this.displayWidth = displayWidth;
        this.scoreValue = scoreValue;
    }

    public int getHp() {
        return hp;
    }

    public int getContactDamage() {
        return contactDamage;
    }

    public int getBulletDamage() {
        return bulletDamage;
    }

    public double getBulletSpeed() {
        return bulletSpeed;
    }

    public int getFireIntervalMs() {
        return fireIntervalMs;
    }

    public int getShots() {
        return shots;
    }

    public int getSpeed() {
        return speed;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getScoreValue() {
        return scoreValue;
    }
}
