package outerspace.roguelite;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import outerspace.map.NodeType;
import outerspace.player.Player;

/**
 * Decides what the player earns and builds the reward and rest screens.
 * Elite fights pay more gold and always offer a rare upgrade; bosses pay the
 * most and repair the hull for the next act. All rolls use the run's seeded
 * random stream.
 */
public final class RewardManager {

    private static final int UPGRADE_PICKS = 3;
    private static final int SKIP_GOLD = 15;
    private static final double REST_HEAL = 0.35;
    private static final double BOSS_REPAIR = 0.5;
    private static final double RARE_CHANCE = 0.25;

    private final RunState run;

    public RewardManager(RunState run) {
        this.run = run;
    }

    /** Pays out gold for a cleared stage and returns the upgrade pick screen. */
    public ChoiceMenu combatReward(NodeType type, ActData act, Runnable onDone) {
        int gold = rollGold(type, act);
        run.addGold(gold);
        String text = "+" + gold + " gold";
        if (type == NodeType.BOSS) {
            int repaired = run.getPlayer().heal((int) (run.getPlayer().getMaxHp() * BOSS_REPAIR));
            text += "   Hull repaired +" + repaired;
        }
        String title = type == NodeType.BOSS ? act.getBoss().getName() + " DESTROYED"
                : type == NodeType.ELITE ? "ELITE DEFEATED" : "SECTOR CLEARED";

        List<Choice> choices = new ArrayList<>();
        for (Upgrade upgrade : rollUpgrades(UPGRADE_PICKS, type != NodeType.BATTLE)) {
            choices.add(upgradeChoice(upgrade));
        }
        choices.add(Choice.of("Skip", "+" + SKIP_GOLD + " gold", () -> {
            run.addGold(SKIP_GOLD);
            return null;
        }));
        return new ChoiceMenu(title, text + "\nChoose an upgrade.", choices, onDone);
    }

    /** Rest site: repair the hull, or tune the ship for one free upgrade. */
    public ChoiceMenu rest(Runnable onDone) {
        Player player = run.getPlayer();
        int heal = (int) (player.getMaxHp() * REST_HEAL);
        List<Upgrade> tune = rollUpgrades(1, false);
        List<Choice> choices = new ArrayList<>();
        choices.add(Choice.of("Repair", "Restore " + heal + " HP", () -> "Hull repaired +" + player.heal(heal) + " HP."));
        if (!tune.isEmpty()) {
            Upgrade upgrade = tune.get(0);
            choices.add(Choice.of("Tune: " + upgrade.getName(), upgrade.getDescription(), () -> {
                run.applyUpgrade(upgrade);
                return "Installed " + upgrade.getName() + ".";
            }));
        }
        return new ChoiceMenu("REST SITE", "A quiet orbit. Time for one job.", choices, onDone);
    }

    public Choice upgradeChoice(Upgrade upgrade) {
        String rare = upgrade.getRarity() == Upgrade.Rarity.RARE ? "  [RARE]" : "";
        return Choice.of(upgrade.getName() + rare, upgrade.getDescription(), () -> {
            run.applyUpgrade(upgrade);
            return null;
        });
    }

    /**
     * Up to {@code count} different upgrades the player can still use. Commons
     * are favoured; with {@code guaranteeRare} the first pick is rare when one
     * is available.
     */
    public List<Upgrade> rollUpgrades(int count, boolean guaranteeRare) {
        Random rng = run.getRng();
        List<Upgrade> commons = new ArrayList<>();
        List<Upgrade> rares = new ArrayList<>();
        for (Upgrade upgrade : Upgrade.ALL) {
            if (upgrade.isAvailable(run.getPlayer())) {
                (upgrade.getRarity() == Upgrade.Rarity.RARE ? rares : commons).add(upgrade);
            }
        }
        List<Upgrade> picks = new ArrayList<>();
        while (picks.size() < count && !(commons.isEmpty() && rares.isEmpty())) {
            boolean wantRare = (guaranteeRare && picks.isEmpty()) || rng.nextDouble() < RARE_CHANCE;
            List<Upgrade> pool = (wantRare && !rares.isEmpty()) || commons.isEmpty() ? rares : commons;
            picks.add(pool.remove(rng.nextInt(pool.size())));
        }
        return picks;
    }

    private int rollGold(NodeType type, ActData act) {
        Random rng = run.getRng();
        int a = act.getNumber();
        switch (type) {
            case BOSS:
                return 90 + a * 30 + rng.nextInt(21);
            case ELITE:
                return 40 + a * 10 + rng.nextInt(21);
            default:
                return 18 + a * 5 + rng.nextInt(13);
        }
    }
}
