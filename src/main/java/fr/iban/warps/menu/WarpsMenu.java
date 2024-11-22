package fr.iban.warps.menu;

import fr.iban.warps.WarpsManager;
import fr.iban.warps.model.PlayerWarp;
import fr.iban.warps.model.enums.SortingTime;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WarpsMenu extends AbstractWarpsMenu {


    public WarpsMenu(Player player, WarpsManager manager, List<PlayerWarp> warps, SortingTime sortingTime) {
        super(player, manager, warps, sortingTime);
    }

    @Override
    public int getElementAmount() {
        return warps.size();
    }

    @Override
    public String getMenuName() {
        if (sortingTime == SortingTime.MONTH) {
            return "§5Classement 30 jours";
        } else if (sortingTime == SortingTime.WEEK) {
            return "§5Classement 7 jours";
        }
        return "§5Classement total";
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        ItemStack current = e.getCurrentItem();

        if(current == null) return;

        if (e.getClickedInventory() != e.getView().getTopInventory()) {
            return;
        }
        checkBottonsClick(current, player);

        if (displayNameEquals(current, "§c§lRetour")) {
            player.closeInventory();
            player.performCommand("warps");
        }

        PlayerWarp clickedWarp = warpAtSlot.get(e.getSlot());

        if (clickedWarp == null) return;

        if (e.getClick() == ClickType.LEFT) {
            manager.teleport(p, clickedWarp);
            p.closeInventory();
        }

    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
        sortWarps().thenRun(() -> {
            inventory.setItem(49, makeItem(Material.RED_STAINED_GLASS_PANE, "§c§lRetour"));
            
            if (warps != null && !warps.isEmpty()) {
                for (int i = 0; i < getMaxItemsPerPage(); i++) {
                    index = getMaxItemsPerPage() * page + i;
                    if (index >= warps.size()) break;
                    if (warps.get(index) != null) {
                        final int slot = inventory.firstEmpty();
                        if (slot == -1) break;
                        PlayerWarp warp = warps.get(index);
                        warpAtSlot.put(slot, warp);
                        inventory.setItem(slot, formatItem(warp));
                        final int p = page;
                        getItemAsync(warp, sortingTime)
                                .thenAccept(item -> {
                                    if (page == p) {
                                        inventory.setItem(slot, item);
                                    }
                                });
                    }
                }
            }
        });
    }


    @Override
    public int getRows() {
        return 6;
    }


}
