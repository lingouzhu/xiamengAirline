package xiaMengAirline.searchEngine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import xiaMengAirline.Exception.AirportNotAcceptDepartureTime2;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.AirPortClose;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.RegularAirPortClose;
import xiaMengAirline.beans.RequestTime;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.utils.InitData;
import xiaMengAirline.utils.Utils;

public class BusinessDomain {
	private static final Logger logger = Logger.getLogger(BusinessDomain.class);
	public static final int MAX_INTERNATIONAL_DELAY = 36;
	public static final int MAX_DOMESTIC_DELAY = 24;
	public static final int MAX_DOMESTIC_EARLIER = 6;

	public static boolean isValidConnectedForJoin(Flight firstFlight, Flight secondFlight) {
		if (getJointFlight(firstFlight) != secondFlight)
			return false;

		if (firstFlight.isInternationalFlight() || secondFlight.isInternationalFlight())
			return false;

		if (isAffected(firstFlight) != 0)
			return true;
		else
			return false;
	}

	public static boolean isValidDelay(Flight aFlight) {
		Calendar aCal = Calendar.getInstance();
		aCal.setTime(aFlight.getPlannedFlight().getDepartureTime());
		if (aFlight.isInternationalFlight()) {
			aCal.add(Calendar.HOUR, MAX_INTERNATIONAL_DELAY);
		} else
			aCal.add(Calendar.HOUR, MAX_DOMESTIC_DELAY);

		if (aFlight.getDepartureTime().before(aCal.getTime()))
			return true;
		else
			return false;
	}

	public static boolean isValidEarlier(Flight aFlight, boolean isTyphoon) {
		if (!isTyphoon || aFlight.isInternationalFlight())
			return false;

		Calendar aCal = Calendar.getInstance();
		aCal.setTime(aFlight.getPlannedFlight().getDepartureTime());
		aCal.add(Calendar.HOUR, -MAX_DOMESTIC_EARLIER);

		if (aFlight.getDepartureTime().after(aCal.getTime()))
			return true;
		else
			return false;
	}

	public static boolean isTyphoon(AirPort aAirport, Date aTime) {
		for (AirPortClose aClose : aAirport.getCloseSchedule()) {
			if (aTime.after(aClose.getStartTime()) && aTime.before(aClose.getEndTime())) {
				return true;
			}
		}
		return false;

	}

	public static boolean checkAirportAvailablity(AirPort aAirport, Date aTime, boolean isTakeoff, boolean checkonly,
			boolean isRelease) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(aTime);
		int years = calendar.get(Calendar.YEAR);
		int months = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);

		int checkMin = minutes / 5 * 5;

		String checkTime = String.valueOf(years);
		checkTime += "-";
		checkTime += String.format("%02d", months);
		checkTime += "-";
		checkTime += String.format("%02d", day);
		checkTime += " ";
		checkTime += String.format("%02d", hours);
		checkTime += ":";
		checkTime += String.format("%02d", checkMin);

		System.out.println("CheckAirportAvailablity Searching key ... " + checkTime);

		if (isTakeoff) {
			if (aAirport.getTakeoffCapability().containsKey(checkTime)) {
				int cap = aAirport.getTakeoffCapability().get(checkTime);
				if (isRelease) {
					cap++;
					aAirport.getTakeoffCapability().put(checkTime, cap);
					return true;
				} else {
					if (cap > 0) {
						if (checkonly)
							return true;
						else {
							cap--;
							aAirport.getTakeoffCapability().put(checkTime, cap);
							return true;
						}
					} else {
						return false;
					}
				}

			} else {
				return true;
			}
		} else {
			if (aAirport.getLandingCapability().containsKey(checkTime)) {
				int cap = aAirport.getLandingCapability().get(checkTime);
				if (isRelease) {
					cap++;
					aAirport.getLandingCapability().put(checkTime, cap);
					return true;
				} else {
					if (cap > 0) {
						if (checkonly)
							return true;
						else {
							cap--;
							aAirport.getLandingCapability().put(checkTime, cap);
							return true;
						}
					} else {
						return false;
					}
				}

			} else {
				return true;
			}
		}

	}

	// to do
	public static int getGroundingTime(int fromFlightId, int toFlightId) {
		// check if actual grounding time is less than the standard
		// check if this is the first flight
		if (fromFlightId >= toFlightId)
			return Flight.GroundingTime;

		String currentFlightId = String.valueOf(fromFlightId);
		String nextFlightId = String.valueOf(toFlightId);

		// look up special flight time table
		String searchKey = currentFlightId;
		searchKey += "_";
		searchKey += nextFlightId;

		if (InitData.specialFlightMap.containsKey(searchKey))
			return (InitData.specialFlightMap.get(searchKey));
		else
			return Flight.GroundingTime;

	}

	public static boolean validateFlights(Aircraft oldAir1, Aircraft oldAir2, Aircraft newAir1, Aircraft newAir2) {
		int oldSize = oldAir1.getFlightChain().size();
		oldSize += oldAir2.getFlightChain().size();
		int newSize = newAir1.getFlightChain().size();
		newSize += newAir2.getFlightChain().size();

		if (oldSize != newSize) {
			logger.warn("Unmatched flight size after exchange airs: " + newAir1.getId() + ":" + newAir2.getId());
			return false;
		}

		return true;

	}

	public static boolean isValidFlightPath(Aircraft air, Flight aFlight) {
		if (InitData.airLimitationList.contains(air.getId() + "_" + aFlight.getSourceAirPort().getId() + "_"
				+ aFlight.getDesintationAirport().getId())) {
			logger.warn("5.2 error air limit: flightID" + aFlight.getFlightId());
			return false;
		}
		return true;
	}

	// methods to be adjusted
	/**
	 * if a departure time is in the error time range
	 * 
	 * @param depTime
	 * @param airport
	 * @return
	 */
	public static boolean isDepTimeAffected(Date depTime, AirPort airport) {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (depTime.after(aClose.getStartTime()) && depTime.before(aClose.getEndTime())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * if a arrival time is in the error time range
	 * 
	 * @param arvTime
	 * @param airport
	 * @return
	 */
	public static boolean isArvTimeAffected(Date arvTime, AirPort airport) {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (arvTime.after(aClose.getStartTime()) && arvTime.before(aClose.getEndTime())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDepTimeAffectedByNormal(Date depTime, AirPort airport) throws ParseException {
		for (RegularAirPortClose aClose : airport.getRegularCloseSchedule()) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(depTime);
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();

			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			Date aCloseDate = formatter2.parse(aDateC);
			Date aOpenDate = formatter2.parse(aDateO);

			if (depTime.after(aCloseDate) && depTime.before(aOpenDate)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * departure time is eligible to set earlier.
	 * 
	 * @param depTime
	 * @param airport
	 * @return
	 */
	public static boolean isEarlyDeparturePossible(Date depTime, AirPort airport) {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (depTime.after(aClose.getStartTime()) && depTime.before(addMinutes(aClose.getStartTime(), 360))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * get next valid arrival time
	 * 
	 * @param flight
	 * @param airport
	 * @return
	 * @throws ParseException
	 */
	public static Date getPossibleArrivalTime(Flight flight, AirPort airport,
			HashMap<String, HashMap<Integer, ArrayList<Integer>>> timeload) throws ParseException {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (flight.getArrivalTime().after(aClose.getStartTime())
					&& flight.getArrivalTime().before(aClose.getEndTime())) {
				if (isNewFlight(flight)) {
					int timePoint = getNextEmptyTimePoint(timeload, airport, flight.getFlightId());
					return addMinutes(aClose.getEndTime(), timePoint * 5);
				} else {
					if (isValidDelay(getPlannedArrival(flight), aClose.getEndTime(), flight.isInternationalFlight())) {
						return getPossibleLoadTime(airport, aClose.getEndTime(), getPlannedArrival(flight),
								flight.isInternationalFlight(), timeload, flight.getFlightId());
					} else {
						return null;
					}
				}
			}
		}
		for (RegularAirPortClose aClose : airport.getRegularCloseSchedule()) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(flight.getArrivalTime());
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();

			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			Date aCloseDate = formatter2.parse(aDateC);
			Date aOpenDate = formatter2.parse(aDateO);

			if (flight.getArrivalTime().after(aCloseDate) && flight.getArrivalTime().before(aOpenDate)) {
				if (isNewFlight(flight)) {
					return aOpenDate;
				} else {
					if (isValidDelay(getPlannedDeparture(flight), aOpenDate, flight.isInternationalFlight())) {
						return aOpenDate;
					} else {
						return null;
					}
				}
			}
		}
		return (Date) flight.getArrivalTime().clone();
	}

	/**
	 * attempt delay the flight
	 * 
	 * @param flight
	 * @param airport
	 * @return
	 * @throws ParseException
	 */
	public static Date getPossibleDelayDeparture(Flight flight, AirPort airport, boolean isFirstFlight,
			Flight lastFlight, HashMap<String, HashMap<Integer, ArrayList<Integer>>> timeload) throws ParseException {
		Date tempDepTime = null;
		if (lastFlight != null) {
			Date shiftDeparture = addMinutes(lastFlight.getArrivalTime(), getGroundingTime(lastFlight, flight));
			tempDepTime = shiftDeparture.before(flight.getDepartureTime()) ? flight.getDepartureTime() : shiftDeparture;
		} else {
			tempDepTime = flight.getDepartureTime();
		}
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (tempDepTime.after(aClose.getStartTime()) && tempDepTime.before(aClose.getEndTime())) {
					if (isFirstFlight) {
						if (isNewFlight(flight)) {
							int timePoint = getNextEmptyTimePoint(timeload, airport, flight.getFlightId());
							return addMinutes(aClose.getEndTime(), timePoint * 5);
						} else {
							if (isValidDelay(getPlannedDeparture(flight), aClose.getEndTime(),
									flight.isInternationalFlight())) {
								return getPossibleLoadTime(airport, aClose.getEndTime(), getPlannedDeparture(flight),
										flight.isInternationalFlight(), timeload, flight.getFlightId());
							} else {
								return null;
							}
						}

					} else {
						return null;
					}
				}
			}
		}
		for (RegularAirPortClose aClose : airport.getRegularCloseSchedule()) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(tempDepTime);
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();

			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			Date aCloseDate = formatter2.parse(aDateC);
			Date aOpenDate = formatter2.parse(aDateO);

			if (tempDepTime.after(aCloseDate) && tempDepTime.before(aOpenDate)) {
				if (isNewFlight(flight)) {
					return aOpenDate;
				} else {
					if (isValidDelay(getPlannedDeparture(flight), aOpenDate, flight.isInternationalFlight())) {
						return aOpenDate;
					} else {
						return null;
					}
				}
			}
		}
		return (Date) tempDepTime.clone();
		// need to check parking somewhere
	}

	/**
	 * check if this flight is able to departure earlier.
	 * 
	 * @return if possible return the time, otherwise return null
	 * @throws ParseException
	 */
	public static Date getPossibleEarlierDepartureTime(Flight flight, AirPort airport, boolean hasEarlyLimit,
			Flight lastFlight) throws ParseException {
		if (lastFlight != null) {
			// not the first flight
			if (getPlannedDeparture(flight).before(lastFlight.getArrivalTime())) {
				return null;
			}
			for (AirPortClose aClose : airport.getCloseSchedule()) {
				if (!aClose.isAllowForTakeoff()) {
					if (isNewFlight(flight)) {
						if (flight.getDepartureTime().after(aClose.getStartTime())
								&& flight.getDepartureTime().before(aClose.getStartTime())) {
							Date tempDep = addMinutes(lastFlight.getArrivalTime(),
									Flight.getGroundingTime(lastFlight.getFlightId(), flight.getFlightId()));
							if (tempDep.after(aClose.getStartTime())) {
								return null;
							} else {
								return tempDep;
							}
						}
					} else {
						if (getPlannedDeparture(flight).after(aClose.getStartTime())
								&& getPlannedDeparture(flight).before(addMinutes(aClose.getStartTime(), 360))) {
							Date tempDep = addMinutes(lastFlight.getArrivalTime(),
									Flight.getGroundingTime(lastFlight.getFlightId(), flight.getFlightId()));
							if (tempDep.after(aClose.getStartTime())) {
								return null;
							} else {
								if (!hasEarlyLimit) {
									Date tempDep2 = addMinutes(getPlannedDeparture(flight), -360);
									return tempDep.before(tempDep2) ? tempDep2 : tempDep;
								} else {
									return aClose.getStartTime();
								}
							}
						} else {
							if (flight.getDepartureTime().after(addMinutes(aClose.getStartTime(), 360))
									&& flight.getDepartureTime().before(aClose.getStartTime())) {
								return null;
							}
						}
					}
				}
			}
		} else {
			// is the first flight
			for (AirPortClose aClose : airport.getCloseSchedule()) {
				if (!aClose.isAllowForTakeoff()) {
					if (isNewFlight(flight)) {
						if (flight.getDepartureTime().after(aClose.getStartTime())
								&& flight.getDepartureTime().before(aClose.getStartTime())) {
							// decide later
							return aClose.getStartTime();
						}
					} else {
						if (getPlannedDeparture(flight).after(aClose.getStartTime())
								&& getPlannedDeparture(flight).before(addMinutes(aClose.getStartTime(), 360))) {
							if (!hasEarlyLimit) {
								Date tempDep = addMinutes(getPlannedDeparture(flight), -360);
								return tempDep;
							} else {
								return aClose.getStartTime();
							}
						} else {
							if (flight.getDepartureTime().after(addMinutes(aClose.getStartTime(), 360))
									&& flight.getDepartureTime().before(aClose.getStartTime())) {
								return null;
							}
						}
					}
				}
			}
		}
		for (RegularAirPortClose aClose : airport.getRegularCloseSchedule()) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(flight.getDepartureTime());
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();

			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			Date aCloseDate = formatter2.parse(aDateC);
			Date aOpenDate = formatter2.parse(aDateO);

			if (flight.getDepartureTime().after(aCloseDate) && flight.getDepartureTime().before(aOpenDate)) {
				if (lastFlight != null) {
					// not the first flight
					if (isNewFlight(flight)) {
						Date tempDep = addMinutes(lastFlight.getArrivalTime(),
								Flight.getGroundingTime(lastFlight.getFlightId(), flight.getFlightId()));
						if (tempDep.after(aCloseDate)) {
							return null;
						} else {
							return tempDep;
						}
					} else {
						return null;
					}
				} else {
					// the first flight
					if (isNewFlight(flight)) {
						// decide later
						return aCloseDate;
					} else {
						return null;
					}
				}
			}
		}
		return (Date) flight.getDepartureTime().clone();
	}

	public static Date getAirportDepartureCloseStart(AirPort airport) {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				return aClose.getStartTime();
			}
		}
		return null;
	}

	/**
	 * get flight time between two airports
	 * 
	 * @param airport1Id
	 * @param airport2Id
	 * @param aircraft
	 * @return
	 */
	public static long getFlightTime(String airport1Id, String airport2Id, Aircraft aircraft) {
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

	/**
	 * the airline is international
	 * 
	 * @param airport1
	 * @param airport2
	 * @return
	 */
	public static boolean isInternational(String airport1, String airport2) {
		if (InitData.domesticAirportList.contains(airport1) && InitData.domesticAirportList.contains(airport2)) {
			return false;
		}
		return true;
	}

	/**
	 * the airline is valid
	 * 
	 * @param aircraft
	 * @param sourceAir
	 * @param destAir
	 * @return
	 */
	public static boolean isEligibalAircraft(Aircraft aircraft, AirPort sourceAir, AirPort destAir) {
		String searchKey = aircraft.getId() + "_" + sourceAir.getId() + "_" + destAir.getId();
		return InitData.airLimitationList.contains(searchKey) ? false : true;
	}

	/**
	 * check if the flight delay is valid
	 * 
	 * @param planTime
	 * @param adjustTime
	 * @param isInternational
	 * @return
	 */
	public static boolean isValidDelay(Date planTime, Date adjustTime, boolean isInternational) {
		if (getMinuteDifference(adjustTime, planTime) > (isInternational
				? Aircraft.INTERNATIONAL_MAXIMUM_DELAY_TIME * 60 : Aircraft.DOMESTIC_MAXIMUM_DELAY_TIME * 60)) {
			return false;
		}

		return true;
	}

	/**
	 * get flight index in the original flight chain by flight id
	 * 
	 * @param aFlightId
	 * @return
	 */
	public static int getFlightIndexByFlightId(int aFlightId, ArrayList<Flight> originalFlights) {
		for (int i = 0; i < originalFlights.size(); i++) {
			if (originalFlights.get(i).getFlightId() == aFlightId)
				return i;
		}
		return -1;
	}

	/**
	 * check if the aircraft parking is valid
	 * 
	 * @param arrivalTime
	 * @param departureTime
	 * @param airport
	 * @return
	 */
	public static boolean isValidParking(Date arrivalTime, Date departureTime, AirPort airport) {
		if (airport.getId().equals("25")) {
			return true;
		}
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

	/**
	 * get original arrival time
	 * 
	 * @param flight
	 * @return
	 */
	public static Date getPlannedArrival(Flight flight) {
		return flight.getPlannedFlight().getArrivalTime();
	}

	/**
	 * get original departure time
	 * 
	 * @param flight
	 * @return
	 */
	public static Date getPlannedDeparture(Flight flight) {
		return flight.getPlannedFlight().getDepartureTime();
	}

	/**
	 * get grounding time between two flights
	 * 
	 * @param flight1
	 * @param flight2
	 * @return
	 */
	public static int getGroundingTime(Flight flight1, Flight flight2) {
		return Flight.getGroundingTime(flight1.getFlightId(), flight2.getFlightId());
	}

	/**
	 * compress the departure time of next flight
	 * 
	 * @param lastFlight
	 * @param thisFlight
	 * @return
	 */
	public static Date getCompressedDeparture(Flight lastFlight, Flight thisFlight) {
		Date thisPlannedDeparture = getPlannedDeparture(thisFlight);
		Date thisShiftedDeparture = addMinutes(lastFlight.getArrivalTime(), getGroundingTime(lastFlight, thisFlight));
		return thisPlannedDeparture.before(thisShiftedDeparture) ? thisPlannedDeparture : thisShiftedDeparture;
	}

	/**
	 * a flight is a new flight
	 * 
	 * @param flight
	 * @return
	 */
	public static boolean isNewFlight(Flight flight) {
		return flight.getFlightId() > InitData.plannedMaxFligthId;
	}

	/**
	 * if a flight is joint flight
	 * 
	 * @param flight
	 * @return joint flight position
	 */
	public static int getJointFlightPosition(Flight flight) {
		if (InitData.jointFlightMap.keySet().contains(flight.getFlightId())) {
			if (InitData.jointFlightMap.get(flight.getFlightId()) != null) {
				return 1;
			} else {
				return 2;
			}
		}
		return 0;
	}

	/**
	 * get joint flight
	 * 
	 * @param flight
	 * @return
	 */
	public static Flight getJointFlight(Flight flight) {
		return InitData.jointFlightMap.get(flight.getFlightId());
	}

	/**
	 * get how this flight is affected
	 * 
	 * @param flight
	 * @return 1 departure, 2 arrival, 0 no
	 */
	public static int isAffected(Flight flight) {
		for (AirPortClose aClose : flight.getSourceAirPort().getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (flight.getDepartureTime().after(aClose.getStartTime())
						&& flight.getDepartureTime().before(aClose.getEndTime())) {
					return 1;
				}
			}
		}
		for (AirPortClose aClose : flight.getDesintationAirport().getCloseSchedule()) {
			if (flight.getArrivalTime().after(aClose.getStartTime())
					&& flight.getArrivalTime().before(aClose.getEndTime())) {
				return 2;
			}
		}
		return 0;
	}

	/**
	 * get next flight id
	 * 
	 * @return
	 */
	public static int getNextFlightId() {
		int maxFlightId = InitData.maxFligthId;
		InitData.maxFligthId = maxFlightId + 1;
		return maxFlightId + 1;
	}

	/**
	 * get new departure time by new arrival time
	 * 
	 * @param flight
	 * @param newArrivalTime
	 * @return
	 */
	public static Date getDepartureTimeByArrivalTime(Flight flight, Date newArrivalTime, Aircraft originalAircraft) {
		if (flight.getPlannedAir() != null) {
			if (getJointFlightPosition(flight) == 1) {
				if (!flight.getDesintationAirport().getId()
						.equals(flight.getPlannedFlight().getDesintationAirport().getId())) {
					long flightTime = getFlightTime(flight.getSourceAirPort().getId(),
							flight.getDesintationAirport().getId(), originalAircraft);
					if (flightTime > 0) {
						return addMinutes(newArrivalTime, -flightTime);
					} else {
						int flightTime1 = (int) getMinuteDifference(getPlannedArrival(flight),
								getPlannedDeparture(flight));
						int flightTime2 = (int) getMinuteDifference(getPlannedArrival(getJointFlight(flight)),
								getPlannedDeparture(getJointFlight(flight)));
						int timeTotal = flightTime1 + flightTime2;
						return addMinutes(newArrivalTime, -timeTotal);
					}
				} else {
					int flightTime = (int) getMinuteDifference(getPlannedArrival(flight), getPlannedDeparture(flight));
					return addMinutes(newArrivalTime, -flightTime);
				}
			} else {
				int flightTime = (int) getMinuteDifference(getPlannedArrival(flight), getPlannedDeparture(flight));
				return addMinutes(newArrivalTime, -flightTime);
			}
		} else {
			long flightTime = getFlightTime(flight.getSourceAirPort().getId(), flight.getDesintationAirport().getId(),
					originalAircraft);
			if (flightTime > 0) {
				return addMinutes(newArrivalTime, -flightTime);
			}
		}
		return null;
	}

	public static Date getArrivalTimeByDepartureTime(Flight flight, Date newDepartureTime, Aircraft originalAircraft) {
		if (!isNewFlight(flight)) {
			if (getJointFlightPosition(flight) == 1) {
				if (!flight.getDesintationAirport().getId()
						.equals(flight.getPlannedFlight().getDesintationAirport().getId())) {
					long flightTime = getFlightTime(flight.getSourceAirPort().getId(),
							flight.getDesintationAirport().getId(), originalAircraft);
					if (flightTime > 0) {
						return addMinutes(newDepartureTime, flightTime);
					} else {
						int flightTime1 = (int) getMinuteDifference(getPlannedArrival(flight),
								getPlannedDeparture(flight));
						int flightTime2 = (int) getMinuteDifference(getPlannedArrival(getJointFlight(flight)),
								getPlannedDeparture(getJointFlight(flight)));
						int timeTotal = flightTime1 + flightTime2;
						return addMinutes(newDepartureTime, timeTotal);
					}
				} else {
					int flightTime = (int) getMinuteDifference(getPlannedArrival(flight), getPlannedDeparture(flight));
					return addMinutes(newDepartureTime, flightTime);
				}
			} else {
				int flightTime = (int) getMinuteDifference(getPlannedArrival(flight), getPlannedDeparture(flight));
				return addMinutes(newDepartureTime, flightTime);
			}
		} else {
			long flightTime = getFlightTime(flight.getSourceAirPort().getId(), flight.getDesintationAirport().getId(),
					originalAircraft);
			if (flightTime > 0) {
				return addMinutes(newDepartureTime, flightTime);
			}
		}
		return null;
	}

	public static int getNextEmptyTimePoint(HashMap<String, HashMap<Integer, ArrayList<Integer>>> timeload,
			AirPort airport, int flightId) {
		int startTimePoint = 7;
		boolean done = false;
		while (!done) {
			if (timeload.containsKey(airport.getId())) {
				if (timeload.get(airport.getId()).containsKey(startTimePoint)) {
					if (timeload.get(airport.getId()).get(startTimePoint).size() > 1) {
						startTimePoint++;
					} else {
						timeload.get(airport.getId()).get(startTimePoint).add(flightId);
						return startTimePoint;
					}
				} else {
					ArrayList<Integer> flightList = new ArrayList<Integer>();
					flightList.add(flightId);
					timeload.get(airport.getId()).put(startTimePoint, flightList);
					return startTimePoint;
				}
			} else {
				ArrayList<Integer> flightList = new ArrayList<Integer>();
				flightList.add(flightId);
				HashMap<Integer, ArrayList<Integer>> timeMap = new HashMap<Integer, ArrayList<Integer>>();
				timeMap.put(startTimePoint, flightList);
				return startTimePoint;
			}
		}
		return startTimePoint;
	}

	public static Date getPossibleLoadTime(AirPort airport, Date AirportClosetime, Date planTime,
			boolean isInternational, HashMap<String, HashMap<Integer, ArrayList<Integer>>> timeload, int flightId) {
		int startTimePoint = 7;
		boolean done = false;
		while (!done) {
			if (startTimePoint > 23) {
				Date delayTime = BusinessDomain.addHours(AirportClosetime, startTimePoint * 5);
				if (BusinessDomain.isValidDelay(planTime, delayTime, isInternational)) {
					return delayTime;
				}
			}

			if (timeload.containsKey(airport.getId())) {
				if (timeload.get(airport.getId()).containsKey(startTimePoint)) {
					if (timeload.get(airport.getId()).get(startTimePoint).size() > 1) {
						startTimePoint++;
					} else {
						Date possibleTime = BusinessDomain.addHours(AirportClosetime, startTimePoint * 5);
						if (BusinessDomain.isValidDelay(planTime, possibleTime, isInternational)) {
							timeload.get(airport.getId()).get(startTimePoint).add(flightId);
							return possibleTime;
						} else {
							for (int i = 6; i > 0; i--) {
								if (timeload.get(airport.getId()).containsKey(i)) {
									if (timeload.get(airport.getId()).get(i).size() > 1) {
										continue;
									} else {
										possibleTime = BusinessDomain.addHours(AirportClosetime, i * 5);
										if (BusinessDomain.isValidDelay(planTime, possibleTime, isInternational)) {
											timeload.get(airport.getId()).get(startTimePoint).add(flightId);
											return possibleTime;
										}
									}
								} else {
									possibleTime = BusinessDomain.addHours(AirportClosetime, i * 5);
									if (BusinessDomain.isValidDelay(planTime, possibleTime, isInternational)) {
										ArrayList<Integer> flightList = new ArrayList<Integer>();
										flightList.add(flightId);
										timeload.get(airport.getId()).put(i, flightList);
										return possibleTime;
									}
								}
							}
							return null;
						}
					}
				} else {
					Date possibleTime = BusinessDomain.addHours(AirportClosetime, startTimePoint * 5);
					if (BusinessDomain.isValidDelay(planTime, possibleTime, isInternational)) {
						ArrayList<Integer> flightList = new ArrayList<Integer>();
						flightList.add(flightId);
						timeload.get(airport.getId()).put(startTimePoint, flightList);
						return possibleTime;
					} else {
						for (int i = 6; i > 0; i--) {
							if (timeload.get(airport.getId()).containsKey(i)) {
								if (timeload.get(airport.getId()).get(i).size() > 1) {
									continue;
								} else {
									possibleTime = BusinessDomain.addHours(AirportClosetime, i * 5);
									if (BusinessDomain.isValidDelay(planTime, possibleTime, isInternational)) {
										timeload.get(airport.getId()).get(i).add(flightId);
										return possibleTime;
									}
								}
							} else {
								possibleTime = BusinessDomain.addHours(AirportClosetime, i * 5);
								if (BusinessDomain.isValidDelay(planTime, possibleTime, isInternational)) {
									ArrayList<Integer> flightList = new ArrayList<Integer>();
									flightList.add(flightId);
									timeload.get(airport.getId()).put(i, flightList);
									return possibleTime;
								}
							}
						}
						return null;
					}
				}
			} else {
				Date possibleTime = BusinessDomain.addHours(AirportClosetime, startTimePoint * 5);
				if (BusinessDomain.isValidDelay(planTime, possibleTime, isInternational)) {
					ArrayList<Integer> flightList = new ArrayList<Integer>();
					flightList.add(flightId);
					HashMap<Integer, ArrayList<Integer>> timeMap = new HashMap<Integer, ArrayList<Integer>>();
					timeMap.put(startTimePoint, flightList);
					timeload.put(airport.getId(), timeMap);
					return possibleTime;
				} else {
					for (int i = 6; i > 0; i--) {
						possibleTime = BusinessDomain.addHours(AirportClosetime, i * 5);
						if (BusinessDomain.isValidDelay(planTime, possibleTime, isInternational)) {
							ArrayList<Integer> flightList = new ArrayList<Integer>();
							flightList.add(flightId);
							HashMap<Integer, ArrayList<Integer>> timeMap = new HashMap<Integer, ArrayList<Integer>>();
							timeMap.put(startTimePoint, flightList);
							timeload.put(airport.getId(), timeMap);
							return possibleTime;
						}
					}
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * add minutes to date
	 * 
	 * @param date
	 * @param minutes
	 * @return
	 */
	public static Date addHours(Date date, int hours) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR, hours);
		return cal.getTime();
	}

	/**
	 * add minutes to date
	 * 
	 * @param date
	 * @param minutes
	 * @return
	 */
	public static Date addMinutes2(Date date, int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MINUTE, minutes);
		return cal.getTime();
	}

	/**
	 * add minutes to date
	 * 
	 * @param date
	 * @param minutes
	 * @return
	 */
	public static Date addMinutes(Date date, long minutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		long t = cal.getTimeInMillis();
		return new Date(t + (minutes * 60000));
	}

	/**
	 * get different between two date in minute
	 * 
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static double getMinuteDifference(Date time1, Date time2) {
		return (time1.getTime() - time2.getTime()) / (1000 * 60);
	}

	/**
	 * get different between two date in hours
	 * 
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static double getHourDifference(Date time1, Date time2) {
		return (time1.getTime() - time2.getTime()) / (1000 * 60 * 60);
	}

	public static Date getNextOpenDate(AirPort airport, Date orgDate) {

		Date openDate = null;
		List<RegularAirPortClose> regularStartCloseSchedule = airport.getRegularCloseSchedule();
		for (RegularAirPortClose aClose : regularStartCloseSchedule) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(orgDate);
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();

			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			try {
				Date aCloseDate = formatter2.parse(aDateC);
				Date aOpenDate = formatter2.parse(aDateO);

				if (orgDate.after(aCloseDate) && orgDate.before(aOpenDate)) {
					openDate = aOpenDate;
				} else {
					openDate = orgDate;
				}

			} catch (ParseException e) {
				System.out.println("normal close date error");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return openDate;
	}

	public static boolean isNormalClose(AirPort airport, Date orgDate) {

		boolean closeFlg = false;

		List<RegularAirPortClose> regularStartCloseSchedule = airport.getRegularCloseSchedule();
		for (RegularAirPortClose aClose : regularStartCloseSchedule) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String aDateC = formatter.format(orgDate);
			String aDateO = aDateC;
			aDateC += " ";
			aDateC += aClose.getCloseTime();
			aDateO += " ";
			aDateO += aClose.getOpenTime();

			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			try {
				Date aCloseDate = formatter2.parse(aDateC);
				Date aOpenDate = formatter2.parse(aDateO);

				if (orgDate.after(aCloseDate) && orgDate.before(aOpenDate)) {
					closeFlg = true;
				}

			} catch (ParseException e) {
				System.out.println("normal close date error");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return closeFlg;
	}

	public static boolean isFeasibleDepartureTime(Aircraft air, Flight arrivalFlight, Flight departureFlight,
			boolean ignoreParking) {

		RequestTime myRequest = new RequestTime();

		int currentGroundingTime = Flight.GroundingTime;
		if (arrivalFlight != null) {
			int pos = air.getFlightChain().indexOf(arrivalFlight);
			Flight prevFlight = null;
			int startPos = pos;
			do {
				prevFlight = air.getFlight(startPos);
				startPos--;
			} while (prevFlight.isCanceled() && startPos >= 0);
			if (pos == startPos + 1) {
				currentGroundingTime = BusinessDomain.getGroundingTime(arrivalFlight, departureFlight);
				myRequest.setArrivalTime(arrivalFlight.getArrivalTime());
			} else if (!prevFlight.isCanceled()) {
				long flightTime = BusinessDomain.getFlightTime(prevFlight.getSourceAirPort().getId(),
						departureFlight.getSourceAirPort().getId(), air);
				if (flightTime == 0)
					flightTime = 180; // assuming 180 min for connect flight
				Date newArrivalTime = BusinessDomain.addMinutes(prevFlight.getDepartureTime(), flightTime);
				myRequest.setArrivalTime(newArrivalTime);
			} else {
				myRequest.setArrivalTime(null);
			}

		} else {
			myRequest.setArrivalTime(null);
		}
		myRequest.setDepartureTime(departureFlight.getDepartureTime());
		RequestTime feasibleRequest = null;
		try {
			feasibleRequest = departureFlight.getSourceAirPort().requestAirport(myRequest, currentGroundingTime,
					ignoreParking);
		} catch (AirportNotAcceptDepartureTime2 ex) {
			logger.debug("Airport not acepts departure, airport " + ex.getAirport().getId() + " flight "
					+ departureFlight.getFlightId() + " departure time "
					+ Utils.timeFormatter2(ex.getPlannedDeparture()) + " due to " + ex.getCasue());
			return false;
		} catch (ParseException ex) {
			ex.printStackTrace();
			logger.error("Unable to parse time format!");
			return false;
		}
		if (feasibleRequest != null) {
			// let's try suggested time
			departureFlight.setDepartureTime(feasibleRequest.getDepartureTime());
		}
		// cancel if valid move
		boolean isTyphoon = isTyphoon(departureFlight.getSourceAirPort(), departureFlight.getDepartureTime());
		if (departureFlight.getDepartureTime().before(departureFlight.getPlannedFlight().getDepartureTime())) {
			if (!BusinessDomain.isValidEarlier(departureFlight, isTyphoon)) {
				return false;
			}
		} else {
			if (!BusinessDomain.isValidDelay(departureFlight)) {
				return false;
			}
		}
		return true;
	}

	public static boolean calcuateDepartureTimebyArrival(Aircraft air, Flight arrivalFlight, Flight departureFlight,
			Date newArrival, int maxGroudingTime, boolean ignoreParking) {

		if (arrivalFlight == null) {
			if (newArrival.compareTo(departureFlight.getArrivalTime()) != 0) {
				Calendar cl = Calendar.getInstance();
				cl.setTime(departureFlight.getDepartureTime());
				cl.add(Calendar.MINUTE,
						(int) BusinessDomain.getMinuteDifference(newArrival, departureFlight.getArrivalTime()));
				Date newDepurature = cl.getTime();

				departureFlight.setDepartureTime(newDepurature);

			}
		} else {
			int pos = air.getFlightChain().indexOf(arrivalFlight);
			Flight prevFlight = null;
			int startPos = pos;
			do {
				prevFlight = air.getFlight(startPos);
				startPos--;
			} while (prevFlight.isCanceled() && startPos >= 0);
			Date initialArrivalTime = null;
			int currentGroundingTime = Flight.GroundingTime;
			if (pos == startPos + 1) {
				initialArrivalTime = arrivalFlight.getArrivalTime();
				currentGroundingTime = BusinessDomain.getGroundingTime(arrivalFlight, departureFlight);
			} else if (!prevFlight.isCanceled()) {
				long flightTime = BusinessDomain.getFlightTime(prevFlight.getSourceAirPort().getId(),
						departureFlight.getSourceAirPort().getId(), air);
				if (flightTime == 0)
					flightTime = 180; // assuming 180 min for connect flight
				initialArrivalTime = BusinessDomain.addMinutes(prevFlight.getDepartureTime(), flightTime);
			}
			if (initialArrivalTime != null) {
				Date earliestDepartureTime = BusinessDomain.addMinutes(initialArrivalTime, currentGroundingTime);
				Date latestDepartureTime = BusinessDomain.addHours(initialArrivalTime, maxGroudingTime);

				if (newArrival.compareTo(departureFlight.getArrivalTime()) != 0) {
					Calendar cl = Calendar.getInstance();
					cl.setTime(departureFlight.getDepartureTime());
					cl.add(Calendar.MINUTE,
							(int) BusinessDomain.getMinuteDifference(newArrival, departureFlight.getArrivalTime()));
					Date newDepurature = cl.getTime();

					if (newDepurature.before(departureFlight.getDepartureTime())) {
						if (newDepurature.before(earliestDepartureTime))
							return false;
						else {
							departureFlight.setDepartureTime(newDepurature);
						}
					} else {
						if (newDepurature.after(latestDepartureTime))
							return false;
						else
							departureFlight.setDepartureTime(newDepurature);
					}
				}
			} else {
				if (newArrival.compareTo(departureFlight.getArrivalTime()) != 0) {
					Calendar cl = Calendar.getInstance();
					cl.setTime(departureFlight.getDepartureTime());
					cl.add(Calendar.MINUTE,
							(int) BusinessDomain.getMinuteDifference(newArrival, departureFlight.getArrivalTime()));
					Date newDepurature = cl.getTime();

					departureFlight.setDepartureTime(newDepurature);

				}
			}
			

		}

		if (!BusinessDomain.isFeasibleDepartureTime(air, arrivalFlight, departureFlight, ignoreParking)) {
			return false;
		}

		return true;

	}

	public static boolean isFeasibleArrivalTime(Aircraft air, Flight prevFlight, Flight arrivalFlight,
			int maxGroudingTime, boolean ignoreParking) {

		RequestTime myRequest = new RequestTime();
		myRequest.setArrivalTime(arrivalFlight.getArrivalTime());
		myRequest.setDepartureTime(null);
		try {
			myRequest = arrivalFlight.getDesintationAirport().requestAirport(myRequest, Flight.GroundingTime,
					ignoreParking);
			if (myRequest != null) {
				if (BusinessDomain.calcuateDepartureTimebyArrival(air, prevFlight, arrivalFlight,
						myRequest.getArrivalTime(), maxGroudingTime, ignoreParking)) {
					arrivalFlight.setArrivalTime(myRequest.getArrivalTime());
					;
				} else {
					logger.warn("Unable to find right arrival time " + arrivalFlight.getFlightId());
					return false;
				}
			}

		} catch (AirportNotAcceptDepartureTime2 e1) {
			logger.warn(" Not possible to have AirportNotAcceptDepartureTime2 " + e1.getAirport().getId() + " flight "
					+ arrivalFlight.getFlightId());
			return false;
		} catch (ParseException e1) {
			logger.warn(" Not possible to have Parse error " + " flight " + arrivalFlight.getFlightId());
			return false;
		}
		return true;

	}

	public static boolean validateDuplicatedFlight(XiaMengAirlineSolution aSolution) {
		List<Aircraft> airList = new ArrayList<Aircraft>(aSolution.getSchedule().values());
		Map<Integer, Flight> flightMap = new HashMap<Integer, Flight>();
		for (Aircraft air : airList) {
			for (Flight aFlight : air.getFlightChain()) {
				if (flightMap.containsKey(aFlight.getFlightId())) {
					logger.error("Duplicated flight flightId: " + aFlight.getFlightId());
					logger.error(" first flight: air-" + air.getId());
					logger.error(
							" second flight: air-" + flightMap.get(aFlight.getFlightId()).getAssignedAir().getId());
					return false;
				} else
					flightMap.put(aFlight.getFlightId(), aFlight);
			}
		}
		return true;
	}

	public static void printOutAircraft(Aircraft aAir) {

		logger.debug("Aircraft in details: " + aAir.getId() + " cost: " + aAir.getCost());
		for (Flight aFlight : aAir.getFlightChain()) {
			logger.debug("Flight " + aFlight.getFlightId());
			boolean isCancelled = aAir.isCancel() || aFlight.isCanceled();
			logger.debug("isCancelled? " + isCancelled);
			logger.debug("source airport " + aFlight.getSourceAirPort().getId());
			logger.debug("dest airport" + aFlight.getDesintationAirport().getId());
			logger.debug("departure time " + aFlight.getDepartureTime());
			logger.debug("arrival time " + aFlight.getArrivalTime());
			logger.debug("assigned air" + aFlight.getAssignedAir().getId());
		}
	}

}
