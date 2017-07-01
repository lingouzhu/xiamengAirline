package xiaMengAirline.util;


import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import xiaMengAirline.beans.OrgScheduleBean;




public class InitData {
	
	/** init data */
	public static List<OrgScheduleBean> initDataList = new ArrayList<OrgScheduleBean>();

	
	public static void main(String[] args) {
		String l = "C://Users//esunnen//Desktop//厦航大赛数据20170627.xlsx";
		try {
			
			InputStream stream = new FileInputStream(l);  
			Workbook wb = new XSSFWorkbook(stream);  
			
			Sheet schdSheet = wb.getSheet("航班");  
			int cnt = 0;
			for (Row row : schdSheet) { 
				if (cnt == 0) {
					cnt++;
					continue;
				}
				System.out.print(row.getCell(0).getNumericCellValue() + " ");
				System.out.print(row.getCell(1).getDateCellValue() + " ");
//				OrgScheduleBean orgDataBean = new OrgScheduleBean();
//    			orgDataBean.setSchdID(row.getCell(0).getNumericCellValue());
//    			orgDataBean.setSchdDate(str[1]);
//    			orgDataBean.setInterFlg(Utils.interToBoolean(str[2]));
//    			orgDataBean.setSchdNo(str[3]);
//    			orgDataBean.setStartPort(str[4]);
//    			orgDataBean.setEndPort(str[5]);
//    			orgDataBean.setStartTime(new Date(str[6]));
//    			orgDataBean.setEndTime(new Date(str[7]));
//    			orgDataBean.setAirID(str[8]);
//    			orgDataBean.setAirType(str[9]);
//    			orgDataBean.setImpCoe(Utils.strToBigDecimal(str[10]));
//    			
//    			initDataList.add(orgDataBean);
//				
//				
//				
//	            for (int i = 0; i <= 10; i++) {
//	            	Cell cell = row.getCell(i);
//	            	
//	            	if (cell.) {
//	            		
//	            	}
//	                System.out.print(cell.getc+"  ");  
//	            }  
	            System.out.println();  
	        }  
			
			
			
			
//			for (String line = br.readLine(); line != null; line = br.readLine()) {
//        		if (!Utils.isEmpty(line)) {
//        			String[] str = line.split(",");
//        			
//        			OrgScheduleBean orgDataBean = new OrgScheduleBean();
//        			orgDataBean.setSchdID(str[0]);
//        			orgDataBean.setSchdDate(str[1]);
//        			orgDataBean.setInterFlg(Utils.interToBoolean(str[2]));
//        			orgDataBean.setSchdNo(str[3]);
//        			orgDataBean.setStartPort(str[4]);
//        			orgDataBean.setEndPort(str[5]);
//        			orgDataBean.setStartTime(new Date(str[6]));
//        			orgDataBean.setEndTime(new Date(str[7]));
//        			orgDataBean.setAirID(str[8]);
//        			orgDataBean.setAirType(str[9]);
//        			orgDataBean.setImpCoe(Utils.strToBigDecimal(str[10]));
//        			
//        			initDataList.add(orgDataBean);
//        			
//        		}
//			}
				
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
	}
	

	
}
