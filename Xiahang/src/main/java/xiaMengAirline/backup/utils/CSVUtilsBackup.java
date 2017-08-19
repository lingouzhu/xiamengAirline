package xiaMengAirline.backup.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import xiaMengAirline.backup.beans.FlightBackup;

/**   
 * CSV操作(导出和导入)
 *
 * @author Data Forest
 */
public class CSVUtilsBackup {
    
    /**
     * 导出
     * 
     * @param file csv文件(路径+文件名)，csv文件不存在会自动创建
     * @param dataList 数据
     * @return
     */
    public static boolean exportCsv(File file, List<String> dataList){
        boolean isSucess=false;
        
        FileOutputStream out=null;
        OutputStreamWriter osw=null;
        BufferedWriter bw=null;
        try {
            out = new FileOutputStream(file);
            osw = new OutputStreamWriter(out);
            bw =new BufferedWriter(osw);
            if(dataList!=null && !dataList.isEmpty()){
                for(String data : dataList){
                    bw.append(data).append("\r");
                }
            }
            isSucess=true;
        } catch (Exception e) {
            isSucess=false;
        }finally{
            if(bw!=null){
                try {
                    bw.close();
                    bw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(osw!=null){
                try {
                    osw.close();
                    osw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(out!=null){
                try {
                    out.close();
                    out=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
        }
        
        return isSucess;
    }
    
    /**
     * 导入
     * 
     * @param file csv文件(路径+文件)
     * @return
     */
    public static List<String> importCsv(File file){
        List<String> dataList=new ArrayList<String>();
        
        BufferedReader br=null;
        try { 
            br = new BufferedReader(new FileReader(file));
            String line = ""; 
            while ((line = br.readLine()) != null) { 
                dataList.add(line);
            }
        }catch (Exception e) {
        }finally{
            if(br!=null){
                try {
                    br.close();
                    br=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
 
        return dataList;
    }
    
    
    public static String flight2Output(FlightBackup flight, String airID, String isCancel, String isStretch, String isEmpty) {
    	
    	String output = "";
    	if ("1".equals(isCancel)) {
    		output = flight.getFlightId() + "," + flight.getPlannedFlight().getSourceAirPort().getId() + ","
        			+ flight.getPlannedFlight().getDesintationAirport().getId() + "," + UtilsBackup.timeFormatter2(flight.getPlannedFlight().getDepartureTime()) 
        			+  "," + UtilsBackup.timeFormatter2(flight.getPlannedFlight().getArrivalTime()) + "," + flight.getPlannedAir().getId() + "," 
        			+ isCancel + ","+ isStretch + ","+ isEmpty;
    	} else {
    		output = flight.getFlightId() + "," + flight.getSourceAirPort().getId() + ","
        			+ flight.getDesintationAirport().getId() + "," + UtilsBackup.timeFormatter2(flight.getDepartureTime()) 
        			+  "," + UtilsBackup.timeFormatter2(flight.getArrivalTime()) + "," + airID + "," 
        			+ isCancel + ","+ isStretch + ","+ isEmpty;
    	}
    	
    	return output;
    }
}