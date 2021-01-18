package fr.iban.warps.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import fr.iban.claims.ClaimsPlugin;
import fr.iban.claims.objects.Claim;
import fr.iban.claims.objects.claimtypes.PlayerClaim;
import fr.iban.claims.utils.ClaimAction;
import fr.iban.warps.PlayerWarp;
import fr.iban.warps.Warp;
import fr.iban.warps.Warps;
import fr.iban.warps.WarpsManager;
import fr.iban.warps.utils.ChatUtils;
import fr.iban.warps.utils.StarRating;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class PlayerWarpCMD implements CommandExecutor, TabCompleter{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		WarpsManager wm = Warps.getInstance().getWarpManager();
		if(sender instanceof Player) {
			Player player = (Player)sender;
			UUID uuid = player.getUniqueId();
			PlayerWarp warp = wm.getPlayerWarp(uuid);

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
					wm.setPlayerWarp(player, "&aWarp de &2" + player.getName(), null);
					break;
				case "remove":
					wm.removePlayerWarp(player);
					break;
				case "tag":
					if(exist(player, warp)) {
						if(args.length == 3) {
							String tag = args[2];
							if(args[1].equalsIgnoreCase("add")) {
								if(wm.getTags().contains(tag)) {
									if(!warp.getTags().contains(tag)) {
										warp.getTags().add(tag);
										warp.save();
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
									warp.save();
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
						Claim claim = ClaimsPlugin.getInstance().getClaimManager().getClaimAt(player.getChunk());
						if(claim instanceof PlayerClaim && claim.isBypassing(player, ClaimAction.SET_WARP)) {
							warp.setLocation(player.getLocation());
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
							player.sendMessage("§aVous avez ouvert votre warp.");
						}else {
							player.sendMessage("§cVotre warp est déjà ouvert.");
						}
					}
					break;
				case "visit":
					if(args.length == 2) {
						Player target = Bukkit.getPlayer(args[1]);
						if(target != null) {
							PlayerWarp twarp = wm.getPlayerWarp(target.getUniqueId());
							if(twarp != null) {
								wm.teleport(player, twarp);
							}else {
								player.sendMessage("§cCe joueur n'a pas de warp.");
							}
						}else {
							player.sendMessage("§cCe joueur n'est pas en ligne.");
						}
					}
					break;
				case "rate":
					if(args.length == 3) {
						UUID id = UUID.fromString(args[1]);
						short note = Short.parseShort(args[2]);
						PlayerWarp targetWarp = wm.getPlayerWarp(id);
						if(targetWarp == null) return false;
						if(note >= 0 && note <= 5) {
							targetWarp.getAvis().put(uuid.toString(), note);
							targetWarp.save();
							player.sendMessage("§aVotre avis ("+ StarRating.getStars(note) +"§a) a bien été enregistrée.");
						}else {
							player.sendMessage("La note doit être entre 0 et 5.");
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

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> list = new ArrayList<>();
		if(sender instanceof Player) {
			Player player = (Player)sender;
			WarpsManager wm = Warps.getInstance().getWarpManager();
			if(args.length == 1) {
				list.add("create");
				list.add("remove");
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
					list.add("remove");
					list.add("add");
				}
				if(args[0].equalsIgnoreCase("visit")) {
					Bukkit.getOnlinePlayers().forEach(p -> list.add(p.getName()));
				}
			}else if(args.length == 3){
				if(args[0].equalsIgnoreCase("tag")) {
					PlayerWarp warp = wm.getPlayerWarp(player.getUniqueId());
					if(exist(player, warp)) {
						if(args[1].equalsIgnoreCase("add")) {
							list.addAll(wm.getTags());
							list.removeAll(warp.getTags());
							return list;
						}
						if(args[1].equalsIgnoreCase("remove")) {
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
