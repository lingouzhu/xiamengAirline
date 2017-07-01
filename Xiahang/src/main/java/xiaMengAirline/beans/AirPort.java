package xiaMengAirline.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AirPort {
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public boolean equal(AirPort anotherAirport) {
			return id.equals(anotherAirport.getId()); 
	}
	
	static public HashMap<Integer, List<Integer>> getOverlappedAirports (List<AirPort> airPortList1, List<AirPort> airPortList2) {
		HashMap<Integer, List<Integer>> retOverlappedAirPortList = new HashMap<Integer, List<Integer>> ();
		for (AirPort firstAirPort:airPortList1) {
			ArrayList<Integer> matchList = new ArrayList<Integer> ();
			for (AirPort secondAirPort:airPortList2) {
				if (firstAirPort.equal(secondAirPort)) {
					matchList.add(airPortList2.indexOf(secondAirPort));
				}
			}
			if (!matchList.isEmpty()) 
				retOverlappedAirPortList.put(airPortList1.indexOf(firstAirPort), matchList);
		}
		return (retOverlappedAirPortList);
	}
	
	static public HashMap<Integer, List<Integer>> getCircuitAirports (List<AirPort> airPortList) {
		HashMap<Integer, List<Integer>> retCircuitAirPortList = new HashMap<Integer, List<Integer>> ();
		for (int i=0;i < airPortList.size()-1;i++) {
			ArrayList<Integer> matchList = new ArrayList<Integer> ();
			for (int j=i+2; j < airPortList.size()-1;j++) {
				if (airPortList.get(i).equal(airPortList.get(j))) {
					matchList.add(j);
				}
			}
			if (!matchList.isEmpty())
				retCircuitAirPortList.put(i, matchList);
		}
		return (retCircuitAirPortList);
		
	}

}
