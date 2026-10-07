package outerspace.roguelite;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import outerspace.player.Player;
import outerspace.player.PlayerStats;

/**
 * A run upgrade: name, description, rarity, shop price and its effect on the
 * player. The full catalogue lives in {@link #ALL}; add an entry there to add
 * an upgrade to rewards, the shop and events at once.
 */
public final class Upgrade {

    public enum Rarity {
        COMMON,
        RARE
    }

    public static final List<Upgrade> ALL = List.of(
            new Upgrade("Rapid Feed", "Fire rate +15%", Rarity.COMMON, 45,
                    p -> p.getStats().scaleFireInterval(0.85),
                    p -> p.getStats().getFireIntervalMs() > PlayerStats.MIN_FIRE_INTERVAL_MS),
            new Upgrade("Hull Plating", "Max HP +20, repair 20", Rarity.COMMON, 45,
                    p -> {
                        p.getStats().addMaxHp(20);
                        p.heal(20);
                    },
                    p -> true),
            new Upgrade("Thrusters", "Move speed +1", Rarity.COMMON, 40,
                    p -> p.getStats().addSpeed(1),
                    p -> p.getStats().getSpeed() < PlayerStats.MAX_SPEED),
            new Upgrade("Deflector", "Take 10% less damage", Rarity.COMMON, 50,
                    p -> p.getStats().addArmor(0.1),
                    p -> p.getStats().getArmor() < PlayerStats.MAX_ARMOR),
            new Upgrade("Rail Coils", "Bullet speed +2", Rarity.COMMON, 35,
                    p -> p.getStats().addBulletSpeed(2),
                    p -> true),
            new Upgrade("Plasma Rounds", "Bullet damage +1", Rarity.RARE, 85,
                    p -> p.getStats().addDamage(1),
                    p -> true),
            new Upgrade("Split Barrel", "+1 bullet per shot", Rarity.RARE, 90,
                    p -> p.getStats().addShots(1),
                    p -> p.getStats().getShots() < PlayerStats.MAX_SHOTS));

    private final String name;
    private final String description;
    private final Rarity rarity;
    private final int price;
    private final Consumer<Player> effect;
    private final Predicate<Player> available;

    private Upgrade(String name, String description, Rarity rarity, int price,
            Consumer<Player> effect, Predicate<Player> available) {
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.price = price;
        this.effect = effect;
        this.available = available;
    }

    public void apply(Player player) {
        effect.accept(player);
    }

    /** False when the upgrade would do nothing (its stat is already capped). */
    public boolean isAvailable(Player player) {
        return available.test(player);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public int getPrice() {
        return price;
    }
}
