package xiaMengAirline.beans;

import java.util.HashMap;

public class XiaMengAirlineSolution implements Cloneable{
	private long cost = 0;
	private HashMap<String, Aircraft> schedule = new  HashMap<String, Aircraft>();
	public long getCost() {
		return cost;
	}
	public void setCost(long cost) {
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
		this.cost = 0;
		for (Aircraft aAir:schedule.values()) {
			this.cost += 0;
		}
	}
	public void refreshCost (long detla) {
		this.cost += detla;
	}
	public long calcuateDeltaCost (XiaMengAirlineSolution oldSoluiton) {
		long deltaCost = 0;
		for (Aircraft aAir:schedule.values()) {
			deltaCost += 0;
		}
		return deltaCost;
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

}
