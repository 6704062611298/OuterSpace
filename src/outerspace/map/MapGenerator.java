package outerspace.map;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Builds act maps as a layered graph, deterministically from a seed.
 *
 * <ol>
 * <li>Floor sizes: floor 0 has {@code startNodes}; each floor above differs from
 * the one below by at most one node, so the map widens and narrows smoothly.</li>
 * <li>Edges: two adjacent floors are stitched with a monotone walk from their
 * leftmost to their rightmost nodes. Every step goes straight up, branches or
 * merges, so every node gets at least one edge in and out and no two edges
 * cross. Optional cross-links are then added where they cross nothing.</li>
 * <li>Every node on the last floor links to the boss.</li>
 * <li>Types: fixed floors are set first; the rest are drawn from a shuffled
 * "bag" sized by the configured shares and min/max counts, subject to rules
 * (earliest floor per type, no repeats along a path, siblings differ).</li>
 * <li>The result is validated; a rejected map is rebuilt with the next
 * seed-derived attempt, so the same seed always yields the same map.</li>
 * </ol>
 */
public final class MapGenerator {

    private static final int MAX_ATTEMPTS = 50;
    private static final int MAX_OUT_EDGES = 3;
    /** Cross-links only join nodes this close horizontally (layout units 0..1). */
    private static final double MAX_LINK_DISTANCE = 0.4;
    /** How far a node may drift from its slot, as a fraction of the slot width. */
    private static final double LAYOUT_JITTER = 0.3;

    private final MapConfig config;

    public MapGenerator(MapConfig config) {
        this.config = config;
    }

    public LevelData generate(long seed) {
        LevelData map = null;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            map = build(seed, new Random(seed + attempt * 0x9E3779B97F4A7C15L));
            if (validate(map, config).isEmpty()) {
                return map;
            }
        }
        // Structure is valid by construction; only soft type quotas can still be off here.
        return map;
    }

    private LevelData build(long seed, Random rng) {
        List<List<MapNode>> floors = new ArrayList<>();
        int count = config.getStartNodes();
        for (int f = 0; f < config.getFloors(); f++) {
            if (f > 0) {
                count = clamp(count + rng.nextInt(3) - 1, config.getMinNodes(), config.getMaxNodes());
            }
            floors.add(createFloor(f, count, rng));
        }
        for (int f = 0; f + 1 < floors.size(); f++) {
            connectFloors(floors.get(f), floors.get(f + 1), rng);
        }
        MapNode boss = new MapNode(floors.size(), 0, 0.5);
        boss.setType(NodeType.BOSS);
        for (MapNode node : floors.get(floors.size() - 1)) {
            node.connect(boss);
        }
        assignTypes(floors, rng);
        return new LevelData(seed, floors, boss);
    }

    private List<MapNode> createFloor(int floor, int count, Random rng) {
        List<MapNode> nodes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double jitter = (rng.nextDouble() - 0.5) * LAYOUT_JITTER / count;
            nodes.add(new MapNode(floor, i, (i + 0.5) / count + jitter));
        }
        return nodes;
    }

    private void connectFloors(List<MapNode> lower, List<MapNode> upper, Random rng) {
        int i = 0;
        int j = 0;
        lower.get(0).connect(upper.get(0));
        while (i < lower.size() - 1 || j < upper.size() - 1) {
            if (i == lower.size() - 1) {
                j++;
            } else if (j == upper.size() - 1) {
                i++;
            } else if (rng.nextDouble() < config.getStraightChance()) {
                i++;
                j++;
            } else if (rng.nextBoolean()) {
                i++;
            } else {
                j++;
            }
            lower.get(i).connect(upper.get(j));
        }

        for (MapNode from : lower) {
            for (MapNode to : upper) {
                if (from.getNext().contains(to)
                        || from.getNext().size() >= MAX_OUT_EDGES
                        || Math.abs(from.getX() - to.getX()) > MAX_LINK_DISTANCE
                        || crossesExisting(lower, from.getIndex(), to.getIndex())) {
                    continue;
                }
                if (rng.nextDouble() < config.getExtraEdgeChance()) {
                    from.connect(to);
                }
            }
        }
    }

    /** True if an edge lower[a] -> upper[b] would cross an existing edge between the floors. */
    private static boolean crossesExisting(List<MapNode> lower, int a, int b) {
        for (MapNode other : lower) {
            for (MapNode child : other.getNext()) {
                int c = other.getIndex();
                int d = child.getIndex();
                if ((c < a && d > b) || (c > a && d < b)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void assignTypes(List<List<MapNode>> floors, Random rng) {
        int freeCount = 0;
        for (List<MapNode> floor : floors) {
            NodeType fixed = config.getFixedType(floor.get(0).getFloor());
            for (MapNode node : floor) {
                if (fixed != null) {
                    node.setType(fixed);
                } else {
                    freeCount++;
                }
            }
        }

        List<NodeType> bag = buildBag(freeCount, rng);
        for (List<MapNode> floor : floors) {
            for (MapNode node : floor) {
                if (node.getType() != null) {
                    continue;
                }
                NodeType type = drawFromBag(node, bag, true, rng);
                if (type == null) {
                    type = drawFromBag(node, bag, false, rng);
                }
                node.setType(type != null ? type : NodeType.BATTLE);
            }
        }
    }

    private List<NodeType> buildBag(int size, Random rng) {
        List<NodeType> bag = new ArrayList<>(size);
        for (Map.Entry<NodeType, Double> entry : config.getWeights().entrySet()) {
            NodeType type = entry.getKey();
            int target = clamp((int) Math.round(size * entry.getValue()),
                    config.getMinCount(type), config.getMaxCount(type));
            for (int i = 0; i < target && bag.size() < size; i++) {
                bag.add(type);
            }
        }
        while (bag.size() < size) {
            bag.add(NodeType.BATTLE);
        }
        Collections.shuffle(bag, rng);
        return bag;
    }

    /**
     * Removes and returns a random bag entry that may be placed on {@code node},
     * or {@code null} if none fits. Picking randomly among the allowed entries
     * (instead of the first one) keeps floor-restricted types from piling up on
     * the first floor where they become legal.
     */
    private NodeType drawFromBag(MapNode node, List<NodeType> bag, boolean strict, Random rng) {
        List<Integer> allowed = new ArrayList<>();
        for (int i = 0; i < bag.size(); i++) {
            if (isAllowed(node, bag.get(i), strict)) {
                allowed.add(i);
            }
        }
        if (allowed.isEmpty()) {
            return null;
        }
        return bag.remove((int) allowed.get(rng.nextInt(allowed.size())));
    }

    /** Placement rules. {@code strict} adds the soft "siblings differ" rule. */
    private boolean isAllowed(MapNode node, NodeType type, boolean strict) {
        if (node.getFloor() < config.getMinFloor(type)) {
            return false;
        }
        if (config.isNoConsecutive(type)) {
            for (MapNode parent : node.getPrev()) {
                if (parent.getType() == type) {
                    return false;
                }
            }
            // Children are only typed yet when they sit on a fixed floor.
            for (MapNode child : node.getNext()) {
                if (child.getType() == type) {
                    return false;
                }
            }
        }
        if (strict) {
            for (MapNode parent : node.getPrev()) {
                for (MapNode sibling : parent.getNext()) {
                    if (sibling != node && sibling.getType() == type) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** Returns every rule the map breaks; an empty list means the map is valid. */
    public static List<String> validate(LevelData map, MapConfig config) {
        List<String> problems = new ArrayList<>();
        List<MapNode> all = map.allNodes();

        // Structure: everything reachable from START, and everything leads to the boss.
        Set<MapNode> fromStart = walk(map.getFloor(0), true);
        Set<MapNode> toBoss = walk(List.of(map.getBoss()), false);
        for (MapNode node : all) {
            if (!fromStart.contains(node)) {
                problems.add("unreachable node " + describe(node));
            }
            if (!toBoss.contains(node)) {
                problems.add("dead end at " + describe(node));
            }
        }

        // Types: fixed floors, earliest floors, quotas, and no forbidden repeats.
        EnumMap<NodeType, Integer> counts = new EnumMap<>(NodeType.class);
        for (MapNode node : all) {
            if (node == map.getBoss()) {
                continue;
            }
            NodeType fixed = config.getFixedType(node.getFloor());
            if (fixed != null) {
                if (node.getType() != fixed) {
                    problems.add("fixed floor type broken at " + describe(node));
                }
                continue;
            }
            counts.merge(node.getType(), 1, Integer::sum);
            if (node.getFloor() < config.getMinFloor(node.getType())) {
                problems.add(node.getType() + " too early at " + describe(node));
            }
            for (MapNode child : node.getNext()) {
                if (child.getType() == node.getType() && config.isNoConsecutive(node.getType())) {
                    problems.add("repeated " + node.getType() + " after " + describe(node));
                }
            }
        }
        for (NodeType type : config.getWeights().keySet()) {
            int count = counts.getOrDefault(type, 0);
            if (count < config.getMinCount(type) || count > config.getMaxCount(type)) {
                problems.add(type + " count " + count + " outside quota");
            }
        }

        // Variety: enough distinct routes, and none that is all plain battles.
        if (countPaths(map) < config.getMinPaths()) {
            problems.add("too few routes");
        }
        if (minSpecialOnAnyPath(map, config) < config.getMinSpecialPerPath()) {
            problems.add("a route has too few special nodes");
        }
        return problems;
    }

    private static Set<MapNode> walk(List<MapNode> sources, boolean forward) {
        Set<MapNode> seen = new HashSet<>(sources);
        Deque<MapNode> queue = new ArrayDeque<>(sources);
        while (!queue.isEmpty()) {
            MapNode node = queue.poll();
            for (MapNode n : forward ? node.getNext() : node.getPrev()) {
                if (seen.add(n)) {
                    queue.add(n);
                }
            }
        }
        return seen;
    }

    /** Number of distinct START-to-BOSS routes, counted top-down. */
    public static long countPaths(LevelData map) {
        Map<MapNode, Long> ways = new HashMap<>();
        ways.put(map.getBoss(), 1L);
        long total = 0;
        for (int f = map.getFloorCount() - 1; f >= 0; f--) {
            for (MapNode node : map.getFloor(f)) {
                long sum = 0;
                for (MapNode child : node.getNext()) {
                    sum += ways.get(child);
                }
                ways.put(node, sum);
                if (f == 0) {
                    total += sum;
                }
            }
        }
        return total;
    }

    /** The smallest number of non-Battle free-floor nodes found on any single route. */
    private static int minSpecialOnAnyPath(LevelData map, MapConfig config) {
        Map<MapNode, Integer> best = new HashMap<>();
        best.put(map.getBoss(), 0);
        int result = Integer.MAX_VALUE;
        for (int f = map.getFloorCount() - 1; f >= 0; f--) {
            for (MapNode node : map.getFloor(f)) {
                int below = Integer.MAX_VALUE;
                for (MapNode child : node.getNext()) {
                    below = Math.min(below, best.get(child));
                }
                boolean special = node.getType() != NodeType.BATTLE && config.getFixedType(f) == null;
                best.put(node, below + (special ? 1 : 0));
                if (f == 0) {
                    result = Math.min(result, best.get(node));
                }
            }
        }
        return result;
    }

    private static String describe(MapNode node) {
        return "floor " + node.getFloor() + " #" + node.getIndex();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
