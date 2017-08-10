package xiaMengAirline.newBranch.LocalOptimize;

import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineFeasibleSolution;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineRawSolution;
import xiaMengAirline.newBranch.GlobalOptimize.IterativeMethod;

public interface LocalOptimizer {
	XiaMengAirlineFeasibleSolution constructLocalBestSolution (XiaMengAirlineRawSolution aRawSolution);
	void setupIterativeStragety (IterativeMethod aStragety);

}
