package xiaMengAirline.newBranch.BasicObject;

import java.util.Date;
import java.util.List;

public abstract class ResourceUnavailableEvent  {
	Date startTime;
	Date endTime;

	public PairedTime getUnavailableEventTime() {
		PairedTime retPair = new PairedTime();
		retPair.setStartTime(startTime);
		retPair.setEndTime(endTime);
		return retPair;
	}

	public abstract List<UnavailableEventType> getFixedUnavailableEventType() ;

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
