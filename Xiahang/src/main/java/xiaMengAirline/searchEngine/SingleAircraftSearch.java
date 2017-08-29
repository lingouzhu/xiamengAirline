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
import xiaMengAirline.utils.InitData;
import xiaMengAirline.utils.Utils;

public class SingleAircraftSearch {
	private Aircraft originalAircraft;
	private ArrayList<Flight> originalFlights;
	private TreeMap<Long, ArrayList<ArrayList<Flight>>> openArrayList = new TreeMap<Long, ArrayList<ArrayList<Flight>>>();
	private ArrayList<ArrayList<Flight>> finishedArrayList = new ArrayList<ArrayList<Flight>>();
	private boolean isFullSearch;
	
	public SingleAircraftSearch(Aircraft aAircraft, boolean isFullSearch) throws CloneNotSupportedException {
		originalAircraft = aAircraft.clone();
		originalFlights = (ArrayList<Flight>) originalAircraft.getFlightChain();
	}
	
	public ArrayList<Aircraft> getAdjustedAircraftPair() throws CloneNotSupportedException, AircraftNotAdjustable{
		if (originalFlights.size() < 2){
			throw new AircraftNotAdjustable(originalAircraft);
		}
		
		boolean started = false;
		boolean finished = false;
		// loop to open all solutions
		while ((!started || !finished)){
			try {
				finished = processNextPath(started);
			} catch (CloneNotSupportedException | FlightDurationNotFound | ParseException e) {
				e.printStackTrace();
			}
			started = true;
			if (finishedArrayList.size() > 0){
				if (!isFullSearch){
					finished = true;
				}
			}
		}
		if (finishedArrayList.size() == 0){
			// no solution
			throw new AircraftNotAdjustable(originalAircraft);
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
		
		boolean adjusted = false;
		if (originalFlights.size() == bestList.size()){
			for (int i = 0; i < bestList.size() - 1; i++){
				if (originalFlights.get(i).getFlightId() != bestList.get(i).getFlightId()
						|| originalFlights.get(i).getArrivalTime() != bestList.get(i).getArrivalTime()
						|| originalFlights.get(i).getDepartureTime() != bestList.get(i).getDepartureTime()
						|| originalFlights.get(i).getSourceAirPort() != bestList.get(i).getSourceAirPort()
						|| originalFlights.get(i).getDesintationAirport() != bestList.get(i).getDesintationAirport()){
					adjusted = true;
				}
			}
		} else {
			adjusted = true;
		}
		if (!adjusted){
			return null;
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
		Flight thisFlight = originalFlights.get(0).clone();
		// try earlier departure
		if (thisFlight.isAdjustable()) {
			Date earlyDepTime = BusinessDomain.getPossibleEarlierDepartureTime(thisFlight, thisFlight.getSourceAirPort(), false, null);
			if (earlyDepTime != null && !thisFlight.isInternationalFlight()) {
				if (earlyDepTime.compareTo(thisFlight.getDepartureTime()) != 0){
					boolean stretchable = false;
					Flight newFlight = thisFlight.clone();
					newFlight.setDepartureTime(earlyDepTime);
					newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, earlyDepTime, originalAircraft));
					Date adjustedArrival = BusinessDomain.getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
					if (adjustedArrival != null){
						if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
							//ok open next
							ArrayList<Flight> aNewFlightChain = new ArrayList<Flight>();
							aNewFlightChain.add(newFlight);
							aNewFlightChain.add(originalFlights.get(1).clone());
							insertPathToOpenList(aNewFlightChain);
							stretchable = true;
						} else {
							Date newDeparture = BusinessDomain.getDepartureTimeByArrivalTime(newFlight, adjustedArrival, originalAircraft);
							if (!BusinessDomain.isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
								newFlight.setDepartureTime(newDeparture);
								if (BusinessDomain.isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
									Date newTempDelayDep = BusinessDomain.getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, null);
									if (newTempDelayDep != null){
										newFlight.setDepartureTime(newTempDelayDep);
										newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, newTempDelayDep, originalAircraft));
										if (!BusinessDomain.isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
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
					if (stretchable && BusinessDomain.getJointFlightPosition(newFlight) == 1 && 
							!newFlight.isInternationalFlight() && !BusinessDomain.getJointFlight(newFlight).isInternationalFlight()) {
						ArrayList<Flight> newFlightList =  new ArrayList<Flight>();
						newFlightList.add(newFlight);
						openJointFlightNode(newFlight, newFlightList);
					}
				}
			}
			
			// try delay departure
			Date delayDepTime = BusinessDomain.getPossibleDelayDeparture(thisFlight, thisFlight.getSourceAirPort(), true, null);
			if (delayDepTime != null) {
				boolean stretchable = false;
				Flight newFlight = thisFlight.clone();
				newFlight.setDepartureTime(delayDepTime);
				newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, delayDepTime, originalAircraft));
				Date adjustedArrival = BusinessDomain.getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
				if (adjustedArrival != null){
					if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
						//ok open next
						ArrayList<Flight> aNewFlightChain = new ArrayList<Flight>();
						aNewFlightChain.add(newFlight);
						aNewFlightChain.add(originalFlights.get(1).clone());
						insertPathToOpenList(aNewFlightChain);
						stretchable = true;
					} else {
						
						Date newDeparture = BusinessDomain.getDepartureTimeByArrivalTime(newFlight, adjustedArrival, originalAircraft);
						if (!BusinessDomain.isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
							newFlight.setDepartureTime(newDeparture);
							if (BusinessDomain.isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
								Date newTempDelayDep = BusinessDomain.getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, null);
								if (newTempDelayDep != null){
									newFlight.setDepartureTime(newTempDelayDep);
									newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, newTempDelayDep, originalAircraft));
									if (!BusinessDomain.isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
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
				if (stretchable && BusinessDomain.getJointFlightPosition(newFlight) == 1 && 
						!newFlight.isInternationalFlight() && !BusinessDomain.getJointFlight(newFlight).isInternationalFlight()) {
					ArrayList<Flight> newFlightList =  new ArrayList<Flight>();
					newFlightList.add(newFlight);
					openJointFlightNode(newFlight, newFlightList);
				}
			}
			
			// try cancel
			ArrayList<Flight> newFlightList =  new ArrayList<Flight>();
			newFlightList.add(thisFlight.clone());
			openCancelFlightNode(thisFlight, newFlightList);
		} else {
			ArrayList<Flight> aNewFlightChain = new ArrayList<Flight>();
			aNewFlightChain.add(thisFlight.clone());
			aNewFlightChain.add(originalFlights.get(1).clone());
			insertPathToOpenList(aNewFlightChain);
		}
		
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
		int thisFlightIndex = BusinessDomain.getFlightIndexByFlightId(thisFlight.getFlightId(), originalFlights);
		int nextFlightIndex = thisFlightIndex + 1;
		if (thisFlight.isAdjustable()) {
			// track back
			ArrayList<Flight> optimizedFlightList = cloneList(oldFlights);
			for (int i = oldFlights.size() - 2; i >= 0; i--){
				Flight trackFlight = optimizedFlightList.get(i);
				Flight nextFlight = optimizedFlightList.get(i + 1);
				if (!BusinessDomain.isNewFlight(trackFlight)){
					if (BusinessDomain.getPlannedDeparture(trackFlight).after(trackFlight.getDepartureTime())) {
						Date currentDeparture = trackFlight.getDepartureTime();
						Date nextFlightDeparture = nextFlight.getDepartureTime();
						int groundingTimeBetween = BusinessDomain.getGroundingTime(trackFlight, nextFlight);
						Date trackFlightTempArrival = addMinutes(nextFlightDeparture, -groundingTimeBetween);
						// get minimum departure time of track flight
						Date trackFlightTempDeparture = BusinessDomain.getDepartureTimeByArrivalTime(trackFlight, trackFlightTempArrival, originalAircraft);
						trackFlightTempDeparture = currentDeparture.before(trackFlightTempDeparture) ? trackFlightTempDeparture : currentDeparture;
						// airport close start time
						Date closeStartTime = BusinessDomain.getAirportDepartureCloseStart(trackFlight.getSourceAirPort());
						trackFlight.setDepartureTime(trackFlightTempDeparture.before(closeStartTime) ? trackFlightTempDeparture : closeStartTime);
						trackFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(trackFlight, trackFlightTempDeparture.before(closeStartTime) ? trackFlightTempDeparture : closeStartTime, originalAircraft));
					}
				}
			}
			
			Flight thisFlightOpt = optimizedFlightList.get(optimizedFlightList.size() - 1);
			Flight lastFlightOpt = optimizedFlightList.get(optimizedFlightList.size() - 2);

			if (nextFlightIndex < originalFlights.size()) {
				// adjust this flight by early departure, delay, stretch and cancel
				// try early departure
				Date earlyDepTime = BusinessDomain.getPossibleEarlierDepartureTime(thisFlight, thisFlight.getSourceAirPort(), false, lastFlight);
				if (earlyDepTime != null && !thisFlight.isInternationalFlight()) {
					if (earlyDepTime.compareTo(thisFlight.getDepartureTime()) != 0){
						boolean stretchable = false;
						Flight newFlight = thisFlight.clone();
						newFlight.setDepartureTime(earlyDepTime);
						newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, earlyDepTime, originalAircraft));
						Date adjustedArrival = BusinessDomain.getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
						if (adjustedArrival != null){
							if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
								//ok open next
								if (BusinessDomain.isValidParking(lastFlight.getArrivalTime(), newFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
									ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
									aNewFlightChain.remove(aNewFlightChain.size() - 1);
									aNewFlightChain.add(newFlight);
									aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
									insertPathToOpenList(aNewFlightChain);
									stretchable = true;
								}
							} else {
								Date newDeparture = BusinessDomain.getDepartureTimeByArrivalTime(newFlight, adjustedArrival, originalAircraft);
								if (!BusinessDomain.isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
									newFlight.setDepartureTime(newDeparture);
									if (BusinessDomain.isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
										Date newTempDelayDep = BusinessDomain.getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, lastFlight);
										if (newTempDelayDep != null){
											newFlight.setDepartureTime(newTempDelayDep);
											newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, newTempDelayDep, originalAircraft));
											if (!BusinessDomain.isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
												if (BusinessDomain.isValidParking(lastFlight.getArrivalTime(), newFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
													ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
													aNewFlightChain.remove(aNewFlightChain.size() - 1);
													aNewFlightChain.add(newFlight);
													aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
													insertPathToOpenList(aNewFlightChain);
													stretchable = true;
												}
												
											}
										}
									} else {
										//ok open next
										newFlight.setArrivalTime(adjustedArrival);
										if (BusinessDomain.isValidParking(lastFlight.getArrivalTime(), newFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
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
						}
						// try stretch
						if (stretchable && BusinessDomain.getJointFlightPosition(newFlight) == 1 && 
								!newFlight.isInternationalFlight() && !BusinessDomain.getJointFlight(newFlight).isInternationalFlight()) {
							ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
							aNewFlightChain.remove(aNewFlightChain.size() - 1);
							aNewFlightChain.add(newFlight);
							openJointFlightNode(newFlight, aNewFlightChain);
						}
					}
				}
				
				// try delay
				Date delayDepTime = BusinessDomain.getPossibleDelayDeparture(thisFlightOpt, thisFlightOpt.getSourceAirPort(), false, lastFlightOpt);
				if (delayDepTime != null) {
					boolean stretchable = false;
					Flight newFlight = thisFlightOpt.clone();

					newFlight.setDepartureTime(delayDepTime);
					newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, delayDepTime, originalAircraft));
					Date adjustedArrival = BusinessDomain.getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
					if (adjustedArrival != null){
						if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
							//ok open next
							if (BusinessDomain.isValidParking(lastFlightOpt.getArrivalTime(), newFlight.getDepartureTime(), lastFlightOpt.getDesintationAirport())){
								ArrayList<Flight> aNewFlightChain = cloneList(optimizedFlightList);
								aNewFlightChain.remove(aNewFlightChain.size() - 1);
								aNewFlightChain.add(newFlight);
								aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
								insertPathToOpenList(aNewFlightChain);
								stretchable = true;	
							}
							
						} else {
							Date newDeparture = BusinessDomain.getDepartureTimeByArrivalTime(newFlight, adjustedArrival, originalAircraft);
							if (!BusinessDomain.isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
								newFlight.setDepartureTime(newDeparture);
								if (BusinessDomain.isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
									Date newTempDelayDep = BusinessDomain.getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, lastFlightOpt);
									if (newTempDelayDep != null){
										newFlight.setDepartureTime(newTempDelayDep);
										newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, newTempDelayDep, originalAircraft));
										if (!BusinessDomain.isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
											if (BusinessDomain.isValidParking(lastFlightOpt.getArrivalTime(), newFlight.getDepartureTime(), lastFlightOpt.getDesintationAirport())){
												ArrayList<Flight> aNewFlightChain = cloneList(optimizedFlightList);
												aNewFlightChain.remove(aNewFlightChain.size() - 1);
												aNewFlightChain.add(newFlight);
												aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
												insertPathToOpenList(aNewFlightChain);
												stretchable = true;	
											}
										}
									}
								} else {
									//ok open next
									newFlight.setArrivalTime(adjustedArrival);
									if (BusinessDomain.isValidParking(lastFlightOpt.getArrivalTime(), newFlight.getDepartureTime(), lastFlightOpt.getDesintationAirport())){
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
					}
					// try stretch
					if (stretchable && BusinessDomain.getJointFlightPosition(newFlight) == 1 && 
							!newFlight.isInternationalFlight() && !BusinessDomain.getJointFlight(newFlight).isInternationalFlight()) {
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
				Date earlyDepTime = BusinessDomain.getPossibleEarlierDepartureTime(thisFlight, thisFlight.getSourceAirPort(), false, lastFlight);
				if (earlyDepTime != null && !thisFlight.isInternationalFlight()) {
					Flight newFlight = thisFlight.clone();
					if (earlyDepTime.compareTo(thisFlight.getDepartureTime()) != 0){
						newFlight.setDepartureTime(earlyDepTime);
						newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, earlyDepTime, originalAircraft));
						Date adjustedArrival = BusinessDomain.getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
						if (adjustedArrival != null){
							if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
								//ok open next
								if (BusinessDomain.isValidParking(lastFlight.getArrivalTime(), newFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
									ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
									aNewFlightChain.remove(aNewFlightChain.size() - 1);
									aNewFlightChain.add(newFlight);
									finishedArrayList.add(aNewFlightChain);
								}
							} else {
								Date newDeparture = BusinessDomain.getDepartureTimeByArrivalTime(newFlight, adjustedArrival, originalAircraft);
								if (!BusinessDomain.isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
									newFlight.setDepartureTime(newDeparture);
									if (BusinessDomain.isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
										Date newTempDelayDep = BusinessDomain.getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, lastFlight);
										if (newTempDelayDep != null){
											newFlight.setDepartureTime(newTempDelayDep);
											newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, newTempDelayDep, originalAircraft));
											if (!BusinessDomain.isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
												if (BusinessDomain.isValidParking(lastFlight.getArrivalTime(), newFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
													ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
													aNewFlightChain.remove(aNewFlightChain.size() - 1);
													aNewFlightChain.add(newFlight);
													finishedArrayList.add(aNewFlightChain);
												}
											}
										}
									} else {
										//ok open next
										newFlight.setArrivalTime(adjustedArrival);
										if (BusinessDomain.isValidParking(lastFlight.getArrivalTime(), newFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
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
				}
				
				// try delay
				Date delayDepTime = BusinessDomain.getPossibleDelayDeparture(thisFlightOpt, thisFlightOpt.getSourceAirPort(), false, lastFlightOpt);
				if (delayDepTime != null) {
					Flight newFlight = thisFlightOpt.clone();
					newFlight.setDepartureTime(delayDepTime);
					
					newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, delayDepTime, originalAircraft));
					Date adjustedArrival = BusinessDomain.getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
					if (adjustedArrival != null){
						if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
							//ok open next
							if (BusinessDomain.isValidParking(lastFlightOpt.getArrivalTime(), newFlight.getDepartureTime(), lastFlightOpt.getDesintationAirport())){
								ArrayList<Flight> aNewFlightChain = cloneList(optimizedFlightList);
								aNewFlightChain.remove(aNewFlightChain.size() - 1);
								aNewFlightChain.add(newFlight);
								finishedArrayList.add(aNewFlightChain);
							}
						} else {
							Date newDeparture = BusinessDomain.getDepartureTimeByArrivalTime(newFlight, adjustedArrival, originalAircraft);
							if (!BusinessDomain.isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
								newFlight.setDepartureTime(newDeparture);
								if (BusinessDomain.isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
									Date newTempDelayDep = BusinessDomain.getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, lastFlightOpt);
									if (newTempDelayDep != null){
										newFlight.setDepartureTime(newTempDelayDep);
										newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, newTempDelayDep, originalAircraft));
										if (!BusinessDomain.isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
											if (BusinessDomain.isValidParking(lastFlightOpt.getArrivalTime(), newFlight.getDepartureTime(), lastFlightOpt.getDesintationAirport())){
												ArrayList<Flight> aNewFlightChain = cloneList(optimizedFlightList);
												aNewFlightChain.remove(aNewFlightChain.size() - 1);
												aNewFlightChain.add(newFlight);
												finishedArrayList.add(aNewFlightChain);
											}
										}
									}
								} else {
									//ok open next
									newFlight.setArrivalTime(adjustedArrival);
									if (BusinessDomain.isValidParking(lastFlightOpt.getArrivalTime(), newFlight.getDepartureTime(), lastFlightOpt.getDesintationAirport())){
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
		} else {
			ArrayList<Flight> aNewFlightChain = cloneList(oldFlights);
			aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
			finishedArrayList.add(aNewFlightChain);
		}
		
	}
	
	
	public void openCancelFlightNode(Flight origFlight, ArrayList<Flight> origFlightList) throws CloneNotSupportedException, ParseException, FlightDurationNotFound {
		int thisFlightIndex = BusinessDomain.getFlightIndexByFlightId(origFlight.getFlightId(), originalFlights);
		Flight lastFlight = origFlightList.size() > 1 ? origFlightList.get(origFlightList.size() - 2) : null;
		ArrayList<Flight> flightChain = cloneList(origFlightList);
		flightChain.remove(flightChain.size() - 1);
		boolean adjustable = false;
		
		Flight newFlight = origFlight.clone();
		newFlight.setFlightId(BusinessDomain.getNextFlightId());
		newFlight.setPlannedFlight(null);;
		if (thisFlightIndex > 0) {
			boolean newFlag = false;
			for (Flight flight : flightChain){
				if (BusinessDomain.isNewFlight(flight)){
					newFlag = true;
				}
			}
			if (!newFlag){
				Date depTime = addMinutes(lastFlight.getArrivalTime(), BusinessDomain.getGroundingTime(lastFlight, newFlight));
				newFlight.setDepartureTime(depTime);
				Date adjustedDep = BusinessDomain.getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, lastFlight);
				if (adjustedDep != null) {
					adjustable = true;
					newFlight.setDepartureTime(adjustedDep);
				}
			}
		} else {
			adjustable = true;
		}
		if (adjustable) {
			for (int i = thisFlightIndex + 1; i < originalFlights.size() - 1; i++) {
				Flight nextFlight = originalFlights.get(i).clone();
				AirPort destAirport = nextFlight.getSourceAirPort();
				Flight aNewFlight = newFlight.clone();
				if (destAirport.getId().equals(aNewFlight.getSourceAirPort().getId())) {
					// valid parking
					if (thisFlightIndex > 0) {
						if (aNewFlight.getFlightId() == 1340){
							
						}
						if (!BusinessDomain.isValidParking(lastFlight.getArrivalTime(), nextFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
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
				if (BusinessDomain.isInternational(aNewFlight.getSourceAirPort().getId(), destAirport.getId())){
					continue;
				}
				
				// aircraft constraint
				if (!BusinessDomain.isEligibalAircraft(aNewFlight.getAssignedAir(), aNewFlight.getSourceAirPort(), destAirport)){
					continue;
				}

				long flightTime = BusinessDomain.getFlightTime(aNewFlight.getSourceAirPort().getId(), destAirport.getId(), aNewFlight.getAssignedAir());
				if (flightTime > 0){
					aNewFlight.setDesintationAirport(nextFlight.getSourceAirPort());
					aNewFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(aNewFlight, aNewFlight.getDepartureTime(), originalAircraft));
					Date adjustedArrival = BusinessDomain.getPossibleArrivalTime(aNewFlight, aNewFlight.getDesintationAirport());
					if (adjustedArrival != null){
						if (adjustedArrival.compareTo(aNewFlight.getArrivalTime()) == 0){
							//ok open next
							ArrayList<Flight> aNewFlightChain = cloneList(flightChain);
							aNewFlightChain.add(aNewFlight);
							aNewFlightChain.add(nextFlight);
							if (!BusinessDomain.isValidParking(aNewFlight.getArrivalTime(), nextFlight.getDepartureTime(), aNewFlight.getDesintationAirport())){
								continue;
							}
							insertPathToOpenList(aNewFlightChain);
						} else {
							Date newDeparture = addMinutes(adjustedArrival, -flightTime);
							if (!BusinessDomain.isDepTimeAffected(newDeparture, aNewFlight.getSourceAirPort())){
								aNewFlight.setDepartureTime(newDeparture);
								if (BusinessDomain.isDepTimeAffectedByNormal(newDeparture, aNewFlight.getSourceAirPort())){
									Date newTempDelayDep = BusinessDomain.getPossibleDelayDeparture(aNewFlight, aNewFlight.getSourceAirPort(), false, lastFlight);
									if (newTempDelayDep != null){
										aNewFlight.setDepartureTime(newTempDelayDep);
										aNewFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(aNewFlight, newTempDelayDep, originalAircraft));
										if (!BusinessDomain.isArvTimeAffected(aNewFlight.getArrivalTime(), aNewFlight.getDesintationAirport())){
											ArrayList<Flight> aNewFlightChain = cloneList(flightChain);
											aNewFlightChain.add(aNewFlight);
											aNewFlightChain.add(nextFlight);
											if (!BusinessDomain.isValidParking(aNewFlight.getArrivalTime(), nextFlight.getDepartureTime(), aNewFlight.getDesintationAirport())){
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
									if (!BusinessDomain.isValidParking(aNewFlight.getArrivalTime(), nextFlight.getDepartureTime(), aNewFlight.getDesintationAirport())){
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
	 * @throws ParseException 
	 * @throws AircraftNotAdjustable 
	 */
	public void openJointFlightNode(Flight originalFlight, ArrayList<Flight> origFlightChain) throws CloneNotSupportedException, FlightDurationNotFound, ParseException{
		Flight jointFlight = BusinessDomain.getJointFlight(originalFlight);
		Flight lastFlight = origFlightChain.size() > 1 ? origFlightChain.get(origFlightChain.size() - 2) : null;
		ArrayList<Flight> flightChain = cloneList(origFlightChain);
		flightChain.remove(flightChain.size() - 1);
		if (BusinessDomain.isAffected(jointFlight) == 1) {
			// departure time is affected
			Flight newFlight = originalFlight.clone();
			newFlight.setDesintationAirport(jointFlight.getDesintationAirport());
			newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, newFlight.getDepartureTime(), originalAircraft));
			Date adjustedArrival = BusinessDomain.getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
			if (adjustedArrival != null){
				if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
					//ok open next
					flightChain.add(newFlight);
					// get next node
					int jointFlightIndex = BusinessDomain.getFlightIndexByFlightId(jointFlight.getFlightId(), originalFlights);
					if (jointFlightIndex > originalFlights.size() - 2) {
						finishedArrayList.add(flightChain);
					} else {
						Flight nextFlight = originalFlights.get(jointFlightIndex + 1).clone();
						flightChain.add(nextFlight);
						insertPathToOpenList(flightChain);
					}
				} else {
					Date newDeparture = BusinessDomain.getDepartureTimeByArrivalTime(newFlight, adjustedArrival, originalAircraft);
					if (!BusinessDomain.isDepTimeAffected(newDeparture, newFlight.getSourceAirPort())){
						newFlight.setDepartureTime(newDeparture);
						if (BusinessDomain.isDepTimeAffectedByNormal(newDeparture, newFlight.getSourceAirPort())){
							Date newTempDelayDep = BusinessDomain.getPossibleDelayDeparture(newFlight, newFlight.getSourceAirPort(), false, lastFlight);
							if (newTempDelayDep != null){
								newFlight.setDepartureTime(newTempDelayDep);
								newFlight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(newFlight, newTempDelayDep, originalAircraft));
								if (!BusinessDomain.isArvTimeAffected(newFlight.getArrivalTime(), newFlight.getDesintationAirport())){
									flightChain.add(newFlight);
									// get next node
									int jointFlightIndex = BusinessDomain.getFlightIndexByFlightId(jointFlight.getFlightId(), originalFlights);
									if (jointFlightIndex > originalFlights.size() - 2) {
										finishedArrayList.add(flightChain);
									} else {
										Flight nextFlight = originalFlights.get(jointFlightIndex + 1).clone();
										flightChain.add(nextFlight);
										insertPathToOpenList(flightChain);
									}
								}
							}
						} else {
							//ok open next
							newFlight.setArrivalTime(adjustedArrival);
							flightChain.add(newFlight);
							// get next node
							int jointFlightIndex = BusinessDomain.getFlightIndexByFlightId(jointFlight.getFlightId(), originalFlights);
							if (jointFlightIndex > originalFlights.size() - 2) {
								finishedArrayList.add(flightChain);
							} else {
								Flight nextFlight = originalFlights.get(jointFlightIndex + 1).clone();
								flightChain.add(nextFlight);
								insertPathToOpenList(flightChain);
							}
						}
					}
				}
			}
		}
	}
	
	// method utility
	
	
	
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
				if (i == 0 && BusinessDomain.isNewFlight(newFlight)) {
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
