package xiaMengAirline.searchEngine;

import xiaMengAirline.beans.XiaMengAirlineSolution;

public class SelfSearch {
	public XiaMengAirlineSolution constructInitialSolution(XiaMengAirlineSolution originalSolution)
			throws CloneNotSupportedException {
		XiaMengAirlineSolution aNewSolution = originalSolution.clone();
		
		return aNewSolution;
	}
}
