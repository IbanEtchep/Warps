package fr.iban.warps;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.redisson.api.RMap;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.objects.Land;
import fr.iban.warps.objects.PlayerWarp;
import fr.iban.warps.objects.Vote;
import fr.iban.warps.objects.Warp;
import fr.iban.warps.storage.Storage;
import fr.iban.warps.utils.WarpSyncMessage;

public class WarpsManager {

	private WarpsPlugin plugin;

	private Storage storage = new Storage();
	private Map<UUID, PlayerWarp> pwarps = new HashMap<>();
	private Map<Integer, Warp> warps = new HashMap<>();
	private List<String> tags = Arrays.asList("#shop", "#farm", "#ville", "#maison", "#architecture");


	public WarpsManager(WarpsPlugin plugin) {
		this.plugin = plugin;
		loadWarps();
	}


	public Map<UUID, PlayerWarp> getPlayerWarps() {
		return pwarps;
	}

	public List<String> getTags() {
		return tags;
	}

	public Map<Integer, Warp> getWarps() {
		return warps;
	}

	public PlayerWarp getPlayerWarp(UUID uuid) {
		return pwarps.get(uuid);
	}

	public Warp getWarp(String name) {
		for(Warp warp : getWarps().values()) {
			if(warp.getName().equalsIgnoreCase(name)) {
				return warp;
			}
		}
		return null;
	}

	public void loadWarps() {
		long now = System.currentTimeMillis();
		plugin.getLogger().info("Chargement des warps joueurs.");
		getPlayersWarpsAsync().thenAccept(warps -> {
			warps.forEach(warp -> {
				pwarps.put(warp.getOwner(), warp);
			});
			plugin.getLogger().info(warps.size() + " warps joueurs chargés en " + (System.currentTimeMillis() - now) + " ms.");
		});
		plugin.getLogger().info("Chargement des warps.");
		getWarpsAsync().thenAccept(w -> {
			w.forEach(warp -> {
				warps.put(warp.getId(), warp);
			});
			plugin.getLogger().info(w.size() + " warps chargés en " + (System.currentTimeMillis() - now) + " ms.");
		});
	}

	public void setPlayerWarp(Player player, String name, @Nullable String desc) {
		UUID uuid = player.getUniqueId();
		Land land = LandsPlugin.getInstance().getLandManager().getLandAt(player.getChunk());
		if(land.isBypassing(player, Action.SET_WARP)) {
			if(!getPlayerWarps().containsKey(uuid)) {
				PlayerWarp warp = new PlayerWarp(-1, uuid, SLocationUtils.getSLocation(player.getLocation()), name, desc);
				pwarps.put(uuid, warp);
				createWarp(warp);
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
		Warp warp = getPlayerWarp(uuid);
		if(warp != null) {
			deleteWarp(warp);
			pwarps.remove(uuid);
			player.sendMessage("§aLe warp a bien été supprimé.");
		}else {
			player.sendMessage("§cVous n'avez pas de warp.");
		}
	}

	public CompletableFuture<List<PlayerWarp>> getPlayersWarpsAsync() {
		return future(() -> {
			return storage.getPlayersWarps();
		});
	}

	public CompletableFuture<List<Warp>> getWarpsAsync() {
		return future(() -> {
			return storage.getWarps();
		});
	}

	public void updateWarp(UUID uuid) {
		if(pwarps.containsKey(uuid)) {
			pwarps.remove(uuid);
		}
		future(() -> {
			return storage.getPlayerWarp(uuid);
		}).thenAccept(pwarp -> {
			if(pwarp != null) {
				pwarps.put(uuid, pwarp);
			}
		});
	}

	public void updateWarp(int id) {
		if(warps.containsKey(id)) {
			warps.remove(id);
		}
		future(() -> {
			return storage.getWarp(id);
		}).thenAccept(warp -> {
			if(warp != null) {
				warps.put(id, warp);
			}
		});
	}

	public void createWarp(Warp warp) {
		future(() -> storage.addWarp(warp)).thenRunAsync(() -> {
			if(warp instanceof PlayerWarp) {
				PlayerWarp pw = (PlayerWarp)warp;
				pwarps.put(pw.getOwner(), pw);
			}else {
				Warp w = storage.getSystemWarp(warp.getName());
				warps.put(w.getId(), w);
			}
		});
	}

	public void saveWarp(Warp warp) {
		future(() -> storage.saveWarp(warp)).thenRun(() -> syncWarp(warp));
	}

	public void addVote(Warp warp, UUID voteur, Vote vote) {
		warp.getVotes().put(voteur.toString(), vote);
		future(() -> storage.vote(warp, voteur, vote)).thenRun(() -> syncWarp(warp));
	}

	public void removeVote(Warp warp, UUID voteur, Vote vote) {
		warp.getVotes().put(voteur.toString(), vote);
		future(() -> storage.unvote(warp, voteur)).thenRun(() -> syncWarp(warp));
	}

	public void addTag(Warp warp, String tag) {
		future(() -> storage.addTag(warp, tag)).thenRun(() -> syncWarp(warp));
	}

	public void removeTag(Warp warp, String tag) {
		future(() -> storage.removeTag(warp, tag)).thenRun(() -> syncWarp(warp));
	}

	private void syncWarp(Warp warp) {
		CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
		if(warp instanceof PlayerWarp) {
			PlayerWarp pwarp = (PlayerWarp)warp;
			plugin.getWarpSyncTopic().publish(new WarpSyncMessage(true, core.getServerName() , warp.getId(), pwarp.getOwner()));
		}else {
			plugin.getWarpSyncTopic().publish(new WarpSyncMessage(false, core.getServerName() , warp.getId(), null));
		}
	}

	public void deleteWarp(Warp warp) {
		future(() -> storage.deleteWarp(warp)).thenRun(() -> syncWarp(warp));
		if(warp instanceof PlayerWarp) {
			PlayerWarp pw = (PlayerWarp)warp;
			pwarps.remove(pw.getOwner());
		}else {
			warps.remove(warp.getId());
		}
	}

	public void teleport(Player player, Warp warp) {
		CoreBukkitPlugin.getInstance().getTeleportManager().teleport(player, warp.getLocation(), 2);

		if(warp instanceof PlayerWarp) {
			PlayerWarp pwarp = (PlayerWarp)warp;
			if(!pwarp.getOwner().equals(player.getUniqueId())) {
				RMap<String, String> tplist = CoreBukkitPlugin.getInstance().getRedisClient().getMap("WarpTpWaiting");
				tplist.fastPutAsync(player.getUniqueId().toString(), pwarp.getOwner().toString());
			}
		}
	}

	public <T> CompletableFuture<T> future(Callable<T> supplier) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return supplier.call();
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> future(Runnable runnable) {
		return CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		});
	}
	
//	public void migrateWarps() {
//		Bukkit.broadcastMessage("Migration des warps file -> sql");
//		int i = 0;
//		File dir = new File("plugins/Warps/pwarps");
//		if(dir.exists()) {
//			for(File file : dir.listFiles()) {
//				if(file.getName().endsWith(".yml")) {
//					if(file.exists()) {
//						UUID uuid = UUID.fromString(file.getName().split("\\.")[0]);
//						PlayerWarpFileUtility wfu = new PlayerWarpFileUtility(uuid);
//						Map<String, Vote> avis = new HashMap<>();
//						for(String key : wfu.getConfig().getConfigurationSection("avis").getKeys(false)) {
//							byte vote = (byte) wfu.getConfig().getInt("avis."+key);
//							if(vote >= 3) {
//								vote = 1;
//							}else {
//								vote = -1;
//							}
//							avis.put(key, new Vote(vote, System.currentTimeMillis()));
//						}
//
//						Location loc = wfu.getConfig().getLocation("location");
//
//						PlayerWarp warp = new PlayerWarp(-1,
//								UUID.fromString(wfu.getConfig().getString("owner")),
//								SLocationUtils.getSLocation(loc),
//								wfu.getConfig().getString("name"),
//								wfu.getConfig().getString("description")
//								);
//						warp.setOpened(wfu.getConfig().getBoolean("opened"));
//						warp.setVotes(avis);
//						warp.setTags(wfu.getConfig().getStringList("tags"));
//						future(() -> storage.addWarp(warp)).thenRun(() -> {
//							future(() -> {
//								return storage.getPlayerWarp(uuid);
//							}).thenAccept(pwarp -> {
//								warp.getVotes().entrySet().forEach(entry -> {
//									addVote(pwarp, UUID.fromString(entry.getKey()), entry.getValue());
//								});
//								warp.getTags().forEach(tag -> {
//									addTag(pwarp, tag);
//								});
//							});
//						});
//						i++;
//						Bukkit.broadcastMessage(i+ " - Migratiuon du warp :" + uuid);
//					}
//				}
//			}
//		}
//	}

}
