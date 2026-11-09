#!/usr/bin/env python3
"""Genera los avances (logros) de la pestaña «Aptitudes» (v31).

Uso:  python3 tools/advancements/gen_advancements.py src/main/resources/data/aptitudes/advancement

La lista debe coincidir con el enum dev.aptitudes.achievement.Achievement (lo comprueba
AchievementCatalogTest). Los ids no se renombran ni se borran: forman parte del progreso guardado.
Salvo la raíz (minecraft:tick, para que la pestaña aparezca al entrar), cada logro tiene un único
criterio "done" de tipo minecraft:impossible que concede el servidor.
"""
import json
import os
import sys

BACKGROUND = "minecraft:textures/block/deepslate_tiles.png"

# (id, padre, marco, icono)
ACHIEVEMENTS = [
    ("root", None, "task", "aptitudes:guide_book"),
    ("rest_bonfire", "root", "task", "aptitudes:bonfire"),
    ("soul_level", "rest_bonfire", "goal", "aptitudes:soul_shard"),
    ("choose_role", "root", "task", "aptitudes:weapon_sigil"),
    ("combo_finisher", "choose_role", "task", "minecraft:iron_sword"),
    ("evolve_master", "choose_role", "goal", "aptitudes:mastery_emblem"),
    ("evolve_ultimate", "evolve_master", "challenge", "aptitudes:ultimate_core"),
    ("skill_mastery", "root", "goal", "minecraft:diamond_pickaxe"),
    ("socket_rune", "root", "task", "aptitudes:rune_force"),
    ("slay_boss", "socket_rune", "goal", "aptitudes:tower_core"),
    ("slay_herald", "slay_boss", "challenge", "aptitudes:eclipse_core"),
    ("survive_soul_fog", "root", "task", "minecraft:soul_lantern"),
    ("survive_ember_rain", "survive_soul_fog", "task", "minecraft:magma_block"),
    ("survive_eclipse", "survive_ember_rain", "challenge", "aptitudes:eclipse_fragment"),
    ("join_party", "root", "task", "minecraft:cake"),
    ("first_contract", "root", "task", "aptitudes:contract_board"),
    ("contract_veteran", "first_contract", "goal", "minecraft:emerald"),
    ("guild_master", "contract_veteran", "challenge", "aptitudes:guild_seal"),
    ("temple_opened", "slay_boss", "goal", "aptitudes:temple_key"),
    ("life_vessel", "temple_opened", "goal", "aptitudes:life_vessel"),
]


def build(aid, parent, frame, icon):
    root = parent is None
    display = {
        "icon": {"id": icon},
        "title": {"translate": f"advancement.aptitudes.{aid}.title"},
        "description": {"translate": f"advancement.aptitudes.{aid}.description"},
        "frame": frame,
        "show_toast": not root,
        "announce_to_chat": not root,
        "hidden": False,
    }
    if root:
        display["background"] = BACKGROUND
    data = {"display": display}
    if not root:
        data["parent"] = f"aptitudes:aptitudes/{parent}"
    criterion = "tick" if root else "done"
    data["criteria"] = {criterion: {"trigger": "minecraft:tick" if root else "minecraft:impossible"}}
    data["requirements"] = [[criterion]]
    return data


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(1)
    out = os.path.join(sys.argv[1], "aptitudes")
    os.makedirs(out, exist_ok=True)
    for aid, parent, frame, icon in ACHIEVEMENTS:
        path = os.path.join(out, aid + ".json")
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            f.write(json.dumps(build(aid, parent, frame, icon), ensure_ascii=False, indent=2) + "\n")
    print(f"{len(ACHIEVEMENTS)} avances en {out}")


if __name__ == "__main__":
    main()
