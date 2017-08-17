package xiaMengAirline.newBranch.BasicObject;



public class ResourceUnavailableEventType {
	public enum AllowType {
	    ALLOWED,
	    NOT_ALLOWED,
	    CONDITION;
	}
	private AllowType allowForTakeOff = AllowType.ALLOWED;
	private AllowType allowForLanding = AllowType.ALLOWED;
	private AllowType allowForParking = AllowType.ALLOWED;
	
	private int capability = Integer.MAX_VALUE;

	public AllowType getAllowForTakeOff() {
		return allowForTakeOff;
	}

	public void setAllowForTakeOff(AllowType allowForTakeOff) {
		this.allowForTakeOff = allowForTakeOff;
	}

	public AllowType getAllowForLanding() {
		return allowForLanding;
	}

	public void setAllowForLanding(AllowType allowForLanding) {
		this.allowForLanding = allowForLanding;
	}

	public AllowType getAllowForParking() {
		return allowForParking;
	}

	public void setAllowForParking(AllowType allowForParking) {
		this.allowForParking = allowForParking;
	}

	public int getCapability() {
		return capability;
	}

	public void setCapability(int capability) {
		this.capability = capability;
	}
	
	

	
}

