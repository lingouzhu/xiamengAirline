package xiaMengAirline.Exception;

import xiaMengAirline.beans.FlightBackup;
import xiaMengAirline.beans.FlightTimeBackup;

public class AirportNotAcceptArrivalTimeBackup extends Exception {
	private FlightBackup aFlight;
	private FlightTimeBackup availableTime;
	public FlightBackup getaFlight() {
		return aFlight;
	}
	public void setaFlight(FlightBackup aFlight) {
		this.aFlight = aFlight;
	}
	public FlightTimeBackup getAvailableTime() {
		return availableTime;
	}
	public void setAvailableTime(FlightTimeBackup availableTime) {
		this.availableTime = availableTime;
	}
	public AirportNotAcceptArrivalTimeBackup(FlightBackup aFlight, FlightTimeBackup availableTime) {
		super();
		this.aFlight = aFlight;
		this.availableTime = availableTime;
	}
	
	
	

}
