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
	private Date plannedArrivalTime;
	private Date plannedDepartureTime;
	private Date adjustedArrivalTime;
	private Date adjustedDepartureTime;
	private Date adjustedReadinessTime;
	//private Aircraft assignedFlight;
	private int passengers;
	private int jointPassengers;	
	private BigDecimal impCoe;
	
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
	public Date getPlannedArrivalTime() {
		return plannedArrivalTime;
	}
	public void setPlannedArrivalTime(Date plannedArrivalTime) {
		this.plannedArrivalTime = plannedArrivalTime;
	}
	public Date getPlannedDepartureTime() {
		return plannedDepartureTime;
	}
	public void setPlannedDepartureTime(Date plannedDepartureTime) {
		this.plannedDepartureTime = plannedDepartureTime;
	}
	public Date getAdjustedArrivalTime() {
		return adjustedArrivalTime;
	}
	public void setAdjustedArrivalTime(Date adjustedArrivalTime) {
		this.adjustedArrivalTime = adjustedArrivalTime;
	}
	public Date getAdjustedDepartureTime() {
		return adjustedDepartureTime;
	}
	public void setAdjustedDepartureTime(Date adjustedDepartureTime) {
		this.adjustedDepartureTime = adjustedDepartureTime;
	}
	public Date getAdjustedReadinessTime() {
		return adjustedReadinessTime;
	}
	public void setAdjustedReadinessTime(Date adjustedReadinessTime) {
		this.adjustedReadinessTime = adjustedReadinessTime;
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
	public int getPassengers() {
		return passengers;
	}
	public void setPassengers(int passengers) {
		this.passengers = passengers;
	}
	public int getJointPassengers() {
		return jointPassengers;
	}
	public void setJointPassengers(int jointPassengers) {
		this.jointPassengers = jointPassengers;
	}
	public BigDecimal getImpCoe() {
		return impCoe;
	}
	public void setImpCoe(BigDecimal impCoe) {
		this.impCoe = impCoe;
	}
	
	public boolean valdiate () {
		return true;
	}
	
	public Flight clone() throws CloneNotSupportedException {
		return (Flight) (super.clone());
	}
	

}
