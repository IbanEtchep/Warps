package fr.iban.warps.utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.iban.warps.Warps;

public class PlayerWarpFileUtility {
	
	File file = null;
	FileConfiguration configuration = null;
	UUID uuid;

	/**
	 * @param id reference of data
	 */
	public PlayerWarpFileUtility(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * get the config from file
	 * @return file configuration
	 */
	public FileConfiguration getConfig() {
		if(configuration == null) {
			configuration = YamlConfiguration.loadConfiguration(getFile());
			return configuration;
		}
		return configuration;
	}

	/**
	 * this will save the config to the file
	 */
	public void saveConfig() {
		try {
			configuration.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * set an object to a certain path
	 * @param path the path
	 * @param object the object to set
	 */
	public void set(String path, Object object) {
		getConfig().set(path, object);
		saveConfig();
	}

	/**
	 * this will get the player's file
	 * @return the player's uuid file
	 */
	public File getFile() {
		if(file == null) {
			this.file = new File(Warps.getInstance().getDataFolder() + "/pwarps", uuid + ".yml");
			if(!this.file.exists()) {
				try {
					if(this.file.createNewFile()) {
						Warps.getInstance().getLogger().log(Level.INFO, "Création du fichier" + file.getName());
					}
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
			return file;
		}
		return file;
	}

	/**
	 * this will reload the config
	 */
	public void reloadConfig() {
		YamlConfiguration.loadConfiguration(file);
	}
}
