package xiaMengAirline.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.FlightWithEmptySeat;
import xiaMengAirline.beans.Transit;
import xiaMengAirline.beans.XiaMengAirlineSolution;

public class Passenager implements java.io.Serializable {
	

	private static final long serialVersionUID = 2564994668054897827L;

	public static XiaMengAirlineSolution refreshPassenger(XiaMengAirlineSolution solution) {
		
		Map<String, Aircraft> schedule = solution.getSchedule();
		int count = 0;
		Map<String, List<Flight>> flightInfoForPassenger = new HashMap<String, List<Flight>>();
		Map<Integer, Flight> flightInfoMap = new HashMap<Integer, Flight>();
		Map<Integer, Integer> transitFailedMap = new HashMap<Integer, Integer>();
		List<String> transitFailedList = new ArrayList<String>();
		
		// init passenger data
		List<Aircraft> airList = new ArrayList<Aircraft>(schedule.values());
		for (Aircraft aAir : airList) {
			
			if (!aAir.isCancel()) {
				for (Flight flight : aAir.getFlightChain()) {
					
					flight.setCanceled(false);
					flight.setIsTransfer("0");
					flight.setTransferInfo("");
					count++;
					flightInfoMap.put(flight.getFlightId(), flight);
					flight.setSeatNum(aAir.getNumberOfSeats());
//					System.out.println("====0  flight :" + flight.getFlightId() +  "transitinfo"  + flight.getTransferInfo());
//					System.out.println(flight.getSourceAirPort().getId());
//					System.out.println(flight.getDesintationAirport().getId());
//					System.out.println(flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId()));
					
					
					if (flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId()) == null ) {
						List<Flight> flightList = new ArrayList<Flight>();
						flightList.add(flight);
						flightInfoForPassenger.put(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId(), flightList);
//						System.out.println("-----1: start: + " +  flight.getSourceAirPort().getId()  + "end :" + flight.getDesintationAirport().getId() + "size:" + flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId()).size());
					} else {
						flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId()).add(flight);
//						System.out.println("----2: start: + " +  flight.getSourceAirPort().getId()  + "end :" + flight.getDesintationAirport().getId() + "size:" + flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId()).size());
					}
					
				}
				
			} else {
				for (Flight flight : aAir.getFlightChain()) {
					
					flight.setIsTransfer("0");
					flight.setTransferInfo("");
					count++;
					flightInfoMap.put(flight.getFlightId(), flight);
//					System.out.println("====1  flight :" + flight.getFlightId() +  "transitinfo"  + flight.getTransferInfo());
					flight.setCanceled(true);
					if (flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId()) == null ) {
						List<Flight> flightList = new ArrayList<Flight>();
						flightList.add(flight);
						flightInfoForPassenger.put(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId(), flightList);
//						System.out.println("------3: start: + " +  flight.getSourceAirPort().getId()  + "end :" + flight.getDesintationAirport().getId() + "size:" + flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId()).size());
					} else {
						flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId()).add(flight);
//						System.out.println("---4: start: + " +  flight.getSourceAirPort().getId()  + "end :" + flight.getDesintationAirport().getId() + "size:" + flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId()).size());
					}
					
				}
			}
		}
		
		System.out.println(" total filght No.(include cancel & new) :" + count);
		
		// InitData.transitList
		
		
		for (Transit transit : InitData.transitList) {
			Flight flight1 = flightInfoMap.get(transit.getFlightID1());
			Flight flight2 = flightInfoMap.get(transit.getFlightID2());
			if (flight1 == null ) {
				System.out.println("flight1 :" + transit.getFlightID1());
			}
			
			if (flight2 == null ) {
				System.out.println("flight2 :" + transit.getFlightID2());
			}
			
			if (flight1.isCanceled()
					|| (!flight1.isCanceled() && !flight2.isCanceled() && Utils.minutiesBetweenTime(flight2.getDepartureTime(), flight1.getArrivalTime()).intValue() < transit.getTransitMins())) {
				if (transitFailedMap.get(flight1.getFlightId()) != null) {
					transitFailedMap.put(flight1.getFlightId(), transitFailedMap.get(flight1.getFlightId()) + transit.getTransitPersons());
				} else {
					transitFailedMap.put(flight1.getFlightId(), transit.getTransitPersons());
				}
				
				if (transitFailedMap.get(flight2.getFlightId()) != null) {
					transitFailedMap.put(flight2.getFlightId(), transitFailedMap.get(flight2.getFlightId()) + transit.getTransitPersons());
				} else {
					transitFailedMap.put(flight2.getFlightId(), transit.getTransitPersons());
				}
				
				
//				System.out.println("transit failed by flight1 canceled. flight1 : " + flight1.getFlightId() + ",flight2: + " +  flight2.getFlightId());
//				flight2.setSeatNum(flight2.getSeatNum() + transit.getTransitPersons());
			} 
//			else if (!flight1.isCanceled() && !flight2.isCanceled()) {
//				if (Utils.minutiesBetweenTime(flight2.getDepartureTime(), flight1.getArrivalTime()).intValue() < transit.getTransitMins()) {
////					System.out.println("transit failed by limited time. flight1 : " + flight1.getFlightId() + ",flight2: + " +  flight2.getFlightId() + ",failed person:" + transit.getTransitPersons());
//					flight2.setSeatNum(flight2.getSeatNum() + transit.getTransitPersons());
//				}
//			}
			
		}
		
		System.out.println(transitFailedMap.get(875));
		// sort flight desc
		for (String key : flightInfoForPassenger.keySet()) {
			List<Flight> flightList = flightInfoForPassenger.get(key);
			List<FlightWithEmptySeat> emptyList = new ArrayList<FlightWithEmptySeat>();
			Utils.sort(flightList, "departureTime", false);
			
			for (Flight flight : flightList) {
//				System.out.println("888 + " + emptyList.size() + "+" + flight.getDesintationAirport().getId() + "+" + flight.getSourceAirPort().getId());
				
				// adjustable and not new flight
				if (flight.isAdjustable() && flight.getFlightId() < InitData.plannedMaxFligthId) {
					
//					System.out.println("33333333 + " + emptyList.size());
					// not cancel
					if (!flight.isCanceled()) {
//						System.out.println("5555555 + flight.getDesintationAirport().getId() " + flight.getDesintationAirport().getId() + "flight.getPlannedFlight().getDesintationAirport().getId()" + flight.getPlannedFlight().getDesintationAirport().getId());
						if (!flight.getDesintationAirport().getId().equals(flight.getPlannedFlight().getDesintationAirport().getId())) {
							
//							System.out.println("4444444 + flight.getNumberOfJoinedPassenger() " + flight.getNumberOfJoinedPassenger() + ",flight.getSeatNum()" + flight.getSeatNum());
							
							if (flight.getNumberOfJoinedPassenger() < flight.getSeatNum()) {
//								System.out.println("6666666666666666666");
								emptyList.add(new FlightWithEmptySeat(flight.getFlightId(), flight.getSeatNum() - flight.getNumberOfJoinedPassenger(), flight.getDepartureTime()));
//								if (flight.getFlightId() == 243){
//									System.out.println("7777777777 + " + String.valueOf(flight.getSeatNum() - flight.getNumberOfJoinedPassenger()));
//								}
//								System.out.println("7777777777 + " + emptyList.size());
							}
						} else {
							
//							System.out.println("4444444 + flight.getNumberOfJoinedPassenger() " + flight.getNumberOfJoinedPassenger() + ",flight.getSeatNum()" + flight.getSeatNum() + ",flight.getNumberOfPassenger() + " + flight.getNumberOfPassenger());
							
							if (flight.getNumberOfPassenger() + flight.getNumberOfJoinedPassenger() <= flight.getSeatNum()) {
								emptyList.add(new FlightWithEmptySeat(flight.getFlightId(), flight.getSeatNum() - flight.getNumberOfPassenger() - flight.getNumberOfJoinedPassenger(), flight.getDepartureTime()));
//								if (flight.getFlightId() == 243){
//									System.out.println("888888888 + " + String.valueOf(flight.getSeatNum() - flight.getNumberOfPassenger() - flight.getNumberOfJoinedPassenger()));
//								}
							} else {
								int overPNo = 0;
								if (InitData.jointFlightMap.get(flight.getFlightId()) != null) {
									if (flight.getNumberOfPassenger() + flight.getNumberOfJoinedPassenger() - flight.getSeatNum() > flight.getNumberOfPassenger()) {
										overPNo = flight.getNumberOfPassenger();
									} else {
										overPNo = flight.getNumberOfPassenger() + flight.getNumberOfJoinedPassenger() - flight.getSeatNum();
									}
									
								} else {
									overPNo = flight.getNumberOfPassenger() + flight.getNumberOfJoinedPassenger() - flight.getSeatNum();
								}
								
								if (transitFailedMap.get(flight.getFlightId()) != null) {
									overPNo = overPNo - transitFailedMap.get(flight.getFlightId());
								}
								
								for (int i = emptyList.size() - 1; i > 0; i--) {
									FlightWithEmptySeat flightWithEmptySeat = emptyList.get(i);
									if (Utils.hoursBetweenTime(flightWithEmptySeat.getDepartureTime(), flight.getDepartureTime()).compareTo(new BigDecimal("48")) <= 0) {
										// with empty seat
										if (flightWithEmptySeat.getEmptySeatNo() > 0) {
											if (flightWithEmptySeat.getEmptySeatNo() >= overPNo) {
												// hour check + 中转
												flightWithEmptySeat.setEmptySeatNo(flightWithEmptySeat.getEmptySeatNo() - overPNo);
												flight.setIsTransfer("1");
												flight.setTransferInfo(flight.getTransferInfo() + "&" + String.valueOf(flightWithEmptySeat.getFlightID()) + ":" + String.valueOf(overPNo));
												break;
											} else {
												
												flight.setIsTransfer("1");
												flight.setTransferInfo(flight.getTransferInfo() + "&" + String.valueOf(flightWithEmptySeat.getFlightID()) + ":" + flightWithEmptySeat.getEmptySeatNo());
												// hour check + 中转
												overPNo = overPNo - flightWithEmptySeat.getEmptySeatNo();
												flightWithEmptySeat.setEmptySeatNo(0);
												
											}
											
										}
									}
									
									
								}
								
							}
						}
						
					} else {
//						System.out.println("11111111 + " + flight.getFlightId());
//						System.out.println("11111111 + " + flight.getDesintationAirport().getId());
//						System.out.println("11111111 + " + flight.getSourceAirPort().getId());
						
						int overPNo = flight.getNumberOfPassenger();
						
						if (transitFailedMap.get(flight.getFlightId()) != null) {
							overPNo = overPNo - transitFailedMap.get(flight.getFlightId());
						}
						
						
						for (int i = emptyList.size() - 1; i > 0; i--) {
							FlightWithEmptySeat flightWithEmptySeat = emptyList.get(i);
//							System.out.println("222222222 +" + flightWithEmptySeat.getFlightID());
							
							if (Utils.hoursBetweenTime(flightWithEmptySeat.getDepartureTime(), flight.getDepartureTime()).compareTo(new BigDecimal("48")) <= 0) {
								// with empty seat
								if (flightWithEmptySeat.getEmptySeatNo() > 0) {
									if (flightWithEmptySeat.getEmptySeatNo() >= overPNo) {
										flightWithEmptySeat.setEmptySeatNo(flightWithEmptySeat.getEmptySeatNo() - overPNo);
										flight.setIsTransfer("1");
										flight.setTransferInfo(flight.getTransferInfo() + "&" + String.valueOf(flightWithEmptySeat.getFlightID()) + ":" + String.valueOf(overPNo));
										break;
									} else {
										flight.setIsTransfer("1");
										flight.setTransferInfo(flight.getTransferInfo() + "&" + String.valueOf(flightWithEmptySeat.getFlightID()) + ":" + flightWithEmptySeat.getEmptySeatNo());
										
										overPNo = overPNo - flightWithEmptySeat.getEmptySeatNo();
										flightWithEmptySeat.setEmptySeatNo(0);
										
									}
									
								}
							}
							
							
						}
						
					
					}
					
					
				}
				
			}
			
		}
		
		return solution;
		
	}
	
	
	
	
	
}
