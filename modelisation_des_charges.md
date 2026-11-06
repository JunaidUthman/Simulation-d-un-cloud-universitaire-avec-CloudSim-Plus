## 3. Modélisation des charges (Cloudlets)

Pour simuler le comportement du cloud de l'Université Abdelmalek Essaâdi (qui compte plus de 130 000 étudiants) dans CloudSim Plus, nous devons modéliser les tâches exécutées par les serveurs. Dans CloudSim, une tâche ou requête est représentée par un objet `Cloudlet`, dont la taille est mesurée en MI (Millions d'Instructions).

Nous distinguons deux scénarios d'utilisation pour notre simulation :

### A. Période Normale (Semestre classique)

Durant un semestre typique, la charge est modérée et répartie sur la journée. Les étudiants consultent des cours, tandis que les chercheurs effectuent des travaux de fond.

* **Nombre de connexions (Trafic) :** Modéré. Environ 10 000 à 15 000 requêtes concurrentes (représentant environ 10% des étudiants actifs simultanément sur les services de l'université).
* **Types de requêtes (Cloudlets) :**
  * **E-learning / Moodle (SaaS) :** Téléchargement de polycopiés (PDF) et streaming de capsules vidéo.
    * *Modélisation :* Cloudlets de grande taille (ex: 50 000 à 100 000 MI). Un Cloudlet Moodle ne représente pas un « simple clic », mais une session complète d'un étudiant (ex. consultation du cours, génération de quiz, traitement de devoirs, compression de fichiers), nécessitant beaucoup de temps de calcul (CPU) et une bande passante importante.
  * **Recherche et DSI (IaaS) :** Exécution de scripts, simulations scientifiques et compilation de code.
    * *Modélisation :* Cloudlets très longs (ex: 300 000 MI). Tâches lourdes mais peu nombreuses.
  * **Messagerie et Bibliothèque (SaaS) :** Recherches documentaires et lecture d'emails.
    * *Modélisation :* Cloudlets de taille moyenne (ex: 10 000 MI).

### B. Période de Pointe (Inscriptions et Examens en ligne)

Ce scénario modélise les goulots d'étranglement critiques : la semaine des inscriptions en septembre (sur Apogée/ENT) et les sessions d'examens en ligne.

* **Nombre de connexions (Trafic) :** Pic massif. De 80 000 à 100 000 requêtes générées de façon quasi simultanée, saturant les ressources initiales et déclenchant le débordement sur le Cloud Public (`HorizontalVmScaling`).
* **Types de requêtes (Cloudlets) :**
  * **Inscriptions & ENT (SaaS) :** Soumission de formulaires d'inscription, requêtes vers les bases de données Apogée, génération d'attestations.
    * *Modélisation :* Cloudlets très courts (ex: 2 000 à 5 000 MI) mais en quantité astronomique. La charge ne vient pas de la complexité de la tâche, mais du volume massif de requêtes simultanées.
  * **Examens en ligne (SaaS) :** Clics sur des QCM, minuteurs, validation de réponses.
    * *Modélisation :* Cloudlets extrêmement courts (ex: 1 000 à 2 000 MI). Ils exigent un temps de réponse instantané pour ne pas pénaliser les étudiants pendant l'épreuve.

### C. Stratégie d'allocation dans CloudSim Plus

Pour traduire cette modélisation dans le code Java (Partie 4) :
1. **Modèle d'utilisation (UtilizationModel) :** Nous utiliserons un modèle stochastique (`UtilizationModelStochastic`) pour la période normale afin de simuler des variations aléatoires, et un modèle d'utilisation complète (`UtilizationModelFull`) pour la période de pointe afin de saturer rapidement les CPU.
2. **Ordonnancement (CloudletScheduler) :** Les requêtes d'examens en ligne et d'inscriptions devront être traitées en priorité. L'utilisation du `CloudletSchedulerTimeShared` permettra aux VMs de traiter plusieurs petites requêtes QCM simultanément en partageant le temps CPU, garantissant une meilleure réactivité globale.