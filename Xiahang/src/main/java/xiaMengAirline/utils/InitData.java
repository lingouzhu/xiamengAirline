package xiaMengAirline.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.AirPortClose;
import xiaMengAirline.beans.AirPortList;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.RegularAirPortClose;
import xiaMengAirline.beans.Transit;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.searchEngine.BusinessDomain;

public class InitData {
	private static final Logger logger = Logger.getLogger(InitData.class);

	/** air limitation list air_startPort_endPort */
	public static List<String> airLimitationList = new ArrayList<String>();

	/** flght time map key: air_startport_endport value: time */
	public static Map<String, Integer> fightDurationMap = new HashMap<String, Integer>();

	/** aircraft list */
	public static XiaMengAirlineSolution originalSolution = new XiaMengAirlineSolution();

	public static AirPortList airportList = new AirPortList();

	/** joint flight -- key: flight id, value : next flight (if no then null) */
	public static Map<Integer, Flight> jointFlightMap = new HashMap<Integer, Flight>();

	/** fist flight */
	public static Map<Integer, Flight> firstFlightMap = new HashMap<Integer, Flight>();

	/** last flight */
	public static Map<Integer, Flight> lastFlightMap = new HashMap<Integer, Flight>();

	/** flight < 50 mins */
	public static Map<String, Integer> specialFlightMap = new HashMap<String, Integer>();

	/** domestic airports list **/
	public static List<String> domesticAirportList = new ArrayList<String>();

	public static int maxFligthId = 0;
	public static int plannedMaxFligthId = 0;

	public static Random rndNumbers = new Random();
	public static Random rndRcl = new Random();

	/** change air cost */
	public static HashMap<String, Double> changeAirCostMap = new HashMap<String, Double>() {
		{
			put("1_1", 0.0);
			put("1_2", 0.5);
			put("1_3", 1.5);
			put("1_4", 1.5);

			put("2_1", 0.0);
			put("2_2", 0.0);
			put("2_3", 1.5);
			put("2_4", 1.5);

			put("3_1", 2.0);
			put("3_2", 2.0);
			put("3_3", 0.0);
			put("3_4", 2.0);

			put("4_1", 4.0);
			put("4_2", 4.0);
			put("4_3", 2.0);
			put("4_4", 0.0);
		}
	};

	/** change air cost */
	public static List<Transit> transitList = new ArrayList<Transit>();

	public static void initData(String initDatafile) {

		try {

			Date startTime = Utils.stringFormatToTime2("06/05/2017 06:00:00");
			Date endTime = Utils.stringFormatToTime2("09/05/2017 00:00:00");

			InputStream stream = new FileInputStream(initDatafile);
			Workbook wb = new XSSFWorkbook(stream);

			/**************************************** 航班 *************************************************/
			Sheet schdSheet = wb.getSheet("航班");
			int cnt = 0;
			for (Row row : schdSheet) {
				if (cnt == 0) {
					cnt++;
					continue;
				}
				Flight aFlight = new Flight();
				int aFlightId = (int) row.getCell(0).getNumericCellValue();
				if (aFlightId > maxFligthId) {
					maxFligthId = aFlightId;
					plannedMaxFligthId = maxFligthId;
				}

				aFlight.setFlightId(aFlightId);
				aFlight.setSchdDate(row.getCell(1).getDateCellValue());
				aFlight.setInternationalFlight(Utils.interToBoolean(row.getCell(2).getStringCellValue()));
				aFlight.setSchdNo((int) row.getCell(3).getNumericCellValue());

				AirPort aAirport = airportList.getAirport(String.valueOf((int) row.getCell(4).getNumericCellValue()));
				aFlight.setSourceAirPort(aAirport);

				aAirport = airportList.getAirport(String.valueOf((int) row.getCell(5).getNumericCellValue()));
				aFlight.setDesintationAirport(aAirport);

				aFlight.setDepartureTime(row.getCell(6).getDateCellValue());
				aFlight.setArrivalTime(row.getCell(7).getDateCellValue());

				// process adjustable
				if (aFlight.getDepartureTime().before(startTime) || (aFlight.getDepartureTime().after(endTime)))
					aFlight.setAdjustable(false);

				String airId = String.valueOf((int) row.getCell(8).getNumericCellValue());
				String airType = String.valueOf((int) row.getCell(9).getNumericCellValue());

				Aircraft aAir = originalSolution.getAircraft(airId, airType, false, true);

				int numberOfPassenger = (int) row.getCell(10).getNumericCellValue();
				aFlight.setNumberOfPassenger(numberOfPassenger);
				int numberOfJoinedPassenger = (int) row.getCell(11).getNumericCellValue();
				aFlight.setNumberOfJoinedPassenger(numberOfJoinedPassenger);
				int numberOfSeats = (int) row.getCell(12).getNumericCellValue();
				aAir.setNumberOfSeats(numberOfSeats);

				// aFlight.setImpCoe(new
				// BigDecimal(row.getCell(10).getNumericCellValue()));
				aFlight.setImpCoe(
						new BigDecimal(row.getCell(13).getNumericCellValue()).setScale(2, BigDecimal.ROUND_HALF_UP));
				aFlight.setAssignedAir(aAir);
				Aircraft aPlannedAir = aAir.clone();
				aPlannedAir.clear();
				aFlight.setPlannedAir(aPlannedAir);
				aFlight.setPlannedFlight(aFlight.clone());
				aAir.addFlight(aFlight);
				// originalSolution.replaceOrAddNewAircraft(aAir);

			}

			List<Aircraft> schedule = new ArrayList<Aircraft>(originalSolution.getSchedule().values());
			HashMap<String, Flight> flightScheduleNumber = new HashMap<String, Flight>();
			for (Aircraft aAir : schedule) {
				aAir.sortFlights();
				List<Flight> flightList = aAir.getFlightChain();
				for (int i = 0; i < flightList.size(); i++) {
					Flight aFlight = flightList.get(i);
					if (i == 0) {
						firstFlightMap.put(aFlight.getFlightId(), aFlight);
					} else {
						Flight pFlight = flightList.get(i - 1);
						int bTime = Utils.minutiesBetweenTime(aFlight.getDepartureTime(), pFlight.getArrivalTime())
								.intValue();
						if (bTime < 50) {
							// System.out.println(pFlight.getArrivalTime());
							// System.out.println(aFlight.getDepartureTime());
							// System.out.println(bTime);

							specialFlightMap.put(
									String.valueOf(pFlight.getFlightId()) + "_" + String.valueOf(aFlight.getFlightId()),
									bTime);
						}
					}

					if (i == flightList.size() - 1) {
						lastFlightMap.put(aFlight.getFlightId(), aFlight);
					}

					String aKey = Integer.toString(aFlight.getSchdNo());
					aKey += "_";
					aKey += Utils.dateFormatter(aFlight.getSchdDate());

					Flight lastFlight = flightScheduleNumber.put(aKey, aFlight);
					if (lastFlight != null) {
						jointFlightMap.put(lastFlight.getFlightId(), aFlight.clone());
						jointFlightMap.put(aFlight.getFlightId(), null);
						if (!lastFlight.isAdjustable() && aFlight.isAdjustable()) 
							aFlight.setExtraNonAdjustable(true);
					}

					// if (!aFlight.isInternationalFlight()) {
					// String sourceAirport =
					// aFlight.getSourceAirPort().getId();
					// String destAirport =
					// aFlight.getDesintationAirport().getId();
					// if (!domesticAirportList.contains(sourceAirport)) {
					// domesticAirportList.add(sourceAirport);
					// }
					// if (!domesticAirportList.contains(destAirport)) {
					// domesticAirportList.add(destAirport);
					// }
					// }

				}

			}
			
			// List<Aircraft> scheduleCheck = new ArrayList<Aircraft> (
			// originalSolution.getSchedule().values());
			// for (Aircraft aAir:scheduleCheck) {
			// logger.debug("Air id " + aAir.getId());
			// for (Flight aFlight:aAir.getFlightChain()) {
			// logger.debug(" Flight id " + aFlight.getSchdNo());
			// }
			// }

			// ****************************************航班
			// 飞机限制*************************************************//*
			Sheet airLimitSheet = wb.getSheet("航线-飞机限制");
			cnt = 0;
			for (Row row : airLimitSheet) {
				if (cnt == 0) {
					cnt++;
					continue;
				}
				String startPort = String.valueOf((int) row.getCell(0).getNumericCellValue());
				String endPort = String.valueOf((int) row.getCell(1).getNumericCellValue());
				String airID = String.valueOf((int) row.getCell(2).getNumericCellValue());

				airLimitationList.add(airID + "_" + startPort + "_" + endPort);
			}

			/**************************************** 机场关闭限制 *************************************************/
			Sheet portCloseSheet = wb.getSheet("机场关闭限制");
			cnt = 0;
			for (Row row : portCloseSheet) {
				if (cnt == 0) {
					cnt++;
					continue;
				}

				RegularAirPortClose portCloseBean = new RegularAirPortClose();
				String airPortId = String.valueOf((int) row.getCell(0).getNumericCellValue());
				AirPort aAirport = airportList.getAirport(airPortId);
				portCloseBean.setCloseTime(Utils.timeFormatter(row.getCell(1).getDateCellValue()).substring(11));
				portCloseBean.setOpenTime(Utils.timeFormatter(row.getCell(2).getDateCellValue()).substring(11));
				portCloseBean.setCloseDate(Utils.dateFormatter(row.getCell(3).getDateCellValue()));
				portCloseBean.setOpenDate(Utils.dateFormatter(row.getCell(4).getDateCellValue()));

				aAirport.addRegularCloseSchedule(portCloseBean);

			}

			/**************************************** 台风场景 *************************************************/
			Sheet taifengSheet = wb.getSheet("台风场景");
			cnt = 0;
			for (Row row : taifengSheet) {
				if (cnt == 0) {
					cnt++;
					continue;
				}

				AirPortClose portCloseBean = new AirPortClose();
				String airPortId = String.valueOf((int) row.getCell(3).getNumericCellValue());
				AirPort aAirport = airportList.getAirport(airPortId);
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

				// create special airport recover time
				if (impactType.equals("起飞")) {
					// only allows 2 flights take-off / landing before / after
					String aDate = "2017-05-06 ";
					String startHour = "15:";
					int bufferStartTime = 0;

					for (int i = 1; i <= 12; i++) {
						String bufferStartMin = String.format("%02d", bufferStartTime);
						aAirport.getTakeoffCapability().put(aDate + startHour + bufferStartMin, 2);
						bufferStartTime += 5;

					}

					aDate = "2017-05-07 ";
					startHour = "17:";
					bufferStartTime = 0;

					for (int i = 1; i <= 12; i++) {
						String bufferStartMin = String.format("%02d", bufferStartTime);
						aAirport.getTakeoffCapability().put(aDate + startHour + bufferStartMin, 2);
						bufferStartTime += 5;

					}

					aDate = "2017-05-07 ";
					startHour = "18:";
					bufferStartTime = 0;

					for (int i = 1; i <= 12; i++) {
						String bufferStartMin = String.format("%02d", bufferStartTime);
						aAirport.getTakeoffCapability().put(aDate + startHour + bufferStartMin, 2);
						bufferStartTime += 5;

					}

				}

				// create special airport recover time
				if (impactType.equals("降落")) {
					// only allows 2 flights take-off / landing before / after
					String aDate = "2017-05-07 ";
					String startHour = "17:";
					int bufferStartTime = 0;

					for (int i = 1; i <= 12; i++) {
						String bufferStartMin = String.format("%02d", bufferStartTime);
						aAirport.getLandingCapability().put(aDate + startHour + bufferStartMin, 2);
						bufferStartTime += 5;

					}

					aDate = "2017-05-07 ";
					startHour = "18:";
					bufferStartTime = 0;

					for (int i = 1; i <= 12; i++) {
						String bufferStartMin = String.format("%02d", bufferStartTime);
						aAirport.getLandingCapability().put(aDate + startHour + bufferStartMin, 2);
						bufferStartTime += 5;

					}

				}

			}

			// ****************************************中转时间限制*************************************************//*
			Sheet transitSheet = wb.getSheet("中转时间限制");
			cnt = 0;
			for (Row row : transitSheet) {
				if (cnt == 0) {
					cnt++;
					continue;
				}
				Transit transit = new Transit();
				transit.setFlightID1((int) row.getCell(0).getNumericCellValue());
				transit.setFlightID2((int) row.getCell(1).getNumericCellValue());
				transit.setTransitMins((int) row.getCell(2).getNumericCellValue());
				transit.setTransitPersons((int) row.getCell(3).getNumericCellValue());
				transitList.add(transit);
			}

			// ****************************************飞行时间*************************************************//*
			Sheet flightTimeSheet = wb.getSheet("飞行时间");
			cnt = 0;
			for (Row row : flightTimeSheet) {
				if (cnt == 0) {
					cnt++;
					continue;
				}

				String airType = String.valueOf((int) row.getCell(0).getNumericCellValue());
				String startPort = String.valueOf((int) row.getCell(1).getNumericCellValue());
				String endPort = String.valueOf((int) row.getCell(2).getNumericCellValue());
				int time = (int) row.getCell(3).getNumericCellValue();

				fightDurationMap.put(airType + "_" + startPort + "_" + endPort, time);
			}

			// *****************************************************************************************//*
			Sheet airportSheet = wb.getSheet("机场");
			cnt = 0;
			for (Row row : airportSheet) {
				if (cnt == 0) {
					cnt++;
					continue;
				}
				String airPortId = String.valueOf((int) row.getCell(0).getNumericCellValue());
				String domestic = String.valueOf((int) row.getCell(1).getNumericCellValue());

				if ("1".equals(domestic)) {
					domesticAirportList.add(airPortId);
				}
				AirPort aAirport = airportList.getAirport(airPortId);
				aAirport.setInternational(
						Utils.interToBoolean(String.valueOf((int) row.getCell(1).getNumericCellValue())));
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
