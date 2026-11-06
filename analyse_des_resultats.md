# 5. Analyse des Résultats de Simulation et Conclusion

Ce document répond aux exigences de la **Partie 1 (Section 5)** du cahier des charges du projet Cloud Computing :
> *"Analyser les résultats : temps de réponse, temps d'exécution, taux d'utilisation des hôtes, coût. Conclure : l'architecture proposée suffit-elle en période de pointe ?"*

---

## 1. Tableau Récapitulatif des Résultats de Simulation

Les simulations ont été exécutées sur le moteur **CloudSim Plus (v8.0.0)** en injectant les charges modélisées dans [`modelisation_des_charges.md`](file:///home/junaid_uth/partie1/projet-cloudsim/modelisation_des_charges.md) sur l'architecture hybride définie dans [`architecture_uae.md`](file:///home/junaid_uth/partie1/projet-cloudsim/architecture_uae.md).

| Scénario / Configuration | Ordonnanceur Tâches | Politique Alloc. VM | Makespan (s) | Temps Rép. Moy. (s) | Temps Attente (s) | CPU Hôtes (%) | VMs Créées (Privé / Public) | Tâches Réussies | Coût Total ($) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **1. Période Normale (Référence)** | `TimeShared` | `Simple` (First-Fit) | 248.95 s | 76.50 s | 0.00 s | 5.3 % | 42 (42 / 0) | 83 / 83 | $845.28 |
| **2. Période de Pointe (Statique)** | `TimeShared` | `Simple` (First-Fit) | 133.68 s | 97.63 s | 0.00 s | 5.7 % | 42 (42 / 0) | 700 / 700 | $633.19 |
| **3. Période de Pointe (Statique)** | `SpaceShared` | `Simple` (First-Fit) | 130.87 s | 54.30 s | 45.77 s | 7.1 % | 42 (42 / 0) | 700 / 700 | $628.01 |
| **4. Période Normale (BestFit)** | `TimeShared` | `BestFit` | 248.25 s | 77.04 s | 0.00 s | 5.0 % | 42 (42 / 0) | 83 / 83 | $844.00 |
| **5. Période de Pointe (Élastique)** | `TimeShared` | `BestFit` | **120.43 s** | **15.24 s** | **0.00 s** | **11.1 %** | **90 (64 / 26)** | 700 / 700 | $2 962.46 |

> [!NOTE]
> **Détail sur les services critiques à forte élasticité (ENT Apogée & Examens QCM) :**
> * En **Pointe Statique (42 VMs)** : Temps de réponse moyen = **102.36 s**.
> * En **Pointe Élastique (90 VMs)** : Temps de réponse moyen = **13.63 s** (divisé par **7.5×**).

---

## 2. Analyse Détaillée des Métriques

### A. Temps de Réponse (Response Time) & Temps d'Attente (Waiting Time)

Le temps de réponse représente le délai perçu par l'usager (étudiant ou enseignant) entre la soumission de sa requête et la réception du résultat complet.

1. **Comparaison `TimeShared` vs `SpaceShared` :**
   * En `CloudletSchedulerSpaceShared`, les cœurs de processeur sont alloués de façon exclusive selon le principe FIFO (*First In, First Out*). Dès qu'un afflux massif de clics QCM ou de soumissions Apogée arrive, les cœurs sont saturés et le temps d'attente explose à **45.77 s**. Les étudiants subissent un blocage total en file d'attente.
   * En `CloudletSchedulerTimeShared`, le processeur partage ses cycles d'horloge entre toutes les requêtes actives. Le temps d'attente en file est de **0.00 s**, garantissant qu'aucun étudiant n'est mis en attente indéfinie.

2. **Impact de l'Auto-scaling horizontal (`HorizontalVmScaling`) :**
   * Sous une charge de pointe sans élasticité (configuration statique à 42 VMs), le temps de réponse moyen grimpe à **97.63 s** au niveau global, et dépasse **102 s** sur l'ENT et les examens. Ce délai est critique et inacceptable pour un examen en ligne minuté.
   * Avec l'activation de `HorizontalVmScaling` et le déploiement dynamique de 48 VMs supplémentaires, le temps de réponse global s'effondre à **15.24 s**, et tombe à **13.63 s** sur les examens et inscriptions. La charge est immédiatement ventilée sur les nouveaux nœuds de calcul.

---

### B. Temps d'Exécution Global (Makespan)

Le Makespan désigne la durée totale nécessaire pour terminer l'ensemble des tâches soumises lors de la simulation.

* **En Période Normale (Makespan $\approx$ 248 s) :** 
  La durée globale est conditionnée par les simulations scientifiques lourdes de la recherche (DSI : 300 000 MI) et les sessions complètes d'e-learning (Moodle : 50 000 à 100 000 MI) modélisées avec `UtilizationModelStochastic`. Ces calculs de fond s'étalent sur la journée.
* **En Période de Pointe (Makespan $\approx$ 120 s à 133 s) :**
  L'utilisation de `UtilizationModelFull` mobilise 100 % des capacités CPU pour absorber le choc des requêtes simultanées. L'auto-scaling permet de réduire le Makespan de **133.68 s à 120.43 s**, prouvant une évacuation plus rapide de l'engorgement global.

---

### C. Taux d'Utilisation des Hôtes Physiques (CPU & RAM)

* **Comportement en charge normale (4.2 % à 5.3 %) :**
  L'infrastructure physique locale dispose d'une capacité colossale : 10 hôtes $\times$ 32 cœurs = **320 cœurs physiques** (800 000 MIPS). En période de fonctionnement classique, le taux d'utilisation moyen oscille autour de 5 %, ce qui laisse une marge thermique et énergétique optimale.
* **Comportement en période de pointe (11.1 %) :**
  Lors du pic d'inscriptions et d'examens, l'activité CPU moyenne sur les serveurs privés double pour atteindre **11.1 %**, avec des pointes de saturation localisées sur les hôtes hébergeant les VMs ENT et bases de données.
* **Comparaison `VmAllocationPolicySimple` vs `VmAllocationPolicyBestFit` :**
  * `Simple` (First-Fit) alloue la VM sur le premier serveur physique trouvé ayant des ressources libres. Les VMs se retrouvent dispersées sur les 10 hôtes.
  * `BestFit` sélectionne l'hôte physique laissant le moins de ressources résiduelles. Cette stratégie maximise la consolidation, évite l'éparpillement et permet de concentrer la charge sur un nombre réduit de serveurs (permettant d'éteindre les serveurs inactifs pour économiser l'électricité).

---

### D. Analyse Financière et Modèle de Coûts

Le modèle économique hybride distingue les coûts internes du Cloud Privé (amortissement matériel, électricité) des coûts d'usage à la demande du Cloud Public (tarification à la seconde d'AWS ou Azure) :

$$\text{Coût Total} = \text{Coût Cloud Privé} + \text{Coût Cloud Public (Bursting)}$$

| Scénario | Coût Cloud Privé | Coût Cloud Public | Coût Total | Remarque |
| :--- | :---: | :---: | :---: | :--- |
| **Période Normale (42 VMs)** | $844.00 | $0.00 | **$844.00** | 100 % des VMs hébergées en interne |
| **Pointe Statique (42 VMs)** | $633.19 | $0.00 | **$633.19** | Saturation sévère, aucun coût externe |
| **Pointe Élastique (90 VMs)** | $912.30 | $2 050.16 | **$2 962.46** | Débordement de 26 VMs sur le Cloud Public |

> [!TIP]
> **Justification économique (Capex vs Opex) :**
> Bien que la période de pointe élastique génère un coût supérieur ($2 962.46), il s'agit d'une **dépense opérationnelle temporaire (Opex)** facturée uniquement durant les quelques jours de pointe (inscriptions de septembre et examens de fin de semestre).
> Acheter 48 serveurs physiques supplémentaires pour absorber ce pic en interne coûterait plusieurs dizaines de milliers d'euros en investissement matériel (Capex), serveurs qui resteraient inutilisés 11 mois sur 12. Le recours au Cloud Bursting est donc la solution la plus rentable pour l'université.

---

## 3. Souveraineté des Données et Sécurité dans le Cloud Hybride

Conformément à la politique définie par le `DatacenterMapper` :
* **Bases de Données (PaaS/IaaS - 6 VMs) :** Hébergent les données hautement sensibles (dossiers scolaires Apogée, notes d'examens, identités des 130 000 étudiants). Elles restent **strictement confinées dans les datacenters privés de Tanger et Tétouan**. Aucun débordement sur le cloud public n'est autorisé pour ces VMs.
* **Serveurs Applicatifs sans état (SaaS) :** Seules les VMs de présentation web (formulaires ENT et minuteurs QCM) débordent sur le Cloud Public. En cas de compromission d'une instance publique, aucune base de données critique n'est exposée.

---

## 4. Conclusion Générale : L'Architecture Proposée Suffit-elle en Période de Pointe ?

Pour répondre formellement à la question posée dans l'énoncé du projet :

### 1. En infrastructure purement locale (Cloud Privé seul) : **NON, ELLE NE SUFFIT PAS.**
* Le Cloud Privé dispose au total de **320 cœurs physiques** et **1 280 Go de RAM**.
* Pour garantir la tolérance aux pannes (*Disaster Recovery*) et la stabilité des hyperviseurs, un seuil de sécurité strict de **85 % (272 cœurs maximum)** a été fixé.
* En dehors des pics, les services consomment déjà **184 cœurs** (42 VMs de base).
* La capacité d'absorption locale restante avant d'atteindre le seuil critique n'est que de **88 cœurs** (soit **22 VMs** de 4 cœurs).
* Or, le pic simultané des inscriptions et des examens exige **48 nouvelles VMs (192 cœurs additionnels)**. L'infrastructure locale seule est donc mathématiquement incapable d'encaisser la charge sans risquer l'effondrement ou des temps d'attente inacceptables (> 100 s).

### 2. Avec l'architecture Cloud Hybride adoptée (Cloud Bursting) : **OUI, PARFAITEMENT.**
* L'orchestrateur sature d'abord intelligemment les ressources locales jusqu'à concurrence du seuil de sécurité de 85 %, déployant **22 VMs dans le Cloud Privé** (pour atteindre 64 VMs locales).
* Les **26 VMs excédentaires (104 cœurs)** sont automatiquement instanciées sur le **Cloud Public**.
* **Bilan final :**
  1. **Disponibilité :** 100 % des requêtes sont traitées avec succès sans aucune perte.
  2. **Performance :** Le temps de réponse moyen pour les étudiants passe de plus de 102 secondes à **moins de 14 secondes**.
  3. **Souveraineté :** Toutes les données sensibles restent hébergées sur le territoire universitaire.
  4. **Résilience :** La marge de réserve de 15 % (48 cœurs) reste intacte pour parer à une panne éventuelle d'un hôte physique ou du datacenter de Tanger.

