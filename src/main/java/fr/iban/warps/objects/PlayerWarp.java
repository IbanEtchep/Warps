package fr.iban.warps.objects;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import fr.iban.common.teleport.SLocation;
import fr.iban.warps.utils.HeadUtils;

public class PlayerWarp extends Warp {

	private UUID owner;

	public PlayerWarp(int id, UUID owner, SLocation location, String name, String desc) {
		super(id, location, name, desc);
		this.owner = owner;
	}

	public UUID getOwner() {
		return owner;
	}

	public ItemStack getIcon() {
		if(icon == null) {
			icon = HeadUtils.getPlayerHead(getOwner());
		}
		return icon;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
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
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}
	
	
	
	
}
