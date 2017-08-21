package xiaMengAirline.searchEngine;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.SolutionNotValid;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.MatchedFlight;
import xiaMengAirline.beans.RestrictedCandidateList;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.utils.InitData;

public class LocalSearch2 {

	private static final Logger logger = Logger.getLogger(LocalSearch2.class);
	private int BATCH_SIZE = 20;

	private BigDecimal lowestScore = new BigDecimal(Long.MAX_VALUE);

	private XiaMengAirlineSolution buildLocalSolution(List<Aircraft> airList) {
		XiaMengAirlineSolution aNewLocalSolution = new XiaMengAirlineSolution();
		for (Aircraft aAir : airList) {
			aNewLocalSolution.replaceOrAddNewAircraft(aAir);
		}
		return aNewLocalSolution;

	}
	
	private BigDecimal adjust2(XiaMengAirlineSolution newSolution, XiaMengAirlineSolution oldSolution) throws CloneNotSupportedException, SolutionNotValid {
		List<Aircraft> airList = new ArrayList<Aircraft>(newSolution.getSchedule().values());

		for (Aircraft aAir : airList) {
			if (!aAir.validate())
				return lowestScore;
		}
		
		try {
			XiaMengAirlineSolution aBetterSolution = newSolution.getBestSolution();
			XiaMengAirlineSolution aBetterOutput = aBetterSolution.reConstruct2();
			aBetterOutput.refreshCost(false);
			return aBetterOutput.getCost();
		} catch (AircraftNotAdjustable ex) {
			logger.warn("New solution is not adjustable air " + ex.getAir().getId());
			return lowestScore;
		}


	}

	private BigDecimal adjust(XiaMengAirlineSolution newSolution, XiaMengAirlineSolution oldSolution) throws CloneNotSupportedException, SolutionNotValid {
		List<Aircraft> airList = new ArrayList<Aircraft>(newSolution.getSchedule().values());

		for (Aircraft aAir : airList) {
			if (!aAir.validate())
				return lowestScore;
		}
		if (!newSolution.validflightNumers(oldSolution)) {
			throw new SolutionNotValid(newSolution, "exchange");
		}
		

		XiaMengAirlineSolution backup = newSolution.clone();
		if (newSolution.adjust()) {
			if (!newSolution.validAlternativeflightNumers(oldSolution) 
					|| !newSolution.validflightNumers(oldSolution)) {
				backup.adjust();
				newSolution.validAlternativeflightNumers(oldSolution);
				throw new SolutionNotValid(newSolution, "adjust");
			}
			return newSolution.getCost();
		} else
			return lowestScore;

	}

	private BigDecimal calculateDeltaCost(XiaMengAirlineSolution newSolution, XiaMengAirlineSolution oldSolution)
			throws CloneNotSupportedException, SolutionNotValid {
		XiaMengAirlineSolution oldSolOut = oldSolution.reConstruct();
		oldSolOut.refreshCost(false);
		return (adjust2(newSolution, oldSolution).subtract(oldSolOut.getCost()));
	}

	public XiaMengAirlineSolution buildSolution(List<Aircraft> checkSList, XiaMengAirlineSolution bestSolution)
			throws CloneNotSupportedException, SolutionNotValid {
		// build batch list for first air
		HashMap<Integer, List<Aircraft>> airBatchList = new HashMap<Integer, List<Aircraft>>();
		int numberOfBatches = (int) Math.ceil((float) checkSList.size() / BATCH_SIZE);
		for (int batchNo = 1; batchNo <= numberOfBatches; batchNo++) {
			int noOfSelected = 1;
			List<Aircraft> airBatch = new ArrayList<Aircraft>();
			while (noOfSelected <= BATCH_SIZE && checkSList.size() > 0) {
				Aircraft air1 = checkSList.remove(InitData.rndNumbers.nextInt(checkSList.size()));
				airBatch.add(air1);
				noOfSelected++;
			}
			airBatchList.put(batchNo, airBatch);
		}

		for (Map.Entry<Integer, List<Aircraft>> entry : airBatchList.entrySet()) {
			RestrictedCandidateList neighboursResult = new RestrictedCandidateList();
			int currentBatch = entry.getKey();
			List<Aircraft> firstAirList = entry.getValue();
			List<Aircraft> firstAirNewList = new ArrayList<Aircraft>();
			// rebuild search list as from latest best solution
			for (Aircraft air1 : firstAirList) {
				air1 = bestSolution.getAircraft(air1.getId(), air1.getType(), air1.isCancel(), false);
				firstAirNewList.add(air1);
			}

			System.out.println("Processing batch ... " + currentBatch);
			for (Aircraft air1 : firstAirNewList) {
				logger.info("Processing first air " + air1.getId());
				List<Aircraft> air2CheckList = new ArrayList<Aircraft>(bestSolution.getSchedule().values());
				air2CheckList.remove(air1);
				boolean isImproved = false;
				while (!isImproved && air2CheckList.size() >= 1) {
					// randomly select 2nd air
					Aircraft air2 = air2CheckList.remove(InitData.rndNumbers.nextInt(air2CheckList.size()));
					if ((air1.isCancel() && air2.isCancel())
							&& (air1.getAlternativeAircraft() == null && air2.getAlternativeAircraft() == null)
							&& (!air1.isUpdated() && !air2.isUpdated())) {
						// if not exchangeable, select next air
						boolean isFound = false;
						while (air2CheckList.size() > 0 || isFound) {
							air2 = air2CheckList.remove(InitData.rndNumbers.nextInt(air2CheckList.size()));
							if ((air1.isCancel() && air2.isCancel())
									&& (air1.getAlternativeAircraft() == null && air2.getAlternativeAircraft() == null)
									&& (!air1.isUpdated() && !air2.isUpdated())) {
								;
							} else {
								isFound = true;
							}
						}
						if (!isFound) {
							System.out.println("Unable to find more valid aircrafts for exchange!");
							System.out.println("Completed air ... " + air1.getId() + " on batch " + currentBatch);
							break;
						}
					}
					logger.info("Processing second air " + air2.getId());

					// start
					HashMap<Flight, List<Flight>> circuitFlightsAir1 = air1.getCircuitFlights();
					HashMap<Flight, List<Flight>> circuitFlightsAir2 = air2.getCircuitFlights();
					HashMap<Flight, List<MatchedFlight>> matchedFlights = air1.getMatchedFlights(air2);

					int m = air1.getFlightChain().size();
					int n = air2.getFlightChain().size();

					// Method 1
					// if aircraft1 flights is circuit, place it into
					// cancellation route - Method 1
					if (!air1.isCancel()) {
						for (int uu = 0; uu <= m - 1; uu++) {
							Flight flightAir1 = air1.getFlight(uu);
							if (circuitFlightsAir1.containsKey(flightAir1)) {
								for (Flight destFlight : circuitFlightsAir1.get(flightAir1)) {
									Aircraft newAircraft1 = air1.clone();
									Aircraft cancelledAir = bestSolution
											.getAircraft(air1.getId(), air1.getType(), true, true).clone();

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
										logger.info("Air cancelled " + cancelledAir.getId() + " flight "
												+ aF.getFlightId());
									}
									logger.info("Method 1 Complete exchange ...");

									// only update solution when new change
									// goes better
									ArrayList<Aircraft> airList = new ArrayList<Aircraft>();
									airList.add(newAircraft1);
									airList.add(cancelledAir);

									ArrayList<Aircraft> airOldList = new ArrayList<Aircraft>();
									airOldList.add(air1);
									airOldList.add(bestSolution.getAircraft(air1.getId(), air1.getType(), true, true));

									XiaMengAirlineSolution aNewSolution = buildLocalSolution(airList);
									XiaMengAirlineSolution aOldSolution = buildLocalSolution(airOldList);

									if (aNewSolution.validateIter()) {
										BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

										if (deltaCost.longValue() < 0) {
											logger.info("Better Solution exists! Method 1 : " + deltaCost);
											isImproved = true;
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
									Aircraft cancelledAir = bestSolution
											.getAircraft(air2.getId(), air2.getType(), true, true).clone();

									Flight sFlight = newAircraft2.getFlight(xx);
									Flight dFlight = newAircraft2.getFlight(air2.getFlightChain().indexOf(destFlight));

									cancelledAir.insertFlightChain(air2, flightAir2, destFlight,
											cancelledAir.getFlight(cancelledAir.getFlightChain().size() - 1), false);
									newAircraft2.removeFlightChain(sFlight, dFlight);

									logger.info("Method 2 After exchange ...");
									List<Flight> updateList1 = newAircraft2.getFlightChain();
									for (Flight aF : updateList1) {
										logger.info("Air " + newAircraft2.getId() + " Flights: " + aF.getFlightId());
									}
									List<Flight> updateList2 = cancelledAir.getFlightChain();
									for (Flight aF : updateList2) {
										logger.info("Air cancelled " + cancelledAir.getId() + " Flights: "
												+ aF.getFlightId());
									}
									logger.info("Method 2 Complete exchange ...");

									// only update solution when new change
									// goes better
									ArrayList<Aircraft> airList = new ArrayList<Aircraft>();
									airList.add(newAircraft2);
									airList.add(cancelledAir);

									ArrayList<Aircraft> airOldList = new ArrayList<Aircraft>();
									airOldList.add(air2);
									airOldList.add(bestSolution.getAircraft(air2.getId(), air2.getType(), true, true));

									XiaMengAirlineSolution aNewSolution = buildLocalSolution(airList);
									XiaMengAirlineSolution aOldSolution = buildLocalSolution(airOldList);

									if (aNewSolution.validateIter()) {
										BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

										if (deltaCost.longValue() < 0) {
											logger.info("Better Solution exists! Method 2 : " + deltaCost);
											isImproved = true;
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

								// if cancel flights, need adjust its
								// alternative
								if (newAircraft1.isCancel()) {
									Aircraft airCancelAlt = newAircraft1.getAlternativeAircraft();
									if (airCancelAlt != null) {
										List<Flight> movedFlights = newAircraft1
												.getSpecifiedFlightChain(air1SourceFlight, air1DestFlight);
										for (Flight aFlight : movedFlights) {
											if (airCancelAlt.getFlightByFlightId(aFlight.getFlightId()) != null) {
												airCancelAlt.getFlightChain().remove(
														airCancelAlt.getFlightByFlightId(aFlight.getFlightId()));
											}
										}
									}
								}

								if (newAircraft2.isCancel()) {
									Aircraft airCancelAlt = newAircraft2.getAlternativeAircraft();
									if (airCancelAlt != null) {
										List<Flight> movedFlights = newAircraft2
												.getSpecifiedFlightChain(air2SourceFlight, air2DestFlight);
										for (Flight aFlight : movedFlights) {
											if (airCancelAlt.getFlightByFlightId(aFlight.getFlightId()) != null) {
												airCancelAlt.getFlightChain().remove(
														airCancelAlt.getFlightByFlightId(aFlight.getFlightId()));
											}
										}
									}
								}
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
								airOldList.add(air1);
								airOldList.add(air2);

								XiaMengAirlineSolution aNewSolution = buildLocalSolution(airList);
								XiaMengAirlineSolution aOldSolution = buildLocalSolution(airOldList);
								if (aNewSolution.validateIter()) {

									BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

									if (deltaCost.longValue() < 0) {
										logger.info("Better Solution exists! Method 3 : " + deltaCost);
										isImproved = true;
										XiaMengAirlineSolution aBetterSolution = bestSolution.clone();
										aBetterSolution.replaceOrAddNewAircraft(newAircraft1);
										aBetterSolution.replaceOrAddNewAircraft(newAircraft2);
										aBetterSolution.refreshCost(deltaCost);
										neighboursResult.addSolution(aBetterSolution);
									}
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
										

										logger.info("Method 4 After exchange ...");
										List<Flight> updateList1 = newAircraft1.getFlightChain();
										for (Flight aF : updateList1) {
											logger.info(
													"Air " + newAircraft1.getId() + " Flights: " + aF.getFlightId());
										}
										List<Flight> updateList2 = newAircraft2.getFlightChain();
										for (Flight aF : updateList2) {
											logger.info(
													"Air  " + newAircraft2.getId() + " Flights: " + aF.getFlightId());
										}
										logger.info("Method 4 Complete exchange ...");

										// if cancel flights, need adjust its
										// alternative
										if (newAircraft1.isCancel()) {
											Aircraft airCancelAlt = newAircraft1.getAlternativeAircraft();
											if (airCancelAlt != null) {
												List<Flight> movedFlights = newAircraft1
														.getSpecifiedFlightChain(air1SourceFlight, air1DestFlight);
												for (Flight aFlight : movedFlights) {
													if (airCancelAlt
															.getFlightByFlightId(aFlight.getFlightId()) != null) {
														airCancelAlt.getFlightChain().remove(airCancelAlt
																.getFlightByFlightId(aFlight.getFlightId()));
													}
												}
											}
										}
										newAircraft1.removeFlightChain(air1SourceFlight, air1DestFlight);

										// only update solution when new change
										// goes better
										ArrayList<Aircraft> airList = new ArrayList<Aircraft>();
										airList.add(newAircraft1);
										airList.add(newAircraft2);

										ArrayList<Aircraft> airOldList = new ArrayList<Aircraft>();
										airOldList.add(air1);
										airOldList.add(air2);

										XiaMengAirlineSolution aNewSolution = buildLocalSolution(airList);
										XiaMengAirlineSolution aOldSolution = buildLocalSolution(airOldList);

										if (aNewSolution.validateIter()) {
											BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);
											if (deltaCost.longValue() < 0) {
												logger.info("Better Solution exists! Method 4 : " + deltaCost);
												isImproved = true;
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
										

										logger.info("Method 5 After exchange ...");
										List<Flight> updateList1 = newAircraft1.getFlightChain();
										for (Flight aF : updateList1) {
											logger.info(
													"Air  " + newAircraft1.getId() + " Flights: " + aF.getFlightId());
										}
										List<Flight> updateList2 = newAircraft2.getFlightChain();
										for (Flight aF : updateList2) {
											logger.info(
													"Air  " + newAircraft2.getId() + " Flights: " + aF.getFlightId());
										}
										logger.info("Method 5 Complete exchange ...");
										
										// if cancel flights, need adjust its
										// alternative
										if (newAircraft2.isCancel()) {
											Aircraft airCancelAlt = newAircraft2.getAlternativeAircraft();
											if (airCancelAlt != null) {
												List<Flight> movedFlights = newAircraft2
														.getSpecifiedFlightChain(air2SourceFlight, air2DestFlight);
												for (Flight aFlight : movedFlights) {
													if (airCancelAlt.getFlightByFlightId(aFlight.getFlightId()) != null) {
														airCancelAlt.getFlightChain().remove(
																airCancelAlt.getFlightByFlightId(aFlight.getFlightId()));
													}
												}
											}
										}
										newAircraft2.removeFlightChain(air2SourceFlight, air2DestFlight);

										// only update solution when new change
										// goes better
										ArrayList<Aircraft> airList = new ArrayList<Aircraft>();
										airList.add(newAircraft1);
										airList.add(newAircraft2);

										ArrayList<Aircraft> airOldList = new ArrayList<Aircraft>();
										airOldList.add(air1);
										airOldList.add(air2);

										XiaMengAirlineSolution aNewSolution = buildLocalSolution(airList);
										XiaMengAirlineSolution aOldSolution = buildLocalSolution(airOldList);

										if (aNewSolution.validateIter()) {
											BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

											if (deltaCost.longValue() < 0) {
												logger.info("Better Solution exists! Method 5 : " + deltaCost);
												isImproved = true;
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
									Flight air1DestFlight = newAircraft1
											.getFlight(newAircraft1.getFlightChain().size() - 1);
									Flight air2DestFlight = newAircraft2
											.getFlight(newAircraft2.getFlightChain().size() - 1);

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
									airOldList.add(air1);
									airOldList.add(air2);

									XiaMengAirlineSolution aNewSolution = buildLocalSolution(airList);
									XiaMengAirlineSolution aOldSolution = buildLocalSolution(airOldList);

									BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

									if (aNewSolution.validateIter()) {
										if (deltaCost.longValue() < 0) {
											logger.info("Better Solution exists! Method 6 : " + deltaCost);
											isImproved = true;
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

					}

				}

				if (isImproved) {
					System.out.println("Completed air ... " + air1.getId() + " on batch " + currentBatch);
				}

			}
			if (neighboursResult.hasSolution()) {
				bestSolution = neighboursResult.selectASoluiton();
				neighboursResult.clear();
				System.out.println("Completed batch ... " + currentBatch + " Cost: " + bestSolution.getCost());
				if (bestSolution.getCost().longValue() < 90 && (currentBatch % 3 == 0)) {
					Date dNow = new Date( );
				      SimpleDateFormat ft = 
				      new SimpleDateFormat ("hh_mm_ss");
					XiaMengAirlineSolution aBetterOutput = bestSolution.reConstruct();
					aBetterOutput.refreshCost(true);
					aBetterOutput.generateOutput("batch_"+currentBatch+"_"+ft.format(dNow));
				}
			}
		}

		return bestSolution;
	}

	public XiaMengAirlineSolution constructNewSolution(XiaMengAirlineSolution bestSolution)
			throws CloneNotSupportedException, SolutionNotValid {
		List<Aircraft> checkSList = new ArrayList<Aircraft>(bestSolution.getSchedule().values());
		return buildSolution(checkSList, bestSolution);

	}

	public int getBATCH_SIZE() {
		return BATCH_SIZE;
	}

	public void setBATCH_SIZE(int bATCH_SIZE) {
		BATCH_SIZE = bATCH_SIZE;
	}
}
