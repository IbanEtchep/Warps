package fr.iban.warps.utils;

import java.util.UUID;

public class WarpSyncMessage {
	
	private boolean playerWarp;
	private int id;
	private UUID uuid;
	
	public WarpSyncMessage(boolean playerWarp, int id, UUID uuid) {
		super();
		this.playerWarp = playerWarp;
		this.id = id;
		this.uuid = uuid;
	}
	
	
	public boolean isPlayerWarp() {
		return playerWarp;
	}
	
	public void setPlayerWarp(boolean playerWarp) {
		this.playerWarp = playerWarp;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	

}
