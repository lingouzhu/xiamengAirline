package xiaMengAirline.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class RestrictedCandidateList {
	final public static int maxBestSolutions = 20;
	private BigDecimal bestScore = new BigDecimal(-1);
	private BigDecimal lowestScore = new BigDecimal(Long.MAX_VALUE);
	private int currentLevel = 0;
	private TreeMap<BigDecimal, List<XiaMengAirlineSolution>> bestSolutionList = new TreeMap<BigDecimal, List<XiaMengAirlineSolution>>  ();
	
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
				if (aNewSolution.getCost().compareTo(bestScore) == -1) 
					bestScore = aNewSolution.getCost();
				if (aNewSolution.getCost().compareTo(lowestScore) == 1)
					lowestScore = aNewSolution.getCost();
			}
			return true;
		} else {
			if (aNewSolution.getCost().compareTo(lowestScore) == -1
					|| aNewSolution.getCost().compareTo(lowestScore) == 0 ) {
				// when this score already existed
				if (bestSolutionList.containsKey(aNewSolution.getCost())) {
					List<XiaMengAirlineSolution> aSolutionList = bestSolutionList.get(aNewSolution.getCost());
					aSolutionList.add(aNewSolution);
					// randomly select one
					Random rndNumbers = new Random(1234); 
//					int lowest = 0;
//					int highest = aSolutionList.size() - 1;
//					int selected = lowest + (int) (Math.random() * ((highest - lowest) + 1));
					aSolutionList.remove(rndNumbers.nextInt(aSolutionList.size())).clear();
				} else {
					// when a new lower score
					List<XiaMengAirlineSolution> aSolutionList = new ArrayList<XiaMengAirlineSolution> ();
					aSolutionList.add(aNewSolution);
					bestSolutionList.put(aNewSolution.getCost(), aSolutionList);
					bestSolutionList.remove(bestSolutionList.lastEntry()).clear();
					if (aNewSolution.getCost().compareTo(bestScore) == -1) 
						bestScore = aNewSolution.getCost();
					lowestScore = bestSolutionList.lastEntry().getKey();
				}
				return true;
			} 
		}
		return false;
		

	}
	
	public void clear () {
		for(Map.Entry<BigDecimal, List<XiaMengAirlineSolution>> entry : bestSolutionList.entrySet()) {
			  List<XiaMengAirlineSolution> solutionList= entry.getValue();
			  for (XiaMengAirlineSolution aSolution:solutionList)
				  aSolution.clear();
			  solutionList.clear();
			  
		}
		bestSolutionList.clear();
	}
	
	public XiaMengAirlineSolution selectASoluiton () throws CloneNotSupportedException {
		if (!bestSolutionList.isEmpty()) {
			// randomly select one
			int lowest = 0;
			int highest = currentLevel - 1;
			int selected = lowest + (int) (Math.random() * ((highest - lowest) + 1));
			
			
			List<XiaMengAirlineSolution> allNewSolutions = new ArrayList<XiaMengAirlineSolution> ();
			for(Map.Entry<BigDecimal, List<XiaMengAirlineSolution>> entry : bestSolutionList.entrySet()) {
				  allNewSolutions.addAll(entry.getValue());
			}
			
			return allNewSolutions.get(selected).clone();
		} else 
			return null;

	}

}
