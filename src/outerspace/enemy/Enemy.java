package outerspace.enemy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import outerspace.combat.Bullet;
import outerspace.player.Player;
import outerspace.util.Constants;

/**
 * Base for all enemies. Owns position, size, sprite, HP and rendering; the
 * numbers come from {@link EnemyStats}. Subclasses define movement in
 * {@link #update(Player)} and firing in {@link #fire(Player, long, List)}.
 * The sprite is drawn flipped vertically so every enemy faces the player at
 * the bottom of the screen.
 */
public abstract class Enemy {

    private static final long HIT_FLASH_MS = 80;

    protected int x;
    protected int y;
    protected final int displayWidth;
    protected final int displayHeight;
    protected final BufferedImage image;
    protected final EnemyStats stats;
    private int hp;
    private long lastHitTime;

    protected Enemy(int x, int y, BufferedImage image, EnemyStats stats) {
        this.image = image;
        this.stats = stats;
        double aspect = (double) image.getHeight() / image.getWidth();
        this.displayWidth = stats.getDisplayWidth();
        this.displayHeight = (int) (displayWidth * aspect);
        this.x = x;
        this.y = y;
        this.hp = stats.getHp();
    }

    /** Per-frame movement. Subclasses decide how to chase or patrol. */
    public abstract void update(Player player);

    /**
     * Adds any bullets fired this frame to {@code out}. The base class never
     * fires; only shooting enemies override this.
     */
    public void fire(Player player, long now, List<Bullet> out) {
        // no weapon
    }

    /** Applies damage. Returns {@code true} if this hit destroyed the enemy. */
    public boolean takeDamage(int amount) {
        hp -= amount;
        lastHitTime = System.currentTimeMillis();
        return hp <= 0;
    }

    /** Ordinary enemies are destroyed when they ram the player. */
    public boolean diesOnContact() {
        return true;
    }

    /**
     * For enemies that survive contact: returns {@code true} if a contact hit
     * may land now (and starts the cooldown). Unused by ordinary enemies.
     */
    public boolean tryContactHit(long now) {
        return false;
    }

    public boolean isOffScreen() {
        return y > Constants.SCREEN_HEIGHT;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return stats.getHp();
    }

    public int getContactDamage() {
        return stats.getContactDamage();
    }

    public int getScoreValue() {
        return stats.getScoreValue();
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

        if (System.currentTimeMillis() - lastHitTime < HIT_FLASH_MS) {
            g.setColor(new Color(255, 255, 255, 90));
            g.fillRect(x, y, displayWidth, displayHeight);
        }
        // Tough enemies show a thin health bar once damaged.
        if (stats.getHp() > 1 && hp < stats.getHp() && hp > 0) {
            g.setColor(new Color(255, 255, 255, 60));
            g.fillRect(x, y - 6, displayWidth, 3);
            g.setColor(Color.WHITE);
            g.fillRect(x, y - 6, displayWidth * hp / stats.getHp(), 3);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, displayWidth, displayHeight);
    }
}
