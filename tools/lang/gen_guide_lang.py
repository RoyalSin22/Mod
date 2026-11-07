#!/usr/bin/env python3
"""T8 fase 2: genera el grimorio sin alterar las demás traducciones.

Ejecutar después de gen_interface_lang.py. --check comprueba sin escribir.
Las filas de evolución se obtienen de gen_evolved_lang.py.
"""
import argparse
import json

from check_lang import LANG_DIR, LOCALES, load, validate
from guide_catalog import TRANSLATIONS


def guide_key(key):
    return key.startswith('guide.aptitudes.')


def build(reference, locale, previous):
    translated = TRANSLATIONS[locale]
    expected = {key for key in reference if guide_key(key)}
    if set(translated) != expected:
        raise ValueError(f'Cobertura del grimorio: faltan {sorted(expected - translated.keys())}; '
                         f'sobran {sorted(translated.keys() - expected)}')
    if any(not isinstance(value, str) or not value.strip() for value in translated.values()):
        raise ValueError(f'{locale}: texto vacío o inválido en el grimorio')
    out = {key: translated[key] if guide_key(key) else previous.get(key, value)
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
        reviewed = sum(guide_key(key) for key in reference)
        failed = False
        for locale in LOCALES:
            path = LANG_DIR / f'{locale}.json'
            data = build(reference, locale, load(path))
            rendered = json.dumps(data, ensure_ascii=False, indent=2) + '\n'
            if args.check:
                if path.read_bytes() != rendered.encode('utf-8'):
                    print(f'{locale}: grimorio desactualizado')
                    failed = True
            else:
                path.write_text(rendered, encoding='utf-8', newline='\n')
            print(f'{locale}: {reviewed} claves del grimorio; las demás se conservan')
        return int(failed)
    except (OSError, ValueError) as error:
        print(f'ERROR: {error}')
        return 1


if __name__ == '__main__':
    raise SystemExit(main())
