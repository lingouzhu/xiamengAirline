package xiaMengAirline.newBranch.BasicObject;

import java.util.Date;

public interface FlightAdjustableMethod {
	public boolean cancelFlight (Flight aFlight);
	public boolean addNewEmptyFlight (Flight aFlight);
	public boolean moveToNewAircraft (Flight aFlight, Aircraft newAir);
	public boolean moveToNewDestination (Flight aFlight, Airport newDestAirport);
	public boolean moveToNewDepartureTime (Flight aFlight, Date newDepartureTime);

}
