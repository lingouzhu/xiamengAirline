package xiaMengAirline.Exception;

import java.util.Date;

import xiaMengAirline.beans.AirPort;

public class AirportNotAcceptDepartureTime2 extends Exception {
	private Date plannedDeparture;
	private String casue;
	private AirPort airport;
	public Date getPlannedDeparture() {
		return plannedDeparture;
	}
	public void setPlannedDeparture(Date plannedDeparture) {
		this.plannedDeparture = plannedDeparture;
	}
	public String getCasue() {
		return casue;
	}
	public void setCasue(String casue) {
		this.casue = casue;
	}
	public AirPort getAirport() {
		return airport;
	}
	public void setAirport(AirPort airport) {
		this.airport = airport;
	}
	public AirportNotAcceptDepartureTime2(Date plannedDeparture, String casue, AirPort airport) {
		super();
		this.plannedDeparture = plannedDeparture;
		this.casue = casue;
		this.airport = airport;
	}


	
	
}
