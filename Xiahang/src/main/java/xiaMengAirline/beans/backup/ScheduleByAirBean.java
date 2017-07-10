package xiaMengAirline.beans.backup;

import java.util.List;

public class ScheduleByAirBean {
	
	private int airID;
	private List<OrgScheduleBean> scheduBeanList;
	
	
	public int getAirID() {
		return airID;
	}
	public void setAirID(int airID) {
		this.airID = airID;
	}
	public List<OrgScheduleBean> getScheduBeanList() {
		return scheduBeanList;
	}
	public void setScheduBeanList(List<OrgScheduleBean> scheduBeanList) {
		this.scheduBeanList = scheduBeanList;
	}
	
	

}
