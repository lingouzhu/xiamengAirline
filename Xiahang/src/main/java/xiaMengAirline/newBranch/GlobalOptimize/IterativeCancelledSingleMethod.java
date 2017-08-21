package xiaMengAirline.newBranch.GlobalOptimize;

import java.util.ArrayList;
import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;

public class IterativeCancelledSingleMethod implements IterativeMethod {
	private List<Aircraft> cancellist = null;
	private boolean isFresh = false;

	@Override
	public List<Aircraft> getNextDriveForIterative() {
		if (isFresh) {
			isFresh = false;
			return (cancellist);
		}
			
		else
			return null;
	}

	@Override
	public void setupIterationContent(XiaMengAirlineSolution aSolution) {
		cancellist = new ArrayList<Aircraft>(aSolution.getCancelledSchedule().values());
		isFresh = true;
		
	}

	@Override
	public void setupIterationStragety(OptimizerStragety aStragety) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getCurrentIterationNumber() {
		return 1;
	}

}
