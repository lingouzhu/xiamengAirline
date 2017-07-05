package xiaMengAirline.util;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import xiaMengAirline.beans.AirPort;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.OrgScheduleBean;
import xiaMengAirline.beans.PortCloseBean;
import xiaMengAirline.beans.ScheduleByAirBean;




public class InitData {
	
	/** init org data */
	public static List<OrgScheduleBean> initDataList = new ArrayList<OrgScheduleBean>();
	
	/** sort by air data */
	public static List<ScheduleByAirBean> airDataList = new ArrayList<ScheduleByAirBean>();

	/** air limitation list air_startPort_endPort */
	public static List<String> airLimitationList = new ArrayList<String>();
	
	/** port close list */
	public static List<PortCloseBean> portCloseList = new ArrayList<PortCloseBean>();
	
	/** flght time map key: air_startport_endport value: time */
	public static Map<String, String> fightTimeMap = new HashMap<String, String>();
	
	/** aircraft list */
	public static List<Aircraft> aircraftList = new ArrayList<Aircraft>();
	

	
	public static void initData(String initDatafile, String fightTimeFile) {
		
		try {
			
			InputStream stream = new FileInputStream(initDatafile);  
			Workbook wb = new XSSFWorkbook(stream);  
			
			/****************************************机场关闭限制*************************************************/
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
				
			}
			
			
			/****************************************航班*************************************************/
			Sheet schdSheet = wb.getSheet("航班");  
			cnt = 0;
			for (Row row : schdSheet) { 
				if (cnt == 0) {
					cnt++;
					continue;
				}
				OrgScheduleBean orgDataBean = new OrgScheduleBean();
    			orgDataBean.setSchdID((int)row.getCell(0).getNumericCellValue());
    			orgDataBean.setSchdDate(row.getCell(1).getDateCellValue());
    			orgDataBean.setInterFlg(Utils.interToBoolean(row.getCell(2).getStringCellValue()));
    			orgDataBean.setSchdNo((int)row.getCell(3).getNumericCellValue());
    			orgDataBean.setStartPort((int)row.getCell(4).getNumericCellValue());
    			orgDataBean.setEndPort((int)row.getCell(5).getNumericCellValue());
    			orgDataBean.setStartTime(row.getCell(6).getDateCellValue());
    			orgDataBean.setEndTime(row.getCell(7).getDateCellValue());
    			orgDataBean.setAirID((int)row.getCell(8).getNumericCellValue());
    			orgDataBean.setAirType((int)row.getCell(9).getNumericCellValue());
    			orgDataBean.setPassengers((int)row.getCell(10).getNumericCellValue());
    			orgDataBean.setJointPassengers((int)row.getCell(11).getNumericCellValue());
    			orgDataBean.setImpCoe(new BigDecimal(row.getCell(11).getNumericCellValue()));
    			
    			initDataList.add(orgDataBean);
				
	        }  
			
			Utils.sort(initDataList, "airID", true);
			int tmpAirID = 0;
			
			Aircraft aircraftBean = new Aircraft();
			
			
			for (OrgScheduleBean orgDataBean : initDataList) {
				Flight flight = new Flight();
				flight.setFlightId(String.valueOf(orgDataBean.getSchdID()));
				flight.setSchdDate(orgDataBean.getSchdDate());
				AirPort sourceAirPort = new AirPort();
				sourceAirPort.setId(String.valueOf(orgDataBean.getStartPort()));
				flight.setSourceAirPort(sourceAirPort);
				AirPort desintationAirport = new AirPort();
				desintationAirport.setId(String.valueOf(orgDataBean.getEndPort()));
				flight.setDesintationAirport(desintationAirport);
				flight.setSchdNo(orgDataBean.getSchdNo());
				flight.setPlannedDepartureTime(orgDataBean.getStartTime());
				flight.setPlannedArrivalTime(orgDataBean.getEndTime());
				flight.setPassengers(orgDataBean.getPassengers());
				flight.setJointPassengers(orgDataBean.getJointPassengers());
				flight.setImpCoe(orgDataBean.getImpCoe());
				
				if (tmpAirID != orgDataBean.getAirID()) {
					if (tmpAirID != 0) {
						Utils.sort(aircraftBean.getFlightChain(), "plannedDepartureTime", true);
						
						aircraftList.add(aircraftBean);
					}
					
					tmpAirID = orgDataBean.getAirID();
					
					aircraftBean = new Aircraft();
					aircraftBean.setId(String.valueOf(tmpAirID));
					aircraftBean.setType(String.valueOf(orgDataBean.getAirType()));
					
					List<Flight> flightList = new ArrayList<Flight>();
					
					
					
					flightList.add(flight);
					aircraftBean.setFlightChain(flightList);
					
				} else {
					aircraftBean.getFlightChain().add(flight);
				}
				
			}
			
			Utils.sort(aircraftBean.getFlightChain(), "plannedDepartureTime", true);
			aircraftList.add(aircraftBean);
			
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
			
			
			/****************************************航班 飞机限制*************************************************/
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
			
			
			
			/****************************************飞行时间*************************************************/
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fightTimeFile)));
			String line;
			cnt = 0;
			while ((line = br.readLine()) != null ) {
				if (cnt == 0) {
					cnt++;
					continue;
				}
				String fightTimeInfo[] = line.split(",");
				String air = fightTimeInfo[0];
				String startPort = fightTimeInfo[1];
				String endPort = fightTimeInfo[2];
				String time = fightTimeInfo[3];
				
				fightTimeMap.put(air + "_" + startPort + "_" + endPort, time);
			}
			
			for (int i = 0; i < initDataList.size(); i++) {
				OrgScheduleBean tmpBean = initDataList.get(i);
				
				String startPort = String.valueOf(tmpBean.getStartPort());
				String endPort =  String.valueOf(tmpBean.getEndPort());
				String airID =  String.valueOf(tmpBean.getAirID());
				
				if (airLimitationList.contains(airID + "_" + startPort + "_" + endPort)) {
					System.out.println(tmpBean.getSchdID());
				}
				
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
	}
	

	
}
