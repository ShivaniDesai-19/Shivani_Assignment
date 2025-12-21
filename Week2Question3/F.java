import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


class Flight{
    String origin;
    String destination;
    double price;
    String flightNumber;
     
    public Flight(String from, String to, double price, String flightNum) { 
        this.origin = from; 
        this.destination = to; 
        this.price = price; 
        this.flightNumber = flightNum; 
    } 
}

class Trip implements Comparable<Trip>{
    String currentCity;
    List<Flight> flightsSoFar;
    double totalCost;

    public Trip(String currentCity, List<Flight> flights, double cost){
        this.currentCity = currentCity;
        this.flightsSoFar = new ArrayList<>(flights); 
        this.totalCost = cost;
    }

    public int compareTo(Trip other){
        return Double.compare(this.totalCost, other.totalCost);
    }
}


class FlightRoutingEngine { 

    private final Map<String, List<Flight>> flightNetwork = new HashMap<>(); 

    public void addFlight(String from, String to, double price, String flightNum) { 
        flightNetwork.computeIfAbsent(from, k -> new ArrayList<>()) 
                     .add(new Flight(from, to, price, flightNum)); 
    } 

    public void findCheapestRoute(String start, String end, int maxStops) {

    PriorityQueue<Trip> pq = new PriorityQueue<>();
    pq.offer(new Trip(start, new ArrayList<>(), 0.0));

    while (!pq.isEmpty()) {
        Trip current = pq.poll();
           if (current.currentCity.equals(end)) {
            System.out.println("Cheapest cost: " + current.totalCost);
            for (Flight f : current.flightsSoFar) {
                System.out.println(f.origin + " -> " + f.destination + " ($" + f.price + ")");
            }
            return;
        }

        List<Flight> nextFlights = flightNetwork.get(current.currentCity);
        if (nextFlights == null) continue;

        for (Flight f : nextFlights) {

           
            boolean visited = false;
            for (Flight taken : current.flightsSoFar) {
                if (taken.origin.equals(f.destination) ||
                    taken.destination.equals(f.destination)) {
                    visited = true;
                    break;
                }
            }
            if (visited) continue;

           
            int newFlights = current.flightsSoFar.size() + 1;
            int newStops = newFlights - 1;
            if (newStops > maxStops) continue;

            List<Flight> newPath = new ArrayList<>(current.flightsSoFar);
            newPath.add(f);

            pq.offer(new Trip(
                f.destination,
                newPath,
                current.totalCost + f.price
            ));
        }
    }

    System.out.println("No valid route found.");
}

}


public class F {
    public static void main(String[] args) {
        

    FlightRoutingEngine engine = new FlightRoutingEngine();

    
    engine.addFlight("JFK", "LAX", 300.0, "DL100");
    engine.addFlight("LAX", "SYD", 1200.0, "QF12");

    
    engine.addFlight("JFK", "LHR", 400.0, "BA01");
    engine.addFlight("LHR", "DXB", 300.0, "EK05");
    engine.addFlight("DXB", "SYD", 400.0, "EK06");

    
    engine.addFlight("JFK", "MIA", 100.0, "AA50");
    engine.addFlight("MIA", "JFK", 100.0, "AA51");

    
    engine.addFlight("JFK", "ORD", 100.0, "UA1");
    engine.addFlight("ORD", "DEN", 100.0, "UA2");
    engine.addFlight("DEN", "SFO", 100.0, "UA3");
    engine.addFlight("SFO", "SYD", 500.0, "UA4");

   
    engine.findCheapestRoute("JFK", "SYD", 2);
    }       
}
