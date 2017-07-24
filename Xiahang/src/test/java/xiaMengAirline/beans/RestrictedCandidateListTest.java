package xiaMengAirline.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import xiaMengAirline.util.InitData;

import static org.junit.Assert.*;

public class RestrictedCandidateListTest {

	@Before
	public void setUp() throws Exception {
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
		checkList.add(new String("1"));
		checkList.add(new String("2"));
		checkList.add(new String("3"));
		checkList.add(new String("4"));
		checkList.add(new String("5"));
		checkList.add(new String("6"));
		checkList.add(new String("7"));
		checkList.add(new String("8"));
		checkList.add(new String("9"));
		checkList.add(new String("10"));
		List<String> checkOList = new ArrayList<String>(checkList);

		while (checkList.size() >= 1) {
			// randomly select first air
			String air1 = checkList.remove(InitData.rndNumbers.nextInt(checkList.size()));
			System.out.println("air 1 " + air1);
			List<String> air2CheckList = new ArrayList<String>(checkOList);
			air2CheckList.remove(air1);
			boolean isImproved = false;
			while (!isImproved && air2CheckList.size() >= 1) {
				// randomly select 2nd air
				String air2 = air2CheckList.remove(InitData.rndNumbers.nextInt(air2CheckList.size()));
				System.out.println("air 2 " + air2);
				
				if (air2.equals("8")) 
					isImproved = true;

			}
			
		}
		
	}


}
