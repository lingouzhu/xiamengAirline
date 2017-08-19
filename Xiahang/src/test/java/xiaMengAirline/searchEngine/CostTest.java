package xiaMengAirline.searchEngine;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.backup.Exception.SolutionNotValidBackup;
import xiaMengAirline.backup.beans.AirPortBackup;
import xiaMengAirline.backup.beans.AircraftBackup;
import xiaMengAirline.backup.beans.FlightBackup;
import xiaMengAirline.backup.beans.XiaMengAirlineSolutionBackup;
import xiaMengAirline.backup.searchEngine.LocalSearchBackup;
import xiaMengAirline.backup.utils.InitDataBackup;
import xiaMengAirline.backup.utils.UtilsBackup;

public class CostTest {

	
	AircraftBackup air1 = new AircraftBackup();
	
	AircraftBackup air2 = new AircraftBackup();
	
	private FlightBackup createFlight(int flightId, String srcPort, String destPort, String planneddestPort,
			Date depTime, Date plannedDepTime, String plannedAirType, BigDecimal impCoe, int schdNo, Date scheDate) {
		FlightBackup flight = new FlightBackup();
		try {
		
			flight.setFlightId(flightId);
			AirPortBackup aAirport = InitDataBackup.airportList.getAirport(srcPort);
			AirPortBackup bAirport = InitDataBackup.airportList.getAirport(destPort);
			AirPortBackup pAirport = InitDataBackup.airportList.getAirport(planneddestPort);
			flight.setSourceAirPort(aAirport);
			flight.setDesintationAirport(bAirport);
			
			flight.setDepartureTime(depTime);
			flight.setArrivalTime(UtilsBackup.addMinutes(depTime, 60));
		
			flight.setPlannedFlight(flight.clone());
			
			flight.getPlannedFlight().setDesintationAirport(pAirport);
			flight.getPlannedFlight().setDepartureTime(plannedDepTime);
			
			AircraftBackup plannedAir = new AircraftBackup();
			plannedAir.setType(plannedAirType);
			plannedAir.setId("10");
			flight.setPlannedAir(plannedAir);
			
			flight.setImpCoe(impCoe);
			
			flight.setSchdDate(scheDate);
			flight.setSchdNo(schdNo);
			
			
//			System.out.println(flight.getDesintationAirport().getId());
//			System.out.println(flight.getPlannedFlight().getDesintationAirport().getId());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return flight;
	}
	
	@Before
	public void setUp() throws Exception {
		
		InitDataBackup.plannedMaxFligthId = 1000;
		FlightBackup fligt1 = createFlight(102, "002", "003", "003", UtilsBackup.timeStr2date("2017-01-01 13:00:00"), 
				UtilsBackup.timeStr2date("2017-01-01 13:00:00"), "1" , new BigDecimal("1"), 202, UtilsBackup.dateStr2date("2017-01-01"));
		
		FlightBackup fligt2 = createFlight(103, "003", "004", "004", UtilsBackup.timeStr2date("2017-01-01 18:00:00"), 
				UtilsBackup.timeStr2date("2017-01-01 18:00:00"), "1" , new BigDecimal("1"), 202, UtilsBackup.dateStr2date("2017-01-01"));
		
		InitDataBackup.jointFlightMap.put(fligt1.getFlightId(), fligt2);
		InitDataBackup.jointFlightMap.put(fligt2.getFlightId(), null);
		
		// delay 1 hour 100
		List<FlightBackup> flightChain = new ArrayList<FlightBackup>();
		flightChain.add(createFlight(101, "001", "002", "002", UtilsBackup.timeStr2date("2017-01-01 10:00:00"), 
				UtilsBackup.timeStr2date("2017-01-01 9:00:00"), "1" , new BigDecimal("1"), 201, UtilsBackup.dateStr2date("2017-01-01")));
		// joint 1500
		flightChain.add(createFlight(102, "002", "004", "003", UtilsBackup.timeStr2date("2017-01-01 10:00:00"), 
				UtilsBackup.timeStr2date("2017-01-01 10:00:00"), "1" , new BigDecimal("1"), 202, UtilsBackup.dateStr2date("2017-01-01")));
		

		// cancel
//		flightChain.add(createFlight(102, "002", "003", "003", Utils.timeStr2date("2017-01-01 13:00:00"), 
//				Utils.timeStr2date("2017-01-01 13:00:00"), "1" , new BigDecimal("1"), 202, Utils.dateStr2date("2017-01-01")));
//		// cancel
//		flightChain.add(createFlight(103, "003", "004", "004", Utils.timeStr2date("2017-01-01 18:00:00"), 
//				Utils.timeStr2date("2017-01-01 18:00:00"), "1" , new BigDecimal("1"), 202, Utils.dateStr2date("2017-01-01")));
		// change air type 1000
		flightChain.add(createFlight(104, "004", "005", "005", UtilsBackup.timeStr2date("2017-01-01 10:00:00"), 
				UtilsBackup.timeStr2date("2017-01-02 10:00:00"), "2" , new BigDecimal("1"), 204, UtilsBackup.dateStr2date("2017-01-02")));
		// move up 150
		flightChain.add(createFlight(105, "005", "006", "006", UtilsBackup.timeStr2date("2017-01-01 14:00:00"), 
				UtilsBackup.timeStr2date("2017-01-02 15:00:00"), "1" , new BigDecimal("1"), 205, UtilsBackup.dateStr2date("2017-01-02")));
		
//		flightChain.add(createFlight(106, "006", "007", "007", Utils.timeStr2date("2017-01-01 19:00:00"), 
//				Utils.timeStr2date("2017-01-02 19:00:00"), "1" , new BigDecimal("1"), 206, Utils.dateStr2date("2017-01-02")));
//		
//		// joint cost
//		flightChain.add(createFlight(107, "007", "008", "008", Utils.timeStr2date("2017-01-01 10:00:00"), 
//				Utils.timeStr2date("2017-01-03 10:00:00"), "1" , new BigDecimal("1"), 207, Utils.dateStr2date("2017-01-03")));
//		// cancel
//		flightChain.add(createFlight(108, "008", "009", "009", Utils.timeStr2date("2017-01-01 10:00:00"), 
//				Utils.timeStr2date("2017-01-03 10:00:00"), "1" , new BigDecimal("1"), 207, Utils.dateStr2date("2017-01-03")));
//		
//		flightChain.add(createFlight(109, "009", "010", "010", Utils.timeStr2date("2017-01-01 10:00:00"), 
//				Utils.timeStr2date("2017-01-03 10:00:00"), "1" , new BigDecimal("1"), 208, Utils.dateStr2date("2017-01-03")));
		
		air1.setFlightChain(flightChain);
		air1.setId("1");
		air1.setType("1");
		
		List<FlightBackup> flightChain2 = new ArrayList<FlightBackup>();
		// 1000
		flightChain2.add(createFlight(201, "001", "002", "002", UtilsBackup.timeStr2date("2017-01-01 10:00:00"), 
				UtilsBackup.timeStr2date("2017-01-01 9:00:00"), "1" , new BigDecimal("1"), 201, UtilsBackup.dateStr2date("2017-01-01")));
		
		air2.setFlightChain(flightChain2);
		air2.setCancel(true);
		air1.setId("1");
		air2.setType("1");
	}

	@Test
	public void testConstructNewSolution() throws CloneNotSupportedException, ParseException, SolutionNotValidBackup {
		
		XiaMengAirlineSolutionBackup aSolution = new XiaMengAirlineSolutionBackup();
		aSolution.replaceOrAddNewAircraft(air1);
		aSolution.replaceOrAddNewAircraft(air2);
		// 3750
		aSolution.refreshCost(true);
		
		
		aSolution.generateOutput("2");
		
		LocalSearchBackup searchEngine = new LocalSearchBackup();
		
		searchEngine.constructNewSolution(aSolution);
		
		System.out.println(aSolution.getCost().toString());
		
	}


}
