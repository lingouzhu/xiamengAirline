package xiaMengAirline.searchEngine;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAcceptArrivalTime;
import xiaMengAirline.Exception.AirportNotAcceptDepartureTime;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.AirPortClose;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.FlightTime;
import xiaMengAirline.beans.RegularAirPortClose;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.util.InitData;
import xiaMengAirline.util.Utils;

public class SingleAircraftSearch {
	private Aircraft originalAircraft;
	private ArrayList<Flight> originalFlights;
	private TreeMap<Long, ArrayList<ArrayList<Flight>>> openArrayList = new TreeMap<Long, ArrayList<ArrayList<Flight>>>();
	private ArrayList<ArrayList<Flight>> finishedArrayList = new ArrayList<ArrayList<Flight>>();
	
	public SingleAircraftSearch(Aircraft aAircraft) throws CloneNotSupportedException {
		originalAircraft = aAircraft.clone();
		originalFlights = (ArrayList<Flight>) originalAircraft.getFlightChain();
	}
	
	public ArrayList<Aircraft> getAdjustedAircraftPair() throws CloneNotSupportedException{
		boolean started = false;
		boolean finished = false;
		// loop to open all solutions
		int i = 0;
		while ((!started || !finished)){
			try {
				finished = processNextPath(started);
			} catch (CloneNotSupportedException | FlightDurationNotFound | ParseException e) {
				e.printStackTrace();
			}
			started = true;
		}
		
		
		
		// evaluate solutions to get the best solution
		ArrayList<Flight> bestList = finishedArrayList.get(0);
		long bestCost = calDeltaCost(originalFlights, finishedArrayList.get(0));
		for (ArrayList<Flight> list : finishedArrayList){
			long newCost = calDeltaCost(originalFlights, list);
			if (newCost < bestCost){
				bestList = list;
				bestCost = newCost;
			}
		}
		// get flights canceled from best solution
		ArrayList<Flight> cancelList = new ArrayList<Flight>();
		for (Flight orgFlight : originalFlights){
			boolean found = false;
			for (Flight newFlight : bestList){
				if (newFlight.getFlightId() == orgFlight.getFlightId()){
					found = true;
				}
			}
			if (!found){
				cancelList.add(orgFlight);
			}
		}
		// build two aircraft
		Aircraft normalAircraft = originalAircraft.clone();
		normalAircraft.setAlternativeAircraft(null);
		normalAircraft.setFlightChain(bestList);
		Aircraft cancelAircraft = originalAircraft.clone();
		cancelAircraft.setCancel(true);
		cancelAircraft.setAlternativeAircraft(null);
		cancelAircraft.setFlightChain(cancelList);
		
		ArrayList<Aircraft> returnList = new ArrayList<Aircraft>();
		returnList.add(normalAircraft);
		returnList.add(cancelAircraft);
		return returnList;
	}
	
	public boolean processNextPath(boolean started) throws CloneNotSupportedException, FlightDurationNotFound, ParseException {
		if (openArrayList.size() > 0) {
			Map.Entry<Long, ArrayList<ArrayList<Flight>>> entry = openArrayList.entrySet().iterator().next();
			long costKey = entry.getKey();
			ArrayList<Flight> path = entry.getValue().get(0);
			entry.getValue().remove(0);
			if (openArrayList.get(costKey).size() == 0){
				openArrayList.remove(costKey);
			}
			openNextNode(path);
		} else {
			if (!started){
				openFirstNode();
			} else {
				return true;
			}
		}
		return false;
	}
	
	public void openFirstNode() throws CloneNotSupportedException, FlightDurationNotFound, ParseException {
		Flight thisFlight = originalFlights.get(0);
		// try earlier departure
		Date earlyDepTime = getPossibleEarlierDepartureTime(thisFlight, thisFlight.getSourceAirPort(), false, null);
		if (earlyDepTime != null && !thisFlight.isInternationalFlight()) {
			if (earlyDepTime.compareTo(thisFlight.getDepartureTime()) != 0){
				boolean stretchable = false;
				Flight newFlight = thisFlight.clone();
				newFlight.setDepartureTime(earlyDepTime);
				newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, earlyDepTime));
				Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
				if (adjustedArrival != null){
					if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
						//ok open next
						ArrayList<Flight> aNewFlightChain = new ArrayList<Flight>();
						aNewFlightChain.add(newFlight);
						aNewFlightChain.add(originalFlights.get(1).clone());
						insertPathToOpenList(aNewFlightChain);
						stretchable = true;
					} else {
						Date newDeparture = getDepartureTimeByArrivalTime(newFlight, adjustedArrival);
						if (!isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
							newFlight.setDepartureTime(newDeparture);
							if (isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
								Date newTempDelayDep = getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, null);
								if (newTempDelayDep != null){
									newFlight.setDepartureTime(newTempDelayDep);
									newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, newTempDelayDep));
									if (!isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
										ArrayList<Flight> aNewFlightChain = new ArrayList<Flight>();
										aNewFlightChain.add(newFlight);
										aNewFlightChain.add(originalFlights.get(1).clone());
										insertPathToOpenList(aNewFlightChain);
										stretchable = true;
									}
								}
							} else {
								//ok open next
								newFlight.setArrivalTime(adjustedArrival);
								ArrayList<Flight> aNewFlightChain = new ArrayList<Flight>();
								aNewFlightChain.add(newFlight);
								aNewFlightChain.add(originalFlights.get(1).clone());
								insertPathToOpenList(aNewFlightChain);
								stretchable = true;
							}
						}
					}
				}
				// try stretch
				if (stretchable && getJointFlightPosition(newFlight) == 1) {
					ArrayList<Flight> newFlightList =  new ArrayList<Flight>();
					newFlightList.add(newFlight);
					openJointFlightNode(newFlight, newFlightList);
				}
			}
		}
		
		// try delay departure
		Date delayDepTime = getPossibleDelayDeparture(thisFlight, thisFlight.getSourceAirPort(), true, null);
		if (delayDepTime != null) {
			boolean stretchable = false;
			Flight newFlight = thisFlight.clone();
			newFlight.setDepartureTime(delayDepTime);
			newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, delayDepTime));
			Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
			if (adjustedArrival != null){
				if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
					//ok open next
					ArrayList<Flight> aNewFlightChain = new ArrayList<Flight>();
					aNewFlightChain.add(newFlight);
					aNewFlightChain.add(originalFlights.get(1).clone());
					insertPathToOpenList(aNewFlightChain);
					stretchable = true;
				} else {
					
					Date newDeparture = getDepartureTimeByArrivalTime(newFlight, adjustedArrival);
					if (!isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
						newFlight.setDepartureTime(newDeparture);
						if (isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
							Date newTempDelayDep = getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, null);
							if (newTempDelayDep != null){
								newFlight.setDepartureTime(newTempDelayDep);
								newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, newTempDelayDep));
								if (!isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
									ArrayList<Flight> aNewFlightChain = new ArrayList<Flight>();
									aNewFlightChain.add(newFlight);
									aNewFlightChain.add(originalFlights.get(1).clone());
									insertPathToOpenList(aNewFlightChain);
									stretchable = true;
								}
							}
						} else {
							//ok open next
							newFlight.setArrivalTime(adjustedArrival);
							ArrayList<Flight> aNewFlightChain = new ArrayList<Flight>();
							aNewFlightChain.add(newFlight);
							aNewFlightChain.add(originalFlights.get(1).clone());
							insertPathToOpenList(aNewFlightChain);
							stretchable = true;
						}
					}
				}
			}
			// try stretch
			if (stretchable && getJointFlightPosition(newFlight) == 1) {
				ArrayList<Flight> newFlightList =  new ArrayList<Flight>();
				newFlightList.add(newFlight);
				openJointFlightNode(newFlight, newFlightList);
			}
		}
		
		// try cancel
		ArrayList<Flight> newFlightList =  new ArrayList<Flight>();
		newFlightList.add(thisFlight.clone());
		openCancelFlightNode(thisFlight, newFlightList);
	}
	
	/** 
	 * thisFlight is the last flight in the opened flight ArrayList
	 * nextFlight is new opened flight
	 * @param oldFlights
	 * @throws ParseException 
	 * @throws FlightDurationNotFound 
	 * @throws CloneNotSupportedException 
	 */
	public void openNextNode(ArrayList<Flight> oldFlights) throws ParseException, FlightDurationNotFound, CloneNotSupportedException {
		Flight thisFlight = oldFlights.get(oldFlights.size() - 1);
		Flight lastFlight = oldFlights.get(oldFlights.size() - 2);
		int thisFlightIndex = getFlightIndexByFlightId(thisFlight.getFlightId());
		int nextFlightIndex = thisFlightIndex + 1;
		
		// track back
		ArrayList<Flight> optimizedFlightList = cloneList(oldFlights);
		for (int i = oldFlights.size() - 2; i >= 0; i--){
			Flight trackFlight = optimizedFlightList.get(i);
			Flight nextFlight = optimizedFlightList.get(i + 1);
			if (!isNewFlight(trackFlight)){
				if (getPlannedDeparture(trackFlight).after(trackFlight.getDepartureTime())) {
					Date currentDeparture = trackFlight.getDepartureTime();
					Date nextFlightDeparture = nextFlight.getDepartureTime();
					int groundingTimeBetween = getGroundingTime(trackFlight, nextFlight);
					Date trackFlightTempArrival = addMinutes(nextFlightDeparture, -groundingTimeBetween);
					// get minimum departure time of track flight
					Date trackFlightTempDeparture = getDepartureTimeByArrivalTime(trackFlight, trackFlightTempArrival);
					trackFlightTempDeparture = currentDeparture.before(trackFlightTempDeparture) ? trackFlightTempDeparture : currentDeparture;
					// airport close start time
					Date closeStartTime = getAirportDepartureCloseStart(trackFlight.getSourceAirPort());
					trackFlight.setDepartureTime(trackFlightTempDeparture.before(closeStartTime) ? trackFlightTempDeparture : closeStartTime);
					trackFlight.setArrivalTime(getArrivalTimeByDepartureTime(trackFlight, trackFlightTempDeparture.before(closeStartTime) ? trackFlightTempDeparture : closeStartTime));
				}
			}
		}
		
		Flight thisFlightOpt = optimizedFlightList.get(optimizedFlightList.size() - 1);
		Flight lastFlightOpt = optimizedFlightList.get(optimizedFlightList.size() - 2);

		if (nextFlightIndex < originalFlights.size()) {
			// adjust this flight by early departure, delay, stretch and cancel
			// try early departure
			Date earlyDepTime = getPossibleEarlierDepartureTime(thisFlight, thisFlight.getSourceAirPort(), false, lastFlight);
			if (earlyDepTime != null && !thisFlight.isInternationalFlight()) {
				if (earlyDepTime.compareTo(thisFlight.getDepartureTime()) != 0){
					boolean stretchable = false;
					Flight newFlight = thisFlight.clone();
					newFlight.setDepartureTime(earlyDepTime);
					newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, earlyDepTime));
					Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
					if (adjustedArrival != null){
						if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
							//ok open next
							ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
							aNewFlightChain.remove(aNewFlightChain.size() - 1);
							aNewFlightChain.add(newFlight);
							aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
							insertPathToOpenList(aNewFlightChain);
							stretchable = true;
						} else {
							Date newDeparture = getDepartureTimeByArrivalTime(newFlight, adjustedArrival);
							if (!isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
								newFlight.setDepartureTime(newDeparture);
								if (isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
									Date newTempDelayDep = getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, lastFlight);
									if (newTempDelayDep != null){
										newFlight.setDepartureTime(newTempDelayDep);
										newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, newTempDelayDep));
										if (!isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
											ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
											aNewFlightChain.remove(aNewFlightChain.size() - 1);
											aNewFlightChain.add(newFlight);
											aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
											insertPathToOpenList(aNewFlightChain);
											stretchable = true;
										}
									}
								} else {
									//ok open next
									newFlight.setArrivalTime(adjustedArrival);
									ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
									aNewFlightChain.remove(aNewFlightChain.size() - 1);
									aNewFlightChain.add(newFlight);
									aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
									insertPathToOpenList(aNewFlightChain);
									stretchable = true;
								}
							}
						}
					}
					// try stretch
					if (stretchable && getJointFlightPosition(newFlight) == 1) {
						ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
						aNewFlightChain.remove(aNewFlightChain.size() - 1);
						aNewFlightChain.add(newFlight);
						openJointFlightNode(newFlight, aNewFlightChain);
					}
				}
			}
			
			// try delay
			Date delayDepTime = getPossibleDelayDeparture(thisFlightOpt, thisFlightOpt.getSourceAirPort(), false, lastFlightOpt);
			if (delayDepTime != null) {
				boolean stretchable = false;
				Flight newFlight = thisFlightOpt.clone();

				newFlight.setDepartureTime(delayDepTime);
				newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, delayDepTime));
				Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
				if (adjustedArrival != null){
					if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
						//ok open next
						ArrayList<Flight> aNewFlightChain = cloneList(optimizedFlightList);
						aNewFlightChain.remove(aNewFlightChain.size() - 1);
						aNewFlightChain.add(newFlight);
						aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
						insertPathToOpenList(aNewFlightChain);
						stretchable = true;
					} else {
						Date newDeparture = getDepartureTimeByArrivalTime(newFlight, adjustedArrival);
						if (!isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
							newFlight.setDepartureTime(newDeparture);
							if (isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
								Date newTempDelayDep = getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, lastFlightOpt);
								if (newTempDelayDep != null){
									newFlight.setDepartureTime(newTempDelayDep);
									newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, newTempDelayDep));
									if (!isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
										ArrayList<Flight> aNewFlightChain = cloneList(optimizedFlightList);
										aNewFlightChain.remove(aNewFlightChain.size() - 1);
										aNewFlightChain.add(newFlight);
										aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
										insertPathToOpenList(aNewFlightChain);
										stretchable = true;
									}
								}
							} else {
								//ok open next
								newFlight.setArrivalTime(adjustedArrival);
								ArrayList<Flight> aNewFlightChain = cloneList(optimizedFlightList);
								aNewFlightChain.remove(aNewFlightChain.size() - 1);
								aNewFlightChain.add(newFlight);
								aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
								insertPathToOpenList(aNewFlightChain);
								stretchable = true;
							}
						}
					}
				}
				// try stretch
				if (stretchable && getJointFlightPosition(newFlight) == 1) {
					ArrayList<Flight> aNewFlightChain = cloneList(optimizedFlightList);
					aNewFlightChain.remove(aNewFlightChain.size() - 1);
					aNewFlightChain.add(newFlight);
					openJointFlightNode(newFlight, aNewFlightChain);
				}
			}
			
			// try cancel
			ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
			aNewFlightChain.remove(aNewFlightChain.size() - 1);
			aNewFlightChain.add(thisFlight.clone());
			openCancelFlightNode(thisFlight, aNewFlightChain);
		} else {
			// last flight is loaded
			// try early departure
			Date earlyDepTime = getPossibleEarlierDepartureTime(thisFlight, thisFlight.getSourceAirPort(), false, lastFlight);
			if (earlyDepTime != null && !thisFlight.isInternationalFlight()) {
				Flight newFlight = thisFlight.clone();
				if (earlyDepTime.compareTo(thisFlight.getDepartureTime()) != 0){
					newFlight.setDepartureTime(earlyDepTime);
					newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, earlyDepTime));
					Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
					if (adjustedArrival != null){
						if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
							//ok open next
							ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
							aNewFlightChain.remove(aNewFlightChain.size() - 1);
							aNewFlightChain.add(newFlight);
							finishedArrayList.add(aNewFlightChain);
						} else {
							Date newDeparture = getDepartureTimeByArrivalTime(newFlight, adjustedArrival);
							if (!isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
								newFlight.setDepartureTime(newDeparture);
								if (isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
									Date newTempDelayDep = getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, lastFlight);
									if (newTempDelayDep != null){
										newFlight.setDepartureTime(newTempDelayDep);
										newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, newTempDelayDep));
										if (!isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
											ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
											aNewFlightChain.remove(aNewFlightChain.size() - 1);
											aNewFlightChain.add(newFlight);
											finishedArrayList.add(aNewFlightChain);
										}
									}
								} else {
									//ok open next
									newFlight.setArrivalTime(adjustedArrival);
									ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
									aNewFlightChain.remove(aNewFlightChain.size() - 1);
									aNewFlightChain.add(newFlight);
									finishedArrayList.add(aNewFlightChain);
								}
							}
						}
					}
				}
			}
			
			// try delay
			Date delayDepTime = getPossibleDelayDeparture(thisFlightOpt, thisFlightOpt.getSourceAirPort(), false, lastFlightOpt);
			if (delayDepTime != null) {
				Flight newFlight = thisFlightOpt.clone();
				newFlight.setDepartureTime(delayDepTime);
				
				newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, delayDepTime));
				Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
				if (adjustedArrival != null){
					if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
						//ok open next
						ArrayList<Flight> aNewFlightChain = cloneList(optimizedFlightList);
						aNewFlightChain.remove(aNewFlightChain.size() - 1);
						aNewFlightChain.add(newFlight);
						finishedArrayList.add(aNewFlightChain);
					} else {
						Date newDeparture = getDepartureTimeByArrivalTime(newFlight, adjustedArrival);
						if (!isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
							newFlight.setDepartureTime(newDeparture);
							if (isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
								Date newTempDelayDep = getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, lastFlightOpt);
								if (newTempDelayDep != null){
									newFlight.setDepartureTime(newTempDelayDep);
									newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, newTempDelayDep));
									if (!isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
										ArrayList<Flight> aNewFlightChain = cloneList(optimizedFlightList);
										aNewFlightChain.remove(aNewFlightChain.size() - 1);
										aNewFlightChain.add(newFlight);
										finishedArrayList.add(aNewFlightChain);
									}
								}
							} else {
								//ok open next
								newFlight.setArrivalTime(adjustedArrival);
								ArrayList<Flight> aNewFlightChain = cloneList(optimizedFlightList);
								aNewFlightChain.remove(aNewFlightChain.size() - 1);
								aNewFlightChain.add(newFlight);
								finishedArrayList.add(aNewFlightChain);
							}
						}
					}
				}
			}
		}
	}
	
	
	public void openCancelFlightNode(Flight origFlight, ArrayList<Flight> origFlightList) throws CloneNotSupportedException, ParseException, FlightDurationNotFound {
		int thisFlightIndex = getFlightIndexByFlightId(origFlight.getFlightId());
		Flight lastFlight = origFlightList.size() > 1 ? origFlightList.get(origFlightList.size() - 2) : null;
		ArrayList<Flight> flightChain = cloneList(origFlightList);
		flightChain.remove(flightChain.size() - 1);
		boolean adjustable = false;
		
		Flight newFlight = origFlight.clone();
		newFlight.setFlightId(getNextFlightId());
		newFlight.setPlannedFlight(null);;
		if (thisFlightIndex > 0) {
			boolean newFlag = false;
			for (Flight flight : flightChain){
				if (isNewFlight(flight)){
					newFlag = true;
				}
			}
			if (!newFlag){
				Date depTime = addMinutes(lastFlight.getArrivalTime(), getGroundingTime(lastFlight, newFlight));
				newFlight.setDepartureTime(depTime);
				Date adjustedDep = getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, lastFlight);
				if (adjustedDep != null) {
					adjustable = true;
					newFlight.setDepartureTime(adjustedDep);
				}
			}
		} else {
			adjustable = true;
		}
		if (adjustable) {
			for (int i = thisFlightIndex + 1; i < originalFlights.size(); i++) {
				Flight nextFlight = originalFlights.get(i).clone();
				AirPort destAirport = nextFlight.getSourceAirPort();
				Flight aNewFlight = newFlight.clone();
				if (destAirport.getId().equals(aNewFlight.getSourceAirPort().getId())) {
					// valid parking
					if (thisFlightIndex > 0) {
						if (aNewFlight.getFlightId() == 1340){
							
						}
						if (!isValidParking(lastFlight.getArrivalTime(), nextFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
							continue;
						} else {
							ArrayList<Flight> aNewFlightChain = cloneList(flightChain);
							aNewFlightChain.add(nextFlight);
							insertPathToOpenList(aNewFlightChain);
							continue;
						}
					}
				}
				
				// international flight is not eligible
				if (isInternational(aNewFlight.getSourceAirPort().getId(), destAirport.getId())){
					continue;
				}
				
				// aircraft constraint
				if (!isEligibalAircraft(aNewFlight.getAssignedAir(), aNewFlight.getSourceAirPort(), destAirport)){
					continue;
				}

				long flightTime = getFlightTime(aNewFlight.getSourceAirPort().getId(), destAirport.getId(), aNewFlight.getAssignedAir());
				if (flightTime > 0){
					aNewFlight.setDesintationAirport(nextFlight.getSourceAirPort());
					aNewFlight.setArrivalTime(getArrivalTimeByDepartureTime(aNewFlight, aNewFlight.getDepartureTime()));
					Date adjustedArrival = getPossibleArrivalTime(aNewFlight, aNewFlight.getDesintationAirport());
					if (adjustedArrival != null){
						if (adjustedArrival.compareTo(aNewFlight.getArrivalTime()) == 0){
							//ok open next
							ArrayList<Flight> aNewFlightChain = cloneList(flightChain);
							aNewFlightChain.add(aNewFlight);
							aNewFlightChain.add(nextFlight);
							if (!isValidParking(aNewFlight.getArrivalTime(), nextFlight.getDepartureTime(), aNewFlight.getDesintationAirport())){
								continue;
							}
							insertPathToOpenList(aNewFlightChain);
						} else {
							Date newDeparture = addMinutes(adjustedArrival, -flightTime);
							if (!isDepTimeAffected(newDeparture, aNewFlight.getSourceAirPort())){
								aNewFlight.setDepartureTime(newDeparture);
								if (isDepTimeAffectedByNormal(newDeparture, aNewFlight.getSourceAirPort())){
									Date newTempDelayDep = getPossibleDelayDeparture(aNewFlight, aNewFlight.getSourceAirPort(), false, lastFlight);
									if (newTempDelayDep != null){
										aNewFlight.setDepartureTime(newTempDelayDep);
										aNewFlight.setArrivalTime(getArrivalTimeByDepartureTime(aNewFlight, newTempDelayDep));
										if (!isArvTimeAffected(aNewFlight.getArrivalTime(), aNewFlight.getDesintationAirport())){
											ArrayList<Flight> aNewFlightChain = cloneList(flightChain);
											aNewFlightChain.add(aNewFlight);
											aNewFlightChain.add(nextFlight);
											if (!isValidParking(aNewFlight.getArrivalTime(), nextFlight.getDepartureTime(), aNewFlight.getDesintationAirport())){
												continue;
											}
											insertPathToOpenList(aNewFlightChain);
										}
									}
								} else {
									//ok open next
									aNewFlight.setArrivalTime(adjustedArrival);
									ArrayList<Flight> aNewFlightChain = cloneList(flightChain);
									aNewFlightChain.add(aNewFlight);
									aNewFlightChain.add(nextFlight);
									if (!isValidParking(aNewFlight.getArrivalTime(), nextFlight.getDepartureTime(), aNewFlight.getDesintationAirport())){
										continue;
									}
									insertPathToOpenList(aNewFlightChain);
								}
							}
						}
					}
				}
				
			}
		}
	}
	
	/**
	 * open a node for joint flight
	 * @param originalFlight
	 * @param flightChain
	 * @throws CloneNotSupportedException
	 * @throws FlightDurationNotFound
	 */
	public void openJointFlightNode(Flight originalFlight, ArrayList<Flight> origFlightChain) throws CloneNotSupportedException, FlightDurationNotFound{
		Flight jointFlight = getJointFlight(originalFlight);
		ArrayList<Flight> flightChain = cloneList(origFlightChain);
		flightChain.remove(flightChain.size() - 1);
		if (isAffected(jointFlight) == 1) {
			// departure time is affected
			Flight newFlight = originalFlight.clone();
			newFlight.setDesintationAirport(jointFlight.getDesintationAirport());
			newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, newFlight.getDepartureTime()));
			flightChain.add(newFlight);
			// get next node
			int jointFlightIndex = getFlightIndexByFlightId(jointFlight.getFlightId());
			Flight nextFlight = originalFlights.get(jointFlightIndex + 1).clone();
			flightChain.add(nextFlight);
			insertPathToOpenList(flightChain);
		}
	}
	
	// method utility
	
	/**
	 * if a departure time is in the error time range
	 * @param depTime
	 * @param airport
	 * @return
	 */
	public boolean isDepTimeAffected(Date depTime, AirPort airport) {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (depTime.after(aClose.getStartTime())
						&& depTime.before(aClose.getEndTime())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * if a arrival time is in the error time range
	 * @param arvTime
	 * @param airport
	 * @return
	 */
	public boolean isArvTimeAffected(Date arvTime, AirPort airport) {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (arvTime.after(aClose.getStartTime())
					&& arvTime.before(aClose.getEndTime())) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean isDepTimeAffectedByNormal(Date depTime, AirPort airport) throws ParseException{
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
			
			if (depTime.after(aCloseDate)
					&& depTime.before(aOpenDate)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * departure time is eligible to set earlier.
	 * @param depTime
	 * @param airport
	 * @return
	 */
	public boolean isEarlyDeparturePossible(Date depTime, AirPort airport) {
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (depTime.after(aClose.getStartTime())
						&& depTime.before(addMinutes(aClose.getStartTime(), 360))) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * get next valid arrival time
	 * @param flight
	 * @param airport
	 * @return
	 * @throws ParseException
	 */
	public Date getPossibleArrivalTime(Flight flight, AirPort airport) throws ParseException{
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (flight.getArrivalTime().after(aClose.getStartTime())
					&& flight.getArrivalTime().before(aClose.getEndTime())) {
				if (isNewFlight(flight)){
					return aClose.getEndTime();
				} else {
					if (isValidDelay(getPlannedArrival(flight), aClose.getEndTime(), flight.isInternationalFlight())){
						return aClose.getEndTime();
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
			
			if (flight.getArrivalTime().after(aCloseDate)
					&& flight.getArrivalTime().before(aOpenDate)) {
				if (isNewFlight(flight)){
					return aOpenDate;
				} else {
					if (isValidDelay(getPlannedDeparture(flight), aOpenDate, flight.isInternationalFlight())){
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
	 * @param flight
	 * @param airport
	 * @return
	 * @throws ParseException 
	 */
	public Date getPossibleDelayDeparture(Flight flight, AirPort airport, boolean isFirstFlight, Flight lastFlight) throws ParseException {
		Date tempDepTime = null;
		if (lastFlight != null){
			Date shiftDeparture = addMinutes(lastFlight.getArrivalTime(), getGroundingTime(lastFlight, flight));
			tempDepTime = shiftDeparture.before(flight.getDepartureTime()) ? flight.getDepartureTime() : shiftDeparture;
		} else {
			tempDepTime = flight.getDepartureTime();
		}
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (tempDepTime.after(aClose.getStartTime())
						&& tempDepTime.before(aClose.getEndTime())) {
					if (isFirstFlight) {
						if (isNewFlight(flight)){
							return aClose.getEndTime();
						} else {
							if (isValidDelay(getPlannedDeparture(flight), aClose.getEndTime(), flight.isInternationalFlight())){
								return aClose.getEndTime();
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
			
			if (tempDepTime.after(aCloseDate)
					&& tempDepTime.before(aOpenDate)) {
				if (isNewFlight(flight)){
					return aOpenDate;
				} else {
					if (isValidDelay(getPlannedDeparture(flight), aOpenDate, flight.isInternationalFlight())){
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
	 * @return if possible return the time, otherwise return null
	 * @throws ParseException 
	 */
	public Date getPossibleEarlierDepartureTime(Flight flight, AirPort airport, boolean hasEarlyLimit, Flight lastFlight) throws ParseException {
		if (lastFlight != null) {
			// not the first flight
			if (getPlannedDeparture(flight).before(lastFlight.getArrivalTime())){
				return null;
			}
			for (AirPortClose aClose : airport.getCloseSchedule()) {
				if (!aClose.isAllowForTakeoff()) {
					if (isNewFlight(flight)){
						if (flight.getDepartureTime().after(aClose.getStartTime())
								&& flight.getDepartureTime().before(aClose.getStartTime())) {
							Date tempDep = addMinutes(lastFlight.getArrivalTime(), Flight.getGroundingTime(lastFlight.getFlightId(), flight.getFlightId()));
							if (tempDep.after(aClose.getStartTime())) {
								return null;
							} else {
								return tempDep;
							}
						}
					} else {
						if (getPlannedDeparture(flight).after(aClose.getStartTime())
								&& getPlannedDeparture(flight).before(addMinutes(aClose.getStartTime(), 360))) {
							Date tempDep = addMinutes(lastFlight.getArrivalTime(), Flight.getGroundingTime(lastFlight.getFlightId(), flight.getFlightId()));
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
					if (isNewFlight(flight)){
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
			
			if (flight.getDepartureTime().after(aCloseDate)
					&& flight.getDepartureTime().before(aOpenDate)) {
				if (lastFlight != null) {
					// not the first flight
					if (isNewFlight(flight)){
						Date tempDep = addMinutes(lastFlight.getArrivalTime(), Flight.getGroundingTime(lastFlight.getFlightId(), flight.getFlightId()));
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
					if (isNewFlight(flight)){
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
	
	
	public Date getAirportDepartureCloseStart(AirPort airport){
		for (AirPortClose aClose : airport.getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				return aClose.getStartTime();
			}
		}
		return null;
	}
	
	/**
	 * get flight time between two airports
	 * @param airport1Id
	 * @param airport2Id
	 * @param aircraft
	 * @return
	 */
	public long getFlightTime(String airport1Id, String airport2Id, Aircraft aircraft){
		String searchKey = aircraft.getType();
		searchKey += "_";
		searchKey += airport1Id;
		searchKey += "_";
		searchKey += airport2Id;
		
		if (InitData.fightDurationMap.containsKey(searchKey)){
			long flightTime = InitData.fightDurationMap.get(searchKey);
			return flightTime;
		}else{
			return 0;
		}
		
	}
	
	/**
	 * the airline is international
	 * @param airport1
	 * @param airport2
	 * @return
	 */
	public boolean isInternational(String airport1, String airport2){
		if (InitData.domesticAirportList.contains(airport1) && InitData.domesticAirportList.contains(airport2)){
			return false;
		}
		return true;
	}
	
	/**
	 * the airline is valid
	 * @param aircraft
	 * @param sourceAir
	 * @param destAir
	 * @return
	 */
	public boolean isEligibalAircraft(Aircraft aircraft, AirPort sourceAir, AirPort destAir){
		String searchKey = aircraft.getId() + "_" + sourceAir.getId() + "_" + destAir.getId();
		return InitData.airLimitationList.contains(searchKey) ? false : true;
	}
	
	/**
	 * check if the flight delay is valid
	 * @param planTime
	 * @param adjustTime
	 * @param isInternational
	 * @return
	 */
	public boolean isValidDelay(Date planTime, Date adjustTime, boolean isInternational){
		if (getMinuteDifference(adjustTime, planTime) > (isInternational ? Aircraft.INTERNATIONAL_MAXIMUM_DELAY_TIME*60: Aircraft.DOMESTIC_MAXIMUM_DELAY_TIME*60)){
			return false;
		}
		
		return true;
	}
	
	/** 
	 * get flight index in the original flight chain by flight id
	 * @param aFlightId
	 * @return
	 */
	public int getFlightIndexByFlightId(int aFlightId) {
		for (int i = 0; i < originalFlights.size(); i++) {
			if (originalFlights.get(i).getFlightId() == aFlightId)
				return i;
		}
		return -1;
	}
	
	/**
	 * check if the aircraft parking is valid
	 * @param arrivalTime
	 * @param departureTime
	 * @param airport
	 * @return
	 */
	public boolean isValidParking(Date arrivalTime, Date departureTime, AirPort airport){
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
	 * @param flight
	 * @return
	 */
	public Date getPlannedArrival(Flight flight){
		return flight.getPlannedFlight().getArrivalTime();
	}
	
	/**
	 * get original departure time
	 * @param flight
	 * @return
	 */
	public Date getPlannedDeparture(Flight flight){
		return flight.getPlannedFlight().getDepartureTime();
	}
	
	/**
	 * get grounding time between two flights
	 * @param flight1
	 * @param flight2
	 * @return
	 */
	public int getGroundingTime(Flight flight1, Flight flight2) {
		return Flight.getGroundingTime(flight1.getFlightId(), flight2.getFlightId());
	}
	
	/**
	 * compress the departure time of next flight
	 * @param lastFlight
	 * @param thisFlight
	 * @return
	 */
	public Date getCompressedDeparture(Flight lastFlight, Flight thisFlight) {
		Date thisPlannedDeparture = getPlannedDeparture(thisFlight);
		Date thisShiftedDeparture = addMinutes(lastFlight.getArrivalTime(), getGroundingTime(lastFlight, thisFlight));
		return thisPlannedDeparture.before(thisShiftedDeparture) ? thisPlannedDeparture : thisShiftedDeparture;
	}
	
	/**
	 * a flight is a new flight
	 * @param flight
	 * @return
	 */
	public boolean isNewFlight(Flight flight) {
		return flight.getFlightId() > InitData.plannedMaxFligthId;
	}
	
	/**
	 * if a flight is joint flight
	 * @param flight
	 * @return joint flight position
	 */
	public int getJointFlightPosition(Flight flight){
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
	 * @param flight
	 * @return
	 */
	public Flight getJointFlight(Flight flight){
		return InitData.jointFlightMap.get(flight.getFlightId());
	}
	
	/**
	 * get how this flight is affected
	 * @param flight
	 * @return 1 departure, 2 arrival, 0 no
	 */
	public int isAffected(Flight flight) {
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
	 *  get next flight id
	 * @return
	 */
	public int getNextFlightId(){
		int maxFlightId = InitData.maxFligthId;
		InitData.maxFligthId = maxFlightId + 1;
		return maxFlightId + 1;
	}
	
	/**
	 * get new departure time by new arrival time
	 * @param flight
	 * @param newArrivalTime
	 * @return
	 */
	public Date getDepartureTimeByArrivalTime(Flight flight, Date newArrivalTime) {
		if (flight.getPlannedAir() != null){
			if (getJointFlightPosition(flight) == 1){
				if (!flight.getDesintationAirport().getId().equals(flight.getPlannedFlight().getDesintationAirport().getId())){
					long flightTime = getFlightTime(flight.getSourceAirPort().getId(), flight.getDesintationAirport().getId(), originalAircraft);
					if (flightTime > 0){
						return addMinutes(newArrivalTime, -flightTime);
					} else {
						int flightTime1 = (int) getMinuteDifference(getPlannedArrival(flight), getPlannedDeparture(flight));
						int flightTime2 = (int) getMinuteDifference(getPlannedArrival(getJointFlight(flight)), getPlannedDeparture(getJointFlight(flight)));
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
			long flightTime = getFlightTime(flight.getSourceAirPort().getId(), flight.getDesintationAirport().getId(), originalAircraft);
			if (flightTime > 0){
				return addMinutes(newArrivalTime, -flightTime);
			}
		}
		return null;
	}
	
	
	public Date getArrivalTimeByDepartureTime(Flight flight, Date newDepartureTime){
		if (!isNewFlight(flight)){
			if (getJointFlightPosition(flight) == 1){
				if (!flight.getDesintationAirport().getId().equals(flight.getPlannedFlight().getDesintationAirport().getId())){
					long flightTime = getFlightTime(flight.getSourceAirPort().getId(), flight.getDesintationAirport().getId(), originalAircraft);
					if (flightTime > 0){
						return addMinutes(newDepartureTime, flightTime);
					} else {
						int flightTime1 = (int) getMinuteDifference(getPlannedArrival(flight), getPlannedDeparture(flight));
						int flightTime2 = (int) getMinuteDifference(getPlannedArrival(getJointFlight(flight)), getPlannedDeparture(getJointFlight(flight)));
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
			long flightTime = getFlightTime(flight.getSourceAirPort().getId(), flight.getDesintationAirport().getId(), originalAircraft);
			if (flightTime > 0){
				return addMinutes(newDepartureTime, flightTime);
			}
		}
		return null;
	}
	
	/**
	 * insert into open list
	 * @param path
	 * @throws CloneNotSupportedException
	 */
	public void insertPathToOpenList(ArrayList<Flight> path) throws CloneNotSupportedException{
		Flight anchorFlight = path.get(path.size() - 1);
		ArrayList<Flight> adjustedPart = cloneList(path);
		adjustedPart.remove(path.size() - 1);
		ArrayList<Flight> orgList = new ArrayList<Flight>();
		for (Flight flight : originalFlights){
			if (flight.getFlightId() == anchorFlight.getFlightId()){
				break;
			}
			orgList.add(flight);
		}
		long deltaCost = calDeltaCost(orgList, adjustedPart);
		if (openArrayList.containsKey(deltaCost)){
			openArrayList.get(deltaCost).add(path);
		} else {
			ArrayList<ArrayList<Flight>> pathList = new ArrayList<ArrayList<Flight>>();
			pathList.add(path);
			openArrayList.put(deltaCost, pathList);
		}
	}
	
	// common utility
	/** 
	 * add minutes to date
	 * @param date
	 * @param minutes
	 * @return
	 */
	public Date addMinutes(Date date, long minutes){
		Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    long t= cal.getTimeInMillis();
	    return new Date(t + (minutes * 60000));
	}
	
	/**
	 * get different between two date in minute
	 * @param time1
	 * @param time2
	 * @return
	 */
	public double getMinuteDifference(Date time1, Date time2){
		return (time1.getTime() - time2.getTime()) / (1000 * 60);
	}
	
	/**
	 * print string value
	 * @param value
	 */
	public void print(String value){
		System.out.println(value);
	}
	/**
	 * print integer value
	 * @param value
	 */
	public void print(int value){
		System.out.println(value);
	}
	
	/**
	 * deep clone array list of flights
	 * @param flights
	 * @return newList
	 * @throws CloneNotSupportedException 
	 */
	public ArrayList<Flight> cloneList(ArrayList<Flight> flights) throws CloneNotSupportedException{
		ArrayList<Flight> newList = new ArrayList<Flight>();
		for (Flight flight : flights){
			newList.add(flight.clone());
		}
		return newList;
	}
	
	/**
	 * derived from calCostByAir in Util
	 * @param orgAir
	 * @param newAir
	 * @return
	 */
	public long calDeltaCost(ArrayList<Flight> orgAir, ArrayList<Flight> newAir) {
		BigDecimal cost = new BigDecimal("0");
		// org air
		for (int i = 0; i < orgAir.size(); i++) {
			
			Flight orgFlight = orgAir.get(i);
			boolean existFlg = false;
			// new air
			for (int j = 0; j < newAir.size(); j++) {
				
				Flight newFlight = newAir.get(j);
				// empty
				if (i == 0 && isNewFlight(newFlight)) {
					cost = cost.add(new BigDecimal("5000"));
				}
				// exist
				if (orgFlight.getFlightId() == newFlight.getFlightId()) {
					existFlg = true;
					// delay or move up
					if (!orgFlight.getDepartureTime().equals(newFlight.getDepartureTime())) {
						BigDecimal hourDiff = Utils.hoursBetweenTime(newFlight.getDepartureTime(), orgFlight.getDepartureTime());
						
						if (hourDiff.signum() == -1){
							cost = cost.add(new BigDecimal("150").multiply(hourDiff.abs()).multiply(orgFlight.getImpCoe()));
						} else {
							cost = cost.add(new BigDecimal("100").multiply(hourDiff.abs()).multiply(orgFlight.getImpCoe()));
						}
					}
					// joint stretch
					if (InitData.jointFlightMap.get(newFlight.getFlightId()) != null) {
						if (!newFlight.getDesintationAirport().getId().equals((orgFlight.getDesintationAirport().getId()))) {
							Flight nextFlight = InitData.jointFlightMap.get(newFlight.getFlightId());
							
							cost = cost.add(new BigDecimal("750").multiply(newFlight.getImpCoe()));
							cost = cost.add(new BigDecimal("750").multiply(nextFlight.getImpCoe()));
							
						}
						
					}
					
				} 
			}
			
			// cancel
			if (!existFlg) {
				// not 2nd of joint flight
				if (!InitData.jointFlightMap.containsKey(orgFlight.getFlightId()) || InitData.jointFlightMap.get(orgFlight.getFlightId()) != null) {
					cost = cost.add(new BigDecimal("1000").multiply(orgFlight.getImpCoe()));
				}
			}
			
		}
		
		return cost.longValue();
    }
	
	public void printAllFlightsIsList(ArrayList<Flight> list){
		String s = "";
		for (Flight f : list){
			s += f.getFlightId() + "-";
		}
		System.out.println(s);
	}
	
	public void printAllPathInList(TreeMap<Long, ArrayList<ArrayList<Flight>>> tm){
		Iterator<Entry<Long, ArrayList<ArrayList<Flight>>>> it = tm.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Long, ArrayList<ArrayList<Flight>>> pair = (Map.Entry<Long, ArrayList<ArrayList<Flight>>>) it.next();
	        long cost = pair.getKey();
	        ArrayList<ArrayList<Flight>> flightsList = pair.getValue();
	        for (ArrayList<Flight> fl : flightsList){
	        	String op = cost + "|";
	        	for (Flight f : fl){
	        		op += f.getFlightId() + "-";
	        	}
	        	print(op);
	        }
	    }
	}
	
	public void printListInList(ArrayList<ArrayList<Flight>> al){
		for (ArrayList<Flight> fl : al){
			String op = "";
        	for (Flight f : fl){
        		op += f.getFlightId() + "-";
        	}
        	print(op);
        }
	}
}
