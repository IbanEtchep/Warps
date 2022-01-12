package fr.iban.warps.utils;

import java.util.UUID;

public class WarpSyncMessage {
	
	private boolean playerWarp;
	private String senderServer;
	private int id;
	private UUID uuid;
	
	public WarpSyncMessage() {}
	
	
	public WarpSyncMessage(boolean playerWarp, String senderServer, int id, UUID uuid) {
		super();
		this.playerWarp = playerWarp;
		this.senderServer = senderServer;
		this.id = id;
		this.uuid = uuid;
	}
	
	
	public boolean isPlayerWarp() {
		return playerWarp;
	}
	
	public void setPlayerWarp(boolean playerWarp) {
		this.playerWarp = playerWarp;
	}
	
	public String getSenderServer() {
		return senderServer;
	}
	
	public void setSenderServer(String senderServer) {
		this.senderServer = senderServer;
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
