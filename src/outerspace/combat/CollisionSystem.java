package outerspace.combat;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

import outerspace.enemy.Enemy;
import outerspace.player.Player;
import outerspace.util.Constants;

/**
 * Pure collision resolution between entities. Has no state of its own and
 * does not know about the game loop or rendering.
 */
public final class CollisionSystem {

    private CollisionSystem() {
        // no instances
    }

    /**
     * Resolves player bullets vs enemies. Returns the score gained from
     * destroyed enemies. On hit, both the bullet and the enemy are removed.
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
                    enemyIt.remove();
                    scoreGained += Constants.SCORE_PER_ENEMY;
                    break;
                }
            }
        }
        return scoreGained;
    }

    /**
     * Resolves enemies vs the player. On contact the enemy is destroyed and
     * the player takes {@link Constants#ENEMY_DAMAGE}.
     */
    public static void handleEnemyPlayer(List<Enemy> enemies, Player player) {
        Rectangle playerBounds = player.getBounds();

        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();
            if (playerBounds.intersects(enemy.getBounds())) {
                player.takeDamage(Constants.ENEMY_DAMAGE);
                enemyIt.remove();
            }
        }
    }
}
