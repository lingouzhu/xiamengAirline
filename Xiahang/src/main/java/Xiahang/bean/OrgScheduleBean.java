package Xiahang.bean;

import java.math.BigDecimal;
import java.util.Date;

public class OrgScheduleBean {
	
	private String schdID;
	private String schdDate;
	private boolean interFlg;
	private String schdNo;
	private String startPort;
	private String endPort;
	private Date startTime;
	private Date endTime;
	private String airID;
	private String airType;
	private BigDecimal impCoe;
	
	public String getSchdID() {
		return schdID;
	}
	public void setSchdID(String schdID) {
		this.schdID = schdID;
	}
	public String getSchdDate() {
		return schdDate;
	}
	public void setSchdDate(String schdDate) {
		this.schdDate = schdDate;
	}
	public boolean isInterFlg() {
		return interFlg;
	}
	public void setInterFlg(boolean interFlg) {
		this.interFlg = interFlg;
	}
	public String getSchdNo() {
		return schdNo;
	}
	public void setSchdNo(String schdNo) {
		this.schdNo = schdNo;
	}
	public String getStartPort() {
		return startPort;
	}
	public void setStartPort(String startPort) {
		this.startPort = startPort;
	}
	public String getEndPort() {
		return endPort;
	}
	public void setEndPort(String endPort) {
		this.endPort = endPort;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getAirID() {
		return airID;
	}
	public void setAirID(String airID) {
		this.airID = airID;
	}
	public String getAirType() {
		return airType;
	}
	public void setAirType(String airType) {
		this.airType = airType;
	}
	public BigDecimal getImpCoe() {
		return impCoe;
	}
	public void setImpCoe(BigDecimal impCoe) {
		this.impCoe = impCoe;
	}
	

}
