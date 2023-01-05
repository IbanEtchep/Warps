package fr.iban.warps.menu;

import fr.iban.bukkitcore.utils.Head;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.objects.PlayerWarp;
import fr.iban.warps.objects.Warp;
import fr.iban.warps.utils.ItemBuilder;
import fr.iban.warps.utils.SortingTime;
import fr.iban.warps.utils.WarpTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.security.PrivateKey;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainWarpsMenu extends AbstractWarpsMenu {

	private WarpTag tag;
	private boolean onlyFavorites = false;
	private List<PlayerWarp> filteredWarps;

	public MainWarpsMenu(Player player, WarpsManager manager, List<PlayerWarp> warps, SortingTime sortingTime) {
		super(player, manager, warps, sortingTime);
	}


	@Override
	public String getMenuName() {
		if(tag == null) {
			return "§5Warps des joueurs";
		}
		return switch (tag) {
			case FARM -> "§5Farms des joueurs";
			case SHOP -> "§5Shops des joueurs";
			case VILLE -> "§5Villes des joueurs";
		};
	}

	@Override
	public int getRows() {
		return 6;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();
		if(item == null) return;

		if(displayNameEquals(item, "§5§lTop votes")) {
			sortingTime = SortingTime.ALL;
			super.open();
		}else if(displayNameEquals(item, "§5§lTop votes 30 jours")) {
			sortingTime = SortingTime.MONTH;
			super.open();		
		}else if(displayNameEquals(item, "§5§lTop votes 7 jours")) {
			sortingTime = SortingTime.WEEK;
			super.open();		
		}else if(displayNameEquals(item, "§5§lVoir plus")) {
			new WarpsMenu(player, manager, warps, sortingTime).open();
		}else if(displayNameEquals(item, "§5§lWarps aimés")) {
			onlyFavorites = !onlyFavorites;
			super.open();		
		}else if(displayNameEquals(item, "§5§lShops")) {
			if(tag != WarpTag.SHOP) {
				tag = WarpTag.SHOP;
			}else {
				tag = null;
			}
			super.open();
		}else if(displayNameEquals(item, "§5§lFarms")) {
			if(tag != WarpTag.FARM) {
				tag = WarpTag.FARM;
			}else {
				tag = null;
			}
			super.open();		
		}else if(displayNameEquals(item, "§5§lVilles")) {
			if(tag != WarpTag.VILLE) {
				tag = WarpTag.VILLE;
			}else {
				tag = null;
			}
			super.open();		
		}

		PlayerWarp clickedWarp = warpAtSlot.get(e.getSlot());

		if(clickedWarp == null) return;

		if(e.getClick() == ClickType.LEFT) {
			manager.teleport(player, clickedWarp);
			player.closeInventory();
		}

	}

	@Override
	protected Predicate<Warp> getWarpFilterPredicate() {
		if(onlyFavorites) {
			return warp -> warp.getVotes().containsKey(player.getUniqueId().toString());
		}
		if(tag != null) {
			return tag.hasTag();
		}
		return super.getWarpFilterPredicate();
	}

	@Override
	public void setMenuItems() {
		sortWarps().thenRun(() -> Bukkit.getScheduler().runTask(WarpsPlugin.getInstance(), () -> {
			for(int i = 0 ; i < 9 ; i++) {
				inventory.setItem(i, FILLER_GLASS);
			}

			inventory.setItem(45, new ItemBuilder(Material.DIAMOND_BLOCK).setDisplayName("§5§lTop votes").addLore("§7Cliquez pour afficher le").addLore("§7classement total.").setGlow(sortingTime == SortingTime.ALL).build());
			inventory.setItem(46, new ItemBuilder(Material.GOLD_BLOCK).setDisplayName("§5§lTop votes 30 jours").addLore("§7Cliquez pour afficher le").addLore("§7classement 30 jours.").setGlow(sortingTime == SortingTime.MONTH).build());
			inventory.setItem(47, new ItemBuilder(Material.IRON_BLOCK).setDisplayName("§5§lTop votes 7 jours").addLore("§7Cliquez pour afficher le").addLore("§7 7 jours.").setGlow(sortingTime == SortingTime.WEEK).build());



			inventory.setItem(50, new ItemBuilder(Head.getByID("3133")).setDisplayName("§5§lWarps aimés").addLore("§7Cliquez pour (dé)selectionner le filtre.").build());
			inventory.setItem(51, new ItemBuilder(Head.CHEST.get()).setDisplayName("§5§lShops").addLore("§7Cliquez pour (dé)selectionner le filtre.").build());
			inventory.setItem(52, new ItemBuilder(Head.FARMER_STEVE.get()).setDisplayName("§5§lFarms").addLore("§7Cliquez pour (dé)selectionner le filtre.").build());
			inventory.setItem(53, new ItemBuilder(Head.HOUSE.get()).setDisplayName("§5§lVilles").addLore("§7Cliquez pour (dé)selectionner le filtre.").build());


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


			for(int i = 27 ; i < 36 ; i++) {
				inventory.setItem(i, FILLER_GLASS);
			}

			for(int i = 36 ; i <= 44 ; i++) {
				inventory.setItem(i, FILLER_GLASS);
			}

			inventory.setItem(31, new ItemBuilder(Head.OAK_PLUS.get()).setDisplayName("§5§lVoir plus").addLore("§dClic pour voir le").addLore("§dreste du classment.").build());

			for(PlayerWarp warp : filteredWarps) {
				int slot = inventory.firstEmpty();
				if(slot == -1 || warp == null) break;
				warpAtSlot.put(slot, warp);
				inventory.setItem(slot, formatItem(warp));
				getItemAsync(warp, sortingTime)
						.thenAccept(item -> inventory.setItem(slot, item));
			}
		}));
	}

	@Override
	protected CompletableFuture<Void> sortWarps() {
		return manager.future(() -> {

			filteredWarps = warps.stream()
					.filter(getWarpFilterPredicate() != null ? getWarpFilterPredicate() : w -> true)
					.collect(Collectors.toList());

			filteredWarps = filteredWarps.stream()
					.sorted(getWarpSortComparator().reversed())
					.collect(Collectors.toList());
		});
	}

}
