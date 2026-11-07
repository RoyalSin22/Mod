"""Regresión de cobertura y datos de los generadores, sin cargar Minecraft."""
from collections import Counter
from copy import deepcopy
import json
import re
import unittest
from unittest.mock import patch

from check_lang import LANG_DIR, LOCALES, load
import gen_evolved_lang as evolution
import gen_path_lang as paths
from gen_nodes_lang import base_key, build, node_key
from node_catalog import TEXT


def numbers(text):
    return Counter(re.findall(r'\d+(?:[.,]\d+)?', text))


class NodeTranslationTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.reference = load(LANG_DIR / 'en_us.json')

    def test_complete_node_coverage_and_no_english_descriptions(self):
        for locale in LOCALES:
            result = build(self.reference, locale, self.reference)
            for key, source in self.reference.items():
                if node_key(key) and key.endswith('.desc'):
                    self.assertNotEqual(source, result[key], f'{locale}/{key}')
            self.assertEqual(list(self.reference), list(result))

    def test_numbers_preserved_for_every_node_description(self):
        for locale in LOCALES:
            result = build(self.reference, locale, self.reference)
            for key, source in self.reference.items():
                if node_key(key) and key.endswith('.desc'):
                    self.assertEqual(numbers(source), numbers(result[key]), f'{locale}/{key}')

    def test_regeneration_preserves_other_phases(self):
        previous = dict(self.reference)
        previous['guide.aptitudes.start.body'] = 'Capítulo revisado'
        previous['screen.aptitudes.title'] = 'Interfaz revisada'
        for locale in LOCALES:
            result = build(self.reference, locale, previous)
            for key in self.reference:
                if not node_key(key):
                    self.assertEqual(previous[key], result[key], key)
            self.assertEqual(result, build(self.reference, locale, result))

    def test_new_base_text_requires_translation(self):
        reference = dict(self.reference, **{'node.aptitudes.future': 'Untranslated new node'})
        with self.assertRaisesRegex(ValueError, 'node.aptitudes.future'):
            build(reference, 'pt_br', reference)

    def test_new_generated_node_requires_generator_support(self):
        reference = dict(self.reference, **{'node.aptitudes.future_a7': 'Future'})
        with self.assertRaisesRegex(ValueError, 'node.aptitudes.future_a7'):
            build(reference, 'pt_br', reference)

    def test_empty_node_and_broken_percent_rejected(self):
        for bad in ('', '%s'):
            catalog = deepcopy(TEXT)
            catalog['Tough Skin']['pt_br'] = bad
            with patch('gen_nodes_lang.TEXT', catalog):
                with self.assertRaises(ValueError):
                    build(self.reference, 'pt_br', self.reference)

    def test_duplicate_generated_node_rejected(self):
        with patch('gen_nodes_lang.paths', return_value={'node.aptitudes.berserker_m1': 'Duplicado'}):
            with self.assertRaisesRegex(ValueError, 'duplicado'):
                build(self.reference, 'pt_br', self.reference)

    def test_missing_path_name_rejected(self):
        rows = deepcopy(paths.LOCAL_ROWS)
        del rows['pt_br']['berserker_a']
        with patch.object(paths, 'LOCAL_ROWS', rows):
            with self.assertRaisesRegex(ValueError, 'Cobertura'):
                paths.build('pt_br', self.reference)

    def test_forks_boosts_and_keystones_use_server_tables(self):
        effects, templates = paths.java_effects(), paths.keystone_templates()
        for locale in LOCALES:
            result = paths.build(locale, self.reference)
            for role, path, *_ in paths.ROWS:
                base = f'{role}_{paths.LETTERS[path - 1]}'
                for suffix in ('5x', '5y'):
                    self.assertIn(paths.FORK[locale], result[f'node.aptitudes.{base}{suffix}.desc'])
                self.assertEqual(paths.desc(effects[role][path - 1][3], 1.5, locale,
                                            [paths.BOOSTED[locale]]), result[f'node.aptitudes.{base}6.desc'])
                template = paths.TEMPLATE[locale][templates[role][path - 1]][1]
                self.assertIn(template, result[f'node.aptitudes.{base}7.desc'])

    def test_regenerating_legacy_locales_keeps_every_byte(self):
        for locale in ('en_us', 'es_es'):
            original = load(LANG_DIR / f'{locale}.json')
            for module in (evolution, paths):
                result = module.update(locale, dict(original))
                self.assertEqual(original, result, f'{module.__name__}/{locale}')
                self.assertEqual(list(original), list(result))
                rendered = (json.dumps(result, ensure_ascii=False, indent=2) + '\n').encode('utf-8')
                self.assertEqual((LANG_DIR / f'{locale}.json').read_bytes(), rendered)
                if locale == 'es_es':
                    for variant in ('es_mx', 'es_ar', 'es_cl', 'es_ec', 'es_uy', 'es_ve'):
                        self.assertEqual(rendered, (LANG_DIR / f'{variant}.json').read_bytes())
                self.assertEqual(result, module.update(locale, dict(result)))

    def test_base_catalog_has_no_unused_entries(self):
        sources = {v for k, v in self.reference.items() if base_key(k)}
        self.assertEqual(sources, set(TEXT))

    def test_no_accidental_invisible_characters(self):
        for locale in LOCALES:
            for key, text in build(self.reference, locale, self.reference).items():
                if node_key(key):
                    self.assertFalse(set(text) & {'\u00ad', '\u200b', '\ufeff'}, key)


if __name__ == '__main__':
    unittest.main()
