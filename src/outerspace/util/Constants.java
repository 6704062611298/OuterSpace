package outerspace.util;

/**
 * Central place for tunable values used across the prototype.
 */
public final class Constants {

    private Constants() {
        // no instances
    }

    // Screen
    public static final int SCREEN_WIDTH = 450;   // 9:16 portrait
    public static final int SCREEN_HEIGHT = 800;
    public static final int FPS = 60;

    // Background parallax (scroll speed in pixels per frame, slowest = farthest)
    public static final double BG_SCROLL_SPEED = 0.5;
    public static final double BG_SEAM_BLEND = 0.25;  // fraction of image height crossfaded to hide the loop seam
    public static final double FAR_STAR_SPEED = 1.5;
    public static final double NEAR_STAR_SPEED = 3.5;
    public static final int FAR_STAR_COUNT = 40;
    public static final int NEAR_STAR_COUNT = 15;

    // Player
    public static final int PLAYER_SPEED = 5;
    public static final int PLAYER_MAX_HP = 100;
    public static final int PLAYER_DISPLAY_WIDTH = 70;

    // Bullet
    public static final int BULLET_SPEED = 8;
    public static final int BULLET_FIRE_INTERVAL_MS = 200;
    public static final int BULLET_DISPLAY_WIDTH = 12;

    // Enemy (per-act enemy stats live in roguelite.ActData)
    public static final int ENEMY_DISPLAY_WIDTH = 55;

    // Formation / spawn
    public static final int FORMATION_SPACING = 70;
    public static final int SPAWN_MARGIN = 40;
    public static final int SPAWN_TOP_Y = 30;
    public static final int WAVE_COOLDOWN_MS = 1500;

    // Assets (relative to the project working directory)
    public static final String ASSET_DIR = "Asset";
    public static final String PLAYER_IMAGE = ASSET_DIR + "/PlayerPlaneNormal_1.png";
    public static final String ENEMY_IMAGE = ASSET_DIR + "/Enemy_1.png";
    public static final String RAMMING_IMAGE = ASSET_DIR + "/Enemy_1.png";
    public static final String BULLET_IMAGE = ASSET_DIR + "/Bullet.png";
    // public static final String BULLET_IMAGE = ASSET_DIR + "/Bullet.png";
    public static final String BOSS_IMAGE = ASSET_DIR + "/Enemy_1.png";  // placeholder until a boss sprite exists
    public static final String BACKGROUND_IMAGE = "assets/EarthOrbit.jpg";
}
