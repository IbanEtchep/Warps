package fr.iban.warps.menu;

import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.objects.PlayerWarp;
import fr.iban.warps.objects.Warp;
import fr.iban.warps.utils.ItemBuilder;
import fr.iban.warps.utils.SortingTime;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractWarpsMenu extends PaginatedMenu {

    protected final WarpsManager manager;
    protected final Map<Integer, PlayerWarp> warpAtSlot = new HashMap<>();
    protected SortingTime sortingTime;
    protected List<PlayerWarp> warps;

    public AbstractWarpsMenu(Player player, WarpsManager manager, List<PlayerWarp> warps, SortingTime sortingTime) {
        super(player);
        this.manager = manager;
        this.warps = warps;
        this.sortingTime = sortingTime;
    }

    protected Comparator<Warp> getWarpSortComparator() {
        return Comparator.comparingInt(warp -> warp.getVotesIn(sortingTime.getTimeMillis()));
    }

    protected Predicate<Warp> getWarpFilterPredicate() {
        return null;
    }

    protected CompletableFuture<ItemStack> getItemAsync(PlayerWarp warp, SortingTime type) {
        return CompletableFuture.supplyAsync(() -> {
            long time = System.currentTimeMillis();

            if (type == SortingTime.MONTH) {
                time = 2592000000L;
            } else if (type == SortingTime.WEEK) {
                time = 604800000L;
            } else if (type == SortingTime.DAY) {
                time = 216000000L;
            }


            ItemStack item = warp.getIcon();

            return new ItemBuilder(item.clone())
                    .setName(warp.getName())
                    .setLore(splitString(warp.getDesc(), 32))
                    .addLore("§8Propriétaire : §7" + warp.getOwnerName())
                    .addLore("§8Tags : §7" + (warp.getTags().isEmpty() ? "Aucun" : String.join(", ", warp.getTags())))
                    .addLore("§8J'aimes : §7" + warp.getVotesIn(time) + "§4 ❤")
                    .build();
        });

    }

    protected ItemStack formatItem(PlayerWarp warp) {
        return new ItemBuilder(Material.PLAYER_HEAD)
                .setName(warp.getName())
                .setLore(splitString(warp.getDesc(), 32))
                .addLore("§8Propriétaire : §7" + warp.getOwnerName())
                .addLore("§8Tags : §7" + (warp.getTags().isEmpty() ? "Aucun" : String.join(", ", warp.getTags())))
                .addLore("§8J'aimes : §7--§4 ❤")
                .build();
    }


    @Override
    public String getMenuName() {
        return null;
    }

    @Override
    public int getRows() {
        return 0;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

    }

    @Override
    public void setMenuItems() {

    }

    protected CompletableFuture<Void> sortWarps() {
        return manager.future(() -> {

            if(getWarpFilterPredicate() != null) {
                warps = warps.stream()
                        .filter(getWarpFilterPredicate())
                        .collect(Collectors.toList());
            }

            warps = warps.stream()
                    .sorted(getWarpSortComparator().reversed())
                    .collect(Collectors.toList());
        });
    }
}
