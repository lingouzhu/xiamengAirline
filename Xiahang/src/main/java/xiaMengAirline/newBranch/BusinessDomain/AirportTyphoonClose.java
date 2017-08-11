package xiaMengAirline.newBranch.BusinessDomain;

import java.util.ArrayList;
import java.util.List;

public class AirportTyphoonClose extends ResourceUnavailableEvent {
	private boolean allowForTakeoff;
	private boolean allowForLanding;
		

	@Override
	public List<ResourceUnavailableEventType> getUnavailableEventType() {
		List<ResourceUnavailableEventType> retEventType =  new ArrayList<ResourceUnavailableEventType> ();
		ResourceUnavailableEventType aEventType = new ResourceUnavailableEventType();
		aEventType.setAllowForLanding(allowForLanding);
		aEventType.setAllowForTakeOff(allowForTakeoff);
		retEventType.add(aEventType);
		return retEventType;
	}

	public boolean isAllowForTakeoff() {
		return allowForTakeoff;
	}

	public void setAllowForTakeoff(boolean allowForTakeoff) {
		this.allowForTakeoff = allowForTakeoff;
	}

	public boolean isAllowForLanding() {
		return allowForLanding;
	}

	public void setAllowForLanding(boolean allowForLanding) {
		this.allowForLanding = allowForLanding;
	}

	

}
