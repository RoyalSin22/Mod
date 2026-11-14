#!/usr/bin/env python3
"""Fase 2a del lore: escribe las claves lore.aptitudes.* en los archivos de idioma.

- en_us y es_es llevan el texto propio; es_mx/es_ar/es_cl/es_ec/es_uy/es_ve son copias de es_es.
- pt_br, fr_fr y de_de llevan el texto en inglés como respaldo hasta que se traduzcan
  (así check_lang.py sigue exigiendo las mismas claves y el mismo orden que en_us).
- Las claves nuevas se añaden al final y no se reformatea nada más.

Ejecutar después de los demás generadores. --check comprueba sin escribir.
"""
import argparse
import json

from check_lang import LANG_DIR, LOCALES, load, validate
from generator_cli import SPANISH_VARIANTS
from lore_catalog import ENTRIES

PREFIX = 'lore.aptitudes.'
SOURCE_LOCALES = ('en_us', 'es_es', *LOCALES)


def managed(key):
    return key.startswith(PREFIX)


def key_for(entry_id, part):
    """lore.cosmology.echoes -> lore.aptitudes.cosmology.echoes.title"""
    if not entry_id.startswith('lore.'):
        raise ValueError(f'Id sin prefijo lore.: {entry_id}')
    return f'{PREFIX}{entry_id[len("lore."):]}.{part}'


def lore_keys(language):
    """Pares clave/texto en el orden del catálogo. language: 'es' o 'en'."""
    out = {}
    for entry_id, texts in ENTRIES.items():
        title, body = texts[language]
        if title is not None:
            out[key_for(entry_id, 'title')] = title
        out[key_for(entry_id, 'body')] = body
    return out


def render(locale, current):
    """Idioma con las claves gestionadas al final y todo lo demás intacto."""
    language = 'es' if locale.startswith('es') else 'en'
    out = {key: value for key, value in current.items() if not managed(key)}
    out.update(lore_keys(language))
    return out


def encode(data):
    return (json.dumps(data, ensure_ascii=False, indent=2) + '\n').encode('utf-8')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    try:
        reference = render('en_us', load(LANG_DIR / 'en_us.json'))
        failed = False
        for locale in SOURCE_LOCALES:
            path = LANG_DIR / f'{locale}.json'
            data = render(locale, load(path))
            if list(data) != list(reference):
                raise ValueError(f'{locale}: claves u orden distintos del inglés')
            if locale in LOCALES:
                errors = validate(reference, data)
                if errors:
                    raise ValueError('\n'.join(errors))
            rendered = encode(data)
            targets = [path]
            if locale == 'es_es':
                targets.extend(LANG_DIR / f'{variant}.json' for variant in SPANISH_VARIANTS)
            for target in targets:
                if args.check:
                    if target.read_bytes() != rendered:
                        print(f'{target.stem}: lore desactualizado')
                        failed = True
                else:
                    target.write_bytes(rendered)
            print(f'{locale}: lore comprobado' if args.check else f'{locale}: lore generado')
        return int(failed)
    except (OSError, ValueError) as error:
        print(f'ERROR: {error}')
        return 1


if __name__ == '__main__':
    raise SystemExit(main())
