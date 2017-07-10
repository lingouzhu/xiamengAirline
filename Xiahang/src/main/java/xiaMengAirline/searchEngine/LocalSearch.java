package xiaMengAirline.searchEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.MatchedFlight;
import xiaMengAirline.beans.RestrictedCandidateList;
import xiaMengAirline.beans.XiaMengAirlineSolution;

public class LocalSearch {

	private static final Logger logger = Logger.getLogger(LocalSearch.class);
	
	private long calcuateDeltaCost(Aircraft newAir1, Aircraft newAir2, Aircraft oldAir1, Aircraft oldAir2) {
		XiaMengAirlineSolution aNewLocalSolution = new XiaMengAirlineSolution();
		aNewLocalSolution.replaceOrAddNewAircraft(newAir1);
		aNewLocalSolution.replaceOrAddNewAircraft(newAir2);
		
		XiaMengAirlineSolution aOldLocalSolution = new XiaMengAirlineSolution();
		aOldLocalSolution.replaceOrAddNewAircraft(oldAir1);
		aOldLocalSolution.replaceOrAddNewAircraft(oldAir2);
		
		long deltaCost = aNewLocalSolution.calcuateDeltaCost(aOldLocalSolution);
		
		return deltaCost;
		
	}
	
	private boolean adjust(Aircraft newAir1, Aircraft newAir2, Aircraft oldAir1, Aircraft oldAir2) {
		if (newAir1.validate() && newAir2.validate()) {
			newAir1.adjustment();
			newAir2.adjustment();
			return true;
		} else
			return false;
	}

	public XiaMengAirlineSolution constructNewSolution(XiaMengAirlineSolution bestSolution)
			throws CloneNotSupportedException {
		RestrictedCandidateList neighboursResult = new RestrictedCandidateList();
		List<Aircraft> aircrafts = new ArrayList<Aircraft> ( bestSolution.getSchedule().values());
		int r = aircrafts.size();
		for (int i = 0; i < r - 1; i++) {
			Aircraft aircraft1 = aircrafts.get(i);
			int m = aircraft1.getFlightChain().size();
			for (int j = i + 1; j < r; j++) {
				Aircraft aircraft2 = aircrafts.get(j);
				int n = aircraft2.getFlightChain().size();
				if (!aircraft1.isCancel() || !aircraft2.isCancel()) {
					HashMap<Flight, List<Flight>> circuitFlightsAir1 = aircraft1.getCircuitFlights();
					HashMap<Flight, List<Flight>> circuitFlightsAir2 = aircraft2.getCircuitFlights();
					HashMap<Flight, List<MatchedFlight>> matchedFlights = aircraft1.getMatchedFlights(aircraft2);

					for (int uu = 0; uu <= m - 1; uu++) {
						// if aircraft1 flights is circuit, place it into
						// cancellation route - method 1
						if (!aircraft1.isCancel()) {
							Flight flightAir1 = aircraft1.getFlight(uu);
							if (circuitFlightsAir1.containsKey(flightAir1)) {
								for (Flight destFlight : circuitFlightsAir1.get(flightAir1)) {
									Aircraft newAircraft1 = aircraft1.clone();
									Aircraft cancelledAir = newAircraft1.getCancelledAircraft();
									
									Flight sFlight = newAircraft1.getFlight(uu);
									Flight dFlight = newAircraft1.getFlight(aircraft1.getFlightChain().indexOf(destFlight));

									cancelledAir.insertFlightChain(aircraft1, flightAir1, destFlight,
											cancelledAir.getFlight(cancelledAir.getFlightChain().size() - 1), false);
									newAircraft1.removeFlightChain(sFlight, dFlight);

									logger.info("Method 1 After exchange ...");
									List<Flight> updateList1 = newAircraft1.getFlightChain();
									for (Flight aF : updateList1) {
										logger.info("Air 1 " + aF.getSchdNo());
									}
									List<Flight> updateList2 = cancelledAir.getFlightChain();
									for (Flight aF : updateList2) {
										logger.info("Air 1 cancelled " + aF.getSchdNo());
									}
									logger.info("Method 1 Complete exchange ...");
									
									
									// only update solution when new change
									// goes better
									if (newAircraft1.validate()) {
										newAircraft1.adjustment();

										long deltaCost = calcuateDeltaCost(newAircraft1, cancelledAir, aircraft1, aircraft1.getCancelledAircraft());

										if (deltaCost < 0) {
											XiaMengAirlineSolution aNewSolution = bestSolution.clone();
											aNewSolution.replaceOrAddNewAircraft(newAircraft1);
											aNewSolution.replaceOrAddNewAircraft(cancelledAir);
											aNewSolution.refreshCost(deltaCost);
											neighboursResult.addSolution(aNewSolution);
										}

									}

								}
							}
						}
					}

					for (int xx = 0; xx <= n - 1; xx++) {
						// if aircraft2 flights is circuit, place it into
						// cancellation route - Method 2
						if (!aircraft2.isCancel()) {
							Flight flightAir2 = aircraft2.getFlight(xx);
							if (circuitFlightsAir2.containsKey(flightAir2)) {
								for (Flight destFlight : circuitFlightsAir2.get(flightAir2)) {
									Aircraft newAircraft2 = aircraft2.clone();
									Aircraft cancelledAir = newAircraft2.getCancelledAircraft();
									
									Flight sFlight = newAircraft2.getFlight(xx);
									Flight dFlight = newAircraft2.getFlight(aircraft2.getFlightChain().indexOf(destFlight));

									cancelledAir.insertFlightChain(aircraft2, flightAir2, destFlight,
											cancelledAir.getFlight(cancelledAir.getFlightChain().size() - 1), false);
									newAircraft2.removeFlightChain(sFlight, dFlight);

									logger.info("Method 2 After exchange ...");
									List<Flight> updateList1 = newAircraft2.getFlightChain();
									for (Flight aF : updateList1) {
										logger.info("Air 2 " + aF.getSchdNo());
									}
									List<Flight> updateList2 = cancelledAir.getFlightChain();
									for (Flight aF : updateList2) {
										logger.info("Air 2 cancelled " + aF.getSchdNo());
									}
									logger.info("Method 2 Complete exchange ...");
									
									
									// only update solution when new change
									// goes better
									if (newAircraft2.validate()) {
										newAircraft2.adjustment();

										long deltaCost = calcuateDeltaCost(newAircraft2, cancelledAir, aircraft2, aircraft2.getCancelledAircraft());

										if (deltaCost < 0) {
											XiaMengAirlineSolution aNewSolution = bestSolution.clone();
											aNewSolution.replaceOrAddNewAircraft(newAircraft2);
											aNewSolution.replaceOrAddNewAircraft(cancelledAir);
											aNewSolution.refreshCost(deltaCost);
											neighboursResult.addSolution(aNewSolution);
										}

									}

								}
							}
						}
					}

					for (int u = 0; u <= m - 1; u++) {
						// if aircraft1/aircraft2 flights same source and
						// same destination, do exchange overlapped part - Method 3
						if (matchedFlights.containsKey(aircraft1.getFlight(u))) {
							List<MatchedFlight> matchedList = matchedFlights.get(aircraft1.getFlight(u));
							for (MatchedFlight aMatched : matchedList) {
								Aircraft newAircraft1 = aircraft1.clone();
								Aircraft newAircraft2 = aircraft2.clone();
								Flight air1SourceFlight = newAircraft1.getFlight(aMatched.getAir1SourceFlight());
								Flight air1DestFlight = newAircraft1.getFlight(aMatched.getAir1DestFlight());
								Flight air2SourceFlight = newAircraft2.getFlight(aMatched.getAir2SourceFlight());
								Flight air2DestFlight = newAircraft2.getFlight(aMatched.getAir2DestFlight());
								
								
								newAircraft1.insertFlightChain(aircraft2, aircraft2.getFlight(aMatched.getAir2SourceFlight()),
										 aircraft2.getFlight(aMatched.getAir2DestFlight()), 
										 air1DestFlight, false);
								newAircraft2.insertFlightChain(aircraft1, aircraft1.getFlight(aMatched.getAir1SourceFlight()),
										aircraft1.getFlight(aMatched.getAir1DestFlight()), 
										air2DestFlight, false);
								newAircraft1.removeFlightChain(air1SourceFlight,air1DestFlight);
								newAircraft2.removeFlightChain(air2SourceFlight,air2DestFlight);
								
								logger.info("Method 3 After exchange ...");
								List<Flight> updateList1 = newAircraft1.getFlightChain();
								for (Flight aF : updateList1) {
									logger.info("Air 1 " + aF.getSchdNo());
								}
								List<Flight> updateList2 = newAircraft2.getFlightChain();
								for (Flight aF : updateList2) {
									logger.info("Air 2  " + aF.getSchdNo());
								}
								logger.info("Method 3 Complete exchange ...");

								// only update solution when new change
								// goes better
								if (adjust(newAircraft1, newAircraft2, aircraft1, aircraft2)) {
									
									long deltaCost = calcuateDeltaCost(newAircraft1, newAircraft2, aircraft1, aircraft2);
									
									if (deltaCost < 0) {
										XiaMengAirlineSolution aNewSolution = bestSolution.clone();
										aNewSolution.replaceOrAddNewAircraft(newAircraft1);
										aNewSolution.replaceOrAddNewAircraft(newAircraft2);
										aNewSolution.refreshCost(deltaCost);
										neighboursResult.addSolution(aNewSolution);										
									}

								}

							}
						}

						for (int x = 0; x <= n - 1; x++) {
							// if aircraft1 flights is circuit, insert circuit
							// in front of flight x of aircraft2 - Method 4
							if (!aircraft2.isCancel()) {
								Flight flightAir1 = aircraft1.getFlight(u);
								if (circuitFlightsAir1.containsKey(flightAir1)) {
									for (Flight destFlight : circuitFlightsAir1.get(flightAir1)) {
										Aircraft newAircraft1 = aircraft1.clone();
										Aircraft newAircraft2 = aircraft2.clone();
										Flight air1SourceFlight = newAircraft1.getFlight(u);
										Flight air1DestFlight = newAircraft1.getFlight(aircraft1.getFlightChain().indexOf(destFlight));
										Flight air2Flight = newAircraft2.getFlight(x);

										newAircraft2.insertFlightChain(aircraft1, flightAir1, destFlight,
												air2Flight, true);
										newAircraft1.removeFlightChain(air1SourceFlight, air1DestFlight);
										
										logger.info("Method 4 After exchange ...");
										List<Flight> updateList1 = newAircraft1.getFlightChain();
										for (Flight aF : updateList1) {
											logger.info("Air 1 " + aF.getSchdNo());
										}
										List<Flight> updateList2 = newAircraft2.getFlightChain();
										for (Flight aF : updateList2) {
											logger.info("Air 2  " + aF.getSchdNo());
										}
										logger.info("Method 4 Complete exchange ...");

										// only update solution when new change
										// goes better
										if (adjust(newAircraft1, newAircraft2, aircraft1, aircraft2)) {
											
											long deltaCost = calcuateDeltaCost(newAircraft1, newAircraft2, aircraft1, aircraft2);
											
											if (deltaCost < 0) {
												XiaMengAirlineSolution aNewSolution = bestSolution.clone();
												aNewSolution.replaceOrAddNewAircraft(newAircraft1);
												aNewSolution.replaceOrAddNewAircraft(newAircraft2);
												aNewSolution.refreshCost(deltaCost);
												neighboursResult.addSolution(aNewSolution);												
											}

										}

									}

								}
							}
							// if aircraft2 flights is circuit, insert circuit
							// in front of flight u - method 5
							if (!aircraft1.isCancel()) {
								Flight flightAir2 = aircraft2.getFlight(x);
								if (circuitFlightsAir2.containsKey(flightAir2)) {
									for (Flight destFlight : circuitFlightsAir2.get(flightAir2)) {
										Aircraft newAircraft1 = aircraft1.clone();
										Aircraft newAircraft2 = aircraft2.clone();
										Flight air2SourceFlight = newAircraft2.getFlight(x);
										Flight air2DestFlight = newAircraft2.getFlight(aircraft2.getFlightChain().indexOf(destFlight));
										Flight air1Flight = newAircraft1.getFlight(u);

										newAircraft1.insertFlightChain(aircraft2, flightAir2, destFlight,
												air1Flight, true);
										newAircraft2.removeFlightChain(air2SourceFlight, air2DestFlight);
										
										logger.info("Method 5 After exchange ...");
										List<Flight> updateList1 = newAircraft1.getFlightChain();
										for (Flight aF : updateList1) {
											logger.info("Air 1 " + aF.getSchdNo());
										}
										List<Flight> updateList2 = newAircraft2.getFlightChain();
										for (Flight aF : updateList2) {
											logger.info("Air 2  " + aF.getSchdNo());
										}
										logger.info("Method 5 Complete exchange ...");

										// only update solution when new change
										// goes better
										if (adjust(newAircraft1, newAircraft2, aircraft1, aircraft2)) {
											long deltaCost = calcuateDeltaCost(newAircraft1, newAircraft2, aircraft1, aircraft2);
											
											if (deltaCost < 0) {
												XiaMengAirlineSolution aNewSolution = bestSolution.clone();
												aNewSolution.replaceOrAddNewAircraft(newAircraft1);
												aNewSolution.replaceOrAddNewAircraft(newAircraft2);
												aNewSolution.refreshCost(deltaCost);
												neighboursResult.addSolution(aNewSolution);												
											}
										}

									}

								}
							}

							// if aircraft1/2 have the same source, do exchange
							// to end - method 6
							if (!aircraft1.isCancel() && !aircraft2.isCancel()) {
								Flight aFlight = aircraft1.getFlight(u);
								Flight bFlight = aircraft2.getFlight(x);
								if (aFlight.getSourceAirPort().getId().equals(bFlight.getSourceAirPort().getId())) {
									Aircraft newAircraft1 = aircraft1.clone();
									Aircraft newAircraft2 = aircraft2.clone();
									
									
									Flight air1Flight = newAircraft1.getFlight(u);
									Flight air2Flight = newAircraft2.getFlight(x);
									Flight air1DestFlight = newAircraft1.getFlight(newAircraft1.getFlightChain().size() - 1);
									Flight air2DestFlight = newAircraft2.getFlight(newAircraft2.getFlightChain().size() - 1);
									
									newAircraft1.insertFlightChain(aircraft2, bFlight,
											aircraft2.getFlight(aircraft2.getFlightChain().size() - 1), air1Flight, true);
									newAircraft2.insertFlightChain(aircraft1, aFlight,
											aircraft1.getFlight(aircraft1.getFlightChain().size() - 1), air2Flight, true);
									newAircraft1.removeFlightChain(air1Flight,
											air1DestFlight);
									newAircraft2.removeFlightChain(air2Flight,
											air2DestFlight);
									
									logger.info("Method 6 After exchange ...");
									List<Flight> updateList1 = newAircraft1.getFlightChain();
									for (Flight aF : updateList1) {
										logger.info("Air 1 " + aF.getSchdNo());
									}
									List<Flight> updateList2 = newAircraft2.getFlightChain();
									for (Flight aF : updateList2) {
										logger.info("Air 2  " + aF.getSchdNo());
									}
									logger.info("Method 6 Complete exchange ...");

									// only update solution when new change
									// goes better
									if (adjust(newAircraft1, newAircraft2, aircraft1, aircraft2)) {
										long deltaCost = calcuateDeltaCost(newAircraft1, newAircraft2, aircraft1, aircraft2);
										
										if (deltaCost < 0) {
											XiaMengAirlineSolution aNewSolution = bestSolution.clone();
											aNewSolution.replaceOrAddNewAircraft(newAircraft1);
											aNewSolution.replaceOrAddNewAircraft(newAircraft2);
											aNewSolution.refreshCost(deltaCost);
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
