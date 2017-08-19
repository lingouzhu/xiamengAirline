package xiaMengAirline.backup.Exception;

import xiaMengAirline.backup.beans.XiaMengAirlineSolutionBackup;

public class SolutionNotValidBackup extends Exception {
	XiaMengAirlineSolutionBackup aSolution;
	String invalidTime;

	public XiaMengAirlineSolutionBackup getaSolution() {
		return aSolution;
	}

	public void setaSolution(XiaMengAirlineSolutionBackup aSolution) {
		this.aSolution = aSolution;
	}

	public String getInvalidTime() {
		return invalidTime;
	}

	public void setInvalidTime(String invalidTime) {
		this.invalidTime = invalidTime;
	}

	public SolutionNotValidBackup(XiaMengAirlineSolutionBackup aSolution, String invalidTime) {
		super();
		this.aSolution = aSolution;
		this.invalidTime = invalidTime;
	}



}
