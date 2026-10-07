package outerspace.shop;

import java.util.ArrayList;
import java.util.List;

import outerspace.roguelite.ActData;
import outerspace.roguelite.Choice;
import outerspace.roguelite.ChoiceMenu;
import outerspace.roguelite.RewardManager;
import outerspace.roguelite.RunState;
import outerspace.roguelite.Upgrade;

/**
 * Builds a shop's stock: a few upgrades and a hull repair, priced up by act.
 * Every purchase goes through {@link RunState#spend}, so the shop never edits
 * gold directly. Each item can be bought once; Leave closes the shop.
 */
public final class Shop {

    private static final int UPGRADE_SLOTS = 3;
    private static final int REPAIR_AMOUNT = 30;
    private static final int REPAIR_PRICE = 30;
    private static final double PRICE_STEP_PER_ACT = 0.15;

    private Shop() {
        // no instances
    }

    public static ChoiceMenu open(RunState run, ActData act, Runnable onDone) {
        double markup = 1 + PRICE_STEP_PER_ACT * (act.getNumber() - 1);
        List<Choice> stock = new ArrayList<>();
        for (Upgrade upgrade : new RewardManager(run).rollUpgrades(UPGRADE_SLOTS, false)) {
            int price = (int) Math.round(upgrade.getPrice() * markup);
            stock.add(Choice.of(upgrade.getName() + "  " + price + "g", upgrade.getDescription(), () -> {
                run.spend(price);
                run.applyUpgrade(upgrade);
                return null;
            }).requires(() -> run.canAfford(price)).oneShot());
        }
        int repairPrice = (int) Math.round(REPAIR_PRICE * markup);
        stock.add(Choice.of("Hull Repair  " + repairPrice + "g", "Restore " + REPAIR_AMOUNT + " HP", () -> {
            run.spend(repairPrice);
            run.getPlayer().heal(REPAIR_AMOUNT);
            return null;
        }).requires(() -> run.canAfford(repairPrice)
                && run.getPlayer().getHp() < run.getPlayer().getMaxHp()).oneShot());
        stock.add(Choice.of("Leave", "Back to the map", () -> null));
        return new ChoiceMenu("TRADING POST", "Gold buys survival out here.", stock, onDone);
    }
}
