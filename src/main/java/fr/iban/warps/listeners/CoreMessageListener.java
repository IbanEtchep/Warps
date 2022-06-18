package fr.iban.warps.listeners;

import com.google.gson.Gson;
import fr.iban.bukkitcore.event.CoreMessageEvent;
import fr.iban.common.messaging.Message;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.utils.WarpSyncMessage;
import fr.iban.warps.utils.WarpTpMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CoreMessageListener implements Listener {

    private final WarpsManager manager;
    private final Gson gson = new Gson();

    public CoreMessageListener(WarpsManager warpManager) {
        this.manager = warpManager;
    }

    @EventHandler
    public void onMessage(CoreMessageEvent e) {
        Message message = e.getMessage();

        if (message.getChannel().equals(manager.SYNC_CHANNEL)) {
            WarpSyncMessage warpSyncMessage = gson.fromJson(message.getMessage(), WarpSyncMessage.class);

            if (warpSyncMessage.isPlayerWarp()) {
                manager.updateWarp(warpSyncMessage.getUuid());
            } else {
                manager.updateWarp(warpSyncMessage.getId());
            }
        } else if (message.getChannel().equals(manager.TP_WAITING_CHANNEL)) {
			WarpTpMessage tpMessage = gson.fromJson(message.getMessage(), WarpTpMessage.class);
			manager.addWarpTpWaiting(tpMessage.getPlayerUUID(), tpMessage.getWarpOwnerUUID());
        }

    }
}
