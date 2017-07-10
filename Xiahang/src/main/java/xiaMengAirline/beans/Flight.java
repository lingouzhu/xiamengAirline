package xiaMengAirline.beans;

import java.math.BigDecimal;
import java.util.Date;

public class Flight implements Cloneable {
	private String flightId;
	private Date schdDate;
	private AirPort sourceAirPort;
	private AirPort desintationAirport;
	private boolean interFlg;
	private int schdNo;
	private Date arrivalTime;
	private Date departureTime;
	//private Aircraft assignedFlight;
	private double impCoe;
	
	public String getFlightId() {
		return flightId;
	}
	public void setFlightId(String id) {
		this.flightId = id;
	}
	public AirPort getSourceAirPort() {
		return sourceAirPort;
	}
	public void setSourceAirPort(AirPort sourceAirPort) {
		this.sourceAirPort = sourceAirPort;
	}
	public AirPort getDesintationAirport() {
		return desintationAirport;
	}
	public void setDesintationAirport(AirPort desintationAirport) {
		this.desintationAirport = desintationAirport;
	}
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

	//public Aircraft getAssignedFlight() {
	//	return assignedFlight;
	//}
	//public void setAssignedFlight(Aircraft assignedFlight) {
	//	this.assignedFlight = assignedFlight;
	//}
	public Date getSchdDate() {
		return schdDate;
	}
	public void setSchdDate(Date schdDate) {
		this.schdDate = schdDate;
	}
	public boolean isInterFlg() {
		return interFlg;
	}
	public void setInterFlg(boolean interFlg) {
		this.interFlg = interFlg;
	}
	public int getSchdNo() {
		return schdNo;
	}
	public void setSchdNo(int schdNo) {
		this.schdNo = schdNo;
	}

	public double getImpCoe() {
		return impCoe;
	}
	public void setImpCoe(double impCoe) {
		this.impCoe = impCoe;
	}
	
	public boolean valdiate () {
		return true;
	}
	
	public Flight clone() throws CloneNotSupportedException {
		return (Flight) (super.clone());
	}
	

}
