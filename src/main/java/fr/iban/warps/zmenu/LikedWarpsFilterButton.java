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

public class LikedWarpsFilterButton extends ZButton {

    private final WarpsPlugin plugin;
    private final WarpsManager warpsManager;

    public LikedWarpsFilterButton(Plugin plugin) {
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
                    line = (Objects.equals(getFilterName(warpMenuData.isOnlyFavorites()), choice)) ? line.replace(tag, "§a") : line.replace(tag, "");
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
        warpMenuData.setOnlyFavorites(!warpMenuData.isOnlyFavorites());
        plugin.getMenuManager().update(player);
    }

    private List<String> getFilterChoices() {
        return List.of("TRUE", "FALSE");
    }

    private boolean getFilter(String filter) {
        return filter.equals("TRUE");
    }

    private String getFilterName(boolean filter) {
        return filter ? "TRUE" : "FALSE";
    }
}
