package fr.iban.warps.model;

import java.util.UUID;

import com.destroystokyo.paper.profile.PlayerProfile;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.iban.common.teleport.SLocation;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

public class PlayerWarp extends Warp {

	@NotNull
	private final UUID owner;

	public PlayerWarp(int id, @NotNull UUID owner, SLocation location, String name, String desc) {
		super(id, location, name, desc);
		this.owner = owner;
	}

	public @NotNull UUID getOwner() {
		return owner;
	}

	public ItemStack getIcon() {
		if(icon == null) {
			icon = new ItemStack(Material.PLAYER_HEAD, 1);
			SkullMeta sm = (SkullMeta) icon.getItemMeta();
			PlayerProfile profile = Bukkit.createProfile(owner);
			profile.complete(true);
			sm.setPlayerProfile(profile);
			icon.setItemMeta(sm);
		}
		return icon;
	}

	public String getOwnerName() {
		String name = CoreBukkitPlugin.getInstance().getPlayerManager().getOfflinePlayer(owner).getName();
		return name != null ? name : "inconnu";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + owner.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayerWarp other = (PlayerWarp) obj;
		return owner.equals(other.owner);
	}
}
