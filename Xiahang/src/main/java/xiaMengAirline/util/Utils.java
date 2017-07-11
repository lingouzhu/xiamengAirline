package xiaMengAirline.util;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.comparators.ComparableComparator;

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
		if ("国内".equals(str)) {
			return true;
		} else {
			return false;
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
	
	public static String timeFormatter(Date date) {  
		String result = "";
		
		if (date != null) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			result = formatter.format(date);
			
		}
		return result;
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
		long diff = date1.getTime() - date2.getTime();
		long days = diff / (1000 * 60 * 60 * 24);  
		
		long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
		
		return new BigDecimal(hours);
    }
	
	
	public static BigDecimal normalAircraftCost(Aircraft air) {  
		BigDecimal cost = new BigDecimal("0");
		List<Flight> newFlightList = air.getFlightChain();
		
		for (Flight newFlight : newFlightList) {
			if (newFlight.getPlannedAir() == null) {
				cost.add(new BigDecimal("5000"));
			} else {
				if (!newFlight.getPlannedAir().getType().equals(air.getType())) {
					cost.add(new BigDecimal("1000").multiply(newFlight.getImpCoe()));
				}
				
				if (!newFlight.getDepartureTime().equals(newFlight.getPlannedFlight().getDepartureTime())) {
					BigDecimal hourDiff = Utils.hoursBetweenTime(newFlight.getDepartureTime(), newFlight.getPlannedFlight().getDepartureTime());
					
					if (hourDiff.signum() == -1){
						cost.add(new BigDecimal("150").multiply(hourDiff.abs()).multiply(newFlight.getImpCoe()));
					} else {
						cost.add(new BigDecimal("100").multiply(hourDiff.abs()).multiply(newFlight.getImpCoe()));
					}
				}
			}
			
		}
		
		return cost;
    }
	
	
	public static BigDecimal canceledAircraftCost(Aircraft air) {  
		BigDecimal cost = new BigDecimal("0");
		
		List<Flight> cancelFlightList = air.getFlightChain(); 
		for (Flight cancelFlight : cancelFlightList) {
			if (cancelFlight.getPlannedAir() != null) {
				cost.add(new BigDecimal("1000").multiply(cancelFlight.getImpCoe()));
			}
			
		}
		
		return cost;
    }
	
}
