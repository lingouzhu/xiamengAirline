package xiaMengAirline.newBranch.BasicObject;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import xiaMengAirline.newBranch.BusinessDomain.ResourceAvailability;



public class Aircraft {
	private static final Logger logger = Logger.getLogger(Aircraft.class);
	private String id;
	private String type;
	private List<Flight> flightChain = new ArrayList<Flight>();
	private boolean isCancel = false;
	private ResourceAvailability seatsAvailability;
	
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
	public boolean isCancel() {
		return isCancel;
	}
	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}
	public static Logger getLogger() {
		return logger;
	}
	public ResourceAvailability getSeatsAvailability() {
		return seatsAvailability;
	}
	public void setSeatsAvailability(ResourceAvailability seatsAvailability) {
		this.seatsAvailability = seatsAvailability;
	}
}
