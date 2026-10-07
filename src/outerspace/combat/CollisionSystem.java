package outerspace.combat;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

import outerspace.enemy.Enemy;
import outerspace.player.Player;

/**
 * Pure collision resolution between entities. Has no state of its own and
 * does not know about the game loop or rendering.
 */
public final class CollisionSystem {

    private CollisionSystem() {
        // no instances
    }

    /**
     * Resolves player bullets vs enemies. Each hit removes the bullet and
     * damages the enemy; enemies at 0 HP are removed. Returns the score gained
     * from destroyed enemies.
     */
    public static int handleBulletEnemy(List<Bullet> bullets, List<Enemy> enemies) {
        int scoreGained = 0;

        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet bullet = bulletIt.next();
            Rectangle bulletBounds = bullet.getBounds();

            Iterator<Enemy> enemyIt = enemies.iterator();
            while (enemyIt.hasNext()) {
                Enemy enemy = enemyIt.next();
                if (bulletBounds.intersects(enemy.getBounds())) {
                    bulletIt.remove();
                    if (enemy.takeDamage(bullet.getDamage())) {
                        enemyIt.remove();
                        scoreGained += enemy.getScoreValue();
                    }
                    break;
                }
            }
        }
        return scoreGained;
    }

    /**
     * Resolves enemies vs the player. Ordinary enemies are destroyed on contact;
     * enemies that survive contact (bosses) hit again only after a cooldown.
     */
    public static void handleEnemyPlayer(List<Enemy> enemies, Player player, long now) {
        Rectangle playerBounds = player.getBounds();

        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();
            if (!playerBounds.intersects(enemy.getBounds())) {
                continue;
            }
            if (enemy.diesOnContact()) {
                player.takeDamage(enemy.getContactDamage());
                enemyIt.remove();
            } else if (enemy.tryContactHit(now)) {
                player.takeDamage(enemy.getContactDamage());
            }
        }
    }

    /** Resolves enemy bullets vs the player. On hit the bullet is removed. */
    public static void handleEnemyBulletPlayer(List<Bullet> enemyBullets, Player player) {
        Rectangle playerBounds = player.getBounds();

        Iterator<Bullet> it = enemyBullets.iterator();
        while (it.hasNext()) {
            Bullet bullet = it.next();
            if (playerBounds.intersects(bullet.getBounds())) {
                player.takeDamage(bullet.getDamage());
                it.remove();
            }
        }
    }
}
