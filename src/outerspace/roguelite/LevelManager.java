package outerspace.roguelite;

import java.util.List;

import outerspace.map.LevelData;
import outerspace.map.MapGenerator;
import outerspace.map.MapNode;
import outerspace.map.PathManager;

/**
 * Owns the act sequence and the current act's map and path. Each act's map is
 * generated from the run seed and the act number, so a seed always produces
 * the same maps no matter which route the player takes.
 */
public final class LevelManager {

    private final List<ActData> acts;
    private final long runSeed;
    private int actIndex = -1;
    private PathManager path;

    public LevelManager(List<ActData> acts, long runSeed) {
        this.acts = acts;
        this.runSeed = runSeed;
    }

    /** Advances to the next act and generates its map. */
    public void startNextAct() {
        actIndex++;
        ActData act = getAct();
        long mapSeed = runSeed * 31 + act.getNumber();
        LevelData map = new MapGenerator(act.getMapConfig()).generate(mapSeed);
        path = new PathManager(map, act.getMapConfig().getVisionFloors());
    }

    public void enter(MapNode node) {
        path.enter(node);
    }

    public boolean isFinalAct() {
        return actIndex == acts.size() - 1;
    }

    public ActData getAct() {
        return acts.get(actIndex);
    }

    public PathManager getPath() {
        return path;
    }

    public MapNode getCurrentNode() {
        return path.getCurrent();
    }
}
