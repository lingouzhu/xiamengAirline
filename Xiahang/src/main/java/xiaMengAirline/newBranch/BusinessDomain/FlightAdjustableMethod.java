package xiaMengAirline.newBranch.BusinessDomain;

import java.util.Date;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.Airport;
import xiaMengAirline.newBranch.BasicObject.Flight;

public interface FlightAdjustableMethod {
	public boolean cancelFlight (Flight aFlight);
	public boolean addNewEmptyFlight (Flight aFlight);
	public boolean moveToNewAircraft (Flight aFlight, Aircraft newAir);
	public boolean moveToNewDestination (Flight aFlight, Airport newDestAirport);
	public boolean moveToNewDepartureTime (Flight aFlight, Date newDepartureTime);

}
