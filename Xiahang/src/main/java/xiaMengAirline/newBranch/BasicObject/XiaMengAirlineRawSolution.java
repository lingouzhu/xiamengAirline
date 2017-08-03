package xiaMengAirline.newBranch.BasicObject;

import xiaMengAirline.newBranch.BusinessDomain.XiaMengAirlineFeasibleSolution;
import xiaMengAirline.newBranch.LocalOptimize.AirlineAbstractedSolution;

public class XiaMengAirlineRawSolution extends AirlineAbstractedSolution {
	private XiaMengAirlineFeasibleSolution aFeasibleSolution = null;
	private int version;

	public XiaMengAirlineFeasibleSolution getaFeasibleSolution() {
		return aFeasibleSolution;
	}

	public void setaFeasibleSolution(XiaMengAirlineFeasibleSolution aFeasibleSolution) {
		this.aFeasibleSolution = aFeasibleSolution;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}
