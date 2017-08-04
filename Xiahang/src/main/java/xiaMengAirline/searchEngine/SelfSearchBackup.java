package xiaMengAirline.searchEngine;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import xiaMengAirline.Exception.AircraftNotAdjustableBackup;
import xiaMengAirline.Exception.AirportNotAcceptArrivalTimeBackup;
import xiaMengAirline.Exception.AirportNotAcceptDepartureTimeBackup;
import xiaMengAirline.Exception.AirportNotAvailableBackup;
import xiaMengAirline.Exception.FlightDurationNotFoundBackup;
import xiaMengAirline.beans.AirPortBackup;
import xiaMengAirline.beans.AirPortCloseBackup;
import xiaMengAirline.beans.AircraftBackup;
import xiaMengAirline.beans.FlightBackup;
import xiaMengAirline.beans.FlightTimeBackup;
import xiaMengAirline.beans.RegularAirPortCloseBackup;
import xiaMengAirline.beans.XiaMengAirlineSolutionBackup;
import xiaMengAirline.util.InitDataBackup;

public class SelfSearchBackup {
	private static final Logger logger = Logger.getLogger(SelfSearchBackup.class);
	private XiaMengAirlineSolutionBackup mySolution = null;
	
	public XiaMengAirlineSolutionBackup constructInitialSolution()
			throws CloneNotSupportedException, ParseException, FlightDurationNotFoundBackup, AirportNotAvailableBackup, AircraftNotAdjustableBackup {
		//when construct initial solution, clone a new copy

		List<AircraftBackup> airList = new ArrayList<AircraftBackup> (mySolution.getSchedule().values());
		for (AircraftBackup aircraft : airList){
			adjustAircraft(aircraft, 0, mySolution.getAircraft(aircraft.getId(), aircraft.getType(), true, true));
		}
		XiaMengAirlineSolutionBackup aNewSol = mySolution.reConstruct();
		aNewSol.refreshCost(false);
		mySolution.setCost(aNewSol.getCost());
		aNewSol.clear();
		
		return mySolution;
	}
	
	public List<AircraftBackup> adjustAircraft (AircraftBackup originalAir, int startIndex, AircraftBackup originalCancelAir) throws CloneNotSupportedException, ParseException, FlightDurationNotFoundBackup, AirportNotAvailableBackup, AircraftNotAdjustableBackup {
		AircraftBackup thisAc = originalAir.clone();
		//original cancel air
		AircraftBackup thisAcCancel = originalCancelAir.clone();
		HashMap<Integer, AircraftBackup> forkList = new HashMap<Integer, AircraftBackup>();
		
		// loop until all flight sorted
		AircraftBackup aircraft = originalAir.clone();
		AircraftBackup aircraftCancel = thisAcCancel.clone();
		boolean isFinish = false;
		int infinitLoopCnt = 0;
		if (startIndex == 0) {
			FlightBackup firstFlight = aircraft.getFlightChain().get(0);
			FlightTimeBackup firstFlightTime = new FlightTimeBackup();
			
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy"); 
			Date startDate = df.parse("01/01/1970");
			firstFlightTime.setArrivalTime(startDate);
			firstFlightTime.setDepartureTime(firstFlight.getDepartureTime());
			
			FlightTimeBackup newfirstFlightTime = firstFlight.getSourceAirPort().requestAirport(firstFlightTime, firstFlight.getGroundingTime(0,1));
			if (newfirstFlightTime != null && newfirstFlightTime.getDepartureTime() != null) {
				if (!firstFlight.isInternationalFlight() && getMinuteDifference(firstFlight.getDepartureTime(), newfirstFlightTime.getDepartureTime()) > 360) {
					for (AirPortCloseBackup aClose : firstFlight.getSourceAirPort().getCloseSchedule()) {
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
			List<FlightBackup> flights = aircraft.getFlightChain();
			try{
				boolean adjusted = aircraft.adjustFlightTime(startIndex);
				isFinish = true;
				if (startIndex == 0 && !adjusted){
					originalAir.setAlternativeAircraft(null);
					originalCancelAir.setAlternativeAircraft(null);
				}
			} catch (AirportNotAcceptArrivalTimeBackup anaat){
				FlightBackup thisFlight = anaat.getaFlight();
				FlightTimeBackup avaliableTime = anaat.getAvailableTime();
				int flightIndex = aircraft.getFlightIndexByFlightId(thisFlight.getFlightId());
				
				if (avaliableTime.isIsTyphoon() && isJointFlight(thisFlight) && getJointFlight(thisFlight) != null
						&& !thisFlight.isInternationalFlight() && !getJointFlight(thisFlight).isInternationalFlight()){
					AircraftBackup forkAir = aircraft.clone();
					FlightBackup firstFlight = forkAir.getFlightChain().get(flightIndex);
					FlightBackup secondFlight = forkAir.getFlightChain().get(flightIndex + 1); 
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
							flights.get(flightIndex + 1).setDepartureTime(addMinutes(avaliableTime.getDepartureTime(), flights.get(flightIndex + 1).getGroundingTime(flightIndex, flightIndex + 1)));
						}
					} else {
						try {
							if (flightIndex != flights.size() - 1){
								aircraft = cancelFlight(aircraft, aircraftCancel, flightIndex);
								if (aircraft == null){
									print("Invalid aircraft: AicraftId " + thisAc.getId());
									throw new AircraftNotAdjustableBackup(aircraft);
								}
							} else {
								throw new AircraftNotAdjustableBackup(aircraft);
							}
						} catch (Exception e){
							//e.printStackTrace();
							print("Invalid aircraft: AicraftId " + thisAc.getId());
							throw new AircraftNotAdjustableBackup(aircraft);
						}
					}
					startIndex = flightIndex;
				}else{
					try {
						if (flightIndex != flights.size() - 1){
							aircraft = cancelFlight(aircraft, aircraftCancel, flightIndex);
							if (aircraft == null){
								print("Invalid aircraft: AicraftId " + thisAc.getId());
								throw new AircraftNotAdjustableBackup(aircraft);
							}
						} else {
							throw new AircraftNotAdjustableBackup(aircraft);
						}
					} catch (Exception e){
						//e.printStackTrace();
						print("Invalid aircraft: AicraftId " + thisAc.getId());
						throw new AircraftNotAdjustableBackup(aircraft);
					}
					startIndex = flightIndex;
				}
			} catch (AirportNotAcceptDepartureTimeBackup anadt){
				FlightBackup thisFlight = anadt.getaFlight();
				int flightIndex = aircraft.getFlightIndexByFlightId(thisFlight.getFlightId());
				if (isJointFlight(thisFlight) && getJointFlight(thisFlight) == null
						&& !thisFlight.isInternationalFlight() && !flights.get(flightIndex - 1).isInternationalFlight()){
					AircraftBackup forkAir = aircraft.clone();
					FlightBackup firstFlight = forkAir.getFlightChain().get(flightIndex - 1);
					FlightBackup secondFlight = forkAir.getFlightChain().get(flightIndex); 
					firstFlight.setDesintationAirport(secondFlight.getDesintationAirport());
					firstFlight.setArrivalTime(addMinutes(firstFlight.getDepartureTime(), getJointFlightDuration(firstFlight, secondFlight, forkAir)));
					forkAir.moveToDropOut(secondFlight);
					forkList.put(flightIndex, forkAir);
				}
				
				try {
					if (flightIndex != flights.size() - 1){
						aircraft = cancelFlight(aircraft, aircraftCancel, flightIndex);
						if (aircraft == null){
							print("Invalid aircraft: AicraftId " + thisAc.getId());
							throw new AircraftNotAdjustableBackup(aircraft);
						}
					} else {
						throw new AircraftNotAdjustableBackup(aircraft);
					}
				} catch (Exception e){
					//e.printStackTrace();
					print("Invalid aircraft: AicraftId " + thisAc.getId());
					throw new AircraftNotAdjustableBackup(aircraft);
				}
				startIndex = flightIndex;
			} catch (AirportNotAvailableBackup ana){
				FlightBackup thisFlight = ana.getaFlight();
				int flightIndex = aircraft.getFlightIndexByFlightId(thisFlight.getFlightId());
				try {
					if (flightIndex != flights.size() - 1){
						aircraft = cancelFlight(aircraft, aircraftCancel, flightIndex);
						if (aircraft == null){
							print("Invalid aircraft: AicraftId " + thisAc.getId());
							throw new AircraftNotAdjustableBackup(aircraft);
						}
					} else {
						throw new AircraftNotAdjustableBackup(aircraft);
					}
				} catch (Exception e){
					//e.printStackTrace();
					print("Invalid aircraft: AicraftId " + thisAc.getId());
					throw new AircraftNotAdjustableBackup(aircraft);
				}
				startIndex = flightIndex;
			} catch (Exception e){
				// invalid
				//e.printStackTrace();
				throw new AircraftNotAdjustableBackup(aircraft);
			}

			if (infinitLoopCnt > 1){
				// last flight cannot be adjusted, invalid flight chain
				throw new AircraftNotAdjustableBackup(aircraft);
			}
			
			if (startIndex == flights.size() - 1){
				infinitLoopCnt++;
			}
		}
		double thisCost = getAircraftCost(aircraft, aircraftCancel);
		
		Iterator<Entry<Integer, AircraftBackup>> it = forkList.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, AircraftBackup> pair = (Map.Entry<Integer, AircraftBackup>) it.next();
	        int nextStartIndex = pair.getKey();
	        AircraftBackup nextForkAc = pair.getValue();
	        AircraftBackup newReturnAc = null;
	        AircraftBackup newReturnAcCancel = null;
	        List<AircraftBackup> newReturnPair = adjustAircraft(nextForkAc, nextStartIndex, aircraftCancel);
	        for (AircraftBackup ac : newReturnPair) {
	        	if (ac.isCancel()) {
	        		newReturnAcCancel = ac;
	        	}else {
	        		newReturnAc = ac;
	        	}
	        }
	        double itCost = getAircraftCost(newReturnAc, newReturnAcCancel);
	        if (thisCost > itCost){
	        	aircraft = newReturnAc;
	        	aircraftCancel = newReturnAcCancel;
	        	//double ensure, no duplicated
	        	for (FlightBackup aFlight:aircraft.getFlightChain()) {
	        		if (aircraftCancel.getFlightByFlightId(aFlight.getFlightId()) != null) {
	        			aircraftCancel.getFlightChain().remove(aircraftCancel.getFlightByFlightId(aFlight.getFlightId()));
	        		}
	        	}
	        	for (FlightBackup aFlight:aircraft.getDropOutList()) {
	        		if (aircraftCancel.getFlightByFlightId(aFlight.getFlightId()) != null) {
	        			aircraftCancel.getFlightChain().remove(aircraftCancel.getFlightByFlightId(aFlight.getFlightId()));
	        		}
	        	}
	        	thisCost = itCost;
	        }
	    }

		
    	aircraft.setAlternativeAircraft(null);
    	aircraftCancel.setAlternativeAircraft(null);
		originalAir.setAlternativeAircraft(aircraft);
		originalCancelAir.setAlternativeAircraft(aircraftCancel);
		
	    List<AircraftBackup> aircraftReturn = new ArrayList<AircraftBackup>();
	    aircraftReturn.add(aircraft);
	    aircraftReturn.add(aircraftCancel);
	    return aircraftReturn;
	}
	
	public SelfSearchBackup(XiaMengAirlineSolutionBackup mySolution) {
		super();
		this.mySolution = mySolution;
	}

	public void print (String str){
		logger.info(str);
	}
	
	// cancel a flight
	public AircraftBackup cancelFlight(AircraftBackup aircraft, AircraftBackup aircraftCancel, int flightIndex) throws FlightDurationNotFoundBackup, CloneNotSupportedException, ParseException{
		List<FlightBackup> flights = aircraft.getFlightChain();
		FlightBackup thisFlight = flights.get(flightIndex);
		FlightBackup newFlight = new FlightBackup();
		FlightTimeBackup tempFt = new FlightTimeBackup();
		tempFt.setArrivalTime(flights.get(flightIndex - 1).getArrivalTime());
		Date tempDep = addMinutes(flights.get(flightIndex - 1).getArrivalTime(), newFlight.getGroundingTime(flightIndex - 1, flightIndex));
		tempFt.setDepartureTime(tempDep);
		tempFt = thisFlight.getSourceAirPort().requestAirport(tempFt, newFlight.getGroundingTime(flightIndex - 1, flightIndex));
		if (tempFt == null){
			newFlight.setDepartureTime(tempDep);
		}else{
			if (tempFt.getDepartureTime() == null){
				newFlight.setDepartureTime(tempDep);
			}else{
				newFlight.setDepartureTime(tempFt.getDepartureTime());
			}
		}
		
		newFlight.setSourceAirPort(thisFlight.getSourceAirPort());
		
		
		HashMap<Integer, FlightBackup> indexFlightPair = createNewFlight(newFlight, flightIndex, aircraft);
		if (indexFlightPair != null){
			Map.Entry<Integer,FlightBackup> entry=indexFlightPair.entrySet().iterator().next();
			int cancelFlightEndIndex = entry.getKey();
			newFlight = entry.getValue();
			List<Integer> removeFlightIndeces = new ArrayList<Integer>();

			for (int cancelIndex = flightIndex; cancelIndex < cancelFlightEndIndex + 1; cancelIndex++){
				if (flights.get(cancelIndex).getFlightId() <= InitDataBackup.plannedMaxFligthId){
					aircraftCancel.addFlight(flights.get(cancelIndex));
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
	public int getJointFlightDuration(FlightBackup firstFlight, FlightBackup secondFlight, AircraftBackup aircraft){
		String searchKey = aircraft.getId() + "_" + firstFlight.getSourceAirPort() + "_" + secondFlight.getDesintationAirport();
		if (InitDataBackup.fightDurationMap.containsKey(searchKey)){
			return InitDataBackup.fightDurationMap.get(searchKey);
		}else{
			Double flightTime = getMinuteDifference(firstFlight.getArrivalTime(), firstFlight.getDepartureTime())
					+ getMinuteDifference(secondFlight.getArrivalTime(), secondFlight.getArrivalTime());
			return flightTime.intValue();
		}
	}
	
	// get joint flight
	public FlightBackup getJointFlight(FlightBackup flight){
		return InitDataBackup.jointFlightMap.get(flight.getFlightId());
	}
	
	public boolean isJointFlight(FlightBackup flight){
		return InitDataBackup.jointFlightMap.keySet().contains(flight.getFlightId()) ? true : false;
	}
	
	// create new flight
	public HashMap<Integer, FlightBackup> createNewFlight(FlightBackup prototypeFlight, int flightPosition, AircraftBackup aircraft) throws FlightDurationNotFoundBackup, CloneNotSupportedException, ParseException{
		HashMap<Integer, FlightBackup> newIndexAndFlight = createEligibalFlight(aircraft.getFlightChain(), flightPosition, aircraft, prototypeFlight);
		if (newIndexAndFlight != null){
			Map.Entry<Integer,FlightBackup> entry=newIndexAndFlight.entrySet().iterator().next();
			int destIndex = entry.getKey();
			FlightBackup newFlight = entry.getValue();
			newFlight.setFlightId(getNextFlightId());
			newFlight.setAssignedAir(aircraft);
			
			HashMap<Integer, FlightBackup> indexAndFlight = new HashMap<Integer, FlightBackup>();
			indexAndFlight.put(destIndex, newFlight);
			return indexAndFlight;
		}
		return null;
	}
	
	// get next normal airport
	public HashMap<Integer, FlightBackup> createEligibalFlight(List<FlightBackup> flightChain, int currentFlightIndex, AircraftBackup ac, FlightBackup newFlight) throws CloneNotSupportedException, ParseException{
		FlightBackup thisFlight = newFlight;
		for (int i = currentFlightIndex + 1; i < flightChain.size(); i++){
			FlightBackup nextFlight = flightChain.get(i);
			AirPortBackup destAirport = nextFlight.getSourceAirPort();
			// if new flight is a chain cancel
			if (thisFlight.getSourceAirPort().getId().equals(destAirport.getId())){
				thisFlight.setDesintationAirport(destAirport);
				FlightBackup lastFlight = flightChain.get(currentFlightIndex - 1);
				FlightTimeBackup tempFlightTime = new FlightTimeBackup();
				tempFlightTime.setArrivalTime(lastFlight.getArrivalTime());
				tempFlightTime.setDepartureTime(nextFlight.getDepartureTime());
				if (nextFlight.getSourceAirPort().requestAirport(tempFlightTime, nextFlight.getGroundingTime(i-1,i)) == null){
					tempFlightTime.setArrivalTime(nextFlight.getArrivalTime());
					if (i < flightChain.size() - 2){
						tempFlightTime.setDepartureTime(flightChain.get(i + 1).getDepartureTime());
						if (nextFlight.getDesintationAirport().requestAirport(tempFlightTime, nextFlight.getGroundingTime(i, i+1)) == null){
							if (!isValidParking(thisFlight.getArrivalTime(), nextFlight.getDepartureTime(), thisFlight.getDesintationAirport())){
								continue;
							}
							HashMap<Integer, FlightBackup> destIndexAndNewFight = new HashMap<Integer, FlightBackup>();
							destIndexAndNewFight.put(i - 1, thisFlight);
							return destIndexAndNewFight;
						}else{
							continue;
						}
					} else {
						if (!isValidParking(thisFlight.getArrivalTime(), nextFlight.getDepartureTime(), thisFlight.getDesintationAirport())){
							continue;
						}
						HashMap<Integer, FlightBackup> destIndexAndNewFight = new HashMap<Integer, FlightBackup>();
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
				
				List<FlightBackup> newFlightChain = new ArrayList<FlightBackup>();
				newFlightChain.add(thisFlight);
				newFlightChain.add(nextFlight);
				AircraftBackup newAircraft = ac.clone();
				newAircraft.setFlightChain(newFlightChain);
				try {
					newAircraft.adjustFlightTime(0);
					HashMap<Integer, FlightBackup> destIndexAndNewFight = new HashMap<Integer, FlightBackup>();
					destIndexAndNewFight.put(i - 1, thisFlight);
					if (!isValidParking(thisFlight.getArrivalTime(), nextFlight.getDepartureTime(), thisFlight.getDesintationAirport())){
						continue;
					}
					return destIndexAndNewFight;
				} catch (AirportNotAcceptArrivalTimeBackup anaat){
					FlightTimeBackup avaliableTime = anaat.getAvailableTime();
					if (getMinuteDifference(avaliableTime.getArrivalTime(), getPlannedArrival(thisFlight)) < 24 * 60){
						if (!isValidParking(avaliableTime.getArrivalTime(), avaliableTime.getDepartureTime(), thisFlight.getDesintationAirport())){
							continue;
						}
						thisFlight.setArrivalTime(avaliableTime.getArrivalTime());
						thisFlight.setDepartureTime(addMinutes(thisFlight.getArrivalTime(), -flightTime));
						HashMap<Integer, FlightBackup> destIndexAndNewFight = new HashMap<Integer, FlightBackup>();
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
		if (InitDataBackup.domesticAirportList.contains(airport1) && InitDataBackup.domesticAirportList.contains(airport2)){
			return false;
		}
		return true;
	}
	
	public boolean isEligibalAircraft(AircraftBackup aircraft, AirPortBackup sourceAir, AirPortBackup destAir){
		String searchKey = aircraft.getId() + "_" + sourceAir.getId() + "_" + destAir.getId();
		return InitDataBackup.airLimitationList.contains(searchKey) ? false : true;
	}
	
	// get flight time between two airports
	public long getFlightTime(String airport1Id, String airport2Id, AircraftBackup aircraft){
		String searchKey = aircraft.getType();
		searchKey += "_";
		searchKey += airport1Id;
		searchKey += "_";
		searchKey += airport2Id;
		
		if (InitDataBackup.fightDurationMap.containsKey(searchKey)){
			long flightTime = InitDataBackup.fightDurationMap.get(searchKey);
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
		if (getMinuteDifference(adjustTime, planTime) > (isInternational ? AircraftBackup.INTERNATIONAL_MAXIMUM_DELAY_TIME*60: AircraftBackup.DOMESTIC_MAXIMUM_DELAY_TIME*60)){
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
	public Date getPlannedArrival(FlightBackup flight){
		return flight.getPlannedFlight().getArrivalTime();
	}
	
	// get original departure time
	public Date getPlannedDeparture(FlightBackup flight){
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
		int maxFlightId = InitDataBackup.maxFligthId;
		InitDataBackup.maxFligthId = maxFlightId + 1;
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
	public double getAircraftCost(AircraftBackup ac, AircraftBackup acCancel){
		XiaMengAirlineSolutionBackup solution = new XiaMengAirlineSolutionBackup();
		solution.replaceOrAddNewAircraft(ac);
		solution.replaceOrAddNewAircraft(acCancel);
		solution.refreshCost(false); 
		return solution.getCost().doubleValue();
	}
	

	public XiaMengAirlineSolutionBackup getMySolution() {
		return mySolution;
	}

	public Date getValidDeparture(Date departureTime, AirPortBackup airport) throws ParseException {
		for (AirPortCloseBackup aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (departureTime.compareTo(aClose.getStartTime()) > 0
						&& departureTime.compareTo(aClose.getEndTime()) < 0) {
					return aClose.getEndTime();
				}
			}
		}
		for (RegularAirPortCloseBackup aClose : airport.getRegularCloseSchedule()) {
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
	
	public boolean isValidParking(Date arrivalTime, Date departureTime, AirPortBackup airport){
		if (arrivalTime != null && arrivalTime != null) {
			for (AirPortCloseBackup aClose : airport.getCloseSchedule()) {
				if (arrivalTime.compareTo(aClose.getStartTime()) <= 0
						&& departureTime.compareTo(aClose.getEndTime()) >= 0) {
					return false;
				}
			}
		}
		return true;
	}
}
