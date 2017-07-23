package xiaMengAirline.beans;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.util.InitData;

public class Flight implements Cloneable {
	private static final Logger logger = Logger.getLogger(Flight.class);
	
	private int flightId;
	private Date schdDate;
	private AirPort sourceAirPort;
	private AirPort desintationAirport;
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
	public void setFlightId(int id) {
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

	public Date getSchdDate() {
		return schdDate;
	}
	public void setSchdDate(Date schdDate) {
		this.schdDate = schdDate;
	}
	public boolean isInternationalFlight() {
		return internationalFlight;
	}
	public void setInternationalFlight(boolean interFlg) {
		this.internationalFlight = interFlg;
	}
	public int getSchdNo() {
		return schdNo;
	}
	public void setSchdNo(int schdNo) {
		this.schdNo = schdNo;
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
	
	public Date calcuateNextArrivalTime () throws FlightDurationNotFound {
		if (plannedFlight == null || (plannedFlight != null && !plannedFlight.getDesintationAirport().getId().equals(desintationAirport.getId()))) {
			//find out flight time
			String searchKey = assignedAir.getType();
			searchKey += "_";
			searchKey += sourceAirPort.getId();
			searchKey += "_";
			searchKey += desintationAirport.getId();
			
			int flightDur = 0;
			if (InitData.fightDurationMap.containsKey(searchKey))
				flightDur =  InitData.fightDurationMap.get(searchKey);
			else {
				if (plannedFlight == null)
					throw new FlightDurationNotFound(this, searchKey);
				Flight linkedFlight = InitData.jointFlightMap.get(flightId);
				if (linkedFlight == null)
					throw new FlightDurationNotFound(this, searchKey);
				else {
					long diff = plannedFlight.getArrivalTime().getTime() - plannedFlight.getDepartureTime().getTime();
					diff += linkedFlight.getArrivalTime().getTime() - linkedFlight.getDepartureTime().getTime();
					flightDur = (int) diff / (60 * 1000) ;
				}
					
			}
				
		    Calendar cl = Calendar. getInstance();
		    cl.setTime(departureTime);
		    cl.add(Calendar.MINUTE, flightDur);
		    return (cl.getTime());			
		} else {
			long diff = plannedFlight.getArrivalTime().getTime() - plannedFlight.getDepartureTime().getTime();
			long diffMin = diff / (60 * 1000) ;
			Calendar cl = Calendar. getInstance();
		    cl.setTime(departureTime);
		    cl.add(Calendar.MINUTE, (int) diffMin);
		    return (cl.getTime());
		}

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
	
	public int getGroundingTime (Flight previousFlight) {
		//check  if actual grounding time is less than the standard
//		Calendar cl = Calendar. getInstance();
//	    cl.setTime(previousFlight.plannedFlight.getArrivalTime());
//	    cl.add(Calendar.MINUTE, AirPort.GroundingTime);
//	    if (cl.getTime().after(plannedFlight.getDepartureTime())) {
//	    	long diff = plannedFlight.getDepartureTime().getTime() - previousFlight.getPlannedFlight().getArrivalTime().getTime();
//			long diffMin = diff / (60 * 1000);
//			logger.warn("Flight" + schdNo + " changed grounding time to " + diffMin);
//			return (int) diffMin;
//	    } else 
	    	return AirPort.GroundingTime;
	}


}
