package xiaMengAirline.searchEngine;

import java.math.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import xiaMengAirline.Exception.*;
import xiaMengAirline.beans.*;
import xiaMengAirline.util.InitData;

public class SelfSearch {
	private static final Logger logger = Logger.getLogger(InitData.class);
	private final int domesticMaxDelay = 24;
	private final int internationalMaxDelay = 36;
	private final int minGroundTime = 50;
	private final int flightCancelCost = 1000;
	private final int flightDelayCost = 100;
	
	public XiaMengAirlineSolution constructInitialSolution(XiaMengAirlineSolution originalSolution)
			throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable {
		//when construct initial solution, clone a new copy
		XiaMengAirlineSolution aNewSolution = originalSolution.clone();
		XiaMengAirlineSolution rtnSolution = new XiaMengAirlineSolution();
		for (Aircraft aircraft : aNewSolution.getSchedule().values()){
			aircraft = adjustAircraft(aircraft, 0);
			if (aircraft != null){
				rtnSolution.replaceOrAddNewAircraft(aircraft);
			}
		}
		
		return rtnSolution;
	}
	
	public Aircraft adjustAircraft (Aircraft originalAir, int startIndex) throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable {
		Aircraft thisAc = originalAir.clone();
		HashMap<Integer, Aircraft> forkList = new HashMap<Integer, Aircraft>();
		
		//delete new flights in cancel flight list
		List<Integer> cancelFlightList = new ArrayList<Integer>();
		if (thisAc.getCancelAircrafted() != null && thisAc.getCancelAircrafted().getFlightChain() != null){

			for (int i = 0; i < thisAc.getCancelAircrafted().getFlightChain().size(); i++){
				if (thisAc.getCancelAircrafted().getFlightChain().get(i).getFlightId() > InitData.plannedMaxFligthId){
					cancelFlightList.add(i);
				}
			}
			thisAc.getCancelAircrafted().removeFlightChain(cancelFlightList);
		}
		
		// loop until all flight sorted
		Aircraft aircraft = thisAc.clone();
		boolean isFinish = false;
		int infinitLoopCnt = 0;
		if (startIndex == 0) {
			Flight firstFlight = aircraft.getFlightChain().get(0);
			FlightTime firstFlightTime = new FlightTime();
			
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy"); 
			Date startDate = df.parse("01/01/1970");
			firstFlightTime.setArrivalTime(startDate);
			firstFlightTime.setDepartureTime(firstFlight.getDepartureTime());
			
			FlightTime newfirstFlightTime = firstFlight.getSourceAirPort().requestAirport(firstFlightTime, minGroundTime);
			if (newfirstFlightTime != null && newfirstFlightTime.getDepartureTime() != null) {
				if (getHourDifference(firstFlight.getDepartureTime(), newfirstFlightTime.getDepartureTime()) > 6) {
					for (AirPortClose aClose : firstFlight.getSourceAirPort().getCloseSchedule()) {
						if (firstFlight.getDepartureTime().compareTo(aClose.getStartTime()) > 0
								&& firstFlight.getDepartureTime().compareTo(aClose.getEndTime()) < 0) {
							firstFlight.setDepartureTime(aClose.getEndTime());
						}
					}
				}else {
					firstFlight.setDepartureTime(newfirstFlightTime.getDepartureTime());
				}
			}
			
		}
		while (!isFinish){
			List<Flight> flights = aircraft.getFlightChain();
			try{
				boolean adjusted = aircraft.adjustFlightTime(startIndex);
				isFinish = true;
				if (startIndex == 0 && !adjusted){
					originalAir.setAlternativeAircraft(null);
				}
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
					Date tempDeparture = addMinutes(avaliableTime.getArrivalTime(), (int)getMinuteDifference(getPlannedDeparture(thisFlight), getPlannedArrival(thisFlight)));
					Date adjustedDeparture = getValidDeparture(tempDeparture, thisFlight.getSourceAirPort());

					if (isEligibalDelay(getPlannedDeparture(thisFlight), adjustedDeparture, thisFlight.isInternationalFlight())) {
						thisFlight.setDepartureTime(adjustedDeparture);
						thisFlight.calcuateNextArrivalTime();
						if (flightIndex < flights.size() - 1) {
							flights.get(flightIndex + 1).setDepartureTime(addMinutes(avaliableTime.getDepartureTime(), minGroundTime));
						}
					} else {
						try {
							aircraft = cancelFlight(aircraft, flightIndex);
							if (aircraft == null){
								print("Invalid aircraft: AicraftId " + thisAc.getId());
								return null;
							}
						} catch (Exception e){
							e.printStackTrace();
							print("Invalid aircraft: AicraftId " + thisAc.getId());
							return null;
						}
					}
					
					
					startIndex = flightIndex;
				}else{
					try {
						aircraft = cancelFlight(aircraft, flightIndex);
						if (aircraft == null){
							print("Invalid aircraft: AicraftId " + thisAc.getId());
							return null;
						}
					} catch (Exception e){
						e.printStackTrace();
						print("Invalid aircraft: AicraftId " + thisAc.getId());
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
					if (aircraft == null){
						print("Invalid aircraft: AicraftId " + thisAc.getId());
						return null;
					}
				} catch (Exception e){
					e.printStackTrace();
					print("Invalid aircraft: AicraftId " + thisAc.getId());
					return null;
				}
				startIndex = flightIndex;
			} catch (AirportNotAvailable ana){
				Flight thisFlight = ana.getaFlight();
				int flightIndex = aircraft.getFlightIndexByFlightId(thisFlight.getFlightId());
				try {
					aircraft = cancelFlight(aircraft, flightIndex);
					if (aircraft == null){
						print("Invalid aircraft: AicraftId " + thisAc.getId());
						return null;
					}
				} catch (Exception e){
					e.printStackTrace();
					print("Invalid aircraft: AicraftId " + thisAc.getId());
					return null;
				}
				startIndex = flightIndex;
			} catch (Exception e){
				// invalid
				e.printStackTrace();
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
		double thisCost = getAircraftCost(aircraft);
		
		Iterator<Entry<Integer, Aircraft>> it = forkList.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, Aircraft> pair = (Map.Entry<Integer, Aircraft>) it.next();
	        int nextStartIndex = pair.getKey();
	        Aircraft nextForkAc = pair.getValue();
	        Aircraft newReturnAc = adjustAircraft(nextForkAc, nextStartIndex);
	        double itCost = getAircraftCost(newReturnAc);
	        if (thisCost > itCost){
	        	aircraft = newReturnAc;
	        	thisCost = itCost;
	        }
	    }
	    if (startIndex == 0){
			originalAir.setAlternativeAircraft(aircraft);
		}
	    return aircraft;
	}
	
	public void print (String str){
		System.out.println(str);
	}
	
	// cancel a flight
	public Aircraft cancelFlight(Aircraft aircraft, int flightIndex) throws FlightDurationNotFound, CloneNotSupportedException, ParseException{
		List<Flight> flights = aircraft.getFlightChain();
		Flight thisFlight = flights.get(flightIndex);
		Flight newFlight = new Flight();
		newFlight.setDepartureTime(addMinutes(flights.get(flightIndex - 1).getArrivalTime(), minGroundTime));
		newFlight.setSourceAirPort(thisFlight.getSourceAirPort());
		HashMap<Integer, Flight> indexFlightPair = createNewFlight(newFlight, flightIndex, aircraft);
		if (indexFlightPair != null){
			Map.Entry<Integer,Flight> entry=indexFlightPair.entrySet().iterator().next();
			int cancelFlightEndIndex = entry.getKey();
			newFlight = entry.getValue();
			List<Integer> removeFlightIndeces = new ArrayList<Integer>();
			if (aircraft.getCancelledAircraft() == null){
				aircraft.setCancelAircrafted(aircraft.clone());
				aircraft.getCancelledAircraft().setFlightChain(new ArrayList<Flight>());
			}
			for (int cancelIndex = flightIndex; cancelIndex < cancelFlightEndIndex + 1; cancelIndex++){
				if (flights.get(cancelIndex).getFlightId() <= InitData.plannedMaxFligthId){
					aircraft.getCancelledAircraft().addFlight(flights.get(cancelIndex));
				}
				removeFlightIndeces.add(cancelIndex);
			}
			aircraft.removeFlightChain(removeFlightIndeces);
			if (!newFlight.getSourceAirPort().getId().equals(newFlight.getDesintationAirport().getId())){
				aircraft.addFlight(flightIndex, newFlight);
			}
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
	
	// get joint flight
	public Flight getJointFlight(Flight flight){
		return InitData.jointFlightMap.get(flight.getFlightId());
	}
	
	public boolean isJointFlight(Flight flight){
		return InitData.jointFlightMap.keySet().contains(flight.getFlightId()) ? true : false;
	}
	
	// create new flight
	public HashMap<Integer, Flight> createNewFlight(Flight prototypeFlight, int flightPosition, Aircraft aircraft) throws FlightDurationNotFound, CloneNotSupportedException, ParseException{
		HashMap<Integer, Flight> newIndexAndFlight = createEligibalFlight(aircraft.getFlightChain(), flightPosition, aircraft, prototypeFlight);
		if (newIndexAndFlight != null){
			Map.Entry<Integer,Flight> entry=newIndexAndFlight.entrySet().iterator().next();
			int destIndex = entry.getKey();
			Flight newFlight = entry.getValue();
			newFlight.setFlightId(getNextFlightId());
			newFlight.setAssignedAir(aircraft);
			
			HashMap<Integer, Flight> indexAndFlight = new HashMap<Integer, Flight>();
			indexAndFlight.put(destIndex, newFlight);
			return indexAndFlight;
		}
		return null;
	}
	
	// get next normal airport
	public HashMap<Integer, Flight> createEligibalFlight(List<Flight> flightChain, int currentFlightIndex, Aircraft ac, Flight newFlight) throws CloneNotSupportedException, ParseException{
		Flight thisFlight = newFlight;
		for (int i = currentFlightIndex + 1; i < flightChain.size(); i++){
			Flight nextFlight = flightChain.get(i);
			AirPort destAirport = nextFlight.getSourceAirPort();
			// if new flight is a chain cancel
			if (thisFlight.getSourceAirPort().getId().equals(destAirport.getId())){
				thisFlight.setDesintationAirport(destAirport);
				Flight lastFlight = flightChain.get(currentFlightIndex - 1);
				FlightTime tempFlightTime = new FlightTime();
				tempFlightTime.setArrivalTime(lastFlight.getArrivalTime());
				tempFlightTime.setDepartureTime(nextFlight.getDepartureTime());
				if (nextFlight.getSourceAirPort().requestAirport(tempFlightTime, minGroundTime) == null){
					tempFlightTime.setArrivalTime(nextFlight.getArrivalTime());
					if (i < flightChain.size() - 2){
						tempFlightTime.setDepartureTime(flightChain.get(i + 1).getDepartureTime());
						if (nextFlight.getDesintationAirport().requestAirport(tempFlightTime, minGroundTime) == null){
							if (!isValidParking(thisFlight.getArrivalTime(), nextFlight.getDepartureTime(), thisFlight.getDesintationAirport())){
								continue;
							}
							HashMap<Integer, Flight> destIndexAndNewFight = new HashMap<Integer, Flight>();
							destIndexAndNewFight.put(i - 1, thisFlight);
							return destIndexAndNewFight;
						}else{
							continue;
						}
					} else {
						if (!isValidParking(thisFlight.getArrivalTime(), nextFlight.getDepartureTime(), thisFlight.getDesintationAirport())){
							continue;
						}
						HashMap<Integer, Flight> destIndexAndNewFight = new HashMap<Integer, Flight>();
						destIndexAndNewFight.put(i - 1, thisFlight);
						return destIndexAndNewFight;
					}
					
				}
				continue;
			}
			
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
				thisFlight.setPlannedFlight(thisFlight.clone());
				List<Flight> newFlightChain = new ArrayList<Flight>();
				newFlightChain.add(thisFlight);
				newFlightChain.add(nextFlight);
				Aircraft newAircraft = ac.clone();
				newAircraft.setFlightChain(newFlightChain);
				try {
					newAircraft.adjustFlightTime(0);
					HashMap<Integer, Flight> destIndexAndNewFight = new HashMap<Integer, Flight>();
					destIndexAndNewFight.put(i - 1, thisFlight);
					if (!isValidParking(thisFlight.getArrivalTime(), nextFlight.getDepartureTime(), thisFlight.getDesintationAirport())){
						continue;
					}
					return destIndexAndNewFight;
				} catch (AirportNotAcceptArrivalTime anaat){
					FlightTime avaliableTime = anaat.getAvailableTime();
					if (getHourDifference(avaliableTime.getArrivalTime(), getPlannedArrival(thisFlight)) < 24){
						//Date adjustedDeparture = getValidDeparture(tempDeparture, thisFlight.getSourceAirPort());
						
						if (!isValidParking(avaliableTime.getArrivalTime(), avaliableTime.getDepartureTime(), thisFlight.getDesintationAirport())){
							continue;
						}
						thisFlight.setArrivalTime(avaliableTime.getArrivalTime());
						thisFlight.setDepartureTime(addMinutes(thisFlight.getArrivalTime(), -flightTime));
						HashMap<Integer, Flight> destIndexAndNewFight = new HashMap<Integer, Flight>();
						destIndexAndNewFight.put(i - 1, thisFlight);
						return destIndexAndNewFight;
					}else{
						continue;
					}
				} catch (Exception e){
					continue;
				}
			}
		}
		
		// unable to find next flight destination
		print("New flight unable to find next available airport, flightID" + flightChain.get(currentFlightIndex).getFlightId());
		logger.info("New flight unable to find next available airport, flightID" + flightChain.get(currentFlightIndex).getFlightId());
		return null;
	}
	
	// tell if a flight is international between two airport
	public boolean isInternational(String airport1, String airport2){
		if (InitData.domesticAirportList.contains(airport1) && InitData.domesticAirportList.contains(airport2)){
			return false;
		}
		return true;
	}
	
	public boolean isEligibalAircraft(Aircraft aircraft, AirPort sourceAir, AirPort destAir){
		String searchKey = aircraft.getId() + "_" + sourceAir.getId() + "_" + destAir.getId();
		return InitData.airLimitationList.contains(searchKey) ? false : true;
	}
	
	// get flight time between two airports
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
	
	// add minutes to date
	public Date addMinutes(Date date, long minutes){
		Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    long t= cal.getTimeInMillis();
	    return new Date(t + (minutes * 60000));
	}
	
	// flight is eligible for delay
	public boolean isEligibalDelay(Date planTime, Date adjustTime, boolean isInternational){
		if (getMinuteDifference(adjustTime, planTime) > (isInternational ? internationalMaxDelay * 60 : domesticMaxDelay * 60)){
			return false;
		}
		
		return true;
	}
	
	// get time difference between time1 and time2, time1 > time2 is positive otherwise negative
	public double getHourDifference(Date time1, Date time2){
		return (time1.getTime() - time2.getTime()) / (1000 * 60 * 60);
	}
	
	public double getMinuteDifference(Date time1, Date time2){
		return (time1.getTime() - time2.getTime()) / (1000 * 60);
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
	
	// get aircraft cost for local comparison
	public double getAircraftCost(Aircraft ac){
		XiaMengAirlineSolution solution = new XiaMengAirlineSolution();
		solution.replaceOrAddNewAircraft(ac);
		if (ac.getCancelAircrafted() != null){
			solution.replaceOrAddNewAircraft(ac.getCancelAircrafted());
		}
		solution.refreshCost(false); 
		return solution.getCost().doubleValue();
	}
	
	// get aircraft cost for local comparison
	public BigDecimal getAircraftCostSolutionLv(Aircraft ac){
		XiaMengAirlineSolution solution = new XiaMengAirlineSolution();
		solution.replaceOrAddNewAircraft(ac);
		if (ac.getCancelAircrafted() != null){
			solution.replaceOrAddNewAircraft(ac.getCancelAircrafted());
		}
		solution.refreshCost(false); 
		return solution.getCost();
	}
	
	public Date getValidDeparture(Date departureTime, AirPort airport) throws ParseException {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (departureTime.compareTo(aClose.getStartTime()) > 0
						&& departureTime.compareTo(aClose.getEndTime()) < 0) {
					return aClose.getEndTime();
				}
			}
		}
		for (RegularAirPortClose aClose : airport.getRegularCloseSchedule()) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(departureTime);
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();
			
			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			Date aCloseDate = formatter2.parse(aDateC);
			Date aOpenDate = formatter2.parse(aDateO);
			
			if (departureTime.after(aCloseDate)
					&& departureTime.before(aOpenDate)) {
				return aOpenDate;
			}
		} 
		return departureTime;
	}
	
	public boolean isValidParking(Date arrivalTime, Date departureTime, AirPort airport){
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (arrivalTime.compareTo(aClose.getStartTime()) <= 0
					&& departureTime.compareTo(aClose.getEndTime()) >= 0) {
				return false;
			}
		}
		return true;
	}
}
