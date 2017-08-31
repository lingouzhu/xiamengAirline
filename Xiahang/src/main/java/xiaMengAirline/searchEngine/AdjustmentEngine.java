package xiaMengAirline.searchEngine;

import java.text.ParseException;

import xiaMengAirline.beans.Aircraft;

public interface AdjustmentEngine {
	public boolean adjust (Aircraft aAir, Aircraft itsCancelled) throws CloneNotSupportedException, ParseException;

}
