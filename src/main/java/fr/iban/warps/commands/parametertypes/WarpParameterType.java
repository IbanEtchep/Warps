package fr.iban.warps.commands.parametertypes;

import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.model.Warp;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;

public class WarpParameterType implements ParameterType<BukkitCommandActor, Warp> {

    private final WarpsPlugin plugin;

    public WarpParameterType(WarpsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Warp parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<@NotNull BukkitCommandActor> executionContext) {
        String value = input.readString();
        Warp warp = plugin.getWarpManager().getWarp(value);

        if (warp == null) {
            throw new CommandErrorException("Ce warp n''existe pas.");
        }

        return warp;
    }

    @Override
    public @NotNull SuggestionProvider<@NotNull BukkitCommandActor> defaultSuggestions() {
        return (context) -> plugin.getWarpManager().getWarps().values().stream().map(Warp::getName).toList();
    }

    @Override
    public @NotNull PrioritySpec parsePriority() {
        return PrioritySpec.highest();
    }
}