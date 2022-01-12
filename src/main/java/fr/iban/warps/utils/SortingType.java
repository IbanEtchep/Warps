package fr.iban.warps.utils;

public enum SortingType {
	
	ALL(1625326740994L),
	DAY(216000000L),
	WEEK(604800000L),
	MONTH(2592000000L);
	
	private long time;

	private SortingType(long time) {
		this.time = time;
	}
	
	public long getTime() {
		return time;
	}
}
