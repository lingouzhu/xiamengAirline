package xiaMengAirline.newBranch.GlobalOptimize;

import java.util.ArrayList;
import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;

public class IterativeSingleMethod implements IterativeMethod {

	@Override
	public List<Aircraft> getDrivesForIterative(XiaMengAirlineSolution aSolution) {
		return (new ArrayList<Aircraft>(aSolution.getNormalSchedule().values()));
	}

}
