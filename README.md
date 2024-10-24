# cloudsimplus-simulation_ugrad_thesis

The repository is organized into the following main folders:

1. common:
Contains utility classes and core components essential for the task scheduling simulation. The key classes include-
SimulationAbstractFactory: Provides abstract classes and methods to define the properties of VMs, hosts, tasks (cloudlets), and their interactions. It is responsible for setting up the simulation environment.
VMExtended: Extends the basic VM functionalities, allowing for more detailed configurations and capabilities tailored to the task scheduling problem.
WeightedCloudletToVMSolution: Implements the optimization or cost function, which calculates the efficiency of task-to-VM assignments. This is crucial for comparing different scheduling strategies, focusing on metrics like makespan, load balancing, and total cost.

2. fwa (Fireworks Algorithm):
Contains the implementation of the Fireworks Algorithm (FWA) for task scheduling.

3. woa (Whale Optimization Algorithm):
Contains the implementation of the Whale Optimization Algorithm (WOA) for task scheduling.

4. sequential (Sequential Hybrid Algorithm):
This folder includes the sequential hybrid approach, which integrates the Fireworks and Whale Optimization Algorithms sequentially.

5. parallel (Parallel Hybrid Algorithm):
This folder includes the parallel hybrid approach, where FWA and WOA are applied simultaneously on different solutions, and the best outcomes from each are combined for better performance.

6. Combined:
   Component level integration
