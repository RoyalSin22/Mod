"""Traducciones del lore narrativo (pt, fr, de). Un módulo por idioma; este los reúne para gen_lore_lang.py.

Clave: código de idioma ('pt', 'fr', 'de') -> {id de la entrada: (título o None, cuerpo)}. Si una entrada no está
en el idioma, el generador usa el texto inglés de lore_catalog.py como respaldo.
"""
from lore_i18n_de import TEXTS as DE
from lore_i18n_fr import TEXTS as FR
from lore_i18n_pt import TEXTS as PT

TRANSLATIONS = {'pt': PT, 'fr': FR, 'de': DE}
