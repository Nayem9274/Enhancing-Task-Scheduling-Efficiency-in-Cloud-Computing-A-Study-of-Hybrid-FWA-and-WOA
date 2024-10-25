# Enhancing Task Scheduling Efficiency in Cloud Computing: A Study of Hybrid Fireworks (FWA) and Whale Optimization (WOA) Techniques

This project focuses on exploring multiple hybridization models, including se-quential, parallel, and component-level combinations of FWA and WOA. By employing this
approach, we aim to get the best out of both algorithms and improve task scheduling in dynamic cloud environments. 

#

The repository is organized into the following main folders **(inside src/main/java/thesis)**:

# 1. common:
Contains utility classes and core components essential for the task scheduling simulation. The key classes include-

**SimulationAbstractFactory**: Provides abstract classes and methods to define the properties of VMs, hosts, tasks (cloudlets), and their interactions. It is responsible for setting up the simulation environment.

**VMExtended**: Extends the basic VM functionalities, allowing for more detailed configurations and capabilities tailored to the task scheduling problem.

**WeightedCloudletToVMSolution**: Implements the optimization or cost function, which calculates the efficiency of task-to-VM assignments. This is crucial for comparing different scheduling strategies, focusing on metrics like makespan, load balancing, and total cost.

# 2. fwa (Fireworks Algorithm):
Contains the implementation of the original Fireworks Algorithm (FWA) for task scheduling.

# 3. woa (Whale Optimization Algorithm):
Contains the implementation of the original Whale Optimization Algorithm (WOA) for task scheduling.

# 4. sequential (Sequential Hybrid Algorithm):
This folder includes the sequential hybrid approach, which integrates the Fireworks and Whale Optimization Algorithms sequentially.

# 5. parallel (Parallel Hybrid Algorithm):
This folder includes the parallel hybrid approach, where FWA and WOA are applied simultaneously on different solutions, and the best outcomes from each are combined for better performance.

# 6. Combined:
Component level integration like using encircling mechanism of woa inside fwa etc.

# 7. Main:  
Contains all the main functions to run the individual algorithms for a different number of VM's, cloudlets(tasks) and hosts. 

#

For plotting different metrics from the output csv file generated from running each algorithm thriugh the main function, we used the jupiter notebook **graph.ipynb**
