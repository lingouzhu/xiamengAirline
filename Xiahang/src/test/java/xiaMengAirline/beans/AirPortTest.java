package xiaMengAirline.beans;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class AirPortTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testGetOverlappedAirports() {
		ArrayList<AirPort> listAirportA = new ArrayList<AirPort> ();
		ArrayList<AirPort> listAirportB = new ArrayList<AirPort> ();
		
		AirPort aAir = new AirPort();
		aAir.setId("16");
		listAirportA.add(aAir);
		
		aAir = new AirPort();
		aAir.setId("49");
		listAirportA.add(aAir);
		
		aAir = new AirPort();
		aAir.setId("32");
		listAirportA.add(aAir);
		
		aAir = new AirPort();
		aAir.setId("49");
		listAirportA.add(aAir);
		
		aAir = new AirPort();
		aAir.setId("12");
		listAirportA.add(aAir);
		
		aAir = new AirPort();
		aAir.setId("49");
		listAirportA.add(aAir);
		
		aAir = new AirPort();
		aAir.setId("32");
		listAirportB.add(aAir);
		
		aAir = new AirPort();
		aAir.setId("49");
		listAirportB.add(aAir);
		
		aAir = new AirPort();
		aAir.setId("16");
		listAirportB.add(aAir);
		
		aAir = new AirPort();
		aAir.setId("49");
		listAirportB.add(aAir);
		
		aAir = new AirPort();
		aAir.setId("25");
		listAirportB.add(aAir);
		
		aAir = new AirPort();
		aAir.setId("66");
		listAirportB.add(aAir);
		
		HashMap<Integer, List<Integer>> listOverlapped = AirPort.getOverlappedAirports(listAirportA, listAirportB);
		for(Map.Entry<Integer, List<Integer>> entry : listOverlapped.entrySet()) {
			Integer key = entry.getKey();
			List<Integer> value = entry.getValue();

			  System.out.println(key + " => " + value);
			}
		
	}

	@Test
	public void testGetMatchedAirports() {
		fail("Not yet implemented");
	}
	
	private Flight createFlight(int flightId, String srcPort, String destPort) {
		Flight flight = new Flight();
		flight.setSchdNo(101);
		AirPort aAirport = new AirPort();
		AirPort bAirport = new AirPort();
		aAirport.setId(srcPort);
		bAirport.setId(destPort);

		flight.setSourceAirPort(aAirport);
		flight.setDesintationAirport(bAirport);
		
		return flight;
	}

	@Test
	public void testGetCircuitAirports() throws CloneNotSupportedException {
		Aircraft air1 = new Aircraft();
		List<Flight> flightChain = new ArrayList<Flight> ();
		flightChain.add(createFlight(101, "ORF", "EWR"));
		flightChain.add(createFlight(102, "EWR", "STL"));
		flightChain.add(createFlight(103, "STL", "CLE"));
		flightChain.add(createFlight(104, "CLE", "BDL"));
		flightChain.add(createFlight(105, "BDL", "CLE"));
		air1.setFlightChain(flightChain);
		air1.setId("1");
		
		Aircraft air2 = new Aircraft();
		List<Flight> flightChain2 = new ArrayList<Flight> ();
		flightChain2.add(createFlight(201, "CLE", "ATL"));
		flightChain2.add(createFlight(202, "ATL", "EWR"));
		flightChain2.add(createFlight(203, "EWR", "BWI"));
		flightChain2.add(createFlight(204, "BWI", "CLI"));
		flightChain2.add(createFlight(205, "CLE", "MDW"));
		air2.setFlightChain(flightChain2);
		air2.setId("2");
		
		HashMap<Integer, List<Integer>> circuitAirports1 = AirPort
				.getCircuitAirports(air1.getAirports());
		HashMap<Integer, List<Integer>> circuitAirports2 = AirPort
				.getCircuitAirports(air2.getAirports());
		
		for(Map.Entry<Integer, List<Integer>> entry : circuitAirports1.entrySet()) {
			Integer key = entry.getKey();
			List<Integer> value = entry.getValue();

			  System.out.println("Air1 " + key + " => " + value);
			}
		
		for(Map.Entry<Integer, List<Integer>> entry : circuitAirports2.entrySet()) {
			Integer key = entry.getKey();
			List<Integer> value = entry.getValue();

			  System.out.println("Air2 " + key + " => " + value);
			}
		Aircraft newAir1 = air1.clone();
		Aircraft newAir2 = air2.clone();
		List<Integer> circuitChain = circuitAirports1.get(3);
		for (Integer aCircuit:circuitChain) {
			newAir2.insertFlightChain(air1, 3, aCircuit, 4, true);
			newAir1.removeFlightChain(3, aCircuit);
		}
		List<Flight> updateList1 = newAir1.getFlightChain();
		for (Flight aF:updateList1) {
			System.out.println("Air 1 Source " + aF.getSourceAirPort().getId());
			System.out.println("Air 1 Dest " + aF.getDesintationAirport().getId());
		}
		List<Flight> updateList2 = newAir2.getFlightChain();
		for (Flight aF:updateList2) {
			System.out.println("Air 2 Source " + aF.getSourceAirPort().getId());
			System.out.println("Air 2 Dest " + aF.getDesintationAirport().getId());
		}
		
		

		
		
		
//		ArrayList<AirPort> listAirportB = new ArrayList<AirPort> ();
//		AirPort aAir = new AirPort();
//		
//		aAir = new AirPort();
//		aAir.setId("32");
//		listAirportB.add(aAir);
//		
//		aAir = new AirPort();
//		aAir.setId("49");
//		listAirportB.add(aAir);
//		
//		aAir = new AirPort();
//		aAir.setId("16");
//		listAirportB.add(aAir);
//		
//		aAir = new AirPort();
//		aAir.setId("49");
//		listAirportB.add(aAir);
//		
//		aAir = new AirPort();
//		aAir.setId("25");
//		listAirportB.add(aAir);
//		
//		aAir = new AirPort();
//		aAir.setId("66");
//		listAirportB.add(aAir);
//		
//		aAir = new AirPort();
//		aAir.setId("49");
//		listAirportB.add(aAir);
//		
//		HashMap<Integer, List<Integer>> listCircuit =  AirPort.getCircuitAirports(listAirportB);
//		for(Map.Entry<Integer, List<Integer>> entry : listCircuit.entrySet()) {
//			Integer key = entry.getKey();
//			List<Integer> value = entry.getValue();
//
//			  System.out.println(key + " => " + value);
//			}
		
	}

}
