#!/usr/bin/env python3
"""T8 fase 3: nodos base, evolución y sendas para pt_br/fr_fr/de_de.

Los textos evolucionados y de senda proceden de sus generadores originales.
Exige cobertura completa sin cambiar interfaz, objetos, grimorio ni idiomas previos.
"""
import argparse
import json
import re

from check_lang import LANG_DIR, LOCALES, load, validate
from gen_evolved_lang import build as evolved
from gen_path_lang import build as paths
from node_catalog import TEXT

GENERATED_NODE = re.compile(r'_(?:[abc](?:[1-7]|5[xy])|[mu][1-5])(?:\.desc)?$')


def node_key(key):
    return key.startswith(('node.aptitudes.', 'path.aptitudes.'))


def base_key(key):
    return key.startswith('node.aptitudes.') and not GENERATED_NODE.search(key)


def build(reference, locale, previous):
    translated = {}
    for key, source in reference.items():
        if base_key(key):
            if source not in TEXT:
                raise ValueError(f'Nodo base sin traducir: {key}: {source}')
            translated[key] = TEXT[source][locale]
    for generated in (evolved(locale), paths(locale, reference)):
        for key, value in generated.items():
            if node_key(key):
                if key in translated:
                    raise ValueError(f'Nodo duplicado: {key}')
                translated[key] = value
    expected = {key for key in reference if node_key(key)}
    if set(translated) != expected:
        raise ValueError(f'Cobertura de nodos: faltan {sorted(expected - translated.keys())}; '
                         f'sobran {sorted(translated.keys() - expected)}')
    if any(not isinstance(value, str) or not value.strip() for value in translated.values()):
        raise ValueError(f'{locale}: nodo vacío o inválido')
    out = {key: translated[key] if node_key(key) else previous.get(key, value)
           for key, value in reference.items()}
    errors = validate(reference, out)
    if errors:
        raise ValueError('\n'.join(errors))
    return out


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    try:
        reference = load(LANG_DIR / 'en_us.json')
        failed = False
        for locale in LOCALES:
            path = LANG_DIR / f'{locale}.json'
            out = build(reference, locale, load(path))
            rendered = json.dumps(out, ensure_ascii=False, indent=2) + '\n'
            if args.check:
                if path.read_bytes() != rendered.encode('utf-8'):
                    print(f'{locale}: nodos desactualizados')
                    failed = True
            else:
                path.write_text(rendered, encoding='utf-8', newline='\n')
            print(f'{locale}: {sum(node_key(k) for k in reference)} claves de nodos traducidas')
        return int(failed)
    except (OSError, ValueError) as error:
        print(f'ERROR: {error}')
        return 1


if __name__ == '__main__':
    raise SystemExit(main())
