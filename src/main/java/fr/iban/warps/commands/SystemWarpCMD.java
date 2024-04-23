package fr.iban.warps.commands;

import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.objects.Warp;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"warp"})
public class SystemWarpCMD {

    private final WarpsManager manager;

    public SystemWarpCMD(WarpsPlugin plugin) {
        this.manager = plugin.getWarpManager();
    }

    @Subcommand("help")
    @CommandPermission("systemwarp.admin")
    public void help(BukkitCommandActor actor) {
        actor.reply("warp create/delete nom");
    }

    @Command("visit")
    @DefaultFor("warp")
    public void visit(Player player, Warp warp) {
        manager.teleport(player, warp);
    }

    @Subcommand("create")
    @CommandPermission("systemwarp.admin")
    public void create(Player player, String name) {
        if (manager.getWarp(name) == null) {
            manager.createWarp(new Warp(0, SLocationUtils.getSLocation(player.getLocation()), name, ""));
            player.sendMessage("§aUn nouveau warp du nom de " + name + " a été crée !");
        } else {
            player.sendMessage("§cCe warp existe déjà.");
        }
    }

    @Subcommand("delete")
    @CommandPermission("systemwarp.admin")
    public void delete(BukkitCommandActor actor, Warp warp) {
        manager.deleteWarp(warp);
        actor.reply("§aLe warp a bien été supprimé");
    }

}