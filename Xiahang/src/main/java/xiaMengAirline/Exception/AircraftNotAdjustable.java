package xiaMengAirline.Exception;

import xiaMengAirline.beans.Aircraft;

public class AircraftNotAdjustable extends Exception {
	Aircraft air;

	public Aircraft getAir() {
		return air;
	}

	public void setAir(Aircraft air) {
		this.air = air;
	}

	public AircraftNotAdjustable(Aircraft air) {
		super();
		this.air = air;
	}
	

}
