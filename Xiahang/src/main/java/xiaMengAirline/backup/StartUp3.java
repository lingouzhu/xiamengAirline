package xiaMengAirline.backup;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.Exception.SolutionNotValid;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.MatchedFlight;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.evaluator.Main;
import xiaMengAirline.searchEngine.backup.LocalSearch2;
import xiaMengAirline.utils.InitData;

public class StartUp3 {

	final public static long iterLength = 1L;
	final public static long preiterLength = 25L;
	final public static long postiterLength = 5L;
	final public static int preQueueSize = 15;
	final public static int postQueueSize = 10;

	public static TreeMap<Integer, List<Aircraft>> searchHeavyList(List<Aircraft> airList) {
		TreeMap<Integer, List<Aircraft>> topAirList = new TreeMap<Integer, List<Aircraft>>();

		int topOverlapped = -1;
		int bottomOverlapped = Integer.MAX_VALUE;
		int queueSize = postQueueSize;
		for (Aircraft aAir : airList) {
			int numberOfOverLapped = 0;
			for (Aircraft bAir : airList) {
				if (!aAir.getId().equals(bAir.getId())) {
					HashMap<Flight, List<MatchedFlight>> matchedFlights = aAir.getMatchedFlights(bAir);
					for (Map.Entry<Flight, List<MatchedFlight>> entry : matchedFlights.entrySet()) {
						numberOfOverLapped += entry.getValue().size();
					}
				}
			}

			if (topAirList.keySet().size() < queueSize) {
				if (topAirList.containsKey(numberOfOverLapped)) {
					topAirList.get(numberOfOverLapped).add(aAir);
				} else {
					List<Aircraft> list = new ArrayList<Aircraft>();
					list.add(aAir);
					topAirList.put(numberOfOverLapped, list);
				}
				if (numberOfOverLapped < bottomOverlapped) {
					bottomOverlapped = numberOfOverLapped;
				} else if (numberOfOverLapped > topOverlapped) {
					topOverlapped = numberOfOverLapped;
				}

			} else {
				if (topAirList.containsKey(numberOfOverLapped)) {
					topAirList.get(numberOfOverLapped).add(aAir);
				} else if (numberOfOverLapped > bottomOverlapped) {
					List<Aircraft> list = new ArrayList<Aircraft>();
					list.add(aAir);
					topAirList.put(numberOfOverLapped, list);
					if (numberOfOverLapped < bottomOverlapped) {
						bottomOverlapped = numberOfOverLapped;
					} else if (numberOfOverLapped > topOverlapped) {
						topOverlapped = numberOfOverLapped;
					}
				}
				if (topAirList.keySet().size() > queueSize) {
					topAirList.remove(topAirList.firstKey());
				}

			}

		}
		return topAirList;

	}

	public static TreeMap<Integer, List<Aircraft>> searchTopList(List<Aircraft> airList) {
		TreeMap<Integer, List<Aircraft>> topAirList = new TreeMap<Integer, List<Aircraft>>();

		int topOverlapped = -1;
		int bottomOverlapped = Integer.MAX_VALUE;
		int queueSize = preQueueSize;
		for (Aircraft aAir : airList) {
			int numberOfOverLapped = 0;
			for (Aircraft bAir : airList) {
				if (!aAir.getId().equals(bAir.getId())) {
					HashMap<Flight, List<MatchedFlight>> matchedFlights = aAir.getMatchedFlights(bAir);
					for (Map.Entry<Flight, List<MatchedFlight>> entry : matchedFlights.entrySet()) {
						numberOfOverLapped += entry.getValue().size();
					}
				}
			}

			if (numberOfOverLapped > 0) {
				if (topAirList.keySet().size() < queueSize) {
					if (topAirList.containsKey(numberOfOverLapped)) {
						topAirList.get(numberOfOverLapped).add(aAir);
					} else {
						List<Aircraft> list = new ArrayList<Aircraft>();
						list.add(aAir);
						topAirList.put(numberOfOverLapped, list);
					}
					if (numberOfOverLapped < bottomOverlapped) {
						bottomOverlapped = numberOfOverLapped;
					} else if (numberOfOverLapped > topOverlapped) {
						topOverlapped = numberOfOverLapped;
					}

				} else {
					if (topAirList.containsKey(numberOfOverLapped)) {
						topAirList.get(numberOfOverLapped).add(aAir);
					} else if (numberOfOverLapped < topOverlapped) {
						List<Aircraft> list = new ArrayList<Aircraft>();
						list.add(aAir);
						topAirList.put(numberOfOverLapped, list);
						if (numberOfOverLapped < bottomOverlapped) {
							bottomOverlapped = numberOfOverLapped;
						} else if (numberOfOverLapped > topOverlapped) {
							topOverlapped = numberOfOverLapped;
						}
					}

					if (topAirList.keySet().size() > queueSize) {
						topAirList.remove(topAirList.lastKey());
					}

				}
			}

		}

		return topAirList;

	}

	public static void main(String[] args) throws CloneNotSupportedException, ParseException, FlightDurationNotFound,
			AirportNotAvailable, AircraftNotAdjustable {
		try {
			long startTime = System.currentTimeMillis();
			// Step1, Load all data & initialize
			String initDatafile = "XiahangData20170814.xlsx";
			InitData.initData(initDatafile);

			LocalSearch2 localEngine = new LocalSearch2();
			
			

			// Step2, construct initial solution & validate it
			XiaMengAirlineSolution initialSolution = InitData.originalSolution.getBestSolution();
		
			XiaMengAirlineSolution initialOutput = initialSolution.reConstruct2();
			initialOutput.refreshCost(true);
			initialSolution.setCost(initialOutput.getCost());
			if (initialOutput.validflightNumers3(InitData.originalSolution))
				System.out.println("Passed init!");
			else
				System.out.println("Failed init!");

			initialOutput.generateOutput(String.valueOf("0"));
			Main main = new Main();
			main.evalutor("数据森林_" + initialOutput.getStrCost() + "_0.csv");


			XiaMengAirlineSolution aBetterSolution = initialSolution;
			// step3a, small iteration on most important data
			List<Aircraft> airList = new ArrayList<Aircraft>(aBetterSolution.getSchedule().values());
			TreeMap<Integer, List<Aircraft>> topAirList = searchTopList(airList);

			int currentSize = localEngine.getBATCH_SIZE();

			List<Aircraft> importantAirList = new ArrayList<Aircraft>();
			for (Map.Entry<Integer, List<Aircraft>> entry : topAirList.entrySet()) {
				importantAirList.addAll(entry.getValue());
			}
			localEngine.setBATCH_SIZE(importantAirList.size());

			for (int i = 0; i < preiterLength; i++) {
				List<Aircraft> preIterList = new ArrayList<Aircraft>(importantAirList);
				aBetterSolution = localEngine.buildSolution(preIterList, aBetterSolution);
				System.out.println("Pre-Iter " + i + " Cost: " + aBetterSolution.getCost());
			}
			XiaMengAirlineSolution aBetterOutput = aBetterSolution.reConstruct2();
			aBetterOutput.refreshCost(false);
			aBetterSolution.setCost(aBetterOutput.getCost());
			
			if (aBetterOutput.validflightNumers3(InitData.originalSolution))
				System.out.println("Pass Pre-Iter!");
			else
				System.out.println("Failed Pre-Iter!");

			localEngine.setBATCH_SIZE(currentSize);
			// Step3b, loop through to search optimized solutions
			
			for (int i = 0; i < iterLength; i++) {
				aBetterSolution = localEngine.constructNewSolution(aBetterSolution);
				aBetterOutput = aBetterSolution.reConstruct2();
				aBetterOutput.refreshCost(true);
				aBetterSolution.setCost(aBetterOutput.getCost());
				aBetterOutput.generateOutput("bb_" + i);
				System.out.println("Current Iter " + i + " Cost: " + aBetterSolution.getCost());
			}

			// step3c, small post iteration on most searchable data
			airList = new ArrayList<Aircraft>(aBetterSolution.getSchedule().values());

			topAirList = searchHeavyList(airList);

			importantAirList = new ArrayList<Aircraft>();
			for (Map.Entry<Integer, List<Aircraft>> entry : topAirList.entrySet()) {
				importantAirList.addAll(entry.getValue());
			}
			localEngine.setBATCH_SIZE(importantAirList.size());
			for (int i = 0; i < postiterLength; i++) {
				List<Aircraft> preIterList = new ArrayList<Aircraft>(importantAirList);
				aBetterSolution = localEngine.buildSolution(preIterList, aBetterSolution);
				System.out.println("Post-Iter " + i + " Cost: " + aBetterSolution.getCost());
			}
			aBetterOutput = aBetterSolution.reConstruct2();
			aBetterOutput.refreshCost(true);
			aBetterOutput.generateOutput("cc");
			aBetterSolution.setCost(aBetterOutput.getCost());
			main = new Main();
			main.evalutor("数据森林_" + aBetterOutput.getStrCost() + "_cc.csv");
			
			//step3d, single improvement
			//XiaMengAirlineSolution finalSolution = aBetterSolution.getBestSolution();
			//re-adjust
			aBetterSolution = aBetterSolution.getBestSolution();
			if (aBetterSolution.validAlternativeflightNumers(InitData.originalSolution))
				System.out.println("Pass Iter Single!!!");
			else
				System.out.println("Failed Iter Single!!!");
			aBetterOutput = aBetterSolution.reConstruct2();
			if (aBetterOutput.validflightNumers3(InitData.originalSolution))
				System.out.println("Pass Iter Single Constructed!");
			else
				System.out.println("Failed Iter Single Cosntructed!");
			

			long endTime = System.currentTimeMillis();
			long mins = (endTime - startTime) / (1000 * 60);
			System.out.println("Consumed ... " + mins);
			
			aBetterOutput.refreshCost(true);
			aBetterOutput.generateOutput("ee");
			main = new Main();
			main.evalutor("数据森林_" + aBetterOutput.getStrCost() + "_ee.csv");

		} catch (SolutionNotValid ex) {
			ex.printStackTrace();
			System.out.println("Reason: " + ex.getInvalidTime());
			XiaMengAirlineSolution aSolution = ex.getaSolution();
			aSolution.generateOutput("error");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
