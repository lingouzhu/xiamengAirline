package xiaMengAirline.searchEngine;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.Exception.AirportNotAvailableBackup;
import xiaMengAirline.Exception.FlightDurationNotFoundBackup;
import xiaMengAirline.Exception.SolutionNotValidBackup;
import xiaMengAirline.beans.AirPortBackup;
import xiaMengAirline.beans.AircraftBackup;
import xiaMengAirline.beans.FlightBackup;
import xiaMengAirline.beans.XiaMengAirlineSolutionBackup;
import xiaMengAirline.util.InitDataBackup;
import xiaMengAirline.util.UtilsBackup;

public class LocalSearchTest {

	private FlightBackup createFlight(int flightId, String srcPort, String destPort) {
		FlightBackup flight = new FlightBackup();
		flight.setSchdNo(flightId);
		AirPortBackup aAirport = InitDataBackup.airportList.getAirport(srcPort);
		AirPortBackup bAirport = InitDataBackup.airportList.getAirport(destPort);
		

		flight.setSourceAirPort(aAirport);
		flight.setDesintationAirport(bAirport);

		return flight;
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testConstructNewSolution() throws CloneNotSupportedException, ParseException, FlightDurationNotFoundBackup, AirportNotAvailableBackup, SolutionNotValidBackup {
		AircraftBackup air1 = new AircraftBackup();
		List<FlightBackup> flightChain = new ArrayList<FlightBackup>();
		flightChain.add(createFlight(101, "ORF", "EWR"));
		flightChain.add(createFlight(102, "EWR", "STL"));
		flightChain.add(createFlight(103, "STL", "CLE"));
		flightChain.add(createFlight(104, "CLE", "BDL"));
		flightChain.add(createFlight(105, "BDL", "CLE"));
		air1.setFlightChain(flightChain);
		air1.setId("1");
		air1.setType("1");
		for (FlightBackup aFlight:flightChain) {
			aFlight.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
			aFlight.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
			aFlight.setImpCoe(new BigDecimal(1.5));
			aFlight.setAssignedAir(air1);
			aFlight.setPlannedFlight(aFlight);
			aFlight.setPlannedAir(air1);
		}

		AircraftBackup air2 = new AircraftBackup();
		List<FlightBackup> flightChain2 = new ArrayList<FlightBackup>();
		flightChain2.add(createFlight(201, "CLE", "ATL"));
		flightChain2.add(createFlight(202, "ATL", "EWR"));
		flightChain2.add(createFlight(203, "EWR", "BWI"));
		flightChain2.add(createFlight(204, "BWI", "CLE"));
		flightChain2.add(createFlight(205, "CLE", "MDW"));
		air2.setFlightChain(flightChain2);
		air2.setId("2");
		air2.setType("2");
		for (FlightBackup aFlight:flightChain2) {
			aFlight.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
			aFlight.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
			aFlight.setImpCoe(new BigDecimal(1.5));
			aFlight.setAssignedAir(air2);
			aFlight.setPlannedFlight(aFlight);
			aFlight.setPlannedAir(air2);
		}
		
		LocalSearchBackup searchEngine = new LocalSearchBackup();
		
		XiaMengAirlineSolutionBackup aSolution = new XiaMengAirlineSolutionBackup();
		aSolution.replaceOrAddNewAircraft(air1);
		aSolution.replaceOrAddNewAircraft(air2);
		
		searchEngine.constructNewSolution(aSolution);
	}

}
