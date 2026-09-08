package outerspace.util;

/**
 * Central place for tunable values used across the prototype.
 */
public final class Constants {

    private Constants() {
        // no instances
    }

    // Screen
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    public static final int FPS = 60;

    // Player
    public static final int PLAYER_SPEED = 5;
    public static final int PLAYER_MAX_HP = 100;
    public static final int PLAYER_DISPLAY_WIDTH = 70;

    // Bullet
    public static final int BULLET_SPEED = 8;
    public static final int BULLET_FIRE_INTERVAL_MS = 200;
    public static final int BULLET_DISPLAY_WIDTH = 12;

    // Enemy
    public static final int ENEMY_SPEED = 2;
    public static final int ENEMY_DAMAGE = 25;
    public static final int ENEMY_SPAWN_INTERVAL_MS = 1000;
    public static final int ENEMY_DISPLAY_WIDTH = 55;
    public static final int ENEMY_BULLET_SPEED = 4;
    public static final int ENEMY_BULLET_DAMAGE = 10;
    public static final int ENEMY_FIRE_INTERVAL_MS = 1500;
    public static final int SHOOTING_ENEMY_SPEED = 2;
    public static final int RAMMING_ENEMY_SPEED = 2;

    // Formation / spawn
    public static final int FORMATION_SPACING = 70;
    public static final int SPAWN_MARGIN = 40;
    public static final int SPAWN_TOP_Y = 30;
    public static final int WAVE_COOLDOWN_MS = 1500;

    // Score
    public static final int SCORE_PER_ENEMY = 100;

    // Assets (relative to the project working directory)
    public static final String ASSET_DIR = "Asset";
    public static final String PLAYER_IMAGE = ASSET_DIR + "/PlayerPlaneNormal_1.png";
    public static final String ENEMY_IMAGE = ASSET_DIR + "/Enemy_1.png";
    public static final String RAMMING_IMAGE = ASSET_DIR + "/Enemy_1.png";
    public static final String BULLET_IMAGE = ASSET_DIR + "/Bullet.png";
}
