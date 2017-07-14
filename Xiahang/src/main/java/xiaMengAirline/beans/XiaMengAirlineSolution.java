package xiaMengAirline.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
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
						&& flight.getSourceAirPort().getId() == cancelFlight.getSourceAirPort().getId()) {
					
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
		for (Aircraft aAir:schedule) {
			if (aAir.validate()) return false;
		}
		return true;
	}
	
	public void generateOutput () {
		
	}

}
