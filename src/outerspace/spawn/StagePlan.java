package outerspace.spawn;

import outerspace.boss.BossData;
import outerspace.enemy.EnemyStats;

/**
 * Everything one combat stage needs to know: how many waves, how big they get,
 * how fast enemies stream in, the enemy stats to use, and an optional boss that
 * appears after the waves. Built by the act from the node type; read by
 * {@link WaveSpawner}.
 */
public final class StagePlan {

    private final int waves;
    private final int baseWaveSize;
    private final int maxWaveSize;
    private final long spawnIntervalMs;
    private final EnemyStats shooterStats;
    private final EnemyStats rammerStats;
    private final BossData boss;

    public StagePlan(int waves, int baseWaveSize, int maxWaveSize, long spawnIntervalMs,
            EnemyStats shooterStats, EnemyStats rammerStats, BossData boss) {
        this.waves = waves;
        this.baseWaveSize = baseWaveSize;
        this.maxWaveSize = maxWaveSize;
        this.spawnIntervalMs = spawnIntervalMs;
        this.shooterStats = shooterStats;
        this.rammerStats = rammerStats;
        this.boss = boss;
    }

    public int getWaves() {
        return waves;
    }

    /** Enemies in wave {@code wave} (1-based): one more every other wave, capped. */
    public int waveSize(int wave) {
        return Math.min(baseWaveSize + (wave - 1) / 2, maxWaveSize);
    }

    public long getSpawnIntervalMs() {
        return spawnIntervalMs;
    }

    public EnemyStats getShooterStats() {
        return shooterStats;
    }

    public EnemyStats getRammerStats() {
        return rammerStats;
    }

    /** The boss to spawn after the waves, or {@code null} for a normal stage. */
    public BossData getBoss() {
        return boss;
    }
}
