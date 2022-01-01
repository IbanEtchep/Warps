package fr.iban.warps.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import fr.iban.bukkitcore.utils.SLocationUtils;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.objects.Land;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.objects.PlayerWarp;
import fr.iban.warps.objects.Vote;
import fr.iban.warps.objects.Warp;
import fr.iban.warps.utils.ChatUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class PlayerWarpCMD implements CommandExecutor, TabCompleter{

	private WarpsManager manager;

	public PlayerWarpCMD(WarpsManager manager) {
		this.manager = manager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player)sender;
			UUID uuid = player.getUniqueId();
			PlayerWarp warp = manager.getPlayerWarp(uuid);

			if(args.length == 0) {
				player.sendMessage("§8Aide pour la commande /pwarp :");
				player.sendMessage("");
				player.sendMessage(getCommandUsage("/pwarp create", "créer votre warp."));
				player.sendMessage(getCommandUsage("/pwarp remove", "supprimer votre warp."));
				player.sendMessage(getCommandUsage("/pwarp tag add/remove <#tag>", "Ajouter/retirer un tag."));
				player.sendMessage(getCommandUsage("/pwarp changepoint", "changer l'emplacement de votre warp."));
				player.sendMessage(getCommandUsage("/pwarp changename <nouveau nom>", "changer le nom de votre warp."));
				player.sendMessage(getCommandUsage("/pwarp changedesc <nouvelle description>", "changer la description de votre warp."));
				player.sendMessage(getCommandUsage("/pwarp open", "ouvrir votre warp"));
				player.sendMessage(getCommandUsage("/pwarp close", "fermer votre warp."));
				player.sendMessage("");
				player.sendMessage(getCommandUsage("/warp visit <joueur>", "Accéder au warp d'un joueur."));
				player.sendMessage("");

			}else {
				switch (args[0]) {
				case "create":
					manager.setPlayerWarp(player, "&aWarp de &2" + player.getName(), null);
					break;
				case "delete":
					if(args.length == 1) {
						manager.removePlayerWarp(player);
					}else if(args.length == 2 && sender.hasPermission("spartacube.deletewarps")) {
						@SuppressWarnings("deprecation")
						OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
						PlayerWarp targetWarp = manager.getPlayerWarp(target.getUniqueId());
						if(target != null && targetWarp != null) {
							manager.deleteWarp(targetWarp);
							sender.sendMessage("§cLe warps de " + target.getName() + " a été supprimé.");
						}
					}
					break;
				case "tag":
					if(exist(player, warp)) {
						if(args.length == 3) {
							String tag = args[2];
							if(args[1].equalsIgnoreCase("add")) {
								if(manager.getTags().contains(tag)) {
									if(!warp.getTags().contains(tag)) {
										warp.getTags().add(tag);
										manager.addTag(warp, tag);
										player.sendMessage("§aLe tag " + tag + " a été ajouté à votre warp.");
									}else {
										player.sendMessage("§cVotre warp a déjà ce tag.");
									}
								}else {
									player.sendMessage("§cCe tag n'éxiste pas.");
								}
							}else if(args[1].equalsIgnoreCase("remove")) {
								if(warp.getTags().contains(tag)) {
									warp.getTags().remove(tag);
									manager.removeTag(warp, tag);
									player.sendMessage("§cLe tag " + tag + " a été retité de votre warp.");
								}else {
									player.sendMessage("§cVotre warp n'a pas ce tag.");
								}
							}
						}else {
							player.sendMessage("/pwarp tag add/remove <#tag>");
						}
					}
					break;
				case "changepoint":
					if(exist(player, warp)) {
						Land land = LandsPlugin.getInstance().getLandManager().getLandAt(player.getChunk());
						if(land.isBypassing(player, Action.SET_WARP)) {
							warp.setLocation(SLocationUtils.getSLocation(player.getLocation()));
							manager.saveWarp(warp);
							player.sendMessage("§aPosition de votre warp redéfinie à la position où vous vous trouvez.");
						}else {
							player.sendMessage("§cVous devez vous trouver dans un de vos claims.");
						}
					}
					break;
				case "changename":
					if(args.length > 1) {
						if(exist(player, warp)) {
							StringBuilder bc = new StringBuilder();
							for (int i = 1; i < args.length; i++) {
								bc.append(args[i] + " ");
							}
							String name = bc.toString();
							if(name.length() <= 32) {
								warp.setName(name);
								manager.saveWarp(warp);
								player.sendMessage("§aNouveau nom : §r" + warp.getName());
							}else {
								player.sendMessage("§cLe nom ne doit pas dépasser les 32 caractères.");
							}
						}
					}else {
						player.sendMessage("/warp changename <nouveau nom>");
					}
					break;
				case "changedesc":
					if(args.length > 1) {
						if(exist(player, warp)) {
							StringBuilder bc = new StringBuilder();
							for (int i = 1; i < args.length; i++) {
								bc.append(args[i] + " ");
							}
							String desc = bc.toString();
							warp.setDesc(desc);
							manager.saveWarp(warp);
							player.sendMessage("§aNouvelle description : §r" + warp.getDesc());
						}
					}else {
						player.sendMessage("/warp changedesc <nouvelle description>");
					}
					break;
				case "close":
					if(exist(player, warp)) {
						if(warp.isOpened()) {
							warp.setOpened(false);
							manager.saveWarp(warp);
							player.sendMessage("§cVous avez fermé votre warp.");
						}else {
							player.sendMessage("§cVotre warp est déjà fermé.");
						}
					}
					break;
				case "open":
					if(exist(player, warp)) {
						if(!warp.isOpened()) {
							warp.setOpened(true);
							manager.saveWarp(warp);
							player.sendMessage("§aVous avez ouvert votre warp.");
						}else {
							player.sendMessage("§cVotre warp est déjà ouvert.");
						}
					}
					break;
				case "visit":
					if(args.length == 2) {
						OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[1]);
						if(target != null){
							PlayerWarp twarp = manager.getPlayerWarp(target.getUniqueId());
							if(twarp != null) {
								if(twarp.isOpened()) {
									manager.teleport(player, twarp);
								}else {
									if(player.hasPermission("spartacube.warps.bypassclosed")) {
										player.sendMessage("§aCe warp est fermé mais vous avez bypassé la fermeture !");
										manager.teleport(player, twarp);
									}else {
										player.sendMessage("§cCe warp est fermé !");
									}
								}
							}else {
								player.sendMessage("§cCe joueur n'a pas de warp.");
							}
						}else{
							player.sendMessage("§cCe joueur n'a jamais joué sur le serveur.");
						}

					}
					break;

					//				case "migrate":
					//					if(sender.hasPermission("warps.migrate")) {
					//						player.sendMessage("migration..");
					//						manager.migrateWarps();
					//					}
					//					break;
				case "rate":
					if(args.length == 2) {
						UUID id = UUID.fromString(args[1]);
						PlayerWarp targetWarp = manager.getPlayerWarp(id);
						if(targetWarp != null) {
							byte note = 1;
							long date = System.currentTimeMillis();
								if(targetWarp.getVotes().containsKey(uuid.toString())) {
									Vote vote = targetWarp.getVotes().get(uuid.toString());
									date = vote.getDate();
									if(vote.getVote() == 1) {
										note = 0;
									}
									long timeSinceVote = System.currentTimeMillis() - vote.getDate();
									if(timeSinceVote < 10000) {
										player.sendMessage("§cPas si vite ! Vous devez attendre " + (10000-timeSinceVote)/1000 + " secondes avant de changer votre vote !.");
										return false;
									}
								}
								
								if(note == 1) {
									player.sendMessage("§aVous avez ajouté un j'aime à ce warp.");
								}else {
									player.sendMessage("§cVous n'aimez désormais plus ce warp.");
								}
								
								manager.addVote(targetWarp, uuid, new Vote(note, date));
								Player owner = Bukkit.getPlayer(targetWarp.getOwner());
								if(owner != null) {
									if(note == 1) {
										owner.sendMessage("§a " + player.getName() + " aime votre warp.");
									}else {
										owner.sendMessage("§c " + player.getName() + " n'aime plus votre warp.");
									}
								}
						}else {
							player.sendMessage("§cCe joueur n'a pas de warp !");
						}
					}
					break;
				default:
					break;
				}
			}
		}
		return false;
	}

	private boolean exist(Player player, Warp warp) {
		if(warp != null) {
			return true;
		}else {
			player.sendMessage("§cVous n'avez pas de warp.");
			return false;
		}
	}
	
	private LoadingCache<UUID, String> nameCache = Caffeine.newBuilder().build(uuid -> Bukkit.getOfflinePlayer(uuid).getName());

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> list = new ArrayList<>();
		if(sender instanceof Player) {
			Player player = (Player)sender;
			if(args.length == 1) {
				list.add("create");
				list.add("delete");
				list.add("changepoint");
				list.add("changedesc");
				list.add("changename");
				list.add("open");
				list.add("close");
				list.add("tag");
				list.add("visit");
				return list;
			}else if(args.length == 2) {
				if(args[0].equalsIgnoreCase("tag")) {
					list.add("delete");
					list.add("add");
				}
				if(args[0].equalsIgnoreCase("visit")) {
					List<String> warplist = manager.getPlayerWarps().values().stream().filter(warp -> warp.isOpened() && nameCache.get(warp.getOwner()) != null).map(warp -> nameCache.get(warp.getOwner())).collect(Collectors.toList());
					return warplist.stream().filter(string -> string.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
				}
			}else if(args.length == 3){
				if(args[0].equalsIgnoreCase("tag")) {
					PlayerWarp warp = manager.getPlayerWarp(player.getUniqueId());
					if(exist(player, warp)) {
						if(args[1].equalsIgnoreCase("add")) {
							list.addAll(manager.getTags());
							list.removeAll(warp.getTags());
							return list;
						}
						if(args[1].equalsIgnoreCase("delete")) {
							return warp.getTags();
						}
					}
				}
				return list;
			}
		}
		return list;
	}

	private BaseComponent[] getCommandUsage(String command, String desc) {
		ComponentBuilder builder = new ComponentBuilder("- ").color(ChatColor.GRAY);
		builder.append(new ComponentBuilder(command)
				.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
				.event(ChatUtils.getShowTextHoverEvent(ChatColor.GRAY + "Clic"))
				.color(ChatColor.DARK_GRAY).create());
		builder.append(new ComponentBuilder(" - ").color(ChatColor.GRAY).append(desc).color(ChatColor.GRAY).create());
		return builder.create();
	}

}
