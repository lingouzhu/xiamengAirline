package xiaMengAirline.searchEngine;

import java.util.HashMap;
import java.util.List;

import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.ConnectedDestinationPort;
import xiaMengAirline.beans.RestrictedCandidateList;
import xiaMengAirline.beans.XiaMengAirlineSolution;

public class LocalSearch {

	public XiaMengAirlineSolution constructNewSolution(XiaMengAirlineSolution bestSolution) {
		RestrictedCandidateList neighboursResult = new RestrictedCandidateList();
		List<Aircraft> aircrafts = bestSolution.getSchedule();
		int r = aircrafts.size();
		for (int i = 0; i < r - 1; i++) {
			Aircraft aircraft1 = aircrafts.get(i);
			int m = aircraft1.getFlightChain().size();
			for (int j = i + 1; j < r; j++) {
				Aircraft aircraft2 = aircrafts.get(j);
				int n = aircraft2.getFlightChain().size();
				HashMap<Integer, List<Integer>> overlappedAirports = AirPort
						.getOverlappedAirports(aircraft1.getAirports(), aircraft2.getAirports());
				if (!overlappedAirports.isEmpty() && (!aircraft1.isCancel() || !aircraft2.isCancel())) {
					HashMap<Integer, List<Integer>> circuitAirports1 = AirPort
							.getCircuitAirports(aircraft1.getAirports());
					HashMap<Integer, List<Integer>> circuitAirports2 = AirPort
							.getCircuitAirports(aircraft2.getAirports());

					for (int u = 0; u <= m - 1; u++) {
						for (int x = 0; x <= n - 1; x++) {

							// if aircraft1 flights is circuit, insert circuit
							// in front of flight x of aircraft2
							if (!aircraft2.isCancel() && x != 0) {
								AirPort sourcePort1 = aircraft1.getAirport(u, true);
								AirPort sourcePort2 = aircraft2.getAirport(x, true);
								if (circuitAirports1.containsKey(u)
										&& sourcePort1.getId().equals(sourcePort2.getId())) {
									Aircraft newAircraft1 = aircraft1.clone();
									Aircraft newAircraft2 = aircraft2.clone();
									List<Integer> circuitChain = circuitAirports1.get(u);
									newAircraft2.insertFlightChain(aircraft1, circuitChain, x);
									newAircraft1.removeFlightChain(circuitChain);
									if (newAircraft1.validate() && newAircraft2.validate()) {
										newAircraft1.adjustment();
										newAircraft2.adjustment();

										newAircraft1.refreshCost();
										newAircraft2.refreshCost();

										long newCost = newAircraft1.getCost() + newAircraft2.getCost();
										long oldCost = aircraft1.getCost() + aircraft2.getCost();
										// only update solution when new change
										// goes better
										if (newCost < oldCost) {
											XiaMengAirlineSolution aNewSolution = bestSolution.clone();
											aNewSolution.replaceOrAddNewAircraft(newAircraft1);
											aNewSolution.replaceOrAddNewAircraft(newAircraft2);
											aNewSolution.refreshCost();
											neighboursResult.addSolution(aNewSolution);
										}

									}

								}
							}
							// if aircraft2 flights is circuit, insert circuit
							// in front of flight u
							if (!aircraft1.isCancel() && u != 0) {
								AirPort sourcePort1 = aircraft1.getAirport(u, true);
								AirPort sourcePort2 = aircraft2.getAirport(x, true);
								if (circuitAirports2.containsKey(x)
										&& sourcePort1.getId().equals(sourcePort2.getId())) {
									Aircraft newAircraft1 = aircraft1.clone();
									Aircraft newAircraft2 = aircraft2.clone();
									List<Integer> circuitChain = circuitAirports2.get(u);
									newAircraft1.insertFlightChain(aircraft2, circuitChain, u);
									newAircraft2.removeFlightChain(circuitChain);

									if (newAircraft1.validate() && newAircraft2.validate()) {
										newAircraft1.adjustment();
										newAircraft2.adjustment();

										newAircraft1.refreshCost();
										newAircraft2.refreshCost();

										long newCost = newAircraft1.getCost() + newAircraft2.getCost();
										long oldCost = aircraft1.getCost() + aircraft2.getCost();
										// only update solution when new change
										// goes better
										if (newCost < oldCost) {
											XiaMengAirlineSolution aNewSolution = bestSolution.clone();
											aNewSolution.replaceOrAddNewAircraft(newAircraft1);
											aNewSolution.replaceOrAddNewAircraft(newAircraft2);
											aNewSolution.refreshCost();
											neighboursResult.addSolution(aNewSolution);
										}

									}

								}
							}
							// if aircraft1/aircraft2 flights same source and
							// same destination, do exchange overlapped part
							List<ConnectedDestinationPort> matchedList = AirPort.getMatchedAirports(overlappedAirports,
									u, x);
							for (ConnectedDestinationPort aConnectedPort : matchedList) {
								// if dest port is next to source port, nothing
								// can be exchanged
								Aircraft newAircraft1 = aircraft1.clone();
								Aircraft newAircraft2 = aircraft2.clone();
								if (aConnectedPort.isFirstAircraftDestNextSource()) {
									newAircraft1.insertFlightChain(aircraft2,
											aConnectedPort.getSecondAircraftSourceFlightIndex() + 1,
											aConnectedPort.getSecondAircraftDestinationFlightIndex() - 1,
											aConnectedPort.getFirstAircraftSourceFlightIndex(), false);
									newAircraft2.removeFlightChain(
											aConnectedPort.getSecondAircraftSourceFlightIndex() + 1,
											aConnectedPort.getSecondAircraftDestinationFlightIndex() - 1);
								} else if (aConnectedPort.isSecondAircraftDestNextSource()) {
									newAircraft2.insertFlightChain(aircraft1,
											aConnectedPort.getFirstAircraftSourceFlightIndex() + 1,
											aConnectedPort.getFirstAircraftDestinationFlightIndex() - 1,
											aConnectedPort.getSecondAircraftSourceFlightIndex(), false);
									newAircraft1.removeFlightChain(
											aConnectedPort.getFirstAircraftSourceFlightIndex() + 1,
											aConnectedPort.getFirstAircraftDestinationFlightIndex() - 1);
								} else {
									newAircraft1.insertFlightChain(aircraft2,
											aConnectedPort.getSecondAircraftSourceFlightIndex() + 1,
											aConnectedPort.getSecondAircraftDestinationFlightIndex() - 1,
											aConnectedPort.getFirstAircraftSourceFlightIndex(), false);
									newAircraft2.insertFlightChain(aircraft1,
											aConnectedPort.getFirstAircraftSourceFlightIndex() + 1,
											aConnectedPort.getFirstAircraftDestinationFlightIndex() - 1,
											aConnectedPort.getSecondAircraftSourceFlightIndex(), false);
									newAircraft1.removeFlightChain(
											aConnectedPort.getFirstAircraftSourceFlightIndex() + 1,
											aConnectedPort.getFirstAircraftDestinationFlightIndex() - 1);
									newAircraft2.removeFlightChain(
											aConnectedPort.getSecondAircraftSourceFlightIndex() + 1,
											aConnectedPort.getSecondAircraftDestinationFlightIndex() - 1);
								}
								if (newAircraft1.validate() && newAircraft2.validate()) {
									newAircraft1.adjustment();
									newAircraft2.adjustment();

									newAircraft1.refreshCost();
									newAircraft2.refreshCost();

									long newCost = newAircraft1.getCost() + newAircraft2.getCost();
									long oldCost = aircraft1.getCost() + aircraft2.getCost();
									// only update solution when new change goes
									// better
									if (newCost < oldCost) {
										XiaMengAirlineSolution aNewSolution = bestSolution.clone();
										aNewSolution.replaceOrAddNewAircraft(newAircraft1);
										aNewSolution.replaceOrAddNewAircraft(newAircraft2);
										aNewSolution.refreshCost();
										neighboursResult.addSolution(aNewSolution);
									}

								}

							}
							// if aircraft1/2 have the same source, do exchange
							// to end
							if (u == x && !aircraft1.isCancel() && !aircraft2.isCancel()) {
								Aircraft newAircraft1 = aircraft1.clone();
								Aircraft newAircraft2 = aircraft2.clone();
								newAircraft1.insertFlightChain(aircraft2, x + 1, aircraft1.getFlightChain().size() - 1,
										u, false);
								newAircraft2.insertFlightChain(aircraft1, u + 1, aircraft2.getFlightChain().size() - 1,
										x, false);

								if (newAircraft1.validate() && newAircraft2.validate()) {
									newAircraft1.adjustment();
									newAircraft2.adjustment();

									newAircraft1.refreshCost();
									newAircraft2.refreshCost();

									long newCost = newAircraft1.getCost() + newAircraft2.getCost();
									long oldCost = aircraft1.getCost() + aircraft2.getCost();
									// only update solution when new change goes
									// better
									if (newCost < oldCost) {
										XiaMengAirlineSolution aNewSolution = bestSolution.clone();
										aNewSolution.replaceOrAddNewAircraft(newAircraft1);
										aNewSolution.replaceOrAddNewAircraft(newAircraft2);
										aNewSolution.refreshCost();
										neighboursResult.addSolution(aNewSolution);
									}

								}
							}

							// if aircraft1 flights is circuit, place it into
							// cancellation route
							if (!aircraft1.isCancel() && u != 0) {
								if (circuitAirports1.containsKey(u)) {
									Aircraft newAircraft1 = aircraft1.clone();
									Aircraft newAircraft2 = aircraft2.clone();
									List<Integer> circuitChain = circuitAirports1.get(u);
									newAircraft2.insertFlightChain(aircraft1, circuitChain, x);
									newAircraft2.setCancel(true);
									newAircraft1.removeFlightChain(circuitChain);

									if (newAircraft1.validate() && newAircraft2.validate()) {
										newAircraft1.adjustment();
										newAircraft2.adjustment();

										newAircraft1.refreshCost();
										newAircraft2.refreshCost();

										long newCost = newAircraft1.getCost() + newAircraft2.getCost();
										long oldCost = aircraft1.getCost() + aircraft2.getCost();
										// only update solution when new change
										// goes better
										if (newCost < oldCost) {
											XiaMengAirlineSolution aNewSolution = bestSolution.clone();
											aNewSolution.replaceOrAddNewAircraft(newAircraft1);
											aNewSolution.replaceOrAddNewAircraft(newAircraft2);
											aNewSolution.refreshCost();
											neighboursResult.addSolution(aNewSolution);
										}

									}

								}
							}
							// if aircraft2 flights is circuit, place it into
							// cancellation route
							if (!aircraft2.isCancel() && u != 0) {
								if (circuitAirports2.containsKey(u)) {
									Aircraft newAircraft1 = aircraft1.clone();
									Aircraft newAircraft2 = aircraft2.clone();
									List<Integer> circuitChain = circuitAirports2.get(u);
									newAircraft1.insertFlightChain(aircraft2, circuitChain, x);
									newAircraft1.setCancel(true);
									newAircraft2.removeFlightChain(circuitChain);

									if (newAircraft1.validate() && newAircraft2.validate()) {
										newAircraft1.adjustment();
										newAircraft2.adjustment();

										newAircraft1.refreshCost();
										newAircraft2.refreshCost();

										long newCost = newAircraft1.getCost() + newAircraft2.getCost();
										long oldCost = aircraft1.getCost() + aircraft2.getCost();
										// only update solution when new change
										// goes better
										if (newCost < oldCost) {
											XiaMengAirlineSolution aNewSolution = bestSolution.clone();
											aNewSolution.replaceOrAddNewAircraft(newAircraft1);
											aNewSolution.replaceOrAddNewAircraft(newAircraft2);
											aNewSolution.refreshCost();
											neighboursResult.addSolution(aNewSolution);
										}

									}

								}
							}

						}

					}

				}

			}
		}
		XiaMengAirlineSolution updatedbestSolution = neighboursResult.selectASoluiton();
		neighboursResult.clear();
		if (updatedbestSolution != null) {
			return updatedbestSolution;
		} else
			return bestSolution;
			
		

	}
}
