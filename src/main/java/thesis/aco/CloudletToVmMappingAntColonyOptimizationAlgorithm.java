package thesis.aco;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.distributions.ContinuousDistribution;
import org.cloudsimplus.heuristics.CloudletToVmMappingHeuristic;
import org.cloudsimplus.heuristics.CloudletToVmMappingSolution;
import org.cloudsimplus.vms.Vm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cloudsimplus.heuristics.Heuristic;
import thesis.common.WeightedCloudletToVmSolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CloudletToVmMappingAntColonyOptimizationAlgorithm implements CloudletToVmMappingHeuristic {
    private static final int POPULATION_SIZE = 50;
    private static final int MAX_ITERATIONS = 50;
    private static final double EVAPORATION_RATE = 0.05;
    private static final double PHEROMONE_INITIAL_VALUE = 0.5;

    private final ContinuousDistribution random;
    private int searchesByIteration;
    private double solveTime;
    private CloudletToVmMappingSolution bestSolutionSoFar, initialSolution;
    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;
    private double[][] pheromoneLevels;

    private static final Logger logger = LoggerFactory.getLogger(CloudletToVmMappingAntColonyOptimizationAlgorithm.class.getSimpleName());

    public CloudletToVmMappingAntColonyOptimizationAlgorithm(ContinuousDistribution random) {
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
        initializePheromoneLevels();
        return this;
    }

    @Override
    public CloudletToVmMappingHeuristic setVmList(List<Vm> list) {
        this.vmList = list;
        return this;
    }

    private void initializePheromoneLevels() {
        pheromoneLevels = new double[cloudletList.size()][vmList.size()];
        for (int i = 0; i < cloudletList.size(); i++) {
            for (int j = 0; j < vmList.size(); j++) {
                pheromoneLevels[i][j] = PHEROMONE_INITIAL_VALUE;
            }
        }
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

    private Vm selectVmForCloudlet(int cloudletIdx) {
        double totalPheromone = 0.0;
        for (double pheromone : pheromoneLevels[cloudletIdx]) {
            totalPheromone += pheromone;
        }

        double threshold = random.sample() * totalPheromone;
        double cumulativePheromone = 0.0;
        for (int vmIdx = 0; vmIdx < vmList.size(); vmIdx++) {
            cumulativePheromone += pheromoneLevels[cloudletIdx][vmIdx];
            if (cumulativePheromone >= threshold) {
                return vmList.get(vmIdx);
            }
        }
        return vmList.get(vmList.size() - 1);  // default if threshold not reached
    }

    @Override
    public double getAcceptanceProbability() {
        return 0;
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


    @Override
    public boolean isToStopSearch() {
        return false;
    }

    @Override
    public CloudletToVmMappingSolution getInitialSolution() {
        if (this.initialSolution == null || this.initialSolution.getResult().isEmpty()) {
            this.initialSolution = this.generateRandomSolution();
        }
        return this.initialSolution;
    }

    @Override
    public CloudletToVmMappingSolution getNeighborSolution() {
        return null;
    }

    @Override
    public CloudletToVmMappingSolution createNeighbor(CloudletToVmMappingSolution source) {
        return null;
    }

    @Override
    public CloudletToVmMappingSolution getBestSolutionSoFar() {
        return bestSolutionSoFar;
    }

    private CloudletToVmMappingSolution generateRandomSolution() {
        CloudletToVmMappingSolution solution = new WeightedCloudletToVmSolution(this);
        cloudletList.forEach(cloudlet -> solution.bindCloudletToVm(cloudlet, getRandomVm()));
        return solution;
    }

    private List<CloudletToVmMappingSolution> generateAntSolutions() {
        List<CloudletToVmMappingSolution> solutions = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            CloudletToVmMappingSolution solution = generateRandomSolution();
            solutions.add(solution);
        }
        return solutions;
    }

    private void evaporatePheromones() {
        for (int i = 0; i < cloudletList.size(); i++) {
            for (int j = 0; j < vmList.size(); j++) {
                pheromoneLevels[i][j] *= (1 - EVAPORATION_RATE);
            }
        }
    }

    private void updatePheromones(CloudletToVmMappingSolution solution) {
        double pheromoneAmount = 1.0 / solution.getFitness();
        for (Cloudlet cloudlet : cloudletList) {
            int cloudletIdx = cloudletList.indexOf(cloudlet);  // Safely find index in list
            int vmIdx = vmList.indexOf(solution.getResult().get(cloudlet)); // Safely find index in list

            // Ensure indices are within bounds before accessing pheromoneLevels
            if (cloudletIdx >= 0 && cloudletIdx < cloudletList.size() &&
                    vmIdx >= 0 && vmIdx < vmList.size()) {
                pheromoneLevels[cloudletIdx][vmIdx] += pheromoneAmount;
            } else {
                // Log a warning or handle the out-of-bounds case as needed
                logger.warn("Cloudlet or VM index out of bounds in updatePheromones.");
            }
        }
    }

    private void updateBestSolution(List<CloudletToVmMappingSolution> population) {
        population.sort((s1, s2) -> Double.compare(s2.getFitness(), s1.getFitness()));
        if (bestSolutionSoFar == null || population.get(0).getFitness() > bestSolutionSoFar.getFitness()) {
            bestSolutionSoFar = population.get(0);
        }
    }

    @Override
    public CloudletToVmMappingSolution solve() {
        long startTime = System.currentTimeMillis();
        List<CloudletToVmMappingSolution> population = generateAntSolutions();
        updateBestSolution(population);

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            evaporatePheromones();

            List<CloudletToVmMappingSolution> newSolutions = generateAntSolutions();
            for (CloudletToVmMappingSolution solution : newSolutions) {
                updatePheromones(solution);
            }

            population = newSolutions;
            updateBestSolution(population);
            logger.debug("[#{}] : {}", iteration, bestSolutionSoFar.getFitness());
        }

        setSolveTime((System.currentTimeMillis() - startTime) / 1000.0);
        return bestSolutionSoFar;
    }
}
