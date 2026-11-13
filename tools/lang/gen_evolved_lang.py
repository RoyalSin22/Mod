#!/usr/bin/env python3
"""
Genera los 200 nodos evolucionados en inglés, español, portugués, francés y alemán.
Español se copia a sus seis variantes. --check comprueba sin escribir.

Los valores numéricos salen de los mismos datos que el código (EvolvedTree / EvolvedAbilities):
 - pasivas: valor de senda (PathEffect.perRank) × 1,5 (Maestro, 2 rangos) o × 3 (Definitivo, 1 rango)
 - activas: daño base 6 (Maestro) / 10 (Definitivo) + 1,5 por rango efectivo
Estilo llano (v67): primero qué hace, hablándole al jugador, y después las cifras; sin coletillas.
Si cambias esos números en Java, cambia también las constantes de abajo y vuelve a ejecutarlo.

Uso: python3 tools/lang/gen_evolved_lang.py
"""
from check_lang import LOCALES
from gen_path_lang import RANKS, SENTENCE, amount as path_amount

RANKS_2 = {lang: text.replace(' / 3', '') for lang, text in RANKS.items()}
BONUS = {'es': 'Bonificación:', 'en': 'Bonus:', 'pt_br': 'Bônus:', 'fr_fr': 'Bonus :', 'de_de': 'Bonus:'}

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

for locale, extras, summons in zip(LOCALES, (
    ('', ' Também aplica Lentidão II.', ' Também incendeia os inimigos.', ' Também aplica Decomposição.',
     ' Também os envenena.', ' Também os lança ao ar.', ' Também os empurra com força.'),
    ('', ' Applique aussi Lenteur II.', ' Enflamme aussi les ennemis.', ' Applique aussi Wither.',
     ' Les empoisonne aussi.', ' Les projette aussi en l’air.', ' Les repousse aussi violemment.'),
    ('', ' Verursacht außerdem Langsamkeit II.', ' Setzt Feinde außerdem in Brand.', ' Verursacht außerdem Ausdörrung.',
     ' Vergiftet sie außerdem.', ' Schleudert sie außerdem hoch.', ' Stößt sie außerdem stark zurück.'),
), (
    ('zumbis', 'esqueletos', 'lobos', 'vexes'),
    ('zombies', 'squelettes', 'loups', 'vex'),
    ('Zombies', 'Skelette', 'Wölfe', 'Plagegeister'),
), strict=True):
    # EXTRA y SUMMON se amplían después de definir sus tablas originales.
    LOCAL_NAMES[locale]['_extra'] = extras
    LOCAL_NAMES[locale]['_summon'] = summons


def passive_desc(effect, tier, lang):
    """Frase llana del efecto (la misma de las Sendas) y su valor: Maestro × 1,5 en 2 rangos; Definitivo × 3."""
    v = EFFECTS[effect][0] * (1.5 if tier == 1 else 3.0)
    if tier == 1:
        values = f'{RANKS_2[lang]} {path_amount(effect, v, lang)} / {path_amount(effect, v * 2, lang)}.'
    else:
        values = f'{BONUS[lang]} {path_amount(effect, v, lang)}.'
    # Estas claves se han escrito siempre con %% (sin argumentos se muestra como %).
    return f'{SENTENCE[lang][effect]} {values}'.replace('%', '%%')


# Efecto añadido de las activas, como frase aparte.
EXTRA = {
    'es': {'NONE': '', 'SLOW': ' Además, aplica Lentitud II.', 'FIRE': ' Además, prende fuego a los enemigos.',
           'WITHER': ' Además, aplica Marchitamiento.', 'POISON': ' Además, los envenena.',
           'LAUNCH': ' Además, los lanza por los aires.', 'KNOCKBACK': ' Además, los empuja con fuerza.'},
    'en': {'NONE': '', 'SLOW': ' It also applies Slowness II.', 'FIRE': ' It also sets enemies on fire.',
           'WITHER': ' It also applies Wither.', 'POISON': ' It also poisons them.',
           'LAUNCH': ' It also launches them into the air.', 'KNOCKBACK': ' It also knocks them back hard.'},
}
SUMMON = {'es': {'ZOMBIES': 'zombis', 'SKELETONS': 'esqueletos', 'WOLVES': 'lobos', 'VEXES': 'vexes'},
          'en': {'ZOMBIES': 'zombies', 'SKELETONS': 'skeletons', 'WOLVES': 'wolves', 'VEXES': 'vexes'}}

for locale in LOCALES:
    EXTRA[locale] = dict(zip(EXTRA['en'], LOCAL_NAMES[locale].pop('_extra'), strict=True))
    SUMMON[locale] = dict(zip(SUMMON['en'], LOCAL_NAMES[locale].pop('_summon'), strict=True))

ACTIVE = {
    'es': (
        'Liberas una explosión a tu alrededor que daña a los enemigos cercanos (radio de 5 bloques, algo más con cada rango). Daño: {d}.{x}',
        'Disparas un rayo recto de 24 bloques que atraviesa a todos los enemigos de la línea. Daño: {d}, más un 15 %%.{x}',
        'Das un barrido frontal de 6 bloques que daña y empuja a los enemigos. Daño: {d}.{x}',
        'Embistes hacia delante y dañas a cada enemigo que atraviesas. No recibes daño de caída. Daño: {d}.{x}',
        'Caen 8 impactos durante 4 s sobre el punto que señalas; cada uno hace el 45 %% del daño. Daño: {d}.{x}',
        'Atraes a los enemigos hacia el punto que señalas durante 2 s y luego estalla. Daño: {d}.{x}',
        'Tú, tus aliados cercanos y tus esbirros ganan {buff}. Duración: 12 s, +2 s por rango.',
        'Te curas a ti y a tus aliados a menos de 8 bloques, y quitas los efectos negativos. Curación: {h} + 1,5 por rango.',
        'Tú y tus aliados a menos de 6 bloques ganan absorción y Resistencia I. La absorción sube con el rango.',
        'Invocas de 1 a 3 {who} que luchan a tu lado (más con el rango). Cuentan como esbirros.'),
    'en': (
        'You release a blast around you that hits nearby enemies (5-block radius, a little more each rank). Damage: {d}.{x}',
        'You fire a straight 24-block ray that pierces every enemy in line. Damage: {d}, plus 15%%.{x}',
        'You sweep 6 blocks in front of you, hitting and knocking back enemies. Damage: {d}.{x}',
        'You charge forward and hit every enemy you pass through. You take no fall damage. Damage: {d}.{x}',
        '8 impacts fall over 4 s on the spot you aim at; each deals 45%% of the damage. Damage: {d}.{x}',
        'You pull enemies toward the spot you aim at for 2 s, then it bursts. Damage: {d}.{x}',
        'You, nearby allies and your minions gain {buff}. Duration: 12 s, +2 s per rank.',
        'You heal yourself and allies within 8 blocks and remove harmful effects. Healing: {h} + 1.5 per rank.',
        'You and allies within 6 blocks gain absorption and Resistance I. Absorption grows with rank.',
        'You summon 1 to 3 {who} that fight by your side (more with rank). They count as minions.'),
    'pt_br': (
        'Você libera uma explosão ao seu redor que atinge os inimigos próximos (raio de 5 blocos, um pouco mais a cada grau). Dano: {d}.{x}',
        'Você dispara um raio reto de 24 blocos que atravessa todos os inimigos da linha. Dano: {d}, mais 15%%.{x}',
        'Você varre 6 blocos à sua frente, atingindo e empurrando os inimigos. Dano: {d}.{x}',
        'Você investe para a frente e atinge cada inimigo que atravessa. Não sofre dano de queda. Dano: {d}.{x}',
        '8 impactos caem durante 4 s no ponto que você mira; cada um causa 45%% do dano. Dano: {d}.{x}',
        'Você puxa os inimigos para o ponto que mira durante 2 s, e então ele explode. Dano: {d}.{x}',
        'Você, os aliados próximos e seus servos recebem {buff}. Duração: 12 s, +2 s por grau.',
        'Você cura a si e aos aliados a até 8 blocos e remove efeitos negativos. Cura: {h} + 1.5 por grau.',
        'Você e os aliados a até 6 blocos recebem absorção e Resistência I. A absorção aumenta com o grau.',
        'Você invoca de 1 a 3 {who} que lutam ao seu lado (mais com o grau). Contam como servos.'),
    'fr_fr': (
        'Vous libérez une explosion autour de vous qui frappe les ennemis proches (rayon de 5 blocs, un peu plus à chaque rang). Dégâts : {d}.{x}',
        'Vous tirez un rayon droit de 24 blocs qui traverse tous les ennemis alignés. Dégâts : {d}, plus 15%%.{x}',
        'Vous balayez 6 blocs devant vous, frappant et repoussant les ennemis. Dégâts : {d}.{x}',
        'Vous chargez en avant et frappez chaque ennemi traversé. Vous ne subissez pas de dégâts de chute. Dégâts : {d}.{x}',
        '8 impacts tombent pendant 4 s sur le point visé ; chacun inflige 45%% des dégâts. Dégâts : {d}.{x}',
        'Vous attirez les ennemis vers le point visé pendant 2 s, puis il explose. Dégâts : {d}.{x}',
        'Vous, les alliés proches et vos serviteurs gagnez {buff}. Durée : 12 s, +2 s par rang.',
        'Vous vous soignez, ainsi que les alliés à moins de 8 blocs, et retirez les effets néfastes. Soin : {h} + 1.5 par rang.',
        'Vous et les alliés à moins de 6 blocs gagnez absorption et Résistance I. L’absorption croît avec le rang.',
        'Vous invoquez de 1 à 3 {who} qui combattent à vos côtés (davantage avec le rang). Ils comptent comme serviteurs.'),
    'de_de': (
        'Du entfesselst eine Explosion um dich, die nahe Feinde trifft (Radius 5 Blöcke, mit jedem Rang etwas mehr). Schaden: {d}.{x}',
        'Du feuerst einen geraden Strahl über 24 Blöcke, der alle Feinde in der Linie durchdringt. Schaden: {d}, plus 15%%.{x}',
        'Du fegst 6 Blöcke vor dir, triffst Feinde und stößt sie zurück. Schaden: {d}.{x}',
        'Du stürmst nach vorn und triffst jeden Feind, den du durchquerst. Du erleidest keinen Fallschaden. Schaden: {d}.{x}',
        '8 Einschläge fallen über 4 s auf den anvisierten Punkt; jeder verursacht 45%% des Schadens. Schaden: {d}.{x}',
        'Du ziehst Feinde 2 s lang zum anvisierten Punkt, dann explodiert er. Schaden: {d}.{x}',
        'Du, nahe Verbündete und deine Diener erhaltet {buff}. Dauer: 12 s, +2 s pro Rang.',
        'Du heilst dich und Verbündete im Umkreis von 8 Blöcken und entfernst negative Effekte. Heilung: {h} + 1.5 pro Rang.',
        'Du und Verbündete im Umkreis von 6 Blöcken erhaltet Absorption und Resistenz I. Die Absorption steigt mit dem Rang.',
        'Du beschwörst 1 bis 3 {who}, die an deiner Seite kämpfen (mehr mit höherem Rang). Sie zählen als Diener.'),
}
ACTIVE_KEYS = ('NOVA', 'BEAM', 'CONE', 'DASH', 'ZONE', 'VORTEX', 'AURA', 'HEAL', 'SHIELD', 'SUMMON')
BUFFS = {
    'es': ('Fuerza y Resistencia', 'Invisibilidad y Velocidad II', 'Velocidad, Fuerza y Regeneración'),
    'en': ('Strength and Resistance', 'Invisibility and Speed II', 'Speed, Strength and Regeneration'),
    'pt_br': ('Força e Resistência', 'Invisibilidade e Velocidade II', 'Velocidade, Força e Regeneração'),
    'fr_fr': ('Force et Résistance', 'Invisibilité et Vitesse II', 'Vitesse, Force et Régénération'),
    'de_de': ('Stärke und Resistenz', 'Unsichtbarkeit und Schnelligkeit II', 'Schnelligkeit, Stärke und Regeneration'),
}


PER_RANK = {'es': '1,5 por rango', 'en': '1.5 per rank', 'pt_br': '1.5 por grau', 'fr_fr': '1.5 par rang',
            'de_de': '1.5 pro Rang'}


def active_desc(template, extra, tier, lang):
    """Qué hace la activa y, después, su cifra: daño 6 (Maestro) o 10 (Definitivo) + 1,5 por rango efectivo."""
    base = 10 if tier == 2 else 6
    pattern = dict(zip(ACTIVE_KEYS, ACTIVE[lang], strict=True))[template]
    buff = dict(zip(('FORCE', 'SHADOW', 'SONG'), BUFFS[lang], strict=True)).get(extra, '')
    return pattern.format(d=f'{base} + {PER_RANK[lang]}', x=EXTRA[lang].get(extra, ''), buff=buff,
                          h=8 if tier == 2 else 4, who=SUMMON[lang].get(extra, ''))


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
