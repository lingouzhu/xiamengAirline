package xiaMengAirline.newBranch.BasicObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Protectable;

import static org.junit.Assert.*;

import xiaMengAirline.newBranch.BasicObject.ResourceUnavailableEventType.AllowType;
import xiaMengAirline.newBranch.BusinessDomain.AirPortAvailability;
import xiaMengAirline.newBranch.BusinessDomain.AirPortAvailability.RequestType;

public class AirportTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testClone() throws ParseException, CloneNotSupportedException {
		Airport aAirport = new Airport();
		aAirport.setId("11");
		aAirport.setAirportAvailability(new AirPortAvailability());
		aAirport.getAirportAvailability().setCurrentAllocated(10);
		
		AirportTyphoonClose portCloseBean = new AirportTyphoonClose();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String startTime = "2017-06-05 13:00:00";
		String endTime = "2017-06-06 17:00:00";
		
		portCloseBean.setStartTime(formatter.parse(startTime));
		portCloseBean.setEndTime(formatter.parse(endTime));
		portCloseBean.getUnavailableEventType().setCapability(100);
		portCloseBean.getUnavailableEventType().setAllowForParking(AllowType.CONDITION);
		aAirport.getAirportAvailability().addImpactEvent(portCloseBean);
		
		
		String reqStartTime = "2017-06-05 14:00:00";
		String reqEndTime = "2017-06-06 18:00:00";
		PairedTime requestTime = new PairedTime();
		requestTime.setStartTime(formatter.parse(reqStartTime));
		requestTime.setEndTime(formatter.parse(reqEndTime));;
		
		List<ResourceUnavailableEvent> eventList = aAirport.getAirportAvailability().getCurrentEvents(requestTime);
		assertEquals(100, eventList.get(0).getUnavailableEventType().getCapability());
		
		List<ResourceUnavailableEvent> impactedList = aAirport.getAirportAvailability().estimateCurrentAvailable(requestTime, RequestType.PARKING);
		assertEquals(0, impactedList.size());
		
		Airport newAirport = aAirport.clone();
		impactedList = newAirport.getAirportAvailability().estimateCurrentAvailable(requestTime, RequestType.PARKING);
		assertEquals(0, impactedList.size());
		
		newAirport.getAirportAvailability().setCurrentAllocated(105);
		assertEquals(105, newAirport.getAirportAvailability().getCurrentAllocated());
		impactedList = newAirport.getAirportAvailability().estimateCurrentAvailable(requestTime, RequestType.PARKING);
		assertEquals(100, impactedList.get(0).getUnavailableEventType().getCapability());

		assertEquals(10, aAirport.getAirportAvailability().getCurrentAllocated());
		impactedList = aAirport.getAirportAvailability().estimateCurrentAvailable(requestTime, RequestType.PARKING);
		assertEquals(0, impactedList.size());
		
		aAirport.getAirportAvailability().setCurrentAllocated(100);
		impactedList = aAirport.getAirportAvailability().estimateCurrentAvailable(requestTime, RequestType.PARKING);
		assertEquals(100, impactedList.get(0).getUnavailableEventType().getCapability());
		
		
		
		
		


		
	}

}
