package xiaMengAirline.Exception;

import xiaMengAirline.beans.XiaMengAirlineSolution;

public class SolutionNotValid extends Exception {
	XiaMengAirlineSolution aSolution;
	String invalidTime;

	public XiaMengAirlineSolution getaSolution() {
		return aSolution;
	}

	public void setaSolution(XiaMengAirlineSolution aSolution) {
		this.aSolution = aSolution;
	}

	public String getInvalidTime() {
		return invalidTime;
	}

	public void setInvalidTime(String invalidTime) {
		this.invalidTime = invalidTime;
	}

	public SolutionNotValid(XiaMengAirlineSolution aSolution, String invalidTime) {
		super();
		this.aSolution = aSolution;
		this.invalidTime = invalidTime;
	}



}
