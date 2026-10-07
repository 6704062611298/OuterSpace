package outerspace.map;

/**
 * The kinds of map node. Each type carries its display name and a one-line
 * hint for the map UI. What happens when a node is entered is decided by the
 * run flow, not here.
 */
public enum NodeType {
    BATTLE("Battle", "Fight a squadron. Gold and an upgrade."),
    ELITE("Elite", "Heavy enemies. Better rewards."),
    EVENT("Event", "An unknown signal. Choose wisely."),
    SHOP("Shop", "Spend gold on upgrades and repairs."),
    REST("Rest", "Repair the hull or tune the ship."),
    BOSS("Boss", "Destroy it to leave the sector.");

    private final String label;
    private final String hint;

    NodeType(String label, String hint) {
        this.label = label;
        this.hint = hint;
    }

    public String getLabel() {
        return label;
    }

    public String getHint() {
        return hint;
    }

    /** True for nodes that are played as a shoot-em-up stage. */
    public boolean isCombat() {
        return this == BATTLE || this == ELITE || this == BOSS;
    }
}
