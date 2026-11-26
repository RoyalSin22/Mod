"""Textura del Osario Quebrado (256x256, 2 px por unidad) pintada sobre la plantilla UV de Blockbench.

Lee uv_layout.json (volcado de las UV del proyecto abierto) y escribe:
  osario_quebrado.png            textura base
  osario_quebrado_glowmask.png   máscara de brillo (transparente salvo ojos, núcleo, punta y venas)
  osario_glow_preview.png        solo para previsualizar: negro opaco + píxeles que brillan
  osario_uv_x3.png               hoja UV ampliada x3 para revisar

Uso: python gen_osario_texture.py <carpeta_con_uv_layout.json>

En las cajas con UV de caja, las cuatro caras laterales van seguidas: este, norte (frente), oeste, sur (espalda).
En la cara este u=0 es la espalda; en la norte u=0 es el lado +X; la oeste empieza en el frente.
"""
import json
import sys
from pathlib import Path

from PIL import Image

OUT = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).parent
LAY = json.loads((OUT / "uv_layout.json").read_text(encoding="utf-8"))
SCALE = 2  # px por unidad UV
W = H = 256

PAL = {
    "BL": "#D9D6C3",  # hueso claro
    "BM": "#B7B5A6",  # hueso envejecido
    "BS": "#8F8B7C",  # sombra de hueso
    "EA": "#4A3B2E",  # tierra
    "RO": "#6B4E3A",  # raíz / madera
    "DK": "#2F221F",  # oscuro
    "M1": "#4A5F35",  # musgo base
    "M2": "#5C7142",  # musgo seco (luz)
    "M3": "#3B4D28",  # musgo oscuro
    "EC": "#4DE2A6",  # ectoplasma
    "CO": "#7FFFD4",  # núcleo
    # Derivados (transiciones; no brillan)
    "ED": "#2E9E78",  # ectoplasma apagado: halo y bordes
    "BT": "#A9C4AD",  # hueso contaminado de verde
}
GLOW_COLORS = {"EC", "CO"}
GLOW_CUBES = ("h_eye_l", "h_eye_r", "b_core", "l_tip", "l_fore")
MOSS = {"M1", "M2", "M3"}


def rgba(hx):
    hx = hx.lstrip("#")
    return tuple(int(hx[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


COL = {k: rgba(v) for k, v in PAL.items()}
INV = {v: k for k, v in COL.items()}
LEG = {"L": "BL", "M": "BM", "S": "BS", "A": "EA", "R": "RO", "D": "DK", "1": "M1", "2": "M2", "3": "M3",
       "E": "EC", "C": "CO", "e": "ED", "T": "BT"}

img = Image.new("RGBA", (W, H), (0, 0, 0, 0))


class Face:
    def __init__(self, uv):
        x1, y1, x2, y2 = [v * SCALE for v in uv]
        self.x0 = int(round(min(x1, x2)))
        self.y0 = int(round(min(y1, y2)))
        self.w = max(1, int(round(abs(x2 - x1))))
        self.h = max(1, int(round(abs(y2 - y1))))

    def put(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            img.putpixel((self.x0 + x, self.y0 + y), COL[c])

    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return INV.get(img.getpixel((self.x0 + x, self.y0 + y)))
        return None

    def fill(self, c, x=0, y=0, w=None, h=None):
        w = self.w - x if w is None else w
        h = self.h - y if h is None else h
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                self.put(xx, yy, c)

    def art(self, rows, ox=0, oy=0, flip=False):
        for y, row in enumerate(rows):
            if flip:
                row = row[::-1]
            for x, ch in enumerate(row):
                if ch != ".":
                    self.put(ox + x, oy + y, LEG[ch])


class Strip:
    """Las cuatro caras laterales seguidas (este, norte, oeste, sur): u continua alrededor del cubo."""

    def __init__(self, faces):
        self.spans = []
        off = 0
        for f in faces:
            self.spans.append((f, off))
            off += f.w
        self.w = off
        self.h = faces[0].h

    def _at(self, x):
        x %= self.w
        for f, off in self.spans:
            if off <= x < off + f.w:
                return f, x - off
        raise IndexError(x)

    def put(self, x, y, c):
        f, lx = self._at(x)
        f.put(lx, y, c)

    def get(self, x, y):
        f, lx = self._at(x)
        return f.get(lx, y)

    def fill(self, c, x=0, y=0, w=None, h=None):
        w = self.w if w is None else w
        h = self.h - y if h is None else h
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                self.put(xx, yy, c)

    def art(self, rows, ox=0, oy=0, flip=False):
        for y, row in enumerate(rows):
            if flip:
                row = row[::-1]
            for x, ch in enumerate(row):
                if ch != ".":
                    self.put(ox + x, oy + y, LEG[ch])


class Cube:
    def __init__(self, d):
        self.name = d["name"]
        self.f = {k: Face(v["uv"]) for k, v in d["faces"].items()}
        self.strip = Strip([self.f[k] for k in ("east", "north", "west", "south")])
        self.eo = 0
        self.no = self.f["east"].w
        self.wo = self.no + self.f["north"].w
        self.so = self.wo + self.f["west"].w


C = {d["name"]: Cube(d) for d in LAY["cubes"]}

# ---------------------------------------------------------------- motivos pintados a mano

MOSS_A = [".22..", "2112.", "21113", ".133."]      # sobre hueso
MOSS_B = [".21.", "2113", ".33."]
MOSS_C = ["21.", ".13"]
MOSS_D = ["2.", "12", "13", ".3"]                   # colgante
MOSS_EARTH = [".22.", "2212", ".1D."]               # sobre tierra: domina el tono claro para que se vea

BONE_KNOB = ["LLM..", "LMS..", ".LM..", ".LMS.", "..LM.", "..LMD", "...D."]   # cabeza de hueso que se hunde
BONE_SPLINTER = [".MS", "LMS", "LMS", ".LM", ".LS", "..L", "..D"]              # astilla rota que se afila
BONE_RIB = [".LLLL.", "LMSSML", "MS..SM", "D....D"]                            # trozo de costilla en arco
BONE_JOINT = ["LLMM.", "LLMMS", "LMMMS", "MMMSS", ".SSS.", ".DDD."]            # cabeza de articulación

WOOD_STUMP = [      # tabla podrida: vetas cortadas, hueco de podredumbre y musgo encima
    "22.1..",
    "R2R.RR",
    "RRRDRR",
    "RDRDRR",
    "RDRRRR",
    "RDRRDR",
    "RRRRDR",
    "RDDRDR",
    "RRRRRR",
    "DDDDDD",
]
WOOD_BRANCH = ["RRRR", "RDDR", "RRRR", "RDRR", "RRDR", "RDRR", "DDDD"]        # rama cortada: anillo y veta


# ---------------------------------------------------------------- utilidades

def cap(f, marrow=True):
    """Tapa de un hueso largo (extremo): borde en sombra y médula oscura."""
    f.fill("BS")
    if f.w > 2 and f.h > 2:
        f.fill("BM", 1, 1, f.w - 2, f.h - 2)
    if marrow and f.w >= 6 and f.h >= 6:
        f.fill("DK", f.w // 2 - 1, f.h // 2 - 1, 2, 2)


def chip(target, x, y):
    """Desconchadura: grupo de sombra de 2x2 con 1 px de luz en el lado iluminado."""
    target.put(x, y, "BL")
    target.put(x + 1, y, "BS")
    target.put(x, y + 1, "BS")
    target.put(x + 1, y + 1, "BS")


def long_bone(c, knob=3, gap=None, wear=()):
    """Hueso largo: cabezas claras y redondeadas, caña más estrecha con los bordes en sombra.

    gap: {"north": columna, "south": columna} para pintar dos huesos paralelos (radio y cúbito, tibia y peroné).
    wear: lista de (cara, x, y, tipo) con desgaste propio para que ninguna cara sea copia de otra.
    """
    for name in ("east", "north", "west", "south"):
        f = c.f[name]
        w, h = f.w, f.h
        f.fill("BM")
        f.fill("BL", 0, 0, w, knob - 1)          # cabeza superior
        f.fill("BS", w - 1, 0, 1, knob)
        f.put(0, 0, "BM")                        # esquinas redondeadas
        f.put(w - 1, 0, "BS")
        f.fill("BS", 0, knob, w, 1)              # sombra bajo la cabeza
        for y in range(knob + 1, h - knob - 1):  # caña: más estrecha que las cabezas
            if w >= 8:
                f.put(0, y, "DK")
                f.put(1, y, "BS")
                f.put(2, y, "BL")
                f.put(w - 2, y, "BS")
                f.put(w - 1, y, "DK")
            else:
                f.put(0, y, "BS")
                f.put(1, y, "BL")
                f.put(w - 1, y, "BS")
        f.fill("BL", 0, h - knob - 1, w, 1)      # cabeza inferior
        f.fill("BS", w - 1, h - knob - 1, 1, knob)
        f.fill("BS", 0, h - 1, w, 1)             # junta
        if name in ("north", "south"):           # dos cóndilos en la cabeza inferior
            f.put(w // 2, h - 3, "BS")
            f.put(w // 2, h - 2, "BS")
            f.put(w // 2, h - 1, "DK")
        if gap and name in gap:
            g = gap[name]
            for y in range(knob + 1, h - knob - 1):
                f.put(g - 1, y, "BS")
                f.put(g, y, "DK")
                f.put(g + 1, y, "BL")
    for name, x, y, kind in wear:
        f = c.f[name]
        if kind == "chip":
            chip(f, x, y)
        elif kind == "light":
            f.put(x, y, "BL")
            f.put(x + 1, y, "BL")
        elif kind == "shade":
            f.put(x, y, "BS")
            f.put(x, y + 1, "BS")
    cap(c.f["up"])
    cap(c.f["down"])


def line_pts(pts):
    out = []
    for (x1, y1), (x2, y2) in zip(pts, pts[1:]):
        n = max(abs(x2 - x1), abs(y2 - y1), 1)
        for i in range(n + 1):
            p = (x1 + round((x2 - x1) * i / n), y1 + round((y2 - y1) * i / n))
            if not out or out[-1] != p:
                out.append(p)
    return out


def crack(target, pts, c="EA"):
    """Grieta de 1 px. En color tierra, el primer y el último píxel van en sombra de hueso (pelo fino)."""
    px = line_pts(pts)
    for i, (x, y) in enumerate(px):
        hair = c == "EA" and len(px) > 3 and i in (0, len(px) - 1)
        target.put(x, y, "BS" if hair else c)


def root_path(st, pts, ths):
    """Raíz que serpentea: polilínea con grosor por tramo y sombra debajo (tramos tumbados) o a la derecha."""
    for i, ((x1, y1), (x2, y2)) in enumerate(zip(pts, pts[1:])):
        th = ths[min(i, len(ths) - 1)]
        flat = abs(x2 - x1) > abs(y2 - y1)
        for x, y in line_pts([(x1, y1), (x2, y2)]):
            if flat:
                for t in range(th):
                    st.put(x, y + t, "RO")
                if st.get(x, y + th) not in ("RO", None):
                    st.put(x, y + th, "DK")
            else:
                for t in range(th):
                    st.put(x + t, y, "RO")
                if st.get(x + th, y) != "RO":
                    st.put(x + th, y, "DK")


def pits(st, pts):
    """Huecos oscuros de la tierra: grupos de 2 a 3 px."""
    for x, y, kind in pts:
        st.put(x, y, "DK")
        st.put(x + 1, y, "DK")
        if kind:
            st.put(x, y + 1, "DK")


def dirt_bottom(st, base, profile, clods=()):
    """Barro que sube desde abajo con borde escalonado. profile: desplazamiento por columna (negativo = más alto)."""
    n = len(profile)
    for u in range(st.w):
        top = base + profile[u % n]
        for y in range(top, st.h):
            st.put(u, y, "EA")
    for u in range(st.w):                 # terrón iluminado solo en crestas de 2 px o más
        p = profile[u % n]
        if p <= 0 and (profile[(u + 1) % n] == p or profile[(u - 1) % n] == p):
            st.put(u, base + p, "RO")
    for x, y in clods:                    # salpicaduras sueltas, siempre de 2 px
        st.put(x, y, "EA")
        st.put(x + 1, y, "EA")


# ---------------------------------------------------------------- cabeza

def paint_head():
    sk = C["h_skull"]
    front = [
        "MLLLLLLLLLLLLLLLLLLLLLLM",
        "LLLLLLLLLLLLLSLLLLLLLLLM",
        "LLLLLLLLLLLLLALLLLLLLLMM",
        "LLLLLLLLLLLLAALLLLLLLLMM",
        "LLLLLLLLLLLLAMLLLLLLLMMM",
        "LLLMMLLLLLLAAMMLLLLLMMMS",
        "LLMMMMMMLLLSMMMMMMMMMMMS",
        "LMMMMMMMMMMMMMMMMMMMMMMS",
        "MMSSSSSSSSMMMMSSSSSSSSMS",
        "MMSDDDDDDSMMMMSDDDDDDSMS",
        "MMSDDDDDDSMMMMSDDDDDDSMS",
        "MMSDDDDDDSMMMMSDDDDDDSSS",
        "MSSDDDDDDSMMMMSDDDDDDSSS",
        "MSSDDDDDDSMDDMSDDDDDDSSS",
        "MSSDDDDDDSMDDMSDDDDDDSSS",
        "MMSSSSSSSMDDDDMSSSSSSSMS",
        "SMMMMMMMMMMMMMMMMMMMMMMS",
        "SSLLDLLDLLLDLLLDLLDSDDSS",     # dentadura irregular: incisivos anchos, una pieza perdida
        "SSLLDLDDLLLDLLLDLMDDDDSS",     # un diente mellado y otro envejecido
        "SDDDDDDDDDDDDDDDDDDDDDDS",
    ]
    sk.f["north"].art(front)

    side = [  # cara este: u=0 nuca, u=23 frente. La oeste es el espejo.
        "MLLLLLLLLLLLLLLLLLLLLLLM",
        "LLLLLLLLSLLLLLLLLLLLLLLM",
        "LLLLLLLLALLLLLLLLLLLLLMM",
        "LLLLLLLLLALLLLLLLLLLLLMM",
        "MLLLLLLLLALLLLLLLLLLLMMM",
        "MMLLLLLLAALLLLLLLLLLMMMM",
        "MMMLLLLLAMMMMMMMMMMMMMMS",
        "MMMMMMMMMSMMMMMMMMMMMMMS",
        "SMMMMMMMMMMMMMMMSSSSSSMS",
        "SMMMMMMMMMMMMMMSSSSSSSSS",
        "SSMMMMMMMMMMMMMSSSDDSSSS",
        "SSMMMMMMMMMMMMMSSSDDSSSS",
        "SSSMMMMMMMMMMMMMSSSSSSMS",
        "SSSMMMMMMMMMMMMMMMMMMMMS",
        "SSSSMMMMMLLLLLLLLLLLLLMS",
        "SSSSSMMMMSSSSSSSSSSSSSSS",
        "SSSSSSMMMMMMMMMMMMMMMMMS",
        "SSSSSSSSSSSSMLLDLMDLLDLS",
        "SSSSSSSSSSSSMLLDLMDDLDLS",
        "SSSSSSSSDDDDDDDDDDDDDDDS",
    ]
    sk.f["east"].art(side)
    sk.f["west"].art(side, flip=True)
    chip(sk.f["west"], 9, 11)                     # el lado oeste no es copia exacta
    sk.f["west"].put(14, 3, "BM")
    sk.f["west"].put(15, 4, "BM")

    back = [
        "MLLLLLLLLLLLLLLLLLLLLLLM",
        "LLLLLLLLLLLLLLLLLLLLLLLM",
        "LLLLLLLLLLLLLLLLLLSLLLMM",
        "LLLLLLLLLLLLLLLLLLALLLMM",
        "MLLLLLLLLLLLLLLLLAALLMMM",
        "MMLLLLLLLLLLLLLLLALLMMMM",
        "MMMLLLLLLLLSSLLLAAMMMMMS",     # arranque de la sutura en V invertida
        "MMMMMMMMSSSMMSSSAMMMMMMS",
        "MMMMMMSSMMMMMMMMSSSMMMMS",
        "MMMSSSMMMMMMMMMMMMMSSMMS",
        "SMMMMMMMMMMMMMMMMMMMMSSS",
        "SMMMMMLSMMMMMMMMMMMMMMSS",     # desconchadura
        "SSMMMMSSMMMMMMMMMMMMMSSS",
        "SSMMMMMMMMSSSSMMMMMMMSSS",
        "SSSMMMMMMSSSSSSMMMMMSSSS",
        "SSSSMMMMMSSSSSSMMMMSSSSS",
        "SSSSSMMMMMSSSSMMMMSSSSSS",
        "SSSSSSSMMMMMMMMMMSSSSSSS",
        "SSSSSSSSSSSSSSSSSSSSSSSS",
        "SDDDDDDDDDDDDDDDDDDDDDDS",
    ]
    sk.f["south"].art(back)

    top = sk.f["up"]          # 24x24; el borde inferior toca la cara frontal
    top.fill("BL")
    top.fill("BM", 0, 0, 24, 1)
    top.fill("BM", 0, 0, 1, 24)
    top.fill("BM", 23, 0, 1, 24)
    crack(top, [(13, 23), (13, 20), (12, 18), (12, 14), (11, 11), (12, 8), (12, 3), (11, 0)])   # sutura central
    crack(top, [(1, 9), (6, 10), (9, 9), (11, 11)])                                              # suturas laterales
    crack(top, [(12, 14), (16, 13), (19, 15), (22, 14)])
    # placas del cráneo apoyadas en las suturas (borde escalonado, no rectángulos)
    for x, y, w in ((19, 2, 3), (18, 3, 5), (18, 4, 5), (19, 5, 4), (20, 6, 3), (21, 7, 2)):
        top.fill("BM", x, y, w, 1)
    for x, y, w in ((1, 11, 4), (1, 12, 5), (1, 13, 5), (1, 14, 3), (1, 15, 2)):
        top.fill("BM", x, y, w, 1)
    top.put(18, 16, "BS")
    top.put(19, 16, "BS")
    top.put(7, 5, "BM")
    top.put(8, 5, "BM")

    bot = sk.f["down"]
    bot.fill("BS")
    bot.fill("DK", 1, 1, 22, 22)
    bot.fill("BS", 4, 4, 16, 16)
    bot.fill("DK", 9, 8, 6, 6)           # agujero del cuello
    bot.fill("BM", 8, 7, 8, 1)

    eye = ["EEEE", "ECCE", "ECCE", "EEEE"]
    for n in ("h_eye_l", "h_eye_r"):
        e = C[n]
        for f in e.f.values():
            f.fill("EC")
        e.f["north"].art(eye)
        e.f["south"].fill("DK")


# ---------------------------------------------------------------- pecho y torso

def vertebrae(st, x, y0, y1, w=4):
    """Columna: vértebras de 3 px con disco en sombra (periodo distinto al de las costillas)."""
    for y in range(y0, y1):
        disc = (y - y0) % 4 == 3
        for i in range(w):
            st.put(x + i, y, "BS" if disc else "BM")
        if not disc:
            st.put(x, y, "BL")
            st.put(x + w - 1, y, "BS")


def rib_off(c, u):
    """Arco de las costillas: bajan hacia el frente, suben al llegar al esternón y a la columna."""
    if u < 10:
        return 0
    if u < c.no:
        return 1
    if u < c.wo:
        return 0 if 8 <= u - c.no < 20 else 1
    if u < c.wo + 10:
        return 1
    if u < c.so:
        return 0
    return -1 if (9 <= u - c.so <= 11 or 16 <= u - c.so <= 18) else 0


def face_local(c, u):
    for f, off in c.strip.spans:
        if off <= u < off + f.w:
            return u - off, f.w
    raise IndexError(u)


def paint_ribs(c, rows, skip):
    """Costillas de 2 px (luz arriba, sombra abajo) con volumen en las aristas y punta junto al esternón."""
    st = c.strip
    for u in range(st.w):
        lx, fw = face_local(c, u)
        north = c.no <= u < c.wo
        south = u >= c.so
        if (north or south) and 12 <= lx <= 15:
            continue                                   # esternón o columna
        for i, y in enumerate(rows):
            if skip(u, y):
                continue
            yy = y + rib_off(c, u)
            near_edge = lx < 3 or lx >= fw - 3
            tip = (north or south) and lx in (11, 16)
            if tip and i % 2 == 1:
                continue                               # costillas alternas se despegan del esternón
            st.put(u, yy, "BM" if (near_edge or tip) else "BL")
            if not tip:
                st.put(u, yy + 1, "BS")


def paint_chest():
    ch = C["b_chest_up"]
    st = ch.strip                     # este 0-19, norte 20-47, oeste 48-67, sur 68-95; alto 12
    st.fill("DK")
    st.fill("BM", 0, 0, st.w, 2)      # cintura escapular (clavículas y omóplatos)
    st.fill("BL", 0, 0, st.w, 1)
    paint_ribs(ch, (3, 6, 9), lambda u, y: False)
    for y in range(0, 12):            # esternón
        st.put(ch.no + 12, y, "BL")
        st.put(ch.no + 13, y, "BM")
        st.put(ch.no + 14, y, "BM")
        st.put(ch.no + 15, y, "BS")
    st.put(ch.no + 13, 5, "BS")       # segmentos del esternón
    st.put(ch.no + 14, 5, "BS")
    st.put(ch.no + 13, 9, "BS")
    st.put(ch.no + 14, 9, "BS")
    vertebrae(st, ch.so + 12, 0, 12)
    # costillas rotas: es el Osario "Quebrado"
    n = ch.no                          # segunda costilla del frente, lado izquierdo de la imagen
    for x in range(3, 7):
        st.put(n + x, 7, "DK")
        st.put(n + x, 8, "DK")
    st.put(n + 2, 8, "DK")
    st.put(n + 3, 8, "BS")
    st.put(n + 7, 7, "BM")
    st.put(n + 6, 8, "BS")
    s = ch.so                          # tercera costilla de la espalda
    for x in range(20, 24):
        st.put(s + x, 9, "DK")
        st.put(s + x, 10, "DK")
    st.put(s + 19, 10, "DK")
    st.put(s + 20, 10, "BS")
    st.put(s + 24, 9, "BM")
    st.put(s + 23, 10, "BS")
    # contaminación leve de ectoplasma: puntas de costilla teñidas y resplandor apagado en los huecos bajos
    for x, y in ((9, 6), (10, 6), (17, 6), (18, 6), (9, 9), (10, 9), (17, 9), (18, 9)):
        st.put(n + x, y, "BT")
    for x, y in ((11, 8), (16, 8), (10, 11), (11, 11), (16, 11), (17, 11)):
        st.put(n + x, y, "ED")
    for base in (ch.eo, ch.wo):        # hombros: junta en sombra donde encajan los brazos
        st.fill("BS", base + 7, 2, 6, 1)
    crack(st, [(n + 4, 0), (n + 5, 1)])
    crack(st, [(s + 22, 0), (s + 21, 1)])

    up = ch.f["up"]                    # casi oculta por el cráneo
    up.fill("BM")
    up.fill("BL", 0, 0, up.w, 1)
    up.fill("BS", 0, up.h - 1, up.w, 1)
    up.fill("BS", 10, 6, 8, 8)
    up.fill("DK", 12, 8, 4, 4)         # hueco del cuello
    dn = ch.f["down"]                  # se ve por detrás al inclinarse el pecho
    dn.fill("DK")
    dn.fill("BS", 12, 0, 4, dn.h)

    sp = C["b_spine"]                  # joroba: vértebras que asoman por la espalda
    for k in ("east", "west", "south", "north"):
        f = sp.f[k]
        f.fill("BM")
        f.fill("BL", 0, 0, f.w, 1)
        f.fill("BS", 0, 3, f.w, 1)
        f.fill("BL", 0, 4, f.w, 1)
        f.fill("BS", 0, 7, f.w, 1)
        f.fill("BS", f.w - 1, 0, 1, f.h)
    sp.f["south"].fill("BL", 2, 1, 2, 2)
    sp.f["south"].fill("BL", 2, 5, 2, 2)
    sp.f["up"].fill("BM")
    sp.f["up"].fill("BL", 0, 0, sp.f["up"].w, 1)
    sp.f["down"].fill("BS")


def paint_torso():
    t = C["b_torso"]
    st = t.strip                       # este 0-19, norte 20-47, oeste 48-67, sur 68-95; alto 20
    st.fill("DK")
    # costillas flotantes: cada una más corta; por delante dejan libre el centro, donde va el núcleo
    reach = {1: 9, 4: 7, 7: 5}

    def skip(u, y):
        return t.no <= u < t.wo and reach[y] <= u - t.no < 28 - reach[y]

    paint_ribs(t, (1, 4, 7), skip)
    for y, r in reach.items():         # terminan en punta: sin sombra en los 2 últimos px
        for x in (r - 2, r - 1, 28 - r, 28 - r + 1):
            yy = y + rib_off(t, t.no + x)
            st.put(t.no + x, yy + 1, "DK")
            st.put(t.no + x, yy, "BM")
    # columna: entera por la espalda, hundida y en sombra por delante bajo el núcleo
    vertebrae(st, t.so + 12, 0, 14)
    for y in range(12, 16):
        for dx in (12, 13, 14, 15):
            st.put(t.no + dx, y, "DK" if y == 14 else "BS")

    # pelvis: crestas ilíacas altas en los costados que bajan hacia el pubis
    def pelvis_top(u):
        if t.no <= u < t.wo:
            x = u - t.no
            x = min(x, 27 - x)
            return 13 if x <= 3 else (14 if x <= 9 else 16)
        if u >= t.so:
            x = u - t.so
            x = min(x, 27 - x)
            return 13 if x <= 4 else 14
        return 13

    for u in range(st.w):
        top = pelvis_top(u)
        for y in range(top, 20):
            st.put(u, y, "BM")
        st.put(u, top, "BL")
        st.put(u, 19, "BS")
    n = t.no
    for x in (0, 1, 2, 3, 24, 25, 26, 27):                 # ala de la cresta: 2 px de luz
        st.put(n + x, 14, "BL")
    for x in (4, 5, 6, 7, 20, 21, 22, 23):                 # sombra bajo la cresta
        st.put(n + x, 15, "BS")
    for x0, cut in ((6, 6), (19, 21)):                     # agujeros obturadores 3x3 con la esquina exterior recortada
        for yy in (15, 16, 17):
            for xx in range(x0, x0 + 3):
                if not (yy == 15 and xx == cut):
                    st.put(n + xx, yy, "DK")
    st.put(n + 8, 18, "BL")
    st.put(n + 19, 18, "BL")
    for x in (12, 13, 14, 15):                             # arco púbico y sínfisis
        st.put(n + x, 19, "DK")
    st.put(n + 13, 18, "DK")
    st.put(n + 14, 18, "DK")
    st.put(n + 13, 17, "DK")
    for u in (t.eo, t.wo):                                 # sombra del costado
        st.fill("BS", u + 16, 14, 4, 5)
        st.fill("BS", u, 14, 2, 5)
    st.fill("BS", t.so + 11, 15, 6, 4)                     # sacro
    st.fill("BM", t.so + 12, 15, 4, 3)
    st.put(t.so + 13, 16, "DK")
    st.put(t.so + 14, 17, "DK")
    crack(st, [(n + 23, 14), (n + 24, 16), (n + 24, 18)])
    crack(st, [(t.so + 5, 13), (t.so + 6, 15), (t.so + 5, 17)])
    chip(st, t.eo + 9, 15)
    # suciedad leve: dos manchas de borde escalonado y tamaños distintos por delante, otras dos detrás y al costado
    for x, y in ((1, 19), (2, 19), (3, 19), (4, 19), (1, 18), (2, 18), (3, 18), (22, 19), (23, 19)):
        st.put(n + x, y, "EA")
    for x, y in ((6, 19), (7, 19), (8, 19), (7, 18)):
        st.put(t.wo + x, y, "EA")
    st.put(t.so + 20, 19, "EA")
    st.put(t.so + 21, 19, "EA")
    # resplandor apagado detrás del núcleo (el cubo ocupa x 9-18, y 2-11 de la cara frontal): solo en el centro de cada lado
    st.fill("DK", n + 8, 1, 12, 12)
    st.fill("ED", n + 11, 2, 6, 10)
    st.fill("ED", n + 9, 4, 10, 6)
    st.fill("ED", n + 10, 3, 8, 8)
    for y in range(4, 10):
        st.put(n + 8, y, "ED")
        st.put(n + 19, y, "ED")
    for x in range(11, 17):
        st.put(n + x, 1, "ED")
        st.put(n + x, 12, "ED")
    for x, y0, y1 in ((11, 13, 15), (16, 13, 17), (13, 13, 14)):   # goteo apagado, largos distintos
        for y in range(y0, y1):
            st.put(n + x, y, "ED")
    for x in (7, 20):                                              # puntas de costilla teñidas junto al núcleo
        st.put(n + x, 5, "BT")

    up = t.f["up"]
    up.fill("DK")
    up.fill("BS", 12, 0, 4, 6)          # columna vista por el hueco de la espalda
    up.fill("BM", 12, 1, 4, 1)
    up.fill("BM", 12, 4, 4, 1)
    dn = t.f["down"]
    dn.fill("BS")
    dn.fill("BM", 1, 1, dn.w - 2, dn.h - 2)
    dn.fill("DK", 3, 6, 6, 8)
    dn.fill("DK", 19, 6, 6, 8)
    dn.fill("BS", 11, 2, 6, 16)

    core = C["b_core"]                  # masa redonda, con la luz arriba a la izquierda y media luna apagada abajo
    core.f["north"].art([
        "DDDEEEEDDD",
        "DDEEEEEEDD",
        "DEECCEEEED",
        "EECCCCEEEE",
        "EECCCCCEEE",
        "EECCCCEEeE",
        "EEECCEEeeE",
        "DEEEEEeeED",
        "DDEEeeeEDD",
        "DDDEEEEDDD",
    ])
    for k in ("east", "west"):
        f = core.f[k]
        f.fill("DK")
        f.fill("EC", 0, 3, f.w, 4)
    for k in ("up", "down"):
        f = core.f[k]
        f.fill("DK")
        f.fill("EC", 3, 0, 4, f.h)
    core.f["south"].fill("DK")


# ---------------------------------------------------------------- brazo derecho (hueso limpio)

def paint_right_arm():
    ru = C["r_upper"]
    long_bone(ru, knob=4, wear=(("south", 3, 17, "chip"), ("east", 4, 9, "light"), ("west", 5, 20, "shade")))
    # una sola grieta larga que nace bajo la cabeza del húmero, con una rama corta
    crack(ru.strip, [(ru.no + 6, 5), (ru.no + 6, 8), (ru.no + 5, 10), (ru.no + 5, 13)])
    ru.strip.put(ru.no + 6, 11, "BS")
    ru.strip.put(ru.no + 7, 12, "BS")
    rf = C["r_fore"]
    long_bone(rf, knob=3, gap={"north": 4, "south": 3},
              wear=(("east", 4, 12, "shade"), ("west", 3, 16, "light")))
    crack(rf.strip, [(rf.wo + 3, 4), (rf.wo + 3, 6)], c="BS")

    hd = C["r_hand"]                   # 6x12 px por cara: muñeca arriba, dedos abajo
    for k in ("east", "north", "west", "south"):
        f = hd.f[k]
        f.fill("BM")
        f.fill("BL", 0, 0, f.w, 1)
        f.fill("BS", 0, 3, f.w, 1)     # línea de la muñeca
        f.put(1, 2, "BS")
        f.put(4, 1, "BS")
        for y in range(4, 12):         # dedos: tres por delante y por detrás, dos de perfil
            if k in ("north", "south"):
                for x, c in enumerate(("BL", "DK", "BL", "BM", "DK", "BM")):
                    f.put(x, y, c)
            else:
                for x, c in enumerate(("BL", "BM", "DK", "BL", "BM", "BS")):
                    f.put(x, y, c)
        for x in range(f.w):           # nudillos y puntas
            if f.get(x, 7) != "DK":
                f.put(x, 7, "BS")
            if f.get(x, 11) != "DK":
                f.put(x, 11, "BS")
    hd.f["north"].put(5, 10, "DK")     # un dedo más corto
    hd.f["north"].put(5, 11, "DK")
    hd.f["north"].put(5, 9, "BS")
    cap(hd.f["up"], marrow=False)
    hd.f["down"].fill("BS")
    hd.f["down"].fill("DK", 1, 0, 1, hd.f["down"].h)
    hd.f["down"].fill("DK", 4, 0, 1, hd.f["down"].h)


# ---------------------------------------------------------------- brazo izquierdo arrastrado

def paint_drag_arm():
    # --- hombro: hueso arriba, la tierra lo va cubriendo con un borde distinto en cada cara
    up = C["l_upper"]
    st = up.strip                      # 16 px por cara, 64 de vuelta; alto 24
    for k in ("east", "north", "west", "south"):
        f = up.f[k]
        f.fill("BM")
        f.fill("BL", 0, 0, f.w, 3)
        f.fill("BL", 0, 3, 2, 4)
        f.fill("BS", f.w - 1, 0, 1, 13)
    edge = (
        [1, 1, 0, 0, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0]            # este: borde medio
        + [0, -1, -2, -4, -4, -3, -3, -1, 0, 1, 2, 2, 3, 3, 3, 3]     # norte: lengua alta de tierra
        + [3, 3, 4, 4, 4, 3, 3, 4, 4, 4, 3, 3, 3, 2, 2, 1]            # oeste: borde bajo, más hueso a la vista
        + [1, 1, 0, 0, 1, 1, 0, -1, -2, -2, -3, -3, -3, -2, 0, 1]     # sur: otra lengua, desplazada
    )
    for u in range(st.w):
        top = 8 + edge[u]
        st.put(u, top - 1, "BS")       # sombra del hueso sobre la tierra
        for y in range(top, 24):
            st.put(u, y, "EA")
    for u in range(st.w):              # terrones iluminados solo en crestas de 2 px o más
        e = edge[u]
        if e <= 0 and (edge[(u + 1) % 64] == e or edge[(u - 1) % 64] == e):
            st.put(u, 8 + e, "RO")
    crack(st, [(up.no + 9, 0), (up.no + 9, 2), (up.no + 10, 4)])
    crack(st, [(up.wo + 9, 3), (up.wo + 10, 6), (up.wo + 10, 9)])
    chip(st, up.wo + 3, 5)
    crack(st, [(up.so + 4, 0), (up.so + 4, 3), (up.so + 5, 5)])
    # dos raíces principales y una menor: se afinan, cambian de rumbo, doblan la arista y se bifurcan
    root_path(st, [(2, 10), (4, 13), (9, 15), (15, 16), (19, 20), (20, 23)], [2, 2, 2, 1, 1])
    root_path(st, [(9, 15), (11, 18), (11, 20)], [1])
    root_path(st, [(40, 12), (38, 15), (33, 17), (27, 18), (25, 21), (26, 23)], [2, 2, 2, 1, 1])
    root_path(st, [(33, 17), (31, 20), (32, 22)], [1])
    root_path(st, [(54, 9), (55, 13), (59, 16), (63, 19), (63, 23)], [2, 2, 1, 1])
    st.art(BONE_KNOB, up.no + 10, 12)              # hueso al descubierto: un motivo distinto en cada cara
    st.art(BONE_RIB, up.eo + 6, 17)
    st.art(BONE_SPLINTER, up.so + 2, 12)
    st.art(WOOD_BRANCH, up.wo + 1, 15)             # rama cortada en la zona baja del costado
    pits(st, [(8, 20, 1), (22, 14, 0), (29, 21, 1), (44, 16, 0), (36, 22, 0), (50, 20, 1), (60, 12, 0)])
    st.art(MOSS_EARTH, up.no + 3, 2)               # musgo en lo alto de la lengua de tierra
    st.art(MOSS_D, up.wo + 6, 7)                   # y colgando sobre el hueso
    st.art(MOSS_B, up.so + 9, 3)
    st.art(MOSS_C, up.eo + 5, 5)
    st.fill("DK", 0, 23, st.w, 1)                  # junta con el codo
    st.put(20, 23, "RO")                           # las raíces principales atraviesan la junta
    st.put(21, 23, "RO")
    st.put(26, 23, "RO")
    st.put(63, 23, "RO")

    top = up.f["up"]                   # 16x16: cabeza rota del húmero, con tierra que entra por dos lados y musgo
    top.art([
        "SMMMMMMMMMRAAAAD",
        "MMLLLLLLLLMRAAAS",
        "MLLLLLLLLLLMAASS",
        "MLLLLLLLLLLLMASS",
        "MLLLLLLLLLLLLMSS",
        "MLLLLLLLSSLLLMSS",
        "MLLLLLLLSDLLLMSS",
        "MLLLLLLLLLLLLMSS",
        "RLLLLLLLLLLLLMSS",
        "ARLLLLLLLLLLMMSS",
        "AARLLLLLLLLMMSSS",
        "AAAMLLLLLMMMSSSS",
        "AAMMMMMMMMMMSSSS",
        "AMMMMMMMMMMMSSSS",
        "MMMMSSSSSSSSSSSD",
        "SSSSSSSSSSSSSSDD",
    ])
    crack(top, [(5, 0), (5, 2), (6, 4)])
    top.art(MOSS_A, 3, 1, flip=True)
    top.art(MOSS_B, 4, 8)
    top.art(MOSS_C, 10, 9)
    dn = up.f["down"]
    dn.fill("EA")
    dn.fill("DK", 3, 3, 10, 10)
    dn.fill("RO", 1, 6, 2, 1)
    dn.fill("RO", 12, 1, 1, 2)

    # --- codo: tierra con raíces que siguen las del hombro, tabla podrida, cabeza de hueso y costilla suelta
    el = C["l_elbow"]
    st = el.strip                      # 14 px por cara, 56 de vuelta; alto 20
    st.fill("EA")
    st.fill("DK", 0, 0, st.w, 1)
    st.art(BONE_JOINT, el.no + 8, 3)               # cabeza del codo; la raíz le pasa por encima
    root_path(st, [(17, 0), (17, 3), (13, 5), (8, 6), (5, 10), (6, 14), (4, 19)], [2, 2, 2, 2, 1, 1])
    root_path(st, [(8, 6), (9, 10), (11, 12)], [1])
    root_path(st, [(23, 0), (25, 4), (30, 6), (36, 7), (39, 11), (38, 15), (40, 19)], [2, 2, 2, 2, 1, 1])
    root_path(st, [(36, 7), (41, 8), (44, 10)], [1])
    root_path(st, [(55, 0), (54, 4), (50, 6), (47, 10), (48, 15)], [2, 2, 1, 1])
    st.art(WOOD_STUMP, el.eo + 7, 9)               # tabla podrida con musgo encima
    st.art(BONE_RIB, el.wo + 3, 13)
    st.art(BONE_KNOB, el.so + 1, 10, flip=True)
    pits(st, [(1, 4, 1), (20, 12, 0), (25, 16, 1), (33, 14, 0), (45, 4, 0), (53, 12, 1)])
    st.art(MOSS_EARTH, el.no + 2, 14)
    st.art(MOSS_EARTH, el.so + 8, 2)
    st.fill("DK", 0, 19, st.w, 1)
    st.put(4, 19, "RO")
    st.put(40, 19, "RO")
    f = el.f["up"]
    f.fill("EA")
    f.fill("DK", 0, 0, f.w, 1)
    f.fill("RO", 4, 5, 5, 2)
    f = el.f["down"]
    f.fill("EA")
    f.fill("DK", 3, 3, 8, 8)
    f.fill("RO", 0, 4, 2, 1)
    f.fill("RO", 9, 12, 2, 1)

    # --- antebrazo: tierra y raíces con venas finas de ectoplasma (pocas; no es una superficie brillante)
    fo = C["l_fore"]
    st = fo.strip                      # 12 px por cara, 48 de vuelta; alto 16
    st.fill("EA")
    st.fill("DK", 0, 0, st.w, 1)
    root_path(st, [(3, 0), (4, 4), (8, 6), (13, 7), (15, 11), (14, 15)], [2, 2, 1, 1, 1])
    root_path(st, [(8, 6), (9, 9), (8, 11)], [1])
    root_path(st, [(35, 0), (34, 3), (30, 5), (26, 6), (25, 10), (27, 15)], [2, 2, 1, 1, 1])
    st.art(BONE_SPLINTER, fo.so + 6, 4)
    st.art(WOOD_BRANCH, fo.so + 1, 2)
    pits(st, [(0, 9, 0), (10, 12, 1), (22, 13, 0), (38, 4, 0), (41, 13, 0)])
    veins = [
        [(fo.no + 7, 2), (fo.no + 7, 4), (fo.no + 8, 5), (fo.no + 8, 8), (fo.no + 9, 9), (fo.no + 9, 12)],
        [(fo.eo + 5, 7), (fo.eo + 5, 9), (fo.eo + 6, 10), (fo.eo + 6, 13)],
        [(fo.wo + 7, 8), (fo.wo + 7, 10), (fo.wo + 8, 11), (fo.wo + 8, 13)],
    ]
    for v in veins:
        crack(st, v, "EC")
        st.put(v[0][0], v[0][1] - 1, "ED")          # extremos apagados
        st.put(v[-1][0], v[-1][1] + 1, "ED")
    st.put(fo.no + 9, 7, "EC")                      # rama corta
    st.put(fo.no + 10, 6, "ED")
    st.art(MOSS_EARTH, fo.wo + 1, 2)
    st.fill("DK", 0, 15, st.w, 1)
    st.put(14, 15, "RO")
    st.put(27, 15, "RO")
    f = fo.f["up"]
    f.fill("EA")
    f.fill("DK", 0, 0, f.w, 1)
    f = fo.f["down"]
    f.fill("EA")
    f.fill("DK", 3, 3, 6, 6)
    f.fill("RO", 1, 9, 1, 2)

    # --- mano: falanges que agarran la punta, una raíz que las cruza y una vena apagada que baja hacia la garra
    hd = C["l_hand"]
    st = hd.strip                      # 8 px por cara, 32 de vuelta; alto 8
    st.fill("EA")
    st.fill("DK", 0, 0, st.w, 1)
    finger = ("BL", "BM", "BM", "DK", "BM", "BM", "BS")          # filas 1 a 7: nudillo oscuro en medio
    for base in (hd.no, hd.so):
        for x in (1, 2, 5, 6):
            for i, c in enumerate(finger):
                st.put(base + x, 1 + i, c)
        for y in range(1, 8):
            st.put(base + 3, y, "DK")
        st.put(base + 2, 1, "BM")
        st.put(base + 6, 1, "BM")
    for base, x0 in ((hd.eo, 3), (hd.wo, 2)):
        for x in (x0, x0 + 1):
            for i, c in enumerate(finger):
                st.put(base + x, 1 + i, c)
        st.put(base + x0 + 2, 5, "DK")
    root_path(st, [(hd.eo, 2), (hd.eo + 7, 4)], [2])
    root_path(st, [(hd.wo + 7, 2), (hd.wo + 5, 6)], [1])
    for y in range(2, 8):                                        # vena apagada: sigue la del antebrazo
        st.put(hd.wo + 6 if y < 5 else hd.wo + 7, y, "ED")
    f = hd.f["up"]
    f.fill("EA")
    f = hd.f["down"]
    f.fill("EA")
    f.fill("DK", 1, 1, 6, 6)
    f.fill("ED", 2, 2, 4, 4)

    # --- punta: garra de ectoplasma con faceta, arista de luz y un collar irregular de raíz
    tp = C["l_tip"]
    st = tp.strip                      # este 6, norte 4, oeste 6, sur 4 = 20; alto 8
    st.fill("EC")
    st.fill("RO", 0, 0, st.w, 1)
    for u in (1, 2, 9, 10, 11):
        st.put(u, 1, "DK")
    for u, y1 in ((4, 5), (13, 4)):                              # dos hebras de raíz de largo distinto
        for y in range(1, y1):
            st.put(u, y, "RO")
    for k, cx in (("east", 2), ("west", 2), ("north", 1), ("south", 1)):
        f = tp.f[k]
        for y in range(2, 8):
            f.put(f.w - 1, y, "ED")                              # faceta en sombra
        for y in range(2, 6):
            if f.get(cx, y) == "EC":
                f.put(cx, y, "CO")                               # arista de luz de 1 px
        f.put(0, 7, "ED")                                        # la garra se afila
        f.put(f.w - 1, 7, "ED")
        if f.w >= 6:
            f.put(1, 7, "ED")
            f.put(f.w - 2, 7, "ED")
    tp.f["up"].fill("RO")
    f = tp.f["down"]
    f.fill("EC")
    f.fill("CO", 1, 1, 2, 4)


# ---------------------------------------------------------------- piernas

def paint_leg(side):
    th, sh, ft = C[f"{side}_leg"], C[f"{side}_shin"], C[f"{side}_foot"]
    left = side == "l"
    if left:
        long_bone(th, knob=3, wear=(("north", 4, 6, "chip"), ("east", 4, 9, "light"), ("south", 3, 10, "shade")))
    else:
        long_bone(th, knob=3, wear=(("west", 5, 9, "light"), ("north", 3, 9, "shade"), ("south", 4, 5, "light")))
        crack(th.strip, [(th.wo + 3, 4), (th.wo + 4, 7), (th.wo + 4, 9)])

    # tibia gruesa por dentro y peroné fino por fuera, en las dos piernas
    long_bone(sh, knob=3, gap={"north": 3, "south": 4} if left else {"north": 4, "south": 3})
    if left:
        prof = [0, 0, 1, 3, 3, 2, 2, 0, -1, -1, 1, 2, 4, 4, 3, 1, 0, 2, 3]
        clods = ((6, 10), (21, 9))
    else:
        prof = [2, 3, 1, 0, 0, -1, 1, 2, 2, 4, 3, 3, 1, 0, 1, 2, 0]
        clods = ((13, 10), (28, 9))
    dirt_bottom(sh.strip, 12, prof, clods=clods)
    sh.strip.fill("DK", 0, 17, sh.strip.w, 1)       # junta del tobillo
    if left:
        sh.strip.art(MOSS_C, sh.eo + 3, 8)          # pierna del lado del brazo arrastrado: algo de musgo

    for k in ("east", "west", "south", "north"):    # pie: 16x6 de perfil, 12x6 de frente
        f = ft.f[k]
        f.fill("BM")
        f.fill("BL", 0, 0, f.w, 1)
        f.fill("BS", f.w - 1, 0, 1, f.h)
    e, w_, n, s = ft.f["east"], ft.f["west"], ft.f["north"], ft.f["south"]
    e.fill("BS", 0, 0, 1, 6)                         # talón (u=0 atrás)
    e.put(11, 1, "BS")
    e.put(11, 2, "BS")
    e.put(6, 1, "BS")
    w_.fill("BS", w_.w - 1, 0, 1, 6)                 # la oeste es el espejo: u=0 delante
    w_.put(4, 1, "BS")
    w_.put(4, 2, "BS")
    w_.put(9, 1, "BS")
    n.fill("BL", 0, 0, 12, 1)
    for x in (0, 4, 9):
        n.put(x, 1, "BL")
    s.fill("BS", 0, 1, 1, 5)
    # barro en los pies: alto en talón y puntera, más bajo en el centro del perfil; distinto en cada pie
    if left:
        prof2 = ([0, 0, 1, 1, 2, 2, 2, 2, 1, 1, 1, 0, 0, -1, 0, 0]        # este: talón ... puntera
                 + [0, -1, 0, 1, 0, 0, -1, 0, 1, 0, 0, -1]                 # norte: dedos
                 + [0, -1, 0, 0, 1, 1, 2, 2, 2, 1, 2, 1, 1, 0, 0, 1]       # oeste: puntera ... talón
                 + [0, 0, 1, 1, 0, 0, 1, 1, 0, -1, 0, 0])                  # sur: talón
    else:
        prof2 = ([1, 0, 0, 1, 2, 2, 1, 2, 2, 1, 0, 0, 1, 0, -1, 0]
                 + [-1, 0, 0, 1, 1, 0, 0, -1, 0, 0, 1, 0]
                 + [0, 0, -1, 0, 1, 2, 2, 1, 2, 2, 1, 1, 0, 0, 1, 0]
                 + [1, 0, 0, -1, 0, 1, 1, 0, 0, 1, 1, 0])
    dirt_bottom(ft.strip, 2, prof2)
    ft.strip.fill("DK", 0, 5, ft.strip.w, 1)         # suela en sombra
    for x in (3, 8):                                 # tres dedos al frente
        n.fill("DK", x, 1, 1, 5)

    up = ft.f["up"]                                  # 12x16; el borde inferior es la punta del pie
    up.fill("BM")
    up.fill("BL", 0, 0, 12, 1)
    up.fill("BL", 0, 0, 1, 16)
    up.fill("BS", 11, 0, 1, 16)
    up.fill("BS", 2, 2, 8, 8)                        # sombra bajo la tibia
    up.fill("DK", 3, 3, 6, 6)
    for x in (3, 8):
        up.fill("BS", x, 10, 1, 2)                   # metatarsos
        up.fill("DK", x, 12, 1, 4)                   # separación de los dedos
    up.fill("BS", 0, 15, 12, 1)
    for x in (3, 8):
        up.put(x, 15, "DK")
    if left:                                         # barro sobre la puntera, con borde escalonado
        for x, y in ((0, 12), (0, 13), (1, 13), (0, 14), (1, 14), (2, 14), (0, 15), (1, 15), (2, 15),
                     (5, 14), (6, 14), (5, 15), (6, 15), (7, 15), (4, 15)):
            up.put(x, y, "EA")
        up.fill("EA", 0, 0, 3, 2)
        up.put(1, 2, "EA")
    else:
        for x, y in ((9, 13), (10, 13), (11, 13), (9, 14), (10, 14), (11, 14), (10, 12), (11, 12),
                     (9, 15), (10, 15), (11, 15), (4, 15), (5, 15), (6, 15), (5, 14)):
            up.put(x, y, "EA")
        up.fill("EA", 9, 0, 3, 1)
        up.put(10, 1, "EA")
    for x in (3, 8):
        up.put(x, 15, "DK")
    dn = ft.f["down"]
    dn.fill("DK")
    dn.fill("EA", 1, 1, dn.w - 2, dn.h - 2)


# ---------------------------------------------------------------- salida y comprobaciones

def main():
    paint_head()
    paint_chest()
    paint_torso()
    paint_right_arm()
    paint_drag_arm()
    paint_leg("l")
    paint_leg("r")

    used, problems = {}, []
    for c in C.values():
        for k, f in c.f.items():
            for y in range(f.h):
                for x in range(f.w):
                    p = img.getpixel((f.x0 + x, f.y0 + y))
                    if p[3] != 255:
                        problems.append(f"transparente en {c.name}.{k} ({x},{y})")
                        continue
                    if p not in INV:
                        problems.append(f"color fuera de paleta en {c.name}.{k}: {p}")
                        continue
                    key = INV[p]
                    used[key] = used.get(key, 0) + 1
                    if key in GLOW_COLORS and c.name not in GLOW_CUBES:
                        problems.append(f"color de brillo fuera de las zonas permitidas: {c.name}.{k} ({x},{y})")
                    if key in MOSS and c.name in ("h_skull", "h_eye_l", "h_eye_r"):
                        problems.append(f"musgo en la cabeza: {c.name}.{k} ({x},{y})")
    assert not problems, "\n".join(problems[:20])
    img.save(OUT / "osario_quebrado.png")

    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    prev = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    for c in C.values():                              # previsualización: todo lo pintado, en negro
        for f in c.f.values():
            for y in range(f.h):
                for x in range(f.w):
                    prev.putpixel((f.x0 + x, f.y0 + y), (0, 0, 0, 255))
    gset = {COL[k] for k in GLOW_COLORS}
    per_cube = {}
    for name in GLOW_CUBES:
        for f in C[name].f.values():
            for y in range(f.y0, f.y0 + f.h):
                for x in range(f.x0, f.x0 + f.w):
                    p = img.getpixel((x, y))
                    if p in gset:
                        glow.putpixel((x, y), p)
                        prev.putpixel((x, y), p)
                        per_cube[name] = per_cube.get(name, 0) + 1
    glow.save(OUT / "osario_quebrado_glowmask.png")
    prev.save(OUT / "osario_glow_preview.png")

    big = Image.new("RGBA", (W * 3, H * 3), (24, 24, 28, 255))
    big.alpha_composite(img.resize((W * 3, H * 3), Image.NEAREST))
    big.save(OUT / "osario_uv_x3.png")
    bigg = Image.new("RGBA", (W * 3, H * 3), (24, 24, 28, 255))
    bigg.alpha_composite(glow.resize((W * 3, H * 3), Image.NEAREST))
    bigg.save(OUT / "osario_glowmask_x3.png")

    total = sum(used.values())
    print("pixeles pintados:", total, "| brillan:", sum(per_cube.values()), per_cube)
    for k in PAL:
        print(f"  {k} {PAL[k]} {used.get(k, 0):6d} {100 * used.get(k, 0) / total:5.1f}%")


main()
