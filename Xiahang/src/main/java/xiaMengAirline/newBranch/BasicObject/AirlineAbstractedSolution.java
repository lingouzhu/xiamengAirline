package xiaMengAirline.newBranch.BasicObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirlineAbstractedSolution implements AirlineSolution {
	private Map<String, Aircraft> schedule = new HashMap<String, Aircraft>(); //key NORMAL_airId or CANCEL_airId
	private Map <String, Flight> allFlights; //key flight id
	//original passenger distributes to list of flights
	//key - NORMAL_passengerId or JOIN_passerngerId
	private Map<String, List<Flight>> passengerDistribution = new HashMap<String, List<Flight>> (); 

	@Override
	public void addOrReplaceAircraft(Aircraft aAircraft) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Aircraft> getAircrafts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Aircraft getAircraft(String id, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Aircraft getCancelAircraft(Aircraft regularAircraft) {
		// TODO Auto-generated method stub
		return null;
	}



}
