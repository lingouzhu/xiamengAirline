package xiaMengAirline.newBranch.GlobalOptimize;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;

public interface GlobalOptimizer {
	XiaMengAirlineSolution discoverBetterSolution (List<Aircraft> driver, XiaMengAirlineSolution aSolution);
	void setupIterativeStragety (IterativeMethod aStragety);

}
