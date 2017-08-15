package xiaMengAirline.newBranch.BasicObject;


import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
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

import xiaMengAirline.beans.AirPortBackup;
import xiaMengAirline.beans.AirPortCloseBackup;
import xiaMengAirline.beans.AirPortListBackup;
import xiaMengAirline.beans.AircraftBackup;
import xiaMengAirline.beans.FlightBackup;
import xiaMengAirline.beans.RegularAirPortCloseBackup;
import xiaMengAirline.beans.XiaMengAirlineSolutionBackup;
import xiaMengAirline.newBranch.BusinessDomain.SeatAvailability;




public class InitData {
	private static final Logger logger = Logger.getLogger(InitData.class);
	

	/** air limitation list air_startPort_endPort */
	public static List<String> airLimitationList = new ArrayList<String>();
	
	/** flght time map key: air_startport_endport value: time */
	public static Map<String, Integer> fightDurationMap = new HashMap<String, Integer>();
	
	/** aircraft list */
	public static XiaMengAirlineRawSolution originalSolution = new XiaMengAirlineRawSolution();
	
	/** joint flight -- key: flight id, value : next flight (if no then null)*/
	public static Map<Integer, FlightBackup> jointFlightMap = new HashMap<Integer, FlightBackup>();
	
	/** fist flight*/
	public static Map<String, FlightBackup> firstFlightMap = new HashMap<String, FlightBackup>();
	
	/** last flight*/
	public static Map<String, FlightBackup> lastFlightMap = new HashMap<String, FlightBackup>();
	
	/** flight < 50 mins*/
	public static Map<String, Integer> specialFlightMap = new HashMap<String, Integer>();
	
	/** domestic airports list **/
	public static List<String> domesticAirportList = new ArrayList<String> (); 
	
	public static int maxFligthId = 0;
	public static int plannedMaxFligthId = 0;
	
	public static Random rndNumbers = new Random();
	public static Random rndRcl = new Random();

	
	public static void initData(String initDatafile) {
				
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
				airSeats.setResoruceCapability(seatsNumber);
				airSeats.applyForResource(aFlight.getPassengers().size(), null);
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
			
			List<AircraftBackup> schedule = new ArrayList<AircraftBackup> ( originalSolution.getSchedule().values());
			HashMap<String, FlightBackup> flightScheduleNumber = new HashMap<String, FlightBackup> ();
			for (AircraftBackup aAir:schedule) {
				aAir.sortFlights();
				List<FlightBackup> flightList = aAir.getFlightChain();
				for (int i = 0; i < flightList.size(); i++) {
					FlightBackup aFlight = flightList.get(i);
					if (i == 0) {
						firstFlightMap.put(aAir.getId(), aFlight);
					} else {
						FlightBackup pFlight = flightList.get(i - 1);
						int bTime = UtilsBackup.minutiesBetweenTime(aFlight.getDepartureTime(), pFlight.getArrivalTime()).intValue();
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
					aKey += UtilsBackup.dateFormatter(aFlight.getSchdDate());
					
					FlightBackup lastFlight = flightScheduleNumber.put(aKey, aFlight);
					if (lastFlight !=null) {
						jointFlightMap.put(lastFlight.getFlightId(),aFlight.clone());
						jointFlightMap.put(aFlight.getFlightId(),null);
					}
					
					if (!aFlight.isInternationalFlight()) {
						String sourceAirport = aFlight.getSourceAirPort().getId();
						String destAirport = aFlight.getDesintationAirport().getId();
						if (!domesticAirportList.contains(sourceAirport)) {
							domesticAirportList.add(sourceAirport);
						}
						if (!domesticAirportList.contains(destAirport)) {
							domesticAirportList.add(destAirport);
						}
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
				
				RegularAirPortCloseBackup portCloseBean = new RegularAirPortCloseBackup();
				String airPortId = String.valueOf((int)row.getCell(0).getNumericCellValue());
				AirPortBackup aAirport = airportList.getAirport(airPortId);
				portCloseBean.setCloseTime(UtilsBackup.timeFormatter(row.getCell(1).getDateCellValue()).substring(11));
				portCloseBean.setOpenTime(UtilsBackup.timeFormatter(row.getCell(2).getDateCellValue()).substring(11));
				portCloseBean.setCloseDate(UtilsBackup.dateFormatter(row.getCell(3).getDateCellValue()));
				portCloseBean.setOpenDate(UtilsBackup.dateFormatter(row.getCell(4).getDateCellValue()));
				
				aAirport.addRegularCloseSchedule(portCloseBean);
				
			}
			
			/****************************************台风场景*************************************************/
			Sheet taifengSheet = wb.getSheet("台风场景");  
			cnt = 0;
			for (Row row : taifengSheet) { 
				if (cnt == 0) {
					cnt++;
					continue;
				}
				
				AirPortCloseBackup portCloseBean = new AirPortCloseBackup();
				String airPortId = String.valueOf((int)row.getCell(3).getNumericCellValue());
				AirPortBackup aAirport = airportList.getAirport(airPortId);
				portCloseBean.setStartTime(row.getCell(0).getDateCellValue());
				portCloseBean.setEndTime(row.getCell(1).getDateCellValue());
				String impactType = row.getCell(2).getStringCellValue();
				if (impactType.equals("降落")) {
					portCloseBean.setAllowForLanding(false);
				} else if (impactType.equals("起飞")) {
					portCloseBean.setAllowForTakeoff(false);
				} else if (impactType.equals("停机")) {
					portCloseBean.setMaximumParking(0);
				}
				
				
				aAirport.addCloseSchedule(portCloseBean);
				
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
			
			 
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
	}
	

	
}
