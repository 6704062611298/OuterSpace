package outerspace.enemy;

import java.awt.image.BufferedImage;

import outerspace.combat.Bullet;
import outerspace.player.Player;
import outerspace.util.Constants;

/**
 * A patrolling shooter. Enters from the top edge, descends to its patrol row,
 * then bounces left↔right and never descends further. Fires an aimed bullet at
 * the player on a cooldown, but only once it has reached its patrol row.
 */
public class ShootingEnemy extends Enemy {

    private final BufferedImage bulletImage;
    private final int patrolY;
    private int direction = 1;
    private long lastShotTime;

    public ShootingEnemy(int x, int patrolY, BufferedImage image, BufferedImage bulletImage) {
        super(x, 0, image, Constants.ENEMY_DISPLAY_WIDTH);
        this.bulletImage = bulletImage;
        this.patrolY = patrolY;
        this.y = -displayHeight; // enter from above the top edge
    }

    @Override
    public void update(Player player) {
        if (y < patrolY) {
            // Descend into position.
            y += Constants.SHOOTING_ENEMY_SPEED;
            if (y > patrolY) {
                y = patrolY;
            }
            return;
        }
        // Patrol horizontally, bounce at the screen edges.
        x += direction * Constants.SHOOTING_ENEMY_SPEED;
        if (x < 0) {
            x = 0;
            direction = 1;
        } else if (x + displayWidth > Constants.SCREEN_WIDTH) {
            x = Constants.SCREEN_WIDTH - displayWidth;
            direction = -1;
        }
    }

    @Override
    public Bullet fire(Player player, long now) {
        // Only fire once in position; no shooting while sliding in.
        if (y < patrolY) {
            return null;
        }
        if (now - lastShotTime < Constants.ENEMY_FIRE_INTERVAL_MS) {
            return null;
        }
        lastShotTime = now;
        int sx = getCenterX();
        int sy = getBottomY();
        double ddx = player.getCenterX() - sx;
        double ddy = player.getCenterY() - sy;
        double dist = Math.hypot(ddx, ddy);
        if (dist == 0) {
            dist = 1;
        }
        int vx = (int) Math.round((ddx / dist) * Constants.ENEMY_BULLET_SPEED);
        int vy = (int) Math.round((ddy / dist) * Constants.ENEMY_BULLET_SPEED);
        return new Bullet(sx, sy, bulletImage, vx, vy);
    }

    @Override
    public boolean isOffScreen() {
        // Patrols horizontally and never leaves, so it is never off-screen.
        return false;
    }
}
