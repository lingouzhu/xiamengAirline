package com.aliyun.tianchi.mgr.evaluate.evaluate.file.evaluator.aviation2017;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by peicheng on 17/6/29.
 */
public class ResultFlight implements Comparable{
    //航班ID
    private String flightId;
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
    //是否取消
    private boolean isCancel;
    //是否拉直
    private boolean isStraighten;
    //是否空飞
    private boolean isEmptyFly;

    public String getFlightId() {
        return flightId;
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

    public boolean isCancel() {
        return isCancel;
    }

    public boolean isStraighten() {
        return isStraighten;
    }

    public boolean isEmptyFly() {
        return isEmptyFly;
    }

    public ResultFlight(String dataLine){
        String[] columns = StringUtils.splitByWholeSeparatorPreserveAllTokens(dataLine, ",");
        if(columns.length != 9){
            throw new RuntimeException("选手上传结果数据中列数错误，不等于9项！");
        }
        flightId = columns[0];
        startAirport = columns[1];
        endAirport = columns[2];
        startDateTime = Utils.timeStringToDate(columns[3]);
        endDateTime = Utils.timeStringToDate(columns[4]);
        airplaneId = columns[5];
        isCancel = columns[6].equals("1") ? true : false;
        isStraighten = columns[7].equals("1") ? true : false;
        isEmptyFly = columns[8].equals("1") ? true : false;
    }

    @Override
    public int compareTo(Object o) {
        ResultFlight other = (ResultFlight) o;
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
}

