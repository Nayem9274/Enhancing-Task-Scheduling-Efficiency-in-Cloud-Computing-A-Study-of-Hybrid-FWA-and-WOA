package edu.buet.thesis.le.csa;

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

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CloudletToVmMappingCrowSearchAlgorithm
        implements CloudletToVmMappingHeuristic {
    public final static int FLOCK_SIZE = 200;
    public final static int MAX_ITERATIONS = 100;
    public static final double FLIGHT_LENGTH = 1.1;
    public static final double AWARENESS_PROBABILITY = 0.2;

    private final ContinuousDistribution random;
    private int searchesByIteration;
    private double solveTime;
    private CloudletToVmMappingSolution bestSolutionSoFar, initialSolution;
    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;

    private final static Logger logger = LoggerFactory.getLogger(CloudletToVmMappingCrowSearchAlgorithm.class.getSimpleName());

    public CloudletToVmMappingCrowSearchAlgorithm(ContinuousDistribution random) {
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

    private Double[][] standardize(double[][] crow) {
        Double[][] adjusted = new Double[crow.length][crow[0].length];
        for (int i = 0; i < crow.length; i++) {
            int maxIndex = 0;
            Arrays.fill(adjusted[i], 0.0);
            for (int j = 1; j < crow[i].length; j++)
                if (crow[i][j] > crow[i][maxIndex])
                    maxIndex = j;
            adjusted[i][maxIndex] = 1.0;
        }
        return adjusted;
    }

    private Double[][] generateCrow(Double[][] crow) {
        for (int i = 0; i < crow.length; i++) {
            Arrays.fill(crow[i], 0.0);
            crow[i][this.getRandomValue(crow[0].length)] = 1.0;
        }
        return crow;
    }

    private Double[][][] getFlock() {
        Double[][][] flock = new Double[FLOCK_SIZE][this.cloudletList.size()][this.vmList.size()];
        for (var doubles : flock) {
            generateCrow(doubles);
        }
        return flock;
    }

    private boolean isFeasible(Double[][] crow) {
        return Arrays.stream(crow)
                .mapToDouble(value -> Arrays.stream(value).reduce(0.0, Double::sum))
                .reduce(1.0, (left, right) -> left * right) == 1.0;
    }

    CloudletToVmMappingSolution solutionFromCrow(Double[][] crow) {
        CloudletToVmMappingSolution solution = new WeightedCloudletToVmSolution(this);
        for (int i = 0; i < crow.length; i++) {
            int maxIndex = 0;
            for (int j = 1; j < crow[i].length; j++)
                if (crow[i][j] > crow[i][maxIndex])
                    maxIndex = j;
            solution.bindCloudletToVm(this.cloudletList.get(i), this.vmList.get(maxIndex));
        }
        return solution;
    }

    List<CloudletToVmMappingSolution> solutionsFromFlock(Double[][][] flock) {
        return Arrays.stream(flock).map(this::solutionFromCrow)
                .toList();
    }

    static <T> T[][] deepCopy(T[][] matrix) {
        return Arrays.stream(matrix)
                .map(arr -> arr.clone())
                .toArray(s -> matrix.clone());
    }

    static <T> T[][][] deepCopy(T[][][] matrix) {
        return Arrays.stream(matrix)
                .map(arr -> deepCopy(arr))
                .toArray(s -> matrix.clone());
    }

    @Override
    public CloudletToVmMappingSolution solve() {
        final long startTime = System.currentTimeMillis();

        var population = getFlock();
        var solutions = solutionsFromFlock(population);
        var memory = deepCopy(population);
        var memoryFitness = solutions.stream().mapToDouble(HeuristicSolution::getFitness).toArray();
        this.bestSolutionSoFar = solutions.get(IntStream.range(0, memoryFitness.length)
                .reduce(0, (left, right) -> memoryFitness[right] > memoryFitness[left] ? left : right));

        int iterations = 1;
        do {
            for (int p = 0; p < population.length; p++) {
                final var followerCrow = population[p];

                var followedCrowIndex = p;
                while (followedCrowIndex == p)
                    followedCrowIndex = this.getRandomValue(population.length);
                final var followedCrow = population[followedCrowIndex];

                var rp = this.random.sample();
                if (rp >= AWARENESS_PROBABILITY) {
                    var r = Stream.generate(this.random::sample).limit(followedCrow.length).toArray(Double[]::new);
                    var d = IntStream.range(0, followedCrow.length).mapToObj(
                            i -> IntStream.range(0, followedCrow[i].length).mapToDouble(
                                    j -> followerCrow[i][j] + r[i] * FLIGHT_LENGTH * (followedCrow[i][j] - followerCrow[i][j])
                            ).toArray()
                    ).toArray(double[][]::new);
                    var crow = standardize(d);
                    if (isFeasible(crow))
                        population[p] = crow;
                } else {
                    population[p] = generateCrow(new Double[followerCrow.length][followedCrow[0].length]);
                }

                // Feasibility check
                final var followerCrowUpdated = population[p];
                var solution = solutionFromCrow(followerCrowUpdated);

                if (solution.getFitness() > memoryFitness[p]) {
                    memory[p] = followerCrowUpdated;
                    memoryFitness[p] = solution.getFitness();

                    if (solution.getFitness() > bestSolutionSoFar.getFitness())
                        bestSolutionSoFar = solution;

                } else if (solution.getFitness() < memoryFitness[p]) {
                    population[p] = memory[p];
                }
            }
            if (iterations % 20 == 0)
                logger.debug("[#%5d] : %.6f".formatted(iterations, bestSolutionSoFar.getFitness()));
            iterations++;
        } while (iterations <= MAX_ITERATIONS);
        setSolveTime((System.currentTimeMillis() - startTime) / 1000.0);
        return bestSolutionSoFar;
    }


}
