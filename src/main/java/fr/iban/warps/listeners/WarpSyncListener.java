package fr.iban.warps.listeners;

import org.redisson.api.listener.MessageListener;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.utils.WarpSyncMessage;

public class WarpSyncListener implements MessageListener<WarpSyncMessage> {

	private WarpsManager manager;
	
	public WarpSyncListener(WarpsManager warpManager) {
		this.manager = warpManager;
	}

	@Override
	public void onMessage(CharSequence channel, WarpSyncMessage message) {
		CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
		if(!core.getServerName().equals(message.getSenderServer())) {
			if(message.isPlayerWarp()) {
				manager.updateWarp(message.getUuid());
			}else {
				manager.updateWarp(message.getId());
			}
		}
	}
}
