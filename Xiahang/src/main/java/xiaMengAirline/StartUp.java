package xiaMengAirline;


import java.text.ParseException;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.evaluator.aviation2017.Main;
import xiaMengAirline.searchEngine.LocalSearch;
import xiaMengAirline.searchEngine.SelfSearch;
import xiaMengAirline.util.InitData;

public class StartUp {

	final public static long iterLength = 1L;
	public static void main(String[] args) throws CloneNotSupportedException, ParseException, FlightDurationNotFound, AirportNotAvailable, AircraftNotAdjustable {
		
		long startTime=System.currentTimeMillis();
		//Step1, Load all data & initialize
		String initDatafile = "XiahangData.xlsx";
		
		InitData.initData(initDatafile);
		
		LocalSearch localEngine = new LocalSearch();
		SelfSearch selfEngine = new SelfSearch(InitData.originalSolution.clone());
		
		//Step2, construct initial solution & validate it
		XiaMengAirlineSolution initialSolution = selfEngine.constructInitialSolution();
		//initOutput is optional, to setup a baseline
		XiaMengAirlineSolution initialOutput = initialSolution.reConstruct();
		initialOutput.refreshCost(true);
		
		initialOutput.generateOutput(String.valueOf("0"));
		Main main = new Main();
		main.evalutor("数据森林_"+initialOutput.getStrCost()+"_0.csv");
		
		
		
//		System.out.println("Initial solution cost " + initialOutput.getCost());
//		
//		
////		if (!initialOutput.validate(false)) {
////			System.out.println("Fail to build inital solution! ");
////			return;
////		}
////		
//		//Step3, loop through to search optimized solutions
//		XiaMengAirlineSolution aBetterSolution = initialSolution;
//		for (int i = 0; i < iterLength;i++) {
//			aBetterSolution = localEngine.constructNewSolution(aBetterSolution);
//			System.out.println("Current Iter " + i + " Cost: " + aBetterSolution.getCost());
//		}
//		
//		//Step4, ensure solution is valid
//		XiaMengAirlineSolution aBetterOutput = aBetterSolution.reConstruct();
//		if (!aBetterOutput.validate(false)) {
//			System.out.println("Fail to build final solution! ");
//			return;
//		}
//		
//		//Step5, calcuate cost
//		aBetterOutput.refreshCost(true);
//		// execute time
//		long endTime=System.currentTimeMillis();
//		long mins = (endTime - startTime)/(1000* 60);
//		
//		//Step6, generate output
//		aBetterOutput.generateOutput(String.valueOf(mins));
		
	}

}
