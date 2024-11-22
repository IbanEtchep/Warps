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

    private final WarpsManager manager;

    public WarpsCMD(WarpsPlugin plugin) {
        this.manager = plugin.getWarpManager();
    }

    @Command("warps")
    public void warps(Player player) {
        new MainWarpsMenu(player, manager, manager.getPlayerWarps().values().stream()
                .filter(PlayerWarp::isOpened)
                .collect(Collectors.toList()), SortingTime.ALL).open();
    }
}
