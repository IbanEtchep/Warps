package fr.iban.warps.utils;

import java.util.UUID;

public class WarpTpMessage {
	
	private UUID playerUUID;
	private UUID warpOwnerUUID;

	public WarpTpMessage(UUID playerUUID, UUID warpOwnerUUID) {
		this.playerUUID = playerUUID;
		this.warpOwnerUUID = warpOwnerUUID;
	}

	public UUID getPlayerUUID() {
		return playerUUID;
	}

	public void setPlayerUUID(UUID playerUUID) {
		this.playerUUID = playerUUID;
	}

	public UUID getWarpOwnerUUID() {
		return warpOwnerUUID;
	}

	public void setWarpOwnerUUID(UUID warpOwnerUUID) {
		this.warpOwnerUUID = warpOwnerUUID;
	}
}
