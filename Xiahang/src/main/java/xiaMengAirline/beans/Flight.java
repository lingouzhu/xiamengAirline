package xiaMengAirline.beans;

import java.util.Calendar;
import java.util.Date;

import xiaMengAirline.util.InitData;

public class Flight implements Cloneable {
	private String flightId;
	private Date schdDate;
	private AirPort sourceAirPort;
	private AirPort desintationAirport;
	private boolean interFlg;
	private int schdNo;
	private Date arrivalTime;
	private Date departureTime;
	private double impCoe;
	private Aircraft assignedAir;
	private Aircraft plannedAir;
	
	public String getFlightId() {
		return flightId;
	}
	public void setFlightId(String id) {
		this.flightId = id;
	}
	public AirPort getSourceAirPort() {
		return sourceAirPort;
	}
	public void setSourceAirPort(AirPort sourceAirPort) {
		this.sourceAirPort = sourceAirPort;
	}
	public AirPort getDesintationAirport() {
		return desintationAirport;
	}
	public void setDesintationAirport(AirPort desintationAirport) {
		this.desintationAirport = desintationAirport;
	}
	public Date getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public Date getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(Date departureTime) {
		this.departureTime = departureTime;
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

	public double getImpCoe() {
		return impCoe;
	}
	public void setImpCoe(double impCoe) {
		this.impCoe = impCoe;
	}
	
	public boolean valdiate () {
		return true;
	}
	
	public Flight clone() throws CloneNotSupportedException {
		return (Flight) (super.clone());
	}
	
	public Date calcuateNextArrivalTime () {
		//find out flight time
		String searchKey = assignedAir.getType();
		searchKey += "_";
		searchKey += sourceAirPort.getId();
		searchKey += "_";
		searchKey += desintationAirport.getId();
		
		int flightDur =  InitData.fightTimeMap.get(searchKey);
		
	    Calendar cl = Calendar. getInstance();
	    cl.setTime(departureTime);
	    cl.add(Calendar.MINUTE, flightDur);
	    return (cl.getTime());
	}
	public Aircraft getAssignedAir() {
		return assignedAir;
	}
	public void setAssignedAir(Aircraft assignedAir) {
		this.assignedAir = assignedAir;
	}
	public Aircraft getPlannedAir() {
		return plannedAir;
	}
	public void setPlannedAir(Aircraft plannedAir) {
		this.plannedAir = plannedAir;
	}


}
