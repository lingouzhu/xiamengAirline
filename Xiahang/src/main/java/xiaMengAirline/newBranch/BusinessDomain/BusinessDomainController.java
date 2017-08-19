package xiaMengAirline.newBranch.BusinessDomain;

import java.util.List;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;

public class BusinessDomainController {
	/**
	 * The initalizeSolution function inspects the original solution, and only keep unchangeable part as domain rules
	 * the function shall also adjust unchangeable into feasible according to business domain
	 * Ensure to put cancel flights into dropOutList, because those flights are not recyclable, but dont connect empty flight.
	 * The connection of empty flights will be processed later.
	 * 
	 * @author Data Forest
	 * @param originalSolution,
	 *           a raw solution.
	 * @return split original solution into two solutions
	 * 	unchangeable solution part as first solution, and changeable solution part as second solution
	 */
	public List<XiaMengAirlineSolution> initalizeSoluion (XiaMengAirlineSolution originalSolution) {
		return null;
	}
	
	/**
	 * The split function split flights of an aircraft into impact and not impact part.
	 * The function shall check resource availability to decide if flight impact or not.
	 * 
	 * @author Data Forest
	 * @param aircraft, an input aircraft.
	 * @param impactEdge, specify the end position of impact. 
	 * 		0 means the last impacted flight as the end of impact
	 * 		-n means the N flights before last impacted (N does not count last impacted flight)
	 * 		n means the N flights after last impacted (N does not count last impacted flight) 
	 * @return split input aircraft into two aircrafts
	 * 		first aircraft includes impacted flights
	 * 		second aircraft includes not impacted flights 
	 */
	public List<Aircraft> splitImpactedFlights (Aircraft aAircraft, int impactEdge) {
		return null;
	}

}

