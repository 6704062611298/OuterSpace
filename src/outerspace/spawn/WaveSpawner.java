package outerspace.spawn;

import java.awt.image.BufferedImage;
import java.util.List;

import outerspace.boss.Boss;
import outerspace.enemy.Enemy;
import outerspace.enemy.RammingEnemy;
import outerspace.enemy.ShootingEnemy;
import outerspace.util.Constants;

/**
 * Drives wave-based spawning for one stage, following a {@link StagePlan}.
 * Shooting and ramming enemies are kept on separate waves — they never spawn
 * at the same time. Within a wave, enemies stream in one at a time (staggered)
 * rather than appearing all at once: shooting enemies drop in from the top
 * edge to a patrol row; ramming enemies stream in from the left, right and top
 * edges. The spawner waits for a wave's enemies to clear (plus a cooldown)
 * before the next wave. After the last wave the plan's boss, if any, appears;
 * the stage is complete once everything is cleared.
 */
public class WaveSpawner {

    private final BufferedImage enemyImage;
    private final BufferedImage rammingImage;
    private final BufferedImage bulletImage;
    private final BufferedImage bossImage;

    private StagePlan plan;
    private int wave = 0;
    private boolean waveActive = false;
    private boolean bossSpawned;
    private boolean complete;
    private long cooldownUntil;

    // Staggered spawn state for the active wave.
    private boolean shootingWave;
    private int pendingCount;
    private long nextSpawnAt;
    private int spawnedIndex;

    public WaveSpawner(BufferedImage enemyImage, BufferedImage rammingImage,
            BufferedImage bulletImage, BufferedImage bossImage) {
        this.enemyImage = enemyImage;
        this.rammingImage = rammingImage;
        this.bulletImage = bulletImage;
        this.bossImage = bossImage;
    }

    public int getWave() {
        return wave;
    }

    /** Starts a new stage with the given plan. */
    public void start(StagePlan plan, long now) {
        this.plan = plan;
        wave = 0;
        waveActive = false;
        bossSpawned = false;
        complete = false;
        cooldownUntil = now + Constants.WAVE_COOLDOWN_MS;
        pendingCount = 0;
        spawnedIndex = 0;
        nextSpawnAt = 0;
    }

    /** True once every wave (and the boss) has been spawned and cleared. */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Called every game tick. While a wave is active, streams in its enemies
     * one at a time; once all have spawned and the field is clear, starts the
     * cooldown before the next wave.
     */
    public void update(long now, List<Enemy> enemies) {
        if (complete) {
            return;
        }
        if (waveActive) {
            while (pendingCount > 0 && now >= nextSpawnAt) {
                spawnOne(enemies, spawnedIndex);
                spawnedIndex++;
                pendingCount--;
                nextSpawnAt += plan.getSpawnIntervalMs();
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
        if (wave < plan.getWaves()) {
            startNextWave(now);
            waveActive = true;
        } else if (plan.getBoss() != null && !bossSpawned) {
            enemies.add(new Boss(plan.getBoss(), bossImage, bulletImage));
            bossSpawned = true;
            waveActive = true;
        } else {
            complete = true;
        }
    }

    private void startNextWave(long now) {
        wave++;
        spawnedIndex = 0;
        nextSpawnAt = now;
        // Odd waves shoot from patrol rows; even waves ram in from the edges.
        shootingWave = wave % 2 == 1;
        pendingCount = plan.waveSize(wave);
    }

    private void spawnOne(List<Enemy> enemies, int index) {
        if (shootingWave) {
            int margin = Constants.SPAWN_MARGIN;
            int width = plan.getShooterStats().getDisplayWidth();
            int x = margin + (int) (Math.random() * (Constants.SCREEN_WIDTH - 2 * margin - width));
            // Stagger patrol rows so they don't all land on one line.
            int patrolY = Constants.SPAWN_TOP_Y + (index % 3) * (Constants.FORMATION_SPACING / 2);
            enemies.add(new ShootingEnemy(x, patrolY, enemyImage, bulletImage, plan.getShooterStats()));
        } else {
            RammingEnemy.Entry[] entries = RammingEnemy.Entry.values();
            enemies.add(new RammingEnemy(rammingImage, entries[index % entries.length], plan.getRammerStats()));
        }
    }
}
