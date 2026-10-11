package outerspace.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import outerspace.util.Constants;

/**
 * Ambient effects for the main menu: twinkling dust drifting upward, the odd
 * shooting star, and sparks rising off the selected button. Advanced once per
 * frame by {@link #update()}; purely decorative.
 */
class MenuParticles {

    private static final int DUST_COUNT = 80;
    private static final int SHOOTING_STAR_MIN_FRAMES = 90;
    private static final int SHOOTING_STAR_MAX_FRAMES = 240;

    private final Random random = new Random();
    private final List<Dust> dust = new ArrayList<>();
    private final List<ShootingStar> shootingStars = new ArrayList<>();
    private final List<Spark> sparks = new ArrayList<>();
    private int framesToNextShootingStar = SHOOTING_STAR_MIN_FRAMES;

    MenuParticles() {
        for (int i = 0; i < DUST_COUNT; i++) {
            dust.add(new Dust(random.nextDouble() * Constants.SCREEN_HEIGHT));
        }
    }

    void update() {
        for (Dust d : dust) {
            d.update();
        }

        if (--framesToNextShootingStar <= 0) {
            shootingStars.add(new ShootingStar());
            framesToNextShootingStar = SHOOTING_STAR_MIN_FRAMES
                    + random.nextInt(SHOOTING_STAR_MAX_FRAMES - SHOOTING_STAR_MIN_FRAMES);
        }
        for (Iterator<ShootingStar> it = shootingStars.iterator(); it.hasNext();) {
            if (!it.next().update()) {
                it.remove();
            }
        }
        for (Iterator<Spark> it = sparks.iterator(); it.hasNext();) {
            if (!it.next().update()) {
                it.remove();
            }
        }
    }

    /** Emits a few sparks from the edges of {@code r} (the selected button). */
    void emitSparks(Rectangle r) {
        for (int i = 0; i < 2; i++) {
            double x = r.x + random.nextDouble() * r.width;
            double y = random.nextBoolean() ? r.y : r.y + r.height;
            sparks.add(new Spark(x, y));
        }
    }

    void drawBackLayer(Graphics2D g2) {
        for (Dust d : dust) {
            d.draw(g2);
        }
        Object aa = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (ShootingStar s : shootingStars) {
            s.draw(g2);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
    }

    void drawFrontLayer(Graphics2D g2) {
        for (Spark s : sparks) {
            s.draw(g2);
        }
    }

    private static Color withAlpha(Color c, double alpha) {
        int a = (int) Math.max(0, Math.min(255, alpha * 255));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    /** A speck of space dust that drifts upward and twinkles. */
    private final class Dust {
        double x;
        double y;
        double speed;
        double sway;
        double phase;
        double twinkleSpeed;
        int size;

        Dust(double startY) {
            respawn(startY);
        }

        void respawn(double startY) {
            x = random.nextDouble() * Constants.SCREEN_WIDTH;
            y = startY;
            speed = 0.15 + random.nextDouble() * 0.6;
            sway = random.nextDouble() * Math.PI * 2;
            phase = random.nextDouble() * Math.PI * 2;
            twinkleSpeed = 0.03 + random.nextDouble() * 0.08;
            size = random.nextInt(10) == 0 ? 3 : 1 + random.nextInt(2);
        }

        void update() {
            y -= speed;
            sway += 0.01;
            x += Math.sin(sway) * 0.2;
            phase += twinkleSpeed;
            if (y < -4) {
                respawn(Constants.SCREEN_HEIGHT + 4);
            }
        }

        void draw(Graphics2D g2) {
            double alpha = 0.25 + 0.6 * (0.5 + 0.5 * Math.sin(phase));
            Color base = size == 3 ? UiStyle.ACCENT : Color.WHITE;
            if (size == 3) {
                g2.setColor(withAlpha(base, alpha * 0.25));
                g2.fillRect((int) x - 2, (int) y - 2, size + 4, size + 4);
            }
            g2.setColor(withAlpha(base, alpha));
            g2.fillRect((int) x, (int) y, size, size);
        }
    }

    /** A bright streak crossing the sky diagonally, leaving a fading tail. */
    private final class ShootingStar {
        double x;
        double y;
        final double vx;
        final double vy;
        int life;
        final int maxLife;

        ShootingStar() {
            x = random.nextDouble() * Constants.SCREEN_WIDTH * 1.2;
            y = random.nextDouble() * Constants.SCREEN_HEIGHT * 0.5;
            double speed = 9 + random.nextDouble() * 6;
            double angle = Math.toRadians(140 + random.nextDouble() * 20);  // down and to the left
            vx = Math.cos(angle) * speed;
            vy = Math.sin(angle) * speed;
            maxLife = 35 + random.nextInt(25);
            life = maxLife;
        }

        boolean update() {
            x += vx;
            y += vy;
            return --life > 0;
        }

        void draw(Graphics2D g2) {
            double fade = Math.sin(Math.PI * life / maxLife);  // fade in then out
            float tailX = (float) (x - vx * 6);
            float tailY = (float) (y - vy * 6);
            g2.setPaint(new GradientPaint((float) x, (float) y, withAlpha(Color.WHITE, fade),
                    tailX, tailY, withAlpha(UiStyle.ACCENT, 0)));
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int) x, (int) y, (int) tailX, (int) tailY);
            g2.setColor(withAlpha(Color.WHITE, fade));
            g2.fillOval((int) x - 2, (int) y - 2, 4, 4);
            g2.setStroke(UiStyle.THIN);
        }
    }

    /** A short-lived ember rising from the selected button. */
    private final class Spark {
        double x;
        double y;
        final double vx;
        final double vy;
        int life;
        final int maxLife;

        Spark(double x, double y) {
            this.x = x;
            this.y = y;
            vx = (random.nextDouble() - 0.5) * 0.8;
            vy = -0.4 - random.nextDouble() * 1.2;
            maxLife = 25 + random.nextInt(25);
            life = maxLife;
        }

        boolean update() {
            x += vx;
            y += vy;
            return --life > 0;
        }

        void draw(Graphics2D g2) {
            double t = (double) life / maxLife;
            g2.setColor(withAlpha(UiStyle.ACCENT, t));
            int s = t > 0.5 ? 2 : 1;
            g2.fillRect((int) x, (int) y, s, s);
        }
    }
}
