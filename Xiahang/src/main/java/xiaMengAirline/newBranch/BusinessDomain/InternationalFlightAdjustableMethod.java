package xiaMengAirline.newBranch.BusinessDomain;

import java.util.Date;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.Airport;
import xiaMengAirline.newBranch.BasicObject.Flight;
import xiaMengAirline.newBranch.BasicObject.FlightAdjustableMethod;

public class InternationalFlightAdjustableMethod implements FlightAdjustableMethod {

	@Override
	public boolean cancelFlight(Flight aFlight) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addNewEmptyFlight(Flight aFlight) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean moveToNewAircraft(Flight aFlight, Aircraft newAir) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean moveToNewDestination(Flight aFlight, Airport newDestAirport) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean moveToNewDepartureTime(Flight aFlight, Date newDepartureTime) {
		// TODO Auto-generated method stub
		return false;
	}

}
