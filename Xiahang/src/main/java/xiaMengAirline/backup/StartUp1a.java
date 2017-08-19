package xiaMengAirline.backup;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import xiaMengAirline.backup.Exception.AircraftNotAdjustableBackup;
import xiaMengAirline.backup.Exception.AirportNotAvailableBackup;
import xiaMengAirline.backup.Exception.FlightDurationNotFoundBackup;
import xiaMengAirline.backup.Exception.SolutionNotValidBackup;
import xiaMengAirline.backup.beans.AircraftBackup;
import xiaMengAirline.backup.beans.FlightBackup;
import xiaMengAirline.backup.beans.MatchedFlightBackup;
import xiaMengAirline.backup.beans.XiaMengAirlineSolutionBackup;
import xiaMengAirline.backup.evaluator.Main;
import xiaMengAirline.backup.searchEngine.LocalSearchBackup;
import xiaMengAirline.backup.searchEngine.SelfSearchBackup;
import xiaMengAirline.backup.utils.InitDataBackup;

public class StartUp1a {

	final public static long iterLength = 2L;
	final public static long preiterLength = 10L;
	final public static long postiterLength = 5L;
	final public static int preQueueSize = 15;
	final public static int postQueueSize = 10;

	public static TreeMap<Integer, List<AircraftBackup>> searchHeavyList(List<AircraftBackup> airList) {
		TreeMap<Integer, List<AircraftBackup>> topAirList = new TreeMap<Integer, List<AircraftBackup>>();

		int topOverlapped = -1;
		int bottomOverlapped = Integer.MAX_VALUE;
		int queueSize = postQueueSize;
		for (AircraftBackup aAir : airList) {
			int numberOfOverLapped = 0;
			for (AircraftBackup bAir : airList) {
				if (!aAir.getId().equals(bAir.getId())) {
					HashMap<FlightBackup, List<MatchedFlightBackup>> matchedFlights = aAir.getMatchedFlights(bAir);
					for (Map.Entry<FlightBackup, List<MatchedFlightBackup>> entry : matchedFlights.entrySet()) {
						numberOfOverLapped += entry.getValue().size();
					}
				}
			}

			if (topAirList.keySet().size() < queueSize) {
				if (topAirList.containsKey(numberOfOverLapped)) {
					topAirList.get(numberOfOverLapped).add(aAir);
				} else {
					List<AircraftBackup> list = new ArrayList<AircraftBackup>();
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
					List<AircraftBackup> list = new ArrayList<AircraftBackup>();
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

	public static TreeMap<Integer, List<AircraftBackup>> searchTopList(List<AircraftBackup> airList) {
		TreeMap<Integer, List<AircraftBackup>> topAirList = new TreeMap<Integer, List<AircraftBackup>>();

		int topOverlapped = -1;
		int bottomOverlapped = Integer.MAX_VALUE;
		int queueSize = preQueueSize;
		for (AircraftBackup aAir : airList) {
			int numberOfOverLapped = 0;
			for (AircraftBackup bAir : airList) {
				if (!aAir.getId().equals(bAir.getId())) {
					HashMap<FlightBackup, List<MatchedFlightBackup>> matchedFlights = aAir.getMatchedFlights(bAir);
					for (Map.Entry<FlightBackup, List<MatchedFlightBackup>> entry : matchedFlights.entrySet()) {
						numberOfOverLapped += entry.getValue().size();
					}
				}
			}

			if (numberOfOverLapped > 0) {
				if (topAirList.keySet().size() < queueSize) {
					if (topAirList.containsKey(numberOfOverLapped)) {
						topAirList.get(numberOfOverLapped).add(aAir);
					} else {
						List<AircraftBackup> list = new ArrayList<AircraftBackup>();
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
						List<AircraftBackup> list = new ArrayList<AircraftBackup>();
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

	public static void main(String[] args) throws CloneNotSupportedException, ParseException, FlightDurationNotFoundBackup,
			AirportNotAvailableBackup, AircraftNotAdjustableBackup {
		try {
			long startTime = System.currentTimeMillis();
			// Step1, Load all data & initialize
			String initDatafile = "XiahangData.xlsx";

			InitDataBackup.initData(initDatafile);

			LocalSearchBackup localEngine = new LocalSearchBackup();
			SelfSearchBackup selfEngine = new SelfSearchBackup(InitDataBackup.originalSolution.clone());

			// Step2, construct initial solution & validate it
			//XiaMengAirlineSolution initialSolution = InitData.originalSolution.clone();
			//XiaMengAirlineSolution initialOutput = initialSolution.getBestSolution();
			// initOutput is optional, to setup a baseline
			XiaMengAirlineSolutionBackup initialSolution = selfEngine.constructInitialSolution();
			XiaMengAirlineSolutionBackup initialOutput = initialSolution.reConstruct();
			initialOutput.refreshCost(true);
			if (initialOutput.validflightNumers3(InitDataBackup.originalSolution))
				System.out.println("Passed init!");
			else
				System.out.println("Failed init!");

			initialOutput.generateOutput(String.valueOf("aa"));
			Main main = new Main();
			main.evalutor("数据森林_" + initialOutput.getStrCost() + "_aa.csv");

			XiaMengAirlineSolutionBackup aBetterSolution = initialSolution;
			// step3a, small iteration on most important data
			List<AircraftBackup> airList = new ArrayList<AircraftBackup>(aBetterSolution.getSchedule().values());
			TreeMap<Integer, List<AircraftBackup>> topAirList = searchTopList(airList);

			int currentSize = localEngine.getBATCH_SIZE();

			List<AircraftBackup> importantAirList = new ArrayList<AircraftBackup>();
			for (Map.Entry<Integer, List<AircraftBackup>> entry : topAirList.entrySet()) {
				importantAirList.addAll(entry.getValue());
			}
			localEngine.setBATCH_SIZE(importantAirList.size());

			for (int i = 0; i < preiterLength; i++) {
				List<AircraftBackup> preIterList = new ArrayList<AircraftBackup>(importantAirList);
				aBetterSolution = localEngine.buildSolution(preIterList, aBetterSolution);
				System.out.println("Pre-Iter " + i + " Cost: " + aBetterSolution.getCost());
			}
			XiaMengAirlineSolutionBackup aBetterOutput = aBetterSolution.reConstruct();
			
			if (aBetterOutput.validflightNumers3(InitDataBackup.originalSolution))
				System.out.println("Pass Pre-Iter!");
			else
				System.out.println("Failed Pre-Iter!");

			localEngine.setBATCH_SIZE(currentSize);
			// Step3b, loop through to search optimized solutions
			
			for (int i = 0; i < iterLength; i++) {
				aBetterSolution = localEngine.constructNewSolution(aBetterSolution);
				System.out.println("Current Iter " + i + " Cost: " + aBetterSolution.getCost());
			}

			// step3c, small post iteration on most searchable data
			airList = new ArrayList<AircraftBackup>(aBetterSolution.getSchedule().values());

			topAirList = searchHeavyList(airList);

			importantAirList = new ArrayList<AircraftBackup>();
			for (Map.Entry<Integer, List<AircraftBackup>> entry : topAirList.entrySet()) {
				importantAirList.addAll(entry.getValue());
			}
			localEngine.setBATCH_SIZE(importantAirList.size());
			for (int i = 0; i < postiterLength; i++) {
				List<AircraftBackup> preIterList = new ArrayList<AircraftBackup>(importantAirList);
				aBetterSolution = localEngine.buildSolution(preIterList, aBetterSolution);
				System.out.println("Post-Iter " + i + " Cost: " + aBetterSolution.getCost());
			}
			aBetterOutput = aBetterSolution.reConstruct();
			aBetterOutput.refreshCost(true);
			aBetterOutput.generateOutput("cc");
			main = new Main();
			main.evalutor("数据森林_" + aBetterOutput.getStrCost() + "_cc.csv");
			
			//step3d, single improvement
			//XiaMengAirlineSolution finalSolution = aBetterSolution.getBestSolution();
			//re-adjust
			aBetterOutput = aBetterSolution.getBestSolution();
			if (aBetterOutput.validflightNumers3(InitDataBackup.originalSolution))
				System.out.println("Pass Iter2!");
			else
				System.out.println("Failed Iter2!");

			long endTime = System.currentTimeMillis();
			long mins = (endTime - startTime) / (1000 * 60);
			System.out.println("Consumed ... " + mins);
			
			aBetterOutput.refreshCost(true);
			aBetterOutput.generateOutput("dd");
			main = new Main();
			main.evalutor("数据森林_" + aBetterOutput.getStrCost() + "_dd.csv");

		} catch (SolutionNotValidBackup ex) {
			ex.printStackTrace();
			System.out.println("Reason: " + ex.getInvalidTime());
			XiaMengAirlineSolutionBackup aSolution = ex.getaSolution();
			aSolution.generateOutput("error");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
