import sys
import tempfile
import unittest
from pathlib import Path

from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parent))
import weapon_art  # noqa: E402
import import_weapon_art as imp  # noqa: E402


def fake_ai_sword(size=256, bg=(255, 0, 255)):
    """Espada diagonal con degradado y bordes suaves sobre fondo liso, como la entregaría un generador de imágenes."""
    img = Image.new('RGB', (size, size), bg)
    d = ImageDraw.Draw(img)
    d.line([(30, size - 30), (size - 40, 40)], fill=(180, 190, 200), width=26)
    d.line([(30, size - 30), (size - 40, 40)], fill=(235, 240, 245), width=8)
    d.line([(20, size - 70), (70, size - 20)], fill=(210, 170, 40), width=18)
    d.line([(10, size - 10), (40, size - 40)], fill=(90, 55, 30), width=16)
    return img.filter(__import__('PIL.ImageFilter', fromlist=['x']).GaussianBlur(1.5))


class WeaponArtTest(unittest.TestCase):
    def test_external_art_is_normalized_to_a_valid_icon(self):
        out = weapon_art.normalize(fake_ai_sword(), colors=16)
        self.assertEqual(out.size, (16, 16))
        errors, warnings = weapon_art.validate(out)
        self.assertEqual(errors, [], errors)
        self.assertEqual(warnings, [], warnings)

    def test_background_is_removed_and_edges_are_hard(self):
        out = weapon_art.normalize(fake_ai_sword())
        alphas = {out.getpixel((x, y))[3] for x in range(16) for y in range(16)}
        self.assertEqual(alphas, {0, 255})
        self.assertEqual(out.getpixel((0, 0))[3], 0)
        self.assertLessEqual(len({out.getpixel((x, y))[:3] for x in range(16) for y in range(16) if out.getpixel((x, y))[3]}), 16)

    def test_empty_image_is_rejected(self):
        with self.assertRaises(ValueError):
            weapon_art.normalize(Image.new('RGBA', (64, 64), (0, 0, 0, 0)))

    def test_validation_catches_the_common_problems(self):
        semi = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        for i in range(16):
            semi.putpixel((i, 15 - i), (200, 200, 200, 128))
        errors, _ = weapon_art.validate(semi)
        self.assertTrue(any('semitransparentes' in e for e in errors))
        tiny = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        tiny.putpixel((8, 8), (255, 0, 0, 255))
        errors, _ = weapon_art.validate(tiny)
        self.assertTrue(any('opacos' in e for e in errors))
        errors, _ = weapon_art.validate(Image.new('RGBA', (32, 32), (0, 0, 0, 0)))
        self.assertTrue(any('tamaño' in e for e in errors))

    def test_wrong_orientation_is_only_a_warning(self):
        flat = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        for x in range(2, 14):
            for y in (3, 4):
                flat.putpixel((x, y), (200, 200, 210, 255))
        errors, warnings = weapon_art.validate(flat)
        self.assertEqual(errors, [])
        self.assertTrue(warnings)

    def test_flip_mirrors_the_weapon(self):
        a = weapon_art.normalize(fake_ai_sword())
        b = weapon_art.normalize(fake_ai_sword(), flip=True)
        self.assertNotEqual(list(a.getdata()), list(b.getdata()))

    def test_depth_is_thicker_in_dark_materials(self):
        img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        img.putpixel((3, 3), (230, 230, 240, 255))
        img.putpixel((4, 4), (40, 25, 15, 255))
        img.putpixel((5, 5), (230, 230, 240, 128))
        depth = weapon_art.depth_map(img)
        self.assertEqual((depth[3][3], depth[4][4], depth[5][5], depth[0][0]), (1, 2, 0, 0))

    def test_override_reads_only_valid_sized_files(self):
        old = weapon_art.SOURCE
        with tempfile.TemporaryDirectory() as tmp:
            weapon_art.SOURCE = Path(tmp)
            try:
                self.assertIsNone(weapon_art.override('mage_weapon_master'))
                Image.new('RGBA', (32, 32)).save(Path(tmp) / 'mage_weapon_master.png')
                self.assertIsNone(weapon_art.override('mage_weapon_master'))
                Image.new('RGBA', (16, 16)).save(Path(tmp) / 'mage_weapon_master.png')
                self.assertIsNotNone(weapon_art.override('mage_weapon_master'))
            finally:
                weapon_art.SOURCE = old

    def test_role_names_and_sheet_splitting(self):
        self.assertEqual(imp.role_of('Berserker'), 'berserker')
        self.assertEqual(imp.role_of('weapon_summoner_weapon_master'), 'weapon_summoner')
        self.assertIsNone(imp.role_of('espada'))
        self.assertEqual(len(imp.ROLES), 20)
        cells = imp.split_sheet(Image.new('RGB', (500, 200)), 5, 2)
        self.assertEqual(len(cells), 10)
        self.assertEqual(cells[0].size, (100, 100))
        with self.assertRaises(SystemExit):
            imp.split_sheet(Image.new('RGB', (30, 30)), 5, 5)

    def test_roles_match_the_mod_role_order(self):
        import importlib.util
        spec = importlib.util.spec_from_file_location('gen_items', Path(__file__).parent / 'gen_items.py')
        gi = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(gi)
        self.assertEqual(imp.ROLES, list(gi.ROLE_COLORS))


if __name__ == '__main__':
    unittest.main()
