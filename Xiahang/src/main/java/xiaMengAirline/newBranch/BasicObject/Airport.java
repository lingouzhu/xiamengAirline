package xiaMengAirline.newBranch.BasicObject;

import java.util.List;

import xiaMengAirline.newBranch.BusinessDomain.AirportAvailability;

public class Airport {
	private String id;
	private AirportAvailability airportAvaiablityChecker = new AirportAvailability();
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}



	public void setCloseSchedule(List<ResourceUnavailableEvent> closeSchedule) {
		airportAvaiablityChecker.setImpactEvents(closeSchedule);
	}
	
	public Airport() {
		super();
		airportAvaiablityChecker.setAImpactAirport(this);
	}

	public void setMaximumParking (int maxParking) {
		airportAvaiablityChecker.setMaximumParking(maxParking);
	}




}
