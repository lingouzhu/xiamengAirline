package xiaMengAirline.searchEngine;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import xiaMengAirline.beans.Flight;
import xiaMengAirline.utils.InitData;
import xiaMengAirline.utils.Utils;

public class CostDomain {
	public static BigDecimal cancelCost (Flight aFlight) {
		return new BigDecimal("1200").multiply(aFlight.getImpCoe());
	}
	
	public static BigDecimal changeAirCost (Flight aFlight) {
		BigDecimal cost = new BigDecimal(0);
		Date changeTime = null;
		try {
			changeTime = Utils.stringFormatToTime2("06/06/2017 16:00:00");
		} catch (ParseException e) {
			System.out.println("change time error");
			e.printStackTrace();
		}
		
		// change air cost
		if (aFlight.getDepartureTime().after(changeTime)) {
			cost = cost.add(new BigDecimal("5").multiply(aFlight.getImpCoe()));
		} else {
			cost = cost.add(new BigDecimal("15").multiply(aFlight.getImpCoe()));
		}
		
		// change air type cost
		return cost.add(new BigDecimal(InitData.changeAirCostMap.get(aFlight.getPlannedAir().getType() + "_" + aFlight.getAssignedAir().getType())).multiply(new BigDecimal("500")).multiply(aFlight.getImpCoe()));
	}
	
	public static BigDecimal delayCost (Flight aFlight) {
		return new BigDecimal("100").multiply(
				Utils.hoursBetweenTime(aFlight.getDepartureTime(), aFlight.getPlannedFlight().getDepartureTime()).abs())
				.multiply(aFlight.getImpCoe());
	}
	
	public static BigDecimal earlierCost (Flight aFlight) {
		return new BigDecimal("150").multiply(
				Utils.hoursBetweenTime(aFlight.getDepartureTime(), aFlight.getPlannedFlight().getDepartureTime()).abs())
				.multiply(aFlight.getImpCoe());
	}
	
	public static BigDecimal connectedFlightCost (Flight aFlight) {
		return new BigDecimal("750").multiply(aFlight.getImpCoe());
	}
	
	public static BigDecimal emptyFlightCost () {
		return new BigDecimal("5000");
	}

}
