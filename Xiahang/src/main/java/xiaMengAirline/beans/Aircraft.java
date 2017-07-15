package xiaMengAirline.beans;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.firefox.UnableToCreateProfileException;

import xiaMengAirline.Exception.AirportNotAcceptArrivalTime;
import xiaMengAirline.searchEngine.SelfSearch;
import xiaMengAirline.util.InitData;
import xiaMengAirline.util.Utils;

public class Aircraft implements Cloneable{
	private String id;
	private String type;
	private List<Flight> flightChain = new ArrayList<Flight>() ;
	private boolean isCancel;
	private Aircraft cancelAircrafted = null;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<Flight> getFlightChain() {
		return flightChain;
	}
	public Flight getFlight(int position) {
		if (position >= 0)
			return this.flightChain.get(position);
		else	
			return null;
				
	}
	public void setFlightChain(List<Flight> flightChain) {
		this.flightChain = flightChain;
	}
	public void addFlight (Flight aFlight) {
		aFlight.setAssignedAir(this);
		flightChain.add(aFlight);
	}
	public boolean hasFlight (Flight aFlight) {
		return flightChain.contains(aFlight);
	}
	
	public void insertFlightChain (Aircraft sourceAircraft, List<Integer> addFlights, int position) {
		List<Flight> newFlights = new ArrayList<Flight> (); 
		for (int anAdd:addFlights) {
			sourceAircraft.getFlight(anAdd).setAssignedAir(this);
			newFlights.add(sourceAircraft.getFlight(anAdd));
		}
		this.flightChain.addAll(position,newFlights );
	}
	public void insertFlightChain (Aircraft sourceAircraft, Flight startFlight, Flight endFlight, Flight insertFlight, boolean isBefore) {
		List<Flight> newFlights = new ArrayList<Flight> (); 
		int addFlightStartPosition = sourceAircraft.getFlightChain().indexOf(startFlight);
		int addFlightEndPosition = sourceAircraft.getFlightChain().indexOf(endFlight);
		int insertFlightPosition = this.flightChain.indexOf(insertFlight);
		for (int i=addFlightStartPosition;i<=addFlightEndPosition;i++) {
			sourceAircraft.getFlight(i).setAssignedAir(this);
			newFlights.add(sourceAircraft.getFlight(i));
		}
		if (insertFlight != null) {
			if (isBefore)
				this.flightChain.addAll(insertFlightPosition,newFlights );
			else
				this.flightChain.addAll(insertFlightPosition+1,newFlights );			
		} else 
			this.flightChain.addAll(newFlights );

	}

	public void removeFlightChain (List<Integer> deleteFlights)  {
		List<Flight> removeList = new ArrayList<Flight> ();
		for (Integer i:deleteFlights) 
			removeList.add(this.flightChain.get(i));

		this.flightChain.removeAll(removeList);
	}
	public void removeFlightChain (Flight startFlight, Flight endFlight)  {
		List<Flight> removeList = new ArrayList<Flight> ();
		int removeSFlighttartPosition = this.flightChain.indexOf(startFlight);
		int removeFlightEndPosition = this.flightChain.indexOf(endFlight);
		
		for (int i=removeSFlighttartPosition; i <= removeFlightEndPosition; i++)
			removeList.add(this.flightChain.get(i));
		
		this.flightChain.removeAll(removeList);
	}
	
	public List<AirPort>  getAirports() {
		ArrayList<AirPort> retAirPortList = new ArrayList<AirPort> ();
		for (Flight aFlight : flightChain) {
			retAirPortList.add(aFlight.getSourceAirPort());
		}
		if (!flightChain.isEmpty()) {
			//add last destination
			retAirPortList.add(flightChain.get(flightChain.size()-1).getDesintationAirport());
		}
		return (retAirPortList);
		
	}
	
	public AirPort getAirport (int position, boolean isSource) {
		if (isSource)
			return (flightChain.get(position).getSourceAirPort());
		else
			return (flightChain.get(position).getDesintationAirport());
	}
	public boolean isCancel() {
		return isCancel;
	}
	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}
	
	public Aircraft clone() throws CloneNotSupportedException{
		Aircraft aNew = (Aircraft) super.clone();
		List<Flight> newFlightChain = new ArrayList<Flight> ();
		for (Flight aFlight:flightChain) {
			newFlightChain.add(aFlight.clone());
		}
		aNew.setFlightChain(newFlightChain);
		return (aNew);
	}
	
	public void adjustment  () throws CloneNotSupportedException, ParseException {
		SelfSearch selfAdjustEngine = new SelfSearch();
		if (!isCancel) {
			selfAdjustEngine.adjustAircraft(this);
		}
		
	}

//	public Aircraft getCancelAircrafted() {
//		return cancelAircrafted;
//	}
//	public void setCancelAircrafted(Aircraft cancelAircrafted) {
//		this.cancelAircrafted = cancelAircrafted;
//	}
	public Aircraft getCancelledAircraft() {
		Aircraft retCancelled = cancelAircrafted;
		if (retCancelled == null) {
			retCancelled = new Aircraft();
			retCancelled.setCancel(true);
			retCancelled.setFlightChain(new ArrayList<Flight> ());
			retCancelled.setId(this.id);
			retCancelled.setType(this.type);
			this.cancelAircrafted = retCancelled;
		}
		return retCancelled;
	}
	
	public void clear() {
		flightChain.clear();
	}
	
	public HashMap<Flight, List<Flight>> getCircuitFlights () {
		HashMap<Flight, List<Flight>> retCircuitList = new HashMap<Flight, List<Flight>> ();
		
		for (Flight aFlight:flightChain) {
			ArrayList<Flight> matchedList = new ArrayList<Flight> ();
			int currentPos = flightChain.indexOf(aFlight);
			String currentSourceAirport = aFlight.getSourceAirPort().getId();
			for (int j=currentPos+1;j < flightChain.size();j++) {
				String nextDestAirport = flightChain.get(j).getDesintationAirport().getId();
				if (currentSourceAirport.equals(nextDestAirport)) {
					matchedList.add(flightChain.get(j));
				}
			}
			
			if (!matchedList.isEmpty()) 
				retCircuitList.put(aFlight, matchedList);
		}
		
		return (retCircuitList);
		
	}
	
	public HashMap<Flight, List<MatchedFlight>> getMatchedFlights (Aircraft air2) {
		HashMap<Flight, List<MatchedFlight>> retMatchedList = new HashMap<Flight, List<MatchedFlight>> ();
		
		for (Flight aFlight:flightChain) {
			String sourceAirPortAir1 = aFlight.getSourceAirPort().getId();
			for (Flight bFlight:air2.getFlightChain()) {
				String sourceAirPortAir2 = bFlight.getSourceAirPort().getId();
				if (sourceAirPortAir1.equals(sourceAirPortAir2)) {
					List<MatchedFlight> matchedList = new ArrayList<MatchedFlight>();
					for (int i=flightChain.indexOf(aFlight);i < flightChain.size();i++) {
						String airPortA = getFlight(i).getDesintationAirport().getId();
						for (int j=air2.getFlightChain().indexOf(bFlight);j < air2.getFlightChain().size();j++) {
							String airPortB = air2.getFlight(j).getDesintationAirport().getId();
							if (airPortA.equals(airPortB)) {
								MatchedFlight aMatched = new MatchedFlight();
								aMatched.setAir1SourceFlight(flightChain.indexOf(aFlight));
								aMatched.setAir1DestFlight(i);
								aMatched.setAir2SourceFlight(air2.getFlightChain().indexOf(bFlight));
								aMatched.setAir2DestFlight(j);
								matchedList.add(aMatched);
							}
						}
					}
					if (!matchedList.isEmpty()) {
						retMatchedList.put(aFlight, matchedList);
					} else {
						//means source airport overlapped but no destination overlapped
						;
					}
				}
			}
		}
		return retMatchedList;
	}
	
	public void sortFlights () {
		Utils.sort(flightChain, "departureTime", true);
	}
	
	public boolean validate () {
		
		if (isCancel) return true;
		
		List<Flight> flightChain = getFlightChain();
		
		for (int i = 0; i < flightChain.size(); i++) {
			Flight flight = flightChain.get(i);
			
			String startPort = flight.getSourceAirPort().getId();
			String endPort =  flight.getDesintationAirport().getId();
			String airID =  getId();
			
			if (InitData.airLimitationList.contains(airID + "_" + startPort + "_" + endPort)) {
				return true;
			}
			if (i != 0) {
				Flight preFlight = flightChain.get(i - 1);
				
				if (!preFlight.getDesintationAirport().getId().equals(flight.getSourceAirPort().getId())) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	//must secure the flight can departure at departureTime
	public void adjustFlightTime (int startPosition) throws ParseException, AirportNotAcceptArrivalTime {
		Flight currentFlight = null;
		Flight nextFlight = null;
		for (int i=startPosition; i < flightChain.size(); i++) {
			nextFlight = flightChain.get(i);
			 
			if (i > startPosition) {
				Calendar cl = Calendar. getInstance();
			    cl.setTime(currentFlight.getArrivalTime());
			    int plannedGroundingTime = nextFlight.getGroundingTime(currentFlight);
			    cl.add(Calendar.MINUTE, plannedGroundingTime);
			    FlightTime aScheduledTime = new FlightTime();
			    aScheduledTime.setArrivalTime(currentFlight.getArrivalTime());
			    if (cl.getTime().before(nextFlight.getDepartureTime()))
			    	aScheduledTime.setDepartureTime(nextFlight.getDepartureTime());
			    else
			    	aScheduledTime.setDepartureTime(cl.getTime());
			    FlightTime newFlightTime = currentFlight.getDesintationAirport().requestAirport(aScheduledTime, plannedGroundingTime );
			    if (newFlightTime!=null) {
			    	if (aScheduledTime.getArrivalTime().compareTo(newFlightTime.getArrivalTime()) != 0) {
			    		throw new AirportNotAcceptArrivalTime(currentFlight, newFlightTime);
			    	} else {
				    	nextFlight.setDepartureTime(newFlightTime.getDepartureTime());
			    	}
			    } else {
			    	nextFlight.setDepartureTime(aScheduledTime.getDepartureTime());
			    }
			}
			
			currentFlight = nextFlight;
			
			Date newArrival = currentFlight.calcuateNextArrivalTime();
			if (newArrival.compareTo(currentFlight.getArrivalTime()) !=0 )
				currentFlight.setArrivalTime(newArrival);
		}
		
	}
	
}
