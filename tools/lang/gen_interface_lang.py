#!/usr/bin/env python3
"""T8 fase 1: genera interfaz y objetos conservando el orden exacto de en_us.

Las claves de fases posteriores se copian del inglés solo al crear el archivo;
al regenerar se conservan sus valores existentes. --check no escribe archivos.
"""
import argparse
import json

from check_lang import LANG_DIR, load, validate
from gen_evolved_lang import UI as EVOLVED_UI
from gen_path_lang import UI as PATH_UI
from ui_catalog import LOCALES, TRANSLATIONS


def phase_one(key):
    return not key.startswith(('guide.aptitudes.', 'node.aptitudes.', 'path.aptitudes.',
                                'config.aptitudes.field.'))  # v67: tooltips de configuración, es/en primero


def build(reference, locale, previous):
    index = LOCALES.index(locale)
    translated = {k: values[index] for k, values in TRANSLATIONS.items()}
    for generated in (EVOLVED_UI[locale], PATH_UI[locale]):
        duplicates = translated.keys() & generated.keys()
        if duplicates:
            raise ValueError(f'Texto generado duplicado en el catálogo: {sorted(duplicates)}')
        translated.update(generated)
    expected = {k for k in reference if phase_one(k)}
    if set(translated) != expected:
        raise ValueError(f'Cobertura de fase 1: faltan {sorted(expected - translated.keys())}; '
                         f'sobran {sorted(translated.keys() - expected)}')
    out = {k: translated[k] if phase_one(k) else previous.get(k, value) for k, value in reference.items()}
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
        reviewed = sum(phase_one(key) for key in reference)
        failed = False
        for locale in LOCALES:
            path = LANG_DIR / f'{locale}.json'
            previous = load(path) if path.exists() else {}
            data = build(reference, locale, previous)
            rendered = json.dumps(data, ensure_ascii=False, indent=2) + '\n'
            if args.check:
                if not path.exists() or path.read_bytes() != rendered.encode('utf-8'):
                    print(f'{locale}: archivo ausente o desactualizado')
                    failed = True
            else:
                path.write_text(rendered, encoding='utf-8', newline='\n')
            print(f'{locale}: {reviewed} claves de fase 1; {len(reference) - reviewed} de fases posteriores')
        return int(failed)
    except (OSError, ValueError) as error:
        print(f'ERROR: {error}')
        return 1


if __name__ == '__main__':
    raise SystemExit(main())
