package xiaMengAirline.beans;

import java.util.Date;

public class FlightWithEmptySeat {
	private int flightID;
	private int emptySeatNo;
	private Date departureTime;
	
	public FlightWithEmptySeat(int flightID, int emptySeatNo, Date departureTime) {
		this.flightID = flightID;
		this.emptySeatNo = emptySeatNo;
		this.departureTime = departureTime;
	}
	
	public int getFlightID() {
		return flightID;
	}
	public void setFlightID(int flightID) {
		this.flightID = flightID;
	}
	public int getEmptySeatNo() {
		return emptySeatNo;
	}
	public void setEmptySeatNo(int emptySeatNo) {
		this.emptySeatNo = emptySeatNo;
	}

	public Date getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(Date departureTime) {
		this.departureTime = departureTime;
	}
	
}