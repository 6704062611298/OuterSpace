package outerspace.map;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Data that shapes a generated map: its size, how branchy it is, and how node
 * types are distributed. Each act owns one, so acts can differ without any
 * generator changes. Setters return {@code this} for chaining.
 */
public final class MapConfig {

    private int floors;
    private int startNodes;
    private int minNodes;
    private int maxNodes;
    private double straightChance;
    private double extraEdgeChance;
    private int visionFloors;
    private int minPaths;
    private int minSpecialPerPath;
    /** Floor -> forced type. Negative floors count from the top (-1 = last floor). */
    private final Map<Integer, NodeType> fixedFloors = new HashMap<>();
    /**
     * Share of the free (non-fixed-floor) nodes each type should take; BATTLE fills
     * the rest. Min/max counts also apply to free nodes only.
     */
    private final EnumMap<NodeType, Double> weights = new EnumMap<>(NodeType.class);
    private final EnumMap<NodeType, Integer> minCounts = new EnumMap<>(NodeType.class);
    private final EnumMap<NodeType, Integer> maxCounts = new EnumMap<>(NodeType.class);
    private final EnumMap<NodeType, Integer> minFloors = new EnumMap<>(NodeType.class);
    /** Types that may not follow themselves along a path (e.g. no Elite -> Elite). */
    private final EnumSet<NodeType> noConsecutive = EnumSet.noneOf(NodeType.class);

    /** The baseline map; acts start from this and tweak it. */
    public static MapConfig standard() {
        return new MapConfig()
                .floors(10)
                .startNodes(3)
                .nodesPerFloor(2, 4)
                .straightChance(0.45)
                .extraEdgeChance(0.3)
                .visionFloors(4)
                .minPaths(8)
                .minSpecialPerPath(2)
                .fixedFloor(0, NodeType.BATTLE)
                .fixedFloor(-1, NodeType.REST)
                .weight(NodeType.EVENT, 0.22).count(NodeType.EVENT, 2, 7).minFloor(NodeType.EVENT, 1)
                .weight(NodeType.ELITE, 0.12).count(NodeType.ELITE, 1, 4).minFloor(NodeType.ELITE, 3)
                .weight(NodeType.SHOP, 0.08).count(NodeType.SHOP, 1, 2).minFloor(NodeType.SHOP, 2)
                .weight(NodeType.REST, 0.08).count(NodeType.REST, 0, 2).minFloor(NodeType.REST, 3)
                .noConsecutive(NodeType.ELITE, NodeType.SHOP, NodeType.REST);
    }

    public MapConfig floors(int floors) {
        this.floors = floors;
        return this;
    }

    public MapConfig startNodes(int count) {
        this.startNodes = count;
        return this;
    }

    public MapConfig nodesPerFloor(int min, int max) {
        this.minNodes = min;
        this.maxNodes = max;
        return this;
    }

    /** Chance that a connection step goes straight up instead of branching or merging. */
    public MapConfig straightChance(double chance) {
        this.straightChance = chance;
        return this;
    }

    /** Chance of adding each optional cross-link between neighbouring nodes. */
    public MapConfig extraEdgeChance(double chance) {
        this.extraEdgeChance = chance;
        return this;
    }

    /** How many floors ahead of the player show their node type. */
    public MapConfig visionFloors(int floors) {
        this.visionFloors = floors;
        return this;
    }

    /** Fewest distinct START-to-BOSS routes a valid map may have. */
    public MapConfig minPaths(int count) {
        this.minPaths = count;
        return this;
    }

    /** Fewest non-Battle nodes (Event/Elite/Shop/Rest) on any single route, fixed floors excluded. */
    public MapConfig minSpecialPerPath(int count) {
        this.minSpecialPerPath = count;
        return this;
    }

    public MapConfig fixedFloor(int floor, NodeType type) {
        fixedFloors.put(floor, type);
        return this;
    }

    public MapConfig weight(NodeType type, double share) {
        weights.put(type, share);
        return this;
    }

    public MapConfig count(NodeType type, int min, int max) {
        minCounts.put(type, min);
        maxCounts.put(type, max);
        return this;
    }

    public MapConfig minFloor(NodeType type, int floor) {
        minFloors.put(type, floor);
        return this;
    }

    public MapConfig noConsecutive(NodeType... types) {
        Collections.addAll(noConsecutive, types);
        return this;
    }

    public int getFloors() {
        return floors;
    }

    public int getStartNodes() {
        return startNodes;
    }

    public int getMinNodes() {
        return minNodes;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public double getStraightChance() {
        return straightChance;
    }

    public double getExtraEdgeChance() {
        return extraEdgeChance;
    }

    public int getVisionFloors() {
        return visionFloors;
    }

    public int getMinPaths() {
        return minPaths;
    }

    public int getMinSpecialPerPath() {
        return minSpecialPerPath;
    }

    /** The forced type for {@code floor}, or {@code null} if the floor is free. */
    public NodeType getFixedType(int floor) {
        NodeType type = fixedFloors.get(floor);
        return type != null ? type : fixedFloors.get(floor - floors);
    }

    public Map<NodeType, Double> getWeights() {
        return weights;
    }

    public int getMinCount(NodeType type) {
        return minCounts.getOrDefault(type, 0);
    }

    public int getMaxCount(NodeType type) {
        return maxCounts.getOrDefault(type, Integer.MAX_VALUE);
    }

    public int getMinFloor(NodeType type) {
        return minFloors.getOrDefault(type, 0);
    }

    public boolean isNoConsecutive(NodeType type) {
        return noConsecutive.contains(type);
    }
}
