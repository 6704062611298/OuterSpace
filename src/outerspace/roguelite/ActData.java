package outerspace.roguelite;

import java.util.List;

import outerspace.boss.BossData;
import outerspace.enemy.EnemyStats;
import outerspace.map.MapConfig;
import outerspace.map.NodeType;
import outerspace.spawn.StagePlan;
import outerspace.util.Constants;

/**
 * Data for one act: its name, map shape, background, enemy stats (normal and
 * elite), stage sizes, reward scale and boss. Difficulty rises act by act
 * purely through these numbers; add an entry to {@link #ACTS} to add an act.
 */
public final class ActData {

    public static final List<ActData> ACTS = List.of(
            new ActData(1, "Outer Orbit", Constants.ACT1_BACKGROUND,
                    MapConfig.standard(),
                    // hp, contact, bulletDmg, bulletSpd, fireMs, shots, speed, width, score
                    new EnemyStats(1, 25, 10, 4.0, 1500, 1, 2, 55, 100),
                    new EnemyStats(1, 25, 0, 0, 0, 0, 2, 55, 100),
                    new EnemyStats(5, 30, 12, 4.5, 1200, 3, 2, 70, 300),
                    new EnemyStats(4, 35, 0, 0, 0, 0, 3, 70, 300),
                    4, 3, 6, 600,
                    new BossData("SENTINEL", 120, 10, 30, 3.5, 1, 3000)),
            new ActData(2, "Ruined Spires", Constants.ACT2_BACKGROUND,
                    MapConfig.standard().floors(11),
                    new EnemyStats(2, 30, 13, 4.5, 1300, 2, 2, 55, 150),
                    new EnemyStats(2, 30, 0, 0, 0, 0, 3, 55, 150),
                    new EnemyStats(8, 35, 15, 5.0, 1050, 3, 3, 70, 450),
                    new EnemyStats(6, 40, 0, 0, 0, 0, 3, 70, 450),
                    5, 4, 7, 520,
                    new BossData("WARDEN", 200, 13, 35, 4.0, 2, 5000)),
            new ActData(3, "The Core", Constants.ACT3_BACKGROUND,
                    MapConfig.standard().floors(12).nodesPerFloor(2, 5)
                            .weight(NodeType.ELITE, 0.16).count(NodeType.ELITE, 2, 5),
                    new EnemyStats(3, 35, 16, 5.0, 1100, 3, 3, 55, 200),
                    new EnemyStats(3, 35, 0, 0, 0, 0, 3, 55, 200),
                    new EnemyStats(12, 40, 18, 5.5, 900, 5, 3, 70, 600),
                    new EnemyStats(9, 45, 0, 0, 0, 0, 4, 70, 600),
                    6, 5, 8, 450,
                    new BossData("LEVIATHAN", 300, 16, 40, 4.5, 3, 8000)));

    private final int number;
    private final String name;
    private final String background;
    private final MapConfig mapConfig;
    private final EnemyStats shooter;
    private final EnemyStats rammer;
    private final EnemyStats eliteShooter;
    private final EnemyStats eliteRammer;
    private final int battleWaves;
    private final int baseWaveSize;
    private final int maxWaveSize;
    private final long spawnIntervalMs;
    private final BossData boss;

    private ActData(int number, String name, String background, MapConfig mapConfig,
            EnemyStats shooter, EnemyStats rammer, EnemyStats eliteShooter, EnemyStats eliteRammer,
            int battleWaves, int baseWaveSize, int maxWaveSize, long spawnIntervalMs, BossData boss) {
        this.number = number;
        this.name = name;
        this.background = background;
        this.mapConfig = mapConfig;
        this.shooter = shooter;
        this.rammer = rammer;
        this.eliteShooter = eliteShooter;
        this.eliteRammer = eliteRammer;
        this.battleWaves = battleWaves;
        this.baseWaveSize = baseWaveSize;
        this.maxWaveSize = maxWaveSize;
        this.spawnIntervalMs = spawnIntervalMs;
        this.boss = boss;
    }

    /**
     * The combat stage for a node of this act. Elite stages are shorter but
     * use elite stats; boss stages open with one escort wave before the boss.
     */
    public StagePlan stageFor(NodeType type) {
        switch (type) {
            case ELITE:
                return new StagePlan(battleWaves - 1, baseWaveSize - 1, maxWaveSize - 2,
                        spawnIntervalMs + 200, eliteShooter, eliteRammer, null);
            case BOSS:
                return new StagePlan(1, baseWaveSize, baseWaveSize, spawnIntervalMs,
                        shooter, rammer, boss);
            default:
                return new StagePlan(battleWaves, baseWaveSize, maxWaveSize,
                        spawnIntervalMs, shooter, rammer, null);
        }
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getBackground() {
        return background;
    }

    public MapConfig getMapConfig() {
        return mapConfig;
    }

    public BossData getBoss() {
        return boss;
    }
}
