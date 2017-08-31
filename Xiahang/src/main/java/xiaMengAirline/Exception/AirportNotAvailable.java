package xiaMengAirline.Exception;

import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.RequestTime;

public class AirportNotAvailable extends Exception {
	private Flight aFlight;
	private RequestTime availableTime;
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
	public AirportNotAvailable(Flight aFlight, RequestTime availableTime) {
		super();
		this.aFlight = aFlight;
		this.availableTime = availableTime;
	}
	
}
