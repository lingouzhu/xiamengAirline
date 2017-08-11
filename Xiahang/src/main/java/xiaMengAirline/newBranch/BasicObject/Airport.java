package xiaMengAirline.newBranch.BasicObject;

import xiaMengAirline.newBranch.BusinessDomain.ResourceAvailability;

public class Airport {
	private String id;
	private boolean isDomestic;
	private ResourceAvailability  airportAvailability = null;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public boolean isDomestic() {
		return isDomestic;
	}

	public void setDomestic(boolean isDomestic) {
		this.isDomestic = isDomestic;
	}

	public ResourceAvailability getAirportAvailability() {
		return airportAvailability;
	}

	public void setAirportAvailability(ResourceAvailability airportAvailability) {
		this.airportAvailability = airportAvailability;
	}



}
