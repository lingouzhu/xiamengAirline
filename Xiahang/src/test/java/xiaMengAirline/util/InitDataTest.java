package xiaMengAirline.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.StartUp;
import xiaMengAirline.Exception.AircraftNotAdjustableBackup;
import xiaMengAirline.Exception.AirportNotAcceptArrivalTimeBackup;
import xiaMengAirline.Exception.AirportNotAcceptDepartureTimeBackup;
import xiaMengAirline.Exception.AirportNotAvailableBackup;
import xiaMengAirline.Exception.FlightDurationNotFoundBackup;
import xiaMengAirline.Exception.SolutionNotValidBackup;
import xiaMengAirline.beans.AirPortBackup;
import xiaMengAirline.beans.AirPortCloseBackup;
import xiaMengAirline.beans.AircraftBackup;
import xiaMengAirline.beans.FlightBackup;
import xiaMengAirline.beans.FlightTimeBackup;
import xiaMengAirline.beans.RegularAirPortCloseBackup;
import xiaMengAirline.beans.XiaMengAirlineSolutionBackup;
import xiaMengAirline.evaluator.aviation2017.Main;
import xiaMengAirline.searchEngine.LocalSearchBackup;
import xiaMengAirline.searchEngine.SelfSearchBackup;

public class InitDataTest {

	@Before
	public void setUp() throws Exception {
		// Step1, Load all data & initialize
		File file=new File(".");
	    System.out.println("Current Working Directory: " + file.getAbsolutePath());
		String initDatafile = "XiahangData.xlsx";
		InitDataBackup.initData(initDatafile);
	}

	@Test
	public void testInitData() throws ParseException, CloneNotSupportedException, FlightDurationNotFoundBackup, AirportNotAvailableBackup, AircraftNotAdjustableBackup, SolutionNotValidBackup {

		AircraftBackup air50 = InitDataBackup.originalSolution.getAircraft("50", "2", false,false).clone();
		
		
		//check air
		assertEquals("50",air50.getId());
		assertEquals(false, air50.isCancel());
		
		//check flight
		FlightBackup f15 = air50.getFlightByFlightId(15);
		assertEquals(UtilsBackup.stringFormatToTime2("05/05/2017 07:30:00"), f15.getDepartureTime());
		assertEquals(UtilsBackup.stringFormatToTime2("05/05/2017  10:25:00"), f15.getArrivalTime());
		assertEquals(new BigDecimal(1.74).setScale(2, BigDecimal.ROUND_HALF_UP), f15.getImpCoe());
		assertEquals(false,f15.isInternationalFlight());
		assertEquals(349, f15.getSchdNo());
		assertEquals("50", f15.getPlannedAir().getId());
		assertEquals("50", f15.getAssignedAir().getId());
		assertEquals(15,f15.getPlannedFlight().getFlightId());
		assertEquals("50",f15.getSourceAirPort().getId());
		assertEquals("72", f15.getDesintationAirport().getId());
		
		//joined flight
		AircraftBackup air122 = InitDataBackup.originalSolution.getAircraft("122", "2", false,false).clone();
		FlightBackup f1918 = air122.getFlightByFlightId(1918);
		int anotherF = InitDataBackup.jointFlightMap.get(f1918.getFlightId()).getFlightId();
		assertEquals(1920, anotherF);
		FlightBackup f1920 = air122.getFlightByFlightId(1920);
		assertEquals(null, InitDataBackup.jointFlightMap.get(f1920.getFlightId()));
		
		AircraftBackup air109 = InitDataBackup.originalSolution.getAircraft("109", "2", false,false).clone();
		FlightBackup f325 = air109.getFlightByFlightId(325);
		assertEquals(null, InitDataBackup.jointFlightMap.get(f325.getFlightId()));
		
		//airport close
		AirPortBackup port6 = InitDataBackup.airportList.getAirport("6");
		List<RegularAirPortCloseBackup> regClose = port6.getRegularCloseSchedule();
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
		AirPortBackup aAirport = InitDataBackup.airportList.getAirport(airPortId);
		
		List<AirPortCloseBackup> taiFengList = aAirport.getCloseSchedule();
		
		assertEquals(UtilsBackup.stringFormatToTime2("06/05/2017 14:00:00"), taiFengList.get(0).getStartTime());
		assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), taiFengList.get(0).getEndTime());
		assertEquals(false, taiFengList.get(0).isAllowForLanding());
		assertEquals(true, taiFengList.get(0).isAllowForTakeoff());
		
		assertEquals(UtilsBackup.stringFormatToTime2("06/05/2017 16:00:00"), taiFengList.get(1).getStartTime());
		assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), taiFengList.get(1).getEndTime());
		assertEquals(true, taiFengList.get(1).isAllowForLanding());
		assertEquals(false, taiFengList.get(1).isAllowForTakeoff());
		
		assertEquals(UtilsBackup.stringFormatToTime2("06/05/2017 16:00:00"), taiFengList.get(2).getStartTime());
		assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), taiFengList.get(2).getEndTime());
		assertEquals(0, taiFengList.get(2).getMaximumParking());

		//flight duration
		String aKey = "4";
		aKey += "_50";
		aKey += "_5";
		assertEquals(95, InitDataBackup.fightDurationMap.get(aKey).intValue());
		
		//domestic airport
		assertEquals(true,InitDataBackup.domesticAirportList.contains("50") && InitDataBackup.domesticAirportList.contains("48"));
		
		assertEquals(false,InitDataBackup.domesticAirportList.contains("36") && InitDataBackup.domesticAirportList.contains("4"));
		
		//grounding time
		AircraftBackup air134 = InitDataBackup.originalSolution.getAircraft("134", "2", false, false).clone();
		FlightBackup f399 = air134.getFlightByFlightId(399);
		FlightBackup f760 = air134.getFlightByFlightId(760);
		assertEquals(45, f399.getGroundingTime(337,399));
		assertEquals(50, f760.getGroundingTime(57,760));
		
		AircraftBackup air116 = InitDataBackup.originalSolution.getAircraft("116", "2", false, false).clone();
		try {
			air116.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFoundBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailableBackup e) {
			fail("shall not fail");
		}
		
		AircraftBackup air35 = InitDataBackup.originalSolution.getAircraft("35", "2", false, false).clone();
		FlightBackup f817 = air35.getFlightByFlightId(817);
		FlightBackup f610 = air35.getFlightByFlightId(610);
		FlightBackup f1026 = air35.getFlightByFlightId(1026);
		air35.removeFlightChain(f610, f1026);
		f817.setDepartureTime(UtilsBackup.stringFormatToTime2("05/05/2017 18:00:00"));
		try {
			air35.adjustFlightTime(air35.getFlightChain().indexOf(f817));
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFoundBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			assertEquals(381, e.getaFlight().getFlightId());
			assertEquals(UtilsBackup.stringFormatToTime2("06/05/2017 16:00:00"), e.getAvailableTime().getDepartureTime());
		} catch (AirportNotAvailableBackup e) {
			fail("shall not fail");
		}
		
		AirPortBackup port49 = InitDataBackup.airportList.getAirport("49");
		FlightTimeBackup aReq = new FlightTimeBackup();
		aReq.setArrivalTime(UtilsBackup.stringFormatToTime2("05/05/2017 23:35:00"));
		aReq.setDepartureTime(UtilsBackup.stringFormatToTime2("06/05/2017 00:25:00"));
		aReq = port49.requestAirport(aReq, 50);
		assertEquals(UtilsBackup.stringFormatToTime2("06/05/2017 06:10:00"), aReq.getDepartureTime());
		
		
		
		
		AircraftBackup air93 = InitDataBackup.originalSolution.getAircraft("93", "2", false, false).clone();
		try {
			air93.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFoundBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailableBackup e) {
			fail("shall not fail");
		}
		
		AircraftBackup air5 = InitDataBackup.originalSolution.getAircraft("5", "2", false, false).clone();
		try {
			air5.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFoundBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailableBackup e) {
			fail("shall not fail");
		}
		
		AircraftBackup air12 = InitDataBackup.originalSolution.getAircraft("12", "2", false, false).clone();
		try {
			air12.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFoundBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailableBackup e) {
			fail("shall not fail");
		}
		
		AircraftBackup air16 = InitDataBackup.originalSolution.getAircraft("16", "2", false, false).clone();
		try {
			air16.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFoundBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailableBackup e) {
			fail("shall not fail");
		}
		
		
		
		
		AircraftBackup air111 = InitDataBackup.originalSolution.getAircraft("111", "2", false, false).clone();
		FlightBackup f552 = air111.getFlightByFlightId(552);
		f552.setDepartureTime(UtilsBackup.stringFormatToTime2("08/05/2017 23:10:00"));
		try {
			air111.adjustFlightTime(air111.getFlightIndexByFlightId(552));
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(UtilsBackup.stringFormatToTime2("09/05/2017 06:10:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(null, e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFoundBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailableBackup e) {
			fail("shall not fail");
		}
		
		AircraftBackup air105 = InitDataBackup.originalSolution.getAircraft("105", "2", false, false).clone();
		XiaMengAirlineSolutionBackup aTest = new XiaMengAirlineSolutionBackup();
		aTest.replaceOrAddNewAircraft(air105);
		
		
		try {
			air105.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFoundBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTimeBackup e) {
			System.out.println(e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailableBackup e) {
			fail("shall not fail");
		}
		
		System.out.println("top list ...");
		List<AircraftBackup> airList = new ArrayList<AircraftBackup>(InitDataBackup.originalSolution.getSchedule().values());
		TreeMap<Integer, List<AircraftBackup>> topAirList = StartUp.searchTopList(airList);
		for (Map.Entry<Integer, List<AircraftBackup>> entry : topAirList.entrySet()) {
			int key = entry.getKey();
			List<AircraftBackup> value = entry.getValue();
			for (AircraftBackup air:value)
				System.out.println("Score " + key + " Air " + air.getId());

		}
		
		System.out.println("heavy list ...");
		airList = new ArrayList<AircraftBackup>(InitDataBackup.originalSolution.getSchedule().values());
		topAirList = StartUp.searchHeavyList(airList);
		for (Map.Entry<Integer, List<AircraftBackup>> entry : topAirList.entrySet()) {
			int key = entry.getKey();
			List<AircraftBackup> value = entry.getValue();
			for (AircraftBackup air:value)
				System.out.println("Score " + key + " Air " + air.getId());

		}
		
		AircraftBackup air91 = InitDataBackup.originalSolution.getAircraft("91", "2", false, false).clone();
		try {
			air91.adjustFlightTime(0);
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			System.out.println("AirportNotAcceptArrivalTim e" + e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFoundBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTimeBackup e) {
			System.out.println("AirportNotAcceptArrivalTime " + e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
			fail("shall not fail");
		} catch (AirportNotAvailableBackup e) {
			fail("shall not fail");
		}
		
		AircraftBackup air3 = InitDataBackup.originalSolution.getAircraft("3", "2", false, false).clone();
		FlightBackup f386 = air3.getFlightByFlightId(386);
		f386.setDepartureTime(UtilsBackup.stringFormatToTime2("05/05/2017 09:20:00"));
		air3.getFlightChain().remove(1);
		air3.getFlightChain().remove(1);
		try {
			air3.adjustFlightTime(air3.getFlightChain().indexOf(f386));
		} catch (AirportNotAcceptArrivalTimeBackup e) {
			System.out.println("AirportNotAcceptArrivalTim e" + e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:00:00"), e.getAvailableTime().getArrivalTime());
			assertEquals(UtilsBackup.stringFormatToTime2("07/05/2017 17:50:00"), e.getAvailableTime().getDepartureTime());
		} catch (FlightDurationNotFoundBackup e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AirportNotAcceptDepartureTimeBackup e) {
			System.out.println("AirportNotAcceptArrivalTime " + e.getaFlight().getFlightId() + " From " + e.getaFlight().getSourceAirPort().getId() 
					+ " To " + e.getaFlight().getDesintationAirport().getId()
					+ " Avaialble time " + e.getAvailableTime().getArrivalTime() +" " + e.getAvailableTime().getDepartureTime());
			System.out.println(e.getCasue());
		} catch (AirportNotAvailableBackup e) {
			fail("shall not fail");
		}
		
		AircraftBackup air90 = InitDataBackup.originalSolution.getAircraft("90", "2", false, false).clone();
		FlightBackup f45 = air90.getFlightByFlightId(45);
		boolean isJoined = InitDataBackup.jointFlightMap.containsKey(f45.getFlightId());
		assertEquals(true, isJoined);
		assertEquals(65, InitDataBackup.jointFlightMap.get(f45.getFlightId()).getFlightId());
		
		AircraftBackup air79 = InitDataBackup.originalSolution.getAircraft("79", "2", false, false).clone();
		AircraftBackup air79c = InitDataBackup.originalSolution.getAircraft("79", "2", true, true).clone();
		XiaMengAirlineSolutionBackup aSol = new XiaMengAirlineSolutionBackup();
		aSol.replaceOrAddNewAircraft(air79);
		FlightBackup f1594 = air79.getFlightByFlightId(1594);
		FlightBackup f1605 = air79.getFlightByFlightId(1605);
		air79c.getFlightChain().add(f1605);
		air79c.getFlightChain().add(f1594);
		air79.getFlightChain().remove(f1605);
		air79.getFlightChain().remove(f1594);
		
		XiaMengAirlineSolutionBackup aBSol = aSol.getBestSolution();
		
		
		fail("stop");
		
		Main main2 = new Main();
		main2.evalutor("dataforest_985118.3490_d.csv");
		


		SelfSearchBackup selfEngine = new SelfSearchBackup(aTest);
		XiaMengAirlineSolutionBackup sol105 = selfEngine.constructInitialSolution();
		XiaMengAirlineSolutionBackup initial105 = sol105.reConstruct();
		initial105.refreshCost(true);
		assertEquals(15775, initial105.getCost().longValue());
		
		
		
		
		selfEngine = new SelfSearchBackup(InitDataBackup.originalSolution.clone());
		
		
		
		//Step2, construct initial solution & validate it
		XiaMengAirlineSolutionBackup initialSolution = selfEngine.constructInitialSolution();
		XiaMengAirlineSolutionBackup initialOutput = initialSolution.reConstruct();
		initialOutput.refreshCost(true);
		assertEquals(1514185, initialOutput.getCost().longValue());
		
		initialOutput.generateOutput("0");
		Main main = new Main();
		main.evalutor("数据森林_"+initialOutput.getStrCost()+"_0.csv");
		
				
		AircraftBackup air94 = initialSolution.getAircraft("94", "2", false, false).clone();
		AircraftBackup air94C = initialSolution.getAircraft("94", "2", true, false).clone();
		
		XiaMengAirlineSolutionBackup aTestSol = new XiaMengAirlineSolutionBackup();
		aTestSol.replaceOrAddNewAircraft(air94);
		aTestSol.replaceOrAddNewAircraft(air94C);
		XiaMengAirlineSolutionBackup aTestOut = aTestSol.reConstruct();
		aTestOut.refreshCost(false);
		assertEquals(14927, aTestOut.getCost().longValue());
		
		aTestSol.validflightNumers(aTestSol);
		

		
		fail("stop");
		LocalSearchBackup localEngine = new LocalSearchBackup();
		List<AircraftBackup> testAirList = new ArrayList<AircraftBackup> ();
		testAirList.add(air94);
		localEngine.buildSolution(testAirList, initialSolution);
		
		
		
		FlightBackup f1156 = air94C.getFlightByFlightId(1156);
		assertEquals("72", f1156.getSourceAirPort().getId());
		assertEquals("49", f1156.getDesintationAirport().getId());
		assertEquals(UtilsBackup.stringFormatToTime2("06/05/2017 11:35:00"), f1156.getDepartureTime());
		assertEquals(UtilsBackup.stringFormatToTime2("06/05/2017 14:20:00"), f1156.getArrivalTime());
		
		
		//test local search
		
		XiaMengAirlineSolutionBackup sol133 = new XiaMengAirlineSolutionBackup();
		AircraftBackup air133 = initialSolution.getAircraft("133", "2", false, false);
		air94 = initialSolution.getAircraft("94", "2", false, false);
		sol133.replaceOrAddNewAircraft(air133);
		sol133.replaceOrAddNewAircraft(air94);

		
		XiaMengAirlineSolutionBackup sol133Out = sol133.reConstruct();
		sol133Out.refreshCost(false);
		System.out.println("So133 init: " + sol133Out.getCost());
		XiaMengAirlineSolutionBackup aBetterSolution = localEngine.constructNewSolution(sol133);
		XiaMengAirlineSolutionBackup aBetterOutput = aBetterSolution.reConstruct();
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
