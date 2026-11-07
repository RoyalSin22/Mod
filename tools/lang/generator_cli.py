"""Escritura y comprobación común de los generadores de evolución y sendas."""
import argparse
import json

from check_lang import LANG_DIR, LOCALES, load, validate

SPANISH_VARIANTS = ('es_mx', 'es_ar', 'es_cl', 'es_ec', 'es_uy', 'es_ve')


def run(update, description):
    parser = argparse.ArgumentParser(description=description)
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--locales', nargs='+', choices=('en_us', 'es_es', *LOCALES),
                        default=('en_us', 'es_es', *LOCALES))
    args = parser.parse_args()
    try:
        reference = load(LANG_DIR / 'en_us.json')
        failed = False
        for locale in args.locales:
            path = LANG_DIR / f'{locale}.json'
            data = update(locale, load(path))
            if list(data) != list(reference):
                raise ValueError(f'{locale}: claves u orden distintos del inglés')
            if locale in LOCALES:
                errors = validate(reference, data)
                if errors:
                    raise ValueError('\n'.join(errors))
            rendered = (json.dumps(data, ensure_ascii=False, indent=2) + '\n').encode('utf-8')
            targets = [path]
            if locale == 'es_es':
                targets.extend(LANG_DIR / f'{variant}.json' for variant in SPANISH_VARIANTS)
            for target in targets:
                if args.check:
                    if target.read_bytes() != rendered:
                        print(f'{target.stem}: desactualizado')
                        failed = True
                else:
                    target.write_bytes(rendered)
            print(f'{locale}: generación comprobada' if args.check else f'{locale}: generado')
        return int(failed)
    except (OSError, ValueError) as error:
        print(f'ERROR: {error}')
        return 1
