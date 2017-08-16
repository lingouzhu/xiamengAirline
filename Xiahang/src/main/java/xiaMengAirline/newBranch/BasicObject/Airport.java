package xiaMengAirline.newBranch.BasicObject;

import xiaMengAirline.newBranch.BusinessDomain.AirPortAvailability;

public class Airport {
	private String id;
	private boolean isDomestic;
	private AirPortAvailability  airportAvailability = null;

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

	public AirPortAvailability getAirportAvailability() {
		return airportAvailability;
	}

	public void setAirportAvailability(AirPortAvailability airportAvailability) {
		this.airportAvailability = airportAvailability;
	}


}
