"""V73: diseños propios de las 20 armas de evolución (Maestro de los 20 roles; el Definitivo ya no tiene arma, V74).

Antes todas compartían cuatro plantillas (espada, mandoble y dos focos) y solo cambiaba el color. Ahora cada rol tiene su
silueta: hacha, dagas, hoz, lanza, espadas, mazas, bastones, guadañas, laúd, revólver… Se dibujan con primitivas (líneas y
rectángulos) sobre una rejilla de 16×16 y se entregan como plantillas de ``gen_items.py`` (misma leyenda de letras).

Orientación: la empuñadura abajo a la izquierda y la punta arriba a la derecha, como el resto de armas del mod, para que las
transformaciones de mano de vanilla y los modelos 3D encajen.
Materiales: S acero (s brillo), G oro o metal de la guarda, A color del rol, J gema del rol, L cuero, W madera, D oscuro, K muy oscuro.
"""


class Canvas:
    def __init__(self):
        self.g = [['.'] * 16 for _ in range(16)]

    def p(self, x, y, ch):
        if 0 <= x < 16 and 0 <= y < 16:
            self.g[y][x] = ch
        return self

    def line(self, x0, y0, x1, y1, ch):
        dx, dy = abs(x1 - x0), -abs(y1 - y0)
        sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
        err = dx + dy
        while True:
            self.p(x0, y0, ch)
            if x0 == x1 and y0 == y1:
                break
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                x0 += sx
            if e2 <= dx:
                err += dx
                y0 += sy
        return self

    def rect(self, x0, y0, x1, y1, ch):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.p(x, y, ch)
        return self

    def pts(self, ch, *points):
        for x, y in points:
            self.p(x, y, ch)
        return self

    def rows(self):
        return [''.join(r) for r in self.g]

    def blade(self, x0, y0, x1, y1, dark='S', light='s'):
        """Hoja de dos de ancho: la diagonal inferior en tono base y la superior en brillo."""
        self.line(x0, y0, x1, y1, dark)
        self.line(x0 + 1, y0, x1, y1 + 1, light)
        return self


def _hilt(c, gx=4, gy=11, wing=2, guard='G', grip='L', gem=True):
    """Guarda perpendicular a la hoja diagonal, empuñadura y pomo."""
    c.line(gx - wing, gy - wing, gx + wing, gy + wing, guard)
    if gem:
        c.p(gx, gy, 'J')
    c.line(gx - 1, gy + 1, gx - 3, gy + 3, grip)
    c.p(gx - 4, gy + 4, guard)


# ------------------------------------------------------------------------------------------------ Cuerpo a cuerpo

def berserker_master():
    c = Canvas()
    c.line(2, 13, 10, 5, 'W')
    c.pts('S', (11, 1), (12, 1), (13, 1), (10, 2), (14, 2), (10, 3), (14, 3), (11, 4), (14, 4), (12, 5), (14, 5), (12, 6), (13, 6))
    c.pts('s', (11, 2), (12, 2), (13, 2), (11, 3), (12, 3), (13, 3), (12, 4), (13, 4), (13, 5))
    c.pts('A', (9, 6), (10, 6), (10, 7), (9, 5))
    c.pts('J', (11, 5))
    c.pts('G', (1, 14), (0, 15))
    return c.rows()




def assassin_master():
    c = Canvas()
    c.blade(6, 9, 13, 2)
    c.pts('S', (14, 1))
    c.line(4, 11, 8, 11, 'G')
    c.line(5, 10, 5, 12, 'G')
    c.line(3, 12, 1, 14, 'L')
    c.pts('J', (5, 11), (0, 15))
    c.blade(3, 5, 6, 2, 'S', 's')
    c.pts('A', (2, 6), (7, 1))
    return c.rows()




def herbalist_master():
    c = Canvas()
    c.line(2, 13, 9, 6, 'W')
    c.pts('S', (10, 5), (11, 4), (12, 3), (13, 3), (14, 4), (14, 5), (13, 6), (12, 6))
    c.pts('s', (11, 5), (12, 4), (13, 4), (13, 5), (12, 5))
    c.pts('A', (8, 7), (9, 8), (6, 9), (5, 8), (7, 9))
    c.pts('J', (10, 6), (3, 12))
    c.pts('G', (1, 14), (0, 15))
    return c.rows()




def lancer_master():
    c = Canvas()
    c.line(1, 14, 12, 3, 'W')
    c.pts('S', (13, 2), (14, 1), (15, 0), (14, 2), (13, 1), (12, 2))
    c.pts('s', (13, 3), (14, 3), (12, 1))
    c.pts('G', (11, 4), (10, 5), (12, 5), (11, 3), (9, 6), (13, 4))
    c.pts('A', (10, 7), (9, 8), (8, 9), (9, 9), (10, 8), (7, 10), (8, 10), (9, 10))
    c.pts('J', (11, 4), (2, 13))
    return c.rows()




def swordsman_master():
    c = Canvas()
    c.blade(5, 10, 14, 1)
    c.pts('S', (15, 0))
    _hilt(c, 4, 11, 3)
    return c.rows()




def paladin_master():
    c = Canvas()
    c.line(2, 13, 9, 6, 'W')
    c.rect(9, 1, 13, 5, 'S')
    c.rect(10, 2, 12, 4, 's')
    c.pts('J', (11, 3))
    c.pts('G', (9, 6), (13, 6), (8, 5), (14, 5), (10, 0), (12, 0))
    c.pts('A', (1, 14), (0, 15))
    return c.rows()




def merchant_master():
    c = Canvas()
    c.blade(5, 10, 12, 3)
    c.pts('S', (13, 2), (13, 3), (14, 3), (14, 4), (12, 4))
    c.pts('s', (13, 4))
    _hilt(c, 4, 11, 2, 'G', 'L')
    c.pts('J', (14, 1), (15, 0))
    c.pts('G', (14, 1), (3, 13))
    return c.rows()




def monk_master():
    c = Canvas()
    c.line(1, 14, 14, 1, 'W')
    c.line(2, 14, 14, 2, 'L')
    for x, y in ((4, 11), (8, 7), (11, 4)):
        c.p(x, y, 'G')
        c.p(x + 1, y - 1, 'G')
    c.pts('A', (6, 9), (7, 8), (13, 2), (12, 3))
    c.pts('J', (14, 1), (15, 0))
    return c.rows()




def shapeshifter_master():
    c = Canvas()
    c.line(4, 12, 13, 3, 'S')
    c.line(5, 12, 14, 4, 's')
    c.line(3, 11, 11, 3, 'S')
    c.line(2, 10, 8, 4, 'S')
    c.pts('s', (4, 10), (5, 9), (6, 8), (7, 7), (9, 5), (10, 4))
    c.pts('A', (3, 13), (4, 13), (5, 13), (3, 12), (2, 14))
    c.pts('J', (4, 12))
    c.pts('L', (1, 15), (2, 15), (1, 14))
    return c.rows()




def weapon_summoner_master():
    c = Canvas()
    for i, (x, y) in enumerate(((9, 3), (6, 4), (12, 6))):
        c.line(x - 1, y + 1, x + 2, y - 2, 'S')
        c.line(x, y + 1, x + 2, y - 1, 's')
        c.p(x - 2, y + 2, 'G')
    c.line(2, 13, 6, 9, 'L')
    c.pts('G', (1, 14), (3, 11), (4, 10), (5, 11), (4, 12))
    c.pts('J', (4, 11))
    c.pts('+', (8, 8), (9, 9), (11, 11))
    return c.rows()




def hunter_master():
    c = Canvas()
    c.line(5, 10, 13, 2, 'S')
    c.line(5, 11, 14, 2, 's')
    c.pts('S', (14, 3), (13, 3))
    c.pts('B', (4, 11), (3, 12), (2, 13))
    c.line(3, 12, 1, 14, 'L')
    c.pts('G', (5, 12), (6, 11), (4, 10), (3, 11))
    c.pts('J', (4, 11), (0, 15))
    c.pts('A', (7, 8), (9, 6), (11, 4))
    return c.rows()




def tamer_master():
    c = Canvas()
    c.line(2, 13, 7, 8, 'L')
    c.pts('G', (1, 14), (0, 15))
    c.pts('A', (8, 7), (9, 6), (10, 6), (11, 5), (12, 5), (12, 4), (12, 3), (11, 2), (10, 2), (9, 3))
    c.pts('a', (9, 4), (10, 3))
    c.pts('B', (13, 2), (14, 1), (15, 0))
    c.pts('J', (11, 3))
    return c.rows()




# --------------------------------------------------------------------------------------------------------- Focos

def mage_master():
    c = Canvas()
    c.line(1, 14, 10, 5, 'W')
    c.pts('G', (10, 4), (11, 5), (9, 4), (10, 6), (11, 3))
    c.rect(11, 0, 13, 2, 'J')
    c.pts('j', (12, 1))
    c.pts('+', (10, 1), (14, 1), (12, 3), (12, 0))
    c.pts('A', (7, 8), (4, 11), (5, 10))
    return c.rows()




def necromancer_master():
    c = Canvas()
    c.line(2, 14, 10, 6, 'D')
    c.pts('S', (10, 5), (10, 4), (10, 3), (11, 2), (12, 2), (13, 3), (13, 4), (12, 5), (11, 1))
    c.pts('s', (11, 3), (12, 3), (11, 4), (12, 4))
    c.pts('B', (7, 3), (8, 3), (7, 4), (8, 4), (7, 5))
    c.pts('K', (7, 3), (8, 4))
    c.pts('J', (8, 5), (6, 3))
    c.pts('A', (5, 11), (4, 12))
    return c.rows()




def druid_master():
    c = Canvas()
    c.line(1, 14, 8, 7, 'W')
    c.line(8, 7, 9, 4, 'W')
    c.line(9, 4, 12, 3, 'W')
    c.line(8, 7, 12, 8, 'W')
    c.pts('A', (9, 3), (10, 2), (12, 2), (13, 3), (13, 7), (14, 8), (12, 9), (8, 3))
    c.pts('a', (11, 3), (11, 2), (12, 8), (13, 8))
    c.pts('J', (11, 5), (10, 5))
    c.pts('+', (11, 1), (14, 2), (14, 6), (7, 4))
    return c.rows()




def engineer_master():
    c = Canvas()
    c.line(2, 13, 9, 6, 'S')
    c.line(3, 13, 10, 6, 's')
    c.rect(10, 2, 13, 5, 'G')
    c.pts('A', (11, 3), (12, 3), (11, 4), (12, 4))
    c.pts('J', (14, 3), (14, 4), (15, 3))
    c.pts('D', (9, 1), (10, 1), (13, 1), (14, 5), (9, 5))
    c.pts('L', (1, 14), (2, 14), (1, 15))
    return c.rows()




def elementalist_master():
    c = Canvas()
    c.line(1, 14, 8, 7, 'W')
    c.pts('G', (8, 6), (9, 7), (7, 6), (9, 6), (8, 5))
    c.rect(9, 1, 12, 4, 'J')
    c.pts('j', (10, 2), (11, 3))
    c.pts('A', (13, 2), (13, 3), (8, 2), (8, 3))
    c.pts('Y', (10, 0), (11, 0))
    c.pts('+', (13, 1), (14, 3), (9, 5), (12, 5))
    return c.rows()




def gunslinger_master():
    c = Canvas()
    c.rect(5, 6, 13, 7, 'S')
    c.rect(6, 7, 12, 7, 's')
    c.rect(14, 6, 15, 6, 'S')
    c.rect(7, 4, 9, 5, 'G')
    c.pts('J', (8, 5))
    c.pts('S', (4, 7), (4, 8), (5, 8))
    c.line(4, 8, 2, 13, 'L')
    c.line(5, 8, 3, 13, 'L')
    c.pts('G', (3, 8), (6, 8), (6, 9))
    c.pts('A', (13, 5), (12, 5))
    return c.rows()




def bard_master():
    c = Canvas()
    c.line(7, 8, 14, 1, 'W')
    c.pts('G', (14, 0), (15, 0), (15, 1), (13, 2), (12, 3))
    c.rect(4, 8, 6, 8, 'A')
    c.rect(3, 9, 7, 9, 'A')
    c.rect(2, 10, 7, 12, 'A')
    c.rect(3, 13, 6, 13, 'A')
    c.rect(4, 14, 5, 14, 'A')
    c.rect(4, 10, 5, 12, 'a')
    c.pts('K', (4, 11), (5, 11))
    c.pts('J', (4, 13), (5, 13), (3, 13))
    c.line(3, 13, 12, 4, 'G')
    c.pts('W', (1, 14), (1, 15), (2, 14))
    return c.rows()




def warlock_master():
    c = Canvas()
    c.line(2, 13, 9, 6, 'D')
    c.rect(9, 1, 13, 5, 'K')
    c.pts('G', (9, 0), (10, 0), (13, 0), (14, 1), (14, 5), (9, 6), (8, 1), (8, 5))
    c.rect(10, 2, 12, 4, 'J')
    c.pts('K', (11, 3))
    c.pts('A', (7, 8), (4, 11), (5, 10))
    c.pts('+', (11, 0), (15, 3), (7, 3))
    return c.rows()




MASTER = {
    'berserker': berserker_master, 'assassin': assassin_master, 'herbalist': herbalist_master, 'lancer': lancer_master,
    'swordsman': swordsman_master, 'paladin': paladin_master, 'merchant': merchant_master, 'monk': monk_master,
    'shapeshifter': shapeshifter_master, 'weapon_summoner': weapon_summoner_master, 'hunter': hunter_master,
    'tamer': tamer_master, 'mage': mage_master, 'necromancer': necromancer_master, 'druid': druid_master,
    'engineer': engineer_master, 'elementalist': elementalist_master, 'gunslinger': gunslinger_master,
    'bard': bard_master, 'warlock': warlock_master,
}
def templates():
    """Plantillas por nombre: ``<rol>_master`` (16 filas de 16 caracteres)."""
    out = {}
    for role, fn in MASTER.items():
        out[f'{role}_master'] = fn()
    for name, rows in out.items():
        assert len(rows) == 16 and all(len(r) == 16 for r in rows), name
    return out
