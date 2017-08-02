package xiaMengAirline.newBranch.BasicObject;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;


import xiaMengAirline.newBranch.BusinessDomain.AdjustableMethod;

public abstract class Flight implements AdjustableMethod {
	private static final Logger logger = Logger.getLogger(Flight.class);
	
	
	private int flightId;
	private Date schdDate;
	private Airport sourceAirPort;
	private Airport desintationAirport;
	private boolean internationalFlight;
	private int schdNo;
	private Date arrivalTime;
	private Date departureTime;
	private BigDecimal impCoe;
	private Aircraft assignedAir;
	private Aircraft plannedAir;
	private Flight plannedFlight;
	public int getFlightId() {
		return flightId;
	}
	public void setFlightId(int flightId) {
		this.flightId = flightId;
	}
	public Date getSchdDate() {
		return schdDate;
	}
	public void setSchdDate(Date schdDate) {
		this.schdDate = schdDate;
	}
	public boolean isInternationalFlight() {
		return internationalFlight;
	}
	public void setInternationalFlight(boolean internationalFlight) {
		this.internationalFlight = internationalFlight;
	}
	public int getSchdNo() {
		return schdNo;
	}
	public void setSchdNo(int schdNo) {
		this.schdNo = schdNo;
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
	public BigDecimal getImpCoe() {
		return impCoe;
	}
	public void setImpCoe(BigDecimal impCoe) {
		this.impCoe = impCoe;
	}
	public Aircraft getAssignedAir() {
		return assignedAir;
	}
	public void setAssignedAir(Aircraft assignedAir) {
		this.assignedAir = assignedAir;
	}
	public Aircraft getPlannedAir() {
		return plannedAir;
	}
	public void setPlannedAir(Aircraft plannedAir) {
		this.plannedAir = plannedAir;
	}
	public Flight getPlannedFlight() {
		return plannedFlight;
	}
	public void setPlannedFlight(Flight plannedFlight) {
		this.plannedFlight = plannedFlight;
	}
	public static Logger getLogger() {
		return logger;
	}
	public Airport getSourceAirPort() {
		return sourceAirPort;
	}
	public void setSourceAirPort(Airport sourceAirPort) {
		this.sourceAirPort = sourceAirPort;
	}
	public Airport getDesintationAirport() {
		return desintationAirport;
	}
	public void setDesintationAirport(Airport desintationAirport) {
		this.desintationAirport = desintationAirport;
	}
	
	
	



}
