package xiaMengAirline.newBranch.GlobalOptimize;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import xiaMengAirline.backup.beans.AircraftBackup;
import xiaMengAirline.backup.beans.FlightBackup;
import xiaMengAirline.backup.beans.MatchedFlightBackup;
import xiaMengAirline.backup.beans.XiaMengAirlineSolutionBackup;
import xiaMengAirline.backup.utils.InitDataBackup;
import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.Flight;
import xiaMengAirline.newBranch.BasicObject.MatchedFlight;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;
import xiaMengAirline.newBranch.BusinessDomain.FlightAdjustableMethod.FlightAdjustMethodType;

public class GlobalSearchExchange implements GlobalOptimizer {
	private static final Logger logger = Logger.getLogger(GlobalSearchExchange.class);
	private IterativeMethod aDriver;

	@Override
	public void setupIterativeDriver(IterativeMethod aDriver) {
		this.aDriver = aDriver;

	}

	@Override
	public XiaMengAirlineSolution discoverBetterSolution(XiaMengAirlineSolution aSolution) {
		List<Aircraft> aBatch = aDriver.getNextDriveForIterative();
		while (aBatch != null) {
			RestrictedCandidateList neighboursResult = new RestrictedCandidateList();
			int currentBatch = aDriver.getCurrentIterationNumber();

			System.out.println("Processing batch ... " + currentBatch);
			for (Aircraft air1 : aBatch) {
				logger.debug("Processing first air " + air1.getId());
				List<Aircraft> air2CheckList = aSolution.getAllAircrafts();
				boolean isImproved = false;
				while (!isImproved) {
					// randomly select 2nd air
					Aircraft air2 = null;
					boolean isLast = false;

					do {
						if (air2CheckList.size() == 0) {
							isLast = true;
							break;
						}
						air2 = air2CheckList.remove(InitDataBackup.rndNumbers.nextInt(air2CheckList.size()));
					} while (air1.isCancel() && air2.isCancel() || air2.isFinalized());

					if (isLast) {
						System.out.println("Completed air ... " + air1.getId() + " on batch " + currentBatch);
						break;
					}

					// branch out solution
					List<String> processingAirs = new ArrayList<String>();
					processingAirs.add(air1.getId());
					processingAirs.add(air2.getId());
					XiaMengAirlineSolution smallSolution = aSolution.springOutNewSolution(processingAirs);
					logger.debug("Processing second air " + air2.getId() + " version " + smallSolution.getVersion());

					air1 = smallSolution.getAircraft(air1.getId(), air1.getType(), air1.isCancel(), true);
					air2 = smallSolution.getAircraft(air2.getId(), air2.getType(), air2.isCancel(), true);

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
							if (flightAir1.isAdjustable() && !flightAir1.isFinalized()
									&& circuitFlightsAir1.containsKey(flightAir1)) {

								for (Flight destFlight : circuitFlightsAir1.get(flightAir1)) {
									if (destFlight.isAdjustable() && !destFlight.isFinalized()) {
										XiaMengAirlineSolution newSmallSolution = smallSolution.clone();
										Aircraft newAir1 = newSmallSolution.getAircraft(air1.getId(), air1.getType(),
												false);
										int destPos = air1.getFlightChain().indexOf(destFlight);
										boolean isValid = false;
										for (int aPos=uu;aPos<=destPos;aPos++) {
											isValid = newAir1.getFlight(aPos).adjustFlight(newSmallSolution, FlightAdjustMethodType.CANCEL);
											if (!isValid)
												break;
										}


										logger.debug("Method 1 After exchange ...");
										List<Flight> updateList1 = newAir1.getFlightChain();
										for (Flight aF : updateList1) {
											logger.debug("Air  " + newAir1.getId() + " flight " + aF.getFlightId());
										}
										List<Flight> updateList2 = newSmallSolution.getCancelAircraft(newAir1, true).getFlightChain();
										for (Flight aF : updateList2) {
											logger.debug("Air cancelled " + newAir1.getId() + " flight "
													+ aF.getFlightId());
										}
										logger.debug("Method 1 Complete exchange ...");

										// only update solution when new change
										// goes better
										ArrayList<AircraftBackup> airList = new ArrayList<AircraftBackup>();
										airList.add(newAircraft1);
										airList.add(cancelledAir);

										ArrayList<AircraftBackup> airOldList = new ArrayList<AircraftBackup>();
										airOldList.add(air1);
										airOldList.add(
												bestSolution.getAircraft(air1.getId(), air1.getType(), true, true));

										XiaMengAirlineSolutionBackup aNewSolution = buildLocalSolution(airList);
										XiaMengAirlineSolutionBackup aOldSolution = buildLocalSolution(airOldList);

										if (aNewSolution.validateIter()) {
											BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

											if (deltaCost.longValue() < 0) {
												logger.info("Better Solution exists! Method 1 : " + deltaCost);
												isImproved = true;
												XiaMengAirlineSolutionBackup aBetterSolution = bestSolution.clone();
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
					}

					// Method 2
					if (!air2.isCancel()) {
						for (int xx = 0; xx <= n - 1; xx++) {
							// if aircraft2 flights is circuit, place it into
							// cancellation route - Method 2
							FlightBackup flightAir2 = air2.getFlight(xx);
							if (circuitFlightsAir2.containsKey(flightAir2)) {
								for (FlightBackup destFlight : circuitFlightsAir2.get(flightAir2)) {
									AircraftBackup newAircraft2 = air2.clone();
									AircraftBackup cancelledAir = bestSolution
											.getAircraft(air2.getId(), air2.getType(), true, true).clone();

									FlightBackup sFlight = newAircraft2.getFlight(xx);
									FlightBackup dFlight = newAircraft2
											.getFlight(air2.getFlightChain().indexOf(destFlight));

									cancelledAir.insertFlightChain(air2, flightAir2, destFlight,
											cancelledAir.getFlight(cancelledAir.getFlightChain().size() - 1), false);
									newAircraft2.removeFlightChain(sFlight, dFlight);

									logger.info("Method 2 After exchange ...");
									List<FlightBackup> updateList1 = newAircraft2.getFlightChain();
									for (FlightBackup aF : updateList1) {
										logger.info("Air " + newAircraft2.getId() + " Flights: " + aF.getFlightId());
									}
									List<FlightBackup> updateList2 = cancelledAir.getFlightChain();
									for (FlightBackup aF : updateList2) {
										logger.info("Air cancelled " + cancelledAir.getId() + " Flights: "
												+ aF.getFlightId());
									}
									logger.info("Method 2 Complete exchange ...");

									// only update solution when new change
									// goes better
									ArrayList<AircraftBackup> airList = new ArrayList<AircraftBackup>();
									airList.add(newAircraft2);
									airList.add(cancelledAir);

									ArrayList<AircraftBackup> airOldList = new ArrayList<AircraftBackup>();
									airOldList.add(air2);
									airOldList.add(bestSolution.getAircraft(air2.getId(), air2.getType(), true, true));

									XiaMengAirlineSolutionBackup aNewSolution = buildLocalSolution(airList);
									XiaMengAirlineSolutionBackup aOldSolution = buildLocalSolution(airOldList);

									if (aNewSolution.validateIter()) {
										BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

										if (deltaCost.longValue() < 0) {
											logger.info("Better Solution exists! Method 2 : " + deltaCost);
											isImproved = true;
											XiaMengAirlineSolutionBackup aBetterSolution = bestSolution.clone();
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
							List<MatchedFlightBackup> matchedList = matchedFlights.get(air1.getFlight(u));
							for (MatchedFlightBackup aMatched : matchedList) {
								AircraftBackup newAircraft1 = air1.clone();
								AircraftBackup newAircraft2 = air2.clone();
								FlightBackup air1SourceFlight = newAircraft1.getFlight(aMatched.getAir1SourceFlight());
								FlightBackup air1DestFlight = newAircraft1.getFlight(aMatched.getAir1DestFlight());
								FlightBackup air2SourceFlight = newAircraft2.getFlight(aMatched.getAir2SourceFlight());
								FlightBackup air2DestFlight = newAircraft2.getFlight(aMatched.getAir2DestFlight());

								newAircraft1.insertFlightChain(air2, air2.getFlight(aMatched.getAir2SourceFlight()),
										air2.getFlight(aMatched.getAir2DestFlight()), air1DestFlight, false);
								newAircraft2.insertFlightChain(air1, air1.getFlight(aMatched.getAir1SourceFlight()),
										air1.getFlight(aMatched.getAir1DestFlight()), air2DestFlight, false);

								// if cancel flights, need adjust its
								// alternative
								if (newAircraft1.isCancel()) {
									AircraftBackup airCancelAlt = newAircraft1.getAlternativeAircraft();
									if (airCancelAlt != null) {
										List<FlightBackup> movedFlights = newAircraft1
												.getSpecifiedFlightChain(air1SourceFlight, air1DestFlight);
										for (FlightBackup aFlight : movedFlights) {
											if (airCancelAlt.getFlightByFlightId(aFlight.getFlightId()) != null) {
												airCancelAlt.getFlightChain().remove(
														airCancelAlt.getFlightByFlightId(aFlight.getFlightId()));
											}
										}
									}
								}

								if (newAircraft2.isCancel()) {
									AircraftBackup airCancelAlt = newAircraft2.getAlternativeAircraft();
									if (airCancelAlt != null) {
										List<FlightBackup> movedFlights = newAircraft2
												.getSpecifiedFlightChain(air2SourceFlight, air2DestFlight);
										for (FlightBackup aFlight : movedFlights) {
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
								List<FlightBackup> updateList1 = newAircraft1.getFlightChain();
								for (FlightBackup aF : updateList1) {
									logger.info("Air  " + newAircraft1.getId() + "isCancel " + newAircraft1.isCancel()
											+ " flight " + aF.getFlightId());
								}
								List<FlightBackup> updateList2 = newAircraft2.getFlightChain();
								for (FlightBackup aF : updateList2) {
									logger.info("Air  " + newAircraft2.getId() + "isCancel " + newAircraft2.isCancel()
											+ " flight " + aF.getFlightId());
								}
								logger.info("Method 3 Complete exchange ...");

								// only update solution when new change
								// goes better
								ArrayList<AircraftBackup> airList = new ArrayList<AircraftBackup>();
								airList.add(newAircraft1);
								airList.add(newAircraft2);

								ArrayList<AircraftBackup> airOldList = new ArrayList<AircraftBackup>();
								airOldList.add(air1);
								airOldList.add(air2);

								XiaMengAirlineSolutionBackup aNewSolution = buildLocalSolution(airList);
								XiaMengAirlineSolutionBackup aOldSolution = buildLocalSolution(airOldList);
								if (aNewSolution.validateIter()) {

									BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

									if (deltaCost.longValue() < 0) {
										logger.info("Better Solution exists! Method 3 : " + deltaCost);
										isImproved = true;
										XiaMengAirlineSolutionBackup aBetterSolution = bestSolution.clone();
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
							FlightBackup flightAir1 = air1.getFlight(u);
							if (circuitFlightsAir1.containsKey(flightAir1)) {
								for (int x = 0; x <= n - 1; x++) {
									for (FlightBackup destFlight : circuitFlightsAir1.get(flightAir1)) {
										AircraftBackup newAircraft1 = air1.clone();
										AircraftBackup newAircraft2 = air2.clone();
										FlightBackup air1SourceFlight = newAircraft1.getFlight(u);
										FlightBackup air1DestFlight = newAircraft1
												.getFlight(air1.getFlightChain().indexOf(destFlight));
										FlightBackup air2Flight = newAircraft2.getFlight(x);

										newAircraft2.insertFlightChain(air1, flightAir1, destFlight, air2Flight, true);

										logger.info("Method 4 After exchange ...");
										List<FlightBackup> updateList1 = newAircraft1.getFlightChain();
										for (FlightBackup aF : updateList1) {
											logger.info(
													"Air " + newAircraft1.getId() + " Flights: " + aF.getFlightId());
										}
										List<FlightBackup> updateList2 = newAircraft2.getFlightChain();
										for (FlightBackup aF : updateList2) {
											logger.info(
													"Air  " + newAircraft2.getId() + " Flights: " + aF.getFlightId());
										}
										logger.info("Method 4 Complete exchange ...");

										// if cancel flights, need adjust its
										// alternative
										if (newAircraft1.isCancel()) {
											AircraftBackup airCancelAlt = newAircraft1.getAlternativeAircraft();
											if (airCancelAlt != null) {
												List<FlightBackup> movedFlights = newAircraft1
														.getSpecifiedFlightChain(air1SourceFlight, air1DestFlight);
												for (FlightBackup aFlight : movedFlights) {
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
										ArrayList<AircraftBackup> airList = new ArrayList<AircraftBackup>();
										airList.add(newAircraft1);
										airList.add(newAircraft2);

										ArrayList<AircraftBackup> airOldList = new ArrayList<AircraftBackup>();
										airOldList.add(air1);
										airOldList.add(air2);

										XiaMengAirlineSolutionBackup aNewSolution = buildLocalSolution(airList);
										XiaMengAirlineSolutionBackup aOldSolution = buildLocalSolution(airOldList);

										if (aNewSolution.validateIter()) {
											BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);
											if (deltaCost.longValue() < 0) {
												logger.info("Better Solution exists! Method 4 : " + deltaCost);
												isImproved = true;
												XiaMengAirlineSolutionBackup aBetterSolution = bestSolution.clone();
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
								FlightBackup flightAir2 = air2.getFlight(x);
								if (circuitFlightsAir2.containsKey(flightAir2)) {
									for (FlightBackup destFlight : circuitFlightsAir2.get(flightAir2)) {
										AircraftBackup newAircraft1 = air1.clone();
										AircraftBackup newAircraft2 = air2.clone();
										FlightBackup air2SourceFlight = newAircraft2.getFlight(x);
										FlightBackup air2DestFlight = newAircraft2
												.getFlight(air2.getFlightChain().indexOf(destFlight));
										FlightBackup air1Flight = newAircraft1.getFlight(u);

										newAircraft1.insertFlightChain(air2, flightAir2, destFlight, air1Flight, true);

										logger.info("Method 5 After exchange ...");
										List<FlightBackup> updateList1 = newAircraft1.getFlightChain();
										for (FlightBackup aF : updateList1) {
											logger.info(
													"Air  " + newAircraft1.getId() + " Flights: " + aF.getFlightId());
										}
										List<FlightBackup> updateList2 = newAircraft2.getFlightChain();
										for (FlightBackup aF : updateList2) {
											logger.info(
													"Air  " + newAircraft2.getId() + " Flights: " + aF.getFlightId());
										}
										logger.info("Method 5 Complete exchange ...");

										// if cancel flights, need adjust its
										// alternative
										if (newAircraft2.isCancel()) {
											AircraftBackup airCancelAlt = newAircraft2.getAlternativeAircraft();
											if (airCancelAlt != null) {
												List<FlightBackup> movedFlights = newAircraft2
														.getSpecifiedFlightChain(air2SourceFlight, air2DestFlight);
												for (FlightBackup aFlight : movedFlights) {
													if (airCancelAlt
															.getFlightByFlightId(aFlight.getFlightId()) != null) {
														airCancelAlt.getFlightChain().remove(airCancelAlt
																.getFlightByFlightId(aFlight.getFlightId()));
													}
												}
											}
										}
										newAircraft2.removeFlightChain(air2SourceFlight, air2DestFlight);

										// only update solution when new change
										// goes better
										ArrayList<AircraftBackup> airList = new ArrayList<AircraftBackup>();
										airList.add(newAircraft1);
										airList.add(newAircraft2);

										ArrayList<AircraftBackup> airOldList = new ArrayList<AircraftBackup>();
										airOldList.add(air1);
										airOldList.add(air2);

										XiaMengAirlineSolutionBackup aNewSolution = buildLocalSolution(airList);
										XiaMengAirlineSolutionBackup aOldSolution = buildLocalSolution(airOldList);

										if (aNewSolution.validateIter()) {
											BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

											if (deltaCost.longValue() < 0) {
												logger.info("Better Solution exists! Method 5 : " + deltaCost);
												isImproved = true;
												XiaMengAirlineSolutionBackup aBetterSolution = bestSolution.clone();
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
								FlightBackup aFlight = air1.getFlight(u);
								FlightBackup bFlight = air2.getFlight(x);
								if (aFlight.getSourceAirPort().getId().equals(bFlight.getSourceAirPort().getId())) {
									AircraftBackup newAircraft1 = air1.clone();
									AircraftBackup newAircraft2 = air2.clone();

									FlightBackup air1Flight = newAircraft1.getFlight(u);
									FlightBackup air2Flight = newAircraft2.getFlight(x);
									FlightBackup air1DestFlight = newAircraft1
											.getFlight(newAircraft1.getFlightChain().size() - 1);
									FlightBackup air2DestFlight = newAircraft2
											.getFlight(newAircraft2.getFlightChain().size() - 1);

									newAircraft1.insertFlightChain(air2, bFlight,
											air2.getFlight(air2.getFlightChain().size() - 1), air1Flight, true);
									newAircraft2.insertFlightChain(air1, aFlight,
											air1.getFlight(air1.getFlightChain().size() - 1), air2Flight, true);
									newAircraft1.removeFlightChain(air1Flight, air1DestFlight);
									newAircraft2.removeFlightChain(air2Flight, air2DestFlight);

									logger.info("Method 6 After exchange ...");
									List<FlightBackup> updateList1 = newAircraft1.getFlightChain();
									for (FlightBackup aF : updateList1) {
										logger.info("Air  " + newAircraft1.getId() + " Flights: " + aF.getFlightId());
									}
									List<FlightBackup> updateList2 = newAircraft2.getFlightChain();
									for (FlightBackup aF : updateList2) {
										logger.info("Air  " + newAircraft2.getId() + " Flights: " + aF.getFlightId());
									}
									logger.info("Method 6 Complete exchange ...");

									// only update solution when new change
									// goes better
									ArrayList<AircraftBackup> airList = new ArrayList<AircraftBackup>();
									airList.add(newAircraft1);
									airList.add(newAircraft2);

									ArrayList<AircraftBackup> airOldList = new ArrayList<AircraftBackup>();
									airOldList.add(air1);
									airOldList.add(air2);

									XiaMengAirlineSolutionBackup aNewSolution = buildLocalSolution(airList);
									XiaMengAirlineSolutionBackup aOldSolution = buildLocalSolution(airOldList);

									BigDecimal deltaCost = calculateDeltaCost(aNewSolution, aOldSolution);

									if (aNewSolution.validateIter()) {
										if (deltaCost.longValue() < 0) {
											logger.info("Better Solution exists! Method 6 : " + deltaCost);
											isImproved = true;
											XiaMengAirlineSolutionBackup aBetterSolution = bestSolution.clone();
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
				if (bestSolution.getCost().longValue() < 95 && (currentBatch % 3 == 0)) {
					Date dNow = new Date();
					SimpleDateFormat ft = new SimpleDateFormat("hh_mm_ss");
					XiaMengAirlineSolutionBackup aBetterOutput = bestSolution.reConstruct();
					aBetterOutput.refreshCost(true);
					aBetterOutput.generateOutput("batch_" + currentBatch + "_" + ft.format(dNow));
				}
			}
			aBatch = aDriver.getNextDriveForIterative();

		}

		return bestSolution;
	}

}
