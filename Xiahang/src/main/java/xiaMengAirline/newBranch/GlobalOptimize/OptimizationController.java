package xiaMengAirline.newBranch.GlobalOptimize;

import java.util.ArrayList;
import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.Flight;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolutionCost;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;
import xiaMengAirline.newBranch.BusinessDomain.BusinessDomainController;
import xiaMengAirline.newBranch.LocalOptimize.LocalOptimizationController;

public class OptimizationController {
	private RestrictedCandidcateList aRCL;
	private OptimizerStragety aStragety = null;
	
	public void constructSolutionSet (XiaMengAirlineSolution aRawSolution) throws CloneNotSupportedException {
		BusinessDomainController domainController = new BusinessDomainController();
		//step1, partition raw solution into three pieces
		
		//step1a, not changeable part and changeable part
		List<XiaMengAirlineSolution> startUpSolutions = domainController.initalizeSoluion(aRawSolution.clone());
		
		//step1b, for changeable part, extract impacted flights
		List<Flight> regularFlights = new ArrayList<Flight> ();
		List<Aircraft> impactedAirList = new ArrayList<Aircraft> ();
		XiaMengAirlineSolution changeablePart = startUpSolutions.get(1);
		List<Aircraft> airList = new ArrayList<Aircraft> (changeablePart.getNormalSchedule().values());
		for (Aircraft aAir:airList) {
			List<Aircraft> pairedAir = domainController.splitImpactedFlights(aAir, aStragety.getImpactEdge());
			impactedAirList.add(pairedAir.get(0));
			regularFlights.addAll(pairedAir.get(1).getFlightChain());
		}
		
		//step2, pickup flights and fit into raw solution version 1
		LocalOptimizationController localController = new LocalOptimizationController();
		
		
		
		
		
	}
	
	public XiaMengAirlineSolution selectASolution (int backVersionNumber) {
		return null;
	}
	
	public XiaMengAirlineSolution selectFeasibleSolution () {
		return null;
	}

	public OptimizerStragety getaStragety() {
		return aStragety;
	}

	public void setaStragety(OptimizerStragety aStragety) {
		this.aStragety = aStragety;
	}

}
