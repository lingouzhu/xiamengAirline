package xiaMengAirline.newBranch.BasicObject;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
public class FlightTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testClone() throws CloneNotSupportedException {
		Aircraft aAir = new Aircraft();
		aAir.setId("111");
		Flight aFlight = new Flight();
		aFlight.setFlightId(100);
		List<Passenger> passList = new ArrayList<Passenger>();
		for (int i=0;i<100;i++) {
			Passenger aPass = new Passenger(aFlight, aAir);
			passList.add(aPass);
		}
		aFlight.setPassengers(passList);
		
		Flight bFlight = new Flight();
		bFlight.setFlightId(101);
		passList = new ArrayList<Passenger>();
		for (int i=0;i<200;i++) {
			Passenger aPass = new Passenger(bFlight, aAir);
			passList.add(aPass);
		}
		bFlight.setPassengers(passList);
		
		Flight cFlight = new Flight();
		cFlight.setFlightId(102);
		passList = new ArrayList<Passenger>();
		for (int i=0;i<300;i++) {
			Passenger aPass = new Passenger(cFlight, aAir);
			passList.add(aPass);
		}
		cFlight.setPassengers(passList);
		
		bFlight.setJoined1stlight(bFlight);
		bFlight.setJoined2ndFlight(cFlight);
		cFlight.setJoined1stlight(bFlight);
		cFlight.setJoined2ndFlight(cFlight);
		
		Flight bbFlight = bFlight.clone();
		assertEquals(bbFlight, bbFlight.getJoined1stlight());
		assertEquals(cFlight, bbFlight.getJoined2ndFlight());
		assertEquals(bbFlight, cFlight.getJoined1stlight());
		assertEquals(cFlight, cFlight.getJoined2ndFlight());
		
		Flight ccFlight = cFlight.clone();
		assertEquals(bbFlight, bbFlight.getJoined1stlight());
		assertEquals(ccFlight, bbFlight.getJoined2ndFlight());
		assertEquals(bbFlight, ccFlight.getJoined1stlight());
		assertEquals(ccFlight, ccFlight.getJoined2ndFlight());
		
		assertEquals(bFlight.getPassengers().size(),bbFlight.getPassengers().size());
		assertEquals(bbFlight, bbFlight.getPassengers().get(0).getAssignedFlight());
		assertEquals(bFlight, bFlight.getPassengers().get(0).getAssignedFlight());
		
	}

}

