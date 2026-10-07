package outerspace.player;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import outerspace.util.Constants;

/**
 * The player's ship. Responsible only for its own movement, HP, stats,
 * rendering and hitbox. It does not spawn enemies or control the shop.
 * HP carries over between stages; only a new run resets it.
 */
public class Player {

    private int x;
    private int y;
    private final int displayWidth;
    private final int displayHeight;
    private final BufferedImage image;
    private PlayerStats stats = new PlayerStats();
    private int hp;

    public Player(BufferedImage image) {
        this.image = image;
        double aspect = (double) image.getHeight() / image.getWidth();
        this.displayWidth = Constants.PLAYER_DISPLAY_WIDTH;
        this.displayHeight = (int) (Constants.PLAYER_DISPLAY_WIDTH * aspect);
        this.hp = stats.getMaxHp();
        resetPosition();
    }

    public void moveBy(int dx, int dy) {
        this.x += dx;
        this.y += dy;
        clamp();
    }

    private void clamp() {
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (x + displayWidth > Constants.SCREEN_WIDTH) {
            x = Constants.SCREEN_WIDTH - displayWidth;
        }
        if (y + displayHeight > Constants.SCREEN_HEIGHT) {
            y = Constants.SCREEN_HEIGHT - displayHeight;
        }
    }

    /** Applies damage reduced by armor (always at least 1). */
    public void takeDamage(int amount) {
        int reduced = Math.max(1, (int) Math.round(amount * (1 - stats.getArmor())));
        hp = Math.max(0, hp - reduced);
    }

    /** Restores HP up to the maximum. Returns how much was actually restored. */
    public int heal(int amount) {
        int before = hp;
        hp = Math.min(stats.getMaxHp(), hp + Math.max(0, amount));
        return hp - before;
    }

    /** Re-applies the HP cap, e.g. after max HP was lowered. */
    public void clampHp() {
        hp = Math.min(hp, stats.getMaxHp());
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return stats.getMaxHp();
    }

    public PlayerStats getStats() {
        return stats;
    }

    public int getCenterX() {
        return x + displayWidth / 2;
    }

    public int getCenterY() {
        return y + displayHeight / 2;
    }

    public int getTopY() {
        return y;
    }

    /** Start of a new run: fresh stats, full HP, starting position. */
    public void reset(PlayerStats stats) {
        this.stats = stats;
        this.hp = stats.getMaxHp();
        resetPosition();
    }

    /** Back to the starting position, e.g. at the start of each stage. */
    public void resetPosition() {
        this.x = (Constants.SCREEN_WIDTH - displayWidth) / 2;
        this.y = Constants.SCREEN_HEIGHT - displayHeight - 20;
    }

    public void draw(Graphics2D g) {
        g.drawImage(image, x, y, displayWidth, displayHeight, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, displayWidth, displayHeight);
    }
}
