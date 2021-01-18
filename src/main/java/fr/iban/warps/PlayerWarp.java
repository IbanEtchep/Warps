package fr.iban.warps;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import fr.iban.warps.utils.HeadUtils;
import fr.iban.warps.utils.ItemBuilder;
import fr.iban.warps.utils.StarRating;

public class PlayerWarp extends Warp {

	private UUID owner;

	public PlayerWarp(UUID owner, Location location, String name, String desc) {
		super(location, name, desc);
		this.owner = owner;
	}

	public UUID getOwner() {
		return owner;
	}

	@Override
	public void save() {
		Warps.getInstance().getWarpManager().savePlayerWarp(this);
	}

	@Override
	public ItemStack getIcon() {
		if(icon == null) {
			icon = HeadUtils.getPlayerHead(getOwner());
		}
		return icon;
	}
	
	public CompletableFuture<ItemStack> getItemAsync(){
		return CompletableFuture.supplyAsync(() -> {
			
			ItemStack item = getIcon();
			
			return new ItemBuilder(item.clone())
			.setName(getName())
			.setLore(splitString(getDesc(), 32))
			.addLore("§8Propriétaire : §7" + Bukkit.getOfflinePlayer(getOwner()).getName())
			.addLore("§8Tags : §7" + (getTags().isEmpty() ? "Aucun" : StringUtils.join(getTags(), ", ")))
			.addLore("§8Note : §7" + (getAvis().isEmpty()? "" : StarRating.getStars(getNote())) + " §7" + getAvis().size() + " avis")
			.build();
		});
		
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
}
