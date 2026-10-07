package outerspace.boss;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import outerspace.combat.Bullet;
import outerspace.combat.BulletPattern;
import outerspace.enemy.Enemy;
import outerspace.enemy.EnemyStats;
import outerspace.player.Player;
import outerspace.util.Constants;

/**
 * A boss enemy. Descends to its post, then sways side to side and cycles
 * attack patterns. It has three phases (above 70% HP, above 30%, below) that
 * fire faster and add patterns; its {@link BossData#getTier() tier} adds more
 * mechanics on top. It survives ramming, hitting the player again only after
 * a short cooldown.
 */
public class Boss extends Enemy {

    private static final int POST_Y = 105;
    private static final int DISPLAY_WIDTH = 150;
    private static final long CONTACT_COOLDOWN_MS = 800;

    private final BossData data;
    private final BufferedImage bulletImage;
    private final long spawnTime;
    private double ringAngle;
    private long lastPatternTime;
    private long lastStreamTime;
    private int patternStep;
    private long lastContactTime;

    public Boss(BossData data, BufferedImage image, BufferedImage bulletImage) {
        super(0, 0, image, new EnemyStats(data.getHp(), data.getContactDamage(), data.getBulletDamage(),
                data.getBulletSpeed(), 0, 1, 1, DISPLAY_WIDTH, data.getScoreValue()));
        this.data = data;
        this.bulletImage = bulletImage;
        this.x = (Constants.SCREEN_WIDTH - displayWidth) / 2;
        this.y = -displayHeight;
        this.spawnTime = System.currentTimeMillis();
        this.lastPatternTime = spawnTime;
    }

    /** 1, 2 or 3; drops as HP falls below 70% and 30%. */
    public int getPhase() {
        double ratio = (double) getHp() / getMaxHp();
        if (ratio > 0.7) {
            return 1;
        }
        return ratio > 0.3 ? 2 : 3;
    }

    public String getName() {
        return data.getName();
    }

    @Override
    public void update(Player player) {
        if (y < POST_Y) {
            y += 2;
            return;
        }
        double t = (System.currentTimeMillis() - spawnTime) / 1000.0;
        double sway = (Constants.SCREEN_WIDTH - displayWidth) / 2.0;
        x = (int) (sway + Math.sin(t * (0.5 + 0.2 * getPhase())) * (sway - 10));
    }

    @Override
    public void fire(Player player, long now, List<Bullet> out) {
        if (y < POST_Y) {
            return;
        }
        int phase = getPhase();
        int tier = data.getTier();
        double cx = getCenterX();
        double cy = getBottomY() - 20;
        double speed = data.getBulletSpeed();
        int damage = data.getBulletDamage();

        // Tier 2+: a slow spiral stream between the main patterns.
        if (tier >= 2 && phase >= 2 && now - lastStreamTime >= 260) {
            lastStreamTime = now;
            ringAngle += 0.45;
            BulletPattern.fan(out, bulletImage, cx, cy, ringAngle, 1, 0, speed * 0.8, damage);
            BulletPattern.fan(out, bulletImage, cx, cy, ringAngle + Math.PI, 1, 0, speed * 0.8, damage);
        }

        long interval = 1700 - phase * 250L;
        if (now - lastPatternTime < interval) {
            return;
        }
        lastPatternTime = now;
        patternStep++;

        // The pattern cycle grows with phase: aimed fan, downward sweep, ring.
        int patterns = Math.min(phase + 1, 3);
        switch (patternStep % patterns) {
            case 0:
                BulletPattern.aimed(out, bulletImage, cx, cy, player.getCenterX(), player.getCenterY(),
                        1 + phase * 2, 12, speed, damage);
                break;
            case 1:
                BulletPattern.fan(out, bulletImage, cx, cy, Math.PI / 2, 5 + phase * 2, 16, speed * 0.9, damage);
                break;
            default:
                BulletPattern.ring(out, bulletImage, cx, cy, 10 + phase * 3, ringAngle, speed * 0.75, damage);
                break;
        }
        // Tier 3: in the last phase, every ring is followed by a slower, offset ring.
        if (tier >= 3 && phase == 3 && patternStep % patterns == 2) {
            BulletPattern.ring(out, bulletImage, cx, cy, 12, ringAngle + Math.PI / 12, speed * 0.5, damage);
        }
    }

    @Override
    public boolean diesOnContact() {
        return false;
    }

    @Override
    public boolean tryContactHit(long now) {
        if (now - lastContactTime < CONTACT_COOLDOWN_MS) {
            return false;
        }
        lastContactTime = now;
        return true;
    }

    @Override
    public boolean isOffScreen() {
        return false;
    }

    /** The boss bar is drawn by the HUD, so skip the small enemy health bar. */
    @Override
    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image,
                x, y + displayHeight, x + displayWidth, y,
                0, 0, image.getWidth(), image.getHeight(),
                null);
        if (getPhase() == 3 && (System.currentTimeMillis() / 150) % 2 == 0) {
            g.setColor(new Color(255, 255, 255, 40));
            g.fillRect(x, y, displayWidth, displayHeight);
        }
    }
}
