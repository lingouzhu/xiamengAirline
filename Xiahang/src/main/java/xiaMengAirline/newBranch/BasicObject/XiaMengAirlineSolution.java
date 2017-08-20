package xiaMengAirline.newBranch.BasicObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import xiaMengAirline.newBranch.BusinessDomain.XiaMengAirlineSolutionCost;

public class XiaMengAirlineSolution  implements Cloneable {
	private static final Logger logger = Logger.getLogger(XiaMengAirlineSolution.class);
	private Map<String, Aircraft> normalSchedule = new HashMap<String, Aircraft>(); //key airId
	private Map<String, Aircraft> cancelledSchedule = new HashMap<String, Aircraft>(); //key airId
	//original passenger distributes to list of flights
	//key - NORMAL_passengerId or JOIN_passerngerId
	//not yet decided
	//private Map<String, List<Flight>> passengerDistribution = new HashMap<String, List<Flight>> ();
	private Map <String, Airport> allAirports; //key airport Id
	private List<Flight> dropOutList = new ArrayList<Flight> ();
	private XiaMengAirlineSolution aFeasibleSolution = null;
	private XiaMengAirlineSolutionCost aCost = null;
	
	//rule of versions a.b.c.d.e
	//a is the main branch version
	//b is the branched version from a
	//c is the branched version from a.b
	//d is the branched version from a.b.c
	//e is the branched version from a.b.c.d
	//etc.
	private String version;
	
	public XiaMengAirlineSolution clone() throws CloneNotSupportedException {
		XiaMengAirlineSolution aNewSolution = (XiaMengAirlineSolution) super.clone();
				
		HashMap<String, Aircraft> newSchedule = new HashMap<String, Aircraft>();
		for (String aAir : normalSchedule.keySet()) {
			newSchedule.put(aAir, normalSchedule.get(aAir).clone());
		}
		aNewSolution.setNormalSchedule(newSchedule);
		
		HashMap<String, Aircraft> newCacnelSchedule = new HashMap<String, Aircraft>();
		for (String aAir : cancelledSchedule.keySet()) {
			newCacnelSchedule.put(aAir, cancelledSchedule.get(aAir).clone());
		}
		aNewSolution.setCancelledSchedule(newCacnelSchedule);
		
		List<Flight> newDropOutList = new ArrayList<Flight> ();
		for (Flight aFlight:dropOutList) {
			newDropOutList.add(aFlight.clone());
		}
		aNewSolution.setDropOutList(newDropOutList);
		
		Map <String, Airport> newAirports = new HashMap<String, Airport>();
		for (String aAirport : newAirports.keySet()) {
			newAirports.put(aAirport, newAirports.get(aAirport).clone());
		}
		aNewSolution.setAllAirports(newAirports);
		aNewSolution.increaseMainVersion();
		
		aNewSolution.setaCost(aCost.clone());
		
		return aNewSolution;
	}
	
	public XiaMengAirlineSolution springOutNewSolution (List<String> selectedAir) throws CloneNotSupportedException {
		XiaMengAirlineSolution aNewSolution = (XiaMengAirlineSolution) super.clone();
		HashMap<String, Aircraft> newSchedule = new HashMap<String, Aircraft>();
		HashMap<String, Aircraft> newCacnelSchedule = new HashMap<String, Aircraft>();
		List<Flight> newDropOutList = new ArrayList<Flight> ();
		Map <String, Airport> newAirports = new HashMap<String, Airport>();
		aNewSolution.setVersion(version+".0");
		
		for (String aAir : selectedAir) {
			if (normalSchedule.containsKey(aAir)) {
				newSchedule.put(aAir, normalSchedule.get(aAir).clone());
			} else if (cancelledSchedule.containsKey(aAir)) {
				cancelledSchedule.put(aAir, cancelledSchedule.get(aAir).clone());
			} else {
				logger.warn("Requst to branch unknow aircraft: " + aAir + " from solution version " + version);
			}
		}
		aNewSolution.setNormalSchedule(newSchedule);
		aNewSolution.setCancelledSchedule(newCacnelSchedule);
		aNewSolution.setDropOutList(newDropOutList); //dropout list is not recyclable, and will not move out?
		
		//check airports
		List<Aircraft> normalAir = new ArrayList<Aircraft> (newSchedule.values());
		String airPortId;
		for (Aircraft aAir:normalAir) {
			for (Flight aFlight:aAir.getFlightChain()) {
				airPortId = aFlight.getSourceAirPort().getId();
				if (!newAirports.containsKey(airPortId)) {
					newAirports.put(airPortId, getAirport(airPortId).clone());
				}
				airPortId = aFlight.getDesintationAirport().getId();
				if (!newAirports.containsKey(airPortId)) {
					newAirports.put(airPortId, getAirport(airPortId).clone());
				}
			}
		}
		
		List<Aircraft> cancelledAir = new ArrayList<Aircraft> (newCacnelSchedule.values());
		for (Aircraft aAir:cancelledAir) {
			for (Flight aFlight:aAir.getFlightChain()) {
				airPortId = aFlight.getSourceAirPort().getId();
				if (!newAirports.containsKey(airPortId)) {
					newAirports.put(airPortId, getAirport(airPortId).clone());
				}
				airPortId = aFlight.getDesintationAirport().getId();
				if (!newAirports.containsKey(airPortId)) {
					newAirports.put(airPortId, getAirport(airPortId).clone());
				}
			}
		}
		aNewSolution.setAllAirports(newAirports);
		return aNewSolution;
	
	}
	
	public void increaseMainVersion () {
		String[] versionList = version.split(".");
		int mainVersion = Integer.parseInt(versionList[versionList.length -1]);
		if (versionList.length == 1 ) {
			setVersion(String.valueOf(mainVersion++));
		} else {
			String baseVersion = version.substring(0, version.lastIndexOf("."));
			baseVersion += ".";
			baseVersion += mainVersion++;
			setVersion( baseVersion);
		}
	}
	
	//Attention! aircrafts must be aligned with baseline, unknown aircrafts will be ignored.
	public boolean mergeUpdatedSolution (XiaMengAirlineSolution betterSolution) {
		//merge allowed only when same baseline..
		//if a.b.c merged back, parent must on a.b
		if (betterSolution.getVersion().indexOf(".") == -1)
			return false;
		String baseVersion = betterSolution.getVersion().substring(0,betterSolution.getVersion().lastIndexOf("."));
		if (!baseVersion.equals(version))
			return false;
		increaseMainVersion();
				
		//update normal
		List<Aircraft> normalAirs =  new ArrayList<Aircraft> (betterSolution.getNormalSchedule().values());
		for (Aircraft aAir:normalAirs) {
			if (normalSchedule.containsKey(aAir.getId())) {
				normalSchedule.put(aAir.getId(), aAir);
			} else {
				if (cancelledSchedule.containsKey(aAir.getId())) {
					normalSchedule.put(aAir.getId(), aAir);
					cancelledSchedule.remove(aAir.getId());
				} else 
					logger.warn("New aircraft will be ignored " + aAir.getId() + " from solution " + betterSolution.getVersion());
			}
		}
		
		//update cancel
		List<Aircraft> cancelAirs =  new ArrayList<Aircraft> (betterSolution.getCancelledSchedule().values());
		for (Aircraft aAir:cancelAirs) {
			if (cancelledSchedule.containsKey(aAir.getId())) {
				cancelledSchedule.put(aAir.getId(), aAir);
			} else {
				if (normalSchedule.containsKey(aAir.getId())) {
					cancelledSchedule.put(aAir.getId(), aAir);
					normalSchedule.remove(aAir.getId());
				} else
					logger.warn("New cancelled aircraft will be ignored " + aAir.getId() + " from solution " + betterSolution.getVersion());
			}
		}
		
		
		//merge airport list
		List<Airport> airports = new ArrayList<Airport> (betterSolution.getAllAirports().values());
		for (Airport aAirport:airports) {
			if (allAirports.containsKey(aAirport.getId())) {
				allAirports.put(aAirport.getId(), aAirport);
			} else {
				logger.warn("new airport identified: " + aAirport.getId() + " merged from solution version " + betterSolution.getVersion());		
			}
		}
		
		//merge dropout list
		dropOutList.addAll(betterSolution.getDropOutList());
		
		return true;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
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

	public XiaMengAirlineSolution getaFeasibleSolution() {
		return aFeasibleSolution;
	}

	public void setaFeasibleSolution(XiaMengAirlineSolution aFeasibleSolution) {
		this.aFeasibleSolution = aFeasibleSolution;
	}

	public XiaMengAirlineSolutionCost getaCost() {
		return aCost;
	}

	public void setaCost(XiaMengAirlineSolutionCost aCost) {
		this.aCost = aCost;
	}
	
	
	public void refreshCost(boolean output) {
		aCost.refreshCost(this, output);
	}



}
