package outerspace.core;

import java.util.ArrayList;
import java.util.List;

import outerspace.map.MapNode;
import outerspace.map.NodeType;
import outerspace.player.Player;
import outerspace.player.PlayerStats;
import outerspace.roguelite.ActData;
import outerspace.roguelite.Choice;
import outerspace.roguelite.ChoiceMenu;
import outerspace.roguelite.EventLibrary;
import outerspace.roguelite.GameEvent;
import outerspace.roguelite.LevelManager;
import outerspace.roguelite.RewardManager;
import outerspace.roguelite.RunState;
import outerspace.shop.Shop;

/**
 * The run's flow, independent of Swing: start a run, pick a node, play its
 * stage or screen, collect the reward, go back to the map, advance acts, and
 * end the run in victory or death. {@link GamePanel} forwards input here and
 * draws whatever state this reports.
 *
 * <pre>
 * MAP --Battle/Elite/Boss--> PLAYING --cleared--> CHOICE (reward) --> MAP / next act
 * MAP --Event/Shop/Rest----> CHOICE --> MAP
 * PLAYING --dead--> MENU (run summary)
 * </pre>
 */
public class RunController {

    private static final int STARTING_GOLD = 50;
    private static final long BANNER_MS = 2200;

    private final Player player;
    private final CombatStage stage;
    private GameState state = GameState.MENU;
    private RunState run;
    private LevelManager levels;
    private RewardManager rewards;
    private ChoiceMenu menu;
    /** Banner shown on the map for a moment when an act starts. */
    private String actBanner;
    private long actStartTime;
    /** End-of-run summary for the main menu, or {@code null} before the first run. */
    private String lastRunSummary;

    public RunController(Player player, CombatStage stage) {
        this.player = player;
        this.stage = stage;
    }

    public void startRun(long seed) {
        player.reset(new PlayerStats());
        run = new RunState(seed, player, STARTING_GOLD);
        rewards = new RewardManager(run);
        levels = new LevelManager(ActData.ACTS, seed);
        startNextAct();
    }

    private void startNextAct() {
        levels.startNextAct();
        ActData act = levels.getAct();
        actBanner = "ACT " + act.getNumber() + " - " + act.getName().toUpperCase();
        actStartTime = System.currentTimeMillis();
        state = GameState.MAP;
    }

    /** Enters a node from the map. Ignores nodes that are not connected. */
    public void selectNode(MapNode node) {
        if (state != GameState.MAP || !levels.getPath().canEnter(node)) {
            return;
        }
        levels.enter(node);
        actBanner = null;
        ActData act = levels.getAct();
        NodeType type = node.getType();
        if (type.isCombat()) {
            stage.start(act.stageFor(type));
            state = GameState.PLAYING;
            return;
        }
        switch (type) {
            case SHOP:
                showChoice(Shop.open(run, act, this::returnToMap));
                break;
            case REST:
                showChoice(rewards.rest(this::returnToMap));
                break;
            default:
                GameEvent event = EventLibrary.pick(run, act.getNumber());
                showChoice(new ChoiceMenu(event.getTitle(), event.getText(),
                        event.buildChoices(run), this::returnToMap));
                break;
        }
    }

    /** One combat frame; handles the stage ending. */
    public void updateCombat(int dx, int dy, boolean firing) {
        if (state != GameState.PLAYING) {
            return;
        }
        stage.update(dx, dy, firing);
        run.addScore(stage.takeScore());
        if (stage.isPlayerDead()) {
            endRun(false);
        } else if (stage.isCleared()) {
            NodeType type = levels.getCurrentNode().getType();
            Runnable next = type == NodeType.BOSS ? this::finishAct : this::returnToMap;
            showChoice(rewards.combatReward(type, levels.getAct(), next));
        }
    }

    /**
     * Picks a choice on the current screen. A result text is shown on a
     * follow-up screen; one-shot choices (shop items) keep the screen open.
     */
    public void choose(int index) {
        if (state != GameState.CHOICE || index < 0 || index >= menu.getChoices().size()) {
            return;
        }
        Choice choice = menu.getChoices().get(index);
        if (!choice.isEnabled()) {
            return;
        }
        String result = choice.choose();
        if (player.isDead()) {
            endRun(false);
            return;
        }
        if (!choice.closes()) {
            return;
        }
        Runnable onDone = menu.getOnDone();
        if (result != null) {
            showChoice(ChoiceMenu.result(menu.getTitle(), result, onDone));
        } else {
            onDone.run();
        }
    }

    private void showChoice(ChoiceMenu choiceMenu) {
        menu = choiceMenu;
        state = GameState.CHOICE;
    }

    private void returnToMap() {
        state = GameState.MAP;
    }

    private void finishAct() {
        if (levels.isFinalAct()) {
            endRun(true);
        } else {
            startNextAct();
        }
    }

    private void endRun(boolean victory) {
        ActData act = levels.getAct();
        List<String> lines = new ArrayList<>();
        lines.add(victory ? "VICTORY" : "SHIP DESTROYED");
        lines.add("Act " + act.getNumber() + "  -  Score " + run.getScore());
        lines.add("Seed " + run.getSeed());
        lastRunSummary = String.join("\n", lines);
        state = GameState.MENU;
    }

    public GameState getState() {
        return state;
    }

    public RunState getRun() {
        return run;
    }

    public LevelManager getLevels() {
        return levels;
    }

    public ChoiceMenu getMenu() {
        return menu;
    }

    public CombatStage getStage() {
        return stage;
    }

    /** The act-start banner while it is still showing, otherwise {@code null}. */
    public String getActBanner() {
        if (actBanner != null && System.currentTimeMillis() - actStartTime > BANNER_MS) {
            actBanner = null;
        }
        return actBanner;
    }

    public String getLastRunSummary() {
        return lastRunSummary;
    }
}
