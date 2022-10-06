package fr.iban.warps.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.objects.Warp;

public class SystemWarpCMD implements CommandExecutor, TabCompleter{

	private final WarpsManager manager;

	public SystemWarpCMD(WarpsPlugin warpsPlugin) {
		this.manager = warpsPlugin.getWarpManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player)sender;
			if(args.length == 0 && player.hasPermission("systemwarp.help")) {
				player.sendMessage("systemwarp create/delete nom");
			}else if(args.length == 1) {
				String name = args[0];
				Warp warp = manager.getWarp(name);
				if(warp != null){
					manager.teleport(player, warp);
				}else {
					player.sendMessage("§cCe warp n'existe pas.");
				}
			}else if(args.length >= 2 && sender.hasPermission("systemwarp.manage")) {
				if(args[0].equalsIgnoreCase("create")) {
					String name = args[1];
					if(manager.getWarp(name) == null){
						StringBuilder bc = new StringBuilder();
						for (int i = 2; i < args.length; i++) {
							bc.append(args[i] + " ");
						}
						String desc = bc.toString();
						manager.createWarp(new Warp(0, SLocationUtils.getSLocation(player.getLocation()), name, desc));
						player.sendMessage("§aUn nouveau warp du nom de " + name + " a été crée !");
					}else {
						player.sendMessage("§cCe warp existe déjà.");
					}
				}else if(args[0].equalsIgnoreCase("delete")) {
					String name = args[1];
					Warp warp = manager.getWarp(name);
					if(warp != null){
						manager.deleteWarp(warp);
						player.sendMessage("§aLe warp a bien été supprimé");
					}else {
						player.sendMessage("§cCe warp n'existe pas.");
					}
				}
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(sender instanceof Player && args.length == 1) {
			return getStartsWithList(manager.getWarps().values().stream().map(warp -> warp.getName()).collect(Collectors.toList()), args[0]);
		}
		return null;
	}

	private List<String> getStartsWithList(List<String> list, String with){
		return list.stream().filter(string -> string.toLowerCase().startsWith(with.toLowerCase())).collect(Collectors.toList());
	}
}
