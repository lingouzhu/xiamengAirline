package xiaMengAirline.backup.Exception;

import xiaMengAirline.backup.beans.FlightBackup;
import xiaMengAirline.backup.beans.FlightTimeBackup;

public class AirportNotAcceptDepartureTimeBackup extends Exception {
	private FlightBackup aFlight;
	private FlightTimeBackup availableTime;
	private String casue;
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
	public AirportNotAcceptDepartureTimeBackup(FlightBackup aFlight, FlightTimeBackup availableTime, String cause) {
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
