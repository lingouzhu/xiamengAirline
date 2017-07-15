package xiaMengAirline.searchEngine;

import java.math.*;
import java.text.ParseException;
import java.util.*;

import xiaMengAirline.beans.*;
import xiaMengAirline.util.InitData;

public class SelfSearch {
	private final int domesticMaxDelay = 24;
	private final int internationalMaxDelay = 36;
	private final int minGroundTime = 50;
	
	public XiaMengAirlineSolution constructInitialSolution(XiaMengAirlineSolution originalSolution)
			throws CloneNotSupportedException {
		//when construct initial solution, clone a new copy
		XiaMengAirlineSolution aNewSolution = originalSolution.clone();
		
		return aNewSolution;
	}
	
	public Aircraft adjustAircraft (Aircraft originalAir) throws CloneNotSupportedException, ParseException {
		//when construct new plan for aircraft, no need clone?
		List<Integer> canceledFlightIDs = new ArrayList<Integer>();
		Aircraft thisAc = originalAir.clone();
		thisAc = shrinkFlightChain(thisAc, 0);
		List<Flight> flights = thisAc.getFlightChain();
		for (int i = 1; i < flights.size(); i++){
			Flight lastFlight = flights.get(getLastFlightIndex(canceledFlightIDs, i));
			Flight thisFlight = flights.get(i);
			
			if (canceledFlightIDs.contains(thisFlight.getFlightId())){
				continue;
			}
			
			FlightTime flightPair = new FlightTime();
			AirPort thisAirport = thisFlight.getSourceAirPort();
			
			Date lastArrival = lastFlight.getArrivalTime();
			Date thisDeparture = thisFlight.getDepartureTime();
			flightPair.setArrivalTime(lastArrival);
			flightPair.setDepartureTime(thisDeparture);
			flightPair = thisAirport.requestAirport(flightPair, minGroundTime);
			if (flightPair.getArrivalTime() != null){
				if (!isEligibalDelay(getPlannedArrival(lastFlight), flightPair.getArrivalTime(), lastFlight.isInternationalFlight())){
					// cancel flight, add empty flight
					if (1 != 1){
						// dummy chain flight
					}else{
						for (int j = getLastFlightIndex(canceledFlightIDs, i); j > -1; j--){
							if (canceledFlightIDs.contains(flights.get(j).getFlightId())){
								continue;
							}
							Flight newFlight = createNewFlight(flights.get(j), j, thisAc);
							if (newFlight != null){
								thisAc.getCancelledAircraft().addFlight(flights.get(j));
								canceledFlightIDs.add(flights.get(j).getFlightId());
								boolean isNormal = false;
								List<Integer> putBackIDs = new ArrayList<Integer>();
								for (int m = j + 1; m < flights.size(); m++){
									if (isNormal){
										for (int n = 0; n > canceledFlightIDs.size(); n++){
											if (canceledFlightIDs.get(n) == flights.get(m).getFlightId()){
												putBackIDs.add(canceledFlightIDs.get(n));
											}
										}
										canceledFlightIDs.removeAll(putBackIDs);
										continue;
									}
									
									if (!flights.get(m).getSourceAirPort().getId().equals(newFlight.getDesintationAirport())){
										canceledFlightIDs.add(flights.get(m).getFlightId());
									}else{
										shrinkFlightChain(thisAc, m);
										isNormal = true;
									}
								}
								flights.set(j, newFlight);
								i = j;
								break;
							}else{
								if (j == 0){
									return null;
								}
							}
						}
					}
				}else{
					lastFlight.setArrivalTime(flightPair.getArrivalTime());
				}
			}
			if (flightPair.getDepartureTime() != null){
				if (!isEligibalDelay(getPlannedDeparture(thisFlight), flightPair.getDepartureTime(), thisFlight.isInternationalFlight())){
					// cancel flight, add empty flight
					if (1 != 1){
						// dummy chain flight
					}else{
						
						for (int j = i; j > -1; j--){
							if (canceledFlightIDs.contains(flights.get(j).getFlightId())){
								continue;
							}
							Flight newFlight = createNewFlight(flights.get(j), j, thisAc);
							if (newFlight != null){
								thisAc.getCancelledAircraft().addFlight(flights.get(j));
								canceledFlightIDs.add(flights.get(j).getFlightId());
								boolean isNormal = false;
								List<Integer> putBackIDs = new ArrayList<Integer>();
								for (int m = j + 1; m < flights.size(); m++){
									if (isNormal){
										for (int n = 0; n > canceledFlightIDs.size(); n++){
											if (canceledFlightIDs.get(n) == flights.get(m).getFlightId()){
												putBackIDs.add(canceledFlightIDs.get(n));
											}
										}
										canceledFlightIDs.removeAll(putBackIDs);
										continue;
									}
									
									if (!flights.get(m).getSourceAirPort().getId().equals(newFlight.getDesintationAirport())){
										canceledFlightIDs.add(flights.get(m).getFlightId());
									}else{
										shrinkFlightChain(thisAc, m);
										isNormal = true;
									}
								}
								flights.set(j, newFlight);
								i = j;
								break;
							}else{
								if (j == 0){
									return null;
								}
							}
						}
					}
				}else{
					thisFlight.setDepartureTime(flightPair.getDepartureTime());
				}
			}
		}
		
		// remove flights in canceledFlightIDs
		List<Flight> removedFlight = new ArrayList<Flight>();
		for (int flightId : canceledFlightIDs){
			removedFlight.add(thisAc.getFlightByFlightId(flightId));
		}
		thisAc.getFlightChain().removeAll(removedFlight);
		return thisAc;
	}
	
	// remove the extra time margin 
	public Aircraft shrinkFlightChain(Aircraft originalAir, int startIndex){
		List<Flight> flights = originalAir.getFlightChain();
		for (int i = startIndex; i < flights.size(); i++){
			Flight thisFlight = flights.get(i);
			if (i == 0){
				if (isLaterThan(getPlannedArrival(thisFlight),thisFlight.getDepartureTime())) {
					return null;
				}else if (isEarlierThan(getPlannedArrival(thisFlight),thisFlight.getDepartureTime())){
					thisFlight.setDepartureTime(getPlannedArrival(thisFlight));
					thisFlight.setArrivalTime(thisFlight.calcuateNextArrivalTime());
				}
			} else{
				Flight lastFlight = flights.get(i-1);
				Date lastArrival = lastFlight.getArrivalTime();
				Date thisDeparture = isLaterThan(addMinutes(lastArrival, minGroundTime), getPlannedDeparture(thisFlight)) ?
						addMinutes(lastArrival, minGroundTime) : getPlannedDeparture(thisFlight);
				thisFlight.setDepartureTime(thisDeparture);
				Date thisArrival = thisFlight.calcuateNextArrivalTime();
				thisFlight.setArrivalTime(thisArrival);
			}
		}
		originalAir.setFlightChain(flights);
		return originalAir;
	}
	
	// create new flight
	public Flight createNewFlight(Flight replaceFlight, int flightPosition, Aircraft aircraft) throws ParseException{
		AirPort newDestAirport = getNextAvaliableAirport(aircraft.getFlightChain(), flightPosition, aircraft);
		if (newDestAirport != null){
			Flight newFlight = new Flight();
			newFlight.setFlightId(getNextFlightId());
			newFlight.setSourceAirPort(replaceFlight.getSourceAirPort());
			newFlight.setDesintationAirport(newDestAirport);
			newFlight.setAssignedAir(aircraft);
			newFlight.setDepartureTime(replaceFlight.getDepartureTime());
			newFlight.setArrivalTime(newFlight.calcuateNextArrivalTime());
			return newFlight;
		}
		return null;
	}
	
	// get next normal airport
	public AirPort getNextAvaliableAirport(List<Flight> flightChain, int currentFlightIndex, Aircraft ac) throws ParseException{
		Flight thisFlight = flightChain.get(currentFlightIndex);
		for (int i = currentFlightIndex + 2; i < flightChain.size(); i++){
			Flight nextFlight = flightChain.get(i);
			AirPort destAirport = nextFlight.getSourceAirPort();
			if (isInternational(thisFlight.getSourceAirPort().getId(), destAirport.getId())){
				continue;
			}
			long flightTime = getFlightTime(thisFlight.getSourceAirPort().getId(), destAirport.getId(), ac);
			if (flightTime > 0){
				Date arrivalTime = addMinutes(thisFlight.getDepartureTime(), flightTime);
				Date nextDeparture = getPlannedDeparture(nextFlight);
				FlightTime flightPair = new FlightTime();
				flightPair.setArrivalTime(arrivalTime);
				flightPair.setDepartureTime(nextDeparture);
				flightPair = destAirport.requestAirport(flightPair, minGroundTime);
				if (flightPair.getDepartureTime() != null){
					if (isEligibalDelay(nextDeparture, flightPair.getDepartureTime(), false)){
						return destAirport;
					}
				}
			}
		}
		return null;
	}
	
	// tell if a flight is international between two airport
	public boolean isInternational(String airport1, String airport2){
		return false;
	}
	
	// get flight time between two airports
	public long getFlightTime(String airport1Id, String airport2Id, Aircraft aircraft){
		String searchKey = aircraft.getType();
		searchKey += "_";
		searchKey += airport1Id;
		searchKey += "_";
		searchKey += airport2Id;
		
		return InitData.fightDurationMap.get(searchKey);
	}
	
	// add minutes to date
	public Date addMinutes(Date date, long minutes){
		Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    long t= cal.getTimeInMillis();
	    return new Date(t + (minutes * 60000));
	}
	
	// flight is eligible for delay
	public boolean isEligibalDelay(Date planTime, Date adjustTime, boolean isInternational){
		if (getHourDifference(adjustTime, planTime) > (isInternational ? internationalMaxDelay : domesticMaxDelay)){
			return false;
		}
		
		return true;
	}
	
	// get time difference between time1 and time2, time1 > time2 is positive otherwise negative
	public double getHourDifference(Date time1, Date time2){
		BigDecimal diff = new BigDecimal(time1.getTime() - time2.getTime()).setScale(4, RoundingMode.HALF_UP);
		return diff.divide(new BigDecimal((1000 * 60 * 60))).doubleValue();
	}
	
	// get original arrival time
	public Date getPlannedArrival(Flight flight){
		return flight.getPlannedFlight().getArrivalTime();
	}
	
	// get original departure time
	public Date getPlannedDeparture(Flight flight){
		return flight.getPlannedFlight().getDepartureTime();
	}
	
	// a is later than b
	public boolean isLaterThan(Date time1, Date time2){
		if (time1.compareTo(time2) > 0){
			return true;
		}else{
			return false;
		}
	}
	
	// a is earlier than b
	public boolean isEarlierThan(Date time1, Date time2){
		if (time1.compareTo(time2) < 0){
			return true;
		}else{
			return false;
		}
	}
	
	// get next flight id
	public int getNextFlightId(){
		return 9999;
	}
	
	// get last flight index
	public int getLastFlightIndex(List<Integer> canceledFlightIDs, int thisIndex){
		if (thisIndex == 0) return 0;
		for (int i = thisIndex - 1; i > -1 ; i--){
			if (!canceledFlightIDs.contains(i)){
				return i;
			}
		}
		return 0;
	}
}
