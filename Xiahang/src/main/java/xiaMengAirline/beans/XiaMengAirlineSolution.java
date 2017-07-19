package xiaMengAirline.beans;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import xiaMengAirline.util.CSVUtils;
import xiaMengAirline.util.InitData;
import xiaMengAirline.util.Utils;

public class XiaMengAirlineSolution implements Cloneable{
	private BigDecimal cost = new BigDecimal("0");
	private HashMap<String, Aircraft> schedule = new  HashMap<String, Aircraft>();
	
	private List<String> outputList = new ArrayList<String>();
	
	public BigDecimal getCost() {
		return cost;
	}
	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}
	
	public XiaMengAirlineSolution clone() throws CloneNotSupportedException{
		XiaMengAirlineSolution aNewSolution = (XiaMengAirlineSolution) super.clone();
		 HashMap<String, Aircraft> newSchedule = new  HashMap<String, Aircraft> ();
		for (String aAir:schedule.keySet()) {
			newSchedule.put(aAir, schedule.get(aAir).clone());
		}
		aNewSolution.setSchedule(newSchedule);
		return aNewSolution;
	}
	public void replaceOrAddNewAircraft (Aircraft aNewAircraft) {
		
		String aKey = aNewAircraft.getId();
		if (aNewAircraft.isCancel())
			aKey += "_CANCEL";
		else
			aKey += "_NORMAL";
		if (schedule.containsKey(aKey)) {
			Aircraft current = schedule.get(aNewAircraft.getId());
			schedule.put(aKey, aNewAircraft);
			current.clear();
		} else
			schedule.put(aKey, aNewAircraft);
		
		
	}
	
	public void refreshCost (boolean refreshOut) {
		this.cost = new BigDecimal("0");
		
		outputList = new ArrayList<String>();
		
		
		List<Aircraft> airList = new ArrayList<Aircraft> ( schedule.values());
		for (Aircraft aAir:airList) {
			if (!aAir.isCancel()) {
				for (Flight newFlight : aAir.getFlightChain()) {

					if (newFlight.getFlightId() > InitData.plannedMaxFligthId) {
						cost = cost.add(new BigDecimal("5000"));
						if (refreshOut) {
							outputList.add(CSVUtils.flight2Output(newFlight, aAir.getId(), "0", "0", "1"));
						}
					} else {
						boolean isChanged = false;
						boolean isStretch = false;
						if (!newFlight.getPlannedAir().getType().equals(aAir.getType())) {
							cost = cost.add(new BigDecimal("1000").multiply(newFlight.getImpCoe()));
							isChanged = true;
						}
						
						if (!newFlight.getDepartureTime().equals(newFlight.getPlannedFlight().getDepartureTime())) {
							BigDecimal hourDiff = Utils.hoursBetweenTime(newFlight.getDepartureTime(), newFlight.getPlannedFlight().getDepartureTime());
							
							if (hourDiff.signum() == -1){
								cost = cost.add(new BigDecimal("150").multiply(hourDiff.abs()).multiply(newFlight.getImpCoe()));
							} else {
								cost = cost.add(new BigDecimal("100").multiply(hourDiff.abs()).multiply(newFlight.getImpCoe()));
							}
							isChanged = true;
						}
						
						if (InitData.jointFlightMap.get(newFlight.getFlightId()) != null) {
							if (!newFlight.getDesintationAirport().getId().equals((newFlight.getPlannedFlight().getDesintationAirport().getId()))) {
								Flight nextFlight = InitData.jointFlightMap.get(newFlight.getFlightId());
								
								cost = cost.add(new BigDecimal("750").multiply(newFlight.getImpCoe()));
								cost = cost.add(new BigDecimal("750").multiply(nextFlight.getImpCoe()));
								isStretch = true;
								if (refreshOut) {
									outputList.add(CSVUtils.flight2Output(newFlight, aAir.getId(), "0", "1", "0"));
									outputList.add(CSVUtils.flight2Output(nextFlight, aAir.getId(), "1", "1", "0"));
								}
								
							}
							
						}
						
						if (refreshOut && !isStretch) {
							if (refreshOut) {
								outputList.add(CSVUtils.flight2Output(newFlight, aAir.getId(), "0", "0", "0"));
							}
						}
					}
					
				}	
				
			} else {
				for (Flight cancelFlight : aAir.getFlightChain()) {
					if (cancelFlight.getFlightId() > InitData.plannedMaxFligthId) {
						continue;
					}
					
					cost = cost.add(new BigDecimal("1000").multiply(cancelFlight.getImpCoe()));
					if (refreshOut) {
						outputList.add(CSVUtils.flight2Output(cancelFlight, aAir.getId(), "1", "0", "0"));
					}
					
				}
			}
			
		}
		// joint flight 
//		for (Flight cancelFlight : joint2CancelFlightList) {
//			for (Flight flight : joint1FlightList) {
//				if (InitData.jointFlightMap.get(flight.getFlightId()).getFlightId() == cancelFlight.getFlightId()) {
//					
//					cost.add(new BigDecimal("750").multiply(flight.getImpCoe()));
//					cost.add(new BigDecimal("750").multiply(cancelFlight.getImpCoe()));
//					
//				}
//			}
//		}
	}
	public void refreshCost (BigDecimal detla) {
		this.cost.add(detla);
	}

	public void clear () {
		for (Aircraft aAir:schedule.values()) 
			aAir.clear();
	}
	
	public Aircraft getAircraft (String id, String type, boolean isCancel, boolean autoGenerate) {
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
	
	public boolean validate (boolean isCheckLianChengOnly) {
		List<Aircraft> schedule = new ArrayList<Aircraft> ( getSchedule().values());
		try {
			for (Aircraft aAir:schedule) {
				
				List<Flight> flightChain = aAir.getFlightChain();
				
				for (int i = 0; i < flightChain.size(); i++) {
					Flight flight = flightChain.get(i);
					
					String startPort = flight.getSourceAirPort().getId();
					String endPort =  flight.getDesintationAirport().getId();
					String airID =  aAir.getId();
					
//					if (InitData.airLimitationList.contains(airID + "_" + startPort + "_" + endPort)) {
//						return false;
//					}
					// 5.0 departure time check
					if (flight.getFlightId() <= InitData.plannedMaxFligthId) {
						if (flight.isInternationalFlight()) {
							if (Utils.hoursBetweenTime(flight.getDepartureTime(), flight.getPlannedFlight().getDepartureTime()).compareTo(new BigDecimal("36")) > 0
									|| flight.getDepartureTime().after(flight.getPlannedFlight().getDepartureTime())) {
								System.out.println("5.0 error departure time: flightID" + flight.getFlightId());
								return false;
							}
						} else {
							if (Utils.hoursBetweenTime(flight.getDepartureTime(), flight.getPlannedFlight().getDepartureTime()).compareTo(new BigDecimal("24")) > 0
									|| Utils.hoursBetweenTime(flight.getDepartureTime(), flight.getPlannedFlight().getDepartureTime()).compareTo(new BigDecimal("-6")) <  0) {
								System.out.println("5.0 error departure time: flightID" + flight.getFlightId());
								return false;
							}
						}
					}
					
					// 5.1 joint flight
					if (i != 0) {
						Flight preFlight = flightChain.get(i - 1);
						
						if (!preFlight.getDesintationAirport().getId().equals(flight.getSourceAirPort().getId())) {
							System.out.println("5.1 error flight connection: flightID1" + preFlight.getFlightId() + "flightID2" + flight.getFlightId());
							return false;
						}
					}
					// 5.2 air limit
					if (InitData.airLimitationList.contains(airID + "_" + startPort + "_" + endPort)) {
						System.out.println("5.2 error air limit: flightID" + flight.getFlightId());
						return false;
					}
					// 5.3  start air port regular close
					List<RegularAirPortClose> regularStartCloseSchedule = flight.getSourceAirPort().getRegularCloseSchedule();
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
							System.out.println("5.3 error start airport regular closed: flightID" + flight.getFlightId());
							return false;
						}
					
					}	
					// 5.3  end air port regular close
					List<RegularAirPortClose> regularEndCloseSchedule = flight.getDesintationAirport().getRegularCloseSchedule();
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
							System.out.println("5.3 error end airport regular closed: flightID" + flight.getFlightId());
							return false;
						}
					
					}	
					// 5.4 check betwween time
					if (i != 0) {
						// 5.4 check betwween time
						Flight preFlight = flightChain.get(i - 1);
						
						if (Utils.minutiesBetweenTime(flight.getDepartureTime(), preFlight.getArrivalTime()).compareTo(new BigDecimal("50")) < 0
								&& (preFlight.getFlightId() > InitData.plannedMaxFligthId || flight.getFlightId() > InitData.plannedMaxFligthId 
										|| Utils.minutiesBetweenTime(flight.getDepartureTime(), preFlight.getArrivalTime()).
								compareTo(Utils.minutiesBetweenTime(flight.getPlannedFlight().getDepartureTime(), preFlight.getPlannedFlight().getArrivalTime())) != 0
								|| !flight.getPlannedAir().getId().equals(preFlight.getPlannedAir().getId()))) {
							System.out.println("5.4 error time between: flightID1" + preFlight.getFlightId() + "flightID2" + flight.getFlightId());
							return false;
						}
						
						// 5.5 joint flight
						if (InitData.jointFlightMap.get(preFlight.getFlightId()) != null) {
							if (preFlight.getDesintationAirport().getId().equals((preFlight.getPlannedFlight().getDesintationAirport().getId()))
									&& InitData.jointFlightMap.get(preFlight.getFlightId()).getFlightId() != flight.getFlightId()) {
								System.out.println("5.5 error joint flight : flightID1" + preFlight.getFlightId() + "flightID2" + flight.getFlightId());
								return false;
							}
						}
					}
					
					
					//  5.6  start air port typhoon close
					List<AirPortClose> typhoonStartCloseSchedule = flight.getSourceAirPort().getCloseSchedule();
					for (AirPortClose aClose : typhoonStartCloseSchedule) {
						if (flight.getDepartureTime().compareTo(aClose.getStartTime()) > 0
								&& flight.getDepartureTime().compareTo(aClose.getEndTime()) < 0 && !aClose.isAllowForTakeoff()) {
							System.out.println("5.6 error start airport typhoon closed: flightID" + flight.getFlightId());
							return false;
						}
						
					}
					
					//  5.6  start air port typhoon close
					List<AirPortClose> typhoonEndCloseSchedule = flight.getDesintationAirport().getCloseSchedule();
					for (AirPortClose aClose : typhoonEndCloseSchedule) {
						if (flight.getArrivalTime().compareTo(aClose.getStartTime()) > 0
								&& flight.getArrivalTime().compareTo(aClose.getEndTime()) < 0 && !aClose.isAllowForLanding()) {
							System.out.println("5.6 error end airport typhoon closed: flightID" + flight.getFlightId());
							return false;
						}
						
						if (flight.getArrivalTime().compareTo(aClose.getStartTime()) < 0) {
							if (i >= flightChain.size()) {
								System.out.println("5.6 error air parking : flightID" + flight.getFlightId());
								return false;
							}
							Flight nextFlight = flightChain.get(i + 1);
							if (nextFlight.getDepartureTime().compareTo(aClose.getStartTime()) > 0 && aClose.getAllocatedParking() == 0) {
								System.out.println("5.6 error air parking: flightID" + flight.getFlightId());
								return false;
							}
						}
						
					}
					
					//  5.7  border limited
					if (i == 0) {
						if (!flight.getSourceAirPort().getId().equals(InitData.firstFlightMap.get(airID).getPlannedFlight().getSourceAirPort().getId())) {
							System.out.println("5.7 error wrong start airpot: flightID" + flight.getFlightId());
							return false;
						}
						
					}
					
					if (i == flightChain.size() - 1) {
						if (!flight.getDesintationAirport().getId().equals(InitData.lastFlightMap.get(airID).getPlannedFlight().getDesintationAirport().getId())) {
							System.out.println("5.7 error wrong end airpot: flightID" + flight.getFlightId());
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
	
	public void generateOutput(String minutes) {
		CSVUtils.exportCsv(new File("数据森林" + "_" + cost.toString() + "_" +  minutes), outputList);
	}

}
