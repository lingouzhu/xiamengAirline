package xiaMengAirline.newBranch.BasicObject;


import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import xiaMengAirline.newBranch.BasicObject.ResourceUnavailableEventType.AllowType;
import xiaMengAirline.newBranch.BusinessDomain.AirPortAvailability;
import xiaMengAirline.newBranch.BusinessDomain.DomesticFlightAdjustableMethod;
import xiaMengAirline.newBranch.BusinessDomain.InternationalFlightAdjustableMethod;
import xiaMengAirline.newBranch.BusinessDomain.JoinedFlightAdjustableMethod;
import xiaMengAirline.newBranch.BusinessDomain.SeatAvailability;
import xiaMengAirline.newBranch.BusinessDomain.XiaMengAirlineSolutionCost;




public class InitData {
	private static final Logger logger = Logger.getLogger(InitData.class);
	

	/** air limitation list air_startPort_endPort */
	public static List<String> airLimitationList = new ArrayList<String>();
	
	/** flght time map key: air_startport_endport value: time */
	public static Map<String, Integer> fightDurationMap = new HashMap<String, Integer>();
	
		
	/** fist flight*/
	public static Map<String, Flight> firstFlightMap = new HashMap<String, Flight>();
	
	/** last flight*/
	public static Map<String, Flight> lastFlightMap = new HashMap<String, Flight>();
	
	/** flight < 50 mins*/
	public static Map<String, Integer> specialFlightMap = new HashMap<String, Integer>();
	
	/** passenger transition table keyl sourceFlightId_sourceFlightId, value shortestTransition_numberOfPassenger*/
	public static Map<String, String> passengerTransitionMap = new HashMap<String, String>();
	
	
	public static int maxFligthId = 0;
	public static int plannedMaxFligthId = 0;
	
	public static Random rndNumbers = new Random();
	public static Random rndRcl = new Random();

	
	public static XiaMengAirlineSolution initData(String initDatafile) throws Exception {
		XiaMengAirlineSolution originalSolution = new XiaMengAirlineSolution();
		originalSolution.setVersion("0");
		originalSolution.setaCost(new XiaMengAirlineSolutionCost());
				
		try {
			
			InputStream stream = new FileInputStream(initDatafile);  
			Workbook wb = new XSSFWorkbook(stream);  
			
			
			/****************************************航班*************************************************/
			Sheet schdSheet = wb.getSheet("航班");  
			int cnt = 0;
			for (Row row : schdSheet) { 
				if (cnt == 0) {
					cnt++;
					continue;
				}
				Flight aFlight = new Flight();
				int aFlightId = (int)row.getCell(0).getNumericCellValue();
				if (aFlightId > maxFligthId) {
					maxFligthId = aFlightId;
				}
					
				
				aFlight.setFlightId(aFlightId);
				aFlight.setSchdDate(row.getCell(1).getDateCellValue());
				aFlight.setInternationalFlight(Utils.interToBoolean(row.getCell(2).getStringCellValue()));
				if (aFlight.isInternationalFlight())
					aFlight.getAdjustableMethod().add(new InternationalFlightAdjustableMethod());
				else
					aFlight.getAdjustableMethod().add(new DomesticFlightAdjustableMethod());
				aFlight.setSchdNo((int)row.getCell(3).getNumericCellValue());
				
				Airport aAirport = originalSolution.getAirport(String.valueOf((int)row.getCell(4).getNumericCellValue()));
				aFlight.setSourceAirPort(aAirport);
				
				aAirport = originalSolution.getAirport(String.valueOf((int)row.getCell(5).getNumericCellValue()));
				aFlight.setDesintationAirport(aAirport);
				
				aFlight.setDepartureTime(row.getCell(6).getDateCellValue());
				aFlight.setArrivalTime(row.getCell(7).getDateCellValue());
				
				String airId = String.valueOf((int)row.getCell(8).getNumericCellValue());
				String airType = String.valueOf((int)row.getCell(9).getNumericCellValue());
				
				Aircraft aAir = originalSolution.getAircraft(airId, airType, true);
				
				int passengerNumber = (int)row.getCell(10).getNumericCellValue();
				int joinedPassengerNumber = (int)row.getCell(11).getNumericCellValue();
				
				for (int i=1;i<=passengerNumber;i++) {
					Passenger aPass = new Passenger(aFlight, aAir);
					if (i <= joinedPassengerNumber) 
						aPass.setNormal(false);
					else
						aPass.setNormal(true);
					aFlight.getPassengers().add(aPass);
				}
				
				int seatsNumber = (int)row.getCell(12).getNumericCellValue();
				SeatAvailability airSeats = new SeatAvailability();
				airSeats.setSeatCapability(seatsNumber);
				airSeats.applyForResource(aFlight.getPassengers().size());
				aAir.setSeatsAvailability(airSeats);
				
				
				aFlight.setImpCoe(new BigDecimal(row.getCell(13).getNumericCellValue()).setScale(2, BigDecimal.ROUND_HALF_UP));
				aFlight.setAssignedAir(aAir);
				Aircraft aPlannedAir = aAir.clone();
				aPlannedAir.clear();
				aFlight.setPlannedAir(aPlannedAir);
				aFlight.setPlannedFlight(aFlight.clone());
				aAir.addFlight(aFlight);
	        }
			plannedMaxFligthId = maxFligthId;
			
			List<Aircraft> schedule = new ArrayList<Aircraft> ( originalSolution.getNormalSchedule().values());
			HashMap<String, Flight> flightScheduleNumber = new HashMap<String, Flight> ();
			for (Aircraft aAir:schedule) {
				aAir.sortFlights();
				List<Flight> flightList = aAir.getFlightChain();
				for (int i = 0; i < flightList.size(); i++) {
					Flight aFlight = flightList.get(i);
					if (i == 0) {
						firstFlightMap.put(aAir.getId(), aFlight);
					} else {
						Flight pFlight = flightList.get(i - 1);
						int bTime = Utils.minutiesBetweenTime(aFlight.getDepartureTime(), pFlight.getArrivalTime()).intValue();
						if (bTime < 50) {
//							System.out.println(pFlight.getArrivalTime());
//							System.out.println(aFlight.getDepartureTime());
//							System.out.println(bTime);
							
							specialFlightMap.put(String.valueOf(pFlight.getFlightId()) + "_" + String.valueOf(aFlight.getFlightId()) , bTime);
						}
					}
					
					if (i == flightList.size() - 1) {
						lastFlightMap.put(aAir.getId(), aFlight);
					}
					
					String aKey = Integer.toString(aFlight.getSchdNo());
					aKey += "_";
					aKey += Utils.dateFormatter(aFlight.getSchdDate());
					
					Flight lastFlight = flightScheduleNumber.put(aKey, aFlight);
					if (lastFlight !=null) {
						lastFlight.setJoined1stlight(lastFlight);
						lastFlight.setJoined2ndFlight(aFlight);
						lastFlight.getAdjustableMethod().add(new JoinedFlightAdjustableMethod());
						
						aFlight.setJoined1stlight(lastFlight);
						aFlight.setJoined2ndFlight(aFlight);
						aFlight.getAdjustableMethod().add(new JoinedFlightAdjustableMethod());
						
					}
					
					
				}
				
			}
			
//			List<Aircraft> scheduleCheck = new ArrayList<Aircraft> ( originalSolution.getSchedule().values());
//			for (Aircraft aAir:scheduleCheck) {
//				logger.debug("Air id " + aAir.getId());
//				for (Flight aFlight:aAir.getFlightChain()) {
//					logger.debug("		Flight id " + aFlight.getSchdNo());
//				}
//			}
			
			//****************************************airport type
			Sheet airportSheet = wb.getSheet("机场");  
			cnt = 0;
			for (Row row : airportSheet) { 
				if (cnt == 0) {
					cnt++;
					continue;
				}
				String airPortId =  String.valueOf((int)row.getCell(0).getNumericCellValue());
				int domestic =  (int)row.getCell(1).getNumericCellValue();
				
				Airport aAirport = originalSolution.getAirport(airPortId);
				if (domestic == 0)
					aAirport.setDomestic(true);
				else
					aAirport.setDomestic(false);
				
			}
			
			//****************************************航班 飞机限制*************************************************//*
			Sheet airLimitSheet = wb.getSheet("航线-飞机限制");  
			cnt = 0;
			for (Row row : airLimitSheet) { 
				if (cnt == 0) {
					cnt++;
					continue;
				}
				String startPort =  String.valueOf((int)row.getCell(0).getNumericCellValue());
				String endPort =  String.valueOf((int)row.getCell(1).getNumericCellValue());
				String airID =  String.valueOf((int)row.getCell(2).getNumericCellValue());
				
				airLimitationList.add(airID + "_" + startPort + "_" + endPort);
			}
			
			/****************************************机场关闭限制*************************************************/
			Sheet portCloseSheet = wb.getSheet("机场关闭限制");  
			cnt = 0;
			for (Row row : portCloseSheet) { 
				if (cnt == 0) {
					cnt++;
					continue;
				}
				
				AirportRegularClose portCloseBean = new AirportRegularClose();
				String airPortId = String.valueOf((int)row.getCell(0).getNumericCellValue());
				Airport aAirport = originalSolution.getAirport(airPortId);
				
				if (aAirport.getAirportAvailability() == null)
					aAirport.setAirportAvailability(new AirPortAvailability());

				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String aStartTime = Utils.timeFormatter(row.getCell(1).getDateCellValue()).substring(11);
				String aEndTime = Utils.timeFormatter(row.getCell(2).getDateCellValue()).substring(11);
				
				String aDate = "2017-05-05 ";
				portCloseBean.setStartTime(formatter.parse(aDate + aStartTime));
				portCloseBean.setEndTime(formatter.parse(aDate + aEndTime));
				aAirport.getAirportAvailability().addImpactEvent(portCloseBean);
				
				aDate = "2017-05-06 ";
				portCloseBean.setStartTime(formatter.parse(aDate + aStartTime));
				portCloseBean.setEndTime(formatter.parse(aDate + aEndTime));
				aAirport.getAirportAvailability().addImpactEvent(portCloseBean);
				
				aDate = "2017-05-07 ";
				portCloseBean.setStartTime(formatter.parse(aDate + aStartTime));
				portCloseBean.setEndTime(formatter.parse(aDate + aEndTime));
				aAirport.getAirportAvailability().addImpactEvent(portCloseBean);
				
				if (!airPortId.equals("22")) {
					aDate = "2017-05-08 ";
					portCloseBean.setStartTime(formatter.parse(aDate + aStartTime));
					portCloseBean.setEndTime(formatter.parse(aDate + aEndTime));
					aAirport.getAirportAvailability().addImpactEvent(portCloseBean);
				} 
				
			}
			
			/****************************************台风场景*************************************************/
			Sheet taifengSheet = wb.getSheet("台风场景");  
			cnt = 0;
			for (Row row : taifengSheet) { 
				if (cnt == 0) {
					cnt++;
					continue;
				}
				
				AirportTyphoonClose portCloseBean = new AirportTyphoonClose();
				String airPortId = String.valueOf((int)row.getCell(3).getNumericCellValue());
				Airport aAirport = originalSolution.getAirport(airPortId);
				portCloseBean.setStartTime(row.getCell(0).getDateCellValue());
				portCloseBean.setEndTime(row.getCell(1).getDateCellValue());
				String impactType = row.getCell(2).getStringCellValue();
				if (impactType.equals("降落")) {
					portCloseBean.getUnavailableEventType().setAllowForLanding(AllowType.NOT_ALLOWED);
				} else if (impactType.equals("起飞")) {
					portCloseBean.getUnavailableEventType().setAllowForTakeOff(AllowType.NOT_ALLOWED);
				} else if (impactType.equals("停机")) {
					int nParking =  (int)row.getCell(6).getNumericCellValue();
					portCloseBean.getUnavailableEventType().setAllowForParking(AllowType.ALLOWED.CONDITION);
					portCloseBean.getUnavailableEventType().setCapability(nParking);
				}
				
				if (aAirport.getAirportAvailability() == null)
					aAirport.setAirportAvailability(new AirPortAvailability());
								
				aAirport.getAirportAvailability().addImpactEvent(portCloseBean);
				
				//create special airport recover time
				 if (impactType.equals("起飞")) {
					 //only allows 2 flights take-off / landing before / after
					 String aDate = "2017-05-06 ";
					 String startHour = "15:";
					 int startTime = 0;
					 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

					 for (int i = 1;i <= 12; i++) {
						 String startMin = String.format("%02d", startTime);
						 String endMin = String.format("%02d", startTime + 5);
						 AirportTyphoonClose portBufferBean1 = new AirportTyphoonClose();
						 AirportTyphoonClose portBufferBean2 = new AirportTyphoonClose();

						 portBufferBean1.setStartTime(formatter.parse(aDate + startHour + startMin ));
						 portBufferBean1.getUnavailableEventType().setAllowForLanding(AllowType.CONDITION);
						 portBufferBean1.getUnavailableEventType().setCapability(2);
						 portBufferBean1.setEndTime(formatter.parse(aDate + startHour + endMin ));
						 
						 portBufferBean2.setStartTime(formatter.parse(aDate + startHour + startMin ));
						 portBufferBean2.getUnavailableEventType().setAllowForTakeOff(AllowType.CONDITION);
						 portBufferBean2.getUnavailableEventType().setCapability(2);						 
						 portBufferBean2.setEndTime(formatter.parse(aDate + startHour + endMin ));
						 
						 aAirport.getAirportAvailability().addImpactEvent(portBufferBean1);
						 aAirport.getAirportAvailability().addImpactEvent(portBufferBean2);
						 
						 startTime += 5;
						 
					 }
					 
					 aDate = "2017-05-07 ";
					 startHour = "17:";
					 startTime = 0;

					 for (int i = 1;i <= 12; i++) {
						 String startMin = String.format("%02d", startTime);
						 String endMin = String.format("%02d", startTime + 5);
						 AirportTyphoonClose portBufferBean1 = new AirportTyphoonClose();
						 AirportTyphoonClose portBufferBean2 = new AirportTyphoonClose();

						 portBufferBean1.setStartTime(formatter.parse(aDate + startHour + startMin ));
						 portBufferBean1.getUnavailableEventType().setAllowForLanding(AllowType.CONDITION);
						 portBufferBean1.getUnavailableEventType().setCapability(2);
						 portBufferBean1.setEndTime(formatter.parse(aDate + startHour + endMin ));
						 
						 portBufferBean2.setStartTime(formatter.parse(aDate + startHour + startMin ));
						 portBufferBean2.getUnavailableEventType().setAllowForTakeOff(AllowType.CONDITION);
						 portBufferBean2.getUnavailableEventType().setCapability(2);						 
						 portBufferBean2.setEndTime(formatter.parse(aDate + startHour + endMin ));
						 
						 aAirport.getAirportAvailability().addImpactEvent(portBufferBean1);
						 aAirport.getAirportAvailability().addImpactEvent(portBufferBean2);
						 
						 startTime += 5;
						 
					 }
					 
					 aDate = "2017-05-07 ";
					 startHour = "18:";
					 startTime = 0;

					 for (int i = 1;i <= 12; i++) {
						 String startMin = String.format("%02d", startTime);
						 String endMin = String.format("%02d", startTime + 5);
						 AirportTyphoonClose portBufferBean1 = new AirportTyphoonClose();
						 AirportTyphoonClose portBufferBean2 = new AirportTyphoonClose();

						 portBufferBean1.setStartTime(formatter.parse(aDate + startHour + startMin ));
						 portBufferBean1.getUnavailableEventType().setAllowForLanding(AllowType.CONDITION);
						 portBufferBean1.getUnavailableEventType().setCapability(2);
						 portBufferBean1.setEndTime(formatter.parse(aDate + startHour + endMin ));
						 
						 portBufferBean2.setStartTime(formatter.parse(aDate + startHour + startMin ));
						 portBufferBean2.getUnavailableEventType().setAllowForTakeOff(AllowType.CONDITION);
						 portBufferBean2.getUnavailableEventType().setCapability(2);						 
						 portBufferBean2.setEndTime(formatter.parse(aDate + startHour + endMin ));
						 
						 aAirport.getAirportAvailability().addImpactEvent(portBufferBean1);
						 aAirport.getAirportAvailability().addImpactEvent(portBufferBean2);
						 
						 startTime += 5;
						 
					 }
				 }
				
			}
			
	
			
			
			//****************************************飞行时间*************************************************//*
			Sheet flightTimeSheet = wb.getSheet("飞行时间");  
			cnt = 0;
			for (Row row : flightTimeSheet) { 
				if (cnt == 0) {
					cnt++;
					continue;
				}
				
				String airType = String.valueOf((int)row.getCell(0).getNumericCellValue());
				String startPort = String.valueOf((int)row.getCell(1).getNumericCellValue());
				String endPort = String.valueOf((int)row.getCell(2).getNumericCellValue());
				int time = (int)row.getCell(3).getNumericCellValue();
				
				fightDurationMap.put(airType + "_" + startPort + "_" + endPort, time);
			}
			
			//****************************************旅客中转时间限制*************************************************//*
			Sheet passengerTransitionSheet = wb.getSheet("中转时间限制");  
			cnt = 0;
			for (Row row : passengerTransitionSheet) { 
				if (cnt == 0) {
					cnt++;
					continue;
				}
				
				String sourceFlightId = String.valueOf((int)row.getCell(0).getNumericCellValue());
				String destFlightId = String.valueOf((int)row.getCell(1).getNumericCellValue());
				String shortestTransition = String.valueOf((int)row.getCell(2).getNumericCellValue());
				String numberOfPassenger = String.valueOf((int)row.getCell(3).getNumericCellValue());
				
				passengerTransitionMap.put( sourceFlightId+ "_" + destFlightId, shortestTransition + "_" + numberOfPassenger);
			}
			
			return originalSolution;
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}  
		
	}
	

	
}
