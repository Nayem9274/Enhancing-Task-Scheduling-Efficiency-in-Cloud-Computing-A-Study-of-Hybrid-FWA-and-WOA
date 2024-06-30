package edu.buet.thesis.le.ga;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.distributions.ContinuousDistribution;
import org.cloudsimplus.heuristics.CloudletToVmMappingHeuristic;
import org.cloudsimplus.heuristics.CloudletToVmMappingSolution;
import org.cloudsimplus.heuristics.Heuristic;
import org.cloudsimplus.vms.Vm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import thesis.common.WeightedCloudletToVmSolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Define a class that implements CloudletToVmMappingHeuristic interface
public class CloudletToVmMappingGeneticAlgorithm
        implements CloudletToVmMappingHeuristic {

    private final static Logger logger = LoggerFactory.getLogger(CloudletToVmMappingGeneticAlgorithm.class.getSimpleName());

    public final static int POPULATION_SIZE = 100;
    public final static int MAX_ITERATIONS = 1000;
    public static final double CROSSOVER_RATE = 0.9;
    public static final double MUTATION_RATE = 0.1;

    private final ContinuousDistribution random;
    private int searchesByIteration;
    private double solveTime;
    private CloudletToVmMappingSolution bestSolutionSoFar, initialSolution;
    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;


    // Define a constructor for the class
    public CloudletToVmMappingGeneticAlgorithm(ContinuousDistribution random) {
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
        int idx = this.getRandomValue(this.vmList.size());
        return this.vmList.get(idx);
    }

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
        this.cloudletList.forEach((cloudlet) -> solution.bindCloudletToVm(cloudlet, this.getRandomVm()));
        return solution;
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
        final long startTime = System.currentTimeMillis();
        this.bestSolutionSoFar = getInitialSolution();
        var population = generatePopulation();
        int iterations = 1;
        do {
            trimPopulation(population);
            if (iterations % 20 == 0) {
                bestSolutionSoFar = population.get(population.size() - 1);
                //logger.debug("[#%5d] : %.6f".formatted(iterations, bestSolutionSoFar.getFitness()));
            }

            if (this.random.sample() < CROSSOVER_RATE) {
                var children = crossover(population);
                if (this.random.sample() < MUTATION_RATE) {
                    mutation(children[this.getRandomValue(2)]);
                }
                population.addAll(Arrays.asList(children));
            }

            iterations++;
        } while (iterations <= MAX_ITERATIONS);
        setSolveTime((System.currentTimeMillis() - startTime) / 1000.0);

        return bestSolutionSoFar;
    }

}

