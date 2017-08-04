package xiaMengAirline.beans;

import java.util.Date;


/**
 * The FlightTime class specify suggested flight arrival / departure time,
 * @author Leonard
 */
public class FlightTimeBackup {
	/** Represents suggested arrival time.
	*/
	private Date arrivalTime;
	/** Represents suggested departure time.
	*/
	private Date departureTime;
	/** Represents if the reschedule is caused by typhoon.
	*/
	private boolean IsTyphoon = false;
	
	public Date getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public Date getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(Date departureTime) {
		this.departureTime = departureTime;
	}
	public boolean isIsTyphoon() {
		return IsTyphoon;
	}
	public void setIsTyphoon(boolean isTyphoon) {
		IsTyphoon = isTyphoon;
	}
	
	

}
