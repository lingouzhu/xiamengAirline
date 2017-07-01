package xiaMengAirline.beans;

import java.util.Date;

public class Flight {
	private String id;
	private AirPort sourceAirPort;
	private AirPort desintationAirport;
	private Date plannedArrivalTime;
	private Date plannedDepartureTime;
	private Date adjustedArrivalTime;
	private Date adjustedDepartureTime;
	private Date adjustedReadinessTime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	
	

}
