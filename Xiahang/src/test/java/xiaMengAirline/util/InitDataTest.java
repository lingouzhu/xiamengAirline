package xiaMengAirline.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAcceptArrivalTime;
import xiaMengAirline.Exception.AirportNotAcceptDepartureTime;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.AirPortClose;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.RegularAirPortClose;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.searchEngine.SelfSearch;

public class InitDataTest {

	@Before
	public void setUp() throws Exception {
		// Step1, Load all data & initialize
		File file=new File(".");
	    System.out.println("Current Working Directory: " + file.getAbsolutePath());
		String initDatafile = "XiahangData.xlsx";
		InitData.initData(initDatafile);
	}

	@Test
	public void testInitData() throws ParseException, CloneNotSupportedException, FlightDurationNotFound, AirportNotAvailable, AircraftNotAdjustable {

		Aircraft air50 = InitData.originalSolution.getAircraft("50", "2", false,false).clone();
		
		
		//check air
		assertEquals("50",air50.getId());
		assertEquals(false, air50.isCancel());
		
		//check flight
		Flight f15 = air50.getFlightByFlightId(15);
		assertEquals(Utils.stringFormatToTime2("05/05/2017 07:30:00"), f15.getDepartureTime());
		assertEquals(Utils.stringFormatToTime2("05/05/2017  10:25:00"), f15.getArrivalTime());
		assertEquals(new BigDecimal(1.74).setScale(2, BigDecimal.ROUND_HALF_UP), f15.getImpCoe());
		assertEquals(false,f15.isInternationalFlight());
		assertEquals(349, f15.getSchdNo());
		assertEquals("50", f15.getPlannedAir().getId());
		assertEquals("50", f15.getAssignedAir().getId());
		assertEquals(15,f15.getPlannedFlight().getFlightId());
		assertEquals("50",f15.getSourceAirPort().getId());
		assertEquals("72", f15.getDesintationAirport().getId());
		
		//joined flight
		Aircraft air122 = InitData.originalSolution.getAircraft("122", "2", false,false).clone();
		Flight f1918 = air122.getFlightByFlightId(1918);
		int anotherF = InitData.jointFlightMap.get(f1918.getFlightId()).getFlightId();
		assertEquals(1920, anotherF);
		Flight f1920 = air122.getFlightByFlightId(1920);
		assertEquals(null, InitData.jointFlightMap.get(f1920.getFlightId()));
		
		Aircraft air109 = InitData.originalSolution.getAircraft("109", "2", false,false).clone();
		Flight f325 = air109.getFlightByFlightId(325);
		assertEquals(null, InitData.jointFlightMap.get(f325.getFlightId()));
		
		//airport close
		AirPort port6 = InitData.airportList.getAirport("6");
		List<RegularAirPortClose> regClose = port6.getRegularCloseSchedule();
		assertEquals("00:00:00", regClose.get(0).getCloseTime());
		assertEquals("06:00:00", regClose.get(0).getOpenTime());
		assertEquals("2014-01-01", regClose.get(0).getCloseDate());
		assertEquals("2017-12-31", regClose.get(0).getOpenDate());
		assertEquals("23:00:00", regClose.get(1).getCloseTime());
		assertEquals("23:59:00", regClose.get(1).getOpenTime());
		assertEquals("2014-01-01", regClose.get(1).getCloseDate());
		assertEquals("2017-12-31", regClose.get(1).getOpenDate());
		
		//taifeng
		String airPortId = "49";
		AirPort aAirport = InitData.airportList.getAirport(airPortId);
		
		List<AirPortClose> taiFengList = aAirport.getCloseSchedule();
		
		assertEquals(Utils.stringFormatToTime2("06/05/2017 14:00:00"), taiFengList.get(0).getStartTime());
		assertEquals(Utils.stringFormatToTime2("07/05/2017 17:00:00"), taiFengList.get(0).getEndTime());
		assertEquals(false, taiFengList.get(0).isAllowForLanding());
		assertEquals(true, taiFengList.get(0).isAllowForTakeoff());
		
		assertEquals(Utils.stringFormatToTime2("06/05/2017 16:00:00"), taiFengList.get(1).getStartTime());
		assertEquals(Utils.stringFormatToTime2("07/05/2017 17:00:00"), taiFengList.get(1).getEndTime());
		assertEquals(true, taiFengList.get(1).isAllowForLanding());
		assertEquals(false, taiFengList.get(1).isAllowForTakeoff());
		
		assertEquals(Utils.stringFormatToTime2("06/05/2017 16:00:00"), taiFengList.get(2).getStartTime());
		assertEquals(Utils.stringFormatToTime2("07/05/2017 17:00:00"), taiFengList.get(2).getEndTime());
		assertEquals(0, taiFengList.get(2).getMaximumParking());

		//flight duration
		String aKey = "4";
		aKey += "_50";
		aKey += "_5";
		assertEquals(95, InitData.fightDurationMap.get(aKey).intValue());
		
		//domestic airport
		assertEquals(true,InitData.domesticAirportList.contains("50") && InitData.domesticAirportList.contains("48"));
		
		assertEquals(false,InitData.domesticAirportList.contains("36") && InitData.domesticAirportList.contains("4"));
		
		Aircraft air116 = InitData.originalSolution.getAircraft("116", "2", false, false).clone();
		try {
			air116.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailable e) {
			fail("shall not fail");
		}
		
		
		Aircraft air93 = InitData.originalSolution.getAircraft("93", "2", false, false).clone();
		try {
			air93.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailable e) {
			fail("shall not fail");
		}
		
		Aircraft air5 = InitData.originalSolution.getAircraft("5", "2", false, false).clone();
		try {
			air5.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailable e) {
			fail("shall not fail");
		}
		
		Aircraft air12 = InitData.originalSolution.getAircraft("12", "2", false, false).clone();
		try {
			air12.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailable e) {
			fail("shall not fail");
		}
		
		Aircraft air16 = InitData.originalSolution.getAircraft("16", "2", false, false).clone();
		try {
			air16.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailable e) {
			fail("shall not fail");
		}
		
		Aircraft air105 = InitData.originalSolution.getAircraft("105", "2", false, false).clone();
		XiaMengAirlineSolution aTest = new XiaMengAirlineSolution();
		aTest.replaceOrAddNewAircraft(air105);
		
		
		try {
			air105.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailable e) {
			fail("shall not fail");
		}
		
		SelfSearch selfEngine = new SelfSearch(aTest);
		XiaMengAirlineSolution sol105 = selfEngine.constructInitialSolution();
		XiaMengAirlineSolution initial105 = sol105.reConstruct();
		initial105.refreshCost(true);
		assertEquals(15175, initial105.getCost().longValue());
		
		
		
		
		selfEngine = new SelfSearch(InitData.originalSolution.clone());
		
		
		
		//Step2, construct initial solution & validate it
		XiaMengAirlineSolution initialSolution = selfEngine.constructInitialSolution();
		XiaMengAirlineSolution initialOutput = initialSolution.reConstruct();
		initialOutput.refreshCost(true);
		assertEquals(1444294, initialOutput.getCost().longValue());
		Aircraft air94 = initialOutput.getAircraft("94", "2", false, false);
		Aircraft air94C = initialOutput.getAircraft("94", "2", true, false);
		Flight f1156 = air94C.getFlightByFlightId(1156);
		assertEquals("72", f1156.getSourceAirPort().getId());
		assertEquals("49", f1156.getDesintationAirport().getId());
		assertEquals(Utils.stringFormatToTime2("06/05/2017 11:35:00"), f1156.getDepartureTime());
		assertEquals(Utils.stringFormatToTime2("06/05/2017 14:20:00"), f1156.getArrivalTime());
		Flight f2375 = air94.getFlightByFlightId(2375);
		assertEquals("72", f2375.getSourceAirPort().getId());
		assertEquals("49", f2375.getDesintationAirport().getId());
		assertEquals(Utils.stringFormatToTime2("06/05/2017 11:15:00"), f2375.getDepartureTime());
		assertEquals(Utils.stringFormatToTime2("06/05/2017 13:50:00"), f2375.getArrivalTime());
//		
//		
//		//initialOutput.generateOutput("1");
//		
//		LocalSearch localEngine = new LocalSearch();
//		XiaMengAirlineSolution sol133 = new XiaMengAirlineSolution();
//		Aircraft air133 = initialSolution.getAircraft("133", "2", false, false);
//		sol133.replaceOrAddNewAircraft(air133);
//		sol133.replaceOrAddNewAircraft(air94);
//		
//		HashMap<Flight, List<Flight>> circuitFlightsAir1 = air133.getCircuitFlights();
//		
//		for (Map.Entry<Flight, List<Flight>> entry : circuitFlightsAir1.entrySet()) {
//			Flight key = entry.getKey();
//			List<Flight> value = entry.getValue();
//			
//			for (Flight aFlight : value) {
//				System.out.println("Flight " + key.getFlightId() + " => " + aFlight.getFlightId());
//			}
//		}
//		
//		Flight f1274 = air133.getFlightByFlightId(1274);
//		for (Flight destFlight : circuitFlightsAir1.get(f1274)) {
//			Aircraft newAircraft1 = air133.clone();
//			Aircraft cancelledAir = sol133.getAircraft(air133.getId(), air133.getType(), true, true).clone();
//
//			Flight sFlight = newAircraft1.getFlight(0);
//			Flight dFlight = newAircraft1
//					.getFlight(air133.getFlightChain().indexOf(destFlight));
//
//			cancelledAir.insertFlightChain(air133, f1274, destFlight,
//					cancelledAir.getFlight(cancelledAir.getFlightChain().size() - 1), false);
//			newAircraft1.removeFlightChain(sFlight, dFlight);
//			System.out.println("Move " );
//			for (int i=0;i <= air133.getFlightChain().indexOf(destFlight);i++) {
//				System.out.println(air133.getFlight(i).getFlightId());
//			}
//			System.out.println("Move End" );
//
//			List<Flight> updateList1 = newAircraft1.getFlightChain();
//			for (Flight aF : updateList1) {
//				System.out.println("Air  " + newAircraft1.getId() + " flight " + aF.getFlightId());
//			}
//			List<Flight> updateList2 = cancelledAir.getFlightChain();
//			for (Flight aF : updateList2) {
//				System.out.println(
//						"Air cancelled " + newAircraft1.getId() + " flight " + aF.getFlightId());
//			}
//		}
		
		//XiaMengAirlineSolution aBetterSolution = localEngine.constructNewSolution(sol133);
		
		//XiaMengAirlineSolution aBetterSolution = localEngine.constructNewSolution(initialSolution);
	}
	

}
