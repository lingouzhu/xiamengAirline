package xiaMengAirline;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import xiaMengAirline.Exception.AircraftNotAdjustable;
import xiaMengAirline.Exception.AirportNotAvailable;
import xiaMengAirline.Exception.FlightDurationNotFound;
import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.MatchedFlight;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.evaluator.aviation2017.Main;
import xiaMengAirline.searchEngine.LocalSearch;
import xiaMengAirline.searchEngine.SelfSearch;
import xiaMengAirline.util.InitData;

public class StartUp {

	final public static long iterLength = 1L;
	final public static long preiterLength = 10L;
	final public static long postiterLength = 5L;
	final public static int preQueueSize = 15;
	final public static int postQueueSize = 10;

	public static void main(String[] args) throws CloneNotSupportedException, ParseException, FlightDurationNotFound,
			AirportNotAvailable, AircraftNotAdjustable {

		long startTime = System.currentTimeMillis();
		// Step1, Load all data & initialize
		String initDatafile = "XiahangData.xlsx";

		InitData.initData(initDatafile);

		LocalSearch localEngine = new LocalSearch();
		SelfSearch selfEngine = new SelfSearch(InitData.originalSolution.clone());

		// Step2, construct initial solution & validate it
		XiaMengAirlineSolution initialSolution = selfEngine.constructInitialSolution();
		// initOutput is optional, to setup a baseline
		XiaMengAirlineSolution initialOutput = initialSolution.reConstruct();
		initialOutput.refreshCost(true);

		initialOutput.generateOutput(String.valueOf("0"));
		Main main = new Main();
		main.evalutor("数据森林_" + initialOutput.getStrCost() + "_0.csv");

		// System.out.println("Initial solution cost " +
		// initialOutput.getCost());
		//
		//
		//// if (!initialOutput.validate(false)) {
		//// System.out.println("Fail to build inital solution! ");
		//// return;
		//// }

		XiaMengAirlineSolution aBetterSolution = initialSolution;
		// step3a, small iteration on most important data
		List<Aircraft> airList = new ArrayList<Aircraft>(aBetterSolution.getSchedule().values());
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
		
		int currentSize = localEngine.getBATCH_SIZE();

		List<Aircraft> importantAirList = new ArrayList<Aircraft> ();
		for(Map.Entry<Integer, List<Aircraft>> entry : topAirList.entrySet()) {
			importantAirList.addAll(entry.getValue());
		}
		localEngine.setBATCH_SIZE(importantAirList.size());
		
		
		for (int i = 0; i < preiterLength; i++) {
			List<Aircraft> preIterList = new ArrayList<Aircraft> (importantAirList);
			aBetterSolution = localEngine.buildSolution(preIterList,aBetterSolution);
			System.out.println("Pre-Iter " + i + " Cost: " + aBetterSolution.getCost());
		}
		
		
		localEngine.setBATCH_SIZE(currentSize);
		// Step3b, loop through to search optimized solutions
		XiaMengAirlineSolution aBetterOutput;
		for (int i = 0; i < iterLength; i++) {
			aBetterSolution = localEngine.constructNewSolution(aBetterSolution);
			System.out.println("Current Iter " + i + " Cost: " + aBetterSolution.getCost());
		}
		
		// step3c, small post iteration on most searchable data
		airList = new ArrayList<Aircraft>(aBetterSolution.getSchedule().values());
		topAirList = new TreeMap<Integer, List<Aircraft>>();

		topOverlapped = -1;
		bottomOverlapped = Integer.MAX_VALUE;
		queueSize = postQueueSize;
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
					topAirList.remove(topAirList.firstEntry());
				}
				
			}
				
		}
		
		currentSize = localEngine.getBATCH_SIZE();

		importantAirList = new ArrayList<Aircraft> ();
		for(Map.Entry<Integer, List<Aircraft>> entry : topAirList.entrySet()) {
			importantAirList.addAll(entry.getValue());
		}
		localEngine.setBATCH_SIZE(importantAirList.size());
		for (int i = 0; i < postiterLength; i++) {
			List<Aircraft> preIterList = new ArrayList<Aircraft> (importantAirList);
			aBetterSolution = localEngine.buildSolution(preIterList,aBetterSolution);
			System.out.println("Post-Iter " + i + " Cost: " + aBetterSolution.getCost());
		}
		
		
		long endTime = System.currentTimeMillis();
		long mins = (endTime - startTime) / (1000 * 60);
		System.out.println("Consumed ... " + mins);
		aBetterOutput = aBetterSolution.reConstruct();
		aBetterOutput.refreshCost(true);
		aBetterOutput.generateOutput("c");
		main = new Main();
		main.evalutor("数据森林_" + aBetterOutput.getStrCost() + "_c.csv");

	}

}
