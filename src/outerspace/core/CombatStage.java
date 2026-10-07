package outerspace.core;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import outerspace.boss.Boss;
import outerspace.combat.Bullet;
import outerspace.combat.BulletPattern;
import outerspace.combat.CollisionSystem;
import outerspace.enemy.Enemy;
import outerspace.player.Player;
import outerspace.player.PlayerStats;
import outerspace.spawn.StagePlan;
import outerspace.spawn.WaveSpawner;

/**
 * One shoot-em-up stage: the player, enemies, bullets, spawning and
 * collisions. Started from a {@link StagePlan}; reports when it is cleared or
 * the player has died. Input arrives as a movement direction and a fire flag,
 * so the stage knows nothing about keys.
 */
public class CombatStage {

    private static final double SHOT_SPREAD_DEG = 8;

    private final Player player;
    private final BufferedImage bulletImage;
    private final WaveSpawner spawner;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Bullet> enemyBullets = new ArrayList<>();
    private long lastShotTime;
    private int scoreGained;

    public CombatStage(Player player, BufferedImage enemyImg, BufferedImage rammingImg,
            BufferedImage bulletImg, BufferedImage bossImg) {
        this.player = player;
        this.bulletImage = bulletImg;
        this.spawner = new WaveSpawner(enemyImg, rammingImg, bulletImg, bossImg);
    }

    public void start(StagePlan plan) {
        enemies.clear();
        bullets.clear();
        enemyBullets.clear();
        lastShotTime = 0;
        scoreGained = 0;
        player.resetPosition();
        spawner.start(plan, System.currentTimeMillis());
    }

    /** One frame. {@code dx}/{@code dy} are -1, 0 or 1. */
    public void update(int dx, int dy, boolean firing) {
        PlayerStats stats = player.getStats();
        player.moveBy(dx * stats.getSpeed(), dy * stats.getSpeed());

        // --- Shooting (fire-rate limited) ---
        long now = System.currentTimeMillis();
        if (firing && now - lastShotTime >= stats.getFireIntervalMs()) {
            BulletPattern.fan(bullets, bulletImage, player.getCenterX(), player.getTopY(), -Math.PI / 2,
                    stats.getShots(), SHOT_SPREAD_DEG, stats.getBulletSpeed(), stats.getDamage());
            lastShotTime = now;
        }

        // --- Bullets ---
        updateBullets(bullets);

        // --- Wave spawning ---
        spawner.update(now, enemies);

        // --- Enemies ---
        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();
            enemy.update(player);
            if (enemy.isOffScreen()) {
                enemyIt.remove();
                continue;
            }
            enemy.fire(player, now, enemyBullets);
        }

        // --- Enemy bullets ---
        updateBullets(enemyBullets);

        // --- Collisions ---
        scoreGained += CollisionSystem.handleBulletEnemy(bullets, enemies);
        CollisionSystem.handleEnemyPlayer(enemies, player, now);
        CollisionSystem.handleEnemyBulletPlayer(enemyBullets, player);
    }

    private static void updateBullets(List<Bullet> list) {
        Iterator<Bullet> it = list.iterator();
        while (it.hasNext()) {
            Bullet bullet = it.next();
            bullet.update();
            if (bullet.isOffScreen()) {
                it.remove();
            }
        }
    }

    public boolean isCleared() {
        return spawner.isComplete();
    }

    public boolean isPlayerDead() {
        return player.isDead();
    }

    /** Score earned since the last call; resets the counter. */
    public int takeScore() {
        int gained = scoreGained;
        scoreGained = 0;
        return gained;
    }

    public int getWave() {
        return spawner.getWave();
    }

    /** The boss currently on screen, or {@code null}. */
    public Boss getBoss() {
        for (Enemy enemy : enemies) {
            if (enemy instanceof Boss) {
                return (Boss) enemy;
            }
        }
        return null;
    }

    public void draw(Graphics2D g2) {
        for (Bullet bullet : bullets) {
            bullet.draw(g2);
        }
        for (Enemy enemy : enemies) {
            enemy.draw(g2);
        }
        for (Bullet bullet : enemyBullets) {
            bullet.draw(g2);
        }
        player.draw(g2);
    }
}
