package fr.iban.warps.commands;


import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;

public class MarketCMD {

	@Command("marché")
	public void market(Player player) {
		player.performCommand("warp marché");
	}

}
