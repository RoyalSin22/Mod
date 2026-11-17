import sys
import tempfile
import unittest
from pathlib import Path
from types import SimpleNamespace

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
    def test_external_art_is_normalized_to_a_valid_icon_at_both_densities(self):
        for size in (16, 32, 64):
            out = weapon_art.normalize(fake_ai_sword(), colors=16, size=size)
            self.assertEqual(out.size, (size, size))
            errors, warnings = weapon_art.validate(out)
            self.assertEqual(errors, [], f'{size}: {errors}')
            self.assertEqual(warnings, [], f'{size}: {warnings}')

    def test_default_density_is_64_and_unknown_sizes_are_rejected(self):
        self.assertEqual(weapon_art.normalize(fake_ai_sword()).size, (64, 64))
        with self.assertRaises(ValueError):
            weapon_art.normalize(fake_ai_sword(), size=24)

    def test_background_is_removed_and_edges_are_hard(self):
        out = weapon_art.normalize(fake_ai_sword(), colors=16)
        n = out.size[0]
        alphas = {out.getpixel((x, y))[3] for x in range(n) for y in range(n)}
        self.assertEqual(alphas, {0, 255})
        self.assertEqual(out.getpixel((0, 0))[3], 0)
        self.assertLessEqual(len({out.getpixel((x, y))[:3] for x in range(n) for y in range(n) if out.getpixel((x, y))[3]}), 16)

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
        errors, _ = weapon_art.validate(Image.new('RGBA', (24, 24), (0, 0, 0, 0)))
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
        a = weapon_art.normalize(fake_ai_sword(), size=16)
        b = weapon_art.normalize(fake_ai_sword(), flip=True, size=16)
        self.assertNotEqual(list(a.getdata()), list(b.getdata()))

    def test_bevel_grows_from_the_edge_to_the_centre_and_stays_in_range(self):
        img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        for y in range(4, 12):
            for x in range(4, 12):
                img.putpixel((x, y), (200, 200, 200, 255))
        d = weapon_art.bevel_depth_map(img, min_units=0.5, max_units=1.5)
        self.assertEqual(d[0][0], 0)
        self.assertEqual(d[4][4], 0.5)
        self.assertGreater(d[7][7], d[4][4])
        self.assertLessEqual(max(max(row) for row in d), 1.5)

    def test_depth_is_uniform_so_the_blade_surface_is_smooth(self):
        img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        img.putpixel((3, 3), (230, 230, 240, 255))
        img.putpixel((4, 4), (40, 25, 15, 255))
        img.putpixel((5, 5), (230, 230, 240, 128))
        depth = weapon_art.depth_map(img)
        self.assertEqual((depth[3][3], depth[4][4], depth[5][5], depth[0][0]), (1, 1, 0, 0), 'claro y oscuro con el mismo grosor')

    def test_defringe_replaces_magenta_tinted_outline_with_a_dark_neutral_one(self):
        img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        for y in range(4, 12):
            for x in range(4, 12):
                img.putpixel((x, y), (150, 140, 120, 255))
        for i in range(4, 12):  # contorno contaminado por #FF00FF
            for p in ((i, 3), (i, 12), (3, i), (12, i)):
                img.putpixel(p, (90, 6, 91, 255))
        out = weapon_art.defringe(img)
        for p in ((6, 3), (6, 12), (3, 6), (12, 6)):
            r, g, b, a = out.getpixel(p)
            self.assertEqual(a, 255)
            self.assertLess(abs(r - b), 40, 'sin tinte magenta')
            self.assertGreater(g, 6, 'verde recuperado')
            self.assertLess(max(r, g, b), 90, 'sigue siendo un contorno oscuro')
        self.assertEqual(out.getpixel((8, 8)), (150, 140, 120, 255), 'el interior no se toca')

    def test_defringe_keeps_intentional_purple_with_more_blue_than_red(self):
        img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        for y in range(4, 12):
            for x in range(4, 12):
                img.putpixel((x, y), (142, 68, 173, 255))  # cristal morado del Mago
        out = weapon_art.defringe(img)
        self.assertEqual(out.getpixel((4, 4)), (142, 68, 173, 255))
        self.assertEqual(out.getpixel((7, 4)), (142, 68, 173, 255))

    def test_override_reads_only_valid_sized_files(self):
        old = weapon_art.SOURCE
        with tempfile.TemporaryDirectory() as tmp:
            weapon_art.SOURCE = Path(tmp)
            try:
                self.assertIsNone(weapon_art.override('mage_weapon_master'))
                Image.new('RGBA', (24, 24)).save(Path(tmp) / 'mage_weapon_master.png')
                self.assertIsNone(weapon_art.override('mage_weapon_master'))
                Image.new('RGBA', (16, 16)).save(Path(tmp) / 'mage_weapon_master.png')
                self.assertIsNotNone(weapon_art.override('mage_weapon_master'))
                for n in (32, 64):
                    Image.new('RGBA', (n, n)).save(Path(tmp) / 'mage_weapon_master.png')
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

    def test_downscale_keeps_hard_edges_and_shrinks_to_the_gui_size(self):
        big = weapon_art.normalize(fake_ai_sword(), size=64)
        small = weapon_art.downscale(big)
        self.assertEqual(small.size, (32, 32))
        self.assertEqual({small.getpixel((x, y))[3] for x in range(32) for y in range(32)}, {0, 255})
        self.assertEqual(weapon_art.downscale(small).size, (32, 32), 'ya reducida: no cambia')
        self.assertEqual(weapon_art.validate(small)[0], [])

    def _sheet(self, size, items):
        img = Image.new('RGB', size, (255, 0, 255))
        d = ImageDraw.Draw(img)
        for kind, a, b in items:
            if kind == 'line':
                d.line(a, fill=(120, 90, 60), width=b)
            else:
                d.rectangle(a, fill=b)
        return img

    def test_auto_split_ignores_the_grid_and_keeps_reading_order(self):
        # Como la hoja que entrega un generador de imágenes: tamaños y huecos irregulares, una pieza fina en medio.
        img = self._sheet((418, 244), [
            ('line', [(40, 200), (150, 40)], 14), ('rect', [110, 20, 170, 80], (150, 60, 60)),
            ('line', [(245, 70), (245, 20)], 3),
            ('line', [(270, 210), (370, 60)], 14), ('rect', [320, 20, 390, 70], (150, 130, 90))])
        crops = weapon_art.split_auto(img)
        self.assertEqual(len(crops), 3)
        self.assertEqual([c.size[0] > c.size[1] for c in crops], [False, False, False])
        self.assertLess(crops[1].size[0], 40, 'la pieza fina queda en el centro, entre las dos armas')

    def test_auto_split_reads_rows_top_to_bottom_then_left_to_right(self):
        img = self._sheet((400, 400), [
            ('rect', [20, 20, 120, 120], (200, 50, 50)), ('rect', [250, 30, 350, 130], (50, 200, 50)),
            ('rect', [30, 260, 130, 360], (50, 50, 200)), ('rect', [260, 250, 360, 380], (200, 200, 50))])
        crops = weapon_art.split_auto(img)
        self.assertEqual(len(crops), 4)
        colors = [c.getpixel((c.size[0] // 2, c.size[1] // 2))[:3] for c in crops]
        self.assertEqual(colors, [(200, 50, 50), (50, 200, 50), (50, 50, 200), (200, 200, 50)])

    def test_auto_split_keeps_sparks_with_their_weapon_and_drops_stray_dust(self):
        img = self._sheet((500, 250), [
            ('rect', [30, 40, 150, 200], (180, 180, 190)), ('rect', [154, 60, 162, 68], (255, 200, 60)),
            ('rect', [300, 40, 420, 200], (160, 120, 80)), ('rect', [250, 120, 251, 121], (255, 255, 255))])
        crops = weapon_art.split_auto(img)
        self.assertEqual(len(crops), 2, 'la chispa pegada se une al arma; la mota suelta (1 px) se descarta')

    def test_auto_split_drops_pieces_stuck_to_the_border(self):
        img = self._sheet((418, 244), [
            ('rect', [5, 60, 8, 140], (200, 150, 120)),      # resto del marco, pegado al borde izquierdo
            ('line', [(40, 200), (150, 40)], 14), ('line', [(270, 210), (370, 60)], 14)])
        report = {}
        crops = weapon_art.split_auto(img, report=report)
        self.assertEqual(len(crops), 2)
        self.assertEqual(report['edge'], 1)

    def test_a_black_capture_frame_and_magenta_background_are_both_removed(self):
        img = Image.new('RGB', (300, 200), (0, 0, 0))
        d = ImageDraw.Draw(img)
        d.rectangle([10, 10, 289, 189], fill=(255, 0, 255))
        d.line([(60, 160), (220, 40)], fill=(120, 90, 60), width=14)
        d.line([(60, 160), (80, 140)], fill=(8, 8, 8), width=4)      # contorno oscuro del arma: no debe borrarse
        crops = weapon_art.split_auto(img)
        self.assertEqual(len(crops), 1)

    def test_slivers_and_straight_bars_are_rejected_before_they_are_scaled_up(self):
        sliver = self._sheet((200, 200), [('rect', [90, 40, 98, 160], (200, 150, 120))])
        self.assertIn('demasiado estrecha', weapon_art.shape_problem(sliver))
        bar = self._sheet((300, 200), [('rect', [20, 90, 280, 104], (200, 150, 120))])
        self.assertIn('demasiado estrecha', weapon_art.shape_problem(bar))
        diagonal = self._sheet((256, 256), [('line', [(30, 226), (226, 30)], 20)])
        self.assertIsNone(weapon_art.shape_problem(diagonal))
        self.assertIn('vacía', weapon_art.shape_problem(Image.new('RGB', (50, 50), (255, 0, 255))))

    def test_the_join_radius_is_tuned_to_find_the_expected_count(self):
        # Un arma de piezas sueltas (espadas flotantes) separadas por huecos mayores que el radio por defecto.
        img = self._sheet((900, 300), [
            ('rect', [30, 100, 90, 160], (150, 60, 60)), ('rect', [130, 100, 190, 160], (150, 60, 60)), ('rect', [230, 100, 290, 160], (150, 60, 60)),
            ('rect', [600, 40, 700, 260], (60, 90, 150))])
        default = weapon_art.split_auto(img)
        self.assertEqual(len(default), 4)
        crops, join, _ = weapon_art.split_auto_expect(img, 2)
        self.assertEqual(len(crops), 2)
        self.assertGreater(join, 0.03)

    def test_a_weapon_close_to_the_border_is_kept_but_a_thin_remnant_is_dropped(self):
        img = self._sheet((600, 300), [
            ('rect', [4, 60, 100, 240], (150, 60, 60)),       # arma con cuerpo, casi pegada al borde
            ('rect', [596, 20, 598, 280], (200, 150, 120)),   # resto estrecho del marco
            ('rect', [300, 60, 400, 240], (60, 90, 150))])
        report = {}
        self.assertEqual(len(weapon_art.split_auto(img, report=report)), 2)
        self.assertEqual(report['edge'], 1)

    def test_background_inside_a_closed_hole_is_removed(self):
        img = self._sheet((200, 200), [('rect', [40, 40, 160, 160], (150, 120, 60)), ('rect', [80, 80, 120, 120], (255, 0, 255))])
        cleaned = weapon_art.remove_background(img, 40)
        self.assertEqual(cleaned.getpixel((100, 100))[3], 0, 'el hueco interior (magenta) queda transparente')
        self.assertEqual(cleaned.getpixel((50, 50))[3], 255)

    def test_auto_split_of_an_empty_image_finds_nothing(self):
        self.assertEqual(weapon_art.split_auto(Image.new('RGB', (200, 100), (255, 0, 255))), [])

    def test_cli_stops_without_writing_when_the_count_does_not_match(self):
        old = weapon_art.SOURCE
        with tempfile.TemporaryDirectory() as tmp:
            weapon_art.SOURCE = Path(tmp) / 'out'
            sheet = Path(tmp) / 'hoja.png'
            self._sheet((418, 244), [('rect', [20, 20, 120, 220], (150, 60, 60)), ('rect', [250, 20, 350, 220], (150, 130, 90))]).save(sheet)

            args = SimpleNamespace(inputs=[str(sheet)], sheet='auto', roles='berserker,paladin,lancer', debug_split=None, join=None,
                                   colors=32, tolerance=40, size=64, no_fit=False, flip=False, rotate=0, dry_run=False)
            try:
                with self.assertRaises(SystemExit) as raised:
                    imp.do_import(args)
                self.assertIn('2 armas', str(raised.exception))
                self.assertFalse((Path(tmp) / 'out').exists())
            finally:
                weapon_art.SOURCE = old

    def test_roles_match_the_mod_role_order(self):
        import importlib.util
        spec = importlib.util.spec_from_file_location('gen_items', Path(__file__).parent / 'gen_items.py')
        gi = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(gi)
        self.assertEqual(imp.ROLES, list(gi.ROLE_COLORS))


if __name__ == '__main__':
    unittest.main()
