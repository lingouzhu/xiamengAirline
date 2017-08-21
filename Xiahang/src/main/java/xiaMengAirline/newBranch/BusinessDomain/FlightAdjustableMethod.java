package xiaMengAirline.newBranch.BusinessDomain;

import java.util.Date;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.Airport;
import xiaMengAirline.newBranch.BasicObject.Flight;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;

public interface FlightAdjustableMethod {
	public enum FlightAdjustMethodType {
	    CANCEL,
	    NEW_EMPTY,
	    NEW_AIRCRAFT,;
	} 
	public boolean cancelFlight (XiaMengAirlineSolution context);
	public boolean addNewEmptyFlight (Flight aFlight);
	public boolean moveToNewAircraft (Flight aFlight, Aircraft newAir);
	public boolean moveToNewDestination (Flight aFlight, Airport newDestAirport);
	public boolean moveToNewDepartureTime (Flight aFlight, Date newDepartureTime);

}
