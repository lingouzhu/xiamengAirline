package xiaMengAirline.newBranch.BusinessDomain;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.PairedTime;

public interface ResourceAvailability {
	
	public void setImpactEvents (List<ResourceUnavailableEvent> events);
	public List<ResourceUnavailableEvent> getImpactEvents();
	public List<ResourceUnavailableEventType> estimateImpact (PairedTime requestedTime);
	public PairedTime estimateNextAvailable (PairedTime requestedTime);
	public PairedTime estimatePreviousAvailable (PairedTime requestedTime);
	public int setResoruceCapability (int maxAllowed);

}
