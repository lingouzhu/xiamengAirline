package xiaMengAirline.newBranch.BusinessDomain;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.PairedTime;

public interface ResourceAvailability extends Cloneable {
	
	public void addImpactEvent (ResourceUnavailableEvent event);
	public List<ResourceUnavailableEvent> getImpactEvents();
	public List<ResourceUnavailableEventType> getUnavailabilityReasons (PairedTime requestedTime);
	public int estimateCurrentAvailable (PairedTime requestedTime);
	public PairedTime estimateNextAvailable (PairedTime requestedTime);
	public PairedTime estimatePreviousAvailable (PairedTime requestedTime);
	public void setResoruceCapability (int maxAllowed);
	public boolean applyForResource (int numberOfResources, PairedTime requestedTime);
	public  ResourceAvailability clone () throws CloneNotSupportedException;
	

}
