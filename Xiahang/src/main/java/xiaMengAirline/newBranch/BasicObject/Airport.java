package xiaMengAirline.newBranch.BasicObject;

import java.util.List;

import xiaMengAirline.newBranch.BusinessDomain.ResourceAvailability;
import xiaMengAirline.newBranch.BusinessDomain.ResourceUnavailableEvent;
import xiaMengAirline.newBranch.BusinessDomain.ResourceUnavailableEventType;

public class Airport implements ResourceAvailability {
	private String id;
	private List<ResourceUnavailableEvent> events;

	@Override
	public void setImpactEvents(List<ResourceUnavailableEvent> events) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ResourceUnavailableEvent> getImpactEvents() {
		return events;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
