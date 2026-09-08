package outerspace.combat;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import outerspace.util.Constants;

/**
 * A single projectile fired by the player. Moves straight up each update
 * and is removed once it leaves the top of the screen.
 */
public class Bullet {

    private int x;
    private int y;
    private final int displayWidth;
    private final int displayHeight;
    private final BufferedImage image;

    public Bullet(int centerX, int bottomY, BufferedImage image) {
        this.image = image;
        double aspect = (double) image.getHeight() / image.getWidth();
        this.displayWidth = Constants.BULLET_DISPLAY_WIDTH;
        this.displayHeight = (int) (Constants.BULLET_DISPLAY_WIDTH * aspect);
        this.x = centerX - displayWidth / 2;
        this.y = bottomY - displayHeight;
    }

    public void update() {
        y -= Constants.BULLET_SPEED;
    }

    public boolean isOffScreen() {
        return y + displayHeight < 0;
    }

    public void draw(Graphics2D g) {
        g.drawImage(image, x, y, displayWidth, displayHeight, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, displayWidth, displayHeight);
    }
}
