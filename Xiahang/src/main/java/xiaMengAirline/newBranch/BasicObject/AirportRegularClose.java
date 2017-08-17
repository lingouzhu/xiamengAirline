package xiaMengAirline.newBranch.BasicObject;

import xiaMengAirline.newBranch.BasicObject.ResourceUnavailableEventType.AllowType;

public class AirportRegularClose extends ResourceUnavailableEvent {

	@Override
	public ResourceUnavailableEventType getUnavailableEventType() {
		ResourceUnavailableEventType aEventType = new ResourceUnavailableEventType();
		aEventType.setAllowForLanding(AllowType.NOT_ALLOWED);
		aEventType.setAllowForTakeOff(AllowType.NOT_ALLOWED);
		return aEventType;
	}

}
