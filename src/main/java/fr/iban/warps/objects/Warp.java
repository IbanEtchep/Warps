package fr.iban.warps.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import fr.iban.common.teleport.SLocation;
import fr.iban.warps.utils.ChatUtils;
import fr.iban.warps.utils.SortingTime;

public class Warp {

	private int id;
	private SLocation location;
	private String name;
	private String desc = "Pas de description.";
	private Map<String, Vote> votes = new HashMap<>();
	private List<String> tags = new ArrayList<>();

	private boolean opened = true;
	protected ItemStack icon;


	public Warp(int id, SLocation location, String name, String desc) {
		this.id = id;
		this.location = location;
		this.name = name;
		if(desc != null)
			this.desc = desc;
	}

	public int getId() {
		return id;
	}

	public SLocation getLocation() {
		return location;
	}
	public void setLocation(SLocation location) {
		this.location = location;
	}
	public String getName() {
		return ChatUtils.translateColors(name);
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return ChatUtils.translateColors(desc);
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Map<String, Vote> getVotes() {
		return votes;
	}

	public void setVotes(Map<String, Vote> votes) {
		this.votes = votes;
	}
	
	public int getVotesIn(long time) {
		int votes = 0;
		if(!getVotes().isEmpty()) {
			for(Vote vote: getVotes().values()) {
				if(vote.getVote() == 1) {
					if(System.currentTimeMillis() - vote.getDate() > time) continue;
					votes++;
				}
			}
		}
		return votes;
	}

	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean opened) {
		this.opened = opened;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((desc == null) ? 0 : desc.hashCode());
		result = prime * result + ((icon == null) ? 0 : icon.hashCode());
		result = prime * result + id;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (opened ? 1231 : 1237);
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((votes == null) ? 0 : votes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Warp other = (Warp) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	

}
