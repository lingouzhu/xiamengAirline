package xiaMengAirline.newBranch.BasicObject;

import java.util.List;

public interface AirlineSolution {
	public void addOrReplaceAircraft (Aircraft aAircraft);
	public List<Aircraft> getAircrafts ();
	public Aircraft getAircraft (String id, String type);
	public Aircraft getCancelAircraft (Aircraft regularAircraft);

}
