package xiaMengAirline.searchEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.MatchedFlight;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.utils.Utils;

public class IterativeLeastOverlappedAirports implements IterativeMethod {
	private TreeMap<Integer, List<Aircraft>> topAirList = new TreeMap<Integer, List<Aircraft>>();
	private OptimizerStragety aStragety;

	@Override
	public List<Aircraft> getNextDriveForIterative() {
		List<Aircraft> leastOverlappedAirList = new ArrayList<Aircraft>();
		for (Map.Entry<Integer, List<Aircraft>> entry : topAirList.entrySet()) {
			leastOverlappedAirList.addAll(entry.getValue());
		}
		return leastOverlappedAirList;
	}

	@Override
	public void setupIterationContent(XiaMengAirlineSolution aSolution) {
		List<Aircraft> airList = new ArrayList<Aircraft> (aSolution.getSchedule().values());
		Map<String, Integer> overlappedMap = new HashMap<String, Integer>();
		int topOverlapped = -1;
		int bottomOverlapped = Integer.MAX_VALUE;
		int queueSize = aStragety.getTopQueueSize();
		for (Aircraft aAir : airList) {
			int numberOfOverLapped = 0;
			for (Aircraft bAir : airList) {
				if (!aAir.getId().equals(bAir.getId())) {
					String aKey = Utils.build2AirKey(aAir.getId(), bAir.getId());;
					
					if (overlappedMap.containsKey(aKey)) {
						numberOfOverLapped += overlappedMap.get(aKey);
					} else {
						HashMap<Flight, List<MatchedFlight>> matchedFlights = aAir.getMatchedFlights(bAir);
						int nOverlappedFortheAir = 0;
						for (Map.Entry<Flight, List<MatchedFlight>> entry : matchedFlights.entrySet()) {
							numberOfOverLapped += entry.getValue().size();
							nOverlappedFortheAir += entry.getValue().size();
						}
						overlappedMap.put(aKey, nOverlappedFortheAir);
					}
					//System.out.println("air " + aAir.getId() + " air " + bAir.getId() + " value " + overlappedMap.get(aKey));
				}
			}
			//System.out.println("air " + aAir.getId() + " total: " + numberOfOverLapped);
			

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
					} 
					if (numberOfOverLapped > topOverlapped) {
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
						} 
						if (numberOfOverLapped > topOverlapped) {
							topOverlapped = numberOfOverLapped;
						}
					}

					if (topAirList.keySet().size() > queueSize) {
						topAirList.remove(topAirList.lastKey());
					}

				}
			}

		}

	}

	@Override
	public void setupIterationStragety(OptimizerStragety aStragety) {
		this.aStragety = aStragety;

	}

	@Override
	public int getCurrentIterationNumber() {
		return 1;
	}

	@Override
	public int getNumberOfBatches() {
		return 1;
	}

}
