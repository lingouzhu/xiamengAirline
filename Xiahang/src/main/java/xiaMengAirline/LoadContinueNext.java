package xiaMengAirline;

import xiaMengAirline.beans.Aircraft;
import xiaMengAirline.beans.Flight;
import xiaMengAirline.beans.XiaMengAirlineSolution;
import xiaMengAirline.utils.InitData;
import xiaMengAirline.utils.Utils;

public class LoadContinueNext {

	public static void main(String[] args) {
		// Step1, Load all data & initialize
		String initDatafile = "XiahangData20170814.xlsx";

		InitData.initData(initDatafile);
		
		XiaMengAirlineSolution loadedSolution = (XiaMengAirlineSolution)  Utils.FileToObj("testObj");
		//check
		Aircraft air63 = loadedSolution.getAircraft("63", "2", false, false);
		Flight f666 = air63.getFlightByFlightId(666);
		System.out.println("f666 source " + f666.getSourceAirPort().getId());
		System.out.println("f666 dest " + f666.getDesintationAirport().getId());
		System.out.println("f666 departure " + Utils.timeFormatter2(f666.getDepartureTime()));
		System.out.println("f666 arrival " + Utils.timeFormatter2(f666.getArrivalTime()));

	}

}
