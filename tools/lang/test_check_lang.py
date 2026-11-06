"""Mutaciones que deben detectar el validador y el generador, sin dependencias externas."""
import tempfile
from pathlib import Path
import unittest

from check_lang import LANG_DIR, load, markers, validate
from gen_interface_lang import build


class LanguageValidationTest(unittest.TestCase):
    def test_accepts_literals_and_escaped_percent(self):
        self.assertEqual([], validate({'a': '%s: +10%%, under 35% health'},
                                      {'a': '%s : +10%%, sous 35% de santé'}))

    def test_missing_key(self):
        self.assertTrue(validate({'a': 'x'}, {}))

    def test_extra_key(self):
        self.assertTrue(validate({}, {'a': 'x'}))

    def test_order(self):
        self.assertTrue(validate({'a': 'x', 'b': 'y'}, {'b': 'z', 'a': 't'}))

    def test_escaped_percent_cannot_become_single(self):
        self.assertTrue(validate({'a': '%s%%'}, {'a': '%s%'}))

    def test_missing_duplicate_argument(self):
        self.assertTrue(validate({'a': '%s/%s'}, {'a': '%s'}))

    def test_numbered_index_cannot_change(self):
        self.assertTrue(validate({'a': '%1$s'}, {'a': '%2$s'}))

    def test_indexed_arguments_can_be_reordered(self):
        self.assertEqual([], validate({'a': '%1$s %2$d'}, {'a': '%2$d %1$s'}))

    def test_unindexed_arguments_cannot_be_reordered(self):
        self.assertTrue(validate({'a': '%s %d'}, {'a': '%d %s'}))

    def test_argument_width_precision_and_flags(self):
        self.assertEqual(['%1$+08.2f', '%%', '%n'], markers('%1$+08.2f %% %n'))
        self.assertTrue(validate({'a': '%1$+08.2f'}, {'a': '%1$+08.3f'}))

    def test_non_string_value(self):
        self.assertTrue(validate({'a': 'text'}, {'a': 42}))

    def test_duplicate_json_keys(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'lang.json'
            path.write_text('{"a":"x","a":"y"}', encoding='utf-8')
            with self.assertRaisesRegex(ValueError, 'duplicada'):
                load(path)

    def test_non_object_json(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'lang.json'
            path.write_text('[]', encoding='utf-8')
            with self.assertRaises(ValueError):
                load(path)

    def test_unknown_phase_one_key_requires_a_translation(self):
        reference = load(LANG_DIR / 'en_us.json')
        reference['screen.aptitudes.future'] = 'New label'
        with self.assertRaisesRegex(ValueError, 'screen.aptitudes.future'):
            build(reference, 'pt_br', {})

    def test_regeneration_keeps_later_phase_translations_and_reference_order(self):
        source = load(LANG_DIR / 'en_us.json')
        previous = {'guide.aptitudes.tree.body': 'Revisado: ' + source['guide.aptitudes.tree.body']}
        result = build(source, 'pt_br', previous)
        self.assertEqual(previous['guide.aptitudes.tree.body'], result['guide.aptitudes.tree.body'])
        self.assertEqual(list(source), list(result))
        self.assertEqual(source['node.aptitudes.rage'], result['node.aptitudes.rage'])


if __name__ == '__main__':
    unittest.main()
