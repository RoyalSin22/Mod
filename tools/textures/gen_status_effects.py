#!/usr/bin/env python3
"""Iconos de alquimia de 18×18 con las rampas y contornos del generador de objetos.

Uso: python tools/textures/gen_status_effects.py <textures/mob_effect> [--check] [--preview hoja.png]
"""
import argparse
from pathlib import Path

from PIL import Image

from gen_items import render


ICONS = {
    'mana': ([
        "................",
        ".......A........",
        "......AaA.......",
        "......AaA.......",
        ".....AaaAA......",
        "....AAaaAAA.....",
        "....AaaaAAA.....",
        "...AAaaaAAAA....",
        "...AaaaaAAAA....",
        "...AaaaAAAAA....",
        "...AaaAAAAAA....",
        "....AAAAAAA.....",
        ".....AAAAA......",
        "................",
        "................",
        "................",
    ], {'A': '#49a6e8'}),
    'frenzy': ([
        "................",
        "..........AA....",
        ".........AaA....",
        "........AaaA....",
        ".......AaaA.....",
        "......AaaA......",
        ".....AaaaAAAA...",
        "....AaaaaAAA....",
        ".......AAAA.....",
        "......AAAA......",
        ".....AAAA.......",
        "....AAAA........",
        "...AAA..........",
        "..AA............",
        "................",
        "................",
    ], {'A': '#d45d46'}),
    'eclipse_veil': ([
        "................",
        ".....GGGGGG.....",
        "...GGGAAAAGGG...",
        "..GGAAAAAAAAGG..",
        "..GAaAKKKKAAAG..",
        ".GGaAKKKKKKAAGG.",
        ".GAaAKKKKKKAAAG.",
        ".GAAAKKKKKKAAAG.",
        ".GAAAKKKKKKAAAG.",
        "..GAAAKKKKAAAG..",
        "..GGAAAAAAAAGG..",
        "...GGAAAAAAGG...",
        "....GGAAAAGG....",
        ".....GGGGGG.....",
        ".......GG.......",
        "................",
    ], {'A': '#9174c9', 'G': '#dfb958', 'K': '#242032'}),
}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('folder', type=Path)
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--preview', type=Path)
    args = parser.parse_args()
    rendered = {}
    for name, (template, colors) in ICONS.items():
        assert all(len(row) == 16 for row in template), name
        icon = Image.new('RGBA', (18, 18))
        icon.paste(render(template, colors), (1, 1))
        rendered[name] = icon
        path = args.folder / f'{name}.png'
        if args.check:
            with Image.open(path) as saved:
                if saved.size != icon.size or saved.convert('RGBA').tobytes() != icon.tobytes():
                    raise SystemExit(f'Icono diferente: {path}')
        else:
            args.folder.mkdir(parents=True, exist_ok=True)
            icon.save(path)
    if args.preview:
        sheet = Image.new('RGBA', (18 * 8 * len(rendered), 18 * 8), '#353442')
        for index, icon in enumerate(rendered.values()):
            sheet.alpha_composite(icon.resize((144, 144), Image.Resampling.NEAREST), (144 * index, 0))
        sheet.save(args.preview)
    print(f'{len(rendered)} iconos {"verificados" if args.check else "generados"}')


if __name__ == '__main__':
    main()
