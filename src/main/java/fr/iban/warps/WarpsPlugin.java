package fr.iban.warps;

import fr.iban.warps.commands.MarketCMD;
import fr.iban.warps.commands.PlayerWarpCMD;
import fr.iban.warps.commands.SystemWarpCMD;
import fr.iban.warps.commands.WarpsCMD;
import fr.iban.warps.commands.parametertypes.PlayerWarpParameterType;
import fr.iban.warps.commands.parametertypes.WarpParameterType;
import fr.iban.warps.listeners.CommandListeners;
import fr.iban.warps.listeners.CoreMessageListener;
import fr.iban.warps.listeners.PlayerRespawnListener;
import fr.iban.warps.listeners.TeleportListener;
import fr.iban.warps.model.PlayerWarp;
import fr.iban.warps.model.Warp;
import fr.iban.warps.storage.SqlTables;
import fr.iban.warps.utils.TagCompleter;
import fr.iban.warps.zmenu.ZMenuManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

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
        Lamp<BukkitCommandActor> lamp =  BukkitLamp.builder(this)
                .parameterTypes(builder ->
                        builder
                                .addParameterType(PlayerWarp.class, new PlayerWarpParameterType(this))
                                .addParameterType(Warp.class, new WarpParameterType(this)))
                .suggestionProviders(builder ->
                        builder.addProviderForAnnotation(TagCompleter.class,
                                annotation -> context -> warpManager.getTags()
                        ))
                .build();

        lamp.register(new PlayerWarpCMD(this));
        lamp.register(new WarpsCMD(this));
        lamp.register(new SystemWarpCMD(this));
        lamp.register(new MarketCMD());
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
