package xiaMengAirline.newBranch.BasicObject;

public class AirportRegularClose extends ResourceUnavailableEvent {

	@Override
	public ResourceUnavailableEventType getUnavailableEventType() {
		ResourceUnavailableEventType aEventType = new ResourceUnavailableEventType();
		aEventType.setAllowForLanding(false);
		aEventType.setAllowForTakeOff(false);
		return aEventType;
	}

}
