package fr.iban.warps;

import fr.iban.warps.commands.MarketCMD;
import fr.iban.warps.commands.PlayerWarpCMD;
import fr.iban.warps.commands.SystemWarpCMD;
import fr.iban.warps.commands.WarpsCMD;
import fr.iban.warps.listeners.CommandListeners;
import fr.iban.warps.listeners.TeleportListener;
import fr.iban.warps.listeners.CoreMessageListener;
import fr.iban.warps.storage.SqlTables;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class WarpsPlugin extends JavaPlugin {
	
	private static WarpsPlugin instance;

	private WarpsManager warpManager;
			
    @Override
    public void onEnable() {
    	instance = this;
    	warpManager = new WarpsManager(this);
    	
    	PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new CommandListeners(), this);
        pm.registerEvents(new TeleportListener(this), this);
        pm.registerEvents(new CoreMessageListener(warpManager), this);
        
        SqlTables.createTables();
        getCommand("pwarp").setExecutor(new PlayerWarpCMD(warpManager));
        getCommand("pwarp").setTabCompleter(new PlayerWarpCMD(warpManager));
        getCommand("warps").setExecutor(new WarpsCMD(this));
        getCommand("warps").setTabCompleter(new WarpsCMD(this));
        getCommand("systemwarp").setExecutor(new SystemWarpCMD(this));
        getCommand("marché").setExecutor(new MarketCMD());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public static WarpsPlugin getInstance() {
		return instance;
	}

	public WarpsManager getWarpManager() {
		return warpManager;
	}
}
