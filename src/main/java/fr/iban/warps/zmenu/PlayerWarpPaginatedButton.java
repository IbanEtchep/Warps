package fr.iban.warps.zmenu;


import fr.iban.warps.WarpsManager;
import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.model.PlayerWarp;
import fr.iban.warps.model.enums.SortingTime;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class PlayerWarpPaginatedButton extends ZButton implements PaginateButton {

    private final WarpsPlugin plugin;
    private final WarpsManager warpsManager;

    public PlayerWarpPaginatedButton(Plugin plugin) {
        this.plugin = (WarpsPlugin) plugin;
        this.warpsManager = this.plugin.getWarpManager();
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {
        WarpMenuData warpMenuData = warpsManager.getMenuData(player);
        List<PlayerWarp> warps = warpsManager.getOpenedPlayerWarps();

        warps = warps.stream()
                .filter(warpMenuData.getWarpFilterPredicate())
                .sorted(warpMenuData.getWarpSortingComparator())
                .toList();

        Pagination<PlayerWarp> pagination = new Pagination<>();
        List<PlayerWarp> paginatedWarps = pagination.paginate(warps, this.slots.size(), inventory.getPage());

        for (int i = 0; i < paginatedWarps.size(); i++) {
            int slot = this.slots.get(i);
            PlayerWarp playerWarp = paginatedWarps.get(i);

            inventory.addItem(slot, getWarpItem(player, playerWarp)).setLeftClick(event -> {
                plugin.getWarpManager().teleport(player, playerWarp);
                player.closeInventory();
            });
        }
    }

    @Override
    public int getPaginationSize(Player player) {
        var warpMenuData = plugin.getWarpManager().getMenuData(player);

        return plugin.getWarpManager().getOpenedPlayerWarps().stream()
                .filter(warpMenuData.getWarpFilterPredicate())
                .sorted(warpMenuData.getWarpSortingComparator())
                .toList().size();
    }

    protected ItemStack getWarpItem(Player player, PlayerWarp warp) {
        var warpMenuData = plugin.getWarpManager().getMenuData(player);

        SortingTime sortingTime = warpMenuData.getSortingTime();

        Placeholders placeholders = new Placeholders();
        placeholders.register("owner", warp.getOwnerName());
        placeholders.register("name", warp.getName());
        placeholders.register("description", warp.getDesc());
        placeholders.register("tags", warp.getTags().isEmpty() ? "Aucun" : String.join(", ", warp.getTags()));
        placeholders.register("votes", String.valueOf(warp.getVotesIn(sortingTime.getTimeMillis())));

        ItemStack itemstack = this.getItemStack().build(player, false, placeholders);

        ItemStack finalItem = warp.getIcon().clone();
        var meta = finalItem.getItemMeta();
        meta.lore(itemstack.getItemMeta().lore());
        meta.displayName(itemstack.getItemMeta().displayName());
        finalItem.setItemMeta(meta);

        return finalItem;
    }
}
