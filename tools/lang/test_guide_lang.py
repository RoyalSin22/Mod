"""Cobertura y regeneración del grimorio, sin cargar Minecraft."""
from copy import deepcopy
import unittest
from unittest.mock import patch

from check_lang import LANG_DIR, LOCALES, load
from gen_evolved_lang import GUIDE, guide_extra
from gen_guide_lang import build, guide_key
from guide_catalog import TRANSLATIONS


class GuideTranslationTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.reference = load(LANG_DIR / 'en_us.json')

    def test_each_chapter_body_is_translated_in_each_locale(self):
        for key, source in self.reference.items():
            if guide_key(key) and key.endswith('.body'):
                bodies = [TRANSLATIONS[locale][key] for locale in LOCALES]
                self.assertNotIn(source, bodies, key)
                self.assertEqual(len(LOCALES), len(set(bodies)), key)

    def test_new_guide_key_requires_translation(self):
        reference = dict(self.reference, **{'guide.aptitudes.future.body': 'New chapter'})
        with self.assertRaisesRegex(ValueError, 'guide.aptitudes.future.body'):
            build(reference, 'pt_br', self.reference)

    def test_extra_catalog_key_is_rejected(self):
        catalog = deepcopy(TRANSLATIONS)
        catalog['pt_br']['guide.aptitudes.extra.title'] = 'Extra'
        with patch('gen_guide_lang.TRANSLATIONS', catalog):
            with self.assertRaisesRegex(ValueError, 'guide.aptitudes.extra.title'):
                build(self.reference, 'pt_br', self.reference)

    def test_blank_body_is_rejected(self):
        catalog = deepcopy(TRANSLATIONS)
        catalog['pt_br']['guide.aptitudes.start.body'] = '  '
        with patch('gen_guide_lang.TRANSLATIONS', catalog):
            with self.assertRaisesRegex(ValueError, 'vacío'):
                build(self.reference, 'pt_br', self.reference)

    def test_percent_marker_change_is_rejected(self):
        catalog = deepcopy(TRANSLATIONS)
        catalog['pt_br']['guide.aptitudes.paths.body'] += ' %%'
        with patch('gen_guide_lang.TRANSLATIONS', catalog):
            with self.assertRaisesRegex(ValueError, 'marcadores distintos'):
                build(self.reference, 'pt_br', self.reference)

    def test_other_phases_are_preserved_and_order_follows_english(self):
        for locale in LOCALES:
            previous = dict(reversed(list(load(LANG_DIR / f'{locale}.json').items())))
            previous['node.aptitudes.rage'] = 'Traducción futura'
            result = build(self.reference, locale, previous)
            self.assertEqual(list(self.reference), list(result))
            for key in self.reference:
                if not guide_key(key):
                    self.assertEqual(previous[key], result[key], key)

    def test_regeneration_is_idempotent_and_valid_for_every_locale(self):
        for locale in LOCALES:
            result = build(self.reference, locale, self.reference)
            self.assertEqual(result, build(self.reference, locale, result))

    def test_generated_evolution_paragraph_is_unicode_and_present_once(self):
        for locale in LOCALES:
            extra = guide_extra(locale)
            self.assertTrue(extra.startswith('\n\n'))
            self.assertNotIn('\\n', extra)
            self.assertEqual(GUIDE[locale].replace('\\n', '\n'), extra)
            body = TRANSLATIONS[locale]['guide.aptitudes.tree.body']
            self.assertTrue(body.endswith(extra))
            self.assertEqual(1, body.count(extra))


if __name__ == '__main__':
    unittest.main()
