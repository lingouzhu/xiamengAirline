package xiaMengAirline.searchEngine;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.util.InitData;
import xiaMengAirline.util.Utils;

public class IntegrationTest1 {

	Aircraft air1 = new Aircraft();

	Aircraft air2 = new Aircraft();

	private Flight createFlight(int flightId, String srcPort, String destPort, String planneddestPort, Date depTime,
			Date plannedDepTime, String plannedAirType, BigDecimal impCoe, int schdNo, Date scheDate) {
		Flight flight = new Flight();
		try {

			flight.setFlightId(flightId);
			AirPort aAirport = InitData.airportList.getAirport(srcPort);
			AirPort bAirport = InitData.airportList.getAirport(destPort);
			AirPort pAirport = InitData.airportList.getAirport(planneddestPort);
			flight.setSourceAirPort(aAirport);
			flight.setDesintationAirport(bAirport);

			flight.setDepartureTime(depTime);
			flight.setArrivalTime(Utils.addMinutes(depTime, 60));

			flight.setPlannedFlight(flight.clone());

			flight.getPlannedFlight().setDesintationAirport(pAirport);
			flight.getPlannedFlight().setDepartureTime(plannedDepTime);

			Aircraft plannedAir = new Aircraft();
			plannedAir.setType(plannedAirType);
			plannedAir.setId("10");
			flight.setPlannedAir(plannedAir);

			flight.setImpCoe(impCoe);

			flight.setSchdDate(scheDate);
			flight.setSchdNo(schdNo);

			// System.out.println(flight.getDesintationAirport().getId());
			// System.out.println(flight.getPlannedFlight().getDesintationAirport().getId());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return flight;
	}

	@Before
	public void setUp() throws Exception {

		InitData.plannedMaxFligthId = 1000;
		// Flight fligt1 = createFlight(102, "002", "003", "003",
		// Utils.timeStr2date("2017-01-01 13:00:00"),
		// Utils.timeStr2date("2017-01-01 13:00:00"), "1" , new BigDecimal("1"),
		// 202, Utils.dateStr2date("2017-01-01"));
		//
		// Flight fligt2 = createFlight(103, "003", "004", "004",
		// Utils.timeStr2date("2017-01-01 18:00:00"),
		// Utils.timeStr2date("2017-01-01 18:00:00"), "1" , new BigDecimal("1"),
		// 202, Utils.dateStr2date("2017-01-01"));
		//
		// InitData.jointFlightMap.put(fligt1.getFlightId(), fligt2);
		// InitData.jointFlightMap.put(fligt2.getFlightId(), null);
		//
		// delay 1 hour 100
		List<Flight> flightChain = new ArrayList<Flight>();
		flightChain.add(createFlight(101, "001", "002", "002", Utils.timeStr2date("2017-01-01 10:00:00"),
				Utils.timeStr2date("2017-01-01 9:00:00"), "1", new BigDecimal("1"), 201,
				Utils.dateStr2date("2017-01-01")));
		// joint 5000
		flightChain.add(createFlight(10001, "002", "004", "003", Utils.timeStr2date("2017-01-01 10:00:00"),
				Utils.timeStr2date("2017-01-01 10:00:00"), "1", new BigDecimal("1"), 202,
				Utils.dateStr2date("2017-01-01")));

		// change air type
		// flightChain.add(createFlight(104, "004", "005", "005",
		// Utils.timeStr2date("2017-01-01 10:00:00"),
		// Utils.timeStr2date("2017-01-02 10:00:00"), "2" , new BigDecimal("1"),
		// 204, Utils.dateStr2date("2017-01-02")));
		//
		// flightChain.add(createFlight(105, "005", "006", "006",
		// Utils.timeStr2date("2017-01-01 15:00:00"),
		// Utils.timeStr2date("2017-01-02 15:00:00"), "1" , new BigDecimal("1"),
		// 205, Utils.dateStr2date("2017-01-02")));
		//
		// flightChain.add(createFlight(106, "006", "007", "007",
		// Utils.timeStr2date("2017-01-01 19:00:00"),
		// Utils.timeStr2date("2017-01-02 19:00:00"), "1" , new BigDecimal("1"),
		// 206, Utils.dateStr2date("2017-01-02")));
		//
		// // joint cost
		// flightChain.add(createFlight(107, "007", "008", "008",
		// Utils.timeStr2date("2017-01-01 10:00:00"),
		// Utils.timeStr2date("2017-01-03 10:00:00"), "1" , new BigDecimal("1"),
		// 207, Utils.dateStr2date("2017-01-03")));
		// // cancel
		// flightChain.add(createFlight(108, "008", "009", "009",
		// Utils.timeStr2date("2017-01-01 10:00:00"),
		// Utils.timeStr2date("2017-01-03 10:00:00"), "1" , new BigDecimal("1"),
		// 207, Utils.dateStr2date("2017-01-03")));
		//
		// flightChain.add(createFlight(109, "009", "010", "010",
		// Utils.timeStr2date("2017-01-01 10:00:00"),
		// Utils.timeStr2date("2017-01-03 10:00:00"), "1" , new BigDecimal("1"),
		// 208, Utils.dateStr2date("2017-01-03")));

		air1.setFlightChain(flightChain);
		air1.setId("1");
		air1.setType("1");

		List<Flight> flightChain2 = new ArrayList<Flight>();

		// cancel 1000
		flightChain2.add(createFlight(102, "002", "003", "003", Utils.timeStr2date("2017-01-01 13:00:00"),
				Utils.timeStr2date("2017-01-01 13:00:00"), "1", new BigDecimal("1"), 202,
				Utils.dateStr2date("2017-01-01")));
		// cancel 1000
		flightChain2.add(createFlight(103, "003", "004", "004", Utils.timeStr2date("2017-01-01 18:00:00"),
				Utils.timeStr2date("2017-01-01 18:00:00"), "1", new BigDecimal("1"), 202,
				Utils.dateStr2date("2017-01-01")));

		// flightChain2.add(createFlight(201, "001", "002", "002",
		// Utils.timeStr2date("2017-01-01 10:00:00"),
		// Utils.timeStr2date("2017-01-01 9:00:00"), "1" , new BigDecimal("1"),
		// 201, Utils.dateStr2date("2017-01-01")));
		air2.setFlightChain(flightChain2);
		air2.setCancel(true);
		air2.setId("1");
		air2.setType("1");
	}

	@Test
	public void testConstructNewSolution() throws CloneNotSupportedException, ParseException {

		XiaMengAirlineSolution aSolution = new XiaMengAirlineSolution();
		aSolution.replaceOrAddNewAircraft(air1);
		aSolution.replaceOrAddNewAircraft(air2);

		aSolution.refreshCost(true);

		assertEquals(7100, aSolution.getCost().intValue());

		// aSolution.generateOutput("2");
		LocalSearch searchEngine = new LocalSearch();

		XiaMengAirlineSolution aBetterSolution = null;

		aBetterSolution = searchEngine.constructNewSolution(aSolution);
		aBetterSolution.refreshCost(true);
		aBetterSolution.generateOutput("2");
		assertEquals(100, aBetterSolution.getCost().intValue());

	}

}
