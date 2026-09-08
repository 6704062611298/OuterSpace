package outerspace;

import javax.swing.SwingUtilities;
import outerspace.core.Game;

/**
 * Entry point for the OuterSpace prototype.
 *
 * Run with the project root as the working directory so that the
 * {@code Asset/} folder resolves correctly.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}
