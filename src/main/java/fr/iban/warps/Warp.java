package fr.iban.warps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import fr.iban.warps.utils.ChatUtils;

public abstract class Warp {

	private Location location;
	private String name;
	private String desc = "Pas de description.";
	private Map<String, Short> avis = new HashMap<>();
	private List<String> tags = new ArrayList<>();

	private boolean opened = true;
	protected ItemStack icon;


	public Warp(Location location, String name, String desc) {
		this.location = location;
		this.name = name;
		if(desc != null)
			this.desc = desc;
	}

	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
		save();
	}
	public String getName() {
		return ChatUtils.translateColors(name);
	}
	public void setName(String name) {
		this.name = name;
		save();
	}
	public String getDesc() {
		return ChatUtils.translateColors(desc);
	}
	public void setDesc(String desc) {
		this.desc = desc;
		save();
	}
	public Map<String, Short> getAvis() {
		return avis;
	}
	public void setAvis(Map<String, Short> avis) {
		this.avis = avis;
		save();
	}
	
	public int getAvisAmount() {
		return avis.size();
	}
	
	public double getNote() {
		double note = 0;
		if(getAvis().isEmpty()) return note;
		for(short n: getAvis().values()) {
			note += n;
		}
		return note/getAvis().size();
	}

	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean opened) {
		this.opened = opened;
		save();
	}
	
	public List<String> getTags() {
		return tags;
	}
	
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	public abstract ItemStack getIcon();

	protected abstract void save();

}
