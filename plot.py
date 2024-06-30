import pandas as pd
import matplotlib.pyplot as plt

# Path to the CSV file
csv_file_path = 'C:\\Users\\User\\eclipse-workspace\\cloudsim-simulation_csa_ga\\sequential.csv'

# Read the CSV file into a DataFrame
df = pd.read_csv(csv_file_path)

# Check the unique values for cloudlets
print(df['cloudlets'].unique())

# Group by 'cloudlets' and calculate the mean fitness for each group
grouped_df = df.groupby('cloudlets').mean().reset_index()

# Extract the relevant columns for plotting
cloudlets = grouped_df['cloudlets']
ga_fitness = grouped_df['ga_fitness']
seq_fitness = grouped_df['seq_fitness']
fwa_fitness = grouped_df['fwa_fitness']
woa_fitness = grouped_df['woa_fitness']

# Plotting the fitness values
plt.figure(figsize=(12, 6))

# Plot each algorithm's fitness
plt.plot(cloudlets, ga_fitness, label='Genetic Algorithm (GA)', marker='o')
plt.plot(cloudlets, seq_fitness, label='Sequential Algorithm', marker='s')
plt.plot(cloudlets, fwa_fitness, label='Fireworks Algorithm (FWA)', marker='^')
plt.plot(cloudlets, woa_fitness, label='Whale Optimization Algorithm (WOA)', marker='d')

# Add title and labels
plt.title('Fitness Comparison of Algorithms')
plt.xlabel('Cloudlets')
plt.ylabel('Fitness')
plt.legend()
plt.grid(True)

# Show the plot
plt.show()
