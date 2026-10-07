package outerspace.core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.Timer;

import outerspace.map.MapNode;
import outerspace.map.PathManager;
import outerspace.player.Player;
import outerspace.roguelite.ActData;
import outerspace.ui.ChoiceScreen;
import outerspace.ui.Hud;
import outerspace.ui.MainMenu;
import outerspace.ui.MapScreen;
import outerspace.util.Constants;

/**
 * The playable surface. Owns the game loop (a Swing {@link Timer} at ~60 FPS),
 * turns keyboard and mouse input into {@link RunController} calls, and draws
 * the screen for the current {@link GameState}. Update and render are kept
 * separate; gameplay lives in {@link CombatStage} and run flow in
 * {@link RunController}.
 */
public class GamePanel extends JPanel {

    private final RunController run;
    /** One scrolling background per image path, loaded the first time an act uses it. */
    private final Map<String, ParallaxBackground> backgrounds = new HashMap<>();
    private final Set<Integer> keys = new HashSet<>();
    private final MainMenu menu = new MainMenu();
    private final MapScreen mapScreen = new MapScreen();
    private final ChoiceScreen choiceScreen = new ChoiceScreen();
    private final Hud hud = new Hud();
    /** Last state drawn, to reset per-screen UI state when it changes. */
    private GameState shownState;
    /**
     * Set when a stage ends: Space was likely held to fire, and its key repeat
     * must not confirm the next screen. Cleared when Space is released.
     */
    private boolean spaceLocked;

    public GamePanel(BufferedImage playerImg, BufferedImage enemyImg, BufferedImage rammingImg,
            BufferedImage bulletImg, BufferedImage bossImg) {
        Player player = new Player(playerImg);
        CombatStage stage = new CombatStage(player, enemyImg, rammingImg, bulletImg, bossImg);
        this.run = new RunController(player, stage);

        setPreferredSize(new Dimension(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        setFocusable(true);

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // unused
            }

            @Override
            public void keyPressed(KeyEvent e) {
                keys.add(e.getKeyCode());
                handleActionKey(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keys.remove(e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    spaceLocked = false;
                }
            }
        });

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hover(e.getX(), e.getY());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                click(e.getX(), e.getY());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        Timer loop = new Timer(1000 / Constants.FPS, e -> tick());
        loop.start();
    }

    // --- Input ----------------------------------------------------------------

    private void handleActionKey(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
        boolean up = code == KeyEvent.VK_W || code == KeyEvent.VK_UP;
        boolean down = code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN;
        boolean left = code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT;
        boolean right = code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT;
        boolean confirm = code == KeyEvent.VK_ENTER || (code == KeyEvent.VK_SPACE && !spaceLocked);

        switch (run.getState()) {
            case MENU:
                if (up) {
                    menu.moveUp();
                } else if (down) {
                    menu.moveDown();
                } else if (confirm) {
                    selectMenuOption();
                }
                break;
            case MAP:
                PathManager path = run.getLevels().getPath();
                if (left || up) {
                    mapScreen.moveSelection(path, -1);
                } else if (right || down) {
                    mapScreen.moveSelection(path, 1);
                } else if (confirm) {
                    enterNode(mapScreen.getTarget(path));
                }
                break;
            case CHOICE:
                if (up || left) {
                    choiceScreen.moveSelection(run.getMenu(), -1);
                } else if (down || right) {
                    choiceScreen.moveSelection(run.getMenu(), 1);
                } else if (confirm) {
                    choose(choiceScreen.getSelected(run.getMenu()));
                }
                break;
            default:
                break;
        }
    }

    private void hover(int x, int y) {
        switch (run.getState()) {
            case MENU:
                menu.hover(x, y);
                break;
            case MAP:
                mapScreen.hover(run.getLevels().getPath(), x, y);
                break;
            case CHOICE:
                choiceScreen.hover(run.getMenu(), x, y);
                break;
            default:
                break;
        }
    }

    private void click(int x, int y) {
        switch (run.getState()) {
            case MENU:
                if (menu.hover(x, y) != -1) {
                    selectMenuOption();
                }
                break;
            case MAP:
                enterNode(mapScreen.hover(run.getLevels().getPath(), x, y));
                break;
            case CHOICE:
                int index = choiceScreen.hover(run.getMenu(), x, y);
                if (index != -1) {
                    choose(index);
                }
                break;
            default:
                break;
        }
    }

    private void selectMenuOption() {
        if (menu.getSelected() == MainMenu.START) {
            // A fixed seed replays the same maps: java -Douterspace.seed=1234 ...
            run.startRun(Long.getLong("outerspace.seed", Math.floorMod(System.nanoTime(), 1_000_000L)));
        } else {
            System.exit(0);
        }
    }

    private void enterNode(MapNode node) {
        if (node != null) {
            // Space both confirms on the map and fires; don't carry it into the stage.
            keys.clear();
            run.selectNode(node);
        }
    }

    private void choose(int index) {
        keys.clear();
        run.choose(index);
    }

    // --- Update ---------------------------------------------------------------

    private void tick() {
        if (run.getState() == GameState.PLAYING) {
            background().update();
            int dx = 0;
            int dy = 0;
            if (keys.contains(KeyEvent.VK_A) || keys.contains(KeyEvent.VK_LEFT)) {
                dx -= 1;
            }
            if (keys.contains(KeyEvent.VK_D) || keys.contains(KeyEvent.VK_RIGHT)) {
                dx += 1;
            }
            if (keys.contains(KeyEvent.VK_W) || keys.contains(KeyEvent.VK_UP)) {
                dy -= 1;
            }
            if (keys.contains(KeyEvent.VK_S) || keys.contains(KeyEvent.VK_DOWN)) {
                dy += 1;
            }
            boolean firing = keys.contains(KeyEvent.VK_SPACE);
            run.updateCombat(dx, dy, firing);
            if (run.getState() != GameState.PLAYING && firing) {
                spaceLocked = true;
            }
        }
        repaint();
    }

    // --- Render ---------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        GameState state = run.getState();

        if (state != shownState) {
            if (state == GameState.MENU) {
                menu.reset();
            } else if (state == GameState.MAP) {
                mapScreen.reset(run.getLevels().getPath());
            }
            shownState = state;
        }

        switch (state) {
            case MENU:
                menu.draw(g2, run.getLastRunSummary());
                break;
            case MAP:
                ActData act = run.getLevels().getAct();
                mapScreen.draw(g2, run.getLevels().getPath(), run.getRun(),
                        "ACT " + act.getNumber() + "  " + act.getName().toUpperCase(), run.getActBanner());
                break;
            case PLAYING:
                drawStage(g2);
                break;
            case CHOICE:
                // Rewards open over the frozen stage; shops, events and rests over the backdrop.
                if (run.getLevels().getCurrentNode().getType().isCombat()) {
                    drawStage(g2);
                } else {
                    background().draw(g2);
                }
                choiceScreen.draw(g2, run.getMenu(), run.getRun());
                break;
            default:
                break;
        }
    }

    private ParallaxBackground background() {
        String path = run.getLevels().getAct().getBackground();
        return backgrounds.computeIfAbsent(path, p -> new ParallaxBackground(Game.loadImage(p)));
    }

    private void drawStage(Graphics2D g2) {
        background().draw(g2);
        CombatStage stage = run.getStage();
        stage.draw(g2);
        MapNode node = run.getLevels().getCurrentNode();
        String label = "ACT " + run.getLevels().getAct().getNumber() + "  " + node.getType().getLabel().toUpperCase();
        hud.draw(g2, run.getRun(), label, stage.getWave(), stage.getBoss());
    }
}
