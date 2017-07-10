package xiaMengAirline.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AirPort {
	private String id;
	private List<AirPortClose> closeSchedule;
	private List<AirPortClose> closeNormalSchedule;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public boolean equal(AirPort anotherAirport) {
			return id.equals(anotherAirport.getId()); 
	}
	
//	static public HashMap<Integer, List<Integer>> getOverlappedAirports (List<AirPort> airPortList1, List<AirPort> airPortList2) {
//		HashMap<Integer, List<Integer>> retOverlappedAirPortList = new HashMap<Integer, List<Integer>> ();
//		for (AirPort firstAirPort:airPortList1) {
//			ArrayList<Integer> matchList = new ArrayList<Integer> ();
//			for (AirPort secondAirPort:airPortList2) {
//				if (firstAirPort.equal(secondAirPort)) {
//					matchList.add(airPortList2.indexOf(secondAirPort));
//				}
//			}
//			if (!matchList.isEmpty()) 
//				retOverlappedAirPortList.put(airPortList1.indexOf(firstAirPort), matchList);
//		}
//		return (retOverlappedAirPortList);
//	}
	


	
	public FlightTime requestAirport (FlightTime requestTime ) {
		return requestTime;
	}



	public List<AirPortClose> getCloseSchedule() {
		return closeSchedule;
	}

	public void setCloseSchedule(List<AirPortClose> closeSchedule) {
		this.closeSchedule = closeSchedule;
	}
}
