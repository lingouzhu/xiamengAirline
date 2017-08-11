package xiaMengAirline.newBranch.BasicObject;

import xiaMengAirline.newBranch.BusinessDomain.PassengerAdjustableMethod;

public class Passenger {
	private String passengerId; //original flight id
	private Flight assignedFlight;
	private Aircraft assignedAir;
	private int passengerNumber;
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
	public int getPassengerNumber() {
		return passengerNumber;
	}
	public void setPassengerNumber(int passengerNumber) {
		this.passengerNumber = passengerNumber;
	}
	public PassengerAdjustableMethod getAdjustableMethod() {
		return adjustableMethod;
	}
	public void setAdjustableMethod(PassengerAdjustableMethod adjustableMethod) {
		this.adjustableMethod = adjustableMethod;
	}
	public String getPassengerId() {
		return passengerId;
	}
	public void setPassengerId(String passengerId) {
		this.passengerId = passengerId;
	}
	public boolean isNormal() {
		return isNormal;
	}
	public void setNormal(boolean isNormal) {
		this.isNormal = isNormal;
	}

}
