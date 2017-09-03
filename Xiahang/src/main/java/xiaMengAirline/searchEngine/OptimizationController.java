package xiaMengAirline.searchEngine;

import java.text.ParseException;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.Exception.SolutionNotValid;
import xiaMengAirline.beans.XiaMengAirlineSolution;


public class OptimizationController {
	private OptimizerStragety aStragety = null;
	
	public XiaMengAirlineSolution constructSolutionSet (XiaMengAirlineSolution aRawSolution) throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable, AircraftNotAdjustable, SolutionNotValid {
		SelfSearch selfEngine = new SelfSearch();
		selfEngine.setaStragety(aStragety);
		
		//step1, construct initial solution
		XiaMengAirlineSolution solutionVersion1 = selfEngine.constructInitialSolution(aRawSolution);
		System.out.println("Initial solution generated, cost " + solutionVersion1.getCost());	
		
		//step2, iteration for exchange
		IterativeMethod aBatchDriver = new IterativeBatchMethod();
		ExchangeSearch aSearch = new ExchangeSearch();
		aSearch.setupIterationStragety(aStragety);
		aSearch.setupIterativeDriver(aBatchDriver);
		aSearch.setAdjustmentEngine(selfEngine);
		
		XiaMengAirlineSolution aBetterSolution = solutionVersion1;
		for (int i=0; i< aStragety.getNumberOfIter();i++) {
			aBetterSolution = aSearch.discoverBetterSolution(aBetterSolution);
		}
		
				
		
		//step3, select multiple top solutions
//		List <XiaMengAirlineSolution> allTopSolutions = aSearch.getNeighboursResult().getAllSolutions();
//		
//		List<XiaMengAirlineSolution> selectedSolutions = new ArrayList<XiaMengAirlineSolution> ();
//		for (int i =0 ; i < aStragety.getNumberOfSolutions(); i++) {
//			selectedSolutions.add(allTopSolutions.remove(InitData.rndRcl.nextInt(allTopSolutions.size())));
//		}
		
		aBetterSolution.printOutSolution();
		
		return aBetterSolution;
		
		
		
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
