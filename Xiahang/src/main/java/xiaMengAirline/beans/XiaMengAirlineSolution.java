package xiaMengAirline.beans;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import xiaMengAirline.util.InitData;
import xiaMengAirline.util.Utils;

public class XiaMengAirlineSolution implements Cloneable{
	private BigDecimal cost = new BigDecimal("0");
	private HashMap<String, Aircraft> schedule = new  HashMap<String, Aircraft>();
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
		
		if (schedule.containsKey(aNewAircraft.getId())) {
			Aircraft current = schedule.get(aNewAircraft.getId());
			schedule.put(aNewAircraft.getId(), aNewAircraft);
			current.clear();
		} else
			schedule.put(aNewAircraft.getId(), aNewAircraft);
		
		
	}
	public void addAircraft (Aircraft aNewAircraft) {
		if (!schedule.containsKey(aNewAircraft.getId())) {
			schedule.put(aNewAircraft.getId(), aNewAircraft);
		}

	}
	
	public void refreshCost () {
		this.cost = new BigDecimal("0");
		
		List<Flight> joint1FlightList = new ArrayList<Flight>();
		List<Flight> joint2CancelFlightList = new ArrayList<Flight>();
		
		List<Aircraft> airList = new ArrayList<Aircraft> ( schedule.values());
		for (Aircraft aAir:airList) {
			if (!aAir.isCancel()) {
				for (Flight newFlight : aAir.getFlightChain()) {

					if (newFlight.getFlightId() > InitData.plannedMaxFligthId) {
						cost.add(new BigDecimal("5000"));
					} else {
						if (!newFlight.getPlannedAir().getType().equals(aAir.getType())) {
							cost.add(new BigDecimal("1000").multiply(newFlight.getImpCoe()));
						}
						
						if (!newFlight.getDepartureTime().equals(newFlight.getPlannedFlight().getDepartureTime())) {
							BigDecimal hourDiff = Utils.hoursBetweenTime(newFlight.getDepartureTime(), newFlight.getPlannedFlight().getDepartureTime());
							
							if (hourDiff.signum() == -1){
								cost.add(new BigDecimal("150").multiply(hourDiff.abs()).multiply(newFlight.getImpCoe()));
							} else {
								cost.add(new BigDecimal("100").multiply(hourDiff.abs()).multiply(newFlight.getImpCoe()));
							}
						}
						
						if (InitData.jointFlightMap.get(newFlight.getFlightId()) != null && InitData.jointFlightMap.get(newFlight.getFlightId()) != 0) {
							
							joint1FlightList.add(newFlight);
						}
						
					}
					
				}				
			} else {
				for (Flight cancelFlight : aAir.getFlightChain()) {
					if (cancelFlight.getFlightId() > InitData.plannedMaxFligthId) {
						continue;
					}
					if (InitData.jointFlightMap.get(cancelFlight.getFlightId()) != null && InitData.jointFlightMap.get(cancelFlight.getFlightId()) == 0) {
						joint2CancelFlightList.add(cancelFlight);
					} else {
						cost.add(new BigDecimal("1000").multiply(cancelFlight.getImpCoe()));
					}
					
					
				}
			}
			
		}
		// joint flight 
		for (Flight cancelFlight : joint2CancelFlightList) {
			boolean jointFlight = false;
			for (Flight flight : joint1FlightList) {
				if (InitData.jointFlightMap.get(flight.getFlightId()) == cancelFlight.getFlightId()
						&& flight.getDesintationAirport().getId() == cancelFlight.getDesintationAirport().getId()) {
					
					cost.add(new BigDecimal("750").multiply(flight.getImpCoe()));
					cost.add(new BigDecimal("750").multiply(cancelFlight.getImpCoe()));
					
					jointFlight = true;
				}
			}
			
			if (!jointFlight) {
				cost.add(new BigDecimal("1000").multiply(cancelFlight.getImpCoe()));
			}
		}
	}
	public void refreshCost (BigDecimal detla) {
		this.cost.add(detla);
	}

	public void clear () {
		for (Aircraft aAir:schedule.values()) 
			aAir.clear();
	}
	
	public Aircraft getAircraft (String id, String type, boolean autoGenerate) {
		if (schedule.containsKey(id)) {
			return (schedule.get(id));
		} else {
			if (autoGenerate) {
				Aircraft aAir = new Aircraft();
				aAir.setId(id);
				aAir.setType(type);
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
					
					if (InitData.airLimitationList.contains(airID + "_" + startPort + "_" + endPort)) {
						return true;
					}
					// 5.1 joint flight
					if (i != 0) {
						Flight preFlight = flightChain.get(i - 1);
						
						if (!preFlight.getDesintationAirport().getId().equals(flight.getSourceAirPort().getId())) {
							System.out.println("5.1 error flight connection: flightID1" + preFlight.getFlightId() + "flightID2" + flight.getFlightId());
							return true;
						}
					}
					// 5.2 air limit
					if (InitData.airLimitationList.contains(airID + "_" + startPort + "_" + endPort)) {
						System.out.println("5.2 error air limit: flightID" + flight.getFlightId());
						return true;
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
							return true;
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
							return true;
						}
					
					}	
					// 5.4 check betwween time
					if (i != 0) {
						// 5.4 check betwween time
						Flight preFlight = flightChain.get(i - 1);
						
						if (Utils.minutiesBetweenTime(flight.getDepartureTime(), preFlight.getArrivalTime()).compareTo(new BigDecimal("50")) < 0
								&& Utils.minutiesBetweenTime(flight.getDepartureTime(), preFlight.getArrivalTime()).
								compareTo(Utils.minutiesBetweenTime(flight.getPlannedFlight().getDepartureTime(), preFlight.getPlannedFlight().getArrivalTime())) != 0
								) {
							System.out.println("5.4 error time between: flightID1" + preFlight.getFlightId() + "flightID2" + flight.getFlightId());
							return true;
						}
						
						// 5.5 joint flight
//						if ((InitData.jointFlightMap.get(preFlight.getFlightId()) != null && InitData.jointFlightMap.get(preFlight.getFlightId()) != 0)) {
//							
//						}
					}
					
					
					//  5.6  start air port typhoon close
					List<AirPortClose> typhoonStartCloseSchedule = flight.getSourceAirPort().getCloseSchedule();
					for (AirPortClose aClose : typhoonStartCloseSchedule) {
						if (flight.getDepartureTime().compareTo(aClose.getStartTime()) > 0
								&& flight.getDepartureTime().compareTo(aClose.getEndTime()) < 0 && !aClose.isAllowForTakeoff()) {
							System.out.println("5.6 error start airport typhoon closed: flightID" + flight.getFlightId());
							return true;
						}
						
					}
					
					//  5.6  start air port typhoon close
					List<AirPortClose> typhoonEndCloseSchedule = flight.getDesintationAirport().getCloseSchedule();
					for (AirPortClose aClose : typhoonEndCloseSchedule) {
						if (flight.getArrivalTime().compareTo(aClose.getStartTime()) > 0
								&& flight.getArrivalTime().compareTo(aClose.getEndTime()) < 0 && !aClose.isAllowForLanding()) {
							System.out.println("5.6 error end airport typhoon closed: flightID" + flight.getFlightId());
							return true;
						}
						
						if (flight.getArrivalTime().compareTo(aClose.getStartTime()) < 0) {
							if (i >= flightChain.size()) {
								System.out.println("5.6 error air parking : flightID" + flight.getFlightId());
								return true;
							}
							Flight nextFlight = flightChain.get(i + 1);
							if (nextFlight.getDepartureTime().compareTo(aClose.getStartTime()) > 0 && aClose.getAllocatedParking() == 0) {
								System.out.println("5.6 error air parking: flightID" + flight.getFlightId());
								return true;
							}
						}
						
					}
					
					//  5.7  border limited
					if (i == 0) {
						if (!flight.getSourceAirPort().getId().equals(InitData.firstFlightMap.get(airID).getPlannedFlight().getSourceAirPort().getId())) {
							System.out.println("5.7 error wrong start airpot: flightID" + flight.getFlightId());
							return true;
						}
						
					}
					
					if (i == flightChain.size() - 1) {
						if (!flight.getDesintationAirport().getId().equals(InitData.lastFlightMap.get(airID).getPlannedFlight().getDesintationAirport().getId())) {
							System.out.println("5.7 error wrong end airpot: flightID" + flight.getFlightId());
							return true;
						}
						
					}
					
					
				}
				
				
				
			}
		} catch (Exception e) {
			e.getStackTrace();
		}
		
		return true;
	}
	
	public void generateOutput () {
		
	}

}
