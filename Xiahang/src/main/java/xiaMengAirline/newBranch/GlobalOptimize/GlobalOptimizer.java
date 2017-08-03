package xiaMengAirline.newBranch.GlobalOptimize;

import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineRawSolution;

public interface GlobalOptimizer {
	XiaMengAirlineRawSolution discoverBetterSolution (XiaMengAirlineRawSolution aSolution);
	void setupIterativeStragety (IterativeMethod aStragety);

}
