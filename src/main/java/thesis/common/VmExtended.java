package thesis.common;

import org.cloudsimplus.schedulers.MipsShare;
import org.cloudsimplus.schedulers.cloudlet.CloudletScheduler;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

public class VmExtended extends VmSimple {

    public VmExtended(Vm sourceVm) {
        super(sourceVm);
    }

    public VmExtended(double mipsCapacity, long pesNumber) {
        super(mipsCapacity, pesNumber);
    }

    public VmExtended(double mipsCapacity, long pesNumber, CloudletScheduler cloudletScheduler) {
        super(mipsCapacity, pesNumber, cloudletScheduler);
    }

    public VmExtended(long id, double mipsCapacity, long pesNumber) {
        super(id, mipsCapacity, pesNumber);
    }

    public VmExtended(long id, long mipsCapacity, long pesNumber) {
        super(id, mipsCapacity, pesNumber);
    }

    private double cpuUtilizationSum = 0, ramUtilizationSum = 0, bwUtilizationSum = 0;
    private double powerConsumptionSum = 0;
    private int timesUpdated = 0;

    @Override
    public double updateProcessing(double currentTime, MipsShare mipsShare) {
        double nextTime = super.updateProcessing(currentTime, mipsShare);
        cpuUtilizationSum += getTotalCpuMipsUtilization(currentTime);
        ramUtilizationSum += getRam().getPercentUtilization();
        bwUtilizationSum += getBw().getPercentUtilization();
//        powerConsumptionSum += getHost().getPowerModel().getPower(getTotalCpuMipsUtilization(currentTime));
        timesUpdated++;
        return nextTime;
    }

    public double getAverageCpuUtilization() {
        return cpuUtilizationSum / timesUpdated;
    }

    public double getAverageRamUtilization() {
        return ramUtilizationSum / timesUpdated;
    }

    public double getAverageBwUtilization() {
        return bwUtilizationSum / timesUpdated;
    }

    public double getAveragePowerConsumption() {
        return powerConsumptionSum / timesUpdated;
    }
}
