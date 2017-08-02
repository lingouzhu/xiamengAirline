package xiaMengAirline.newBranch.BusinessDomain;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Airport;
import xiaMengAirline.newBranch.BasicObject.PairedTime;
import xiaMengAirline.newBranch.BasicObject.ResourceUnavailableEvent;
import xiaMengAirline.newBranch.BasicObject.UnavailableEventType;

public interface ResourceAvailability {
	Airport aImpactAirport = null;
	List<ResourceUnavailableEvent> events = null;
	
	public void setAImpactAirport (Airport aAirPort);
	public void setImpactEvents (List<ResourceUnavailableEvent> events);
	public Airport getAImpactAirport ();
	public List<ResourceUnavailableEvent> getImpactEvents();
	public List<UnavailableEventType> estimateImpact (PairedTime requestedTime);
	public PairedTime estimateNextAvailable (PairedTime requestedTime);
	public PairedTime estimatePreviousAvailable (PairedTime requestedTime);

}
