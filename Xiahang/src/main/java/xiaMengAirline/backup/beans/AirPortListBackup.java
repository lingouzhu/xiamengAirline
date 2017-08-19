package xiaMengAirline.backup.beans;

import java.util.HashMap;
import java.util.Map;

public class AirPortListBackup {
	/** airport list */
	private Map<String, AirPortBackup> airPortList = new HashMap<String, AirPortBackup> ();

	public Map<String, AirPortBackup> getAirPortList() {
		return airPortList;
	}

	public void setAirPortList(Map<String, AirPortBackup> airPortList) {
		this.airPortList = airPortList;
	}
	
	public AirPortBackup getAirport (String airPortId) {
		if (airPortList.containsKey(airPortId)) {
			return (airPortList.get(airPortId));
		} else {
			AirPortBackup airPort = new AirPortBackup ();
			airPort.setId(airPortId);
			airPortList.put(airPortId, airPort);
			return airPort;
		}
	}
}
