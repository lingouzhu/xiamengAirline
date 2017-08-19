package xiaMengAirline.newBranch.GlobalOptimize;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;

public interface IterativeMethod {
	List<Aircraft> getDrivesForIterative (XiaMengAirlineSolution aSolution);

}
