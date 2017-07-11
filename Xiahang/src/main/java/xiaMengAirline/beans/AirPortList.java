package xiaMengAirline.beans;

import java.util.HashMap;
import java.util.Map;

public class AirPortList {
	/** airport list */
	private Map<String, AirPort> airPortList = new HashMap<String, AirPort> ();

	public Map<String, AirPort> getAirPortList() {
		return airPortList;
	}

	public void setAirPortList(Map<String, AirPort> airPortList) {
		this.airPortList = airPortList;
	}
	
	public AirPort getAirport (String airPortId) {
		if (airPortList.containsKey(airPortId)) {
			return (airPortList.get(airPortId));
		} else {
			AirPort airPort = new AirPort ();
			airPort.setId(airPortId);
			airPortList.put(airPortId, airPort);
			return airPort;
		}
	}
}
