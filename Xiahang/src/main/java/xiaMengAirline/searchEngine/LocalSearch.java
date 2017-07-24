package xiaMengAirline.searchEngine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.MatchedFlight;
import xiaMengAirline.beans.RestrictedCandidateList;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.util.InitData;

public class LocalSearch {

	private static final Logger logger = Logger.getLogger(LocalSearch.class);
	private BigDecimal lowestScore = new BigDecimal(Long.MAX_VALUE);

	private XiaMengAirlineSolution buildLocalSolution(List<Aircraft> airList) {
		XiaMengAirlineSolution aNewLocalSolution = new XiaMengAirlineSolution();
		for (Aircraft aAir : airList) {
			aNewLocalSolution.replaceOrAddNewAircraft(aAir);
		}
		return aNewLocalSolution;

	}

	private BigDecimal adjust(XiaMengAirlineSolution newSolution) throws CloneNotSupportedException {
		List<Aircraft> airList = new ArrayList<Aircraft>(newSolution.getSchedule().values());

		for (Aircraft aAir : airList) {
			if (!aAir.validate())
				return lowestScore;
		}

		if (newSolution.adjust()) {
			return newSolution.getCost();
		} else
			return lowestScore;

	}

	private BigDecimal calculateDeltaCost(XiaMengAirlineSolution newSolution, XiaMengAirlineSolution oldSolution)
			throws CloneNotSupportedException {
		oldSolution.reConstruct();
		oldSolution.refreshCost(false);
		return (adjust(newSolution).subtract(oldSolution.getCost()));
	}

	public XiaMengAirlineSolution constructNewSolution(XiaMengAirlineSolution bestSolution)
			throws CloneNotSupportedException {
		List<Aircraft> checkList = new ArrayList<Aircraft>(bestSolution.getSchedule().values());
		List<Aircraft> checkOList = new ArrayList<Aircraft>(bestSolution.getSchedule().values());

		while (checkList.size() >= 1) {
			RestrictedCandidateList neighboursResult = new RestrictedCandidateList();
			// randomly select first air
			Aircraft air1 = checkList.remove(InitData.rndNumbers.nextInt(checkList.size()));
			List<Aircraft> air2CheckList = new ArrayList<Aircraft>(checkOList);
			air2CheckList.remove(air1);
			boolean isImproved = false;
			while (!isImproved && air2CheckList.size() >= 1) {
				// randomly select 2nd air
				Aircraft air2 = air2CheckList.remove(InitData.rndNumbers.nextInt(air2CheckList.size()));

				if ((air1.isCancel() && air2.isCancel())
						&& (air1.getAlternativeAircraft() == null && air2.getAlternativeAircraft() == null)
						&& (!air1.isUpdated() && !air2.isUpdated())) {
					// if just last 2 aircrafts
					if (checkList.size() == 0) {
						logger.warn("Unable to find more valid aircrafts for exchange!");
						break;
					} else {
						// if more than 2
						boolean isFound = false;
						List<Aircraft> notUseList = new ArrayList<Aircraft>();
						while (checkList.size() > 0 || isFound) {
							notUseList.add(air2);
							air2 = checkList.remove(InitData.rndNumbers.nextInt(checkList.size()));
							if ((air1.isCancel() && air2.isCancel())
									&& (air1.getAlternativeAircraft() == null && air2.getAlternativeAircraft() == null)
									&& (!air1.isUpdated() && !air2.isUpdated())) {
								;
							} else {
								isFound = true;
								//add back not use list
								checkList.addAll(notUseList);
							}

						}
						if (!isFound) {
							logger.warn("Unable to find more valid aircrafts for exchange!");
							break;
						}
							
					}
				}

				// start
				HashMap<Flight, List<Flight>> circuitFlightsAir1 = air1.getCircuitFlights();
				HashMap<Flight, List<Flight>> circuitFlightsAir2 = air2.getCircuitFlights();
				HashMap<Flight, List<MatchedFlight>> matchedFlights = air1.getMatchedFlights(air2);

				int m = air1.getFlightChain().size();
				int n = air2.getFlightChain().size();

				// Method 1
				if (!air1.isCancel()) {
					for (int uu = 0; uu <= m - 1; uu++) {
						Flight flightAir1 = air1.getFlight(uu);
						if (circuitFlightsAir1.containsKey(flightAir1)) {
							for (Flight destFlight : circuitFlightsAir1.get(flightAir1)) {
								Aircraft newAircraft1 = air1.clone();
								Aircraft cancelledAir = bestSolution.getAircraft(air1.getId(), air1.getType(), true, true)
										.clone();

								Flight sFlight = newAircraft1.getFlight(uu);
								Flight dFlight = newAircraft1.getFlight(air1.getFlightChain().indexOf(destFlight));

								cancelledAir.insertFlightChain(air1, flightAir1, destFlight,
										cancelledAir.getFlight(cancelledAir.getFlightChain().size() - 1), false);
								newAircraft1.removeFlightChain(sFlight, dFlight);

								logger.info("Method 1 After exchange ...");
								List<Flight> updateList1 = newAircraft1.getFlightChain();
								for (Flight aF : updateList1) {
									logger.info("Air  " + newAircraft1.getId() + " flight " + aF.getFlightId());
								}
								List<Flight> updateList2 = cancelledAir.getFlightChain();
								for (Flight aF : updateList2) {
									logger.info("Air cancelled " + cancelledAir.getId() + " flight " + aF.getFlightId());
								}
								logger.info("Method 1 Complete exchange ...");

								// only update solution when new change
								// goes better
								ArrayList<Aircraft> airList = new ArrayList<Aircraft>();
								airList.add(newAircraft1);
								airList.add(cancelledAir);

								ArrayList<Aircraft> airOldList = new ArrayList<Aircraft>();
								if (air1.getAlternativeAircraft() != null)
									airOldList.add(air1.getAlternativeAircraft());
								else
									airOldList.add(air1);

								XiaMengAirlineSolution aNewSolution = buildLocalSolution(airList);
								XiaMengAirlineSolution aOldSolution = buildLocalSolution(airOldList);

								BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

								if (deltaCost.longValue() < 0) {
									logger.info("Better Solution exists! Method 1 : " + deltaCost);
									XiaMengAirlineSolution aBetterSolution = bestSolution.clone();
									aBetterSolution.replaceOrAddNewAircraft(newAircraft1);
									aBetterSolution.replaceOrAddNewAircraft(cancelledAir);
									aBetterSolution.refreshCost(deltaCost);
									neighboursResult.addSolution(aBetterSolution);
								}

							}
						}

					}
				}

				// Method 2
				if (!air2.isCancel()) {
					for (int xx = 0; xx <= n - 1; xx++) {
						// if aircraft2 flights is circuit, place it into
						// cancellation route - Method 2
						Flight flightAir2 = air2.getFlight(xx);
						if (circuitFlightsAir2.containsKey(flightAir2)) {
							for (Flight destFlight : circuitFlightsAir2.get(flightAir2)) {
								Aircraft newAircraft2 = air2.clone();
								Aircraft cancelledAir = bestSolution.getAircraft(air2.getId(), air2.getType(), true, true)
										.clone();

								Flight sFlight = newAircraft2.getFlight(xx);
								Flight dFlight = newAircraft2.getFlight(air2.getFlightChain().indexOf(destFlight));

								cancelledAir.insertFlightChain(air2, flightAir2, destFlight,
										cancelledAir.getFlight(cancelledAir.getFlightChain().size() - 1), false);
								newAircraft2.removeFlightChain(sFlight, dFlight);

								logger.info("Method 2 After exchange ...");
								List<Flight> updateList1 = newAircraft2.getFlightChain();
								for (Flight aF : updateList1) {
									logger.info("Air " + newAircraft2.getId()+ " Flights: " +aF.getFlightId());
								}
								List<Flight> updateList2 = cancelledAir.getFlightChain();
								for (Flight aF : updateList2) {
									logger.info("Air cancelled " + cancelledAir.getId()+ " Flights: " + aF.getFlightId());
								}
								logger.info("Method 2 Complete exchange ...");

								// only update solution when new change
								// goes better
								ArrayList<Aircraft> airList = new ArrayList<Aircraft>();
								airList.add(newAircraft2);
								airList.add(cancelledAir);

								ArrayList<Aircraft> airOldList = new ArrayList<Aircraft>();
								if (air2.getAlternativeAircraft() != null)
									airOldList.add(air2.getAlternativeAircraft());
								else
									airOldList.add(air2);

								XiaMengAirlineSolution aNewSolution = buildLocalSolution(airList);
								XiaMengAirlineSolution aOldSolution = buildLocalSolution(airOldList);

								BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

								if (deltaCost.longValue() < 0) {
									logger.info("Better Solution exists! Method 2 : " + deltaCost);
									XiaMengAirlineSolution aBetterSolution = bestSolution.clone();
									aBetterSolution.replaceOrAddNewAircraft(newAircraft2);
									aBetterSolution.replaceOrAddNewAircraft(cancelledAir);
									aBetterSolution.refreshCost(deltaCost);
									neighboursResult.addSolution(aBetterSolution);
								}

							}
						}
					}

				}

				for (int u = 0; u <= m - 1; u++) {
					// if aircraft1/aircraft2 flights same source and
					// same destination, do exchange overlapped part -
					// Method 3
					if (matchedFlights.containsKey(air1.getFlight(u))) {
						List<MatchedFlight> matchedList = matchedFlights.get(air1.getFlight(u));
						for (MatchedFlight aMatched : matchedList) {
							Aircraft newAircraft1 = air1.clone();
							Aircraft newAircraft2 = air2.clone();
							Flight air1SourceFlight = newAircraft1.getFlight(aMatched.getAir1SourceFlight());
							Flight air1DestFlight = newAircraft1.getFlight(aMatched.getAir1DestFlight());
							Flight air2SourceFlight = newAircraft2.getFlight(aMatched.getAir2SourceFlight());
							Flight air2DestFlight = newAircraft2.getFlight(aMatched.getAir2DestFlight());

							newAircraft1.insertFlightChain(air2, air2.getFlight(aMatched.getAir2SourceFlight()),
									air2.getFlight(aMatched.getAir2DestFlight()), air1DestFlight, false);
							newAircraft2.insertFlightChain(air1, air1.getFlight(aMatched.getAir1SourceFlight()),
									air1.getFlight(aMatched.getAir1DestFlight()), air2DestFlight, false);
							newAircraft1.removeFlightChain(air1SourceFlight, air1DestFlight);
							newAircraft2.removeFlightChain(air2SourceFlight, air2DestFlight);

							logger.info("Method 3 After exchange ...");
							List<Flight> updateList1 = newAircraft1.getFlightChain();
							for (Flight aF : updateList1) {
								logger.info("Air  " + newAircraft1.getId() + "isCancel " + newAircraft1.isCancel()
										+ " flight " + aF.getFlightId());
							}
							List<Flight> updateList2 = newAircraft2.getFlightChain();
							for (Flight aF : updateList2) {
								logger.info("Air  " + newAircraft2.getId() + "isCancel " + newAircraft2.isCancel()
										+ " flight " + aF.getFlightId());
							}
							logger.info("Method 3 Complete exchange ...");

							// only update solution when new change
							// goes better
							ArrayList<Aircraft> airList = new ArrayList<Aircraft>();
							airList.add(newAircraft1);
							airList.add(newAircraft2);

							ArrayList<Aircraft> airOldList = new ArrayList<Aircraft>();
							if (air1.getAlternativeAircraft() != null)
								airOldList.add(air1.getAlternativeAircraft());
							else
								airOldList.add(air1);
							if (air2.getAlternativeAircraft() != null)
								airOldList.add(air2.getAlternativeAircraft());
							else
								airOldList.add(air2);

							XiaMengAirlineSolution aNewSolution = buildLocalSolution(airList);
							XiaMengAirlineSolution aOldSolution = buildLocalSolution(airOldList);

							BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);
							if (deltaCost.longValue() < 0) {
								logger.info("Better Solution exists! Method 3 : " + deltaCost);
								XiaMengAirlineSolution aBetterSolution = bestSolution.clone();
								aBetterSolution.replaceOrAddNewAircraft(newAircraft1);
								aBetterSolution.replaceOrAddNewAircraft(newAircraft2);
								aBetterSolution.refreshCost(deltaCost);
								neighboursResult.addSolution(aBetterSolution);
							}

						}
					}

					// if aircraft1 flights is circuit, insert circuit
					// in front of flight x of aircraft2 - Method 4
					if (!air2.isCancel()) {
						Flight flightAir1 = air1.getFlight(u);
						if (circuitFlightsAir1.containsKey(flightAir1)) {
							for (int x = 0; x <= n - 1; x++) {
								for (Flight destFlight : circuitFlightsAir1.get(flightAir1)) {
									Aircraft newAircraft1 = air1.clone();
									Aircraft newAircraft2 = air2.clone();
									Flight air1SourceFlight = newAircraft1.getFlight(u);
									Flight air1DestFlight = newAircraft1
											.getFlight(air1.getFlightChain().indexOf(destFlight));
									Flight air2Flight = newAircraft2.getFlight(x);

									newAircraft2.insertFlightChain(air1, flightAir1, destFlight, air2Flight, true);
									newAircraft1.removeFlightChain(air1SourceFlight, air1DestFlight);

									logger.info("Method 4 After exchange ...");
									List<Flight> updateList1 = newAircraft1.getFlightChain();
									for (Flight aF : updateList1) {
										logger.info("Air " + newAircraft1.getId() + " Flights: " + aF.getFlightId());
									}
									List<Flight> updateList2 = newAircraft2.getFlightChain();
									for (Flight aF : updateList2) {
										logger.info("Air  " + newAircraft2.getId() + " Flights: " + aF.getFlightId());
									}
									logger.info("Method 4 Complete exchange ...");

									// only update solution when new change
									// goes better
									ArrayList<Aircraft> airList = new ArrayList<Aircraft>();
									airList.add(newAircraft1);
									airList.add(newAircraft2);

									ArrayList<Aircraft> airOldList = new ArrayList<Aircraft>();

									if (air1.getAlternativeAircraft() != null)
										airOldList.add(air1.getAlternativeAircraft());
									else
										airOldList.add(air1);
									if (air2.getAlternativeAircraft() != null)
										airOldList.add(air2.getAlternativeAircraft());
									else
										airOldList.add(air2);

									XiaMengAirlineSolution aNewSolution = buildLocalSolution(airList);
									XiaMengAirlineSolution aOldSolution = buildLocalSolution(airOldList);

									BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);
									if (deltaCost.longValue() < 0) {
										logger.info("Better Solution exists! Method 4 : " + deltaCost);
										XiaMengAirlineSolution aBetterSolution = bestSolution.clone();
										aBetterSolution.replaceOrAddNewAircraft(newAircraft1);
										aBetterSolution.replaceOrAddNewAircraft(newAircraft2);
										aBetterSolution.refreshCost(deltaCost);
										neighboursResult.addSolution(aBetterSolution);
									}

								}
							}
						}

					}

					// if aircraft2 flights is circuit, insert circuit
					// in front of flight u - method 5
					if (!air1.isCancel()) {
						for (int x = 0; x <= n - 1; x++) {
							Flight flightAir2 = air2.getFlight(x);
							if (circuitFlightsAir2.containsKey(flightAir2)) {
								for (Flight destFlight : circuitFlightsAir2.get(flightAir2)) {
									Aircraft newAircraft1 = air1.clone();
									Aircraft newAircraft2 = air2.clone();
									Flight air2SourceFlight = newAircraft2.getFlight(x);
									Flight air2DestFlight = newAircraft2
											.getFlight(air2.getFlightChain().indexOf(destFlight));
									Flight air1Flight = newAircraft1.getFlight(u);

									newAircraft1.insertFlightChain(air2, flightAir2, destFlight, air1Flight, true);
									newAircraft2.removeFlightChain(air2SourceFlight, air2DestFlight);

									logger.info("Method 5 After exchange ...");
									List<Flight> updateList1 = newAircraft1.getFlightChain();
									for (Flight aF : updateList1) {
										logger.info("Air  " + newAircraft1.getId() + " Flights: " + aF.getFlightId());
									}
									List<Flight> updateList2 = newAircraft2.getFlightChain();
									for (Flight aF : updateList2) {
										logger.info("Air  " + newAircraft2.getId() + " Flights: " + aF.getFlightId());
									}
									logger.info("Method 5 Complete exchange ...");

									// only update solution when new change
									// goes better
									ArrayList<Aircraft> airList = new ArrayList<Aircraft>();
									airList.add(newAircraft1);
									airList.add(newAircraft2);

									ArrayList<Aircraft> airOldList = new ArrayList<Aircraft>();
									if (air1.getAlternativeAircraft() != null)
										airOldList.add(air1.getAlternativeAircraft());
									else
										airOldList.add(air1);
									if (air2.getAlternativeAircraft() != null)
										airOldList.add(air2.getAlternativeAircraft());
									else
										airOldList.add(air2);

									XiaMengAirlineSolution aNewSolution = buildLocalSolution(airList);
									XiaMengAirlineSolution aOldSolution = buildLocalSolution(airOldList);

									BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

									if (deltaCost.longValue() < 0) {
										logger.info("Better Solution exists! Method 5 : " + deltaCost);
										XiaMengAirlineSolution aBetterSolution = bestSolution.clone();
										aBetterSolution.replaceOrAddNewAircraft(newAircraft1);
										aBetterSolution.replaceOrAddNewAircraft(newAircraft2);
										aBetterSolution.refreshCost(deltaCost);
										neighboursResult.addSolution(aBetterSolution);
									}
								}

							}
						}

					}

					// if aircraft1/2 have the same source, do exchange
					// to end - method 6
					if (!air1.isCancel() && !air2.isCancel()) {
						for (int x = 0; x <= n - 1; x++) {
							Flight aFlight = air1.getFlight(u);
							Flight bFlight = air2.getFlight(x);
							if (aFlight.getSourceAirPort().getId().equals(bFlight.getSourceAirPort().getId())) {
								Aircraft newAircraft1 = air1.clone();
								Aircraft newAircraft2 = air2.clone();

								Flight air1Flight = newAircraft1.getFlight(u);
								Flight air2Flight = newAircraft2.getFlight(x);
								Flight air1DestFlight = newAircraft1.getFlight(newAircraft1.getFlightChain().size() - 1);
								Flight air2DestFlight = newAircraft2.getFlight(newAircraft2.getFlightChain().size() - 1);

								newAircraft1.insertFlightChain(air2, bFlight,
										air2.getFlight(air2.getFlightChain().size() - 1), air1Flight, true);
								newAircraft2.insertFlightChain(air1, aFlight,
										air1.getFlight(air1.getFlightChain().size() - 1), air2Flight, true);
								newAircraft1.removeFlightChain(air1Flight, air1DestFlight);
								newAircraft2.removeFlightChain(air2Flight, air2DestFlight);

								logger.info("Method 6 After exchange ...");
								List<Flight> updateList1 = newAircraft1.getFlightChain();
								for (Flight aF : updateList1) {
									logger.info("Air  " + newAircraft1.getId() + " Flights: " + aF.getFlightId());
								}
								List<Flight> updateList2 = newAircraft2.getFlightChain();
								for (Flight aF : updateList2) {
									logger.info("Air  " + newAircraft2.getId() + " Flights: " + aF.getFlightId());
								}
								logger.info("Method 6 Complete exchange ...");

								// only update solution when new change
								// goes better
								ArrayList<Aircraft> airList = new ArrayList<Aircraft>();
								airList.add(newAircraft1);
								airList.add(newAircraft2);

								ArrayList<Aircraft> airOldList = new ArrayList<Aircraft>();
								if (air1.getAlternativeAircraft() != null)
									airOldList.add(air1.getAlternativeAircraft());
								else
									airOldList.add(air1);
								if (air2.getAlternativeAircraft() != null)
									airOldList.add(air2.getAlternativeAircraft());
								else
									airOldList.add(air2);

								XiaMengAirlineSolution aNewSolution = buildLocalSolution(airList);
								XiaMengAirlineSolution aOldSolution = buildLocalSolution(airOldList);

								BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

								if (deltaCost.longValue() < 0) {
									logger.info("Better Solution exists! Method 6 : " + deltaCost);
									XiaMengAirlineSolution aBetterSolution = bestSolution.clone();
									aBetterSolution.replaceOrAddNewAircraft(newAircraft1);
									aBetterSolution.replaceOrAddNewAircraft(newAircraft2);
									aBetterSolution.refreshCost(deltaCost);
									neighboursResult.addSolution(aBetterSolution);
								}

							}
						}

					}

				}
				if (neighboursResult.hasSolution()) {
					isImproved = true;
					bestSolution = neighboursResult.selectASoluiton();
					neighboursResult.clear();				
				}				
			} 
			
		}

		return bestSolution;

	}
}
