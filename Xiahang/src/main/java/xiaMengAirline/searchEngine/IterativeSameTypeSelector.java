package xiaMengAirline.searchEngine;

import java.util.ArrayList;
import java.util.List;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.utils.InitData;

public class IterativeSameTypeSelector implements IterativeSelector {
	private List<Aircraft> candidateList = new ArrayList<Aircraft> ();
	private OptimizerStragety aStargety;

	@Override
	public void setupIterationStragety(OptimizerStragety aStragety) {
		this.aStargety = aStragety;

	}

	@Override
	public void setupCandidateList(List<Aircraft> candList) {
		candidateList.addAll(candList);

	}

	@Override
	public Aircraft selectAircraft(Aircraft aPrimary) {
		Aircraft retAir = null;

		do {
			retAir = null;
			if (candidateList.size() == 0) {
				break;
			}
			retAir = candidateList.remove(InitData.rndNumbers.nextInt(candidateList.size()));
		} while (aPrimary.isCancel() && retAir.isCancel() || aPrimary.getId().equals(retAir.getId())
				|| !aPrimary.getType().equals(retAir.getType()));

		return retAir;
	}

}
