package xiaMengAirline.beans;

import java.util.ArrayList;
import java.util.List;

public class XiaMengAirlineSolution implements Cloneable{
	private long cost;
	private List<Aircraft> schedule;
	public long getCost() {
		return cost;
	}
	public void setCost(long cost) {
		this.cost = cost;
	}
	public List<Aircraft> getSchedule() {
		return schedule;
	}
	public void setSchedule(List<Aircraft> schedule) {
		this.schedule = schedule;
	}
	public XiaMengAirlineSolution clone() throws CloneNotSupportedException{
		XiaMengAirlineSolution aNewSolution = (XiaMengAirlineSolution) super.clone();
		List<Aircraft> newSchedule = new ArrayList<Aircraft> ();
		for (Aircraft aAir:schedule) {
			newSchedule.add(aAir.clone());
		}
		aNewSolution.setSchedule(newSchedule);
		return aNewSolution;
	}
	public void replaceOrAddNewAircraft (Aircraft aNewAircraft) {
		boolean isFound = false;
		for (Aircraft aAir:schedule) {
			if ((aAir.getId().equals(aNewAircraft.getId()))
				&& (aAir.isCancel() == aNewAircraft.isCancel())) 
			{
				schedule.set(schedule.indexOf(aAir), aNewAircraft);
				aAir.clear();
				isFound = true;
				break;
			}
		}
		if (!isFound)
			schedule.add(aNewAircraft);
	}
	public void refreshCost () {
		this.cost = 0;
		for (Aircraft aAir:schedule) {
			this.cost += aAir.getCost();
		}
	}
	public void refreshCost (long detla) {
		this.cost += detla;
	}
	public long calcuateDeltaCost (XiaMengAirlineSolution oldSoluiton) {
		long deltaCost = 0;
		for (Aircraft aAir:schedule) {
			deltaCost += aAir.getCost();
		}
		return deltaCost;
	}
	public void clear () {
		for (Aircraft aAir:schedule) 
			aAir.clear();
	}
	

}
