package xiaMengAirline.newBranch.BusinessDomain;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.PairedTime;

public class SeatAvailability implements ResourceAvailability {


	@Override
	public List<ResourceUnavailableEvent> getImpactEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResourceUnavailableEventType> getUnavailabilityReasons(PairedTime requestedTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int estimateCurrentAvailable(PairedTime requestedTime) {
		// TODO Auto-generated method stub
		return 0;
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
	public void setResoruceCapability(int maxAllowed) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean applyForResource(int numberOfResources, PairedTime requestedTime) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ResourceAvailability clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (ResourceAvailability) super.clone();
	}

	@Override
	public void addImpactEvent(ResourceUnavailableEvent event) {
		// TODO Auto-generated method stub
		
	}






}
