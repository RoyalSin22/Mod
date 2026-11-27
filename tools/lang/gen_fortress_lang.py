#!/usr/bin/env python3
"""V91: aviso del Mariscal sin reja, en inglés, español y variantes regionales."""

import argparse
import json

from check_lang import LANG_DIR, load
from generator_cli import SPANISH_VARIANTS

KEY = 'towers.aptitudes.marshal_ready'
AFTER = 'towers.aptitudes.lord_open'
TEXT = {
    'en_us': 'Both Guardians have fallen. The Marshal can now be challenged.',
    'es_es': 'Han caído los dos Guardianes. Ya puedes enfrentarte al Mariscal.',
}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    for locale, value in TEXT.items():
        path = LANG_DIR / f'{locale}.json'
        original = load(path)
        result = {}
        for key, text in original.items():
            if key != KEY:
                result[key] = text
            if key == AFTER:
                result[KEY] = value
        if KEY not in result:
            raise ValueError(f'{AFTER} no aparece en {path}')
        rendered = (json.dumps(result, ensure_ascii=False, indent=2) + '\n').encode('utf-8')
        targets = [path] + ([LANG_DIR / f'{variant}.json' for variant in SPANISH_VARIANTS] if locale == 'es_es' else [])
        for target in targets:
            if args.check:
                if target.read_bytes() != rendered:
                    raise ValueError(f'{target}: desactualizado')
            else:
                target.write_bytes(rendered)
    print('Textos V91 correctos' if args.check else 'Textos V91 generados')


if __name__ == '__main__':
    main()
