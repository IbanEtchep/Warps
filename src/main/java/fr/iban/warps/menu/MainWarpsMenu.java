package fr.iban.warps.menu;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.bukkitcore.utils.Head;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.WarpsPlugin;
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MainWarpsMenu extends Menu {

	private WarpsManager manager;
	private Map<Integer, PlayerWarp> warpAtSlot = new HashMap<>();
	private List<PlayerWarp> warps;
	private List<PlayerWarp> sortedWarps;

	private enum Tag {
		SHOP,
		VILLE,
		FARM,
		NONE
	}

	private Tag tag = Tag.NONE;
	private SortingType sortingType;

	public MainWarpsMenu(Player player, WarpsManager manager, List<PlayerWarp> warps, SortingType sortingType) {
		super(player);
		this.manager = manager;
		this.warps = warps;
		this.sortingType = sortingType;
	}

	@Override
	public String getMenuName() {
		if(tag == Tag.SHOP) {
			return "§5Shops des joueurs";
		}

		if(tag == Tag.FARM) {
			return "§5Farms des joueurs";
		}

		if(tag == Tag.VILLE) {
			return "§5Villes des joueurs";
		}
		return "§5Warps des joueurs";
	}

	@Override
	public int getRows() {
		return 6;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();

		if(displayNameEquals(item, "§5§lTop votes")) {
			sortingType = SortingType.ALL;
			super.open();
		}else if(displayNameEquals(item, "§5§lTop votes 30 jours")) {
			sortingType = SortingType.MONTH;
			super.open();		
		}else if(displayNameEquals(item, "§5§lTop votes 7 jours")) {
			sortingType = SortingType.WEEK;
			super.open();		
		}else if(displayNameEquals(item, "§5§lVoir plus")) {
			new WarpsMenu(player, manager, sortedWarps, sortingType).open();
		}else if(displayNameEquals(item, "§5§lFiltre shops")) {
			tag = Tag.SHOP;
			super.open();		
		}else if(displayNameEquals(item, "§5§lFiltre farms")) {
			tag = Tag.FARM;
			super.open();		
		}else if(displayNameEquals(item, "§5§lFiltre villes")) {
			tag = Tag.VILLE;
			super.open();		
		}

		PlayerWarp clickedWarp = warpAtSlot.get(e.getSlot());

		if(clickedWarp == null) return;

		if(e.getClick() == ClickType.LEFT) {
			manager.teleport(player, clickedWarp);
			player.closeInventory();
		}

	}

	private CompletableFuture<Void> sortWarps() {
		return manager.future(() -> {
			
			sortedWarps = warps;

			if(tag == Tag.SHOP) {
				sortedWarps = sortedWarps.stream().filter(warp -> containsTag(warp, "#shop")).collect(Collectors.toList());
			}

			if(tag == Tag.FARM) {
				sortedWarps = sortedWarps.stream().filter(warp -> containsTag(warp, "#farm")).collect(Collectors.toList());
			}

			if(tag == Tag.VILLE) {
				sortedWarps = sortedWarps.stream().filter(warp -> containsTag(warp, "#ville")).collect(Collectors.toList());
			}

			sortedWarps = sortedWarps.stream()
					.sorted(Comparator.comparingInt(warp -> ((Warp) warp).getNote(sortingType)).reversed())
					.collect(Collectors.toList());
		});
	}

	@Override
	public void setMenuItems() {
		sortWarps().thenRun(() -> {
			Bukkit.getScheduler().runTask(WarpsPlugin.getInstance(), () -> {
				for(int i = 0 ; i < 9 ; i++) {
					inventory.setItem(i, FILLER_GLASS);
				}

				inventory.setItem(45, new ItemBuilder(Material.DIAMOND_BLOCK).setDisplayName("§5§lTop votes").addLore("§7Cliquez pour afficher le").addLore("§7classement total.").setGlow(sortingType == SortingType.ALL).build());
				inventory.setItem(46, new ItemBuilder(Material.GOLD_BLOCK).setDisplayName("§5§lTop votes 30 jours").addLore("§7Cliquez pour afficher le").addLore("§7classement 30 jours.").setGlow(sortingType == SortingType.MONTH).build());
				inventory.setItem(47, new ItemBuilder(Material.IRON_BLOCK).setDisplayName("§5§lTop votes 7 jours").addLore("§7Cliquez pour afficher le").addLore("§7 7 jours.").setGlow(sortingType == SortingType.WEEK).build());



				inventory.setItem(51, new ItemBuilder(Head.CHEST.get()).setDisplayName("§5§lFiltre shops").addLore("§7Cliquez pour (dé)selectionner le filtre.").build());
				inventory.setItem(52, new ItemBuilder(Head.FARMER_STEVE.get()).setDisplayName("§5§lFiltre farms").addLore("§7Cliquez pour (dé)selectionner le filtre.").build());
				inventory.setItem(53, new ItemBuilder(Head.HOUSE.get()).setDisplayName("§5§lFiltre villes").addLore("§7Cliquez pour (dé)selectionner le filtre.").build());


				inventory.setItem(9, FILLER_GLASS);
				inventory.setItem(10, FILLER_GLASS);
				inventory.setItem(11, FILLER_GLASS);
				inventory.setItem(15, FILLER_GLASS);
				inventory.setItem(16, FILLER_GLASS);
				inventory.setItem(17, FILLER_GLASS);
				inventory.setItem(18, FILLER_GLASS);
				inventory.setItem(26, FILLER_GLASS);
				inventory.setItem(48, FILLER_GLASS);
				inventory.setItem(49, FILLER_GLASS);
				inventory.setItem(50, FILLER_GLASS);


				for(int i = 27 ; i < 36 ; i++) {
					inventory.setItem(i, FILLER_GLASS);
				}

				for(int i = 36 ; i <= 44 ; i++) {
					inventory.setItem(i, FILLER_GLASS);
				}

				inventory.setItem(31, new ItemBuilder(Head.OAK_PLUS.get()).setDisplayName("§5§lVoir plus").addLore("§dClic pour voir le").addLore("§dreste du classment.").build());

				for(PlayerWarp warp : sortedWarps) {
					int slot = inventory.firstEmpty();
					if(slot == -1 || warp == null) break;
					warpAtSlot.put(slot, warp);
					inventory.setItem(slot, formatItem(warp));
					getItemAsync(warp, sortingType)
							.thenAccept(item -> {
								inventory.setItem(slot, item);
							});
				}
			});
		});
	}

	public CompletableFuture<ItemStack> getItemAsync(PlayerWarp warp, SortingType type){
		return CompletableFuture.supplyAsync(() -> {
			long time = type.getTime();

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

	private boolean containsTag(Warp warp, String tag) {
		for(String t : warp.getTags()) {
			if(t.equalsIgnoreCase(tag)) {
				return true;
			}
		}
		return false;
	}

}
