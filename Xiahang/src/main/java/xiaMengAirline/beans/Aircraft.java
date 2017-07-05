package xiaMengAirline.beans;

import java.util.ArrayList;
import java.util.List;

public class Aircraft {
	private String id;
	private String type;
	private List<Flight> flightChain;
	boolean isCancel;
	boolean isAdjusted = false;;
	long cost;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<Flight> getFlightChain() {
		return flightChain;
	}
	public Flight getFlight(int position) {
		return this.flightChain.get(position);
	}
	public void setFlightChain(List<Flight> flightChain) {
		this.flightChain = flightChain;
	}
	public void insertFlightChain (Aircraft sourceAircraft, List<Integer> addFlights, int position) {
		List<Flight> newFlights = new ArrayList<Flight> (); 
		for (int anAdd:addFlights) {
			newFlights.add(sourceAircraft.getFlight(anAdd));
		}
		this.flightChain.addAll(position,newFlights );
	}
	public void insertFlightChain (Aircraft sourceAircraft, int addFlightStartPosition, int addFlightEndPosition, int position, boolean isBefore) {
		List<Flight> newFlights = new ArrayList<Flight> (); 
		for (int i=addFlightStartPosition+1;i<=addFlightEndPosition;i++) {
			newFlights.add(sourceAircraft.getFlight(i));
		}
		if (isBefore)
			this.flightChain.addAll(position,newFlights );
		else
			this.flightChain.addAll(position+1,newFlights );
	}

	public void removeFlightChain (List<Integer> deleteFlights)  {
		for (int aDelete:deleteFlights)
			this.flightChain.remove(aDelete);
	}
	public void removeFlightChain (int removeStartPosition, int removeEndPosition)  {
		for (int i=removeStartPosition; i <= removeEndPosition; i++)
			this.flightChain.remove(i);
	}
	
	public List<AirPort>  getAirports() {
		ArrayList<AirPort> retAirPortList = new ArrayList<AirPort> ();
		for (Flight aFlight : flightChain) {
			retAirPortList.add(aFlight.getSourceAirPort());
		}
		if (!flightChain.isEmpty()) {
			//add last destination
			retAirPortList.add(flightChain.get(flightChain.size()-1).getDesintationAirport());
		}
		return (retAirPortList);
		
	}
	
	public AirPort getAirport (int position, boolean isSource) {
		if (isSource)
			return (flightChain.get(position).getSourceAirPort());
		else
			return (flightChain.get(position).getDesintationAirport());
	}
	public boolean isCancel() {
		return isCancel;
	}
	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}
	
	public Aircraft clone() {
		Aircraft aNew = this.clone();
		aNew.setAdjusted(false);
		List<Flight> newFlightChain = new ArrayList<Flight> ();
		for (Flight aFlight:flightChain) {
			newFlightChain.add(aFlight.clone());
		}
		aNew.setFlightChain(newFlightChain);
		return (aNew);
	}
	public long getCost() {
		return cost;
	}
	public void setCost(long cost) {
		this.cost = cost;
	}
	public boolean isAdjusted() {
		return isAdjusted;
	}
	public void setAdjusted(boolean isAdjusted) {
		this.isAdjusted = isAdjusted;
	}
	
	public boolean validate () {
		for (Flight aFligth:flightChain) {
			aFligth.valdiate();
		}
		return true;
		
	}
	public void adjustment  () {
		
	}

	

}
