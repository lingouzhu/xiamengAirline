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
	

	
	public static void main(String[] args) {
		String initDatafile = "C://Users//esunnen//Desktop//厦航大赛数据20170627.xlsx";
		String fightTimeFile = "C://Users//esunnen//Desktop//飞行时间表.csv";
		
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
			ScheduleByAirBean scheduleByAirBean = new ScheduleByAirBean();
			
			for (OrgScheduleBean orgDataBean : initDataList) {
				if (tmpAirID != orgDataBean.getAirID()) {
					if (tmpAirID != 0) {
						Utils.sort(scheduleByAirBean.getScheduBeanList(), "startTime", true);
						
						airDataList.add(scheduleByAirBean);
					}
					
					tmpAirID = orgDataBean.getAirID();
					
					scheduleByAirBean = new ScheduleByAirBean();
					scheduleByAirBean.setAirID(tmpAirID);
					
					List<OrgScheduleBean> scheduBeanList = new ArrayList<OrgScheduleBean>();
					scheduBeanList.add(orgDataBean);
					scheduleByAirBean.setScheduBeanList(scheduBeanList);
					
				} else {
					scheduleByAirBean.getScheduBeanList().add(orgDataBean);
				}
				
			}
			
			Utils.sort(scheduleByAirBean.getScheduBeanList(), "startTime", true);
			airDataList.add(scheduleByAirBean);
			
			
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
			
			/****************************************机场关闭限制*************************************************/
			Sheet portCloseSheet = wb.getSheet("机场关闭限制");  
			cnt = 0;
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
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
	}
	

	
}
