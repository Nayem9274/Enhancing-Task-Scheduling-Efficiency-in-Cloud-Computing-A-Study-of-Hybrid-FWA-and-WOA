package thesis.wgoa;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.distributions.ContinuousDistribution;
import org.cloudsimplus.heuristics.CloudletToVmMappingHeuristic;
import org.cloudsimplus.heuristics.CloudletToVmMappingSolution;
import org.cloudsimplus.heuristics.Heuristic;
import org.cloudsimplus.heuristics.HeuristicSolution;
import org.cloudsimplus.vms.Vm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.buet.thesis.le.ga.CloudletToVmMappingGeneticAlgorithm;
import thesis.common.WeightedCloudletToVmSolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CloudletToVmMappingWGOA implements CloudletToVmMappingHeuristic {
    public static final int POPULATION_SIZE = 100;
    public static final int MAX_ITERATIONS = 400;

    private final ContinuousDistribution random;
    private int searchesByIteration;
    private double solveTime;
    private CloudletToVmMappingSolution bestSolutionSoFar, initialSolution;
    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;
   
    public static final double CROSSOVER_RATE = 0.9;
    public static final double MUTATION_RATE = 0.1;


    

    private final static Logger logger = LoggerFactory.getLogger(CloudletToVmMappingWGOA.class.getSimpleName());

    public CloudletToVmMappingWGOA(ContinuousDistribution random) {
        this.random = random;
        //new Random().setSeed(System.currentTimeMillis()); // Ensure different seeds for each run
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
    //
    private Vm getRandomVm() {
        int idx = getRandomValue(vmList.size());
        return vmList.get(idx);
    }

//    public Vm getRandomVm() {
//        int idx = (int) (Math.random() * vmList.size()); // Use Math.random() for uniform distribution
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
//        // Ensure a more even distribution of cloudlets to VMs
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

    private List<CloudletToVmMappingSolution> generateInitialPopulation() {
        List<CloudletToVmMappingSolution> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(generateRandomSolution());
        }
        return population;
    }

    public double[] updateCoefficients(int iteration) {
        double a = 2 * (1 - (double) iteration / MAX_ITERATIONS);
        double r = random.sample();
        double A = 2 * a * r - a;
        double C = 2 * r;
        //System.out.println("A: "+A+" C: "+C+" a: "+a);
        return new double[]{A, C, a};
    }

    public CloudletToVmMappingSolution createNewPosition(CloudletToVmMappingSolution whale, CloudletToVmMappingSolution best, double A, double C) {
        CloudletToVmMappingSolution newWhale = new WeightedCloudletToVmSolution(this);
        for (Cloudlet cloudlet : cloudletList) {
            Vm bestVm = best.getResult().get(cloudlet);
            Vm whaleVm = whale.getResult().get(cloudlet);
            double D = Math.abs(C * bestVm.getId() - whaleVm.getId());
            //System.out.println("D(new pos): "+D);
            int newVmId = (int) (bestVm.getId() - A * D);
            //System.out.println("newVmId(new pos): "+newVmId);
            newVmId = Math.floorMod(newVmId, vmList.size());
            //System.out.println("newVmId(new pos) after MOD: "+newVmId);
            if (newVmId == bestVm.getId()) {
                newVmId = (newVmId + 1) % vmList.size();
            }
            //System.out.println("newVmId(new pos) after MOD: "+newVmId);
            newWhale.bindCloudletToVm(cloudlet, vmList.get(newVmId));
        }
        return newWhale;
    }

    public CloudletToVmMappingSolution createSpiralPosition(CloudletToVmMappingSolution whale, CloudletToVmMappingSolution best) {
        CloudletToVmMappingSolution newWhale = new WeightedCloudletToVmSolution(this);
        double b = 2;
        double l = random.sample() * 2 - 1; // a random number in [ −1,1]
        for (Cloudlet cloudlet : cloudletList) {
            Vm bestVm = best.getResult().get(cloudlet);
            Vm whaleVm = whale.getResult().get(cloudlet);
            double D = Math.abs(bestVm.getId() - whaleVm.getId());
            // System.out.println("D(spiral pos): "+D);
            int newVmId = (int) (D * Math.exp(b * l) * Math.cos(2 * Math.PI * l) + bestVm.getId());
            //System.out.println("newVmId(spiral pos) : "+newVmId);
            newVmId = Math.floorMod(newVmId, vmList.size());
            //System.out.println("newVmId(spiral pos) after MOD: "+newVmId);
            if (newVmId == bestVm.getId()) {
                newVmId = (newVmId + 1) % vmList.size();
            }
            newWhale.bindCloudletToVm(cloudlet, vmList.get(newVmId));
        }
        return newWhale;
    }

    public void updateBestSolution(List<CloudletToVmMappingSolution> population) {
        population.sort((s1, s2) -> Double.compare(s2.getFitness(), s1.getFitness()));
        if (bestSolutionSoFar == null || population.get(0).getFitness() > bestSolutionSoFar.getFitness()) {
            bestSolutionSoFar = population.get(0);
        }
    }
    
   
    private List<CloudletToVmMappingSolution> generatePopulation() {
        int populationSize = POPULATION_SIZE;
        List<CloudletToVmMappingSolution> population = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            population.add(this.generateRandomSolution());
        }
        return population;
    }

    private CloudletToVmMappingSolution[] selectParents(List<CloudletToVmMappingSolution> population) {
        CloudletToVmMappingSolution[] parents = new WeightedCloudletToVmSolution[2];
        // sum fitness to assign probability and select parents
        double sumFitness = population.stream().mapToDouble(CloudletToVmMappingSolution::getFitness).sum();
        double[] probabilities = population.stream().mapToDouble((solution) -> solution.getFitness() / sumFitness).toArray();
        double[] cumulativeProbabilities = new double[probabilities.length];
        cumulativeProbabilities[0] = probabilities[0];
        for (int i = 1; i < probabilities.length; i++)
            cumulativeProbabilities[i] = cumulativeProbabilities[i - 1] + probabilities[i];

        for (int i = 0; i < 2; i++) {
            double random = this.random.sample();
            for (int j = 0; j < cumulativeProbabilities.length; j++) {
                if (random < cumulativeProbabilities[j]) {
                    parents[i] = population.get(j);
                    break;
                }
            }
        }

        return parents;
    }

    private CloudletToVmMappingSolution[] crossover(List<CloudletToVmMappingSolution> population) {
        CloudletToVmMappingSolution[] parents = selectParents(population);
        while (parents[0] == parents[1]) {
            parents = selectParents(population);
        }
        CloudletToVmMappingSolution father = parents[0];
        CloudletToVmMappingSolution mother = parents[1];

        CloudletToVmMappingSolution son = new WeightedCloudletToVmSolution(father);
        CloudletToVmMappingSolution daughter = new WeightedCloudletToVmSolution(mother);

        var cloudlets = new ArrayList<>(father.getResult().keySet());

        for (var cloudlet : cloudlets) {
            if (this.random.sample() < 0.5) {
                var fatherVm = father.getResult().get(cloudlet);
                var motherVm = mother.getResult().get(cloudlet);
                son.bindCloudletToVm(cloudlet, motherVm);
                daughter.bindCloudletToVm(cloudlet, fatherVm);
            }
        }

        return new CloudletToVmMappingSolution[]{son, daughter};
    }

    private void mutation(CloudletToVmMappingSolution solution) {
        var cloudlets = new ArrayList<>(solution.getResult().keySet());
        var cloudlet = cloudlets.get(this.getRandomValue(cloudlets.size()));
        var vm = this.getRandomVm();
        solution.bindCloudletToVm(cloudlet, vm);
    }

    private void trimPopulation(List<CloudletToVmMappingSolution> population) {
        Collections.sort(population);
        while (population.size() > POPULATION_SIZE) {
            population.remove(0);
        }
    }
    
    @Override
  public CloudletToVmMappingSolution solve() {
    long startTime = System.currentTimeMillis();
    List<CloudletToVmMappingSolution> population = generateInitialPopulation(); // Generate initial population
    updateBestSolution(population); // Update the best solution so far

    for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
        double[] coefficients = updateCoefficients(iteration); // Calculate A, C, and a
        double A = coefficients[0];
        double C = coefficients[1];
        double a = coefficients[2];

        List<CloudletToVmMappingSolution> newPopulation = new ArrayList<>();

        for (CloudletToVmMappingSolution whale : population) {
            double p = random.sample(); // Random probability for choosing strategy
            if (p < 0.5) {
                if (Math.abs(A) < 1) {
                    // Exploitation: Update position using the best solution
                    newPopulation.add(createNewPosition(whale, bestSolutionSoFar, A, C));
                } else {
                    // Exploration: Random position selection
                    int randIdx = getRandomValue(population.size());
                    newPopulation.add(createNewPosition(whale, population.get(randIdx), A, C));
                }
            } else {
                // Spiral updating position
                newPopulation.add(createSpiralPosition(whale, bestSolutionSoFar));
            }
        }

        // Apply crossover and mutation after updating positions
        if (random.sample() < CROSSOVER_RATE) {
            var children = crossover(newPopulation); // Apply crossover
            if (random.sample() < MUTATION_RATE) {
                mutation(children[getRandomValue(2)]); // Apply mutation on one child
            }
            System.err.println("IN GENEEEEEEEE\n");
            newPopulation.addAll(Arrays.asList(children)); // Add children to the population
        }

        population = newPopulation; // Update the population with the new one
        updateBestSolution(population); // Update the best solution found

        // Debugging and logging every 50 iterations
        if (iteration % 50 == 0) {
            logger.debug("[#{}] : {}", iteration, bestSolutionSoFar.getResult());
            logger.debug("[#{}] : {}", iteration, bestSolutionSoFar.getFitness());
        }
    }

    // Set the total time taken for the solution
    setSolveTime((System.currentTimeMillis() - startTime) / 1000.0);

    return bestSolutionSoFar; // Return the best solution found
}

}
