package xiaMengAirline.searchEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.AircraftConstrains;
import xiaMengAirline.beans.AircraftCost;
import xiaMengAirline.beans.ConnectedDestinationPort;

public class PairSearch {
	List<Aircraft> pairSearch (Aircraft aircraft1, Aircraft airraft2) {
		ArrayList<Aircraft> searchResult = new ArrayList <Aircraft> ();
		
		
		
		return searchResult;
	}

	
	List<Aircraft> constructNeighbours (Aircraft aircraft1, Aircraft aircraft2) {
		ArrayList<Aircraft> neighboursResult = new ArrayList <Aircraft> ();
		HashMap<Integer, List<Integer>> overlappedAirports = AirPort.getOverlappedAirports(aircraft1.getAirports(), aircraft2.getAirports());
		HashMap<Integer, List<Integer>> circuitAirports1 = AirPort.getCircuitAirports(aircraft1.getAirports());
		HashMap<Integer, List<Integer>> circuitAirports2 = AirPort.getCircuitAirports(aircraft2.getAirports());
		if (!overlappedAirports.isEmpty() && (!aircraft1.isCancel() || !aircraft2.isCancel())) {
			for (int u=0;u<=aircraft1.getFlightChain().size()-1;u++) {
				for (int x=0;x<=aircraft2.getFlightChain().size()-1;x++) {
					Aircraft newAircraft1 = aircraft1.clone();
					Aircraft newAircraft2 = aircraft2.clone();
					//if aircraft1 flights is circuit, insert circuit in front of flight x 
					if (!aircraft2.isCancel() && x!=0) {
						AirPort sourcePort1 = aircraft1.getAirport(u, true);
						AirPort sourcePort2 = aircraft2.getAirport(x, true);
						if (circuitAirports1.containsKey(u) && sourcePort1.getId().equals(sourcePort2.getId())) {
							List<Integer> circuitChain = circuitAirports1.get(u);
							newAircraft2.insertFlightChain(aircraft1, circuitChain, x);
							newAircraft1.removeFlightChain(circuitChain);
							if (AircraftConstrains.validate(newAircraft1)
									&& AircraftConstrains.validate(newAircraft2)) {
								newAircraft1.setCost(AircraftCost.cacluate(newAircraft1));
								newAircraft2.setCost(AircraftCost.cacluate(newAircraft2));
								newAircraft1.setAdjusted(true);
								newAircraft2.setAdjusted(true);
							}

						}
					}
					//validate & cost
					if (AircraftConstrains.validate(newAircraft1)
							&& AircraftConstrains.validate(newAircraft2)) {
						newAircraft1.setCost(AircraftCost.cacluate(newAircraft1));
						newAircraft2.setCost(AircraftCost.cacluate(newAircraft2));
					}
					//if aircraft2 flights is circuit, insert circuit in front of flight u
					if (!aircraft1.isCancel() && u!=0) {
						AirPort sourcePort1 = aircraft1.getAirport(u, true);
						AirPort sourcePort2 = aircraft2.getAirport(x, true);
						if (circuitAirports2.containsKey(x) && sourcePort1.getId().equals(sourcePort2.getId())) {
							List<Integer> circuitChain = circuitAirports2.get(u);
							newAircraft1.insertFlightChain(aircraft2, circuitChain, u);
							newAircraft2.removeFlightChain(circuitChain);
							newAircraft1.setAdjusted(true);
							newAircraft2.setAdjusted(true);

						}
					}
					//validate & cost
					if (AircraftConstrains.validate(newAircraft1)
							&& AircraftConstrains.validate(newAircraft2)) {
						newAircraft1.setCost(AircraftCost.cacluate(newAircraft1));
						newAircraft2.setCost(AircraftCost.cacluate(newAircraft2));
					}
					//if aircraft1/aircraft2 flights same source and same destination, do exchange  
					List<ConnectedDestinationPort> matchedList = AirPort.getMatchedAirports(overlappedAirports, u, x);
					for (ConnectedDestinationPort aConnectedPort:matchedList) {
						//if dest port is next to source port, nothing can be exchanged
						if (aConnectedPort.isFirstAircraftDestNextSource()) {
							newAircraft1.insertFlightChain(aircraft2, aConnectedPort.getSecondAircraftSourceFlightIndex()+1, aConnectedPort.getSecondAircraftDestinationFlightIndex()-1, aConnectedPort.getFirstAircraftSourceFlightIndex(), false);
							newAircraft2.removeFlightChain(aConnectedPort.getSecondAircraftSourceFlightIndex()+1, aConnectedPort.getSecondAircraftDestinationFlightIndex()-1);
						} else if (aConnectedPort.isSecondAircraftDestNextSource()) {
							newAircraft2.insertFlightChain(aircraft1, aConnectedPort.getFirstAircraftSourceFlightIndex()+1, aConnectedPort.getFirstAircraftDestinationFlightIndex()-1, aConnectedPort.getSecondAircraftSourceFlightIndex(), false);
							newAircraft1.removeFlightChain(aConnectedPort.getFirstAircraftSourceFlightIndex()+1, aConnectedPort.getFirstAircraftDestinationFlightIndex()-1);
						} else {
							newAircraft1.insertFlightChain(aircraft2, aConnectedPort.getSecondAircraftSourceFlightIndex()+1, aConnectedPort.getSecondAircraftDestinationFlightIndex()-1, aConnectedPort.getFirstAircraftSourceFlightIndex(), false);
							newAircraft2.insertFlightChain(aircraft1, aConnectedPort.getFirstAircraftSourceFlightIndex()+1, aConnectedPort.getFirstAircraftDestinationFlightIndex()-1, aConnectedPort.getSecondAircraftSourceFlightIndex(), false);
							newAircraft1.removeFlightChain(aConnectedPort.getFirstAircraftSourceFlightIndex()+1, aConnectedPort.getFirstAircraftDestinationFlightIndex()-1);
							newAircraft2.removeFlightChain(aConnectedPort.getSecondAircraftSourceFlightIndex()+1, aConnectedPort.getSecondAircraftDestinationFlightIndex()-1);
						}
						newAircraft1.setAdjusted(true);
						newAircraft2.setAdjusted(true);
						//validate & cost
						if (AircraftConstrains.validate(newAircraft1)
								&& AircraftConstrains.validate(newAircraft2)) {
							newAircraft1.setCost(AircraftCost.cacluate(newAircraft1));
							newAircraft2.setCost(AircraftCost.cacluate(newAircraft2));
						}						
						
					}



				}

			}

			
		}
	}
}
