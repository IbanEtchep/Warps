package fr.iban.warps.listeners;

import fr.iban.warps.WarpsManager;
import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.model.PlayerWarp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.UUID;

public class TeleportListener implements Listener {

    private final WarpsManager manager;

    public TeleportListener(WarpsPlugin plugin) {
        this.manager = plugin.getWarpManager();
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

                player.sendMessage(Component.text("Téléportation au warp : ")
                        .color(NamedTextColor.GREEN)
                        .append(Component.text(pwarp.getName())));

                boolean likeWarp = pwarp.getVotes().containsKey(uuid.toString())
                        && pwarp.getVotes().get(uuid.toString()).getVote() == 1;

                if(player.getName().startsWith(".")) {
                    player.sendMessage(Component.text((likeWarp ?
                                    "Vous n'aimez plus ce warp? T"
                                    : "Vous aimez ce warp? Ret") + "\n➥ apez /warp like " + pwarp.getOwnerName())
                            .color(NamedTextColor.GRAY)
                            .decorate(TextDecoration.BOLD));
                } else {
                    player.sendMessage(
                            Component.text((likeWarp ?
                                            "Vous n'aimez plus ce warp?"
                                            : "Vous aimez ce warp?") + "\n➥ Cliquez sur ")
                                    .color(NamedTextColor.GRAY)
                                    .decorate(TextDecoration.BOLD)
                                    .append(getVoteComponent(pwarp, likeWarp))
                    );
                }
            }
        }
    }

    private Component getVoteComponent(PlayerWarp warp, boolean like) {
        if (like) {
            return Component.text("[JE N'AIME PLUS]")
                    .color(NamedTextColor.RED)
                    .decorate(TextDecoration.BOLD)
                    .hoverEvent(Component.text("Je n'aime plus ce warp !")
                            .color(NamedTextColor.RED))
                    .clickEvent(ClickEvent.runCommand("/pwarp like " + warp.getOwnerName()));
        } else {
            return Component.text("[J'AIME]")
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .decorate(TextDecoration.BOLD)
                    .hoverEvent(Component.text("J'aime ce warp !")
                            .color(NamedTextColor.LIGHT_PURPLE))
                    .clickEvent(ClickEvent.runCommand("/pwarp like " + warp.getOwnerName()));
        }
    }
}
