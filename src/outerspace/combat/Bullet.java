package outerspace.combat;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import outerspace.util.Constants;

/**
 * A single projectile. Moves by a per-bullet velocity vector each update
 * and is removed once it leaves the screen. Player bullets travel straight
 * up; enemy bullets are aimed at the player.
 */
public class Bullet {

    private int x;
    private int y;
    private final int displayWidth;
    private final int displayHeight;
    private final BufferedImage image;
    private final int dx;
    private final int dy;

    /** Player bullet: spawns just above {@code bottomY}, travels straight up. */
    public Bullet(int centerX, int bottomY, BufferedImage image) {
        this(centerX, bottomY, image, 0, -Constants.BULLET_SPEED);
    }

    /** Aimed bullet (enemies): arbitrary velocity, spawns at {@code topY}. */
    public Bullet(int centerX, int topY, BufferedImage image, int dx, int dy) {
        this.image = image;
        double aspect = (double) image.getHeight() / image.getWidth();
        this.displayWidth = Constants.BULLET_DISPLAY_WIDTH;
        this.displayHeight = (int) (Constants.BULLET_DISPLAY_WIDTH * aspect);
        this.x = centerX - displayWidth / 2;
        this.y = topY;
        this.dx = dx;
        this.dy = dy;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public boolean isOffScreen() {
        return y + displayHeight < 0 || y > Constants.SCREEN_HEIGHT
                || x + displayWidth < 0 || x > Constants.SCREEN_WIDTH;
    }

    public void draw(Graphics2D g) {
        g.drawImage(image, x, y, displayWidth, displayHeight, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, displayWidth, displayHeight);
    }
}
