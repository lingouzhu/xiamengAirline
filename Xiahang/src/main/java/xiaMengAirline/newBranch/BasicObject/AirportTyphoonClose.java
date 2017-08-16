package xiaMengAirline.newBranch.BasicObject;

public class AirportTyphoonClose extends ResourceUnavailableEvent {
	private ResourceUnavailableEventType aEventType = new ResourceUnavailableEventType(); 
		

	@Override
	public ResourceUnavailableEventType getUnavailableEventType() {
		return aEventType;
	}


}
