package xiaMengAirline.newBranch.BasicObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class XiaMengAirlineFeasibleSolution extends AirlineAbstractedSolution {
	private static final Logger logger = Logger.getLogger(XiaMengAirlineFeasibleSolution.class);
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

	public void refreshCost(boolean setupOutput) {

	}

	public void generateOutput(String minutes) {

	}

	public BigDecimal calCostbyAir(XiaMengAirlineFeasibleSolution newSolution) {

		BigDecimal cost = new BigDecimal("0");
		List<Aircraft> oldAirs = new ArrayList<Aircraft>(getNormalSchedule().values());
		for (Aircraft orgAir : oldAirs) {
			Aircraft newAir = getAircraft(orgAir.getId(), orgAir.getType(), true);
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
								Flight nextFlight = InitData.jointFlightMap.get(Flight.getFlightId());

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
		}

		return cost;
	}

}
