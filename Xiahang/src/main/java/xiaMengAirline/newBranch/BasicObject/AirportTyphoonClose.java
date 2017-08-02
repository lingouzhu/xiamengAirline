package xiaMengAirline.newBranch.BasicObject;

import java.util.ArrayList;
import java.util.List;

public class AirportTyphoonClose extends ResourceUnavailableEvent {
	private boolean allowForTakeoff;
	private boolean allowForLanding;
		

	@Override
	public List<UnavailableEventType> getFixedUnavailableEventType() {
		List<UnavailableEventType> retEventType =  new ArrayList<UnavailableEventType> ();
		UnavailableEventType aEventType = new UnavailableEventType();
		aEventType.setAllowForLanding(allowForLanding);
		aEventType.setAllowForTakeOff(allowForTakeoff);
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
