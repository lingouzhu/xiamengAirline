package xiaMengAirline.newBranch.BasicObject;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import xiaMengAirline.newBranch.BusinessDomain.FlightAdjustableMethod;

public class Flight {
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
	private Passenger normalPassenger;
	private Passenger joinedPassenger;
	private Passenger plannedNormalPassenger;
	private Passenger plannedJoinedPassenger;
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
	public Passenger getNormalPassenger() {
		return normalPassenger;
	}
	public void setNormalPassenger(Passenger normalPassenger) {
		this.normalPassenger = normalPassenger;
	}
	public Passenger getJoinedPassenger() {
		return joinedPassenger;
	}
	public void setJoinedPassenger(Passenger joinedPassenger) {
		this.joinedPassenger = joinedPassenger;
	}
	public Passenger getPlannedNormalPassenger() {
		return plannedNormalPassenger;
	}
	public void setPlannedNormalPassenger(Passenger plannedNormalPassenger) {
		this.plannedNormalPassenger = plannedNormalPassenger;
	}
	public Passenger getPlannedJoinedPassenger() {
		return plannedJoinedPassenger;
	}
	public void setPlannedJoinedPassenger(Passenger plannedJoinedPassenger) {
		this.plannedJoinedPassenger = plannedJoinedPassenger;
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
	

	
	
	



}
