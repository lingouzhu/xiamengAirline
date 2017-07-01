package xiaMengAirline.beans;

import java.util.List;

public class Aircraft {
	private String id;
	private String type;
	private List<Flight> flightChain;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<Flight> getFlightChain() {
		return flightChain;
	}
	public void setFlightChain(List<Flight> flightChain) {
		this.flightChain = flightChain;
	}

	
}
