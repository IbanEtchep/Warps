package fr.iban.warps.zmenu;

import fr.iban.warps.WarpsManager;
import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.model.enums.WarpTag;
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
import java.util.Objects;

public class TagFilterButton extends ZButton {

    private final WarpsPlugin plugin;
    private final WarpsManager warpsManager;

    public TagFilterButton(Plugin plugin) {
        this.plugin = (WarpsPlugin) plugin;
        this.warpsManager = this.plugin.getWarpManager();
    }

    @Override
    public ItemStack getCustomItemStack(Player player) {
        WarpMenuData warpMenuData = warpsManager.getMenuData(player);
        ItemStack item = this.getItemStack().build(player, false);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        if (lore == null) return item;

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            for (String choice : getFilterChoices()) {
                String tag = "[" + choice + "]";
                if (line.contains(tag)) {
                    line = (Objects.equals(getFilterName(warpMenuData.getTagFilter()), choice)) ? line.replace(tag, "§a") : line.replace(tag, "");
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
        WarpMenuData warpMenuData = warpsManager.getMenuData(player);
        changeFilter(warpMenuData);
        plugin.getMenuManager().update(player);
    }

    private void changeFilter(WarpMenuData warpMenuData) {
        WarpTag filter = warpMenuData.getTagFilter();
        int index = getFilterChoices().indexOf(getFilterName(filter));
        index++;

        if (index >= getFilterChoices().size()) {
            index = 0;
        }

        String newFilter = getFilterChoices().get(index);
        warpMenuData.setTagFilter(getFilter(newFilter));
    }

    private List<String> getFilterChoices() {
        List<String> choices = new ArrayList<>();
        choices.add("ALL");

        for (WarpTag tag : WarpTag.values()) {
            choices.add(tag.toString());
        }

        return choices;
    }

    private WarpTag getFilter(String filter) {
        if(filter.equals("ALL")) {
            return null;
        }
        return WarpTag.valueOf(filter);
    }

    private String getFilterName(WarpTag filter) {
        if(filter == null) {
            return "ALL";
        }
        return filter.toString();
    }
}
