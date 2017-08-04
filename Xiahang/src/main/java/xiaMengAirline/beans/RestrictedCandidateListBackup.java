package xiaMengAirline.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import xiaMengAirline.util.InitDataBackup;

public class RestrictedCandidateListBackup {
	public static int maxBestSolutions = 10;
	private BigDecimal lowestCost = new BigDecimal(Long.MAX_VALUE);
	private BigDecimal highestCost = new BigDecimal(-1);
	private int currentLevel = 0;
	private TreeMap<BigDecimal, List<XiaMengAirlineSolutionBackup>> bestSolutionList = new TreeMap<BigDecimal, List<XiaMengAirlineSolutionBackup>>  ();
	private String version = "0"; //Iter_Batch
	
	public boolean addSolution (XiaMengAirlineSolutionBackup aNewSolution) {
		if (bestSolutionList.size() < maxBestSolutions) {
			if (bestSolutionList.containsKey(aNewSolution.getCost())) {
				bestSolutionList.get(aNewSolution.getCost()).add(aNewSolution);
				currentLevel++;
			} else {
				List<XiaMengAirlineSolutionBackup> aSolutionList = new ArrayList<XiaMengAirlineSolutionBackup> ();
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
					List<XiaMengAirlineSolutionBackup> aSolutionList = bestSolutionList.get(aNewSolution.getCost());
					aSolutionList.add(aNewSolution);
				} else {
					// when a new lower score
					List<XiaMengAirlineSolutionBackup> aSolutionList = new ArrayList<XiaMengAirlineSolutionBackup> ();
					aSolutionList.add(aNewSolution);
					bestSolutionList.put(aNewSolution.getCost(), aSolutionList);
				}
				if (bestSolutionList.keySet().size() > 1) {
					List<XiaMengAirlineSolutionBackup> dropSolutions = bestSolutionList.remove(bestSolutionList.lastEntry().getKey());
					currentLevel= currentLevel + 1 - dropSolutions.size();
					for (XiaMengAirlineSolutionBackup aSol:dropSolutions)
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
		for(Map.Entry<BigDecimal, List<XiaMengAirlineSolutionBackup>> entry : bestSolutionList.entrySet()) {
			  List<XiaMengAirlineSolutionBackup> solutionList= entry.getValue();
			  for (XiaMengAirlineSolutionBackup aSolution:solutionList)
				  aSolution.clear();
			  solutionList.clear();
			  
		}
		bestSolutionList.clear();
	}
	
	public XiaMengAirlineSolutionBackup selectASoluiton () throws CloneNotSupportedException {
		if (!bestSolutionList.isEmpty()) {
			// randomly select one
			int lowest = 0;
			int highest = currentLevel ;
			//int selected = lowest + (int) (Math.random() * ((highest - lowest) + 1));
			
			
			List<XiaMengAirlineSolutionBackup> allNewSolutions = new ArrayList<XiaMengAirlineSolutionBackup> ();
			for(Map.Entry<BigDecimal, List<XiaMengAirlineSolutionBackup>> entry : bestSolutionList.entrySet()) {
				  allNewSolutions.addAll(entry.getValue());
			}
			
			return allNewSolutions.get(InitDataBackup.rndRcl.nextInt(allNewSolutions.size())).clone();
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

	public TreeMap<BigDecimal, List<XiaMengAirlineSolutionBackup>> getBestSolutionList() {
		return bestSolutionList;
	}

	public void setBestSolutionList(TreeMap<BigDecimal, List<XiaMengAirlineSolutionBackup>> bestSolutionList) {
		this.bestSolutionList = bestSolutionList;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
