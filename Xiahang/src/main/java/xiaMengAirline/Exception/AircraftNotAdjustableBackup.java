package xiaMengAirline.Exception;

import xiaMengAirline.beans.AircraftBackup;

public class AircraftNotAdjustableBackup extends Exception {
	AircraftBackup air;

	public AircraftBackup getAir() {
		return air;
	}

	public void setAir(AircraftBackup air) {
		this.air = air;
	}

	public AircraftNotAdjustableBackup(AircraftBackup air) {
		super();
		this.air = air;
	}
	

}
