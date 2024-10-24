package thesis.common;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.heuristics.CloudletToVmMappingSolution;
import org.cloudsimplus.heuristics.Heuristic;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmCost;

import static java.util.stream.Collectors.groupingBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WeightedCloudletToVmSolution extends CloudletToVmMappingSolution {
    private boolean recomputeCost = true;
    private double lastCost;
    private double alpha = (double) 4/5; // weight for makespan
    private double beta = 0;  // weight for load balancing
    private double gamma = (double) 1/5;  // weight for operational cost
    
    private static final double CPU_WEIGHT = 0.2;
    private static final double MEMORY_WEIGHT = 0.05;
    //private static final double BANDWIDTH_WEIGHT = 0.02;
    
    private static final double CPU_BASE = 0.1;
    private static final double MEMORY_BASE = 0.05;
    private static final double STORAGE_BASE = 0.01;
    private static final double BANDWIDTH_BASE = 0.02;
    private static final double CPU_TRAN_COST = 0.01;
    private static final double MEMORY_TRAN_COST = 0.01;
    private static final double STORAGE_TRAN_COST = 0.01;
    private static final double BANDWIDTH_TRAN_COST = 0.01;
    
    //public static final double PES_WEIGHT = 0.2, RAM_WEIGHT = 0.2, BANDWIDTH_WEIGHT = 0.2, THROUGHPUT_WEIGHT = 0.5, EXECUTION_TIME_WEIGHT = 0.5;

    public static final double PES_WEIGHT = 0.2, RAM_WEIGHT = 0.2, BANDWIDTH_WEIGHT = 0.1, THROUGHPUT_WEIGHT = 0.25, EXECUTION_TIME_WEIGHT = 0.25;
    private Map<Vm, Double> pesUsages = new HashMap<Vm, Double>();
    private Map<Vm, Double> timeUsages = new HashMap<Vm, Double>();

    public WeightedCloudletToVmSolution(Heuristic heuristic) {
        super(heuristic);
    }

    public WeightedCloudletToVmSolution(CloudletToVmMappingSolution solution) {
        super(solution, 1);
    }

    private void recomputeCostIfRequested() {
        if (this.recomputeCost) {
            this.lastCost = this.computeCostOfAllVms();
            this.recomputeCost = false;
        }
    }

    @Override
    public double getCost() {
        this.recomputeCostIfRequested();
        //System.out.println(this.lastCost);
        return this.lastCost;
    }

    @Override
    public double getCost(boolean forceRecompute) {
        this.recomputeCost |= forceRecompute;
        return this.getCost();
    }

    private double computeCostOfAllVms() {
        var result = this.getResult().entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue));
        var costSum = result.entrySet().stream().mapToDouble(this::getVmCost).sum();
        // System.out.println(this.pesUsages.size());
        var standardizePesCost = this.pesUsages.values().stream()
                .mapToDouble(Double::doubleValue)
                .map(cost -> cost / this.pesUsages.values().stream()
                        .mapToDouble(Double::doubleValue).average().getAsDouble())
                .average().orElse(0.0);
        // standardizePesCost = standardizePesCost.getAsDouble();
        // System.out.println("standard pes cost " + standardizePesCost);
        var standardizeTimeCost = this.timeUsages.values().stream()
                .mapToDouble(Double::doubleValue)
                .map(cost -> cost / this.pesUsages.values().stream().mapToDouble(Double::doubleValue).average().getAsDouble()).average().orElse(0.0);
        // System.out.println(standardizePesCost + " " + standardizeTimeCost);

        var totalCost = costSum + standardizePesCost * PES_WEIGHT + standardizeTimeCost * EXECUTION_TIME_WEIGHT;

        // System.out.println(totalCost);
        // System.out.println("Total Cost: " + totalCost);
        // System.out.println("result size: " + result.size());
        return result.size() == 0 ? Double.MAX_VALUE : totalCost / result.size();
    }

    @Override
    public double getVmCost(Vm vm, List<Cloudlet> cloudlets) {
        double cost = 0;

        double ramUtilization = cloudlets.stream().mapToDouble(Cloudlet::getUtilizationOfRam).sum();
        double bandwidthUtilization = cloudlets.stream().mapToDouble(Cloudlet::getUtilizationOfBw).sum();
        double cpuUtilization = cloudlets.stream().mapToDouble(Cloudlet::getUtilizationOfCpu).sum();

        double pesNeeded = cloudlets.stream().mapToDouble(Cloudlet::getPesNumber).sum();
        double instructionCount = cloudlets.stream().mapToDouble(Cloudlet::getTotalLength).sum();
        // System.out.println("Instruction Count: " + instructionCount);
        // System.out.println("Pes Needed: " + pesNeeded);
        // System.out.println("Total Mips Capacity: " + vm.getTotalMipsCapacity());
        // System.out.println("Pes Number: " + vm.getPesNumber());



        double relativePesCount = pesNeeded / vm.getPesNumber();
        double relativeTotalTime = instructionCount / vm.getTotalMipsCapacity();

        cost += ramUtilization * RAM_WEIGHT;
        cost += bandwidthUtilization * BANDWIDTH_WEIGHT;
        cost += cpuUtilization * THROUGHPUT_WEIGHT;

        this.pesUsages.put(vm, relativePesCount);
        this.timeUsages.put(vm, relativeTotalTime);

        cost += relativePesCount * PES_WEIGHT;
        cost += relativeTotalTime * EXECUTION_TIME_WEIGHT;

        return cost;
    }
/*
    private double computeCostOfAllVms() {
        var result = this.getResult().entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue));
        //System.out.println(result);

        // Compute makespan
        double makespan = result.entrySet().stream().mapToDouble(entry -> {
            Vm vm = entry.getKey();
            List<Cloudlet> cloudlets = entry.getValue().stream().map(Map.Entry::getKey).collect(Collectors.toList());
            //System.out.println(vm);
            //System.out.println(cloudlets);
            return computeMakespan(vm, cloudlets);
        }).max().orElse(0);
        
        // Compute load balancing
        double loadBalancing = computeLoadBalancing(result);

        // Compute total cost
        double totalCost = result.entrySet().stream().mapToDouble(entry -> {
            Vm vm = entry.getKey();
            List<Cloudlet> cloudlets = entry.getValue().stream().map(Map.Entry::getKey).collect(Collectors.toList());
            return computeTotalCost(vm, cloudlets);
        }).sum();
        //System.out.println("total cost: " + totalCost);

        // Normalize the objectives
        double maxMakespan = getMaxMakespan(result);
        double maxLoadBalancing = getMaxLoadBalancing(result);
        double maxOperationalCost = getMaxOperationalCost(result);

        //double normalizedMakespan = makespan / maxMakespan;
       // double normalizedLoadBalancing = loadBalancing / maxLoadBalancing;
        //double normalizedOperationalCost = totalCost / maxOperationalCost;

        double cost = alpha * makespan  + gamma * totalCost +beta *loadBalancing ;
       // System.out.println("mspan: " + makespan+" tcost: "+totalCost+" lcost: "+loadBalancing +" cost: "+ cost*10E-3);

        return cost*10E-3;
    }*/

//    private double computeCostOfAllVms() {
//        var result = this.getResult().entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue));
//        var averageCost = result.entrySet().stream().mapToDouble(this::getVmCost).sum();
//        var costSum = result.entrySet().stream().mapToDouble(this::getVmCost).map(cost -> Math.abs(cost/averageCost - 1)).sum();
//        return costSum / result.size();
//        
//    	  
//    }
//  

//    @Override
//    public double getVmCost(Vm vm, List<Cloudlet> cloudlets) {
//    	   
//        double cost = 0;
//
//        double ramUtilization = cloudlets.stream().mapToDouble(Cloudlet::getUtilizationOfRam).sum();
//        double bandwidthUtilization = cloudlets.stream().mapToDouble(Cloudlet::getUtilizationOfBw).sum();
//        double cpuUtilization = cloudlets.stream().mapToDouble(Cloudlet::getUtilizationOfCpu).sum();
//
//        double pesNeeded = cloudlets.stream().mapToDouble(Cloudlet::getPesNumber).sum();
//        double instructionCount = cloudlets.stream().mapToDouble(Cloudlet::getTotalLength).sum();
//
//        double relativePesCost = pesNeeded / vm.getPesNumber();
//        double relativeInstruction = instructionCount / vm.getTotalMipsCapacity();
//
//        cost += ramUtilization * RAM_WEIGHT;
//        cost += bandwidthUtilization * BANDWIDTH_WEIGHT;
//        cost += cpuUtilization * THROUGHPUT_WEIGHT;
//
//        cost += relativePesCost * PES_WEIGHT;
//        cost += relativeInstruction * EXECUTION_TIME_WEIGHT;
//
//        return cost;
//    }
    
    private long getTotalCloudletsPes(final List<Cloudlet> cloudletListForVm) {
        return cloudletListForVm
                .stream()
                .mapToLong(Cloudlet::getPesNumber)
                .sum();
    }

//    @Override
//    public double getVmCost(Vm vm, List<Cloudlet> cloudlets) {
//        double makespan = computeMakespan(vm, cloudlets);
//        double totalCost = computeTotalCost(vm, cloudlets);
//
//        double cost = alpha * makespan + beta * totalCost;
//
//        return cost;
//    }

    private double computeMakespan(Vm vm, List<Cloudlet> cloudlets) {
        double makespan = cloudlets.stream().mapToDouble(cloudlet -> {
            double execTime = cloudlet.getTotalLength() / vm.getTotalMipsCapacity();
            //System.out.println(execTime);
            return execTime;
        }).max().orElse(0);
        //System.out.println(makespan);
        return makespan;
    }
    

    private double computeLoadBalancing(Map<Vm, List<Map.Entry<Cloudlet, Vm>>> result) {
        // Step 1: Compute the average execution time per VM (vavgj)
        Map<Vm, Double> vmAvgExecTimes = new HashMap<>();
        Map<Vm, Long> vmTotalCloudlets = new HashMap<>();

        for (Map.Entry<Vm, List<Map.Entry<Cloudlet, Vm>>> entry : result.entrySet()) {
            Vm vm = entry.getKey();
            List<Map.Entry<Cloudlet, Vm>> cloudlets = entry.getValue();

            double totalExecTime = cloudlets.stream()
                    .mapToDouble(cloudletEntry -> cloudletEntry.getKey().getTotalLength() / cloudletEntry.getValue().getTotalMipsCapacity())
                    .sum();

            vmTotalCloudlets.put(vm, (long) cloudlets.size());
            double avgExecTime = cloudlets.isEmpty() ? 0 : totalExecTime / cloudlets.size();
            vmAvgExecTimes.put(vm, avgExecTime);
        }

        // Step 2: Compute the load balance cost
        double variance = result.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .mapToDouble(cloudletEntry -> {
                    Vm vm = cloudletEntry.getValue();
                    double execTime = cloudletEntry.getKey().getTotalLength() / vm.getTotalMipsCapacity();
                    return Math.pow(execTime - vmAvgExecTimes.get(vm), 2);
                }).sum();

        return Math.sqrt(variance / result.size());
    }


    private double computeTotalCost(Vm vm, List<Cloudlet> cloudlets) {
//        double totalCpuCost = 0.0;
//        double totalBwCost = 0.0;
//        double totalRamCost = 0.0;
//        double cpuUtilization = vm.getTotalCpuMipsUtilization();
//        double bwUtilization = vm.getBw().getCapacity();
//        double ramUtilization = vm.getRam().getCapacity();
//
//            System.out.println("cpu: "+cpuUtilization+" bw: "+bwUtilization+" ram: "+ramUtilization);
//
        final var cost = new VmCost(vm);
        double processingTotalCost = 0, memoryTotaCost = 0, storageTotalCost = 0, bwTotalCost = 0;
        double totalCost = 0.0;
        for (Cloudlet cloudlet : cloudlets) {
            totalCost += cost.getTotalCost();
            processingTotalCost += cost.getProcessingCost();//4 (negligible)
            memoryTotaCost += cost.getMemoryCost(); //3
            storageTotalCost += cost.getStorageCost(); //1 (high) - 10 times more than bwcost
            bwTotalCost += cost.getBwCost(); //2

//            totalCpuCost += cpuUtilization * CPU_WEIGHT;
//            totalBwCost += bwUtilization * BANDWIDTH_WEIGHT;
//            totalRamCost += ramUtilization * RAM_WEIGHT;
        }
//
//        return totalCpuCost + totalBwCost + totalRamCost;

        int totalNonIdleVms = 0;




        //System.out.println("pcost: "+processingTotalCost+" mcost: "+memoryTotaCost+" scost: "+storageTotalCost+" bwcost: "+bwTotalCost+" totalcost: "+totalCost);
        return totalCost;
    }
    
    private double getMaxMakespan(Map<Vm, List<Map.Entry<Cloudlet, Vm>>> result) {
        return result.entrySet().stream().mapToDouble(entry -> {
            Vm vm = entry.getKey();
            List<Cloudlet> cloudlets = entry.getValue().stream().map(Map.Entry::getKey).collect(Collectors.toList());
            return computeMakespan(vm, cloudlets);
        }).max().orElse(1);
    }

    private double getMaxLoadBalancing(Map<Vm, List<Map.Entry<Cloudlet, Vm>>> result) {
        double avgTime = result.values().stream().mapToDouble(cloudlets -> cloudlets.stream()
                .mapToDouble(entry -> entry.getKey().getTotalLength() / entry.getValue().getTotalMipsCapacity()).sum())
                .average().orElse(0);

        return result.values().stream().mapToDouble(cloudlets -> {
            double totalExecTime = cloudlets.stream()
                    .mapToDouble(entry -> entry.getKey().getTotalLength() / entry.getValue().getTotalMipsCapacity()).sum();
            return Math.pow(totalExecTime - avgTime, 2);
        }).max().orElse(1);
    }

    private double getMaxOperationalCost(Map<Vm, List<Map.Entry<Cloudlet, Vm>>> result) {
        return result.entrySet().stream().mapToDouble(entry -> {
            Vm vm = entry.getKey();
            List<Cloudlet> cloudlets = entry.getValue().stream().map(Map.Entry::getKey).collect(Collectors.toList());
            return computeTotalCost(vm, cloudlets);
        }).max().orElse(1);
    }


    private double computeCpuCost(Vm vm, List<Cloudlet> cloudlets) {
        double runTime = computeMakespan(vm, cloudlets);
        final double hostMips = vm.getHost().getMips();
        final double costPerMI = hostMips == 0 ? 0.0 : vm.getHost().getDatacenter().getCharacteristics().getCostPerSecond() / hostMips;
        return costPerMI * vm.getTotalMipsCapacity() * vm.getTotalExecutionTime() + CPU_TRAN_COST;
    }

    private double computeMemoryCost(Vm vm, List<Cloudlet> cloudlets) {
        double runTime = computeMakespan(vm, cloudlets);
        return vm.getHost().getDatacenter().getCharacteristics().getCostPerMem() * vm.getRam().getCapacity()  + MEMORY_TRAN_COST;
    }

    private double computeStorageCost(Vm vm, List<Cloudlet> cloudlets) {
        double runTime = computeMakespan(vm, cloudlets);
        return vm.getHost().getDatacenter().getCharacteristics().getCostPerStorage() * vm.getStorage().getCapacity()  + STORAGE_TRAN_COST;
    }

    private double computeBandwidthCost(Vm vm, List<Cloudlet> cloudlets) {
        double runTime = computeMakespan(vm, cloudlets);
        return vm.getHost().getDatacenter().getCharacteristics().getCostPerBw() * vm.getBw().getCapacity()  + BANDWIDTH_TRAN_COST;
    }

    
//    private double computeTotalCost(Vm vm, List<Cloudlet> cloudlets) {
//  	   //var cost = new VmCost(vm);
//  	
//     double pricePerUnitTime = vm.getHostBwUtilization()+ vm.getHostRamUtilization() ; // Assuming bandwidth cost as price per unit time for simplicity
//  	  //double pricePerUnitTime = cost.getTotalCost();
//      double totalCost = cloudlets.stream().mapToDouble(cloudlet -> {
//          double execTime = cloudlet.getTotalLength() / vm.getTotalMipsCapacity();
//          return execTime * pricePerUnitTime;
//      }).sum();
//
//      return totalCost;
//  }
    
 /*   
    private double computeMakespan(Vm vm, List<Cloudlet> cloudlets) {
        double makespan = cloudlets.stream().mapToDouble(cloudlet -> {
            double execTime = cloudlet.getTotalLength() / (vm.getTotalMipsCapacity() * cloudlet.getUtilizationModelCpu().getUtilization(vm.getSimulation().clock()));
            return execTime;
        }).max().orElse(0);

        return makespan;
    }
*/
    
/*
    private double computeTotalCost(Vm vm, List<Cloudlet> cloudlets) {
        double pricePerUnitTime = vm.getHostBwUtilization() * 0.01; // Existing bandwidth cost calculation
        
        // Example costs per unit utilization for RAM and BW
        double ramCostPerUnit = 0.001; // Cost per unit of RAM utilization
        double bwCostPerUnit = 0.01; // Cost per unit of Bandwidth utilization

        double totalCost = cloudlets.stream().mapToDouble(cloudlet -> {
            double execTime = cloudlet.getTotalLength() / vm.getTotalMipsCapacity();
            
            // Incorporate dynamic RAM and Bandwidth utilization models
            double ramUtilization = cloudlet.getUtilizationOfRam(vm.getSimulation().clock());
            double bwUtilization = cloudlet.getUtilizationOfBw(vm.getSimulation().clock());

            // Calculate total cost considering all resource utilizations
            return execTime * (pricePerUnitTime + ramUtilization * ramCostPerUnit + bwUtilization * bwCostPerUnit);
        }).sum();

        return totalCost;
    }
*/


    
}
