package com.uae.cloud;

/**
 * Scénarios de charge simulés conformément au document modelisation_des_charges.md.
 */
public enum SimulationScenario {
    /**
     * Période Normale (Semestre classique) :
     * Charge modérée répartie, 42 VMs de base,
     * Modèle d'utilisation stochastique (UtilizationModelStochastic).
     */
    NORMAL_PERIOD("Période Normale (Semestre classique)"),

    /**
     * Période de Pointe Statique (Inscriptions & Examens SANS Auto-scaling) :
     * Pic massif de requêtes avec infrastructure figée à 42 VMs,
     * Montre la saturation, les files d'attente et la dégradation de service.
     */
    PEAK_PERIOD_STATIC("Période de Pointe (Statique - Sans élasticité)"),

    /**
     * Période de Pointe avec Auto-scaling (HorizontalVmScaling & Cloud Bursting) :
     * Détection de surcharge (seuil 85%), création automatique de VMs,
     * Saturation du privé jusqu'au seuil de sécurité, puis débordement sur le Public Cloud.
     */
    PEAK_PERIOD_AUTOSCALE("Période de Pointe (Élastique - HorizontalVmScaling + Cloud Bursting)");

    private final String description;

    SimulationScenario(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

