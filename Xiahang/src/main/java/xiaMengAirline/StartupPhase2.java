package xiaMengAirline;

import xiaMengAirline.newBranch.BasicObject.InitData;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;
import xiaMengAirline.newBranch.GlobalOptimize.OptimizationController;
import xiaMengAirline.newBranch.GlobalOptimize.OptimizerStragety;

public class StartupPhase2 {
	public static long startTime;

	public static void main(String[] args) throws Exception {
		startTime = System.currentTimeMillis();
		System.out.println("Starting on ..." + startTime);
		// Step1, Load all data & initialize
		String initDatafile = "XiahangData20170809.xlsx";
		
		XiaMengAirlineSolution originalSolution = InitData.initData(initDatafile);
		
		//Step2, request controller to construct solution set
		OptimizationController aController = new OptimizationController();
		OptimizerStragety aStragety = new OptimizerStragety();
		aStragety.setImpactEdge(0);
		aStragety.setNumberOfSolution(20);
		aStragety.setNumberOfIter(10);
		aController.setaStragety(aStragety);
		aController.constructSolutionSet(originalSolution);
		

	}

}
