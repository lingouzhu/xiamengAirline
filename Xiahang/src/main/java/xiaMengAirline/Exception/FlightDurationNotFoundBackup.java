package xiaMengAirline.Exception;

import xiaMengAirline.beans.FlightBackup;

public class FlightDurationNotFoundBackup extends Exception {
	FlightBackup theFlight;
	String searchKey;
	public FlightBackup getTheFlight() {
		return theFlight;
	}
	public void setTheFlight(FlightBackup theFlight) {
		this.theFlight = theFlight;
	}
	public String getSearchKey() {
		return searchKey;
	}
	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}
	public FlightDurationNotFoundBackup(FlightBackup theFlight, String searchKey) {
		super();
		this.theFlight = theFlight;
		this.searchKey = searchKey;
	}
	

}
