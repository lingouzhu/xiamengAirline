package xiaMengAirline.newBranch.BasicObject.Exception;

import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;

public class InvaildSolution extends Exception {
	public enum PhaseType {
	    SPLIT_NONCHANGALBE_FROM_ORIGINAL,
	    SPLIT_IMPACT_FROM_CHANGEABLE,
	    PICKUP_FLIGHTS;
	}
	private XiaMengAirlineSolution aSolution;
	private String invalidReason;
	private PhaseType whichPhase;
	
	public InvaildSolution(XiaMengAirlineSolution aSolution, String invalidReason, PhaseType whichPhase) {
		super();
		this.aSolution = aSolution;
		this.invalidReason = invalidReason;
		this.whichPhase = whichPhase;
	}

	public XiaMengAirlineSolution getaSolution() {
		return aSolution;
	}

	public void setaSolution(XiaMengAirlineSolution aSolution) {
		this.aSolution = aSolution;
	}

	public String getInvalidReason() {
		return invalidReason;
	}

	public void setInvalidReason(String invalidReason) {
		this.invalidReason = invalidReason;
	}

	public PhaseType getWhichPhase() {
		return whichPhase;
	}

	public void setWhichPhase(PhaseType whichPhase) {
		this.whichPhase = whichPhase;
	}

}
