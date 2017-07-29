package xiaMengAirline.beans;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAcceptArrivalTime;
import xiaMengAirline.Exception.AirportNotAcceptDepartureTime;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.searchEngine.SelfSearch;
import xiaMengAirline.util.InitData;
import xiaMengAirline.util.Utils;

public class Aircraft implements Cloneable {
	private static final Logger logger = Logger.getLogger(Aircraft.class);
	final static private int MAXIMUM_EARLIER_TIME = 6; // HOUR
	final static public int DOMESTIC_MAXIMUM_DELAY_TIME = 24; // HOUR
	final static public int INTERNATIONAL_MAXIMUM_DELAY_TIME = 36; // HOUR
	private String id;
	private String type;
	private List<Flight> flightChain = new ArrayList<Flight>();
	private boolean isCancel = false;
	private List<Flight> dropOutList = new ArrayList<Flight>();
	private boolean isUpdated = false;
	private Aircraft alternativeAircraft = null; //alternative aircraft must be a cloned air, and assigned to one & only one its parent aircraft

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Flight> getFlightChain() {
		return flightChain;
	}

	public Flight getFlight(int position) {
		if (position >= 0)
			return this.flightChain.get(position);
		else
			return null;

	}

	public Flight getFlightByFlightId(int aFlightId) {
		for (Flight aFlight : flightChain) {
			if (aFlight.getFlightId() == aFlightId)
				return aFlight;
		}
		return null;

	}

	public List<Flight> getFlightByScheduleId(int aScheduleId) {
		List<Flight> retFlights = new ArrayList<Flight>();
		for (Flight aFlight : flightChain) {
			if (aFlight.getSchdNo() == aScheduleId) {
				retFlights.add(aFlight);
			}

		}
		return retFlights;

	}

	public void setFlightChain(List<Flight> flightChain) {
		this.flightChain = flightChain;
	}

	public void addFlight(Flight aFlight) {
		aFlight.setAssignedAir(this);
		flightChain.add(aFlight);
	}
	
	public void addFlight(int index, Flight aFlight) {
		aFlight.setAssignedAir(this);
		flightChain.add(index, aFlight);
	}
	
	public boolean hasFlight(Flight aFlight) {
		return flightChain.contains(aFlight);
	}

	/**
	 * The aircraft's insertFlight method inserts a flight into aircraft's
	 * flight chain. The flight must be fresh new and no referred by others.
	 * 
	 * @author Data Forest
	 * @param aFlight,
	 *            a new flight, either fresh new created or cloned.
	 * @param position,
	 *            specify the location of current flight chain. Flight will be
	 *            inserted before this position
	 * @return none
	 * @throws CloneNotSupportedException
	 */
	public void insertFlight(Flight aFlight, int position) throws CloneNotSupportedException {
		Aircraft aAir = this.clone();
		aAir.clear();
		aFlight.setPlannedAir(aAir);
		aFlight.setAssignedAir(this);
		aFlight.setPlannedFlight(aFlight.clone());
		flightChain.add(position, aFlight);
	}

	/**
	 * The aircraft's insertFlightChain method inserts a list of flight into
	 * aircraft's flight chain,
	 * 
	 * @author Data Forest
	 * @param sourceAircraft,
	 *            specify where the list of flight comes from.
	 * @param addFlights,
	 *            specify a list of flight indexes of the sourceAircraft, to be
	 *            inserted.
	 * @param position,
	 *            specify the location of current flight chain. Flight will be
	 *            inserted before this position
	 * @return none
	 */
	public void insertFlightChain(Aircraft sourceAircraft, List<Integer> addFlights, int position) {
		List<Flight> newFlights = new ArrayList<Flight>();
		for (int anAdd : addFlights) {
			sourceAircraft.getFlight(anAdd).setAssignedAir(this);
			newFlights.add(sourceAircraft.getFlight(anAdd));
		}
		this.flightChain.addAll(position, newFlights);
	}

	/**
	 * The aircraft's insertFlightChain method inserts a list of flight into
	 * aircraft's flight chain,
	 * 
	 * @author Data Forest
	 * @param sourceAircraft,
	 *            specify where the list of flight comes from.
	 * @param startFlight,
	 *            specify the first flight of source aircraft, which will be
	 *            inserted.
	 * @param endFlight,
	 *            specify the last flight of source aircraft, which will be
	 *            inserted.
	 * @param insertFlight,
	 *            specify the location of current flight chain
	 * @param isBefore,
	 *            is inserted before the insertFlight or after
	 * @return none
	 */
	public void insertFlightChain(Aircraft sourceAircraft, Flight startFlight, Flight endFlight, Flight insertFlight,
			boolean isBefore) {
		List<Flight> newFlights = new ArrayList<Flight>();
		int addFlightStartPosition = sourceAircraft.getFlightChain().indexOf(startFlight);
		int addFlightEndPosition = sourceAircraft.getFlightChain().indexOf(endFlight);
		int insertFlightPosition = this.flightChain.indexOf(insertFlight);
		for (int i = addFlightStartPosition; i <= addFlightEndPosition; i++) {
			sourceAircraft.getFlight(i).setAssignedAir(this);
			newFlights.add(sourceAircraft.getFlight(i));
		}
		if (insertFlight != null) {
			if (isBefore)
				this.flightChain.addAll(insertFlightPosition, newFlights);
			else
				this.flightChain.addAll(insertFlightPosition + 1, newFlights);
		} else
			this.flightChain.addAll(newFlights);

	}

	/**
	 * The aircraft's removeFlightChain method removes list of flights,
	 * 
	 * @author Data Forest
	 * @param deleteFlights,
	 *            specify the list of flights, to be removed
	 * @return none
	 */
	public void removeFlightChain(List<Integer> deleteFlights) {
		List<Flight> removeList = new ArrayList<Flight>();
		for (Integer i : deleteFlights)
			removeList.add(this.flightChain.get(i));

		this.flightChain.removeAll(removeList);
	}

	/**
	 * The aircraft's removeFlightChain method removes list of flights,
	 * 
	 * @author Data Forest
	 * @param startFlight,
	 *            specify the start flight, to be removed.
	 * @param endFlight,
	 *            specify the end flight, to be removed.
	 * @return none
	 */
	public void removeFlightChain(Flight startFlight, Flight endFlight) {
		List<Flight> removeList = new ArrayList<Flight>();
		int removeSFlighttartPosition = this.flightChain.indexOf(startFlight);
		int removeFlightEndPosition = this.flightChain.indexOf(endFlight);

		for (int i = removeSFlighttartPosition; i <= removeFlightEndPosition; i++)
			removeList.add(this.flightChain.get(i));

		this.flightChain.removeAll(removeList);
	}
	
	public List<Flight> getSpecifiedFlightChain(Flight startFlight, Flight endFlight) {
		List<Flight> retList = new ArrayList<Flight>();
		int retSFlighttartPosition = this.flightChain.indexOf(startFlight);
		int retFlightEndPosition = this.flightChain.indexOf(endFlight);

		for (int i = retSFlighttartPosition; i <= retFlightEndPosition; i++)
			retList.add(this.flightChain.get(i));

		return (retList);
		
	}

	public List<AirPort> getAirports() {
		ArrayList<AirPort> retAirPortList = new ArrayList<AirPort>();
		for (Flight aFlight : flightChain) {
			retAirPortList.add(aFlight.getSourceAirPort());
		}
		if (!flightChain.isEmpty()) {
			// add last destination
			retAirPortList.add(flightChain.get(flightChain.size() - 1).getDesintationAirport());
		}
		return (retAirPortList);

	}

	public AirPort getAirport(int position, boolean isSource) {
		if (isSource)
			return (flightChain.get(position).getSourceAirPort());
		else
			return (flightChain.get(position).getDesintationAirport());
	}

	public boolean isCancel() {
		return isCancel;
	}

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}

	public Aircraft clone() throws CloneNotSupportedException {
		Aircraft aNew = (Aircraft) super.clone();
		List<Flight> newFlightChain = new ArrayList<Flight>();
		for (Flight aFlight : flightChain) {
			Flight newFlight = aFlight.clone();
			newFlight.setPlannedFlight(aFlight.getPlannedFlight());
			newFlight.setPlannedAir(aFlight.getPlannedAir());
			newFlight.setAssignedAir(aNew);
			newFlightChain.add(newFlight);
		}
		aNew.setFlightChain(newFlightChain);
		
		List<Flight> newDropList = new ArrayList<Flight>();
		for (Flight aFlight:dropOutList) {
			newDropList.add(aFlight.clone());
		}
		aNew.setDropOutList(newDropList);
		if (alternativeAircraft!=null)
			aNew.setAlternativeAircraft(alternativeAircraft.clone());
		
		return (aNew);
	}

	public void adjustment(XiaMengAirlineSolution mySolution)
			throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable, AircraftNotAdjustable {
		SelfSearch selfAdjustEngine = new SelfSearch(mySolution);
		if (!isCancel) {
			selfAdjustEngine.adjustAircraft(this, 0, mySolution.getAircraft(id, type, true, true));
		}

	}

	// public Aircraft getCancelAircrafted() {
	// return cancelAircrafted;
	// }
	// public void setCancelAircrafted(Aircraft cancelAircrafted) {
	// this.cancelAircrafted = cancelAircrafted;
	// }

	public void clear() {
		flightChain.clear();
		dropOutList.clear();
	}

	public HashMap<Flight, List<Flight>> getCircuitFlights() {
		HashMap<Flight, List<Flight>> retCircuitList = new HashMap<Flight, List<Flight>>();

		for (Flight aFlight : flightChain) {
			ArrayList<Flight> matchedList = new ArrayList<Flight>();
			int currentPos = flightChain.indexOf(aFlight);
			String currentSourceAirport = aFlight.getSourceAirPort().getId();
			for (int j = currentPos + 1; j < flightChain.size(); j++) {
				String nextDestAirport = flightChain.get(j).getDesintationAirport().getId();
				if (currentSourceAirport.equals(nextDestAirport)) {
					matchedList.add(flightChain.get(j));
				}
			}

			if (!matchedList.isEmpty())
				retCircuitList.put(aFlight, matchedList);
		}

		return (retCircuitList);

	}

	public HashMap<Flight, List<MatchedFlight>> getMatchedFlights(Aircraft air2) {
		HashMap<Flight, List<MatchedFlight>> retMatchedList = new HashMap<Flight, List<MatchedFlight>>();

		for (Flight aFlight : flightChain) {
			String sourceAirPortAir1 = aFlight.getSourceAirPort().getId();
			for (Flight bFlight : air2.getFlightChain()) {
				String sourceAirPortAir2 = bFlight.getSourceAirPort().getId();
				if (sourceAirPortAir1.equals(sourceAirPortAir2)) {
					List<MatchedFlight> matchedList = new ArrayList<MatchedFlight>();
					for (int i = flightChain.indexOf(aFlight); i < flightChain.size(); i++) {
						String airPortA = getFlight(i).getDesintationAirport().getId();
						for (int j = air2.getFlightChain().indexOf(bFlight); j < air2.getFlightChain().size(); j++) {
							String airPortB = air2.getFlight(j).getDesintationAirport().getId();
							if (airPortA.equals(airPortB)) {
								MatchedFlight aMatched = new MatchedFlight();
								aMatched.setAir1SourceFlight(flightChain.indexOf(aFlight));
								aMatched.setAir1DestFlight(i);
								aMatched.setAir2SourceFlight(air2.getFlightChain().indexOf(bFlight));
								aMatched.setAir2DestFlight(j);
								matchedList.add(aMatched);
							}
						}
					}
					if (!matchedList.isEmpty()) {
						retMatchedList.put(aFlight, matchedList);
					} else {
						// means source airport overlapped but no destination
						// overlapped
						;
					}
				}
			}
		}
		return retMatchedList;
	}

	public void sortFlights() {
		Utils.sort(flightChain, "departureTime", true);
	}

	public boolean validate() {

		if (isCancel)
			return true;

		List<Flight> flightChain = getFlightChain();

		for (int i = 0; i < flightChain.size(); i++) {
			Flight flight = flightChain.get(i);

			String startPort = flight.getSourceAirPort().getId();
			String endPort = flight.getDesintationAirport().getId();
			String airID = getId();

			if (InitData.airLimitationList.contains(airID + "_" + startPort + "_" + endPort)) {
				return false;
			}
			if (i != 0) {
				Flight preFlight = flightChain.get(i - 1);

				if (!preFlight.getDesintationAirport().getId().equals(flight.getSourceAirPort().getId())) {
					return false;
				}

				if (Utils.minutiesBetweenTime(flight.getDepartureTime(), preFlight.getArrivalTime())
						.compareTo(new BigDecimal("50")) < 0
						&& (preFlight.getFlightId() > InitData.plannedMaxFligthId
								|| flight.getFlightId() > InitData.plannedMaxFligthId
								|| Utils.minutiesBetweenTime(flight.getDepartureTime(), preFlight.getArrivalTime())
										.compareTo(
												Utils.minutiesBetweenTime(flight.getPlannedFlight().getDepartureTime(),
														preFlight.getPlannedFlight().getArrivalTime())) != 0
								|| !flight.getPlannedAir().getId().equals(preFlight.getPlannedAir().getId()))) {
					return false;
				}

				if (InitData.jointFlightMap.get(preFlight.getFlightId()) != null) {
					if (preFlight.getDesintationAirport().getId()
							.equals((preFlight.getPlannedFlight().getDesintationAirport().getId()))
							&& InitData.jointFlightMap.get(preFlight.getFlightId()).getFlightId() != flight
									.getFlightId()) {
						return false;
					}
				}

			}
			
			//  5.7  border limited
			if (i == 0) {
				if (!flight.getSourceAirPort().getId().equals(InitData.firstFlightMap.get(airID).getPlannedFlight().getSourceAirPort().getId())) {
					return false;
				}
				
			}
			
			if (i == flightChain.size() - 1) {
				if (!flight.getSourceAirPort().getId().equals(InitData.lastFlightMap.get(airID).getPlannedFlight().getSourceAirPort().getId())
						|| !flight.getDesintationAirport().getId().equals(InitData.lastFlightMap.get(airID).getPlannedFlight().getDesintationAirport().getId())) {
					return false;
				}
				
			}
		}

		return true;

	}

	/**
	 * The aircraft's adjustFlightTime method adjusts its all flights' departure
	 * time and arrival time, until the end of flight chain or aborted due to
	 * airport issues. This method will check airport's available time, the
	 * method will automatically adjust departure time to fit airport, but the
	 * method will not adjust arrival time. For normal flights, This method
	 * takes the departure time of the specified flight, and then use its
	 * planned information calculate its arrival time. For joined flights (when
	 * change destination of first flight), This method first search the
	 * flightDuration table, if not found, then use planned information of
	 * joined flights For other extra empty flight, This method searches the
	 * flightDuration. This method must secure the given flight can departure as
	 * its departureTime
	 * 
	 * @author Data Forest
	 * @param startPosition,
	 *            specify starts from which flight. The first flight is 0.
	 * @return boolean, if adjusted.
	 * @exception ParseException
	 *                - date format is not valid
	 * @see ParseException
	 * @exception AirportNotAcceptArrivalTime
	 *                - the destination airport does not accept suggested
	 *                arrival time. This exception contains two objects, flight
	 *                (Flight), where the problem flight is at the flight chain
	 * @see Flight availableTime (FlightTime), suggested arr/dep by airport, if
	 *      caused by typhoon
	 * @see FlightTime
	 * @exception FlightDurationNotFound
	 *                - the flight duration is not found, means flight not
	 *                allowed. This exception contains two objects, theFlight
	 *                (Flight), flight is not allowed searchKey (String), the
	 *                failed search key for the lookup
	 * @throws AirportNotAcceptDepartureTime
	 *             - the airport does not accept suggested departure time. - the
	 *             source airport does not accept suggested departure time. This
	 *             exception contains two objects, flight (Flight), where the
	 *             problem flight is at the flight chain
	 * @throws AirportNotAvailable
	 * @see Flight availableTime (FlightTime), suggested arr/dep by airport, if
	 *      caused by typhoon
	 * @see FlightTime
	 */

	public boolean adjustFlightTime(int startPosition) throws ParseException, AirportNotAcceptArrivalTime,
			FlightDurationNotFound, AirportNotAcceptDepartureTime, AirportNotAvailable {
		boolean isChanged = false;
		Flight currentFlight = null;
		Flight nextFlight = null;
		for (int i = startPosition; i < flightChain.size(); i++) {
			nextFlight = flightChain.get(i);

			if (i > startPosition) {
				Calendar cl = Calendar.getInstance();
				cl.setTime(currentFlight.getArrivalTime());
				int plannedGroundingTime = nextFlight.getGroundingTime(currentFlight.getFlightId(), nextFlight.getFlightId());
				cl.add(Calendar.MINUTE, plannedGroundingTime);
				FlightTime aScheduledTime = new FlightTime();
				aScheduledTime.setArrivalTime(currentFlight.getArrivalTime());
				if (cl.getTime().before(nextFlight.getPlannedFlight().getDepartureTime()))
					aScheduledTime.setDepartureTime(nextFlight.getPlannedFlight().getDepartureTime());
				else
					aScheduledTime.setDepartureTime(cl.getTime());
				FlightTime newFlightTime = currentFlight.getDesintationAirport().requestAirport(aScheduledTime,
						plannedGroundingTime);
				if (newFlightTime != null) {
					if (aScheduledTime.getArrivalTime().compareTo(newFlightTime.getArrivalTime()) != 0) {
						throw new AirportNotAcceptArrivalTime(currentFlight, newFlightTime);
					} else {
						// check if departure time earlier
						if (newFlightTime.getDepartureTime().before(nextFlight.getPlannedFlight().getDepartureTime())) {
							// must ensure it is typhoon & not international
							// flight
							if (newFlightTime.isIsTyphoon() && !nextFlight.isInternationalFlight()) {
								// check if more than 6 hours earlier
								cl.setTime(newFlightTime.getDepartureTime());
								cl.add(Calendar.HOUR, MAXIMUM_EARLIER_TIME);
								if (cl.getTime().before(nextFlight.getPlannedFlight().getDepartureTime())) {
									logger.warn("This flight earlier too much - " + nextFlight.getFlightId()
											+ " planned dep: " + nextFlight.getPlannedFlight().getDepartureTime()
											+ " wanted dep: " + newFlightTime.getDepartureTime());
									throw new AirportNotAcceptDepartureTime(nextFlight, newFlightTime,
											"Departure Too Earlier");

								} else {
									if (nextFlight.getDepartureTime().compareTo(newFlightTime.getDepartureTime()) != 0) {
										nextFlight.setDepartureTime(newFlightTime.getDepartureTime());
										isChanged = true;
									}
								}
							} else
								throw new AirportNotAcceptDepartureTime(nextFlight, newFlightTime,
										"Departure Earlier Not Allowed For International");
						} else {
							if (nextFlight.getPlannedFlight().getDepartureTime()
									.compareTo(newFlightTime.getDepartureTime()) != 0) {
								// if departure time delayed, this only happens
								// normal close
								// because typhoon has 2 hours for take-off
								if (newFlightTime.isIsTyphoon()) {
									// if aircraft can arrive but cannot
									// departure, typhoon not allows parking!
									throw new AirportNotAvailable(currentFlight, newFlightTime);
								} else {
									// if delay too much
									cl.setTime(nextFlight.getPlannedFlight().getDepartureTime());
									if (currentFlight.isInternationalFlight()) {
										cl.add(Calendar.HOUR, INTERNATIONAL_MAXIMUM_DELAY_TIME);
									} else {
										cl.add(Calendar.HOUR, DOMESTIC_MAXIMUM_DELAY_TIME);
									}
									if (cl.getTime().before(newFlightTime.getDepartureTime())) {
										logger.warn("This flight delays too long - " + nextFlight.getFlightId()
												+ " planned dep: " + nextFlight.getPlannedFlight().getDepartureTime()
												+ " wanted dep: " + newFlightTime.getDepartureTime());
										throw new AirportNotAvailable(currentFlight, newFlightTime);
									} else {
										// it shall be normal airport close, so
										// not delay too much
										if (nextFlight.getDepartureTime().compareTo(newFlightTime.getDepartureTime()) != 0) {
											nextFlight.setDepartureTime(newFlightTime.getDepartureTime());
											isChanged = true;
										}
									}
								}
							}

						}
					}
				} else {
					if (nextFlight.getDepartureTime().compareTo(aScheduledTime.getDepartureTime()) != 0) {
						nextFlight.setDepartureTime(aScheduledTime.getDepartureTime());
						isChanged = true;
					}
				}
			}

			currentFlight = nextFlight;

			Date newArrival = currentFlight.calcuateNextArrivalTime();

			if (currentFlight.getArrivalTime().compareTo(newArrival) != 0) {
				currentFlight.setArrivalTime(newArrival);
				isChanged = true;
			}
				
		}
		
		//check if last flight can arrive destination?
		FlightTime aScheduledTime = new FlightTime();
		aScheduledTime.setArrivalTime(currentFlight.getArrivalTime());
		aScheduledTime.setDepartureTime(null);

		FlightTime newFlightTime = currentFlight.getDesintationAirport().requestAirport(aScheduledTime,
				currentFlight.getGroundingTime(currentFlight.getFlightId(), -1));
		
		if (newFlightTime != null) {
			if (aScheduledTime.getArrivalTime().compareTo(newFlightTime.getArrivalTime()) != 0) {
				throw new AirportNotAcceptArrivalTime(currentFlight, newFlightTime);
			} 
		} 		
		return isChanged;

	}


	public List<Flight> getDropOutList() {
		return dropOutList;
	}

	public void setDropOutList(List<Flight> dropOutList) {
		this.dropOutList = dropOutList;
	}

	/**
	 * The aircraft's moveToDropout method drops a regular flight into
	 * dropOutList,
	 * 
	 * @author Data Forest
	 * @param flight,
	 *            specify to-be-removed flight, the flight must be part of
	 *            aircraft's regular flight.
	 * @return true - if the flight has been successfully moved. false - flight
	 *         is not belong to this aircraft.
	 */
	public boolean moveToDropOut(Flight aFlight) {
		if (flightChain.contains(aFlight)) {
			dropOutList.add(aFlight);
			flightChain.remove(aFlight);
			return true;
		} else
			return false;
	}

	// get flight index by flight id
	public int getFlightIndexByFlightId(int aFlightId) {
		for (int i = 0; i < flightChain.size(); i++) {
			if (flightChain.get(i).getFlightId() == aFlightId)
				return i;
		}
		return -1;
	}


	public boolean isUpdated() {
		return isUpdated;
	}

	public void setUpdated(boolean isUpdated) {
		this.isUpdated = isUpdated;
	}

	public Aircraft getAlternativeAircraft() {
		return alternativeAircraft;
	}

	public void setAlternativeAircraft(Aircraft alternativeAircraft) {
		this.alternativeAircraft = alternativeAircraft;
	}
	

}