package xiaMengAirline.newBranch.BusinessDomain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import xiaMengAirline.newBranch.BasicObject.Aircraft;
import xiaMengAirline.newBranch.BasicObject.Flight;
import xiaMengAirline.newBranch.BasicObject.InitData;
import xiaMengAirline.newBranch.BasicObject.Utils;
import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;

public class XiaMengAirlineSolutionCost implements Cloneable {
	private static final Logger logger = Logger.getLogger(XiaMengAirlineSolutionCost.class);
	private BigDecimal cost = new BigDecimal("0");
	private List<String> outputList = new ArrayList<String>();

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public void addDeltaCost(BigDecimal detla) {

	}

	/**
	 * The refresh function calculate cost from the solution. 
	 * When airports do not connect, add empty flight cost and generate output with empty flight if required. But dont change solution data.
	 * This function shall support a solution only with a single aircraft. 
	 * 
	 * @author Data Forest
	 * @param	output, specify if need prepare output data
	 * @return 
	 */
	public void refreshCost(XiaMengAirlineSolution aSolution, boolean setupOutput) {

	}

	public void generateOutput(String minutes) {

	}

	public BigDecimal calCostbyAir(XiaMengAirlineSolution oldSolution, XiaMengAirlineSolution newSolution) {

		BigDecimal cost = new BigDecimal("0");
		List<Aircraft> oldAirs = new ArrayList<Aircraft>(oldSolution.getNormalSchedule().values());
		for (Aircraft orgAir : oldAirs) {
			Aircraft newAir = oldSolution.getAircraft(orgAir.getId(), orgAir.getType(), true);
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
						if (newFlight.getJoined1stlight()!=null && newFlight.getJoined1stlight()==newFlight) {
							if (!newFlight.getDesintationAirport().getId()
									.equals((orgFlight.getDesintationAirport().getId()))) {
								Flight nextFlight = newFlight.getJoined2ndFlight();

								cost = cost.add(new BigDecimal("750").multiply(newFlight.getImpCoe()));
								cost = cost.add(new BigDecimal("750").multiply(nextFlight.getImpCoe()));

							}

						}

					}
				}

				// cancel
				if (!existFlg) {
					// not 2nd of joint flight
					if (orgFlight.getJoined2ndFlight() !=null && orgFlight.getJoined2ndFlight() == orgFlight) {
						cost = cost.add(new BigDecimal("1000").multiply(orgFlight.getImpCoe()));
					}
				}

			}
		}

		return cost;
	}

	@Override
	public XiaMengAirlineSolutionCost clone() throws CloneNotSupportedException {
		XiaMengAirlineSolutionCost aNewCost = (XiaMengAirlineSolutionCost) super.clone();
		aNewCost.setOutputList(new ArrayList<String> (outputList));
		return aNewCost;
	}

	public List<String> getOutputList() {
		return outputList;
	}

	public void setOutputList(List<String> outputList) {
		this.outputList = outputList;
	}
	
	

}
