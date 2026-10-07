package outerspace.core;

/**
 * High-level state of the game.
 * <ul>
 * <li>MENU: title screen, or the end-of-run summary.</li>
 * <li>MAP: choosing the next node on the act map.</li>
 * <li>PLAYING: a shoot-em-up stage (battle, elite or boss).</li>
 * <li>CHOICE: a reward, event, shop or rest screen.</li>
 * </ul>
 */
public enum GameState {
    MENU,
    MAP,
    PLAYING,
    CHOICE
}
