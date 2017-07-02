package xiaMengAirline.searchEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.AircraftConstrains;
import xiaMengAirline.beans.AircraftCost;

public class PairSearch {
	List<Aircraft> pairSearch (Aircraft aircraft1, Aircraft airraft2) {
		ArrayList<Aircraft> searchResult = new ArrayList <Aircraft> ();
		
		
		
		return searchResult;
	}

	
	List<Aircraft> constructNeighbours (Aircraft aircraft1, Aircraft aircraft2) {
		ArrayList<Aircraft> neighboursResult = new ArrayList <Aircraft> ();
		HashMap<Integer, List<Integer>> overlappedAirports = AirPort.getOverlappedAirports(aircraft1.getAirports(), aircraft2.getAirports());
		HashMap<Integer, List<Integer>> circuitAirports1 = AirPort.getCircuitAirports(aircraft1.getAirports());
		if (!overlappedAirports.isEmpty() && (!aircraft1.isCancel() || !aircraft2.isCancel())) {
			for (int i=0;i<=aircraft1.getFlightChain().size()-1;i++) {
				//if aircraft1 flights is circuit, insert circuit in front of flight i 
				if (!aircraft2.isCancel() && i!=0) {
					if (circuitAirports1.containsKey(i)) {
						Aircraft newAircraft1 = aircraft1.clone();
						Aircraft newAircraft2 = aircraft2.clone();
						List<Integer> circuitChain = circuitAirports1.get(i);
						newAircraft2.insertFlightChain(aircraft1, circuitChain, i);;
						newAircraft1.removeFlightChain(circuitChain);
						if (AircraftConstrains.validate(newAircraft1) && AircraftConstrains.validate(newAircraft2)) {
							newAircraft1.setCost(AircraftCost.cacluate(newAircraft1));
							newAircraft2.setCost(AircraftCost.cacluate(newAircraft2));
						}
						
					}
				}				
			}

			for (Integer firstFlightIndex:overlappedAirports.keySet()) {
				List<Integer> secondFlightOverlapped = overlappedAirports.get(firstFlightIndex);
				//reconstruct new schedule for the two aircrafts
				
			}
		}
	}
}
