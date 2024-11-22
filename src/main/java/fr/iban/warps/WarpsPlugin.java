package fr.iban.warps;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.BukkitPlayerManager;
import fr.iban.warps.commands.MarketCMD;
import fr.iban.warps.commands.PlayerWarpCMD;
import fr.iban.warps.commands.SystemWarpCMD;
import fr.iban.warps.commands.WarpsCMD;
import fr.iban.warps.listeners.CommandListeners;
import fr.iban.warps.listeners.CoreMessageListener;
import fr.iban.warps.listeners.PlayerRespawnListener;
import fr.iban.warps.listeners.TeleportListener;
import fr.iban.warps.model.PlayerWarp;
import fr.iban.warps.model.Warp;
import fr.iban.warps.storage.SqlTables;
import fr.iban.warps.utils.TagCompleter;
import fr.iban.warps.zmenu.ZMenuManager;
import fr.maxlego08.menu.api.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.exception.CommandErrorException;

import java.util.UUID;

public final class WarpsPlugin extends JavaPlugin {

    private static WarpsPlugin instance;

    private WarpsManager warpManager;
    private ZMenuManager zMenuManager;

    @Override
    public void onEnable() {
        instance = this;
        warpManager = new WarpsManager(this);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new CommandListeners(), this);
        pm.registerEvents(new TeleportListener(this), this);
        pm.registerEvents(new CoreMessageListener(warpManager), this);
        pm.registerEvents(new PlayerRespawnListener(this), this);

        this.zMenuManager = new ZMenuManager(this);
        zMenuManager.loadZMenu();
        pm.registerEvents(zMenuManager, this);

        SqlTables.createTables();
        registerCommands();
    }

    @Override
    public void onDisable() {
        zMenuManager.unloadZMenu();
    }

    private void registerCommands() {
        BukkitCommandHandler commandHandler = BukkitCommandHandler.create(this);
        BukkitPlayerManager playerManager = CoreBukkitPlugin.getInstance().getPlayerManager();

        commandHandler.accept(CoreBukkitPlugin.getInstance().getCommandHandlerVisitor());
        commandHandler.getAutoCompleter().registerSuggestionFactory(parameter -> {
            if (parameter.hasAnnotation(TagCompleter.class)) {
                return (args, sender, command) -> warpManager.getTags();
            }
            return null;
        });

        commandHandler.getAutoCompleter().registerParameterSuggestions(PlayerWarp.class, (args, sender, command) ->
                warpManager.getPlayerWarps().values().stream()
                        .filter(Warp::isOpened)
                        .map(warp -> playerManager.getName(warp.getOwner())).toList());

        commandHandler.getAutoCompleter().registerParameterSuggestions(Warp.class, (args, sender, command) ->
                warpManager.getWarps().values().stream().map(Warp::getName).toList());

        commandHandler.registerValueResolver(0, PlayerWarp.class, context -> {
            String value = context.arguments().pop();
            UUID uuid = playerManager.getOfflinePlayerUUID(value);

            if (uuid == null) {
                throw new CommandErrorException("Le joueur " + value + " n''a jamais joué sur le serveur.");
            }

            PlayerWarp warp = warpManager.getPlayerWarp(uuid);
            if (warp == null) {
                throw new CommandErrorException("Ce joueur n''a pas de warp.");
            }

            return warp;
        });

        commandHandler.registerValueResolver(0, Warp.class, context -> {
            String value = context.arguments().pop();
            Warp warp = warpManager.getWarp(value);

            if (warp == null) {
                throw new CommandErrorException("Ce warp n''existe pas.");
            }

            return warp;
        });

        commandHandler.register(new PlayerWarpCMD(this));
        commandHandler.register(new WarpsCMD(this));
        commandHandler.register(new SystemWarpCMD(this));
        commandHandler.register(new MarketCMD());
        commandHandler.registerBrigadier();
    }

    private <T> @Nullable T getProvider(Class<T> classProvider) {
        RegisteredServiceProvider<T> provider = Bukkit.getServer().getServicesManager().getRegistration(classProvider);
        return provider == null ? null : provider.getProvider();
    }

    public static WarpsPlugin getInstance() {
        return instance;
    }

    public WarpsManager getWarpManager() {
        return warpManager;
    }

    public ZMenuManager getMenuManager() {
        return zMenuManager;
    }
}
