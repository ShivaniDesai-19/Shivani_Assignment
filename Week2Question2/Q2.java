package Week2Question2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

enum ServiceType{
    COMPUTE,
    STORAGE,
    DATABASE;
}



class UsageEvent{
    String departmentId;
    String resourceId;
    ServiceType serviceType;
    Double unitsConsumed;
    Instant timestamp;
    public UsageEvent(String departmentId, String resourceId, ServiceType serviceType, Double unitsConsumed,
            Instant timestamp) {
        this.departmentId = departmentId;
        this.resourceId = resourceId;
        this.serviceType = serviceType;
        this.unitsConsumed = unitsConsumed;
        this.timestamp = timestamp;
    }
    public String getDepartmentId() {
        return departmentId;
    }
    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }
    public String getResourceId() {
        return resourceId;
    }
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    public ServiceType getServiceType() {
        return serviceType;
    }
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
    public Double getUnitsConsumed() {
        return unitsConsumed;
    }
    public void setUnitsConsumed(Double unitsConsumed) {
        this.unitsConsumed = unitsConsumed;
    }
    public Instant getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

@Override
    public String toString() {
    return "UsageEvent{" +
            "departmentId='" + departmentId + '\'' +
            ", resourceId='" + resourceId + '\'' +
            ", serviceType=" + serviceType +
            ", unitsConsumed=" + unitsConsumed +
            ", timestamp=" + timestamp +
            '}';
}
    
}

class UsageEventReader {

    public List<UsageEvent> readRecentEvents() {
        List<UsageEvent> events = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(
                    new FileReader("C:\\Users\\ShivaniDesai\\Desktop\\Shivani_Assignment\\Week2Question2\\logs.txt")
            );

            String line;
            Instant sevenDaysAgo = Instant.now().minusSeconds(7 * 24 * 60 * 60);

            while ((line = br.readLine()) != null) {
               
                if (line.trim().startsWith("departmentId")) continue;

                String[] parts = line.split(",");

                String departmentId = parts[0];
                String resourceId = parts[1];
                ServiceType serviceType = ServiceType.valueOf(parts[2]);
                Double unitsConsumed = Double.parseDouble(parts[3]);
                Instant timestamp = Instant.parse(parts[4].trim()); 

                UsageEvent event = new UsageEvent(departmentId, resourceId, serviceType, unitsConsumed, timestamp);

               
                if (!event.getTimestamp().isBefore(sevenDaysAgo)) {
                    events.add(event);
                }
            }

            br.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return events;
    }
}

class DepartmentReport{
    private double totalCost;
    private ServiceType mostExpensiveService;
    private int resourceCount;
    public DepartmentReport(double totalCost, ServiceType mostExpensiveService, int resourceCount) {
        this.totalCost = totalCost;
        this.mostExpensiveService = mostExpensiveService;
        this.resourceCount = resourceCount;
    }
    public double getTotalCost() { return totalCost; }
    public ServiceType getMostExpensiveService() { return mostExpensiveService; }
    public int getResourceCount() { return resourceCount; }

    @Override
    public String toString() {
        return "DepartmentReport{" +
                "totalCost=" + totalCost +
                ", mostExpensiveService=" + mostExpensiveService +
                ", resourceCount=" + resourceCount +
                '}';
    }


    
}




class DepartmentReportGenerator {
    public double calculateCost(UsageEvent event) {
        switch (event.getServiceType()) {
            case COMPUTE: return event.getUnitsConsumed() * 0.10;
            case STORAGE: return event.getUnitsConsumed() * 0.05;
            case DATABASE: return event.getUnitsConsumed() * 0.20;
            default: return 0;
        }
    }


   //key is Departement ID , value is Department report 
   //Department report has - totalCost , mostExpensiveService , resourceCount

public Map<String, DepartmentReport> generateReport(List<UsageEvent> events) {

    // Step 1: Group events by departmentId
    Map<String, List<UsageEvent>> eventsByDept =
            events.stream()
                  .collect(Collectors.groupingBy(event -> event.getDepartmentId()));

    Map<String, DepartmentReport> reportMap = new java.util.HashMap<>();

    // Process each department
    for (Map.Entry<String, List<UsageEvent>> entry : eventsByDept.entrySet()) {

        String department = entry.getKey();
        List<UsageEvent> deptEvents = entry.getValue();

        // total cost
        double totalCost = deptEvents.stream()
                .mapToDouble(event -> calculateCost(event))
                .sum();

        // unique resource count
        int resourceCount = (int) deptEvents.stream()
                .map(event -> event.getResourceId())
                .distinct()
                .count();

        // most expensive service
        Map<ServiceType, Double> costByService =
                deptEvents.stream()
                        .collect(Collectors.groupingBy(
                                event -> event.getServiceType(),
                                Collectors.summingDouble(event -> calculateCost(event))
                        ));

        ServiceType mostExpensiveService =
                Collections.max(costByService.entrySet(),
                        Map.Entry.comparingByValue())
                        .getKey();

        reportMap.put(
                department,
                new DepartmentReport(totalCost, mostExpensiveService, resourceCount)
        );
    }

    return reportMap;
   }
}



public class Q2 {
    public static void main(String[] args) { 
        
        UsageEventReader reader = new UsageEventReader();
        List<UsageEvent> recentEvents = reader.readRecentEvents();

        DepartmentReportGenerator generator = new DepartmentReportGenerator();
        Map<String, DepartmentReport> reports = generator.generateReport(recentEvents);

        
        reports.forEach((dept, report) -> {
            System.out.println(dept + " -> " + report);
        });
    }
}
























// public Map<String, DepartmentReport> generateReport(List<UsageEvent> events) {
//     // Group events by department
//     Map<String, List<UsageEvent>> eventsByDept = new HashMap<>();
//     for (UsageEvent event : events) {
//         eventsByDept
//             .computeIfAbsent(event.getDepartmentId(), k -> new ArrayList<>())
//             .add(event);
//     }

//     // Prepare the final report map
//     Map<String, DepartmentReport> report = new HashMap<>();

//     // Loop through each department
//     for (Map.Entry<String, List<UsageEvent>> entry : eventsByDept.entrySet()) {
//         String dept = entry.getKey();
//         List<UsageEvent> deptEvents = entry.getValue();

//         // a) totalCost
//         double totalCost = 0;
//         for (UsageEvent e : deptEvents) {
//             totalCost += calculateCost(e);
//         }

//         // b) resourceCount (unique resources)
//         Set<String> uniqueResources = new HashSet<>();
//         for (UsageEvent e : deptEvents) {
//             uniqueResources.add(e.getResourceId());
//         }
//         int resourceCount = uniqueResources.size();

//         // c) mostExpensiveService
//         Map<ServiceType, Double> costByService = new HashMap<>();
//         for (UsageEvent e : deptEvents) {
//             costByService.put(
//                 e.getServiceType(),
//                 costByService.getOrDefault(e.getServiceType(), 0.0) + calculateCost(e)
//             );
//         }

//         ServiceType mostExpensiveService = Collections.max(
//             costByService.entrySet(),
//             Map.Entry.comparingByValue()
//         ).getKey();

//         // d) Create DepartmentReport and add to map
//         report.put(dept, new DepartmentReport(totalCost, mostExpensiveService, resourceCount));
//     }

//     return report;
// }
