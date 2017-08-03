package xiaMengAirline.newBranch.BasicObject;

import java.util.ArrayList;
import java.util.List;

import xiaMengAirline.newBranch.BusinessDomain.ResourceUnavailableEvent;
import xiaMengAirline.newBranch.BusinessDomain.ResourceUnavailableEventType;

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
