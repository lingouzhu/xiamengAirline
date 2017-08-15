package xiaMengAirline.newBranch.BasicObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AirlineAbstractedSolution  {
	private Map<String, Aircraft> normalSchedule = new HashMap<String, Aircraft>(); //key airId
	private Map<String, Aircraft> cancelledSchedule = new HashMap<String, Aircraft>(); //key airId
	//original passenger distributes to list of flights
	//key - NORMAL_passengerId or JOIN_passerngerId
	//not yet decided
	//private Map<String, List<Flight>> passengerDistribution = new HashMap<String, List<Flight>> ();
	private Map <String, Airport> allAirports; //key airport Id
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

	public Aircraft getAircraft(String id, String type, boolean autoGenerate) {
		String aKey = id;
		if (normalSchedule.containsKey(aKey)) {
			return (normalSchedule.get(aKey));
		} else {
			if (autoGenerate) {
				Aircraft aAir = new Aircraft();
				aAir.setId(id);
				aAir.setType(type);
				aAir.setCancel(false);
				normalSchedule.put(aKey, aAir);
				return aAir;
			} else
				return null;

		}
	}
	
	public Aircraft getCancelAircraft(String id, String type, boolean autoGenerate) {
		String aKey = id;
		if (cancelledSchedule.containsKey(aKey)) {
			return (cancelledSchedule.get(aKey));
		} else {
			if (autoGenerate) {
				Aircraft aAir = new Aircraft();
				aAir.setId(id);
				aAir.setType(type);
				aAir.setCancel(true);
				cancelledSchedule.put(aKey, aAir);
				return aAir;
			} else
				return null;

		}
	}

	public Aircraft getCancelAircraft(Aircraft regularAircraft, boolean autoGenerate) {
		return getCancelAircraft(regularAircraft.getId(), regularAircraft.getType(), autoGenerate);
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


	public Airport getAirport (String airPortId) {
		if (allAirports.containsKey(airPortId)) {
			return (allAirports.get(airPortId));
		} else {
			Airport airPort = new Airport ();
			airPort.setId(airPortId);
			allAirports.put(airPortId, airPort);
			return airPort;
		}
	}

	public Map<String, Aircraft> getNormalSchedule() {
		return normalSchedule;
	}

	public void setNormalSchedule(Map<String, Aircraft> normalSchedule) {
		this.normalSchedule = normalSchedule;
	}

	public Map<String, Aircraft> getCancelledSchedule() {
		return cancelledSchedule;
	}

	public void setCancelledSchedule(Map<String, Aircraft> cancelledSchedule) {
		this.cancelledSchedule = cancelledSchedule;
	}

	
	public void addFlight (Flight aFlight) {
		Aircraft myAir = null;
		Aircraft air = aFlight.getAssignedAir();
		if (air.isCancel()) 
			myAir = getCancelAircraft(air, true);
		else
			myAir = getAircraft(air.getId(), air.getType(), true);
		
		myAir.addFlight(aFlight);
		
	}
	

	public Map<String, Airport> getAllAirports() {
		return allAirports;
	}

	public void setAllAirports(Map<String, Airport> allAirports) {
		this.allAirports = allAirports;
	}

	public void setDropOutList(List<Flight> dropOutList) {
		this.dropOutList = dropOutList;
	}



}
