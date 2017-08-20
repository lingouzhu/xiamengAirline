package xiaMengAirline.newBranch.LocalOptimize;

import java.util.ArrayList;
import java.util.Date;

import xiaMengAirline.newBranch.BasicObject.*;

public class SelectorUtil {
	
	/**
	 * check airport continuity between two flights
	 * @param firstFlight
	 * @param secondFlight
	 * @return
	 */
	public static boolean chkAirportContinuity(Flight firstFlight, Flight secondFlight) {
		if (firstFlight.getDesintationAirport().getId().equals(secondFlight.getSourceAirPort().getId())) {
			return true;
		}
		return false;
	}
	
	/**
	 * check the delay possibility
	 * 1. check the flight need to be delayed, if no need, return true
	 * 2. check the delay time meets the 24/36 hrs rule
	 * 3. check the delayed flight won't be affected by the typhoon
	 * 4. check the delayed flight won't use any typhoon parking position
	 * @param firstFlight
	 * @param secondFlight
	 * @return
	 * @throws CloneNotSupportedException
	 */
	public static boolean chkPossibleDelay(Flight firstFlight, Flight secondFlight) throws CloneNotSupportedException {
		Date nextPlannedDeparture = secondFlight.getPlannedFlight().getDepartureTime();
		Date nextMinDeparture = calculateNextDeparture(firstFlight, secondFlight);
		if (nextPlannedDeparture.before(nextMinDeparture)) {
			//check valid delay (24/36 hrs)
			if (!chkValidDelay(nextPlannedDeparture, nextMinDeparture, secondFlight.isInternationalFlight())) {
				return false;
			}
			Flight clonedSecondFlight = secondFlight.clone();
			clonedSecondFlight.setDepartureTime(nextMinDeparture);
			clonedSecondFlight.setArrivalTime(calculateArrivalTime(clonedSecondFlight));
			//if the delayed flight is affected, consider this flight as not suitable
			if (chkFlightAffected(clonedSecondFlight)){
				return false;
			}
			//make sure the new delayed flight won't park over typhoon to save the parking place for shuttle
			if (chkTyphoonParking(firstFlight, clonedSecondFlight)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * check a route is flyable by an aircraft
	 * @param flight
	 * @param aircraft
	 * @return
	 */
	public static boolean chkValidRouteByAircraft(Flight flight, Aircraft aircraft) {
		String sourceAirport = flight.getSourceAirPort().getId();
		String destnationAirport = flight.getDesintationAirport().getId();
		String aircraftType = aircraft.getType();
		//call business method to check
		
		return true;
	}
	
	/**
	 * check the delay is valid, normally 24 hrs for domestic & 36 hrs for international flight
	 * @param plannedTime
	 * @param actualTime
	 * @param isInternational
	 * @return true for valid
	 */
	public static boolean chkValidDelay(Date actualTime, Date plannedTime, boolean isInternational) {
		return true;
	}
	
	/**
	 * 
	 * @param firstFlight
	 * @param secondFlight
	 * @return true for is typhoon parking
	 */
	public static boolean chkTyphoonParking(Flight firstFlight, Flight secondFlight) {
		return false;
	}
	
	/**
	 * check if a flight is affected by thyphoon
	 * @param flight
	 * @return true for is affected
	 */
	public static boolean chkFlightAffected(Flight flight){
		//call method in business domain
		return false;
	}
	
	/**
	 * check if a flight is in the adjustable window. 
	 * e.g. after 5/6 6:00
	 * @param flight
	 * @return true for is adjustable
	 */
	public static boolean chkFlightIsAdjustable(Flight flight){
		//call method in business domain
		return true;
	}
	
	/**
	 * calculate next closest departure time based on this arrival time
	 * @param flight
	 * @return
	 */
	public static Date calculateNextDeparture(Flight firstFlight, Flight secondFlight) {
		//should call business method to get the next earliest departure time
		return firstFlight.getArrivalTime();
	}
	
	/**
	 * calculate the arrival time based on the departure time
	 * @param flight
	 * @return
	 */
	public static Date calculateArrivalTime(Flight flight) {
		return flight.getArrivalTime();
	}
	
	/**
	 * calculate the delay time for this flight
	 * @param flight
	 * @return
	 */
	public static long calculateDelayTime(Flight flight) {
		Date plannedDeparture = flight.getPlannedFlight().getDepartureTime();
		Date actualDeparture = flight.getDepartureTime();
		return (actualDeparture.getTime() - plannedDeparture.getTime()) / (1000 * 60);
	}
	
	/**
	 * build next flight, if delayed return the delayed flight, else return original flight
	 * @param lastFlight
	 * @param thisFlight
	 * @return
	 * @throws CloneNotSupportedException
	 */
	public static Flight buildDelayedFlight(Flight lastFlight, Flight thisFlight) throws CloneNotSupportedException {
		Date earliestDeparture = calculateNextDeparture(lastFlight, thisFlight);
		Date plannedDeparture = thisFlight.getPlannedFlight().getDepartureTime();
		Flight clonedFlight = thisFlight.clone();
		if (plannedDeparture.before(earliestDeparture)) {
			clonedFlight.setDepartureTime(earliestDeparture);
			clonedFlight.setArrivalTime(SelectorUtil.calculateArrivalTime(clonedFlight));
		}
		return clonedFlight;
	}
	
	/**
	 * remove flights from flight list by id
	 * @param flightList
	 * @param flight
	 * @return
	 */
	public static ArrayList<Flight> removeFlightFromList(ArrayList<Flight> flightList, Flight flight){
		return flightList;
	}
}
