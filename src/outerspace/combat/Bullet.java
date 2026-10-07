package outerspace.combat;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import outerspace.util.Constants;

/**
 * A single projectile. Moves by its own velocity vector each update, carries
 * the damage it deals on hit, and is removed once it leaves the screen.
 * Positions are doubles so angled shots (spreads, rings) travel smoothly.
 */
public class Bullet {

    private double x;
    private double y;
    private final int displayWidth;
    private final int displayHeight;
    private final BufferedImage image;
    private final double dx;
    private final double dy;
    private final int damage;

    public Bullet(double centerX, double topY, BufferedImage image, double dx, double dy, int damage) {
        this.image = image;
        double aspect = (double) image.getHeight() / image.getWidth();
        this.displayWidth = Constants.BULLET_DISPLAY_WIDTH;
        this.displayHeight = (int) (Constants.BULLET_DISPLAY_WIDTH * aspect);
        this.x = centerX - displayWidth / 2.0;
        this.y = topY;
        this.dx = dx;
        this.dy = dy;
        this.damage = damage;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public boolean isOffScreen() {
        return y + displayHeight < 0 || y > Constants.SCREEN_HEIGHT
                || x + displayWidth < 0 || x > Constants.SCREEN_WIDTH;
    }

    public int getDamage() {
        return damage;
    }

    public void draw(Graphics2D g) {
        g.drawImage(image, (int) x, (int) y, displayWidth, displayHeight, null);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, displayWidth, displayHeight);
    }
}
