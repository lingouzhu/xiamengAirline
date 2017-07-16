package xiaMengAirline.Exception;

import xiaMengAirline.beans.Flight;

public class FlightDurationNotFound extends Exception {
	Flight theFlight;
	String searchKey;
	public Flight getTheFlight() {
		return theFlight;
	}
	public void setTheFlight(Flight theFlight) {
		this.theFlight = theFlight;
	}
	public String getSearchKey() {
		return searchKey;
	}
	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}
	public FlightDurationNotFound(Flight theFlight, String searchKey) {
		super();
		this.theFlight = theFlight;
		this.searchKey = searchKey;
	}
	

}
