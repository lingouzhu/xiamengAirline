package xiaMengAirline.newBranch.GlobalOptimize;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.AirlineAbstractedSolution;

public interface IterativeMethod {
	List<Aircraft> getDrivesForIterative (AirlineAbstractedSolution aSolution);

}
