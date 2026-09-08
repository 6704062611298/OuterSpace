package outerspace.enemy;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import outerspace.combat.Bullet;
import outerspace.player.Player;
import outerspace.util.Constants;

/**
 * Base for all enemies. Owns position, size, sprite and rendering. Subclasses
 * define movement in {@link #update(Player)} and firing in
 * {@link #fire(Player, long)}. The sprite is drawn flipped vertically so every
 * enemy faces the player at the bottom of the screen.
 */
public abstract class Enemy {

    protected int x;
    protected int y;
    protected final int displayWidth;
    protected final int displayHeight;
    protected final BufferedImage image;

    protected Enemy(int x, int y, BufferedImage image, int displayWidth) {
        this.image = image;
        double aspect = (double) image.getHeight() / image.getWidth();
        this.displayWidth = displayWidth;
        this.displayHeight = (int) (displayWidth * aspect);
        this.x = x;
        this.y = y;
    }

    /** Per-frame movement. Subclasses decide how to chase or patrol. */
    public abstract void update(Player player);

    /**
     * Returns a bullet to spawn this frame, or {@code null} when the enemy does
     * not fire or is on cooldown. The base class returns {@code null}; only
     * shooting enemies override this.
     */
    public Bullet fire(Player player, long now) {
        return null;
    }

    public boolean isOffScreen() {
        return y > Constants.SCREEN_HEIGHT;
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
