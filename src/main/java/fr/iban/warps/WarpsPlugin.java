package fr.iban.warps;

import fr.iban.warps.utils.WarpSyncMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.api.RTopic;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.warps.commands.MarketCMD;
import fr.iban.warps.commands.PlayerWarpCMD;
import fr.iban.warps.commands.SystemWarpCMD;
import fr.iban.warps.commands.WarpsCMD;
import fr.iban.warps.listeners.CommandListeners;
import fr.iban.warps.listeners.TeleportListener;
import fr.iban.warps.listeners.WarpSyncListener;
import fr.iban.warps.storage.SqlTables;

public final class WarpsPlugin extends JavaPlugin {
	
	private static WarpsPlugin instance;

	private WarpsManager warpManager;

    private RTopic warpSyncTopic;
    private WarpSyncListener warpSyncListener;
			
    @Override
    public void onEnable() {
    	instance = this;
    	warpManager = new WarpsManager(this);
    	
    	PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new CommandListeners(), this);
        pm.registerEvents(new TeleportListener(getWarpManager()), instance);
        
        SqlTables.createTables();
        getCommand("pwarp").setExecutor(new PlayerWarpCMD(warpManager));
        getCommand("pwarp").setTabCompleter(new PlayerWarpCMD(warpManager));
        getCommand("warps").setExecutor(new WarpsCMD(this));
        getCommand("warps").setTabCompleter(new WarpsCMD(this));
        getCommand("systemwarp").setExecutor(new SystemWarpCMD(this));
        getCommand("marché").setExecutor(new MarketCMD());
		warpSyncTopic = CoreBukkitPlugin.getInstance().getRedisClient().getTopic("SyncWarp");
        warpSyncListener = new WarpSyncListener(warpManager);
        warpSyncTopic.addListener(WarpSyncMessage.class, warpSyncListener);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        warpSyncTopic.removeListener(warpSyncListener);
    }

    public static WarpsPlugin getInstance() {
		return instance;
	}

	public WarpsManager getWarpManager() {
		return warpManager;
	}
}
