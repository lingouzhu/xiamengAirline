package xiaMengAirline.searchEngine;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.XiaMengAirlineSolution;

public class SelfSearch {
	public XiaMengAirlineSolution constructInitialSolution(XiaMengAirlineSolution originalSolution)
			throws CloneNotSupportedException {
		//when construct intial solution, clone a new copy
		XiaMengAirlineSolution aNewSolution = originalSolution.clone();
		
		return aNewSolution;
	}
	
	public Aircraft adjustAircraft (Aircraft originalAir) {
		//when construct new plan for aircraft, no need clone?
		return originalAir;
	}
}
