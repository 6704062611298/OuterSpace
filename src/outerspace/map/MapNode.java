package outerspace.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One node on an act map. Knows its floor, its order within the floor, a
 * normalized horizontal layout position (0..1), its type, and its edges to
 * the floor above ({@code next}) and the floor below ({@code prev}).
 */
public final class MapNode {

    private final int floor;
    private final int index;
    private final double x;
    private NodeType type;
    private final List<MapNode> next = new ArrayList<>();
    private final List<MapNode> prev = new ArrayList<>();

    MapNode(int floor, int index, double x) {
        this.floor = floor;
        this.index = index;
        this.x = x;
    }

    void connect(MapNode child) {
        if (!next.contains(child)) {
            next.add(child);
            child.prev.add(this);
        }
    }

    void setType(NodeType type) {
        this.type = type;
    }

    public int getFloor() {
        return floor;
    }

    public int getIndex() {
        return index;
    }

    public double getX() {
        return x;
    }

    public NodeType getType() {
        return type;
    }

    public List<MapNode> getNext() {
        return Collections.unmodifiableList(next);
    }

    public List<MapNode> getPrev() {
        return Collections.unmodifiableList(prev);
    }
}
