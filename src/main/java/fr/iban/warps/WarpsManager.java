package fr.iban.warps;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.model.land.Land;
import fr.iban.warps.model.PlayerWarp;
import fr.iban.warps.model.Vote;
import fr.iban.warps.model.Warp;
import fr.iban.warps.storage.Storage;
import fr.iban.warps.utils.WarpSyncMessage;
import fr.iban.warps.utils.WarpTpMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class WarpsManager {

    private final WarpsPlugin plugin;
    private final Storage storage;
    private final Map<UUID, PlayerWarp> pwarps = new HashMap<>();
    private final Map<Integer, Warp> warps = new HashMap<>();
    private final Map<UUID, UUID> warpTpWaiting = new HashMap<>();
    private final List<String> tags = Arrays.asList("#shop", "#farm", "#ville", "#maison", "#architecture");

    public String SYNC_CHANNEL = "WarpSync";
    public String TP_WAITING_CHANNEL = "WarpTpWaiting";


    public WarpsManager(WarpsPlugin plugin) {
        this.plugin = plugin;
        this.storage = new Storage(plugin);
        loadWarps();
    }

    public Map<UUID, PlayerWarp> getPlayerWarps() {
        return pwarps;
    }

    public List<PlayerWarp> getOpenedPlayerWarps() {
        return pwarps.values().stream().filter(PlayerWarp::isOpened).toList();
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
        for (Warp warp : getWarps().values()) {
            if (warp.getName().equalsIgnoreCase(name)) {
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
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
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
        Land land = LandsPlugin.getInstance().getLandRepository().getLandAt(player.getChunk());
        if (land.isBypassing(player, Action.SET_WARP)) {
            if (!getPlayerWarps().containsKey(uuid)) {
                PlayerWarp warp = new PlayerWarp(-1, uuid, SLocationUtils.getSLocation(player.getLocation()), name, desc);
                pwarps.put(uuid, warp);
                createWarp(warp);
                player.sendMessage("§aVotre warp a bien été crée.");
            } else {
                player.sendMessage("§cVous avez déjà un warp.");
            }
        } else {
            player.sendMessage("§cVous devez vous trouver dans un de vos claims.");
        }
    }

    public void removePlayerWarp(Player player) {
        UUID uuid = player.getUniqueId();
        Warp warp = getPlayerWarp(uuid);
        if (warp != null) {
            deleteWarp(warp);
            pwarps.remove(uuid);
            player.sendMessage("§aLe warp a bien été supprimé.");
        } else {
            player.sendMessage("§cVous n'avez pas de warp.");
        }
    }

    public CompletableFuture<List<PlayerWarp>> getPlayersWarpsAsync() {
        return future(storage::getPlayersWarps);
    }

    public CompletableFuture<List<Warp>> getWarpsAsync() {
        return future(storage::getWarps);
    }

    public void updateWarp(UUID uuid) {
        pwarps.remove(uuid);
        future(() -> storage.getPlayerWarp(uuid)).thenAccept(pwarp -> {
            if (pwarp != null) {
                pwarps.put(uuid, pwarp);
            }
        });
    }

    public void updateWarp(int id) {
        warps.remove(id);
        future(() -> storage.getWarp(id)).thenAccept(warp -> {
            if (warp != null) {
                warps.put(id, warp);
            }
        });
    }

    public void createWarp(Warp warp) {
        future(() -> storage.addWarp(warp)).thenRunAsync(() -> {
            if (warp instanceof PlayerWarp) {
                PlayerWarp pw = storage.getPlayerWarp(((PlayerWarp) warp).getOwner());
                pwarps.put(pw.getOwner(), pw);
            } else {
                Warp w = storage.getSystemWarp(warp.getName());
                warps.put(w.getId(), w);
            }
        }).thenRun(() -> syncWarp(warp));
    }

    public void saveWarp(Warp warp) {
        future(() -> storage.saveWarp(warp)).thenRun(() -> syncWarp(warp));
    }

    public void addVote(Warp warp, UUID voteur, Vote vote) {
        warp.getVotes().put(voteur.toString(), vote);
        future(() -> storage.vote(warp, voteur, vote)).thenRun(() -> syncWarp(warp));
    }

    public void addTag(Warp warp, String tag) {
        future(() -> storage.addTag(warp, tag)).thenRun(() -> syncWarp(warp));
    }

    public void removeTag(Warp warp, String tag) {
        future(() -> storage.removeTag(warp, tag)).thenRun(() -> syncWarp(warp));
    }

    private void syncWarp(Warp warp) {
        CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
        if (warp instanceof PlayerWarp playerWarp) {
            core.getMessagingManager().sendMessage(SYNC_CHANNEL, new WarpSyncMessage(true, warp.getId(), playerWarp.getOwner()));
        } else {
            core.getMessagingManager().sendMessage(SYNC_CHANNEL, new WarpSyncMessage(false, warp.getId(), null));
        }
    }

    public void deleteWarp(Warp warp) {
        future(() -> storage.deleteWarp(warp)).thenRun(() -> syncWarp(warp));
        if (warp instanceof PlayerWarp playerWarp) {
            pwarps.remove(playerWarp.getOwner());
        } else {
            warps.remove(warp.getId());
        }
    }

    public void teleport(Player player, Warp warp) {
        CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
        core.getTeleportManager().teleport(player, warp.getLocation(), 2);

        if (warp instanceof PlayerWarp playerWarp) {
            if (!playerWarp.getOwner().equals(player.getUniqueId())) {
                core.getMessagingManager().sendMessage(TP_WAITING_CHANNEL, new WarpTpMessage(player.getUniqueId(), playerWarp.getOwner()));
                addWarpTpWaiting(player.getUniqueId(), playerWarp.getOwner());
            }
        }
    }

    public void addWarpTpWaiting(UUID uuid, UUID warpOwnerId) {
        warpTpWaiting.put(uuid, warpOwnerId);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (warpTpWaiting.containsKey(uuid)) {
                if (warpTpWaiting.get(uuid).equals(warpOwnerId)) {
                    warpTpWaiting.remove(uuid);
                }
            }
        }, 80L);
    }

    public Map<UUID, UUID> getWarpTpWaiting() {
        return warpTpWaiting;
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
                throw (RuntimeException) e;
            }
        });
    }

}
