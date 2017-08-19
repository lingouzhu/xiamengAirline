package xiaMengAirline.newBranch.LocalOptimize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import xiaMengAirline.newBranch.BasicObject.*;

public class AirlineSelector {
	//common variable
	private ArrayList<Flight> normalFlightPool; //unaffected flights
	private ArrayList<Flight> affectedFlightPool; //affected flights
	private ArrayList<Aircraft> aircraftPool; //sorted aircrafts
	private ArrayList<Aircraft> finishedAircraftList; //sorted aircrafts
	private ArrayList<Aircraft> unusedAircraftList; //sorted aircrafts
	private HashMap<String, Integer> lastFlightMap;
	private ArrayList<Flight> lastFlightList;
	//parametric
	//the smaller number the first to be fired
	private int prioritySameAircraftType = 10;
	private int priorityMaximumDelayTime = 20;
	private int priorityImportance = 30;
	private int prioritySameAircraft = 40;
	
	private int delayTimeThreshold = 300; // in minutes
	private int importanceThreshold = 3;
	
	private int[] priorities;
	
	/**
	 * Constructor
	 * @throws CloneNotSupportedException 
	 */
	public AirlineSelector(ArrayList<Aircraft> aircraftList) throws CloneNotSupportedException{
		normalFlightPool = new ArrayList<Flight>();
		affectedFlightPool = new ArrayList<Flight>();
		lastFlightMap = new HashMap<String, Integer>();
		lastFlightList = new ArrayList<Flight>();
		finishedAircraftList = new ArrayList<Aircraft>();
		unusedAircraftList = new ArrayList<Aircraft>();
		aircraftPool = sortAllAircraftsByCondition(aircraftList, 1);
		priorities = new int[]{prioritySameAircraftType, priorityMaximumDelayTime, priorityImportance, prioritySameAircraft};
		Arrays.sort(priorities);
		setFlightPools(aircraftPool);
	}
	
	public void pickup() throws CloneNotSupportedException {
		for (Aircraft aircraft : aircraftPool) {
			boolean reachedEnd = false;
			boolean aircraftUncompletable = false;
			ArrayList<Flight> followingFlightList = new ArrayList<Flight>();
			Flight lastFlight = aircraft.getFlight(aircraft.getFlightChain().size() - 1);
			while (!reachedEnd && !aircraftUncompletable) {
				int startIndex = 0;
				Flight nextFlight = pickNextFlight(lastFlight, aircraft, startIndex);
				if (nextFlight == null) {
					aircraftUncompletable = true;
				} else {
					Flight builtFlight = SelectorUtil.buildDelayedFlight(lastFlight, nextFlight); //perform clone as well
					followingFlightList.add(builtFlight);
					lastFlight = builtFlight;
				}
				
				if (lastFlightList.contains(nextFlight)) {
					reachedEnd = true;
				}
			}
			if (aircraftUncompletable) {
				unusedAircraftList.add(aircraft);
			}
			if (reachedEnd) {
				for (Flight flight : followingFlightList) {
					aircraft.addFlight(flight);
					normalFlightPool = SelectorUtil.removeFlightFromList(normalFlightPool, flight);
				}
				finishedAircraftList.add(aircraft);
			}
		}
		for (Flight flight : normalFlightPool) {
			affectedFlightPool.add(flight);
		}
	}
	
	public Flight pickNextFlight(Flight lastFlight, Aircraft aircraft, int startIndex) throws CloneNotSupportedException {
		ArrayList<Flight> cadidateFlightList = new ArrayList<Flight>();
		for (int i = startIndex; i < normalFlightPool.size(); i++) {
			Flight flight = normalFlightPool.get(i);
			//check constraint
			//check airport continuity
			if (!SelectorUtil.chkAirportContinuity(lastFlight, flight)) {
				continue;
			}
			//check if the aircraft can flight the route
			if (!SelectorUtil.chkValidRouteByAircraft(flight, aircraft)) {
				continue;
			}
			//check valid delay
			if (!SelectorUtil.chkPossibleDelay(lastFlight, flight)) {
				continue;
			}
			
			//add to the candidate list to be further filtered
			cadidateFlightList.add(flight);
		}
		
		if (cadidateFlightList.size() > 0) {
			for (int i = 0; i < priorities.length; i++) {
			    int priority = priorities[i];
			    if (priority == prioritySameAircraftType) {
			    	cadidateFlightList = filterByAircraftType(cadidateFlightList, aircraft.getType());
			    }
			    if (priority == priorityMaximumDelayTime) {
			    	cadidateFlightList = filterByMaximumDelayTime(cadidateFlightList, lastFlight);
			    }
			    if (priority == priorityImportance) {
			    	cadidateFlightList = filterByImportance(cadidateFlightList, aircraft.getType());
			    }
			    if (priority == prioritySameAircraft) {
			    	cadidateFlightList = filterByAircraft(cadidateFlightList, aircraft.getId());
			    }
			}
			cadidateFlightList = sortNormalFlightListByDepTime(cadidateFlightList);
			return cadidateFlightList.get(0);
		} else {
			return null;
		}
	}
	
	/**
	 * filter the flight list: the use the aircraft that has same type to fly the flight
	 * @param flightListToBeFiltered
	 * @param aircraftType
	 * @return
	 */
	public ArrayList<Flight> filterByAircraftType(ArrayList<Flight> flightListToBeFiltered, String aircraftType){
		ArrayList<Flight> filteredFlightList = new ArrayList<Flight>();
		for (Flight flight : flightListToBeFiltered) {
			if (flight.getAssignedAir().getType().equals(aircraftType)) {
				filteredFlightList.add(flight);
			}
		}
		if (filteredFlightList.size() == 0) {
			return flightListToBeFiltered;
		}
		return filteredFlightList;
	}
	
	/**
	 * filter the flight list: the use the original aircraft to fly the flight
	 * @param flightListToBeFiltered
	 * @param aircraftId
	 * @return
	 */
	public ArrayList<Flight> filterByAircraft(ArrayList<Flight> flightListToBeFiltered, String aircraftId){
		ArrayList<Flight> filteredFlightList = new ArrayList<Flight>();
		for (Flight flight : flightListToBeFiltered) {
			if (flight.getAssignedAir().getId().equals(aircraftId)) {
				filteredFlightList.add(flight);
			}
		}
		if (filteredFlightList.size() == 0) {
			return flightListToBeFiltered;
		}
		return filteredFlightList;
	}
	
	/**
	 * filter the flight list: within the maximum delay time
	 * @param flightListToBeFiltered
	 * @param aircraftId
	 * @return
	 * @throws CloneNotSupportedException 
	 */
	public ArrayList<Flight> filterByMaximumDelayTime(ArrayList<Flight> flightListToBeFiltered, Flight lastFlight) throws CloneNotSupportedException{
		ArrayList<Flight> filteredFlightList = new ArrayList<Flight>();
		for (Flight flight : flightListToBeFiltered) {
			Date earliestDeparture = SelectorUtil.calculateNextDeparture(lastFlight, flight);
			Date plannedDeparture = flight.getPlannedFlight().getDepartureTime();
			if (plannedDeparture.before(earliestDeparture)) {
				Flight clonedFlight = flight.clone();
				clonedFlight.setDepartureTime(earliestDeparture);
				clonedFlight.setArrivalTime(SelectorUtil.calculateArrivalTime(clonedFlight));
				long delayTime = SelectorUtil.calculateDelayTime(clonedFlight);
				if (delayTime <= delayTimeThreshold) {
					filteredFlightList.add(flight);
				}
			}
		}
		if (filteredFlightList.size() == 0) {
			return flightListToBeFiltered;
		}
		return filteredFlightList;
	}
	
	/**
	 * should use the same aircraft if possible when meet the importance threshold
	 * @param flightListToBeFiltered
	 * @return
	 */
	public ArrayList<Flight> filterByImportance(ArrayList<Flight> flightListToBeFiltered, String aircraftType){
		ArrayList<Flight> filteredFlightList = new ArrayList<Flight>();
		for (Flight flight : flightListToBeFiltered) {
			if (flight.getImpCoe().doubleValue() >= importanceThreshold) {
				if (flight.getPlannedAir().getId().equals(aircraftType)) {
					filteredFlightList.add(flight);
				}
			} else {
				filteredFlightList.add(flight);
			}
		}
		if (filteredFlightList.size() == 0) {
			return flightListToBeFiltered;
		}
		return filteredFlightList;
	}
	
	/**
	 * normal - get flights that are not in the typhoon window 
	 * 			or, none of the source or the destination airport of a flight is typhoon airport
	 * affected - flights that are affected by the typhoon
	 * @param aircrafts
	 * @throws CloneNotSupportedException 
	 */
	public void setFlightPools(ArrayList<Aircraft> aircrafts) throws CloneNotSupportedException{
		for (Aircraft aircraft : aircrafts){
			ArrayList<Flight> flightChain = new ArrayList<Flight>(aircraft.getFlightChain());
			ArrayList<Integer> indexOfFlightsToBeRemoved = new ArrayList<Integer>();
			for (int i = 0; i < flightChain.size(); i++){
				//should clone all flights
				Flight clonedFlight = flightChain.get(i).clone();
				if (SelectorUtil.chkFlightIsAdjustable(clonedFlight)){
					//flight adjustable
					indexOfFlightsToBeRemoved.add(i);
					if (SelectorUtil.chkFlightAffected(clonedFlight)){
						affectedFlightPool.add(clonedFlight);
					} else {
						normalFlightPool.add(clonedFlight);
					}
				}
				if (i == flightChain.size() - 1) {
					lastFlightMap.put(aircraft.getId(), clonedFlight.getFlightId());
					lastFlightList.add(clonedFlight);
				}
			}
			aircraft.removeFlightChain(indexOfFlightsToBeRemoved);
		}
		normalFlightPool = sortNormalFlightListByDepTime(normalFlightPool);
	}
	
	/**
	 * sort flight list by departure time
	 * @return
	 */
	public ArrayList<Flight> sortNormalFlightListByDepTime(ArrayList<Flight> flightList) {
		return flightList;
	}
	
	/**
	 * sort all aircrafts in a order to determine the order of aircraft selection
	 * @param sortMethodId
	 * @return sorted aircrafts base on the condition passed
	 */
	public ArrayList<Aircraft> sortAllAircraftsByCondition(ArrayList<Aircraft> aircraftList, int sortMethodId){
		//should clone all aircrafts
		ArrayList<Aircraft> sortedAircraftList = new ArrayList<Aircraft>();
		switch(sortMethodId){
			case 0:
				//random select
				break;
			case 1:
				//biggest aircraft first
				break;
			case 2:
				//smallest aircraft first
				break;
			case 3:
				//aircraft with most flights first
				break;
			case 4:
				//aircraft with least flight first
				break;
		}
		
		return sortedAircraftList;
	}


}
