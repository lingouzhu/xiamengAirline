package xiaMengAirline.newBranch.BasicObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import xiaMengAirline.newBranch.BusinessDomain.SeatAvailability;



public class Aircraft implements Cloneable {
	private static final Logger logger = Logger.getLogger(Aircraft.class);
	private String id;
	private String type;
	private List<Flight> flightChain = new ArrayList<Flight>();
	private boolean isCancel = false;
	private SeatAvailability seatsAvailability = null;
	
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
	public void setFlightChain(List<Flight> flightChain) {
		this.flightChain = flightChain;
	}
	
	public Flight getFlight(int position) {
		if (position >= 0)
			return this.flightChain.get(position);
		else
			return null;

	}

	public Flight getFlightByFlightId(int aFlightId) {
		for (Flight aFlight : flightChain) {
			if (aFlight.getFlightId() == aFlightId)
				return aFlight;
		}
		return null;

	}

	public List<Flight> getFlightByScheduleId(int aScheduleId) {
		List<Flight> retFlights = new ArrayList<Flight>();
		for (Flight aFlight : flightChain) {
			if (aFlight.getSchdNo() == aScheduleId) {
				retFlights.add(aFlight);
			}

		}
		return retFlights;

	}
	public void addFlight(Flight aFlight) {
		aFlight.setAssignedAir(this);
		flightChain.add(aFlight);
	}
	
	public void addFlight(int index, Flight aFlight) {
		aFlight.setAssignedAir(this);
		flightChain.add(index, aFlight);
	}
	
	public boolean hasFlight(Flight aFlight) {
		return flightChain.contains(aFlight);
	}
	
	/**
	 * The aircraft's insertFlight method inserts a flight into aircraft's
	 * flight chain. The flight must be fresh new and no referred by others.
	 * 
	 * @author Data Forest
	 * @param aFlight,
	 *            a new flight, either fresh new created or cloned.
	 * @param position,
	 *            specify the location of current flight chain. Flight will be
	 *            inserted before this position
	 * @return none
	 * @throws CloneNotSupportedException
	 */
	public void insertFlight(Flight aFlight, int position) throws CloneNotSupportedException {
		Aircraft aAir = this.clone();
		aAir.clear();
		aFlight.setPlannedAir(aAir);
		aFlight.setAssignedAir(this);
		aFlight.setPlannedFlight(aFlight.clone());
		flightChain.add(position, aFlight);
	}

	/**
	 * The aircraft's insertFlightChain method inserts a list of flight into
	 * aircraft's flight chain,
	 * 
	 * @author Data Forest
	 * @param sourceAircraft,
	 *            specify where the list of flight comes from.
	 * @param addFlights,
	 *            specify a list of flight indexes of the sourceAircraft, to be
	 *            inserted.
	 * @param position,
	 *            specify the location of current flight chain. Flight will be
	 *            inserted before this position
	 * @return none
	 */
	public void insertFlightChain(Aircraft sourceAircraft, List<Integer> addFlights, int position) {
		List<Flight> newFlights = new ArrayList<Flight>();
		for (int anAdd : addFlights) {
			sourceAircraft.getFlight(anAdd).setAssignedAir(this);
			newFlights.add(sourceAircraft.getFlight(anAdd));
		}
		this.flightChain.addAll(position, newFlights);
	}

	/**
	 * The aircraft's insertFlightChain method inserts a list of flight into
	 * aircraft's flight chain,
	 * 
	 * @author Data Forest
	 * @param sourceAircraft,
	 *            specify where the list of flight comes from.
	 * @param startFlight,
	 *            specify the first flight of source aircraft, which will be
	 *            inserted.
	 * @param endFlight,
	 *            specify the last flight of source aircraft, which will be
	 *            inserted.
	 * @param insertFlight,
	 *            specify the location of current flight chain
	 * @param isBefore,
	 *            is inserted before the insertFlight or after
	 * @return none
	 */
	public void insertFlightChain(Aircraft sourceAircraft, Flight startFlight, Flight endFlight, Flight insertFlight,
			boolean isBefore) {
		List<Flight> newFlights = new ArrayList<Flight>();
		int addFlightStartPosition = sourceAircraft.getFlightChain().indexOf(startFlight);
		int addFlightEndPosition = sourceAircraft.getFlightChain().indexOf(endFlight);
		int insertFlightPosition = this.flightChain.indexOf(insertFlight);
		for (int i = addFlightStartPosition; i <= addFlightEndPosition; i++) {
			sourceAircraft.getFlight(i).setAssignedAir(this);
			newFlights.add(sourceAircraft.getFlight(i));
		}
		if (insertFlight != null) {
			if (isBefore)
				this.flightChain.addAll(insertFlightPosition, newFlights);
			else
				this.flightChain.addAll(insertFlightPosition + 1, newFlights);
		} else
			this.flightChain.addAll(newFlights);

	}

	/**
	 * The aircraft's removeFlightChain method removes list of flights,
	 * 
	 * @author Data Forest
	 * @param deleteFlights,
	 *            specify the list of flights, to be removed
	 * @return none
	 */
	public void removeFlightChain(List<Integer> deleteFlights) {
		List<Flight> removeList = new ArrayList<Flight>();
		for (Integer i : deleteFlights)
			removeList.add(this.flightChain.get(i));

		this.flightChain.removeAll(removeList);
	}

	/**
	 * The aircraft's removeFlightChain method removes list of flights,
	 * 
	 * @author Data Forest
	 * @param startFlight,
	 *            specify the start flight, to be removed.
	 * @param endFlight,
	 *            specify the end flight, to be removed.
	 * @return none
	 */
	public void removeFlightChain(Flight startFlight, Flight endFlight) {
		List<Flight> removeList = new ArrayList<Flight>();
		int removeSFlighttartPosition = this.flightChain.indexOf(startFlight);
		int removeFlightEndPosition = this.flightChain.indexOf(endFlight);

		for (int i = removeSFlighttartPosition; i <= removeFlightEndPosition; i++)
			removeList.add(this.flightChain.get(i));

		this.flightChain.removeAll(removeList);
	}
	
	public List<Flight> getSpecifiedFlightChain(Flight startFlight, Flight endFlight) {
		List<Flight> retList = new ArrayList<Flight>();
		int retSFlighttartPosition = this.flightChain.indexOf(startFlight);
		int retFlightEndPosition = this.flightChain.indexOf(endFlight);

		for (int i = retSFlighttartPosition; i <= retFlightEndPosition; i++)
			retList.add(this.flightChain.get(i));

		return (retList);
		
	}
	public List<Airport> getAirports() {
		ArrayList<Airport> retAirPortList = new ArrayList<Airport>();
		for (Flight aFlight : flightChain) {
			retAirPortList.add(aFlight.getSourceAirPort());
		}
		if (!flightChain.isEmpty()) {
			// add last destination
			retAirPortList.add(flightChain.get(flightChain.size() - 1).getDesintationAirport());
		}
		return (retAirPortList);

	}
	public Airport getAirport(int position, boolean isSource) {
		if (isSource)
			return (flightChain.get(position).getSourceAirPort());
		else
			return (flightChain.get(position).getDesintationAirport());
	}
	
	public HashMap<Flight, List<Flight>> getCircuitFlights() {
		HashMap<Flight, List<Flight>> retCircuitList = new HashMap<Flight, List<Flight>>();

		for (Flight aFlight : flightChain) {
			ArrayList<Flight> matchedList = new ArrayList<Flight>();
			int currentPos = flightChain.indexOf(aFlight);
			String currentSourceAirport = aFlight.getSourceAirPort().getId();
			for (int j = currentPos + 1; j < flightChain.size(); j++) {
				String nextDestAirport = flightChain.get(j).getDesintationAirport().getId();
				if (currentSourceAirport.equals(nextDestAirport)) {
					matchedList.add(flightChain.get(j));
				}
			}

			if (!matchedList.isEmpty())
				retCircuitList.put(aFlight, matchedList);
		}

		return (retCircuitList);

	}
	
	public HashMap<Flight, List<MatchedFlight>> getMatchedFlights(Aircraft air2) {
		HashMap<Flight, List<MatchedFlight>> retMatchedList = new HashMap<Flight, List<MatchedFlight>>();

		for (Flight aFlight : flightChain) {
			String sourceAirPortAir1 = aFlight.getSourceAirPort().getId();
			for (Flight bFlight : air2.getFlightChain()) {
				String sourceAirPortAir2 = bFlight.getSourceAirPort().getId();
				if (sourceAirPortAir1.equals(sourceAirPortAir2)) {
					List<MatchedFlight> matchedList = new ArrayList<MatchedFlight>();
					for (int i = flightChain.indexOf(aFlight); i < flightChain.size(); i++) {
						String airPortA = getFlight(i).getDesintationAirport().getId();
						for (int j = air2.getFlightChain().indexOf(bFlight); j < air2.getFlightChain().size(); j++) {
							String airPortB = air2.getFlight(j).getDesintationAirport().getId();
							if (airPortA.equals(airPortB)) {
								MatchedFlight aMatched = new MatchedFlight();
								aMatched.setAir1SourceFlight(flightChain.indexOf(aFlight));
								aMatched.setAir1DestFlight(i);
								aMatched.setAir2SourceFlight(air2.getFlightChain().indexOf(bFlight));
								aMatched.setAir2DestFlight(j);
								matchedList.add(aMatched);
							}
						}
					}
					if (!matchedList.isEmpty()) {
						retMatchedList.put(aFlight, matchedList);
					} else {
						// means source airport overlapped but no destination
						// overlapped
						;
					}
				}
			}
		}
		return retMatchedList;
	}
	
	public void sortFlights() {
		Utils.sort(flightChain, "departureTime", true);
	}
	
	public boolean isCancel() {
		return isCancel;
	}
	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}
	public static Logger getLogger() {
		return logger;
	}

	
	public Aircraft clone() throws CloneNotSupportedException {
		Aircraft aNew = (Aircraft) super.clone();
		if (seatsAvailability!=null)
			aNew.setSeatsAvailability(seatsAvailability.clone());
		List<Flight> newFlightChain = new ArrayList<Flight>();
		for (Flight aFlight : flightChain) {
			Flight newFlight = aFlight.clone();
			newFlight.setAssignedAir(aNew);
			for (Passenger aPass:newFlight.getPassengers()) 
				aPass.setAssignedAir(aNew);
			newFlightChain.add(newFlight);
		}
		aNew.setFlightChain(newFlightChain);
		
		return (aNew);
	}
	public void clear() {
		flightChain.clear();
	}
	public SeatAvailability getSeatsAvailability() {
		return seatsAvailability;
	}
	public void setSeatsAvailability(SeatAvailability seatsAvailability) {
		this.seatsAvailability = seatsAvailability;
	}
}
