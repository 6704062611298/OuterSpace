package outerspace.player;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import outerspace.util.Constants;

/**
 * The player's ship. Responsible only for its own movement, state,
 * rendering and hitbox. It does not spawn enemies or control the shop.
 */
public class Player {

    private int x;
    private int y;
    private final int displayWidth;
    private final int displayHeight;
    private final int speed;
    private int hp;
    private final BufferedImage image;

    public Player(BufferedImage image) {
        this.image = image;
        double aspect = (double) image.getHeight() / image.getWidth();
        this.displayWidth = Constants.PLAYER_DISPLAY_WIDTH;
        this.displayHeight = (int) (Constants.PLAYER_DISPLAY_WIDTH * aspect);
        this.speed = Constants.PLAYER_SPEED;
        this.hp = Constants.PLAYER_MAX_HP;
        this.x = (Constants.SCREEN_WIDTH - displayWidth) / 2;
        this.y = Constants.SCREEN_HEIGHT - displayHeight - 20;
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

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp < 0) {
            hp = 0;
        }
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public int getHp() {
        return hp;
    }

    public int getCenterX() {
        return x + displayWidth / 2;
    }

    public int getTopY() {
        return y;
    }

    /** Reset to starting position and full HP for a new run. */
    public void reset() {
        this.hp = Constants.PLAYER_MAX_HP;
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
