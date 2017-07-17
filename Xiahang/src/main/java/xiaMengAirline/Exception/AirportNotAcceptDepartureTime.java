package xiaMengAirline.Exception;

import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.FlightTime;

public class AirportNotAcceptDepartureTime extends Exception {
	private Flight aFlight;
	private FlightTime availableTime;
	public Flight getaFlight() {
		return aFlight;
	}
	public void setaFlight(Flight aFlight) {
		this.aFlight = aFlight;
	}
	public FlightTime getAvailableTime() {
		return availableTime;
	}
	public void setAvailableTime(FlightTime availableTime) {
		this.availableTime = availableTime;
	}
	public AirportNotAcceptDepartureTime(Flight aFlight, FlightTime availableTime) {
		super();
		this.aFlight = aFlight;
		this.availableTime = availableTime;
	}
	
	
}
