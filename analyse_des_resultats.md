# 5. Analyse des Résultats de Simulation et Conclusion

Ce document répond aux exigences de la **Partie 1 (Section 5)** du cahier des charges du projet Cloud Computing :
> *"Analyser les résultats : temps de réponse, temps d'exécution, taux d'utilisation des hôtes, coût. Conclure : l'architecture proposée suffit-elle en période de pointe ?"*

---

## 1. Tableau Récapitulatif des Résultats de Simulation

Les simulations ont été exécutées sur le moteur **CloudSim Plus (v8.0.0)** en injectant les charges modélisées dans [`modelisation_des_charges.md`](file:///home/junaid_uth/partie1/projet-cloudsim/modelisation_des_charges.md) sur l'architecture hybride définie dans [`architecture_uae.md`](file:///home/junaid_uth/partie1/projet-cloudsim/architecture_uae.md).

| Scénario / Configuration | Ordonnanceur Tâches | Politique Alloc. VM | Makespan (s) | Temps Rép. Moy. (s) | Temps Attente (s) | CPU Hôtes (%) | VMs Créées (Privé / Public) | Tâches Réussies | Coût Total ($) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **1. Période Normale (Référence)** | `TimeShared` | `Simple` (First-Fit) | 32.45 s | 7.36 s | 0.00 s | 2.6 % | 42 (42 / 0) | 90 / 90 | $446.91 |
| **2. Période de Pointe (Statique)** | `TimeShared` | `Simple` (First-Fit) | 120.43 s | 30.83 s | 0.00 s | 12.0 % | 42 (42 / 0) | 700 / 700 | $608.80 |
| **3. Période de Pointe (Statique)** | `SpaceShared` | `Simple` (First-Fit) | 120.43 s | 22.02 s | 12.87 s | 12.0 % | 42 (42 / 0) | 700 / 700 | $608.80 |
| **4. Période Normale (BestFit)** | `TimeShared` | `BestFit` | 34.43 s | 7.45 s | 0.00 s | 4.9 % | 42 (42 / 0) | 90 / 90 | $450.56 |
| **5. Période de Pointe (Élastique)** | `TimeShared` | `BestFit` | **120.38 s** | **10.83 s** | **0.00 s** | **9.3 %** | **90 (64 / 26)** | 700 / 700 | $2 951.81 |

> [!NOTE]
> **Détail sur le service en pointe (Inscriptions & ENT Apogée - 605 Cloudlets) :**
> * En **Période Normale** : Temps de réponse moyen = **2.70 s** (délai web parfaitement fluide et réactif).
> * En **Pointe Statique (42 VMs - 5 VMs ENT)** : Temps de réponse moyen = **32.34 s** (engorgement sévère des 605 étudiants sur seulement 5 serveurs d'inscription, latence multipliée par **12×**).
> * En **Pointe Élastique (90 VMs - 53 VMs ENT)** : Temps de réponse moyen = **9.20 s** (chute spectaculaire de la latence grâce aux 48 VMs additionnelles et au Cloud Bursting).

---

## 2. Analyse Détaillée des Métriques

### A. Temps de Réponse (Response Time) & Temps d'Attente (Waiting Time)

Le temps de réponse représente le délai perçu par l'usager (étudiant ou enseignant) entre la soumission de sa requête et la réception du résultat complet.

1. **Hiérarchie naturelle des performances :**
   * **Période Normale (7.36 s au global / 2.70 s sur l'ENT) :** L'infrastructure locale de base (42 VMs) absorbe sans aucune difficulté la charge de routine. Les requêtes interactives se traitent en moins de 3 secondes.
   * **Période de Pointe Statique (30.83 s au global / 32.34 s sur l'ENT) :** Sous l'afflux massif de 700 requêtes d'étudiants, la latence est multipliée par plus de 4 au global et par **12× sur l'ENT**. L'infrastructure statique est totalement engorgée.
   * **Période de Pointe Élastique (10.83 s au global / 9.20 s sur l'ENT) :** L'auto-scaling horizontal instancie dynamiquement 48 nouvelles VMs (dont 26 sur le Cloud Public), ramenant le délai à un niveau acceptable sous forte charge.

2. **Comparaison `TimeShared` vs `SpaceShared` :**
   * En `CloudletSchedulerSpaceShared`, les cœurs de processeur sont alloués de façon exclusive selon le principe FIFO (*First In, First Out*). Dès que l'afflux massif de 605 soumissions d'inscription et de transactions DB arrive sur les 5 VMs ENT de base, les cœurs sont saturés et un temps d'attente bloquant explose à **12.87 s en moyenne** (et **14.82 s** sur l'ENT). Les étudiants subissent un blocage total devant une page blanche.
   * En `CloudletSchedulerTimeShared`, le processeur partage ses cycles d'horloge entre toutes les requêtes actives. Le temps d'attente en file est de **0.00 s**, garantissant un démarrage immédiat du traitement pour chaque étudiant.

---

### B. Temps d'Exécution Global (Makespan)

Le Makespan désigne la durée totale nécessaire pour terminer l'ensemble des tâches soumises lors de la simulation.

* **En Période Normale (Makespan $\approx$ 32 à 34 s) :** 
  L'ensemble des 90 tâches de routine universitaires est évacué rapidement, démontrant une infrastructure saine en temps de fonctionnement standard.
* **En Période de Pointe (Makespan $\approx$ 120 s) :**
  Ce Makespan reflète la durée totale du pic de rentrée avec l'injection échelonnée de 6 vagues successives d'inscriptions et d'écritures SQL réparties sur l'horloge de simulation. L'auto-scaling absorbe l'intégralité du pic sans aucun ralentissement résiduel.

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
| **Période Normale (42 VMs)** | $450.56 | $0.00 | **$450.56** | 100 % des VMs hébergées en interne |
| **Pointe Statique (42 VMs)** | $608.80 | $0.00 | **$608.80** | Saturation sévère, aucun coût externe |
| **Pointe Élastique (90 VMs)** | $901.65 | $2 050.16 | **$2 951.81** | Débordement de 26 VMs sur le Cloud Public |

> [!TIP]
> **Justification économique (Capex vs Opex) :**
> Bien que la période de pointe élastique génère un coût supérieur ($2 951.81), il s'agit d'une **dépense opérationnelle temporaire (Opex)** facturée uniquement durant les quelques jours du grand pic d'inscriptions de septembre.
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
* Or, le pic massif des inscriptions de rentrée exige **48 nouvelles VMs ENT (192 cœurs additionnels)** pour traiter les 605 requêtes d'étudiants. L'infrastructure locale seule est donc mathématiquement incapable d'encaisser la charge sans risquer l'effondrement ou des temps d'attente inacceptables (> 30 s).

### 2. Avec l'architecture Cloud Hybride adoptée (Cloud Bursting) : **OUI, PARFAITEMENT.**
* L'orchestrateur sature d'abord intelligemment les ressources locales jusqu'à concurrence du seuil de sécurité de 85 %, déployant **22 VMs ENT dans le Cloud Privé** (pour atteindre 64 VMs locales).
* Les **26 VMs excédentaires (104 cœurs)** sont automatiquement instanciées sur le **Cloud Public**.
* **Bilan final :**
  1. **Disponibilité :** 100 % des requêtes sont traitées avec succès sans aucune perte.
  2. **Performance :** Le temps de réponse moyen pour les étudiants sur le portail d'inscriptions (ENT) passe de plus de 32 secondes en statique à **9.20 secondes** (et **10.83 secondes** au niveau global).
  3. **Souveraineté :** Toutes les données sensibles restent hébergées sur le territoire universitaire.
  4. **Résilience :** La marge de réserve de 15 % (48 cœurs) reste intacte pour parer à une panne éventuelle d'un hôte physique ou du datacenter de Tanger.

