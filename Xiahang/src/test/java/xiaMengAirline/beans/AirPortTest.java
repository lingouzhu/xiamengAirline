package xiaMengAirline.beans;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.rmi.CORBA.Util;

import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.util.InitData;
import xiaMengAirline.util.Utils;

import static org.junit.Assert.*;

public class AirPortTest {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testGetMatchedAirports() throws CloneNotSupportedException {
		Aircraft air1 = new Aircraft();
		List<Flight> flightChain = new ArrayList<Flight>();
		flightChain.add(createFlight(101, "ORF", "EWR"));
		flightChain.add(createFlight(102, "EWR", "STL"));
		flightChain.add(createFlight(103, "STL", "CLE"));
		flightChain.add(createFlight(104, "CLE", "BDL"));
		flightChain.add(createFlight(105, "BDL", "CLE"));
		air1.setFlightChain(flightChain);
		air1.setId("1");

		for (Flight aFlight : flightChain) {
			aFlight.setAssignedAir(air1);
		}

		Aircraft air2 = new Aircraft();
		List<Flight> flightChain2 = new ArrayList<Flight>();
		flightChain2.add(createFlight(201, "CLE", "ATL"));
		flightChain2.add(createFlight(202, "ATL", "EWR"));
		flightChain2.add(createFlight(203, "EWR", "BWI"));
		flightChain2.add(createFlight(204, "BWI", "CLE"));
		flightChain2.add(createFlight(205, "CLE", "MDW"));
		air2.setFlightChain(flightChain2);
		air2.setId("2");

		for (Flight aFlight : flightChain2) {
			aFlight.setAssignedAir(air2);
		}

		HashMap<Flight, List<MatchedFlight>> matchedFlights = air1.getMatchedFlights(air2);

		// air1 Flight 104 shall match
		assertEquals(true, matchedFlights.containsKey(air1.getFlight(3)));

		// air1 Flight 102-103 (EWR - CLE) shall match air2 203-204 (EWR - CLE)
		assertEquals(true, matchedFlights.containsKey(air1.getFlight(1)));
		MatchedFlight aMatched = matchedFlights.get(air1.getFlight(1)).get(0);
		assertEquals(air2.getFlight(aMatched.getAir2SourceFlight()).getSourceAirPort().getId(),
				air1.getFlight(aMatched.getAir1SourceFlight()).getSourceAirPort().getId());
		assertEquals(air2.getFlight(aMatched.getAir2DestFlight()).getDesintationAirport().getId(),
				air1.getFlight(aMatched.getAir1DestFlight()).getDesintationAirport().getId());

		// test exchange
		// air 1 101, 203, 204, 104, 105
		// air 2 201, 202, 102, 103, 205
		Aircraft newAircraftt1 = air1.clone();
		Aircraft newAircraftt2 = air2.clone();
		Flight air1SourcetFlight = newAircraftt1.getFlight(aMatched.getAir1SourceFlight());
		Flight air1DesttFlight = newAircraftt1.getFlight(aMatched.getAir1DestFlight());
		Flight air2SourcetFlight = newAircraftt2.getFlight(aMatched.getAir2SourceFlight());
		Flight air2DesttFlight = newAircraftt2.getFlight(aMatched.getAir2DestFlight());
		newAircraftt1.insertFlightChain(air2, air2.getFlight(aMatched.getAir2SourceFlight()),
				air2.getFlight(aMatched.getAir2DestFlight()), air1DesttFlight, false);
		newAircraftt2.insertFlightChain(air1, air1.getFlight(aMatched.getAir1SourceFlight()),
				air1.getFlight(aMatched.getAir1DestFlight()), air2DesttFlight, false);
		newAircraftt1.removeFlightChain(air1SourcetFlight, air1DesttFlight);
		newAircraftt2.removeFlightChain(air2SourcetFlight, air2DesttFlight);

		List<Integer> air1FlightList = new ArrayList<Integer>();
		air1FlightList.add(101);
		air1FlightList.add(203);
		air1FlightList.add(204);
		air1FlightList.add(104);
		air1FlightList.add(105);

		List<Integer> air2FlightList = new ArrayList<Integer>();
		air2FlightList.add(201);
		air2FlightList.add(202);
		air2FlightList.add(102);
		air2FlightList.add(103);
		air2FlightList.add(205);

		Flight testF203 = null;
		Flight testF104 = null;
		List<Integer> air1FlightListAct = new ArrayList<Integer>();
		for (Flight aFlight : newAircraftt1.getFlightChain()) {
			air1FlightListAct.add(aFlight.getSchdNo());
			if (aFlight.getSchdNo() == 203)
				testF203 = aFlight;
			else if (aFlight.getSchdNo() == 104)
				testF104 = aFlight;

		}

		assertEquals(air1FlightList, air1FlightListAct);
		assertEquals(air1.getId(), testF203.getAssignedAir().getId());
		assertEquals(air1.getId(), testF104.getAssignedAir().getId());

		List<Integer> air2FlightListAct = new ArrayList<Integer>();
		for (Flight aFlight : newAircraftt2.getFlightChain()) {
			air2FlightListAct.add(aFlight.getSchdNo());
		}

		assertEquals(air2FlightList, air2FlightListAct);

		System.out.println("testGetMatchedAirports - start");
		for (Map.Entry<Flight, List<MatchedFlight>> entry : matchedFlights.entrySet()) {
			Flight key = entry.getKey();
			List<MatchedFlight> value = entry.getValue();

			System.out.println(key.getSchdNo());
			for (MatchedFlight aConn : value) {
				System.out.println("air1 source:" + air1.getFlight(aConn.getAir1SourceFlight()).getSchdNo());
				System.out.println("air1 dest:" + air1.getFlight(aConn.getAir1DestFlight()).getSchdNo());
				System.out.println("air2 source:" + air2.getFlight(aConn.getAir2SourceFlight()).getSchdNo());
				System.out.println("air2 dest:" + air2.getFlight(aConn.getAir2DestFlight()).getSchdNo());

				Aircraft newAircraft1 = air1.clone();
				Aircraft newAircraft2 = air2.clone();
				Flight air1SourceFlight = newAircraft1.getFlight(aConn.getAir1SourceFlight());
				Flight air1DestFlight = newAircraft1.getFlight(aConn.getAir1DestFlight());
				Flight air2SourceFlight = newAircraft2.getFlight(aConn.getAir2SourceFlight());
				Flight air2DestFlight = newAircraft2.getFlight(aConn.getAir2DestFlight());
				newAircraft1.insertFlightChain(air2, air2.getFlight(aConn.getAir2SourceFlight()),
						air2.getFlight(aConn.getAir2DestFlight()), air1DestFlight, false);
				newAircraft2.insertFlightChain(air1, air1.getFlight(aConn.getAir1SourceFlight()),
						air1.getFlight(aConn.getAir1DestFlight()), air2DestFlight, false);
				newAircraft1.removeFlightChain(air1SourceFlight, air1DestFlight);
				newAircraft2.removeFlightChain(air2SourceFlight, air2DestFlight);
				List<Flight> updateList1 = newAircraft1.getFlightChain();
				System.out.println("After exchange ...");
				for (Flight aF : updateList1) {
					System.out.println("Air 1 " + aF.getSchdNo());
				}
				List<Flight> updateList2 = newAircraft2.getFlightChain();
				for (Flight aF : updateList2) {
					System.out.println("Air 2 " + aF.getSchdNo());
				}

			}
		}

		System.out.println("testGetMatchedAirports - end");
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
	public void testGetCircuitAirports() throws CloneNotSupportedException {
		Aircraft air1 = new Aircraft();
		List<Flight> flightChain = new ArrayList<Flight>();
		flightChain.add(createFlight(101, "ORF", "EWR"));
		flightChain.add(createFlight(102, "EWR", "STL"));
		flightChain.add(createFlight(103, "STL", "CLE"));
		Flight f104 = createFlight(104, "CLE", "BDL");
		flightChain.add(f104);
		Flight f105 = createFlight(105, "BDL", "CLE");
		flightChain.add(f105);
		air1.setFlightChain(flightChain);
		air1.setId("1");
		for (Flight aFlight : flightChain) {
			aFlight.setAssignedAir(air1);
		}

		Aircraft air2 = new Aircraft();
		List<Flight> flightChain2 = new ArrayList<Flight>();
		flightChain2.add(createFlight(201, "CLE", "ATL"));
		flightChain2.add(createFlight(202, "ATL", "EWR"));
		flightChain2.add(createFlight(203, "EWR", "BWI"));
		flightChain2.add(createFlight(204, "BWI", "CLE"));
		flightChain2.add(createFlight(205, "CLE", "MDW"));
		air2.setFlightChain(flightChain2);
		air2.setId("2");
		for (Flight aFlight : flightChain2) {
			aFlight.setAssignedAir(air2);
		}

		HashMap<Flight, List<Flight>> circuitFlightsAir1 = air1.getCircuitFlights();
		HashMap<Flight, List<Flight>> circuitFlightsAir2 = air2.getCircuitFlights();

		assertEquals(true, circuitFlightsAir1.containsKey(f104));
		assertEquals(f105, circuitFlightsAir1.get(f104).get(0));

		// test cancel
		Aircraft newAirt1 = air1.clone();
		Aircraft cancelledtAir = newAirt1.getCancelledAircraft();
		Flight sourcetFlight = newAirt1.getFlight(air1.getFlightChain().indexOf(f104));
		Flight desttFlight = newAirt1.getFlight(air1.getFlightChain().indexOf(f105));

		cancelledtAir.insertFlightChain(air1, f104, f105,
				cancelledtAir.getFlight(cancelledtAir.getFlightChain().size() - 1), false);
		newAirt1.removeFlightChain(sourcetFlight, desttFlight);

		List<Integer> air1FlightList = new ArrayList<Integer>();
		air1FlightList.add(101);
		air1FlightList.add(102);
		air1FlightList.add(103);

		List<Integer> air2FlightList = new ArrayList<Integer>();
		air2FlightList.add(104);
		air2FlightList.add(105);

		Flight testF102 = null;
		List<Integer> air1FlightListAct = new ArrayList<Integer>();
		for (Flight aFlight : newAirt1.getFlightChain()) {
			air1FlightListAct.add(aFlight.getSchdNo());
			if (aFlight.getSchdNo() == 102)
				testF102 = aFlight;
		}

		assertEquals(air1FlightList, air1FlightListAct);

		Flight testF105 = null;
		List<Integer> air2FlightListAct = new ArrayList<Integer>();
		for (Flight aFlight : cancelledtAir.getFlightChain()) {
			air2FlightListAct.add(aFlight.getSchdNo());
			if (aFlight.getSchdNo() == 105)
				testF105 = aFlight;
		}

		assertEquals(air2FlightList, air2FlightListAct);
		assertEquals(false, testF102.getAssignedAir().isCancel());
		assertEquals(true, testF105.getAssignedAir().isCancel());
		assertEquals(cancelledtAir, newAirt1.getCancelledAircraft());

		for (Map.Entry<Flight, List<Flight>> entry : circuitFlightsAir1.entrySet()) {
			Flight key = entry.getKey();
			List<Flight> value = entry.getValue();

			for (Flight aFlight : value) {
				System.out.println("Air1 " + key.getSchdNo() + " => " + aFlight.getSchdNo());
				Aircraft newAir1 = air1.clone();
				Aircraft cancelledAir = newAir1.getCancelledAircraft();
				Flight sourceFlight = newAir1.getFlight(air1.getFlightChain().indexOf(key));
				Flight destFlight = newAir1.getFlight(air1.getFlightChain().indexOf(aFlight));

				cancelledAir.insertFlightChain(air1, key, aFlight,
						cancelledAir.getFlight(cancelledAir.getFlightChain().size() - 1), false);
				newAir1.removeFlightChain(sourceFlight, destFlight);

				System.out.println("After exchange ...");
				List<Flight> updateList1 = newAir1.getFlightChain();
				for (Flight aF : updateList1) {
					System.out.println("Air 1 " + aF.getSchdNo());
				}
				List<Flight> updateList2 = cancelledAir.getFlightChain();
				for (Flight aF : updateList2) {
					System.out.println("Air 1 cancelled " + aF.getSchdNo());
				}
			}
		}

		for (Map.Entry<Flight, List<Flight>> entry : circuitFlightsAir2.entrySet()) {
			Flight key = entry.getKey();
			List<Flight> value = entry.getValue();

			for (Flight aFlight : value) {
				System.out.println("Air2 " + key.getSchdNo() + " => " + aFlight.getSchdNo());

				Aircraft newAircraft1 = air1.clone();
				Aircraft newAircraft2 = air2.clone();
				Flight air2SourceFlight = newAircraft2.getFlight(air2.getFlightChain().indexOf(key));
				Flight air2DestFlight = newAircraft2.getFlight(air2.getFlightChain().indexOf(aFlight));
				Flight air1Flight = newAircraft1.getFlight(1);

				newAircraft1.insertFlightChain(air2, key, aFlight, air1Flight, true);
				newAircraft2.removeFlightChain(air2SourceFlight, air2DestFlight);

				System.out.println("After exchange ...");
				List<Flight> updateList1 = newAircraft1.getFlightChain();
				for (Flight aF : updateList1) {
					System.out.println("Air 1 " + aF.getSchdNo());
				}
				List<Flight> updateList2 = newAircraft2.getFlightChain();
				for (Flight aF : updateList2) {
					System.out.println("Air 2 " + aF.getSchdNo());
				}
			}
		}

		// ArrayList<AirPort> listAirportB = new ArrayList<AirPort> ();
		// AirPort aAir = new AirPort();
		//
		// aAir = new AirPort();
		// aAir.setId("32");
		// listAirportB.add(aAir);
		//
		// aAir = new AirPort();
		// aAir.setId("49");
		// listAirportB.add(aAir);
		//
		// aAir = new AirPort();
		// aAir.setId("16");
		// listAirportB.add(aAir);
		//
		// aAir = new AirPort();
		// aAir.setId("49");
		// listAirportB.add(aAir);
		//
		// aAir = new AirPort();
		// aAir.setId("25");
		// listAirportB.add(aAir);
		//
		// aAir = new AirPort();
		// aAir.setId("66");
		// listAirportB.add(aAir);
		//
		// aAir = new AirPort();
		// aAir.setId("49");
		// listAirportB.add(aAir);
		//
		// HashMap<Integer, List<Integer>> listCircuit =
		// AirPort.getCircuitAirports(listAirportB);
		// for(Map.Entry<Integer, List<Integer>> entry : listCircuit.entrySet())
		// {
		// Integer key = entry.getKey();
		// List<Integer> value = entry.getValue();
		//
		// System.out.println(key + " => " + value);
		// }

	}

	@Test
	public void testRequestAirport() throws CloneNotSupportedException, ParseException {
		AirPortClose portCloseBean = new AirPortClose();
		String airPortId = "49";
		AirPort aAirport = InitData.airportList.getAirport(airPortId);

		portCloseBean.setStartTime(Utils.stringFormatToTime2("06/05/2017 14:00:00"));
		portCloseBean.setEndTime(Utils.stringFormatToTime2("07/05/2017 17:00:00"));
		portCloseBean.setAllowForLanding(false);
		aAirport.addCloseSchedule(portCloseBean);

		portCloseBean = new AirPortClose();
		portCloseBean.setStartTime(Utils.stringFormatToTime2("06/05/2017 16:00:00"));
		portCloseBean.setEndTime(Utils.stringFormatToTime2("07/05/2017 17:00:00"));
		portCloseBean.setAllowForTakeoff(false);
		aAirport.addCloseSchedule(portCloseBean);

		portCloseBean = new AirPortClose();
		portCloseBean.setStartTime(Utils.stringFormatToTime2("06/05/2017 16:00:00"));
		portCloseBean.setEndTime(Utils.stringFormatToTime2("07/05/2017 17:00:00"));
		portCloseBean.setAllowForTakeoff(false);
		aAirport.addCloseSchedule(portCloseBean);

		RegularAirPortClose normalCloseBean = new RegularAirPortClose();
		normalCloseBean.setCloseTime("00:10");
		normalCloseBean.setOpenTime("06:10");
		normalCloseBean.setOpenDate("2017-04-28");
		normalCloseBean.setCloseDate("2017-06-01");
		aAirport.addRegularCloseSchedule(normalCloseBean);

		// normal
		FlightTime aTime = new FlightTime();
		aTime.setArrivalTime(Utils.stringFormatToTime2("03/05/2017 14:00:00"));
		aTime.setDepartureTime(Utils.stringFormatToTime2("03/05/2017 20:00:00"));

		FlightTime rightTime = aAirport.requestAirport(aTime);

		assertEquals(null, rightTime);

		// normal
		aTime = new FlightTime();
		aTime.setArrivalTime(Utils.stringFormatToTime2("08/05/2017 14:00:00"));
		aTime.setDepartureTime(Utils.stringFormatToTime2("08/05/2017 20:00:00"));

		rightTime = aAirport.requestAirport(aTime);

		assertEquals(null, rightTime);

		// arrival-dep in event
		aTime = new FlightTime();
		aTime.setArrivalTime(Utils.stringFormatToTime2("06/05/2017 18:00:00"));
		aTime.setDepartureTime(Utils.stringFormatToTime2("06/05/2017 22:00:00"));

		System.out.println(Utils.timeFormatToString2(aTime.getArrivalTime()));

		rightTime = aAirport.requestAirport(aTime);

		assertEquals("07/05/2017 17:00:00", Utils.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("07/05/2017 17:50:00", Utils.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival close to airport open, dep > grounding time
		aTime = new FlightTime();
		aTime.setArrivalTime(Utils.stringFormatToTime2("07/05/2017 16:30:00"));
		aTime.setDepartureTime(Utils.stringFormatToTime2("07/05/2017 20:00:00"));

		System.out.println(Utils.timeFormatToString2(aTime.getArrivalTime()));

		rightTime = aAirport.requestAirport(aTime);

		assertEquals("07/05/2017 17:00:00", Utils.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("07/05/2017 20:00:00", Utils.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival close to airport open, dep < grounding time
		aTime = new FlightTime();
		aTime.setArrivalTime(Utils.stringFormatToTime2("07/05/2017 16:30:00"));
		aTime.setDepartureTime(Utils.stringFormatToTime2("07/05/2017 17:30:00"));

		System.out.println(Utils.timeFormatToString2(aTime.getArrivalTime()));

		rightTime = aAirport.requestAirport(aTime);

		assertEquals("07/05/2017 17:00:00", Utils.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("07/05/2017 17:50:00", Utils.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival out, dep in airport event, gap > grounding time
		aTime = new FlightTime();
		aTime.setArrivalTime(Utils.stringFormatToTime2("06/05/2017 13:00:00"));
		aTime.setDepartureTime(Utils.stringFormatToTime2("07/05/2017 10:00:00"));

		rightTime = aAirport.requestAirport(aTime);

		assertEquals("06/05/2017 13:00:00", Utils.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("06/05/2017 16:00:00", Utils.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival in landing-off period, dep in airport event, gap > grounding
		// time
		aTime = new FlightTime();
		aTime.setArrivalTime(Utils.stringFormatToTime2("06/05/2017 13:59:00"));
		aTime.setDepartureTime(Utils.stringFormatToTime2("07/05/2017 10:00:00"));

		rightTime = aAirport.requestAirport(aTime);

		assertEquals("06/05/2017 13:59:00", Utils.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("06/05/2017 16:00:00", Utils.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival in landing-off period, dep in airport event, gap > grounding,
		// departure earlier
		// time
		aTime = new FlightTime();
		aTime.setArrivalTime(Utils.stringFormatToTime2("06/05/2017 13:59:00"));
		aTime.setDepartureTime(Utils.stringFormatToTime2("06/05/2017 15:00:00"));

		rightTime = aAirport.requestAirport(aTime);

		assertEquals(null, rightTime);

		// arrival in normal close period, enough gap
		aTime = new FlightTime();
		aTime.setArrivalTime(Utils.stringFormatToTime2("26/05/2017 01:00:00"));
		aTime.setDepartureTime(Utils.stringFormatToTime2("26/05/2017 15:00:00"));

		rightTime = aAirport.requestAirport(aTime);

		assertEquals("26/05/2017 06:10:00", Utils.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("26/05/2017 15:00:00", Utils.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival in normal close period, small gap
		aTime = new FlightTime();
		aTime.setArrivalTime(Utils.stringFormatToTime2("26/05/2017 01:00:00"));
		aTime.setDepartureTime(Utils.stringFormatToTime2("26/05/2017 06:20:00"));

		rightTime = aAirport.requestAirport(aTime);

		assertEquals("26/05/2017 06:10:00", Utils.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("26/05/2017 07:00:00", Utils.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival in normal close period, dep in range, enough gap
		aTime = new FlightTime();
		aTime.setArrivalTime(Utils.stringFormatToTime2("26/05/2017 00:00:00"));
		aTime.setDepartureTime(Utils.stringFormatToTime2("26/05/2017 03:20:00"));

		rightTime = aAirport.requestAirport(aTime);

		assertEquals("26/05/2017 00:00:00", Utils.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("26/05/2017 06:10:00", Utils.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival in next to close period, dep out range, 
		aTime = new FlightTime();
		aTime.setArrivalTime(Utils.stringFormatToTime2("26/05/2017 00:00:00"));
		aTime.setDepartureTime(Utils.stringFormatToTime2("26/05/2017 06:15:00"));

		rightTime = aAirport.requestAirport(aTime);
		
		assertEquals(null, rightTime);


	}

}
