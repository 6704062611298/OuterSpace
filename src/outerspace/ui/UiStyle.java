package outerspace.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;

import outerspace.util.Constants;

/**
 * Shared look for the run screens: a black and white sci-fi palette, fonts,
 * strokes and a few drawing helpers.
 */
final class UiStyle {

    static final Color BG = new Color(6, 6, 10);
    static final Color PANEL = new Color(0, 0, 0, 200);
    static final Color BRIGHT = Color.WHITE;
    static final Color MID = new Color(170, 170, 180);
    static final Color DIM = new Color(90, 90, 100);
    static final Color FAINT = new Color(45, 45, 52);
    static final Color ACCENT = new Color(120, 220, 255);

    static final Font TITLE = new Font("Monospaced", Font.BOLD, 22);
    static final Font BODY = new Font("Monospaced", Font.PLAIN, 14);
    static final Font SMALL = new Font("Monospaced", Font.PLAIN, 12);
    static final Font LABEL = new Font("Monospaced", Font.BOLD, 15);

    static final Stroke THIN = new BasicStroke(1f);
    static final Stroke THICK = new BasicStroke(2f);
    static final Stroke DASHED = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            10f, new float[] {3f, 5f}, 0f);

    private UiStyle() {
        // no instances
    }

    static void drawCentered(Graphics2D g2, String text, int y) {
        drawCentered(g2, text, Constants.SCREEN_WIDTH / 2, y);
    }

    static void drawCentered(Graphics2D g2, String text, int cx, int y) {
        int w = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, cx - w / 2, y);
    }

    /** Draws multi-line text centered, one line per {@code lineHeight}. Returns the y after the last line. */
    static int drawLines(Graphics2D g2, String text, int y, int lineHeight) {
        for (String line : text.split("\n")) {
            drawCentered(g2, line, y);
            y += lineHeight;
        }
        return y;
    }
}
