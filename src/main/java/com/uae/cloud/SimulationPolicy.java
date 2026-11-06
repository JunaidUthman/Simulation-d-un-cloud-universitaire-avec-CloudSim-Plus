package com.uae.cloud;

/**
 * Politiques d'ordonnancement des tâches et d'allocation des VMs à comparer,
 * conformément au cahier des charges du projet.
 */
public class SimulationPolicy {

    public enum TaskSchedulerType {
        TIME_SHARED("CloudletSchedulerTimeShared (Temps partagé)"),
        SPACE_SHARED("CloudletSchedulerSpaceShared (Espace partagé / FIFO)");

        private final String label;

        TaskSchedulerType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum VmAllocationPolicyType {
        SIMPLE("VmAllocationPolicySimple (First-Fit)"),
        BEST_FIT("VmAllocationPolicyBestFit (Meilleur ajustement)");

        private final String label;

        VmAllocationPolicyType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final TaskSchedulerType taskSchedulerType;
    private final VmAllocationPolicyType vmAllocationPolicyType;

    public SimulationPolicy(TaskSchedulerType taskSchedulerType, VmAllocationPolicyType vmAllocationPolicyType) {
        this.taskSchedulerType = taskSchedulerType;
        this.vmAllocationPolicyType = vmAllocationPolicyType;
    }

    public static SimulationPolicy defaultPolicy() {
        return new SimulationPolicy(TaskSchedulerType.TIME_SHARED, VmAllocationPolicyType.SIMPLE);
    }

    public TaskSchedulerType getTaskSchedulerType() {
        return taskSchedulerType;
    }

    public VmAllocationPolicyType getVmAllocationPolicyType() {
        return vmAllocationPolicyType;
    }

    @Override
    public String toString() {
        return "[" + taskSchedulerType.name() + " | " + vmAllocationPolicyType.name() + "]";
    }
}

