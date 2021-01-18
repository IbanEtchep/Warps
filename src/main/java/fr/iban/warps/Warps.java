package fr.iban.warps;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.iban.warps.commands.PlayerWarpCMD;
import fr.iban.warps.commands.WarpsCMD;
import fr.iban.warps.listeners.InventoryListener;

public final class Warps extends JavaPlugin {
	
	private static Warps instance;
	
	private WarpsManager warpManager;
		
	// ★☆⯪
	
    @Override
    public void onEnable() {
    	instance = this;
    	warpManager = new WarpsManager();
    	
    	PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new InventoryListener(), this);
        
        getCommand("pwarp").setExecutor(new PlayerWarpCMD());
        getCommand("pwarp").setTabCompleter(new PlayerWarpCMD());
        getCommand("warps").setExecutor(new WarpsCMD());
        getCommand("warps").setTabCompleter(new WarpsCMD());
        
        warpManager.loadPlayerWarps();
        
    }

    @Override
    public void onDisable() {
    	warpManager.savePlayerWarps();
    }
    

	public static Warps getInstance() {
		return instance;
	}

	public WarpsManager getWarpManager() {
		return warpManager;
	}
}
