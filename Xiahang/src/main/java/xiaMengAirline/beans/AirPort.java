package xiaMengAirline.beans;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirPort {
	private String id;
	private boolean isInternational;
	private List<AirPortClose> closeSchedule = new ArrayList<AirPortClose>();
	private List<RegularAirPortClose> regularCloseSchedule = new ArrayList<RegularAirPortClose>();
	private Map<String, Integer> takeoffCapability = new HashMap<String, Integer> ();
	private Map<String, Integer> landingCapability = new HashMap<String, Integer> ();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean equal(AirPort anotherAirport) {
		return id.equals(anotherAirport.getId());
	}

	public FlightTime requestAirport(FlightTime requestTime, int groundingTime) throws ParseException {
		// check airport events first
		FlightTime retFlightTime = null;
		for (AirPortClose aClose : closeSchedule) {
			if (requestTime.getArrivalTime().compareTo(aClose.getStartTime()) > 0
					&& requestTime.getArrivalTime().compareTo(aClose.getEndTime()) < 0) {
				if ((aClose.getAllocatedParking() == 0) || (!aClose.isAllowForLanding())) {
					if (retFlightTime == null)
						retFlightTime = new FlightTime();
					retFlightTime.setArrivalTime(aClose.getEndTime());
					Calendar cl = Calendar.getInstance();
					cl.setTime(aClose.getEndTime());
					cl.add(Calendar.MINUTE, groundingTime);
					if (requestTime.getDepartureTime() != null) {
						if (cl.getTime().compareTo(requestTime.getDepartureTime()) > 0)
							retFlightTime.setDepartureTime(cl.getTime());
						else
							retFlightTime.setDepartureTime(requestTime.getDepartureTime());
					} else {
						retFlightTime.setDepartureTime(null);
					}

				}
			}
			if (requestTime.getDepartureTime() != null
					&& requestTime.getDepartureTime().compareTo(aClose.getStartTime()) > 0
					&& requestTime.getDepartureTime().compareTo(aClose.getEndTime()) < 0) {
				if (retFlightTime == null) {
					if (!aClose.isAllowForTakeoff()) {
						retFlightTime = new FlightTime();
						// check if enough grounding time
						Calendar cl = Calendar.getInstance();
						cl.setTime(requestTime.getArrivalTime());
						cl.add(Calendar.MINUTE, groundingTime);
						if (cl.getTime().before(aClose.getStartTime())) {
							retFlightTime.setArrivalTime(requestTime.getArrivalTime());
							retFlightTime.setDepartureTime(aClose.getStartTime());
						} else {
							retFlightTime.setArrivalTime(aClose.getEndTime());
							cl.setTime(aClose.getEndTime());
							cl.add(Calendar.MINUTE, groundingTime);
							retFlightTime.setDepartureTime(cl.getTime());
						}
					}
				}
			}
			if (retFlightTime != null)
				retFlightTime.setIsTyphoon(true);
			if ((requestTime.getDepartureTime() != null) 
					&& requestTime.getArrivalTime().before(aClose.getStartTime())
					&& ((requestTime.getDepartureTime().after(aClose.getEndTime()))
							||(requestTime.getDepartureTime().compareTo(aClose.getEndTime())) == 0)
					&& !aClose.isAllowForTakeoff()
					&& (aClose.getAllocatedParking() == 0)
					) {
				retFlightTime = new FlightTime();
				// check if enough grounding time
				Calendar cl = Calendar.getInstance();
				cl.setTime(requestTime.getArrivalTime());
				cl.add(Calendar.MINUTE, groundingTime);
				if (cl.getTime().before(aClose.getStartTime())) {
					retFlightTime.setArrivalTime(requestTime.getArrivalTime());
					retFlightTime.setDepartureTime(aClose.getStartTime());
				} else {
					retFlightTime.setArrivalTime(aClose.getEndTime());
					cl.setTime(aClose.getEndTime());
					cl.add(Calendar.MINUTE, groundingTime);
					retFlightTime.setDepartureTime(cl.getTime());
				}
			}
			// adjust departure time to closer its planned time
//			if (retFlightTime != null) {
//				if (requestTime.getDepartureTime() != null
//						&& requestTime.getDepartureTime().after(aClose.getStartTime())
//						&& retFlightTime.getDepartureTime().before(aClose.getStartTime())) {
//					retFlightTime.setDepartureTime(aClose.getStartTime());
//
//				}
//			}



		}
		if (retFlightTime == null) {
			for (RegularAirPortClose aClose : regularCloseSchedule) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				String aDateC = formatter.format(requestTime.getArrivalTime());
				String aDateO = aDateC;
				aDateC += " ";
				aDateC += aClose.getCloseTime();
				aDateO += " ";
				aDateO += aClose.getOpenTime();

				SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

				Date aCloseDate = formatter2.parse(aDateC);
				Date aOpenDate = formatter2.parse(aDateO);

				if (requestTime.getArrivalTime().after(aCloseDate) && requestTime.getArrivalTime().before(aOpenDate)) {
					retFlightTime = new FlightTime();
					retFlightTime.setArrivalTime(aOpenDate);
					Calendar cl = Calendar.getInstance();
					cl.setTime(aOpenDate);
					cl.add(Calendar.MINUTE, groundingTime);
					if (requestTime.getDepartureTime() != null) {
						if (cl.getTime().compareTo(requestTime.getDepartureTime()) > 0)
							retFlightTime.setDepartureTime(cl.getTime());
						else
							retFlightTime.setDepartureTime(requestTime.getDepartureTime());
					}

				}

				if (retFlightTime == null && requestTime.getDepartureTime() != null) {
					String dDateC = formatter.format(requestTime.getDepartureTime());
					String dDateO = dDateC;
					dDateC += " ";
					dDateC += aClose.getCloseTime();
					dDateO += " ";
					dDateO += aClose.getOpenTime();

					Date dCloseDate = formatter2.parse(dDateC);
					Date dOpenDate = formatter2.parse(dDateO);

					if (requestTime.getDepartureTime() != null && requestTime.getDepartureTime().after(dCloseDate)
							&& requestTime.getDepartureTime().before(dOpenDate)) {
						retFlightTime = new FlightTime();
						retFlightTime.setArrivalTime(requestTime.getArrivalTime());
						Calendar cl = Calendar.getInstance();
						cl.setTime(requestTime.getArrivalTime());
						cl.add(Calendar.MINUTE, groundingTime);
						if (cl.getTime().before(dOpenDate)) {
							retFlightTime.setDepartureTime(dOpenDate);
						} else {
							retFlightTime.setDepartureTime(cl.getTime());
						}

					}
				}
			}

		}

		return retFlightTime;
	}

	public List<AirPortClose> getCloseSchedule() {
		return closeSchedule;
	}

	public void setCloseSchedule(List<AirPortClose> closeSchedule) {
		this.closeSchedule = closeSchedule;
	}

	public void addCloseSchedule(AirPortClose aCloseSchedule) {
		closeSchedule.add(aCloseSchedule);
	}

	public List<RegularAirPortClose> getRegularCloseSchedule() {
		return regularCloseSchedule;
	}

	public void setRegularCloseSchedule(List<RegularAirPortClose> regularCloseSchedule) {
		this.regularCloseSchedule = regularCloseSchedule;
	}

	public void addRegularCloseSchedule(RegularAirPortClose aRegularCloseSchedule) {
		regularCloseSchedule.add(aRegularCloseSchedule);
	}

	public boolean isInternational() {
		return isInternational;
	}

	public void setInternational(boolean isInternational) {
		this.isInternational = isInternational;
	}

	public Map<String, Integer> getTakeoffCapability() {
		return takeoffCapability;
	}

	public void setTakeoffCapability(Map<String, Integer> takeoffCapability) {
		this.takeoffCapability = takeoffCapability;
	}

	public Map<String, Integer> getLandingCapability() {
		return landingCapability;
	}

	public void setLandingCapability(Map<String, Integer> landingCapability) {
		this.landingCapability = landingCapability;
	}



}
