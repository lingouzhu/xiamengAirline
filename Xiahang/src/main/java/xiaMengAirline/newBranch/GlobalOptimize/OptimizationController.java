package xiaMengAirline.newBranch.GlobalOptimize;

import java.util.ArrayList;
import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.Flight;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;
import xiaMengAirline.newBranch.BasicObject.Exception.InvaildSolution;
import xiaMengAirline.newBranch.BusinessDomain.BusinessDomainController;
import xiaMengAirline.newBranch.BusinessDomain.XiaMengSolutionValidation;
import xiaMengAirline.newBranch.LocalOptimize.LocalOptimizationController;

public class OptimizationController {
	private RestrictedCandidcateList aRCL;
	private OptimizerStragety aStragety = null;
	
	public void constructSolutionSet (XiaMengAirlineSolution aRawSolution) throws CloneNotSupportedException, InvaildSolution {
		BusinessDomainController domainController = new BusinessDomainController();
		//step1, partition raw solution into three pieces
		
		//step1a, not changeable part and changeable part
		List<XiaMengAirlineSolution> startUpSolutions = domainController.initalizeSoluion(aRawSolution.clone());
		
		//step1b, for non changeable part, calculate cost
		XiaMengAirlineSolution solutionVersion1 = startUpSolutions.get(0);
		solutionVersion1.setVersion("1");
		XiaMengSolutionValidation aValidator = new XiaMengSolutionValidation();
		aValidator.validate(solutionVersion1);
		solutionVersion1.refreshCost(false);
		
		
		//step1c, for changeable part, extract impacted flights
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
		
		//step2a, fit regular flights
		LocalOptimizationController localController = new LocalOptimizationController();
		List<XiaMengAirlineSolution> fittedSolutions = localController.fitRegularFlights(solutionVersion1, regularFlights);
		
		
		//step2b, calculate cost
		XiaMengAirlineSolution solutionVersion2 = fittedSolutions.get(0);
		solutionVersion2.setVersion("2");
		solutionVersion2.refreshCost(false);
		
		//step3, Iterative for exchange 
		//step3a, 
		
		//step3b, iterative for better solution
		IterativeSingleMethod aSingleDriver = new IterativeSingleMethod();
		List<Aircraft> driver = aSingleDriver.getDrivesForIterative(fittedSolutions.get(1));
		XiaMengAirlineSolution aBetterSolution;
		GlobalOptimizer exchangeOptimzer = new GlobalSearchExchange();
		for (int i=0; i< aStragety.getNumberOfIter();i++) {
			aBetterSolution = exchangeOptimzer.discoverBetterSolution(driver, aBetterSolution);
		}
		
		
		
		
		
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
