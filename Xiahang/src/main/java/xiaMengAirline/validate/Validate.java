package xiaMengAirline.validate;


import java.util.ArrayList;
import java.util.List;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.util.InitData;

public class Validate {
	
	public boolean checkSolution (XiaMengAirlineSolution aSolution) {
		
		List<Aircraft> schedule = new ArrayList<Aircraft> ( aSolution.getSchedule().values());
		for (Aircraft aAir:schedule) {
			if (!checkAircraft(aAir)) return false;
		}
		return true;
	}

	
	public boolean checkAircraft(Aircraft aircraft) {  
		
		List<Flight> flightChain = aircraft.getFlightChain();
		
		for (int i = 0; i <= flightChain.size(); i++) {
			Flight flight = flightChain.get(i);
			
			String startPort = flight.getSourceAirPort().getId();
			String endPort =  flight.getDesintationAirport().getId();
			String airID =  aircraft.getId();
			
			if (InitData.airLimitationList.contains(airID + "_" + startPort + "_" + endPort)) {
				return true;
			}
			if (i != 0) {
				Flight preFlight = flightChain.get(i - 1);
				
				if (!preFlight.getDesintationAirport().getId().equals(flight.getSourceAirPort().getId())) {
					return true;
				}
			}
		}
		
		return false;
		
    }
	
	
}
