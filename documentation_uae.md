# Partie 1 : Simulation d'un cloud universitaire avec CloudSim Plus

## 1. Documentation sur l'Université Abdelmalek Essaâdi (UAE)

Pour concevoir une architecture Cloud Computing adaptée aux besoins réels de l'Université Abdelmalek Essaâdi (UAE), il est primordial de comprendre l'envergure de l'institution, sa répartition géographique et les services numériques qu'elle propose à ses usagers.

### A. Chiffres clés (Ressources Humaines et Étudiants)
L'UAE est l'une des plus grandes universités du Maroc. Pour dimensionner correctement notre infrastructure cloud (nombre d'utilisateurs potentiels), nous nous basons sur les effectifs suivants :
*   **Étudiants :** 130 750 étudiants inscrits dans les différentes filières.
*   **Corps enseignant :** 1 357 enseignants-chercheurs.
*   **Corps administratif et technique :** Environ 600 membres du personnel.
> **Source :** Portail officiel de l'Université Abdelmalek Essaâdi - Rubrique "Établissements" (www.uae.ac.ma).


### B. Répartition géographique et Établissements
Le cloud de l'université doit être capable de desservir de multiples sites répartis sur une vaste zone géographique (la région Tanger-Tétouan-Al Hoceïma). L'université regroupe environ 18 établissements universitaires répartis dans 5 villes principales :
1.  **Tanger :** FST (Faculté des Sciences et Techniques), ENCG, ENSA, FSJES, FMP (Médecine), FMD (Dentaire).
2.  **Tétouan / Martil :** Faculté des Sciences, FLSH (Lettres), ENS, ENSA Tétouan.
3.  **Larache :** Faculté Polydisciplinaire (FPL).
4.  **Al Hoceïma :** ENSA, FST, FSJES.
5.  **Ksar El-Kébir :** Faculté Polydisciplinaire.
> **Source :** Portail officiel de l'Université Abdelmalek Essaâdi - Rubrique "Établissements" (www.uae.ac.ma).

### C. Services Numériques (Utilisés et Potentiels)
Pour identifier les types de charges (Cloudlets) et les configurations des machines virtuelles nécessaires dans notre simulation CloudSim Plus, voici les services numériques de l'université :

*   **Services de Scolarité et Inscriptions (SaaS/Base de données) :** Utilisation du système d'information "Apogée" et de l'Espace Numérique de Travail (ENT) pour les inscriptions, le suivi des notes et la demande de documents (attestations). *Ces services connaissent des pics de charge extrêmes en septembre/octobre et lors de l'affichage des résultats.*
*   **Plateforme E-learning (SaaS) :** Plateforme Moodle (e-learning.uae.ac.ma) utilisée pour le dépôt des cours, les travaux dirigés et les examens à distance. *Gourmand en bande passante et en stockage.*
*   **Services de communication (SaaS) :** Messagerie institutionnelle (@etu.uae.ac.ma)
*   **Bibliothèque Numérique (SaaS/PaaS) :** Accès aux bases de données scientifiques et thèses en ligne pour les chercheurs.
* **Examens en ligne et Évaluations (Service potentiel) :** Mise en place d'une plateforme sécurisée pour le passage de tests et QCM à distance. Ce service critique nécessite une haute disponibilité et génère des pics de charge très intenses et de courte durée lors des sessions d'examens, nécessitant un dimensionnement élastique du cloud.
* **Environnements virtuels de TP (IaaS - Potentiel) :** Mise à disposition de machines virtuelles pour les étudiants des écoles d'ingénieurs (ENSA, FST) pour leurs travaux pratiques en réseaux, cloud ou développement, nécessitant des ressources à la demande.
> **Source :** Déductions basées sur les services accessibles via l'ENT étudiant de l'UAE (ent.uae.ac.ma) et les standards des universités marocaines.