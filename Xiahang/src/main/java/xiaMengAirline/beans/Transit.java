package xiaMengAirline.beans;

public class Transit implements java.io.Serializable {
  	private static final long serialVersionUID = -4555285920666738147L;
	private int flightID1;
	private int flightID2;
	private int transitMins;
	private int transitPersons;
	
	
	public int getFlightID1() {
		return flightID1;
	}
	public void setFlightID1(int flightID1) {
		this.flightID1 = flightID1;
	}
	public int getFlightID2() {
		return flightID2;
	}
	public void setFlightID2(int flightID2) {
		this.flightID2 = flightID2;
	}
	public int getTransitPersons() {
		return transitPersons;
	}
	public void setTransitPersons(int transitPersons) {
		this.transitPersons = transitPersons;
	}
	public int getTransitMins() {
		return transitMins;
	}
	public void setTransitMins(int transitMins) {
		this.transitMins = transitMins;
	}
	
	
	
}