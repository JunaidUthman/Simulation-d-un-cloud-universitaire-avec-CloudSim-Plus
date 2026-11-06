package com.uae.cloud;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicy;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicySimple;
import org.cloudsimplus.autoscaling.HorizontalVmScaling;
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletScheduler;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.util.Log;
import org.cloudsimplus.utilizationmodels.UtilizationModel;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;
import org.cloudsimplus.utilizationmodels.UtilizationModelStochastic;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmCost;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Moteur de simulation d'architecture Cloud pour l'Université Abdelmalek Essaâdi.
 * Implémente la création des datacenters privés et publics, le déploiement des VMs,
 * la modélisation des Cloudlets selon les périodes, l'auto-scaling horizontal et le Cloud Bursting.
 */
public class CloudSimulation {

    private final SimulationScenario scenario;
    private final SimulationPolicy policy;

    private CloudSimPlus simulation;
    private Datacenter dcTanger;
    private Datacenter dcTetouan;
    private Datacenter dcPublic;
    private DatacenterBroker broker;

    private final List<Vm> vmList = new ArrayList<>();
    private final List<Cloudlet> cloudletList = new ArrayList<>();

    // Pools de VMs par service pour le routage des requêtes
    private final List<Vm> dbVms = new ArrayList<>();
    private final List<Vm> moodleVms = new ArrayList<>();
    private final List<Vm> entVms = new ArrayList<>();
    private final List<Vm> examVms = new ArrayList<>();
    private final List<Vm> dsiVms = new ArrayList<>();
    private final List<Vm> mailVms = new ArrayList<>();

    // Compteurs pour l'élasticité et le débordement
    private final AtomicInteger scaledVmsCount = new AtomicInteger(0);
    private final AtomicInteger publicBurstVmsCount = new AtomicInteger(0);

    public CloudSimulation(SimulationScenario scenario, SimulationPolicy policy) {
        this.scenario = scenario;
        this.policy = policy;
    }

    /**
     * Exécute la simulation complète et retourne l'ensemble des métriques collectées.
     */
    public SimulationMetrics run() {
        Log.setLevel(ch.qos.logback.classic.Level.ERROR);

        this.simulation = new CloudSimPlus();

        // 1. Création des Datacenters (Tanger, Tétouan et Cloud Public)
        buildDatacenters();

        // 2. Création du DatacenterBroker avec mapping intelligent (Cloud Hybride)
        buildBroker();

        // 3. Déploiement des VMs de base (42 VMs selon l'architecture)
        buildBaselineVms();

        // 4. Configuration de l'auto-scaling si scénario élastique
        if (scenario == SimulationScenario.PEAK_PERIOD_AUTOSCALE) {
            configureHorizontalAutoscaling();
        }

        // 5. Génération et soumission des requêtes (Cloudlets)
        buildCloudlets();

        // 6. Démarrage de la simulation CloudSim Plus
        simulation.start();

        // 7. Collecte et calcul des métriques
        return collectMetrics();
    }

    // =========================================================================
    // ÉTAPE 1 : CRÉATION DE L'INFRASTRUCTURE PHYSIQUE (DATACENTERS)
    // =========================================================================
    private void buildDatacenters() {
        // Datacenter 1 : Tanger (5 hôtes de 32 cœurs)
        List<Host> hostsTanger = createHosts(CloudArchitectureConfig.DC1_HOSTS_COUNT);
        dcTanger = new DatacenterSimple(simulation, hostsTanger, createVmAllocationPolicy());
        configureDcCharacteristics(dcTanger, false);
        dcTanger.setSchedulingInterval(1.0);

        // Datacenter 2 : Tétouan / Martil (5 hôtes de 32 cœurs)
        List<Host> hostsTetouan = createHosts(CloudArchitectureConfig.DC2_HOSTS_COUNT);
        dcTetouan = new DatacenterSimple(simulation, hostsTetouan, createVmAllocationPolicy());
        configureDcCharacteristics(dcTetouan, false);
        dcTetouan.setSchedulingInterval(1.0);

        // Datacenter Public : Cloud Bursting (6 hôtes pour accueillir les 26 VMs débordées)
        List<Host> hostsPublic = createHosts(CloudArchitectureConfig.PUBLIC_DC_HOSTS_COUNT);
        dcPublic = new DatacenterSimple(simulation, hostsPublic, createVmAllocationPolicy());
        configureDcCharacteristics(dcPublic, true);
        dcPublic.setSchedulingInterval(1.0);
    }

    private List<Host> createHosts(int count) {
        List<Host> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            List<Pe> peList = new ArrayList<>(CloudArchitectureConfig.HOST_PES);
            for (int p = 0; p < CloudArchitectureConfig.HOST_PES; p++) {
                peList.add(new PeSimple(CloudArchitectureConfig.HOST_MIPS));
            }
            Host host = new HostSimple(
                CloudArchitectureConfig.HOST_RAM_MB,
                CloudArchitectureConfig.HOST_BW_MBPS,
                CloudArchitectureConfig.HOST_STORAGE_MB,
                peList
            );
            host.enableUtilizationStats();
            list.add(host);
        }
        return list;
    }

    private VmAllocationPolicy createVmAllocationPolicy() {
        if (policy.getVmAllocationPolicyType() == SimulationPolicy.VmAllocationPolicyType.BEST_FIT) {
            return new VmAllocationPolicyBestFit();
        }
        return new VmAllocationPolicySimple();
    }

    private void configureDcCharacteristics(Datacenter dc, boolean isPublic) {
        if (isPublic) {
            dc.getCharacteristics()
                .setCostPerSecond(CloudArchitectureConfig.COST_PUBLIC_PER_SEC)
                .setCostPerMem(CloudArchitectureConfig.COST_PUBLIC_PER_MEM)
                .setCostPerStorage(CloudArchitectureConfig.COST_PUBLIC_PER_STORAGE)
                .setCostPerBw(CloudArchitectureConfig.COST_PUBLIC_PER_BW);
        } else {
            dc.getCharacteristics()
                .setCostPerSecond(CloudArchitectureConfig.COST_PRIVATE_PER_SEC)
                .setCostPerMem(CloudArchitectureConfig.COST_PRIVATE_PER_MEM)
                .setCostPerStorage(CloudArchitectureConfig.COST_PRIVATE_PER_STORAGE)
                .setCostPerBw(CloudArchitectureConfig.COST_PRIVATE_PER_BW);
        }
    }

    // =========================================================================
    // ÉTAPE 2 : ROUTAGE HYBRIDE (DATACENTER BROKER MAPPER)
    // =========================================================================
    private void buildBroker() {
        broker = new DatacenterBrokerSimple(simulation);

        // Règle de routage du Cloud Hybride (DatacenterMapper) :
        // - Bases de données : STRICTEMENT dans le Cloud Privé (souveraineté des données)
        // - VMs initiales : réparties entre Tanger et Tétouan
        // - Débordement (Bursting) : dirigé vers le Cloud Public dès saturation du seuil 85%
        broker.setDatacenterMapper((defaultDc, vm) -> {
            String desc = vm.getDescription();
            if (desc != null && desc.contains("PUBLIC")) {
                return dcPublic;
            } else if (desc != null && desc.contains("TETOUAN")) {
                return dcTetouan;
            } else {
                return dcTanger;
            }
        });

        // Routage dynamique des requêtes vers les pools de VMs de service (VmMapper)
        // Utilise la priorité du Cloudlet pour identifier le service :
        // 1=DB, 2=MOODLE, 3=ENT, 4=EXAM, 5=DSI, 6=MAIL
        // Permet d'alimenter immédiatement les VMs créées dynamiquement par l'auto-scaling
        broker.setVmMapper(cloudlet -> {
            int prio = cloudlet.getPriority();
            long id = Math.abs(cloudlet.getId());
            switch (prio) {
                case 3: // ENT
                    return entVms.isEmpty() ? Vm.NULL : entVms.get((int) (id % entVms.size()));
                case 4: // EXAM
                    return examVms.isEmpty() ? Vm.NULL : examVms.get((int) (id % examVms.size()));
                case 2: // MOODLE
                    return moodleVms.isEmpty() ? Vm.NULL : moodleVms.get((int) (id % moodleVms.size()));
                case 5: // DSI
                    return dsiVms.isEmpty() ? Vm.NULL : dsiVms.get((int) (id % dsiVms.size()));
                case 6: // MAIL
                    return mailVms.isEmpty() ? Vm.NULL : mailVms.get((int) (id % mailVms.size()));
                case 1: // DB
                    return dbVms.isEmpty() ? Vm.NULL : dbVms.get((int) (id % dbVms.size()));
                default:
                    return entVms.isEmpty() ? Vm.NULL : entVms.get(0);
            }
        });
    }

    // =========================================================================
    // ÉTAPE 3 : CRÉATION DES VMS DE BASE (42 VMs = 184 cœurs)
    // =========================================================================
    private void buildBaselineVms() {
        // A. Bases de données (PaaS) : 6 VMs (3 Tanger, 3 Tétouan) - 8 cœurs, 32 Go RAM
        for (int i = 0; i < CloudArchitectureConfig.VM_DB_COUNT; i++) {
            boolean isTanger = (i < 3);
            Vm vm = createVm(
                CloudArchitectureConfig.VM_DB_PES,
                CloudArchitectureConfig.VM_DB_RAM_MB,
                isTanger ? "PRIV_TANGER:DB" : "PRIV_TETOUAN:DB"
            );
            dbVms.add(vm);
            vmList.add(vm);
        }

        // B. E-learning Moodle (SaaS) : 15 VMs (8 Tanger, 7 Tétouan) - 4 cœurs, 16 Go RAM
        for (int i = 0; i < CloudArchitectureConfig.VM_MOODLE_COUNT; i++) {
            boolean isTanger = (i < 8);
            Vm vm = createVm(
                CloudArchitectureConfig.VM_MOODLE_PES,
                CloudArchitectureConfig.VM_MOODLE_RAM_MB,
                isTanger ? "PRIV_TANGER:MOODLE" : "PRIV_TETOUAN:MOODLE"
            );
            moodleVms.add(vm);
            vmList.add(vm);
        }

        // C. ENT & Inscriptions (SaaS) : 5 VMs de base (3 Tanger, 2 Tétouan) - 4 cœurs, 8 Go RAM
        for (int i = 0; i < CloudArchitectureConfig.VM_ENT_BASE_COUNT; i++) {
            boolean isTanger = (i < 3);
            Vm vm = createVm(
                CloudArchitectureConfig.VM_ENT_PES,
                CloudArchitectureConfig.VM_ENT_RAM_MB,
                isTanger ? "PRIV_TANGER:ENT" : "PRIV_TETOUAN:ENT"
            );
            entVms.add(vm);
            vmList.add(vm);
        }

        // D. Examens en ligne (SaaS) : 2 VMs de base (1 Tanger, 1 Tétouan) - 4 cœurs, 8 Go RAM
        for (int i = 0; i < CloudArchitectureConfig.VM_EXAMS_BASE_COUNT; i++) {
            boolean isTanger = (i < 1);
            Vm vm = createVm(
                CloudArchitectureConfig.VM_EXAMS_PES,
                CloudArchitectureConfig.VM_EXAMS_RAM_MB,
                isTanger ? "PRIV_TANGER:EXAMS" : "PRIV_TETOUAN:EXAMS"
            );
            examVms.add(vm);
            vmList.add(vm);
        }

        // E. DSI & Recherche (IaaS) : 10 VMs (5 Tanger, 5 Tétouan) - 4 cœurs, 8 Go RAM
        for (int i = 0; i < CloudArchitectureConfig.VM_DSI_COUNT; i++) {
            boolean isTanger = (i < 5);
            Vm vm = createVm(
                CloudArchitectureConfig.VM_DSI_PES,
                CloudArchitectureConfig.VM_DSI_RAM_MB,
                isTanger ? "PRIV_TANGER:DSI" : "PRIV_TETOUAN:DSI"
            );
            dsiVms.add(vm);
            vmList.add(vm);
        }

        // F. Messagerie & Bibliothèque (SaaS) : 4 VMs (2 Tanger, 2 Tétouan) - 2 cœurs, 4 Go RAM
        for (int i = 0; i < CloudArchitectureConfig.VM_MAIL_COUNT; i++) {
            boolean isTanger = (i < 2);
            Vm vm = createVm(
                CloudArchitectureConfig.VM_MAIL_PES,
                CloudArchitectureConfig.VM_MAIL_RAM_MB,
                isTanger ? "PRIV_TANGER:MAIL" : "PRIV_TETOUAN:MAIL"
            );
            mailVms.add(vm);
            vmList.add(vm);
        }

        broker.submitVmList(vmList);
    }

    private Vm createVm(int pes, long ramMb, String description) {
        CloudletScheduler scheduler = (policy.getTaskSchedulerType() == SimulationPolicy.TaskSchedulerType.TIME_SHARED)
            ? new CloudletSchedulerTimeShared()
            : new CloudletSchedulerSpaceShared();

        Vm vm = new VmSimple(CloudArchitectureConfig.HOST_MIPS, pes, scheduler);
        vm.setRam(ramMb)
          .setBw(1000)
          .setSize(20000)
          .setDescription(description);
        return vm;
    }

    // =========================================================================
    // ÉTAPE 4 : CONFIGURATION DE L'AUTO-SCALING (HORIZONTAL VM SCALING)
    // =========================================================================
    // =========================================================================
    // ÉTAPE 4 : CONFIGURATION DE L'AUTO-SCALING (HORIZONTAL VM SCALING)
    // =========================================================================
    /**
     * Auto-scaling horizontal adapté aux besoins universitaires :
     * Permet à chaque service critique (ENT, Examens) de provisionner des VMs
     * supplémentaires tant que le processeur est surchargé (> 60%) ou que
     * des requêtes attendent en file, jusqu'au dimensionnement plafond du cahier des charges.
     */
    private static class UaeHorizontalVmScaling extends HorizontalVmScalingSimple {
        private final int maxScalePerVm;
        private int scaledCount = 0;

        public UaeHorizontalVmScaling(int maxScalePerVm) {
            this.maxScalePerVm = maxScalePerVm;
        }

        @Override
        protected boolean requestUpScaling(double time) {
            if (scaledCount >= maxScalePerVm) {
                return false;
            }
            scaledCount++;
            Vm newVm = getVmSupplier().get();
            if (newVm != null && newVm != Vm.NULL) {
                getVm().getBroker().submitVm(newVm);
                return true;
            }
            return false;
        }
    }

    private void configureHorizontalAutoscaling() {
        // Capacité d'absorption locale : 22 VMs (88 cœurs) avant débordement
        // Reliquat vers le Cloud Public : jusqu'à 26 VMs (104 cœurs)
        // Total additionnel possible : +48 VMs (20 ENT + 28 Examens)
        final int MAX_LOCAL_BURST_VMS = CloudArchitectureConfig.AVAILABLE_LOCAL_BURST_CORES / CloudArchitectureConfig.VM_ENT_PES; // 22

        // Chaque VM ENT de base (5 VMs) peut provisionner jusqu'à 4 VMs supplémentaires (5 * 4 = +20 VMs)
        int entScalePerVm = CloudArchitectureConfig.VM_ENT_BURST_ADDITIONAL / CloudArchitectureConfig.VM_ENT_BASE_COUNT; // 4
        for (Vm vm : entVms) {
            UaeHorizontalVmScaling scaling = new UaeHorizontalVmScaling(entScalePerVm);
            scaling.setVmSupplier(() -> {
                int currentScaled = scaledVmsCount.incrementAndGet();
                boolean routeToPublic = (currentScaled > MAX_LOCAL_BURST_VMS);

                String tag;
                if (routeToPublic) {
                    publicBurstVmsCount.incrementAndGet();
                    tag = "BURST_PUBLIC:ENT";
                } else {
                    tag = (currentScaled % 2 == 0) ? "PRIV_TETOUAN:ENT_SCALE" : "PRIV_TANGER:ENT_SCALE";
                }

                Vm newVm = createVm(CloudArchitectureConfig.VM_ENT_PES, CloudArchitectureConfig.VM_ENT_RAM_MB, tag);
                entVms.add(newVm);
                vmList.add(newVm);
                return newVm;
            });
            scaling.setOverloadPredicate(v -> v.getCpuPercentUtilization() > 0.50);
            vm.setHorizontalScaling(scaling);
        }

        // Chaque VM Examen de base (2 VMs) peut provisionner jusqu'à 14 VMs supplémentaires (2 * 14 = +28 VMs)
        int examScalePerVm = CloudArchitectureConfig.VM_EXAMS_BURST_ADDITIONAL / CloudArchitectureConfig.VM_EXAMS_BASE_COUNT; // 14
        for (Vm vm : examVms) {
            UaeHorizontalVmScaling scaling = new UaeHorizontalVmScaling(examScalePerVm);
            scaling.setVmSupplier(() -> {
                int currentScaled = scaledVmsCount.incrementAndGet();
                boolean routeToPublic = (currentScaled > MAX_LOCAL_BURST_VMS);

                String tag;
                if (routeToPublic) {
                    publicBurstVmsCount.incrementAndGet();
                    tag = "BURST_PUBLIC:EXAM";
                } else {
                    tag = (currentScaled % 2 == 0) ? "PRIV_TETOUAN:EXAM_SCALE" : "PRIV_TANGER:EXAM_SCALE";
                }

                Vm newVm = createVm(CloudArchitectureConfig.VM_EXAMS_PES, CloudArchitectureConfig.VM_EXAMS_RAM_MB, tag);
                examVms.add(newVm);
                vmList.add(newVm);
                return newVm;
            });
            scaling.setOverloadPredicate(v -> v.getCpuPercentUtilization() > 0.50);
            vm.setHorizontalScaling(scaling);
        }
    }

    // =========================================================================
    // ÉTAPE 5 : MODÉLISATION DES CHARGES (CLOUDLETS)
    // =========================================================================
    private void buildCloudlets() {
        Random rand = new Random(42); // graine fixe pour reproductibilité

        if (scenario == SimulationScenario.NORMAL_PERIOD) {
            // Période normale : charge modérée, utilisation stochastique (~80 requêtes réparties)
            submitCloudletBatch(2, 25, 50000, 100000, 4, true, rand); // MOODLE
            submitCloudletBatch(5, 10, 300000, 300000, 4, true, rand); // DSI
            submitCloudletBatch(6, 20, 10000,  15000,  2, true, rand); // MAIL
            submitCloudletBatch(3, 15, 2000,   4000,   4, true, rand); // ENT
            submitCloudletBatch(4,  5, 1000,   2000,   4, true, rand); // EXAM
            submitCloudletBatch(1,  8, 15000,  25000,  8, true, rand); // DB
        } else {
            // Période de pointe (Statique ou Élastique) :
            // Trafic de fond normal soumis au démarrage
            submitCloudletBatch(2, 20, 50000, 80000, 4, false, rand); // MOODLE
            submitCloudletBatch(5,  5, 300000, 300000, 4, false, rand); // DSI
            submitCloudletBatch(6, 15, 10000, 12000, 2, false, rand); // MAIL
            submitCloudletBatch(1, 10, 20000, 30000, 8, false, rand); // DB

            // Vagues massives et échelonnées d'inscriptions (ENT) et d'examens
            // Vague 1 à t=0 pour déclencher la saturation immédiate
            submitCloudletBatch(3, 60, 3000, 5000, 4, false, rand); // ENT
            submitCloudletBatch(4, 50, 1000, 2000, 4, false, rand); // EXAM

            // Vagues successives injectées dynamiquement au fil de l'horloge
            // Permet d'alimenter les nouvelles VMs dès qu'elles sont créées par l'auto-scaling
            final int[] wavesSent = {0};
            final int TOTAL_WAVES = 6;
            simulation.addOnClockTickListener(evt -> {
                double time = evt.getTime();
                // Injecter une nouvelle vague toutes les 2 secondes jusqu'à la fin du pic
                if (wavesSent[0] < TOTAL_WAVES && time >= (wavesSent[0] + 1) * 2.0) {
                    wavesSent[0]++;
                    submitCloudletBatch(3, 50, 3000, 5000, 4, false, rand); // ENT (+50 par vague)
                    submitCloudletBatch(4, 40, 1000, 2000, 4, false, rand); // EXAM (+40 par vague)
                }
            });
        }
    }

    private void submitCloudletBatch(
            int servicePriority,
            int count,
            long minMi,
            long maxMi,
            int pesNumber,
            boolean isStochastic,
            Random rand) {

        List<Cloudlet> batch = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            long length = (minMi == maxMi) ? minMi : (minMi + (long) (rand.nextDouble() * (maxMi - minMi)));

            UtilizationModel cpuModel = isStochastic
                ? new UtilizationModelStochastic()
                : new UtilizationModelFull();

            Cloudlet cloudlet = new CloudletSimple(length, pesNumber, cpuModel);
            // 0.5% RAM par requête (évite le swapping en mémoire virtuelle sous forte concurrence)
            cloudlet.setUtilizationModelRam(new UtilizationModelDynamic(0.005));
            cloudlet.setUtilizationModelBw(new UtilizationModelDynamic(0.005));
            cloudlet.setPriority(servicePriority);
            cloudlet.setSubmissionDelay(simulation.clock());

            batch.add(cloudlet);
            cloudletList.add(cloudlet);
        }

        broker.submitCloudletList(batch);
    }

    // =========================================================================
    // ÉTAPE 6 : COLLECTE DES INDICATEURS DE PERFORMANCE & COÛTS
    // =========================================================================
    private SimulationMetrics collectMetrics() {
        SimulationMetrics m = new SimulationMetrics(
            scenario.getDescription(),
            policy.getTaskSchedulerType().getLabel(),
            policy.getVmAllocationPolicyType().getLabel()
        );

        m.setMakespanSeconds(simulation.clock());

        List<Cloudlet> finished = broker.getCloudletFinishedList();
        m.setTotalCloudletsSubmitted(cloudletList.size());
        m.setTotalCloudletsFinished(finished.size());

        double totalWaitTime = 0.0;
        double totalExecTime = 0.0;
        double totalResponseTime = 0.0;

        for (Cloudlet c : finished) {
            double wait = c.getWaitingTime();
            double exec = c.getActualCpuTime();
            double response = c.getFinishTime() - c.getSubmissionDelay();

            totalWaitTime += wait;
            totalExecTime += exec;
            totalResponseTime += Math.max(0.0, response);
        }

        int count = Math.max(1, finished.size());
        m.setAvgWaitingTimeSeconds(totalWaitTime / count);
        m.setAvgExecutionTimeSeconds(totalExecTime / count);
        m.setAvgResponseTimeSeconds(totalResponseTime / count);

        // Analyse spécifique des services critiques à forte élasticité (ENT & Examens)
        double entExamWait = 0.0;
        double entExamResponse = 0.0;
        int entExamCount = 0;
        for (Cloudlet c : finished) {
            if (c.getPriority() == 3 || c.getPriority() == 4) { // ENT ou EXAM
                entExamWait += c.getWaitingTime();
                entExamResponse += (c.getFinishTime() - c.getSubmissionDelay());
                entExamCount++;
            }
        }
        if (entExamCount > 0) {
            System.out.printf("      [Détail ENT/Examens] Rép. Moy: %.2fs | Attente Moy: %.2fs (%d tâches)%n",
                entExamResponse / entExamCount, entExamWait / entExamCount, entExamCount);
        }

        // Calcul de l'utilisation moyenne des hôtes
        List<Host> privateHosts = new ArrayList<>();
        privateHosts.addAll(dcTanger.getHostList());
        privateHosts.addAll(dcTetouan.getHostList());

        double sumCpuUtil = 0.0;
        double sumRamUtil = 0.0;
        for (Host h : privateHosts) {
            sumCpuUtil += h.getCpuUtilizationStats().getMean();
            sumRamUtil += ((double) h.getRam().getAllocatedResource() / h.getRam().getCapacity());
        }
        int privateHostCount = Math.max(1, privateHosts.size());
        m.setHostCpuUtilizationPercent((sumCpuUtil / privateHostCount) * 100.0);
        m.setHostRamUtilizationPercent((sumRamUtil / privateHostCount) * 100.0);

        // Nombre de VMs
        m.setTotalVmsCreated(vmList.size());
        m.setPublicBurstVmsCount(publicBurstVmsCount.get());
        m.setPrivateVmsCount(vmList.size() - publicBurstVmsCount.get());

        // Calcul des coûts
        double privateCost = 0.0;
        double publicCost = 0.0;

        for (Vm vm : vmList) {
            VmCost vmCost = new VmCost(vm);
            double cost = vmCost.getTotalCost();
            String desc = vm.getDescription();
            if (desc != null && desc.contains("PUBLIC")) {
                publicCost += cost;
            } else {
                privateCost += cost;
            }
        }

        m.setPrivateCloudCost(privateCost);
        m.setPublicCloudCost(publicCost);
        m.setTotalCost(privateCost + publicCost);

        return m;
    }
}
