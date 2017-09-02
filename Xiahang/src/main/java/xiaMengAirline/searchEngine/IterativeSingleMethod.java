package xiaMengAirline.searchEngine;

import java.util.ArrayList;
import java.util.List;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.XiaMengAirlineSolution;



public class IterativeSingleMethod implements IterativeMethod {
	private List<Aircraft> airList = null;
	private boolean isFresh = false;

	@Override
	public List<Aircraft> getNextDriveForIterative() {
		if (isFresh) {
			isFresh = false;
			return (airList);
		}
			
		else
			return null;
	}

	@Override
	public void setupIterationContent(XiaMengAirlineSolution aSolution) {
		airList = new ArrayList<Aircraft> ();
		List<Aircraft> aList = new ArrayList<Aircraft>(aSolution.getSchedule().values());
		for (Aircraft air:aList) {
			if (!air.getFlightChain().isEmpty())
				airList.add(air);
		}
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

	@Override
	public int getNumberOfBatches() {
		return 1;
	}

}
