"""
generate_diagram.py
-------------------
Génère le diagramme d'infrastructure Cloud Hybride de l'UAE
(Université Abdelmalek Essaâdi) en layout VERTICAL.

Sortie : uae_cloud_architecture.png dans le même dossier.
Dépendance : binaire `dot` de Graphviz (sudo apt install graphviz).
"""

import subprocess
import sys
import os
import textwrap

# ─── Chemin de sortie ─────────────────────────────────────────────────────────
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_PNG  = os.path.join(SCRIPT_DIR, "uae_cloud_architecture.png")

# ─── Source DOT – layout vertical TB ──────────────────────────────────────────
DOT_SOURCE = textwrap.dedent(r"""
digraph UAE_Cloud {

    // ── Paramètres globaux ────────────────────────────────────────────────────
    graph [
        label     = "Architecture Cloud Hybride\nUniversité Abdelmalek Essaâdi (UAE) — CloudSim Plus"
        labelloc  = "t"
        fontsize  = 26
        fontname  = "Helvetica-Bold"
        fontcolor = "#1a1a2e"
        bgcolor   = "#f4f6fb"
        rankdir   = TB
        splines   = curved
        nodesep   = 0.6
        ranksep   = 1.0
        pad       = 0.8
        size      = "14,22!"
    ]
    node [fontname="Helvetica" fontsize=11 style="filled,rounded" shape=box]
    edge [fontname="Helvetica" fontsize=10 color="#555555"]

    // ══════════════════════════════════════════════════════════════════════════
    // LAYER 0 – UTILISATEURS
    // ══════════════════════════════════════════════════════════════════════════
    subgraph cluster_users {
        label      = "Utilisateurs  —  130 750 étudiants · 1 357 enseignants · 600 admins"
        fontsize   = 13
        fontname   = "Helvetica-Bold"
        fontcolor  = "#ffffff"
        style      = "filled,rounded"
        fillcolor  = "#2c3e50"
        color      = "#1a252f"
        penwidth   = 2.5

        TANGER  [label="Zone Tanger\nFST · ENCG · ENSA · FMPT\nFSJP · FEG · ESRFT"
                 fillcolor="#3498db" fontcolor="#ffffff" shape=cylinder]
        TETOUAN [label="Zone Tétouan / Martil\nFac. Sciences · FLSH · FSJES\nENS · ENSA · EST · FOD"
                 fillcolor="#3498db" fontcolor="#ffffff" shape=cylinder]
        AUTRES  [label="Al Hoceïma (ENSA · FST · FSJES · EST · ENCG)\nLarache (FPL) · Ksar El-Kébir (FPK)"
                 fillcolor="#2980b9" fontcolor="#ffffff" shape=cylinder]

        TANGER -> TETOUAN -> AUTRES [style=invis]
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LAYER 1 – LOAD BALANCER
    // ══════════════════════════════════════════════════════════════════════════
    LB [
        label     = "Load Balancer / API Gateway\nRépartition de charge · Fail-over automatique"
        fillcolor = "#e67e22"
        fontcolor = "#ffffff"
        shape     = diamond
        penwidth  = 2.5
        fontsize  = 13
        style     = "filled"
    ]

    TANGER  -> LB [label="HTTPS" penwidth=2   color="#2874a6" fontcolor="#2874a6"]
    TETOUAN -> LB [label="HTTPS" penwidth=2   color="#2874a6" fontcolor="#2874a6"]
    AUTRES  -> LB [label="HTTPS" penwidth=1.5 color="#2874a6" fontcolor="#2874a6"]

    // ══════════════════════════════════════════════════════════════════════════
    // LAYER 2 – CLOUD PRIVÉ
    // Capacité totale : 10 hôtes × 32c/128GB = 320 cœurs / 1280 GB RAM
    // Période normale : 184 cœurs / 584 GB utilisés
    // Seuil bursting  : 85 % = 272 cœurs max autorisés
    // ══════════════════════════════════════════════════════════════════════════
    subgraph cluster_private {
        label      = "Cloud Privé  (IaaS · PaaS · SaaS)  —  320 cœurs · 1 280 GB RAM  |  Seuil bursting : 85 % (272 cœurs)"
        fontsize   = 14
        fontname   = "Helvetica-Bold"
        fontcolor  = "#154360"
        style      = "filled,rounded"
        fillcolor  = "#dbeafe"
        color      = "#1a5276"
        penwidth   = 3

        DC1_ANCHOR [style=invis width=0 height=0 label=""]
        DC2_ANCHOR [style=invis width=0 height=0 label=""]
        { rank=same; DC1_ANCHOR; DC2_ANCHOR }
        DC1_ANCHOR -> DC2_ANCHOR [style=invis]

        // ── Datacenter 1 – Tanger ─────────────────────────────────────────
        subgraph cluster_dc1 {
            label      = "Datacenter 1 – Tanger"
            fontsize   = 14
            fontname   = "Helvetica-Bold"
            fontcolor  = "#1a5276"
            style      = "filled,rounded"
            fillcolor  = "#c3dafe"
            color      = "#1a5276"
            penwidth   = 2.5

            subgraph cluster_dc1_hosts {
                label    = "5 Hôtes physiques  (32 vCPU · 128 GB RAM · 10 TB · 10 Gbps chacun)"
                fontsize = 9
                style    = "dashed"
                color    = "#5d6d7e"

                H1 [label="Hôte 1\n32c / 128GB / 10TB" fillcolor="#aed6f1" shape=box3d style=filled]
                H2 [label="Hôte 2\n32c / 128GB / 10TB" fillcolor="#aed6f1" shape=box3d style=filled]
                H3 [label="Hôte 3\n32c / 128GB / 10TB" fillcolor="#aed6f1" shape=box3d style=filled]
                H4 [label="Hôte 4\n32c / 128GB / 10TB" fillcolor="#aed6f1" shape=box3d style=filled]
                H5 [label="Hôte 5\n32c / 128GB / 10TB" fillcolor="#aed6f1" shape=box3d style=filled]
                H1 -> H2 -> H3 -> H4 -> H5 [style=invis]
            }

            subgraph cluster_dc1_vms {
                label    = "Machines Virtuelles (SaaS / IaaS)"
                fontsize = 9
                style    = "dashed"
                color    = "#2874a6"

                VM1_DB     [label="Bases de données (VM 1-3)\nPaaS/IaaS — 3 VMs\n8 cœurs / 32 GB RAM"
                            fillcolor="#34d399" color="#059669" penwidth=2 style="filled,rounded"]
                VM1_ELEARN [label="E-learning (VM 4-11)\nSaaS · Moodle — 8 VMs\n4 cœurs / 16 GB RAM"
                            fillcolor="#93c6f0" style="filled,rounded"]
                VM1_ENT    [label="ENT / Inscriptions Apogée\nSaaS · 5 → 53 VMs  AUTO-SCALE\n4 cœurs / 8 GB RAM"
                            fillcolor="#fde68a" color="#d97706" penwidth=2 style="filled,rounded"]
                VM1_EXAMS  [label="Examens en ligne\nSaaS · 2 → 30 VMs  AUTO-SCALE\n4 cœurs / 8 GB RAM"
                            fillcolor="#fde68a" color="#d97706" penwidth=2 style="filled,rounded"]
                VM1_DSI    [label="DSI / Recherche\nIaaS · 5 VMs — 4 cœurs / 8 GB RAM\nContrôle OS total"
                            fillcolor="#c4b5fd" style="filled,rounded"]
                VM1_MAIL   [label="Messagerie & Bibliothèque\nSaaS · 2 VMs · 2 cœurs / 4 GB RAM"
                            fillcolor="#93c6f0" style="filled,rounded"]

                VM1_DB -> VM1_ELEARN -> VM1_ENT -> VM1_EXAMS -> VM1_DSI -> VM1_MAIL [style=invis]
            }
        }

        // ── Datacenter 2 – Tétouan ────────────────────────────────────────
        subgraph cluster_dc2 {
            label      = "Datacenter 2 – Tétouan / Martil"
            fontsize   = 14
            fontname   = "Helvetica-Bold"
            fontcolor  = "#1a5276"
            style      = "filled,rounded"
            fillcolor  = "#c3dafe"
            color      = "#1a5276"
            penwidth   = 2.5

            subgraph cluster_dc2_hosts {
                label    = "5 Hôtes physiques  (32 vCPU · 128 GB RAM · 10 TB · 10 Gbps chacun)"
                fontsize = 9
                style    = "dashed"
                color    = "#5d6d7e"

                H6  [label="Hôte 6\n32c / 128GB / 10TB"  fillcolor="#aed6f1" shape=box3d style=filled]
                H7  [label="Hôte 7\n32c / 128GB / 10TB"  fillcolor="#aed6f1" shape=box3d style=filled]
                H8  [label="Hôte 8\n32c / 128GB / 10TB"  fillcolor="#aed6f1" shape=box3d style=filled]
                H9  [label="Hôte 9\n32c / 128GB / 10TB"  fillcolor="#aed6f1" shape=box3d style=filled]
                H10 [label="Hôte 10\n32c / 128GB / 10TB" fillcolor="#aed6f1" shape=box3d style=filled]
                H6 -> H7 -> H8 -> H9 -> H10 [style=invis]
            }

            subgraph cluster_dc2_vms {
                label    = "Machines Virtuelles (SaaS / IaaS)"
                fontsize = 9
                style    = "dashed"
                color    = "#2874a6"

                VM2_DB     [label="Bases de données (VM 4-6)\nPaaS/IaaS — 3 VMs\n8 cœurs / 32 GB RAM"
                            fillcolor="#34d399" color="#059669" penwidth=2 style="filled,rounded"]
                VM2_ELEARN [label="E-learning (VM 12-18)\nSaaS · Moodle — 7 VMs\n4 cœurs / 16 GB RAM"
                            fillcolor="#93c6f0" style="filled,rounded"]
                VM2_ENT    [label="ENT / Inscriptions Apogée\nSaaS · 5 → 53 VMs  AUTO-SCALE\n4 cœurs / 8 GB RAM"
                            fillcolor="#fde68a" color="#d97706" penwidth=2 style="filled,rounded"]
                VM2_EXAMS  [label="Examens en ligne\nSaaS · 2 → 30 VMs  AUTO-SCALE\n4 cœurs / 8 GB RAM"
                            fillcolor="#fde68a" color="#d97706" penwidth=2 style="filled,rounded"]
                VM2_DSI    [label="DSI / Recherche\nIaaS · 5 VMs — 4 cœurs / 8 GB RAM\nContrôle OS total"
                            fillcolor="#c4b5fd" style="filled,rounded"]
                VM2_MAIL   [label="Messagerie & Bibliothèque\nSaaS · 2 VMs · 2 cœurs / 4 GB RAM"
                            fillcolor="#93c6f0" style="filled,rounded"]

                VM2_DB -> VM2_ELEARN -> VM2_ENT -> VM2_EXAMS -> VM2_DSI -> VM2_MAIL [style=invis]
            }
        }

        // Disaster Recovery
        H1 -> H6 [
            label      = "  Disaster Recovery / Failover  "
            style      = dashed
            color      = "#e74c3c"
            fontcolor  = "#c0392b"
            fontsize   = 10
            dir        = both
            penwidth   = 2.5
            constraint = false
        ]
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LAYER 3 – CLOUD PUBLIC (en bas)
    // 48 VMs déployées en pointe : 20 ENT + 28 Examens = 192 cœurs
    // Local absorbe 22 VMs (88c), cloud public prend 26 VMs (104c)
    // ══════════════════════════════════════════════════════════════════════════
    subgraph cluster_public {
        label      = "Cloud Public  —  Débordement (Cloud Bursting)\nServices élastiques : ENT & Examens  |  Local : 22 VMs (88c)  →  Public : 26 VMs (104c)"
        fontsize   = 13
        fontname   = "Helvetica-Bold"
        fontcolor  = "#7d6608"
        style      = "filled,rounded"
        fillcolor  = "#fef9c3"
        color      = "#d97706"
        penwidth   = 3

        PUB_ENT [
            label     = "VMs ENT / Inscriptions  overflow\nHorizontalVmScaling\n+26 VMs sur Cloud Public · 4c/8GB"
            fillcolor = "#fde68a"
            color     = "#d97706"
            penwidth  = 2
            style     = "filled,rounded"
        ]
        PUB_EXAMS [
            label     = "VMs Examens en ligne  overflow\nHorizontalVmScaling\nDébordement sessions examens · 4c/8GB"
            fillcolor = "#fde68a"
            color     = "#d97706"
            penwidth  = 2
            style     = "filled,rounded"
        ]
        PUB_ENT -> PUB_EXAMS [style=invis]
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ARÊTES FONCTIONNELLES
    // ══════════════════════════════════════════════════════════════════════════

    LB -> DC1_ANCHOR [penwidth=2.5 color="#1a5276" fontcolor="#1a5276" label="vers DC1 – Tanger"]
    LB -> DC2_ANCHOR [penwidth=2.5 color="#1a5276" fontcolor="#1a5276" label="vers DC2 – Tétouan"]
    DC1_ANCHOR -> H1 [style=invis]
    DC2_ANCHOR -> H6 [style=invis]

    // Hôtes → VMs
    H1 -> VM1_DB     [style=dashed color="#2874a6" arrowhead=open label="virt."]
    H2 -> VM1_ELEARN [style=dashed color="#2874a6" arrowhead=open]
    H3 -> VM1_ENT    [style=dashed color="#2874a6" arrowhead=open]
    H4 -> VM1_EXAMS  [style=dashed color="#2874a6" arrowhead=open]
    H5 -> VM1_DSI    [style=dashed color="#2874a6" arrowhead=open]

    H6  -> VM2_DB     [style=dashed color="#2874a6" arrowhead=open label="virt."]
    H7  -> VM2_ELEARN [style=dashed color="#2874a6" arrowhead=open]
    H8  -> VM2_ENT    [style=dashed color="#2874a6" arrowhead=open]
    H9  -> VM2_EXAMS  [style=dashed color="#2874a6" arrowhead=open]
    H10 -> VM2_DSI    [style=dashed color="#2874a6" arrowhead=open]

    // VMs Auto-scale → Cloud Public (DB jamais sur cloud public)
    VM1_ENT   -> PUB_ENT   [label="Cloud Bursting (+26 VMs)" color="#e67e22" fontcolor="#d35400" penwidth=2.5 style=bold]
    VM2_ENT   -> PUB_ENT   [color="#e67e22" penwidth=2 style=bold]
    VM1_EXAMS -> PUB_EXAMS [label="Cloud Bursting (Examens)" color="#e67e22" fontcolor="#d35400" penwidth=2 style=dashed]
    VM2_EXAMS -> PUB_EXAMS [color="#e67e22" penwidth=2 style=dashed]

    // ══════════════════════════════════════════════════════════════════════════
    // LÉGENDE
    // ══════════════════════════════════════════════════════════════════════════
    subgraph cluster_legend {
        label      = "Légende"
        fontsize   = 11
        fontname   = "Helvetica-Bold"
        style      = "filled,rounded"
        fillcolor  = "#f1f5f9"
        color      = "#94a3b8"
        penwidth   = 1.5

        L1 [label="──→   Flux réseau HTTPS"              fillcolor="#f8fafc" shape=plaintext style=filled fontsize=10]
        L2 [label="-->   Virtualisation (Hôte → VM)"     fillcolor="#f8fafc" shape=plaintext style=filled fontsize=10]
        L3 [label="━━→   Cloud Bursting (auto-scale)"    fillcolor="#f8fafc" shape=plaintext style=filled fontsize=10]
        L4 [label="⟺    Disaster Recovery / Failover"   fillcolor="#f8fafc" shape=plaintext style=filled fontsize=10]
        L5 [label="🟡  Auto-scaling (HorizontalVmScaling)" fillcolor="#fde68a" shape=plaintext style=filled fontsize=10]
        L6 [label="🟢  Bases de données (jamais sur cloud public)" fillcolor="#34d399" shape=plaintext style=filled fontsize=10]
        L1 -> L2 -> L3 -> L4 -> L5 -> L6 [style=invis]
    }
}
""")


def build_diagram(dot_source: str, output_path: str, dpi: int = 150) -> None:
    """
    Génère un PNG depuis une source DOT en appelant le binaire `dot` de Graphviz.

    Args:
        dot_source:  Contenu DOT du graphe.
        output_path: Chemin de sortie du fichier PNG.
        dpi:         Résolution en points par pouce.
    """
    result = subprocess.run(
        ["dot", f"-Gdpi={dpi}", "-Tpng", f"-o{output_path}"],
        input=dot_source.encode("utf-8"),
        capture_output=True,
    )

    # Afficher les avertissements sans bloquer
    if result.stderr:
        for line in result.stderr.decode().splitlines():
            print(f"[graphviz] {line}", file=sys.stderr)

    result.check_returncode()


def main() -> None:
    print("=" * 60)
    print("  Génération du diagramme — UAE Cloud Architecture")
    print("  Layout : VERTICAL (portrait)")
    print("=" * 60)

    # Vérifier que `dot` est disponible
    check = subprocess.run(["which", "dot"], capture_output=True)
    if check.returncode != 0:
        print("ERREUR : Graphviz (`dot`) n'est pas installé.")
        print("         sudo apt install graphviz")
        sys.exit(1)

    version = subprocess.run(["dot", "-V"], capture_output=True).stderr.decode().strip()
    print(f"Graphviz : {version}")
    print(f"Sortie   : {OUTPUT_PNG}")
    print()

    build_diagram(DOT_SOURCE, OUTPUT_PNG, dpi=150)

    size_kb = os.path.getsize(OUTPUT_PNG) / 1024
    print(f"[OK] Image générée  ({size_kb:.1f} KB)")
    print(f"     → {OUTPUT_PNG}")


if __name__ == "__main__":
    main()
