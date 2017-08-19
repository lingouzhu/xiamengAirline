package xiaMengAirline.newBranch.BusinessDomain;

public class SeatAvailability implements Cloneable {
	private int seatCapability = Integer.MAX_VALUE;
	private int currentAllocated = 0;
	
	public int getCurrentAvailable() {
		return seatCapability - currentAllocated;
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


	public SeatAvailability clone() throws CloneNotSupportedException {
		return (SeatAvailability) super.clone();
	}






}
