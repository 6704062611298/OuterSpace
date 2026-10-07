package outerspace.map;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The player's progress through one act map: where they are, where they have
 * been, which nodes they may enter next, and how far ahead they can see.
 * Before the first move the player stands at START, below floor 0.
 */
public final class PathManager {

    private final LevelData map;
    private final int visionFloors;
    private final List<MapNode> visited = new ArrayList<>();
    private final Set<MapNode> reachable = new HashSet<>();
    private MapNode current;

    public PathManager(LevelData map, int visionFloors) {
        this.map = map;
        this.visionFloors = visionFloors;
        reachable.addAll(map.allNodes());
    }

    /** Nodes the player may enter now, ordered left to right. */
    public List<MapNode> getAvailable() {
        List<MapNode> options = new ArrayList<>(current == null ? map.getFloor(0) : current.getNext());
        options.sort(Comparator.comparingDouble(MapNode::getX));
        return options;
    }

    public boolean canEnter(MapNode node) {
        return node != null && getAvailable().contains(node);
    }

    public void enter(MapNode node) {
        if (!canEnter(node)) {
            throw new IllegalArgumentException("Node is not connected to the current position");
        }
        current = node;
        visited.add(node);
        reachable.clear();
        Deque<MapNode> queue = new ArrayDeque<>(node.getNext());
        while (!queue.isEmpty()) {
            MapNode next = queue.poll();
            if (reachable.add(next)) {
                queue.addAll(next.getNext());
            }
        }
    }

    /** The node the player is on, or {@code null} at START. */
    public MapNode getCurrent() {
        return current;
    }

    /** Floor of the current node; -1 at START. */
    public int getCurrentFloor() {
        return current == null ? -1 : current.getFloor();
    }

    public boolean isVisited(MapNode node) {
        return visited.contains(node);
    }

    /** True if the node can still be reached from the current position. */
    public boolean isReachable(MapNode node) {
        return reachable.contains(node);
    }

    /** True if the node's type is visible: within vision range, already visited, or the boss. */
    public boolean isRevealed(MapNode node) {
        return node.getType() == NodeType.BOSS
                || visited.contains(node)
                || node.getFloor() <= getCurrentFloor() + visionFloors;
    }

    /** True if the player travelled directly from {@code from} to {@code to}; {@code from == null} means START. */
    public boolean isTraveled(MapNode from, MapNode to) {
        int i = visited.indexOf(to);
        if (i < 0) {
            return false;
        }
        return i == 0 ? from == null : visited.get(i - 1) == from;
    }

    public List<MapNode> getVisited() {
        return Collections.unmodifiableList(visited);
    }

    public LevelData getMap() {
        return map;
    }
}
