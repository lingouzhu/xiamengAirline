package xiaMengAirline.newBranch.BasicObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AirlineAbstractedSolution  {
	private Map<String, Aircraft> schedule = new HashMap<String, Aircraft>(); //key NORMAL_airId or CANCEL_airId
	private Map <String, Flight> allFlights; //key flight id
	//original passenger distributes to list of flights
	//key - NORMAL_passengerId or JOIN_passerngerId
	private Map <String, Airport> allAirports; //key airport Id
	private Map<String, List<Flight>> passengerDistribution = new HashMap<String, List<Flight>> ();
	private List<Flight> dropOutList = new ArrayList<Flight> ();
	
	private int version;
	
	public AirlineAbstractedSolution springOutNewSolution (List<Aircraft> selectedAir) {
		return null;
	}
	
	public void mergeUpdatedSolution (AirlineAbstractedSolution betterSolution) {
		
	}

	public void addOrReplaceAircraft(Aircraft aAircraft) {
		// TODO Auto-generated method stub

	}

	public List<Aircraft> getAircrafts() {
		// TODO Auto-generated method stub
		return null;
	}

	public Aircraft getAircraft(String id, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	public Aircraft getCancelAircraft(Aircraft regularAircraft) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public List<Flight> getDropOutList() {
		return dropOutList;
	}

	public Map<String, Aircraft> getSchedule() {
		return schedule;
	}

	public void setSchedule(Map<String, Aircraft> schedule) {
		this.schedule = schedule;
	}



}
