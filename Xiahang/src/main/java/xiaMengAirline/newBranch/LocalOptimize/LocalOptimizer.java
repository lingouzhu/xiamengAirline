package xiaMengAirline.newBranch.LocalOptimize;

import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;
import xiaMengAirline.newBranch.GlobalOptimize.IterativeMethod;

public interface LocalOptimizer {
	XiaMengAirlineSolution constructLocalBestSolution (XiaMengAirlineSolution aRawSolution);
	void setupIterativeStragety (IterativeMethod aStragety);

}
