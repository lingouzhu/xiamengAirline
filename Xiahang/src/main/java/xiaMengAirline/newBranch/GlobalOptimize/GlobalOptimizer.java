package xiaMengAirline.newBranch.GlobalOptimize;

import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;

public interface GlobalOptimizer {
	XiaMengAirlineSolution discoverBetterSolution (XiaMengAirlineSolution aSolution);
	void setupIterativeDriver (IterativeMethod aDriver);

}
