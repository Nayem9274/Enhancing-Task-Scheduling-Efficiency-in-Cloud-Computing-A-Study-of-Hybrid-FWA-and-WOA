package thesis.fwa;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.distributions.ContinuousDistribution;
import org.cloudsimplus.heuristics.CloudletToVmMappingHeuristic;
import org.cloudsimplus.heuristics.CloudletToVmMappingSolution;
import org.cloudsimplus.heuristics.Heuristic;
import org.cloudsimplus.heuristics.HeuristicSolution;
import org.cloudsimplus.vms.Vm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import thesis.common.WeightedCloudletToVmSolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CloudletToVmMappingFireworksAlgorithm implements CloudletToVmMappingHeuristic {
    public static final int POPULATION_SIZE = 100;
    public static final int MAX_ITERATIONS = 50;
    public static final int A = 40;
    public static final int m = 800;
    public static final double a = 0.04;
    public static final double b = 0.8;
    public static final double EPSILON = 1e-6;

    private final ContinuousDistribution random;
    private int searchesByIteration;
    private double solveTime;
    private CloudletToVmMappingSolution bestSolutionSoFar, initialSolution;
    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;
    private final static Logger logger = LoggerFactory.getLogger(CloudletToVmMappingFireworksAlgorithm.class.getSimpleName());

    public CloudletToVmMappingFireworksAlgorithm(ContinuousDistribution random) {
        this.random = random;
    }

    @Override
    public double getSolveTime() {
        return solveTime;
    }

    private void setSolveTime(double v) {
        this.solveTime = v;
    }

    @Override
    public List<Cloudlet> getCloudletList() {
        return cloudletList;
    }

    @Override
    public List<Vm> getVmList() {
        return vmList;
    }

    @Override
    public CloudletToVmMappingHeuristic setCloudletList(List<Cloudlet> list) {
        this.cloudletList = list;
        return this;
    }

    @Override
    public CloudletToVmMappingHeuristic setVmList(List<Vm> list) {
        this.vmList = list;
        return this;
    }

    @Override
    public int getSearchesByIteration() {
        return searchesByIteration;
    }

    @Override
    public Heuristic<CloudletToVmMappingSolution> setSearchesByIteration(int i) {
        this.searchesByIteration = i;
        return this;
    }


    @Override
    public int getRandomValue(int maxValue) {
        double uniform = random.sample();
        return (int) (uniform >= 1.0 ? uniform % (double) maxValue : uniform * (double) maxValue);
    }


    private Vm getRandomVm() {
        int idx = getRandomValue(vmList.size());
        return vmList.get(idx);
    }

//    private Vm getRandomVm() {
//        int idx = (int) (Math.random() * vmList.size()); // Using Math.random() for uniform distribution
//        return vmList.get(idx);
//    }

    @Override
    public CloudletToVmMappingSolution getInitialSolution() {
        if (this.initialSolution == null || this.initialSolution.getResult().isEmpty()) {
            this.initialSolution = this.generateRandomSolution();
        }
        return this.initialSolution;
    }

    @Override
    public double getAcceptanceProbability() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CloudletToVmMappingSolution getNeighborSolution() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CloudletToVmMappingSolution createNeighbor(CloudletToVmMappingSolution source) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CloudletToVmMappingSolution getBestSolutionSoFar() {
        return bestSolutionSoFar;
    }

    @Override
    public boolean isToStopSearch() {
        // TODO Auto-generated method stub
        // Should we stop the search
        return false;
    }

    private CloudletToVmMappingSolution generateRandomSolution() {
        CloudletToVmMappingSolution solution = new WeightedCloudletToVmSolution(this);
        cloudletList.forEach(cloudlet -> solution.bindCloudletToVm(cloudlet, getRandomVm()));
        return solution;
    }

//    private CloudletToVmMappingSolution generateRandomSolution() {
//        CloudletToVmMappingSolution solution = new WeightedCloudletToVmSolution(this);
//
//        // Ensuring a more even distribution of cloudlets to VMs
//        List<Vm> shuffledVms = new ArrayList<>(vmList);
//        Collections.shuffle(shuffledVms);
//
//        int vmIndex = 0;
//        for (Cloudlet cloudlet : cloudletList) {
//            solution.bindCloudletToVm(cloudlet, shuffledVms.get(vmIndex));
//            vmIndex = (vmIndex + 1) % shuffledVms.size();
//        }
//        return solution;
//    }

//    private CloudletToVmMappingSolution generateRandomSolution() {
//        CloudletToVmMappingSolution solution = new WeightedCloudletToVmSolution(this);
//        List<Vm> shuffledVms = new ArrayList<>(vmList);
//        Collections.shuffle(shuffledVms);
//
//        // Track usage of VMs to avoid consecutive assignments
//        int[] vmUsageCount = new int[vmList.size()];
//
//        for (Cloudlet cloudlet : cloudletList) {
//            // Find the least used VM
//            int minUsageIndex = 0;
//            for (int i = 1; i < vmUsageCount.length; i++) {
//                if (vmUsageCount[i] < vmUsageCount[minUsageIndex]) {
//                    minUsageIndex = i;
//                }
//            }
//
//            // Assign cloudlet to the least used VM
//            Vm selectedVm = shuffledVms.get(minUsageIndex);
//            solution.bindCloudletToVm(cloudlet, selectedVm);
//            vmUsageCount[minUsageIndex]++;
//            
//            // Shuffle VMs to ensure randomness
//            Collections.shuffle(shuffledVms);
//        }
//
//        return solution;
//    }


    private List<CloudletToVmMappingSolution> generateInitialPopulation() {
        List<CloudletToVmMappingSolution> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(generateRandomSolution());
        }
        return population;
    }

    private int calculateNumSparks(CloudletToVmMappingSolution firework, double maxFitness, List<CloudletToVmMappingSolution> population) {
        // Calculate the summation of (maxFitness - fitness of each solution) + epsilon
        double fitnessDifferenceSum = 0.0;
        for (CloudletToVmMappingSolution solution : population) {
            fitnessDifferenceSum += (maxFitness - solution.getFitness());
        }

        // Calculate Si using eqn 2
        double si = m * ((maxFitness - firework.getFitness() + EPSILON) / (fitnessDifferenceSum + EPSILON));

        // Apply bounds and rounding using eqn 3
        if (si < a * m) {
            si = Math.round(a * m);
        } else if (si > b * m) {
            si = Math.round(b * m);
        } else {
            si = Math.round(si);
        }

        return (int) si;
    }

    private double calculateAmplitude(CloudletToVmMappingSolution firework, double minFitness, List<CloudletToVmMappingSolution> population) {
        // Calculate the summation of (fitness of each solution - minFitness) + epsilon
        double fitnessDifferenceSum = 0.0;
        for (CloudletToVmMappingSolution solution : population) {
            fitnessDifferenceSum += (solution.getFitness() - minFitness);
        }

        // Calculate Ai using eqn 4
        double ai = A * ((firework.getFitness() - minFitness + EPSILON) / (fitnessDifferenceSum + EPSILON));

        return ai;
    }

    private CloudletToVmMappingSolution createSpark(CloudletToVmMappingSolution firework, double amplitude) {
        // Algorithm 1 part 1: Initialize spark's location = firework's location
        CloudletToVmMappingSolution spark = new WeightedCloudletToVmSolution(this);
        for (Cloudlet cloudlet : cloudletList) {
            Vm vm = firework.getResult().get(cloudlet);
            spark.bindCloudletToVm(cloudlet, vm);
        }
        mutateSpark(spark, amplitude);
        return spark;
    }

    private void mutateSpark(CloudletToVmMappingSolution spark, double amplitude) {
        // Algorithm 1 part 2:
        // Determine the number of dimensions to mutate
        int z = (int) (cloudletList.size() * random.sample());
        List<Integer> dimensions = new ArrayList<>(IntStream.range(0, cloudletList.size()).boxed().collect(Collectors.toList()));
        Collections.shuffle(dimensions);
        List<Integer> selectedDimensions = dimensions.subList(0, z);

        // Calculate the displacement h
        double h = amplitude * (random.sample() * 2 - 1);

        for (int dimension : selectedDimensions) {
            Cloudlet cloudlet = cloudletList.get(dimension);
            Vm originalVm = spark.getResult().get(cloudlet);
            int originalVmId = (int) originalVm.getId();

            // Calculate new Vm id with displacement h
            int newVmId = originalVmId + (int) h;

            // Ensure newVmId is within bounds and different from originalVmId
            newVmId = Math.floorMod(newVmId, vmList.size());
            if (newVmId == originalVmId) {
                newVmId = (newVmId + 1) % vmList.size();
            }
            // Bind the cloudlet to the new Vm
            Vm newVm = vmList.get(newVmId);
            spark.bindCloudletToVm(cloudletList.get(dimension), newVm);
        }
    }
    
/*   
    //Use of  Load Distribution
    private void mutateSpark(CloudletToVmMappingSolution spark, double amplitude) {
        // Determine the number of dimensions to mutate
        int z = (int) (cloudletList.size() * random.sample());
        List<Integer> dimensions = new ArrayList<>(IntStream.range(0, cloudletList.size()).boxed().collect(Collectors.toList()));
        Collections.shuffle(dimensions);
        List<Integer> selectedDimensions = dimensions.subList(0, z);

        // Calculate the displacement h
        double h = amplitude * (random.sample() * 2 - 1);

        // Initialize the load tracking map for VMs
        Map<Integer, Integer> vmLoad = new HashMap<>();
        for (Vm vm : vmList) {
            vmLoad.put((int) vm.getId(), 0);
        }
        for (Cloudlet cloudlet : cloudletList) {
            int vmId = (int) spark.getResult().get(cloudlet).getId();
            vmLoad.put(vmId, vmLoad.getOrDefault(vmId, 0) + 1);
        }

        for (int dimension : selectedDimensions) {
            Cloudlet cloudlet = cloudletList.get(dimension);
            Vm originalVm = spark.getResult().get(cloudlet);
            int originalVmId = (int) originalVm.getId();

            // Find a new VM with the least load
            int newVmId = originalVmId + (int) h;
         
            int minLoad = Integer.MAX_VALUE;
            for (Vm vm : vmList) {
                int vmId = (int) vm.getId();
                if (vmLoad.get(vmId) < minLoad && vmId != originalVmId) {
                    newVmId = vmId;
                    minLoad = vmLoad.get(vmId);
                }
            }

            // Update the load tracking
            vmLoad.put(newVmId, vmLoad.get(newVmId) + 1);
            vmLoad.put(originalVmId, vmLoad.get(originalVmId) - 1);
            newVmId = Math.floorMod(newVmId, vmList.size());

            // Bind the cloudlet to the new Vm
            Vm newVm = vmList.get(newVmId);
            spark.bindCloudletToVm(cloudletList.get(dimension), newVm);
        }
    }
*/

    private CloudletToVmMappingSolution createGaussianSpark(CloudletToVmMappingSolution firework) {
        // Algorithm 2 part 1: Initialize Spark's location= firework's location
        CloudletToVmMappingSolution spark = new WeightedCloudletToVmSolution(this);
        for (Cloudlet cloudlet : cloudletList) {
            Vm vm = firework.getResult().get(cloudlet);
            spark.bindCloudletToVm(cloudlet, vm);
        }
        mutateGaussianSpark(spark);
        return spark;
    }
/*
   // Load Distribution    
    private void mutateGaussianSpark(CloudletToVmMappingSolution spark) {
        // Determine the number of dimensions to mutate
        int z = (int) (cloudletList.size() * random.sample());
        List<Integer> dimensions = new ArrayList<>(IntStream.range(0, cloudletList.size()).boxed().collect(Collectors.toList()));
        Collections.shuffle(dimensions);
        List<Integer> selectedDimensions = dimensions.subList(0, z);

        // Calculate the Gaussian coefficient g
        double g = new Random().nextGaussian();

        // Initialize the load tracking map for VMs
        Map<Integer, Integer> vmLoad = new HashMap<>();
        for (Vm vm : vmList) {
            vmLoad.put((int) vm.getId(), 0);
        }
        for (Cloudlet cloudlet : cloudletList) {
            int vmId = (int) spark.getResult().get(cloudlet).getId();
            vmLoad.put(vmId, vmLoad.getOrDefault(vmId, 0) + 1);
        }

        for (int dimension : selectedDimensions) {
            Cloudlet cloudlet = cloudletList.get(dimension);
            Vm originalVm = spark.getResult().get(cloudlet);
            int originalVmId = (int) originalVm.getId();

            // Find a new VM with the least load
            int newVmId = (int) (originalVmId * g);
            int minLoad = Integer.MAX_VALUE;
            for (Vm vm : vmList) {
                int vmId = (int) vm.getId();
                if (vmLoad.get(vmId) < minLoad && vmId != originalVmId) {
                    newVmId = vmId;
                    minLoad = vmLoad.get(vmId);
                }
            }

            // Update the load tracking
            vmLoad.put(newVmId, vmLoad.get(newVmId) + 1);
            vmLoad.put(originalVmId, vmLoad.get(originalVmId) - 1);
            newVmId = Math.floorMod(newVmId, vmList.size());

            // Bind the cloudlet to the new Vm
            Vm newVm = vmList.get(newVmId);
            spark.bindCloudletToVm(cloudletList.get(dimension), newVm);
        }
    }
*/

    private void mutateGaussianSpark(CloudletToVmMappingSolution spark) {
        // Algorithm 2 part 2:
        // Determine the number of dimensions to mutate
        int z = (int) (cloudletList.size() * random.sample());
        List<Integer> dimensions = new ArrayList<>(IntStream.range(0, cloudletList.size()).boxed().collect(Collectors.toList()));
        Collections.shuffle(dimensions);
        List<Integer> selectedDimensions = dimensions.subList(0, z);

        // Calculate the Gaussian coefficient g
        double g = new Random().nextGaussian();

        for (int dimension : selectedDimensions) {
            Cloudlet cloudlet = cloudletList.get(dimension);
            Vm originalVm = spark.getResult().get(cloudlet);
            int originalVmId = (int) originalVm.getId();

            // Calculate new Vm id with Gaussian coefficient g
            int newVmId = (int) (originalVmId * g);
            // Ensure newVmId is within bounds and different from originalVmId
            newVmId = Math.floorMod(newVmId, vmList.size());
            if (newVmId == originalVmId) {
                newVmId = (newVmId + 1) % vmList.size();
            }
            // Bind the cloudlet to the new Vm
            Vm newVm = vmList.get(newVmId);
            spark.bindCloudletToVm(cloudletList.get(dimension), newVm);
        }
    }

    private void updateBestSolution(List<CloudletToVmMappingSolution> population) {
        // Sort in descending order of fitness
        population.sort((s1, s2) -> Double.compare(s2.getFitness(), s1.getFitness()));
        if (population.get(0).getFitness() > bestSolutionSoFar.getFitness()) {
            // The best fitness is the first member of the population
            bestSolutionSoFar = population.get(0);
        }
    }

    private List<CloudletToVmMappingSolution> selectNewPopulation(List<CloudletToVmMappingSolution> population) {
        List<CloudletToVmMappingSolution> newPopulation = new ArrayList<>();
        newPopulation.add(bestSolutionSoFar);

        List<Double> distances = new ArrayList<>();
        double totalDistance = 0;
        for (CloudletToVmMappingSolution solution : population) {
            double distance = calculateDistance(solution);
            distances.add(distance);
            totalDistance += distance;
        }

        List<Double> selectionProbabilities = new ArrayList<>();
        for (double distance : distances) {
            selectionProbabilities.add(distance / totalDistance);
        }

        Random rand = new Random();
        for (int i = 1; i < POPULATION_SIZE; i++) {
            double r = rand.nextDouble();
            double cumulativeProbability = 0;
            for (int j = 0; j < population.size(); j++) {
                cumulativeProbability += selectionProbabilities.get(j);
                if (r <= cumulativeProbability) {
                    newPopulation.add(population.get(j));
                    break;
                }
            }
        }

        return newPopulation;
    }

    private double calculateDistance(CloudletToVmMappingSolution solution) {
        double distance = 0;
        for (Cloudlet cloudlet : cloudletList) {
            //int idx = cloudletList.indexOf(cloudlet);
            Vm vm1 = solution.getResult().get(cloudlet);
            Vm vm2 = bestSolutionSoFar.getResult().get(cloudlet);
            distance += Math.pow(vm1.getId() - vm2.getId(), 2);
        }
        return Math.sqrt(distance);
    }

    @Override
    public CloudletToVmMappingSolution solve() {

        long startTime = System.currentTimeMillis();
        List<CloudletToVmMappingSolution> population = generateInitialPopulation();
        population.sort((s1, s2) -> Double.compare(s2.getFitness(), s1.getFitness()));
        bestSolutionSoFar = population.get(0);

        int iterations = 0;
        while (iterations < MAX_ITERATIONS) {
            OptionalDouble maxFitnessOptional = population.stream()
                    .mapToDouble(HeuristicSolution::getFitness)
                    .max();

            double maxFitness = maxFitnessOptional.isPresent() ? maxFitnessOptional.getAsDouble() : 0.0; // Or any default value

            OptionalDouble minFitnessOptional = population.stream()
                    .mapToDouble(HeuristicSolution::getFitness)
                    .min();

            double minFitness = minFitnessOptional.isPresent() ? minFitnessOptional.getAsDouble() : 0.0; // Or any default value
//        	  if (iterations % 20 == 0) {
//              	logger.debug("[#{}] : {}", iterations, maxFitness);
//              	logger.debug("[#{}] : {}", iterations, minFitness);
//              }
            List<CloudletToVmMappingSolution> sparks = new ArrayList<>();
            for (CloudletToVmMappingSolution firework : population) {
                int numSparks = calculateNumSparks(firework, maxFitness, population);
                double amplitude = calculateAmplitude(firework, minFitness, population);

                for (int i = 0; i < numSparks; i++) {
                    sparks.add(createSpark(firework, amplitude));
                }
            }

            for (int i = 0; i < (int) (population.size() * 0.1); i++) {
                sparks.add(createGaussianSpark(population.get((int) (population.size() * random.sample()))));
            }

            population.addAll(sparks);
            updateBestSolution(population);
            population = selectNewPopulation(population);

            if (iterations % 50 == 0) {
                logger.debug("[#{}] : {}", iterations, bestSolutionSoFar.getResult());
                logger.debug("[#%5d] : %.6f".formatted(iterations, bestSolutionSoFar.getFitness()));
            }
            iterations++;
        }
        setSolveTime((System.currentTimeMillis() - startTime) / 1000.0);
        return bestSolutionSoFar;
    }
}
