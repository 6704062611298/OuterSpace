package outerspace.ui;

import java.awt.Graphics2D;

import outerspace.boss.Boss;
import outerspace.roguelite.RunState;
import outerspace.util.Constants;

/**
 * In-stage overlay: HP bar, gold, score, act/node label, wave counter and the
 * boss health bar. Reads run and stage data only.
 */
public class Hud {

    private static final int BAR_WIDTH = 140;

    public void draw(Graphics2D g, RunState run, String stageLabel, int wave, Boss boss) {
        int hp = run.getPlayer().getHp();
        int maxHp = run.getPlayer().getMaxHp();

        // HP bar, top-left
        g.setColor(UiStyle.PANEL);
        g.fillRect(10, 10, BAR_WIDTH + 4, 14);
        g.setColor(UiStyle.BRIGHT);
        g.drawRect(10, 10, BAR_WIDTH + 4, 14);
        g.fillRect(12, 12, BAR_WIDTH * hp / maxHp, 11);
        g.setFont(UiStyle.SMALL);
        g.drawString("HP " + hp + "/" + maxHp, 12, 40);
        g.drawString("GOLD " + run.getGold(), 12, 56);

        // Score and stage, top-right
        String score = "SCORE " + run.getScore();
        int right = Constants.SCREEN_WIDTH - 12;
        g.drawString(score, right - g.getFontMetrics().stringWidth(score), 22);
        g.setColor(UiStyle.MID);
        g.drawString(stageLabel, right - g.getFontMetrics().stringWidth(stageLabel), 38);
        if (boss == null && wave > 0) {
            String waveText = "WAVE " + wave;
            g.drawString(waveText, right - g.getFontMetrics().stringWidth(waveText), 54);
        }

        if (boss != null) {
            drawBossBar(g, boss);
        }
    }

    private void drawBossBar(Graphics2D g, Boss boss) {
        // Under the top HUD, so it never covers the player at the bottom.
        int width = Constants.SCREEN_WIDTH - 60;
        int x = 30;
        int y = 84;
        g.setColor(UiStyle.PANEL);
        g.fillRect(x, y, width, 10);
        g.setColor(UiStyle.BRIGHT);
        g.drawRect(x, y, width, 10);
        g.fillRect(x + 2, y + 2, Math.max(0, (width - 3) * boss.getHp() / boss.getMaxHp()), 7);
        // Phase marks at 70% and 30%.
        g.setColor(UiStyle.BG);
        g.fillRect(x + (int) (width * 0.7), y, 2, 10);
        g.fillRect(x + (int) (width * 0.3), y, 2, 10);
        g.setColor(UiStyle.BRIGHT);
        g.setFont(UiStyle.LABEL);
        UiStyle.drawCentered(g, boss.getName() + "   PHASE " + boss.getPhase(), y - 6);
    }
}
