package xiaMengAirline.newBranch.LocalOptimize;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Flight;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;

public class LocalOptimizationController {
	/**
	 * The fit function fits regular flights available aircrafts provided by initialized solution
	 * The main goal of the fitting is to adopt shuttle bus stragety
	 * The details are,
	 * 1, Try to arrange all post-typhoon flights, once airport re-open
	 * 2, compress flights into aircrafts, to squeeze out idle aircraft for processing events, prefer on varieties of aircraft types
	 * 3, arrange enough aircrafts (or time space of aircrafts) to support high pressure of the airport reopen timing (i.e. 7/5 17:50) 
	 * 
	 * @author Data Forest
	 * @param initalizedSolution,
	 *           the solution template, contains unchangeable part.
	 * @param regularFlights
	 * 			all flights are not impacted by typhoon, need be arrange for a best fit          
	 * @return two aircraft set
	 * 			The first solution is the fitting on the initialized solution, a solution version 2
	 * 			The second solution contains set of aircrafts are idle, not used. 
	 */
	public List<XiaMengAirlineSolution> fitRegularFlights(XiaMengAirlineSolution initalizedSolution, List<Flight> regularFlights) {
		return null;
	}

}
