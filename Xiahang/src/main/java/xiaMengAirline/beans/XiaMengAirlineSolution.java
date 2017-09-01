package xiaMengAirline.beans;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.searchEngine.SingleAircraftSearch;
import xiaMengAirline.utils.CSVUtils;
import xiaMengAirline.utils.InitData;
import xiaMengAirline.utils.Utils;

public class XiaMengAirlineSolution implements Cloneable {
	private static final Logger logger = Logger.getLogger(XiaMengAirlineSolution.class);
	private BigDecimal cost = new BigDecimal("0");
	private String strCost = "";
	private HashMap<String, Aircraft> schedule = new HashMap<String, Aircraft>();
	private HashMap<String, List<Flight>> flightInfoForPassenger = new HashMap<String, List<Flight>>();

	private List<String> outputList = new ArrayList<String>();

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public XiaMengAirlineSolution clone() throws CloneNotSupportedException {
		XiaMengAirlineSolution aNewSolution = (XiaMengAirlineSolution) super.clone();
		HashMap<String, Aircraft> newSchedule = new HashMap<String, Aircraft>();
		for (String aAir : schedule.keySet()) {
			newSchedule.put(aAir, schedule.get(aAir).clone());
		}
		aNewSolution.setSchedule(newSchedule);
		return aNewSolution;
	}

	public void replaceOrAddNewAircraft(Aircraft aNewAircraft) {

		String aKey = aNewAircraft.getId();

		aNewAircraft.setUpdated(true);

		if (aNewAircraft.isCancel())
			aKey += "_CANCEL";
		else
			aKey += "_NORMAL";
		if (schedule.containsKey(aKey)) {
			Aircraft current = schedule.get(aKey);
			schedule.put(aKey, aNewAircraft);
			current.clear();
		} else
			schedule.put(aKey, aNewAircraft);

	}

	public void refreshCost(boolean refreshOut) {
		this.cost = new BigDecimal("0");

		outputList = new ArrayList<String>();

		BigDecimal cancel = new BigDecimal("0");
		int empty = 0;
		int change = 0;
		BigDecimal delay = new BigDecimal("0");
		BigDecimal ahead = new BigDecimal("0");
		BigDecimal connect = new BigDecimal("0");

		List<Aircraft> airList = new ArrayList<Aircraft>(schedule.values());
		for (Aircraft aAir : airList) {
			// System.out.println("Cost Air: " + aAir.getId());
			// for (Flight aFlight:aAir.getFlightChain()) {
			// System.out.println(" Flight " + aFlight.getFlightId() + "
			// Departure Time: " +
			// Utils.timeFormatter2(aFlight.getDepartureTime()));
			// }
			if (!aAir.isCancel()) {
				for (Flight newFlight : aAir.getFlightChain()) {

					if (newFlight.getFlightId() > InitData.plannedMaxFligthId) {
						cost = cost.add(new BigDecimal("5000"));
						empty++;
						if (refreshOut) {
							outputList.add(CSVUtils.flight2Output(newFlight, aAir.getId(), "0", "0", "1", "0", ""));
						}
					} else {
						boolean isChanged = false;
						boolean isStretch = false;
						if (!newFlight.getPlannedAir().getType().equals(aAir.getType())) {
							
							Date changeTime = null;
							try {
								changeTime = Utils.stringFormatToTime2("06/06/2017 16:00:00");
							} catch (ParseException e) {
								System.out.println("change time error");
								e.printStackTrace();
								
							}
							
							// change air cost
							if (newFlight.getDepartureTime().after(changeTime)) {
								cost = cost.add(new BigDecimal("5").multiply(newFlight.getImpCoe()));
							} else {
								cost = cost.add(new BigDecimal("15").multiply(newFlight.getImpCoe()));
							}
							
							
							cost = cost.add(new BigDecimal(InitData.changeAirCostMap.get(newFlight.getPlannedAir().getType() + "_" + aAir.getType()))
									.multiply(new BigDecimal("500")).multiply(newFlight.getImpCoe()));
							
							isChanged = true;
							change++;
						}

						if (!newFlight.getDepartureTime().equals(newFlight.getPlannedFlight().getDepartureTime())) {
							BigDecimal hourDiff = Utils.hoursBetweenTime(newFlight.getDepartureTime(),
									newFlight.getPlannedFlight().getDepartureTime());

							if (hourDiff.signum() == -1) {
								// System.out.println(newFlight.getDepartureTime());
								// System.out.println(newFlight.getPlannedFlight().getDepartureTime());
								// System.out.println(hourDiff);

								ahead = ahead.add(hourDiff.abs().multiply(newFlight.getImpCoe()));
								cost = cost.add(
										new BigDecimal("150").multiply(hourDiff.abs()).multiply(newFlight.getImpCoe()));
							} else {
								delay = delay.add(hourDiff.abs().multiply(newFlight.getImpCoe()));
								cost = cost.add(
										new BigDecimal("100").multiply(hourDiff.abs()).multiply(newFlight.getImpCoe()));
							}
							isChanged = true;
						}

						if (InitData.jointFlightMap.get(newFlight.getFlightId()) != null) {
							if (!newFlight.getDesintationAirport().getId()
									.equals((newFlight.getPlannedFlight().getDesintationAirport().getId()))) {
								Flight nextFlight = InitData.jointFlightMap.get(newFlight.getFlightId());

								cost = cost.add(new BigDecimal("750").multiply(newFlight.getImpCoe()));
								cost = cost.add(new BigDecimal("750").multiply(nextFlight.getImpCoe()));
								connect = connect.add(newFlight.getImpCoe());
								connect = connect.add(nextFlight.getImpCoe());
								isStretch = true;
								if (refreshOut) {
									outputList.add(CSVUtils.flight2Output(newFlight, aAir.getId(), "0", "1", "0", newFlight.getIsTransfer(), newFlight.getTransferInfo()));
									outputList.add(CSVUtils.flight2Output(nextFlight, aAir.getId(), "1", "1", "0", newFlight.getIsTransfer(), newFlight.getTransferInfo()));
								}

							}

						}

						if (refreshOut && !isStretch) {
							if (refreshOut) {
								outputList.add(CSVUtils.flight2Output(newFlight, aAir.getId(), "0", "0", "0", newFlight.getIsTransfer(), newFlight.getTransferInfo()));
							}
						}
					}

				}

			} else {

				for (Flight cancelFlight : aAir.getFlightChain()) {
					if (cancelFlight.getFlightId() > InitData.plannedMaxFligthId) {
						continue;
					}
					cancel = cancel.add(cancelFlight.getImpCoe());
					cost = cost.add(new BigDecimal("1200").multiply(cancelFlight.getImpCoe()));
					if (refreshOut) {
						outputList.add(CSVUtils.flight2Output(cancelFlight, aAir.getId(), "1", "0", "0", cancelFlight.getIsTransfer(), cancelFlight.getTransferInfo()));
					}

				}

			}

		}
		// System.out.println("empty:" + empty);
		// System.out.println("change:" + change);
		// System.out.println("cancel:" + cancel.toString());
		// System.out.println("delay:" + delay.toString());
		// System.out.println("ahead:" + ahead.toString());
		// System.out.println("connect:" + connect.toString());
		// joint flight
		// for (Flight cancelFlight : joint2CancelFlightList) {
		// for (Flight flight : joint1FlightList) {
		// if (InitData.jointFlightMap.get(flight.getFlightId()).getFlightId()
		// == cancelFlight.getFlightId()) {
		//
		// cost.add(new BigDecimal("750").multiply(flight.getImpCoe()));
		// cost.add(new BigDecimal("750").multiply(cancelFlight.getImpCoe()));
		//
		// }
		// }
		// }

	}

	
	public void refreshPassenger() {
		
		// init passenger data
		List<Aircraft> airList = new ArrayList<Aircraft>(schedule.values());
		for (Aircraft aAir : airList) {
			if (!aAir.isCancel()) {
				for (Flight flight : aAir.getFlightChain()) {
					flight.setSeatNum(aAir.getNumberOfSeats());
					if (flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport()) != null ) {
						List<Flight> flightList = new ArrayList<Flight>();
						flightList.add(flight);
						flightInfoForPassenger.put(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId(), flightList);
					} else {
						flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId()).add(flight);
					}
					
				}
				
			} else {
				for (Flight flight : aAir.getFlightChain()) {
					flight.setCancel(true);
					if (flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport()) != null ) {
						List<Flight> flightList = new ArrayList<Flight>();
						flightList.add(flight);
						flightInfoForPassenger.put(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId(), flightList);
					} else {
						flightInfoForPassenger.get(flight.getSourceAirPort().getId() + "_" +  flight.getDesintationAirport().getId()).add(flight);
					}
					
				}
			}
		}
		
		
		
		// sort flight desc
		for (String key : flightInfoForPassenger.keySet()) {
			List<Flight> flightList = flightInfoForPassenger.get(key);
			List<FlightWithEmptySeat> emptyList = new ArrayList<FlightWithEmptySeat>();
			Utils.sort(flightList, "departureTime", false);
			
			for (Flight flight : flightList) {
				// adjustable and not new flight
				if (flight.isAdjustable() && flight.getFlightId() < InitData.plannedMaxFligthId) {
					// not cancel
					if (!flight.isCancel()) {
						if (flight.getDesintationAirport().getId().equals(flight.getPlannedFlight().getDesintationAirport().getId())) {
							if (flight.getNumberOfJoinedPassenger() < flight.getSeatNum()) {
								emptyList.add(new FlightWithEmptySeat(flight.getFlightId(), flight.getSeatNum() - flight.getNumberOfJoinedPassenger()));
							}
						} else {
							if (flight.getNumberOfPassenger() + flight.getNumberOfJoinedPassenger() <= flight.getSeatNum()) {
								emptyList.add(new FlightWithEmptySeat(flight.getFlightId(), flight.getSeatNum() - flight.getNumberOfPassenger() - flight.getNumberOfJoinedPassenger()));
							} else {
								int overPNo = flight.getNumberOfPassenger() + flight.getNumberOfJoinedPassenger() - flight.getSeatNum();
								
								for (int i = emptyList.size() - 1; i > 0; i--) {
									FlightWithEmptySeat flightWithEmptySeat = emptyList.get(i);
									// with empty seat
									if (flightWithEmptySeat.getEmptySeatNo() > 0) {
										if (flightWithEmptySeat.getEmptySeatNo() >= overPNo) {
											flightWithEmptySeat.setEmptySeatNo(flightWithEmptySeat.getEmptySeatNo() - overPNo);
											flight.setIsTransfer("1");
											flight.setTransferInfo(flight.getTransferInfo() + "&" + String.valueOf(flightWithEmptySeat.getFlightID()) + ":" + String.valueOf(overPNo));
											break;
										} else {
											flight.setIsTransfer("1");
											flight.setTransferInfo(flight.getTransferInfo() + "&" + String.valueOf(flightWithEmptySeat.getFlightID()) + ":" + String.valueOf(overPNo - flightWithEmptySeat.getEmptySeatNo()));
											
											overPNo = overPNo - flightWithEmptySeat.getEmptySeatNo();
											flightWithEmptySeat.setEmptySeatNo(0);
											
										}
										
									}
									
								}
								
							}
						}
						
					} else {

						int overPNo = flight.getNumberOfPassenger() + flight.getNumberOfJoinedPassenger();
						
						for (int i = emptyList.size() - 1; i > 0; i--) {
							FlightWithEmptySeat flightWithEmptySeat = emptyList.get(i);
							// with empty seat
							if (flightWithEmptySeat.getEmptySeatNo() > 0) {
								if (flightWithEmptySeat.getEmptySeatNo() >= overPNo) {
									flightWithEmptySeat.setEmptySeatNo(flightWithEmptySeat.getEmptySeatNo() - overPNo);
									flight.setIsTransfer("1");
									flight.setTransferInfo(flight.getTransferInfo() + "&" + String.valueOf(flightWithEmptySeat.getFlightID()) + ":" + String.valueOf(overPNo));
									break;
								} else {
									flight.setIsTransfer("1");
									flight.setTransferInfo(flight.getTransferInfo() + "&" + String.valueOf(flightWithEmptySeat.getFlightID()) + ":" + String.valueOf(overPNo - flightWithEmptySeat.getEmptySeatNo()));
									
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
	
	
	public Flight getFlightByFlightId(int aFlightId) {
		
		List<Aircraft> airList = new ArrayList<Aircraft>(schedule.values());
		for (Aircraft aAir : airList) {
			for (Flight flight : aAir.getFlightChain()) {
				if (flight.getFlightId() == aFlightId)
					return flight;
			}	
		}
		return null;
	}
	
	public void refreshCost () {
		setCost(new BigDecimal(0));
		List<Aircraft> airList = new ArrayList<Aircraft> (schedule.values());
		for (Aircraft air:airList) {
			this.cost = this.cost.add(new BigDecimal(air.getCost()));
		}
	}
	public void refreshCost(BigDecimal detla) {
		this.cost = this.cost.add(detla);
	}

	public void clear() {
		for (Aircraft aAir : schedule.values())
			aAir.clear();
	}

	public Aircraft getAircraft(String id, String type, boolean isCancel, boolean autoGenerate) {
		String aKey = id;
		if (isCancel)
			aKey += "_CANCEL";
		else
			aKey += "_NORMAL";
		if (schedule.containsKey(aKey)) {
			return (schedule.get(aKey));
		} else {
			if (autoGenerate) {
				Aircraft aAir = new Aircraft();
				aAir.setId(id);
				aAir.setType(type);
				aAir.setCancel(isCancel);
				schedule.put(aKey, aAir);
				return aAir;
			} else
				return null;

		}
	}

	public HashMap<String, Aircraft> getSchedule() {
		return schedule;
	}

	public void setSchedule(HashMap<String, Aircraft> schedule) {
		this.schedule = schedule;
	}

	public boolean validateIter() {
		List<Integer> cancenFlightIDList = new ArrayList<Integer>();
		List<Aircraft> schedule = new ArrayList<Aircraft>(getSchedule().values());
		try {
			for (Aircraft aAir : schedule) {
				if (aAir.isCancel()) {
					List<Flight> cancelFlightList = aAir.getFlightChain();
					for (Flight cancelFlight : cancelFlightList) {
						cancenFlightIDList.add(cancelFlight.getFlightId());
					}
				}
			}

			for (Aircraft aAir : schedule) {
				if (!aAir.isCancel()) {
					List<Flight> flightList = aAir.getFlightChain();
					for (int i = 1; i < flightList.size(); i++) {

						Flight flight = flightList.get(i);
						Flight preFlight = flightList.get(i - 1);
						// joint flight error
						if (InitData.jointFlightMap.get(preFlight.getFlightId()) != null
								&& !cancenFlightIDList
										.contains(InitData.jointFlightMap.get(preFlight.getFlightId()).getFlightId())
								&& InitData.jointFlightMap.get(preFlight.getFlightId()).getFlightId() != flight
										.getFlightId()) {
							return false;
						}

					}
				}
			}
		} catch (Exception e) {
			e.getStackTrace();
		}
		return true;
	}

	public boolean validate(boolean isCheckLianChengOnly) {
		List<Aircraft> schedule = new ArrayList<Aircraft>(getSchedule().values());
		try {
			for (Aircraft aAir : schedule) {
				if (!aAir.isCancel()) {
					List<Flight> flightChain = aAir.getFlightChain();

					for (int i = 0; i < flightChain.size(); i++) {
						Flight flight = flightChain.get(i);

						String startPort = flight.getSourceAirPort().getId();
						String endPort = flight.getDesintationAirport().getId();
						String airID = aAir.getId();

						// if (InitData.airLimitationList.contains(airID + "_" +
						// startPort + "_" + endPort)) {
						// return false;
						// }
						// 5.0 departure time check
						if (flight.getFlightId() <= InitData.plannedMaxFligthId) {
							if (flight.isInternationalFlight()) {
								if (Utils
										.hoursBetweenTime(flight.getDepartureTime(),
												flight.getPlannedFlight().getDepartureTime())
										.compareTo(new BigDecimal("36")) > 0
										|| flight.getDepartureTime()
												.before(flight.getPlannedFlight().getDepartureTime())) {
									System.out.println("5.0 error departure time: flightID" + flight.getFlightId());
									return false;
								}
							} else {
								if (Utils
										.hoursBetweenTime(flight.getDepartureTime(),
												flight.getPlannedFlight().getDepartureTime())
										.compareTo(new BigDecimal("24")) > 0 || Utils
												.hoursBetweenTime(flight.getDepartureTime(),
														flight.getPlannedFlight().getDepartureTime())
												.compareTo(new BigDecimal("-6")) < 0) {
									System.out.println("5.0 error departure time: flightID" + flight.getFlightId());
									return false;
								}
							}
						}

						// 5.1 joint flight
						if (i != 0) {
							Flight preFlight = flightChain.get(i - 1);

							if (!preFlight.getDesintationAirport().getId().equals(flight.getSourceAirPort().getId())) {
								System.out.println("5.1 error flight connection: flightID1" + preFlight.getFlightId()
										+ "flightID2" + flight.getFlightId());
								return false;
							}
						}
						// 5.2 air limit
						if (InitData.airLimitationList.contains(airID + "_" + startPort + "_" + endPort)) {
							System.out.println("5.2 error air limit: flightID" + flight.getFlightId());
							return false;
						}
						// 5.3 start air port regular close
						List<RegularAirPortClose> regularStartCloseSchedule = flight.getSourceAirPort()
								.getRegularCloseSchedule();
						for (RegularAirPortClose aClose : regularStartCloseSchedule) {
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
							String aDateC = formatter.format(flight.getSchdDate());
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
								System.out.println(
										"5.3 error start airport regular closed: flightID" + flight.getFlightId());
								return false;
							}

						}
						// 5.3 end air port regular close
						List<RegularAirPortClose> regularEndCloseSchedule = flight.getDesintationAirport()
								.getRegularCloseSchedule();
						for (RegularAirPortClose aClose : regularEndCloseSchedule) {
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
							String aDateC = formatter.format(flight.getSchdDate());
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
								System.out.println(
										"5.3 error end airport regular closed: flightID" + flight.getFlightId());
								return false;
							}

						}
						// 5.4 check betwween time
						if (i != 0) {
							// 5.4 check betwween time
							Flight preFlight = flightChain.get(i - 1);

							if (Utils.minutiesBetweenTime(flight.getDepartureTime(), preFlight.getArrivalTime())
									.compareTo(new BigDecimal("50")) < 0
									&& (preFlight.getFlightId() > InitData.plannedMaxFligthId
											|| flight.getFlightId() > InitData.plannedMaxFligthId
											|| Utils.minutiesBetweenTime(flight.getDepartureTime(),
													preFlight.getArrivalTime())
													.compareTo(Utils.minutiesBetweenTime(
															flight.getPlannedFlight().getDepartureTime(),
															preFlight.getPlannedFlight().getArrivalTime())) != 0
											|| !flight.getPlannedAir().getId()
													.equals(preFlight.getPlannedAir().getId()))) {

								// System.out.println(preFlight.getArrivalTime());
								// System.out.println(flight.getDepartureTime());

								System.out.println("5.4 error time between: flightID1" + preFlight.getFlightId()
										+ "flightID2" + flight.getFlightId());
								return false;
							}

							// 5.5 joint flight
							if (InitData.jointFlightMap.get(preFlight.getFlightId()) != null) {
								if (preFlight.getDesintationAirport().getId()
										.equals((preFlight.getPlannedFlight().getDesintationAirport().getId()))
										&& InitData.jointFlightMap.get(preFlight.getFlightId()).getFlightId() != flight
												.getFlightId()) {
									System.out.println("5.5 error joint flight : flightID1" + preFlight.getFlightId()
											+ "flightID2" + flight.getFlightId());
									return false;
								}
							}
						}

						// 5.6 start air port typhoon close
						List<AirPortClose> typhoonStartCloseSchedule = flight.getSourceAirPort().getCloseSchedule();
						for (AirPortClose aClose : typhoonStartCloseSchedule) {
							if (flight.getDepartureTime().compareTo(aClose.getStartTime()) > 0
									&& flight.getDepartureTime().compareTo(aClose.getEndTime()) < 0
									&& !aClose.isAllowForTakeoff()) {
								System.out.println(
										"5.6 error start airport typhoon closed: flightID" + flight.getFlightId());
								return false;
							}

						}

						// 5.6 start air port typhoon close
						List<AirPortClose> typhoonEndCloseSchedule = flight.getDesintationAirport().getCloseSchedule();
						for (AirPortClose aClose : typhoonEndCloseSchedule) {
							// System.out.println("5.6 error start airport
							// typhoon closed: start time:" +
							// aClose.getStartTime());
							// System.out.println("5.6 error start airport
							// typhoon closed: end time:" +
							// aClose.getEndTime());
							// System.out.println("5.6 error start airport
							// typhoon closed: isAllowForTakeoff:" +
							// aClose.isAllowForTakeoff());
							// System.out.println("5.6 error start airport
							// typhoon closed: isAllowForLanding:" +
							// aClose.isAllowForLanding());
							if (flight.getArrivalTime().compareTo(aClose.getStartTime()) > 0
									&& flight.getArrivalTime().compareTo(aClose.getEndTime()) < 0
									&& !aClose.isAllowForLanding()) {

								System.out.println(
										"5.6 error end airport typhoon closed: flightID" + flight.getFlightId());
								return false;
							}

							if (flight.getArrivalTime().compareTo(aClose.getStartTime()) < 0) {
								if (i >= flightChain.size()) {
									System.out.println("5.6 error air parking : flightID" + flight.getFlightId());
									return false;
								}
								Flight nextFlight = flightChain.get(i + 1);
								System.out.println("5.6 error air parking: " + aClose.getStartTime());
								System.out.println("5.6 error air parking: " + aClose.getAllocatedParking());
								if (nextFlight.getDepartureTime().compareTo(aClose.getStartTime()) > 0
										&& !aClose.isAllowForTakeoff()) {
									System.out.println("5.6 error air parking: " + nextFlight.getDepartureTime());
									System.out.println("5.6 error air parking: " + aClose.getStartTime());
									System.out.println("5.6 error air parking: " + aClose.getAllocatedParking());

									System.out.println("5.6 error air parking: flightID" + flight.getFlightId());
									return false;
								}
							}

						}

						// 5.7 border limited
						if (i == 0) {
							if (!flight.getSourceAirPort().getId().equals(
									InitData.firstFlightMap.get(airID).getPlannedFlight().getSourceAirPort().getId())) {
								System.out.println("5.7 error wrong start airpot: flightID" + flight.getFlightId());
								return false;
							}

						}

//						if (i == flightChain.size() - 1) {
//							if (!flight.getSourceAirPort().getId().equals(
//									InitData.lastFlightMap.get(airID).getPlannedFlight().getSourceAirPort().getId())
//									|| !flight.getDesintationAirport().getId().equals(InitData.lastFlightMap.get(airID)
//											.getPlannedFlight().getDesintationAirport().getId())) {
//								System.out.println("5.7 error wrong end airpot: flightID" + flight.getFlightId());
//								return false;
//							}
//
//						}

					}

				}
			}
		} catch (Exception e) {
			e.getStackTrace();
		}

		return true;
	}

	public void generateOutput(String minutes) {
		if (cost.toString().length() > 11) {
			this.strCost = cost.toString().substring(0, 11);
		} else {
			this.strCost = cost.toString();
		}
		CSVUtils.exportCsv(new File("数据森林" + "_" + this.strCost + "_" + minutes + ".csv"), outputList);
	}

	public String getStrCost() {
		return strCost;
	}

	public void setStrCost(String strCost) {
		this.strCost = strCost;
	}

	public boolean adjust() throws CloneNotSupportedException {
		// To-do, add adjustment solution
		List<Aircraft> airList = new ArrayList<Aircraft>(schedule.values());
		// must reset alternative first
		for (Aircraft aAir : airList) {
			if (aAir.getAlternativeAircraft() != null)
				aAir.getAlternativeAircraft().clear();
			aAir.setAlternativeAircraft(null);
		}
		try {
			for (Aircraft aAir : airList) {
				if (!aAir.isCancel()) {
					aAir.adjustment(this);
				} else {
					aAir.setAlternativeAircraft(aAir.clone());
				}
			}
		} catch (Exception ex) {
			return false;
		}

		XiaMengAirlineSolution aNewSol = reConstruct();
		aNewSol.refreshCost(false);
		cost = aNewSol.getCost();
		aNewSol.clear();
		
		if (!validAlternativeflightNumers(this)) {
			System.out.println("Invalid solution!");
		}
		
		return true; // return false, if unable to build valid solution
	}

	public XiaMengAirlineSolution reConstruct2() throws CloneNotSupportedException {
		XiaMengAirlineSolution costSolution = new XiaMengAirlineSolution();
		// List<Aircraft> airList = new ArrayList<Aircraft>(schedule.values());
		// for (Aircraft aAir : airList) {
		// logger.info("Before Reconstruct Air " + aAir.getId() + " isCancel " +
		// aAir.isCancel());
		// }
		List<Aircraft> airList = new ArrayList<Aircraft>(schedule.values());
		for (Aircraft aircraft : airList) {
			if (aircraft.getAlternativeAircraft() != null) {
				costSolution.replaceOrAddNewAircraft(aircraft.getAlternativeAircraft().clone());
			} else {
				costSolution.replaceOrAddNewAircraft(aircraft.clone());

			}

		}

		return costSolution;

	}

	public XiaMengAirlineSolution reConstruct() throws CloneNotSupportedException {
		XiaMengAirlineSolution costSolution = new XiaMengAirlineSolution();
		// List<Aircraft> airList = new ArrayList<Aircraft>(schedule.values());
		// for (Aircraft aAir : airList) {
		// logger.info("Before Reconstruct Air " + aAir.getId() + " isCancel " +
		// aAir.isCancel());
		// }
		List<Aircraft> airList = new ArrayList<Aircraft>(schedule.values());
		for (Aircraft aircraft : airList) {
			if (aircraft.getAlternativeAircraft() != null) {
				costSolution.replaceOrAddNewAircraft(aircraft.getAlternativeAircraft().clone());
			} else {
				costSolution.replaceOrAddNewAircraft(aircraft.clone());
			}
		}

		return costSolution;

	}

	public boolean validflightNumers3(XiaMengAirlineSolution anotherSolution) {
		int count = 0;
		int countb = 0;

		List<Aircraft> schedule = new ArrayList<Aircraft>(getSchedule().values());
		for (Aircraft aAir : schedule) {
			for (Flight aFlight : aAir.getFlightChain()) {
				if (aFlight.getFlightId() <= InitData.plannedMaxFligthId)
					count++;
			}
			count += aAir.getDropOutList().size();

		}

		List<Aircraft> scheduleb = new ArrayList<Aircraft>(anotherSolution.getSchedule().values());
		for (Aircraft aAir : scheduleb) {
			countb += aAir.getFlightChain().size();
		}

		if (count != countb) {
			System.out.println("Flights not matched! new " + count + " old " + countb);
			return false;
		}

		return true;
	}

	public boolean validflightNumers(XiaMengAirlineSolution anotherSolution) {
		int count = 0;
		int countb = 0;

		List<Aircraft> schedule = new ArrayList<Aircraft>(getSchedule().values());
		for (Aircraft aAir : schedule) {
			count += aAir.getFlightChain().size();
		}

		List<Aircraft> scheduleb = new ArrayList<Aircraft>(anotherSolution.getSchedule().values());
		for (Aircraft aAir : scheduleb) {
			countb += aAir.getFlightChain().size();
		}

		if (count != countb) {
			System.out.println("Flights not matched! new " + count + " old " + countb);
			return false;
		}

		return true;
	}

	public boolean validAlternativeflightNumers(XiaMengAirlineSolution oldSolution) {
		int count = 0;
		int countb = 0;
		int countDropout = 0;
		int countExtra = 0;

		List<Aircraft> schedule = new ArrayList<Aircraft>(getSchedule().values());
		for (Aircraft aAir : schedule) {
			if (aAir.getAlternativeAircraft() != null) {
				count += aAir.getAlternativeAircraft().getFlightChain().size();
				for (Flight aFlight : aAir.getAlternativeAircraft().getFlightChain()) {
					if (aFlight.getFlightId() > InitData.plannedMaxFligthId)
						countExtra++;
				}
				for (Flight aDFlight : aAir.getAlternativeAircraft().getDropOutList()) {
					if (InitData.jointFlightMap.containsKey(aDFlight.getFlightId())) {
						Flight jF = InitData.jointFlightMap.get(aDFlight.getFlightId());
						if (jF == null)
							countDropout++;
						else
							System.out.println("Drop off only for 2nd flight!");
					} else
						System.out.println("Drop off only for joined");
				}
			}
		}
		count -= countExtra;
		count += countDropout;

		countExtra = 0;
		for (Aircraft aAir : schedule) {
			countb += aAir.getFlightChain().size();
			for (Flight aFlight : aAir.getFlightChain()) {
				if (aFlight.getFlightId() > InitData.plannedMaxFligthId)
					countExtra++;
			}
		}
		countb -= countExtra;

		if (count != countb) {
			System.out.println("Alternative Flights not matched! Expected " + countb + " actual " + count);
			for (Aircraft aAir : schedule) {
				if (aAir.getAlternativeAircraft() != null) {
					System.out.println("Alt Air " + aAir.getId() + " cancel " + aAir.isCancel());
					for (Flight aFlight : aAir.getAlternativeAircraft().getFlightChain()) {
						System.out.println("    Flight Id " + aFlight.getFlightId());
					}
					for (Flight aFlight : aAir.getAlternativeAircraft().getDropOutList()) {
						System.out.println("    Dropout Flight Id " + aFlight.getFlightId());
					}
				}
			}
			for (Aircraft aAir : schedule) {
				if (aAir.getAlternativeAircraft() != null) {
					System.out.println("Main Air " + aAir.getId() + " cancel " + aAir.isCancel());
					for (Flight aFlight : aAir.getFlightChain()) {
						System.out.println("    Flight Id " + aFlight.getFlightId());
					}
				}
			}
			List<Aircraft> scheduleb = new ArrayList<Aircraft>(oldSolution.getSchedule().values());
			for (Aircraft aAir : scheduleb) {
				System.out.println("Old Air " + aAir.getId() + " cancel " + aAir.isCancel());
				for (Flight aFlight : aAir.getFlightChain()) {
					System.out.println("    Flight Id " + aFlight.getFlightId());
				}
			}
			System.out.println("Alternative Flights not matched Completed!");
			return false;
		}

		return true;
	}

	public XiaMengAirlineSolution getBestSolution() throws CloneNotSupportedException, AircraftNotAdjustable {
		XiaMengAirlineSolution bestSolution = this.clone();
		List<Aircraft> airListBase = new ArrayList<Aircraft>(bestSolution.getSchedule().values());
		// must reset alternative first
		for (Aircraft aAir : airListBase) {
			if (aAir.getAlternativeAircraft() != null)
				aAir.getAlternativeAircraft().clear();
			aAir.setAlternativeAircraft(null);
		}

		for (Aircraft aircraft : airListBase) {
			if (!aircraft.isCancel()) {
				SingleAircraftSearch sas = new SingleAircraftSearch(aircraft, true);
				ArrayList<Aircraft> resultAircraftPair = sas.getAdjustedAircraftPair();
				for (Aircraft ac : resultAircraftPair) {
					if (ac != null) {
						Aircraft aBase = bestSolution.getAircraft(ac.getId(), ac.getId(), ac.isCancel(), true);
						if (!ac.isCancel())
							aBase.setAlternativeAircraft(ac.clone());
						else {
							Aircraft cancelAir = ac.clone();
							cancelAir.getFlightChain().addAll(aBase.getFlightChain());
							aBase.setAlternativeAircraft(cancelAir);
						}

					}
				}
			} else {
				Aircraft aBase = bestSolution.getAircraft(aircraft.getId(), aircraft.getId(), aircraft.isCancel(), true);
				Aircraft aBaseAlt = aBase.getAlternativeAircraft();
				if (aBaseAlt == null) {
					aBase.setAlternativeAircraft(aircraft.clone());
				}
			}
		}
		

		if (!bestSolution.validAlternativeflightNumers(this)) {
			System.out.println("Not pass!");
		}

		return bestSolution;
	}
	
	public void printOutSolution () {
		List<Aircraft> airList = new ArrayList<Aircraft>(getSchedule().values());
		
		logger.debug("solution in details:");
		for (Aircraft aircraft : airList) {
			logger.debug("Aircraft " + aircraft.getId() + " isCanceled? " + aircraft.isCancel());
			for (Flight aFlight : aircraft.getFlightChain()) {
				logger.debug("Flight " + aFlight.getFlightId());
				logger.debug("isCancelled? " + aircraft.isCancel());
				logger.debug("source airport " + aFlight.getSourceAirPort().getId());
				logger.debug("dest airport" + aFlight.getDesintationAirport().getId());
				logger.debug("departure time " + aFlight.getDepartureTime());
				logger.debug("arrival time " + aFlight.getArrivalTime());
			}
		}
	}
}
