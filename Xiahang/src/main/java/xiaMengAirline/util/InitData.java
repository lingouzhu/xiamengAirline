package xiaMengAirline.util;


import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.beans.backup.PortCloseBean;
import xiaMengAirline.beans.backup.ScheduleByAirBean;




public class InitData {
	private static final Logger logger = Logger.getLogger(InitData.class);
	
	/** sort by air data */
	public static List<ScheduleByAirBean> airDataList = new ArrayList<ScheduleByAirBean>();

	/** air limitation list air_startPort_endPort */
	public static List<String> airLimitationList = new ArrayList<String>();
	
	/** port close list */
	public static List<PortCloseBean> portCloseList = new ArrayList<PortCloseBean>();
	
	/** flght time map key: air_startport_endport value: time */
	public static Map<String, String> fightTimeMap = new HashMap<String, String>();
	
	/** aircraft list */
	public static XiaMengAirlineSolution originalSolution = new XiaMengAirlineSolution();
	

	
	public static void initData(String initDatafile, String fightTimeFile) {
		
				
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
				aFlight.setFlightId(String.valueOf((int)row.getCell(0).getNumericCellValue()));
				aFlight.setSchdDate(row.getCell(1).getDateCellValue());
				aFlight.setInterFlg(Utils.interToBoolean(row.getCell(2).getStringCellValue()));
				aFlight.setSchdNo((int)row.getCell(3).getNumericCellValue());
				
				AirPort aAirport = new AirPort();
				aAirport.setId(String.valueOf((int)row.getCell(4).getNumericCellValue()));
				aFlight.setSourceAirPort(aAirport);
				
				aAirport = new AirPort();
				aAirport.setId(String.valueOf((int)row.getCell(5).getNumericCellValue()));
				aFlight.setDesintationAirport(aAirport);
				
				aFlight.setArrivalTime(row.getCell(6).getDateCellValue());
				aFlight.setDepartureTime(row.getCell(7).getDateCellValue());
				
				String airId = String.valueOf((int)row.getCell(8).getNumericCellValue());
				String airType = String.valueOf((int)row.getCell(9).getNumericCellValue());
				
				Aircraft aAir = originalSolution.getAircraft(airId, airType, true);
				
				aFlight.setImpCoe(row.getCell(10).getNumericCellValue());
				aAir.addFlight(aFlight);
				originalSolution.addAircraft(aAir);
	        }
			
			List<Aircraft> schedule = new ArrayList<Aircraft> ( originalSolution.getSchedule().values());
			for (Aircraft aAir:schedule) {
				aAir.sortFlights();
			}
			
			List<Aircraft> scheduleCheck = new ArrayList<Aircraft> ( originalSolution.getSchedule().values());
			for (Aircraft aAir:scheduleCheck) {
				logger.info("Air id " + aAir.getId());
				for (Flight aFlight:aAir.getFlightChain()) {
					logger.info("		Flight id " + aFlight.getSchdNo());
				}
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
			
/*			*//****************************************机场关闭限制*************************************************//*
			Sheet portCloseSheet = wb.getSheet("机场关闭限制");  
			int cnt = 0;
			for (Row row : portCloseSheet) { 
				if (cnt == 0) {
					cnt++;
					continue;
				}
				
				PortCloseBean portCloseBean = new PortCloseBean();
				portCloseBean.setPort((int)row.getCell(0).getNumericCellValue());
				portCloseBean.setCloseTime(Utils.timeFormatter(row.getCell(1).getDateCellValue()).substring(11));
				portCloseBean.setOpenTime(Utils.timeFormatter(row.getCell(2).getDateCellValue()).substring(11));
				portCloseBean.setCloseDate(Utils.dateFormatter(row.getCell(3).getDateCellValue()));
				portCloseBean.setOpenDate(Utils.dateFormatter(row.getCell(4).getDateCellValue()));
				
				portCloseList.add(portCloseBean);
				
			}*/
			
//			ScheduleByAirBean scheduleByAirBean = new ScheduleByAirBean();
//			
//			for (OrgScheduleBean orgDataBean : initDataList) {
//				if (tmpAirID != orgDataBean.getAirID()) {
//					if (tmpAirID != 0) {
//						Utils.sort(scheduleByAirBean.getScheduBeanList(), "startTime", true);
//						
//						airDataList.add(scheduleByAirBean);
//					}
//					
//					tmpAirID = orgDataBean.getAirID();
//					
//					scheduleByAirBean = new ScheduleByAirBean();
//					scheduleByAirBean.setAirID(tmpAirID);
//					
//					List<OrgScheduleBean> scheduBeanList = new ArrayList<OrgScheduleBean>();
//					scheduBeanList.add(orgDataBean);
//					scheduleByAirBean.setScheduBeanList(scheduBeanList);
//					
//				} else {
//					scheduleByAirBean.getScheduBeanList().add(orgDataBean);
//				}
//				
//			}
//			
//			Utils.sort(scheduleByAirBean.getScheduBeanList(), "startTime", true);
//			airDataList.add(scheduleByAirBean);
			
			

			
			
			
			//****************************************飞行时间*************************************************//*
//			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fightTimeFile)));
//			String line;
//			cnt = 0;
//			while ((line = br.readLine()) != null ) {
//				if (cnt == 0) {
//					cnt++;
//					continue;
//				}
//				String fightTimeInfo[] = line.split(",");
//				String air = fightTimeInfo[0];
//				String startPort = fightTimeInfo[1];
//				String endPort = fightTimeInfo[2];
//				String time = fightTimeInfo[3];
//				
//				fightTimeMap.put(air + "_" + startPort + "_" + endPort, time);
//			}
			
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
	}
	

	
}
