package fr.iban.warps.menu;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.objects.PlayerWarp;
import fr.iban.warps.objects.Warp;
import fr.iban.warps.utils.ItemBuilder;
import fr.iban.warps.utils.SortingType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WarpsMainMenu extends Menu {

    private WarpsManager manager;
    private Map<Integer, PlayerWarp> warpAtSlot = new HashMap<>();
    private List<PlayerWarp> warps;

    private List<PlayerWarp> topTotal;
    private List<PlayerWarp> top30d;
    private List<PlayerWarp> top7d;


    public WarpsMainMenu(Player player, WarpsManager manager, List<PlayerWarp> warps) {
        super(player);
        this.manager = manager;
        this.warps = warps;
    }

    @Override
    public String getMenuName() {
        return "§5Warps des joueurs";
    }

    @Override
    public int getRows() {
        return 5;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();

        if (displayNameEquals(item, "§5§lTop votes")) {
            new WarpsMenu(player, manager, warps, SortingType.ALL).open();
        } else if (displayNameEquals(item, "§5§lTop votes 30 jours")) {
            new WarpsMenu(player, manager, warps, SortingType.MONTH).open();
        } else if (displayNameEquals(item, "§5§lTop votes 7 jours")) {
            new WarpsMenu(player, manager, warps, SortingType.WEEK).open();
        }

        PlayerWarp clickedWarp = warpAtSlot.get(e.getSlot());

        if (clickedWarp == null) return;

        if (e.getClick() == ClickType.LEFT) {
            manager.teleport(player, clickedWarp);
            player.closeInventory();
        }

    }

    private CompletableFuture<Void> sortWarps() {
        return manager.future(() -> {

            topTotal = warps.stream()
                    .sorted(Comparator.comparingInt(warp -> ((Warp) warp).getNote(SortingType.ALL)).reversed())
                    .limit(8)
                    .collect(Collectors.toList());

            top30d = warps.stream()
                    .sorted(Comparator.comparingInt(warp -> ((Warp) warp).getNote(SortingType.MONTH)).reversed())
                    .limit(8)
                    .collect(Collectors.toList());

            top7d = warps.stream()
                    .sorted(Comparator.comparingInt(warp -> ((Warp) warp).getNote(SortingType.WEEK)).reversed())
                    .limit(8)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public void setMenuItems() {
        sortWarps().thenRun(() -> {
            inventory.setItem(0, makeItem(Material.NETHERITE_BLOCK, "§5§lTop votes", "", "§7Cliquez pour ouvrir le classement complet."));
            inventory.setItem(18, makeItem(Material.DIAMOND_BLOCK, "§5§lTop votes 30 jours", "", "§7Cliquez pour ouvrir le classement complet."));
            inventory.setItem(36, makeItem(Material.GOLD_BLOCK, "§5§lTop votes 7 jours", "", "§7Cliquez pour ouvrir le classement complet."));

            inventory.setItem(1, FILLER_GLASS);
            inventory.setItem(19, FILLER_GLASS);
            inventory.setItem(37, FILLER_GLASS);
            inventory.setItem(8, FILLER_GLASS);
            inventory.setItem(26, FILLER_GLASS);
            inventory.setItem(44, FILLER_GLASS);


            Iterator<PlayerWarp> total = topTotal.iterator();
            Iterator<PlayerWarp> month = top30d.iterator();
            Iterator<PlayerWarp> week = top7d.iterator();


            for (int i = 2; i <= 7; i++) {
                int slot = i;
                if (total.hasNext()) {
                    PlayerWarp warp = total.next();
                    warpAtSlot.put(i, warp);
                    inventory.setItem(i, formatItem(warp));
                    getItemAsync(warp, SortingType.ALL)
                            .thenAccept(item -> {
                                inventory.setItem(slot, item);
                            });
                }
            }

            for (int i = 9; i <= 17; i++) {
                inventory.setItem(i, FILLER_GLASS);
            }

            for (int i = 20; i <= 25; i++) {
                int slot = i;
                if (month.hasNext()) {
                    PlayerWarp warp = month.next();
                    warpAtSlot.put(i, warp);
                    inventory.setItem(i, formatItem(warp));
                    getItemAsync(warp, SortingType.MONTH)
                            .thenAccept(item -> {
                                inventory.setItem(slot, item);
                            });
                }
            }

            for (int i = 27; i <= 35; i++) {
                inventory.setItem(i, FILLER_GLASS);
            }

            for (int i = 38; i <= 43; i++) {
                if (week.hasNext()) {
                    int slot = i;
                    PlayerWarp warp = week.next();
                    warpAtSlot.put(i, warp);
                    inventory.setItem(i, formatItem(warp));
                    getItemAsync(warp, SortingType.WEEK)
                            .thenAccept(item -> {
                                inventory.setItem(slot, item);
                            });
                }
            }
        });
    }

    public CompletableFuture<ItemStack> getItemAsync(PlayerWarp warp, SortingType type) {
        return CompletableFuture.supplyAsync(() -> {
            long time = System.currentTimeMillis();

            if (type == SortingType.MONTH) {
                time = 2592000000L;
            } else if (type == SortingType.WEEK) {
                time = 604800000L;
            } else if (type == SortingType.DAY) {
                time = 216000000L;
            }

            ItemStack item = warp.getIcon();

            return new ItemBuilder(item.clone())
                    .setName(warp.getName())
                    .setLore(splitString(warp.getDesc(), 32))
                    .addLore("§8Propriétaire : §7" + (Bukkit.getOfflinePlayer(warp.getOwner()).hasPlayedBefore() ? Bukkit.getOfflinePlayer(warp.getOwner()).getName() : "Inconnu"))
                    .addLore("§8Tags : §7" + (warp.getTags().isEmpty() ? "Aucun" : String.join(", ", warp.getTags())))
                    .addLore("§8J'aimes : §7" + warp.getUpVotesIn(time) + "§4 ❤")
                    .build();
        });

    }

    private ItemStack formatItem(PlayerWarp warp) {
        return new ItemBuilder(Material.PLAYER_HEAD)
                .setName(warp.getName())
                .setLore(splitString(warp.getDesc(), 32))
                .addLore("§8Propriétaire : §7" + (Bukkit.getOfflinePlayer(warp.getOwner()).hasPlayedBefore() ? Bukkit.getOfflinePlayer(warp.getOwner()).getName() : "Inconnu"))
                .addLore("§8Tags : §7" + (warp.getTags().isEmpty() ? "Aucun" : String.join(", ", warp.getTags())))
                .addLore("§8J'aimes : §7--§4 ❤")
                .build();
    }

}
