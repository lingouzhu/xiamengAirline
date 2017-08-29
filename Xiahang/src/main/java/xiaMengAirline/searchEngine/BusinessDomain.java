package xiaMengAirline.searchEngine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.AirPortClose;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.RegularAirPortClose;
import xiaMengAirline.utils.InitData;

public class BusinessDomain {
	private static final Logger logger = Logger.getLogger(BusinessDomain.class);
	public static final int MAX_INTERNATIONAL_DELAY = 36;
	public static final int MAX_DOMESTIC_DELAY = 24;
	public static final int MAX_DOMESTIC_EARLIER = 6;
	public static boolean isValidDelay(Flight aFlight, Date delay) {
		Calendar aCal = Calendar.getInstance();
		aCal.setTime(aFlight.getPlannedFlight().getDepartureTime());
		if (aFlight.isInternationalFlight()) {
			aCal.add(Calendar.HOUR, MAX_INTERNATIONAL_DELAY);
		} else
			aCal.add(Calendar.HOUR, MAX_DOMESTIC_DELAY);
		
		if (delay.before(aCal.getTime()))
			return true;
		else
			return false;
	}
	
	public static boolean isValidEarlier(Flight aFlight, Date earlier, boolean isTyphoon) {
		if (!isTyphoon || aFlight.isInternationalFlight())
			return false;
		
		Calendar aCal = Calendar.getInstance();
		aCal.setTime(aFlight.getPlannedFlight().getDepartureTime());
		aCal.add(Calendar.HOUR, -MAX_DOMESTIC_EARLIER);
		
		if (earlier.after(aCal.getTime()))
			return true;
		else
			return false;
	}
	
	public static boolean isTyphoon (AirPort aAirport, Date aTime) {
		for (AirPortClose aClose : aAirport.getCloseSchedule()) {
			if (aTime.after(aClose.getStartTime())
					&& aTime.before(aClose.getEndTime())) {
				return true;
			}
		}
		return false;
		
	}

	public static boolean checkAirportAvailablity(AirPort aAirport, Date aTime, boolean isTakeoff, boolean checkonly,
			boolean isRelease) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(aTime);
		int years = calendar.get(Calendar.YEAR);
		int months = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
	
		int checkMin = minutes / 5 * 5;
	
		String checkTime = String.valueOf(years);
		checkTime += "-";
		checkTime += String.format("%02d", months);
		checkTime += "-";
		checkTime += String.format("%02d", day);
		checkTime += " ";
		checkTime += String.format("%02d", hours);
		checkTime += ":";
		checkTime += String.format("%02d", checkMin);
	
		System.out.println("CheckAirportAvailablity Searching key ... " + checkTime);
	
		if (isTakeoff) {
			if (aAirport.getTakeoffCapability().containsKey(checkTime)) {
				int cap = aAirport.getTakeoffCapability().get(checkTime);
				if (isRelease) {
					cap++;
					aAirport.getTakeoffCapability().put(checkTime, cap);
					return true;
				} else {
					if (cap > 0) {
						if (checkonly)
							return true;
						else {
							cap--;
							aAirport.getTakeoffCapability().put(checkTime, cap);
							return true;
						}
					} else {
						return false;
					}
				}
	
			} else {
				return true;
			}
		} else {
			if (aAirport.getLandingCapability().containsKey(checkTime)) {
				int cap = aAirport.getLandingCapability().get(checkTime);
				if (isRelease) {
					cap++;
					aAirport.getLandingCapability().put(checkTime, cap);
					return true;
				} else {
					if (cap > 0) {
						if (checkonly)
							return true;
						else {
							cap--;
							aAirport.getLandingCapability().put(checkTime, cap);
							return true;
						}
					} else {
						return false;
					}
				}
	
			} else {
				return true;
			}
		}
	
	}

	//to do
	public static int getGroundingTime (int fromFlightId, int toFlightId) {
		//check  if actual grounding time is less than the standard
		//check if this is the first flight
		if (fromFlightId >= toFlightId)
			return Flight.GroundingTime;
		
		String currentFlightId = String.valueOf(fromFlightId);
		String nextFlightId = String.valueOf(toFlightId);
	
		
		//look up special flight time table
		String searchKey = currentFlightId;
		searchKey += "_";
		searchKey +=  nextFlightId;
		
		if (InitData.specialFlightMap.containsKey(searchKey))
			return (InitData.specialFlightMap.get(searchKey));
		else
			return Flight.GroundingTime;
		
	}

	
	public static boolean validateFlights (Aircraft oldAir1, Aircraft oldAir2, Aircraft newAir1, Aircraft newAir2) {
		int oldSize = oldAir1.getFlightChain().size();
		oldSize += oldAir2.getFlightChain().size();
		int newSize = newAir1.getFlightChain().size();
		newSize += newAir2.getFlightChain().size();
		
		if (oldSize != newSize)
		{
			logger.warn("Unmatched flight size after exchange airs: " + newAir1.getId() + ":" + newAir2.getId());
			return false;
		}
		
		if (newAir1.isCancel()) {
			for (Flight aFlight:newAir1.getFlightChain()) {
				if (!aFlight.isAdjustable()) {
					logger.warn("Flight shall not adjust but cancelled after exchange air: " + newAir1.getId() + " flightId:" + aFlight.getFlightId());
					return false;
				}
			}
		}
		
		if (newAir2.isCancel()) {
			for (Flight aFlight:newAir2.getFlightChain()) {
				if (!aFlight.isAdjustable()) {
					logger.warn("Flight shall not adjust but cancelled after exchange air: " + newAir2.getId() + " flightId:" + aFlight.getFlightId());
					return false;
				}
			}
		}
		
		return true;
			
				
	}
	
	
	// methods to be adjusted
	/**
	 * if a departure time is in the error time range
	 * @param depTime
	 * @param airport
	 * @return
	 */
	public static boolean isDepTimeAffected(Date depTime, AirPort airport) {
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
	 * if a arrival time is in the error time range
	 * @param arvTime
	 * @param airport
	 * @return
	 */
	public static boolean isArvTimeAffected(Date arvTime, AirPort airport) {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (arvTime.after(aClose.getStartTime())
					&& arvTime.before(aClose.getEndTime())) {
				return true;
			}
		}
		return false;
	}
	
	
	public static boolean isDepTimeAffectedByNormal(Date depTime, AirPort airport) throws ParseException{
		for (RegularAirPortClose aClose : airport.getRegularCloseSchedule()) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(depTime);
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();
			
			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			Date aCloseDate = formatter2.parse(aDateC);
			Date aOpenDate = formatter2.parse(aDateO);
			
			if (depTime.after(aCloseDate)
					&& depTime.before(aOpenDate)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * departure time is eligible to set earlier.
	 * @param depTime
	 * @param airport
	 * @return
	 */
	public static boolean isEarlyDeparturePossible(Date depTime, AirPort airport) {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (depTime.after(aClose.getStartTime())
						&& depTime.before(addMinutes(aClose.getStartTime(), 360))) {
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
	public static Date getPossibleArrivalTime(Flight flight, AirPort airport) throws ParseException{
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (flight.getArrivalTime().after(aClose.getStartTime())
					&& flight.getArrivalTime().before(aClose.getEndTime())) {
				if (isNewFlight(flight)){
					return addMinutes(aClose.getEndTime(), 125);
				} else {
					if (isValidDelay(getPlannedArrival(flight), aClose.getEndTime(), flight.isInternationalFlight())){
						return getPossibleLoadTime(airport, aClose.getEndTime(), getPlannedArrival(flight), flight.isInternationalFlight(), false, false);
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
				if (isNewFlight(flight)){
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
	public static Date getPossibleDelayDeparture(Flight flight, AirPort airport, boolean isFirstFlight, Flight lastFlight) throws ParseException {
		Date tempDepTime = null;
		if (lastFlight != null){
			Date shiftDeparture = addMinutes(lastFlight.getArrivalTime(), getGroundingTime(lastFlight, flight));
			tempDepTime = shiftDeparture.before(flight.getDepartureTime()) ? flight.getDepartureTime() : shiftDeparture;
		} else {
			tempDepTime = flight.getDepartureTime();
		}
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (tempDepTime.after(aClose.getStartTime())
						&& tempDepTime.before(aClose.getEndTime())) {
					if (isFirstFlight) {
						if (isNewFlight(flight)){
							return addMinutes(aClose.getEndTime(), 125);
						} else {
							if (isValidDelay(getPlannedDeparture(flight), aClose.getEndTime(), flight.isInternationalFlight())){
								return getPossibleLoadTime(airport, aClose.getEndTime(), getPlannedDeparture(flight), flight.isInternationalFlight(), true, false);
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
			String aDateC = formatter.format(tempDepTime);
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();
			
			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			Date aCloseDate = formatter2.parse(aDateC);
			Date aOpenDate = formatter2.parse(aDateO);
			
			if (tempDepTime.after(aCloseDate)
					&& tempDepTime.before(aOpenDate)) {
				if (isNewFlight(flight)){
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
		return (Date) tempDepTime.clone();
		// need to check parking somewhere
	}
	
	/** 
	 * check if this flight is able to departure earlier. 
	 * @return if possible return the time, otherwise return null
	 * @throws ParseException 
	 */
	public static Date getPossibleEarlierDepartureTime(Flight flight, AirPort airport, boolean hasEarlyLimit, Flight lastFlight) throws ParseException {
		if (lastFlight != null) {
			// not the first flight
			if (getPlannedDeparture(flight).before(lastFlight.getArrivalTime())){
				return null;
			}
			for (AirPortClose aClose : airport.getCloseSchedule()) {
				if (!aClose.isAllowForTakeoff()) {
					if (isNewFlight(flight)){
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
							if (flight.getDepartureTime().after(addMinutes(aClose.getStartTime(), 360))
									&& flight.getDepartureTime().before(aClose.getStartTime())) {
								return null;
							}
						}
					}
				}
			}
		} else {
			// is the first flight
			for (AirPortClose aClose : airport.getCloseSchedule()) {
				if (!aClose.isAllowForTakeoff()) {
					if (isNewFlight(flight)){
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
							if (flight.getDepartureTime().after(addMinutes(aClose.getStartTime(), 360))
									&& flight.getDepartureTime().before(aClose.getStartTime())) {
								return null;
							}
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
					if (isNewFlight(flight)){
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
					if (isNewFlight(flight)){
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
	
	
	public static Date getAirportDepartureCloseStart(AirPort airport){
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				return aClose.getStartTime();
			}
		}
		return null;
	}
	
	/**
	 * get flight time between two airports
	 * @param airport1Id
	 * @param airport2Id
	 * @param aircraft
	 * @return
	 */
	public static long getFlightTime(String airport1Id, String airport2Id, Aircraft aircraft){
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
	public static boolean isInternational(String airport1, String airport2){
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
	public static boolean isEligibalAircraft(Aircraft aircraft, AirPort sourceAir, AirPort destAir){
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
	public static boolean isValidDelay(Date planTime, Date adjustTime, boolean isInternational){
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
	public static int getFlightIndexByFlightId(int aFlightId, ArrayList<Flight> originalFlights) {
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
	public static boolean isValidParking(Date arrivalTime, Date departureTime, AirPort airport){
		if (airport.getId().equals("25")) {
			return true;
		}
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
	public static Date getPlannedArrival(Flight flight){
		return flight.getPlannedFlight().getArrivalTime();
	}
	
	/**
	 * get original departure time
	 * @param flight
	 * @return
	 */
	public static Date getPlannedDeparture(Flight flight){
		return flight.getPlannedFlight().getDepartureTime();
	}
	
	/**
	 * get grounding time between two flights
	 * @param flight1
	 * @param flight2
	 * @return
	 */
	public static int getGroundingTime(Flight flight1, Flight flight2) {
		return Flight.getGroundingTime(flight1.getFlightId(), flight2.getFlightId());
	}
	
	/**
	 * compress the departure time of next flight
	 * @param lastFlight
	 * @param thisFlight
	 * @return
	 */
	public static Date getCompressedDeparture(Flight lastFlight, Flight thisFlight) {
		Date thisPlannedDeparture = getPlannedDeparture(thisFlight);
		Date thisShiftedDeparture = addMinutes(lastFlight.getArrivalTime(), getGroundingTime(lastFlight, thisFlight));
		return thisPlannedDeparture.before(thisShiftedDeparture) ? thisPlannedDeparture : thisShiftedDeparture;
	}
	
	/**
	 * a flight is a new flight
	 * @param flight
	 * @return
	 */
	public static boolean isNewFlight(Flight flight) {
		return flight.getFlightId() > InitData.plannedMaxFligthId;
	}
	
	/**
	 * if a flight is joint flight
	 * @param flight
	 * @return joint flight position
	 */
	public static int getJointFlightPosition(Flight flight){
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
	public static Flight getJointFlight(Flight flight){
		return InitData.jointFlightMap.get(flight.getFlightId());
	}
	
	/**
	 * get how this flight is affected
	 * @param flight
	 * @return 1 departure, 2 arrival, 0 no
	 */
	public static int isAffected(Flight flight) {
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
	public static int getNextFlightId(){
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
	public static Date getDepartureTimeByArrivalTime(Flight flight, Date newArrivalTime, Aircraft originalAircraft) {
		if (flight.getPlannedAir() != null){
			if (getJointFlightPosition(flight) == 1){
				if (!flight.getDesintationAirport().getId().equals(flight.getPlannedFlight().getDesintationAirport().getId())){
					long flightTime = getFlightTime(flight.getSourceAirPort().getId(), flight.getDesintationAirport().getId(), originalAircraft);
					if (flightTime > 0){
						return addMinutes(newArrivalTime, -flightTime);
					} else {
						int flightTime1 = (int) getMinuteDifference(getPlannedArrival(flight), getPlannedDeparture(flight));
						int flightTime2 = (int) getMinuteDifference(getPlannedArrival(getJointFlight(flight)), getPlannedDeparture(getJointFlight(flight)));
						int timeTotal = flightTime1 + flightTime2;
						return addMinutes(newArrivalTime, -timeTotal); 
					}
				} else {
					int flightTime = (int) getMinuteDifference(getPlannedArrival(flight), getPlannedDeparture(flight));
					return addMinutes(newArrivalTime, -flightTime);
				}
			} else {
				int flightTime = (int) getMinuteDifference(getPlannedArrival(flight), getPlannedDeparture(flight));
				return addMinutes(newArrivalTime, -flightTime);
			}
		} else {
			long flightTime = getFlightTime(flight.getSourceAirPort().getId(), flight.getDesintationAirport().getId(), originalAircraft);
			if (flightTime > 0){
				return addMinutes(newArrivalTime, -flightTime);
			}
		}
		return null;
	}
	
	
	public static Date getArrivalTimeByDepartureTime(Flight flight, Date newDepartureTime, Aircraft originalAircraft){
		if (!isNewFlight(flight)){
			if (getJointFlightPosition(flight) == 1){
				if (!flight.getDesintationAirport().getId().equals(flight.getPlannedFlight().getDesintationAirport().getId())){
					long flightTime = getFlightTime(flight.getSourceAirPort().getId(), flight.getDesintationAirport().getId(), originalAircraft);
					if (flightTime > 0){
						return addMinutes(newDepartureTime, flightTime);
					} else {
						int flightTime1 = (int) getMinuteDifference(getPlannedArrival(flight), getPlannedDeparture(flight));
						int flightTime2 = (int) getMinuteDifference(getPlannedArrival(getJointFlight(flight)), getPlannedDeparture(getJointFlight(flight)));
						int timeTotal = flightTime1 + flightTime2;
						return addMinutes(newDepartureTime, timeTotal); 
					}
				} else {
					int flightTime = (int) getMinuteDifference(getPlannedArrival(flight), getPlannedDeparture(flight));
					return addMinutes(newDepartureTime, flightTime);
				}
			} else {
				int flightTime = (int) getMinuteDifference(getPlannedArrival(flight), getPlannedDeparture(flight));
				return addMinutes(newDepartureTime, flightTime);
			}
		} else {
			long flightTime = getFlightTime(flight.getSourceAirPort().getId(), flight.getDesintationAirport().getId(), originalAircraft);
			if (flightTime > 0){
				return addMinutes(newDepartureTime, flightTime);
			}
		}
		return null;
	}
	

	private static HashMap<String, int[]> timeload = new HashMap<String, int[]>();
	public static Date getPossibleLoadTime(AirPort airport, Date time, Date planTime, boolean isInternational, boolean isDep, boolean isEarly) {
		if (isDep) {
			if (isEarly) {
				String airportId = airport.getId();
				if (timeload.containsKey(airportId)) {
					int load = timeload.get(airportId)[0];
					Date rtTime = addMinutes(time, -5 * load);
					timeload.get(airportId)[0]++;
					return rtTime;
				} else {
					return time;
				}
			} else {
				String airportId = airport.getId();
				if (timeload.containsKey(airportId)) {
					int load = timeload.get(airportId)[1];
					if (isValidDelay(planTime, addMinutes(time, 125), isInternational)) {
						return addMinutes(time, 125);
					} else {
						Date rtTime = addMinutes(time, 5 * load);
						timeload.get(airportId)[1]++;
						return null;
					}
				} else {
					return time;
				}
			}
		} else {
			String airportId = airport.getId();
			if (timeload.containsKey(airportId)) {
				int load = timeload.get(airportId)[1];
				if (isValidDelay(planTime, addMinutes(time, 125), isInternational)) {
					return addMinutes(time, 125);
				} else {
					Date rtTime = addMinutes(time, 5 * load);
					timeload.get(airportId)[1]++;
					return null;
				}
			} else {
				return time;
			}
		}
	}
	
	/** 
	 * add minutes to date
	 * @param date
	 * @param minutes
	 * @return
	 */
	public static Date addMinutes(Date date, long minutes){
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
	public static double getMinuteDifference(Date time1, Date time2){
		return (time1.getTime() - time2.getTime()) / (1000 * 60);
	}
	
public static Date getNextOpenDate(AirPort airport, Date orgDate) {
		
		Date openDate = null;
		List<RegularAirPortClose> regularStartCloseSchedule = airport.getRegularCloseSchedule();
		for (RegularAirPortClose aClose : regularStartCloseSchedule) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(orgDate);
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();

			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			try {
				Date aCloseDate = formatter2.parse(aDateC);
				Date aOpenDate = formatter2.parse(aDateO);
				
				if (orgDate.after(aCloseDate)
						&& orgDate.before(aOpenDate)) {
					openDate = aOpenDate;
				} else {
					openDate = orgDate;
				}
				
			} catch (ParseException e) {
				System.out.println("normal close date error");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		return openDate;
	}
	
	public static boolean isNormalClose(AirPort airport, Date orgDate) {
		
		boolean closeFlg = false;
		
		List<RegularAirPortClose> regularStartCloseSchedule = airport.getRegularCloseSchedule();
		for (RegularAirPortClose aClose : regularStartCloseSchedule) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(orgDate);
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();

			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			try {
				Date aCloseDate = formatter2.parse(aDateC);
				Date aOpenDate = formatter2.parse(aDateO);
				
				if (orgDate.after(aCloseDate)
						&& orgDate.before(aOpenDate)) {
					closeFlg = true;
				}
				
			} catch (ParseException e) {
				System.out.println("normal close date error");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		return closeFlg;
	}
}
