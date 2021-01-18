package fr.iban.warps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.iban.claims.ClaimsPlugin;
import fr.iban.claims.objects.Claim;
import fr.iban.claims.objects.claimtypes.PlayerClaim;
import fr.iban.claims.utils.ClaimAction;
import fr.iban.warps.utils.PlayerWarpFileUtility;
import fr.iban.warps.utils.StarRating;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class WarpsManager {

	private Map<UUID, PlayerWarp> pwarps = new HashMap<>();
	private List<String> tags = Arrays.asList("#shop", "#farm", "#ville", "#maison");

	public Map<UUID, PlayerWarp> getPlayerWarps() {
		return pwarps;
	}

	public List<String> getTags() {
		return tags;
	}

	public void loadPlayerWarps() {
		File dir = new File(Warps.getInstance().getDataFolder(), "/pwarps");
		if(dir.exists()) {
			for(File file : dir.listFiles()) {
				if(file.getName().endsWith(".yml")) {
					if(file.exists()) {
						UUID uuid = UUID.fromString(file.getName().split("\\.")[0]);
						PlayerWarpFileUtility wfu = new PlayerWarpFileUtility(uuid);
						Map<String, Short> avis = new HashMap<>();
						for(String key : wfu.getConfig().getConfigurationSection("avis").getKeys(false)) {
							avis.put(key, (short) wfu.getConfig().getInt("avis."+key));
						}
						PlayerWarp warp = new PlayerWarp(
								UUID.fromString(wfu.getConfig().getString("owner")),
								wfu.getConfig().getLocation("location"),
								wfu.getConfig().getString("name"),
								wfu.getConfig().getString("description")
								);
						warp.setOpened(wfu.getConfig().getBoolean("opened"));
						warp.setAvis(avis);
						warp.setTags(wfu.getConfig().getStringList("tags"));
						pwarps.put(uuid, warp);
						if(System.currentTimeMillis() - Bukkit.getOfflinePlayer(uuid).getLastSeen() > 1209600000) {
							warp.setOpened(false);
							savePlayerWarp(warp);
						}
					}
				}
			}
		}
	}

	public void savePlayerWarp(PlayerWarp warp) {
		PlayerWarpFileUtility wfu = new PlayerWarpFileUtility(warp.getOwner());
		wfu.set("owner", warp.getOwner().toString());
		wfu.set("name", warp.getName());
		wfu.set("description", warp.getDesc());
		wfu.set("opened", warp.isOpened());
		wfu.set("location", warp.getLocation());
		wfu.set("tags", warp.getTags());
		wfu.set("avis", warp.getAvis());
	}

	public void savePlayerWarps() {
		for (PlayerWarp warp : pwarps.values()) {
			savePlayerWarp(warp);
		}
	}

	public void deletePlayerWarpConf(PlayerWarp warp) {
		PlayerWarpFileUtility wfu = new PlayerWarpFileUtility(warp.getOwner());
		try {
			Files.delete(wfu.getFile().toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reloadWarps() {
		savePlayerWarps();
		loadPlayerWarps();
	}



	public PlayerWarp getPlayerWarp(UUID uuid) {
		return pwarps.get(uuid);
	}


	public void setPlayerWarp(Player player, String name, @Nullable String desc) {
		UUID uuid = player.getUniqueId();
		Claim claim = ClaimsPlugin.getInstance().getClaimManager().getClaimAt(player.getChunk());
		if(claim instanceof PlayerClaim && claim.isBypassing(player, ClaimAction.SET_WARP)){
			if(!getPlayerWarps().containsKey(uuid)) {
				PlayerWarp warp = new PlayerWarp(uuid, player.getLocation(), name, desc);
				pwarps.put(uuid, warp);
				savePlayerWarp(warp);
				player.sendMessage("§aVotre warp a bien été crée.");
			}else {
				player.sendMessage("§cVous avez déjà un warp.");
			}
		}else {
			player.sendMessage("§cVous devez vous trouver dans un de vos claims.");
		}
	}

	public void removePlayerWarp(Player player) {
		UUID uuid = player.getUniqueId();
		if(getPlayerWarp(uuid) != null) {
			deletePlayerWarpConf(getPlayerWarp(uuid));
			pwarps.remove(uuid);
			player.sendMessage("§aLe warp a bien été supprimé.");
		}else {
			player.sendMessage("§cVous n'avez pas de warp.");
		}
	}


	public void teleport(Player player, PlayerWarp warp) {
		if(warp.isOpened() || warp.getOwner().equals(player.getUniqueId())) {
			player.teleportAsync(warp.getLocation());
			player.sendMessage("§aTéléportation au warp : §r" + warp.getName());
			player.sendMessage(
					new ComponentBuilder("§7"+ (warp.getAvis().containsKey(player.getUniqueId().toString()) ? "Changer votre note ("+ StarRating.getStars(warp.getAvis().get(player.getUniqueId().toString())) +"§7)" : "Noter ce warp") +": ")
					.append(StarRating.getNoteStars(warp))
					.create());
		}else {
			player.sendMessage("§cCe warp est fermé.");
		}
	}

}
