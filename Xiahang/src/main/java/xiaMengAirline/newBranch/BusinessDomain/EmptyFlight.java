package xiaMengAirline.newBranch.BusinessDomain;

import java.util.Date;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.Airport;
import xiaMengAirline.newBranch.BasicObject.Flight;

public class EmptyFlight extends Flight {

	@Override
	public boolean cancelFlight() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addNewEmptyFlight() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean moveToNewAircraft(Aircraft newAir) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean moveToNewDestination(Airport newDestAirport) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean moveToNewDepartureTime(Date newDepartureTime) {
		// TODO Auto-generated method stub
		return false;
	}

}
