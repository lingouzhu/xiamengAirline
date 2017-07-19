package xiaMengAirline.searchEngine;

import java.math.*;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

import xiaMengAirline.Exception.*;
import xiaMengAirline.beans.*;
import xiaMengAirline.util.InitData;

public class SelfSearch {
	private final int domesticMaxDelay = 24;
	private final int internationalMaxDelay = 36;
	private final int minGroundTime = 50;
	private final int flightCancelCost = 1000;
	private final int flightDelayCost = 100;
	
	public XiaMengAirlineSolution constructInitialSolution(XiaMengAirlineSolution originalSolution)
			throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable {
		//when construct initial solution, clone a new copy
		XiaMengAirlineSolution aNewSolution = originalSolution.clone();
		for (Aircraft aircraft : aNewSolution.getSchedule().values()){
			aircraft = adjustAircraft(aircraft, 0);
		}
		return aNewSolution;
	}
	
	public Aircraft adjustAircraft (Aircraft originalAir, int startIndex) throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable {
		Aircraft thisAc = originalAir.clone();
		HashMap<Integer, Aircraft> forkList = new HashMap<Integer, Aircraft>();
		
		//delete new flights in cancel flight list
		List<Integer> cancelFlightList = new ArrayList<Integer>();
		for (int i = 0; i < thisAc.getCancelAircrafted().getFlightChain().size(); i++){
			if (thisAc.getCancelAircrafted().getFlightChain().get(i).getFlightId() > InitData.plannedMaxFligthId){
				cancelFlightList.add(i);
			}
		}
		thisAc.getCancelAircrafted().removeFlightChain(cancelFlightList);
		
		// loop until all flight sorted
		Aircraft aircraft = thisAc.clone();
		boolean isFinish = false;
		int infinitLoopCnt = 0;
		while (!isFinish){
			List<Flight> flights = aircraft.getFlightChain();
			try{
				aircraft.adjustFlightTime(startIndex);
				isFinish = true;
			} catch (AirportNotAcceptArrivalTime anaat){
				Flight thisFlight = anaat.getaFlight();
				FlightTime avaliableTime = anaat.getAvailableTime();
				int flightIndex = aircraft.getFlightIndexByFlightId(thisFlight.getFlightId());
				
				if (avaliableTime.isIsTyphoon() && isJointFlight(thisFlight) && getJointFlight(thisFlight) != null
						&& !thisFlight.isInternationalFlight() && !getJointFlight(thisFlight).isInternationalFlight()){
					Aircraft forkAir = aircraft.clone();
					Flight firstFlight = forkAir.getFlightChain().get(flightIndex);
					Flight secondFlight = forkAir.getFlightChain().get(flightIndex + 1); 
					firstFlight.setDesintationAirport(secondFlight.getDesintationAirport());
					firstFlight.setArrivalTime(addMinutes(firstFlight.getDepartureTime(), getJointFlightDuration(firstFlight, secondFlight, forkAir)));
					forkAir.moveToDropOut(secondFlight);
					forkList.put(flightIndex, forkAir);
				}
				
				if (isEligibalDelay(getPlannedArrival(thisFlight), avaliableTime.getArrivalTime(), thisFlight.isInternationalFlight())){
					thisFlight.setArrivalTime(avaliableTime.getArrivalTime());
					thisFlight.setDepartureTime(addMinutes(thisFlight.getArrivalTime(), (int)getMinuteDifference(getPlannedDeparture(thisFlight), getPlannedArrival(thisFlight))));
					flights.get(flightIndex + 1).setDepartureTime(avaliableTime.getDepartureTime());
				}else{
					try {
						aircraft = cancelFlight(aircraft, flightIndex);
					} catch (Exception e){
						return null;
					}
					startIndex = flightIndex;
				}
			} catch (AirportNotAcceptDepartureTime anadt){
				Flight thisFlight = anadt.getaFlight();
				int flightIndex = aircraft.getFlightIndexByFlightId(thisFlight.getFlightId());
				if (isJointFlight(thisFlight) && getJointFlight(thisFlight) == null
						&& !thisFlight.isInternationalFlight() && !flights.get(flightIndex - 1).isInternationalFlight()){
					Aircraft forkAir = aircraft.clone();
					Flight firstFlight = forkAir.getFlightChain().get(flightIndex - 1);
					Flight secondFlight = forkAir.getFlightChain().get(flightIndex); 
					firstFlight.setDesintationAirport(secondFlight.getDesintationAirport());
					firstFlight.setArrivalTime(addMinutes(firstFlight.getDepartureTime(), getJointFlightDuration(firstFlight, secondFlight, forkAir)));
					forkAir.moveToDropOut(secondFlight);
					forkList.put(flightIndex, forkAir);
				}
				
				try {
					aircraft = cancelFlight(aircraft, flightIndex);
				} catch (Exception e){
					return null;
				}
				startIndex = flightIndex;
			} catch (AirportNotAvailable ana){
				Flight thisFlight = ana.getaFlight();
				int flightIndex = aircraft.getFlightIndexByFlightId(thisFlight.getFlightId());
				try {
					aircraft = cancelFlight(aircraft, flightIndex);
				} catch (Exception e){
					return null;
				}
				startIndex = flightIndex;
			} catch (Exception e){
				// invalid
				return null;
			}

			if (infinitLoopCnt > 1){
				// last flight cannot be adjusted, invalid flight chain
				return null;
			}
			
			if (startIndex == flights.size() - 1){
				infinitLoopCnt++;
			}
		}
		
		double thisCost = getCost(aircraft);
		
		Iterator<Entry<Integer, Aircraft>> it = forkList.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, Aircraft> pair = (Map.Entry<Integer, Aircraft>) it.next();
	        int nextStartIndex = pair.getKey();
	        Aircraft nextForkAc = pair.getValue();
	        Aircraft newReturnAc = adjustAircraft(nextForkAc, nextStartIndex);
	        double itCost = getCost(newReturnAc);
	        if (thisCost > itCost){
	        	aircraft = newReturnAc;
	        	thisCost = itCost;
	        }
	    }
		return aircraft;
	}
	
	// cancel a flight
	public Aircraft cancelFlight(Aircraft aircraft, int flightIndex) throws FlightDurationNotFound, CloneNotSupportedException{
		List<Flight> flights = aircraft.getFlightChain();
		Flight thisFlight = flights.get(flightIndex);
		Flight newFlight = new Flight();
		newFlight.setDepartureTime(addMinutes(flights.get(flightIndex - 1).getArrivalTime(), minGroundTime));
		newFlight.setSourceAirPort(thisFlight.getSourceAirPort());
		HashMap<Integer, Flight> indexFlightPair = createNewFlight(newFlight, flightIndex, aircraft);
		if (indexFlightPair != null){
			Map.Entry<Integer,Flight> entry=indexFlightPair.entrySet().iterator().next();
			int cancelFlightEndIndex = entry.getKey();
			newFlight = entry.getValue();List<Integer> removeFlightIndeces = new ArrayList<Integer>();
			for (int cancelIndex = flightIndex; cancelIndex < cancelFlightEndIndex + 1; cancelIndex++){
				if (flights.get(cancelIndex).getFlightId() <= InitData.plannedMaxFligthId){
					aircraft.getCancelledAircraft().addFlight(flights.get(cancelIndex));
				}
				removeFlightIndeces.add(cancelIndex);
			}
			aircraft.removeFlightChain(removeFlightIndeces);
			if (!thisFlight.getSourceAirPort().getId().equals(newFlight.getSourceAirPort().getId())){
				aircraft.addFlight(newFlight);
			}
			aircraft.sortFlights();
			return aircraft;
		}
		return null;
	}
	
	// get joint flight's flight duration
	public int getJointFlightDuration(Flight firstFlight, Flight secondFlight, Aircraft aircraft){
		String searchKey = aircraft.getId() + "_" + firstFlight.getSourceAirPort() + "_" + secondFlight.getDesintationAirport();
		if (InitData.fightDurationMap.containsKey(searchKey)){
			return InitData.fightDurationMap.get(searchKey);
		}else{
			Double flightTime = getMinuteDifference(firstFlight.getArrivalTime(), firstFlight.getDepartureTime())
					+ getMinuteDifference(secondFlight.getArrivalTime(), secondFlight.getArrivalTime());
			return flightTime.intValue();
		}
	}
	
	// remove the extra time margin 
	public Aircraft shrinkFlightChain(Aircraft originalAir, int startIndex) throws FlightDurationNotFound{
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
	
	// get joint flight
	public Flight getJointFlight(Flight flight){
		return InitData.jointFlightMap.get(flight.getFlightId());
	}
	
	public boolean isJointFlight(Flight flight){
		return InitData.jointFlightMap.keySet().contains(flight.getFlightId()) ? true : false;
	}
	
	// create new flight
	public HashMap<Integer, Flight> createNewFlight(Flight prototypeFlight, int flightPosition, Aircraft aircraft) throws FlightDurationNotFound, CloneNotSupportedException{
		HashMap<Integer, AirPort> newDestAirport = getNextAvaliableAirport(aircraft.getFlightChain(), flightPosition, aircraft, prototypeFlight);
		if (newDestAirport != null){
			Map.Entry<Integer,AirPort> entry=newDestAirport.entrySet().iterator().next();
			int destIndex = entry.getKey();
			AirPort destAirport =entry.getValue();
			Flight newFlight = new Flight();
			newFlight.setFlightId(getNextFlightId());
			newFlight.setSourceAirPort(prototypeFlight.getSourceAirPort());
			newFlight.setDesintationAirport(destAirport);
			newFlight.setAssignedAir(aircraft);
			newFlight.setDepartureTime(prototypeFlight.getDepartureTime());
			newFlight.setArrivalTime(newFlight.calcuateNextArrivalTime());
			
			HashMap<Integer, Flight> indexAndFlight = new HashMap<Integer, Flight>();
			indexAndFlight.put(destIndex, newFlight);
			return indexAndFlight;
		}
		return null;
	}
	
	// get next normal airport
	public HashMap<Integer, AirPort> getNextAvaliableAirport(List<Flight> flightChain, int currentFlightIndex, Aircraft ac, Flight newFlight) throws CloneNotSupportedException{
		Flight thisFlight = newFlight;
		for (int i = currentFlightIndex + 1; i < flightChain.size(); i++){
			Flight nextFlight = flightChain.get(i);
			AirPort destAirport = nextFlight.getSourceAirPort();
			
			// international flight is not eligible
			if (isInternational(thisFlight.getSourceAirPort().getId(), destAirport.getId())){
				continue;
			}
			// aircraft constraint
			if (!isEligibalAircraft(ac, thisFlight.getSourceAirPort(), destAirport)){
				continue;
			}
			
			long flightTime = getFlightTime(thisFlight.getSourceAirPort().getId(), destAirport.getId(), ac);
			if (flightTime > 0){
				thisFlight.setArrivalTime(addMinutes(thisFlight.getDepartureTime(), flightTime));
				thisFlight.setDesintationAirport(destAirport);
				thisFlight.setPlannedFlight(thisFlight);
				
				List<Flight> newFlightChain = new ArrayList<Flight>();
				newFlightChain.add(thisFlight);
				newFlightChain.add(nextFlight);
				Aircraft newAircraft = ac.clone();
				newAircraft.setFlightChain(newFlightChain);
				try {
					newAircraft.adjustFlightTime(0);
				} catch (AirportNotAcceptArrivalTime anaat){
					if (isEligibalDelay(getPlannedDeparture(nextFlight), anaat.getAvailableTime().getDepartureTime(), nextFlight.isInternationalFlight())){
						double delayTime = getMinuteDifference(anaat.getAvailableTime().getDepartureTime(), getPlannedDeparture(nextFlight));
						if (delayTime * flightDelayCost > flightCancelCost){
							// need adjustment
							HashMap<Integer, AirPort> destIndexAndAiport = new HashMap<Integer, AirPort>();
							destIndexAndAiport.put(i, destAirport);
							return destIndexAndAiport;
						} else {
							Date newDepartureTime = addMinutes(anaat.getAvailableTime().getArrivalTime(), (int)getMinuteDifference(getPlannedDeparture(thisFlight), getPlannedArrival(thisFlight)));
							for (AirPortClose aClose : destAirport.getCloseSchedule()){
								if (newDepartureTime.compareTo(aClose.getStartTime()) > 0
										&& newDepartureTime.compareTo(aClose.getEndTime()) < 0){
									continue;
								}
							}
							HashMap<Integer, AirPort> destIndexAndAiport = new HashMap<Integer, AirPort>();
							destIndexAndAiport.put(i, destAirport);
							return destIndexAndAiport;
						}
					}else{
						continue;
					}
				} catch (Exception e){
					continue;
				}
			}
		}
		
		// unable to find next flight destination
		return null;
	}
	
	// tell if a flight is international between two airport
	public boolean isInternational(String airport1, String airport2){
		if (InitData.domesticAirportList.contains(airport1) && InitData.domesticAirportList.contains(airport2)){
			return true;
		}
		return false;
	}
	
	public boolean isEligibalAircraft(Aircraft aircraft, AirPort sourceAir, AirPort destAir){
		String searchKey = aircraft.getId() + "_" + sourceAir.getId() + "_" + destAir.getId();
		return InitData.airLimitationList.contains(searchKey) ? true : false;
	}
	
	// get flight time between two airports
	public long getFlightTime(String airport1Id, String airport2Id, Aircraft aircraft){
		String searchKey = aircraft.getType();
		searchKey += "_";
		searchKey += airport1Id;
		searchKey += "_";
		searchKey += airport2Id;
		
		long flightTime = InitData.fightDurationMap.get(searchKey);
		if (flightTime > 0){
			return flightTime;
		}
		return 0;
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
	
	public double getMinuteDifference(Date time1, Date time2){
		BigDecimal diff = new BigDecimal(time1.getTime() - time2.getTime()).setScale(4, RoundingMode.HALF_UP);
		return diff.divide(new BigDecimal((1000 * 60))).doubleValue();
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
		int maxFlightId = InitData.maxFligthId;
		InitData.maxFligthId = maxFlightId + 1;
		return maxFlightId + 1;
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
	
	// compare cost
	public double getCost(Aircraft ac){
		XiaMengAirlineSolution solution = new XiaMengAirlineSolution();
		solution.replaceOrAddNewAircraft(ac);
		solution.refreshCost(false); 
		return solution.getCost().doubleValue();
	}
}
