package outerspace.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import outerspace.util.Constants;

/**
 * The main menu screen: a slowly drifting background image with ambient
 * particles, a title and two options (Start / Exit). Only tracks which option
 * is selected and draws itself; the {@code GamePanel} decides what each option does.
 */
public class MainMenu {

    public static final int START = 0;
    public static final int EXIT = 1;

    private static final String[] OPTIONS = {"START", "EXIT"};
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 50;
    private static final int BUTTON_GAP = 20;
    private static final int FIRST_BUTTON_Y = Constants.SCREEN_HEIGHT / 2 + 40;
    private static final int TITLE_Y = Constants.SCREEN_HEIGHT / 4 + 20;
    /** How much larger than the screen the background is drawn, leaving room to drift. */
    private static final double BG_ZOOM = 1.08;

    private static final Font TITLE_FONT = new Font("Monospaced", Font.BOLD, 54);
    private static final Font SUBTITLE_FONT = new Font("Monospaced", Font.PLAIN, 14);
    private static final Font HEADLINE_FONT = new Font("Monospaced", Font.BOLD, 24);
    private static final Font BUTTON_FONT = new Font("Monospaced", Font.BOLD, 24);

    private final BufferedImage background;
    private final MenuParticles particles = new MenuParticles();
    private int selected = START;
    private long frame;

    /** @param backgroundImage the menu backdrop; scaled once to cover the screen */
    public MainMenu(BufferedImage backgroundImage) {
        this.background = scaleToCover(backgroundImage);
    }

    public int getSelected() {
        return selected;
    }

    public void moveUp() {
        selected = (selected - 1 + OPTIONS.length) % OPTIONS.length;
    }

    public void moveDown() {
        selected = (selected + 1) % OPTIONS.length;
    }

    public void reset() {
        selected = START;
    }

    /** Advances the menu animation by one frame. */
    public void update() {
        frame++;
        particles.update();
        particles.emitSparks(buttonBounds(selected));
    }

    /** Highlights the option under the mouse. Returns its index, or -1 if none. */
    public int hover(int x, int y) {
        for (int i = 0; i < OPTIONS.length; i++) {
            if (buttonBounds(i).contains(x, y)) {
                selected = i;
                return i;
            }
        }
        return -1;
    }

    /**
     * @param lastRun summary of the previous run (first line is the headline),
     *                or {@code null} if no run has been played yet
     */
    public void draw(Graphics2D g2, String lastRun) {
        drawBackground(g2);
        particles.drawBackLayer(g2);

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        drawTitle(g2);

        if (lastRun != null) {
            String[] lines = lastRun.split("\n");
            g2.setColor(lines[0].startsWith("VICTORY") ? Color.WHITE : new Color(255, 90, 90));
            g2.setFont(HEADLINE_FONT);
            drawCentered(g2, lines[0], TITLE_Y + 90);
            g2.setColor(UiStyle.MID);
            g2.setFont(UiStyle.BODY);
            for (int i = 1; i < lines.length; i++) {
                drawCentered(g2, lines[i], TITLE_Y + 96 + i * 22);
            }
        }

        drawButtons(g2);
        particles.drawFrontLayer(g2);

        g2.setColor(UiStyle.DIM);
        g2.setFont(UiStyle.SMALL);
        drawCentered(g2, "W/S or Arrow keys to choose, Enter to select",
                Constants.SCREEN_HEIGHT - 30);
    }

    /** Draws the backdrop drifting slowly back and forth, then darkens the edges and text areas. */
    private void drawBackground(Graphics2D g2) {
        int w = Constants.SCREEN_WIDTH;
        int h = Constants.SCREEN_HEIGHT;
        double spareX = background.getWidth() - w;
        double spareY = background.getHeight() - h;
        double t = frame * 0.004;
        int x = (int) (-spareX / 2 * (1 + Math.sin(t)));
        int y = (int) (-spareY / 2 * (1 + Math.sin(t * 0.7 + 1)));
        g2.drawImage(background, x, y, null);

        // Darken top (title) and bottom (buttons/hint) so the text stays readable.
        g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 170), 0, h * 0.35f, new Color(0, 0, 0, 0)));
        g2.fillRect(0, 0, w, h);
        g2.setPaint(new GradientPaint(0, h * 0.45f, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, 190)));
        g2.fillRect(0, 0, w, h);
        g2.setPaint(new RadialGradientPaint(w / 2f, h / 2f, h * 0.75f,
                new float[] {0.5f, 1f}, new Color[] {new Color(0, 0, 0, 0), new Color(0, 0, 0, 200)}));
        g2.fillRect(0, 0, w, h);
    }

    /** The title with a pulsing glow behind it, an underline and a subtitle. */
    private void drawTitle(Graphics2D g2) {
        int cx = Constants.SCREEN_WIDTH / 2;
        double pulse = 0.5 + 0.5 * Math.sin(frame * 0.05);
        g2.setFont(TITLE_FONT);

        g2.setColor(new Color(120, 220, 255, (int) (25 + 35 * pulse)));
        for (int r = 4; r >= 1; r--) {
            for (int dx = -r; dx <= r; dx += r) {
                for (int dy = -r; dy <= r; dy += r) {
                    drawCentered(g2, "OUTERSPACE", cx + dx, TITLE_Y + dy);
                }
            }
        }
        g2.setColor(Color.WHITE);
        drawCentered(g2, "OUTERSPACE", cx, TITLE_Y);

        int lineW = 180;
        g2.setPaint(new GradientPaint(cx - lineW / 2f, 0, new Color(120, 220, 255, 0),
                cx, 0, new Color(120, 220, 255, 220), true));
        g2.fillRect(cx - lineW / 2, TITLE_Y + 16, lineW, 2);

        g2.setFont(SUBTITLE_FONT);
        g2.setColor(UiStyle.MID);
        drawCentered(g2, "-  A ROGUELITE SHOOTER  -", TITLE_Y + 42);
    }

    private void drawButtons(Graphics2D g2) {
        Object aa = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(BUTTON_FONT);
        double pulse = 0.5 + 0.5 * Math.sin(frame * 0.12);

        for (int i = 0; i < OPTIONS.length; i++) {
            Rectangle r = buttonBounds(i);
            boolean active = i == selected;

            if (active) {
                // Soft pulsing halo, widest layer faintest.
                for (int k = 3; k >= 1; k--) {
                    g2.setColor(new Color(120, 220, 255, (int) ((10 + 14 * pulse) * (4 - k))));
                    g2.fillRoundRect(r.x - k * 3, r.y - k * 3, r.width + k * 6, r.height + k * 6, 16, 16);
                }
                g2.setPaint(new GradientPaint(0, r.y, new Color(30, 110, 160, 230),
                        0, r.y + r.height, new Color(10, 40, 70, 230)));
            } else {
                g2.setColor(new Color(10, 12, 20, 180));
            }
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);

            g2.setStroke(active ? UiStyle.THICK : UiStyle.THIN);
            g2.setColor(active ? UiStyle.ACCENT : UiStyle.DIM);
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2.setStroke(UiStyle.THIN);

            int textY = r.y + (r.height + g2.getFontMetrics().getAscent()) / 2 - 4;
            g2.setColor(active ? Color.WHITE : UiStyle.MID);
            drawCentered(g2, active ? "> " + OPTIONS[i] + " <" : OPTIONS[i], textY);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
    }

    private Rectangle buttonBounds(int index) {
        int x = (Constants.SCREEN_WIDTH - BUTTON_WIDTH) / 2;
        int y = FIRST_BUTTON_Y + index * (BUTTON_HEIGHT + BUTTON_GAP);
        return new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    private void drawCentered(Graphics2D g2, String text, int y) {
        drawCentered(g2, text, Constants.SCREEN_WIDTH / 2, y);
    }

    private void drawCentered(Graphics2D g2, String text, int cx, int y) {
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, cx - textWidth / 2, y);
    }

    /** Scales the image so it covers the screen with {@link #BG_ZOOM} to spare for drifting. */
    private static BufferedImage scaleToCover(BufferedImage image) {
        double scale = BG_ZOOM * Math.max((double) Constants.SCREEN_WIDTH / image.getWidth(),
                (double) Constants.SCREEN_HEIGHT / image.getHeight());
        int w = (int) Math.ceil(image.getWidth() * scale);
        int h = (int) Math.ceil(image.getHeight() * scale);
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, w, h, null);
        g.dispose();
        return scaled;
    }
}
