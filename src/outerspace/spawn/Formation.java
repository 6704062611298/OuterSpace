package outerspace.spawn;

import java.util.ArrayList;
import java.util.List;

import outerspace.util.Constants;

/**
 * A spawn formation: a list of relative (dx, dy) offsets around a shared
 * origin. Named factories build the arcade-style patterns. Positions are
 * computed, not random, so enemies never overlap. Use {@link #place} to turn
 * offsets into absolute screen positions, group-clamped so the whole pattern
 * stays on screen with a margin.
 */
public final class Formation {

    private final List<int[]> offsets;

    private Formation(List<int[]> offsets) {
        this.offsets = offsets;
    }

    public List<int[]> getOffsets() {
        return offsets;
    }

    /**
     * Turns this formation's offsets into absolute (x, y) spawn positions,
     * centered horizontally on {@code originX} and starting at {@code originY}.
     * The whole group is shifted if any edge would breach the screen margin.
     */
    public List<int[]> place(int originX, int originY) {
        int margin = Constants.SPAWN_MARGIN;
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (int[] o : offsets) {
            minX = Math.min(minX, o[0]);
            maxX = Math.max(maxX, o[0]);
        }
        // Shift so the leftmost/rightmost enemies respect the margin.
        int leftEdge = originX + minX;
        int rightEdge = originX + maxX + Constants.ENEMY_DISPLAY_WIDTH;
        int shiftX = 0;
        if (leftEdge < margin) {
            shiftX = margin - leftEdge;
        } else if (rightEdge > Constants.SCREEN_WIDTH - margin) {
            shiftX = (Constants.SCREEN_WIDTH - margin) - rightEdge;
        }

        List<int[]> positions = new ArrayList<>(offsets.size());
        for (int[] o : offsets) {
            positions.add(new int[] { originX + o[0] + shiftX, originY + o[1] });
        }
        return positions;
    }

    // --- Named patterns -----------------------------------------------------

    /** A single row of {@code count} enemies, evenly spaced. */
    public static Formation horizontalLine(int count) {
        List<int[]> o = new ArrayList<>();
        int s = Constants.FORMATION_SPACING;
        int startX = -((count - 1) * s) / 2;
        for (int i = 0; i < count; i++) {
            o.add(new int[] { startX + i * s, 0 });
        }
        return new Formation(o);
    }

    /** A V (or chevron) of {@code count} enemies. */
    public static Formation vFormation(int count) {
        List<int[]> o = new ArrayList<>();
        int s = Constants.FORMATION_SPACING;
        int half = (count - 1) / 2;
        for (int i = 0; i < count; i++) {
            int depth = Math.abs(i - half);
            o.add(new int[] { (i - half) * s, depth * (s / 2) });
        }
        return new Formation(o);
    }

    /** A diamond ring of enemies. */
    public static Formation diamond() {
        int s = Constants.FORMATION_SPACING;
        List<int[]> o = new ArrayList<>();
        o.add(new int[] { 0, 0 });
        o.add(new int[] { s, s / 2 });
        o.add(new int[] { -s, s / 2 });
        o.add(new int[] { 0, s });
        o.add(new int[] { 0, -s });
        o.add(new int[] { s, -s / 2 });
        o.add(new int[] { -s, -s / 2 });
        return new Formation(o);
    }

    /** A compact {@code rows}x{@code cols} grid. */
    public static Formation grid(int rows, int cols) {
        List<int[]> o = new ArrayList<>();
        int s = Constants.FORMATION_SPACING;
        int startX = -((cols - 1) * s) / 2;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                o.add(new int[] { startX + c * s, r * s });
            }
        }
        return new Formation(o);
    }

    /** A diagonal line of {@code count} enemies, top-left to bottom-right. */
    public static Formation diagonal(int count) {
        List<int[]> o = new ArrayList<>();
        int s = Constants.FORMATION_SPACING;
        int startX = -((count - 1) * s) / 2;
        for (int i = 0; i < count; i++) {
            o.add(new int[] { startX + i * s, i * (s / 2) });
        }
        return new Formation(o);
    }

    /** A fan that spreads outward from a top point. */
    public static Formation fan(int count) {
        List<int[]> o = new ArrayList<>();
        int s = Constants.FORMATION_SPACING;
        int half = (count - 1) / 2;
        for (int i = 0; i < count; i++) {
            int spread = (i - half) * s;
            int depth = Math.abs(i - half) * (s / 2);
            o.add(new int[] { spread, depth });
        }
        return new Formation(o);
    }

    /** A sine-ish wave of {@code count} enemies. */
    public static Formation wave(int count) {
        List<int[]> o = new ArrayList<>();
        int s = Constants.FORMATION_SPACING;
        int startX = -((count - 1) * s) / 2;
        for (int i = 0; i < count; i++) {
            int dy = (int) (Math.sin(i * Math.PI / 2.0) * (s / 2));
            o.add(new int[] { startX + i * s, dy });
        }
        return new Formation(o);
    }
}
