package xiaMengAirline.beans.backup;

public class PortCloseBean {
	
	private int port;
	private String closeTime;
	private String openTime;
	private String closeDate;
	private String openDate;
	boolean noDepFlg;
	boolean noArrFlg;
	int stopNum;
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getCloseTime() {
		return closeTime;
	}
	public void setCloseTime(String closeTime) {
		this.closeTime = closeTime;
	}
	public String getOpenTime() {
		return openTime;
	}
	public void setOpenTime(String openTime) {
		this.openTime = openTime;
	}
	public String getCloseDate() {
		return closeDate;
	}
	public void setCloseDate(String closeDate) {
		this.closeDate = closeDate;
	}
	public String getOpenDate() {
		return openDate;
	}
	public void setOpenDate(String openDate) {
		this.openDate = openDate;
	}
	public boolean isNoDepFlg() {
		return noDepFlg;
	}
	public void setNoDepFlg(boolean noDepFlg) {
		this.noDepFlg = noDepFlg;
	}
	public boolean isNoArrFlg() {
		return noArrFlg;
	}
	public void setNoArrFlg(boolean noArrFlg) {
		this.noArrFlg = noArrFlg;
	}
	public int getStopNum() {
		return stopNum;
	}
	public void setStopNum(int stopNum) {
		this.stopNum = stopNum;
	}
	
	

}
