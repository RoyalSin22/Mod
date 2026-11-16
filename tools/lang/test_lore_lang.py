import json
from pathlib import Path
import unittest

from check_lang import LANG_DIR, LOCALES, load, markers, validate
from gen_lore_lang import PREFIX, encode, key_for, lore_keys, managed, render
from generator_cli import SPANISH_VARIANTS
from lore_catalog import ENTRIES
from lore_i18n import TRANSLATIONS

META_PATH = Path(__file__).resolve().parents[1] / 'lore' / 'lore_entries.json'
VERIFICATION = {'existing_hook', 'plausible_hook', 'unverified', 'blocked', 'undefined'}
REVEAL = ['baja', 'media', 'alta', 'muy_alta', 'maxima']
KINDS = {'lore', 'bestiary', 'chronicle'}

# Frases ya corregidas (A-01 a A-10): no deben volver a aparecer en los textos del jugador.
FORBIDDEN = (
    'Herederos de los Sellos', 'Heirs of the Seals', 'Dato de implementación', 'Nota:', 'Note:',
    'prisión primordial', 'primordial prison', 'único Eclipse', 'only Eclipse',
    'alguien esperaba', 'someone waited', 'no significaba rechazar',
)
# Secretos reservados: nombres que no deben salir antes de su momento de descubrimiento.
CONSCIOUSNESS = ('Conciencia Ancestral', 'Ancestral Consciousness')
CONSCIOUSNESS_ALLOWED = set()  # la entidad nunca se nombra así en estas colecciones


def metadata():
    return json.loads(META_PATH.read_text(encoding='utf-8'))['entries']


class LoreMetadataTest(unittest.TestCase):
    def test_ids_match_catalog_in_order(self):
        self.assertEqual([m['id'] for m in metadata()], list(ENTRIES))

    def test_ids_are_unique_and_lowercase(self):
        ids = [m['id'] for m in metadata()]
        self.assertEqual(len(ids), len(set(ids)))
        for entry_id in ids:
            self.assertRegex(entry_id, r'^lore\.[a-z0-9_]+\.[a-z0-9_]+$')

    def test_aliases_unique(self):
        aliases = [m['alias'] for m in metadata()]
        self.assertEqual(len(aliases), len(set(aliases)))

    def test_fields_are_valid(self):
        for meta in metadata():
            self.assertIn(meta['kind'], KINDS, meta['id'])
            self.assertIn(meta['reveal'], REVEAL, meta['id'])
            self.assertIn(meta['unlock']['verification'], VERIFICATION, meta['id'])
            self.assertEqual(meta['text_status'], 'provisional', meta['id'])
            self.assertTrue(meta['unlock']['intent'].strip(), meta['id'])

    def test_dependencies_exist_and_have_no_cycles(self):
        graph = {m['id']: m['depends_on'] for m in metadata()}
        for entry_id, deps in graph.items():
            for dep in deps:
                self.assertIn(dep, graph, f'{entry_id} depende de {dep}')
                self.assertNotEqual(dep, entry_id)

        state = {}

        def visit(node):
            if state.get(node) == 'done':
                return
            self.assertNotEqual(state.get(node), 'open', f'ciclo en {node}')
            state[node] = 'open'
            for dep in graph[node]:
                visit(dep)
            state[node] = 'done'

        for node in graph:
            visit(node)

    def test_dependency_is_not_more_revealing_than_dependent(self):
        order = {name: index for index, name in enumerate(REVEAL)}
        by_id = {m['id']: m for m in metadata()}
        for meta in by_id.values():
            for dep in meta['depends_on']:
                self.assertLessEqual(order[by_id[dep]['reveal']], order[meta['reveal']],
                                     f'{dep} revela más que {meta["id"]}')

    def test_bestiary_entries_match_existing_ids_in_notes(self):
        for meta in metadata():
            if meta['kind'] == 'bestiary':
                self.assertIn('entity', meta['related'], meta['id'])


class LoreTextTest(unittest.TestCase):
    def test_every_entry_has_both_languages(self):
        for entry_id, texts in ENTRIES.items():
            for language in ('es', 'en'):
                title, body = texts[language]
                self.assertTrue(body.strip(), f'{entry_id}/{language}: cuerpo vacío')
                if entry_id.startswith('lore.bestiary.'):
                    self.assertIsNone(title, entry_id)
                else:
                    self.assertTrue(title and title.strip(), f'{entry_id}/{language}: título vacío')

    def test_no_percent_markers(self):
        # Los textos no llevan argumentos; un % literal obligaría a escribir %%.
        for entry_id, texts in ENTRIES.items():
            for language in ('es', 'en'):
                for value in texts[language]:
                    if value is not None:
                        self.assertEqual(markers(value), [], f'{entry_id}/{language}')

    def test_corrected_phrases_do_not_return(self):
        for entry_id, texts in ENTRIES.items():
            for language in ('es', 'en'):
                for value in texts[language]:
                    for phrase in FORBIDDEN:
                        self.assertNotIn(phrase, value or '', f'{entry_id}/{language}')

    def test_reserved_names_stay_secret(self):
        for entry_id, texts in ENTRIES.items():
            if entry_id in CONSCIOUSNESS_ALLOWED:
                continue
            for language in ('es', 'en'):
                for value in texts[language]:
                    for name in CONSCIOUSNESS:
                        self.assertNotIn(name, value or '', f'{entry_id}/{language}')

    def test_english_uses_official_creature_names(self):
        expected = {
            'lore.bestiary.errante_velado': 'Veiled Wanderer',
            'lore.bestiary.osario_quebrado': 'Broken Ossuary',
            'lore.bestiary.ancient_automaton': 'Ancient Automaton',
            'lore.bestiary.custodio_cuatro_llaves': 'Custodian',
            'lore.bestiary.escorificador_hierro': 'Iron Slagger',
        }
        for entry_id, name in expected.items():
            self.assertIn(name, ENTRIES[entry_id]['en'][1], entry_id)


# Títulos que se escriben igual en inglés y en algún idioma (cognados), p. ej. «Eclipse» en portugués.
COGNATE_TITLES = {'Eclipse'}


class LoreTranslationTest(unittest.TestCase):
    """Traducciones pt/fr/de del lore (lore_i18n_*.py): cobertura, estructura y nombres oficiales."""
    OFFICIAL = {
        'pt': {'lore.bestiary.errante_velado': 'Andarilho Velado', 'lore.bestiary.osario_quebrado': 'Ossuário Quebrado',
               'lore.bestiary.ancient_automaton': 'Autômato Antigo', 'lore.bestiary.custodio_cuatro_llaves': 'Custódio',
               'lore.bestiary.escorificador_hierro': 'Escorificador de Ferro'},
        'fr': {'lore.bestiary.errante_velado': 'Errant voilé', 'lore.bestiary.osario_quebrado': 'Ossuaire brisé',
               'lore.bestiary.ancient_automaton': 'Automate ancien', 'lore.bestiary.custodio_cuatro_llaves': 'Gardien',
               'lore.bestiary.escorificador_hierro': 'Scorificateur de fer'},
        # En alemán el nombre va declinado tras el artículo («Der Verschleierte Wanderer»): se comprueba esa forma.
        'de': {'lore.bestiary.errante_velado': 'Verschleierte Wanderer',
               'lore.bestiary.osario_quebrado': 'Zerbrochene Beinhaus',
               'lore.bestiary.ancient_automaton': 'Uralte Automat', 'lore.bestiary.custodio_cuatro_llaves': 'Wächter',
               'lore.bestiary.escorificador_hierro': 'Eisenschlacker'},
    }
    SECRET = ('Consciência Ancestral', 'Conscience ancestrale', 'Uralte Bewusstsein', 'Ahnenbewusstsein', 'Urbewusstsein')

    def test_every_entry_is_translated_and_nothing_is_orphaned(self):
        for code, texts in TRANSLATIONS.items():
            self.assertEqual(list(texts), list(ENTRIES), code)

    def test_titles_follow_the_english_pattern(self):
        for code, texts in TRANSLATIONS.items():
            for entry_id, (title, body) in texts.items():
                if entry_id.startswith('lore.bestiary.'):
                    self.assertIsNone(title, f'{code}/{entry_id}')
                else:
                    self.assertTrue(title and title.strip(), f'{code}/{entry_id}: título vacío')
                self.assertTrue(body.strip(), f'{code}/{entry_id}: cuerpo vacío')

    def test_same_paragraphs_as_english(self):
        for code, texts in TRANSLATIONS.items():
            for entry_id, (_, body) in texts.items():
                english = ENTRIES[entry_id]['en'][1]
                self.assertEqual(len(body.split('\n\n')), len(english.split('\n\n')), f'{code}/{entry_id}')

    def test_not_left_in_english(self):
        for code, texts in TRANSLATIONS.items():
            for entry_id, (title, body) in texts.items():
                self.assertNotEqual(body, ENTRIES[entry_id]['en'][1], f'{code}/{entry_id}')
                if title is not None and title not in COGNATE_TITLES:
                    self.assertNotEqual(title, ENTRIES[entry_id]['en'][0], f'{code}/{entry_id}')

    def test_no_percent_markers_and_secret_name_stays_secret(self):
        for code, texts in TRANSLATIONS.items():
            for entry_id, values in texts.items():
                for value in values:
                    if value is None:
                        continue
                    self.assertEqual(markers(value), [], f'{code}/{entry_id}')
                    for name in self.SECRET:
                        self.assertNotIn(name, value, f'{code}/{entry_id}')

    def test_bestiary_uses_official_creature_names(self):
        for code, expected in self.OFFICIAL.items():
            for entry_id, name in expected.items():
                self.assertIn(name, TRANSLATIONS[code][entry_id][1], f'{code}/{entry_id}')


class LoreLangFilesTest(unittest.TestCase):
    """Los archivos de idioma deben coincidir con lo que genera gen_lore_lang.py."""

    def test_files_are_up_to_date(self):
        for locale in ('en_us', 'es_es', *LOCALES):
            path = LANG_DIR / f'{locale}.json'
            self.assertEqual(path.read_bytes(), encode(render(locale, load(path))),
                             f'{locale}: ejecuta python3 tools/lang/gen_lore_lang.py')

    def test_spanish_variants_are_copies(self):
        source = (LANG_DIR / 'es_es.json').read_bytes()
        for variant in SPANISH_VARIANTS:
            self.assertEqual((LANG_DIR / f'{variant}.json').read_bytes(), source, variant)

    def test_lore_keys_come_last_and_in_catalog_order(self):
        for locale in ('en_us', 'es_es', *LOCALES):
            keys = list(load(LANG_DIR / f'{locale}.json'))
            lore = [key for key in keys if managed(key)]
            self.assertEqual(keys[len(keys) - len(lore):], lore, locale)
            language = 'es' if locale.startswith('es') else 'en'
            self.assertEqual(lore, list(lore_keys(language)), locale)

    def test_other_languages_keep_same_keys_as_english(self):
        reference = load(LANG_DIR / 'en_us.json')
        for locale in LOCALES:
            self.assertEqual(validate(reference, load(LANG_DIR / f'{locale}.json')), [], locale)

    def test_unlock_messages_are_translated(self):
        from lore_catalog import MESSAGES
        for name, texts in MESSAGES.items():
            self.assertEqual(set(texts), {'es', 'en', 'pt', 'fr', 'de'}, name)
            for code in ('pt', 'fr', 'de'):
                self.assertNotEqual(texts[code], texts['en'], f'{name}: {code} sigue en inglés')
                self.assertTrue(texts[code].strip(), f'{name}: {code} vacío')

    def test_key_naming(self):
        self.assertEqual(key_for('lore.cosmology.echoes', 'title'), 'lore.aptitudes.cosmology.echoes.title')
        self.assertTrue(PREFIX.startswith('lore.'))


if __name__ == '__main__':
    unittest.main()
