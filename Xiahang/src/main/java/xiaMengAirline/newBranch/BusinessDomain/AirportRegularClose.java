package xiaMengAirline.newBranch.BusinessDomain;

import java.util.ArrayList;
import java.util.List;

public class AirportRegularClose extends ResourceUnavailableEvent {

	@Override
	public List<ResourceUnavailableEventType> getUnavailableEventType() {
		List<ResourceUnavailableEventType> retEventType =  new ArrayList<ResourceUnavailableEventType> ();
		ResourceUnavailableEventType aEventType = new ResourceUnavailableEventType();
		aEventType.setAllowForLanding(false);
		aEventType.setAllowForParking(true);
		aEventType.setAllowForTakeOff(false);
		retEventType.add(aEventType);
		return retEventType;
	}

}
