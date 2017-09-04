package xiaMengAirline.Exception;

import xiaMengAirline.beans.Aircraft;

public class AircraftNotAdjustable  extends Exception implements java.io.Serializable {
	private static final long serialVersionUID = 591757790964775407L;
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
