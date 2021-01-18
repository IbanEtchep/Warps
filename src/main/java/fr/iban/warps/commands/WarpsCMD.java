package fr.iban.warps.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import fr.iban.warps.Warps;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.menu.menus.WarpsMenu;

public class WarpsCMD implements CommandExecutor, TabCompleter{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player)sender;
			if(args.length == 0) {
				new WarpsMenu(player).open();
			}else if(args.length == 1) {
				if(Warps.getInstance().getWarpManager().getTags().contains(args[0])){
					new WarpsMenu(player, args[0]).open();
				}else {
					player.sendMessage("Ce tag n'existe pas.");
				}
				if(args[0].equalsIgnoreCase("reload") && player.hasPermission("warps.reload")) {
					Warps.getInstance().getWarpManager().reloadWarps();
					player.sendMessage("§aWarps reloadés.");
				}
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> list = new ArrayList<>();
		if(sender instanceof Player) {
			WarpsManager wm = Warps.getInstance().getWarpManager();
			if(args.length == 1) {
				for(String string : wm.getTags()) {
					list.add(string);
				}
				return list;
			}
		}
		return list;
	}

}
