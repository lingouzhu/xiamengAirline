package xiaMengAirline;

import java.text.ParseException;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.Exception.SolutionNotValid;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.evaluator.Main;
import xiaMengAirline.searchEngine.BusinessDomain;
import xiaMengAirline.searchEngine.OptimizationController;
import xiaMengAirline.searchEngine.OptimizerStragety;
import xiaMengAirline.searchEngine.OptimizerStragety.SELECTION;
import xiaMengAirline.utils.InitData;

public class StartupMethod1 {

	public static void main(String[] args) throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable, AircraftNotAdjustable, SolutionNotValid {
		long startTime = System.currentTimeMillis();
		// Step1, Load all data & initialize
		String initDatafile = "XiahangData20170814.xlsx";

		InitData.initData(initDatafile);
		
		OptimizerStragety myStragety = new OptimizerStragety();
		myStragety.setAbortWhenImproved(false);
		myStragety.setDebug(false);
		myStragety.setBatchSize(100);
		myStragety.setIgnoreParking(false);
		myStragety.setMaxBestSolution(10);
		myStragety.setMaxGrounding(48);
		myStragety.setNumberOfIter(1);
		myStragety.setNumberOfSolutions(1);
		myStragety.setSelectionRule(SELECTION.RANDOM);
		myStragety.setTopQueueSize(15);
		
		OptimizationController aController = new OptimizationController();
		aController.setaStragety(myStragety);
		
		XiaMengAirlineSolution aGoodSolution = aController.constructSolutionSet(InitData.originalSolution);
		
		if (!BusinessDomain.validateDuplicatedFlight(aGoodSolution))
			System.out.println("Duplicated flight!");
		
		aGoodSolution.refreshCost(true);
		aGoodSolution.generateOutput(String.valueOf("test"));
		long endTime = System.currentTimeMillis();
		long mins = (endTime - startTime) / (1000 * 60);
		System.out.println("Consumed ... " + mins);
		
		Main.evalutor("数据森林_" + aGoodSolution.getStrCost() + "_test.csv");

	}

}
