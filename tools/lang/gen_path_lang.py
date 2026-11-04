#!/usr/bin/env python3
"""
Genera los textos de las Sendas de la v46 en en_us y es_es y copia es_es a las variantes.

- Nombres y descripciones de los escalones nuevos S4, S5x, S5y y S6 de las 60 sendas (240 nodos).
- Descripción de S1–S3 actualizada: las sendas son exclusivas (antes «2 de las 3»).
- Frase de cada senda (path.aptitudes.<rol>_<a|b|c>.desc) y textos de la vista de sendas.

Los EFECTOS se leen de src/main/java/dev/aptitudes/role/RoleTree.java (paths(...) y deeper(...)), así que
nombres y números no se desincronizan del código. Los nombres salen de docs/V46_DISENO_SENDAS.md §10.
No edites estas claves a mano en los JSON: cambia este script y vuelve a ejecutarlo.

Uso: python3 tools/lang/gen_path_lang.py
"""
import json
import os
import re

ROOT = os.path.join(os.path.dirname(__file__), '..', '..')
L = os.path.join(ROOT, 'src/main/resources/assets/aptitudes/lang/')
JAVA = os.path.join(ROOT, 'src/main/java/dev/aptitudes/role/RoleTree.java')

# PathEffect: (valor por rango, porcentaje) — igual que PathEffect.java
EFFECTS = {
    'LIFESTEAL': (0.02, True), 'CRIT': (0.04, True), 'EXECUTE': (0.06, True), 'THORNS': (0.06, True),
    'DAMAGE_REDUCTION': (0.03, True), 'DODGE': (0.03, True), 'MANA_REGEN': (0.4, False),
    'RESOURCE_GAIN': (0.12, True), 'REGEN': (0.5, False), 'ON_HIT_POISON': (0.08, True),
    'ON_HIT_SLOW': (0.08, True), 'ON_HIT_FIRE': (0.08, True), 'ON_HIT_WITHER': (0.08, True),
    'MINION_POWER': (0.10, True), 'KNOCKBACK': (0.06, True), 'SPEED': (0.02, True), 'MAX_HEALTH': (1.5, False),
    'ATTACK_SPEED': (0.03, True), 'LUCK': (0.4, False),
}
CODE = {'L': 'LIFESTEAL', 'C': 'CRIT', 'X': 'EXECUTE', 'T': 'THORNS', 'R': 'DAMAGE_REDUCTION', 'D': 'DODGE',
        'M': 'MANA_REGEN', 'G': 'RESOURCE_GAIN', 'H': 'REGEN', 'P': 'ON_HIT_POISON', 'S': 'ON_HIT_SLOW',
        'F': 'ON_HIT_FIRE', 'W': 'ON_HIT_WITHER', 'N': 'MINION_POWER', 'K': 'KNOCKBACK', 'V': 'SPEED',
        'HP': 'MAX_HEALTH', 'AS': 'ATTACK_SPEED', 'LK': 'LUCK'}
# Etiqueta del efecto, igual que en las descripciones de S1–S3 que ya existen.
LABEL = {
    'es': {'LIFESTEAL': 'Robo de vida', 'CRIT': 'Golpe crítico (×1,5)',
           'EXECUTE': 'Ejecución (+daño a enemigos bajo el 35 % de vida)', 'THORNS': 'Espinas (devuelve daño cuerpo a cuerpo)',
           'KNOCKBACK': 'Resistencia al empuje', 'DAMAGE_REDUCTION': 'Reducción de daño', 'ATTACK_SPEED': 'Velocidad de ataque',
           'SPEED': 'Velocidad', 'REGEN': 'Regeneración de vida (cada 3 s)', 'ON_HIT_WITHER': 'Probabilidad de marchitar',
           'MANA_REGEN': 'Regeneración de maná (por segundo)', 'RESOURCE_GAIN': 'Carga del recurso de rol',
           'ON_HIT_SLOW': 'Probabilidad de ralentizar', 'DODGE': 'Esquiva', 'ON_HIT_POISON': 'Probabilidad de envenenar',
           'MAX_HEALTH': 'Vida máxima', 'LUCK': 'Suerte', 'ON_HIT_FIRE': 'Probabilidad de quemar',
           'MINION_POWER': 'Poder de los esbirros'},
    'en': {'LIFESTEAL': 'Lifesteal', 'CRIT': 'Critical hit (×1.5)', 'EXECUTE': 'Execute (+damage vs enemies under 35% health)',
           'THORNS': 'Thorns (reflects melee damage)', 'KNOCKBACK': 'Knockback resistance', 'DAMAGE_REDUCTION': 'Damage reduction',
           'ATTACK_SPEED': 'Attack speed', 'SPEED': 'Speed', 'REGEN': 'Health regeneration (every 3 s)',
           'ON_HIT_WITHER': 'Chance to wither', 'MANA_REGEN': 'Mana regeneration (per second)',
           'RESOURCE_GAIN': 'Role resource gain', 'ON_HIT_SLOW': 'Chance to slow', 'DODGE': 'Dodge',
           'ON_HIT_POISON': 'Chance to poison', 'MAX_HEALTH': 'Max health', 'LUCK': 'Luck', 'ON_HIT_FIRE': 'Chance to burn',
           'MINION_POWER': 'Minion power'},
}
EXCLUSIVE = {'es': 'Las sendas son exclusivas: solo puedes seguir una por rol.',
             'en': 'Paths are exclusive: you can follow only one per role.'}
OLD_SENTENCE = {'es': 'Solo puedes seguir 2 de las 3 sendas del rol.',
                'en': "You can follow only 2 of the role's 3 paths."}
FORK = {'es': 'Bifurcación: solo puedes aprender una de las dos ramas.',
        'en': 'Fork: you can learn only one of the two branches.'}
BOOSTED = {'es': 'Potencia el efecto de S4 (×1,5).', 'en': 'Empowers the S4 effect (×1.5).'}

# (rol, senda 1-3, nombres es S4/S5x/S5y/S6, frase es, nombres en, frase en)
ROWS = [
    ('berserker', 1, ('Sed Carmesí', 'Filo Voraz', 'Remate Brutal', 'Hambre Roja'), 'Se fortalece al herir y rematar', ('Crimson Thirst', 'Ravenous Edge', 'Brutal Finish', 'Red Hunger'), 'Grows stronger by wounding and finishing foes'),
    ('berserker', 2, ('Piel de Hierro', 'Muro Espinoso', 'Paso Inamovible', 'Fortaleza Brutal'), 'Convierte el daño recibido en contraataque', ('Iron Skin', 'Thorned Wall', 'Unmovable Stride', 'Brutal Fortitude'), 'Turns the damage taken into a counterattack'),
    ('berserker', 3, ('Ritmo Salvaje', 'Carrera Sangrienta', 'Pulso Vital', 'Torbellino Salvaje'), 'Presión ofensiva constante', ('Savage Rhythm', 'Bloody Rush', 'Vital Pulse', 'Savage Whirlwind'), 'Relentless offensive pressure'),
    ('mage', 1, ('Ruptura Arcana', 'Decreto Mortal', 'Sello Marchito', 'Colapso Arcano'), 'Daño arcano concentrado', ('Arcane Rupture', 'Deadly Decree', 'Withered Seal', 'Arcane Collapse'), 'Concentrated arcane damage'),
    ('mage', 2, ('Fuente Interior', 'Flujo Infinito', 'Manto Arcano', 'Manantial Místico'), 'Combate prolongado con reservas', ('Inner Spring', 'Endless Flow', 'Arcane Mantle', 'Mystic Wellspring'), 'Long fights fuelled by deep reserves'),
    ('mage', 3, ('Cadena Gélida', 'Velo Espectral', 'Reserva Oculta', 'Prisión Temporal'), 'Controla el ritmo y evita golpes', ('Frozen Chain', 'Spectral Veil', 'Hidden Reserve', 'Time Prison'), 'Controls the pace and avoids blows'),
    ('assassin', 1, ('Marca Mortal', 'Corte Perfecto', 'Toxina Letal', 'Sentencia Final'), 'Golpes precisos y letales', ('Death Mark', 'Perfect Cut', 'Lethal Toxin', 'Final Sentence'), 'Precise, lethal strikes'),
    ('assassin', 2, ('Paso Umbrío', 'Viento Oscuro', 'Hoja Entorpecedora', 'Desplazamiento Sombrío'), 'Velocidad y debilitamiento', ('Umbral Step', 'Dark Wind', 'Hindering Blade', 'Shadow Shift'), 'Speed and weakening'),
    ('assassin', 3, ('Herida Profunda', 'Sed del Vacío', 'Danza Mortal', 'Herida Abismal'), 'Desgaste y recuperación', ('Deep Wound', 'Void Thirst', 'Deadly Dance', 'Abyssal Wound'), 'Attrition and recovery'),
    ('herbalist', 1, ('Savia Vital', 'Corteza Antigua', 'Piel Vegetal', 'Savia Eterna'), 'Recuperación y fortaleza natural', ('Vital Sap', 'Ancient Bark', 'Leaf Skin', 'Everlasting Sap'), 'Recovery and natural fortitude'),
    ('herbalist', 2, ('Extracto Letal', 'Polen Paralizante', 'Destilado Mortal', 'Veneno Concentrado'), 'Venenos y debilitamiento', ('Lethal Extract', 'Numbing Pollen', 'Deadly Distillate', 'Concentrated Venom'), 'Poisons and weakening'),
    ('herbalist', 3, ('Flujo Alquímico', 'Reserva Espiritual', 'Recolección Afortunada', 'Flujo Refinado'), 'Recursos y fortuna', ('Alchemical Flow', 'Spirit Reserve', 'Lucky Harvest', 'Refined Flow'), 'Resources and fortune'),
    ('engineer', 1, ('Calibración Letal', 'Protocolo de Ejecución', 'Núcleo de Impacto', 'Mira Perfeccionada'), 'Precisión y daño concentrado', ('Lethal Calibration', 'Execution Protocol', 'Impact Core', 'Perfected Sight'), 'Precision and focused damage'),
    ('engineer', 2, ('Blindaje Reforzado', 'Anclaje Magnético', 'Chasis Reforzado', 'Blindaje Integral'), 'Blindaje y estabilidad', ('Reinforced Plating', 'Magnetic Anchor', 'Reinforced Chassis', 'Full Plating'), 'Plating and stability'),
    ('engineer', 3, ('Engranaje Rápido', 'Propulsión', 'Reactor Interno', 'Engranaje Supremo'), 'Velocidad operativa y energía', ('Quick Gears', 'Propulsion', 'Inner Reactor', 'Supreme Gears'), 'Operating speed and energy'),
    ('lancer', 1, ('Punta Mortal', 'Carga Impetuosa', 'Punta Entorpecedora', 'Estocada Perfecta'), 'Movilidad y ataques precisos', ('Deadly Tip', 'Impetuous Charge', 'Hindering Tip', 'Perfect Thrust'), 'Mobility and precise attacks'),
    ('lancer', 2, ('Púa Defensiva', 'Postura Firme', 'Armadura de Guerra', 'Púa de Acero'), 'Resiste y castiga al que se acerca', ('Defensive Spike', 'Firm Stance', 'War Armour', 'Steel Spike'), 'Holds firm and punishes whoever comes close'),
    ('lancer', 3, ('Sed del Guerrero', 'Coraza Vital', 'Estocada Final', 'Sed del Empalador'), 'Recupera vida y remata', ("Warrior's Thirst", 'Vital Cuirass', 'Final Thrust', "Impaler's Thirst"), 'Recovers health and finishes foes'),
    ('swordsman', 1, ('Corte Magistral', 'Danza de Acero', 'Paso del Duelista', 'Corte Supremo'), 'Duelos, críticos y golpes seguidos', ('Masterful Cut', 'Steel Dance', "Duelist's Step", 'Supreme Cut'), 'Duels, criticals and chained blows'),
    ('swordsman', 2, ('Hambre de Acero', 'Ejecución Marcial', 'Hoja Entorpecedora', 'Hambre de Sangre'), 'Presión, recuperación y remates', ('Steel Hunger', 'Martial Execution', 'Hindering Blade', 'Blood Hunger'), 'Pressure, recovery and finishers'),
    ('swordsman', 3, ('Guardia de Acero', 'Contraataque Espinoso', 'Vitalidad Marcial', 'Guardia Absoluta'), 'Aguanta el desgaste', ('Steel Guard', 'Thorned Riposte', 'Martial Vitality', 'Absolute Guard'), 'Endures the grind'),
    ('paladin', 1, ('Sentencia Divina', 'Filo Radiante', 'Llama Purificadora', 'Veredicto Celestial'), 'Golpes sagrados devastadores', ('Divine Sentence', 'Radiant Edge', 'Purifying Flame', 'Celestial Verdict'), 'Devastating holy strikes'),
    ('paladin', 2, ('Escudo Consagrado', 'Espinas Sagradas', 'Gracia Restauradora', 'Escudo Divino'), 'Defiende al grupo y aguanta', ('Hallowed Shield', 'Sacred Thorns', 'Restoring Grace', 'Divine Shield'), 'Shields the party and holds the line'),
    ('paladin', 3, ('Golpe Radiante', 'Cadencia Sagrada', 'Fe Inquebrantable', 'Golpe Celestial'), 'Ataques veloces y apoyo', ('Radiant Strike', 'Sacred Cadence', 'Unbreakable Faith', 'Celestial Strike'), 'Swift attacks and support'),
    ('necromancer', 1, ('Legión Marchita', 'Reserva Funeraria', 'Maná Sepulcral', 'Legión Ascendente'), 'Los esbirros son su fuerza', ('Withered Legion', 'Funeral Reserve', 'Sepulchral Mana', 'Rising Legion'), 'Minions are their strength'),
    ('necromancer', 2, ('Drenaje Funesto', 'Peste Necrótica', 'Carne Imperecedera', 'Drenaje Abismal'), 'Absorbe vitalidad', ('Baleful Drain', 'Necrotic Blight', 'Undying Flesh', 'Abyssal Drain'), 'Drains vitality'),
    ('necromancer', 3, ('Peste Venenosa', 'Marcha Entorpecedora', 'Decreto de Muerte', 'Peste Eterna'), 'Propaga enfermedades', ('Venom Blight', 'Hindering March', 'Decree of Death', 'Eternal Blight'), 'Spreads disease'),
    ('druid', 1, ('Raíces Vigorosas', 'Corteza Protectora', 'Savia Renovadora', 'Corazón de Roble'), 'Fortaleza y regeneración', ('Vigorous Roots', 'Sheltering Bark', 'Renewing Sap', 'Oak Heart'), 'Fortitude and regeneration'),
    ('druid', 2, ('Instinto Depredador', 'Carrera Feral', 'Colmillo Tóxico', 'Instinto Primordial'), 'Persigue presas con críticos', ('Predator Instinct', 'Feral Rush', 'Toxic Fang', 'Primal Instinct'), 'Hunts prey with critical hits'),
    ('druid', 3, ('Zarzas Defensivas', 'Raíces Inamovibles', 'Polen Entorpecedor', 'Zarzas Letales'), 'Controla el espacio', ('Defensive Brambles', 'Unmovable Roots', 'Hindering Pollen', 'Lethal Brambles'), 'Controls the space around'),
    ('merchant', 1, ('Capital Arcano', 'Reserva Comercial', 'Ojo de Mercader', 'Capital Exponencial'), 'Optimiza recursos', ('Arcane Capital', 'Trade Reserve', "Merchant's Eye", 'Compound Capital'), 'Makes the most of resources'),
    ('merchant', 2, ('Apuesta Mortal', 'Sangre de Riesgo', 'Cobro Final', 'Apuesta Maestra'), 'Críticos y remates oportunistas', ('Deadly Wager', 'Risky Blood', 'Final Collection', 'Master Wager'), 'Opportunistic criticals and finishers'),
    ('merchant', 3, ('Paso Clandestino', 'Fuga Veloz', 'Piel de Negociador', 'Paso Invisible'), 'Evasión y movilidad', ('Covert Step', 'Swift Getaway', "Dealer's Hide", 'Unseen Step'), 'Evasion and mobility'),
    ('monk', 1, ('Golpe Trascendente', 'Cadencia Interior', 'Paso Ligero', 'Puño Absoluto'), 'Críticos y velocidad', ('Transcendent Strike', 'Inner Cadence', 'Light Step', 'Absolute Fist'), 'Criticals and speed'),
    ('monk', 2, ('Respiración Profunda', 'Reflejo Sereno', 'Concentración Interior', 'Respiración Eterna'), 'Concentración y combate largo', ('Deep Breath', 'Serene Reflex', 'Inner Focus', 'Eternal Breath'), 'Focus and long fights'),
    ('monk', 3, ('Drenaje Marcial', 'Impacto Sísmico', 'Golpe Final', 'Drenaje Absoluto'), 'Absorción, impacto y remates', ('Martial Drain', 'Seismic Impact', 'Final Blow', 'Absolute Drain'), 'Draining, impact and finishers'),
    ('elementalist', 1, ('Núcleo Ígneo', 'Colapso Elemental', 'Sentencia Abrasadora', 'Llama Primordial'), 'Devastación con fuego', ('Igneous Core', 'Elemental Collapse', 'Searing Sentence', 'Primal Flame'), 'Devastation by fire'),
    ('elementalist', 2, ('Corriente Arcana', 'Sobrecarga Elemental', 'Manto Energético', 'Corriente Primordial'), 'Sostiene el poder elemental', ('Arcane Current', 'Elemental Surge', 'Energy Mantle', 'Primal Current'), 'Sustains elemental power'),
    ('elementalist', 3, ('Toque Glacial', 'Paso Boreal', 'Viento Elemental', 'Toque Glacial Supremo'), 'Control y distancia', ('Glacial Touch', 'Boreal Step', 'Elemental Wind', 'Supreme Glacial Touch'), 'Control and distance'),
    ('gunslinger', 1, ('Mira Letal', 'Proyectil Final', 'Cadencia de Precisión', 'Mira Absoluta'), 'Disparos precisos', ('Lethal Sight', 'Final Shot', 'Precision Cadence', 'Absolute Sight'), 'Precise shots'),
    ('gunslinger', 2, ('Cámara Energética', 'Reactor de Munición', 'Desplazamiento Táctico', 'Cámara Sobrecargada'), 'Energía y presión', ('Energy Chamber', 'Ammo Reactor', 'Tactical Shift', 'Overcharged Chamber'), 'Energy and pressure'),
    ('gunslinger', 3, ('Disparo Drenante', 'Carrera Balística', 'Proyectil Entorpecedor', 'Disparo Voraz'), 'Movilidad y desgaste', ('Draining Shot', 'Ballistic Rush', 'Hindering Shot', 'Ravenous Shot'), 'Mobility and attrition'),
    ('weapon_summoner', 1, ('Filo Manifestado', 'Impacto Espectral', 'Drenaje del Arsenal', 'Arsenal Desatado'), 'Presión con armas invocadas', ('Manifest Edge', 'Spectral Impact', 'Arsenal Drain', 'Unleashed Arsenal'), 'Pressure with conjured weapons'),
    ('weapon_summoner', 2, ('Armadura Manifestada', 'Reflejo de Acero', 'Núcleo Defensivo', 'Armadura Perpetua'), 'Armamento protector', ('Manifest Armour', 'Steel Reflection', 'Defensive Core', 'Perpetual Armour'), 'Protective armaments'),
    ('weapon_summoner', 3, ('Flujo de Armas', 'Reserva Manifestada', 'Ejecutor Espectral', 'Flujo de Armas Supremo'), 'Recursos y remates', ('Weapon Flow', 'Manifest Reserve', 'Spectral Executioner', 'Supreme Weapon Flow'), 'Resources and finishers'),
    ('hunter', 1, ('Instinto Letal', 'Disparo Certero', 'Trampa Venenosa', 'Instinto de Acecho'), 'Debilidades y ejecución', ('Lethal Instinct', 'True Shot', 'Venom Trap', 'Stalking Instinct'), 'Weak spots and execution'),
    ('hunter', 2, ('Paso Silencioso', 'Carrera del Viento', 'Flecha Entorpecedora', 'Paso del Rastreador'), 'Movilidad y evasión', ('Silent Step', 'Wind Run', 'Hindering Arrow', "Tracker's Step"), 'Mobility and evasion'),
    ('hunter', 3, ('Instinto Sanguinario', 'Constitución Salvaje', 'Impacto de Caza', 'Instinto de Supervivencia'), 'Vitalidad e impacto', ('Bloodthirsty Instinct', 'Wild Constitution', 'Hunting Impact', 'Survival Instinct'), 'Vitality and impact'),
    ('bard', 1, ('Resonancia Vital', 'Melodía Arcana', 'Nota Afortunada', 'Resonancia Superior'), 'Energía y fortuna', ('Vital Resonance', 'Arcane Melody', 'Lucky Note', 'Higher Resonance'), 'Energy and fortune'),
    ('bard', 2, ('Acorde Crítico', 'Ritmo Acelerado', 'Nota Ígnea', 'Acorde Perfecto'), 'Cadencia y ataques ardientes', ('Critical Chord', 'Quickened Rhythm', 'Fiery Note', 'Perfect Chord'), 'Cadence and burning attacks'),
    ('bard', 3, ('Melodía Restauradora', 'Himno Protector', 'Voz Vital', 'Melodía Eterna'), 'Sostiene al grupo', ('Restoring Melody', 'Guardian Hymn', 'Vital Voice', 'Eternal Melody'), 'Sustains the party'),
    ('warlock', 1, ('Marca Marchita', 'Toxina Oscura', 'Veredicto Sombrío', 'Marca Abismal'), 'Maldiciones y ejecución', ('Withering Mark', 'Dark Toxin', 'Grim Verdict', 'Abyssal Mark'), 'Curses and execution'),
    ('warlock', 2, ('Drenaje Oscuro', 'Carne Impía', 'Renovación Umbría', 'Drenaje Profano'), 'La vitalidad como arma', ('Dark Drain', 'Unholy Flesh', 'Umbral Renewal', 'Profane Drain'), 'Vitality as a weapon'),
    ('warlock', 3, ('Golpe del Abismo', 'Reserva Abismal', 'Canalización Oscura', 'Golpe del Vacío'), 'Concentra recursos', ('Abyss Strike', 'Abyssal Reserve', 'Dark Channeling', 'Void Strike'), 'Concentrates resources'),
    ('tamer', 1, ('Piel de Bestia', 'Constitución Feral', 'Rugido de Guerra', 'Piel Primordial'), 'Primera línea con sus bestias', ('Beast Hide', 'Feral Constitution', 'War Roar', 'Primal Hide'), 'Front line alongside their beasts'),
    ('tamer', 2, ('Instinto Alfa', 'Ataque Coordinado', 'Colmillo Voraz', 'Instinto Alfa Supremo'), 'Ataques coordinados', ('Alpha Instinct', 'Coordinated Attack', 'Ravenous Fang', 'Supreme Alpha Instinct'), 'Coordinated attacks'),
    ('tamer', 3, ('Vínculo Vital', 'Instinto Afortunado', 'Reserva Animal', 'Vínculo Eterno'), 'Supervivencia y recursos', ('Vital Bond', 'Lucky Instinct', 'Animal Reserve', 'Eternal Bond'), 'Survival and resources'),
    ('shapeshifter', 1, ('Instinto Letal', 'Hambre Salvaje', 'Carrera Feral', 'Instinto Supremo'), 'Persigue y elimina', ('Lethal Instinct', 'Savage Hunger', 'Feral Rush', 'Supreme Instinct'), 'Hunts down and eliminates'),
    ('shapeshifter', 2, ('Reflejos Salvajes', 'Instinto Adaptativo', 'Flujo Metamórfico', 'Reflejos Primordiales'), 'Movilidad y energía', ('Wild Reflexes', 'Adaptive Instinct', 'Metamorphic Flow', 'Primal Reflexes'), 'Mobility and energy'),
    ('shapeshifter', 3, ('Constitución Monstruosa', 'Piel Acorazada', 'Espinas Naturales', 'Constitución Primordial'), 'Absorbe impactos', ('Monstrous Constitution', 'Armoured Hide', 'Natural Thorns', 'Primal Constitution'), 'Absorbs impacts'),
]

# v46.1: piedras angulares (S7), en el mismo orden que ROWS: (nombre es, nombre en).
KEYSTONES = [
    ('Furia Insaciable', 'Insatiable Fury'), ('Último Bastión', 'Last Bastion'), ('Tormenta de Hachas', 'Axe Storm'),
    ('Tormenta Arcana', 'Arcane Storm'), ('Flujo Perpetuo', 'Perpetual Flow'), ('Reflejo Arcano', 'Arcane Reflex'),
    ('Muerte Silenciosa', 'Silent Death'), ('Ruptura Sombría', 'Shadow Rupture'), ('Peste Silenciosa', 'Silent Blight'),
    ('Jardín Vital', 'Vital Garden'), ('Floración Tóxica', 'Toxic Bloom'), ('Cosecha Dorada', 'Golden Harvest'),
    ('Núcleo Explosivo', 'Explosive Core'), ('Fortaleza Industrial', 'Industrial Fortress'), ('Reactor Central', 'Central Reactor'),
    ('Lanza Relámpago', 'Lightning Lance'), ('Formación Imbatible', 'Unbeatable Formation'), ('Cosecha de Guerra', 'War Harvest'),
    ('Réplica Perfecta', 'Perfect Riposte'), ('Danza Carmesí', 'Crimson Dance'), ('Juramento de Acero', 'Steel Oath'),
    ('Juicio Radiante', 'Radiant Judgement'), ('Fortaleza Consagrada', 'Hallowed Fortress'), ('Gracia Compartida', 'Shared Grace'),
    ('Ejército Sepulcral', 'Sepulchral Host'), ('Maldición Funeraria', 'Funeral Curse'), ('Sepulcro Profanado', 'Defiled Tomb'),
    ('Bosque Protector', 'Sheltering Grove'), ('Caza Salvaje', 'Wild Hunt'), ('Zarzal Invasor', 'Creeping Thicket'),
    ('Mercado Dorado', 'Golden Market'), ('Quiebra Explosiva', 'Explosive Bankruptcy'), ('Escape Calculado', 'Calculated Escape'),
    ('Puños del Trueno', 'Thunder Fists'), ('Serenidad Compartida', 'Shared Serenity'), ('Camino del Destructor', "Destroyer's Way"),
    ('Cataclismo Ígneo', 'Igneous Cataclysm'), ('Corazón Elemental', 'Elemental Heart'), ('Tormenta Cristalina', 'Crystal Storm'),
    ('Munición Fulminante', 'Thunderbolt Rounds'), ('Arsenal Inagotable', 'Endless Arsenal'), ('Bala Fantasma', 'Phantom Bullet'),
    ('Tormenta de Filos', 'Storm of Blades'), ('Forja Imperturbable', 'Unshakable Forge'), ('Núcleo de Invocación', 'Summoning Core'),
    ('Marca del Rastreador', "Tracker's Mark"), ('Botín del Explorador', "Explorer's Spoils"), ('Trofeo Supremo', 'Supreme Trophy'),
    ('Sinfonía Vital', 'Vital Symphony'), ('Sinfonía del Trueno', 'Thunder Symphony'), ('Última Estrofa', 'Final Verse'),
    ('Maldición Infinita', 'Endless Curse'), ('Festín Sombrío', 'Grim Feast'), ('Abismo Inagotable', 'Bottomless Abyss'),
    ('Manada Suprema', 'Supreme Pack'), ('Cacería Dominante', 'Dominant Hunt'), ('Refugio de la Manada', 'Pack Refuge'),
    ('Reflejo Depredador', 'Predator Reflex'), ('Metamorfosis Afortunada', 'Lucky Metamorphosis'), ('Forma Imparable', 'Unstoppable Form'),
]
# Plantilla de cada senda: se lee de src/main/java/dev/aptitudes/role/KeystoneRules.java (entry(...)).
KS_JAVA = os.path.join(ROOT, 'src/main/java/dev/aptitudes/role/KeystoneRules.java')
# Nombre y efecto de cada plantilla con los valores por defecto de la configuración (keystone*).
TEMPLATE = {
    'es': {
        'carnage': ('Carnicería', 'Al matar: recuperas el 10 % de tu vida máxima y ganas Prisa II durante 5 s.'),
        'bulwark': ('Baluarte', 'Al bajar del 30 % de vida: 8 de absorción y Resistencia I durante 6 s (espera de 60 s).'),
        'overload': ('Sobrecarga', 'Cada 4.ª habilidad no gasta maná y su espera se reduce a la mitad.'),
        'detonation': ('Detonación', 'Al matar: explosión de 4 de daño a 3 bloques (no rompe bloques ni daña aliados).'),
        'contagion': ('Contagio', 'Al matar: el veneno, la lentitud, el marchitamiento y el fuego del objetivo pasan a 2 enemigos cercanos.'),
        'legion': ('Legión', '+1 esbirro máximo; tus esbirros te curan con el 10 % del daño que hacen.'),
        'specter': ('Espectro', '+5 % de esquiva. Tras esquivar, tu siguiente golpe en 3 s es un crítico seguro.'),
        'fortune': ('Fortuna', '25 % de una tirada de botín extra al matar criaturas y 10 % de esmeralda al matar élites.'),
        'tempest': ('Tempestad', 'Tus críticos (y el 15 % de tus golpes) lanzan una chispa de 3 de daño a otro enemigo cercano (espera de 1 s).'),
        'sanctuary': ('Santuario', 'Cada 5 s curas 2 de vida a ti y a tus aliados del grupo a 8 bloques.'),
    },
    'en': {
        'carnage': ('Carnage', 'On kill: heal 10% of your max health and gain Haste II for 5 s.'),
        'bulwark': ('Bulwark', 'Dropping below 30% health: 8 absorption and Resistance I for 6 s (60 s cooldown).'),
        'overload': ('Overload', 'Every 4th ability costs no mana and its cooldown is halved.'),
        'detonation': ('Detonation', 'On kill: 4-damage explosion within 3 blocks (no block damage, never hits allies).'),
        'contagion': ('Contagion', "On kill: the target's poison, slowness, wither and fire spread to 2 nearby enemies."),
        'legion': ('Legion', '+1 max minion; your minions heal you for 10% of the damage they deal.'),
        'specter': ('Specter', '+5% dodge. After a dodge, your next hit within 3 s is a guaranteed critical.'),
        'fortune': ('Fortune', '25% chance of an extra loot roll on kill and 10% chance of an emerald from elites.'),
        'tempest': ('Tempest', 'Your criticals (and 15% of your hits) throw a 3-damage spark at another nearby enemy (1 s cooldown).'),
        'sanctuary': ('Sanctuary', 'Every 5 s you heal yourself and party allies within 8 blocks for 2 health.'),
    },
}
KS_NOTE = {'es': 'Piedra angular (nivel 50, 1 rango). Los valores son los de la configuración por defecto.',
           'en': 'Keystone (level 50, 1 rank). Values are the default configuration ones.'}
KS_CAMEL = {'CARNAGE': 'carnage', 'BULWARK': 'bulwark', 'OVERLOAD': 'overload', 'DETONATION': 'detonation',
            'CONTAGION': 'contagion', 'LEGION': 'legion', 'SPECTER': 'specter', 'FORTUNE': 'fortune',
            'TEMPEST': 'tempest', 'SANCTUARY': 'sanctuary'}
SUBTITLES = {
    'es': {'carnage': 'Carnicería: latido', 'bulwark': 'Baluarte: escudo', 'overload': 'Sobrecarga: descarga',
           'detonation': 'Detonación: estallido', 'contagion': 'Contagio: burbujeo', 'legion': 'Legión: coro',
           'specter': 'Espectro: susurro', 'fortune': 'Fortuna: monedas', 'tempest': 'Tempestad: chispa',
           'sanctuary': 'Santuario: cura'},
    'en': {'carnage': 'Carnage: heartbeat', 'bulwark': 'Bulwark: shield', 'overload': 'Overload: discharge',
           'detonation': 'Detonation: blast', 'contagion': 'Contagion: bubbling', 'legion': 'Legion: choir',
           'specter': 'Specter: whisper', 'fortune': 'Fortune: coins', 'tempest': 'Tempest: spark',
           'sanctuary': 'Sanctuary: healing'},
}

UI = {
    'es_es': {
        'aptitudes.role.upgrade.path_locked': 'Ya sigues otra senda en este rol (las sendas son exclusivas)',
        'aptitudes.role.upgrade.branch_locked': 'Ya elegiste la otra rama de esta bifurcación',
        'aptitudes.paths.migrated': 'Sendas exclusivas: en %s conservas la senda %s y recuperas %s puntos de las demás.',
        'screen.aptitudes.paths.button': 'Sendas',
        'screen.aptitudes.paths.title': '%s · Sendas',
        'screen.aptitudes.paths.level': 'Nivel de rol %s/%s',
        'screen.aptitudes.paths.points': 'Puntos libres: %s',
        'screen.aptitudes.paths.free': 'Elige una senda: es exclusiva (bloquea las otras dos)',
        'screen.aptitudes.paths.chosen': 'Tu senda',
        'screen.aptitudes.paths.locked': 'Bloqueada: ya sigues %s',
        'screen.aptitudes.paths.branch': 'Bifurcación: elige una',
        'screen.aptitudes.paths.keystone': 'Piedra angular (nivel 50)',
        'screen.aptitudes.paths.learn': 'Aprender',
        'screen.aptitudes.paths.back': 'Árbol',
        'screen.aptitudes.paths.confirm_title': '¿Elegir esta opción?',
        'screen.aptitudes.paths.confirm_path': 'Al aprender %s eliges la senda %s y se bloquean las otras dos de este rol. Solo podrás cambiarla reasignando el árbol.',
        'screen.aptitudes.paths.confirm_branch': 'Al aprender %s se bloquea la otra rama de la bifurcación. Solo podrás cambiarla reasignando el árbol.',
    },
    'en_us': {
        'aptitudes.role.upgrade.path_locked': 'You already follow another path in this role (paths are exclusive)',
        'aptitudes.role.upgrade.branch_locked': 'You already chose the other branch of this fork',
        'aptitudes.paths.migrated': 'Exclusive paths: in %s you keep the %s path and get back %s points from the others.',
        'screen.aptitudes.paths.button': 'Paths',
        'screen.aptitudes.paths.title': '%s · Paths',
        'screen.aptitudes.paths.level': 'Role level %s/%s',
        'screen.aptitudes.paths.points': 'Free points: %s',
        'screen.aptitudes.paths.free': 'Choose a path: it is exclusive (locks the other two)',
        'screen.aptitudes.paths.chosen': 'Your path',
        'screen.aptitudes.paths.locked': 'Locked: you already follow %s',
        'screen.aptitudes.paths.branch': 'Fork: choose one',
        'screen.aptitudes.paths.keystone': 'Keystone (level 50)',
        'screen.aptitudes.paths.learn': 'Learn',
        'screen.aptitudes.paths.back': 'Tree',
        'screen.aptitudes.paths.confirm_title': 'Choose this option?',
        'screen.aptitudes.paths.confirm_path': 'Learning %s picks the %s path and locks the other two of this role. You can only change it by resetting the tree.',
        'screen.aptitudes.paths.confirm_branch': 'Learning %s locks the other branch of the fork. You can only change it by resetting the tree.',
    },
}
GUIDE = {
    'es_es': ('de ella salen 3 SENDAS y solo puedes seguir 2.',
              'de ella salen 3 SENDAS y solo puedes seguir UNA (son exclusivas). Desde la v46 cada senda llega al nivel 40 '
              'con una bifurcación en el 32 (eliges una rama); ábrelas con el botón «Sendas».'),
    'en_us': ('3 PATHS branch from it and you may follow only 2.',
              '3 PATHS branch from it and you may follow only ONE (they are exclusive). Since v46 every path reaches level '
              '40 with a fork at 32 (you pick one branch); open them with the «Paths» button.'),
}
LETTERS = 'abc'


def java_effects():
    """{rol: [[S1,S2,S3,S4,S5x,S5y,S6] por senda]} leído de RoleTree.java."""
    src = open(JAVA, encoding='utf-8').read()
    pat = r'%s\(Role\.(\w+), of\(([^)]*)\), of\(([^)]*)\), of\(([^)]*)\)\)'
    base = {m.group(1).lower(): [[CODE[x.strip()] for x in g.split(',')] for g in m.groups()[1:]]
            for m in re.finditer(pat % 'paths', src)}
    deep = {m.group(1).lower(): [[CODE[x.strip()] for x in g.split(',')] for g in m.groups()[1:]]
            for m in re.finditer(pat % 'deeper', src)}
    assert len(base) == 20 and set(base) == set(deep), 'RoleTree: faltan sendas'
    return {r: [base[r][p] + deep[r][p] + [deep[r][p][0]] for p in range(3)] for r in base}


def num(v):
    return ('%.2f' % v).rstrip('0').rstrip('.')


def amount(effect, value, lang):
    per, pct = EFFECTS[effect]
    if pct:
        return '+' + num(value * 100) + (' %' if lang == 'es' else '%')
    return '+' + num(value)


def desc(effect, mult, lang, extra):
    per, _ = EFFECTS[effect]
    v = per * mult
    word = ('por rango', 'máx.') if lang == 'es' else ('per rank', 'max')
    text = f"{LABEL[lang][effect]}: {amount(effect, v, lang)} {word[0]} ({word[1]} {amount(effect, v * 3, lang)})."
    return ' '.join([text] + extra + [EXCLUSIVE[lang]])


def keystone_templates():
    """{rol: [plantilla A, B, C]} leído de KeystoneRules.java."""
    src = open(KS_JAVA, encoding='utf-8').read()
    out = {m.group(1): [KS_CAMEL[x] for x in m.groups()[1:]]
           for m in re.finditer(r'entry\("(\w+)", Template\.(\w+), Template\.(\w+), Template\.(\w+)\)', src)}
    assert len(out) == 20, 'KeystoneRules: faltan roles'
    return out


def build(lang, data):
    code = 'es' if lang == 'es_es' else 'en'
    effects = java_effects()
    templates = keystone_templates()
    assert len(KEYSTONES) == len(ROWS)
    out = {}
    for t, (name, desc_) in TEMPLATE[code].items():
        out[f'keystone.aptitudes.{t}'] = name
        out[f'keystone.aptitudes.{t}.desc'] = desc_
        out[f'subtitles.aptitudes.keystone_{t}'] = SUBTITLES[code][t]
    for index, (role, path, es_names, es_flavor, en_names, en_flavor) in enumerate(ROWS):
        tpl = templates[role][path - 1]
        ks_name = KEYSTONES[index][0 if code == 'es' else 1]
        key = f'node.aptitudes.{role}_{LETTERS[path - 1]}7'
        out[key] = ks_name
        out[key + '.desc'] = f'{TEMPLATE[code][tpl][0]}: {TEMPLATE[code][tpl][1]} {KS_NOTE[code]}'

        names = es_names if code == 'es' else en_names
        effs = effects[role][path - 1]
        base = f'{role}_{LETTERS[path - 1]}'
        out[f'path.aptitudes.{base}.desc'] = es_flavor if code == 'es' else en_flavor
        # S1–S3: solo cambia la frase de la exclusividad.
        for i in range(3):
            key = f'node.aptitudes.{base}{i + 1}.desc'
            if key in data:
                out[key] = data[key].replace(OLD_SENTENCE[code], EXCLUSIVE[code])
        for i, suffix in enumerate(['4', '5x', '5y', '6']):
            key = f'node.aptitudes.{base}{suffix}'
            effect = effs[3 + i]
            extra = [FORK[code]] if suffix in ('5x', '5y') else [BOOSTED[code]] if suffix == '6' else []
            out[key] = names[i]
            out[key + '.desc'] = desc(effect, 1.5 if suffix == '6' else 1.0, code, extra)
    out.update(UI[lang])
    return out


def write(lang):
    path = L + lang + '.json'
    data = json.load(open(path, encoding='utf-8'))
    new = build(lang, data)
    guide = 'guide.aptitudes.tree.body'
    old, repl = GUIDE[lang]
    if old in data[guide]:
        data[guide] = data[guide].replace(old, repl)
    data.pop('screen.aptitudes.paths.keystone_soon', None)  # v46.0: aviso provisional, ya no se usa
    # Actualiza en su sitio las claves existentes y añade al final solo las nuevas.
    data.update(new)
    open(path, 'w', encoding='utf-8', newline='\n').write(json.dumps(data, ensure_ascii=False, indent=2) + '\n')
    return len(new)


if __name__ == '__main__':
    n_en = write('en_us')
    n_es = write('es_es')
    src = open(L + 'es_es.json', encoding='utf-8').read()
    for v in ['es_mx', 'es_ar', 'es_cl', 'es_ec', 'es_uy', 'es_ve']:
        open(L + v + '.json', 'w', encoding='utf-8', newline='\n').write(src)
    print(f'{n_en} claves en en_us, {n_es} en es_es (copiado a 6 variantes)')
