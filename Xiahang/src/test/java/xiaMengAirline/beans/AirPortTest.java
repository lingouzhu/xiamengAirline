package xiaMengAirline.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.Exception.AirportNotAcceptArrivalTimeBackup;
import xiaMengAirline.Exception.AirportNotAcceptDepartureTimeBackup;
import xiaMengAirline.Exception.AirportNotAvailableBackup;
import xiaMengAirline.Exception.FlightDurationNotFoundBackup;
import xiaMengAirline.util.InitDataBackup;
import xiaMengAirline.util.UtilsBackup;

public class AirPortTest {

	@Before
	public void setUp() throws Exception {
		String airType = "1";
		String startPort = "EWR";
		String endPort = "CLE";
		int time = 120;

		InitDataBackup.fightDurationMap.put(airType + "_" + startPort + "_" + endPort, time);

		FlightBackup f1 = createFlight(102, "EWR", "STL");
		f1.setFlightId(102);
		f1.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 18:30:00"));
		f1.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 22:30:00"));
		FlightBackup f2 = createFlight(103, "STL", "CLE");
		f2.setFlightId(103);
		f2.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 23:00:00"));
		f2.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 23:30:00"));
		InitDataBackup.jointFlightMap.put(102, f2);
		InitDataBackup.jointFlightMap.put(103, null);
		
		AircraftBackup air1 = new AircraftBackup();
		List<FlightBackup> flightChain = new ArrayList<FlightBackup>();
		FlightBackup f101 = createFlight(101, "ORF", "EWR");
		flightChain.add(f101);
		FlightBackup f102 = createFlight(102, "EWR", "STL");
		flightChain.add(f102);
		FlightBackup f103 = createFlight(103, "STL", "CLE");
		flightChain.add(f103);
		FlightBackup f104 = createFlight(104, "CLE", "BDL");
		flightChain.add(f104);
		FlightBackup f105 = createFlight(105, "BDL", "CLE");
		flightChain.add(f105);
		air1.setFlightChain(flightChain);
		air1.setId("1");
		air1.setType("1");
		f101.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
		f101.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
		f102.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
		f102.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
		f103.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
		f103.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
		f104.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
		f104.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
		f105.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
		f105.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
		f101.setImpCoe(new BigDecimal(1.5));
		f102.setImpCoe(new BigDecimal(1.5));
		f103.setImpCoe(new BigDecimal(1.5));
		f104.setImpCoe(new BigDecimal(1.5));
		f105.setImpCoe(new BigDecimal(1.5));
		f101.setFlightId(101);
		f102.setFlightId(102);
		f103.setFlightId(103);
		f104.setFlightId(104);
		f105.setFlightId(105);

		for (FlightBackup aFlight : flightChain) {
			aFlight.setAssignedAir(air1);
			aFlight.setPlannedFlight(aFlight);
			aFlight.setPlannedAir(air1);
		}

		AircraftBackup air2 = new AircraftBackup();
		List<FlightBackup> flightChain2 = new ArrayList<FlightBackup>();
		FlightBackup f201 =createFlight(201, "CLE", "ATL");
		flightChain2.add(f201);
		FlightBackup f202 = createFlight(202, "ATL", "EWR");
		flightChain2.add(f202);
		FlightBackup f203 = createFlight(203, "EWR", "BWI");
		flightChain2.add(f203);
		FlightBackup f204 = createFlight(204, "BWI", "CLE");
		flightChain2.add(f204);
		FlightBackup f205 = createFlight(205, "CLE", "MDW");
		flightChain2.add(f205);
		air2.setFlightChain(flightChain2);
		air2.setId("2");
		air2.setType("2");
		f201.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
		f201.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
		f202.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
		f202.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
		f203.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
		f203.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
		f204.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
		f204.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
		f205.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
		f205.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
		f201.setImpCoe(new BigDecimal(1.5));
		f202.setImpCoe(new BigDecimal(1.5));
		f203.setImpCoe(new BigDecimal(1.5));
		f204.setImpCoe(new BigDecimal(1.5));
		f205.setImpCoe(new BigDecimal(1.5));
		f201.setFlightId(201);
		f202.setFlightId(202);
		f203.setFlightId(203);
		f204.setFlightId(204);
		f205.setFlightId(205);
		

		for (FlightBackup aFlight : flightChain2) {
			aFlight.setAssignedAir(air2);
			aFlight.setPlannedFlight(aFlight);
			aFlight.setPlannedAir(air2);
		}

		InitDataBackup.originalSolution.replaceOrAddNewAircraft(air1);
		InitDataBackup.originalSolution.replaceOrAddNewAircraft(air2);
		InitDataBackup.maxFligthId = 205;
		InitDataBackup.plannedMaxFligthId = 205;
		
		InitDataBackup.specialFlightMap.put("104_105" , 30);

	}

	@Test
	public void testGetMatchedAirports() throws CloneNotSupportedException, ParseException {
		AircraftBackup air1 = InitDataBackup.originalSolution.getAircraft("1", "1", false,false).clone();
		AircraftBackup air2 = InitDataBackup.originalSolution.getAircraft("2", "1", false,false).clone();
		HashMap<FlightBackup, List<MatchedFlightBackup>> matchedFlights = air1.getMatchedFlights(air2);

		// air1 Flight 104 shall match
		assertEquals(true, matchedFlights.containsKey(air1.getFlight(3)));

		// air1 Flight 102-103 (EWR - CLE) shall match air2 203-204 (EWR - CLE)
		assertEquals(true, matchedFlights.containsKey(air1.getFlight(1)));
		MatchedFlightBackup aMatched = matchedFlights.get(air1.getFlight(1)).get(0);
		assertEquals(air2.getFlight(aMatched.getAir2SourceFlight()).getSourceAirPort().getId(),
				air1.getFlight(aMatched.getAir1SourceFlight()).getSourceAirPort().getId());
		assertEquals(air2.getFlight(aMatched.getAir2DestFlight()).getDesintationAirport().getId(),
				air1.getFlight(aMatched.getAir1DestFlight()).getDesintationAirport().getId());

		// test exchange
		// air 1 101, 203, 204, 104, 105
		// air 2 201, 202, 102, 103, 205
		AircraftBackup newAircraftt1 = air1.clone();
		AircraftBackup newAircraftt2 = air2.clone();
		FlightBackup air1SourcetFlight = newAircraftt1.getFlight(aMatched.getAir1SourceFlight());
		FlightBackup air1DesttFlight = newAircraftt1.getFlight(aMatched.getAir1DestFlight());
		FlightBackup air2SourcetFlight = newAircraftt2.getFlight(aMatched.getAir2SourceFlight());
		FlightBackup air2DesttFlight = newAircraftt2.getFlight(aMatched.getAir2DestFlight());
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

		FlightBackup testF203 = null;
		FlightBackup testF104 = null;
		List<Integer> air1FlightListAct = new ArrayList<Integer>();
		for (FlightBackup aFlight : newAircraftt1.getFlightChain()) {
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
		for (FlightBackup aFlight : newAircraftt2.getFlightChain()) {
			air2FlightListAct.add(aFlight.getSchdNo());
		}

		assertEquals(air2FlightList, air2FlightListAct);

		System.out.println("testGetMatchedAirports - start");
		
		

		for (Map.Entry<FlightBackup, List<MatchedFlightBackup>> entry : matchedFlights.entrySet()) {
			FlightBackup key = entry.getKey();
			List<MatchedFlightBackup> value = entry.getValue();

			System.out.println(key.getSchdNo());
			for (MatchedFlightBackup aConn : value) {
				System.out.println("air1 source:" + air1.getFlight(aConn.getAir1SourceFlight()).getSchdNo());
				System.out.println("air1 dest:" + air1.getFlight(aConn.getAir1DestFlight()).getSchdNo());
				System.out.println("air2 source:" + air2.getFlight(aConn.getAir2SourceFlight()).getSchdNo());
				System.out.println("air2 dest:" + air2.getFlight(aConn.getAir2DestFlight()).getSchdNo());

				AircraftBackup newAircraft1 = air1.clone();
				AircraftBackup newAircraft2 = air2.clone();
				FlightBackup air1SourceFlight = newAircraft1.getFlight(aConn.getAir1SourceFlight());
				FlightBackup air1DestFlight = newAircraft1.getFlight(aConn.getAir1DestFlight());
				FlightBackup air2SourceFlight = newAircraft2.getFlight(aConn.getAir2SourceFlight());
				FlightBackup air2DestFlight = newAircraft2.getFlight(aConn.getAir2DestFlight());
				newAircraft1.insertFlightChain(air2, air2.getFlight(aConn.getAir2SourceFlight()),
						air2.getFlight(aConn.getAir2DestFlight()), air1DestFlight, false);
				newAircraft2.insertFlightChain(air1, air1.getFlight(aConn.getAir1SourceFlight()),
						air1.getFlight(aConn.getAir1DestFlight()), air2DestFlight, false);
				newAircraft1.removeFlightChain(air1SourceFlight, air1DestFlight);
				newAircraft2.removeFlightChain(air2SourceFlight, air2DestFlight);
				List<FlightBackup> updateList1 = newAircraft1.getFlightChain();
				System.out.println("After exchange ...");
				for (FlightBackup aF : updateList1) {
					System.out.println("Air 1 flight Id" + aF.getFlightId());
					//System.out.println("Air 1 sch no" + aF.getSchdNo());
				}
				List<FlightBackup> updateList2 = newAircraft2.getFlightChain();
				for (FlightBackup aF : updateList2) {
					System.out.println("Air 2 flight Id" + aF.getFlightId());
					//System.out.println("Air 2 sch no" + aF.getSchdNo());
				}
				
				//test cost
				//if air type
				XiaMengAirlineSolutionBackup aNewSol = new XiaMengAirlineSolutionBackup();
				
				//if diff air type
				System.out.println("After exchange cost 2...");
				if (key.getFlightId() == 104) {
					aNewSol = new XiaMengAirlineSolutionBackup();
					newAircraft1.setType("1");
					newAircraft2.setType("2");
					aNewSol.replaceOrAddNewAircraft(newAircraft1);
					aNewSol.replaceOrAddNewAircraft(newAircraft2);
					aNewSol.refreshCost(true);
					aNewSol.generateOutput("1");
					assertEquals(9000, (int) Math.round(aNewSol.getCost().doubleValue()));
					System.out.println("After exchange cost 2 completed...");					
				}


			}
		}
		
		

		System.out.println("testGetMatchedAirports - end");
	}

	private FlightBackup createFlight(int flightId, String srcPort, String destPort) {
		FlightBackup flight = new FlightBackup();
		flight.setSchdNo(flightId);
		flight.setFlightId(flightId);
		AirPortBackup aAirport = InitDataBackup.airportList.getAirport(srcPort);
		AirPortBackup bAirport = InitDataBackup.airportList.getAirport(destPort);
		;

		flight.setSourceAirPort(aAirport);
		flight.setDesintationAirport(bAirport);

		return flight;
	}

	@Test
	public void testGetCircuitAirports() throws CloneNotSupportedException {
		XiaMengAirlineSolutionBackup aSol = new XiaMengAirlineSolutionBackup();
		AircraftBackup air1 = new AircraftBackup();
		List<FlightBackup> flightChain = new ArrayList<FlightBackup>();
		flightChain.add(createFlight(101, "ORF", "EWR"));
		flightChain.add(createFlight(102, "EWR", "STL"));
		flightChain.add(createFlight(103, "STL", "CLE"));
		FlightBackup f104 = createFlight(104, "CLE", "BDL");
		flightChain.add(f104);
		FlightBackup f105 = createFlight(105, "BDL", "CLE");
		flightChain.add(f105);
		air1.setFlightChain(flightChain);
		air1.setId("1");
		for (FlightBackup aFlight : flightChain) {
			aFlight.setAssignedAir(air1);
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
		for (FlightBackup aFlight : flightChain2) {
			aFlight.setAssignedAir(air2);
		}
		
		aSol.replaceOrAddNewAircraft(air1);
		aSol.replaceOrAddNewAircraft(air2);

		HashMap<FlightBackup, List<FlightBackup>> circuitFlightsAir1 = air1.getCircuitFlights();
		HashMap<FlightBackup, List<FlightBackup>> circuitFlightsAir2 = air2.getCircuitFlights();

		assertEquals(true, circuitFlightsAir1.containsKey(f104));
		assertEquals(f105, circuitFlightsAir1.get(f104).get(0));

		// test cancel
		AircraftBackup newAirt1 = air1.clone();
		AircraftBackup cancelledtAir = aSol.getAircraft(air1.getId(), air1.getType(), true, true).clone();
		FlightBackup sourcetFlight = newAirt1.getFlight(air1.getFlightChain().indexOf(f104));
		FlightBackup desttFlight = newAirt1.getFlight(air1.getFlightChain().indexOf(f105));

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

		FlightBackup testF102 = null;
		List<Integer> air1FlightListAct = new ArrayList<Integer>();
		for (FlightBackup aFlight : newAirt1.getFlightChain()) {
			air1FlightListAct.add(aFlight.getSchdNo());
			if (aFlight.getSchdNo() == 102)
				testF102 = aFlight;
		}

		assertEquals(air1FlightList, air1FlightListAct);

		FlightBackup testF105 = null;
		List<Integer> air2FlightListAct = new ArrayList<Integer>();
		for (FlightBackup aFlight : cancelledtAir.getFlightChain()) {
			air2FlightListAct.add(aFlight.getSchdNo());
			if (aFlight.getSchdNo() == 105)
				testF105 = aFlight;
		}

		assertEquals(air2FlightList, air2FlightListAct);
		assertEquals(false, testF102.getAssignedAir().isCancel());
		assertEquals(true, testF105.getAssignedAir().isCancel());

		for (Map.Entry<FlightBackup, List<FlightBackup>> entry : circuitFlightsAir1.entrySet()) {
			FlightBackup key = entry.getKey();
			List<FlightBackup> value = entry.getValue();

			for (FlightBackup aFlight : value) {
				System.out.println("Air1 " + key.getSchdNo() + " => " + aFlight.getSchdNo());
				AircraftBackup newAir1 = air1.clone();
				AircraftBackup cancelledAir = aSol.getAircraft(air1.getId(), air1.getType(), true, true).clone();
				FlightBackup sourceFlight = newAir1.getFlight(air1.getFlightChain().indexOf(key));
				FlightBackup destFlight = newAir1.getFlight(air1.getFlightChain().indexOf(aFlight));

				cancelledAir.insertFlightChain(air1, key, aFlight,
						cancelledAir.getFlight(cancelledAir.getFlightChain().size() - 1), false);
				newAir1.removeFlightChain(sourceFlight, destFlight);

				System.out.println("After exchange ...");
				List<FlightBackup> updateList1 = newAir1.getFlightChain();
				for (FlightBackup aF : updateList1) {
					System.out.println("Air 1 " + aF.getSchdNo());
				}
				List<FlightBackup> updateList2 = cancelledAir.getFlightChain();
				for (FlightBackup aF : updateList2) {
					System.out.println("Air 1 cancelled " + aF.getSchdNo());
				}
			}
		}

		for (Map.Entry<FlightBackup, List<FlightBackup>> entry : circuitFlightsAir2.entrySet()) {
			FlightBackup key = entry.getKey();
			List<FlightBackup> value = entry.getValue();

			for (FlightBackup aFlight : value) {
				System.out.println("Air2 " + key.getSchdNo() + " => " + aFlight.getSchdNo());

				AircraftBackup newAircraft1 = air1.clone();
				AircraftBackup newAircraft2 = air2.clone();
				FlightBackup air2SourceFlight = newAircraft2.getFlight(air2.getFlightChain().indexOf(key));
				FlightBackup air2DestFlight = newAircraft2.getFlight(air2.getFlightChain().indexOf(aFlight));
				FlightBackup air1Flight = newAircraft1.getFlight(1);

				newAircraft1.insertFlightChain(air2, key, aFlight, air1Flight, true);
				newAircraft2.removeFlightChain(air2SourceFlight, air2DestFlight);

				System.out.println("After exchange ...");
				List<FlightBackup> updateList1 = newAircraft1.getFlightChain();
				for (FlightBackup aF : updateList1) {
					System.out.println("Air 1 " + aF.getSchdNo());
				}
				List<FlightBackup> updateList2 = newAircraft2.getFlightChain();
				for (FlightBackup aF : updateList2) {
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
	public void testRequestAirport() throws CloneNotSupportedException, ParseException, FlightDurationNotFoundBackup,
			AirportNotAcceptDepartureTimeBackup, AirportNotAvailableBackup {
		AirPortCloseBackup portCloseBean = new AirPortCloseBackup();
		String airPortId = "49";
		AirPortBackup aAirport = InitDataBackup.airportList.getAirport(airPortId);

		portCloseBean.setStartTime(UtilsBackup.stringFormatToTime2("06/05/2017 14:00:00"));
		portCloseBean.setEndTime(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"));
		portCloseBean.setAllowForLanding(false);
		aAirport.addCloseSchedule(portCloseBean);

		portCloseBean = new AirPortCloseBackup();
		portCloseBean.setStartTime(UtilsBackup.stringFormatToTime2("06/05/2017 16:00:00"));
		portCloseBean.setEndTime(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"));
		portCloseBean.setAllowForTakeoff(false);
		aAirport.addCloseSchedule(portCloseBean);

		portCloseBean = new AirPortCloseBackup();
		portCloseBean.setStartTime(UtilsBackup.stringFormatToTime2("06/05/2017 16:00:00"));
		portCloseBean.setEndTime(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"));
		portCloseBean.setAllowForTakeoff(false);
		aAirport.addCloseSchedule(portCloseBean);

		RegularAirPortCloseBackup normalCloseBean = new RegularAirPortCloseBackup();
		normalCloseBean.setCloseTime("00:10");
		normalCloseBean.setOpenTime("06:10");
		normalCloseBean.setOpenDate("2017-04-28");
		normalCloseBean.setCloseDate("2017-06-01");
		aAirport.addRegularCloseSchedule(normalCloseBean);

		// normal
		FlightTimeBackup aTime = new FlightTimeBackup();
		aTime.setArrivalTime(UtilsBackup.stringFormatToTime2("03/05/2017 14:00:00"));
		aTime.setDepartureTime(UtilsBackup.stringFormatToTime2("03/05/2017 20:00:00"));

		FlightTimeBackup rightTime = aAirport.requestAirport(aTime, FlightBackup.GroundingTime);

		assertEquals(null, rightTime);

		// normal
		aTime = new FlightTimeBackup();
		aTime.setArrivalTime(UtilsBackup.stringFormatToTime2("08/05/2017 14:00:00"));
		aTime.setDepartureTime(UtilsBackup.stringFormatToTime2("08/05/2017 20:00:00"));

		rightTime = aAirport.requestAirport(aTime, FlightBackup.GroundingTime);

		assertEquals(null, rightTime);

		// arrival-dep in event
		aTime = new FlightTimeBackup();
		aTime.setArrivalTime(UtilsBackup.stringFormatToTime2("06/05/2017 18:00:00"));
		aTime.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 22:00:00"));

		System.out.println(UtilsBackup.timeFormatToString2(aTime.getArrivalTime()));

		rightTime = aAirport.requestAirport(aTime, FlightBackup.GroundingTime);

		assertEquals("07/05/2017 17:00:00", UtilsBackup.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("07/05/2017 17:50:00", UtilsBackup.timeFormatToString2(rightTime.getDepartureTime()));
		assertEquals(true, rightTime.isIsTyphoon());

		// arrival close to airport open, dep > grounding time
		aTime = new FlightTimeBackup();
		aTime.setArrivalTime(UtilsBackup.stringFormatToTime2("07/05/2017 16:30:00"));
		aTime.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 20:00:00"));

		System.out.println(UtilsBackup.timeFormatToString2(aTime.getArrivalTime()));

		rightTime = aAirport.requestAirport(aTime, FlightBackup.GroundingTime);

		assertEquals("07/05/2017 17:00:00", UtilsBackup.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("07/05/2017 20:00:00", UtilsBackup.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival close to airport open, dep < grounding time
		aTime = new FlightTimeBackup();
		aTime.setArrivalTime(UtilsBackup.stringFormatToTime2("07/05/2017 16:30:00"));
		aTime.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 17:30:00"));

		System.out.println(UtilsBackup.timeFormatToString2(aTime.getArrivalTime()));

		rightTime = aAirport.requestAirport(aTime, FlightBackup.GroundingTime);

		assertEquals("07/05/2017 17:00:00", UtilsBackup.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("07/05/2017 17:50:00", UtilsBackup.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival out, dep in airport event, gap > grounding time
		aTime = new FlightTimeBackup();
		aTime.setArrivalTime(UtilsBackup.stringFormatToTime2("06/05/2017 13:00:00"));
		aTime.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 10:00:00"));

		rightTime = aAirport.requestAirport(aTime, FlightBackup.GroundingTime);

		assertEquals("06/05/2017 13:00:00", UtilsBackup.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("06/05/2017 16:00:00", UtilsBackup.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival in landing-off period, dep in airport event, gap > grounding
		// time
		aTime = new FlightTimeBackup();
		aTime.setArrivalTime(UtilsBackup.stringFormatToTime2("06/05/2017 13:59:00"));
		aTime.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 10:00:00"));

		rightTime = aAirport.requestAirport(aTime, FlightBackup.GroundingTime);

		assertEquals("06/05/2017 13:59:00", UtilsBackup.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("06/05/2017 16:00:00", UtilsBackup.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival in landing-off period, dep in airport event, gap > grounding,
		// departure earlier
		// time
		aTime = new FlightTimeBackup();
		aTime.setArrivalTime(UtilsBackup.stringFormatToTime2("06/05/2017 13:59:00"));
		aTime.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 15:00:00"));

		rightTime = aAirport.requestAirport(aTime, FlightBackup.GroundingTime);

		assertEquals(null, rightTime);

		// arrival in normal close period, enough gap
		aTime = new FlightTimeBackup();
		aTime.setArrivalTime(UtilsBackup.stringFormatToTime2("26/05/2017 01:00:00"));
		aTime.setDepartureTime(UtilsBackup.stringFormatToTime2("26/05/2017 15:00:00"));

		rightTime = aAirport.requestAirport(aTime, FlightBackup.GroundingTime);

		assertEquals("26/05/2017 06:10:00", UtilsBackup.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("26/05/2017 15:00:00", UtilsBackup.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival in normal close period, small gap
		aTime = new FlightTimeBackup();
		aTime.setArrivalTime(UtilsBackup.stringFormatToTime2("26/05/2017 01:00:00"));
		aTime.setDepartureTime(UtilsBackup.stringFormatToTime2("26/05/2017 06:20:00"));

		rightTime = aAirport.requestAirport(aTime, FlightBackup.GroundingTime);

		assertEquals("26/05/2017 06:10:00", UtilsBackup.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("26/05/2017 07:00:00", UtilsBackup.timeFormatToString2(rightTime.getDepartureTime()));
		assertEquals(false, rightTime.isIsTyphoon());

		// arrival in normal close period, dep in range, enough gap
		aTime = new FlightTimeBackup();
		aTime.setArrivalTime(UtilsBackup.stringFormatToTime2("26/05/2017 00:00:00"));
		aTime.setDepartureTime(UtilsBackup.stringFormatToTime2("26/05/2017 03:20:00"));

		rightTime = aAirport.requestAirport(aTime, FlightBackup.GroundingTime);

		assertEquals("26/05/2017 00:00:00", UtilsBackup.timeFormatToString2(rightTime.getArrivalTime()));
		assertEquals("26/05/2017 06:10:00", UtilsBackup.timeFormatToString2(rightTime.getDepartureTime()));

		// arrival in next to close period, dep out range,
		aTime = new FlightTimeBackup();
		aTime.setArrivalTime(UtilsBackup.stringFormatToTime2("26/05/2017 00:00:00"));
		aTime.setDepartureTime(UtilsBackup.stringFormatToTime2("26/05/2017 06:15:00"));

		rightTime = aAirport.requestAirport(aTime, FlightBackup.GroundingTime);

		assertEquals(null, rightTime);

		// now test airport time control
		AircraftBackup air1 = new AircraftBackup();
		List<FlightBackup> flightChain = new ArrayList<FlightBackup>();
		FlightBackup f101 = createFlight(101, "ORF", "EWR");
		flightChain.add(f101);
		f101.setDepartureTime(UtilsBackup.stringFormatToTime2("26/06/2017 00:00:00"));
		f101.setArrivalTime(UtilsBackup.stringFormatToTime2("26/06/2017 05:30:00"));

		FlightBackup f102 = createFlight(102, "EWR", "STL");
		flightChain.add(f102);
		f102.setDepartureTime(UtilsBackup.stringFormatToTime2("26/06/2017 06:20:00"));
		f102.setArrivalTime(UtilsBackup.stringFormatToTime2("26/06/2017 14:00:00"));
		System.out.println(UtilsBackup.timeFormatToString2(f102.getArrivalTime()));

		FlightBackup f103 = createFlight(103, "STL", "CLE");
		flightChain.add(f103);
		f103.setDepartureTime(UtilsBackup.stringFormatToTime2("26/06/2017 15:30:00"));
		f103.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 05:00:00"));

		FlightBackup f104 = createFlight(104, "CLE", "BDL");
		f104.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
		f104.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
		flightChain.add(f104);

		FlightBackup f105 = createFlight(105, "BDL", "CLE");
		f105.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 18:30:00"));
		f105.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 22:10:00"));
		flightChain.add(f105);

		air1.setFlightChain(flightChain);
		air1.setId("1");
		air1.setType("1");
		for (FlightBackup aFlight : flightChain) {
			aFlight.setAssignedAir(air1);
			aFlight.setPlannedFlight(aFlight.clone());
			aFlight.setPlannedAir(air1.clone());
		}

		System.out.println("f101 Departure:" + UtilsBackup.timeFormatToString2(f101.getPlannedFlight().getDepartureTime()));
		System.out.println("f101 Arrival:" + UtilsBackup.timeFormatToString2(f101.getPlannedFlight().getArrivalTime()));
		long f101FlightTime = f101.getPlannedFlight().getArrivalTime().getTime()
				- f101.getPlannedFlight().getDepartureTime().getTime();
		System.out.println("f101 flight time:" + f101FlightTime);
		// normal

		try {
			air1.adjustFlightTime(0);
			for (FlightBackup aFlight : flightChain) {
				assertEquals(aFlight.getPlannedFlight().getArrivalTime(), aFlight.getArrivalTime());
				assertEquals(aFlight.getPlannedFlight().getDepartureTime(), aFlight.getDepartureTime());
			}
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// f101 delay 90 min, recalucate
		f101.setDepartureTime(UtilsBackup.stringFormatToTime2("26/06/2017 01:30:00"));
		try {
			air1.adjustFlightTime(0);
			assertEquals(UtilsBackup.addMinutes(f101.getPlannedFlight().getArrivalTime(), 90), f101.getArrivalTime());
			System.out.println("f101 Actual Depart:" + UtilsBackup.timeFormatToString2(f101.getDepartureTime()));
			System.out.println("f101 Actual Arr:" + UtilsBackup.timeFormatToString2(f101.getArrivalTime()));

			assertEquals(UtilsBackup.addMinutes(f101.getArrivalTime(), 50), f102.getDepartureTime());
			assertEquals(UtilsBackup.addMinutes(f102.getPlannedFlight().getArrivalTime(), 90), f102.getArrivalTime());
			System.out.println("f102 Actual Depart:" + UtilsBackup.timeFormatToString2(f102.getDepartureTime()));
			System.out.println("f102 Actual Arr:" + UtilsBackup.timeFormatToString2(f102.getArrivalTime()));

			assertEquals(UtilsBackup.addMinutes(f102.getArrivalTime(), 50), f103.getDepartureTime());
			assertEquals(UtilsBackup.addMinutes(f103.getPlannedFlight().getArrivalTime(), 50), f103.getArrivalTime());
			System.out.println("f103 Actual Depart:" + UtilsBackup.timeFormatToString2(f103.getDepartureTime()));
			System.out.println("f103 Actual Arr:" + UtilsBackup.timeFormatToString2(f103.getArrivalTime()));

			assertEquals(f104.getPlannedFlight().getArrivalTime(), f104.getArrivalTime());
			assertEquals(f104.getPlannedFlight().getDepartureTime(), f104.getDepartureTime());
			System.out.println("f104 Actual Depart:" + UtilsBackup.timeFormatToString2(f104.getDepartureTime()));
			System.out.println("f104 Actual Arr:" + UtilsBackup.timeFormatToString2(f104.getArrivalTime()));

			assertEquals(f105.getPlannedFlight().getArrivalTime(), f105.getArrivalTime());
			assertEquals(f105.getPlannedFlight().getDepartureTime(), f105.getDepartureTime());
			System.out.println("f105 Actual Depart:" + UtilsBackup.timeFormatToString2(f105.getDepartureTime()));
			System.out.println("f105 Actual Arr:" + UtilsBackup.timeFormatToString2(f105.getArrivalTime()));

		} catch (AirportNotAcceptArrivalTimeBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// fall in airport event area
		System.out.println("fall in airport event area");
		flightChain = new ArrayList<FlightBackup>();
		f101 = createFlight(101, "ORF", "EWR");

		f101.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 00:00:00"));
		f101.setArrivalTime(UtilsBackup.stringFormatToTime2("06/05/2017 08:30:00"));
		flightChain.add(f101);

		f102 = createFlight(102, "EWR", "49");
		f102.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 11:30:00"));
		f102.setArrivalTime(UtilsBackup.stringFormatToTime2("06/05/2017 14:10:00"));
		flightChain.add(f102);

		f103 = createFlight(103, "49", "CLE");
		f103.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 16:30:00"));
		f103.setArrivalTime(UtilsBackup.stringFormatToTime2("07/05/2017 05:00:00"));
		flightChain.add(f103);

		f104 = createFlight(104, "CLE", "BDL");
		f104.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 06:50:00"));
		f104.setArrivalTime(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"));
		flightChain.add(f104);

		f105 = createFlight(105, "BDL", "CLE");
		f105.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 18:30:00"));
		f105.setArrivalTime(UtilsBackup.stringFormatToTime2("07/056/2017 22:10:00"));
		flightChain.add(f105);

		air1.setFlightChain(flightChain);
		for (FlightBackup aFlight : flightChain) {
			aFlight.setPlannedFlight(aFlight.clone());
			aFlight.setAssignedAir(air1);
		}

		try {
			air1.adjustFlightTime(0);
			fail("Airport 49 shall be not available");
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			System.out.println("Airport unable to allocate " + e.getaFlight().getSchdNo());
			System.out.println("Airport suggested arrival time "
					+ UtilsBackup.timeFormatToString2(e.getAvailableTime().getArrivalTime()));
			System.out.println("Airport suggested departure time "
					+ UtilsBackup.timeFormatToString2(e.getAvailableTime().getDepartureTime()));
			assertEquals(102, e.getaFlight().getSchdNo());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(UtilsBackup.addMinutes(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), 50),
					e.getAvailableTime().getDepartureTime());

		}

		// departure fall in airport event area
		System.out.println("departure fall in airport event area");
		flightChain = new ArrayList<FlightBackup>();
		f101 = createFlight(101, "ORF", "EWR");

		f101.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 00:00:00"));
		f101.setArrivalTime(UtilsBackup.stringFormatToTime2("06/05/2017 08:30:00"));
		flightChain.add(f101);

		f102 = createFlight(102, "EWR", "49");
		f102.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 11:30:00"));
		f102.setArrivalTime(UtilsBackup.stringFormatToTime2("06/05/2017 13:45:00"));
		flightChain.add(f102);

		f103 = createFlight(103, "49", "CLE");
		f103.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 16:30:00"));
		f103.setArrivalTime(UtilsBackup.stringFormatToTime2("07/05/2017 05:00:00"));
		flightChain.add(f103);

		f104 = createFlight(104, "CLE", "BDL");
		f104.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 06:50:00"));
		f104.setArrivalTime(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"));
		flightChain.add(f104);

		f105 = createFlight(105, "BDL", "CLE");
		f105.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 18:30:00"));
		f105.setArrivalTime(UtilsBackup.stringFormatToTime2("07/056/2017 22:10:00"));
		flightChain.add(f105);

		air1.setFlightChain(flightChain);
		for (FlightBackup aFlight : flightChain) {
			aFlight.setAssignedAir(air1);
			aFlight.setPlannedFlight(aFlight.clone());
		}

		try {
			air1.adjustFlightTime(0);
			assertEquals(UtilsBackup.stringFormatToTime2("06/05/2017 16:00:00"), f103.getDepartureTime());
			System.out.println("f101 Actual Depart:" + UtilsBackup.timeFormatToString2(f101.getDepartureTime()) + " port: "
					+ f101.getSourceAirPort().getId());
			System.out.println("f101 Actual Arr:" + UtilsBackup.timeFormatToString2(f101.getArrivalTime()) + " port: "
					+ f101.getDesintationAirport().getId());

			System.out.println("f102 Actual Depart:" + UtilsBackup.timeFormatToString2(f102.getDepartureTime()) + " port: "
					+ f102.getSourceAirPort().getId());
			System.out.println("f102 Actual Arr:" + UtilsBackup.timeFormatToString2(f102.getArrivalTime()) + " port: "
					+ f102.getDesintationAirport().getId());

			System.out.println("f103 Actual Depart:" + UtilsBackup.timeFormatToString2(f103.getDepartureTime()) + " port: "
					+ f103.getSourceAirPort().getId());
			System.out.println("f103 Actual Arr:" + UtilsBackup.timeFormatToString2(f103.getArrivalTime()) + " port: "
					+ f103.getDesintationAirport().getId());

			System.out.println("f104 Actual Depart:" + UtilsBackup.timeFormatToString2(f104.getDepartureTime()) + " port: "
					+ f104.getSourceAirPort().getId());
			System.out.println("f104 Actual Arr:" + UtilsBackup.timeFormatToString2(f104.getArrivalTime()) + " port: "
					+ f104.getDesintationAirport().getId());

			System.out.println("f104 Actual Depart:" + UtilsBackup.timeFormatToString2(f105.getDepartureTime()) + " port: "
					+ f105.getSourceAirPort().getId());
			System.out.println("f104 Actual Arr:" + UtilsBackup.timeFormatToString2(f105.getArrivalTime()) + " port: "
					+ f105.getDesintationAirport().getId());
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			fail("Airport 49 issue shall be arranged");

		}

		// departure fall in airport event area, delay too earlier
		System.out.println("departure fall in airport event area, delay too earlier");
		flightChain = new ArrayList<FlightBackup>();
		f101 = createFlight(101, "ORF", "EWR");

		f101.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 00:00:00"));
		f101.setArrivalTime(UtilsBackup.stringFormatToTime2("06/05/2017 08:30:00"));
		flightChain.add(f101);

		f102 = createFlight(102, "EWR", "49");
		f102.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 11:30:00"));
		f102.setArrivalTime(UtilsBackup.stringFormatToTime2("06/05/2017 13:45:00"));
		flightChain.add(f102);

		f103 = createFlight(103, "49", "CLE");
		f103.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 16:01:00"));
		f103.setArrivalTime(UtilsBackup.stringFormatToTime2("07/05/2017 18:00:00"));
		f103.setInternationalFlight(false);
		flightChain.add(f103);

		f104 = createFlight(104, "CLE", "BDL");
		f104.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 06:50:00"));
		f104.setArrivalTime(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"));
		flightChain.add(f104);

		f105 = createFlight(105, "BDL", "CLE");
		f105.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 18:30:00"));
		f105.setArrivalTime(UtilsBackup.stringFormatToTime2("07/056/2017 22:10:00"));
		flightChain.add(f105);

		air1.setFlightChain(flightChain);
		for (FlightBackup aFlight : flightChain) {
			aFlight.setAssignedAir(air1);
			aFlight.setPlannedFlight(aFlight.clone());
		}

		try {
			air1.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			fail("Airport 49 issue shall be arranged");

		} catch (AirportNotAcceptDepartureTimeBackup e) {
			System.out.println("Airport unable to allocate " + e.getaFlight().getSchdNo());
			System.out.println("Airport suggested arrival time "
					+ UtilsBackup.timeFormatToString2(e.getAvailableTime().getArrivalTime()));
			System.out.println("Airport suggested departure time "
					+ UtilsBackup.timeFormatToString2(e.getAvailableTime().getDepartureTime()));
			assertEquals(103, e.getaFlight().getSchdNo());
			assertEquals(UtilsBackup.stringFormatToTime2("06/05/2017 13:45:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(UtilsBackup.stringFormatToTime2("06/05/2017 16:00:00"), e.getAvailableTime().getDepartureTime());

		}

		// departure fall in airport event area, international flight
		System.out.println("departure fall in airport event area, international flight");
		flightChain = new ArrayList<FlightBackup>();
		f101 = createFlight(101, "ORF", "EWR");

		f101.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 00:00:00"));
		f101.setArrivalTime(UtilsBackup.stringFormatToTime2("06/05/2017 08:30:00"));
		flightChain.add(f101);

		f102 = createFlight(102, "EWR", "49");
		f102.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 11:30:00"));
		f102.setArrivalTime(UtilsBackup.stringFormatToTime2("06/05/2017 13:45:00"));
		flightChain.add(f102);

		f103 = createFlight(103, "49", "CLE");
		f103.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 16:01:00"));
		f103.setArrivalTime(UtilsBackup.stringFormatToTime2("07/05/2017 18:00:00"));
		f103.setInternationalFlight(true);
		flightChain.add(f103);

		f104 = createFlight(104, "CLE", "BDL");
		f104.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 06:50:00"));
		f104.setArrivalTime(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"));
		flightChain.add(f104);

		f105 = createFlight(105, "BDL", "CLE");
		f105.setDepartureTime(UtilsBackup.stringFormatToTime2("07/05/2017 18:30:00"));
		f105.setArrivalTime(UtilsBackup.stringFormatToTime2("07/056/2017 22:10:00"));
		flightChain.add(f105);

		air1.setFlightChain(flightChain);
		for (FlightBackup aFlight : flightChain) {
			aFlight.setAssignedAir(air1);
			aFlight.setPlannedFlight(aFlight.clone());
		}

		try {
			air1.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			fail("Airport 49 issue shall be arranged");

		} catch (AirportNotAcceptDepartureTimeBackup e) {
			System.out.println("Airport unable to allocate " + e.getaFlight().getSchdNo());
			assertEquals(103, e.getaFlight().getSchdNo());
			assertEquals("Departure Earlier Not Allowed For International", e.getCasue());

		}

		// departure fall in airport regular area, departure delay too long
		System.out.println("departure fall in airport event area, departure delay");
		flightChain = new ArrayList<FlightBackup>();
		f101 = createFlight(101, "ORF", "EWR");

		f101.setDepartureTime(UtilsBackup.stringFormatToTime2("15/05/2017 00:00:00"));
		f101.setArrivalTime(UtilsBackup.stringFormatToTime2("15/05/2017 08:30:00"));
		flightChain.add(f101);

		f102 = createFlight(102, "EWR", "49");
		f102.setDepartureTime(UtilsBackup.stringFormatToTime2("15/05/2017 11:30:00"));
		f102.setArrivalTime(UtilsBackup.stringFormatToTime2("16/05/2017 00:00:00"));
		flightChain.add(f102);

		f103 = createFlight(103, "49", "CLE");
		f103.setDepartureTime(UtilsBackup.stringFormatToTime2("17/05/2017 01:01:00"));
		f103.setArrivalTime(UtilsBackup.stringFormatToTime2("17/05/2017 18:00:00"));
		f103.setInternationalFlight(false);
		flightChain.add(f103);

		f104 = createFlight(104, "CLE", "BDL");
		f104.setDepartureTime(UtilsBackup.stringFormatToTime2("18/05/2017 06:50:00"));
		f104.setArrivalTime(UtilsBackup.stringFormatToTime2("18/05/2017 17:00:00"));
		flightChain.add(f104);

		f105 = createFlight(105, "BDL", "CLE");
		f105.setDepartureTime(UtilsBackup.stringFormatToTime2("18/05/2017 18:30:00"));
		f105.setArrivalTime(UtilsBackup.stringFormatToTime2("18/056/2017 22:10:00"));
		flightChain.add(f105);

		air1.setFlightChain(flightChain);
		for (FlightBackup aFlight : flightChain) {
			aFlight.setAssignedAir(air1);
			aFlight.setPlannedFlight(aFlight.clone());
		}
		f103.getPlannedFlight().setDepartureTime(UtilsBackup.stringFormatToTime2("16/05/2017 06:00:00"));
		assertEquals(UtilsBackup.stringFormatToTime2("16/05/2017 06:00:00"), f103.getPlannedFlight().getDepartureTime());

		try {
			air1.adjustFlightTime(0);
			assertEquals(UtilsBackup.stringFormatToTime2("16/05/2017 06:10:00"), f103.getDepartureTime());
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			fail("Airport 49 issue shall be arranged");

		} catch (AirportNotAcceptDepartureTimeBackup e) {
			fail("Airport 49 issue shall be arranged");

		} catch (AirportNotAvailableBackup e) {
			System.out.println("Airport unable to allocate " + e.getaFlight().getSchdNo());
			assertEquals(102, e.getaFlight().getSchdNo());
			assertEquals(UtilsBackup.stringFormatToTime2("17/05/2017 06:10:00"), e.getAvailableTime().getDepartureTime());
		}

		// departure fall in airport regular area, departure delay acceptable
		System.out.println("departure fall in airport event area, departure delay");
		flightChain = new ArrayList<FlightBackup>();
		f101 = createFlight(101, "ORF", "EWR");

		f101.setDepartureTime(UtilsBackup.stringFormatToTime2("16/05/2017 00:00:00"));
		f101.setArrivalTime(UtilsBackup.stringFormatToTime2("16/05/2017 08:30:00"));
		flightChain.add(f101);

		f102 = createFlight(102, "EWR", "49");
		f102.setDepartureTime(UtilsBackup.stringFormatToTime2("16/05/2017 11:30:00"));
		f102.setArrivalTime(UtilsBackup.stringFormatToTime2("17/05/2017 00:00:00"));
		flightChain.add(f102);

		f103 = createFlight(103, "49", "CLE");
		f103.setDepartureTime(UtilsBackup.stringFormatToTime2("17/05/2017 01:01:00"));
		f103.setArrivalTime(UtilsBackup.stringFormatToTime2("17/05/2017 18:01:00"));
		f103.setInternationalFlight(false);
		flightChain.add(f103);
		
		f104 = createFlight(104, "CLE", "BDL");
		f104.setDepartureTime(UtilsBackup.stringFormatToTime2("18/05/2017 06:50:00"));
		f104.setArrivalTime(UtilsBackup.stringFormatToTime2("18/05/2017 17:00:00"));
		flightChain.add(f104);

		f105 = createFlight(105, "BDL", "CLE");
		f105.setDepartureTime(UtilsBackup.stringFormatToTime2("18/05/2017 18:30:00"));
		f105.setArrivalTime(UtilsBackup.stringFormatToTime2("18/05/2017 22:10:00"));
		flightChain.add(f105);

		air1.setFlightChain(flightChain);
		for (FlightBackup aFlight : flightChain) {
			aFlight.setAssignedAir(air1);
			aFlight.setPlannedFlight(aFlight.clone());
		}
	

		try {
			air1.adjustFlightTime(0);
			assertEquals(UtilsBackup.stringFormatToTime2("17/05/2017 06:10:00"), f103.getDepartureTime());
			assertEquals(UtilsBackup.stringFormatToTime2("17/05/2017 23:10:00"), f103.getArrivalTime());
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			fail("Airport 49 issue shall be arranged");

		} catch (AirportNotAcceptDepartureTimeBackup e) {
			fail("Airport 49 issue shall be arranged");

		} catch (AirportNotAvailableBackup e) {
			fail("Airport 49 issue shall be arranged");
		}

		// change flight destination for port 49
		System.out.println("f102 directly fly from EWR to CLE");
		f102.setDesintationAirport(InitDataBackup.airportList.getAirport("CLE"));
		air1.getFlightChain().remove(f103);

		try {
			air1.adjustFlightTime(0);
			assertEquals(false, air1.hasFlight(f103));
			assertEquals(UtilsBackup.addMinutes(f102.getDepartureTime(), 120), f102.getArrivalTime());
			System.out.println("f101 Actual Depart:" + UtilsBackup.timeFormatToString2(f101.getDepartureTime()) + " port: "
					+ f101.getSourceAirPort().getId());
			System.out.println("f101 Actual Arr:" + UtilsBackup.timeFormatToString2(f101.getArrivalTime()) + " port: "
					+ f101.getDesintationAirport().getId());

			System.out.println("f102 Actual Depart:" + UtilsBackup.timeFormatToString2(f102.getDepartureTime()) + " port: "
					+ f102.getSourceAirPort().getId());
			System.out.println("f102 Actual Arr:" + UtilsBackup.timeFormatToString2(f102.getArrivalTime()) + " port: "
					+ f102.getDesintationAirport().getId());

			System.out.println("f104 Actual Depart:" + UtilsBackup.timeFormatToString2(f104.getDepartureTime()) + " port: "
					+ f104.getSourceAirPort().getId());
			System.out.println("f104 Actual Arr:" + UtilsBackup.timeFormatToString2(f104.getArrivalTime()) + " port: "
					+ f104.getDesintationAirport().getId());

			System.out.println("f104 Actual Depart:" + UtilsBackup.timeFormatToString2(f105.getDepartureTime()) + " port: "
					+ f105.getSourceAirPort().getId());
			System.out.println("f104 Actual Arr:" + UtilsBackup.timeFormatToString2(f105.getArrivalTime()) + " port: "
					+ f105.getDesintationAirport().getId());

		} catch (AirportNotAcceptArrivalTimeBackup e) {
			fail("Airport 49 issue shall be arranged");

		}

		// short grounding time
		flightChain = new ArrayList<FlightBackup>();
		f101 = createFlight(101, "ORF", "EWR");
		flightChain.add(f101);
		f101.setDepartureTime(UtilsBackup.stringFormatToTime2("26/06/2017 00:00:00"));
		f101.setArrivalTime(UtilsBackup.stringFormatToTime2("26/06/2017 05:30:00"));

		f102 = createFlight(102, "EWR", "STL");
		flightChain.add(f102);
		f102.setDepartureTime(UtilsBackup.stringFormatToTime2("26/06/2017 06:20:00"));
		f102.setArrivalTime(UtilsBackup.stringFormatToTime2("26/06/2017 14:00:00"));

		f103 = createFlight(103, "STL", "CLE");
		flightChain.add(f103);
		f103.setDepartureTime(UtilsBackup.stringFormatToTime2("26/06/2017 15:30:00"));
		f103.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 05:00:00"));

		f104 = createFlight(104, "CLE", "BDL");
		f104.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 06:50:00"));
		f104.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:00:00"));
		flightChain.add(f104);

		f105 = createFlight(105, "BDL", "CLE");
		f105.setDepartureTime(UtilsBackup.stringFormatToTime2("27/06/2017 17:30:00"));
		f105.setArrivalTime(UtilsBackup.stringFormatToTime2("27/06/2017 22:10:00"));
		flightChain.add(f105);

		air1.setFlightChain(flightChain);
		air1.setId("1");
		air1.setType("1");
		for (FlightBackup aFlight : flightChain) {
			aFlight.setAssignedAir(air1);
			aFlight.setPlannedFlight(aFlight.clone());
			aFlight.setPlannedAir(air1.clone());
		}

		try {
			air1.adjustFlightTime(0);
			for (FlightBackup aFlight : flightChain) {
				assertEquals(aFlight.getPlannedFlight().getArrivalTime(), aFlight.getArrivalTime());
				assertEquals(aFlight.getPlannedFlight().getDepartureTime(), aFlight.getDepartureTime());
			}
			assertEquals(30, f105.getGroundingTime(104,105));
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		f102.setFlightId(102);
		f102.setDepartureTime(UtilsBackup.stringFormatToTime2("26/06/2017 06:20:00"));
		f102.setArrivalTime(UtilsBackup.stringFormatToTime2("26/06/2017 14:00:00"));
		f102.setPlannedFlight(f102.clone());
		f102.setDesintationAirport(InitDataBackup.airportList.getAirport("CLE"));
		air1.setType("2");
		f102.calcuateNextArrivalTime();
		assertEquals(UtilsBackup.stringFormatToTime2("26/06/2017 14:30:00"), f102.calcuateNextArrivalTime());

	}

}
