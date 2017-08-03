package xiaMengAirline.newBranch.LocalOptimize;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Aircraft;

public interface AirlineSolution {
	public void addOrReplaceAircraft (Aircraft aAircraft);
	public List<Aircraft> getAircrafts ();
	public Aircraft getAircraft (String id, String type);
	public Aircraft getCancelAircraft (Aircraft regularAircraft);

}
