package xiaMengAirline.searchEngine;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAcceptArrivalTime;
import xiaMengAirline.Exception.AirportNotAcceptDepartureTime;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.Exception.SolutionNotValid;
import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.AirPortClose;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.RegularAirPortClose;
import xiaMengAirline.beans.RequestTime;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.utils.InitData;

public class SelfSearch implements AdjustmentEngine {
	private static final Logger logger = Logger.getLogger(SelfSearch.class);
	private OptimizerStragety aStragety = null;

	public XiaMengAirlineSolution constructInitialSolution(XiaMengAirlineSolution mySolution)
			throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable,
			AircraftNotAdjustable, SolutionNotValid {
		// when construct initial solution, clone a new copy
		XiaMengAirlineSolution myNewSolution = mySolution.clone();

		List<Aircraft> airList = new ArrayList<Aircraft>(myNewSolution.getSchedule().values());
		float cost = 0;
		for (Aircraft aircraft : airList) {
			Aircraft aCancelled = myNewSolution.getAircraft(aircraft.getId(), aircraft.getType(), true, true);
			if (adjust(aircraft, aCancelled)) {
				logger.debug("Air " + aircraft.getId() + " adjusted with cost: " + aircraft.getCost());
				cost += aircraft.getCost();
			}

			else
				throw new SolutionNotValid(myNewSolution, "constructInitialSolution");
		}

		myNewSolution.setCost(new BigDecimal(String.valueOf(cost)));
		logger.debug("Total intial solution cost : " + myNewSolution.getCost());

		if (aStragety.isDebug()) {
			logger.debug("Inital solution in details:");
			for (Aircraft aircraft : airList) {
				logger.debug("Aircraft " + aircraft.getId() + " isCanceled? " + aircraft.isCancel());
				for (Flight aFlight : aircraft.getFlightChain()) {
					logger.debug("Flight " + aFlight.getFlightId());
					logger.debug("isCancelled? " + aFlight.isCanceled());
					logger.debug("source airport " + aFlight.getSourceAirPort().getId());
					logger.debug("dest airport" + aFlight.getDesintationAirport().getId());
					logger.debug("departure time " + aFlight.getDepartureTime());
					logger.debug("arrival time " + aFlight.getArrivalTime());
				}
			}
		}

		return myNewSolution;
	}

	@Override
	public boolean adjust(Aircraft orgAir, Aircraft itsCancelled) throws CloneNotSupportedException, ParseException {
		BigDecimal cost = new BigDecimal("0");
		boolean cancelFlg = false;

		// check if adjustable in cancel
		for (Flight aFlight : itsCancelled.getFlightChain()) {
			if (!aFlight.isAdjustable()) {
				logger.warn("Flight shall not adjust but cancelled after exchange air: " + itsCancelled.getId()
						+ " flightId:" + aFlight.getFlightId());
				return false;
			}
			cost.add(CostDomain.cancelCost(aFlight));
		}

		Aircraft altAir = orgAir;

		for (int i = 0; i < altAir.getFlightChain().size(); i++) {

			Flight flight = altAir.getFlightChain().get(i);
			if (!flight.isAdjustable()) {
				if (!flight.getPlannedAir().getId().equals(altAir.getId())) {
					return false;
				}
			} else {
				
				// last flight
				if (InitData.lastFlightMap.contains(flight.getFlightId())
						&& (!flight.getPlannedAir().getType().equals(altAir.getType()) || !flight.getPlannedFlight()
								.getDesintationAirport().getId().equals(flight.getDesintationAirport().getId()))) {
					return false;
				}

				// we set up schedule from the previous flight

				// joint flight we love joined flight
				int joinPos = BusinessDomain.getJointFlightPosition(flight);
				// if 1st flight of join
				if (joinPos == 1) {
					// if flight is already last,
					if (i == altAir.getFlightChain().size() - 1) {
						return false;
					}
					// then check if 2nd joined next to him
					Flight nextFlight = altAir.getFlight(i + 1);
					if (BusinessDomain.getJointFlight(flight).getFlightId() != nextFlight.getFlightId()) {
						// if 2nd flight not there, then it shall be cancelled
						if (!itsCancelled.hasFlight(nextFlight))
							return false;
						flight.setCanceled(false);
						flight.setFirstJoined(false);
						flight.setSecondJoined(false);
						nextFlight.setCanceled(false);
						nextFlight.setFirstJoined(false);
						nextFlight.setSecondJoined(false);
					} else {
						flight.setCanceled(false);
						flight.setFirstJoined(true);
						flight.setSecondJoined(false);
						nextFlight.setCanceled(false);
						nextFlight.setFirstJoined(false);
						nextFlight.setSecondJoined(true);
						// we move back destination, and think 2nd part shall be
						// treated as changed
						flight.setDesintationAirport(flight.getPlannedFlight().getDesintationAirport());
					}

				} else if (joinPos == 2) {
					// if 2nd flight of join
					// if flight already first
					if (i == 0)
						return false;

					//if first joined already setup, its my turn
					// then check if 1st joined prev to him
					Flight prevFlight = altAir.getFlight(i - 1);
					if (prevFlight.isFirstJoined() 
							&& flight.isSecondJoined()) {
						if (prevFlight.isCanceled()) {
							//1st flight already cancelled
							flight.setCanceled(false);
							flight.setFirstJoined(false);
							flight.setSecondJoined(false);
						} else if (flight.isCanceled()) {
							//2nd flight already processed & cancelled as connected flight
							//already costed
								continue;
						} else {
							//2nd flight still valid join
							flight.setCanceled(false);
							flight.setSecondJoined(true);
							flight.setFirstJoined(false);								
						}

					} else {
						//it sounds first joined already cancelled
						if (BusinessDomain.getJointFlight(prevFlight).getFlightId() != flight.getFlightId()) {
							// if 1st flight not there, then it shall be cancelled
							if (!itsCancelled.hasFlight(prevFlight))
								return false;
							else {
								//1st flight already cancelled
								flight.setCanceled(false);
								flight.setFirstJoined(false);
								flight.setSecondJoined(false);
							}
														
						} else {
							//then 1st flight must be cancelled
							logger.warn("Joined flight looks strage " + prevFlight.getFlightId() + " : " + flight.getFlightId());
							flight.setCanceled(false);
							flight.setFirstJoined(false);
							flight.setSecondJoined(false);
						}
					}
				} else {
					//reset normal flights
					flight.setCanceled(false);
					flight.setFirstJoined(false);
					flight.setSecondJoined(false);
				}

				// we need setup dep/arr time
				// we prefer fly as early as possible even different with
				// original schedule
				Flight prevFligt = null;
				Date initialArrivalTime = null;
				if (i == 0) {
					// To do: this might be issue, first flight adjustable?
					logger.warn("First flight is adjustable " + flight.getFlightId());
					continue;
				} else if (i == 1 || !altAir.getFlight(i - 1).isAdjustable()) {
					prevFligt = altAir.getFlight(i - 1);
					initialArrivalTime = altAir.getFlight(i - 1).getArrivalTime();
				}

				int currentGroundingTime = BusinessDomain.getGroundingTime(prevFligt, flight);
				Date earliestDepartureTime = BusinessDomain.addMinutes(initialArrivalTime, currentGroundingTime);
				Date selectedDepartureTime;
				if (flight.getPlannedFlight().getDepartureTime().before(earliestDepartureTime)) {
					selectedDepartureTime = earliestDepartureTime;
				} else {
					// we need see how long we like grounding, I dont want to
					// ground too long to impact others
					Date newDepartureTime = BusinessDomain.addMinutes(initialArrivalTime, aStragety.getMaxGrounding());
					if (newDepartureTime.before(earliestDepartureTime))
						selectedDepartureTime = earliestDepartureTime;
					else if (newDepartureTime.after(flight.getPlannedFlight().getDepartureTime())) {
						selectedDepartureTime = flight.getPlannedFlight().getDepartureTime();
					} else
						selectedDepartureTime = newDepartureTime;
				}
				// check if this departure time feasible
				RequestTime myRequest = new RequestTime();
				myRequest.setArrivalTime(null);
				myRequest.setDepartureTime(selectedDepartureTime);
				RequestTime feasibleRequest = prevFligt.getDesintationAirport().requestAirport(myRequest,
						currentGroundingTime);
				if (feasibleRequest != null) {
					// let's try suggested time
					flight.setDepartureTime(feasibleRequest.getDepartureTime());
					
					try {
						flight.setArrivalTime(flight.calcuateNextArrivalTime());
					} catch (FlightDurationNotFound e) {
						logger.warn("Unable to find flight duration for " + flight.getFlightId());
						return false;
					}
				} else {
					// accepted
					flight.setDepartureTime(selectedDepartureTime);
					try {
						flight.setArrivalTime(flight.calcuateNextArrivalTime());
					} catch (FlightDurationNotFound e) {
						logger.warn("Unable to find flight duration for " + flight.getFlightId());
						return false;
					}
				}
				// cancel if not valid move
				boolean isTyphoon = false;
				if (feasibleRequest != null) {
					isTyphoon = feasibleRequest.isIsTyphoon();
				}
				if (flight.getDepartureTime().before(flight.getPlannedFlight().getDepartureTime())) {
					if (BusinessDomain.isValidEarlier(flight, isTyphoon)) {
						// all good, let it be
						;
					} else {
						flight.setCanceled(true);
					}
				} else {
					if (BusinessDomain.isValidDelay(flight)) {
						// all good, let it be
						;
					} else {
						flight.setCanceled(true);
					}
				}

				// now continue on arrival time
				if (!flight.isCanceled()) {
					Flight nextFlight = altAir.getFlight(i + 1);
					currentGroundingTime = BusinessDomain.getGroundingTime(flight, nextFlight);
					myRequest = new RequestTime();
					myRequest.setArrivalTime(flight.getArrivalTime());
					myRequest.setDepartureTime(null);
					feasibleRequest = flight.getDesintationAirport().requestAirport(myRequest, currentGroundingTime);
					if (feasibleRequest != null) {
						// how about joined option?
						// if first join, then we can cancel the 2nd join
						if (flight.isFirstJoined() && nextFlight.isSecondJoined()) {
							// check if we can count it as connect flight cost
							if (BusinessDomain.isValidConnectedForJoin(flight, nextFlight)) {
								// now check if new airport works fine
								nextFlight.setCanceled(true);
								flight.setDesintationAirport(nextFlight.getDesintationAirport());
								try {
									flight.calcuateNextArrivalTime();
								} catch (FlightDurationNotFound e) {
									logger.warn("Unable to find flight duration for " + flight.getFlightId());
									return false;
								}
								myRequest = new RequestTime();
								myRequest.setArrivalTime(flight.getArrivalTime());
								myRequest.setDepartureTime(null);
								feasibleRequest = flight.getDesintationAirport().requestAirport(myRequest,
										Flight.GroundingTime);
								if (feasibleRequest != null) {
									// To do, maybe we can try his suggestion again
									flight.setCanceled(true);
								} 
							} else
								flight.setCanceled(true);
						} else {
							flight.setCanceled(true);
						}
					}					
				}


				// now cost it
				if (flight.isCanceled()) {
					cost.add(CostDomain.cancelCost(flight));
					cancelFlg = true;
				}
					
				else {
					//delay cost
					if (flight.getDepartureTime().after(flight.getPlannedFlight().getDepartureTime()))
						cost.add(CostDomain.delayCost(flight));
					else
						cost.add(CostDomain.earlierCost(flight));
					// change air
					if (!flight.getPlannedAir().getId().equals(altAir.getId())) {
						cost.add(CostDomain.changeAirCost(flight));
					}
					//connect flight cost
					if (flight.isFirstJoined() && !flight.isCanceled()) {
						Flight nextFlight = altAir.getFlight(i + 1);
						if (nextFlight.isCanceled() && nextFlight.isSecondJoined()) {
							cost.add(CostDomain.connectedFlightCost());
						}
					}
				}
			}
		}

			

		if (cancelFlg || !itsCancelled.getFlightChain().isEmpty()) {
			cost = cost.add(new BigDecimal("5000"));
		}

		altAir.setCost(cost.floatValue());
		return true;
	}

	public List<Aircraft> adjustAircraft(Aircraft originalAir, int startIndex, Aircraft originalCancelAir)
			throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable,
			AircraftNotAdjustable {
		Aircraft thisAc = originalAir.clone();
		// original cancel air
		Aircraft thisAcCancel = originalCancelAir.clone();
		HashMap<Integer, Aircraft> forkList = new HashMap<Integer, Aircraft>();

		// loop until all flight sorted
		Aircraft aircraft = originalAir.clone();
		Aircraft aircraftCancel = thisAcCancel.clone();
		boolean isFinish = false;
		int infinitLoopCnt = 0;
		if (startIndex == 0) {
			Flight firstFlight = aircraft.getFlightChain().get(0);
			RequestTime firstFlightTime = new RequestTime();

			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			Date startDate = df.parse("01/01/1970");
			firstFlightTime.setArrivalTime(startDate);
			firstFlightTime.setDepartureTime(firstFlight.getDepartureTime());

			RequestTime newfirstFlightTime = firstFlight.getSourceAirPort().requestAirport(firstFlightTime,
					firstFlight.getGroundingTime(0, 1));
			if (newfirstFlightTime != null && newfirstFlightTime.getDepartureTime() != null) {
				if (!firstFlight.isInternationalFlight() && getMinuteDifference(firstFlight.getDepartureTime(),
						newfirstFlightTime.getDepartureTime()) > 360) {
					for (AirPortClose aClose : firstFlight.getSourceAirPort().getCloseSchedule()) {
						if (firstFlight.getDepartureTime().compareTo(aClose.getStartTime()) > 0
								&& firstFlight.getDepartureTime().compareTo(aClose.getEndTime()) < 0) {
							firstFlight.setDepartureTime(aClose.getEndTime());
						}
					}
				} else {
					firstFlight.setDepartureTime(newfirstFlightTime.getDepartureTime());
				}
			}
		}
		while (!isFinish) {
			List<Flight> flights = aircraft.getFlightChain();
			try {
				boolean adjusted = aircraft.adjustFlightTime(startIndex);
				isFinish = true;
				if (startIndex == 0 && !adjusted) {
					originalAir.setAlternativeAircraft(null);
					originalCancelAir.setAlternativeAircraft(null);
				}
			} catch (AirportNotAcceptArrivalTime anaat) {
				Flight thisFlight = anaat.getaFlight();
				RequestTime avaliableTime = anaat.getAvailableTime();
				int flightIndex = aircraft.getFlightIndexByFlightId(thisFlight.getFlightId());

				if (avaliableTime.isIsTyphoon() && isJointFlight(thisFlight) && getJointFlight(thisFlight) != null
						&& !thisFlight.isInternationalFlight() && !getJointFlight(thisFlight).isInternationalFlight()) {
					Aircraft forkAir = aircraft.clone();
					Flight firstFlight = forkAir.getFlightChain().get(flightIndex);
					Flight secondFlight = forkAir.getFlightChain().get(flightIndex + 1);
					firstFlight.setDesintationAirport(secondFlight.getDesintationAirport());
					firstFlight.setArrivalTime(addMinutes(firstFlight.getDepartureTime(),
							getJointFlightDuration(firstFlight, secondFlight, forkAir)));
					forkAir.moveToDropOut(secondFlight);
					forkList.put(flightIndex, forkAir);
				}

				if (isEligibalDelay(getPlannedArrival(thisFlight), avaliableTime.getArrivalTime(),
						thisFlight.isInternationalFlight())) {
					Date tempDeparture = addMinutes(avaliableTime.getArrivalTime(),
							(int) getMinuteDifference(getPlannedDeparture(thisFlight), getPlannedArrival(thisFlight)));
					Date adjustedDeparture = getValidDeparture(tempDeparture, thisFlight.getSourceAirPort());

					if (isEligibalDelay(getPlannedDeparture(thisFlight), adjustedDeparture,
							thisFlight.isInternationalFlight())) {
						thisFlight.setDepartureTime(adjustedDeparture);
						thisFlight.calcuateNextArrivalTime();
						if (flightIndex < flights.size() - 1) {
							flights.get(flightIndex + 1).setDepartureTime(addMinutes(avaliableTime.getDepartureTime(),
									flights.get(flightIndex + 1).getGroundingTime(flightIndex, flightIndex + 1)));
						}
					} else {
						try {
							if (flightIndex != flights.size() - 1) {
								aircraft = cancelFlight(aircraft, aircraftCancel, flightIndex);
								if (aircraft == null) {
									print("Invalid aircraft: AicraftId " + thisAc.getId());
									throw new AircraftNotAdjustable(aircraft);
								}
							} else {
								throw new AircraftNotAdjustable(aircraft);
							}
						} catch (Exception e) {
							// e.printStackTrace();
							print("Invalid aircraft: AicraftId " + thisAc.getId());
							throw new AircraftNotAdjustable(aircraft);
						}
					}
					startIndex = flightIndex;
				} else {
					try {
						if (flightIndex != flights.size() - 1) {
							aircraft = cancelFlight(aircraft, aircraftCancel, flightIndex);
							if (aircraft == null) {
								print("Invalid aircraft: AicraftId " + thisAc.getId());
								throw new AircraftNotAdjustable(aircraft);
							}
						} else {
							throw new AircraftNotAdjustable(aircraft);
						}
					} catch (Exception e) {
						// e.printStackTrace();
						print("Invalid aircraft: AicraftId " + thisAc.getId());
						throw new AircraftNotAdjustable(aircraft);
					}
					startIndex = flightIndex;
				}
			} catch (AirportNotAcceptDepartureTime anadt) {
				Flight thisFlight = anadt.getaFlight();
				int flightIndex = aircraft.getFlightIndexByFlightId(thisFlight.getFlightId());
				if (isJointFlight(thisFlight) && getJointFlight(thisFlight) == null
						&& !thisFlight.isInternationalFlight()
						&& !flights.get(flightIndex - 1).isInternationalFlight()) {
					Aircraft forkAir = aircraft.clone();
					Flight firstFlight = forkAir.getFlightChain().get(flightIndex - 1);
					Flight secondFlight = forkAir.getFlightChain().get(flightIndex);
					firstFlight.setDesintationAirport(secondFlight.getDesintationAirport());
					firstFlight.setArrivalTime(addMinutes(firstFlight.getDepartureTime(),
							getJointFlightDuration(firstFlight, secondFlight, forkAir)));
					forkAir.moveToDropOut(secondFlight);
					forkList.put(flightIndex, forkAir);
				}

				try {
					if (flightIndex != flights.size() - 1) {
						aircraft = cancelFlight(aircraft, aircraftCancel, flightIndex);
						if (aircraft == null) {
							print("Invalid aircraft: AicraftId " + thisAc.getId());
							throw new AircraftNotAdjustable(aircraft);
						}
					} else {
						throw new AircraftNotAdjustable(aircraft);
					}
				} catch (Exception e) {
					// e.printStackTrace();
					print("Invalid aircraft: AicraftId " + thisAc.getId());
					throw new AircraftNotAdjustable(aircraft);
				}
				startIndex = flightIndex;
			} catch (AirportNotAvailable ana) {
				Flight thisFlight = ana.getaFlight();
				int flightIndex = aircraft.getFlightIndexByFlightId(thisFlight.getFlightId());
				try {
					if (flightIndex != flights.size() - 1) {
						aircraft = cancelFlight(aircraft, aircraftCancel, flightIndex);
						if (aircraft == null) {
							print("Invalid aircraft: AicraftId " + thisAc.getId());
							throw new AircraftNotAdjustable(aircraft);
						}
					} else {
						throw new AircraftNotAdjustable(aircraft);
					}
				} catch (Exception e) {
					// e.printStackTrace();
					print("Invalid aircraft: AicraftId " + thisAc.getId());
					throw new AircraftNotAdjustable(aircraft);
				}
				startIndex = flightIndex;
			} catch (Exception e) {
				// invalid
				// e.printStackTrace();
				throw new AircraftNotAdjustable(aircraft);
			}

			if (infinitLoopCnt > 1) {
				// last flight cannot be adjusted, invalid flight chain
				throw new AircraftNotAdjustable(aircraft);
			}

			if (startIndex == flights.size() - 1) {
				infinitLoopCnt++;
			}
		}
		double thisCost = getAircraftCost(aircraft, aircraftCancel);

		Iterator<Entry<Integer, Aircraft>> it = forkList.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Aircraft> pair = (Map.Entry<Integer, Aircraft>) it.next();
			int nextStartIndex = pair.getKey();
			Aircraft nextForkAc = pair.getValue();
			Aircraft newReturnAc = null;
			Aircraft newReturnAcCancel = null;
			List<Aircraft> newReturnPair = adjustAircraft(nextForkAc, nextStartIndex, aircraftCancel);
			for (Aircraft ac : newReturnPair) {
				if (ac.isCancel()) {
					newReturnAcCancel = ac;
				} else {
					newReturnAc = ac;
				}
			}
			double itCost = getAircraftCost(newReturnAc, newReturnAcCancel);
			if (thisCost > itCost) {
				aircraft = newReturnAc;
				aircraftCancel = newReturnAcCancel;
				// double ensure, no duplicated
				for (Flight aFlight : aircraft.getFlightChain()) {
					if (aircraftCancel.getFlightByFlightId(aFlight.getFlightId()) != null) {
						aircraftCancel.getFlightChain()
								.remove(aircraftCancel.getFlightByFlightId(aFlight.getFlightId()));
					}
				}
				for (Flight aFlight : aircraft.getDropOutList()) {
					if (aircraftCancel.getFlightByFlightId(aFlight.getFlightId()) != null) {
						aircraftCancel.getFlightChain()
								.remove(aircraftCancel.getFlightByFlightId(aFlight.getFlightId()));
					}
				}
				thisCost = itCost;
			}
		}

		aircraft.setAlternativeAircraft(null);
		aircraftCancel.setAlternativeAircraft(null);
		originalAir.setAlternativeAircraft(aircraft);
		originalCancelAir.setAlternativeAircraft(aircraftCancel);

		List<Aircraft> aircraftReturn = new ArrayList<Aircraft>();
		aircraftReturn.add(aircraft);
		aircraftReturn.add(aircraftCancel);
		return aircraftReturn;
	}

	public void print(String str) {
		logger.info(str);
	}

	// cancel a flight
	public Aircraft cancelFlight(Aircraft aircraft, Aircraft aircraftCancel, int flightIndex)
			throws FlightDurationNotFound, CloneNotSupportedException, ParseException {
		List<Flight> flights = aircraft.getFlightChain();
		Flight thisFlight = flights.get(flightIndex);
		Flight newFlight = new Flight();
		RequestTime tempFt = new RequestTime();
		tempFt.setArrivalTime(flights.get(flightIndex - 1).getArrivalTime());
		Date tempDep = addMinutes(flights.get(flightIndex - 1).getArrivalTime(),
				newFlight.getGroundingTime(flightIndex - 1, flightIndex));
		tempFt.setDepartureTime(tempDep);
		tempFt = thisFlight.getSourceAirPort().requestAirport(tempFt,
				newFlight.getGroundingTime(flightIndex - 1, flightIndex));
		if (tempFt == null) {
			newFlight.setDepartureTime(tempDep);
		} else {
			if (tempFt.getDepartureTime() == null) {
				newFlight.setDepartureTime(tempDep);
			} else {
				newFlight.setDepartureTime(tempFt.getDepartureTime());
			}
		}

		newFlight.setSourceAirPort(thisFlight.getSourceAirPort());

		HashMap<Integer, Flight> indexFlightPair = createNewFlight(newFlight, flightIndex, aircraft);
		if (indexFlightPair != null) {
			Map.Entry<Integer, Flight> entry = indexFlightPair.entrySet().iterator().next();
			int cancelFlightEndIndex = entry.getKey();
			newFlight = entry.getValue();
			List<Integer> removeFlightIndeces = new ArrayList<Integer>();

			for (int cancelIndex = flightIndex; cancelIndex < cancelFlightEndIndex + 1; cancelIndex++) {
				if (flights.get(cancelIndex).getFlightId() <= InitData.plannedMaxFligthId) {
					aircraftCancel.addFlight(flights.get(cancelIndex));
				}
				removeFlightIndeces.add(cancelIndex);
			}
			aircraft.removeFlightChain(removeFlightIndeces);
			if (!newFlight.getSourceAirPort().getId().equals(newFlight.getDesintationAirport().getId())) {
				aircraft.addFlight(flightIndex, newFlight);
			}
			return aircraft;
		}
		return null;
	}

	// get joint flight's flight duration
	public int getJointFlightDuration(Flight firstFlight, Flight secondFlight, Aircraft aircraft) {
		String searchKey = aircraft.getId() + "_" + firstFlight.getSourceAirPort() + "_"
				+ secondFlight.getDesintationAirport();
		if (InitData.fightDurationMap.containsKey(searchKey)) {
			return InitData.fightDurationMap.get(searchKey);
		} else {
			Double flightTime = getMinuteDifference(firstFlight.getArrivalTime(), firstFlight.getDepartureTime())
					+ getMinuteDifference(secondFlight.getArrivalTime(), secondFlight.getArrivalTime());
			return flightTime.intValue();
		}
	}

	// get joint flight
	public Flight getJointFlight(Flight flight) {
		return InitData.jointFlightMap.get(flight.getFlightId());
	}

	public boolean isJointFlight(Flight flight) {
		return InitData.jointFlightMap.keySet().contains(flight.getFlightId()) ? true : false;
	}

	// create new flight
	public HashMap<Integer, Flight> createNewFlight(Flight prototypeFlight, int flightPosition, Aircraft aircraft)
			throws FlightDurationNotFound, CloneNotSupportedException, ParseException {
		HashMap<Integer, Flight> newIndexAndFlight = createEligibalFlight(aircraft.getFlightChain(), flightPosition,
				aircraft, prototypeFlight);
		if (newIndexAndFlight != null) {
			Map.Entry<Integer, Flight> entry = newIndexAndFlight.entrySet().iterator().next();
			int destIndex = entry.getKey();
			Flight newFlight = entry.getValue();
			newFlight.setFlightId(getNextFlightId());
			newFlight.setAssignedAir(aircraft);

			HashMap<Integer, Flight> indexAndFlight = new HashMap<Integer, Flight>();
			indexAndFlight.put(destIndex, newFlight);
			return indexAndFlight;
		}
		return null;
	}

	// get next normal airport
	public HashMap<Integer, Flight> createEligibalFlight(List<Flight> flightChain, int currentFlightIndex, Aircraft ac,
			Flight newFlight) throws CloneNotSupportedException, ParseException {
		Flight thisFlight = newFlight;
		for (int i = currentFlightIndex + 1; i < flightChain.size(); i++) {
			Flight nextFlight = flightChain.get(i);
			AirPort destAirport = nextFlight.getSourceAirPort();
			// if new flight is a chain cancel
			if (thisFlight.getSourceAirPort().getId().equals(destAirport.getId())) {
				thisFlight.setDesintationAirport(destAirport);
				Flight lastFlight = flightChain.get(currentFlightIndex - 1);
				RequestTime tempFlightTime = new RequestTime();
				tempFlightTime.setArrivalTime(lastFlight.getArrivalTime());
				tempFlightTime.setDepartureTime(nextFlight.getDepartureTime());
				if (nextFlight.getSourceAirPort().requestAirport(tempFlightTime,
						nextFlight.getGroundingTime(i - 1, i)) == null) {
					tempFlightTime.setArrivalTime(nextFlight.getArrivalTime());
					if (i < flightChain.size() - 2) {
						tempFlightTime.setDepartureTime(flightChain.get(i + 1).getDepartureTime());
						if (nextFlight.getDesintationAirport().requestAirport(tempFlightTime,
								nextFlight.getGroundingTime(i, i + 1)) == null) {
							if (!isValidParking(thisFlight.getArrivalTime(), nextFlight.getDepartureTime(),
									thisFlight.getDesintationAirport())) {
								continue;
							}
							HashMap<Integer, Flight> destIndexAndNewFight = new HashMap<Integer, Flight>();
							destIndexAndNewFight.put(i - 1, thisFlight);
							return destIndexAndNewFight;
						} else {
							continue;
						}
					} else {
						if (!isValidParking(thisFlight.getArrivalTime(), nextFlight.getDepartureTime(),
								thisFlight.getDesintationAirport())) {
							continue;
						}
						HashMap<Integer, Flight> destIndexAndNewFight = new HashMap<Integer, Flight>();
						destIndexAndNewFight.put(i - 1, thisFlight);
						return destIndexAndNewFight;
					}
				}
				continue;
			}

			// international flight is not eligible
			if (isInternational(thisFlight.getSourceAirPort().getId(), destAirport.getId())) {
				continue;
			}
			// aircraft constraint
			if (!isEligibalAircraft(ac, thisFlight.getSourceAirPort(), destAirport)) {
				continue;
			}
			long flightTime = getFlightTime(thisFlight.getSourceAirPort().getId(), destAirport.getId(), ac);
			if (flightTime > 0) {
				thisFlight.setArrivalTime(addMinutes(thisFlight.getDepartureTime(), flightTime));
				thisFlight.setDesintationAirport(destAirport);
				thisFlight.setPlannedFlight(thisFlight.clone());

				List<Flight> newFlightChain = new ArrayList<Flight>();
				newFlightChain.add(thisFlight);
				newFlightChain.add(nextFlight);
				Aircraft newAircraft = ac.clone();
				newAircraft.setFlightChain(newFlightChain);
				try {
					newAircraft.adjustFlightTime(0);
					HashMap<Integer, Flight> destIndexAndNewFight = new HashMap<Integer, Flight>();
					destIndexAndNewFight.put(i - 1, thisFlight);
					if (!isValidParking(thisFlight.getArrivalTime(), nextFlight.getDepartureTime(),
							thisFlight.getDesintationAirport())) {
						continue;
					}
					return destIndexAndNewFight;
				} catch (AirportNotAcceptArrivalTime anaat) {
					RequestTime avaliableTime = anaat.getAvailableTime();
					if (getMinuteDifference(avaliableTime.getArrivalTime(), getPlannedArrival(thisFlight)) < 24 * 60) {
						if (!isValidParking(avaliableTime.getArrivalTime(), avaliableTime.getDepartureTime(),
								thisFlight.getDesintationAirport())) {
							continue;
						}
						thisFlight.setArrivalTime(avaliableTime.getArrivalTime());
						thisFlight.setDepartureTime(addMinutes(thisFlight.getArrivalTime(), -flightTime));
						HashMap<Integer, Flight> destIndexAndNewFight = new HashMap<Integer, Flight>();
						destIndexAndNewFight.put(i - 1, thisFlight);
						return destIndexAndNewFight;
					} else {
						continue;
					}
				} catch (Exception e) {
					continue;
				}
			}
		}

		// unable to find next flight destination
		print("New flight unable to find next available airport, flightID"
				+ flightChain.get(currentFlightIndex).getFlightId());
		logger.info("New flight unable to find next available airport, flightID"
				+ flightChain.get(currentFlightIndex).getFlightId());
		return null;
	}

	// tell if a flight is international between two airport
	public boolean isInternational(String airport1, String airport2) {
		if (InitData.domesticAirportList.contains(airport1) && InitData.domesticAirportList.contains(airport2)) {
			return false;
		}
		return true;
	}

	public boolean isEligibalAircraft(Aircraft aircraft, AirPort sourceAir, AirPort destAir) {
		String searchKey = aircraft.getId() + "_" + sourceAir.getId() + "_" + destAir.getId();
		return InitData.airLimitationList.contains(searchKey) ? false : true;
	}

	// get flight time between two airports
	public long getFlightTime(String airport1Id, String airport2Id, Aircraft aircraft) {
		String searchKey = aircraft.getType();
		searchKey += "_";
		searchKey += airport1Id;
		searchKey += "_";
		searchKey += airport2Id;

		if (InitData.fightDurationMap.containsKey(searchKey)) {
			long flightTime = InitData.fightDurationMap.get(searchKey);
			return flightTime;
		} else {
			return 0;
		}

	}

	// add minutes to date
	public Date addMinutes(Date date, long minutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		long t = cal.getTimeInMillis();
		return new Date(t + (minutes * 60000));
	}

	// flight is eligible for delay
	public boolean isEligibalDelay(Date planTime, Date adjustTime, boolean isInternational) {
		if (getMinuteDifference(adjustTime, planTime) > (isInternational
				? Aircraft.INTERNATIONAL_MAXIMUM_DELAY_TIME * 60 : Aircraft.DOMESTIC_MAXIMUM_DELAY_TIME * 60)) {
			return false;
		}

		return true;
	}

	// get time difference between time1 and time2, time1 > time2 is positive
	// otherwise negative
	public double getHourDifference(Date time1, Date time2) {
		return (time1.getTime() - time2.getTime()) / (1000 * 60 * 60);
	}

	public double getMinuteDifference(Date time1, Date time2) {
		return (time1.getTime() - time2.getTime()) / (1000 * 60);
	}

	// get original arrival time
	public Date getPlannedArrival(Flight flight) {
		return flight.getPlannedFlight().getArrivalTime();
	}

	// get original departure time
	public Date getPlannedDeparture(Flight flight) {
		return flight.getPlannedFlight().getDepartureTime();
	}

	// a is later than b
	public boolean isLaterThan(Date time1, Date time2) {
		if (time1.compareTo(time2) > 0) {
			return true;
		} else {
			return false;
		}
	}

	// a is earlier than b
	public boolean isEarlierThan(Date time1, Date time2) {
		if (time1.compareTo(time2) < 0) {
			return true;
		} else {
			return false;
		}
	}

	// get next flight id
	public int getNextFlightId() {
		int maxFlightId = InitData.maxFligthId;
		InitData.maxFligthId = maxFlightId + 1;
		return maxFlightId + 1;
	}

	// get last flight index
	public int getLastFlightIndex(List<Integer> canceledFlightIDs, int thisIndex) {
		if (thisIndex == 0)
			return 0;
		for (int i = thisIndex - 1; i > -1; i--) {
			if (!canceledFlightIDs.contains(i)) {
				return i;
			}
		}
		return 0;
	}

	// get aircraft cost for local comparison
	public double getAircraftCost(Aircraft ac, Aircraft acCancel) {
		XiaMengAirlineSolution solution = new XiaMengAirlineSolution();
		solution.replaceOrAddNewAircraft(ac);
		solution.replaceOrAddNewAircraft(acCancel);
		solution.refreshCost(false);
		return solution.getCost().doubleValue();
	}

	public Date getValidDeparture(Date departureTime, AirPort airport) throws ParseException {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (departureTime.compareTo(aClose.getStartTime()) > 0
						&& departureTime.compareTo(aClose.getEndTime()) < 0) {
					return aClose.getEndTime();
				}
			}
		}
		for (RegularAirPortClose aClose : airport.getRegularCloseSchedule()) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(departureTime);
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();

			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			Date aCloseDate = formatter2.parse(aDateC);
			Date aOpenDate = formatter2.parse(aDateO);

			if (departureTime.after(aCloseDate) && departureTime.before(aOpenDate)) {
				return aOpenDate;
			}
		}
		return departureTime;
	}

	public boolean isValidParking(Date arrivalTime, Date departureTime, AirPort airport) {
		if (arrivalTime != null && arrivalTime != null) {
			for (AirPortClose aClose : airport.getCloseSchedule()) {
				if (arrivalTime.compareTo(aClose.getStartTime()) <= 0
						&& departureTime.compareTo(aClose.getEndTime()) >= 0) {
					return false;
				}
			}
		}
		return true;
	}

	public OptimizerStragety getaStragety() {
		return aStragety;
	}

	public void setaStragety(OptimizerStragety aStragety) {
		this.aStragety = aStragety;
	}
}
