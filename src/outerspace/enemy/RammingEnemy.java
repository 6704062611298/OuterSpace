package outerspace.enemy;

import java.awt.image.BufferedImage;

import outerspace.player.Player;
import outerspace.util.Constants;

/**
 * A weaponless rammer. Enters from a screen edge — left, right, or top — and
 * charges inward/downward toward the player area. Does not fire; its only job
 * is to reach and hit the player.
 */
public class RammingEnemy extends Enemy {

    public enum Entry {
        LEFT,
        RIGHT,
        TOP
    }

    private final Entry entry;

    public RammingEnemy(BufferedImage image, Entry entry) {
        super(0, 0, image, Constants.ENEMY_DISPLAY_WIDTH);
        this.entry = entry;
        switch (entry) {
            case LEFT:
                x = -displayWidth;
                y = 80 + (int) (Math.random() * 120);
                break;
            case RIGHT:
                x = Constants.SCREEN_WIDTH;
                y = 80 + (int) (Math.random() * 120);
                break;
            case TOP:
            default:
                x = (int) (Math.random() * (Constants.SCREEN_WIDTH - displayWidth));
                y = -displayHeight - (int) (Math.random() * 120);
                break;
        }
    }

    @Override
    public void update(Player player) {
        int speed = Constants.RAMMING_ENEMY_SPEED;
        switch (entry) {
            case LEFT:
                // Swoop in down-right.
                x += speed;
                y += speed;
                break;
            case RIGHT:
                // Swoop in down-left.
                x -= speed;
                y += speed;
                break;
            case TOP:
            default:
                // Drop straight down with a slight steer toward the player.
                y += speed;
                int diff = player.getCenterX() - getCenterX();
                int steer = Integer.signum(diff) * Math.min(Math.abs(diff), speed);
                x += steer;
                break;
        }
    }

    @Override
    public boolean isOffScreen() {
        // All entries head inward/downward, so the bottom edge is the exit.
        // Also clean up if one somehow drifts off a side.
        return y > Constants.SCREEN_HEIGHT
                || x + displayWidth < -displayWidth
                || x > Constants.SCREEN_WIDTH + displayWidth;
    }
}
