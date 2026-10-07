package outerspace.roguelite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import outerspace.player.Player;

/**
 * Everything that belongs to the current run and is lost when it ends: the
 * seed and its random stream, gold, score, and the upgrades taken. Gold only
 * changes through {@link #addGold} and {@link #spend}, which is the run's
 * whole economy. The player ship (HP and stats) is referenced, not owned.
 */
public final class RunState {

    private final long seed;
    private final Random rng;
    private final Player player;
    private final List<Upgrade> upgrades = new ArrayList<>();
    private final List<String> seenEvents = new ArrayList<>();
    private int gold;
    private int score;

    public RunState(long seed, Player player, int startingGold) {
        this.seed = seed;
        this.rng = new Random(seed);
        this.player = player;
        this.gold = startingGold;
    }

    public long getSeed() {
        return seed;
    }

    /** Random stream for rewards, events and shops; seeded with the run seed. */
    public Random getRng() {
        return rng;
    }

    public Player getPlayer() {
        return player;
    }

    public int getGold() {
        return gold;
    }

    public void addGold(int amount) {
        gold = Math.max(0, gold + amount);
    }

    public boolean canAfford(int price) {
        return gold >= price;
    }

    /** Spends gold if there is enough. Returns {@code false} and changes nothing otherwise. */
    public boolean spend(int price) {
        if (!canAfford(price)) {
            return false;
        }
        gold -= price;
        return true;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int amount) {
        score += amount;
    }

    public void applyUpgrade(Upgrade upgrade) {
        upgrade.apply(player);
        upgrades.add(upgrade);
    }

    public List<Upgrade> getUpgrades() {
        return Collections.unmodifiableList(upgrades);
    }

    public boolean hasSeenEvent(String title) {
        return seenEvents.contains(title);
    }

    public void markEventSeen(String title) {
        seenEvents.add(title);
    }
}
