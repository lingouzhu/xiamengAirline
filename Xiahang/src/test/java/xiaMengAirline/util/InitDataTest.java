package xiaMengAirline.util;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.AirPortClose;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.RegularAirPortClose;

public class InitDataTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testInitData() throws ParseException {
		// Step1, Load all data & initialize
		String initDatafile = "XiahangData20170705_1.xlsx";
		InitData.initData(initDatafile);
		Aircraft air50 = InitData.originalSolution.getAircraft("50", "2", false,false);
		
		//check air
		assertEquals("50",air50.getId());
		assertEquals(false, air50.isCancel());
		
		//check flight
		Flight f15 = air50.getFlightByFlightId(15);
		assertEquals(Utils.stringFormatToTime2("05/05/2017 07:30:00"), f15.getArrivalTime());
		assertEquals(Utils.stringFormatToTime2("05/05/2017  10:25:00"), f15.getDepartureTime());
		assertEquals(new BigDecimal(1.74).setScale(2, BigDecimal.ROUND_HALF_UP), f15.getImpCoe());
		assertEquals(false,f15.isInternationalFlight());
		assertEquals(349, f15.getSchdNo());
		assertEquals("50", f15.getPlannedAir().getId());
		assertEquals("50", f15.getAssignedAir().getId());
		assertEquals(15,f15.getPlannedFlight().getFlightId());
		assertEquals("50",f15.getSourceAirPort().getId());
		assertEquals("72", f15.getDesintationAirport().getId());
		
		//joined flight
		Aircraft air122 = InitData.originalSolution.getAircraft("122", "2", false,false);
		Flight f1918 = air122.getFlightByFlightId(1918);
		int anotherF = InitData.jointFlightMap.get(f1918.getFlightId()).getFlightId();
		assertEquals(1920, anotherF);
		Flight f1920 = air122.getFlightByFlightId(1920);
		assertEquals(null, InitData.jointFlightMap.get(f1920.getFlightId()));
		
		Aircraft air109 = InitData.originalSolution.getAircraft("109", "2", false,false);
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
	}

}
