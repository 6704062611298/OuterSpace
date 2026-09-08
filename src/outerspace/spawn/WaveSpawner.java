package outerspace.spawn;

import java.awt.image.BufferedImage;
import java.util.List;

import outerspace.enemy.Enemy;
import outerspace.enemy.RammingEnemy;
import outerspace.enemy.ShootingEnemy;
import outerspace.player.Player;
import outerspace.util.Constants;

/**
 * Drives wave-based spawning. Shooting and ramming enemies are kept on
 * separate waves — they never spawn at the same time. Within a wave, enemies
 * stream in one at a time (staggered) rather than appearing all at once:
 * shooting enemies drop in from the top edge to a patrol row; ramming enemies
 * stream in from the left, right and top edges. The spawner waits for a wave's
 * enemies to clear (plus a cooldown) before the next wave. Difficulty scales
 * with the wave number via counts — never via random scatter.
 */
public class WaveSpawner {

    private static final long SPAWN_INTERVAL_MS = 600;

    private final BufferedImage enemyImage;
    private final BufferedImage rammingImage;
    private final BufferedImage bulletImage;

    private int wave = 0;
    private boolean waveActive = false;
    private long cooldownUntil;

    // Staggered spawn state for the active wave.
    private boolean shootingWave;
    private int pendingCount;
    private long nextSpawnAt;
    private int spawnedIndex;

    public WaveSpawner(BufferedImage enemyImage, BufferedImage rammingImage, BufferedImage bulletImage) {
        this.enemyImage = enemyImage;
        this.rammingImage = rammingImage;
        this.bulletImage = bulletImage;
    }

    public int getWave() {
        return wave;
    }

    /** Resets wave progress for a new run. */
    public void reset() {
        wave = 0;
        waveActive = false;
        cooldownUntil = 0;
        pendingCount = 0;
        spawnedIndex = 0;
        nextSpawnAt = 0;
    }

    /**
     * Called every game tick. While a wave is active, streams in its enemies
     * one at a time; once all have spawned and the field is clear, starts the
     * cooldown before the next wave.
     */
    public void update(long now, List<Enemy> enemies, Player player) {
        if (waveActive) {
            while (pendingCount > 0 && now >= nextSpawnAt) {
                spawnOne(enemies, spawnedIndex);
                spawnedIndex++;
                pendingCount--;
                nextSpawnAt += SPAWN_INTERVAL_MS;
            }
            if (pendingCount == 0 && enemies.isEmpty()) {
                waveActive = false;
                cooldownUntil = now + Constants.WAVE_COOLDOWN_MS;
            }
            return;
        }
        if (now < cooldownUntil) {
            return;
        }
        startNextWave(now);
        waveActive = true;
    }

    private void startNextWave(long now) {
        wave++;
        spawnedIndex = 0;
        nextSpawnAt = now;
        if (wave % 2 == 1) {
            // Shooting wave: streams down from the top edge.
            shootingWave = true;
            pendingCount = Math.min(3 + (wave / 2), 8);
        } else {
            // Ramming wave: streams in from the edges.
            shootingWave = false;
            pendingCount = Math.min(3 + (wave / 2), 7);
        }
    }

    private void spawnOne(List<Enemy> enemies, int index) {
        if (shootingWave) {
            int margin = Constants.SPAWN_MARGIN;
            int x = margin + (int) (Math.random() * (Constants.SCREEN_WIDTH - 2 * margin - Constants.ENEMY_DISPLAY_WIDTH));
            // Stagger patrol rows so they don't all land on one line.
            int patrolY = Constants.SPAWN_TOP_Y + (index % 3) * (Constants.FORMATION_SPACING / 2);
            enemies.add(new ShootingEnemy(x, patrolY, enemyImage, bulletImage));
        } else {
            RammingEnemy.Entry[] entries = RammingEnemy.Entry.values();
            enemies.add(new RammingEnemy(rammingImage, entries[index % entries.length]));
        }
    }
}
