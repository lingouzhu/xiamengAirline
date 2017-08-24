package xiaMengAirline.searchEngine;

import java.util.List;

import xiaMengAirline.beans.Aircraft;

public interface IterativeSelector {
	public void setupIterationStragety (OptimizerStragety aStragety);
	public void setupCandidateList (List<Aircraft> candList);
	public Aircraft selectAircraft (Aircraft aPrimary);

}
