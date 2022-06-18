package fr.iban.warps.menu;

import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.objects.PlayerWarp;
import fr.iban.warps.objects.Warp;
import fr.iban.warps.utils.ItemBuilder;
import fr.iban.warps.utils.SortingType;
import jdk.internal.joptsimple.internal.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WarpsMenu extends PaginatedMenu {

	private WarpsManager manager;
	private Map<Integer, PlayerWarp> warpAtSlot = new HashMap<>();
	private SortingType sortingType;
	private List<PlayerWarp> warps;

	public WarpsMenu(Player player, WarpsManager manager, List<PlayerWarp> warps, SortingType sortingType) {
		super(player);
		this.manager = manager;
		this.warps = warps;
		this.sortingType = sortingType;
	}

	@Override
	public int getElementAmount() {
		return warps.size();
	}

	@Override
	public String getMenuName() {
		if(sortingType == SortingType.MONTH) {
			return "§5Classement 30 jours";
		}else if(sortingType == SortingType.WEEK){
			return "§5Classement 7 jours";
		}
		return "§5Classement total";
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		ItemStack current = e.getCurrentItem();

		if(e.getClickedInventory() != e.getView().getTopInventory()) {
			return;
		}

		checkBottonsClick(current, player);

		if(displayNameEquals(current, "§c§lRetour")) {
			player.closeInventory();
			player.performCommand("warps");
		}

		PlayerWarp clickedWarp = warpAtSlot.get(e.getSlot());

		if(clickedWarp == null) return;

		if(e.getClick() == ClickType.LEFT) {
			manager.teleport(p, clickedWarp);
			p.closeInventory();
		}

	}

	@Override
	public void setMenuItems() {
		addMenuBorder();
		sortWarps().thenRun(() -> {
			inventory.setItem(49, makeItem(Material.RED_STAINED_GLASS_PANE, "§c§lRetour"));

			//			if(sortingType == SortingType.MONTH) {
			//				inventory.setItem(45, makeItem(Material.NETHER_STAR, "§aTri par : §2Votes des 30 derniers jours.","§7Cliquez pour changer le tri."));
			//			}else if(sortingType == SortingType.WEEK){
			//				inventory.setItem(45, makeItem(Material.NETHER_STAR, "§aTri par : §2Votes des 7 derniers jours.","§7Cliquez pour changer le tri."));
			//			}else if(sortingType == SortingType.DAY){
			//				inventory.setItem(45, makeItem(Material.NETHER_STAR, "§aTri par : §2Votes des dernières 24h.","§7Cliquez pour changer le tri."));
			//			}else if(sortingType == SortingType.ALL){
			//				inventory.setItem(45, makeItem(Material.NETHER_STAR, "§aTri par : §2Total de votes.","§7Cliquez pour changer le tri."));
			//			}

			if(warps != null && !warps.isEmpty()) {
				for(int i = 0; i < getMaxItemsPerPage(); i++) {
					index = getMaxItemsPerPage() * page + i;
					if(index >= warps.size()) break;
					if (warps.get(index) != null){
						final int slot = inventory.firstEmpty();
						if(slot == -1) break;
						PlayerWarp warp = warps.get(index);
						warpAtSlot.put(slot, warp);
						inventory.setItem(slot, formatItem(warp));
						final int p = page;
						getItemAsync(warp, sortingType)
						.thenAccept(item -> {
							if(page == p) {
								inventory.setItem(slot, item);
							}
						});
					}
				}
			}
		});
	}

	private CompletableFuture<Void> sortWarps() {
		return manager.future(() -> {
			warps = warps.stream()
					.sorted(Comparator.comparingInt(warp -> ((Warp) warp).getNote(sortingType)).reversed())
					.collect(Collectors.toList());
		});
	}

	public CompletableFuture<ItemStack> getItemAsync(PlayerWarp warp, SortingType type){
		return CompletableFuture.supplyAsync(() -> {
			long time = System.currentTimeMillis();

			if(type == SortingType.MONTH) {
				time = 2592000000L;
			}else if(type == SortingType.WEEK) {
				time = 604800000L;
			}else if(type == SortingType.DAY) {
				time = 216000000L;
			}


			ItemStack item = warp.getIcon();

			return new ItemBuilder(item.clone())
					.setName(warp.getName())
					.setLore(splitString(warp.getDesc(), 32))
					.addLore("§8Propriétaire : §7" + Bukkit.getOfflinePlayer(warp.getOwner()).getName())
					.addLore("§8Tags : §7" + (warp.getTags().isEmpty() ? "Aucun" : Strings.join(warp.getTags(), ", ")))
					.addLore("§8J'aimes : §7" + warp.getUpVotesIn(time) + "§4 ❤")
					.build();
		});

	}

	private ItemStack formatItem(PlayerWarp warp) {
		return new ItemBuilder(Material.PLAYER_HEAD)
				.setName(warp.getName())
				.setLore(splitString(warp.getDesc(), 32))
				.addLore("§8Propriétaire : §7" + Bukkit.getOfflinePlayer(warp.getOwner()).getName())
				.addLore("§8Tags : §7" + (warp.getTags().isEmpty() ? "Aucun" : Strings.join(warp.getTags(), ", ")))
				.addLore("§8J'aimes : §7--§4 ❤")
				.build();
	}


	@Override
	public int getRows() {
		return 6;
	}


}
