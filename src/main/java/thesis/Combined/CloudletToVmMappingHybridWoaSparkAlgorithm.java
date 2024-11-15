package thesis.Combined;

import thesis.common.WeightedCloudletToVmSolution;
import thesis.fwa.CloudletToVmMappingFireworksAlgorithm;
import thesis.woa.CloudletToVmMappingWhaleOptimizationAlgorithm;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.distributions.ContinuousDistribution;
import org.cloudsimplus.heuristics.CloudletToVmMappingHeuristic;
import org.cloudsimplus.heuristics.CloudletToVmMappingSolution;
import org.cloudsimplus.heuristics.Heuristic;
import org.cloudsimplus.heuristics.HeuristicSolution;
import org.cloudsimplus.vms.Vm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CloudletToVmMappingHybridWoaSparkAlgorithm implements CloudletToVmMappingHeuristic {
    private final ContinuousDistribution random;
    private int searchesByIteration;
    private double solveTime;
    private CloudletToVmMappingSolution bestSolutionSoFar, initialSolution;
    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;
    private final static Logger logger = LoggerFactory
            .getLogger(CloudletToVmMappingHybridFwaEncircleAlgorithm.class.getSimpleName());

    public static final int MAX_ITER_FWA = 200;
    public static final int MAX_ITER_WOA = 100;
    public static final int POPULATION_SIZE = 100;
    public static final int A = 40;
    public static final int m = 800;
    public static final double a = 0.04;
    public static final double b = 0.8;
    public static final double EPSILON = 1e-6;

    public CloudletToVmMappingHybridWoaSparkAlgorithm(ContinuousDistribution random) {
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

    // private Vm getRandomVm() {
    // int idx = (int) (Math.random() * vmList.size()); // Use Math.random() for
    // uniform distribution
    // return vmList.get(idx);
    // }

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

    // private CloudletToVmMappingSolution generateRandomSolution() {
    // CloudletToVmMappingSolution solution = new
    // WeightedCloudletToVmSolution(this);
    // cloudletList.forEach(cloudlet -> solution.bindCloudletToVm(cloudlet,
    // getRandomVm()));
    // return solution;
    // }

    private CloudletToVmMappingSolution generateRandomSolution() {
        CloudletToVmMappingSolution solution = new WeightedCloudletToVmSolution(this);
        List<Vm> shuffledVms = new ArrayList<>(vmList);
        Collections.shuffle(shuffledVms);

        int vmIndex = 0;
        for (Cloudlet cloudlet : cloudletList) {
            solution.bindCloudletToVm(cloudlet, shuffledVms.get(vmIndex));
            vmIndex = (vmIndex + 1) % shuffledVms.size();
        }
        return solution;
    }

    private CloudletToVmMappingSolution generateRandomSolution2() {
        CloudletToVmMappingSolution solution = new WeightedCloudletToVmSolution(this);
        List<Vm> shuffledVms = new ArrayList<>(vmList);
        Collections.shuffle(shuffledVms);

        // Track usage of VMs to avoid consecutive assignments
        int[] vmUsageCount = new int[vmList.size()];

        for (Cloudlet cloudlet : cloudletList) {
            // Find the least used VM
            int minUsageIndex = 0;
            for (int i = 1; i < vmUsageCount.length; i++) {
                if (vmUsageCount[i] < vmUsageCount[minUsageIndex]) {
                    minUsageIndex = i;
                }
            }

            // Assign cloudlet to the least used VM
            Vm selectedVm = shuffledVms.get(minUsageIndex);
            solution.bindCloudletToVm(cloudlet, selectedVm);
            vmUsageCount[minUsageIndex]++;

            // Shuffle VMs to ensure randomness
            Collections.shuffle(shuffledVms);
        }

        return solution;
    }

    private List<CloudletToVmMappingSolution> generateInitialPopulation() {
        List<CloudletToVmMappingSolution> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(generateRandomSolution());
        }
        return population;
    }

    private List<CloudletToVmMappingSolution> generateInitialPopulationWOA() {
        List<CloudletToVmMappingSolution> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(generateRandomSolution2());
        }
        return population;
    }

    public int calculateNumSparks(CloudletToVmMappingSolution firework, double maxFitness,
            List<CloudletToVmMappingSolution> population) {
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

    public double calculateAmplitude(CloudletToVmMappingSolution firework, double minFitness,
            List<CloudletToVmMappingSolution> population) {
        // Calculate the summation of (fitness of each solution - minFitness) + epsilon
        double fitnessDifferenceSum = 0.0;
        for (CloudletToVmMappingSolution solution : population) {
            fitnessDifferenceSum += (solution.getFitness() - minFitness);
        }

        // Calculate Ai using eqn 4
        double ai = A * ((firework.getFitness() - minFitness + EPSILON) / (fitnessDifferenceSum + EPSILON));

        return ai;
    }

    public CloudletToVmMappingSolution createSpark(CloudletToVmMappingSolution firework, double amplitude) {
        CloudletToVmMappingSolution spark = new WeightedCloudletToVmSolution(this);
        for (Cloudlet cloudlet : cloudletList) {
            Vm vm = firework.getResult().get(cloudlet);
            spark.bindCloudletToVm(cloudlet, vm);
        }
        mutateSpark(spark, amplitude);
        return spark;
    }

    public void mutateSpark(CloudletToVmMappingSolution spark, double amplitude) {
        // Determine the number of dimensions to mutate
        int z = (int) (cloudletList.size() * random.sample());
        List<Integer> dimensions = new ArrayList<>(
                IntStream.range(0, cloudletList.size()).boxed().collect(Collectors.toList()));
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

    public CloudletToVmMappingSolution createGaussianSpark(CloudletToVmMappingSolution firework) {
        CloudletToVmMappingSolution spark = new WeightedCloudletToVmSolution(this);
        for (Cloudlet cloudlet : cloudletList) {
            Vm vm = firework.getResult().get(cloudlet);
            spark.bindCloudletToVm(cloudlet, vm);
        }
        mutateGaussianSpark(spark);
        return spark;
    }

    public void mutateGaussianSpark(CloudletToVmMappingSolution spark) {
        // Determine the number of dimensions to mutate
        int z = (int) (cloudletList.size() * random.sample());
        List<Integer> dimensions = new ArrayList<>(
                IntStream.range(0, cloudletList.size()).boxed().collect(Collectors.toList()));
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

    public void updateBestSolution(List<CloudletToVmMappingSolution> population) {
        population.sort((s1, s2) -> Double.compare(s2.getFitness(), s1.getFitness()));
        if (bestSolutionSoFar == null || population.get(0).getFitness() > bestSolutionSoFar.getFitness()) {
            bestSolutionSoFar = population.get(0);
        }
    }

    public List<CloudletToVmMappingSolution> selectNewPopulation(List<CloudletToVmMappingSolution> population) {
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

    public double calculateDistance(CloudletToVmMappingSolution solution) {
        double distance = 0;
        for (Cloudlet cloudlet : cloudletList) {
            // int idx = cloudletList.indexOf(cloudlet);
            Vm vm1 = solution.getResult().get(cloudlet);
            Vm vm2 = bestSolutionSoFar.getResult().get(cloudlet);
            distance += Math.pow(vm1.getId() - vm2.getId(), 2);
        }
        return Math.sqrt(distance);
    }

    public double[] updateCoefficients(int iteration) {
        double a = 2 * (1 - (double) iteration / MAX_ITER_WOA);
        double r = random.sample();
        double A = 2 * a * r - a;
        double C = 2 * r;
        return new double[] { A, C, a };
    }

    public CloudletToVmMappingSolution createNewPosition(CloudletToVmMappingSolution whale,
            CloudletToVmMappingSolution best, double A, double C) {
        CloudletToVmMappingSolution newWhale = new WeightedCloudletToVmSolution(this);
        for (Cloudlet cloudlet : cloudletList) {
            Vm bestVm = best.getResult().get(cloudlet);
            Vm whaleVm = whale.getResult().get(cloudlet);
            double D = Math.abs(C * bestVm.getId() - whaleVm.getId());
            int newVmId = (int) (bestVm.getId() - A * D);
            newVmId = Math.floorMod(newVmId, vmList.size());
            if (newVmId == bestVm.getId()) {
                newVmId = (newVmId + 1) % vmList.size();
            }
            newWhale.bindCloudletToVm(cloudlet, vmList.get(newVmId));
        }
        return newWhale;
    }

    public CloudletToVmMappingSolution createSpiralPosition(CloudletToVmMappingSolution whale,
            CloudletToVmMappingSolution best) {
        CloudletToVmMappingSolution newWhale = new WeightedCloudletToVmSolution(this);
        double b = 2;
        double l = random.sample() * 2 - 1; // a random number in [ −1,1]
        for (Cloudlet cloudlet : cloudletList) {
            Vm bestVm = best.getResult().get(cloudlet);
            Vm whaleVm = whale.getResult().get(cloudlet);
            double D = Math.abs(bestVm.getId() - whaleVm.getId());
            int newVmId = (int) (D * Math.exp(b * l) * Math.cos(2 * Math.PI * l) + bestVm.getId());
            newVmId = Math.floorMod(newVmId, vmList.size());
            if (newVmId == bestVm.getId()) {
                newVmId = (newVmId + 1) % vmList.size();
            }
            newWhale.bindCloudletToVm(cloudlet, vmList.get(newVmId));
        }
        return newWhale;
    }

    private List<CloudletToVmMappingSolution> selectBestSparks(List<CloudletToVmMappingSolution> sparks,
            int targetSize) {
        sparks.sort(Comparator.comparingDouble(CloudletToVmMappingSolution::getFitness).reversed());
        return sparks.subList(0, Math.min(sparks.size(), targetSize));
    }

    // maintain population size
    private List<CloudletToVmMappingSolution> maintainPopulationSize(List<CloudletToVmMappingSolution> newPopulation,
            int targetSize) {
        if (newPopulation.size() > targetSize) {
            newPopulation.sort(Comparator.comparingDouble(CloudletToVmMappingSolution::getFitness).reversed());
            return newPopulation.subList(0, targetSize);
        } else {
            return newPopulation;
        }
    }

    @Override
    public CloudletToVmMappingSolution solve() {
        long startTime = System.currentTimeMillis();

        // Initial population with FWA
        List<CloudletToVmMappingSolution> population = generateInitialPopulation();
        population.sort((s1, s2) -> Double.compare(s2.getFitness(), s1.getFitness()));
        bestSolutionSoFar = population.get(0);
        updateBestSolution(Collections.singletonList(bestSolutionSoFar));

        int fwaIterations = 0;
        int woaIterations = 0;
        int maxNoImprovementIters = 10;
        int noImprovementCounter = 0;
        double previousBestFitness = bestSolutionSoFar.getFitness();

        while (woaIterations < MAX_ITER_FWA) {

            List<CloudletToVmMappingSolution> woaPopulation = new ArrayList<>(population);

            woaPopulation = runWoaWithSpark(woaPopulation, fwaIterations);
            woaIterations++;

            // System.out.println("FWA Pop Size: "+ fwaPopulation.size());
            // logger.debug("[#{}] : {}", fwaIterations, bestSolutionSoFar.getResult());
            logger.debug("WOA [#%5d] : %.6f".formatted(woaIterations, bestSolutionSoFar.getFitness()));
            logger.debug("[#{}] : {}", bestSolutionSoFar.getResult());

            // population = new ArrayList<>();
            population.addAll(woaPopulation);
            population = maintainPopulationSize(population, POPULATION_SIZE);
            updateBestSolution(population);

            // Checking for improvement
            double currentBestFitness = bestSolutionSoFar.getFitness();
            if (currentBestFitness <= previousBestFitness) {
                noImprovementCounter++;
            } else {
                noImprovementCounter = 0;
                previousBestFitness = currentBestFitness;
            }

            // Switching if no improvement
            if (noImprovementCounter >= maxNoImprovementIters) {
                noImprovementCounter = 0;
                // introduce new individuals to increase diversity
                population = introduceMoreDiversity(population);
            }
        }
        logger.debug("FINAL [#%5d] : %.6f".formatted(woaIterations, bestSolutionSoFar.getFitness()));
        logger.debug("[#{}] : {}", bestSolutionSoFar.getResult());
        setSolveTime((System.currentTimeMillis() - startTime) / 1000.0);
        return bestSolutionSoFar;
    }

    private List<CloudletToVmMappingSolution> runWoaWithSpark(List<CloudletToVmMappingSolution> population, int woaIterations) {
        double[] coefficients = updateCoefficients(woaIterations);
        double A = coefficients[0];
        double C = coefficients[1];
        double a = coefficients[2];
        OptionalDouble minFitnessOptional = population.stream()
                .mapToDouble(HeuristicSolution::getFitness)
                .min();

        double minFitness = minFitnessOptional.isPresent() ? minFitnessOptional.getAsDouble() : 0.0;
        List<CloudletToVmMappingSolution> newPopulation = new ArrayList<>();
        // System.out.println("WOA newPopulation Size(Initial): "+
        // newPopulation.size());
        // logger.debug("BEGINNING [#%5d] : %.6f".formatted(woaIterations,
        // bestSolutionSoFar.getFitness()));

        for (CloudletToVmMappingSolution whale : population) {
            double p = random.sample();
            if (p < 0.5) {
                if (Math.abs(A) < 1) {
                    newPopulation.add(createNewPosition(whale, bestSolutionSoFar, A, C));
                } else {
                    int randIdx = getRandomValue(population.size());
                    newPopulation.add(createNewPosition(whale, population.get(randIdx), A, C));
                }
            } else {
                newPopulation.add(createSpiralPosition(whale, bestSolutionSoFar));
            }
            double amplitude = calculateAmplitude(whale, minFitness, population);
            newPopulation.add(createSpark(whale, amplitude));
        }
        population = newPopulation;
        updateBestSolution(population);
        // logger.debug("ENDING [#%5d] : %.6f".formatted(woaIterations,
        // bestSolutionSoFar.getFitness()));

        return population;
    }
    // private List<CloudletToVmMappingSolution> runWoaWithSpark(List<CloudletToVmMappingSolution> population, int woaIterations) {
    //     double[] coefficients = updateCoefficients(woaIterations);
    //     double A = coefficients[0];
    //     double C = coefficients[1];
    //     double a = coefficients[2];
    //     OptionalDouble maxFitnessOptional = population.stream()
    //     .mapToDouble(HeuristicSolution::getFitness)
    //     .max();

    //     double maxFitness = maxFitnessOptional.isPresent() ? maxFitnessOptional.getAsDouble() : 0.0;

    //     OptionalDouble minFitnessOptional = population.stream()
    //             .mapToDouble(HeuristicSolution::getFitness)
    //             .min();

    //     double minFitness = minFitnessOptional.isPresent() ? minFitnessOptional.getAsDouble() : 0.0;
    //     List<CloudletToVmMappingSolution> newPopulation = new ArrayList<>();
    //     // System.out.println("WOA newPopulation Size(Initial): "+
    //     // newPopulation.size());
    //     // logger.debug("BEGINNING [#%5d] : %.6f".formatted(woaIterations,
    //     // bestSolutionSoFar.getFitness()));

    //     for (CloudletToVmMappingSolution whale : population) {
    //         double p = random.sample();
    //         if (p < 0.5) {
    //             if (Math.abs(A) < 1) {
    //                 newPopulation.add(createNewPosition(whale, bestSolutionSoFar, A, C));
    //             } else {
    //                 int randIdx = getRandomValue(population.size());
    //                 newPopulation.add(createNewPosition(whale, population.get(randIdx), A, C));
    //             }
    //         } else {
    //             newPopulation.add(createSpiralPosition(whale, bestSolutionSoFar));
    //         }
    //         double amplitude = calculateAmplitude(whale, minFitness, population);
    //        // newPopulation.add(createSpark(whale, amplitude));
    //          int numSparks = calculateNumSparks(whale, maxFitness, population);
    //         //double amplitude = calculateAmplitude(whale, minFitness, population);

    //         for (int i = 0; i < numSparks; i++) {
    //             newPopulation.add(createSpark(whale, amplitude));
    //         }
    //     }
        
          
    
    //     population = newPopulation;
    //     updateBestSolution(population);
    //     // logger.debug("ENDING [#%5d] : %.6f".formatted(woaIterations,
    //     // bestSolutionSoFar.getFitness()));

    //     return population;
    // }


    private List<CloudletToVmMappingSolution> runFWA(List<CloudletToVmMappingSolution> population) {
        OptionalDouble maxFitnessOptional = population.stream()
                .mapToDouble(HeuristicSolution::getFitness)
                .max();

        double maxFitness = maxFitnessOptional.isPresent() ? maxFitnessOptional.getAsDouble() : 0.0;

        OptionalDouble minFitnessOptional = population.stream()
                .mapToDouble(HeuristicSolution::getFitness)
                .min();

        double minFitness = minFitnessOptional.isPresent() ? minFitnessOptional.getAsDouble() : 0.0;

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

        return population;
    }

    private List<CloudletToVmMappingSolution> runWOA(List<CloudletToVmMappingSolution> population, int woaIterations) {
        double[] coefficients = updateCoefficients(woaIterations);
        double A = coefficients[0];
        double C = coefficients[1];
        double a = coefficients[2];

        List<CloudletToVmMappingSolution> newPopulation = new ArrayList<>();
        // System.out.println("WOA newPopulation Size(Initial): "+
        // newPopulation.size());
        // logger.debug("BEGINNING [#%5d] : %.6f".formatted(woaIterations,
        // bestSolutionSoFar.getFitness()));

        for (CloudletToVmMappingSolution whale : population) {
            double p = random.sample();
            if (p < 0.5) {
                if (Math.abs(A) < 1) {
                    newPopulation.add(createNewPosition(whale, bestSolutionSoFar, A, C));
                } else {
                    int randIdx = getRandomValue(population.size());
                    newPopulation.add(createNewPosition(whale, population.get(randIdx), A, C));
                }
            } else {
                newPopulation.add(createSpiralPosition(whale, bestSolutionSoFar));
            }
        }

        population = newPopulation;
        updateBestSolution(population);
        // logger.debug("ENDING [#%5d] : %.6f".formatted(woaIterations,
        // bestSolutionSoFar.getFitness()));

        return population;
    }

    private List<CloudletToVmMappingSolution> introduceMoreDiversity(List<CloudletToVmMappingSolution> population) {
        // Implement a more aggressive strategy to introduce diversity
        List<CloudletToVmMappingSolution> newPopulation = new ArrayList<>(population);

        // Add new random individuals
        for (int i = 0; i < POPULATION_SIZE / 2; i++) {
            newPopulation.add(generateRandomSolution());
        }

        // Ensure the population size remains constant
        newPopulation.sort((s1, s2) -> Double.compare(s2.getFitness(), s1.getFitness()));
        return newPopulation.subList(0, Math.min(newPopulation.size(), POPULATION_SIZE));
    }

    private List<CloudletToVmMappingSolution> introduceDiversity(List<CloudletToVmMappingSolution> population) {
        int newIndividualsCount = (int) (population.size() * 0.1);
        for (int i = 0; i < newIndividualsCount; i++) {
            population.add(generateRandomSolution2());
        }
        return population;
    }

}
