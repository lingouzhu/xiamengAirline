package xiaMengAirline.beans;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AirPortBackup {
	private String id;
	private List<AirPortCloseBackup> closeSchedule = new ArrayList<AirPortCloseBackup>();
	private List<RegularAirPortCloseBackup> regularCloseSchedule = new ArrayList<RegularAirPortCloseBackup>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean equal(AirPortBackup anotherAirport) {
		return id.equals(anotherAirport.getId());
	}

	public FlightTimeBackup requestAirport(FlightTimeBackup requestTime, int groundingTime) throws ParseException {
		// check airport events first
		FlightTimeBackup retFlightTime = null;
		for (AirPortCloseBackup aClose : closeSchedule) {
			if (requestTime.getArrivalTime().compareTo(aClose.getStartTime()) > 0
					&& requestTime.getArrivalTime().compareTo(aClose.getEndTime()) < 0) {
				if ((aClose.getAllocatedParking() == 0) || (!aClose.isAllowForLanding())) {
					if (retFlightTime == null)
						retFlightTime = new FlightTimeBackup();
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
						retFlightTime = new FlightTimeBackup();
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
				retFlightTime = new FlightTimeBackup();
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
			for (RegularAirPortCloseBackup aClose : regularCloseSchedule) {
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
					retFlightTime = new FlightTimeBackup();
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
						retFlightTime = new FlightTimeBackup();
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

	public List<AirPortCloseBackup> getCloseSchedule() {
		return closeSchedule;
	}

	public void setCloseSchedule(List<AirPortCloseBackup> closeSchedule) {
		this.closeSchedule = closeSchedule;
	}

	public void addCloseSchedule(AirPortCloseBackup aCloseSchedule) {
		closeSchedule.add(aCloseSchedule);
	}

	public List<RegularAirPortCloseBackup> getRegularCloseSchedule() {
		return regularCloseSchedule;
	}

	public void setRegularCloseSchedule(List<RegularAirPortCloseBackup> regularCloseSchedule) {
		this.regularCloseSchedule = regularCloseSchedule;
	}

	public void addRegularCloseSchedule(RegularAirPortCloseBackup aRegularCloseSchedule) {
		regularCloseSchedule.add(aRegularCloseSchedule);
	}

}
