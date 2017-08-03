package xiaMengAirline.newBranch.BusinessDomain;

public class ResourceUnavailableEventType {
	boolean allowForTakeOff;
	boolean allowForLanding;
	boolean allowForParking;
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

}
