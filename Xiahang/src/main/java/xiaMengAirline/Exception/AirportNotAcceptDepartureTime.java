package xiaMengAirline.Exception;

import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.RequestTime;

public class AirportNotAcceptDepartureTime extends Exception {
	private Flight aFlight;
	private RequestTime availableTime;
	private String casue;
	public Flight getaFlight() {
		return aFlight;
	}
	public void setaFlight(Flight aFlight) {
		this.aFlight = aFlight;
	}
	public RequestTime getAvailableTime() {
		return availableTime;
	}
	public void setAvailableTime(RequestTime availableTime) {
		this.availableTime = availableTime;
	}
	public AirportNotAcceptDepartureTime(Flight aFlight, RequestTime availableTime, String cause) {
		super();
		this.aFlight = aFlight;
		this.availableTime = availableTime;
		this.casue = cause;
	}
	public String getCasue() {
		return casue;
	}
	public void setCasue(String casue) {
		this.casue = casue;
	}
	
	
}
