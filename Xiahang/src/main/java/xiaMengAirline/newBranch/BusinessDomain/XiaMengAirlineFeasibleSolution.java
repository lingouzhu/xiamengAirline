package xiaMengAirline.newBranch.BusinessDomain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import xiaMengAirline.newBranch.LocalOptimize.AirlineAbstractedSolution;



public class XiaMengAirlineFeasibleSolution extends AirlineAbstractedSolution {
	private static final Logger logger = Logger.getLogger(XiaMengAirlineFeasibleSolution.class);
	private BigDecimal cost = new BigDecimal("0");
	private List<String> outputList = new ArrayList<String>();
	
	
	public BigDecimal getCost() {
		return cost;
	}
	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}
	
	public void addDeltaCost(BigDecimal detla) {
		
	}
	
	public void refreshCost (boolean setupOutput) {
		
	}
	
	public void generateOutput(String minutes) {
		
	}
	

}
