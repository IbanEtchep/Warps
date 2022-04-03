package fr.iban.warps.objects;

public class Vote {
	
	private byte vote;
	private long date;
	
	public Vote(byte vote, long date) {
		this.vote = vote;
		this.date = date;
	}

	public byte getVote() {
		return vote;
	}
	
	public void setVote(byte vote) {
		this.vote = vote;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

}
