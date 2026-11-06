package com.uae.cloud;

/**
 * Configuration de l'architecture Cloud de l'Université Abdelmalek Essaâdi (UAE).
 * Cette classe centralise l'ensemble des constantes et spécifications matérielles
 * définies dans architecture_uae.md et documentation_uae.md.
 */
public final class CloudArchitectureConfig {

    private CloudArchitectureConfig() {
        // Classe utilitaire - constructeur privé
    }

    // =========================================================================
    // 1. SPÉCIFICATIONS PHYSIQUES DES HÔTES (SERVEURS PHYSIQUES)
    // =========================================================================
    /** Nombre de cœurs physiques (PEs) par hôte (serveur bi-processeur 16 cœurs) */
    public static final int HOST_PES = 32;

    /** Puissance de calcul par cœur en MIPS (Million Instructions Per Second) */
    public static final double HOST_MIPS = 2500.0;

    /** Capacité mémoire RAM par hôte en Mo (128 Go = 131 072 Mo) */
    public static final long HOST_RAM_MB = 128 * 1024L;

    /** Capacité de stockage par hôte en Mo (10 To = 10 485 760 Mo) */
    public static final long HOST_STORAGE_MB = 10 * 1024 * 1024L;

    /** Bande passante réseau par hôte en Mbps (10 Gbps = 10 000 Mbps) */
    public static final long HOST_BW_MBPS = 10_000L;

    // =========================================================================
    // 2. DATACENTERS DU CLOUD PRIVÉ (Tanger & Tétouan/Martil)
    // =========================================================================
    /** Nombre d'hôtes physiques au Datacenter 1 (Tanger) */
    public static final int DC1_HOSTS_COUNT = 5;

    /** Nombre d'hôtes physiques au Datacenter 2 (Tétouan / Martil) */
    public static final int DC2_HOSTS_COUNT = 5;

    /** Nombre total d'hôtes physiques dans le Cloud Privé (10 hôtes = 320 cœurs, 1280 Go RAM) */
    public static final int TOTAL_PRIVATE_HOSTS = DC1_HOSTS_COUNT + DC2_HOSTS_COUNT;

    /** Capacité totale en cœurs du Cloud Privé */
    public static final int TOTAL_PRIVATE_CORES = TOTAL_PRIVATE_HOSTS * HOST_PES; // 320

    // =========================================================================
    // 3. CAPACITÉ, SEUIL DE BURSTING & CLOUD PUBLIC
    // =========================================================================
    /**
     * Seuil critique d'utilisation CPU du Cloud Privé (85%).
     * Au-delà de ce seuil (272 cœurs), le débordement vers le Cloud Public
     * (Cloud Bursting) est automatiquement déclenché pour préserver la marge
     * de sécurité (Disaster Recovery).
     */
    public static final double PRIVATE_BURSTING_THRESHOLD_PERCENT = 0.85;

    /** Cœurs maximum allouables dans le Cloud Privé avant bursting (320 * 0.85 = 272) */
    public static final int MAX_PRIVATE_CORES_ALLOWED = (int) (TOTAL_PRIVATE_CORES * PRIVATE_BURSTING_THRESHOLD_PERCENT);

    /** Cœurs utilisés en période normale par les services de base (184 cœurs) */
    public static final int NORMAL_PERIOD_CORES_USED = 184;

    /** Cœurs disponibles dans le privé pour l'élasticité locale (272 - 184 = 88 cœurs = 22 VMs de 4 cœurs) */
    public static final int AVAILABLE_LOCAL_BURST_CORES = MAX_PRIVATE_CORES_ALLOWED - NORMAL_PERIOD_CORES_USED;

    /** Nombre d'hôtes alloués au Datacenter Cloud Public pour le bursting */
    public static final int PUBLIC_DC_HOSTS_COUNT = 6;

    // =========================================================================
    // 4. CONFIGURATION DES MACHINES VIRTUELLES (VMs) PAR SERVICE
    // =========================================================================
    // A. Bases de Données (PaaS/IaaS) - 6 VMs (3 par DC) - Strictement privées
    public static final int VM_DB_PES = 8;
    public static final long VM_DB_RAM_MB = 32 * 1024L;
    public static final int VM_DB_COUNT = 6; // 3 Tanger + 3 Tétouan

    // B. E-learning & Moodle (SaaS) - 15 VMs
    public static final int VM_MOODLE_PES = 4;
    public static final long VM_MOODLE_RAM_MB = 16 * 1024L;
    public static final int VM_MOODLE_COUNT = 15; // 8 Tanger + 7 Tétouan

    // C. Inscriptions & ENT Apogée (SaaS) - 5 de base, jusqu'à 25 en pointe (+20)
    public static final int VM_ENT_PES = 4;
    public static final long VM_ENT_RAM_MB = 8 * 1024L;
    public static final int VM_ENT_BASE_COUNT = 5;
    public static final int VM_ENT_MAX_COUNT = 25;
    public static final int VM_ENT_BURST_ADDITIONAL = VM_ENT_MAX_COUNT - VM_ENT_BASE_COUNT; // +20 VMs

    // D. Examens en ligne (SaaS) - 2 de base, jusqu'à 30 en session (+28)
    public static final int VM_EXAMS_PES = 4;
    public static final long VM_EXAMS_RAM_MB = 8 * 1024L;
    public static final int VM_EXAMS_BASE_COUNT = 2;
    public static final int VM_EXAMS_MAX_COUNT = 30;
    public static final int VM_EXAMS_BURST_ADDITIONAL = VM_EXAMS_MAX_COUNT - VM_EXAMS_BASE_COUNT; // +28 VMs

    // E. Serveurs DSI & Recherche (IaaS) - 10 VMs
    public static final int VM_DSI_PES = 4;
    public static final long VM_DSI_RAM_MB = 8 * 1024L;
    public static final int VM_DSI_COUNT = 10; // 5 Tanger + 5 Tétouan

    // F. Messagerie & Bibliothèque (SaaS) - 4 VMs
    public static final int VM_MAIL_PES = 2;
    public static final long VM_MAIL_RAM_MB = 4 * 1024L;
    public static final int VM_MAIL_COUNT = 4; // 2 Tanger + 2 Tétouan

    /** Total des VMs de base en période normale (6+15+5+2+10+4 = 42 VMs) */
    public static final int TOTAL_BASE_VMS = VM_DB_COUNT + VM_MOODLE_COUNT + VM_ENT_BASE_COUNT
            + VM_EXAMS_BASE_COUNT + VM_DSI_COUNT + VM_MAIL_COUNT;

    // =========================================================================
    // 5. MODÈLE DE COÛTS (Cloud Privé vs Cloud Public)
    // =========================================================================
    /** Coût par seconde d'exécution - Cloud Privé (coûts internes : électricité, clim) */
    public static final double COST_PRIVATE_PER_SEC = 0.01;
    public static final double COST_PRIVATE_PER_MEM = 0.0005;
    public static final double COST_PRIVATE_PER_STORAGE = 0.0001;
    public static final double COST_PRIVATE_PER_BW = 0.0001;

    /** Coût par seconde d'exécution - Cloud Public (facturation on-demand externe) */
    public static final double COST_PUBLIC_PER_SEC = 0.08;
    public static final double COST_PUBLIC_PER_MEM = 0.004;
    public static final double COST_PUBLIC_PER_STORAGE = 0.0005;
    public static final double COST_PUBLIC_PER_BW = 0.0005;
}

