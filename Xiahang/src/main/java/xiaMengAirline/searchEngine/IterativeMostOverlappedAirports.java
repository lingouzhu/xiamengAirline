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

public class IterativeMostOverlappedAirports implements IterativeMethod {
	private TreeMap<Integer, List<Aircraft>> topAirList = new TreeMap<Integer, List<Aircraft>>();
	private OptimizerStragety aStragety;

	@Override
	public List<Aircraft> getNextDriveForIterative() {
		List<Aircraft> mostOverlappedAirList = new ArrayList<Aircraft>();
		for (Map.Entry<Integer, List<Aircraft>> entry : topAirList.entrySet()) {
			mostOverlappedAirList.addAll(entry.getValue());
		}
		return mostOverlappedAirList;
	}

	@Override
	public void setupIterationContent(XiaMengAirlineSolution aSolution) {
		List<Aircraft> airList = new ArrayList<Aircraft> (aSolution.getSchedule().values());
		int topOverlapped = -1;
		int bottomOverlapped = Integer.MAX_VALUE;
		int queueSize = aStragety.getTopQueueSize();
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
