package xiaMengAirline.newBranch.GlobalOptimize;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import xiaMengAirline.StartupPhase2;
import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.Flight;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;
import xiaMengAirline.newBranch.BasicObject.Exception.InvaildSolution;
import xiaMengAirline.newBranch.BusinessDomain.BusinessDomainController;
import xiaMengAirline.newBranch.BusinessDomain.XiaMengSolutionValidation;
import xiaMengAirline.newBranch.LocalOptimize.LocalOptimizationController;

public class OptimizationController {
	private RestrictedCandidateList aRCL = new RestrictedCandidateList();
	private OptimizerStragety aStragety = null;
	
	public void constructSolutionSet (XiaMengAirlineSolution aRawSolution) throws CloneNotSupportedException, InvaildSolution {
		BusinessDomainController domainController = new BusinessDomainController();
		//step1, partition raw solution into three pieces
		
		//step1a, not changeable part and changeable part
		XiaMengAirlineSolution solutionVersion1 = domainController.initalizeSoluion(aRawSolution.clone());
		
		//step1b, for non changeable part, calculate cost
		solutionVersion1.setVersion("1");
		XiaMengSolutionValidation aValidator = new XiaMengSolutionValidation();
		aValidator.validate(solutionVersion1);
		solutionVersion1.refreshCost(true);
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar cal = Calendar.getInstance();
		long endTime = System.currentTimeMillis();
		long mins = (endTime - StartupPhase2.startTime) / (1000 * 60);
		System.out.println(solutionVersion1 + " consumed ... " + mins);
		System.out.println(dateFormat.format(cal));
		solutionVersion1.getaCost().generateOutput(dateFormat.format(cal) + "_" + String.valueOf(mins));
		
		
		//step1c, for changeable part, extract impacted flights
		List<Flight> regularFlights = new ArrayList<Flight> ();
		List<Aircraft> impactedAirList = new ArrayList<Aircraft> ();
		List<Aircraft> changeablePart = new ArrayList<Aircraft>(solutionVersion1.getCancelledSchedule().values());
		//empty cancel queue
		solutionVersion1.setCancelledSchedule(new HashMap<String, Aircraft>());
		for (Aircraft aAir:changeablePart) {
			List<Aircraft> pairedAir = domainController.splitImpactedFlights(aAir, aStragety.getImpactEdge());
			impactedAirList.add(pairedAir.get(0));
			regularFlights.addAll(pairedAir.get(1).getFlightChain());
		}
		
		//step2, pickup flights and fit into raw solution version 1
		
		//step2a, fit regular flights
		LocalOptimizationController localController = new LocalOptimizationController();
		XiaMengAirlineSolution solutionVersion2 = localController.fitRegularFlights(solutionVersion1, regularFlights);
		
		
		//step2b, merge cancel queue
		solutionVersion2.setVersion("2");
		for (Aircraft aAir:impactedAirList) {
			Aircraft airInSolution = solutionVersion2.getCancelAircraft(aAir.getId(), aAir.getType(), true);
			airInSolution.getFlightChain().addAll(aAir.getFlightChain());
			airInSolution.sortFlights();
		}
		aValidator.validate(solutionVersion2);
		solutionVersion2.refreshCost(true);
		cal = Calendar.getInstance();
		endTime = System.currentTimeMillis();
		mins = (endTime - StartupPhase2.startTime) / (1000 * 60);
		System.out.println(solutionVersion2.getVersion() + " consumed ... " + mins);
		System.out.println(dateFormat.format(cal));
		solutionVersion2.getaCost().generateOutput(dateFormat.format(cal) + "_" + String.valueOf(mins));		
		
		//step3, Iterative for exchange 
		//IterativeSingleMethod aSingleDriver = new IterativeSingleMethod();
		IterativeCancelledBatchMethod aBatchDriver = new IterativeCancelledBatchMethod();
		aBatchDriver.setupIterationStragety(aStragety);
		aBatchDriver.setupIterationContent(solutionVersion2);
		GlobalOptimizer exchangeOptimzer = new GlobalSearchExchange();
		exchangeOptimzer.setupIterativeDriver(aBatchDriver);
		XiaMengAirlineSolution aBetterSolution = solutionVersion2;
		for (int i=0; i< aStragety.getNumberOfIter();i++) {
			aBetterSolution = exchangeOptimzer.discoverBetterSolution(aBetterSolution);
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
