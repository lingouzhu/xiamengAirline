package xiaMengAirline.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.util.InitData;

import static org.junit.Assert.*;

public class RestrictedCandidateListTest {

	@Before
	public void setUp() throws Exception {
		RestrictedCandidateList.maxBestSolutions = 3;
	}

	@Test
	public void testAddSolution() throws CloneNotSupportedException {
		XiaMengAirlineSolution sol1 = new XiaMengAirlineSolution();
		sol1.setCost(new BigDecimal(100));

		XiaMengAirlineSolution sol2 = new XiaMengAirlineSolution();
		sol2.setCost(new BigDecimal(200));

		XiaMengAirlineSolution sol3 = new XiaMengAirlineSolution();
		sol3.setCost(new BigDecimal(150));

		XiaMengAirlineSolution sol4 = new XiaMengAirlineSolution();
		sol4.setCost(new BigDecimal(250));

		XiaMengAirlineSolution sol5 = new XiaMengAirlineSolution();
		sol5.setCost(new BigDecimal(50));

		XiaMengAirlineSolution sol6 = new XiaMengAirlineSolution();
		sol6.setCost(new BigDecimal(100));

		XiaMengAirlineSolution sol7 = new XiaMengAirlineSolution();
		sol7.setCost(new BigDecimal(70));

		RestrictedCandidateList rcl = new RestrictedCandidateList();
		rcl.addSolution(sol1);
		assertEquals(100, rcl.getHighestCost().longValue());
		assertEquals(100, rcl.getLowestCost().longValue());
		assertEquals(1, rcl.getCurrentLevel());

		rcl.addSolution(sol2);
		assertEquals(200, rcl.getHighestCost().longValue());
		assertEquals(100, rcl.getLowestCost().longValue());
		assertEquals(2, rcl.getCurrentLevel());

		rcl.addSolution(sol3);
		assertEquals(200, rcl.getHighestCost().longValue());
		assertEquals(100, rcl.getLowestCost().longValue());
		assertEquals(3, rcl.getCurrentLevel());

		rcl.addSolution(sol4);
		assertEquals(200, rcl.getHighestCost().longValue());
		assertEquals(100, rcl.getLowestCost().longValue());
		assertEquals(3, rcl.getCurrentLevel());

		rcl.addSolution(sol5);
		assertEquals(150, rcl.getHighestCost().longValue());
		assertEquals(50, rcl.getLowestCost().longValue());
		assertEquals(3, rcl.getCurrentLevel());

		System.out.println("Random select sol " + rcl.selectASoluiton().getCost());

		rcl.addSolution(sol6);
		assertEquals(100, rcl.getHighestCost().longValue());
		assertEquals(50, rcl.getLowestCost().longValue());
		assertEquals(3, rcl.getCurrentLevel());

		rcl.addSolution(sol7);
		assertEquals(70, rcl.getHighestCost().longValue());
		assertEquals(50, rcl.getLowestCost().longValue());
		assertEquals(2, rcl.getCurrentLevel());

		List<String> checkList = new ArrayList<String>();
		checkList.add(new String("1_0"));
		checkList.add(new String("2_0"));
		checkList.add(new String("3_0"));
		checkList.add(new String("4_0"));
		checkList.add(new String("5_0"));
		checkList.add(new String("6_0"));
		checkList.add(new String("7_0"));
		checkList.add(new String("8_0"));
		checkList.add(new String("9_0"));
		checkList.add(new String("10_0"));
		List<String> checkSList = new ArrayList<String>(checkList);

		List<String> bestSolution = new ArrayList<String>();
		for (String aAir : checkList) {
			bestSolution.add(new String(aAir));
		}

		// build batch list for first air
		HashMap<Integer, List<String>> airBatchList = new HashMap<Integer, List<String>>();
		int numberOfBatches = (int) Math.ceil((float) checkSList.size() / 3);
		for (int batchNo = 1; batchNo <= numberOfBatches; batchNo++) {
			int noOfSelected = 1;
			List<String> airBatch = new ArrayList<String>();
			while (noOfSelected <= 3 && checkSList.size() > 0) {
				String air1 = checkSList.remove(InitData.rndNumbers.nextInt(checkSList.size()));
				airBatch.add(air1);
				noOfSelected++;
			}
			airBatchList.put(batchNo, airBatch);
		}

		for (Map.Entry<Integer, List<String>> entry : airBatchList.entrySet()) {
			int key = entry.getKey();
			List<String> value = entry.getValue();

			for (String aFlight : value) {
				System.out.println("Batch " + key + " selected " + aFlight);
			}
		}
		
		List<String> betterSolution = new ArrayList<String> ();

		for (Map.Entry<Integer, List<String>> entry : airBatchList.entrySet()) {
			int currentBatch = entry.getKey();
			List<String> firstAirList = entry.getValue();
			List<String> firstAirNewList = new ArrayList<String>();
			// rebuild search list as from latest best solution
			for (String air1 : firstAirList) {
				String bItem = air1.substring(0,air1.indexOf('_'));
				for (String aAir : bestSolution) {
					String aItem = aAir.substring(0, aAir.indexOf('_'));
					if (aItem.equals(bItem)) {
						air1 = aAir;
						break;
					}
				}
				
				firstAirNewList.add(air1);
			}


			System.out.println("Processing batch ... " + currentBatch);
			for (String air1 : firstAirNewList) {
				// randomly select first air
				System.out.println("Processing first air " + air1);
				List<String> air2CheckList = new ArrayList<String>(bestSolution);
				air2CheckList.remove(air1);
				boolean isImproved = false;
				while (!isImproved && air2CheckList.size() >= 1) {
					// randomly select 2nd air
					String air2 = air2CheckList.remove(InitData.rndNumbers.nextInt(air2CheckList.size()));

					System.out.println("Processing second air " + air2);
					String aItem = air2.substring(0, air2.indexOf('_'));
					if (aItem.equals("8") ) {
						betterSolution.clear();
						for (String aAir : bestSolution) {
							betterSolution.add(new String(aAir + "_"+ currentBatch));
						}
						isImproved = true;
					}

					
				}
			}
			bestSolution = new ArrayList<String>(betterSolution);
			betterSolution.clear();
			System.out.println("Completed batch ... " + currentBatch);
		}
		
		System.out.println("Final best solution ...");
		for (String aAir : bestSolution) {
			System.out.println(aAir);
		}

	}

}
