package xiaMengAirline.searchEngine;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAcceptArrivalTime;
import xiaMengAirline.Exception.AirportNotAcceptDepartureTime;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.FlightTime;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.util.InitData;

public class SelfSearch {
	private static final Logger logger = Logger.getLogger(SelfSearch.class);
	private final int minGroundTime = 50; //plz ensure remove this!
	private XiaMengAirlineSolution mySolution = null;
	
	public XiaMengAirlineSolution constructInitialSolution()
			throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable, AircraftNotAdjustable {
		//when construct initial solution, clone a new copy

		List<Aircraft> airList = new ArrayList<Aircraft> (mySolution.getSchedule().values());
		for (Aircraft aircraft : airList){
			adjustAircraft(aircraft, 0);
		}
		
		return mySolution;
	}
	
	public Aircraft adjustAircraft (Aircraft originalAir, int startIndex) throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable, AircraftNotAdjustable {
		Aircraft thisAc = originalAir.clone();
		//original cancel air
		Aircraft thisAcCancel = mySolution.getAircraft(thisAc.getId(), thisAc.getType(), true, true);
		HashMap<Integer, Aircraft> forkList = new HashMap<Integer, Aircraft>();
		
		// loop until all flight sorted
		Aircraft aircraft = thisAc.clone();
		Aircraft aircraftCancel = thisAcCancel.clone();
		boolean isFinish = false;
		int infinitLoopCnt = 0;
		while (!isFinish){
			List<Flight> flights = aircraft.getFlightChain();
			try{
				aircraft.adjustFlightTime(startIndex);
				isFinish = true;
				if (startIndex == 0){
					originalAir.setAlternativeAircraft(null);
					thisAcCancel.setAlternativeAircraft(null);
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
					thisFlight.setArrivalTime(avaliableTime.getArrivalTime());
					thisFlight.setDepartureTime(addMinutes(thisFlight.getArrivalTime(), (int)getMinuteDifference(getPlannedDeparture(thisFlight), getPlannedArrival(thisFlight))));
					flights.get(flightIndex + 1).setDepartureTime(avaliableTime.getDepartureTime());
					startIndex = flightIndex;
				}else{
					try {
						aircraft = cancelFlight(aircraft, aircraftCancel, flightIndex);
						if (aircraft == null){
							print("Invalid aircraft: AicraftId " + thisAc.getId());
							throw new AircraftNotAdjustable(aircraft);
						}
					} catch (Exception e){
						e.printStackTrace();
						print("Invalid aircraft: AicraftId " + thisAc.getId());
						throw new AircraftNotAdjustable(aircraft);
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
					aircraft = cancelFlight(aircraft, aircraftCancel, flightIndex);
					if (aircraft == null){
						print("Invalid aircraft: AicraftId " + thisAc.getId());
						throw new AircraftNotAdjustable(aircraft);
					}
				} catch (Exception e){
					e.printStackTrace();
					print("Invalid aircraft: AicraftId " + thisAc.getId());
					throw new AircraftNotAdjustable(aircraft);
				}
				startIndex = flightIndex;
			} catch (AirportNotAvailable ana){
				Flight thisFlight = ana.getaFlight();
				int flightIndex = aircraft.getFlightIndexByFlightId(thisFlight.getFlightId());
				try {
					aircraft = cancelFlight(aircraft, aircraftCancel, flightIndex);
					if (aircraft == null){
						print("Invalid aircraft: AicraftId " + thisAc.getId());
						throw new AircraftNotAdjustable(aircraft);
					}
				} catch (Exception e){
					e.printStackTrace();
					print("Invalid aircraft: AicraftId " + thisAc.getId());
					throw new AircraftNotAdjustable(aircraft);
				}
				startIndex = flightIndex;
			} catch (Exception e){
				// invalid
				e.printStackTrace();
				throw new AircraftNotAdjustable(aircraft);
			}

			if (infinitLoopCnt > 1){
				// last flight cannot be adjusted, invalid flight chain
				throw new AircraftNotAdjustable(aircraft);
			}
			
			if (startIndex == flights.size() - 1){
				infinitLoopCnt++;
			}
		}
		double thisCost = getAircraftCost(aircraft, aircraftCancel);
		
		Iterator<Entry<Integer, Aircraft>> it = forkList.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, Aircraft> pair = (Map.Entry<Integer, Aircraft>) it.next();
	        int nextStartIndex = pair.getKey();
	        Aircraft nextForkAc = pair.getValue();
	        Aircraft newReturnAc = adjustAircraft(nextForkAc, nextStartIndex);
	        double itCost = getAircraftCost(newReturnAc, aircraftCancel);
	        if (thisCost > itCost){
	        	aircraft = newReturnAc;
	        	thisCost = itCost;
	        }
	    }
	    if (startIndex != 0){
			originalAir.setAlternativeAircraft(aircraft);
			thisAcCancel.setAlternativeAircraft(aircraftCancel);
		}
	    return aircraft;
	}
	
	public SelfSearch(XiaMengAirlineSolution mySolution) {
		super();
		this.mySolution = mySolution;
	}

	public void print (String str){
		logger.info(str);
	}
	
	// cancel a flight
	public Aircraft cancelFlight(Aircraft aircraft, Aircraft aircraftCancel, int flightIndex) throws FlightDurationNotFound, CloneNotSupportedException, ParseException{
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

			for (int cancelIndex = flightIndex; cancelIndex < cancelFlightEndIndex + 1; cancelIndex++){
				if (flights.get(cancelIndex).getFlightId() <= InitData.plannedMaxFligthId){
					aircraftCancel.addFlight(flights.get(cancelIndex));
				}
				removeFlightIndeces.add(cancelIndex);
			}
			aircraft.removeFlightChain(removeFlightIndeces);
			if (!newFlight.getSourceAirPort().getId().equals(newFlight.getDesintationAirport().getId())){
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
					tempFlightTime.setDepartureTime(flightChain.get(i + 1).getDepartureTime());
					if (nextFlight.getDesintationAirport().requestAirport(tempFlightTime, minGroundTime) == null){
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
					return destIndexAndNewFight;
				} catch (AirportNotAcceptArrivalTime anaat){
					FlightTime avaliableTime = anaat.getAvailableTime();
					if (avaliableTime.getDepartureTime().compareTo(nextFlight.getDepartureTime()) == 0){
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
		if (getHourDifference(adjustTime, planTime) > (isInternational ? Aircraft.INTERNATIONAL_MAXIMUM_DELAY_TIME : Aircraft.DOMESTIC_MAXIMUM_DELAY_TIME)){
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
	public double getAircraftCost(Aircraft ac, Aircraft acCancel){
		XiaMengAirlineSolution solution = new XiaMengAirlineSolution();
		solution.replaceOrAddNewAircraft(ac);
		solution.replaceOrAddNewAircraft(acCancel);
		solution.refreshCost(false); 
		return solution.getCost().doubleValue();
	}
	

	public XiaMengAirlineSolution getMySolution() {
		return mySolution;
	}

}
