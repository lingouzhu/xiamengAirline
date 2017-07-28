package xiaMengAirline.searchEngine;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAcceptArrivalTime;
import xiaMengAirline.Exception.AirportNotAcceptDepartureTime;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.AirPortClose;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.FlightTime;
import xiaMengAirline.beans.RegularAirPortClose;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.util.InitData;

public class SingleAircraftSearch {
	private Aircraft originalAircraft;
	private ArrayList<Flight> originalFlights;
	private ArrayList<ArrayList<Flight>> openArrayList = new ArrayList<ArrayList<Flight>>();
	
	public SingleAircraftSearch(Aircraft aAircraft) throws CloneNotSupportedException {
		originalAircraft = aAircraft.clone();
		originalFlights = (ArrayList<Flight>) originalAircraft.getFlightChain();
	}
	
	public void processNextPath() {
		if (openArrayList.size() > 0) {
			ArrayList<Flight> path = openArrayList.get(0);
			//path = validatePath(path);
			//if (path != null) {
			//	openNextNode(path);
			//}
		} else {
			//openFirstNode();
		}
		
	}
	
	public void openFirstNode() throws CloneNotSupportedException, FlightDurationNotFound, ParseException {
		Flight thisFlight = originalFlights.get(0);
		boolean isAdjusted = false;
		// try earlier departure
		Date earlyDepTime = null;//getPossibleEarlierDepartureTime(thisFlight, thisFlight.getSourceAirPort());
		if (earlyDepTime != null) {
			isAdjusted = true;
			Flight newFlight = thisFlight.clone();
			newFlight.setDepartureTime(earlyDepTime);
			newFlight.calcuateNextArrivalTime();
			ArrayList<Flight> newFlightArrayList = new ArrayList<Flight>();
			newFlightArrayList.add(newFlight);
			openArrayList.add(newFlightArrayList);
			// try stretch
			if (getJointFlightPosition(newFlight) == 1) {
				openJointFlightNode(newFlight, new ArrayList<Flight>());
			}
		}
		
		// try delay departure
		Date delayDepTime = null;// getPossibleDelayDeparture(thisFlight, thisFlight.getSourceAirPort());
		if (delayDepTime != null) {
			isAdjusted = true;
			Flight newFlight = thisFlight.clone();
			newFlight.setDepartureTime(delayDepTime);
			newFlight.calcuateNextArrivalTime();
			ArrayList<Flight> newFlightArrayList = new ArrayList<Flight>();
			newFlightArrayList.add(newFlight);
			openArrayList.add(newFlightArrayList);
			// try stretch
			if (getJointFlightPosition(newFlight) == 1) {
				openJointFlightNode(newFlight, new ArrayList<Flight>());
			}
		}
		
		// try stretch
		if (!isAdjusted && getJointFlightPosition(thisFlight) == 1) {
			openJointFlightNode(thisFlight, new ArrayList<Flight>());
		}
		
		// try cancel
		
	}
	
	public ArrayList<Flight> adjustPath(ArrayList<Flight> flights){
		// validate the flight chain till now
		// delay early
		// last this
		return null;
	}
	
	/** 
	 * thisFlight is the last flight in the opened flight ArrayList
	 * nextFlight is new opened flight
	 * @param oldFlights
	 */
	public void openNextNode(ArrayList<Flight> oldFlights) {
		Flight thisFlight = oldFlights.get(oldFlights.size() - 1);
		int thisFlightIndex = getFlightIndexByFlightId(thisFlight.getFlightId());
		int nextFlightIndex = thisFlightIndex + 1;
		
		// index is not out bound
		if (nextFlightIndex < originalFlights.size()) {
			Flight nextFlight = originalFlights.get(nextFlightIndex);
			nextFlight.setDepartureTime(getCompressedDeparture(thisFlight, nextFlight));
			
			if (thisFlightIndex > 0) {
				if (!isNewFlight(originalFlights.get(thisFlightIndex - 1))) {
					// if last flight is not a new flight, try cancel and create all possible nodes
					
				}
			} else {
				// first flight in the chain
			}
		}
	}
	
	
	public void openCancelFlightNode(Flight origFlight, ArrayList<Flight> origFlightList) throws CloneNotSupportedException, ParseException, FlightDurationNotFound {
		int thisFlightIndex = getFlightIndexByFlightId(origFlight.getFlightId());
		ArrayList<Flight> flightChain = (ArrayList<Flight>) origFlightList.clone();
		flightChain.remove(flightChain.size() - 1);
		boolean adjustable = false;
		
		Flight newFlight = origFlight.clone();
		newFlight.setFlightId(getNextFlightId());
		if (thisFlightIndex > 0) {
			Flight lastFlight = origFlightList.get(origFlightList.size() - 2);
			Date depTime = addMinutes(lastFlight.getArrivalTime(), getGroundingTime(lastFlight, newFlight));
			newFlight.setDepartureTime(depTime);
			Date adjustedDep = getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false);
			if (adjustedDep != null) {
				adjustable = true;
				newFlight.setDepartureTime(adjustedDep);
			}
		} else {
			adjustable = true;
		}
		if (adjustable) {
			for (int i = thisFlightIndex; i < originalFlights.size(); i++) {
				Flight nextFlight = originalFlights.get(i);
				AirPort destAirport = nextFlight.getSourceAirPort();
				
				if (destAirport.getId().equals(newFlight.getSourceAirPort().getId())) {
					// valid parking
					if (thisFlightIndex > 0) {
						Flight lastFlight = originalFlights.get(thisFlightIndex - 1);
						if (!isValidParking(lastFlight.getArrivalTime(), nextFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
							continue;
						} else {
							ArrayList<Flight> newFlightChain = (ArrayList<Flight>) flightChain.clone();
							newFlightChain.add(nextFlight);
							openArrayList.add(flightChain);
							continue;
						}
					}
				}
				
				// international flight is not eligible
				if (isInternational(newFlight.getSourceAirPort().getId(), destAirport.getId())){
					continue;
				}
				
				// aircraft constraint
				if (!isEligibalAircraft(newFlight.getAssignedAir(), newFlight.getSourceAirPort(), destAirport)){
					continue;
				}

				long flightTime = getFlightTime(newFlight.getSourceAirPort().getId(), destAirport.getId(), newFlight.getAssignedAir());
				if (flightTime > 0){
					newFlight.setDesintationAirport(nextFlight.getSourceAirPort());
					newFlight.calcuateNextArrivalTime();
					Date adjustedArrival = getPossibelArrivalTime(newFlight, newFlight.getDesintationAirport());
					if (adjustedArrival != null){
						if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
							//ok open next
							ArrayList<Flight> newFlightChain = (ArrayList<Flight>) flightChain.clone();
							newFlightChain.add(newFlight);
							newFlightChain.add(nextFlight);
							openArrayList.add(flightChain);
						} else {
							Date newDeparture = addMinutes(newFlight.getArrivalTime(), -flightTime);
							if (!isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
								//ok open next
								newFlight.setDepartureTime(newDeparture);
								newFlight.setArrivalTime(adjustedArrival);
								ArrayList<Flight> newFlightChain = (ArrayList<Flight>) flightChain.clone();
								newFlightChain.add(newFlight);
								newFlightChain.add(nextFlight);
								openArrayList.add(flightChain);
							}
						}
					}
				}
				
			}
		}
	}
	
	/**
	 * open a node for joint flight
	 * @param originalFlight
	 * @param flightChain
	 * @throws CloneNotSupportedException
	 * @throws FlightDurationNotFound
	 */
	public void openJointFlightNode(Flight originalFlight, ArrayList<Flight> origFlightChain) throws CloneNotSupportedException, FlightDurationNotFound{
		Flight jointFlight = getJointFlight(originalFlight);
		ArrayList<Flight> flightChain = (ArrayList<Flight>) origFlightChain.clone();
		flightChain.remove(flightChain.size() - 1);
		if (isAffected(jointFlight) == 1) {
			// departure time is affected
			Flight newFlight = originalFlight.clone();
			newFlight.setDesintationAirport(jointFlight.getDesintationAirport());
			newFlight.calcuateNextArrivalTime();
			flightChain.add(newFlight);
			// get next node
			//flightChain.add(e);
			openArrayList.add(flightChain);
		}
	}
	
	// method utility
	
	/**
	 * if a departure time is in the error time range
	 * @param depTime
	 * @param airport
	 * @return
	 */
	public boolean isDepTimeAffected(Date depTime, AirPort airport) {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (depTime.after(aClose.getStartTime())
						&& depTime.before(aClose.getEndTime())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * get next valid arrival time
	 * @param flight
	 * @param airport
	 * @return
	 * @throws ParseException
	 */
	public Date getPossibelArrivalTime(Flight flight, AirPort airport) throws ParseException{
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (flight.getArrivalTime().after(aClose.getStartTime())
					&& flight.getArrivalTime().before(aClose.getEndTime())) {
				if (flight.getFlightId() > InitData.plannedMaxFligthId){
					return aClose.getEndTime();
				} else {
					if (isValidDelay(getPlannedDeparture(flight), aClose.getEndTime(), flight.isInternationalFlight())){
						return aClose.getEndTime();
					} else {
						return null;
					}
				}
			}
		}
		for (RegularAirPortClose aClose : airport.getRegularCloseSchedule()) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(flight.getArrivalTime());
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();
			
			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			Date aCloseDate = formatter2.parse(aDateC);
			Date aOpenDate = formatter2.parse(aDateO);
			
			if (flight.getArrivalTime().after(aCloseDate)
					&& flight.getArrivalTime().before(aOpenDate)) {
				if (flight.getFlightId() > InitData.plannedMaxFligthId){
					return aOpenDate;
				} else {
					if (isValidDelay(getPlannedDeparture(flight), aOpenDate, flight.isInternationalFlight())){
						return aOpenDate;
					} else {
						return null;
					}
				}
			}
		} 
		return (Date) flight.getArrivalTime().clone();
	}
	
	/**
	 * attempt delay the flight
	 * @param flight
	 * @param airport
	 * @return
	 * @throws ParseException 
	 */
	public Date getPossibleDelayDeparture(Flight flight, AirPort airport, boolean isFirstFlight) throws ParseException {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (flight.getDepartureTime().after(aClose.getStartTime())
						&& flight.getDepartureTime().before(aClose.getEndTime())) {
					if (isFirstFlight) {
						if (flight.getFlightId() > InitData.plannedMaxFligthId){
							return aClose.getEndTime();
						} else {
							if (isValidDelay(getPlannedDeparture(flight), aClose.getEndTime(), flight.isInternationalFlight())){
								return aClose.getEndTime();
							} else {
								return null;
							}
						}
						
					} else {
						return null;
					}
				}
			}
		}
		for (RegularAirPortClose aClose : airport.getRegularCloseSchedule()) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(flight.getDepartureTime());
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();
			
			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			Date aCloseDate = formatter2.parse(aDateC);
			Date aOpenDate = formatter2.parse(aDateO);
			
			if (flight.getDepartureTime().after(aCloseDate)
					&& flight.getDepartureTime().before(aOpenDate)) {
				if (flight.getFlightId() > InitData.plannedMaxFligthId){
					return aOpenDate;
				} else {
					if (isValidDelay(getPlannedDeparture(flight), aOpenDate, flight.isInternationalFlight())){
						return aOpenDate;
					} else {
						return null;
					}
				}
			}
		} 
		return (Date) flight.getDepartureTime().clone();
		// need to check parking somewhere
	}
	
	/** 
	 * check if this flight is able to departure earlier. 
	 * @return if possible return the time, otherwise return null
	 * @throws ParseException 
	 */
	public Date getPossibleEarlierDepartureTime(Flight flight, AirPort airport, boolean hasEarlyLimit, Flight lastFlight) throws ParseException {
		if (lastFlight != null) {
			// not the first flight
			for (AirPortClose aClose : airport.getCloseSchedule()) {
				if (!aClose.isAllowForTakeoff()) {
					if (flight.getFlightId() > InitData.plannedMaxFligthId){
						if (flight.getDepartureTime().after(aClose.getStartTime())
								&& flight.getDepartureTime().before(aClose.getStartTime())) {
							Date tempDep = addMinutes(lastFlight.getArrivalTime(), Flight.getGroundingTime(lastFlight.getFlightId(), flight.getFlightId()));
							if (tempDep.after(aClose.getStartTime())) {
								return null;
							} else {
								return tempDep;
							}
						}
					} else {
						if (getPlannedDeparture(flight).after(aClose.getStartTime())
								&& getPlannedDeparture(flight).before(addMinutes(aClose.getStartTime(), 360))) {
							Date tempDep = addMinutes(lastFlight.getArrivalTime(), Flight.getGroundingTime(lastFlight.getFlightId(), flight.getFlightId()));
							if (tempDep.after(aClose.getStartTime())) {
								return null;
							} else {
								if (!hasEarlyLimit) {
									Date tempDep2 = addMinutes(getPlannedDeparture(flight), -360);
									return tempDep.before(tempDep2) ? tempDep2 : tempDep;
								} else {
									return aClose.getStartTime();
								}
							}
						} else {
							return null;
						}
					}
				}
			}
		} else {
			// is the first flight
			for (AirPortClose aClose : airport.getCloseSchedule()) {
				if (!aClose.isAllowForTakeoff()) {
					if (flight.getFlightId() > InitData.plannedMaxFligthId){
						if (flight.getDepartureTime().after(aClose.getStartTime())
								&& flight.getDepartureTime().before(aClose.getStartTime())) {
							// decide later
							return aClose.getStartTime();
						}
					} else {
						if (getPlannedDeparture(flight).after(aClose.getStartTime())
								&& getPlannedDeparture(flight).before(addMinutes(aClose.getStartTime(), 360))) {
							if (!hasEarlyLimit) {
								Date tempDep = addMinutes(getPlannedDeparture(flight), -360);
								return tempDep;
							} else {
								return aClose.getStartTime();
							}
						} else {
							return null;
						}
					}
				}
			}
		}
		for (RegularAirPortClose aClose : airport.getRegularCloseSchedule()) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(flight.getDepartureTime());
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();
			
			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			Date aCloseDate = formatter2.parse(aDateC);
			Date aOpenDate = formatter2.parse(aDateO);
			
			if (flight.getDepartureTime().after(aCloseDate)
					&& flight.getDepartureTime().before(aOpenDate)) {
				if (lastFlight != null) {
					// not the first flight
					if (flight.getFlightId() > InitData.plannedMaxFligthId){
						Date tempDep = addMinutes(lastFlight.getArrivalTime(), Flight.getGroundingTime(lastFlight.getFlightId(), flight.getFlightId()));
						if (tempDep.after(aCloseDate)) {
							return null;
						} else {
							return tempDep;
						}
					} else {
						return null;
					}
				} else {
					// the first flight
					if (flight.getFlightId() > InitData.plannedMaxFligthId){
						// decide later
						return aCloseDate;
					} else {
						return null;
					}
				}
			}
		} 
		return (Date) flight.getDepartureTime().clone();
	}
	
	/**
	 * get flight time between two airports
	 * @param airport1Id
	 * @param airport2Id
	 * @param aircraft
	 * @return
	 */
	public long getFlightTime(String airport1Id, String airport2Id, Aircraft aircraft){
		String searchKey = aircraft.getType();
		searchKey += "_";
		searchKey += airport1Id;
		searchKey += "_";
		searchKey += airport2Id;
		
		if (InitData.fightDurationMap.containsKey(searchKey)){
			long flightTime = InitData.fightDurationMap.get(searchKey);
			return flightTime;
		}else{
			return 0;
		}
		
	}
	
	/**
	 * the airline is international
	 * @param airport1
	 * @param airport2
	 * @return
	 */
	public boolean isInternational(String airport1, String airport2){
		if (InitData.domesticAirportList.contains(airport1) && InitData.domesticAirportList.contains(airport2)){
			return false;
		}
		return true;
	}
	
	/**
	 * the airline is valid
	 * @param aircraft
	 * @param sourceAir
	 * @param destAir
	 * @return
	 */
	public boolean isEligibalAircraft(Aircraft aircraft, AirPort sourceAir, AirPort destAir){
		String searchKey = aircraft.getId() + "_" + sourceAir.getId() + "_" + destAir.getId();
		return InitData.airLimitationList.contains(searchKey) ? false : true;
	}
	
	/**
	 * check if the flight delay is valid
	 * @param planTime
	 * @param adjustTime
	 * @param isInternational
	 * @return
	 */
	public boolean isValidDelay(Date planTime, Date adjustTime, boolean isInternational){
		if (getMinuteDifference(adjustTime, planTime) > (isInternational ? Aircraft.INTERNATIONAL_MAXIMUM_DELAY_TIME*60: Aircraft.DOMESTIC_MAXIMUM_DELAY_TIME*60)){
			return false;
		}
		
		return true;
	}
	
	/** 
	 * get flight index in the original flight chain by flight id
	 * @param aFlightId
	 * @return
	 */
	public int getFlightIndexByFlightId(int aFlightId) {
		for (int i = 0; i < originalFlights.size(); i++) {
			if (originalFlights.get(i).getFlightId() == aFlightId)
				return i;
		}
		return -1;
	}
	
	/**
	 * check if the aircraft parking is valid
	 * @param arrivalTime
	 * @param departureTime
	 * @param airport
	 * @return
	 */
	public boolean isValidParking(Date arrivalTime, Date departureTime, AirPort airport){
		if (arrivalTime != null && arrivalTime != null) {
			for (AirPortClose aClose : airport.getCloseSchedule()) {
				if (arrivalTime.compareTo(aClose.getStartTime()) <= 0
						&& departureTime.compareTo(aClose.getEndTime()) >= 0) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * get original arrival time
	 * @param flight
	 * @return
	 */
	public Date getPlannedArrival(Flight flight){
		return flight.getPlannedFlight().getArrivalTime();
	}
	
	/**
	 * get original departure time
	 * @param flight
	 * @return
	 */
	public Date getPlannedDeparture(Flight flight){
		return flight.getPlannedFlight().getDepartureTime();
	}
	
	/**
	 * get grounding time between two flights
	 * @param flight1
	 * @param flight2
	 * @return
	 */
	public int getGroundingTime(Flight flight1, Flight flight2) {
		return Flight.getGroundingTime(flight1.getFlightId(), flight2.getFlightId());
	}
	
	/**
	 * compress the departure time of next flight
	 * @param lastFlight
	 * @param thisFlight
	 * @return
	 */
	public Date getCompressedDeparture(Flight lastFlight, Flight thisFlight) {
		Date thisPlannedDeparture = getPlannedDeparture(thisFlight);
		Date thisShiftedDeparture = addMinutes(lastFlight.getArrivalTime(), getGroundingTime(lastFlight, thisFlight));
		return thisPlannedDeparture.before(thisShiftedDeparture) ? thisPlannedDeparture : thisShiftedDeparture;
	}
	
	/**
	 * a flight is a new flight
	 * @param flight
	 * @return
	 */
	public boolean isNewFlight(Flight flight) {
		return flight.getFlightId() > InitData.plannedMaxFligthId;
	}
	
	/**
	 * if a flight is joint flight
	 * @param flight
	 * @return joint flight position
	 */
	public int getJointFlightPosition(Flight flight){
		if (InitData.jointFlightMap.keySet().contains(flight.getFlightId())) {
			if (InitData.jointFlightMap.get(flight.getFlightId()) != null) {
				return 1;
			} else {
				return 2;
			}
		}
		return 0;
	}
	
	/**
	 * get joint flight
	 * @param flight
	 * @return
	 */
	public Flight getJointFlight(Flight flight){
		return InitData.jointFlightMap.get(flight.getFlightId());
	}
	
	/**
	 * get how this flight is affected
	 * @param flight
	 * @return 1 departure, 2 arrival, 0 no
	 */
	public int isAffected(Flight flight) {
		for (AirPortClose aClose : flight.getSourceAirPort().getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (flight.getDepartureTime().after(aClose.getStartTime())
						&& flight.getDepartureTime().before(aClose.getEndTime())) {
					return 1;
				}
			}
		}
		for (AirPortClose aClose : flight.getDesintationAirport().getCloseSchedule()) {
			if (flight.getArrivalTime().after(aClose.getStartTime())
					&& flight.getArrivalTime().before(aClose.getEndTime())) {
				return 2;
			}
		}
		return 0;
	}
	
	/**
	 *  get next flight id
	 * @return
	 */
	public int getNextFlightId(){
		int maxFlightId = InitData.maxFligthId;
		InitData.maxFligthId = maxFlightId + 1;
		return maxFlightId + 1;
	}
	
	/**
	 * get new departure time by new arrival time
	 * @param flight
	 * @param newArrivalTime
	 * @return
	 */
	public Date getDepartureTimeByArrivalTime(Flight flight, Date newArrivalTime) {
		int flightTime = (int) getMinuteDifference(flight.getArrivalTime(), flight.getDepartureTime());
		return addMinutes(newArrivalTime, -flightTime);
	}
	
	// common utility
	/** 
	 * add minutes to date
	 * @param date
	 * @param minutes
	 * @return
	 */
	public Date addMinutes(Date date, long minutes){
		Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    long t= cal.getTimeInMillis();
	    return new Date(t + (minutes * 60000));
	}
	
	/**
	 * get different between two date in minute
	 * @param time1
	 * @param time2
	 * @return
	 */
	public double getMinuteDifference(Date time1, Date time2){
		return (time1.getTime() - time2.getTime()) / (1000 * 60);
	}
}
