package xiaMengAirline.searchEngine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.SingleSearchNode;
import xiaMengAirline.beans.XiaMengAirlineSolution;

public class SolutionSearch implements java.io.Serializable {
	private static final long serialVersionUID = -3015890356402530690L;
	private HashMap<String, HashMap<Integer, ArrayList<Integer>>> timeload = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
	private Date closeStart;
	private Date openStart;
	
	public SolutionSearch() {
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			closeStart = formatter.parse("2017/05/06 16:00");
			openStart =  formatter.parse("2017/05/07 17:00");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public XiaMengAirlineSolution search(XiaMengAirlineSolution oldSolution) {
		XiaMengAirlineSolution solution;
		
		try {
			solution = oldSolution.clone();
			HashMap<String, Aircraft> schedule = new HashMap<String, Aircraft>();
			for (Aircraft aircraft : solution.getSchedule().values()) {
				if (!aircraft.isCancel()) {
					for (Flight flight : aircraft.getFlightChain()) {
						if (flight.getSourceAirPort().getId().equals("49") || 
								flight.getSourceAirPort().getId().equals("50") || 
								flight.getSourceAirPort().getId().equals("61")) {
							if ((flight.getDepartureTime().after(BusinessDomain.addHours(closeStart, -60)) && 
									flight.getDepartureTime().before(closeStart)) ||
									(flight.getDepartureTime().before(BusinessDomain.addHours(openStart, 120)) && 
									flight.getDepartureTime().after(openStart))) {
								int timePoint = (int) BusinessDomain.getMinuteDifference(flight.getDepartureTime(), closeStart) / 5;
								if (timeload.containsKey(flight.getSourceAirPort().getId())) {
									if (timeload.get(flight.getSourceAirPort().getId()).containsKey(timePoint)) {
										if (timeload.get(flight.getSourceAirPort().getId()).get(timePoint).size() > 1) {
											int nextTimePoint = 1;
											boolean done = false;
											while (!done) {
												if (timeload.get(flight.getSourceAirPort().getId()).containsKey(timePoint + nextTimePoint)) {
													if (timeload.get(flight.getSourceAirPort().getId()).get(timePoint + nextTimePoint).size() > 1) {
														nextTimePoint++;
													} else {
														timeload.get(flight.getSourceAirPort().getId()).get(timePoint + nextTimePoint).add(flight.getFlightId());
														done = true;
													}
												} else {
													ArrayList<Integer> flightList = new ArrayList<Integer>();
													flightList.add(flight.getFlightId());
													timeload.get(flight.getSourceAirPort().getId()).put(timePoint + nextTimePoint, flightList);
													done = true;
												}
											}
											Date newDeparture = BusinessDomain.addMinutes(flight.getDepartureTime(), nextTimePoint * 5);
											flight.setDepartureTime(newDeparture);
											flight.setArrivalTime(BusinessDomain.getArrivalTimeByDepartureTime(flight, newDeparture, aircraft));
										} else {
											timeload.get(flight.getSourceAirPort().getId()).get(timePoint).add(flight.getFlightId());
										}
									} else {
										ArrayList<Integer> flightList = new ArrayList<Integer>();
										flightList.add(flight.getFlightId());
										timeload.get(flight.getSourceAirPort().getId()).put(timePoint, flightList);
									}
								} else {
									HashMap<Integer, ArrayList<Integer>> timeMap = new HashMap<Integer, ArrayList<Integer>>();
									ArrayList<Integer> flightList = new ArrayList<Integer>();
									flightList.add(flight.getFlightId());
									timeMap.put(timePoint, flightList);
									timeload.put(flight.getSourceAirPort().getId(), timeMap);
								}
							}
						}
						if (flight.getDesintationAirport().getId().equals("49") || 
								flight.getDesintationAirport().getId().equals("50") || 
								flight.getDesintationAirport().getId().equals("61")) {
							if ((flight.getArrivalTime().after(BusinessDomain.addHours(closeStart, -60)) && 
									flight.getArrivalTime().before(closeStart)) ||
									(flight.getArrivalTime().before(BusinessDomain.addHours(openStart, 120)) && 
									flight.getArrivalTime().after(openStart))) {
								int timePoint = (int) BusinessDomain.getMinuteDifference(flight.getArrivalTime(), closeStart) / 5;
								if (timeload.containsKey(flight.getSourceAirPort().getId())) {
									if (timeload.get(flight.getSourceAirPort().getId()).containsKey(timePoint)) {
										if (timeload.get(flight.getSourceAirPort().getId()).get(timePoint).size() > 1) {
											int nextTimePoint = 1;
											boolean done = false;
											while (!done) {
												if (timeload.get(flight.getSourceAirPort().getId()).containsKey(timePoint + nextTimePoint)) {
													if (timeload.get(flight.getSourceAirPort().getId()).get(timePoint + nextTimePoint).size() > 1) {
														nextTimePoint++;
													} else {
														timeload.get(flight.getSourceAirPort().getId()).get(timePoint + nextTimePoint).add(flight.getFlightId());
														done = true;
													}
												} else {
													ArrayList<Integer> flightList = new ArrayList<Integer>();
													flightList.add(flight.getFlightId());
													timeload.get(flight.getSourceAirPort().getId()).put(timePoint + nextTimePoint, flightList);
													done = true;
												}
											}
											Date newArrival = BusinessDomain.addMinutes(flight.getArrivalTime(), nextTimePoint * 5);
											flight.setArrivalTime(newArrival);
											flight.setArrivalTime(BusinessDomain.getDepartureTimeByArrivalTime(flight, newArrival, aircraft));
										} else {
											timeload.get(flight.getSourceAirPort().getId()).get(timePoint).add(flight.getFlightId());
										}
									} else {
										ArrayList<Integer> flightList = new ArrayList<Integer>();
										flightList.add(flight.getFlightId());
										timeload.get(flight.getSourceAirPort().getId()).put(timePoint, flightList);
									}
								} else {
									HashMap<Integer, ArrayList<Integer>> timeMap = new HashMap<Integer, ArrayList<Integer>>();
									ArrayList<Integer> flightList = new ArrayList<Integer>();
									flightList.add(flight.getFlightId());
									timeMap.put(timePoint, flightList);
									timeload.put(flight.getSourceAirPort().getId(), timeMap);
								}
							}
						}
					}
					
					try {
						SingleAircraftSearch sas = new SingleAircraftSearch(aircraft, true, timeload);
						SingleSearchNode ssn = sas.getAdjustedAircraftPair();
						Aircraft adjustedAircraft = aircraft.clone();
						adjustedAircraft.setAlternativeAircraft(null);
						adjustedAircraft.setFlightChain(ssn.getFlightList());
						aircraft.setAlternativeAircraft(adjustedAircraft);
						timeload = ssn.getTimeLoad();
						schedule.put(aircraft.getId(), adjustedAircraft);
					} catch (AircraftNotAdjustable anj) {
						System.out.println(aircraft.getId() + " not adjustable");
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				
			}
			
			solution.setSchedule(schedule);
			ArrayList<Integer> allFlightIds = new ArrayList<Integer>();
			Aircraft cancelAir = null;
			for (Aircraft aircraft : schedule.values()) {
				if (cancelAir == null) {
					cancelAir = aircraft.clone();
					cancelAir.setCancel(true);
					cancelAir.setAlternativeAircraft(null);
				}
				for (Flight flight : aircraft.getFlightChain()) {
					allFlightIds.add(flight.getFlightId());
				}
			}
			
			ArrayList<Flight> cancelFlights = new ArrayList<Flight>();
			for (Aircraft aircraft : oldSolution.getSchedule().values()) {
				for (Flight flight : aircraft.getFlightChain()) {
					if (!allFlightIds.contains(flight.getFlightId())) {
						cancelFlights.add(flight);
					}
				}
			}
			cancelAir.setFlightChain(cancelFlights);
			solution.replaceOrAddNewAircraft(cancelAir);
			return solution;
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
			return null;
		}
	}
	
}
