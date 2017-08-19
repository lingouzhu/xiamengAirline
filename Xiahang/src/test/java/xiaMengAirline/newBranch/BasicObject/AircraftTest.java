package xiaMengAirline.newBranch.BasicObject;

import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.newBranch.BusinessDomain.SeatAvailability;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class AircraftTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSetCancel() throws CloneNotSupportedException {
		Aircraft air = new Aircraft();
		air.setCancel(true);
		List<Flight> flightList = new ArrayList<Flight>();
		Flight aFlight = new Flight();
		aFlight.setFlightId(100);
		List<Passenger> passList = new ArrayList<Passenger>();
		for (int i=0;i<100;i++) {
			Passenger aPass = new Passenger(aFlight, air);
			passList.add(aPass);
		}
		aFlight.setPassengers(passList);
		flightList.add(aFlight);
		air.setFlightChain(flightList);
		
		SeatAvailability aSeat = new SeatAvailability();
		aSeat.setSeatCapability(100);
		aSeat.applyForResource(10);
		air.setSeatsAvailability(aSeat);
		Aircraft air2 = air.clone();
		air2.getSeatsAvailability().applyForResource(20);
		
		assertEquals(90, air.getSeatsAvailability().getCurrentAvailable());
		assertEquals(70, air2.getSeatsAvailability().getCurrentAvailable());
		assertEquals(air2, air2.getFlight(0).getPassengers().get(10).getAssignedAir());
		assertEquals(air2, air2.getFlight(0).getAssignedAir());
		
		
		
		
				
	}

}
