package xiaMengAirline.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import xiaMengAirline.utils.InitData;

public class RestrictedCandidateList {
	public static int maxBestSolutions = 10;
	private BigDecimal lowestCost = new BigDecimal(Long.MAX_VALUE);
	private BigDecimal highestCost = new BigDecimal(-1);
	private int currentLevel = 0;
	private TreeMap<BigDecimal, List<XiaMengAirlineSolution>> bestSolutionList = new TreeMap<BigDecimal, List<XiaMengAirlineSolution>>  ();
	private String version = "0"; //Iter_Batch
	
	public boolean addSolution (XiaMengAirlineSolution aNewSolution) {
		if (bestSolutionList.size() < maxBestSolutions) {
			if (bestSolutionList.containsKey(aNewSolution.getCost())) {
				bestSolutionList.get(aNewSolution.getCost()).add(aNewSolution);
				currentLevel++;
			} else {
				List<XiaMengAirlineSolution> aSolutionList = new ArrayList<XiaMengAirlineSolution> ();
				aSolutionList.add(aNewSolution);
				bestSolutionList.put(aNewSolution.getCost(), aSolutionList);
				currentLevel++;
				if (aNewSolution.getCost().compareTo(lowestCost) == -1) 
					lowestCost = aNewSolution.getCost();
				if (aNewSolution.getCost().compareTo(highestCost) == 1)
					highestCost = aNewSolution.getCost();
			}
			return true;
		} else {
			if (aNewSolution.getCost().compareTo(highestCost) == -1
					|| aNewSolution.getCost().compareTo(highestCost) == 0 ) {
				// when this score already existed
				if (bestSolutionList.containsKey(aNewSolution.getCost())) {
					List<XiaMengAirlineSolution> aSolutionList = bestSolutionList.get(aNewSolution.getCost());
					aSolutionList.add(aNewSolution);
				} else {
					// when a new lower score
					List<XiaMengAirlineSolution> aSolutionList = new ArrayList<XiaMengAirlineSolution> ();
					aSolutionList.add(aNewSolution);
					bestSolutionList.put(aNewSolution.getCost(), aSolutionList);
				}
				if (bestSolutionList.keySet().size() > 1) {
					List<XiaMengAirlineSolution> dropSolutions = bestSolutionList.remove(bestSolutionList.lastEntry().getKey());
					currentLevel= currentLevel + 1 - dropSolutions.size();
					for (XiaMengAirlineSolution aSol:dropSolutions)
						aSol.clear();
					if (aNewSolution.getCost().compareTo(lowestCost) == -1) 
						lowestCost = aNewSolution.getCost();
					highestCost = bestSolutionList.lastEntry().getKey();					
				} else 
					currentLevel++;
				

				
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
			int highest = currentLevel ;
			//int selected = lowest + (int) (Math.random() * ((highest - lowest) + 1));
			
			
			List<XiaMengAirlineSolution> allNewSolutions = new ArrayList<XiaMengAirlineSolution> ();
			for(Map.Entry<BigDecimal, List<XiaMengAirlineSolution>> entry : bestSolutionList.entrySet()) {
				  allNewSolutions.addAll(entry.getValue());
			}
			
			return allNewSolutions.get(InitData.rndRcl.nextInt(allNewSolutions.size())).clone();
		} else 
			return null;

	}
	
	public boolean hasSolution () {
		if (!bestSolutionList.isEmpty())
			return true;
		else
			return false;
	}

	public BigDecimal getLowestCost() {
		return lowestCost;
	}

	public void setLowestCost(BigDecimal lowestCost) {
		this.lowestCost = lowestCost;
	}

	public BigDecimal getHighestCost() {
		return highestCost;
	}

	public void setHighestCost(BigDecimal highestCost) {
		this.highestCost = highestCost;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public TreeMap<BigDecimal, List<XiaMengAirlineSolution>> getBestSolutionList() {
		return bestSolutionList;
	}

	public void setBestSolutionList(TreeMap<BigDecimal, List<XiaMengAirlineSolution>> bestSolutionList) {
		this.bestSolutionList = bestSolutionList;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
