package xiaMengAirline.newBranch.BasicObject;

import xiaMengAirline.newBranch.BusinessDomain.AirPortAvailability;

public class Airport {
	private String id;
	private AirPortAvailability  myAvailability = null;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public AirPortAvailability getMyAvailability() {
		return myAvailability;
	}

	public void setMyAvailability(AirPortAvailability myAvailability) {
		this.myAvailability = myAvailability;
	}



}
