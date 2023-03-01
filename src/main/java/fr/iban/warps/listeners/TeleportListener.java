package fr.iban.warps.listeners;

import fr.iban.warps.WarpsManager;
import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.objects.PlayerWarp;
import fr.iban.warps.utils.ChatUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.UUID;

public class TeleportListener implements Listener {

    private WarpsManager manager;
    private WarpsPlugin plugin;

    public TeleportListener(WarpsPlugin plugin) {
        this.manager = plugin.getWarpManager();
        this.plugin = plugin;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == TeleportCause.PLUGIN) {
            Player player = e.getPlayer();

            UUID uuid = player.getUniqueId();
            UUID targetUUID = manager.getWarpTpWaiting().get(uuid);

            if (targetUUID != null) {

                PlayerWarp pwarp = manager.getPlayerWarp(targetUUID);
                manager.getWarpTpWaiting().remove(uuid);

                player.sendMessage("§aTéléportation au warp : §r" + pwarp.getName());

                boolean likeWarp = pwarp.getVotes().containsKey(uuid.toString()) && pwarp.getVotes().get(uuid.toString()).getVote() == 1;
                if(player.getName().startsWith(".")) {
                    player.sendMessage("§7§l" + (likeWarp ?
                                    "Vous n'aimez plus ce warp? T"
                                    : "Vous aimez ce warp? Ret") + "\n➥ apez /warp like " + pwarp.getOwnerName());
                }else{
                    player.sendMessage(
                            new ComponentBuilder("§7§l" + (likeWarp ?
                                    "Vous n'aimez plus ce warp?"
                                    : "Vous aimez ce warp?") + "\n➥ Cliquez sur ")
                                    .append(getVoteComponent(pwarp, player, likeWarp))
                                    .create());
                }
            }
        }
    }

    private BaseComponent[] getVoteComponent(PlayerWarp warp, Player player, boolean like) {
        BaseComponent[] symbol;
        if (like) {
            symbol = new ComponentBuilder("[JE N'AIME PLUS]").bold(true).color(ChatColor.RED).event(ChatUtils.getShowTextHoverEvent("§cJe n'aime plus ce warp !")).event(new ClickEvent(Action.RUN_COMMAND, "/pwarp like " + warp.getOwnerName())).create();
        } else {
            symbol = new ComponentBuilder("[J'AIME]").bold(true).color(ChatColor.LIGHT_PURPLE).event(ChatUtils.getShowTextHoverEvent("§dJ'aime ce warp !")).event(new ClickEvent(Action.RUN_COMMAND, "/pwarp like " + warp.getOwnerName())).create();
        }
        return new ComponentBuilder().color(ChatColor.GOLD).append(symbol).create();
    }

}