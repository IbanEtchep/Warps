package fr.iban.warps.utils;

public class WarpTpMessage {
	
	private String server;
	private int id;
	private String toTpUUID;
	private String ownerUUID;
	
	
	public WarpTpMessage() {}
	
	
	
	public WarpTpMessage(String server, int id, String ownerUUID) {
		super();
		this.server = server;
		this.id = id;
		this.ownerUUID = ownerUUID;
	}



	public WarpTpMessage(String server, String toTpUUID, String ownerUUID) {
		super();
		this.server = server;
		this.toTpUUID = toTpUUID;
		this.ownerUUID = ownerUUID;
	}


	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getToTpUUID() {
		return toTpUUID;
	}
	public void setToTpUUID(String toTpUUID) {
		this.toTpUUID = toTpUUID;
	}
	public String getOwnerUUID() {
		return ownerUUID;
	}
	public void setOwnerUUID(String ownerUUID) {
		this.ownerUUID = ownerUUID;
	}
	
	

}
