package xiaMengAirline.newBranch.BusinessDomain;

public class SeatAvailability {
	private int seatCapability = Integer.MAX_VALUE;
	private int currentAllocated = 0;
	
	public int getCurrentAvailable() {
		return currentAllocated;
	}


	public boolean applyForResource(int numberOfResources) {
		int newAlloected = currentAllocated + numberOfResources;
		if (newAlloected <= seatCapability) {
			currentAllocated = newAlloected;
			return true;
		} else
			return false;
	}



	public int getSeatCapability() {
		return seatCapability;
	}

	public void setSeatCapability(int seatCapability) {
		this.seatCapability = seatCapability;
	}






}
