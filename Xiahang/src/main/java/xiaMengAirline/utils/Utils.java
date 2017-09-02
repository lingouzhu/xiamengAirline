package xiaMengAirline.utils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.comparators.ComparableComparator;
import org.openqa.selenium.internal.seleniumemulation.IsChecked;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;

public class Utils {

	public static boolean isEmpty(String str) {
		if (str == null || "".equals(str)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean interToBoolean(String str) {
		if ("国内".equals(str) || str.equals("1")) {
			return false;
		} else {
			return true;
		}
	}

	public static BigDecimal strToBigDecimal(String str) {
		if (str == null || "".equals(str)) {
			return new BigDecimal("1");
		} else {
			return new BigDecimal(str);
		}
	}

	public static String dateFormatter(Date date) {
		String result = "";

		if (date != null) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			result = formatter.format(date);

		}
		return result;
	}

	public static Date stringFormatToTime2(String aDate) throws ParseException {
		Date result;

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		result = formatter.parse(aDate);

		return result;
	}

	public static String timeFormatToString2(Date date) {
		String result = "";

		if (date != null) {
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			result = formatter.format(date);

		}
		return result;
	}

	public static String timeFormatter(Date date) {
		String result = "";

		if (date != null) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			result = formatter.format(date);

		}
		return result;
	}

	public static String timeFormatter2(Date date) {
		String result = "";

		if (date != null) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			result = formatter.format(date);

		}
		return result;
	}

	public static Date timeStr2date(String str) {
		Date date = null;
		try {
			if (str != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				date = sdf.parse(str);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	public static Date dateStr2date(String str) {
		Date date = null;
		try {
			if (str != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				date = sdf.parse(str);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	@SuppressWarnings("unchecked")
	public static <T> void sort(List<T> list, String fieldName, boolean asc) {
		Comparator<?> mycmp = ComparableComparator.INSTANCE;
		mycmp = ComparatorUtils.nullLowComparator(mycmp); // 允许null
		if (!asc) {
			mycmp = ComparatorUtils.reversedComparator(mycmp); // 逆序
		}
		Collections.sort(list, new BeanComparator(fieldName, mycmp));
	}

	public static BigDecimal hoursBetweenTime(Date date1, Date date2) {
		BigDecimal diff = new BigDecimal(date1.getTime() - date2.getTime());
		BigDecimal hours = diff.divide(new BigDecimal(1000 * 60 * 60), 5, BigDecimal.ROUND_HALF_UP);

		return hours;
	}

	public static BigDecimal minutiesBetweenTime(Date date1, Date date2) {

		BigDecimal diff = new BigDecimal(date1.getTime() - date2.getTime());
		BigDecimal mins = diff.divide(new BigDecimal(1000 * 60), 5, BigDecimal.ROUND_HALF_UP);

		return mins;
	}

	public static Date addMinutes(Date aDate, int minutes) {
		Calendar cl = Calendar.getInstance();
		cl.setTime(aDate);
		cl.add(Calendar.MINUTE, (int) minutes);
		return cl.getTime();
	}
	
	public static Date minusMinutes(Date aDate, int minutes) {
		Calendar cl = Calendar.getInstance();
		cl.setTime(aDate);
		cl.add(Calendar.MINUTE, -minutes);
		return cl.getTime();
	}

	public static BigDecimal calCostbyAir(Aircraft orgAir, Aircraft newAir) {

		BigDecimal cost = new BigDecimal("0");
		// org air
		for (int i = 0; i < orgAir.getFlightChain().size(); i++) {

			Flight orgFlight = orgAir.getFlightChain().get(i);
			boolean existFlg = false;
			// new air
			for (int j = 0; j < newAir.getFlightChain().size(); j++) {

				Flight newFlight = newAir.getFlightChain().get(j);
				// empty
				if (i == 0 && newFlight.getFlightId() > InitData.plannedMaxFligthId) {
					cost = cost.add(new BigDecimal("5000"));
				}
				// exist
				if (orgFlight.getFlightId() == newFlight.getFlightId()) {
					existFlg = true;
					// delay or move up
					if (!orgFlight.getDepartureTime().equals(newFlight.getDepartureTime())) {
						BigDecimal hourDiff = Utils.hoursBetweenTime(newFlight.getDepartureTime(),
								orgFlight.getDepartureTime());

						if (hourDiff.signum() == -1) {
							cost = cost.add(
									new BigDecimal("150").multiply(hourDiff.abs()).multiply(orgFlight.getImpCoe()));
						} else {
							cost = cost.add(
									new BigDecimal("100").multiply(hourDiff.abs()).multiply(orgFlight.getImpCoe()));
						}
					}
					// joint stretch
					if (InitData.jointFlightMap.get(newFlight.getFlightId()) != null) {
						if (!newFlight.getDesintationAirport().getId()
								.equals((orgFlight.getDesintationAirport().getId()))) {
							Flight nextFlight = InitData.jointFlightMap.get(newFlight.getFlightId());

							cost = cost.add(new BigDecimal("750").multiply(newFlight.getImpCoe()));
							cost = cost.add(new BigDecimal("750").multiply(nextFlight.getImpCoe()));

						}

					}

				}
			}

			// cancel
			if (!existFlg) {
				// not 2nd of joint flight
				if (!InitData.jointFlightMap.containsKey(orgFlight.getFlightId())
						|| InitData.jointFlightMap.get(orgFlight.getFlightId()) != null) {
					cost = cost.add(new BigDecimal("1000").multiply(orgFlight.getImpCoe()));
				}
			}

		}

		return cost;
	}
	
	public static String build2AirKey (String air1Id, boolean isAir1Cancelled, String air2Id, boolean isAir2Cancelled) {
		int aAirId = Integer.valueOf(air1Id);
		int bAirId = Integer.valueOf(air2Id);
		String aKey;
		if (aAirId > bAirId) {
			aKey = air2Id;
			aKey += "_";
			if (isAir2Cancelled)
				aKey += "#CANCEL";
			else
				aKey += "#NORMAL";
			aKey += "_";
			aKey += air1Id;
			aKey += "_";
			if (isAir1Cancelled)
				aKey += "#CANCEL";
			else
				aKey += "#NORMAL";
		} else {
			aKey = air1Id;
			aKey += "_";
			if (isAir1Cancelled)
				aKey += "#CANCEL";
			else
				aKey += "#NORMAL";
			aKey += "_";
			aKey += air2Id;
			aKey += "_";
			if (isAir2Cancelled)
				aKey += "#CANCEL";
			else
				aKey += "#NORMAL";
		}
		return aKey;
	}

}
