package xiaMengAirline.beans;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import xiaMengAirline.Exception.AircraftNotAdjustableBackup;
import xiaMengAirline.Exception.AirportNotAcceptArrivalTimeBackup;
import xiaMengAirline.Exception.AirportNotAcceptDepartureTimeBackup;
import xiaMengAirline.Exception.AirportNotAvailableBackup;
import xiaMengAirline.Exception.FlightDurationNotFoundBackup;
import xiaMengAirline.searchEngine.SelfSearchBackup;
import xiaMengAirline.util.InitDataBackup;
import xiaMengAirline.util.UtilsBackup;

public class AircraftBackup implements Cloneable {
	private static final Logger logger = Logger.getLogger(AircraftBackup.class);
	final static private int MAXIMUM_EARLIER_TIME = 6; // HOUR
	final static public int DOMESTIC_MAXIMUM_DELAY_TIME = 24; // HOUR
	final static public int INTERNATIONAL_MAXIMUM_DELAY_TIME = 36; // HOUR
	private String id;
	private String type;
	private List<FlightBackup> flightChain = new ArrayList<FlightBackup>();
	private boolean isCancel = false;
	private List<FlightBackup> dropOutList = new ArrayList<FlightBackup>();
	private boolean isUpdated = false;
	private AircraftBackup alternativeAircraft = null; //alternative aircraft must be a cloned air, and assigned to one & only one its parent aircraft

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

	public List<FlightBackup> getFlightChain() {
		return flightChain;
	}

	public FlightBackup getFlight(int position) {
		if (position >= 0)
			return this.flightChain.get(position);
		else
			return null;

	}

	public FlightBackup getFlightByFlightId(int aFlightId) {
		for (FlightBackup aFlight : flightChain) {
			if (aFlight.getFlightId() == aFlightId)
				return aFlight;
		}
		return null;

	}

	public List<FlightBackup> getFlightByScheduleId(int aScheduleId) {
		List<FlightBackup> retFlights = new ArrayList<FlightBackup>();
		for (FlightBackup aFlight : flightChain) {
			if (aFlight.getSchdNo() == aScheduleId) {
				retFlights.add(aFlight);
			}

		}
		return retFlights;

	}

	public void setFlightChain(List<FlightBackup> flightChain) {
		this.flightChain = flightChain;
	}

	public void addFlight(FlightBackup aFlight) {
		aFlight.setAssignedAir(this);
		flightChain.add(aFlight);
	}
	
	public void addFlight(int index, FlightBackup aFlight) {
		aFlight.setAssignedAir(this);
		flightChain.add(index, aFlight);
	}
	
	public boolean hasFlight(FlightBackup aFlight) {
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
	public void insertFlight(FlightBackup aFlight, int position) throws CloneNotSupportedException {
		AircraftBackup aAir = this.clone();
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
	public void insertFlightChain(AircraftBackup sourceAircraft, List<Integer> addFlights, int position) {
		List<FlightBackup> newFlights = new ArrayList<FlightBackup>();
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
	public void insertFlightChain(AircraftBackup sourceAircraft, FlightBackup startFlight, FlightBackup endFlight, FlightBackup insertFlight,
			boolean isBefore) {
		List<FlightBackup> newFlights = new ArrayList<FlightBackup>();
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
		List<FlightBackup> removeList = new ArrayList<FlightBackup>();
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
	public void removeFlightChain(FlightBackup startFlight, FlightBackup endFlight) {
		List<FlightBackup> removeList = new ArrayList<FlightBackup>();
		int removeSFlighttartPosition = this.flightChain.indexOf(startFlight);
		int removeFlightEndPosition = this.flightChain.indexOf(endFlight);

		for (int i = removeSFlighttartPosition; i <= removeFlightEndPosition; i++)
			removeList.add(this.flightChain.get(i));

		this.flightChain.removeAll(removeList);
	}
	
	public List<FlightBackup> getSpecifiedFlightChain(FlightBackup startFlight, FlightBackup endFlight) {
		List<FlightBackup> retList = new ArrayList<FlightBackup>();
		int retSFlighttartPosition = this.flightChain.indexOf(startFlight);
		int retFlightEndPosition = this.flightChain.indexOf(endFlight);

		for (int i = retSFlighttartPosition; i <= retFlightEndPosition; i++)
			retList.add(this.flightChain.get(i));

		return (retList);
		
	}

	public List<AirPortBackup> getAirports() {
		ArrayList<AirPortBackup> retAirPortList = new ArrayList<AirPortBackup>();
		for (FlightBackup aFlight : flightChain) {
			retAirPortList.add(aFlight.getSourceAirPort());
		}
		if (!flightChain.isEmpty()) {
			// add last destination
			retAirPortList.add(flightChain.get(flightChain.size() - 1).getDesintationAirport());
		}
		return (retAirPortList);

	}

	public AirPortBackup getAirport(int position, boolean isSource) {
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

	public AircraftBackup clone() throws CloneNotSupportedException {
		AircraftBackup aNew = (AircraftBackup) super.clone();
		List<FlightBackup> newFlightChain = new ArrayList<FlightBackup>();
		for (FlightBackup aFlight : flightChain) {
			FlightBackup newFlight = aFlight.clone();
			newFlight.setPlannedFlight(aFlight.getPlannedFlight());
			newFlight.setPlannedAir(aFlight.getPlannedAir());
			newFlight.setAssignedAir(aNew);
			newFlightChain.add(newFlight);
		}
		aNew.setFlightChain(newFlightChain);
		
		List<FlightBackup> newDropList = new ArrayList<FlightBackup>();
		for (FlightBackup aFlight:dropOutList) {
			newDropList.add(aFlight.clone());
		}
		aNew.setDropOutList(newDropList);
		if (alternativeAircraft!=null)
			aNew.setAlternativeAircraft(alternativeAircraft.clone());
		
		return (aNew);
	}

	public void adjustment(XiaMengAirlineSolutionBackup mySolution)
			throws CloneNotSupportedException, ParseException, FlightDurationNotFoundBackup, AirportNotAvailableBackup, AircraftNotAdjustableBackup {
		SelfSearchBackup selfAdjustEngine = new SelfSearchBackup(mySolution);
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

	public HashMap<FlightBackup, List<FlightBackup>> getCircuitFlights() {
		HashMap<FlightBackup, List<FlightBackup>> retCircuitList = new HashMap<FlightBackup, List<FlightBackup>>();

		for (FlightBackup aFlight : flightChain) {
			ArrayList<FlightBackup> matchedList = new ArrayList<FlightBackup>();
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

	public HashMap<FlightBackup, List<MatchedFlightBackup>> getMatchedFlights(AircraftBackup air2) {
		HashMap<FlightBackup, List<MatchedFlightBackup>> retMatchedList = new HashMap<FlightBackup, List<MatchedFlightBackup>>();

		for (FlightBackup aFlight : flightChain) {
			String sourceAirPortAir1 = aFlight.getSourceAirPort().getId();
			for (FlightBackup bFlight : air2.getFlightChain()) {
				String sourceAirPortAir2 = bFlight.getSourceAirPort().getId();
				if (sourceAirPortAir1.equals(sourceAirPortAir2)) {
					List<MatchedFlightBackup> matchedList = new ArrayList<MatchedFlightBackup>();
					for (int i = flightChain.indexOf(aFlight); i < flightChain.size(); i++) {
						String airPortA = getFlight(i).getDesintationAirport().getId();
						for (int j = air2.getFlightChain().indexOf(bFlight); j < air2.getFlightChain().size(); j++) {
							String airPortB = air2.getFlight(j).getDesintationAirport().getId();
							if (airPortA.equals(airPortB)) {
								MatchedFlightBackup aMatched = new MatchedFlightBackup();
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
		UtilsBackup.sort(flightChain, "departureTime", true);
	}

	public boolean validate() {

		if (isCancel)
			return true;

		List<FlightBackup> flightChain = getFlightChain();

		for (int i = 0; i < flightChain.size(); i++) {
			FlightBackup flight = flightChain.get(i);

			String startPort = flight.getSourceAirPort().getId();
			String endPort = flight.getDesintationAirport().getId();
			String airID = getId();

			if (InitDataBackup.airLimitationList.contains(airID + "_" + startPort + "_" + endPort)) {
				return false;
			}
			if (i != 0) {
				FlightBackup preFlight = flightChain.get(i - 1);

				if (!preFlight.getDesintationAirport().getId().equals(flight.getSourceAirPort().getId())) {
					return false;
				}

				if (UtilsBackup.minutiesBetweenTime(flight.getDepartureTime(), preFlight.getArrivalTime())
						.compareTo(new BigDecimal("50")) < 0
						&& (preFlight.getFlightId() > InitDataBackup.plannedMaxFligthId
								|| flight.getFlightId() > InitDataBackup.plannedMaxFligthId
								|| UtilsBackup.minutiesBetweenTime(flight.getDepartureTime(), preFlight.getArrivalTime())
										.compareTo(
												UtilsBackup.minutiesBetweenTime(flight.getPlannedFlight().getDepartureTime(),
														preFlight.getPlannedFlight().getArrivalTime())) != 0
								|| !flight.getPlannedAir().getId().equals(preFlight.getPlannedAir().getId()))) {
					return false;
				}

				if (InitDataBackup.jointFlightMap.get(preFlight.getFlightId()) != null) {
					if (preFlight.getDesintationAirport().getId()
							.equals((preFlight.getPlannedFlight().getDesintationAirport().getId()))
							&& InitDataBackup.jointFlightMap.get(preFlight.getFlightId()).getFlightId() != flight
									.getFlightId()) {
						return false;
					}
				}

			}
			
			//  5.7  border limited
			if (i == 0) {
				if (!flight.getSourceAirPort().getId().equals(InitDataBackup.firstFlightMap.get(airID).getPlannedFlight().getSourceAirPort().getId())) {
					return false;
				}
				
			}
			
			if (i == flightChain.size() - 1) {
				if (!flight.getSourceAirPort().getId().equals(InitDataBackup.lastFlightMap.get(airID).getPlannedFlight().getSourceAirPort().getId())
						|| !flight.getDesintationAirport().getId().equals(InitDataBackup.lastFlightMap.get(airID).getPlannedFlight().getDesintationAirport().getId())) {
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
	 * @exception AirportNotAcceptArrivalTimeBackup
	 *                - the destination airport does not accept suggested
	 *                arrival time. This exception contains two objects, flight
	 *                (Flight), where the problem flight is at the flight chain
	 * @see FlightBackup availableTime (FlightTime), suggested arr/dep by airport, if
	 *      caused by typhoon
	 * @see FlightTimeBackup
	 * @exception FlightDurationNotFoundBackup
	 *                - the flight duration is not found, means flight not
	 *                allowed. This exception contains two objects, theFlight
	 *                (Flight), flight is not allowed searchKey (String), the
	 *                failed search key for the lookup
	 * @throws AirportNotAcceptDepartureTimeBackup
	 *             - the airport does not accept suggested departure time. - the
	 *             source airport does not accept suggested departure time. This
	 *             exception contains two objects, flight (Flight), where the
	 *             problem flight is at the flight chain
	 * @throws AirportNotAvailableBackup
	 * @see FlightBackup availableTime (FlightTime), suggested arr/dep by airport, if
	 *      caused by typhoon
	 * @see FlightTimeBackup
	 */

	public boolean adjustFlightTime(int startPosition) throws ParseException, AirportNotAcceptArrivalTimeBackup,
			FlightDurationNotFoundBackup, AirportNotAcceptDepartureTimeBackup, AirportNotAvailableBackup {
		boolean isChanged = false;
		FlightBackup currentFlight = null;
		FlightBackup nextFlight = null;
		for (int i = startPosition; i < flightChain.size(); i++) {
			nextFlight = flightChain.get(i);

			if (i > startPosition) {
				Calendar cl = Calendar.getInstance();
				cl.setTime(currentFlight.getArrivalTime());
				int plannedGroundingTime = FlightBackup.getGroundingTime(currentFlight.getFlightId(), nextFlight.getFlightId());
				cl.add(Calendar.MINUTE, plannedGroundingTime);
				FlightTimeBackup aScheduledTime = new FlightTimeBackup();
				aScheduledTime.setArrivalTime(currentFlight.getArrivalTime());
				if (cl.getTime().before(nextFlight.getPlannedFlight().getDepartureTime()))
					aScheduledTime.setDepartureTime(nextFlight.getPlannedFlight().getDepartureTime());
				else
					aScheduledTime.setDepartureTime(cl.getTime());
				FlightTimeBackup newFlightTime = currentFlight.getDesintationAirport().requestAirport(aScheduledTime,
						plannedGroundingTime);
				if (newFlightTime != null) {
					if (aScheduledTime.getArrivalTime().compareTo(newFlightTime.getArrivalTime()) != 0) {
						throw new AirportNotAcceptArrivalTimeBackup(currentFlight, newFlightTime);
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
									throw new AirportNotAcceptDepartureTimeBackup(nextFlight, newFlightTime,
											"Departure Too Earlier");

								} else {
									if (nextFlight.getDepartureTime().compareTo(newFlightTime.getDepartureTime()) != 0) {
										nextFlight.setDepartureTime(newFlightTime.getDepartureTime());
										isChanged = true;
									}
								}
							} else
								throw new AirportNotAcceptDepartureTimeBackup(nextFlight, newFlightTime,
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
									throw new AirportNotAvailableBackup(currentFlight, newFlightTime);
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
										throw new AirportNotAvailableBackup(currentFlight, newFlightTime);
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
		FlightTimeBackup aScheduledTime = new FlightTimeBackup();
		aScheduledTime.setArrivalTime(currentFlight.getArrivalTime());
		aScheduledTime.setDepartureTime(null);

		FlightTimeBackup newFlightTime = currentFlight.getDesintationAirport().requestAirport(aScheduledTime,
				FlightBackup.getGroundingTime(currentFlight.getFlightId(), -1));
		
		if (newFlightTime != null) {
			if (aScheduledTime.getArrivalTime().compareTo(newFlightTime.getArrivalTime()) != 0) {
				throw new AirportNotAcceptArrivalTimeBackup(currentFlight, newFlightTime);
			} 
		} 		
		return isChanged;

	}


	public List<FlightBackup> getDropOutList() {
		return dropOutList;
	}

	public void setDropOutList(List<FlightBackup> dropOutList) {
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
	public boolean moveToDropOut(FlightBackup aFlight) {
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

	public AircraftBackup getAlternativeAircraft() {
		return alternativeAircraft;
	}

	public void setAlternativeAircraft(AircraftBackup alternativeAircraft) {
		this.alternativeAircraft = alternativeAircraft;
	}
	

}