package xiaMengAirline.searchEngine;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.utils.InitData;


public class OptimizationController {
	private OptimizerStragety aStragety = null;
	
	public List<XiaMengAirlineSolution> constructSolutionSet (XiaMengAirlineSolution aRawSolution) throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable, AircraftNotAdjustable {
		SelfSearch selfEngine = new SelfSearch();
		
		//step1, construct initial solution
		XiaMengAirlineSolution solutionVersion1 = selfEngine.constructInitialSolution(aRawSolution);
		
		//step2, iteration for exchange
		IterativeMethod aBatchDriver = new IterativeBatchMethod();
		aBatchDriver.setupIterationStragety(aStragety);
		aBatchDriver.setupIterationContent(solutionVersion1);
		ExchangeSearch aSearch = new ExchangeSearch();
		aSearch.setupIterationStragety(aStragety);
		aSearch.setupIterativeDriver(aBatchDriver);
		aSearch.setAdjustmentEngine(selfEngine);
		
		XiaMengAirlineSolution aBetterSolution = solutionVersion1;
		for (int i=0; i< aStragety.getNumberOfIter();i++) {
			aBetterSolution = aSearch.discoverBetterSolution(aBetterSolution);
		}
		
		//step3, select multiple top solutions
		List <XiaMengAirlineSolution> allTopSolutions = aSearch.getNeighboursResult().getAllSolutions();
		
		List<XiaMengAirlineSolution> selectedSolutions = new ArrayList<XiaMengAirlineSolution> ();
		for (int i =0 ; i < aStragety.getNumberOfSolutions(); i++) {
			selectedSolutions.add(allTopSolutions.remove(InitData.rndRcl.nextInt(allTopSolutions.size())));
		}
		
		return selectedSolutions;
		
		
		
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
