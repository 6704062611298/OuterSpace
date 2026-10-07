package outerspace.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A generated act map: floors of nodes from the bottom (floor 0) upward, plus
 * the boss node above the last floor. Built by {@link MapGenerator}; read-only
 * for everything else.
 */
public final class LevelData {

    private final long seed;
    private final List<List<MapNode>> floors;
    private final MapNode boss;

    LevelData(long seed, List<List<MapNode>> floors, MapNode boss) {
        this.seed = seed;
        this.floors = floors;
        this.boss = boss;
    }

    public long getSeed() {
        return seed;
    }

    /** Number of regular floors (the boss sits above them). */
    public int getFloorCount() {
        return floors.size();
    }

    public List<MapNode> getFloor(int floor) {
        return Collections.unmodifiableList(floors.get(floor));
    }

    public MapNode getBoss() {
        return boss;
    }

    /** Every node, bottom floor first, boss last. */
    public List<MapNode> allNodes() {
        List<MapNode> all = new ArrayList<>();
        for (List<MapNode> floor : floors) {
            all.addAll(floor);
        }
        all.add(boss);
        return all;
    }
}
