package xiaMengAirline.newBranch.BasicObject;

import java.util.ArrayList;
import java.util.List;

public class AirportRegularClose extends ResourceUnavailableEvent {

	@Override
	public List<UnavailableEventType> getFixedUnavailableEventType() {
		List<UnavailableEventType> retEventType =  new ArrayList<UnavailableEventType> ();
		UnavailableEventType aEventType = new UnavailableEventType();
		aEventType.setAllowForLanding(false);
		aEventType.setAllowForParking(true);
		aEventType.setAllowForTakeOff(false);
		return retEventType;
	}

}
