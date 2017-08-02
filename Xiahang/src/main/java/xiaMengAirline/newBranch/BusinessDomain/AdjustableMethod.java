package xiaMengAirline.newBranch.BusinessDomain;

import java.util.Date;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.Airport;

public interface AdjustableMethod {
	public boolean cancelFlight ();
	public boolean addNewEmptyFlight ();
	public boolean moveToNewAircraft (Aircraft newAir);
	public boolean moveToNewDestination (Airport newDestAirport);
	public boolean moveToNewDepartureTime (Date newDepartureTime);

}
