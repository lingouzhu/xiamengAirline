package xiaMengAirline.util;


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
	
	public static Date addMinutes (Date aDate, int minutes) {
		Calendar cl = Calendar. getInstance();
	    cl.setTime(aDate);
	    cl.add(Calendar.MINUTE, (int) minutes);
	    return cl.getTime();
	}
	
	
	
}
