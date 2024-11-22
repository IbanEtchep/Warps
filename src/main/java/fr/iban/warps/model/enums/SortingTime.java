package fr.iban.warps.model.enums;

public enum SortingTime {
	
	ALL(1625326740994L),
	DAY(216000000L),
	WEEK(604800000L),
	MONTH(2592000000L);
	
	private final long time;

	SortingTime(long time) {
		this.time = time;
	}
	
	public long getTimeMillis() {
		return time;
	}
}
