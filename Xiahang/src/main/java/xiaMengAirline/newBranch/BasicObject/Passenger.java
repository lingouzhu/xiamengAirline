package xiaMengAirline.newBranch.BasicObject;

import xiaMengAirline.newBranch.BusinessDomain.PassengerAdjustableMethod;

public class Passenger {
	private Flight assignedFlight;
	private Aircraft assignedAir;
	private Flight plannedFlight;
	private Aircraft plannedAir;
	private boolean isNormal; //normal passenger or joined
	private PassengerAdjustableMethod adjustableMethod;
	
	
	public Flight getAssignedFlight() {
		return assignedFlight;
	}
	public void setAssignedFlight(Flight assignedFlight) {
		this.assignedFlight = assignedFlight;
	}
	public Aircraft getAssignedAir() {
		return assignedAir;
	}
	public void setAssignedAir(Aircraft assignedAir) {
		this.assignedAir = assignedAir;
	}

	public PassengerAdjustableMethod getAdjustableMethod() {
		return adjustableMethod;
	}
	public void setAdjustableMethod(PassengerAdjustableMethod adjustableMethod) {
		this.adjustableMethod = adjustableMethod;
	}

	public Passenger(Flight assignedFlight, Aircraft assignedAir) {
		super();
		this.assignedFlight = assignedFlight;
		this.assignedAir = assignedAir;
		this.plannedFlight = assignedFlight;
		this.plannedAir = assignedAir;
	}
	public boolean isNormal() {
		return isNormal;
	}
	public void setNormal(boolean isNormal) {
		this.isNormal = isNormal;
	}
	public Flight getPlannedFlight() {
		return plannedFlight;
	}
	public void setPlannedFlight(Flight plannedFlight) {
		this.plannedFlight = plannedFlight;
	}
	public Aircraft getPlannedAir() {
		return plannedAir;
	}
	public void setPlannedAir(Aircraft plannedAir) {
		this.plannedAir = plannedAir;
	}

}
