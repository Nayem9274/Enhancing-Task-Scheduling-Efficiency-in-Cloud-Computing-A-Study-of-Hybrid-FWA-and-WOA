package thesis.pso;

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
import java.util.List;
import java.util.Random;

public class CloudletToVmMappingParticleSwarmOptimizationAlgorithm implements CloudletToVmMappingHeuristic {
    private static final int NUM_PARTICLES = 200;
    private static final int MAX_ITERATIONS = 200;
    private static final double INERTIA_WEIGHT = 155;
    private static final double COGNITIVE_COEFFICIENT = 120.5;
    private static final double SOCIAL_COEFFICIENT = 32.5;

    private final ContinuousDistribution random;
    private int searchesByIteration;
    private double solveTime;
    private CloudletToVmMappingSolution bestSolutionSoFar, initialSolution;
    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;
    private final static Logger logger = LoggerFactory.getLogger(CloudletToVmMappingParticleSwarmOptimizationAlgorithm.class.getSimpleName());

    public CloudletToVmMappingParticleSwarmOptimizationAlgorithm(ContinuousDistribution random) {
        this.random = random;
    }

    @Override
    public double getSolveTime() {
        return solveTime;
    }

    private void setSolveTime(double solveTime) {
        this.solveTime = solveTime;
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
    public CloudletToVmMappingHeuristic setCloudletList(List<Cloudlet> cloudletList) {
        this.cloudletList = cloudletList;
        return this;
    }

    @Override
    public CloudletToVmMappingHeuristic setVmList(List<Vm> vmList) {
        this.vmList = vmList;
        return this;
    }

    @Override
    public int getSearchesByIteration() {
        return searchesByIteration;
    }

    @Override
    public Heuristic<CloudletToVmMappingSolution> setSearchesByIteration(int searchesByIteration) {
        this.searchesByIteration = searchesByIteration;
        return this;
    }

    @Override
    public int getRandomValue(int maxValue) {
        double uniform = random.sample();
        return (int) (uniform * maxValue);
    }

    private Vm getRandomVm() {
        int idx = getRandomValue(vmList.size());
        return vmList.get(idx);
    }

    @Override
    public CloudletToVmMappingSolution getInitialSolution() {
        if (this.initialSolution == null || this.initialSolution.getResult().isEmpty()) {
            this.initialSolution = generateRandomSolution();
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

    private List<Particle> initializeParticles() {
        List<Particle> particles = new ArrayList<>();
        for (int i = 0; i < NUM_PARTICLES; i++) {
            particles.add(new Particle(generateRandomSolution()));
        }
        return particles;
    }

    public void updateGlobalBestSolution(List<Particle> particles) {
        for (Particle particle : particles) {
            if (bestSolutionSoFar == null || particle.bestFitness > bestSolutionSoFar.getFitness()) {
                bestSolutionSoFar = new WeightedCloudletToVmSolution(particle.bestSolution);
            }
        }
    }

    @Override
    public CloudletToVmMappingSolution solve() {
        long startTime = System.currentTimeMillis();
        List<Particle> particles = initializeParticles();
        updateGlobalBestSolution(particles);

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            for (Particle particle : particles) {
                particle.updateVelocity(bestSolutionSoFar, random);
                particle.updatePosition(vmList);
                particle.evaluateFitness();
            }
            updateGlobalBestSolution(particles);

            if (iteration % 10 == 0) {
                logger.debug("[Iteration {}] Best fitness: {}", iteration, bestSolutionSoFar.getFitness());
            }
        }

        setSolveTime((System.currentTimeMillis() - startTime) / 1000.0);
        return bestSolutionSoFar;
    }

    private class Particle {
        CloudletToVmMappingSolution solution;
        CloudletToVmMappingSolution bestSolution;
        double[] velocity;
        double bestFitness;
        int stagnantCount; // Counter to track how long the particle has been stagnant

        Particle(CloudletToVmMappingSolution solution) {
            this.solution = solution;
            this.bestSolution = new WeightedCloudletToVmSolution(solution);
            this.bestFitness = solution.getFitness();
            this.velocity = new double[cloudletList.size()];
            this.stagnantCount = 0; // Initialize stagnant counter

            for (int i = 0; i < velocity.length; i++) {
                velocity[i] = random.sample() * 2 - 1; // Random initial velocity between -1 and 1
            }
        }

        void evaluateFitness() {
            double fitness = solution.getFitness();
            if (fitness > bestFitness) {
                bestFitness = fitness;
                bestSolution = new WeightedCloudletToVmSolution(solution);
                stagnantCount = 0; // Reset stagnant counter when an improvement is found
            } else {
                stagnantCount++;
            }
        }

        void updateVelocity(CloudletToVmMappingSolution globalBest, ContinuousDistribution random) {
            for (int i = 0; i < cloudletList.size(); i++) {
                double r1 = random.sample();
                double r2 = random.sample();

                Vm currentVm = solution.getResult().get(cloudletList.get(i));
                Vm personalBestVm = bestSolution.getResult().get(cloudletList.get(i));
                Vm globalBestVm = globalBest.getResult().get(cloudletList.get(i));

                double cognitiveComponent = COGNITIVE_COEFFICIENT * r1 * (personalBestVm.getId() - currentVm.getId());
                double socialComponent = SOCIAL_COEFFICIENT * r2 * (globalBestVm.getId() - currentVm.getId());

                velocity[i] = INERTIA_WEIGHT * velocity[i] + cognitiveComponent + socialComponent;
                velocity[i] = Math.max(-5.0, Math.min(velocity[i], 5.0)); // Clamp velocity
            }
        }

        void updatePosition(List<Vm> vmList) {
            for (int i = 0; i < cloudletList.size(); i++) {
                double probability = Math.abs(velocity[i]) / 5.0;
                if (random.sample() < probability) {
                    int newVmIndex = getRandomValue(vmList.size());
                    solution.bindCloudletToVm(cloudletList.get(i), vmList.get(newVmIndex));
                }
            }

            // Reinitialize the particle if it has been stagnant for too long
            if (stagnantCount > 20) { // Example threshold for stagnation
                solution = generateRandomSolution();
                stagnantCount = 0; // Reset stagnant counter
            }
        }
    }

}
