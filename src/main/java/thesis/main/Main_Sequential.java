package thesis.main;

import ch.qos.logback.classic.Level;
import thesis.common.SimulationAbstractFactory;

import org.cloudsimplus.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main_Sequential {
    private static final Logger logger = LoggerFactory.getLogger(Main.class.getSimpleName());

    private static int HOSTS_TO_CREATE = 100;
    private static int VMS_TO_CREATE = 50;
    private static int CLOUDLETS_TO_CREATE = 100;

    public static void main(String[] args) {
        if (args.length > 0)
            CLOUDLETS_TO_CREATE = Integer.parseInt(args[0]);
        //Enables just some level of log messages.
        Log.setLevel(Level.WARN);
        Log.setLevel(Level.DEBUG);

        int[] hosts = {100};
        int[] vms = {50};
        int[] cloudlets = {100};
        // int[] cloudlets = {10, 20, 35, 50, 65, 80, 100, 150, 200};

        // Write headers to the CSV file
        if (!new File(CSV_FILE_PATH).exists())
            writeHeadersToFile(CSV_FILE_PATH);

        for (int host : hosts) {
            for (int vm : vms) {
                for (int cloudlet : cloudlets) {
                    //if (cloudlet > 2 * host || vm > 2 * host || cloudlet > 5 * vm) continue;
                    for (int i = 0; i < 5; i++)
                        simulate(host, vm, cloudlet);
                }
            }
        }
    }

    private static void simulate(int HOSTS_TO_CREATE, int VMS_TO_CREATE, int CLOUDLETS_TO_CREATE) {
        System.out.printf("Starting Simulations [%d, %d, %d]\n", HOSTS_TO_CREATE, VMS_TO_CREATE, CLOUDLETS_TO_CREATE);
        final var factory = new SimulationAbstractFactory(HOSTS_TO_CREATE, VMS_TO_CREATE, CLOUDLETS_TO_CREATE);

        var geneticAlgorithm = factory.getAlgorithm(SimulationAbstractFactory.GENETIC_ALGORITHM);
        var sequentialAlgorithm = factory.getAlgorithm(SimulationAbstractFactory.SEQUENTIAL_ALGORITHM);
        var fireworksAlgorithm = factory.getAlgorithm(SimulationAbstractFactory.FIREWORKS_ALGORITHM);
        var whaleOptimizationAlgorithm = factory.getAlgorithm(SimulationAbstractFactory.WHALEOPTIMIZATION_ALGORITHM);

        System.out.printf(
                "[%4s] Execution Time: \t\t%.2f\t -> \t%.2f\t -> \t%.2f\t -> \t%.2f\n",
                getBestAlgorithm(geneticAlgorithm.getExecutionTime(), sequentialAlgorithm.getExecutionTime(), fireworksAlgorithm.getExecutionTime(), whaleOptimizationAlgorithm.getExecutionTime()),
                geneticAlgorithm.getExecutionTime(),
                sequentialAlgorithm.getExecutionTime(),
                fireworksAlgorithm.getExecutionTime(),
                whaleOptimizationAlgorithm.getExecutionTime()
        );

        System.out.printf(
                "[%4s] Fitness: \t\t\t%.6f\t -> \t%.6f\t -> \t%.6f\t -> \t%.6f\n",
                getBestAlgorithm(geneticAlgorithm.getFitness(), sequentialAlgorithm.getFitness(), fireworksAlgorithm.getFitness(), whaleOptimizationAlgorithm.getFitness(), true),
                geneticAlgorithm.getFitness(),
                sequentialAlgorithm.getFitness(),
                fireworksAlgorithm.getFitness(),
                whaleOptimizationAlgorithm.getFitness()
        );

        System.out.printf(
                "[%4s] CPU Utilization: \t%.2f\t -> \t%.2f\t -> \t%.2f\t -> \t%.2f\n",
                getBestAlgorithm(geneticAlgorithm.getCPUUtilization(), sequentialAlgorithm.getCPUUtilization(), fireworksAlgorithm.getCPUUtilization(), whaleOptimizationAlgorithm.getCPUUtilization(), true),
                geneticAlgorithm.getCPUUtilization(),
                sequentialAlgorithm.getCPUUtilization(),
                fireworksAlgorithm.getCPUUtilization(),
                whaleOptimizationAlgorithm.getCPUUtilization()
        );

        System.out.printf(
                "[%4s] RAM Utilization: \t%.2f\t -> \t%.2f\t -> \t%.2f\t -> \t%.2f\n",
                getBestAlgorithm(geneticAlgorithm.getMemoryUtilization(), sequentialAlgorithm.getMemoryUtilization(), fireworksAlgorithm.getMemoryUtilization(), whaleOptimizationAlgorithm.getMemoryUtilization(), true),
                geneticAlgorithm.getMemoryUtilization(),
                sequentialAlgorithm.getMemoryUtilization(),
                fireworksAlgorithm.getMemoryUtilization(),
                whaleOptimizationAlgorithm.getMemoryUtilization()
        );

        System.out.printf(
                "[%4s] B/W Utilization: \t%.2f\t -> \t%.2f\t -> \t%.2f\t -> \t%.2f\n",
                getBestAlgorithm(geneticAlgorithm.getBandwidthUtilization(), sequentialAlgorithm.getBandwidthUtilization(), fireworksAlgorithm.getBandwidthUtilization(), whaleOptimizationAlgorithm.getBandwidthUtilization(), true),
                geneticAlgorithm.getBandwidthUtilization(),
                sequentialAlgorithm.getBandwidthUtilization(),
                fireworksAlgorithm.getBandwidthUtilization(),
                whaleOptimizationAlgorithm.getBandwidthUtilization()
        );

        System.out.printf(
                "[%4s] CPU Usage Mean: \t\t%.2f\t -> \t%.2f\t -> \t%.2f\t -> \t%.2f\n",
                getBestAlgorithm(geneticAlgorithm.getCPUUsageMean(), sequentialAlgorithm.getCPUUsageMean(), fireworksAlgorithm.getCPUUsageMean(), whaleOptimizationAlgorithm.getCPUUsageMean(), true),
                geneticAlgorithm.getCPUUsageMean(),
                sequentialAlgorithm.getCPUUsageMean(),
                fireworksAlgorithm.getCPUUsageMean(),
                whaleOptimizationAlgorithm.getCPUUsageMean()
        );

        System.out.printf(
                "[%4s] CPU Usage StdDev: \t%.2f\t -> \t%.2f\t -> \t%.2f\t -> \t%.2f\n",
                getBestAlgorithm(geneticAlgorithm.getCPUUsageStandardDeviation(), sequentialAlgorithm.getCPUUsageStandardDeviation(), fireworksAlgorithm.getCPUUsageStandardDeviation(), whaleOptimizationAlgorithm.getCPUUsageStandardDeviation()),
                geneticAlgorithm.getCPUUsageStandardDeviation(),
                sequentialAlgorithm.getCPUUsageStandardDeviation(),
                fireworksAlgorithm.getCPUUsageStandardDeviation(),
                whaleOptimizationAlgorithm.getCPUUsageStandardDeviation()
        );

        System.out.printf(
                "[%4s] Power Consumption: \t%.2f\t -> \t%.2f\t -> \t%.2f\t -> \t%.2f\n",
                getBestAlgorithm(geneticAlgorithm.powerConsumptionMean(), sequentialAlgorithm.powerConsumptionMean(), fireworksAlgorithm.powerConsumptionMean(), whaleOptimizationAlgorithm.powerConsumptionMean()),
                geneticAlgorithm.powerConsumptionMean(),
                sequentialAlgorithm.powerConsumptionMean(),
                fireworksAlgorithm.powerConsumptionMean(),
                whaleOptimizationAlgorithm.powerConsumptionMean()
        );

        System.out.println("Finished Simulations");

        writeToFile(CSV_FILE_PATH, "%d,%d,%d,%.2f,%.2f,%.2f,%.2f,%.6f,%.6f,%.6f,%.6f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n".formatted(
                HOSTS_TO_CREATE,
                VMS_TO_CREATE,
                CLOUDLETS_TO_CREATE,
                geneticAlgorithm.getExecutionTime(),
                sequentialAlgorithm.getExecutionTime(),
                fireworksAlgorithm.getExecutionTime(),
                whaleOptimizationAlgorithm.getExecutionTime(),
                geneticAlgorithm.getFitness(),
                sequentialAlgorithm.getFitness(),
                fireworksAlgorithm.getFitness(),
                whaleOptimizationAlgorithm.getFitness(),
                geneticAlgorithm.getCPUUtilization(),
                sequentialAlgorithm.getCPUUtilization(),
                fireworksAlgorithm.getCPUUtilization(),
                whaleOptimizationAlgorithm.getCPUUtilization(),
                geneticAlgorithm.getMemoryUtilization(),
                sequentialAlgorithm.getMemoryUtilization(),
                fireworksAlgorithm.getMemoryUtilization(),
                whaleOptimizationAlgorithm.getMemoryUtilization(),
                geneticAlgorithm.getBandwidthUtilization(),
                sequentialAlgorithm.getBandwidthUtilization(),
                fireworksAlgorithm.getBandwidthUtilization(),
                whaleOptimizationAlgorithm.getBandwidthUtilization(),
                geneticAlgorithm.powerConsumptionMean(),
                sequentialAlgorithm.powerConsumptionMean(),
                fireworksAlgorithm.powerConsumptionMean(),
                whaleOptimizationAlgorithm.powerConsumptionMean(),
                geneticAlgorithm.getCPUUsageMean(),
                sequentialAlgorithm.getCPUUsageMean(),
                fireworksAlgorithm.getCPUUsageMean(),
                whaleOptimizationAlgorithm.getCPUUsageMean(),
                geneticAlgorithm.getCPUUsageStandardDeviation(),
                sequentialAlgorithm.getCPUUsageStandardDeviation(),
                fireworksAlgorithm.getCPUUsageStandardDeviation(),
                whaleOptimizationAlgorithm.getCPUUsageStandardDeviation()
        ));
    }

    public static final String CSV_FILE_PATH = "sequential.csv";

    private static void writeHeadersToFile(String filePath) {
        String headers = "hosts,vms,cloudlets,ga_time,seq_time,fwa_time,woa_time,ga_fitness,seq_fitness,fwa_fitness,woa_fitness,ga_cpu_util,seq_cpu_util,fwa_cpu_util,woa_cpu_util,ga_ram_util,seq_ram_util,fwa_ram_util,woa_ram_util,ga_bw_util,seq_bw_util,fwa_bw_util,woa_bw_util,ga_power,seq_power,fwa_power,woa_power,ga_cpu_mean,seq_cpu_mean,fwa_cpu_mean,woa_cpu_mean,ga_cpu_std,seq_cpu_std,fwa_cpu_std,woa_cpu_std\n";
        try (FileWriter fw = new FileWriter(filePath, false); // Open in write mode to overwrite
             BufferedWriter writer = new BufferedWriter(fw)) {
            writer.write(headers);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static void writeToFile(String filePath, String content) {
        try (FileWriter fw = new FileWriter(filePath, true); // Open in append mode to add data
             BufferedWriter writer = new BufferedWriter(fw)) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    // Helper function to get the best algorithm
    private static String getBestAlgorithm(double gaMetric, double seqMetric, double fwaMetric, double woaMetric) {
        if (gaMetric <= seqMetric && gaMetric <= fwaMetric && gaMetric <= woaMetric) {
            return "GA";
        } else if (seqMetric <= gaMetric && seqMetric <= fwaMetric && seqMetric <= woaMetric) {
            return "SEQUENTIAL";
        } else if (fwaMetric <= gaMetric && fwaMetric <= seqMetric && fwaMetric <= woaMetric) {
            return "FWA";
        } else {
            return "WOA";
        }
    }

    // Overloaded helper function to get the best algorithm (for metrics where higher is better)
    private static String getBestAlgorithm(double gaMetric, double seqMetric, double fwaMetric, double woaMetric, boolean higherIsBetter) {
        if (higherIsBetter) {
            if (gaMetric >= seqMetric && gaMetric >= fwaMetric && gaMetric >= woaMetric) {
                return "GA";
            } else if (seqMetric >= gaMetric && seqMetric >= fwaMetric && seqMetric >= woaMetric) {
                return "SEQUENTIAL";
            } else if (fwaMetric >= gaMetric && fwaMetric >= seqMetric && fwaMetric >= woaMetric) {
                return "FWA";
            } else {
                return "WOA";
            }
        } else {
            return getBestAlgorithm(gaMetric, seqMetric, fwaMetric, woaMetric);
        }
    }
}
