package xiaMengAirline.beans;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.util.InitData;
import xiaMengAirline.util.Utils;

public class FlightTest {

	@Before
	public void setUp() throws Exception {
		String airType = "1";
		String startPort = "EWR";
		String endPort = "CLE";
		int time = 100;
		
		InitData.fightDurationMap.put(airType + "_" + startPort + "_" + endPort, time);
	}
	
	private Flight createFlight(int flightId, String srcPort, String destPort) {
		Flight flight = new Flight();
		flight.setSchdNo(flightId);
		AirPort aAirport = new AirPort();
		AirPort bAirport = new AirPort();
		aAirport.setId(srcPort);
		bAirport.setId(destPort);

		flight.setSourceAirPort(aAirport);
		flight.setDesintationAirport(bAirport);

		return flight;
	}

	@Test
	public void testCalcuateNextArrivalTime() throws ParseException {
		Aircraft air1 = new Aircraft();
		List<Flight> flightChain = new ArrayList<Flight>();
		flightChain.add(createFlight(101, "ORF", "EWR"));
		flightChain.add(createFlight(102, "EWR", "STL"));
		flightChain.add(createFlight(103, "STL", "CLE"));
		Flight f104 = createFlight(104, "CLE", "BDL");
		f104.setArrivalTime(Utils.stringFormatToTime2("26/05/2017 00:00:00"));
		f104.setDepartureTime(Utils.stringFormatToTime2("26/05/2017 05:30:00"));
		flightChain.add(f104);
		Flight f105 = createFlight(105, "BDL", "CLE");
		f105.setArrivalTime(Utils.stringFormatToTime2("26/05/2017 09:10:00"));
		f105.setDepartureTime(Utils.stringFormatToTime2("26/05/2017 15:30:00"));
		flightChain.add(f105);
		air1.setFlightChain(flightChain);
		air1.setId("1");
		air1.setType("1");
		for (Flight aFlight : flightChain) {
			aFlight.setAssignedAir(air1);
			aFlight.setPlannedFlight(aFlight);
			aFlight.setPlannedAir(air1);
		}
		

		
		
		
	}

}
