## 2. Proposition d'Architecture Cloud

### A. Modèles de Déploiement et de Service

* **Déploiement : Cloud Hybride**
  * **Justification :** L'université doit conserver un Cloud Privé sur ses propres serveurs pour garantir la souveraineté et la sécurité des données sensibles (dossiers scolaires Apogée, notes, examens). Cependant, pour gérer les périodes de pointe massives (inscriptions de septembre, examens en ligne), elle s'appuie sur un Cloud Public (débordement ou *Cloud Bursting*) permettant d'ajouter des VMs automatiquement (`HorizontalVmScaling`).

* **Modèles de service :**
  * **SaaS (Software as a Service) :** Plateforme e-learning (Moodle), ENT (Espace Numérique de Travail), messagerie et examens en ligne. Les étudiants consomment directement le service.
  * **PaaS (Platform as a Service) :** Bases de données
   * **IaaS (Infrastructure as a Service)** Fourniture de ressources informatiques brutes (machines virtuelles, stockage, réseau) à la demande. Ce modèle est utilisé par la DSI (Direction des Systèmes d'Information) pour héberger des applications métiers spécifiques et par les laboratoires de recherche pour lancer des simulations scientifiques complexes nécessitant un accès de bas niveau.

### B. Nombre et Emplacement des Datacenters

* **Proposition :** 2 Datacenters physiques.
  * **Datacenter 1 (Tanger) :** Héberge et dessert les requêtes de la zone Tanger (FST, ENCG, ENSA, FMPT, FSJP, FEG, ESRFT) ainsi que les zones rattachées (Larache — FPL, Ksar El-Kébir — FPK).
  * **Datacenter 2 (Tétouan/Martil) :** Héberge et dessert la zone Tétouan (Faculté des Sciences, FLSH, FSJES, ENS, ENSA, EST, FOD) ainsi que la zone rattachée (Al Hoceïma — ENSA, FST, FSJES, EST, ENCG).
* **Justification :** Cette répartition géographique minimise la latence réseau pour les étudiants locaux. Elle assure également une haute disponibilité (*Disaster Recovery*) : si le Datacenter de Tanger tombe en panne, le trafic bascule automatiquement sur celui de Tétouan.

### C. Nombre et Capacité des Serveurs (Hôtes)

* **Proposition :** 10 Hôtes au total (5 par Datacenter).
- **Capacité par Hôte :** 32 cœurs (Pe), 128 Go de RAM, 10 To de stockage, 10 Gbps de bande passante.
* **Justification :** Dans un environnement CloudSim Plus, des hôtes puissants permettent un fort taux de consolidation. Ces spécifications sont capables d'héberger plusieurs dizaines de VMs simultanément tout en gardant des ressources pour encaisser une montée en charge soudaine avant de déclencher le débordement sur le cloud public.

### D. Machines Virtuelles (VMs) par Service

* **Serveurs de Bases de Données (PaaS/IaaS) :** 6 VMs de très grande taille (8 cœurs, 32 Go RAM).
  * *Justification :* Hébergement centralisé et hautement sécurisé de toutes les données critiques (dossiers Apogée, notes, sujets d'examens). Ces VMs restent strictement confinées dans le Cloud Privé de l'université (aucun débordement sur le cloud public) pour garantir la souveraineté et la confidentialité des informations, même lors des pics de charge.

* **E-learning & Moodle (SaaS) :** 15 VMs de grande taille (4 cœurs, 16 Go RAM).
  * *Justification :* Le streaming vidéo, le téléchargement de PDF et les requêtes de milliers d'étudiants exigent beaucoup de calcul et de mémoire.


- **Inscriptions & ENT (SaaS) :** 5 VMs de taille moyenne (4 cœurs, 8 Go RAM) en période normale, avec une règle de mise à l'échelle automatique (`HorizontalVmScaling`) pouvant monter jusqu'à 53 VMs (+48 VMs) lors du grand pic des inscriptions de rentrée.

  * *Justification :* Absorber les goulots d'étranglement massifs lors de l'ouverture des inscriptions universitaires sur Apogée.

- **Serveurs de Recherche et DSI (IaaS) :** 10 VMs de taille moyenne (4 cœurs, 8 Go RAM) allouées dynamiquement. Justification : Les chercheurs et administrateurs systèmes ont besoin d'un contrôle total sur le système d'exploitation pour configurer leurs environnements sur mesure.

* **Messagerie et Bibliothèque (SaaS) :** 4 VMs de petite taille (2 cœurs, 4 Go RAM).
  * *Justification :* Services à trafic régulier et prévisible, nécessitant moins de ressources brutes.

- **Examens en ligne (SaaS) :** 2 VMs de taille moyenne (4 cœurs, 8 Go RAM) en période de routine (les sessions d'examens et les inscriptions ne se chevauchant jamais dans le calendrier universitaire).

### E. Bilan et Répartition des Ressources (Capacity Planning & Seuil de Bursting)

L'architecture hybride adoptée repose sur une gestion stricte des ressources physiques et un mécanisme de débordement basé sur un seuil de sécurité critique.

* **Capacité du Cloud Privé :** L'infrastructure physique locale dispose d'un maximum de 320 cœurs et 1280 Go de RAM (10 hôtes de 32 cœurs/128 Go).
* **Période Normale :** En dehors des pics, les services de base consomment 184 cœurs et 584 Go de RAM (42 VMs).
* **Règle de Débordement (Seuil à 85 %) :** Pour éviter les crashs de l'hyperviseur et garantir la tolérance aux pannes (Disaster Recovery), le Cloud Privé ne doit jamais être saturé à 100 %. L'orchestrateur est configuré avec un seuil de débordement fixé à 85 % d'utilisation CPU (soit 272 cœurs maximum autorisés). Les 48 cœurs restants (15 %) constituent une marge intouchable de sécurité. L'espace disponible pour l'auto-scaling local est donc de 88 cœurs (272 - 184).
* **Période de Pointe et Cloud Bursting (Inscriptions de Septembre) :** Les inscriptions de rentrée et les examens semestriels ne pouvant jamais se chevaucher dans la réalité, la période de pointe modélisée simule l'afflux massif de la rentrée universitaire (Inscriptions & ENT). Le système d'auto-scaling (`HorizontalVmScaling`) requiert la création de 48 nouvelles VMs ENT (demande additionnelle de 192 cœurs).
* **Répartition finale :** L'orchestrateur sature d'abord le Cloud Privé jusqu'à son seuil de sécurité de 85 %, absorbant ainsi 88 cœurs (22 VMs ENT privées). Le reliquat de la demande, soit 104 cœurs (représentant 26 VMs ENT), est automatiquement déployé sur le Cloud Public. Ce mécanisme protège l'infrastructure locale de la surcharge tout en garantissant la fluidité d'inscription pour les étudiants.

