package xiaMengAirline.searchEngine;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.AirPortClose;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
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
		aCal.add(Calendar.HOUR, MAX_DOMESTIC_EARLIER);
		
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
	
	

}
