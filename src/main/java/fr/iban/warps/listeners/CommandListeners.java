package fr.iban.warps.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListeners implements Listener {
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if(e.getMessage().equalsIgnoreCase("/warp")) {
			e.setMessage("/pwarp");
		}else if(e.getMessage().equalsIgnoreCase("/spawn") || e.getMessage().equalsIgnoreCase("/spoun")) {
			e.setMessage("/systemwarp spawn");
		}else if(e.getMessage().equalsIgnoreCase("/infos")) {
			e.setMessage("/systemwarp info");
		}else if(e.getMessage().equalsIgnoreCase("/arene") || e.getMessage().equalsIgnoreCase("/arène")) {
			e.setMessage("/systemwarp arene");
		}
	}

}
