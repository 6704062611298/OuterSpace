package outerspace.enemy;

import java.awt.image.BufferedImage;
import java.util.List;

import outerspace.combat.Bullet;
import outerspace.combat.BulletPattern;
import outerspace.player.Player;
import outerspace.util.Constants;

/**
 * A patrolling shooter. Enters from the top edge, descends to its patrol row,
 * then bounces left↔right and never descends further. Fires an aimed volley at
 * the player on a cooldown, but only once it has reached its patrol row. Its
 * stats decide how many bullets fan out per volley.
 */
public class ShootingEnemy extends Enemy {

    private static final double VOLLEY_SPREAD_DEG = 14;

    private final BufferedImage bulletImage;
    private final int patrolY;
    private int direction = 1;
    private long lastShotTime;

    public ShootingEnemy(int x, int patrolY, BufferedImage image, BufferedImage bulletImage, EnemyStats stats) {
        super(x, 0, image, stats);
        this.bulletImage = bulletImage;
        this.patrolY = patrolY;
        this.y = -displayHeight; // enter from above the top edge
        // Stagger the first volley so a wave doesn't fire in unison.
        this.lastShotTime = System.currentTimeMillis() - (long) (Math.random() * stats.getFireIntervalMs() / 2);
    }

    @Override
    public void update(Player player) {
        int speed = stats.getSpeed();
        if (y < patrolY) {
            // Descend into position.
            y += speed;
            if (y > patrolY) {
                y = patrolY;
            }
            return;
        }
        // Patrol horizontally, bounce at the screen edges.
        x += direction * speed;
        if (x < 0) {
            x = 0;
            direction = 1;
        } else if (x + displayWidth > Constants.SCREEN_WIDTH) {
            x = Constants.SCREEN_WIDTH - displayWidth;
            direction = -1;
        }
    }

    @Override
    public void fire(Player player, long now, List<Bullet> out) {
        // Only fire once in position; no shooting while sliding in.
        if (y < patrolY) {
            return;
        }
        if (now - lastShotTime < stats.getFireIntervalMs()) {
            return;
        }
        lastShotTime = now;
        BulletPattern.aimed(out, bulletImage, getCenterX(), getBottomY(),
                player.getCenterX(), player.getCenterY(),
                stats.getShots(), VOLLEY_SPREAD_DEG, stats.getBulletSpeed(), stats.getBulletDamage());
    }

    @Override
    public boolean isOffScreen() {
        // Patrols horizontally and never leaves, so it is never off-screen.
        return false;
    }
}
