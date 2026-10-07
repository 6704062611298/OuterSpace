package outerspace.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;

import outerspace.roguelite.Choice;
import outerspace.roguelite.ChoiceMenu;
import outerspace.roguelite.RunState;
import outerspace.util.Constants;

/**
 * Draws a {@link ChoiceMenu} (reward, event, shop, rest) as a title, body text
 * and a column of option buttons, and tracks which option is selected.
 * Reads the menu; the run controller applies the choice.
 */
public class ChoiceScreen {

    private static final int BUTTON_WIDTH = 360;
    private static final int BUTTON_HEIGHT = 54;
    private static final int BUTTON_GAP = 12;
    private static final int TEXT_TOP = 170;

    private int selected;
    private ChoiceMenu shown;

    public int getSelected(ChoiceMenu menu) {
        sync(menu);
        return selected;
    }

    public void moveSelection(ChoiceMenu menu, int delta) {
        sync(menu);
        int count = menu.getChoices().size();
        selected = Math.floorMod(selected + delta, count);
    }

    /** Highlights the option under the mouse. Returns its index, or -1. */
    public int hover(ChoiceMenu menu, int mx, int my) {
        sync(menu);
        for (int i = 0; i < menu.getChoices().size(); i++) {
            if (buttonBounds(menu, i).contains(mx, my)) {
                selected = i;
                return i;
            }
        }
        return -1;
    }

    /** Resets the selection whenever a different menu is shown. */
    private void sync(ChoiceMenu menu) {
        if (menu != shown) {
            shown = menu;
            selected = firstEnabled(menu);
        }
    }

    private static int firstEnabled(ChoiceMenu menu) {
        List<Choice> choices = menu.getChoices();
        for (int i = 0; i < choices.size(); i++) {
            if (choices.get(i).isEnabled()) {
                return i;
            }
        }
        return 0;
    }

    private int firstButtonY(ChoiceMenu menu) {
        int lines = menu.getText().split("\n").length;
        return TEXT_TOP + lines * 22 + 40;
    }

    private Rectangle buttonBounds(ChoiceMenu menu, int index) {
        int x = (Constants.SCREEN_WIDTH - BUTTON_WIDTH) / 2;
        int y = firstButtonY(menu) + index * (BUTTON_HEIGHT + BUTTON_GAP);
        return new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public void draw(Graphics2D g2, ChoiceMenu menu, RunState run) {
        sync(menu);
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(UiStyle.PANEL);
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Run status
        g.setFont(UiStyle.SMALL);
        g.setColor(UiStyle.MID);
        UiStyle.drawCentered(g, "HP " + run.getPlayer().getHp() + "/" + run.getPlayer().getMaxHp()
                + "    GOLD " + run.getGold() + "    SCORE " + run.getScore(), 40);

        // Title framed by rules
        g.setColor(UiStyle.BRIGHT);
        g.setFont(UiStyle.TITLE);
        UiStyle.drawCentered(g, menu.getTitle(), 120);
        int w = g.getFontMetrics().stringWidth(menu.getTitle());
        int cx = Constants.SCREEN_WIDTH / 2;
        g.setColor(UiStyle.DIM);
        g.drawLine(30, 112, cx - w / 2 - 14, 112);
        g.drawLine(cx + w / 2 + 14, 112, Constants.SCREEN_WIDTH - 30, 112);

        g.setFont(UiStyle.BODY);
        g.setColor(UiStyle.MID);
        UiStyle.drawLines(g, menu.getText(), TEXT_TOP, 22);

        List<Choice> choices = menu.getChoices();
        for (int i = 0; i < choices.size(); i++) {
            drawButton(g, choices.get(i), buttonBounds(menu, i), i == selected);
        }

        g.setFont(UiStyle.SMALL);
        g.setColor(UiStyle.DIM);
        UiStyle.drawCentered(g, "W/S choose   ENTER select", Constants.SCREEN_HEIGHT - 30);
        g.dispose();
    }

    private void drawButton(Graphics2D g, Choice choice, Rectangle r, boolean active) {
        boolean enabled = choice.isEnabled();
        g.setColor(active && enabled ? new Color(255, 255, 255, 28) : UiStyle.BG);
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(active ? (enabled ? UiStyle.BRIGHT : UiStyle.DIM) : UiStyle.FAINT);
        g.setStroke(active ? UiStyle.THICK : UiStyle.THIN);
        g.drawRect(r.x, r.y, r.width, r.height);
        g.setStroke(UiStyle.THIN);
        if (active) {
            // Corner ticks for the sci-fi frame.
            g.setColor(UiStyle.ACCENT);
            g.fillRect(r.x - 3, r.y - 3, 6, 6);
            g.fillRect(r.x + r.width - 3, r.y + r.height - 3, 6, 6);
        }

        String label = choice.isUsed() ? choice.getLabel() + "  - SOLD" : choice.getLabel();
        g.setFont(UiStyle.LABEL);
        g.setColor(enabled ? UiStyle.BRIGHT : UiStyle.DIM);
        g.drawString(label, r.x + 14, r.y + 23);
        g.setFont(UiStyle.SMALL);
        g.setColor(enabled ? UiStyle.MID : UiStyle.FAINT);
        g.drawString(choice.getDetail(), r.x + 14, r.y + 42);
    }
}
