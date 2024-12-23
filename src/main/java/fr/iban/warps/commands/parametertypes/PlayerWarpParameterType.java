package fr.iban.warps.commands.parametertypes;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayer;
import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.model.PlayerWarp;
import fr.iban.warps.model.Warp;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;

public class PlayerWarpParameterType implements ParameterType<BukkitCommandActor, PlayerWarp> {

    private final WarpsPlugin plugin;

    public PlayerWarpParameterType(WarpsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public PlayerWarp parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<@NotNull BukkitCommandActor> executionContext) {
        String value = input.readString();
        MSPlayer msPlayer = CoreBukkitPlugin.getInstance().getPlayerManager().getOfflinePlayer(value);

        if (msPlayer == null) {
            throw new CommandErrorException("Le joueur " + value + " n''a jamais joué sur le serveur.");
        }

        PlayerWarp warp = plugin.getWarpManager().getPlayerWarp(msPlayer.getUniqueId());
        if (warp == null) {
            throw new CommandErrorException("Ce joueur n''a pas de warp.");
        }

        return warp;
    }

    @Override
    public @NotNull SuggestionProvider<@NotNull BukkitCommandActor> defaultSuggestions() {
        PlayerManager playerManager = CoreBukkitPlugin.getInstance().getPlayerManager();

        return (context) -> plugin.getWarpManager().getPlayerWarps().values().stream()
                .filter(Warp::isOpened)
                .map(warp -> playerManager.getOfflinePlayer(warp.getOwner()).getName())
                .toList();
    }

    @Override
    public @NotNull PrioritySpec parsePriority() {
        return PrioritySpec.highest();
    }
}