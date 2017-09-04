package xiaMengAirline.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class SingleSearchNode implements java.io.Serializable {
	private static final long serialVersionUID = -3279603301125480971L;
	private HashMap<String, HashMap<Integer, ArrayList<Integer>>> timeLoad;
	private ArrayList<Flight> flightList;
	
	public SingleSearchNode(HashMap<String, HashMap<Integer, ArrayList<Integer>>> tl, ArrayList<Flight> fl) throws CloneNotSupportedException {
		timeLoad = cloneTimeLoad(tl);
		flightList = cloneList(fl);
	}
	
	public HashMap<String, HashMap<Integer, ArrayList<Integer>>> cloneTimeLoad(HashMap<String, HashMap<Integer, ArrayList<Integer>>> timeload){
		HashMap<String, HashMap<Integer, ArrayList<Integer>>> newTL = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
		Iterator<Entry<String, HashMap<Integer, ArrayList<Integer>>>> itOut = timeload.entrySet().iterator();
		while (itOut.hasNext()) {
			Map.Entry<String, HashMap<Integer, ArrayList<Integer>>> pair = (Map.Entry<String, HashMap<Integer, ArrayList<Integer>>>) itOut.next();
			String keyOut = pair.getKey();
			HashMap<Integer, ArrayList<Integer>> valueOut = pair.getValue();
			HashMap<Integer, ArrayList<Integer>> newTimeMap = new HashMap<Integer, ArrayList<Integer>>();
			Iterator<Entry<Integer, ArrayList<Integer>>> itIn = valueOut.entrySet().iterator();
			while (itIn.hasNext()) {
				Map.Entry<Integer, ArrayList<Integer>> pairIn = (Map.Entry<Integer, ArrayList<Integer>>) itIn.next();
				int keyIn = pairIn.getKey();
				ArrayList<Integer> valueIn = pairIn.getValue();
				ArrayList<Integer> newFlightList = new ArrayList<Integer>();
				for (int flightId : valueIn) {
					newFlightList.add(flightId);
				}
				newTimeMap.put(keyIn, newFlightList);
			}
			newTL.put(keyOut, newTimeMap);
		}
		
		return newTL;
	}
	
	public ArrayList<Flight> cloneList(ArrayList<Flight> flights) throws CloneNotSupportedException{
		ArrayList<Flight> newList = new ArrayList<Flight>();
		for (Flight flight : flights){
			newList.add(flight.clone());
		}
		return newList;
	}
	
	public HashMap<String, HashMap<Integer, ArrayList<Integer>>> getTimeLoad(){
		return timeLoad;
	}
	
	public ArrayList<Flight> getFlightList(){
		return flightList;
	}
}
