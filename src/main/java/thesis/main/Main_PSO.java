package thesis.main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import thesis.common.SimulationAbstractFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Main_PSO {
    private static int HOSTS_TO_CREATE = 100;
    private static int VMS_TO_CREATE = 50;
    private static int CLOUDLETS_TO_CREATE = 100;

    public static void main(String[] args) {
        if (args.length > 0)
            CLOUDLETS_TO_CREATE = Integer.parseInt(args[0]);
        int[] hosts = {100};
        int[] vms = {50};
        int[] cloudlets = {100};
        //    int[] cloudlets = {10,20,40,80,100};

        // Write headers to the CSV file if the file does not exist
        if (!new File("PSO.csv").exists())
            writeHeadersToFile("PSO.csv");

        for (int host : hosts) {
            for (int vm : vms) {
                for (int cloudlet : cloudlets) {
                    //if (cloudlet > 2 * host || vm > 2 * host || cloudlet > 3 * vm) continue;
                    for (int i = 0; i < 5; i++)
                        simulate(host, vm, cloudlet);
                }
            }
        }
    }

    private static void simulate(int HOSTS_TO_CREATE, int VMS_TO_CREATE, int CLOUDLETS_TO_CREATE) {
        System.out.printf("Starting Simulations [%d, %d, %d]\n", HOSTS_TO_CREATE, VMS_TO_CREATE, CLOUDLETS_TO_CREATE);
        final var factory = new SimulationAbstractFactory(HOSTS_TO_CREATE, VMS_TO_CREATE, CLOUDLETS_TO_CREATE);
        var PSOalgorithm = factory.getAlgorithm(SimulationAbstractFactory.PARTICLESWARMOPTIMIZATION_ALGORITHM);
        System.out.printf(
                "Execution Time: \t\t%.2f\n",
                PSOalgorithm.getExecutionTime()
        );

        System.out.printf(
                "Fitness: \t\t\t%.6f\n",
                PSOalgorithm.getFitness()
        );

        System.out.printf(
                "CPU Utilization: \t%.2f\n",
                PSOalgorithm.getCPUUtilization()
        );

        System.out.printf(
                "RAM Utilization: \t%.2f\n",
                PSOalgorithm.getMemoryUtilization()
        );

        System.out.printf(
                "B/W Utilization: \t%.2f\n",
                PSOalgorithm.getBandwidthUtilization()
        );

        System.out.printf(
                "CPU Usage Mean: \t\t%.2f\n",
                PSOalgorithm.getCPUUsageMean()
        );

        System.out.printf(
                "CPU Usage StdDev: \t%.2f\n",
                PSOalgorithm.getCPUUsageStandardDeviation()
        );

        System.out.printf(
                "Power Consumption: \t%.2f\n",
                PSOalgorithm.powerConsumptionMean()
        );



        writeToFile("PSO.csv", "%d,%d,%d,%.2f,%.6f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n".formatted(
                HOSTS_TO_CREATE,
                VMS_TO_CREATE,
                CLOUDLETS_TO_CREATE,
                PSOalgorithm.getExecutionTime(),
                PSOalgorithm.getFitness(),
                PSOalgorithm.getCPUUtilization(),
                PSOalgorithm.getMemoryUtilization(),
                PSOalgorithm.getBandwidthUtilization(),
                PSOalgorithm.powerConsumptionMean(),
                PSOalgorithm.getCPUUsageMean(),
                PSOalgorithm.getCPUUsageStandardDeviation()
        ));


        System.out.println("Finished Simulations");
    }

    private static void writeHeadersToFile(String filePath) {
        String headers = "hosts,vms,cloudlets,pso_time,pso_fitness,pso_cpu_util,pso_ram_util,pso_bw_util,pso_power,pso_cpu_mean,pso_cpu_std\n";
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

}