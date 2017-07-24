package com.aliyun.tianchi.mgr.evaluate.evaluate.file.evaluator.aviation2017;


import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.util.Date;

/**
 * Created by peicheng on 17/6/28.
 */
public class Flight implements Comparable{
    //航班ID
    private String flightId;
    //日期
    private Date date;
    //国际/国内
    private boolean isDomestic;
    //航班号
    private String flightNo;
    //起飞机场
    private String startAirport;
    //降落机场
    private String endAirport;
    //起飞时间
    private Date startDateTime;
    //降落时间
    private Date endDateTime;
    //飞机ID
    private String airplaneId;
    //机型
    private String airplaneType;
    //重要系数
    private float importRatio;

    //是否是联程航班
    private boolean isConnected = false;
    //联程航班后置航班
    private String connectedFlightId;

    public void setConnected(String connectedFlightId) {
        isConnected = true;
        this.connectedFlightId = connectedFlightId;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getConnectedFlightId() {
        return connectedFlightId;
    }

    public String getFlightId() {
        return flightId;
    }

    public Date getDate() {
        return date;
    }

    public boolean isDomestic() {
        return isDomestic;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public String getStartAirport() {
        return startAirport;
    }

    public String getEndAirport() {
        return endAirport;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public String getAirplaneId() {
        return airplaneId;
    }

    public String getAirplaneType() {
        return airplaneType;
    }

    public float getImportRatio() {
        return importRatio;
    }

    //构造函数
    public Flight(Row row){
        if(row.getPhysicalNumberOfCells() != 11){
            throw new RuntimeException("航班信息的数据列数错误，不等于11项！");
        }
        DataFormatter df = new DataFormatter();
        flightId = df.formatCellValue(row.getCell(0));
        date = row.getCell(1).getDateCellValue();
        isDomestic = df.formatCellValue(row.getCell(2)).equals("国内") ? true : false;
        flightNo = df.formatCellValue(row.getCell(3));
        startAirport = df.formatCellValue(row.getCell(4));
        endAirport = df.formatCellValue(row.getCell(5));
        startDateTime = row.getCell(6).getDateCellValue();
        endDateTime = row.getCell(7).getDateCellValue();
        airplaneId = df.formatCellValue(row.getCell(8));
        airplaneType = df.formatCellValue(row.getCell(9));
        importRatio = Float.parseFloat(df.formatCellValue(row.getCell(10)));
    }

    @Override
    public int compareTo(Object o) {
        Flight other = (Flight) o;
        if (this.airplaneId.equals(other.airplaneId)) {
            if (this.startDateTime.after(other.startDateTime)) {
                return 1;
            } else if (this.startDateTime.before(other.startDateTime)) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return this.airplaneId.compareTo(other.airplaneId);
        }
    }

    /**
     * 打印航班信息，用于生成baseline结果
     * @param cancelFlag
     * @param straightenFlag
     * @param emptyFlag
     */
    public String printFlight(int cancelFlag, int straightenFlag, int emptyFlag){
        String str = flightId + ","
                + startAirport + ","
                + endAirport + ","
                + Utils.timeStampToString(startDateTime.getTime()) + ","
                + Utils.timeStampToString(endDateTime.getTime()) + ","
                + airplaneId + ","
                + cancelFlag + ","
                + straightenFlag + ","
                + emptyFlag + "\n";
        return str;
    }
}
