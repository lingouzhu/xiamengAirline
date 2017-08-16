package xiaMengAirline.newBranch.BasicObject;

public class ResourceUnavailableEventType {
	private boolean allowForTakeOff = true;
	private boolean allowForLanding = true;
	private int parkingCapability = Integer.MAX_VALUE;
	
	
	public boolean isAllowForTakeOff() {
		return allowForTakeOff;
	}
	public void setAllowForTakeOff(boolean allowForTakeOff) {
		this.allowForTakeOff = allowForTakeOff;
	}
	public boolean isAllowForLanding() {
		return allowForLanding;
	}
	public void setAllowForLanding(boolean allowForLanding) {
		this.allowForLanding = allowForLanding;
	}
	public int getParkingCapability() {
		return parkingCapability;
	}
	public void setParkingCapability(int parkingCapability) {
		this.parkingCapability = parkingCapability;
	}
}

