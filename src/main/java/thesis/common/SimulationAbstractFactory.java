package thesis.common;

import edu.buet.thesis.le.csa.CloudletToVmMappingCrowSearchAlgorithm;
import edu.buet.thesis.le.ga.CloudletToVmMappingGeneticAlgorithm;
import org.cloudsimplus.datacenters.DatacenterCharacteristicsSimple;
import thesis.fwa.CloudletToVmMappingFireworksAlgorithm;
import thesis.aco.CloudletToVmMappingAntColonyOptimizationAlgorithm;
import thesis.pso.CloudletToVmMappingParticleSwarmOptimizationAlgorithm;
import thesis.sequential.CloudletToVmMappingHybridSequentialAlgorithm;
import thesis.woa.CloudletToVmMappingWhaleOptimizationAlgorithm;
import thesis.parallel.CloudletToVmMappingHybridParallelAlgorithm;
import thesis.wgoa.CloudletToVmMappingWGOA;
import thesis.Combined.*;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyFirstFit;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyRoundRobin;
import org.cloudsimplus.brokers.DatacenterBrokerHeuristic;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.distributions.UniformDistr;
import org.cloudsimplus.heuristics.CloudletToVmMappingHeuristic;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.power.models.PowerModelHostSimple;
import org.cloudsimplus.provisioners.ResourceProvisionerSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmResourceStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulationAbstractFactory {

    /**
     * Defines the power a Host uses, even if it's idle (in Watts).
     */
    private static final double STATIC_POWER = 35;

    /**
     * The max power a Host uses (in Watts).
     */
    private static final int MAX_POWER = 50;

    /**
     * Indicates Host power consumption (in Watts) during startup.
     */
    private static final double HOST_START_UP_POWER = 5;

    /**
     * Indicates Host power consumption (in Watts) during shutdown.
     */
    private static final double HOST_SHUT_DOWN_POWER = 3;

    private static int createdVms = 0, createdCloudlets = 0;
    private final long seed = new Random().nextLong();

    private final int HOSTS_TO_CREATE, VMS_TO_CREATE, CLOUDLETS_TO_CREATE;

    private List<Host> hosts;
    private List<VmExtended> vms;
    private List<Cloudlet> cloudlets;

    public SimulationAbstractFactory(int HOSTS_TO_CREATE, int VMS_TO_CREATE, int CLOUDLETS_TO_CREATE) {
        this.HOSTS_TO_CREATE = HOSTS_TO_CREATE;
        this.VMS_TO_CREATE = VMS_TO_CREATE;
        this.CLOUDLETS_TO_CREATE = CLOUDLETS_TO_CREATE;
    }

    private void initialize() {
        Random random = new Random(12345);

        this.hosts = new ArrayList<Host>();
        for (int i = 0; i < HOSTS_TO_CREATE; i++)
            this.hosts.add(createHost(random));

        this.vms = new ArrayList<>();
        for (int i = 0; i < VMS_TO_CREATE; i++) {
            this.vms.add(createVm(random));
        }

        this.cloudlets = new ArrayList<>();
        for (int i = 0; i < CLOUDLETS_TO_CREATE; i++)
            this.cloudlets.add(createCloudlet(random));
    }

    static private Host createHost(Random random) {
        // capacity of each CPU core (in Million Instructions per Second)
        final long mips = random.nextInt(5000, 15001);
        // host memory (Megabyte)
        final int ram = random.nextInt(8*1024, 12*1024);
        // host storage
        final long storage = random.nextInt(50000, 200001);
        // host bandwidth (Megabit/s)
        final long bw = random.nextInt(4000, 16001);
//        final long mips = 1500; // vm mips
//        final long storage = 200000; // vm storage
//        final int ram = 10*1024; // vm memory
//        final long bw = 9600; // vm bandwidth

        final var peList = new ArrayList<Pe>();
        /*
         * Creates the Host's CPU cores and defines the provisioner
         * used to allocate each core for requesting VMs.
         */
        for (int i = 0; i < 10; i++)
            peList.add(new PeSimple(mips));

        final var powerModel = new PowerModelHostSimple(MAX_POWER, STATIC_POWER);
        powerModel
                .setStartupPower(HOST_START_UP_POWER)
                .setShutDownPower(HOST_SHUT_DOWN_POWER);

        // host.setId(id)
        // .setVmScheduler(vmScheduler)
        // .setPowerModel(powerModel);
        // host.enableUtilizationStats();

        var host = new HostSimple(ram, bw, storage, peList)
                .setRamProvisioner(new ResourceProvisionerSimple())
                .setBwProvisioner(new ResourceProvisionerSimple())
                .setVmScheduler(new VmSchedulerTimeShared());
        host.setPowerModel(powerModel);
        host.enableUtilizationStats();
        return host;
    }

    static private VmExtended createVm(Random random) {
        final long mips = random.nextInt(500, 1501); // vm mips
        final long storage = random.nextInt(5000, 12000); // vm storage
        final int ram = random.nextInt(256, 1024); // vm memory
        final long bw = random.nextInt(400, 1600); // vm bandwidth

        final int pesNumber = random.nextInt(5, 7);

//        final long mips = 1500; // vm mips
//        final long storage = 12000; // vm storage
//        final int ram = 768; // vm memory
//        final long bw = 1600; // vm bandwidth
//
//        final int pesNumber = 3;

        var vm = (VmExtended) new VmExtended(createdVms++, mips, pesNumber)
                .setRam(ram).setBw(bw).setSize(storage)
                .setCloudletScheduler(new CloudletSchedulerTimeShared());
        vm.enableUtilizationStats();
        return vm;
    }

    static private Cloudlet createCloudlet(Random random) {

        final long length = random.nextInt(1000, 5000); //Length of execution (in MI) (1000,5000)-->before, now (10,1700)
        final long fileSize = random.nextInt(50, 200); //Size (in bytes) before execution
        final long outputSize = random.nextInt(50, 200); //Size (in bytes) after execution
//        final long length = 500; //Length of execution (in MI)
//        final long fileSize = 500; //Size (in bytes) before execution
//        final long outputSize = 400; //Size (in bytes) after execution
//        final int pesNumber = 2;

        final int pesNumber = random.nextInt(1, 3);

        final var utilizationFull = new UtilizationModelFull();
        final var utilizationDynamic = new UtilizationModelDynamic(0.01);

        return new CloudletSimple(createdCloudlets++, length, pesNumber)
                .setFileSize(fileSize)
                .setOutputSize(outputSize)
                .setUtilizationModelCpu(utilizationFull)
                .setUtilizationModelRam(utilizationDynamic)
                .setUtilizationModelBw(utilizationDynamic);
    }


    public static final String GENETIC_ALGORITHM = "GA";
    public static final String CROW_SEARCH_ALGORITHM = "CSA";
    public static final String FIREWORKS_ALGORITHM="FWA";
    public static final String WHALEOPTIMIZATION_ALGORITHM="WOA";
    public static final String SEQUENTIAL_ALGORITHM="SEQUENTIAL";
    public static final String PARALLEL_ALGORITHM="PARALLEL";
    public static final String FWA_ENCIRCLING_ALGORITHM="FwaEncircle";
    public static final String WOA_SPARK_ALGORITHM="WoaSpark";
    public static final String ANTCOLONYOPTIMIZATION_ALGORITHM="ACO";
    public static final String PARTICLESWARMOPTIMIZATION_ALGORITHM="PSO";
    public static final String WGOA_ALGORITHM="WGOA";

    public Simulation getAlgorithm(String type) {
        initialize();

        switch (type) {
            case GENETIC_ALGORITHM -> {
                return new Simulation(
                        new CloudletToVmMappingGeneticAlgorithm(new UniformDistr(0, 1)),
                        hosts, vms, cloudlets
                );
            }
            case CROW_SEARCH_ALGORITHM -> {
                return new Simulation(
                        new CloudletToVmMappingCrowSearchAlgorithm(new UniformDistr(0, 1)),
                        hosts, vms, cloudlets
                );
            }
            case FIREWORKS_ALGORITHM -> {
                return new Simulation(
                        new CloudletToVmMappingFireworksAlgorithm(new UniformDistr(0, 1)),
                        hosts, vms, cloudlets
                );

            }
            case WHALEOPTIMIZATION_ALGORITHM -> {
                return new Simulation(
                        new CloudletToVmMappingWhaleOptimizationAlgorithm(new UniformDistr(0, 1)),
                        hosts, vms, cloudlets
                );

            }
            case SEQUENTIAL_ALGORITHM -> {
                return new Simulation(
                        new CloudletToVmMappingHybridSequentialAlgorithm(new UniformDistr(0, 1)),
                        hosts, vms, cloudlets
                );

            }
            case PARALLEL_ALGORITHM -> {
                return new Simulation(
                        new CloudletToVmMappingHybridParallelAlgorithm(new UniformDistr(0, 1)),
                        hosts, vms, cloudlets
                );

            }
            case FWA_ENCIRCLING_ALGORITHM -> {
                return new Simulation(
                        new CloudletToVmMappingHybridFwaEncircleAlgorithm(new UniformDistr(0, 1)),
                        hosts, vms, cloudlets
                );

            }
            case WOA_SPARK_ALGORITHM -> {
                return new Simulation(
                        new CloudletToVmMappingHybridWoaSparkAlgorithm(new UniformDistr(0, 1)),
                        hosts, vms, cloudlets
                );

            }
            case ANTCOLONYOPTIMIZATION_ALGORITHM -> {
                return new Simulation(
                        new CloudletToVmMappingAntColonyOptimizationAlgorithm(new UniformDistr(0, 1)),
                        hosts, vms, cloudlets
                );

            }
            case PARTICLESWARMOPTIMIZATION_ALGORITHM -> {
                return new Simulation(
                        new CloudletToVmMappingParticleSwarmOptimizationAlgorithm(new UniformDistr(0, 1)),
                        hosts, vms, cloudlets
                );

            }
            case WGOA_ALGORITHM -> {
                return new Simulation(
                        new CloudletToVmMappingWGOA(new UniformDistr(0, 1)),
                        hosts, vms, cloudlets
                );

            }
            default -> {
                return null;
            }
        }
    }


    public static class Simulation {
        private final CloudletToVmMappingHeuristic heuristic;
        private final DatacenterBrokerHeuristic broker;
        private final CloudSimPlus simulation;

        private final List<VmExtended> vms;

        public Simulation(CloudletToVmMappingHeuristic heuristic,
                          List<Host> hosts, List<VmExtended> vms,
                          List<Cloudlet> cloudlets) {
            this.vms = vms;
            this.heuristic = heuristic;
            this.simulation = new CloudSimPlus();
            final var datacenter = new DatacenterSimple(simulation, hosts);
            //datacenter.setSchedulingInterval(1);
            datacenter.setVmAllocationPolicy(new VmAllocationPolicyRoundRobin());
            datacenter.setCharacteristics(new DatacenterCharacteristicsSimple(
                    0.00001, // costPerSecond
                    0.00001, // costPerMem
                    0.00001, // costPerStorage
                    0.00001 // costPerBw
            ));
            //datacenter.setSchedulingInterval(0.1); // Decrease interval for more frequent scheduling
            this.broker = new DatacenterBrokerHeuristic(simulation);
            this.broker.setHeuristic(heuristic);
            this.broker.submitVmList(vms);
            this.broker.submitCloudletList(cloudlets);

            // Increased VM destruction delay
            // this.broker.setVmDestructionDelay(100);




            // Start the simulation
            try {
                this.simulation.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Prints the list (output shown in console )
            System.out.println("Simulation Time: " + simulation.clock());
            System.out.println("Cloudlets Finished: " + broker.getCloudletFinishedList().size());
            System.out.println("Cloudlets in Progress: " + broker.getCloudletSubmittedList().size());

            final var cloudletFinishedList = broker.getCloudletFinishedList();
            new CloudletsTableBuilder(cloudletFinishedList).build();
//            printVmsCpuUtilizationAndPowerConsumption(new ArrayList<>(vms));
        }

        private double vmToPowerWatts(VmExtended vm) {
            final var powerModel = vm.getHost().getPowerModel();
            final double hostStaticPower = powerModel instanceof PowerModelHostSimple powerModelHost ? powerModelHost.getStaticPower() : 0;
            final double hostStaticPowerByVm = hostStaticPower / vm.getHost().getVmCreatedList().size();

            //VM CPU utilization relative to the host capacity
            final double vmRelativeCpuUtilization = vm.getCpuUtilizationStats().getMean() / vm.getHost().getVmCreatedList().size();
            assert vmRelativeCpuUtilization >= 0 && vmRelativeCpuUtilization <= 1;
            final double vmPower = powerModel.getPower(vmRelativeCpuUtilization) - hostStaticPower + hostStaticPowerByVm; // W
            return vmPower;
        }

        public List<Cloudlet> getFinishedList() {
            return this.broker.getCloudletFinishedList();
        }

        public double getExecutionTime() {
            var list = this.broker.getCloudletFinishedList();
            return list.get(list.size() - 1).getTotalExecutionTime();
        }

        public double getFitness() {
            return heuristic.getBestSolutionSoFar().getFitness();
        }

        public double getMemoryUtilization() {
            return vms.stream().mapToDouble(VmExtended::getAverageRamUtilization).sum();
        }

        public double getCPUUtilization() {
            return vms.stream().mapToDouble(VmExtended::getAverageCpuUtilization).sum();
        }
        
        public double getThroughput() {
            return this.getCPUUtilization() / this.getExecutionTime();
        }


        public double getBandwidthUtilization() {
            return vms.stream().mapToDouble(VmExtended::getAverageBwUtilization).sum();
        }

        public double getCPUUsageMean() {
            return vms.stream().mapToDouble(vm -> vm.getCpuUtilizationStats().getMean() * 100).average().orElse(0);
        }

        public double getCPUUsageStandardDeviation() {
            return vms.stream().mapToDouble(vm -> vm.getCpuUtilizationStats().getStandardDeviation() * 100).average().orElse(0);
        }

        public double powerConsumptionMean() {
            return vms.stream().mapToDouble(this::vmToPowerWatts).average().orElse(0);
        }
    }
}