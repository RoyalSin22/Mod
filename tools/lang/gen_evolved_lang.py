#!/usr/bin/env python3
"""
Genera los 200 nodos evolucionados en inglés, español, portugués, francés y alemán.
Español se copia a sus seis variantes. --check comprueba sin escribir.

Los valores numéricos salen de los mismos datos que el código (EvolvedTree / EvolvedAbilities):
 - pasivas: valor de senda (PathEffect.perRank) × 1,5 (Maestro, 2 rangos) o × 3 (Definitivo, 1 rango)
 - activas: daño base 6 (Maestro) / 10 (Definitivo) + 1,5 por rango efectivo
Si cambias esos números en Java, cambia también las constantes de abajo y vuelve a ejecutarlo.

Uso: python3 tools/lang/gen_evolved_lang.py
"""
from check_lang import LOCALES

# PathEffect: (valor por rango, porcentaje)
EFFECTS = {
    'LIFESTEAL': (0.02, True), 'CRIT': (0.04, True), 'EXECUTE': (0.06, True), 'THORNS': (0.06, True),
    'DAMAGE_REDUCTION': (0.03, True), 'DODGE': (0.03, True), 'MANA_REGEN': (0.4, False),
    'RESOURCE_GAIN': (0.12, True), 'REGEN': (0.5, False), 'ON_HIT_POISON': (0.08, True),
    'ON_HIT_SLOW': (0.08, True), 'ON_HIT_FIRE': (0.08, True), 'ON_HIT_WITHER': (0.08, True),
    'MINION_POWER': (0.10, True), 'KNOCKBACK': (0.06, True), 'SPEED': (0.02, True), 'MAX_HEALTH': (1.5, False),
    'ATTACK_SPEED': (0.03, True), 'LUCK': (0.4, False),
}

# (efectos m1-m3, (plantilla, extra) m4-m5, efectos u1-u3, (plantilla, extra) u4-u5) — igual que EvolvedTree
TREE = {
    'berserker': (['LIFESTEAL', 'DAMAGE_REDUCTION', 'ATTACK_SPEED'], [('CONE', 'SLOW'), ('DASH', 'NONE')],
                  ['EXECUTE', 'MAX_HEALTH', 'CRIT'], [('NOVA', 'LAUNCH'), ('AURA', 'FORCE')]),
    'mage': (['MANA_REGEN', 'CRIT', 'DAMAGE_REDUCTION'], [('BEAM', 'NONE'), ('NOVA', 'SLOW')],
             ['RESOURCE_GAIN', 'SPEED', 'EXECUTE'], [('ZONE', 'FIRE'), ('VORTEX', 'NONE')]),
    'assassin': (['CRIT', 'DODGE', 'ON_HIT_POISON'], [('NOVA', 'POISON'), ('DASH', 'NONE')],
                 ['EXECUTE', 'SPEED', 'LIFESTEAL'], [('CONE', 'NONE'), ('AURA', 'SHADOW')]),
    'herbalist': (['REGEN', 'MAX_HEALTH', 'ON_HIT_POISON'], [('HEAL', 'NONE'), ('ZONE', 'POISON')],
                  ['REGEN', 'DAMAGE_REDUCTION', 'LUCK'], [('HEAL', 'NONE'), ('VORTEX', 'SLOW')]),
    'engineer': (['DAMAGE_REDUCTION', 'ATTACK_SPEED', 'ON_HIT_FIRE'], [('NOVA', 'SLOW'), ('ZONE', 'NONE')],
                 ['MAX_HEALTH', 'CRIT', 'RESOURCE_GAIN'], [('BEAM', 'FIRE'), ('SHIELD', 'NONE')]),
    'lancer': (['KNOCKBACK', 'CRIT', 'SPEED'], [('DASH', 'NONE'), ('NOVA', 'KNOCKBACK')],
               ['EXECUTE', 'DAMAGE_REDUCTION', 'ATTACK_SPEED'], [('ZONE', 'NONE'), ('BEAM', 'NONE')]),
    'swordsman': (['CRIT', 'DODGE', 'ATTACK_SPEED'], [('CONE', 'NONE'), ('DASH', 'NONE')],
                  ['EXECUTE', 'LIFESTEAL', 'DAMAGE_REDUCTION'], [('NOVA', 'NONE'), ('BEAM', 'NONE')]),
    'paladin': (['REGEN', 'DAMAGE_REDUCTION', 'ON_HIT_FIRE'], [('NOVA', 'KNOCKBACK'), ('HEAL', 'NONE')],
                ['MAX_HEALTH', 'EXECUTE', 'THORNS'], [('ZONE', 'FIRE'), ('SHIELD', 'NONE')]),
    'necromancer': (['MINION_POWER', 'LIFESTEAL', 'ON_HIT_WITHER'], [('SUMMON', 'ZOMBIES'), ('ZONE', 'WITHER')],
                    ['MINION_POWER', 'MAX_HEALTH', 'ON_HIT_SLOW'], [('SUMMON', 'SKELETONS'), ('VORTEX', 'WITHER')]),
    'druid': (['MAX_HEALTH', 'SPEED', 'CRIT'], [('CONE', 'NONE'), ('HEAL', 'NONE')],
              ['REGEN', 'DAMAGE_REDUCTION', 'THORNS'], [('SUMMON', 'WOLVES'), ('ZONE', 'NONE')]),
    'merchant': (['LUCK', 'RESOURCE_GAIN', 'MANA_REGEN'], [('ZONE', 'NONE'), ('SUMMON', 'WOLVES')],
                 ['LUCK', 'CRIT', 'DAMAGE_REDUCTION'], [('NOVA', 'KNOCKBACK'), ('SHIELD', 'NONE')]),
    'monk': (['ATTACK_SPEED', 'REGEN', 'DODGE'], [('CONE', 'KNOCKBACK'), ('DASH', 'NONE')],
             ['MANA_REGEN', 'DAMAGE_REDUCTION', 'CRIT'], [('NOVA', 'NONE'), ('HEAL', 'NONE')]),
    'elementalist': (['CRIT', 'MANA_REGEN', 'ON_HIT_FIRE'], [('BEAM', 'NONE'), ('ZONE', 'SLOW')],
                     ['RESOURCE_GAIN', 'ON_HIT_SLOW', 'DAMAGE_REDUCTION'], [('NOVA', 'FIRE'), ('VORTEX', 'NONE')]),
    'shapeshifter': (['DODGE', 'CRIT', 'MAX_HEALTH'], [('DASH', 'NONE'), ('NOVA', 'KNOCKBACK')],
                     ['LIFESTEAL', 'SPEED', 'THORNS'], [('SUMMON', 'WOLVES'), ('CONE', 'NONE')]),
    'gunslinger': (['ATTACK_SPEED', 'CRIT', 'DODGE'], [('CONE', 'NONE'), ('BEAM', 'NONE')],
                   ['EXECUTE', 'SPEED', 'MANA_REGEN'], [('ZONE', 'NONE'), ('BEAM', 'FIRE')]),
    'weapon_summoner': (['RESOURCE_GAIN', 'CRIT', 'DAMAGE_REDUCTION'], [('CONE', 'NONE'), ('SHIELD', 'NONE')],
                        ['ATTACK_SPEED', 'EXECUTE', 'THORNS'], [('ZONE', 'NONE'), ('BEAM', 'NONE')]),
    'hunter': (['SPEED', 'CRIT', 'ON_HIT_SLOW'], [('CONE', 'NONE'), ('DASH', 'NONE')],
               ['EXECUTE', 'DODGE', 'LIFESTEAL'], [('ZONE', 'NONE'), ('SUMMON', 'WOLVES')]),
    'bard': (['REGEN', 'ATTACK_SPEED', 'RESOURCE_GAIN'], [('NOVA', 'SLOW'), ('HEAL', 'NONE')],
             ['MANA_REGEN', 'MAX_HEALTH', 'ON_HIT_SLOW'], [('AURA', 'SONG'), ('NOVA', 'KNOCKBACK')]),
    'warlock': (['ON_HIT_WITHER', 'LIFESTEAL', 'MANA_REGEN'], [('BEAM', 'WITHER'), ('ZONE', 'WITHER')],
                ['EXECUTE', 'MAX_HEALTH', 'ON_HIT_POISON'], [('VORTEX', 'WITHER'), ('SUMMON', 'VEXES')]),
    'tamer': (['MAX_HEALTH', 'MINION_POWER', 'SPEED'], [('SUMMON', 'WOLVES'), ('DASH', 'NONE')],
              ['MINION_POWER', 'DAMAGE_REDUCTION', 'CRIT'], [('AURA', 'FORCE'), ('CONE', 'KNOCKBACK')]),
}

# Nombres: m1..m5, u1..u5 (es, en)
NAMES = {
    'berserker': (['Sed de sangre', 'Piel de hierro', 'Furia ciega', 'Tajo sísmico', 'Embestida brutal',
                   'Hambre de guerra', 'Inquebrantable', 'Tormenta de acero', 'Terremoto', 'Grito del titán'],
                  ['Bloodthirst', 'Iron Hide', 'Blind Fury', 'Seismic Slash', 'Brutal Charge',
                   'War Hunger', 'Unbreakable Will', 'Steel Storm', 'Earthquake', "Titan's Cry"]),
    'mage': (['Flujo arcano', 'Mente afilada', 'Escudo de maná', 'Rayo prismático', 'Nova de escarcha',
              'Archimago', 'Dominio del tiempo', 'Sobrecarga', 'Lluvia de meteoros', 'Singularidad'],
             ['Arcane Flow', 'Sharp Mind', 'Mana Shield', 'Prismatic Ray', 'Frost Nova',
              'Archmage', 'Time Mastery', 'Overload', 'Meteor Shower', 'Singularity']),
    'assassin': (['Golpe certero', 'Paso sombrío', 'Hoja envenenada', 'Danza de cuchillas', 'Salto de sombra',
                  'Marca de muerte', 'Velo nocturno', 'Sangría', 'Tormenta de dagas', 'Eclipse'],
                 ['True Strike', 'Shadow Step', 'Poisoned Blade', 'Blade Dance', 'Shadow Leap',
                  'Death Mark', 'Night Veil', 'Bloodletting', 'Dagger Storm', 'Eclipse']),
    'herbalist': (['Savia viva', 'Raíces profundas', 'Espinas tóxicas', 'Flor curativa', 'Nube de esporas',
                   'Árbol de la vida', 'Corteza', 'Polen dorado', 'Jardín sagrado', 'Enredadera'],
                  ['Living Sap', 'Deep Roots', 'Toxic Thorns', 'Healing Bloom', 'Spore Cloud',
                   'Tree of Life', 'Bark Skin', 'Golden Pollen', 'Sacred Garden', 'Strangling Vines']),
    'engineer': (['Blindaje', 'Engranajes', 'Sobrecalentado', 'Pulso eléctrico', 'Mortero',
                  'Núcleo reforzado', 'Precisión', 'Batería', 'Rayo láser', 'Escudo de energía'],
                 ['Plating', 'Gearwork', 'Overheated', 'Electric Pulse', 'Mortar',
                  'Reinforced Core', 'Precision', 'Battery', 'Laser Beam', 'Energy Shield']),
    'lancer': (['Postura firme', 'Perforar', 'Carga', 'Estocada', 'Lanza giratoria',
                'Furia del dragón', 'Falange', 'Viento cortante', 'Lluvia de lanzas', 'Rayo celeste'],
               ['Firm Stance', 'Pierce', 'Charge', 'Lunge', 'Spinning Spear',
                "Dragon's Fury", 'Phalanx', 'Cutting Wind', 'Spear Rain', 'Sky Ray']),
    'swordsman': (['Filo templado', 'Parada', 'Ritmo', 'Corte cruzado', 'Iai',
                   'Kensei', 'Sangre fría', 'Mente serena', 'Mil cortes', 'Onda de espada'],
                  ['Tempered Edge', 'Parry', 'Rhythm', 'Cross Cut', 'Iai',
                   'Kensei', 'Cold Blood', 'Serene Mind', 'Thousand Cuts', 'Sword Wave']),
    'paladin': (['Fe', 'Égida', 'Castigo', 'Martillo justiciero', 'Bendición',
                 'Santo', 'Juicio', 'Luz radiante', 'Consagración', 'Escudo divino'],
                ['Faith', 'Aegis', 'Smite', 'Hammer of Justice', 'Blessing',
                 'Saint', 'Judgement', 'Radiant Light', 'Consecration', 'Divine Shield']),
    'necromancer': (['Legión', 'Drenaje', 'Plaga', 'Alzar caídos', 'Nube pestilente',
                     'Señor de la muerte', 'Sangre negra', 'Toque gélido', 'Ejército eterno', 'Abrazo de la tumba'],
                    ['Legion', 'Drain', 'Plague', 'Raise the Fallen', 'Pestilent Cloud',
                     'Lord of Death', 'Black Blood', 'Chilling Touch', 'Eternal Army', "Grave's Embrace"]),
    'druid': (['Pelaje grueso', 'Instinto', 'Garras', 'Zarpazo', 'Luz lunar',
               'Espíritu salvaje', 'Corteza antigua', 'Zarzas', 'Estampida', 'Furia natural'],
              ['Thick Fur', 'Instinct', 'Claws', 'Maul', 'Moonlight',
               'Wild Spirit', 'Ancient Bark', 'Brambles', 'Stampede', "Nature's Wrath"]),
    'merchant': (['Fortuna', 'Regateo', 'Bolsillo lleno', 'Lluvia de oro', 'Guardaespaldas',
                  'Monopolio', 'Mercenario', 'Seguro', 'Golpe de fortuna', 'Escudo de monedas'],
                 ['Fortune', 'Haggle', 'Deep Purse', 'Gold Rain', 'Bodyguard',
                  'Monopoly', 'Mercenary', 'Insurance', 'Lucky Strike', 'Coin Shield']),
    'monk': (['Puño veloz', 'Loto', 'Chi', 'Palma de choque', 'Paso del viento',
              'Iluminación', 'Cuerpo de diamante', 'Golpe vital', 'Mil palmas', 'Meditación'],
             ['Swift Fist', 'Lotus', 'Chi', 'Shock Palm', 'Wind Step',
              'Enlightenment', 'Diamond Body', 'Vital Strike', 'Thousand Palms', 'Meditation']),
    'elementalist': (['Catalizador', 'Conductor', 'Ignición', 'Prisma elemental', 'Tormenta',
                      'Avatar', 'Resonancia', 'Núcleo elemental', 'Cataclismo', 'Ojo del huracán'],
                     ['Catalyst', 'Conduit', 'Ignition', 'Elemental Prism', 'Tempest',
                      'Avatar', 'Resonance', 'Elemental Core', 'Cataclysm', 'Eye of the Storm']),
    'shapeshifter': (['Mimetismo', 'Depredador', 'Piel cambiante', 'Zarpa fantasma', 'Rugido',
                      'Forma perfecta', 'Camaleón', 'Quimera', 'Manada espectral', 'Desgarro'],
                     ['Mimicry', 'Predator', 'Shifting Skin', 'Phantom Claw', 'Roar',
                      'Perfect Form', 'Chameleon', 'Chimera', 'Spectral Pack', 'Rend']),
    'gunslinger': (['Gatillo fácil', 'Ojo de halcón', 'Rodar', 'Ráfaga', 'Bala perforante',
                    'As de las pistolas', 'Forajido', 'Munición arcana', 'Lluvia de balas', 'Disparo final'],
                   ['Quick Trigger', 'Hawk Eye', 'Roll', 'Burst', 'Piercing Shot',
                    'Gun Ace', 'Outlaw', 'Arcane Ammo', 'Bullet Rain', 'Final Shot']),
    'weapon_summoner': (['Arsenal', 'Filo rúnico', 'Bastión', 'Espadas gemelas', 'Escudo de espadas',
                         'Forja eterna', 'Hoja ancestral', 'Armadura viva', 'Tormenta de espadas', 'Lanza de luz'],
                        ['Arsenal', 'Runic Edge', 'Bastion', 'Twin Blades', 'Sword Shield',
                         'Eternal Forge', 'Ancestral Blade', 'Living Armor', 'Sword Storm', 'Spear of Light']),
    'hunter': (['Rastreo', 'Tiro certero', 'Trampa', 'Disparo múltiple', 'Salto evasivo',
                'Depredador ápice', 'Camuflaje', 'Bestia interior', 'Lluvia de flechas', 'Compañero fiel'],
               ['Tracking', 'True Shot', 'Snare', 'Multishot', 'Evasive Leap',
                'Apex Predator', 'Camouflage', 'Inner Beast', 'Arrow Rain', 'Loyal Companion']),
    'bard': (['Armonía', 'Compás', 'Inspiración', 'Acorde vibrante', 'Himno',
              'Virtuoso', 'Balada eterna', 'Disonancia', 'Sinfonía', 'Crescendo final'],
             ['Harmony', 'Tempo', 'Inspiration', 'Ringing Chord', 'Hymn',
              'Virtuoso', 'Eternal Ballad', 'Dissonance', 'Symphony', 'Final Crescendo']),
    'warlock': (['Maldición', 'Sangre maldita', 'Abismo', 'Rayo del vacío', 'Círculo maldito',
                 'Condena', 'Alma oscura', 'Corrupción', 'Portal del abismo', 'Diablillos'],
                ['Curse', 'Cursed Blood', 'Abyss', 'Void Ray', 'Cursed Circle',
                 'Doom', 'Dark Soul', 'Corruption', 'Abyssal Portal', 'Imps']),
    'tamer': (['Vínculo', 'Manada', 'Instinto salvaje', 'Llamada salvaje', 'Embestida',
               'Rey bestia', 'Piel gruesa', 'Colmillo', 'Rugido alfa', 'Estampida'],
              ['Bond', 'Pack', 'Wild Instinct', 'Call of the Wild', 'Charge',
               'Beast King', 'Thick Hide', 'Fang', 'Alpha Roar', 'Stampede']),
}

EFFECT_NAME = {
    'es': {'LIFESTEAL': 'Robo de vida', 'CRIT': 'Probabilidad de crítico', 'EXECUTE': 'Daño de ejecución',
           'THORNS': 'Espinas', 'DAMAGE_REDUCTION': 'Reducción de daño', 'DODGE': 'Esquiva',
           'MANA_REGEN': 'Regeneración de maná', 'RESOURCE_GAIN': 'Carga del recurso de rol',
           'REGEN': 'Regeneración de vida', 'ON_HIT_POISON': 'Veneno al golpear',
           'ON_HIT_SLOW': 'Lentitud al golpear', 'ON_HIT_FIRE': 'Fuego al golpear',
           'ON_HIT_WITHER': 'Marchitamiento al golpear', 'MINION_POWER': 'Poder de los esbirros',
           'KNOCKBACK': 'Resistencia al empuje', 'SPEED': 'Velocidad', 'MAX_HEALTH': 'Vida máxima',
           'ATTACK_SPEED': 'Velocidad de ataque', 'LUCK': 'Suerte'},
    'en': {'LIFESTEAL': 'Lifesteal', 'CRIT': 'Critical chance', 'EXECUTE': 'Execute damage', 'THORNS': 'Thorns',
           'DAMAGE_REDUCTION': 'Damage reduction', 'DODGE': 'Dodge', 'MANA_REGEN': 'Mana regeneration',
           'RESOURCE_GAIN': 'Role resource gain', 'REGEN': 'Health regeneration',
           'ON_HIT_POISON': 'Poison on hit', 'ON_HIT_SLOW': 'Slowness on hit', 'ON_HIT_FIRE': 'Fire on hit',
           'ON_HIT_WITHER': 'Wither on hit', 'MINION_POWER': 'Minion power', 'KNOCKBACK': 'Knockback resistance',
           'SPEED': 'Speed', 'MAX_HEALTH': 'Max health', 'ATTACK_SPEED': 'Attack speed', 'LUCK': 'Luck'},
}
UNIT = {
    'es': {'MANA_REGEN': ' de maná por segundo', 'REGEN': ' de vida cada 3 s', 'MAX_HEALTH': ' de vida', 'LUCK': ' de suerte'},
    'en': {'MANA_REGEN': ' mana per second', 'REGEN': ' health every 3 s', 'MAX_HEALTH': ' health', 'LUCK': ' luck'},
}

# T8: nombres completos por rol, en orden m1..m5/u1..u5; nunca se traducen por fragmentos.
LOCAL_NAMES = {locale: {} for locale in LOCALES}
for row in """
berserker¦Sede de sangue/Pele de ferro/Fúria cega/Corte sísmico/Investida brutal/Fome de guerra/Vontade inabalável/Tempestade de aço/Terremoto/Grito do titã¦Soif de sang/Peau de fer/Furie aveugle/Entaille sismique/Charge brutale/Faim de guerre/Volonté inflexible/Tempête d'acier/Séisme/Cri du titan¦Blutdurst/Eisenhaut/Blinde Wut/Seismischer Hieb/Brutaler Ansturm/Kriegshunger/Unbeugsamer Wille/Stahlsturm/Erdbeben/Titanenschrei
mage¦Fluxo arcano/Mente afiada/Escudo de mana/Raio prismático/Nova de gelo/Arquimago/Domínio do tempo/Sobrecarga/Chuva de meteoros/Singularidade¦Flux arcanique/Esprit affûté/Bouclier de mana/Rayon prismatique/Nova de givre/Archimage/Maîtrise du temps/Surcharge/Pluie de météores/Singularité¦Arkaner Fluss/Scharfer Geist/Manaschild/Prismenstrahl/Frostnova/Erzmagier/Zeitmeisterschaft/Überladung/Meteorregen/Singularität
assassin¦Golpe certeiro/Passo sombrio/Lâmina envenenada/Dança das lâminas/Salto sombrio/Marca da morte/Véu noturno/Sangria/Tempestade de adagas/Eclipse¦Frappe juste/Pas de l'ombre/Lame empoisonnée/Danse des lames/Bond de l'ombre/Marque de mort/Voile nocturne/Saignée/Tempête de dagues/Éclipse¦Gezielter Schlag/Schattenschritt/Vergiftete Klinge/Klingentanz/Schattensprung/Todesmal/Nachtschleier/Aderlass/Dolchsturm/Finsternis
herbalist¦Seiva viva/Raízes profundas/Espinhos tóxicos/Flor curativa/Nuvem de esporos/Árvore da vida/Pele de casca/Pólen dourado/Jardim sagrado/Vinhas sufocantes¦Sève vivante/Racines profondes/Épines toxiques/Fleur curative/Nuage de spores/Arbre de vie/Peau d'écorce/Pollen doré/Jardin sacré/Lianes étrangleuses¦Lebendiger Saft/Tiefe Wurzeln/Giftdornen/Heilblüte/Sporenwolke/Lebensbaum/Rindenhaut/Goldener Pollen/Heiliger Garten/Würgeranken
engineer¦Blindagem/Engrenagens/Superaquecido/Pulso elétrico/Morteiro/Núcleo reforçado/Precisão/Bateria/Raio laser/Escudo de energia¦Blindage/Engrenages/Surchauffe/Impulsion électrique/Mortier/Noyau renforcé/Précision/Batterie/Rayon laser/Bouclier d'énergie¦Panzerung/Zahnwerk/Überhitzt/Elektroimpuls/Mörser/Verstärkter Kern/Präzision/Batterie/Laserstrahl/Energieschild
lancer¦Postura firme/Perfurar/Carga/Estocada/Lança giratória/Fúria do dragão/Falange/Vento cortante/Chuva de lanças/Raio celeste¦Posture ferme/Perforation/Charge/Fente/Lance tournoyante/Fureur du dragon/Phalange/Vent tranchant/Pluie de lances/Rayon céleste¦Fester Stand/Durchbohren/Ansturm/Ausfall/Wirbelspeer/Drachenzorn/Phalanx/Schneidender Wind/Speerregen/Himmelsstrahl
swordsman¦Fio temperado/Aparar/Ritmo/Corte cruzado/Iai/Kensei/Sangue frio/Mente serena/Mil cortes/Onda de espada¦Tranchant trempé/Parade/Rythme/Entaille croisée/Iai/Kensei/Sang-froid/Esprit serein/Mille entailles/Onde d'épée¦Gehärtete Schneide/Parade/Rhythmus/Kreuzschnitt/Iai/Kensei/Kaltes Blut/Ruhiger Geist/Tausend Schnitte/Schwertwelle
paladin¦Fé/Égide/Castigo/Martelo da justiça/Bênção/Santo/Julgamento/Luz radiante/Consagração/Escudo divino¦Foi/Égide/Châtiment/Marteau de justice/Bénédiction/Saint/Jugement/Lumière radieuse/Consécration/Bouclier divin¦Glaube/Ägide/Züchtigung/Hammer der Gerechtigkeit/Segen/Heiliger/Urteil/Strahlendes Licht/Weihe/Göttlicher Schild
necromancer¦Legião/Dreno/Praga/Erguer os caídos/Nuvem pestilenta/Senhor da morte/Sangue negro/Toque gélido/Exército eterno/Abraço da tumba¦Légion/Drain/Peste/Relever les défunts/Nuage pestilentiel/Seigneur de la mort/Sang noir/Toucher glacial/Armée éternelle/Étreinte du tombeau¦Legion/Entzug/Seuche/Gefallene erwecken/Pestwolke/Herr des Todes/Schwarzes Blut/Frostberührung/Ewige Armee/Grabesumarmung
druid¦Pelagem grossa/Instinto/Garras/Golpe de garra/Luar/Espírito selvagem/Casca antiga/Sarças/Estampida/Fúria da natureza¦Fourrure épaisse/Instinct/Griffes/Coup de griffe/Clair de lune/Esprit sauvage/Écorce ancienne/Ronces/Ruée/Colère de la nature¦Dickes Fell/Instinkt/Klauen/Prankenhieb/Mondlicht/Wilder Geist/Alte Rinde/Brombeerranken/Stampede/Naturzorn
merchant¦Fortuna/Barganha/Bolsa funda/Chuva de ouro/Guarda-costas/Monopólio/Mercenário/Seguro/Golpe de sorte/Escudo de moedas¦Fortune/Marchandage/Bourse profonde/Pluie d'or/Garde du corps/Monopole/Mercenaire/Assurance/Coup de chance/Bouclier de pièces¦Glück/Feilschen/Tiefer Geldbeutel/Goldregen/Leibwächter/Monopol/Söldner/Versicherung/Glückstreffer/Münzschild
monk¦Punho veloz/Lótus/Chi/Palma de choque/Passo do vento/Iluminação/Corpo de diamante/Golpe vital/Mil palmas/Meditação¦Poing vif/Lotus/Chi/Paume de choc/Pas du vent/Illumination/Corps de diamant/Frappe vitale/Mille paumes/Méditation¦Flinke Faust/Lotus/Chi/Schockhand/Windschritt/Erleuchtung/Diamantkörper/Lebensschlag/Tausend Handflächen/Meditation
elementalist¦Catalisador/Condutor/Ignição/Prisma elemental/Tempestade/Avatar/Ressonância/Núcleo elemental/Cataclismo/Olho do furacão¦Catalyseur/Conducteur/Ignition/Prisme élémentaire/Tempête/Avatar/Résonance/Noyau élémentaire/Cataclysme/Œil du cyclone¦Katalysator/Leiter/Entzündung/Elementarprisma/Sturm/Avatar/Resonanz/Elementarkern/Kataklysmus/Auge des Sturms
shapeshifter¦Mimetismo/Predador/Pele mutável/Garra fantasma/Rugido/Forma perfeita/Camaleão/Quimera/Matilha espectral/Dilacerar¦Mimétisme/Prédateur/Peau changeante/Griffe fantôme/Rugissement/Forme parfaite/Caméléon/Chimère/Meute spectrale/Lacération¦Mimikry/Raubtier/Wandelhaut/Geisterklaue/Brüllen/Vollkommene Gestalt/Chamäleon/Chimäre/Geisterrudel/Zerreißen
gunslinger¦Gatilho rápido/Olho de falcão/Rolar/Rajada/Disparo perfurante/Ás das pistolas/Fora da lei/Munição arcana/Chuva de balas/Disparo final¦Détente rapide/Œil de faucon/Roulade/Rafale/Tir perforant/As des pistolets/Hors-la-loi/Munitions arcaniques/Pluie de balles/Tir final¦Schneller Abzug/Falkenauge/Rollen/Salve/Durchschlagender Schuss/Pistolenass/Gesetzloser/Arkane Munition/Kugelregen/Letzter Schuss
weapon_summoner¦Arsenal/Fio rúnico/Bastião/Lâminas gêmeas/Escudo de espadas/Forja eterna/Lâmina rúnica/Armadura viva/Tempestade de espadas/Lança de luz¦Arsenal/Tranchant runique/Bastion/Lames jumelles/Bouclier d'épées/Forge éternelle/Lame runique/Armure vivante/Tempête d'épées/Lance de lumière¦Arsenal/Runenschneide/Bastion/Zwillingsklingen/Schwertschild/Ewige Schmiede/Runenklinge/Lebendige Rüstung/Schwertsturm/Lichtspeer
hunter¦Rastreio/Disparo certeiro/Armadilha/Disparo múltiplo/Salto evasivo/Superpredador/Camuflagem/Fera interior/Chuva de flechas/Companheiro fiel¦Pistage/Tir juste/Piège/Tir multiple/Bond d'esquive/Superprédateur/Camouflage/Bête intérieure/Pluie de flèches/Compagnon fidèle¦Fährtenlesen/Gezielter Schuss/Falle/Mehrfachschuss/Ausweichsprung/Spitzenräuber/Tarnung/Innere Bestie/Pfeilregen/Treuer Gefährte
bard¦Harmonia/Compasso/Inspiração/Acorde vibrante/Hino/Virtuoso/Balada eterna/Dissonância/Sinfonia/Crescendo final¦Harmonie/Tempo/Inspiration/Accord vibrant/Hymne/Virtuose/Ballade éternelle/Dissonance/Symphonie/Crescendo final¦Harmonie/Takt/Inspiration/Klingender Akkord/Hymne/Virtuose/Ewige Ballade/Dissonanz/Sinfonie/Letztes Crescendo
warlock¦Maldição/Sangue maldito/Abismo/Raio do vazio/Círculo maldito/Condenação/Alma sombria/Corrupção/Portal do abismo/Diabretes¦Malédiction/Sang maudit/Abîme/Rayon du vide/Cercle maudit/Condamnation/Âme obscure/Corruption/Portail abyssal/Diablotins¦Fluch/Verfluchtes Blut/Abgrund/Leerenstrahl/Fluchkreis/Verdammnis/Dunkle Seele/Verderbnis/Abgrundportal/Wichtel
tamer¦Vínculo/Matilha/Instinto selvagem/Chamado selvagem/Investida/Rei das feras/Pele grossa/Presa/Rugido alfa/Estampida¦Lien/Meute/Instinct sauvage/Appel sauvage/Charge/Roi des bêtes/Peau épaisse/Croc/Rugissement alpha/Ruée¦Band/Rudel/Wilder Instinkt/Ruf der Wildnis/Ansturm/Bestienkönig/Dicke Haut/Fangzahn/Alphabrüllen/Stampede
""".strip().splitlines():
    role, *columns = row.split('¦')
    for locale, column in zip(LOCALES, columns, strict=True):
        names = column.split('/')
        if len(names) != 10 or role in LOCAL_NAMES[locale]:
            raise ValueError(f'Nombres de evolución inválidos: {locale}/{role}')
        LOCAL_NAMES[locale][role] = names

for locale, labels, units, extras, summons in zip(LOCALES, (
    'Roubo de vida/Chance de crítico/Dano de execução/Espinhos/Redução de dano/Esquiva/Regeneração de mana/Carga do recurso de classe/Regeneração de vida/Veneno ao golpear/Lentidão ao golpear/Fogo ao golpear/Decomposição ao golpear/Poder dos servos/Resistência a repulsão/Velocidade/Vida máxima/Velocidade de ataque/Sorte',
    'Vol de vie/Chance de critique/Dégâts d\'exécution/Épines/Réduction des dégâts/Esquive/Régénération de mana/Charge de ressource de rôle/Régénération de santé/Poison au coup/Lenteur au coup/Feu au coup/Wither au coup/Puissance des serviteurs/Résistance au recul/Vitesse/Santé maximale/Vitesse d\'attaque/Chance',
    'Lebensraub/Kritische Trefferchance/Hinrichtungsschaden/Dornen/Schadensreduktion/Ausweichen/Manaregeneration/Rollenressourcengewinn/Lebensregeneration/Gift beim Treffer/Langsamkeit beim Treffer/Feuer beim Treffer/Ausdörrung beim Treffer/Dienerstärke/Rückstoßresistenz/Geschwindigkeit/Maximales Leben/Angriffsgeschwindigkeit/Glück',
), (
    (' de mana por segundo', ' de vida a cada 3 s', ' de vida', ' de sorte'),
    (' de mana par seconde', ' de santé toutes les 3 s', ' de santé', ' de chance'),
    (' Mana pro Sekunde', ' Leben alle 3 s', ' Leben', ' Glück'),
), (
    ('', ' e Lentidão II', ' e incendeia', ' e Decomposição', ' e veneno', ' e lança ao ar', ' e forte repulsão'),
    ('', ' et Lenteur II', ' et enflamme', ' et Wither', ' et poison', ' et projette en l\'air', ' et fort recul'),
    ('', ' und Langsamkeit II', ' und entzündet', ' und Ausdörrung', ' und Gift', ' und schleudert hoch', ' und starker Rückstoß'),
), (
    ('zumbis', 'esqueletos', 'lobos', 'vexes'),
    ('zombies', 'squelettes', 'loups', 'vex'),
    ('Zombies', 'Skelette', 'Wölfe', 'Plagegeister'),
), strict=True):
    EFFECT_NAME[locale] = dict(zip(EFFECTS, labels.split('/'), strict=True))
    UNIT[locale] = dict(zip(UNIT['en'], units, strict=True))
    # EXTRA y SUMMON se amplían después de definir sus tablas originales.
    LOCAL_NAMES[locale]['_extra'] = extras
    LOCAL_NAMES[locale]['_summon'] = summons


def num(v, lang):
    s = ('%.1f' % v).rstrip('0').rstrip('.')
    return s.replace('.', ',') if lang == 'es' else s


def fmt(effect, value, lang):
    per, pct = EFFECTS[effect]
    if pct:
        return '+' + num(value * 100, lang) + (' %%' if lang == 'es' else '%%')
    return '+' + num(value, lang) + UNIT[lang][effect]


def passive_desc(effect, tier, lang):
    per, pct = EFFECTS[effect]
    mult = 1.5 if tier == 1 else 3.0
    v = per * mult
    name = EFFECT_NAME[lang][effect]
    if lang in LOCALES:
        phrases = {
            'pt_br': ('por grau', 'máx.', 'Soma aos Caminhos.'),
            'fr_fr': ('par rang', 'max', "Se cumule avec les Voies."),
            'de_de': ('pro Rang', 'max.', 'Addiert sich zu den Pfaden.'),
        }
        per_rank, maximum, stacks = phrases[lang]
        value = fmt(effect, v, lang)
        if tier == 1:
            value += f' {per_rank} ({maximum} {fmt(effect, v * 2, lang)})'
        return f'{name}: {value}. {stacks}'
    if tier == 1:
        if lang == 'es':
            return f"{name}: {fmt(effect, v, lang)} por rango (máx. {fmt(effect, v * 2, lang)}). Se suma a las Sendas."
        return f"{name}: {fmt(effect, v, lang)} per rank (max {fmt(effect, v * 2, lang)}). Stacks with Paths."
    if lang == 'es':
        return f"{name}: {fmt(effect, v, lang)}. Se suma a las Sendas."
    return f"{name}: {fmt(effect, v, lang)}. Stacks with Paths."


EXTRA = {
    'es': {'NONE': '', 'SLOW': ' y Lentitud II', 'FIRE': ' y prende fuego', 'WITHER': ' y Marchitamiento',
           'POISON': ' y veneno', 'LAUNCH': ' y lanza por los aires', 'KNOCKBACK': ' y gran empuje'},
    'en': {'NONE': '', 'SLOW': ' and Slowness II', 'FIRE': ' and sets on fire', 'WITHER': ' and Wither',
           'POISON': ' and poison', 'LAUNCH': ' and launches into the air', 'KNOCKBACK': ' and heavy knockback'},
}
SUMMON = {'es': {'ZOMBIES': 'zombis', 'SKELETONS': 'esqueletos', 'WOLVES': 'lobos', 'VEXES': 'vexes'},
          'en': {'ZOMBIES': 'zombies', 'SKELETONS': 'skeletons', 'WOLVES': 'wolves', 'VEXES': 'vexes'}}

for locale in LOCALES:
    EXTRA[locale] = dict(zip(EXTRA['en'], LOCAL_NAMES[locale].pop('_extra'), strict=True))
    SUMMON[locale] = dict(zip(SUMMON['en'], LOCAL_NAMES[locale].pop('_summon'), strict=True))

ACTIVE = {
    'pt_br': (
        'Explosão ao redor (raio 5+): {d} de dano{x}.',
        'Raio de 24 blocos que atravessa todos os inimigos: {d} de dano (+15%%){x}.',
        'Varredura frontal de 6 blocos: {d} de dano e repulsão{x}.',
        'Investida: {d} de dano em cada inimigo atravessado{x}. Sem dano de queda.',
        '8 impactos em 4 s no local indicado; cada um causa 45%% de {d}{x}.',
        'Atrai inimigos ao local indicado por 2 s e explode: {d} de dano{x}.',
        '{buff} para você, aliados próximos e servos (12 s + 2 por grau).',
        'Cura {h} + 1.5 por grau de vida em você e aliados (8 blocos) e remove efeitos negativos.',
        'Absorção e Resistência I para você e aliados (6 blocos); absorção aumenta com o grau.',
        'Invoca de 1 a 3 {who} aliados (mais com o grau). Seguem as regras dos servos.'),
    'fr_fr': (
        'Explosion autour de vous (rayon 5+) : {d} dégâts{x}.',
        'Rayon de 24 blocs traversant tous les ennemis : {d} dégâts (+15%%){x}.',
        'Balayage frontal de 6 blocs : {d} dégâts et recul{x}.',
        'Charge en avant : {d} dégâts par ennemi traversé{x}. Sans dégâts de chute.',
        '8 impacts en 4 s sur le point visé ; chacun inflige 45%% de {d}{x}.',
        'Attire les ennemis au point visé pendant 2 s puis explose : {d} dégâts{x}.',
        '{buff} pour vous, les alliés proches et vos serviteurs (12 s + 2 par rang).',
        'Rend {h} + 1.5 de santé par rang à vous et aux alliés (8 blocs) et retire les effets néfastes.',
        'Absorption et Résistance I pour vous et les alliés (6 blocs) ; l\'absorption croît avec le rang.',
        'Invoque de 1 à 3 {who} alliés (davantage avec le rang). Suivent les règles des serviteurs.'),
    'de_de': (
        'Explosion um dich (Radius 5+): {d} Schaden{x}.',
        'Strahl über 24 Blöcke durch alle Feinde: {d} Schaden (+15%%){x}.',
        'Frontschwung über 6 Blöcke: {d} Schaden und Rückstoß{x}.',
        'Ansturm nach vorn: {d} Schaden an jedem durchquerten Feind{x}. Kein Fallschaden.',
        '8 Einschläge über 4 s am Zielpunkt; jeder verursacht 45%% von {d}{x}.',
        'Zieht Feinde für 2 s zum Zielpunkt und explodiert: {d} Schaden{x}.',
        '{buff} für dich, nahe Verbündete und deine Diener (12 s + 2 pro Rang).',
        'Heilt dich und Verbündete (8 Blöcke) um {h} + 1.5 Leben pro Rang und entfernt negative Effekte.',
        'Absorption und Resistenz I für dich und Verbündete (6 Blöcke); Absorption steigt mit dem Rang.',
        'Beschwört 1 bis 3 verbündete {who} (mehr mit höherem Rang). Es gelten die Dienerregeln.'),
}
ACTIVE_KEYS = ('NOVA', 'BEAM', 'CONE', 'DASH', 'ZONE', 'VORTEX', 'AURA', 'HEAL', 'SHIELD', 'SUMMON')
BUFFS = {
    'pt_br': ('Força e Resistência', 'Invisibilidade e Velocidade II', 'Velocidade, Força e Regeneração'),
    'fr_fr': ('Force et Résistance', 'Invisibilité et Vitesse II', 'Vitesse, Force et Régénération'),
    'de_de': ('Stärke und Resistenz', 'Unsichtbarkeit und Schnelligkeit II', 'Schnelligkeit, Stärke und Regeneration'),
}


def active_desc(template, extra, tier, lang):
    base = 10 if tier == 2 else 6
    if lang in LOCALES:
        per_rank = {'pt_br': 'por grau', 'fr_fr': 'par rang', 'de_de': 'pro Rang'}[lang]
        pattern = dict(zip(ACTIVE_KEYS, ACTIVE[lang], strict=True))[template]
        buff = dict(zip(('FORCE', 'SHADOW', 'SONG'), BUFFS[lang], strict=True)).get(extra, '')
        return pattern.format(d=f'{base} + 1.5 {per_rank}', x=EXTRA[lang].get(extra, ''),
                              buff=buff, h=8 if tier == 2 else 4, who=SUMMON[lang].get(extra, ''))
    es = lang == 'es'
    d = f"{base} + 1,5 por rango" if es else f"{base} + 1.5 per rank"
    x = EXTRA[lang].get(extra, '')
    if template == 'NOVA':
        return (f"Explosión a tu alrededor (radio 5+): {d} de daño{x}." if es
                else f"Explosion around you (radius 5+): {d} damage{x}.")
    if template == 'BEAM':
        return (f"Rayo de 24 bloques que atraviesa a todos los enemigos: {d} de daño (+15 %%){x}." if es
                else f"24-block ray that pierces every enemy: {d} damage (+15%%){x}.")
    if template == 'CONE':
        return (f"Barrido frontal de 6 bloques: {d} de daño y empuje{x}." if es
                else f"6-block frontal sweep: {d} damage and knockback{x}.")
    if template == 'DASH':
        return (f"Embestida hacia delante: {d} de daño a cada enemigo que atraviesas{x}. Sin daño de caída." if es
                else f"Forward charge: {d} damage to every enemy you pass through{x}. No fall damage.")
    if template == 'ZONE':
        return (f"Lluvia de 8 impactos en 4 s sobre el punto señalado; cada uno hace el 45 %% de {d}{x}." if es
                else f"8 impacts over 4 s on the targeted spot; each deals 45%% of {d}{x}.")
    if template == 'VORTEX':
        return (f"Atrae a los enemigos al punto señalado durante 2 s y estalla: {d} de daño{x}." if es
                else f"Pulls enemies to the targeted spot for 2 s, then bursts: {d} damage{x}.")
    if template == 'AURA':
        buffs = {'FORCE': ('Fuerza y Resistencia', 'Strength and Resistance'),
                 'SHADOW': ('Invisibilidad y Velocidad II', 'Invisibility and Speed II'),
                 'SONG': ('Velocidad, Fuerza y Regeneración', 'Speed, Strength and Regeneration')}[extra]
        return (f"{buffs[0]} para ti, tus aliados cercanos y tus esbirros (12 s + 2 por rango)." if es
                else f"{buffs[1]} for you, nearby allies and your minions (12 s + 2 per rank).")
    if template == 'HEAL':
        h = 8 if tier == 2 else 4
        return (f"Cura {h} + 1,5 por rango de vida a ti y a tus aliados (8 bloques) y quita los efectos negativos." if es
                else f"Heals {h} + 1.5 per rank health to you and allies (8 blocks) and removes harmful effects.")
    if template == 'SHIELD':
        return ("Absorción y Resistencia I para ti y tus aliados (6 bloques); la absorción sube con el rango." if es
                else "Absorption and Resistance I for you and allies (6 blocks); absorption grows with rank.")
    if template == 'SUMMON':
        who = SUMMON[lang][extra]
        return (f"Invoca de 1 a 3 {who} aliados (más con el rango). Siguen las reglas de los esbirros." if es
                else f"Summons 1 to 3 allied {who} (more with rank). They follow minion rules.")
    raise ValueError(template)


UI = {
    'en_us': {
        'screen.aptitudes.tree.row_master': '✦ MASTER · level 20 · up to 2 ranks',
        'screen.aptitudes.tree.row_ultimate': '✦ ULTIMATE · level 30 · 1 rank',
        'screen.aptitudes.tree.requires_master': 'Locked: evolve the role to Master',
        'screen.aptitudes.tree.requires_ultimate': 'Locked: evolve the role to Ultimate',
        'screen.aptitudes.tree.tier_master': 'Master',
        'screen.aptitudes.tree.tier_ultimate': 'Ultimate',
        'screen.aptitudes.node.evolved': '%s · %s',
        'aptitudes.role.upgrade.tier_too_low': 'This node requires a higher evolution of the role.',
    },
    'es_es': {
        'screen.aptitudes.tree.row_master': '✦ MAESTRO · nivel 20 · hasta 2 rangos',
        'screen.aptitudes.tree.row_ultimate': '✦ DEFINITIVO · nivel 30 · 1 rango',
        'screen.aptitudes.tree.requires_master': 'Bloqueado: evoluciona el rol a Maestro',
        'screen.aptitudes.tree.requires_ultimate': 'Bloqueado: evoluciona el rol a Definitivo',
        'screen.aptitudes.tree.tier_master': 'Maestro',
        'screen.aptitudes.tree.tier_ultimate': 'Definitivo',
        'screen.aptitudes.node.evolved': '%s · %s',
        'aptitudes.role.upgrade.tier_too_low': 'Este nodo requiere una evolución superior del rol.',
    },
}
GUIDE = {
    'en_us': ('\\n\\nEVOLUTION ROWS: Master opens five nodes (three passives and two actives, up to two ranks); '
              'Ultimate opens another five (one rank). They remain locked at the bottom of the tree until you evolve.'),
    'es_es': ('\\n\\nFILAS DE EVOLUCIÓN: Maestro abre cinco nodos (tres pasivas y dos activas, hasta dos rangos); '
              'Definitivo abre otros cinco (un rango). Se ven bloqueados al final del árbol hasta evolucionar.'),
    'pt_br': ('\\n\\nFILAS DE EVOLUÇÃO: Mestre libera cinco nós (três passivas e duas ativas, até dois graus); '
              'Definitivo libera mais cinco (um grau). Permanecem bloqueados no fim da árvore até você evoluir.'),
    'fr_fr': ('\\n\\nRANGÉES D’ÉVOLUTION : Maître ouvre cinq nœuds (trois passifs et deux actifs, jusqu’à deux rangs) ; '
              'Ultime en ouvre cinq autres (un rang). Ils restent verrouillés en bas de l’arbre jusqu’à votre évolution.'),
    'de_de': ('\\n\\nENTWICKLUNGSREIHEN: Meister öffnet fünf Knoten (drei passive und zwei aktive, bis zu zwei Ränge); '
              'Ultimativ öffnet fünf weitere (ein Rang). Sie bleiben unten im Baum gesperrt, bis du dich entwickelst.'),
}


def guide_extra(lang):
    """Solo expande saltos de línea; conserva acentos y el párrafo vigente del grimorio."""
    return GUIDE[lang].replace('\\n', '\n')


# Rótulos generados de la interfaz de evolución.
for locale, labels in {
    'pt_br': (
        '✦ MESTRE · nível 20 · até 2 graus', '✦ DEFINITIVO · nível 30 · 1 grau',
        'Bloqueado: evolua a classe para Mestre', 'Bloqueado: evolua a classe para Definitivo',
        'Mestre', 'Definitivo', '%s · %s', 'Este nó requer um estágio de evolução superior da classe.'),
    'fr_fr': (
        '✦ MAÎTRE · niveau 20 · jusqu’à 2 rangs', '✦ ULTIME · niveau 30 · 1 rang',
        'Verrouillé : faites évoluer le rôle au stade Maître', 'Verrouillé : faites évoluer le rôle au stade Ultime',
        'Maître', 'Ultime', '%s · %s', 'Ce nœud nécessite une évolution supérieure du rôle.'),
    'de_de': (
        '✦ MEISTER · Stufe 20 · bis zu 2 Ränge', '✦ ULTIMATIV · Stufe 30 · 1 Rang',
        'Gesperrt: Entwickle die Rolle zum Meister', 'Gesperrt: Entwickle die Rolle zu Ultimativ',
        'Meister', 'Ultimativ', '%s · %s', 'Dieser Knoten erfordert eine höhere Rollenentwicklung.'),
}.items():
    UI[locale] = dict(zip(UI['en_us'], labels, strict=True))


def build(lang):
    out = {}
    code = lang if lang in LOCALES else 'es' if lang == 'es_es' else 'en'
    for role, (mp, ma, up, ua) in TREE.items():
        names = LOCAL_NAMES[lang][role] if lang in LOCALES else NAMES[role][0 if code == 'es' else 1]
        specs = [('P', e, 1) for e in mp] + [('A', a, 1) for a in ma] + [('P', e, 2) for e in up] + [('A', a, 2) for a in ua]
        for i, (kind, spec, tier) in enumerate(specs):
            nid = f"{role}_{'m' if i < 5 else 'u'}{i % 5 + 1}"
            out[f'node.aptitudes.{nid}'] = names[i]
            out[f'node.aptitudes.{nid}.desc'] = (passive_desc(spec, tier, code) if kind == 'P'
                                                 else active_desc(spec[0], spec[1], tier, code))
    out.update(UI[lang])
    return out


def update(lang, data):
    new = build(lang)
    guide = 'guide.aptitudes.tree.body'
    extra = guide_extra(lang)
    if extra.strip() not in data[guide]:
        data[guide] = data[guide] + extra
    # Actualiza en su sitio las claves existentes y añade al final solo las nuevas:
    # así regenerar no reordena el archivo ni produce diferencias innecesarias.
    data.update(new)
    return data


if __name__ == '__main__':
    from generator_cli import run
    raise SystemExit(run(update, __doc__))
