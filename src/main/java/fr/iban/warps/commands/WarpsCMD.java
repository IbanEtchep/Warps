package fr.iban.warps.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import fr.iban.warps.WarpsManager;
import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.menu.MainWarpsMenu;
import fr.iban.warps.menu.WarpsMainMenu;
import fr.iban.warps.objects.PlayerWarp;
import fr.iban.warps.objects.Warp;
import fr.iban.warps.utils.SortingType;

public class WarpsCMD implements CommandExecutor, TabCompleter {

    private final WarpsManager manager;

    public WarpsCMD(WarpsPlugin plugin) {
        this.manager = plugin.getWarpManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                new MainWarpsMenu(player, manager, manager.getPlayerWarps().values().stream().filter(PlayerWarp::isOpened).collect(Collectors.toList()), SortingType.ALL).open();
            } else if (args.length == 1) {
                if (manager.getTags().contains(args[0])) {
                    new WarpsMainMenu(player, manager, manager.getPlayerWarps().values().stream().filter(PlayerWarp::isOpened).filter(warp -> containsTag(warp, args[0])).collect(Collectors.toList())).open();
                } else {
                    player.sendMessage("Ce tag n'existe pas.");
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (sender instanceof Player) {
            if (args.length == 1) {
                for (String string : manager.getTags()) {
                    list.add(string);
                }
                return list;
            }
        }
        return list;
    }

    private boolean containsTag(Warp warp, String tag) {
        for (String t : warp.getTags()) {
            if (t.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

}
