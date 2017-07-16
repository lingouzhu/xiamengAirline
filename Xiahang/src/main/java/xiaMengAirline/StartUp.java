package xiaMengAirline;


import java.text.ParseException;

import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.searchEngine.LocalSearch;
import xiaMengAirline.searchEngine.SelfSearch;
import xiaMengAirline.util.InitData;

public class StartUp {

	final public static long iterLength = 10000000L;
	public static void main(String[] args) throws CloneNotSupportedException, ParseException, FlightDurationNotFound {
		
		//Step1, Load all data & initialize
		String initDatafile = "XiahangData20170705_1.xlsx";
		//String fightTimeFile = "C://Users//esunnen//Desktop//飞行时间表.csv";
		
		InitData.initData(initDatafile);
		
		LocalSearch localEngine = new LocalSearch();
		SelfSearch selfEngine = new SelfSearch();
		
		//Step2, construct initial solution & validate it
		XiaMengAirlineSolution initalSolution = selfEngine.constructInitialSolution(InitData.originalSolution);
		
		if (initalSolution.validate(false)) {
			System.out.println("Fail to build inital solution! ");
			return;
		}
		
		//Step3, loop through to search optimized solutions
		XiaMengAirlineSolution aBetterSolution = initalSolution;
		for (int i = 0; i < iterLength;i++) {
			aBetterSolution = localEngine.constructNewSolution(aBetterSolution);
		}
		
		//Step4, ensure solution is valid
		if (aBetterSolution.validate(false)) {
			System.out.println("Fail to build final solution! ");
			return;
		}
		
		//Step5, calcuate cost
		aBetterSolution.refreshCost();
		
		//Step6, generate output
		aBetterSolution.generateOutput();
			
			
		
		
		
		
		
		

	}

}
