package xiaMengAirline.beans.backup;

import java.math.BigDecimal;
import java.util.Date;

public class OrgScheduleBean {
	
	private int schdID;
	private Date schdDate;
	private boolean interFlg;
	private int schdNo;
	private int startPort;
	private int endPort;
	private Date startTime;
	private Date endTime;
	private int airID;
	private int airType;
	private int passengers;
	private int jointPassengers;	
	private BigDecimal impCoe;
	public int getSchdID() {
		return schdID;
	}
	public void setSchdID(int schdID) {
		this.schdID = schdID;
	}
	public Date getSchdDate() {
		return schdDate;
	}
	public void setSchdDate(Date schdDate) {
		this.schdDate = schdDate;
	}
	public boolean isInterFlg() {
		return interFlg;
	}
	public void setInterFlg(boolean interFlg) {
		this.interFlg = interFlg;
	}
	public int getSchdNo() {
		return schdNo;
	}
	public void setSchdNo(int schdNo) {
		this.schdNo = schdNo;
	}
	public int getStartPort() {
		return startPort;
	}
	public void setStartPort(int startPort) {
		this.startPort = startPort;
	}
	public int getEndPort() {
		return endPort;
	}
	public void setEndPort(int endPort) {
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
	public int getAirID() {
		return airID;
	}
	public void setAirID(int airID) {
		this.airID = airID;
	}
	public int getAirType() {
		return airType;
	}
	public void setAirType(int airType) {
		this.airType = airType;
	}
	public int getPassengers() {
		return passengers;
	}
	public void setPassengers(int passengers) {
		this.passengers = passengers;
	}
	public int getJointPassengers() {
		return jointPassengers;
	}
	public void setJointPassengers(int jointPassengers) {
		this.jointPassengers = jointPassengers;
	}
	public BigDecimal getImpCoe() {
		return impCoe;
	}
	public void setImpCoe(BigDecimal impCoe) {
		this.impCoe = impCoe;
	}
	
	
	
	

}
