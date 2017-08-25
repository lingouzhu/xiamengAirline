package xiaMengAirline.searchEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.utils.InitData;



public class IterativeBatchMethod implements IterativeMethod {
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
		List<Aircraft> airList = new ArrayList<Aircraft> (aSolution.getSchedule().values());
		numberOfBatches = (int) Math.ceil((float) airList.size() / aStragety.getBatchSize());
		for (int batchNo = 1; batchNo <= numberOfBatches; batchNo++) {
			int noOfSelected = 1;
			List<Aircraft> airBatch = new ArrayList<Aircraft>();
			while (noOfSelected <= aStragety.getBatchSize() && airList.size() > 0) {
				Aircraft air1 = airList.remove(InitData.rndNumbers.nextInt(airList.size()));
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
		return currentBatchNumber - 1;
		
	}

	@Override
	public int getNumberOfBatches() {
		return numberOfBatches;
	}

}
