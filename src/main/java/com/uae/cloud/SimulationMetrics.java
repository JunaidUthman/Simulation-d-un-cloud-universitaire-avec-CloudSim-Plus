package com.uae.cloud;

/**
 * Métriques et résultats collectés à l'issue d'une simulation CloudSim Plus.
 * Répond aux exigences de la section 5 du cahier des charges :
 * "Analyser les résultats : temps de réponse, temps d'exécution, taux d'utilisation des hôtes, coût."
 */
public class SimulationMetrics {

    private final String scenarioName;
    private final String taskSchedulerName;
    private final String vmAllocationName;

    private double makespanSeconds;
    private double avgResponseTimeSeconds;
    private double avgWaitingTimeSeconds;
    private double avgExecutionTimeSeconds;
    private double hostCpuUtilizationPercent;
    private double hostRamUtilizationPercent;

    private int totalVmsCreated;
    private int privateVmsCount;
    private int publicBurstVmsCount;

    private int totalCloudletsSubmitted;
    private int totalCloudletsFinished;

    private double privateCloudCost;
    private double publicCloudCost;
    private double totalCost;

    public SimulationMetrics(String scenarioName, String taskSchedulerName, String vmAllocationName) {
        this.scenarioName = scenarioName;
        this.taskSchedulerName = taskSchedulerName;
        this.vmAllocationName = vmAllocationName;
    }

    // Getters et Setters
    public String getScenarioName() { return scenarioName; }
    public String getTaskSchedulerName() { return taskSchedulerName; }
    public String getVmAllocationName() { return vmAllocationName; }

    public double getMakespanSeconds() { return makespanSeconds; }
    public void setMakespanSeconds(double makespanSeconds) { this.makespanSeconds = makespanSeconds; }

    public double getAvgResponseTimeSeconds() { return avgResponseTimeSeconds; }
    public void setAvgResponseTimeSeconds(double avgResponseTimeSeconds) { this.avgResponseTimeSeconds = avgResponseTimeSeconds; }

    public double getAvgWaitingTimeSeconds() { return avgWaitingTimeSeconds; }
    public void setAvgWaitingTimeSeconds(double avgWaitingTimeSeconds) { this.avgWaitingTimeSeconds = avgWaitingTimeSeconds; }

    public double getAvgExecutionTimeSeconds() { return avgExecutionTimeSeconds; }
    public void setAvgExecutionTimeSeconds(double avgExecutionTimeSeconds) { this.avgExecutionTimeSeconds = avgExecutionTimeSeconds; }

    public double getHostCpuUtilizationPercent() { return hostCpuUtilizationPercent; }
    public void setHostCpuUtilizationPercent(double hostCpuUtilizationPercent) { this.hostCpuUtilizationPercent = hostCpuUtilizationPercent; }

    public double getHostRamUtilizationPercent() { return hostRamUtilizationPercent; }
    public void setHostRamUtilizationPercent(double hostRamUtilizationPercent) { this.hostRamUtilizationPercent = hostRamUtilizationPercent; }

    public int getTotalVmsCreated() { return totalVmsCreated; }
    public void setTotalVmsCreated(int totalVmsCreated) { this.totalVmsCreated = totalVmsCreated; }

    public int getPrivateVmsCount() { return privateVmsCount; }
    public void setPrivateVmsCount(int privateVmsCount) { this.privateVmsCount = privateVmsCount; }

    public int getPublicBurstVmsCount() { return publicBurstVmsCount; }
    public void setPublicBurstVmsCount(int publicBurstVmsCount) { this.publicBurstVmsCount = publicBurstVmsCount; }

    public int getTotalCloudletsSubmitted() { return totalCloudletsSubmitted; }
    public void setTotalCloudletsSubmitted(int totalCloudletsSubmitted) { this.totalCloudletsSubmitted = totalCloudletsSubmitted; }

    public int getTotalCloudletsFinished() { return totalCloudletsFinished; }
    public void setTotalCloudletsFinished(int totalCloudletsFinished) { this.totalCloudletsFinished = totalCloudletsFinished; }

    public double getPrivateCloudCost() { return privateCloudCost; }
    public void setPrivateCloudCost(double privateCloudCost) { this.privateCloudCost = privateCloudCost; }

    public double getPublicCloudCost() { return publicCloudCost; }
    public void setPublicCloudCost(double publicCloudCost) { this.publicCloudCost = publicCloudCost; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    @Override
    public String toString() {
        return String.format(
            "[%s] Ordo: %s | Alloc: %s | Makespan: %.2fs | Rép. Moy: %.2fs | Attente: %.2fs | CPU: %.1f%% | Coût: $%.2f",
            scenarioName, taskSchedulerName, vmAllocationName, makespanSeconds, avgResponseTimeSeconds,
            avgWaitingTimeSeconds, hostCpuUtilizationPercent, totalCost
        );
    }
}

