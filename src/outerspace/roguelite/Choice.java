package outerspace.roguelite;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * One selectable option on a {@link ChoiceMenu}: a reward pick, an event
 * decision, a shop item or a rest action. Its action applies the effect and
 * may return a short result text to show the player (or {@code null}).
 * <p>
 * A closing choice ends the menu. A one-shot choice (shop items) keeps the
 * menu open and can be picked only once.
 */
public final class Choice {

    private static final BooleanSupplier ALWAYS = () -> true;

    private final String label;
    private final String detail;
    private final Supplier<String> action;
    private BooleanSupplier enabled = ALWAYS;
    private boolean closes = true;
    private boolean used;

    private Choice(String label, String detail, Supplier<String> action) {
        this.label = label;
        this.detail = detail;
        this.action = action;
    }

    public static Choice of(String label, String detail, Supplier<String> action) {
        return new Choice(label, detail, action);
    }

    /** Only selectable while {@code condition} holds (e.g. enough gold). */
    public Choice requires(BooleanSupplier condition) {
        this.enabled = condition;
        return this;
    }

    /** Picking this keeps the menu open and disables this choice afterwards. */
    public Choice oneShot() {
        this.closes = false;
        return this;
    }

    /** Runs the effect. Returns the result text, or {@code null} for none. */
    public String choose() {
        if (!closes) {
            used = true;
        }
        return action.get();
    }

    public boolean isEnabled() {
        return !used && enabled.getAsBoolean();
    }

    public boolean closes() {
        return closes;
    }

    public boolean isUsed() {
        return used;
    }

    public String getLabel() {
        return label;
    }

    public String getDetail() {
        return detail;
    }
}
