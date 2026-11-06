package com.uae.cloud;

import java.util.ArrayList;
import java.util.List;

/**
 * Point d'entrée principal de la simulation CloudSim Plus pour l'UAE.
 * Exécute et compare les différentes politiques et périodes demandées par le projet :
 * 1. Période Normale (Référence)
 * 2. Comparaison Ordonnancement des tâches : CloudletSchedulerTimeShared vs CloudletSchedulerSpaceShared
 * 3. Comparaison Allocation des VMs : VmAllocationPolicySimple vs VmAllocationPolicyBestFit
 * 4. Période de Pointe : Infrastructure Statique (Sans élasticité) vs Élastique (HorizontalVmScaling + Cloud Bursting)
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("   SIMULATION CLOUD COMPUTING — UNIVERSITÉ ABDELMALEK ESSAÂDI (UAE)");
        System.out.println("   Moteur : CloudSim Plus | Architecture Hybride : Tanger + Tétouan + Public");
        System.out.println("==========================================================================================\n");

        List<SimulationMetrics> results = new ArrayList<>();

        // ---------------------------------------------------------------------
        // CAS 1 : PÉRIODE NORMALE (Semestre classique - Référence)
        // ---------------------------------------------------------------------
        System.out.println(">>> [1/5] Exécution : Période Normale (TimeShared, Allocation Simple)...");
        CloudSimulation simNormal = new CloudSimulation(
            SimulationScenario.NORMAL_PERIOD,
            new SimulationPolicy(SimulationPolicy.TaskSchedulerType.TIME_SHARED, SimulationPolicy.VmAllocationPolicyType.SIMPLE)
        );
        results.add(simNormal.run());

        // ---------------------------------------------------------------------
        // CAS 2 : PÉRIODE DE POINTE - CloudletSchedulerTimeShared (Statique)
        // ---------------------------------------------------------------------
        System.out.println(">>> [2/5] Exécution : Période de Pointe Statique (TimeShared, Simple)...");
        CloudSimulation simPeakTimeShared = new CloudSimulation(
            SimulationScenario.PEAK_PERIOD_STATIC,
            new SimulationPolicy(SimulationPolicy.TaskSchedulerType.TIME_SHARED, SimulationPolicy.VmAllocationPolicyType.SIMPLE)
        );
        results.add(simPeakTimeShared.run());

        // ---------------------------------------------------------------------
        // CAS 3 : PÉRIODE DE POINTE - CloudletSchedulerSpaceShared (Statique)
        // ---------------------------------------------------------------------
        System.out.println(">>> [3/5] Exécution : Période de Pointe Statique (SpaceShared, Simple)...");
        CloudSimulation simPeakSpaceShared = new CloudSimulation(
            SimulationScenario.PEAK_PERIOD_STATIC,
            new SimulationPolicy(SimulationPolicy.TaskSchedulerType.SPACE_SHARED, SimulationPolicy.VmAllocationPolicyType.SIMPLE)
        );
        results.add(simPeakSpaceShared.run());

        // ---------------------------------------------------------------------
        // CAS 4 : COMPARAISON ALLOCATION VM - VmAllocationPolicyBestFit
        // ---------------------------------------------------------------------
        System.out.println(">>> [4/5] Exécution : Période Normale avec VmAllocationPolicyBestFit...");
        CloudSimulation simBestFit = new CloudSimulation(
            SimulationScenario.NORMAL_PERIOD,
            new SimulationPolicy(SimulationPolicy.TaskSchedulerType.TIME_SHARED, SimulationPolicy.VmAllocationPolicyType.BEST_FIT)
        );
        results.add(simBestFit.run());

        // ---------------------------------------------------------------------
        // CAS 5 : PÉRIODE DE POINTE AVEC AUTO-SCALING (HorizontalVmScaling + Cloud Bursting)
        // ---------------------------------------------------------------------
        System.out.println(">>> [5/5] Exécution : Période de Pointe ÉLASTIQUE (HorizontalVmScaling + Bursting Public)...");
        CloudSimulation simAutoscale = new CloudSimulation(
            SimulationScenario.PEAK_PERIOD_AUTOSCALE,
            new SimulationPolicy(SimulationPolicy.TaskSchedulerType.TIME_SHARED, SimulationPolicy.VmAllocationPolicyType.BEST_FIT)
        );
        results.add(simAutoscale.run());

        System.out.println("\n[OK] Toutes les simulations se sont achevées avec succès.\n");

        // Affichage des tableaux de résultats comparatifs
        printResultsTable(results);

        System.out.println("Pour une analyse complète et détaillée des métriques (temps de réponse, temps d'exécution,");
        System.out.println("taux d'utilisation CPU/RAM des hôtes, coûts et conclusion finale), consultez le fichier :");
        System.out.println("--> analyse_des_resultats.md\n");
    }

    /**
     * Affiche un tableau récapitulatif clair de toutes les métriques collectées.
     */
    private static void printResultsTable(List<SimulationMetrics> list) {
        System.out.println("=========================================================================================================================================================");
        System.out.println("                                                   TABLEAU COMPARATIF DES RÉSULTATS DE SIMULATION");
        System.out.println("=========================================================================================================================================================");
        System.out.printf("| %-38s | %-12s | %-12s | %-10s | %-10s | %-10s | %-8s | %-12s | %-8s | %-10s |%n",
            "Scénario / Configuration", "Ordonnanceur", "Alloc. VM", "Makespan", "Rép. Moy.", "Attente", "CPU Host", "VMs (Priv/Pub)", "Cloudlets", "Coût Total"
        );
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------");

        for (SimulationMetrics m : list) {
            String shortOrdo = m.getTaskSchedulerName().contains("TimeShared") ? "TimeShared" : "SpaceShared";
            String shortAlloc = m.getVmAllocationName().contains("BestFit") ? "BestFit" : "Simple";
            String vmSummary = String.format("%d (%d/%d)", m.getTotalVmsCreated(), m.getPrivateVmsCount(), m.getPublicBurstVmsCount());
            String cloudletSummary = String.format("%d/%d", m.getTotalCloudletsFinished(), m.getTotalCloudletsSubmitted());

            System.out.printf("| %-38s | %-12s | %-12s | %8.2f s | %8.2f s | %8.2f s | %6.1f %% | %-14s | %-8s | $%9.2f |%n",
                truncate(m.getScenarioName(), 38),
                shortOrdo,
                shortAlloc,
                m.getMakespanSeconds(),
                m.getAvgResponseTimeSeconds(),
                m.getAvgWaitingTimeSeconds(),
                m.getHostCpuUtilizationPercent(),
                vmSummary,
                cloudletSummary,
                m.getTotalCost()
            );
        }
        System.out.println("=========================================================================================================================================================\n");
    }

    private static String truncate(String str, int maxLen) {
        if (str == null) return "";
        return (str.length() > maxLen) ? str.substring(0, maxLen - 3) + "..." : str;
    }
}
