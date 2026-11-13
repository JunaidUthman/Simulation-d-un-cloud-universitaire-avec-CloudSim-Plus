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
Le cloud de l'université doit être capable de desservir de multiples sites répartis sur une vaste zone géographique (la région Tanger-Tétouan-Al Hoceïma). L'université regroupe **21 établissements universitaires** répartis dans **5 villes** principales :
1.  **Tanger (7 établissements) :** FST (Faculté des Sciences et Techniques), ENCG (École Nationale de Commerce et de Gestion), ENSA (École Nationale des Sciences Appliquées), FMPT (Faculté de Médecine et de Pharmacie), FSJP (Faculté des Sciences Juridiques et Politiques), FEG (Faculté d'Économie et de Gestion), ESRFT (École Supérieure Roi Fahd de Traduction).
2.  **Tétouan / Martil (7 établissements) :** Faculté des Sciences, FLSH (Faculté des Lettres et des Sciences Humaines), FSJES (Faculté des Sciences Juridiques, Économiques et Sociales), ENS (École Normale Supérieure), ENSA Tétouan, EST (École Supérieure de Technologie), FOD (Formation Ouverte et à Distance).
3.  **Al Hoceïma (5 établissements) :** ENSA, FST, FSJES, EST, ENCG.
4.  **Larache (1 établissement) :** FPL (Faculté Polydisciplinaire de Larache).
5.  **Ksar El-Kébir (1 établissement) :** FPK (Faculté Polydisciplinaire de Ksar El-Kébir).
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