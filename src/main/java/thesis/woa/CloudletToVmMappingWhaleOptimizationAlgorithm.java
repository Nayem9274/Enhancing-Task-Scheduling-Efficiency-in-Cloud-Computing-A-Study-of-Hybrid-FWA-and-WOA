package thesis.woa;

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
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CloudletToVmMappingWhaleOptimizationAlgorithm implements CloudletToVmMappingHeuristic {
    public static final int POPULATION_SIZE = 10;
    public static final int MAX_ITERATIONS = 100;

    private final ContinuousDistribution random;
    private int searchesByIteration;
    private double solveTime;
    private CloudletToVmMappingSolution bestSolutionSoFar, initialSolution;
    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;
    private final static Logger logger = LoggerFactory.getLogger(CloudletToVmMappingWhaleOptimizationAlgorithm.class.getSimpleName());

    public CloudletToVmMappingWhaleOptimizationAlgorithm(ContinuousDistribution random) {
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
       // System.out.println("A: "+A+" C: "+C+" a: "+a);
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

    @Override
    public CloudletToVmMappingSolution solve() {
        long startTime = System.currentTimeMillis();
        List<CloudletToVmMappingSolution> population = generateInitialPopulation();
        updateBestSolution(population);

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            double[] coefficients = updateCoefficients(iteration);
            double A = coefficients[0];
            double C = coefficients[1];
            double a = coefficients[2];

            List<CloudletToVmMappingSolution> newPopulation = new ArrayList<>();

            for (CloudletToVmMappingSolution whale : population) {
                double p = random.sample();
                if (p < 0.5) {
                    if (Math.abs(A) < 1) {
                        newPopulation.add(createNewPosition(whale, bestSolutionSoFar, A, C));
                    } else {
                        int randIdx = getRandomValue(population.size());
                       // logger.debug("[#{}] : {}", iteration, randIdx);
                        newPopulation.add(createNewPosition(whale, population.get(randIdx), A, C));
                    }
                } else {
                    newPopulation.add(createSpiralPosition(whale, bestSolutionSoFar));
                }
            }
            // This part may be redundant as we are already doing the constraint check after each function

            for (CloudletToVmMappingSolution newWhale : newPopulation) {
                for (Cloudlet cloudlet : cloudletList) {
                    Vm vm = newWhale.getResult().get(cloudlet);
                    if (vm == null || vm.getId() < 0 || vm.getId() >= vmList.size()) {
                        newWhale.bindCloudletToVm(cloudlet, getRandomVm());
                    }
                }
                
            }

            population = newPopulation;
            updateBestSolution(population);

            if (iteration % 50 == 0) {
                logger.debug("[#{}] : {}", iteration, bestSolutionSoFar.getResult());
                logger.debug("[#{}] : {}", iteration, bestSolutionSoFar.getFitness());
               
             
               // logger.debug("Population fitness: {}", population.stream().map(CloudletToVmMappingSolution::getFitness).collect(Collectors.toList()));
            }
        }

        setSolveTime((System.currentTimeMillis() - startTime) / 1000.0);
        return bestSolutionSoFar;
    }
}
