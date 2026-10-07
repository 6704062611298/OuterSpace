package outerspace.roguelite;

import java.util.List;
import java.util.function.Function;

/**
 * A random event: a title, a short story, and a builder that creates its
 * choices for the current run (so prices and effects can depend on the run).
 * Events with a minimum act only appear from that act onward.
 */
public final class GameEvent {

    private final String title;
    private final String text;
    private final int minAct;
    private final Function<RunState, List<Choice>> choices;

    public GameEvent(String title, String text, int minAct, Function<RunState, List<Choice>> choices) {
        this.title = title;
        this.text = text;
        this.minAct = minAct;
        this.choices = choices;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public int getMinAct() {
        return minAct;
    }

    public List<Choice> buildChoices(RunState run) {
        return choices.apply(run);
    }
}
