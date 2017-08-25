package xiaMengAirline.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.StartUp;
import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAcceptArrivalTime;
import xiaMengAirline.Exception.AirportNotAcceptDepartureTime;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.Exception.SolutionNotValid;
import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.AirPortClose;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.FlightTime;
import xiaMengAirline.beans.RegularAirPortClose;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.evaluator.Main;
import xiaMengAirline.searchEngine.BusinessDomain;
import xiaMengAirline.searchEngine.IterativeBatchMethod;
import xiaMengAirline.searchEngine.IterativeMethod;
import xiaMengAirline.searchEngine.OptimizerStragety;
import xiaMengAirline.searchEngine.SelfSearch;
import xiaMengAirline.searchEngine.backup.LocalSearch;
import xiaMengAirline.utils.InitData;
import xiaMengAirline.utils.Utils;

public class InitDataTest {

	@Before
	public void setUp() throws Exception {
		// Step1, Load all data & initialize
		File file=new File(".");
	    System.out.println("Current Working Directory: " + file.getAbsolutePath());
		String initDatafile = "XiahangData20170814.xlsx";
		InitData.initData(initDatafile);
	}

	@Test
	public void testInitData() throws ParseException, CloneNotSupportedException, FlightDurationNotFound, AirportNotAvailable, AircraftNotAdjustable, SolutionNotValid {

		Aircraft air50 = InitData.originalSolution.getAircraft("50", "2", false,false).clone();
		
		
		//check air
		assertEquals("50",air50.getId());
		assertEquals(false, air50.isCancel());
		
		AirPort port50 = InitData.airportList.getAirport("50");
		Date testdate = Utils.stringFormatToTime2("05/05/2017 07:36:00");
		assertEquals(true, BusinessDomain.checkAirportAvailablity(port50, testdate, true, true, false));
		testdate = Utils.stringFormatToTime2("06/05/2017 15:03:00");
		assertEquals(true, BusinessDomain.checkAirportAvailablity(port50, testdate, true, false, false));
		testdate = Utils.stringFormatToTime2("06/05/2017 15:04:00");
		assertEquals(true, BusinessDomain.checkAirportAvailablity(port50, testdate, true, false, false));
		testdate = Utils.stringFormatToTime2("06/05/2017 15:02:00");
		assertEquals(false, BusinessDomain.checkAirportAvailablity(port50, testdate, true, false, false));
		assertEquals(true, BusinessDomain.checkAirportAvailablity(port50, testdate, true, false, true));
		assertEquals(true, BusinessDomain.checkAirportAvailablity(port50, testdate, true, false, false));
		assertEquals(false, BusinessDomain.checkAirportAvailablity(port50, testdate, true, false, false));
		
		testdate = Utils.stringFormatToTime2("07/05/2017 18:53:00");
		assertEquals(true, BusinessDomain.checkAirportAvailablity(port50, testdate, true, false, false));
		testdate = Utils.stringFormatToTime2("07/05/2017 18:54:00");
		assertEquals(true, BusinessDomain.checkAirportAvailablity(port50, testdate, true, false, false));
		testdate = Utils.stringFormatToTime2("07/05/2017 18:52:00");
		assertEquals(false, BusinessDomain.checkAirportAvailablity(port50, testdate, true, false, false));
		
		testdate = Utils.stringFormatToTime2("07/05/2017 18:53:00");
		assertEquals(true, BusinessDomain.checkAirportAvailablity(port50, testdate, false, false, false));
		testdate = Utils.stringFormatToTime2("07/05/2017 18:54:00");
		assertEquals(true, BusinessDomain.checkAirportAvailablity(port50, testdate, false, false, false));
		testdate = Utils.stringFormatToTime2("07/05/2017 18:52:00");
		assertEquals(false, BusinessDomain.checkAirportAvailablity(port50, testdate, false, false, false));
		
		assertEquals(false, BusinessDomain.isTyphoon(port50, Utils.stringFormatToTime2("07/05/2017 18:52:00")));
		assertEquals(true, BusinessDomain.isTyphoon(port50, Utils.stringFormatToTime2("06/05/2017 18:52:00")));
		
		
		//check flight
		Flight f15 = air50.getFlightByFlightId(15);
		assertEquals(Utils.stringFormatToTime2("05/05/2017 07:30:00"), f15.getDepartureTime());
		assertEquals(Utils.stringFormatToTime2("05/05/2017  10:25:00"), f15.getArrivalTime());
		assertEquals(false, BusinessDomain.isValidEarlier(f15, Utils.stringFormatToTime2("05/05/2017 03:30:00"), false ));
		assertEquals(true, BusinessDomain.isValidEarlier(f15, Utils.stringFormatToTime2("05/05/2017 03:30:00"), true ));
		assertEquals(false, BusinessDomain.isValidEarlier(f15, Utils.stringFormatToTime2("05/05/2017 00:30:00"), true ));
		assertEquals(true, BusinessDomain.isValidDelay(f15, Utils.stringFormatToTime2("05/05/2017 17:30:00")));
		assertEquals(false, BusinessDomain.isValidDelay(f15, Utils.stringFormatToTime2("06/05/2017 07:31:00")));
		assertEquals(new BigDecimal(1.00).setScale(2, BigDecimal.ROUND_HALF_UP), f15.getImpCoe());
		assertEquals(false,f15.isInternationalFlight());
		assertEquals(349, f15.getSchdNo());
		assertEquals("50", f15.getPlannedAir().getId());
		assertEquals("50", f15.getAssignedAir().getId());
		assertEquals(15,f15.getPlannedFlight().getFlightId());
		assertEquals("50",f15.getSourceAirPort().getId());
		assertEquals("72", f15.getDesintationAirport().getId());
		assertEquals(135, f15.getNumberOfPassenger());
		assertEquals(false, f15.isAdjustable());
		
		Flight f1176 = air50.getFlightByFlightId(1176);
		assertEquals(true, f1176.isAdjustable());
		
		
		//joined flight
		Aircraft air122 = InitData.originalSolution.getAircraft("122", "2", false,false).clone();
		Flight f1918 = air122.getFlightByFlightId(1918);
		assertEquals(26, f1918.getNumberOfJoinedPassenger());
		assertEquals(183, air122.getNumberOfSeats());
		int anotherF = InitData.jointFlightMap.get(f1918.getFlightId()).getFlightId();
		assertEquals(1920, anotherF);
		Flight f1920 = air122.getFlightByFlightId(1920);
		assertEquals(null, InitData.jointFlightMap.get(f1920.getFlightId()));
		
		Aircraft air109 = InitData.originalSolution.getAircraft("109", "2", false,false).clone();
		Flight f325 = air109.getFlightByFlightId(325);
		assertEquals(null, InitData.jointFlightMap.get(f325.getFlightId()));
		assertEquals(false, BusinessDomain.isValidEarlier(f325, Utils.stringFormatToTime2("05/05/2017 09:00:00"), false ));
		assertEquals(false, BusinessDomain.isValidEarlier(f325, Utils.stringFormatToTime2("05/05/2017 09:30:00"), true ));
		assertEquals(false, BusinessDomain.isValidEarlier(f325, Utils.stringFormatToTime2("05/05/2017 00:30:00"), true ));
		assertEquals(true, BusinessDomain.isValidDelay(f325, Utils.stringFormatToTime2("06/05/2017 15:30:00")));
		assertEquals(false, BusinessDomain.isValidDelay(f325, Utils.stringFormatToTime2("07/05/2017 08:31:00")));
		
		//airport close
		AirPort port6 = InitData.airportList.getAirport("6");
		assertEquals(true, port6.isInternational());
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
		
		//grounding time
		Aircraft air134 = InitData.originalSolution.getAircraft("134", "2", false, false).clone();
		Flight f399 = air134.getFlightByFlightId(399);
		Flight f760 = air134.getFlightByFlightId(760);
		assertEquals(45, BusinessDomain.getGroundingTime(337,399));
		assertEquals(50, BusinessDomain.getGroundingTime(57,760));
		
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
		
		Aircraft air35 = InitData.originalSolution.getAircraft("35", "2", false, false).clone();
		Flight f817 = air35.getFlightByFlightId(817);
		Flight f610 = air35.getFlightByFlightId(610);
		Flight f1026 = air35.getFlightByFlightId(1026);
		air35.removeFlightChain(f610, f1026);
		f817.setDepartureTime(Utils.stringFormatToTime2("05/05/2017 18:00:00"));
		try {
			air35.adjustFlightTime(air35.getFlightChain().indexOf(f817));
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
			assertEquals(381, e.getaFlight().getFlightId());
			assertEquals(Utils.stringFormatToTime2("06/05/2017 16:00:00"), e.getAvailableTime().getDepartureTime());
		} catch (AirportNotAvailable e) {
			fail("shall not fail");
		}
		
		AirPort port49 = InitData.airportList.getAirport("49");
		FlightTime aReq = new FlightTime();
		aReq.setArrivalTime(Utils.stringFormatToTime2("05/05/2017 23:35:00"));
		aReq.setDepartureTime(Utils.stringFormatToTime2("06/05/2017 00:25:00"));
		aReq = port49.requestAirport(aReq, 50);
		assertEquals(Utils.stringFormatToTime2("06/05/2017 06:10:00"), aReq.getDepartureTime());
		
		
		
		
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
		
		
		
		
		Aircraft air111 = InitData.originalSolution.getAircraft("111", "2", false, false).clone();
		Flight f552 = air111.getFlightByFlightId(552);
		f552.setDepartureTime(Utils.stringFormatToTime2("08/05/2017 23:10:00"));
		try {
			air111.adjustFlightTime(air111.getFlightIndexByFlightId(552));
		} catch (AirportNotAcceptArrivalTime e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(Utils.stringFormatToTime2("09/05/2017 06:10:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(null, e.getAvailableTime().getDepartureTime());
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
		
		System.out.println("top list ...");
		List<Aircraft> airList = new ArrayList<Aircraft>(InitData.originalSolution.getSchedule().values());
		TreeMap<Integer, List<Aircraft>> topAirList = StartUp.searchTopList(airList);
		for (Map.Entry<Integer, List<Aircraft>> entry : topAirList.entrySet()) {
			int key = entry.getKey();
			List<Aircraft> value = entry.getValue();
			for (Aircraft air:value)
				System.out.println("Score " + key + " Air " + air.getId());

		}
		
		System.out.println("heavy list ...");
		airList = new ArrayList<Aircraft>(InitData.originalSolution.getSchedule().values());
		topAirList = StartUp.searchHeavyList(airList);
		for (Map.Entry<Integer, List<Aircraft>> entry : topAirList.entrySet()) {
			int key = entry.getKey();
			List<Aircraft> value = entry.getValue();
			for (Aircraft air:value)
				System.out.println("Score " + key + " Air " + air.getId());

		}
		
		Aircraft air91 = InitData.originalSolution.getAircraft("91", "2", false, false).clone();
		try {
			air91.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTime e) {
			System.out.println("AirportNotAcceptArrivalTim e" + e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTime e) {
			System.out.println("AirportNotAcceptArrivalTime " + e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailable e) {
			fail("shall not fail");
		}
		
		Aircraft air3 = InitData.originalSolution.getAircraft("3", "2", false, false).clone();
		Flight f386 = air3.getFlightByFlightId(386);
		f386.setDepartureTime(Utils.stringFormatToTime2("05/05/2017 09:20:00"));
		air3.getFlightChain().remove(1);
		air3.getFlightChain().remove(1);
		try {
			air3.adjustFlightTime(air3.getFlightChain().indexOf(f386));
		} catch (AirportNotAcceptArrivalTime e) {
			System.out.println("AirportNotAcceptArrivalTim e" + e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(Utils.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTime e) {
			System.out.println("AirportNotAcceptArrivalTime " + e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
		} catch (AirportNotAvailable e) {
			fail("shall not fail");
		}
		
		Aircraft air90 = InitData.originalSolution.getAircraft("90", "2", false, false).clone();
		Flight f45 = air90.getFlightByFlightId(45);
		boolean isJoined = InitData.jointFlightMap.containsKey(f45.getFlightId());
		assertEquals(true, isJoined);
		assertEquals(65, InitData.jointFlightMap.get(f45.getFlightId()).getFlightId());
		
		Aircraft air79 = InitData.originalSolution.getAircraft("79", "2", false, false).clone();
		Aircraft air79c = InitData.originalSolution.getAircraft("79", "2", true, true).clone();
		XiaMengAirlineSolution aSol = new XiaMengAirlineSolution();
		aSol.replaceOrAddNewAircraft(air79);
		Flight f1594 = air79.getFlightByFlightId(1594);
		Flight f1605 = air79.getFlightByFlightId(1605);
		air79c.getFlightChain().add(f1605);
		air79c.getFlightChain().add(f1594);
		air79.getFlightChain().remove(f1605);
		air79.getFlightChain().remove(f1594);
		
		XiaMengAirlineSolution aNewSolution = InitData.originalSolution.clone();
		OptimizerStragety aStragety = new OptimizerStragety ();
		aStragety.setBatchSize(50);
		IterativeMethod aBatchDriver = new IterativeBatchMethod();
		aBatchDriver.setupIterationStragety(aStragety);
		aBatchDriver.setupIterationContent(aNewSolution);
		assertEquals(3, aBatchDriver.getNumberOfBatches());
		assertEquals(50, aBatchDriver.getNextDriveForIterative().size());
		assertEquals(1, aBatchDriver.getCurrentIterationNumber());
		assertEquals(50, aBatchDriver.getNextDriveForIterative().size());
		assertEquals(2, aBatchDriver.getCurrentIterationNumber());
		assertEquals(43, aBatchDriver.getNextDriveForIterative().size());
		assertEquals(3, aBatchDriver.getCurrentIterationNumber());
		assertEquals(null, aBatchDriver.getNextDriveForIterative());
		
		
		
		
		fail("stop");
		XiaMengAirlineSolution aBSol = aSol.getBestSolution();
		
		
		
		
		Main main2 = new Main();
		main2.evalutor("dataforest_985118.3490_d.csv");
		


		SelfSearch selfEngine = new SelfSearch();
		XiaMengAirlineSolution sol105 = selfEngine.constructInitialSolution(aTest);
		XiaMengAirlineSolution initial105 = sol105.reConstruct();
		initial105.refreshCost(true);
		assertEquals(15775, initial105.getCost().longValue());
		
		
		
		
		selfEngine = new SelfSearch();
		
		
		
		//Step2, construct initial solution & validate it
		XiaMengAirlineSolution initialSolution = selfEngine.constructInitialSolution(InitData.originalSolution.clone());
		XiaMengAirlineSolution initialOutput = initialSolution.reConstruct();
		initialOutput.refreshCost(true);
		assertEquals(1514185, initialOutput.getCost().longValue());
		
		initialOutput.generateOutput("0");
		Main main = new Main();
		main.evalutor("数据森林_"+initialOutput.getStrCost()+"_0.csv");
		
				
		Aircraft air94 = initialSolution.getAircraft("94", "2", false, false).clone();
		Aircraft air94C = initialSolution.getAircraft("94", "2", true, false).clone();
		
		XiaMengAirlineSolution aTestSol = new XiaMengAirlineSolution();
		aTestSol.replaceOrAddNewAircraft(air94);
		aTestSol.replaceOrAddNewAircraft(air94C);
		XiaMengAirlineSolution aTestOut = aTestSol.reConstruct();
		aTestOut.refreshCost(false);
		assertEquals(14927, aTestOut.getCost().longValue());
		
		aTestSol.validflightNumers(aTestSol);
		

		
		fail("stop");
		LocalSearch localEngine = new LocalSearch();
		List<Aircraft> testAirList = new ArrayList<Aircraft> ();
		testAirList.add(air94);
		localEngine.buildSolution(testAirList, initialSolution);
		
		
		
		Flight f1156 = air94C.getFlightByFlightId(1156);
		assertEquals("72", f1156.getSourceAirPort().getId());
		assertEquals("49", f1156.getDesintationAirport().getId());
		assertEquals(Utils.stringFormatToTime2("06/05/2017 11:35:00"), f1156.getDepartureTime());
		assertEquals(Utils.stringFormatToTime2("06/05/2017 14:20:00"), f1156.getArrivalTime());
		
		
		//test local search
		
		XiaMengAirlineSolution sol133 = new XiaMengAirlineSolution();
		Aircraft air133 = initialSolution.getAircraft("133", "2", false, false);
		air94 = initialSolution.getAircraft("94", "2", false, false);
		sol133.replaceOrAddNewAircraft(air133);
		sol133.replaceOrAddNewAircraft(air94);

		
		XiaMengAirlineSolution sol133Out = sol133.reConstruct();
		sol133Out.refreshCost(false);
		System.out.println("So133 init: " + sol133Out.getCost());
		XiaMengAirlineSolution aBetterSolution = localEngine.constructNewSolution(sol133);
		XiaMengAirlineSolution aBetterOutput = aBetterSolution.reConstruct();
		aBetterOutput.refreshCost(true);
		System.out.println("So133 after: " + aBetterOutput.getCost());
//		aBetterOutput.generateOutput("5");
		
		System.out.println("Initial cost " + initialSolution.getCost());
		long startTime=System.currentTimeMillis();
		aBetterSolution = localEngine.constructNewSolution(initialSolution);
		System.out.println("Iter1, Current cost " + aBetterSolution.getCost());
		aBetterSolution = localEngine.constructNewSolution(aBetterSolution);
		System.out.println("Iter2, Current cost " + aBetterSolution.getCost());
		aBetterSolution = localEngine.constructNewSolution(aBetterSolution);
//		System.out.println("Iter3, Current cost " + aBetterSolution.getCost());
//		aBetterSolution = localEngine.constructNewSolution(aBetterSolution);
//		System.out.println("Iter4, Current cost " + aBetterSolution.getCost());
//		aBetterSolution = localEngine.constructNewSolution(aBetterSolution);
//		System.out.println("Iter5, Current cost " + aBetterSolution.getCost());
		long endTime=System.currentTimeMillis();
		long mins = (endTime - startTime)/(1000* 60);
		System.out.println("Consumed ... " + mins);
		aBetterOutput = aBetterSolution.reConstruct();
		aBetterOutput.refreshCost(true);
		aBetterOutput.generateOutput("b");
		main = new Main();
		main.evalutor("数据森林_"+aBetterOutput.getStrCost()+"_b.csv");
	}
	

}
