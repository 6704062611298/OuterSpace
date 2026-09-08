package outerspace.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.Timer;

import outerspace.combat.Bullet;
import outerspace.combat.CollisionSystem;
import outerspace.enemy.Enemy;
import outerspace.player.Player;
import outerspace.spawn.WaveSpawner;
import outerspace.util.Constants;

/**
 * The playable surface. Owns the game loop (a Swing {@link Timer} at ~60 FPS),
 * input handling, the per-frame update step, and rendering. Update and render
 * are kept separate.
 */
public class GamePanel extends JPanel {

    private final Player player;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Bullet> enemyBullets = new ArrayList<>();
    private final BufferedImage bulletImage;
    private final WaveSpawner spawner;
    private final Set<Integer> keys = new HashSet<>();

    private GameState state;
    private int score;
    private long lastShotTime;

    public GamePanel(BufferedImage playerImg, BufferedImage enemyImg, BufferedImage rammingImg, BufferedImage bulletImg) {
        this.player = new Player(playerImg);
        this.bulletImage = bulletImg;
        this.spawner = new WaveSpawner(enemyImg, rammingImg, bulletImg);
        this.state = GameState.PLAYING;

        setPreferredSize(new Dimension(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        setFocusable(true);

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // unused
            }

            @Override
            public void keyPressed(KeyEvent e) {
                keys.add(e.getKeyCode());
                handleActionKey(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keys.remove(e.getKeyCode());
            }
        });

        Timer loop = new Timer(1000 / Constants.FPS, e -> tick());
        loop.start();
    }

    private void handleActionKey(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
        if (state == GameState.GAME_OVER && e.getKeyCode() == KeyEvent.VK_R) {
            restart();
        }
    }

    private void tick() {
        if (state == GameState.PLAYING) {
            update();
        }
        repaint();
    }

    private void update() {
        // --- Input -> movement ---
        int dx = 0;
        int dy = 0;
        if (keys.contains(KeyEvent.VK_A) || keys.contains(KeyEvent.VK_LEFT)) {
            dx -= 1;
        }
        if (keys.contains(KeyEvent.VK_D) || keys.contains(KeyEvent.VK_RIGHT)) {
            dx += 1;
        }
        if (keys.contains(KeyEvent.VK_W) || keys.contains(KeyEvent.VK_UP)) {
            dy -= 1;
        }
        if (keys.contains(KeyEvent.VK_S) || keys.contains(KeyEvent.VK_DOWN)) {
            dy += 1;
        }
        player.moveBy(dx * Constants.PLAYER_SPEED, dy * Constants.PLAYER_SPEED);

        // --- Shooting (fire-rate limited) ---
        long now = System.currentTimeMillis();
        if (keys.contains(KeyEvent.VK_SPACE) && now - lastShotTime >= Constants.BULLET_FIRE_INTERVAL_MS) {
            bullets.add(new Bullet(player.getCenterX(), player.getTopY(), bulletImage));
            lastShotTime = now;
        }

        // --- Bullets ---
        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet bullet = bulletIt.next();
            bullet.update();
            if (bullet.isOffScreen()) {
                bulletIt.remove();
            }
        }

        // --- Wave spawning (formations, not random scatter) ---
        spawner.update(now, enemies, player);

        // --- Enemies ---
        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();
            enemy.update(player);
            if (enemy.isOffScreen()) {
                enemyIt.remove();
                continue;
            }
            // Shooting enemies fire; rammers return null.
            Bullet fired = enemy.fire(player, now);
            if (fired != null) {
                enemyBullets.add(fired);
            }
        }

        // --- Enemy bullets ---
        Iterator<Bullet> enemyBulletIt = enemyBullets.iterator();
        while (enemyBulletIt.hasNext()) {
            Bullet bullet = enemyBulletIt.next();
            bullet.update();
            if (bullet.isOffScreen()) {
                enemyBulletIt.remove();
            }
        }

        // --- Collisions ---
        score += CollisionSystem.handleBulletEnemy(bullets, enemies);
        CollisionSystem.handleEnemyPlayer(enemies, player);
        CollisionSystem.handleEnemyBulletPlayer(enemyBullets, player);

        // --- Death check ---
        if (player.isDead()) {
            state = GameState.GAME_OVER;
        }
    }

    /** Reset everything for a new run without closing the window. */
    private void restart() {
        player.reset();
        enemies.clear();
        bullets.clear();
        enemyBullets.clear();
        score = 0;
        lastShotTime = 0;
        spawner.reset();
        state = GameState.PLAYING;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // Entities
        for (Bullet bullet : bullets) {
            bullet.draw(g2);
        }
        for (Bullet bullet : enemyBullets) {
            bullet.draw(g2);
        }
        for (Enemy enemy : enemies) {
            enemy.draw(g2);
        }
        player.draw(g2);

        // HUD
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("HP: " + player.getHp(), 10, 25);
        g2.drawString("Score: " + score, 10, 50);

        // Game Over overlay
        if (state == GameState.GAME_OVER) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

            g2.setColor(Color.RED);
            g2.setFont(new Font("SansSerif", Font.BOLD, 48));
            drawCentered(g2, "GAME OVER", Constants.SCREEN_HEIGHT / 2 - 40);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 24));
            drawCentered(g2, "Score: " + score, Constants.SCREEN_HEIGHT / 2 + 10);
            drawCentered(g2, "Press R to Restart", Constants.SCREEN_HEIGHT / 2 + 45);
        }
    }

    private void drawCentered(Graphics2D g2, String text, int y) {
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (Constants.SCREEN_WIDTH - textWidth) / 2, y);
    }
}
