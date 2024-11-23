package fr.iban.warps.zmenu;

import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.model.enums.SortingTime;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class SortingTimeButton extends ZButton {

    private final WarpsPlugin plugin;
    private final ZMenuManager menuManager;

    public SortingTimeButton(Plugin plugin) {
        this.plugin = (WarpsPlugin) plugin;
        this.menuManager = this.plugin.getMenuManager();
    }

    @Override
    public ItemStack getCustomItemStack(Player player) {
        WarpMenuData warpMenuData = menuManager.getMenuData(player);
        ItemStack item = this.getItemStack().build(player, false);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        if (lore == null) return item;

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            for (String choice : getSortingTimeChoices()) {
                String tag = "[" + choice + "]";
                if (line.contains(tag)) {
                    line = (getSortingTimeName(warpMenuData.getSortingTime()).equals(choice)) ? line.replace(tag, "§a") : line.replace(tag, "");
                    break;
                }
            }
            lore.set(i, line);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        WarpMenuData warpMenuData = menuManager.getMenuData(player);
        changeSortingTime(warpMenuData);
        plugin.getMenuManager().update(player);
    }

    private void changeSortingTime(WarpMenuData warpMenuData) {
        SortingTime currentSortingTime = warpMenuData.getSortingTime();
        int index = getSortingTimeChoices().indexOf(getSortingTimeName(currentSortingTime));
        index++;

        if (index >= getSortingTimeChoices().size()) {
            index = 0;
        }

        String newSortingTime = getSortingTimeChoices().get(index);
        warpMenuData.setSortingTime(getSortingTime(newSortingTime));
    }

    private List<String> getSortingTimeChoices() {
        List<String> choices = new ArrayList<>();
        for (SortingTime sortingTime : SortingTime.values()) {
            choices.add(sortingTime.toString());
        }
        return choices;
    }

    private SortingTime getSortingTime(String sortingTimeStr) {
        return SortingTime.valueOf(sortingTimeStr);
    }

    private String getSortingTimeName(SortingTime sortingTime) {
        return sortingTime.toString();
    }
}
