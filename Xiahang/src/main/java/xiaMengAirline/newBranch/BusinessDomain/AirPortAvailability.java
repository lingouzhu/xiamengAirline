package xiaMengAirline.newBranch.BusinessDomain;

import java.util.ArrayList;
import java.util.List;

import xiaMengAirline.newBranch.BasicObject.PairedTime;
import xiaMengAirline.newBranch.BasicObject.ResourceUnavailableEvent;
import xiaMengAirline.newBranch.BasicObject.ResourceUnavailableEventType;

public class AirPortAvailability {
	private List<ResourceUnavailableEvent> eventlist = new ArrayList<ResourceUnavailableEvent> ();
	private int currentAllocated = 0;



	public List<ResourceUnavailableEvent> getImpactEvents() {
		return eventlist;
	}
	
	public void addImpactEvent(ResourceUnavailableEvent event) {
		eventlist.add(event);
		
	}

	public List<ResourceUnavailableEventType> estimateCurrentAvailable(PairedTime requestedTime) {
		// TODO Auto-generated method stub
		return null;
	}



	public PairedTime estimateNextAvailable(PairedTime requestedTime) {
		// TODO Auto-generated method stub
		return null;
	}


	public PairedTime estimatePreviousAvailable(PairedTime requestedTime) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean applyForResource(int numberOfResources, PairedTime requestedTime) {
		// TODO Auto-generated method stub
		return false;
	}

	public AirPortAvailability clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return  (AirPortAvailability) super.clone();
	}





}
