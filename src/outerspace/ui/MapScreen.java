package outerspace.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.List;

import outerspace.map.LevelData;
import outerspace.map.MapNode;
import outerspace.map.NodeType;
import outerspace.map.PathManager;
import outerspace.roguelite.RunState;
import outerspace.util.Constants;

/**
 * Draws the act map and turns input into node choices. Floor 0 is at the
 * bottom and the boss at the top; the view scrolls to keep the player's floor
 * in sight. Node states: visited (filled), selectable (bright, pulsing),
 * reachable later (grey), cut off (faint), and beyond vision range ("?").
 * Keyboard: left/right to pick among the selectable nodes, Enter to go.
 * Mouse: hover and click. Reads map data only; never changes it.
 */
public class MapScreen {

    private static final int TOP_BAR = 56;
    private static final int BOTTOM_BAR = 92;
    private static final int FLOOR_GAP = 78;
    private static final int SIDE_MARGIN = 44;
    private static final int NODE_RADIUS = 15;
    private static final int BOSS_RADIUS = 26;

    private int selected;
    private MapNode hovered;
    private double scroll;

    /** Resets keyboard selection and recenters the view; call when the map is shown anew. */
    public void reset(PathManager path) {
        selected = 0;
        hovered = null;
        scroll = targetScroll(path);
    }

    public void moveSelection(PathManager path, int delta) {
        int count = path.getAvailable().size();
        if (count > 0) {
            selected = Math.floorMod(selected + delta, count);
        }
        hovered = null;
    }

    /** The node the player is aiming at with mouse or keyboard, or {@code null}. */
    public MapNode getTarget(PathManager path) {
        List<MapNode> options = path.getAvailable();
        if (hovered != null && options.contains(hovered)) {
            return hovered;
        }
        if (options.isEmpty()) {
            return null;
        }
        return options.get(Math.min(selected, options.size() - 1));
    }

    /** Updates hover from the mouse; returns the selectable node under it, or {@code null}. */
    public MapNode hover(PathManager path, int mx, int my) {
        hovered = null;
        for (MapNode node : path.getAvailable()) {
            int r = radius(node) + 6;
            if (Math.hypot(mx - nodeX(node), my - nodeY(node)) <= r) {
                hovered = node;
            }
        }
        return hovered;
    }

    // --- Layout -------------------------------------------------------------

    private int mapHeight(LevelData map) {
        return (map.getFloorCount() + 1) * FLOOR_GAP;
    }

    private int nodeX(MapNode node) {
        return SIDE_MARGIN + (int) (node.getX() * (Constants.SCREEN_WIDTH - 2 * SIDE_MARGIN));
    }

    /** Screen y; floor 0 sits low, the boss high, offset by the scroll. */
    private int nodeY(MapNode node) {
        int fromBottom = (node.getFloor() + 1) * FLOOR_GAP;
        return (int) (Constants.SCREEN_HEIGHT - BOTTOM_BAR - fromBottom + scroll);
    }

    private int startY() {
        return (int) (Constants.SCREEN_HEIGHT - BOTTOM_BAR + scroll) - 10;
    }

    /** Scroll so the floor after the current one sits in the lower half of the view. */
    private double targetScroll(PathManager path) {
        LevelData map = path.getMap();
        int viewHeight = Constants.SCREEN_HEIGHT - TOP_BAR - BOTTOM_BAR;
        double maxScroll = Math.max(0, mapHeight(map) + 40 - viewHeight);
        double wanted = (path.getCurrentFloor() + 1) * FLOOR_GAP - viewHeight * 0.35;
        return Math.max(0, Math.min(maxScroll, wanted));
    }

    private int radius(MapNode node) {
        return node.getType() == NodeType.BOSS ? BOSS_RADIUS : NODE_RADIUS;
    }

    // --- Drawing ------------------------------------------------------------

    public void draw(Graphics2D g2, PathManager path, RunState run, String actTitle, String banner) {
        scroll += (targetScroll(path) - scroll) * 0.12;
        LevelData map = path.getMap();
        MapNode target = getTarget(path);
        double pulse = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 220.0);

        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(UiStyle.BG);
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        drawGrid(g);

        g.clipRect(0, TOP_BAR, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT - TOP_BAR - BOTTOM_BAR);
        drawEdges(g, path, map, pulse);
        drawStart(g, path);
        for (MapNode node : map.allNodes()) {
            drawNode(g, path, node, node == target, pulse);
        }
        g.setClip(null);

        drawTopBar(g, run, actTitle);
        drawBottomBar(g, path, target);
        if (banner != null) {
            drawBanner(g, banner);
        }
        g.dispose();
    }

    private void drawGrid(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 10));
        for (int x = 0; x < Constants.SCREEN_WIDTH; x += 30) {
            g.drawLine(x, 0, x, Constants.SCREEN_HEIGHT);
        }
        int offset = (int) scroll % 30;
        for (int y = offset; y < Constants.SCREEN_HEIGHT; y += 30) {
            g.drawLine(0, y, Constants.SCREEN_WIDTH, y);
        }
    }

    private void drawEdges(Graphics2D g, PathManager path, LevelData map, double pulse) {
        // START fans out to every floor-0 node.
        int sx = Constants.SCREEN_WIDTH / 2;
        int sy = startY();
        for (MapNode node : map.getFloor(0)) {
            drawEdge(g, path, null, node, sx, sy, nodeX(node), nodeY(node), pulse);
        }
        for (MapNode from : map.allNodes()) {
            for (MapNode to : from.getNext()) {
                drawEdge(g, path, from, to, nodeX(from), nodeY(from), nodeX(to), nodeY(to), pulse);
            }
        }
    }

    private void drawEdge(Graphics2D g, PathManager path, MapNode from, MapNode to,
            int x1, int y1, int x2, int y2, double pulse) {
        Stroke stroke = UiStyle.DASHED;
        if (path.isTraveled(from, to)) {
            g.setColor(UiStyle.BRIGHT);
            stroke = UiStyle.THICK;
        } else if (from == path.getCurrent() && path.canEnter(to)) {
            int c = (int) (150 + 105 * pulse);
            g.setColor(new Color(c, c, c));
            stroke = UiStyle.THICK;
        } else if ((from == null ? path.getCurrent() == null : path.isReachable(from)) && path.isReachable(to)) {
            g.setColor(UiStyle.DIM);
        } else {
            g.setColor(UiStyle.FAINT);
        }
        // Trim the line so it stops at the node rims.
        double len = Math.hypot(x2 - x1, y2 - y1);
        if (len < 1) {
            return;
        }
        double ux = (x2 - x1) / len;
        double uy = (y2 - y1) / len;
        int r1 = from == null ? 6 : radius(from) + 4;
        int r2 = radius(to) + 4;
        g.setStroke(stroke);
        g.drawLine((int) (x1 + ux * r1), (int) (y1 + uy * r1), (int) (x2 - ux * r2), (int) (y2 - uy * r2));
        g.setStroke(UiStyle.THIN);
    }

    private void drawStart(Graphics2D g, PathManager path) {
        int x = Constants.SCREEN_WIDTH / 2;
        int y = startY();
        g.setColor(path.getCurrent() == null ? UiStyle.BRIGHT : UiStyle.MID);
        g.fillOval(x - 4, y - 4, 8, 8);
        g.setFont(UiStyle.SMALL);
        UiStyle.drawCentered(g, "START", x, y + 18);
    }

    private void drawNode(Graphics2D g, PathManager path, MapNode node, boolean targeted, double pulse) {
        int x = nodeX(node);
        int y = nodeY(node);
        int r = radius(node);
        boolean visited = path.isVisited(node);
        boolean current = node == path.getCurrent();
        boolean available = path.canEnter(node);
        boolean reachable = path.isReachable(node);
        boolean revealed = path.isRevealed(node);

        Color color = visited ? UiStyle.MID
                : available ? UiStyle.BRIGHT
                : reachable ? UiStyle.MID
                : UiStyle.FAINT;

        // Body
        g.setColor(UiStyle.BG);
        g.fillOval(x - r, y - r, r * 2, r * 2);
        if (visited) {
            g.setColor(new Color(255, 255, 255, current ? 70 : 35));
            g.fillOval(x - r, y - r, r * 2, r * 2);
        }
        g.setColor(color);
        g.setStroke(available || current ? UiStyle.THICK : UiStyle.THIN);
        g.drawOval(x - r, y - r, r * 2, r * 2);

        // Selectable nodes breathe; the targeted one gets an outer ring.
        if (available) {
            int glow = (int) (r + 4 + 3 * pulse);
            g.setColor(new Color(255, 255, 255, (int) (40 + 60 * pulse)));
            g.drawOval(x - glow, y - glow, glow * 2, glow * 2);
        }
        if (targeted) {
            int ring = r + 10;
            g.setColor(UiStyle.ACCENT);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(x - ring, y - ring, ring * 2, ring * 2);
        }
        g.setStroke(UiStyle.THIN);

        if (revealed) {
            drawIcon(g, node.getType(), x, y, color);
        } else {
            g.setColor(color);
            g.setFont(UiStyle.LABEL);
            UiStyle.drawCentered(g, "?", x, y + 5);
        }
        if (visited && !current) {
            // Visited nodes are crossed through faintly so the trail reads at a glance.
            g.setColor(new Color(255, 255, 255, 90));
            g.drawLine(x - r / 2, y + r / 2, x + r / 2, y - r / 2);
        }
    }

    /** Minimal line icons, one per node type. */
    private void drawIcon(Graphics2D g, NodeType type, int x, int y, Color color) {
        g.setColor(color);
        switch (type) {
            case BATTLE:
                // Crosshair
                g.drawOval(x - 6, y - 6, 12, 12);
                g.drawLine(x - 9, y, x - 3, y);
                g.drawLine(x + 3, y, x + 9, y);
                g.drawLine(x, y - 9, x, y - 3);
                g.drawLine(x, y + 3, x, y + 9);
                break;
            case ELITE:
                // Arrowhead with horns
                g.fillPolygon(new Polygon(new int[] {x - 8, x, x + 8, x}, new int[] {y - 6, y - 1, y - 6, y + 8}, 4));
                g.drawLine(x - 9, y - 9, x - 5, y - 5);
                g.drawLine(x + 9, y - 9, x + 5, y - 5);
                break;
            case EVENT:
                g.setFont(UiStyle.LABEL);
                UiStyle.drawCentered(g, "!", x, y + 5);
                break;
            case SHOP:
                // Crate with a handle
                g.drawRect(x - 7, y - 4, 14, 11);
                g.drawLine(x - 7, y, x + 7, y);
                g.drawLine(x, y - 4, x, y + 7);
                g.drawArc(x - 4, y - 9, 8, 8, 0, 180);
                break;
            case REST:
                // Repair cross
                g.fillRect(x - 2, y - 7, 4, 14);
                g.fillRect(x - 7, y - 2, 14, 4);
                break;
            case BOSS:
            default:
                // Diamond core with spikes
                g.setStroke(UiStyle.THICK);
                g.drawPolygon(new Polygon(new int[] {x, x + 13, x, x - 13}, new int[] {y - 13, y, y + 13, y}, 4));
                g.fillOval(x - 4, y - 4, 8, 8);
                g.drawLine(x - 20, y, x - 15, y);
                g.drawLine(x + 15, y, x + 20, y);
                g.setStroke(UiStyle.THIN);
                break;
        }
    }

    private void drawTopBar(Graphics2D g, RunState run, String actTitle) {
        g.setColor(UiStyle.BG);
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, TOP_BAR);
        g.setColor(UiStyle.FAINT);
        g.drawLine(0, TOP_BAR, Constants.SCREEN_WIDTH, TOP_BAR);

        g.setFont(UiStyle.LABEL);
        g.setColor(UiStyle.BRIGHT);
        g.drawString(actTitle, 14, 22);
        g.setFont(UiStyle.SMALL);
        g.setColor(UiStyle.MID);
        String stats = "HP " + run.getPlayer().getHp() + "/" + run.getPlayer().getMaxHp()
                + "   GOLD " + run.getGold() + "   SCORE " + run.getScore();
        g.drawString(stats, 14, 43);
    }

    private void drawBottomBar(Graphics2D g, PathManager path, MapNode target) {
        int top = Constants.SCREEN_HEIGHT - BOTTOM_BAR;
        g.setColor(UiStyle.BG);
        g.fillRect(0, top, Constants.SCREEN_WIDTH, BOTTOM_BAR);
        g.setColor(UiStyle.FAINT);
        g.drawLine(0, top, Constants.SCREEN_WIDTH, top);

        if (target != null) {
            g.setFont(UiStyle.LABEL);
            g.setColor(UiStyle.BRIGHT);
            UiStyle.drawCentered(g, target.getType().getLabel().toUpperCase(), top + 26);
            g.setFont(UiStyle.SMALL);
            g.setColor(UiStyle.MID);
            UiStyle.drawCentered(g, target.getType().getHint(), top + 46);
        }
        g.setColor(UiStyle.DIM);
        g.setFont(UiStyle.SMALL);
        UiStyle.drawCentered(g, "A/D choose   ENTER go   or click a node", top + 76);
    }

    private void drawBanner(Graphics2D g, String banner) {
        int y = Constants.SCREEN_HEIGHT / 2 - 30;
        g.setColor(UiStyle.PANEL);
        g.fillRect(0, y - 30, Constants.SCREEN_WIDTH, 50);
        g.setColor(UiStyle.BRIGHT);
        g.drawLine(0, y - 30, Constants.SCREEN_WIDTH, y - 30);
        g.drawLine(0, y + 20, Constants.SCREEN_WIDTH, y + 20);
        g.setFont(UiStyle.TITLE);
        UiStyle.drawCentered(g, banner, y + 2);
    }
}
