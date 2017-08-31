package xiaMengAirline.util;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.searchEngine.AdjustmentEngine;

public class MockedAdjustEngine implements AdjustmentEngine {

	@Override
	public boolean adjust(Aircraft aAir, Aircraft cacnelAir) {
		if (aAir.getId().equals("2"))
			aAir.setCost(aAir.getCost()-1);
		else 
			aAir.setCost(aAir.getCost()-3);
		return true;
	}

}
