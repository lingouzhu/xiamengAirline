package xiaMengAirline.newBranch.BusinessDomain;

public class ResourceUnavailableEventType {
	private boolean allowForTakeOff = true;
	private boolean allowForLanding = true;
	private boolean allowForParking = true;
	private boolean hasFreeRoom = true;
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
	public boolean isAllowForParking() {
		return allowForParking;
	}
	public void setAllowForParking(boolean allowForParking) {
		this.allowForParking = allowForParking;
	}
	public boolean isHasFreeRoom() {
		return hasFreeRoom;
	}
	public void setHasFreeRoom(boolean hasFreeRoom) {
		this.hasFreeRoom = hasFreeRoom;
	}


}
