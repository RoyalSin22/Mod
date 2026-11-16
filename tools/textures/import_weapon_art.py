#!/usr/bin/env python3
"""V75: importa y valida el arte externo de las armas de evolución Maestro (encargo en docs/PROMPT_ARTE_ARMAS.md).

Importar a 64×64 (imágenes sueltas con el nombre del rol, p. ej. ``berserker.png`` o ``berserker_weapon_master.png``):
    python3 tools/textures/import_weapon_art.py entregas/*.png
Importar una hoja de varias armas SIN fiarse de una cuadrícula (detecta cada arma y las ordena de arriba abajo y de izquierda a derecha):
    python3 tools/textures/import_weapon_art.py hoja.png --sheet auto --roles berserker,paladin,lancer,swordsman,monk
(también vale una cuadrícula fija: --sheet 5x1). Si el número de armas detectadas no coincide con --roles, se detiene y no escribe nada;
--debug-split salida.png dibuja lo detectado.
Validar lo ya importado (para CI y antes de regenerar):
    python3 tools/textures/import_weapon_art.py --check
Vista previa ampliada de lo importado:
    python3 tools/textures/import_weapon_art.py --preview hoja.png

Después de importar: ``python3 tools/textures/gen_items.py src/main/resources/assets/aptitudes/textures/item`` (borra luego los tres
PNG de sellos retirados que regenera) y ``python3 tools/models/gen_weapon_models.py src/main/resources/assets/aptitudes/models/item``.
Opciones de importación: --size 64|32|16 (por defecto 64), --colors N (por defecto 48), --tolerance T (fondo, 0–255; por defecto 40), --no-fit, --flip,
--rotate 90|180|270, --dry-run (no escribe).
"""
import argparse
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import weapon_art  # noqa: E402

# Orden de los roles del mod (el mismo que ROLE_COLORS de gen_items.py).
ROLES = ['berserker', 'mage', 'assassin', 'herbalist', 'engineer', 'lancer', 'swordsman', 'paladin', 'necromancer', 'druid',
         'merchant', 'monk', 'elementalist', 'shapeshifter', 'gunslinger', 'weapon_summoner', 'hunter', 'bard', 'warlock',
         'tamer']


def role_of(stem):
    stem = stem.lower()
    if stem.endswith(weapon_art.SUFFIX):
        stem = stem[: -len(weapon_art.SUFFIX)]
    return stem if stem in ROLES else None


def split_sheet(img, cols, rows):
    w, h = img.size
    cw, ch = w // cols, h // rows
    if cw < 16 or ch < 16:
        raise SystemExit(f'La hoja de {w}×{h} es demasiado pequeña para {cols}×{rows} armas')
    return [img.crop((c * cw, r * ch, (c + 1) * cw, (r + 1) * ch)) for r in range(rows) for c in range(cols)]


def debug_split(source, cells, out):
    """Muestra las armas detectadas, numeradas en el orden en que se asignarán a los roles."""
    from PIL import ImageDraw
    h = 160
    sheet = Image.new('RGBA', (max(1, len(cells)) * h, h), (58, 60, 72, 255))
    d = ImageDraw.Draw(sheet)
    for i, c in enumerate(cells):
        c = c.copy()
        c.thumbnail((h - 8, h - 24))
        sheet.paste(c, (i * h + 4, 20), c)
        d.text((i * h + 6, 4), f'#{i + 1}', fill=(255, 255, 255, 255))
    sheet.save(out)
    print(f'Detectadas {len(cells)} armas; vista en {out}')


def save(role, img, dry):
    errors, warnings = weapon_art.validate(img)
    name = role + weapon_art.SUFFIX
    status = 'ERROR' if errors else ('aviso' if warnings else 'ok')
    print(f'  {name}: {status}' + ''.join(f'\n    - {m}' for m in errors + warnings))
    if not errors and not dry:
        weapon_art.SOURCE.mkdir(parents=True, exist_ok=True)
        img.save(weapon_art.path_for(name))
    return not errors


def do_import(args):
    jobs = []  # (rol, imagen)
    if args.sheet:
        if len(args.inputs) != 1:
            raise SystemExit('Con --sheet se pasa una sola imagen')
        roles = args.roles.split(',') if args.roles else None
        for r in roles or []:
            if r not in ROLES:
                raise SystemExit(f'Rol desconocido en --roles: {r}')
        if args.sheet.lower() == 'auto':
            source = Image.open(args.inputs[0])
            report = {}
            cells = weapon_art.split_auto(source, tolerance=args.tolerance, report=report)
            if args.debug_split:
                debug_split(source, cells, args.debug_split)
            if roles is None:
                raise SystemExit('Con --sheet auto indica los roles en orden con --roles')
            if len(cells) != len(roles):
                raise SystemExit(f'Se detectaron {len(cells)} armas y --roles tiene {len(roles)}. No se ha escrito nada: '
                                 'revisa la imagen (¿falta alguna arma, se tocan dos o hay una pieza suelta?) o usa --debug-split para verla.'
                                 + (f' Se descartaron {report["edge"]} piezas pegadas al borde (armas cortadas o restos del marco).' if report.get('edge') else ''))
        else:
            cols, rows = (int(v) for v in args.sheet.lower().split('x'))
            roles = roles or ROLES[: cols * rows]
            cells = split_sheet(Image.open(args.inputs[0]), cols, rows)
        if len(roles) > len(cells):
            raise SystemExit(f'--roles tiene {len(roles)} roles y la hoja solo {len(cells)} celdas')
        jobs = list(zip(roles, cells))
    else:
        for item in args.inputs:
            role = role_of(Path(item).stem)
            if role is None:
                print(f'  {item}: nombre sin rol reconocido (usa <rol>.png o <rol>_weapon_master.png); se omite')
                continue
            jobs.append((role, Image.open(item)))
    ok = True
    for role, image in jobs:
        shape = weapon_art.shape_problem(image, args.tolerance)
        if shape:
            print(f'  {role}{weapon_art.SUFFIX}: ERROR\n    - {shape}')
            ok = False
            continue
        try:
            norm = weapon_art.normalize(image, colors=args.colors, tolerance=args.tolerance, fit=not args.no_fit,
                                        flip=args.flip, rotate=args.rotate, size=args.size)
        except ValueError as e:
            print(f'  {role}{weapon_art.SUFFIX}: ERROR\n    - {e}')
            ok = False
            continue
        ok = save(role, norm, args.dry_run) and ok
    print(f'{len(jobs)} imágenes procesadas' + (' (simulación)' if args.dry_run else ''))
    return ok


def do_check():
    found = [r for r in ROLES if weapon_art.path_for(r + weapon_art.SUFFIX).is_file()]
    extra = [p.name for p in weapon_art.SOURCE.glob('*.png') if role_of(p.stem) is None] if weapon_art.SOURCE.is_dir() else []
    ok = True
    for r in found:
        errors, warnings = weapon_art.validate(Image.open(weapon_art.path_for(r + weapon_art.SUFFIX)))
        if errors:
            ok = False
        if errors or warnings:
            print(f'  {r}{weapon_art.SUFFIX}: ' + '; '.join(errors + warnings))
    for name in extra:
        print(f'  {name}: nombre sin rol reconocido')
        ok = False
    print(f'{len(found)} de {len(ROLES)} armas con arte externo ({len(ROLES) - len(found)} usan el diseño por plantilla)')
    return ok


def do_preview(out):
    cell, cols = 128, 10
    names = [r for r in ROLES if weapon_art.path_for(r + weapon_art.SUFFIX).is_file()]
    if not names:
        raise SystemExit('No hay arte importado que mostrar')
    rows = (len(names) + cols - 1) // cols
    sheet = Image.new('RGBA', (cols * cell, rows * cell), (58, 60, 72, 255))
    for i, r in enumerate(names):
        big = Image.open(weapon_art.path_for(r + weapon_art.SUFFIX)).convert('RGBA').resize((cell, cell), Image.NEAREST)
        sheet.paste(big, ((i % cols) * cell, (i // cols) * cell), big)
    sheet.save(out)
    print(f'Vista previa de {len(names)} armas en {out}')


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument('inputs', nargs='*')
    p.add_argument('--sheet', metavar='CxR')
    p.add_argument('--roles')
    p.add_argument('--debug-split', metavar='SALIDA.png', help='con --sheet auto: guarda la imagen con las armas detectadas numeradas')
    p.add_argument('--colors', type=int, default=48)
    p.add_argument('--size', type=int, default=weapon_art.DEFAULT_SIZE, choices=weapon_art.SIZES, help='densidad final: 64 (por defecto), 32 o 16')
    p.add_argument('--tolerance', type=int, default=40)
    p.add_argument('--no-fit', action='store_true')
    p.add_argument('--flip', action='store_true')
    p.add_argument('--rotate', type=int, default=0, choices=(0, 90, 180, 270))
    p.add_argument('--dry-run', action='store_true')
    p.add_argument('--check', action='store_true')
    p.add_argument('--preview', metavar='SALIDA.png')
    args = p.parse_args()
    if args.check:
        sys.exit(0 if do_check() else 1)
    if args.preview:
        do_preview(args.preview)
        return
    if not args.inputs:
        p.print_help()
        sys.exit(1)
    sys.exit(0 if do_import(args) else 1)


if __name__ == '__main__':
    main()
