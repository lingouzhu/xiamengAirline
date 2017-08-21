package xiaMengAirline.newBranch.GlobalOptimize;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;

public interface IterativeMethod {
	public List<Aircraft> getNextDriveForIterative ();
	public void setupIterationContent (XiaMengAirlineSolution aSolution);
	public void setupIterationStragety (OptimizerStragety aStragety);
	public int getCurrentIterationNumber ();

}
