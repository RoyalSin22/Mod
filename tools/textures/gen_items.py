#!/usr/bin/env python3
"""
Generador de texturas de objetos de Aptitudes (v22).

Cada objeto se dibuja a partir de una plantilla de 16×16 hecha a mano, donde cada letra es un MATERIAL:

    mayúscula  -> material con sombreado automático (luz arriba-izquierda, como vanilla)
    minúscula  -> mismo material en tono de brillo (reflejos, filos)
    '*'        -> destello blanco
    '+'        -> halo translúcido del color de acento (se dibuja fuera del contorno)
    '.'        -> transparente

Después se añade un contorno selectivo (solo en vecinos ortogonales, del tono más oscuro del material
adyacente) para que el objeto se lea bien sobre cualquier fondo.

Uso:  python3 tools/textures/gen_items.py <carpeta textures/item> [--preview salida.png]
Requiere Pillow. No modifica nada fuera de la carpeta indicada.
"""
import colorsys
import sys
from PIL import Image

# ----------------------------------------------------------------------------- Paletas


def hex_rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def ramp(base):
    """Rampa de 5 tonos [contorno, sombra, base, luz, brillo] con desplazamiento de matiz."""
    r, g, b = [c / 255 for c in hex_rgb(base) if True]
    h, s, v = colorsys.rgb_to_hsv(r, g, b)

    def mk(hs, sm, vm):
        hh = (h + hs) % 1.0
        ss = max(0.0, min(1.0, s * sm))
        vv = max(0.0, min(1.0, v * vm))
        rr, gg, bb = colorsys.hsv_to_rgb(hh, ss, vv)
        return (int(rr * 255), int(gg * 255), int(bb * 255), 255)

    return [
        mk(+0.03, 1.10, 0.30),   # contorno: más oscuro y hacia el azul
        mk(+0.015, 1.05, 0.62),  # sombra
        mk(0.0, 1.0, 1.0),       # base
        mk(-0.01, 0.80, 1.22),   # luz
        mk(-0.02, 0.45, 1.45),   # brillo
    ]


BASE_MATERIALS = {
    'S': '#9aa3ad',  # acero
    'D': '#4a4650',  # acero oscuro / netherita
    'W': '#7a5230',  # madera
    'L': '#6b4a35',  # cuero
    'C': '#d9cfb8',  # tela / papel / vendas
    'G': '#e0b43a',  # oro
    'B': '#c9c2b4',  # hueso
    'F': '#e9eef2',  # cristal / vidrio
    'K': '#2b2d38',  # negro tinta
    'R': '#b0423a',  # rojo (cuero rojo, cera)
    'V': '#3f8f3a',  # verde hoja
}


# ----------------------------------------------------------------------------- Plantillas
# Coordenadas: (x, y) con y hacia abajo. Las armas van en diagonal, mango abajo-izquierda, como vanilla.

T = {}

T['sword'] = [
    "................",
    ".............AA.",
    "............AaA.",
    "...........AaA..",
    "..........AaA...",
    ".........AaA....",
    "........AaA.....",
    ".......AaA......",
    "...G..AaA.......",
    "....GAaA........",
    ".....GA.........",
    "....L.G.........",
    "...L...G........",
    "..L.............",
    ".GG.............",
    "................",
]

T['great_sword'] = [
    "..............*.",
    "............AAAA",
    "...........AaaaA",
    "..........AaaaA.",
    ".........AaaaA..",
    "........AaaaA...",
    ".......AaaaA....",
    "..G...AaaaA.....",
    "..GG.AaaaA......",
    "...GGAaaA.......",
    "....GJaA........",
    ".....GGGG.......",
    "....L..GG.......",
    "...L....G.......",
    "..L.............",
    ".JJ.............",
]

T['katana'] = [
    "................",
    "..............A.",
    ".............Aa.",
    "............AaA.",
    "...........AaA..",
    "..........AaA...",
    ".........AaA....",
    "........AaA.....",
    ".......AaA......",
    "......AaA.......",
    ".....GGA........",
    ".....GG.........",
    "....R...........",
    "...R............",
    "..R.............",
    ".G..............",
]

T['dagger'] = [
    "................",
    "................",
    "................",
    "................",
    "...........AA...",
    "..........AaA...",
    ".........AaA....",
    "........AaA.....",
    "....G..AaA......",
    ".....GAaA.......",
    "......GA........",
    ".....L.G........",
    "....L...........",
    "...L............",
    "..GG............",
    "................",
]

T['axe'] = [
    "................",
    "....AAA.........",
    "...AaaAA........",
    "..AaAAAAA.......",
    "..AaAAAAWW......",
    "..AaAAAWW.......",
    "...AAAWW........",
    "....AWW.........",
    "....WW..........",
    "...WW...........",
    "..WW............",
    ".WW.............",
    "GG..............",
    "................",
    "................",
    "................",
]

T['spear'] = [
    "..............A.",
    ".............AaA",
    "............AaA.",
    "...........AaA..",
    "..........GAA...",
    ".........WG.....",
    "........W.......",
    ".......W........",
    "......W.........",
    ".....W..........",
    "....W...........",
    "...W............",
    "..W.............",
    ".W..............",
    "G...............",
    "................",
]

T['hammer'] = [
    "................",
    ".......AA.......",
    "......AaAA......",
    ".....AaAGAA.....",
    "....AaAGAAAA....",
    ".....AGAAAWAA...",
    "......AAAWWAA...",
    ".......AWWAA....",
    "........WAA.....",
    ".......WW.......",
    "......WW........",
    ".....LL.........",
    "....LL..........",
    "...LL...........",
    "..GG............",
    "................",
]

T['staff_orb'] = [
    "...........++...",
    "..........+JJ+..",
    ".........+JjJJ+.",
    ".........GJJJJ+.",
    "........G.GJJ+..",
    ".......W..G.....",
    "......W.........",
    ".....W..........",
    "....W...........",
    "...W............",
    "..W.............",
    ".W..............",
    "W...............",
    "................",
    "................",
    "................",
]

T['staff_leaf'] = [
    "..........VV....",
    ".........VvVV...",
    "........VVWVV...",
    ".......VVW.VJ...",
    "........W.......",
    ".......W........",
    "......W.........",
    ".....W..........",
    "....W...........",
    "...W............",
    "..W.............",
    ".W..............",
    "W...............",
    "................",
    "................",
    "................",
]

T['focus_master'] = [
    "............+...",
    "...........+J+..",
    "..........+JjJ+.",
    "..........GJJJ+.",
    ".........G.GJ+..",
    "........AG..G...",
    ".......A........",
    "......A.........",
    ".....W..........",
    "....W...........",
    "...W............",
    "..W.............",
    ".W..............",
    "G...............",
    "................",
    "................",
]

T['focus_ultimate'] = [
    ".........+...+..",
    "........G+JJ+G..",
    "........GJjjJG..",
    ".........GJJG*..",
    "........*.GG....",
    "........A.......",
    ".......AA.......",
    "......AJ........",
    ".....D..........",
    "....D...........",
    "...D............",
    "..D.............",
    ".D..............",
    "J...............",
    "................",
    "................",
]

T['gun'] = [
    "................",
    "................",
    "................",
    "................",
    "..AAAAAAAAAAJJ..",
    ".AaaaaaaaaaAJj..",
    ".ADDDDDDDDDDAA..",
    ".ADDDLLD........",
    "..DDDLL.........",
    "..DDDL..........",
    "..DDD...........",
    "..DDD...........",
    "................",
    "................",
    "................",
    "................",
]

T['revolver'] = [
    "................",
    "................",
    "................",
    "..........J.....",
    "...SSSSSSSSSSS..",
    "..SsssssssssSS..",
    "..SGGGSSSSSSS...",
    "..SGjGS.........",
    "..LGGGL.........",
    "..LL.L..........",
    "..LLL...........",
    "...LL...........",
    "................",
    "................",
    "................",
    "................",
]

T['wrench'] = [
    "................",
    "..........SS.SS.",
    ".........Ss...S.",
    ".........S....S.",
    ".........SS..SS.",
    "..........SSSS..",
    ".........SSS....",
    "........SS......",
    ".......SS.......",
    "......SS........",
    ".....AA.........",
    "....AAa.........",
    "...AAA..........",
    "..AAA...........",
    "..AA............",
    "................",
]

T['tome'] = [
    "................",
    "..AAAAAAAAAAA...",
    "..AaaaaaaaaaAC..",
    "..AAAAAAAAAAAC..",
    "..AAAGGGGAAAAC..",
    "..AAAGJJGAAAAC..",
    "..AAAGJJGAAAAC..",
    "..AAAGGGGAAAAC..",
    "..AAAAAAAAAAAC..",
    "..AAAAAAAAAAAC..",
    "..AAAAAAAAAAAC..",
    "..AAAAAAAAAAAC..",
    "..ACCCCCCCCCCC..",
    "..AAAAAAAAAAAA..",
    "................",
    "................",
]

T['pouch'] = [
    "................",
    "................",
    "......G..G......",
    ".......GG.......",
    "......LLLL......",
    ".....VLLLLV.....",
    "....LLLllLLL....",
    "...LLLLLLLLLL...",
    "...LLLLAALLLL...",
    "...LLLAJJALLL...",
    "...LLLLAALLLL...",
    "...LLLLLLLLLL...",
    "....LLLLLLLL....",
    ".....LLLLLL.....",
    "................",
    "................",
]

T['seal'] = [
    "................",
    "................",
    ".....RRRRRR.....",
    "....RRrRRRRR....",
    "...RRrRRRRRRR...",
    "...RRRGGGGRRR...",
    "..RRRGGJJGGRRR..",
    "..RRRGJJJJGRRR..",
    "..RRRGJJJJGRRR..",
    "..RRRGGJJGGRRR..",
    "...RRRGGGGRRR...",
    "...RRRRRRRRRR...",
    "....RRRRRRRR....",
    ".....RR..RR.....",
    ".....R....R.....",
    "................",
]

T['wraps'] = [
    "................",
    "................",
    "......CCCC......",
    ".....CccCCC.....",
    "....CcCCCCAC....",
    "....CCCAACCC....",
    "....CCACCCCC....",
    "....CCCCCAAC....",
    "....CAACCCCC....",
    ".....CCCCCC.....",
    "......CCCC.CC...",
    "...........CCC..",
    "............CC..",
    "................",
    "................",
    "................",
]

T['orb'] = [
    "................",
    "................",
    "......RRYY......",
    ".....RrRYyY.....",
    "....RRRRYYYY....",
    "....RRRRYYYY....",
    "....BBBBVVVV....",
    "....BBBBVVVV....",
    ".....BBBVVV.....",
    "......BBVV......",
    ".......GG.......",
    "......GGGG......",
    ".....GGggGG.....",
    "......GGGG......",
    "................",
    "................",
]

T['mask'] = [
    "................",
    "................",
    "....AAAAAAAA....",
    "...AaaAAAAAAA...",
    "..AaAAAAAAAAAA..",
    "..AAKKKAAKKKAA..",
    "..AAKJKAAKJKAA..",
    "..AAKKKAAKKKAA..",
    "..AAAAAAAAAAAA..",
    "...AAAAKKAAAA...",
    "...AAAKAAKAAA...",
    "....AAAAAAAA....",
    ".....AAAAAA.....",
    "......AAAA......",
    "................",
    "................",
]

T['sigil'] = [
    "................",
    ".....GGGGGG.....",
    "....G......G....",
    "...G..SSSS..G...",
    "..G..S.ss.S..G..",
    "..G.S..SS..S.G..",
    "..G.S.SJJS.S.G..",
    "..G.S.SJJS.S.G..",
    "..G.S..SS..S.G..",
    "..G..S.SS.S..G..",
    "...G..SSSS..G...",
    "....G......G....",
    ".....GGGGGG.....",
    "................",
    "................",
    "................",
]

T['lute'] = [
    "...........WW...",
    "..........WwW...",
    ".........WWW....",
    "........WW......",
    ".......WW.......",
    "......WW........",
    "...AAAWAA.......",
    "..AaaAWAAA......",
    ".AaAAKKAAAA.....",
    ".AAAKKKKAAA.....",
    ".AAAKKKKAAA.....",
    ".AAAAKKAAAA.....",
    "..AAAGGAAA......",
    "...AAAAAA.......",
    "....AAAA........",
    "................",
]

T['talisman'] = [
    "......GG........",
    ".....G..G.......",
    ".....G..G.......",
    "......GG........",
    ".....KKKK.......",
    "....KKaKKK......",
    "...KKAAAAKK.....",
    "...KAAJJAAK.....",
    "...KAJjJJAK.....",
    "...KAAJJAAK.....",
    "....KAAAAK......",
    ".....KAAK.......",
    "......KK........",
    "......A.........",
    ".....A.A........",
    "................",
]

T['whistle'] = [
    "................",
    "................",
    "................",
    "...........G....",
    "..........G.G...",
    "...........G....",
    ".....BBBBBBL....",
    "...BBbbbbBBL....",
    "..BBBBKBBBBL....",
    "..BBBBBBBBB.....",
    "..BBBBBBBB......",
    "...BBBBBB.......",
    "................",
    "................",
    "................",
    "................",
]

T['knife'] = [
    "................",
    "................",
    "...........AA...",
    "..........AaA...",
    ".........AaAA...",
    "........AaAA....",
    ".......AaAA.....",
    "......AaA.......",
    ".....GGA........",
    "....L.G.........",
    "...L............",
    "..L.............",
    ".BB.............",
    "................",
    "................",
    "................",
]

# --- Objetos clave ---

T['flask'] = [
    "................",
    "......WWW.......",
    ".......F........",
    "......FFF.......",
    "......FfF.......",
    ".....FFFFF......",
    "....FfAAAAF.....",
    "...FfAAaAAAF....",
    "...FAAaAAAAF....",
    "...FAAAAJAAF....",
    "...FAAAJjJAF....",
    "...FAAAAJAAF....",
    "....FAAAAAF.....",
    ".....FFFFF......",
    "................",
    "................",
]

T['shard'] = [
    "................",
    "...........+....",
    "..........+AA...",
    ".........+AaA...",
    "........+AaAA...",
    ".......+AaAA....",
    "......+AaAA.....",
    ".....+AaAA......",
    "....+AaAA.......",
    "....AAAA........",
    "...AAAA.........",
    "...AAA..........",
    "...AA...........",
    "................",
    "................",
    "................",
]

T['emblem'] = [
    "................",
    ".......GG.......",
    "......GggG......",
    ".GGGGGGGGGGGGG..",
    "..GGGGJJJGGGG...",
    "...GGGJjJGGG....",
    "....GGJJJGG.....",
    "....GGGGGGG.....",
    "...GGGG.GGGG....",
    "...GGG...GGG....",
    "..GG.......GG...",
    "................",
    "....AA...AA.....",
    "....AA...AA.....",
    "....A.....A.....",
    "................",
]

T['core'] = [
    "................",
    "....+......+....",
    ".....D.DD.D.....",
    "......DDDD......",
    "....DDJJJJDD....",
    "...DDJJjJJJDD...",
    "..DDJJjJJJJJDD..",
    "...DJJJJJ*JJD...",
    "...DJJJJJJJJD...",
    "..DDJJJJJJJJDD..",
    "...DDJJJJJJDD...",
    "....DDJJJJDD....",
    "......DDDD......",
    ".....D.DD.D.....",
    "....+......+....",
    "................",
]

T['offering'] = [
    "................",
    ".......J........",
    "......JjJ.......",
    ".......J........",
    ".......K........",
    "......CCC.......",
    "......CcC.......",
    "......CCC.......",
    "......CCC.......",
    "....GGGGGGG.....",
    "...GgAAAAAGG....",
    "...GAAJJAAAG....",
    "...GGAAAAAGG....",
    "....GGGGGGG.....",
    "................",
    "................",
]

T['voucher'] = [
    "................",
    "................",
    "...WW......WW...",
    "..WWCCCCCCCCWW..",
    "...WCcCCCCCCW...",
    "...WCKKKKKKCW...",
    "...WCCCCCCCCW...",
    "...WCKKKKKCCW...",
    "...WCCCCCCCCW...",
    "...WCKKKCCRRW...",
    "...WCCCCCCRRW...",
    "..WWCCCCCCCCWW..",
    "...WW......WW...",
    "................",
    "................",
    "................",
]

T['essence'] = [
    "................",
    "......+++.......",
    ".....+AAA+......",
    "....+AaAAA+.....",
    "...+AaAJJAA+....",
    "...+AAJjJJA+....",
    "...+AAJJJAA+....",
    "....+AAJAA+.....",
    ".....+AAA+......",
    "......+A+.......",
    ".......A........",
    "......A.A.......",
    "................",
    "................",
    "................",
    "................",
]

T['heart'] = [
    "................",
    "................",
    "...RRR...RRR....",
    "..RrrRR.RRRRR...",
    "..RrRRRRRRRRR...",
    "..RRRRRRRRRRR...",
    "..RRRRRGRRRRR...",
    "...RRRGJGRRR....",
    "....RRRGRRR.....",
    ".....RRRRR......",
    "......RRR.......",
    ".......R........",
    "................",
    "................",
    "................",
    "................",
]

T['pickaxe'] = [
    "................",
    "....AAAAAA......",
    "...AaaaaaAA.....",
    "..AA....WAAA....",
    "..A....W..AAA...",
    "......W....AA...",
    ".....W......A...",
    "....W...........",
    "...W............",
    "..W.............",
    ".W..............",
    "J...............",
    "................",
    "................",
    "................",
    "................",
]

T['shovel'] = [
    "................",
    "..........AAA...",
    ".........AaaAA..",
    "........AaAAAA..",
    "........AAAAAA..",
    ".........AAAA...",
    "........W.AA....",
    ".......W........",
    "......W.........",
    ".....W..........",
    "....W...........",
    "...W............",
    "..W.............",
    ".LL.............",
    ".LJ.............",
    "................",
]

T['hoe'] = [
    "................",
    "......AAAAAA....",
    ".....AaaaaAA....",
    "..........WA....",
    ".........W.A....",
    "........W.......",
    ".......W........",
    "......W.........",
    ".....W..........",
    "....W...........",
    "...W............",
    "..W.............",
    ".W..............",
    "J...............",
    "................",
    "................",
]

T['ring'] = [
    "................",
    "................",
    "......+JJ+......",
    ".....+JjJJ+.....",
    "......JJJJ......",
    ".....GGJJGG.....",
    "....GgGGGGGG....",
    "...GG......GG...",
    "...G........G...",
    "...G........G...",
    "...GG......GG...",
    "....GGGGGGGG....",
    ".....GGGGGG.....",
    "................",
    "................",
    "................",
]

T['amulet'] = [
    "...G........G...",
    "....G......G....",
    ".....G....G.....",
    "......G..G......",
    ".......GG.......",
    "......GGGG......",
    ".....GJJJJG.....",
    "....GJjJJJJG....",
    "....GJJJJJJG....",
    "....GJJJJJJG....",
    ".....GJJJJG.....",
    "......GJJG......",
    ".......GG.......",
    "................",
    "................",
    "................",
]

T['trophy'] = [
    "................",
    "..LL........LL..",
    "...LL......LL...",
    "....LL....LL....",
    ".....LLLLLL.....",
    "....B.B..B.B....",
    "....B.Bb.Bb.B...",
    "...BB.BB.BB.B...",
    "...B..BB.BB.B...",
    "...B...B..B.BB..",
    "..BB.......B....",
    "................",
    "................",
    "................",
    "................",
    "................",
]

T['book'] = [
    "................",
    "...AAAAAAAAAA...",
    "...AaaaaaaaaAC..",
    "...AAGGGGGGAAC..",
    "...AAGAAAAGAAC..",
    "...AAGAJJAGAAC..",
    "...AAGAJJAGAAC..",
    "...AAGAAAAGAAC..",
    "...AAGGGGGGAAC..",
    "...AAAAAAAAAAC..",
    "...AAAAKKAAAAC..",
    "...AAAAKKAAAAC..",
    "...ACCCCCCCCCC..",
    "...AAAAAAAAAAA..",
    "................",
    "................",
]


# ----------------------------------------------------------------------------- Objetos
# nombre -> (plantilla, {material: color}); 'A' = acento del objeto, 'J' = gema, Y/V/B/R = colores extra.

ROLE_COLORS = {
    'berserker': '#c0392b', 'mage': '#8e44ad', 'assassin': '#4b6584', 'herbalist': '#27ae60',
    'engineer': '#d35400', 'lancer': '#2980b9', 'swordsman': '#bdc3c7', 'paladin': '#f1c40f',
    'necromancer': '#6c3483', 'druid': '#6e8b3d', 'merchant': '#16a085', 'monk': '#e67e22',
    'elementalist': '#3498db', 'shapeshifter': '#9b59b6', 'gunslinger': '#00bcd4',
    'weapon_summoner': '#90a4ae', 'hunter': '#7cb342', 'bard': '#ec407a', 'warlock': '#ad1457',
    'tamer': '#a1887f',
}
MELEE = {'berserker', 'assassin', 'herbalist', 'lancer', 'swordsman', 'paladin', 'merchant', 'monk',
         'shapeshifter', 'weapon_summoner', 'hunter', 'tamer'}

ITEMS = {
    # Armas de rol
    'berserker_axe': ('axe', {'A': '#a8b0b8', 'W': '#5a3a22', 'G': '#c0392b'}),
    'dagger': ('dagger', {'A': '#c7ced6', 'G': '#6a5acd', 'L': '#2c3e50'}),
    'katana': ('katana', {'A': '#e3e8ec', 'G': '#d4a017', 'R': '#8e1b1b'}),
    'spear': ('spear', {'A': '#7fb3d5', 'W': '#7a5230', 'G': '#d4a017'}),
    'arcane_staff': ('staff_orb', {'J': '#a55eea', 'W': '#5d4037', 'G': '#d4a017', 'A': '#c39bd3'}),
    'oak_staff': ('staff_leaf', {'W': '#6d4c2a', 'V': '#5aa83a', 'J': '#f1c40f'}),
    'holy_hammer': ('hammer', {'A': '#d8dde3', 'W': '#7a5230', 'L': '#3a5ba0', 'J': '#f1c40f', 'G': '#d4a017'}),
    'plasma_gun': ('gun', {'A': '#00e5ff', 'D': '#3d4450', 'L': '#2b2d38', 'J': '#7cf9ff'}),
    'arcane_revolver': ('revolver', {'S': '#8fa3b5', 'G': '#d4a017', 'J': '#00bcd4', 'L': '#5d3a1a'}),
    'wrench': ('wrench', {'S': '#aab4be', 'A': '#d35400'}),
    'necro_tome': ('tome', {'A': '#3b1f4a', 'C': '#d8ccb0', 'G': '#9e9e9e', 'J': '#7dff7a'}),
    'herb_pouch': ('pouch', {'L': '#7a5a3a', 'V': '#4caf50', 'G': '#d4a017', 'A': '#27ae60', 'J': '#f1c40f'}),
    'guild_seal': ('seal', {'R': '#a32a2a', 'G': '#d4a017', 'J': '#16a085'}),
    'hand_wraps': ('wraps', {'C': '#efe6d2', 'A': '#e67e22'}),
    'elemental_orb': ('orb', {'R': '#e74c3c', 'Y': '#3498db', 'B': '#f1c40f', 'V': '#2ecc71', 'G': '#d4a017'}),
    'mimic_mask': ('mask', {'A': '#9b59b6', 'K': '#1e1428', 'J': '#f1c40f'}),
    'weapon_sigil': ('sigil', {'G': '#d4a017', 'S': '#90a4ae', 'J': '#4fc3f7'}),
    'hunting_knife': ('knife', {'A': '#b9c3cc', 'G': '#7cb342', 'L': '#5d3a1a', 'B': '#d7ccc8'}),
    'lute': ('lute', {'W': '#8d5a2b', 'A': '#ec407a', 'K': '#3a1a24', 'G': '#d4a017'}),
    'pact_talisman': ('talisman', {'G': '#9e9e9e', 'K': '#2b0f1a', 'A': '#ad1457', 'J': '#ff4d6d'}),
    'beast_whistle': ('whistle', {'B': '#e6d8bf', 'K': '#3a2a1a', 'L': '#8d6e63', 'G': '#d4a017'}),
    # Objetos clave
    'guide_book': ('book', {'A': '#7b2c2c', 'C': '#efe6d2', 'G': '#e0b43a', 'J': '#4fc3f7', 'K': '#3a1a14'}),
    'estus_flask': ('flask', {'F': '#d8e4ea', 'A': '#ff9f1a', 'J': '#fff3a0', 'W': '#6d4c2a'}),
    'ether_flask': ('flask', {'F': '#d8e4ea', 'A': '#3f6fe0', 'J': '#b3e5fc', 'W': '#6d4c2a'}),
    'soul_shard': ('shard', {'A': '#8e7cf0'}),
    'mastery_emblem': ('emblem', {'G': '#e0b43a', 'J': '#4fc3f7', 'A': '#2980b9'}),
    'ultimate_core': ('core', {'D': '#3a2f4a', 'J': '#ff3d7f'}),
    'rebirth_offering': ('offering', {'J': '#ff9f1a', 'K': '#2b2d38', 'C': '#efe6d2', 'G': '#e0b43a', 'A': '#8e44ad'}),
    'armory_voucher': ('voucher', {'W': '#8d5a2b', 'C': '#efe6d2', 'K': '#5d4a3a', 'R': '#b0423a'}),
    'elite_essence': ('essence', {'A': '#c0392b', 'J': '#ffb199'}),
    'champion_heart': ('heart', {'R': '#d32f2f', 'G': '#e0b43a', 'J': '#ffffff'}),
    'soul_pickaxe': ('pickaxe', {'A': '#8e7cf0', 'W': '#3a2f4a', 'J': '#d1c4ff'}),
    'soul_axe': ('axe', {'A': '#8e7cf0', 'W': '#3a2f4a', 'G': '#d1c4ff'}),
    'soul_shovel': ('shovel', {'A': '#8e7cf0', 'W': '#3a2f4a', 'L': '#2b2238', 'J': '#d1c4ff'}),
    'soul_hoe': ('hoe', {'A': '#8e7cf0', 'W': '#3a2f4a', 'J': '#d1c4ff'}),
    # Accesorios
    'ring_of_strength': ('ring', {'G': '#e0b43a', 'J': '#e53935'}),
    'ring_of_vitality': ('ring', {'G': '#e0b43a', 'J': '#ff6f91'}),
    'ring_of_swiftness': ('ring', {'G': '#c9d1d9', 'J': '#81d4fa'}),
    'ring_of_mana': ('ring', {'G': '#c9d1d9', 'J': '#3f6fe0'}),
    'ring_of_the_vampire': ('ring', {'G': '#4a4650', 'J': '#b71c1c'}),
    'ring_of_fortune': ('ring', {'G': '#e0b43a', 'J': '#2ecc71'}),
    'amulet_of_the_guardian': ('amulet', {'G': '#c9d1d9', 'J': '#5c6bc0'}),
    'amulet_of_the_phoenix': ('amulet', {'G': '#e0b43a', 'J': '#ff7043'}),
    'arcane_amulet': ('amulet', {'G': '#e0b43a', 'J': '#ab47bc'}),
    'amulet_of_the_grove': ('amulet', {'G': '#a1887f', 'J': '#66bb6a'}),
    'hunters_trophy': ('trophy', {'L': '#6d4c2a', 'B': '#efe6d2'}),
}

# Armas de evolución: hoja (cuerpo a cuerpo) o foco; Maestro con oro y gema; Definitivo, netherita y destellos.
for role, color in ROLE_COLORS.items():
    if role in MELEE:
        ITEMS[f'{role}_weapon_master'] = ('sword', {'A': color, 'G': '#e0b43a', 'L': '#3a2a1a'})
        ITEMS[f'{role}_weapon_ultimate'] = ('great_sword', {'A': color, 'G': '#4a4650', 'J': '#ffffff', 'L': '#1e1a22'})
    else:
        ITEMS[f'{role}_weapon_master'] = ('focus_master', {'J': color, 'G': '#e0b43a', 'A': '#c9d1d9', 'W': '#5d4037'})
        ITEMS[f'{role}_weapon_ultimate'] = ('focus_ultimate', {'J': color, 'G': '#e0b43a', 'A': color, 'D': '#2b2530'})


# ----------------------------------------------------------------------------- Dibujo

def render(template, colors):
    grid = [row.ljust(16, '.')[:16] for row in template]
    assert len(grid) == 16, f"la plantilla debe tener 16 filas ({len(grid)})"
    ramps = {}

    def ramp_for(mat):
        if mat not in ramps:
            base = colors.get(mat) or BASE_MATERIALS.get(mat) or '#ff00ff'
            ramps[mat] = ramp(base)
        return ramps[mat]

    def mat_at(x, y):
        if 0 <= x < 16 and 0 <= y < 16:
            c = grid[y][x]
            if c.isalpha():
                return c.upper()
        return None

    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(16):
        for x in range(16):
            c = grid[y][x]
            if not c.isalpha():
                continue
            m = c.upper()
            r = ramp_for(m)
            if c.islower():
                px[x, y] = r[4]
                continue
            up = mat_at(x, y - 1) == m
            lf = mat_at(x - 1, y) == m
            dn = mat_at(x, y + 1) == m
            rt = mat_at(x + 1, y) == m
            tone = 2
            if not up or not lf:
                tone = 3
            if (not dn or not rt) and tone == 2:
                tone = 1
            if (not dn and not rt) and (up and lf):
                tone = 1
            px[x, y] = r[tone]
    # Contorno selectivo (vecinos ortogonales)
    out = img.copy()
    op = out.load()
    for y in range(16):
        for x in range(16):
            if px[x, y][3] or grid[y][x] in '*+':
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                m = mat_at(x + dx, y + dy)
                if m:
                    op[x, y] = ramp_for(m)[0]
                    break
    # Halo y destellos
    accent = colors.get('J') or colors.get('A') or '#ffffff'
    ar = hex_rgb(accent)
    for y in range(16):
        for x in range(16):
            c = grid[y][x]
            if c == '+':
                op[x, y] = (ar[0], ar[1], ar[2], 110)
            elif c == '*':
                op[x, y] = (255, 255, 255, 230)
    return out


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    folder = sys.argv[1]
    preview = sys.argv[sys.argv.index('--preview') + 1] if '--preview' in sys.argv else None
    rendered = {}
    for name, (tpl, colors) in ITEMS.items():
        img = render(T[tpl], colors)
        img.save(f'{folder}/{name}.png')
        rendered[name] = img
    if preview:
        names = list(rendered)
        cols = 12
        rows = (len(names) + cols - 1) // cols
        scale = 6
        sheet = Image.new('RGBA', (cols * 16 * scale, rows * 16 * scale), (58, 60, 72, 255))
        for i, n in enumerate(names):
            big = rendered[n].resize((16 * scale, 16 * scale), Image.NEAREST)
            sheet.paste(big, ((i % cols) * 16 * scale, (i // cols) * 16 * scale), big)
        sheet.save(preview)
    print(f'{len(rendered)} texturas generadas en {folder}')


if __name__ == '__main__':
    main()
