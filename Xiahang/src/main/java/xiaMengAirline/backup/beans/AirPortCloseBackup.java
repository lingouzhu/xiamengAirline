package xiaMengAirline.backup.beans;

import java.util.Date;

public class AirPortCloseBackup {
	private Date startTime;
	private Date endTime;
	private boolean allowForTakeoff = true;
	private boolean allowForLanding = true;
	private int maximumParking = Integer.MAX_VALUE;
	private int allocatedParking = 0;
	
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
	public boolean isAllowForTakeoff() {
		return allowForTakeoff;
	}
	public void setAllowForTakeoff(boolean allowForTakeoff) {
		this.allowForTakeoff = allowForTakeoff;
	}
	public boolean isAllowForLanding() {
		return allowForLanding;
	}
	public void setAllowForLanding(boolean allowForLanding) {
		this.allowForLanding = allowForLanding;
	}
	public int getMaximumParking() {
		return maximumParking;
	}
	public void setMaximumParking(int maximumParking) {
		this.maximumParking = maximumParking;
	}
	public int getAllocatedParking() {
		return allocatedParking;
	}
	public void setAllocatedParking(int allocatedParking) {
		this.allocatedParking = allocatedParking;
	}
	

}
