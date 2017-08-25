package xiaMengAirline.searchEngine;

import java.util.List;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.XiaMengAirlineSolution;


public interface IterativeMethod {
	public List<Aircraft> getNextDriveForIterative ();
	public void setupIterationContent (XiaMengAirlineSolution aSolution);
	public void setupIterationStragety (OptimizerStragety aStragety);
	public int getCurrentIterationNumber ();
	public int getNumberOfBatches ();

}
