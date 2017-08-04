package xiaMengAirline.beans;

public class MatchedFlightBackup {
	private int air1SourceFlight;
	private int air1DestFlight;
	private int air2SourceFlight;
	private int air2DestFlight;
	
	
	public boolean isAir1SingleFlight () {
		if (air1SourceFlight == air1DestFlight)
			return true;
		else
			return false;
	}
	
	public boolean isAir2SingleFlight () {
		if (air2SourceFlight == air2DestFlight)
			return true;
		else
			return false;
	}

	public int getAir1SourceFlight() {
		return air1SourceFlight;
	}

	public void setAir1SourceFlight(int air1SourceFlight) {
		this.air1SourceFlight = air1SourceFlight;
	}

	public int getAir1DestFlight() {
		return air1DestFlight;
	}

	public void setAir1DestFlight(int air1DestFlight) {
		this.air1DestFlight = air1DestFlight;
	}

	public int getAir2SourceFlight() {
		return air2SourceFlight;
	}

	public void setAir2SourceFlight(int air2SourceFlight) {
		this.air2SourceFlight = air2SourceFlight;
	}

	public int getAir2DestFlight() {
		return air2DestFlight;
	}

	public void setAir2DestFlight(int air2DestFlight) {
		this.air2DestFlight = air2DestFlight;
	}


	
	

	

}
