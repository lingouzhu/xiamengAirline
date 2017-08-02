package xiaMengAirline.newBranch.BusinessDomain;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Airport;
import xiaMengAirline.newBranch.BasicObject.PairedTime;
import xiaMengAirline.newBranch.BasicObject.ResourceUnavailableEvent;
import xiaMengAirline.newBranch.BasicObject.UnavailableEventType;

public class AirportAvailability implements ResourceAvailability {
	private int maximumParking = Integer.MAX_VALUE;


	public int getMaximumParking() {
		return maximumParking;
	}

	public void setMaximumParking(int maximumParking) {
		this.maximumParking = maximumParking;
	}

	@Override
	public void setAImpactAirport(Airport aAirPort) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImpactEvents(List<ResourceUnavailableEvent> events) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Airport getAImpactAirport() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResourceUnavailableEvent> getImpactEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UnavailableEventType> estimateImpact(PairedTime requestedTime) {
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





}
