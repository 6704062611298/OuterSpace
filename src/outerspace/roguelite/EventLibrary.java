package outerspace.roguelite;

import java.util.ArrayList;
import java.util.List;

import outerspace.player.Player;

/**
 * The catalogue of random events and the logic that picks one. Each event
 * trades something (HP, gold, a stat) for something else, so every choice is
 * a risk/reward decision. Add an entry to {@link #EVENTS} to add an event.
 */
public final class EventLibrary {

    private static final List<GameEvent> EVENTS = List.of(
            new GameEvent("DERELICT FREIGHTER",
                    "A drifting cargo hauler. Its hold is open,\nbut its reactor is leaking.", 1,
                    run -> List.of(
                            Choice.of("Salvage the hold", "+60 gold, take 15 damage", () -> {
                                run.addGold(60);
                                run.getPlayer().takeDamage(15);
                                return "You haul out 60 gold. The radiation burns.";
                            }),
                            Choice.of("Leave it", "Nothing happens", () -> null))),
            new GameEvent("ROGUE ENGINEER",
                    "A mechanic hails you: \"I can push your\nguns past spec. For a price.\"", 1,
                    run -> {
                        RewardManager rewards = new RewardManager(run);
                        List<Upgrade> pick = rewards.rollUpgrades(1, true);
                        List<Choice> choices = new ArrayList<>();
                        if (!pick.isEmpty()) {
                            Upgrade upgrade = pick.get(0);
                            choices.add(Choice.of("Pay 50 gold", upgrade.getName() + ": " + upgrade.getDescription(), () -> {
                                run.spend(50);
                                run.applyUpgrade(upgrade);
                                return "Installed " + upgrade.getName() + ".";
                            }).requires(() -> run.canAfford(50)));
                        }
                        choices.add(Choice.of("Decline", "Nothing happens", () -> null));
                        return choices;
                    }),
            new GameEvent("ION STORM",
                    "A storm front blocks the lane. Going\naround will burn fuel and time.", 1,
                    run -> List.of(
                            Choice.of("Fly through", "Take 20 damage, +1 move speed", () -> {
                                run.getPlayer().takeDamage(20);
                                run.getPlayer().getStats().addSpeed(1);
                                return "The ship shakes apart and comes out faster.";
                            }),
                            Choice.of("Go around", "Lose 25 gold", () -> {
                                run.addGold(-25);
                                return "A long detour. Fuel isn't cheap.";
                            }))),
            new GameEvent("MEDICAL BEACON",
                    "An automated aid station still answers\nyour hail.", 1,
                    run -> List.of(
                            Choice.of("Request repairs", "Restore 30 HP", () ->
                                    "Repaired +" + run.getPlayer().heal(30) + " HP."),
                            Choice.of("Strip it for parts", "+35 gold", () -> {
                                run.addGold(35);
                                return "You take what isn't bolted down.";
                            }))),
            new GameEvent("WRECKED WARSHIP",
                    "The hull is cracked, but the main battery\nlooks intact. Hooking it up means\nrerouting your shields.", 2,
                    run -> List.of(
                            Choice.of("Install the battery", "+1 bullet damage, -15 max HP", () -> {
                                Player player = run.getPlayer();
                                player.getStats().addDamage(1);
                                player.getStats().addMaxHp(-15);
                                player.clampHp();
                                return "Your guns hit harder. Your hull feels thin.";
                            }),
                            Choice.of("Leave it", "Nothing happens", () -> null))),
            new GameEvent("SMUGGLER'S GAMBLE",
                    "\"Double or nothing, pilot.\" The dice\nare already rolling.", 2,
                    run -> List.of(
                            Choice.of("Bet 40 gold", "50%: win 80 gold", () -> {
                                run.spend(40);
                                if (run.getRng().nextBoolean()) {
                                    run.addGold(80);
                                    return "Lucky roll. +80 gold.";
                                }
                                return "The house wins.";
                            }).requires(() -> run.canAfford(40)),
                            Choice.of("Walk away", "Nothing happens", () -> null))));

    private EventLibrary() {
        // no instances
    }

    /** A random event allowed in this act, preferring ones the run has not seen yet. */
    public static GameEvent pick(RunState run, int act) {
        List<GameEvent> fresh = new ArrayList<>();
        List<GameEvent> allowed = new ArrayList<>();
        for (GameEvent event : EVENTS) {
            if (event.getMinAct() <= act) {
                allowed.add(event);
                if (!run.hasSeenEvent(event.getTitle())) {
                    fresh.add(event);
                }
            }
        }
        List<GameEvent> pool = fresh.isEmpty() ? allowed : fresh;
        GameEvent event = pool.get(run.getRng().nextInt(pool.size()));
        run.markEventSeen(event.getTitle());
        return event;
    }
}
