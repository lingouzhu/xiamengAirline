package xiaMengAirline.newBranch.BusinessDomain;

import java.util.Date;
import java.util.List;

import xiaMengAirline.newBranch.BasicObject.PairedTime;

public abstract class ResourceUnavailableEvent  {
	Date startTime;
	Date endTime;

	public PairedTime getUnavailableEventTime() {
		PairedTime retPair = new PairedTime();
		retPair.setStartTime(startTime);
		retPair.setEndTime(endTime);
		return retPair;
	}

	public abstract List<ResourceUnavailableEventType> getUnavailableEventType() ;

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

}
