package fr.iban.warps.commands;

import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.model.land.Land;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.WarpsPlugin;
import fr.iban.warps.model.PlayerWarp;
import fr.iban.warps.model.Vote;
import fr.iban.warps.model.Warp;
import fr.iban.warps.utils.TagCompleter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.command.CommandActor;

@Command({"pwarp", "pw", "playerwarp"})
public class PlayerWarpCMD {

    private final WarpsManager manager;
    private final WarpsPlugin plugin;

    public PlayerWarpCMD(WarpsPlugin plugin) {
        this.manager = plugin.getWarpManager();
        this.plugin = plugin;
    }

    @CommandPlaceholder
    public void pwarp(BukkitCommandActor actor) {
        help(actor);
    }

    @Subcommand("help")
    public void help(BukkitCommandActor actor) {
        actor.reply("§8Aide pour la commande /pwarp :");
        actor.reply("");
        actor.reply("§6/pwarp create §f→ créer votre warp.");
        actor.reply("§6/pwarp delete §f→ supprimer votre warp.");
        actor.reply("§6/pwarp tag add/remove <#tag> §f→ Ajouter/retirer un tag.");
        actor.reply("§6/pwarp setlocation §f→ changer l'emplacement de votre warp.");
        actor.reply("§6/pwarp setname §f→ changer le nom votre warp.");
        actor.reply("§6/pwarp setdesc §f→ changer la description de votre warp.");
        actor.reply("§6/pwarp open §f→ ouvrir votre warp.");
        actor.reply("§6/pwarp close §f→ fermer votre warp.");
        actor.reply("");
        actor.reply("§6/pwarp visit <joueur> §f→ accéder au warp d'un joueur.");
    }

    @Subcommand("create")
    public void create(Player player) {
        manager.setPlayerWarp(player, "&aWarp de &2" + player.getName(), null);
    }

    @Subcommand("delete")
    public void delete(Player player, @Optional OfflinePlayer target) {
        if (target == null) {
            manager.removePlayerWarp(player);
        } else if (player.hasPermission("warps.admin")) {
            PlayerWarp targetWarp = manager.getPlayerWarp(target.getUniqueId());
            if (targetWarp != null) {
                manager.deleteWarp(targetWarp);
                player.sendMessage("§cLe warps de " + target.getName() + " a été supprimé.");
            } else {
                player.sendMessage("§cCe joueur n'a pas de warp.");
            }
        }
    }


    @Subcommand("tag")
    public void tagInfo(Player player) {
        player.sendMessage("/pwarp tag add/remove <#tag>");
    }

    @Subcommand("tag add")
    @Usage("/pwarp tag add <#tag>")
    public void tagAdd(Player player, @TagCompleter String tag) {
        PlayerWarp warp = manager.getPlayerWarp(player.getUniqueId());

        if (exist(player, warp)) {
            if (manager.getTags().contains(tag)) {
                if (!warp.getTags().contains(tag)) {
                    warp.getTags().add(tag);
                    manager.addTag(warp, tag);
                    player.sendMessage("§aLe tag " + tag + " a été ajouté à votre warp.");
                } else {
                    player.sendMessage("§cVotre warp a déjà ce tag.");
                }
            } else {
                player.sendMessage("§cCe tag n'existe pas.");
            }
        }
    }


    @Subcommand("tag remove")
    @Usage("/pwarp tag remove <#tag>")
    public void tagRemove(Player player, @TagCompleter String tag) {
        PlayerWarp warp = manager.getPlayerWarp(player.getUniqueId());

        if (exist(player, warp)) {
            if (warp.getTags().contains(tag)) {
                warp.getTags().remove(tag);
                manager.removeTag(warp, tag);
                player.sendMessage("§cLe tag " + tag + " a été retité de votre warp.");
            } else {
                player.sendMessage("§cVotre warp n'a pas ce tag.");
            }
        }
    }

    @Subcommand("setlocation")
    public void setLocation(Player player) {
        PlayerWarp warp = manager.getPlayerWarp(player.getUniqueId());

        if (exist(player, warp)) {
            Land land = LandsPlugin.getInstance().getLandRepository().getLandAt(player.getChunk());
            if (land.isBypassing(player, Action.SET_WARP)) {
                warp.setLocation(SLocationUtils.getSLocation(player.getLocation()));
                manager.saveWarp(warp);
                player.sendMessage("§aPosition de votre warp redéfinie à la position où vous vous trouvez.");
            } else {
                player.sendMessage("§cVous devez vous trouver dans un de vos claims.");
            }
        }
    }

    @Subcommand("setname")
    public void setName(Player player, @Optional String name) {
        PlayerWarp warp = manager.getPlayerWarp(player.getUniqueId());

        if (name != null) {
            if (exist(player, warp)) {
                if (name.length() <= 32) {
                    warp.setName(name);
                    manager.saveWarp(warp);
                    player.sendMessage("§aNouveau nom : §r" + warp.getName());
                } else {
                    player.sendMessage("§cLe nom ne doit pas dépasser les 32 caractères.");
                }
            }
        } else {
            player.sendMessage("/warp changename <nouveau nom>");
        }
    }

    @Subcommand("setdesc")
    public void setDesc(Player player, @Optional String desc) {
        PlayerWarp warp = manager.getPlayerWarp(player.getUniqueId());

        if (desc != null) {
            if (exist(player, warp)) {
                warp.setDesc(desc);
                manager.saveWarp(warp);
                player.sendMessage("§aNouvelle description : §r" + warp.getDesc());
            }
        } else {
            player.sendMessage("/warp changedesc <nouvelle description>");
        }
    }

    @Subcommand("close")
    public void close(Player player, @Optional OfflinePlayer target) {
        PlayerWarp warp;

        if (target == null) {
            warp = manager.getPlayerWarp(player.getUniqueId());
        } else if (player.hasPermission("warps.admin")) {
            warp = manager.getPlayerWarp(target.getUniqueId());
        } else return;

        if (exist(player, warp)) {
            if (warp.isOpened()) {
                warp.setOpened(false);
                manager.saveWarp(warp);
                player.sendMessage("§cVous avez fermé votre warp.");
            } else {
                player.sendMessage("§cVotre warp est déjà fermé.");
            }
        }
    }

    @Subcommand("open")
    public void open(Player player, @Optional OfflinePlayer target) {
        PlayerWarp warp;

        if (target == null) {
            warp = manager.getPlayerWarp(player.getUniqueId());
        } else if (player.hasPermission("warps.admin")) {
            warp = manager.getPlayerWarp(target.getUniqueId());
        } else return;

        if (exist(player, warp)) {
            if (!warp.isOpened()) {
                warp.setOpened(true);
                manager.saveWarp(warp);
                player.sendMessage("§aVous avez ouvert votre warp.");
            } else {
                player.sendMessage("§cVotre warp est déjà ouvert.");
            }
        }
    }

    @Subcommand("visit")
    public void visit(Player player, PlayerWarp warp) {
        if (warp.isOpened()) {
            manager.teleport(player, warp);
        } else {
            if (player.hasPermission("spartacube.warps.bypassclosed")) {
                player.sendMessage("§aCe warp est fermé mais vous avez bypassé la fermeture !");
                manager.teleport(player, warp);
            } else {
                player.sendMessage("§cCe warp est fermé !");
            }
        }
    }

    @Subcommand("like")
    @Cooldown(value = 10)
    public void like(Player player, PlayerWarp targetWarp) {
        if (targetWarp.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§cVous ne pouvez pas liker votre propre warp.");
            return;
        }

        byte note = 1;
        long date = System.currentTimeMillis();
        if (targetWarp.getVotes().containsKey(player.getUniqueId().toString())) {
            Vote vote = targetWarp.getVotes().get(player.getUniqueId().toString());
            date = vote.getDate();
            if (vote.getVote() == 1) {
                note = 0;
            }
        }

        if (note == 1) {
            player.sendMessage("§aVous avez ajouté un j'aime à ce warp.");
        } else {
            player.sendMessage("§cVous n'aimez désormais plus ce warp.");
        }

        manager.addVote(targetWarp, player.getUniqueId(), new Vote(note, date));
        Player owner = Bukkit.getPlayer(targetWarp.getOwner());
        if (owner != null) {
            if (note == 1) {
                owner.sendMessage("§a " + player.getName() + " aime votre warp.");
            } else {
                owner.sendMessage("§c " + player.getName() + " n'aime plus votre warp.");
            }
        }
    }


    private boolean exist(Player player, Warp warp) {
        if (warp != null) {
            return true;
        } else {
            player.sendMessage("§cVous n'avez pas de warp.");
            return false;
        }
    }
}
