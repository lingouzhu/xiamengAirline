package xiaMengAirline.newBranch.GlobalOptimize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.InitData;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;

public class IterativeCancelledBatchMethod implements IterativeMethod {
	private int currentBatchNumber = 0;
	private int numberOfBatches = 0;
	private OptimizerStragety aStragety;
	private HashMap<Integer, List<Aircraft>> airBatchList = new HashMap<Integer, List<Aircraft>>();

	@Override
	public List<Aircraft> getNextDriveForIterative() {
		if (currentBatchNumber <= numberOfBatches) {
			List<Aircraft> aBatchDriver = airBatchList.get(currentBatchNumber);
			currentBatchNumber++;
			return aBatchDriver;
		} else
			return null;
	}

	@Override
	public void setupIterationContent(XiaMengAirlineSolution aSolution) {
		List<Aircraft> cancelledList = new ArrayList<Aircraft> (aSolution.getCancelledSchedule().values());
		numberOfBatches = (int) Math.ceil((float) cancelledList.size() / aStragety.getBatchSize());
		for (int batchNo = 1; batchNo <= numberOfBatches; batchNo++) {
			int noOfSelected = 1;
			List<Aircraft> airBatch = new ArrayList<Aircraft>();
			while (noOfSelected <= aStragety.getBatchSize() && cancelledList.size() > 0) {
				Aircraft air1 = cancelledList.remove(InitData.rndNumbers.nextInt(cancelledList.size()));
				airBatch.add(air1);
				noOfSelected++;
			}
			airBatchList.put(batchNo, airBatch);
		}
		currentBatchNumber = 1;
		
	}

	@Override
	public void setupIterationStragety(OptimizerStragety aStragety) {
		this.aStragety = aStragety;
		
	}

	@Override
	public int getCurrentIterationNumber() {
		return currentBatchNumber;
		
	}

}
