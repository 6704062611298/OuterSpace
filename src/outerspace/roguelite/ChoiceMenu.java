package outerspace.roguelite;

import java.util.Collections;
import java.util.List;

/**
 * A screen of choices with a title and body text. Rewards, events, shops and
 * rest sites are all expressed as one of these, so the UI and the input
 * handling are shared. {@code onDone} runs when a closing choice is taken (or
 * after its result text has been acknowledged).
 */
public final class ChoiceMenu {

    private final String title;
    private final String text;
    private final List<Choice> choices;
    private final Runnable onDone;

    public ChoiceMenu(String title, String text, List<Choice> choices, Runnable onDone) {
        this.title = title;
        this.text = text;
        this.choices = choices;
        this.onDone = onDone;
    }

    /** A follow-up screen that shows a result and a single Continue button. */
    public static ChoiceMenu result(String title, String text, Runnable onDone) {
        return new ChoiceMenu(title, text, List.of(Choice.of("Continue", "", () -> null)), onDone);
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public List<Choice> getChoices() {
        return Collections.unmodifiableList(choices);
    }

    public Runnable getOnDone() {
        return onDone;
    }
}
