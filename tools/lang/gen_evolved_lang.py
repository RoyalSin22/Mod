#!/usr/bin/env python3
"""
Genera los textos de los 200 nodos evolucionados (v23) en en_us y es_es y copia es_es a las variantes.

Los valores numéricos salen de los mismos datos que el código (EvolvedTree / EvolvedAbilities):
 - pasivas: valor de senda (PathEffect.perRank) × 1,5 (Maestro, 2 rangos) o × 3 (Definitivo, 1 rango)
 - activas: daño base 6 (Maestro) / 10 (Definitivo) + 1,5 por rango efectivo
Si cambias esos números en Java, cambia también las constantes de abajo y vuelve a ejecutarlo.

Uso: python3 tools/lang/gen_evolved_lang.py
"""
import json
import os

ROOT = os.path.join(os.path.dirname(__file__), '..', '..')
L = os.path.join(ROOT, 'src/main/resources/assets/aptitudes/lang/')

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


def active_desc(template, extra, tier, lang):
    base = 10 if tier == 2 else 6
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
    'en_us': ('\\n\\nEVOLUTION ROWS: evolving to Master unlocks a new row of 5 nodes (3 passives and 2 actives, '
              'up to 2 ranks, +10 talent points); Ultimate unlocks another row (1 rank each, +5 points). Until then '
              'they are shown locked at the bottom of the tree.'),
    'es_es': ('\\n\\nFILAS DE EVOLUCIÓN: al evolucionar a Maestro se desbloquea una fila nueva de 5 nodos (3 pasivas y '
              '2 activas, hasta 2 rangos, +10 puntos de talento); con Definitivo, otra fila (1 rango cada nodo, +5 '
              'puntos). Hasta entonces se ven bloqueadas al final del árbol.'),
}


def build(lang):
    out = {}
    code = 'es' if lang == 'es_es' else 'en'
    for role, (mp, ma, up, ua) in TREE.items():
        names = NAMES[role][0 if code == 'es' else 1]
        specs = [('P', e, 1) for e in mp] + [('A', a, 1) for a in ma] + [('P', e, 2) for e in up] + [('A', a, 2) for a in ua]
        for i, (kind, spec, tier) in enumerate(specs):
            nid = f"{role}_{'m' if i < 5 else 'u'}{i % 5 + 1}"
            out[f'node.aptitudes.{nid}'] = names[i]
            out[f'node.aptitudes.{nid}.desc'] = (passive_desc(spec, tier, code) if kind == 'P'
                                                 else active_desc(spec[0], spec[1], tier, code))
    out.update(UI[lang])
    return out


def write(lang):
    path = L + lang + '.json'
    data = json.load(open(path, encoding='utf-8'))
    new = build(lang)
    for k in list(data):
        if k in new:
            del data[k]
    guide = 'guide.aptitudes.tree.body'
    extra = GUIDE[lang].encode().decode('unicode_escape')
    if extra.strip() not in data[guide]:
        data[guide] = data[guide] + extra
    data.update(new)
    text = json.dumps(data, ensure_ascii=False, indent='\t')
    open(path, 'w', encoding='utf-8').write(text + '\n')
    return len(new)


if __name__ == '__main__':
    n_en = write('en_us')
    n_es = write('es_es')
    src = open(L + 'es_es.json', encoding='utf-8').read()
    for v in ['es_mx', 'es_ar', 'es_cl', 'es_ec', 'es_uy', 'es_ve']:
        open(L + v + '.json', 'w', encoding='utf-8').write(src)
    print(f'{n_en} claves en en_us, {n_es} en es_es (copiado a 6 variantes)')
