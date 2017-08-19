package xiaMengAirline.newBranch.BusinessDomain;

import java.util.ArrayList;
import java.util.List;

import xiaMengAirline.newBranch.BasicObject.PairedTime;
import xiaMengAirline.newBranch.BasicObject.ResourceUnavailableEvent;
import xiaMengAirline.newBranch.BasicObject.ResourceUnavailableEventType;
import xiaMengAirline.newBranch.BasicObject.ResourceUnavailableEventType.AllowType;

public class AirPortAvailability implements Cloneable {
	public enum RequestType {
	    TAKEOFF,
	    LANDING,
	    PARKING;
	}
	private List<ResourceUnavailableEvent> eventlist = new ArrayList<ResourceUnavailableEvent> ();
	private int currentAllocated = 0;



	public void addImpactEvent(ResourceUnavailableEvent event) {
		eventlist.add(event);
		
	}
	
	public List<ResourceUnavailableEvent> getCurrentEvents(PairedTime requestedTime) {
		List<ResourceUnavailableEvent> impactedEvents = new ArrayList<ResourceUnavailableEvent> ();
		for (ResourceUnavailableEvent aEvent:eventlist) {
			if (requestedTime.getStartTime().after(aEvent.getStartTime()) 
					&& requestedTime.getStartTime().before(aEvent.getEndTime())
				|| requestedTime.getEndTime().after(aEvent.getStartTime()) 
					&& requestedTime.getEndTime().before(aEvent.getEndTime())) {
				impactedEvents.add(aEvent);
			}
		}
		return impactedEvents;
	}

	public List<ResourceUnavailableEvent> estimateCurrentAvailable(PairedTime requestedTime,RequestType requestType ) {
		List<ResourceUnavailableEvent> impactedEvents = new ArrayList<ResourceUnavailableEvent> ();
		for (ResourceUnavailableEvent aEvent:eventlist) {
			if (requestedTime.getStartTime().after(aEvent.getStartTime()) 
					&& requestedTime.getStartTime().before(aEvent.getEndTime())
				|| requestedTime.getEndTime().after(aEvent.getStartTime()) 
					&& requestedTime.getEndTime().before(aEvent.getEndTime())) {
				//check requestType
				if (requestType.equals(RequestType.LANDING)) {
					if (aEvent.getUnavailableEventType().getAllowForLanding() == AllowType.NOT_ALLOWED)
						impactedEvents.add(aEvent);
					else if (aEvent.getUnavailableEventType().getAllowForLanding() == AllowType.CONDITION) {
						if (currentAllocated >= aEvent.getUnavailableEventType().getCapability())
							impactedEvents.add(aEvent);
					}					
				} else if (requestType.equals(RequestType.PARKING)) {
					if (aEvent.getUnavailableEventType().getAllowForParking() == AllowType.NOT_ALLOWED)
						impactedEvents.add(aEvent);
					else if (aEvent.getUnavailableEventType().getAllowForParking() == AllowType.CONDITION) {
						if (currentAllocated >= aEvent.getUnavailableEventType().getCapability())
							impactedEvents.add(aEvent);
					}
				} else if (requestType.equals(RequestType.TAKEOFF)) {
					if (aEvent.getUnavailableEventType().getAllowForTakeOff() == AllowType.NOT_ALLOWED)
						impactedEvents.add(aEvent);
					else if (aEvent.getUnavailableEventType().getAllowForTakeOff() == AllowType.CONDITION) {
						if (currentAllocated >= aEvent.getUnavailableEventType().getCapability())
							impactedEvents.add(aEvent);
					}
				}

				
			}
		}
		

		return impactedEvents;
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
		//event list shall be shared global
		return  (AirPortAvailability) super.clone();
	}

	public List<ResourceUnavailableEvent> getEventlist() {
		return eventlist;
	}

	public void setEventlist(List<ResourceUnavailableEvent> eventlist) {
		this.eventlist = eventlist;
	}

	public int getCurrentAllocated() {
		return currentAllocated;
	}

	public void setCurrentAllocated(int currentAllocated) {
		this.currentAllocated = currentAllocated;
	}
	
	





}
