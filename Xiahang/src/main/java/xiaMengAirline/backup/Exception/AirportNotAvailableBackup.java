package xiaMengAirline.backup.Exception;

import xiaMengAirline.backup.beans.FlightBackup;
import xiaMengAirline.backup.beans.FlightTimeBackup;

public class AirportNotAvailableBackup extends Exception {
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
	public AirportNotAvailableBackup(FlightBackup aFlight, FlightTimeBackup availableTime) {
		super();
		this.aFlight = aFlight;
		this.availableTime = availableTime;
	}
	
}
