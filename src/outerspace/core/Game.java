package outerspace.core;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import outerspace.util.Constants;

/**
 * The game window. Loads the three assets, builds the {@link GamePanel},
 * and sizes/positions the frame. No gameplay logic lives here.
 */
public class Game extends JFrame {

    public Game() {
        super("OuterSpace");

        BufferedImage playerImg = loadImage(Constants.PLAYER_IMAGE);
        BufferedImage enemyImg = loadImage(Constants.ENEMY_IMAGE);
        BufferedImage bulletImg = loadImage(Constants.BULLET_IMAGE);

        GamePanel panel = new GamePanel(playerImg, enemyImg, bulletImg);
        add(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        panel.requestFocusInWindow();
    }

    private BufferedImage loadImage(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img == null) {
                throw new RuntimeException("ImageIO returned null for: " + path);
            }
            return img;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load image: " + path, e);
        }
    }
}
