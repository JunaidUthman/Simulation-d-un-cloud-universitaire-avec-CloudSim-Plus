## 3. Modélisation des charges (Cloudlets)

Pour simuler le comportement du cloud de l'Université Abdelmalek Essaâdi (qui compte plus de 130 000 étudiants) dans CloudSim Plus, nous devons modéliser les tâches exécutées par les serveurs. Dans CloudSim, une tâche ou requête est représentée par un objet `Cloudlet`, dont la taille est mesurée en MI (Millions d'Instructions).

Nous distinguons deux scénarios d'utilisation pour notre simulation :

### A. Période Normale (Semestre classique)

Durant un semestre typique, la charge est modérée et répartie sur la journée. Les étudiants consultent des cours, tandis que les chercheurs effectuent des travaux de fond.

* **Nombre de connexions (Trafic) :** Modéré. Environ 10 000 à 15 000 requêtes concurrentes (représentant environ 10% des étudiants actifs simultanément sur les services de l'université).
* **Types de requêtes (Cloudlets - Total : 90 Cloudlets) :**
  * **E-learning / Moodle (SaaS) :** Consultation de polycopiés (PDF), diapositives de cours et lecture de forums.
    * *Modélisation :* 25 Cloudlets de 4 000 à 8 000 MI (1 cœur). Tâches interactives réactives exécutées en quelques secondes.
  * **Recherche et DSI (IaaS) :** Exécution de scripts de recherche, simulations scientifiques courantes et tests d'algorithmes.
    * *Modélisation :* 10 Cloudlets de 20 000 à 40 000 MI (4 cœurs). Tâches de calcul plus substantielles mais calibrées pour une utilisation interactive normale.
  * **Messagerie et Bibliothèque (SaaS) :** Recherches documentaires et lecture d'emails universitaires.
    * *Modélisation :* 20 Cloudlets de 2 000 à 5 000 MI (1 cœur).
  * **Scolarité et ENT (SaaS) :** Consultation de routine des dossiers étudiants, emplois du temps et relevés de notes.
    * *Modélisation :* 15 Cloudlets courts (2 000 à 4 000 MI, 1 cœur). Trafic de fond léger sollicitant les 5 VMs ENT de base sans provoquer de saturation.
  * **Évaluations et Quiz continus (SaaS) :** Petits tests hebdomadaires, devoirs et QCM d'entraînement en contrôle continu.
    * *Modélisation :* 5 Cloudlets très courts (1 000 à 2 000 MI, 1 cœur) en faible volume, absorbés aisément par les 2 VMs d'examen de base.
  * **Bases de Données (PaaS/IaaS) :** Requêtes d'interrogation SQL et synchronisations administratives courantes.
    * *Modélisation :* 15 Cloudlets de 5 000 à 10 000 MI (2 cœurs), répartis sur les 6 serveurs de bases de données internes.

### B. Période de Pointe : Rentrée Universitaire (Inscriptions Massives de Septembre)

Dans le calendrier académique réel de l'Université Abdelmalek Essaâdi, **les inscriptions annuelles et les examens semestriels ne se chevauchent jamais** (les inscriptions ont lieu en septembre/octobre, tandis que les partiels se déroulent en janvier et juin). Ce scénario modélise donc le goulot d'étranglement le plus critique de l'année : **la semaine des inscriptions en ligne sur l'ENT et Apogée**, totalisant exactement **700 Cloudlets**.

* **Nombre de connexions (Trafic) :** Pic massif représentant un afflux quasi simultané de dizaines de milliers d'étudiants, saturant les ressources initiales et déclenchant l'auto-scaling et le débordement sur le Cloud Public (`HorizontalVmScaling`).
* **Répartition des 700 Cloudlets :**
  * **Inscriptions & ENT (SaaS - 605 Cloudlets) :** Soumission de formulaires d'inscription, vérification d'éligibilité, téléchargement d'attestations.
    * *Modélisation :* Cloudlets courts (3 000 à 5 000 MI, 1 cœur). 125 requêtes injectées immédiatement à $t=0$ pour saturer les 5 VMs de base, suivies de 6 vagues successives de 80 requêtes (480 au total) injectées au fil de l'eau pour alimenter les VMs au fur et à mesure de leur création.
  * **Bases de Données Apogée (PaaS/IaaS - 50 Cloudlets) :** Écritures transactionnelles critiques, verrous d'inscription et enregistrement des choix de filières.
    * *Modélisation :* 50 Cloudlets de 20 000 à 30 000 MI (2 cœurs), avec 20 requêtes initiales puis 5 requêtes par vague, absorbées à 100 % par les 6 VMs privées locales (souveraineté des données).
  * **Trafic de fond universitaire (45 Cloudlets) :** La vie courante du campus se poursuit pendant les inscriptions :
    * *Moodle :* 20 Cloudlets (50 000 à 80 000 MI).
    * *Messagerie :* 15 Cloudlets (10 000 à 12 000 MI).
    * *DSI & Recherche :* 5 Cloudlets (300 000 MI).
    * *Quiz & Évaluations :* 5 Cloudlets (1 000 à 2 000 MI - devoirs de routine).

### C. Stratégie d'allocation dans CloudSim Plus

Pour traduire cette modélisation dans le code Java (Partie 4) :
1. **Modèle d'utilisation (UtilizationModel) :** Nous utiliserons un modèle stochastique (`UtilizationModelStochastic`) pour la période normale afin de simuler des variations aléatoires, et un modèle d'utilisation complète (`UtilizationModelFull`) pour la période de pointe afin de saturer rapidement les CPU.
2. **Ordonnancement (CloudletScheduler) :** Les requêtes d'examens en ligne et d'inscriptions devront être traitées en priorité. L'utilisation du `CloudletSchedulerTimeShared` permettra aux VMs de traiter plusieurs petites requêtes QCM simultanément en partageant le temps CPU, garantissant une meilleure réactivité globale.
3. **Allocation des cœurs (pesNumber) :** Dans une architecture web réelle, une requête utilisateur (clic QCM, soumission ENT, lecture d'email) est traitée par un thread serveur sur **1 seul cœur (1 vCPU)**. Une VM multi-cœurs (4 vCPUs) peut ainsi traiter 4 requêtes indépendantes en parallèle. Seules les simulations scientifiques de laboratoire (DSI & Recherche) mobilisent **4 cœurs** (calcul parallèle OpenMP), et les transactions de bases de données mobilisent **2 cœurs**.