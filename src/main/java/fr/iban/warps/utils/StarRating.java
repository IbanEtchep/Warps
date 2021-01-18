package fr.iban.warps.utils;

import fr.iban.warps.PlayerWarp;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class StarRating {
	
	public static String getStars(double rate) {
		String stars = "§6";
		//★☆⯪
		
		for (int i = 1; i <= 5; i++) {
			if(i <= rate) {
				stars += "★";
			}else if(i-0.5 <= rate){
				stars += "⯪";
			}else {
				stars += "☆";
			}
		}
		return stars;
	}
	
	public static BaseComponent[] getNoteStars(PlayerWarp warp) {
		BaseComponent[] one = new ComponentBuilder("☆").event(ChatUtils.getShowTextHoverEvent(getStars(1))).event(new ClickEvent(Action.RUN_COMMAND, "/pwarp rate " + warp.getOwner().toString() + " "+ 1)).create();
		BaseComponent[] two = new ComponentBuilder("☆").event(ChatUtils.getShowTextHoverEvent(getStars(2))).event(new ClickEvent(Action.RUN_COMMAND, "/pwarp rate " + warp.getOwner().toString() + " "+ 2)).create();
		BaseComponent[] three = new ComponentBuilder("☆").event(ChatUtils.getShowTextHoverEvent(getStars(3))).event(new ClickEvent(Action.RUN_COMMAND, "/pwarp rate " + warp.getOwner().toString() + " "+ 3)).create();
		BaseComponent[] four = new ComponentBuilder("☆").event(ChatUtils.getShowTextHoverEvent(getStars(4))).event(new ClickEvent(Action.RUN_COMMAND, "/pwarp rate " + warp.getOwner().toString() + " "+ 4)).create();
		BaseComponent[] five = new ComponentBuilder("☆").event(ChatUtils.getShowTextHoverEvent(getStars(5))).event(new ClickEvent(Action.RUN_COMMAND, "/pwarp rate " + warp.getOwner().toString() + " "+ 5)).create();
		return new ComponentBuilder().color(ChatColor.GOLD).append(one).append(two).append(three).append(four).append(five).create();
	}
}
