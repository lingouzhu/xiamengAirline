package xiaMengAirline.newBranch.BasicObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import xiaMengAirline.newBranch.BusinessDomain.FlightAdjustableMethod;

public class Flight implements Cloneable {
	private static final Logger logger = Logger.getLogger(Flight.class);
	
	
	private int flightId;
	private Date schdDate;
	private Airport sourceAirPort;
	private Airport desintationAirport;
	private boolean internationalFlight;
	private Flight joined1stlight;
	private Flight joined2ndFlight;
	private int schdNo;
	private Date arrivalTime;
	private Date departureTime;
	private List<Passenger> passengers;
	private List<Passenger> plannedPassengers;
	private BigDecimal impCoe;
	private Aircraft assignedAir;
	private Aircraft plannedAir;
	private Flight plannedFlight;
	private FlightAdjustableMethod adjustableMethod;
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

	public FlightAdjustableMethod getAdjustableMethod() {
		return adjustableMethod;
	}
	public void setAdjustableMethod(FlightAdjustableMethod adjustableMethod) {
		this.adjustableMethod = adjustableMethod;
	}


	public Flight getJoined1stlight() {
		return joined1stlight;
	}
	public void setJoined1stlight(Flight joined1stlight) {
		this.joined1stlight = joined1stlight;
	}
	public Flight getJoined2ndFlight() {
		return joined2ndFlight;
	}
	public void setJoined2ndFlight(Flight joined2ndFlight) {
		this.joined2ndFlight = joined2ndFlight;
	}
	
	public Flight clone() throws CloneNotSupportedException {
		return (Flight) (super.clone());
	}
	public List<Passenger> getPassengers() {
		return passengers;
	}
	public void setPassengers(List<Passenger> passengers) {
		this.passengers = passengers;
	}
	public List<Passenger> getPlannedPassengers() {
		return plannedPassengers;
	}
	public void setPlannedPassengers(List<Passenger> plannedPassengers) {
		this.plannedPassengers = plannedPassengers;
	}


	
	
	



}
