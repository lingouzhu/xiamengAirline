package xiaMengAirline.searchEngine;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.MatchedFlight;
import xiaMengAirline.beans.RestrictedCandidateList;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.searchEngine.OptimizerStragety.SELECTION;
import xiaMengAirline.utils.Utils;

public class ExchangeSearch {
	private static final Logger logger = Logger.getLogger(ExchangeSearch.class);
	private IterativeMethod aDriver = null;
	private OptimizerStragety aStragety = null;
	private AdjustmentEngine adjustmentEngine = null;
	private RestrictedCandidateList neighboursResult = null;

	public XiaMengAirlineSolution discoverBetterSolution(XiaMengAirlineSolution aSolution)
			throws CloneNotSupportedException, ParseException {
		aDriver.setupIterationStragety(aStragety);
		aDriver.setupIterationContent(aSolution);
		List<Aircraft> aBatch = aDriver.getNextDriveForIterative();
		IterativeSelector aSelector = null;

		while (aBatch != null) {
			neighboursResult = new RestrictedCandidateList();
			neighboursResult.setaStragety(aStragety);
			int currentBatch = aDriver.getCurrentIterationNumber();

			System.out.println("Processing batch ... " + currentBatch);
			logger.debug("Processing batch ... " + currentBatch);
			List<String> processedList = new ArrayList<String>();
			for (Aircraft air1 : aBatch) {
				logger.debug("Processing first air " + air1.getId());
				boolean isImproved = false;
				Aircraft air2 = null;
				if (aStragety.getSelectionRule() == SELECTION.RANDOM)
					aSelector = new IterativeRadomSelector();
				aSelector.setupIterationStragety(aStragety);
				aSelector.setupCandidateList(new ArrayList<Aircraft>(aSolution.getSchedule().values()));

				while (!isImproved) {
					air2 = aSelector.selectAircraft(air1);

					if (air2 == null) {
						System.out.println("Completed air ... " + air1.getId() + " on batch " + currentBatch);
						logger.debug("Completed air ... " + air1.getId() + " on batch " + currentBatch);
						break;
					}

					String aKey = Utils.build2AirKey(air1.getId(), air2.getId());
					if (processedList.contains(aKey))
						continue;
					else
						processedList.add(aKey);

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
									Aircraft air1Cancelled = aSolution.getAircraft(air1.getId(), air1.getType(), true,
											true);
									Aircraft cancelledAir = air1Cancelled.clone();

									Flight sFlight = newAircraft1.getFlight(uu);
									Flight dFlight = newAircraft1.getFlight(air1.getFlightChain().indexOf(destFlight));

									cancelledAir.insertFlightChain(air1, flightAir1, destFlight,
											cancelledAir.getFlight(cancelledAir.getFlightChain().size() - 1), false);
									newAircraft1.removeFlightChain(sFlight, dFlight);

									if (aStragety.isDebug()) {
										logger.debug("Method 1 After exchange...");
										List<Flight> updateList1 = newAircraft1.getFlightChain();
										for (Flight aF : updateList1) {
											logger.debug("Air " + newAircraft1.getId() + " flight " + aF.getFlightId());
										}
										List<Flight> updateList2 = cancelledAir.getFlightChain();
										for (Flight aF : updateList2) {
											logger.debug("Air cancelled " + cancelledAir.getId() + " flight "
													+ aF.getFlightId());
										}
										logger.debug("Method 1 Complete exchange...");
									}

									// only update solution when new change
									// goes better
									if (adjustmentEngine.adjust(newAircraft1, cancelledAir)) {
										if (newAircraft1.getCost() < air1.getCost()) {
											if (BusinessDomain.validateFlights(air1, air1Cancelled, newAircraft1,
													cancelledAir)) {
												if (aStragety.isAbortWhenImproved())
													isImproved = true;
												XiaMengAirlineSolution aBetterSolution = aSolution.clone();
												aBetterSolution.replaceOrAddNewAircraft(newAircraft1);
												aBetterSolution.replaceOrAddNewAircraft(cancelledAir);
												aBetterSolution.refreshCost(
														new BigDecimal(newAircraft1.getCost() - air1.getCost()));
												neighboursResult.addSolution(aBetterSolution);
												logger.debug("Better Solution exists! Method 1 : "
														+ aBetterSolution.getCost());
											} else {
												logger.warn("Invalid aircraft after exchange " + air1.getId());
											}

										}

									}

								}
							}

						}
					}

					if (!air2.isCancel()) {
						for (int xx = 0; xx <= n - 1; xx++) {
							// if aircraft2 flights is circuit, place it into
							// cancellation route - Method 2
							Flight flightAir2 = air2.getFlight(xx);
							if (circuitFlightsAir2.containsKey(flightAir2)) {
								for (Flight destFlight : circuitFlightsAir2.get(flightAir2)) {
									Aircraft newAircraft2 = air2.clone();
									Aircraft air2Cancelled = aSolution.getAircraft(air2.getId(), air2.getType(), true,
											true);
									Aircraft cancelledAir = air2Cancelled.clone();

									Flight sFlight = newAircraft2.getFlight(xx);
									Flight dFlight = newAircraft2.getFlight(air2.getFlightChain().indexOf(destFlight));

									cancelledAir.insertFlightChain(air2, flightAir2, destFlight,
											cancelledAir.getFlight(cancelledAir.getFlightChain().size() - 1), false);
									newAircraft2.removeFlightChain(sFlight, dFlight);

									if (aStragety.isDebug()) {
										logger.debug("Method 2 After exchange ...");
										List<Flight> updateList1 = newAircraft2.getFlightChain();
										for (Flight aF : updateList1) {
											logger.debug(
													"Air " + newAircraft2.getId() + " Flights: " + aF.getFlightId());
										}
										List<Flight> updateList2 = cancelledAir.getFlightChain();
										for (Flight aF : updateList2) {
											logger.debug("Air cancelled " + cancelledAir.getId() + " Flights: "
													+ aF.getFlightId());
										}
										logger.debug("Method 2 Complete exchange...");
									}

									// only update solution when new change
									// goes better
									if (adjustmentEngine.adjust(newAircraft2, cancelledAir)) {
										if (newAircraft2.getCost() < air2.getCost()) {
											if (BusinessDomain.validateFlights(air2, air2Cancelled, newAircraft2,
													cancelledAir)) {
												if (aStragety.isAbortWhenImproved())
													isImproved = true;
												XiaMengAirlineSolution aBetterSolution = aSolution.clone();
												aBetterSolution.replaceOrAddNewAircraft(newAircraft2);
												aBetterSolution.replaceOrAddNewAircraft(cancelledAir);
												aBetterSolution.refreshCost(
														new BigDecimal(newAircraft2.getCost() - air2.getCost()));
												neighboursResult.addSolution(aBetterSolution);
												logger.debug("Better Solution exists! Method 2 : "
														+ aBetterSolution.getCost());
											} else {
												logger.warn("Invalid aircraft after exchange " + air2.getId());
											}

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
								if (!air1SourceFlight.isAdjustable() || !air1DestFlight.isAdjustable()
										|| !air2SourceFlight.isAdjustable() || !air2DestFlight.isAdjustable())
									continue;

								newAircraft1.insertFlightChain(air2, air2.getFlight(aMatched.getAir2SourceFlight()),
										air2.getFlight(aMatched.getAir2DestFlight()), air1DestFlight, false);
								newAircraft2.insertFlightChain(air1, air1.getFlight(aMatched.getAir1SourceFlight()),
										air1.getFlight(aMatched.getAir1DestFlight()), air2DestFlight, false);

								newAircraft1.removeFlightChain(air1SourceFlight, air1DestFlight);
								newAircraft2.removeFlightChain(air2SourceFlight, air2DestFlight);

								if (aStragety.isDebug()) {
									logger.debug("Method 3 After exchange...");
									List<Flight> updateList1 = newAircraft1.getFlightChain();
									for (Flight aF : updateList1) {
										logger.debug("Air " + newAircraft1.getId() + "isCancel "
												+ newAircraft1.isCancel() + " flight " + aF.getFlightId());
									}
									List<Flight> updateList2 = newAircraft2.getFlightChain();
									for (Flight aF : updateList2) {
										logger.debug("Air " + newAircraft2.getId() + "isCancel "
												+ newAircraft2.isCancel() + " flight " + aF.getFlightId());
									}
									logger.debug("Method 3 Complete exchange...");
								}

								// only update solution when new change
								// goes better
								boolean adjustResult = true;
								float newCost = 0;
								Aircraft normalAir = null;
								Aircraft cancelAir = null;
								if (newAircraft1.isCancel()) {
									normalAir = newAircraft1;
									cancelAir = aSolution.getAircraft(normalAir.getId(), normalAir.getType(), true,
											true);
								} else {
									cancelAir = newAircraft1;
									normalAir = aSolution.getAircraft(cancelAir.getId(), cancelAir.getType(), false,
											false);
								}
								adjustResult = adjustResult && adjustmentEngine.adjust(normalAir, cancelAir);
								newCost += normalAir.getCost();

								if (adjustResult) {
									if (newAircraft2.isCancel()) {
										normalAir = newAircraft2;
										cancelAir = aSolution.getAircraft(normalAir.getId(), normalAir.getType(), true,
												true);
									} else {
										cancelAir = newAircraft2;
										normalAir = aSolution.getAircraft(cancelAir.getId(), cancelAir.getType(), false,
												false);
									}

									adjustResult = adjustResult && adjustmentEngine.adjust(normalAir, cancelAir);
									newCost += normalAir.getCost();
								}

								if (adjustResult) {
									float oldCost = 0;
									if (!air1.isCancel())
										oldCost = oldCost + air1.getCost();
									if (!air2.isCancel())
										oldCost = oldCost + air2.getCost();

									if (newCost < oldCost) {
										if (BusinessDomain.validateFlights(air1, air2, newAircraft1, newAircraft2)) {
											if (aStragety.isAbortWhenImproved())
												isImproved = true;
											XiaMengAirlineSolution aBetterSolution = aSolution.clone();
											aBetterSolution.replaceOrAddNewAircraft(newAircraft1);
											aBetterSolution.replaceOrAddNewAircraft(newAircraft2);
											aBetterSolution.refreshCost(new BigDecimal(newCost - oldCost));
											neighboursResult.addSolution(aBetterSolution);
											logger.debug(
													"Better Solution exists! Method 3 : " + aBetterSolution.getCost());
										} else {
											logger.warn("Invalid aircraft after exchange " + air2.getId());
										}

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
									Flight air2Flightold = air2.getFlight(x);
									if (!air2Flightold.isAdjustable())
										continue;

									if (!flightAir1.getSourceAirPort().getId()
											.equals(air2Flightold.getSourceAirPort().getId()))
										continue;

									for (Flight destFlight : circuitFlightsAir1.get(flightAir1)) {
										Aircraft newAircraft1 = air1.clone();
										Aircraft newAircraft2 = air2.clone();
										Flight air1SourceFlight = newAircraft1.getFlight(u);
										Flight air1DestFlight = newAircraft1
												.getFlight(air1.getFlightChain().indexOf(destFlight));
										Flight air2Flight = newAircraft2.getFlight(x);

										newAircraft2.insertFlightChain(air1, flightAir1, destFlight, air2Flight, true);
										newAircraft1.removeFlightChain(air1SourceFlight, air1DestFlight);

										if (aStragety.isDebug()) {
											logger.debug("Method 4 After exchange ...");
											List<Flight> updateList1 = newAircraft1.getFlightChain();
											for (Flight aF : updateList1) {
												logger.debug(
														"Air " + newAircraft1.getId() + "Flights: " + aF.getFlightId());
											}
											List<Flight> updateList2 = newAircraft2.getFlightChain();
											for (Flight aF : updateList2) {
												logger.debug(
														"Air " + newAircraft2.getId() + "Flights: " + aF.getFlightId());
											}
											logger.debug("Method 4 Complete exchange ...");
										}

										// only update solution when new
										// change
										// goes better
										boolean adjustResult = true;
										float newCost = 0;
										Aircraft normalAir = null;
										Aircraft cancelAir = null;
										if (newAircraft1.isCancel()) {
											normalAir = newAircraft1;
											cancelAir = aSolution.getAircraft(normalAir.getId(), normalAir.getType(), true,
													true);
										} else {
											cancelAir = newAircraft1;
											normalAir = aSolution.getAircraft(cancelAir.getId(), cancelAir.getType(), false,
													false);
										}
										adjustResult = adjustResult && adjustmentEngine.adjust(normalAir, cancelAir);
										newCost += normalAir.getCost();

										if (adjustResult) {
											if (newAircraft2.isCancel()) {
												normalAir = newAircraft2;
												cancelAir = aSolution.getAircraft(normalAir.getId(), normalAir.getType(), true,
														true);
											} else {
												cancelAir = newAircraft2;
												normalAir = aSolution.getAircraft(cancelAir.getId(), cancelAir.getType(), false,
														false);
											}

											adjustResult = adjustResult && adjustmentEngine.adjust(normalAir, cancelAir);
											newCost += normalAir.getCost();
										}
										
										if (adjustResult) {
											float oldCost = 0;
											if (!air1.isCancel())
												oldCost = oldCost + air1.getCost();
											if (!air2.isCancel())
												oldCost = oldCost + air2.getCost();

											if (newCost < oldCost) {
												if (BusinessDomain.validateFlights(air1, air2, newAircraft1,
														newAircraft2)) {
													if (aStragety.isAbortWhenImproved())
														isImproved = true;
													XiaMengAirlineSolution aBetterSolution = aSolution.clone();
													aBetterSolution.replaceOrAddNewAircraft(newAircraft1);
													aBetterSolution.replaceOrAddNewAircraft(newAircraft2);
													aBetterSolution.refreshCost(new BigDecimal(newCost - oldCost));
													logger.debug("Better Solution exists! Method 4 : "
															+ aBetterSolution.getCost());
													neighboursResult.addSolution(aBetterSolution);
												} else {
													logger.warn("Invalid aircraft after exchange " + air2.getId());
												}

											}
										}
									}

								}
							}

						}

						// if aircraft2 flights is circuit, insert circuit
						// in front of flight u - method 5
						if (!air1.isCancel()) {
							Flight air1FlightOld = air1.getFlight(u);
							if (air1FlightOld.isAdjustable()) {
								for (int x = 0; x <= n - 1; x++) {
									Flight flightAir2 = air2.getFlight(x);
									if (circuitFlightsAir2.containsKey(flightAir2) && air1FlightOld.getSourceAirPort()
											.getId().equals(flightAir2.getSourceAirPort().getId())) {
										for (Flight destFlight : circuitFlightsAir2.get(flightAir2)) {
											Aircraft newAircraft1 = air1.clone();
											Aircraft newAircraft2 = air2.clone();
											Flight air2SourceFlight = newAircraft2.getFlight(x);
											Flight air2DestFlight = newAircraft2
													.getFlight(air2.getFlightChain().indexOf(destFlight));
											Flight air1Flight = newAircraft1.getFlight(u);

											newAircraft1.insertFlightChain(air2, flightAir2, destFlight, air1Flight,
													true);
											newAircraft2.removeFlightChain(air2SourceFlight, air2DestFlight);

											if (aStragety.isDebug()) {
												logger.debug("Method 5 After exchange ...");
												List<Flight> updateList1 = newAircraft1.getFlightChain();
												for (Flight aF : updateList1) {
													logger.debug("Air " + newAircraft1.getId() + " Flights:"
															+ aF.getFlightId());
												}
												List<Flight> updateList2 = newAircraft2.getFlightChain();
												for (Flight aF : updateList2) {
													logger.debug("Air " + newAircraft2.getId() + " Flights:"
															+ aF.getFlightId());
												}
												logger.debug("Method 5 Complete exchange ...");
											}

											// only update solution when new
											// change
											// goes better
											boolean adjustResult = true;
											float newCost = 0;
											Aircraft normalAir = null;
											Aircraft cancelAir = null;
											if (newAircraft1.isCancel()) {
												normalAir = newAircraft1;
												cancelAir = aSolution.getAircraft(normalAir.getId(), normalAir.getType(), true,
														true);
											} else {
												cancelAir = newAircraft1;
												normalAir = aSolution.getAircraft(cancelAir.getId(), cancelAir.getType(), false,
														false);
											}
											adjustResult = adjustResult && adjustmentEngine.adjust(normalAir, cancelAir);
											newCost += normalAir.getCost();

											if (adjustResult) {
												if (newAircraft2.isCancel()) {
													normalAir = newAircraft2;
													cancelAir = aSolution.getAircraft(normalAir.getId(), normalAir.getType(), true,
															true);
												} else {
													cancelAir = newAircraft2;
													normalAir = aSolution.getAircraft(cancelAir.getId(), cancelAir.getType(), false,
															false);
												}

												adjustResult = adjustResult && adjustmentEngine.adjust(normalAir, cancelAir);
												newCost += normalAir.getCost();
											}

											if (adjustResult) {
												float oldCost = 0;
												if (!air1.isCancel())
													oldCost = oldCost + air1.getCost();
												if (!air2.isCancel())
													oldCost = oldCost + air2.getCost();

												if (newCost < oldCost) {
													if (BusinessDomain.validateFlights(air1, air2, newAircraft1,
															newAircraft2)) {
														if (aStragety.isAbortWhenImproved())
															isImproved = true;
														XiaMengAirlineSolution aBetterSolution = aSolution.clone();
														aBetterSolution.replaceOrAddNewAircraft(newAircraft1);
														aBetterSolution.replaceOrAddNewAircraft(newAircraft2);
														aBetterSolution.refreshCost(new BigDecimal(newCost - oldCost));
														neighboursResult.addSolution(aBetterSolution);
														logger.debug("Better Solution exists! Method 5 : "
																+ aBetterSolution.getCost());
													} else {
														logger.warn("Invalid aircraft after exchange " + air2.getId());
													}

												}
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
								if (!aFlight.isAdjustable() || !bFlight.isAdjustable())
									continue;
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

									if (aStragety.isDebug()) {
										logger.debug("Method 6 After exchange ...");
										List<Flight> updateList1 = newAircraft1.getFlightChain();
										for (Flight aF : updateList1) {
											logger.debug(
													"Air " + newAircraft1.getId() + " Flights: " + aF.getFlightId());
										}
										List<Flight> updateList2 = newAircraft2.getFlightChain();
										for (Flight aF : updateList2) {
											logger.debug(
													"Air " + newAircraft2.getId() + " Flights: " + aF.getFlightId());
										}
										logger.debug("Method 6 Complete exchange ...");
									}

									// only update solution when new change
									// goes better
									boolean adjustResult = true;
									float newCost = 0;
									Aircraft normalAir = null;
									Aircraft cancelAir = null;
									if (newAircraft1.isCancel()) {
										normalAir = newAircraft1;
										cancelAir = aSolution.getAircraft(normalAir.getId(), normalAir.getType(), true,
												true);
									} else {
										cancelAir = newAircraft1;
										normalAir = aSolution.getAircraft(cancelAir.getId(), cancelAir.getType(), false,
												false);
									}
									adjustResult = adjustResult && adjustmentEngine.adjust(normalAir, cancelAir);
									newCost += normalAir.getCost();

									if (adjustResult) {
										if (newAircraft2.isCancel()) {
											normalAir = newAircraft2;
											cancelAir = aSolution.getAircraft(normalAir.getId(), normalAir.getType(), true,
													true);
										} else {
											cancelAir = newAircraft2;
											normalAir = aSolution.getAircraft(cancelAir.getId(), cancelAir.getType(), false,
													false);
										}

										adjustResult = adjustResult && adjustmentEngine.adjust(normalAir, cancelAir);
										newCost += normalAir.getCost();
									}
									

									if (adjustResult) {
										float oldCost = 0;
										if (!air1.isCancel())
											oldCost = oldCost + air1.getCost();
										if (!air2.isCancel())
											oldCost = oldCost + air2.getCost();

										if (newCost < oldCost) {
											if (BusinessDomain.validateFlights(air1, air2, newAircraft1,
													newAircraft2)) {
												if (aStragety.isAbortWhenImproved())
													isImproved = true;
												XiaMengAirlineSolution aBetterSolution = aSolution.clone();
												aBetterSolution.replaceOrAddNewAircraft(newAircraft1);
												aBetterSolution.replaceOrAddNewAircraft(newAircraft2);
												aBetterSolution.refreshCost(new BigDecimal(newCost - oldCost));
												neighboursResult.addSolution(aBetterSolution);
												logger.debug("Better Solution exists! Method 6 : "
														+ aBetterSolution.getCost());
											} else {
												logger.warn("Invalid aircraft after exchange " + air2.getId());
											}

										}
									}

								}
							}

						}

					}

				}

			}
			if (neighboursResult.hasSolution()) {
				aSolution = neighboursResult.selectASoluiton();
				neighboursResult.clear();
				System.out.println("Completed batch ... " + currentBatch + " Cost: " + aSolution.getCost());
			}
			aBatch = aDriver.getNextDriveForIterative();
		}

		return aSolution;
	}

	public void setupIterativeDriver(IterativeMethod aDriver) {
		this.aDriver = aDriver;

	}

	public void setupIterationStragety(OptimizerStragety aStragety) {
		this.aStragety = aStragety;

	}

	public IterativeMethod getaDriver() {
		return aDriver;
	}

	public AdjustmentEngine getAdjustmentEngine() {
		return adjustmentEngine;
	}

	public void setAdjustmentEngine(AdjustmentEngine adjustmentEngine) {
		this.adjustmentEngine = adjustmentEngine;
	}

	public RestrictedCandidateList getNeighboursResult() {
		return neighboursResult;
	}

}
