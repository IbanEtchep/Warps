package fr.iban.warps.menu.menus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.iban.warps.PlayerWarp;
import fr.iban.warps.Warps;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.menu.PaginatedMenu;
import fr.iban.warps.utils.ItemBuilder;
import fr.iban.warps.utils.NoteComparator;
import fr.iban.warps.utils.StarRating;

public class WarpsMenu extends PaginatedMenu {
	
	private enum SortingType {
		NOTE_AMOUNT,
		NOTE
	}

	private WarpsManager wm = Warps.getInstance().getWarpManager();
	private Map<Integer, PlayerWarp> warpAtSlot = new HashMap<>();
	private SortingType sortingType = SortingType.NOTE_AMOUNT;
	private String tag;

	public WarpsMenu(Player player) {
		super(player);
	}
	
	public WarpsMenu(Player player, String tag) {
		super(player);
		if(wm.getTags().contains(tag)) {
			this.tag = tag;
		}
	}

	@Override
	public String getMenuName() {
		return "§5Warps des joueurs";
	}

	@Override
	public int getSlots() {
		return 54;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		ItemStack current = e.getCurrentItem();
		if(e.getClickedInventory() == e.getView().getTopInventory()) {
			if(current.getType().toString().toUpperCase().endsWith("GLASS_PANE")) {
				if(current.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "précédent")){
					if (page == 0){
						p.sendMessage("§cVous êtes déjà à la première page.");
					}else{
						page = page - 1;
						super.open();
					}
				}else if(current.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN  + "suivant")){
					if ((index + 1) <= wm.getPlayerWarps().size()){
						page = page + 1;
						super.open();
					}else{
						p.sendMessage("§cVous êtes déjà à la dernière page.");
					}
				}else if(current.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.DARK_RED + "fermer")){
					p.closeInventory();
				}
			}else if(current.getType() == Material.NETHER_STAR && current.getItemMeta().getDisplayName().startsWith("§aTri par : ")) {
				toggleTri();
			}
			PlayerWarp clickedWarp = warpAtSlot.get(e.getSlot());
			
			if(clickedWarp == null) return;
			
			if(e.getClick() == ClickType.LEFT) {
				wm.teleport(p, clickedWarp);
			}
		}
	}

	@Override
	public void setMenuItems() {
		addMenuBorder();
		
		if(sortingType == SortingType.NOTE_AMOUNT) {
	        inventory.setItem(45, makeItem(Material.NETHER_STAR, "§aTri par : §2Nombre d'avis","§7clic pour changer le tri."));
		}else {
	        inventory.setItem(45, makeItem(Material.NETHER_STAR, "§aTri par : §2Note", "§7Clic pour changer le tri."));
		}


		List<PlayerWarp> warps = wm.getPlayerWarps().values().stream()
				.filter(PlayerWarp::isOpened)
				.collect(Collectors.toList());
		
		if(tag != null) {
			warps = warps.stream()
					.filter(w -> w.getTags().contains(tag))
					.collect(Collectors.toList());
		}
		
		warps = warps.stream()
				.sorted((sortingType == SortingType.NOTE_AMOUNT ? Comparator.comparingInt(PlayerWarp::getAvisAmount).reversed() : new NoteComparator().reversed()))
				.collect(Collectors.toList());

		if(warps != null && !warps.isEmpty()) {
			for(int i = 0; i < getMaxItemsPerPage(); i++) {
				index = getMaxItemsPerPage() * page + i;
				if(index >= warps.size()) break;
				if (warps.get(index) != null){
					final int slot = inventory.firstEmpty();
					PlayerWarp warp = warps.get(index);
					warpAtSlot.put(slot, warp);
					inventory.setItem(slot, formatItem(warp));
					warp.getItemAsync()
					.thenAccept(item -> {
						inventory.setItem(slot, item);
					});
				}
			}
		}
	}

	private ItemStack formatItem(PlayerWarp warp) {
		return new ItemBuilder(Material.PLAYER_HEAD)
				.setName(warp.getName())
				.setLore(splitString(warp.getDesc(), 32))
				.addLore("§8Propriétaire : §7" + Bukkit.getOfflinePlayer(warp.getOwner()).getName())
				.addLore("§8Tags : §7" + (warp.getTags().isEmpty() ? "Aucun" : StringUtils.join(warp.getTags(), ", ")))
				.addLore("§8Note : §7" + (warp.getAvis().isEmpty()? "" : StarRating.getStars(warp.getNote())) + " §7" + warp.getAvis().size() + " avis")
				.build();
	}
	

	private List<String> splitString(String msg, int lineSize) {
		List<String> res = new ArrayList<>();

		Pattern p = Pattern.compile("\\b.{1," + (lineSize-1) + "}\\b\\W?");
		Matcher m = p.matcher(msg);

		while(m.find()) {
			res.add("§a" + m.group());
		}
		return res;
	}
	
	public void toggleTri() {
		if(sortingType == SortingType.NOTE_AMOUNT) {
			sortingType = SortingType.NOTE;
		}else {
			sortingType = SortingType.NOTE_AMOUNT;
		}
		super.open();
	}


}
