package outerspace.enemy;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import outerspace.util.Constants;

/**
 * A basic descending enemy. Spawns above the top edge at a given X and
 * moves downward each update until it leaves the bottom of the screen.
 * Flipped vertically so it faces the player, and can fire on a cooldown.
 */
public class Enemy {

    private int x;
    private int y;
    private final int displayWidth;
    private final int displayHeight;
    private final BufferedImage image;
    private long lastShotTime;

    public Enemy(int x, BufferedImage image) {
        this.image = image;
        double aspect = (double) image.getHeight() / image.getWidth();
        this.displayWidth = Constants.ENEMY_DISPLAY_WIDTH;
        this.displayHeight = (int) (Constants.ENEMY_DISPLAY_WIDTH * aspect);
        this.x = clampX(x);
        this.y = -displayHeight;
    }

    private int clampX(int value) {
        if (value < 0) {
            return 0;
        }
        if (value + displayWidth > Constants.SCREEN_WIDTH) {
            return Constants.SCREEN_WIDTH - displayWidth;
        }
        return value;
    }

    public void update() {
        y += Constants.ENEMY_SPEED;
    }

    public boolean isOffScreen() {
        return y > Constants.SCREEN_HEIGHT;
    }

    /**
     * Returns true (and marks the cooldown) when the enemy may fire this
     * tick. Only fires once the enemy is actually on screen.
     */
    public boolean canFire(long now) {
        if (y < 0) {
            return false;
        }
        if (now - lastShotTime < Constants.ENEMY_FIRE_INTERVAL_MS) {
            return false;
        }
        lastShotTime = now;
        return true;
    }

    public int getCenterX() {
        return x + displayWidth / 2;
    }

    public int getBottomY() {
        return y + displayHeight;
    }

    public void draw(Graphics2D g) {
        // Flip vertically so the sprite's nose points down toward the player.
        g.drawImage(image,
                x, y + displayHeight, x + displayWidth, y,
                0, 0, image.getWidth(), image.getHeight(),
                null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, displayWidth, displayHeight);
    }
}
