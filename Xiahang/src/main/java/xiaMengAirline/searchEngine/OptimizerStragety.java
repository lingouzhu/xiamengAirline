package xiaMengAirline.searchEngine;

public class OptimizerStragety {
	public enum SELECTION {
	    RANDOM,
	    SAMETYPE;
	}
	private int numberOfIter;
	private int batchSize;
	private SELECTION selectionRule;
	private boolean abortWhenImproved;
	private int maxBestSolution; 
	private int numberOfSolutions;
	private int topQueueSize;
	private boolean isDebug = false;
	private long maxGrounding;
	

	public int getNumberOfIter() {
		return numberOfIter;
	}
	public void setNumberOfIter(int numberOfIter) {
		this.numberOfIter = numberOfIter;
	}
	public int getBatchSize() {
		return batchSize;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	public SELECTION getSelectionRule() {
		return selectionRule;
	}
	public void setSelectionRule(SELECTION selectionRule) {
		this.selectionRule = selectionRule;
	}
	public boolean isAbortWhenImproved() {
		return abortWhenImproved;
	}
	public void setAbortWhenImproved(boolean abortWhenImproved) {
		this.abortWhenImproved = abortWhenImproved;
	}
	public int getMaxBestSolution() {
		return maxBestSolution;
	}
	public void setMaxBestSolution(int maxBestSolution) {
		this.maxBestSolution = maxBestSolution;
	}
	public int getNumberOfSolutions() {
		return numberOfSolutions;
	}
	public void setNumberOfSolutions(int numberOfSolutions) {
		this.numberOfSolutions = numberOfSolutions;
	}
	public int getTopQueueSize() {
		return topQueueSize;
	}
	public void setTopQueueSize(int topQueueSize) {
		this.topQueueSize = topQueueSize;
	}
	public boolean isDebug() {
		return isDebug;
	}
	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

	public long getMaxGrounding() {
		return maxGrounding;
	}
	public void setMaxGrounding(long maxGrounding) {
		this.maxGrounding = maxGrounding;
	}
	
	

}
