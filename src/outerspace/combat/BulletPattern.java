package outerspace.combat;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Stateless helpers that emit groups of enemy bullets: an aimed fan and a full
 * ring. Enemies and bosses combine these into their attack patterns.
 */
public final class BulletPattern {

    private BulletPattern() {
        // no instances
    }

    /**
     * {@code count} bullets fanned around the direction from (sx, sy) to the
     * target, {@code spreadDeg} degrees apart. A count of 1 fires straight at it.
     */
    public static void aimed(List<Bullet> out, BufferedImage image, double sx, double sy,
            double targetX, double targetY, int count, double spreadDeg, double speed, int damage) {
        double base = Math.atan2(targetY - sy, targetX - sx);
        fan(out, image, sx, sy, base, count, spreadDeg, speed, damage);
    }

    /** {@code count} bullets fanned around {@code angle} (radians; PI/2 is straight down). */
    public static void fan(List<Bullet> out, BufferedImage image, double sx, double sy,
            double angle, int count, double spreadDeg, double speed, int damage) {
        double step = Math.toRadians(spreadDeg);
        double start = angle - step * (count - 1) / 2.0;
        for (int i = 0; i < count; i++) {
            double a = start + i * step;
            out.add(new Bullet(sx, sy, image, Math.cos(a) * speed, Math.sin(a) * speed, damage));
        }
    }

    /** {@code count} bullets evenly spaced around a full circle, rotated by {@code offset} radians. */
    public static void ring(List<Bullet> out, BufferedImage image, double sx, double sy,
            int count, double offset, double speed, int damage) {
        for (int i = 0; i < count; i++) {
            double a = offset + i * 2 * Math.PI / count;
            out.add(new Bullet(sx, sy, image, Math.cos(a) * speed, Math.sin(a) * speed, damage));
        }
    }
}
