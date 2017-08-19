package xiaMengAirline.backup.beans;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import xiaMengAirline.backup.Exception.FlightDurationNotFoundBackup;
import xiaMengAirline.backup.utils.InitDataBackup;

public class FlightBackup implements Cloneable {
	private static final Logger logger = Logger.getLogger(FlightBackup.class);
	
	final public static int GroundingTime = 50;
	
	private int flightId;
	private Date schdDate;
	private AirPortBackup sourceAirPort;
	private AirPortBackup desintationAirport;
	private boolean internationalFlight;
	private int schdNo;
	private Date arrivalTime;
	private Date departureTime;
	private BigDecimal impCoe;
	private AircraftBackup assignedAir;
	private AircraftBackup plannedAir;
	private FlightBackup plannedFlight;
	
	public int getFlightId() {
		return flightId;
	}
	public void setFlightId(int id) {
		this.flightId = id;
	}
	public AirPortBackup getSourceAirPort() {
		return sourceAirPort;
	}
	public void setSourceAirPort(AirPortBackup sourceAirPort) {
		this.sourceAirPort = sourceAirPort;
	}
	public AirPortBackup getDesintationAirport() {
		return desintationAirport;
	}
	public void setDesintationAirport(AirPortBackup desintationAirport) {
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
	
	public FlightBackup clone() throws CloneNotSupportedException {
		return (FlightBackup) (super.clone());
	}
	
	public Date calcuateNextArrivalTime () throws FlightDurationNotFoundBackup {
		if (plannedFlight == null || (plannedFlight != null && !plannedFlight.getDesintationAirport().getId().equals(desintationAirport.getId()))) {
			//find out flight time
			String searchKey = assignedAir.getType();
			searchKey += "_";
			searchKey += sourceAirPort.getId();
			searchKey += "_";
			searchKey += desintationAirport.getId();
			
			int flightDur = 0;
			if (InitDataBackup.fightDurationMap.containsKey(searchKey))
				flightDur =  InitDataBackup.fightDurationMap.get(searchKey);
			else {
				if (plannedFlight == null)
					throw new FlightDurationNotFoundBackup(this, searchKey);
				FlightBackup linkedFlight = InitDataBackup.jointFlightMap.get(flightId);
				if (linkedFlight == null)
					throw new FlightDurationNotFoundBackup(this, searchKey);
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
	public AircraftBackup getAssignedAir() {
		return assignedAir;
	}
	public void setAssignedAir(AircraftBackup assignedAir) {
		this.assignedAir = assignedAir;
	}
	public AircraftBackup getPlannedAir() {
		return plannedAir;
	}
	public void setPlannedAir(AircraftBackup plannedAir) {
		this.plannedAir = plannedAir;
	}
	public FlightBackup getPlannedFlight() {
		return plannedFlight;
	}
	public void setPlannedFlight(FlightBackup plannedFlight) {
		this.plannedFlight = plannedFlight;
	}
	
	
	//to do
	public static int getGroundingTime (int fromFlightId, int toFlightId) {
		//check  if actual grounding time is less than the standard
		//check if this is the first flight
		if (fromFlightId >= toFlightId)
			return GroundingTime;
		
		String currentFlightId = String.valueOf(fromFlightId);
		String nextFlightId = String.valueOf(toFlightId);

		
		//look up special flight time table
		String searchKey = currentFlightId;
		searchKey += "_";
		searchKey +=  nextFlightId;
		
		if (InitDataBackup.specialFlightMap.containsKey(searchKey))
			return (InitDataBackup.specialFlightMap.get(searchKey));
		else
			return GroundingTime;
		
	}


}
