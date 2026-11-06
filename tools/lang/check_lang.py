#!/usr/bin/env python3
"""Comprueba claves, orden, duplicados y marcadores de los tres idiomas nuevos de T8.

Uso: python tools/lang/check_lang.py [--lang-dir carpeta]
No evalúa la calidad lingüística ni considera el inglés provisional una traducción.
"""
import argparse
from collections import Counter
import json
from pathlib import Path
import re

LOCALES = ('pt_br', 'fr_fr', 'de_de')
LANG_DIR = Path(__file__).resolve().parents[2] / 'src/main/resources/assets/aptitudes/lang'
# Sin bandera de espacio: en los textos del juego «30% de vida» contiene un % literal.
FORMAT = re.compile(r'%(?:[1-9]\d*\$)?[-#+0,(<]*\d*(?:\.\d+)?[bBhHsScCdoxXeEfgGaAn%]')
INDEXED = re.compile(r'%[1-9]\d*\$')


def markers(text):
    """Conserva el token completo (índice, anchura, precisión, %% o % literal)."""
    result = []
    offset = 0
    while offset < len(text):
        if text[offset] != '%':
            offset += 1
            continue
        match = FORMAT.match(text, offset)
        result.append(match.group() if match else '%')
        offset = match.end() if match else offset + 1
    return result


def format_signature(text):
    tokens = markers(text)
    # Los argumentos sin índice dependen del orden; los indexados pueden reordenarse al traducir.
    ordered = tuple(t for t in tokens if t not in ('%', '%%', '%n') and not INDEXED.match(t))
    return Counter(tokens), ordered


def unique_pairs(pairs):
    out = {}
    for key, value in pairs:
        if key in out:
            raise ValueError(f'Clave duplicada: {key}')
        out[key] = value
    return out


def load(path):
    data = json.loads(Path(path).read_text(encoding='utf-8'), object_pairs_hook=unique_pairs)
    if not isinstance(data, dict):
        raise ValueError('El idioma debe ser un objeto JSON')
    return data


def validate(reference, translated):
    errors = []
    missing, extra = set(reference) - set(translated), set(translated) - set(reference)
    if missing:
        errors.append('Faltan claves: ' + ', '.join(sorted(missing)))
    if extra:
        errors.append('Sobran claves: ' + ', '.join(sorted(extra)))
    if not missing and not extra and list(reference) != list(translated):
        errors.append('El orden de las claves difiere de en_us')
    for key in reference.keys() & translated.keys():
        source, target = reference[key], translated[key]
        if not isinstance(source, str) or not isinstance(target, str):
            errors.append(f'{key}: el valor debe ser texto')
        elif format_signature(source) != format_signature(target):
            errors.append(f'{key}: marcadores distintos: {markers(source)!r} / {markers(target)!r}')
    return sorted(errors)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--lang-dir', type=Path, default=LANG_DIR)
    args = parser.parse_args()
    try:
        reference = load(args.lang_dir / 'en_us.json')
        failed = False
        for locale in LOCALES:
            path = args.lang_dir / f'{locale}.json'
            errors = validate(reference, load(path))
            if errors:
                failed = True
                print(f'{locale}: ERROR\n' + '\n'.join(errors))
            else:
                print(f'{locale}: {len(reference)} claves, orden y marcadores correctos')
        return int(failed)
    except (OSError, ValueError) as error:
        print(f'ERROR: {error}')
        return 1


if __name__ == '__main__':
    raise SystemExit(main())
