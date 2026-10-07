package outerspace.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import outerspace.util.Constants;

/**
 * Vertical-scrolling parallax background. Three layers drift down the screen at
 * different speeds so the ship appears to fly upward: the background image
 * (slowest, farthest), a far starfield, and a near starfield (fastest).
 * The image is turned into a seamless vertical tile so it loops forever.
 */
public class ParallaxBackground {

    /** The background image as a seamless vertical tile, pre-scaled to the screen width. */
    private final BufferedImage tile;
    private final StarLayer farStars;
    private final StarLayer nearStars;
    private double imageOffset;

    public ParallaxBackground(BufferedImage image) {
        this.tile = buildTile(image);
        this.farStars = new StarLayer(Constants.FAR_STAR_COUNT, Constants.FAR_STAR_SPEED,
                1, new Color(255, 255, 255, 110));
        this.nearStars = new StarLayer(Constants.NEAR_STAR_COUNT, Constants.NEAR_STAR_SPEED,
                2, new Color(255, 255, 255, 200));
    }

    public void update() {
        imageOffset = (imageOffset + Constants.BG_SCROLL_SPEED) % tile.getHeight();
        farStars.update();
        nearStars.update();
    }

    public void draw(Graphics2D g2) {
        // Stack copies of the tile, starting just above the screen, until it is covered.
        for (int y = (int) imageOffset - tile.getHeight(); y < Constants.SCREEN_HEIGHT; y += tile.getHeight()) {
            g2.drawImage(tile, 0, y, null);
        }

        farStars.draw(g2);
        nearStars.draw(g2);
    }

    /**
     * Scales the image to the screen width and makes it tile vertically: the top
     * band is crossfaded into the image's bottom rows, and those rows are then
     * cropped off. The last row of the tile now flows straight into its first row,
     * so the loop has no visible seam and the image stays upright.
     */
    private static BufferedImage buildTile(BufferedImage image) {
        int w = Constants.SCREEN_WIDTH;
        int h = (int) Math.round((double) image.getHeight() * w / image.getWidth());
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image, 0, 0, w, h, null);
        g.dispose();

        int band = (int) (h * Constants.BG_SEAM_BLEND);
        int tileH = h - band;
        BufferedImage tile = scaled.getSubimage(0, 0, w, tileH);
        BufferedImage out = new BufferedImage(w, tileH, BufferedImage.TYPE_INT_RGB);
        Graphics2D og = out.createGraphics();
        og.drawImage(tile, 0, 0, null);
        og.dispose();

        for (int y = 0; y < band; y++) {
            double t = (double) y / band;  // 0 = fully bottom rows, 1 = fully top rows
            for (int x = 0; x < w; x++) {
                out.setRGB(x, y, blend(scaled.getRGB(x, tileH + y), scaled.getRGB(x, y), t));
            }
        }
        return out;
    }

    private static int blend(int a, int b, double t) {
        int r = (int) (((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int gr = (int) (((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bl = (int) ((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return (r << 16) | (gr << 8) | bl;
    }

    /** A field of square stars that all fall at one speed and wrap to the top. */
    private static final class StarLayer {
        private final double[] xs;
        private final double[] ys;
        private final double speed;
        private final int size;
        private final Color color;

        StarLayer(int count, double speed, int size, Color color) {
            this.xs = new double[count];
            this.ys = new double[count];
            this.speed = speed;
            this.size = size;
            this.color = color;
            for (int i = 0; i < count; i++) {
                xs[i] = Math.random() * Constants.SCREEN_WIDTH;
                ys[i] = Math.random() * Constants.SCREEN_HEIGHT;
            }
        }

        void update() {
            for (int i = 0; i < ys.length; i++) {
                ys[i] += speed;
                if (ys[i] > Constants.SCREEN_HEIGHT) {
                    ys[i] -= Constants.SCREEN_HEIGHT + size;
                    xs[i] = Math.random() * Constants.SCREEN_WIDTH;
                }
            }
        }

        void draw(Graphics2D g2) {
            g2.setColor(color);
            for (int i = 0; i < ys.length; i++) {
                g2.fillRect((int) xs[i], (int) ys[i], size, size);
            }
        }
    }
}
