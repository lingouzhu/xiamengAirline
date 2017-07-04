package xiaMengAirline.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class RestrictedCandidateList {
	final public static int maxBestSolutions = 20;
	private long bestScore = -1;
	private long lowestScore = Long.MAX_VALUE;
	private int currentLevel = 0;
	private TreeMap<Long, List<XiaMengAirlineSolution>> bestSolutionList = new TreeMap<Long, List<XiaMengAirlineSolution>>  ();
	
	public boolean addSolution (XiaMengAirlineSolution aNewSolution) {
		if (currentLevel < maxBestSolutions) {
			if (bestSolutionList.containsKey(aNewSolution.getCost())) {
				bestSolutionList.get(aNewSolution.getCost()).add(aNewSolution);
				currentLevel++;
			} else {
				List<XiaMengAirlineSolution> aSolutionList = new ArrayList<XiaMengAirlineSolution> ();
				aSolutionList.add(aNewSolution);
				bestSolutionList.put(aNewSolution.getCost(), aSolutionList);
				currentLevel++;
				if (aNewSolution.getCost() < bestScore) 
					bestScore = aNewSolution.getCost();
			}
			return true;
		} else {
			if (aNewSolution.getCost() <= lowestScore) {
				// when this score already existed
				if (bestSolutionList.containsKey(aNewSolution.getCost())) {
					List<XiaMengAirlineSolution> aSolutionList = bestSolutionList.get(aNewSolution.getCost());
					aSolutionList.add(aNewSolution);
					// randomly select one
					int lowest = 0;
					int highest = aSolutionList.size() - 1;
					int selected = lowest + (int) (Math.random() * ((highest - lowest) + 1));
					aSolutionList.remove(selected);

				} else {
					// when a new lower score

				}
			}
		}
		return false;
		

	}

}
