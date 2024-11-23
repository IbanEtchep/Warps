package fr.iban.warps.commands;

import fr.iban.warps.WarpsManager;
import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.menu.MainWarpsMenu;
import fr.iban.warps.model.PlayerWarp;
import fr.iban.warps.model.enums.SortingTime;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;

import java.util.stream.Collectors;

public class WarpsCMD {

    private final WarpsPlugin plugin;

    public WarpsCMD(WarpsPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("warps")
    public void warps(Player player) {
        plugin.getMenuManager().clearMenuData(player);
        plugin.getMenuManager().openWarpsMainMenu(player);
    }
}
