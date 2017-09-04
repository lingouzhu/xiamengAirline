package xiaMengAirline.beans;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegularAirPortClose implements java.io.Serializable {
	
	private static final long serialVersionUID = 1932349471600378623L;
	private int port;
	private String closeTime;
	private String openTime;
	private String closeDate;
	private String openDate;
	
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

	
	

}
