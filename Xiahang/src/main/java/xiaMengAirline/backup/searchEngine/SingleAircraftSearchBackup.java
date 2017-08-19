package xiaMengAirline.backup.searchEngine;

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

import xiaMengAirline.backup.Exception.AircraftNotAdjustableBackup;
import xiaMengAirline.backup.Exception.AirportNotAcceptArrivalTimeBackup;
import xiaMengAirline.backup.Exception.AirportNotAcceptDepartureTimeBackup;
import xiaMengAirline.backup.Exception.AirportNotAvailableBackup;
import xiaMengAirline.backup.Exception.FlightDurationNotFoundBackup;
import xiaMengAirline.backup.beans.AirPortBackup;
import xiaMengAirline.backup.beans.AirPortCloseBackup;
import xiaMengAirline.backup.beans.AircraftBackup;
import xiaMengAirline.backup.beans.FlightBackup;
import xiaMengAirline.backup.beans.FlightTimeBackup;
import xiaMengAirline.backup.beans.RegularAirPortCloseBackup;
import xiaMengAirline.backup.beans.XiaMengAirlineSolutionBackup;
import xiaMengAirline.backup.utils.InitDataBackup;
import xiaMengAirline.backup.utils.UtilsBackup;

public class SingleAircraftSearchBackup {
	private AircraftBackup originalAircraft;
	private ArrayList<FlightBackup> originalFlights;
	private TreeMap<Long, ArrayList<ArrayList<FlightBackup>>> openArrayList = new TreeMap<Long, ArrayList<ArrayList<FlightBackup>>>();
	private ArrayList<ArrayList<FlightBackup>> finishedArrayList = new ArrayList<ArrayList<FlightBackup>>();
	private boolean isFullSearch;
	
	public SingleAircraftSearchBackup(AircraftBackup aAircraft, boolean isFullSearch) throws CloneNotSupportedException {
		originalAircraft = aAircraft.clone();
		originalFlights = (ArrayList<FlightBackup>) originalAircraft.getFlightChain();
	}
	
	public ArrayList<AircraftBackup> getAdjustedAircraftPair() throws CloneNotSupportedException, AircraftNotAdjustableBackup{
		if (originalFlights.size() < 2){
			throw new AircraftNotAdjustableBackup(originalAircraft);
		}
		
		boolean started = false;
		boolean finished = false;
		// loop to open all solutions
		while ((!started || !finished)){
			try {
				finished = processNextPath(started);
			} catch (CloneNotSupportedException | FlightDurationNotFoundBackup | ParseException e) {
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
			throw new AircraftNotAdjustableBackup(originalAircraft);
		}
		
		// evaluate solutions to get the best solution
		ArrayList<FlightBackup> bestList = finishedArrayList.get(0);
		long bestCost = calDeltaCost(originalFlights, finishedArrayList.get(0));
		for (ArrayList<FlightBackup> list : finishedArrayList){
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
		ArrayList<FlightBackup> cancelList = new ArrayList<FlightBackup>();
		for (FlightBackup orgFlight : originalFlights){
			boolean found = false;
			for (FlightBackup newFlight : bestList){
				if (newFlight.getFlightId() == orgFlight.getFlightId()){
					found = true;
				}
			}
			if (!found){
				cancelList.add(orgFlight);
			}
		}
		// build two aircraft
		AircraftBackup normalAircraft = originalAircraft.clone();
		normalAircraft.setAlternativeAircraft(null);
		normalAircraft.setFlightChain(bestList);
		AircraftBackup cancelAircraft = originalAircraft.clone();
		cancelAircraft.setCancel(true);
		cancelAircraft.setAlternativeAircraft(null);
		cancelAircraft.setFlightChain(cancelList);
		
		ArrayList<AircraftBackup> returnList = new ArrayList<AircraftBackup>();
		returnList.add(normalAircraft);
		returnList.add(cancelAircraft);
		return returnList;
	}
	
	public boolean processNextPath(boolean started) throws CloneNotSupportedException, FlightDurationNotFoundBackup, ParseException {
		if (openArrayList.size() > 0) {
			Map.Entry<Long, ArrayList<ArrayList<FlightBackup>>> entry = openArrayList.entrySet().iterator().next();
			long costKey = entry.getKey();
			ArrayList<FlightBackup> path = entry.getValue().get(0);
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
	
	public void openFirstNode() throws CloneNotSupportedException, FlightDurationNotFoundBackup, ParseException {
		FlightBackup thisFlight = originalFlights.get(0).clone();
		// try earlier departure
		Date earlyDepTime = getPossibleEarlierDepartureTime(thisFlight, thisFlight.getSourceAirPort(), false, null);
		if (earlyDepTime != null && !thisFlight.isInternationalFlight()) {
			if (earlyDepTime.compareTo(thisFlight.getDepartureTime()) != 0){
				boolean stretchable = false;
				FlightBackup newFlight = thisFlight.clone();
				newFlight.setDepartureTime(earlyDepTime);
				newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, earlyDepTime));
				Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
				if (adjustedArrival != null){
					if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
						//ok open next
						ArrayList<FlightBackup> aNewFlightChain = new ArrayList<FlightBackup>();
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
										ArrayList<FlightBackup> aNewFlightChain = new ArrayList<FlightBackup>();
										aNewFlightChain.add(newFlight);
										aNewFlightChain.add(originalFlights.get(1).clone());
										insertPathToOpenList(aNewFlightChain);
										stretchable = true;
									}
								}
							} else {
								//ok open next
								newFlight.setArrivalTime(adjustedArrival);
								ArrayList<FlightBackup> aNewFlightChain = new ArrayList<FlightBackup>();
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
					ArrayList<FlightBackup> newFlightList =  new ArrayList<FlightBackup>();
					newFlightList.add(newFlight);
					openJointFlightNode(newFlight, newFlightList);
				}
			}
		}
		
		// try delay departure
		Date delayDepTime = getPossibleDelayDeparture(thisFlight, thisFlight.getSourceAirPort(), true, null);
		if (delayDepTime != null) {
			boolean stretchable = false;
			FlightBackup newFlight = thisFlight.clone();
			newFlight.setDepartureTime(delayDepTime);
			newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, delayDepTime));
			Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
			if (adjustedArrival != null){
				if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
					//ok open next
					ArrayList<FlightBackup> aNewFlightChain = new ArrayList<FlightBackup>();
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
									ArrayList<FlightBackup> aNewFlightChain = new ArrayList<FlightBackup>();
									aNewFlightChain.add(newFlight);
									aNewFlightChain.add(originalFlights.get(1).clone());
									insertPathToOpenList(aNewFlightChain);
									stretchable = true;
								}
							}
						} else {
							//ok open next
							newFlight.setArrivalTime(adjustedArrival);
							ArrayList<FlightBackup> aNewFlightChain = new ArrayList<FlightBackup>();
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
				ArrayList<FlightBackup> newFlightList =  new ArrayList<FlightBackup>();
				newFlightList.add(newFlight);
				openJointFlightNode(newFlight, newFlightList);
			}
		}
		
		// try cancel
		ArrayList<FlightBackup> newFlightList =  new ArrayList<FlightBackup>();
		newFlightList.add(thisFlight.clone());
		openCancelFlightNode(thisFlight, newFlightList);
	}
	
	/** 
	 * thisFlight is the last flight in the opened flight ArrayList
	 * nextFlight is new opened flight
	 * @param oldFlights
	 * @throws ParseException 
	 * @throws FlightDurationNotFoundBackup 
	 * @throws CloneNotSupportedException 
	 */
	public void openNextNode(ArrayList<FlightBackup> oldFlights) throws ParseException, FlightDurationNotFoundBackup, CloneNotSupportedException {
		FlightBackup thisFlight = oldFlights.get(oldFlights.size() - 1);
		FlightBackup lastFlight = oldFlights.get(oldFlights.size() - 2);
		int thisFlightIndex = getFlightIndexByFlightId(thisFlight.getFlightId());
		int nextFlightIndex = thisFlightIndex + 1;
		
		// track back
		ArrayList<FlightBackup> optimizedFlightList = cloneList(oldFlights);
		for (int i = oldFlights.size() - 2; i >= 0; i--){
			FlightBackup trackFlight = optimizedFlightList.get(i);
			FlightBackup nextFlight = optimizedFlightList.get(i + 1);
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
		
		FlightBackup thisFlightOpt = optimizedFlightList.get(optimizedFlightList.size() - 1);
		FlightBackup lastFlightOpt = optimizedFlightList.get(optimizedFlightList.size() - 2);

		if (nextFlightIndex < originalFlights.size()) {
			// adjust this flight by early departure, delay, stretch and cancel
			// try early departure
			Date earlyDepTime = getPossibleEarlierDepartureTime(thisFlight, thisFlight.getSourceAirPort(), false, lastFlight);
			if (earlyDepTime != null && !thisFlight.isInternationalFlight()) {
				if (earlyDepTime.compareTo(thisFlight.getDepartureTime()) != 0){
					boolean stretchable = false;
					FlightBackup newFlight = thisFlight.clone();
					newFlight.setDepartureTime(earlyDepTime);
					newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, earlyDepTime));
					Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
					if (adjustedArrival != null){
						if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
							//ok open next
							if (isValidParking(lastFlight.getArrivalTime(), newFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
								ArrayList<FlightBackup> aNewFlightChain = cloneList(oldFlights);
								aNewFlightChain.remove(aNewFlightChain.size() - 1);
								aNewFlightChain.add(newFlight);
								aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
								insertPathToOpenList(aNewFlightChain);
								stretchable = true;
							}
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
											if (isValidParking(lastFlight.getArrivalTime(), newFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
												ArrayList<FlightBackup> aNewFlightChain = cloneList(oldFlights);
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
									if (isValidParking(lastFlight.getArrivalTime(), newFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
										ArrayList<FlightBackup> aNewFlightChain = cloneList(oldFlights);
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
					if (stretchable && getJointFlightPosition(newFlight) == 1) {
						ArrayList<FlightBackup> aNewFlightChain = cloneList(oldFlights);
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
				FlightBackup newFlight = thisFlightOpt.clone();

				newFlight.setDepartureTime(delayDepTime);
				newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, delayDepTime));
				Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
				if (adjustedArrival != null){
					if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
						//ok open next
						if (isValidParking(lastFlightOpt.getArrivalTime(), newFlight.getDepartureTime(), lastFlightOpt.getDesintationAirport())){
							ArrayList<FlightBackup> aNewFlightChain = cloneList(optimizedFlightList);
							aNewFlightChain.remove(aNewFlightChain.size() - 1);
							aNewFlightChain.add(newFlight);
							aNewFlightChain.add(originalFlights.get(nextFlightIndex).clone());
							insertPathToOpenList(aNewFlightChain);
							stretchable = true;	
						}
						
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
										if (isValidParking(lastFlightOpt.getArrivalTime(), newFlight.getDepartureTime(), lastFlightOpt.getDesintationAirport())){
											ArrayList<FlightBackup> aNewFlightChain = cloneList(optimizedFlightList);
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
								if (isValidParking(lastFlightOpt.getArrivalTime(), newFlight.getDepartureTime(), lastFlightOpt.getDesintationAirport())){
									ArrayList<FlightBackup> aNewFlightChain = cloneList(optimizedFlightList);
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
				if (stretchable && getJointFlightPosition(newFlight) == 1) {
					ArrayList<FlightBackup> aNewFlightChain = cloneList(optimizedFlightList);
					aNewFlightChain.remove(aNewFlightChain.size() - 1);
					aNewFlightChain.add(newFlight);
					openJointFlightNode(newFlight, aNewFlightChain);
				}
			}
			
			// try cancel
			ArrayList<FlightBackup> aNewFlightChain = cloneList(oldFlights);
			aNewFlightChain.remove(aNewFlightChain.size() - 1);
			aNewFlightChain.add(thisFlight.clone());
			openCancelFlightNode(thisFlight, aNewFlightChain);
		} else {
			// last flight is loaded
			// try early departure
			Date earlyDepTime = getPossibleEarlierDepartureTime(thisFlight, thisFlight.getSourceAirPort(), false, lastFlight);
			if (earlyDepTime != null && !thisFlight.isInternationalFlight()) {
				FlightBackup newFlight = thisFlight.clone();
				if (earlyDepTime.compareTo(thisFlight.getDepartureTime()) != 0){
					newFlight.setDepartureTime(earlyDepTime);
					newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, earlyDepTime));
					Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
					if (adjustedArrival != null){
						if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
							//ok open next
							if (isValidParking(lastFlight.getArrivalTime(), newFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
								ArrayList<FlightBackup> aNewFlightChain = cloneList(oldFlights);
								aNewFlightChain.remove(aNewFlightChain.size() - 1);
								aNewFlightChain.add(newFlight);
								finishedArrayList.add(aNewFlightChain);
							}
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
											if (isValidParking(lastFlight.getArrivalTime(), newFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
												ArrayList<FlightBackup> aNewFlightChain = cloneList(oldFlights);
												aNewFlightChain.remove(aNewFlightChain.size() - 1);
												aNewFlightChain.add(newFlight);
												finishedArrayList.add(aNewFlightChain);
											}
										}
									}
								} else {
									//ok open next
									newFlight.setArrivalTime(adjustedArrival);
									if (isValidParking(lastFlight.getArrivalTime(), newFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
										ArrayList<FlightBackup> aNewFlightChain = cloneList(oldFlights);
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
			Date delayDepTime = getPossibleDelayDeparture(thisFlightOpt, thisFlightOpt.getSourceAirPort(), false, lastFlightOpt);
			if (delayDepTime != null) {
				FlightBackup newFlight = thisFlightOpt.clone();
				newFlight.setDepartureTime(delayDepTime);
				
				newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, delayDepTime));
				Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
				if (adjustedArrival != null){
					if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
						//ok open next
						if (isValidParking(lastFlightOpt.getArrivalTime(), newFlight.getDepartureTime(), lastFlightOpt.getDesintationAirport())){
							ArrayList<FlightBackup> aNewFlightChain = cloneList(optimizedFlightList);
							aNewFlightChain.remove(aNewFlightChain.size() - 1);
							aNewFlightChain.add(newFlight);
							finishedArrayList.add(aNewFlightChain);
						}
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
										if (isValidParking(lastFlightOpt.getArrivalTime(), newFlight.getDepartureTime(), lastFlightOpt.getDesintationAirport())){
											ArrayList<FlightBackup> aNewFlightChain = cloneList(optimizedFlightList);
											aNewFlightChain.remove(aNewFlightChain.size() - 1);
											aNewFlightChain.add(newFlight);
											finishedArrayList.add(aNewFlightChain);
										}
									}
								}
							} else {
								//ok open next
								newFlight.setArrivalTime(adjustedArrival);
								if (isValidParking(lastFlightOpt.getArrivalTime(), newFlight.getDepartureTime(), lastFlightOpt.getDesintationAirport())){
									ArrayList<FlightBackup> aNewFlightChain = cloneList(optimizedFlightList);
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
	}
	
	
	public void openCancelFlightNode(FlightBackup origFlight, ArrayList<FlightBackup> origFlightList) throws CloneNotSupportedException, ParseException, FlightDurationNotFoundBackup {
		int thisFlightIndex = getFlightIndexByFlightId(origFlight.getFlightId());
		FlightBackup lastFlight = origFlightList.size() > 1 ? origFlightList.get(origFlightList.size() - 2) : null;
		ArrayList<FlightBackup> flightChain = cloneList(origFlightList);
		flightChain.remove(flightChain.size() - 1);
		boolean adjustable = false;
		
		FlightBackup newFlight = origFlight.clone();
		newFlight.setFlightId(getNextFlightId());
		newFlight.setPlannedFlight(null);;
		if (thisFlightIndex > 0) {
			boolean newFlag = false;
			for (FlightBackup flight : flightChain){
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
				FlightBackup nextFlight = originalFlights.get(i).clone();
				AirPortBackup destAirport = nextFlight.getSourceAirPort();
				FlightBackup aNewFlight = newFlight.clone();
				if (destAirport.getId().equals(aNewFlight.getSourceAirPort().getId())) {
					// valid parking
					if (thisFlightIndex > 0) {
						if (aNewFlight.getFlightId() == 1340){
							
						}
						if (!isValidParking(lastFlight.getArrivalTime(), nextFlight.getDepartureTime(), lastFlight.getDesintationAirport())){
							continue;
						} else {
							ArrayList<FlightBackup> aNewFlightChain = cloneList(flightChain);
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
							ArrayList<FlightBackup> aNewFlightChain = cloneList(flightChain);
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
											ArrayList<FlightBackup> aNewFlightChain = cloneList(flightChain);
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
									ArrayList<FlightBackup> aNewFlightChain = cloneList(flightChain);
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
	 * @throws FlightDurationNotFoundBackup
	 * @throws ParseException 
	 * @throws AircraftNotAdjustableBackup 
	 */
	public void openJointFlightNode(FlightBackup originalFlight, ArrayList<FlightBackup> origFlightChain) throws CloneNotSupportedException, FlightDurationNotFoundBackup, ParseException{
		FlightBackup jointFlight = getJointFlight(originalFlight);
		FlightBackup lastFlight = origFlightChain.size() > 1 ? origFlightChain.get(origFlightChain.size() - 2) : null;
		ArrayList<FlightBackup> flightChain = cloneList(origFlightChain);
		flightChain.remove(flightChain.size() - 1);
		if (isAffected(jointFlight) == 1) {
			// departure time is affected
			FlightBackup newFlight = originalFlight.clone();
			newFlight.setDesintationAirport(jointFlight.getDesintationAirport());
			newFlight.setArrivalTime(getArrivalTimeByDepartureTime(newFlight, newFlight.getDepartureTime()));
			Date adjustedArrival = getPossibleArrivalTime(newFlight, newFlight.getDesintationAirport());
			if (adjustedArrival != null){
				if (adjustedArrival.compareTo(newFlight.getArrivalTime()) == 0){
					//ok open next
					flightChain.add(newFlight);
					// get next node
					int jointFlightIndex = getFlightIndexByFlightId(jointFlight.getFlightId());
					if (jointFlightIndex > originalFlights.size() - 2) {
						finishedArrayList.add(flightChain);
					} else {
						FlightBackup nextFlight = originalFlights.get(jointFlightIndex + 1).clone();
						flightChain.add(nextFlight);
						insertPathToOpenList(flightChain);
					}
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
									flightChain.add(newFlight);
									// get next node
									int jointFlightIndex = getFlightIndexByFlightId(jointFlight.getFlightId());
									if (jointFlightIndex > originalFlights.size() - 2) {
										finishedArrayList.add(flightChain);
									} else {
										FlightBackup nextFlight = originalFlights.get(jointFlightIndex + 1).clone();
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
							int jointFlightIndex = getFlightIndexByFlightId(jointFlight.getFlightId());
							if (jointFlightIndex > originalFlights.size() - 2) {
								finishedArrayList.add(flightChain);
							} else {
								FlightBackup nextFlight = originalFlights.get(jointFlightIndex + 1).clone();
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
	 * if a departure time is in the error time range
	 * @param depTime
	 * @param airport
	 * @return
	 */
	public boolean isDepTimeAffected(Date depTime, AirPortBackup airport) {
		for (AirPortCloseBackup aClose : airport.getCloseSchedule()) {
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
	public boolean isArvTimeAffected(Date arvTime, AirPortBackup airport) {
		for (AirPortCloseBackup aClose : airport.getCloseSchedule()) {
			if (arvTime.after(aClose.getStartTime())
					&& arvTime.before(aClose.getEndTime())) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean isDepTimeAffectedByNormal(Date depTime, AirPortBackup airport) throws ParseException{
		for (RegularAirPortCloseBackup aClose : airport.getRegularCloseSchedule()) {
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
	public boolean isEarlyDeparturePossible(Date depTime, AirPortBackup airport) {
		for (AirPortCloseBackup aClose : airport.getCloseSchedule()) {
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
	public Date getPossibleArrivalTime(FlightBackup flight, AirPortBackup airport) throws ParseException{
		for (AirPortCloseBackup aClose : airport.getCloseSchedule()) {
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
		for (RegularAirPortCloseBackup aClose : airport.getRegularCloseSchedule()) {
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
	public Date getPossibleDelayDeparture(FlightBackup flight, AirPortBackup airport, boolean isFirstFlight, FlightBackup lastFlight) throws ParseException {
		Date tempDepTime = null;
		if (lastFlight != null){
			Date shiftDeparture = addMinutes(lastFlight.getArrivalTime(), getGroundingTime(lastFlight, flight));
			tempDepTime = shiftDeparture.before(flight.getDepartureTime()) ? flight.getDepartureTime() : shiftDeparture;
		} else {
			tempDepTime = flight.getDepartureTime();
		}
		for (AirPortCloseBackup aClose : airport.getCloseSchedule()) {
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
		for (RegularAirPortCloseBackup aClose : airport.getRegularCloseSchedule()) {
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
	public Date getPossibleEarlierDepartureTime(FlightBackup flight, AirPortBackup airport, boolean hasEarlyLimit, FlightBackup lastFlight) throws ParseException {
		if (lastFlight != null) {
			// not the first flight
			if (getPlannedDeparture(flight).before(lastFlight.getArrivalTime())){
				return null;
			}
			for (AirPortCloseBackup aClose : airport.getCloseSchedule()) {
				if (!aClose.isAllowForTakeoff()) {
					if (isNewFlight(flight)){
						if (flight.getDepartureTime().after(aClose.getStartTime())
								&& flight.getDepartureTime().before(aClose.getStartTime())) {
							Date tempDep = addMinutes(lastFlight.getArrivalTime(), FlightBackup.getGroundingTime(lastFlight.getFlightId(), flight.getFlightId()));
							if (tempDep.after(aClose.getStartTime())) {
								return null;
							} else {
								return tempDep;
							}
						}
					} else {
						if (getPlannedDeparture(flight).after(aClose.getStartTime())
								&& getPlannedDeparture(flight).before(addMinutes(aClose.getStartTime(), 360))) {
							Date tempDep = addMinutes(lastFlight.getArrivalTime(), FlightBackup.getGroundingTime(lastFlight.getFlightId(), flight.getFlightId()));
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
			for (AirPortCloseBackup aClose : airport.getCloseSchedule()) {
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
		for (RegularAirPortCloseBackup aClose : airport.getRegularCloseSchedule()) {
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
						Date tempDep = addMinutes(lastFlight.getArrivalTime(), FlightBackup.getGroundingTime(lastFlight.getFlightId(), flight.getFlightId()));
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
	
	
	public Date getAirportDepartureCloseStart(AirPortBackup airport){
		for (AirPortCloseBackup aClose : airport.getCloseSchedule()) {
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
	public long getFlightTime(String airport1Id, String airport2Id, AircraftBackup aircraft){
		String searchKey = aircraft.getType();
		searchKey += "_";
		searchKey += airport1Id;
		searchKey += "_";
		searchKey += airport2Id;
		
		if (InitDataBackup.fightDurationMap.containsKey(searchKey)){
			long flightTime = InitDataBackup.fightDurationMap.get(searchKey);
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
		if (InitDataBackup.domesticAirportList.contains(airport1) && InitDataBackup.domesticAirportList.contains(airport2)){
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
	public boolean isEligibalAircraft(AircraftBackup aircraft, AirPortBackup sourceAir, AirPortBackup destAir){
		String searchKey = aircraft.getId() + "_" + sourceAir.getId() + "_" + destAir.getId();
		return InitDataBackup.airLimitationList.contains(searchKey) ? false : true;
	}
	
	/**
	 * check if the flight delay is valid
	 * @param planTime
	 * @param adjustTime
	 * @param isInternational
	 * @return
	 */
	public boolean isValidDelay(Date planTime, Date adjustTime, boolean isInternational){
		if (getMinuteDifference(adjustTime, planTime) > (isInternational ? AircraftBackup.INTERNATIONAL_MAXIMUM_DELAY_TIME*60: AircraftBackup.DOMESTIC_MAXIMUM_DELAY_TIME*60)){
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
	public boolean isValidParking(Date arrivalTime, Date departureTime, AirPortBackup airport){
		if (arrivalTime != null && arrivalTime != null) {
			for (AirPortCloseBackup aClose : airport.getCloseSchedule()) {
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
	public Date getPlannedArrival(FlightBackup flight){
		return flight.getPlannedFlight().getArrivalTime();
	}
	
	/**
	 * get original departure time
	 * @param flight
	 * @return
	 */
	public Date getPlannedDeparture(FlightBackup flight){
		return flight.getPlannedFlight().getDepartureTime();
	}
	
	/**
	 * get grounding time between two flights
	 * @param flight1
	 * @param flight2
	 * @return
	 */
	public int getGroundingTime(FlightBackup flight1, FlightBackup flight2) {
		return FlightBackup.getGroundingTime(flight1.getFlightId(), flight2.getFlightId());
	}
	
	/**
	 * compress the departure time of next flight
	 * @param lastFlight
	 * @param thisFlight
	 * @return
	 */
	public Date getCompressedDeparture(FlightBackup lastFlight, FlightBackup thisFlight) {
		Date thisPlannedDeparture = getPlannedDeparture(thisFlight);
		Date thisShiftedDeparture = addMinutes(lastFlight.getArrivalTime(), getGroundingTime(lastFlight, thisFlight));
		return thisPlannedDeparture.before(thisShiftedDeparture) ? thisPlannedDeparture : thisShiftedDeparture;
	}
	
	/**
	 * a flight is a new flight
	 * @param flight
	 * @return
	 */
	public boolean isNewFlight(FlightBackup flight) {
		return flight.getFlightId() > InitDataBackup.plannedMaxFligthId;
	}
	
	/**
	 * if a flight is joint flight
	 * @param flight
	 * @return joint flight position
	 */
	public int getJointFlightPosition(FlightBackup flight){
		if (InitDataBackup.jointFlightMap.keySet().contains(flight.getFlightId())) {
			if (InitDataBackup.jointFlightMap.get(flight.getFlightId()) != null) {
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
	public FlightBackup getJointFlight(FlightBackup flight){
		return InitDataBackup.jointFlightMap.get(flight.getFlightId());
	}
	
	/**
	 * get how this flight is affected
	 * @param flight
	 * @return 1 departure, 2 arrival, 0 no
	 */
	public int isAffected(FlightBackup flight) {
		for (AirPortCloseBackup aClose : flight.getSourceAirPort().getCloseSchedule()) {
			if (!aClose.isAllowForTakeoff()) {
				if (flight.getDepartureTime().after(aClose.getStartTime())
						&& flight.getDepartureTime().before(aClose.getEndTime())) {
					return 1;
				}
			}
		}
		for (AirPortCloseBackup aClose : flight.getDesintationAirport().getCloseSchedule()) {
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
		int maxFlightId = InitDataBackup.maxFligthId;
		InitDataBackup.maxFligthId = maxFlightId + 1;
		return maxFlightId + 1;
	}
	
	/**
	 * get new departure time by new arrival time
	 * @param flight
	 * @param newArrivalTime
	 * @return
	 */
	public Date getDepartureTimeByArrivalTime(FlightBackup flight, Date newArrivalTime) {
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
	
	
	public Date getArrivalTimeByDepartureTime(FlightBackup flight, Date newDepartureTime){
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
	public void insertPathToOpenList(ArrayList<FlightBackup> path) throws CloneNotSupportedException{
		FlightBackup anchorFlight = path.get(path.size() - 1);
		ArrayList<FlightBackup> adjustedPart = cloneList(path);
		adjustedPart.remove(path.size() - 1);
		ArrayList<FlightBackup> orgList = new ArrayList<FlightBackup>();
		for (FlightBackup flight : originalFlights){
			if (flight.getFlightId() == anchorFlight.getFlightId()){
				break;
			}
			orgList.add(flight);
		}
		long deltaCost = calDeltaCost(orgList, adjustedPart);
		if (openArrayList.containsKey(deltaCost)){
			openArrayList.get(deltaCost).add(path);
		} else {
			ArrayList<ArrayList<FlightBackup>> pathList = new ArrayList<ArrayList<FlightBackup>>();
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
	public ArrayList<FlightBackup> cloneList(ArrayList<FlightBackup> flights) throws CloneNotSupportedException{
		ArrayList<FlightBackup> newList = new ArrayList<FlightBackup>();
		for (FlightBackup flight : flights){
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
	public long calDeltaCost(ArrayList<FlightBackup> orgAir, ArrayList<FlightBackup> newAir) {
		BigDecimal cost = new BigDecimal("0");
		// org air
		for (int i = 0; i < orgAir.size(); i++) {
			
			FlightBackup orgFlight = orgAir.get(i);
			boolean existFlg = false;
			// new air
			for (int j = 0; j < newAir.size(); j++) {
				
				FlightBackup newFlight = newAir.get(j);
				// empty
				if (i == 0 && isNewFlight(newFlight)) {
					cost = cost.add(new BigDecimal("5000"));
				}
				// exist
				if (orgFlight.getFlightId() == newFlight.getFlightId()) {
					existFlg = true;
					// delay or move up
					if (!orgFlight.getDepartureTime().equals(newFlight.getDepartureTime())) {
						BigDecimal hourDiff = UtilsBackup.hoursBetweenTime(newFlight.getDepartureTime(), orgFlight.getDepartureTime());
						
						if (hourDiff.signum() == -1){
							cost = cost.add(new BigDecimal("150").multiply(hourDiff.abs()).multiply(orgFlight.getImpCoe()));
						} else {
							cost = cost.add(new BigDecimal("100").multiply(hourDiff.abs()).multiply(orgFlight.getImpCoe()));
						}
					}
					// joint stretch
					if (InitDataBackup.jointFlightMap.get(newFlight.getFlightId()) != null) {
						if (!newFlight.getDesintationAirport().getId().equals((orgFlight.getDesintationAirport().getId()))) {
							FlightBackup nextFlight = InitDataBackup.jointFlightMap.get(newFlight.getFlightId());
							
							cost = cost.add(new BigDecimal("750").multiply(newFlight.getImpCoe()));
							cost = cost.add(new BigDecimal("750").multiply(nextFlight.getImpCoe()));
							
						}
						
					}
					
				} 
			}
			
			// cancel
			if (!existFlg) {
				// not 2nd of joint flight
				if (!InitDataBackup.jointFlightMap.containsKey(orgFlight.getFlightId()) || InitDataBackup.jointFlightMap.get(orgFlight.getFlightId()) != null) {
					cost = cost.add(new BigDecimal("1000").multiply(orgFlight.getImpCoe()));
				}
			}
			
		}
		
		return cost.longValue();
    }
	
	public void printAllFlightsIsList(ArrayList<FlightBackup> list){
		String s = "";
		for (FlightBackup f : list){
			s += f.getFlightId() + "-";
		}
		System.out.println(s);
	}
	
	public void printAllPathInList(TreeMap<Long, ArrayList<ArrayList<FlightBackup>>> tm){
		Iterator<Entry<Long, ArrayList<ArrayList<FlightBackup>>>> it = tm.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Long, ArrayList<ArrayList<FlightBackup>>> pair = (Map.Entry<Long, ArrayList<ArrayList<FlightBackup>>>) it.next();
	        long cost = pair.getKey();
	        ArrayList<ArrayList<FlightBackup>> flightsList = pair.getValue();
	        for (ArrayList<FlightBackup> fl : flightsList){
	        	String op = cost + "|";
	        	for (FlightBackup f : fl){
	        		op += f.getFlightId() + "-";
	        	}
	        	print(op);
	        }
	    }
	}
	
	public void printListInList(ArrayList<ArrayList<FlightBackup>> al){
		for (ArrayList<FlightBackup> fl : al){
			String op = "";
        	for (FlightBackup f : fl){
        		op += f.getFlightId() + "-";
        	}
        	print(op);
        }
	}
}
