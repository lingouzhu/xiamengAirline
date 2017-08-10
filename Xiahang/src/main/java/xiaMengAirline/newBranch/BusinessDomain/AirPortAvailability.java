package xiaMengAirline.newBranch.BusinessDomain;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.PairedTime;

public class AirPortAvailability implements ResourceAvailability {

	@Override
	public void setImpactEvents(List<ResourceUnavailableEvent> events) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ResourceUnavailableEvent> getImpactEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResourceUnavailableEventType> estimateImpact(PairedTime requestedTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PairedTime estimateNextAvailable(PairedTime requestedTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PairedTime estimatePreviousAvailable(PairedTime requestedTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int setResoruceCapability(int maxAllowed) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean applyForResource(PairedTime requestedTime) {
		// TODO Auto-generated method stub
		return false;
	}

}
