package outerspace.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import outerspace.util.Constants;

/**
 * The main menu screen: a title and two options (Start / Exit).
 * Only tracks which option is selected and draws itself; the
 * {@code GamePanel} decides what each option does.
 */
public class MainMenu {

    public static final int START = 0;
    public static final int EXIT = 1;

    private static final String[] OPTIONS = {"START", "EXIT"};
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 50;
    private static final int BUTTON_GAP = 20;
    private static final int FIRST_BUTTON_Y = Constants.SCREEN_HEIGHT / 2;

    private int selected = START;

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
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        g2.setColor(Color.CYAN);
        g2.setFont(new Font("SansSerif", Font.BOLD, 52));
        drawCentered(g2, "OUTERSPACE", Constants.SCREEN_HEIGHT / 4 + 30);

        if (lastRun != null) {
            String[] lines = lastRun.split("\n");
            g2.setColor(lines[0].startsWith("VICTORY") ? Color.WHITE : Color.RED);
            g2.setFont(new Font("SansSerif", Font.BOLD, 28));
            drawCentered(g2, lines[0], Constants.SCREEN_HEIGHT / 4 + 80);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
            for (int i = 1; i < lines.length; i++) {
                drawCentered(g2, lines[i], Constants.SCREEN_HEIGHT / 4 + 90 + i * 26);
            }
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        for (int i = 0; i < OPTIONS.length; i++) {
            Rectangle r = buttonBounds(i);
            boolean active = i == selected;

            g2.setColor(active ? new Color(0, 120, 200) : new Color(40, 40, 60));
            g2.fillRect(r.x, r.y, r.width, r.height);
            g2.setColor(active ? Color.WHITE : Color.GRAY);
            g2.drawRect(r.x, r.y, r.width, r.height);

            int textY = r.y + (r.height + g2.getFontMetrics().getAscent()) / 2 - 4;
            drawCentered(g2, OPTIONS[i], textY);
        }

        g2.setColor(Color.GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        drawCentered(g2, "W/S or Arrow keys to choose, Enter to select",
                Constants.SCREEN_HEIGHT - 40);
    }

    private Rectangle buttonBounds(int index) {
        int x = (Constants.SCREEN_WIDTH - BUTTON_WIDTH) / 2;
        int y = FIRST_BUTTON_Y + index * (BUTTON_HEIGHT + BUTTON_GAP);
        return new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    private void drawCentered(Graphics2D g2, String text, int y) {
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (Constants.SCREEN_WIDTH - textWidth) / 2, y);
    }
}
