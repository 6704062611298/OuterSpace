package outerspace.player;

import outerspace.util.Constants;

/**
 * The ship's tunable stats for the current run. Starts from the base values in
 * {@link Constants}; upgrades, shop items and events change it through the
 * methods below, which keep every stat inside sane limits.
 */
public final class PlayerStats {

    public static final int MAX_SHOTS = 5;
    public static final int MIN_FIRE_INTERVAL_MS = 70;
    public static final int MAX_SPEED = 9;
    public static final double MAX_ARMOR = 0.5;
    private static final int MIN_MAX_HP = 20;

    private int maxHp = Constants.PLAYER_MAX_HP;
    private int damage = 1;
    private int fireIntervalMs = Constants.BULLET_FIRE_INTERVAL_MS;
    private int speed = Constants.PLAYER_SPEED;
    private int shots = 1;
    private double bulletSpeed = Constants.BULLET_SPEED;
    /** Fraction of incoming damage ignored, 0..MAX_ARMOR. */
    private double armor;

    public int getMaxHp() {
        return maxHp;
    }

    public int getDamage() {
        return damage;
    }

    public int getFireIntervalMs() {
        return fireIntervalMs;
    }

    public int getSpeed() {
        return speed;
    }

    public int getShots() {
        return shots;
    }

    public double getBulletSpeed() {
        return bulletSpeed;
    }

    public double getArmor() {
        return armor;
    }

    public void addMaxHp(int amount) {
        maxHp = Math.max(MIN_MAX_HP, maxHp + amount);
    }

    public void addDamage(int amount) {
        damage = Math.max(1, damage + amount);
    }

    /** Multiplies the delay between shots; below 1 fires faster. */
    public void scaleFireInterval(double factor) {
        fireIntervalMs = Math.max(MIN_FIRE_INTERVAL_MS, (int) Math.round(fireIntervalMs * factor));
    }

    public void addSpeed(int amount) {
        speed = Math.max(1, Math.min(MAX_SPEED, speed + amount));
    }

    public void addShots(int amount) {
        shots = Math.max(1, Math.min(MAX_SHOTS, shots + amount));
    }

    public void addBulletSpeed(double amount) {
        bulletSpeed = Math.max(2, bulletSpeed + amount);
    }

    public void addArmor(double amount) {
        armor = Math.max(0, Math.min(MAX_ARMOR, armor + amount));
    }
}
