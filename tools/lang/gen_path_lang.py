#!/usr/bin/env python3
"""
Genera las Sendas v46 en inglés, español, portugués, francés y alemán.
Español se copia a sus seis variantes. --check comprueba sin escribir.

- Nombres y descripciones de los escalones nuevos S4, S5x, S5y y S6 de las 60 sendas (240 nodos).
- Descripciones de S1–S7 con el estilo llano (v67): qué hace, hablándole al jugador, y la línea
  «Rango 1 / 2 / 3: …» con las cifras de cada rango; sin coletillas.
- Frase de cada senda (path.aptitudes.<rol>_<a|b|c>.desc) y textos de la vista de sendas.

Los EFECTOS se leen de src/main/java/dev/aptitudes/role/RoleTree.java (paths(...) y deeper(...)), así que
nombres y números no se desincronizan del código. Los nombres salen de docs/V46_DISENO_SENDAS.md §10.
No edites estas claves a mano en los JSON: cambia este script y vuelve a ejecutarlo.

Uso: python3 tools/lang/gen_path_lang.py
"""
import os
import re

from check_lang import LOCALES
from ui_catalog import TRANSLATIONS as UI_TRANSLATIONS

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
# Frase llana de cada efecto, hablándole al jugador. Las cifras deben coincidir entre en/pt/fr/de (decimales con punto).
SENTENCE = {
    'es': {
        'LIFESTEAL': 'Te curas con una parte del daño que haces.',
        'CRIT': 'Probabilidad de que tus golpes sean críticos (×1,5 de daño).',
        'EXECUTE': 'Haces más daño a los enemigos con menos del 35 % de vida.',
        'THORNS': 'Quien te golpea cuerpo a cuerpo recibe una parte de ese daño.',
        'DAMAGE_REDUCTION': 'Recibes menos daño (como mucho un 40 % en total).',
        'DODGE': 'Probabilidad de esquivar por completo el ataque de una criatura.',
        'MANA_REGEN': 'Recuperas más maná cada segundo.',
        'RESOURCE_GAIN': 'El recurso de tu rol se carga más rápido.',
        'REGEN': 'Recuperas vida cada 3 s.',
        'ON_HIT_POISON': 'Probabilidad de envenenar durante 3 s al enemigo que golpeas.',
        'ON_HIT_SLOW': 'Probabilidad de ralentizar durante 3 s al enemigo que golpeas.',
        'ON_HIT_FIRE': 'Probabilidad de prender fuego durante 3 s al enemigo que golpeas.',
        'ON_HIT_WITHER': 'Probabilidad de marchitar durante 3 s al enemigo que golpeas.',
        'MINION_POWER': 'Tus esbirros tienen más vida y hacen más daño.',
        'KNOCKBACK': 'Los golpes te empujan menos.',
        'SPEED': 'Te mueves más rápido.',
        'MAX_HEALTH': 'Aumenta tu vida máxima.',
        'ATTACK_SPEED': 'Atacas más rápido.',
        'LUCK': 'Tienes más suerte: mejor botín y mejor pesca.',
    },
    'en': {
        'LIFESTEAL': 'You heal for part of the damage you deal.',
        'CRIT': 'Chance for your hits to be critical (×1.5 damage).',
        'EXECUTE': 'You deal more damage to enemies below 35% health.',
        'THORNS': 'Whoever hits you in melee takes back part of that damage.',
        'DAMAGE_REDUCTION': 'You take less damage (40% at most in total).',
        'DODGE': "Chance to fully dodge a creature's attack.",
        'MANA_REGEN': 'You regain extra mana every second.',
        'RESOURCE_GAIN': 'Your role resource charges faster.',
        'REGEN': 'You regain health every 3 s.',
        'ON_HIT_POISON': 'Chance to poison the enemy you hit for 3 s.',
        'ON_HIT_SLOW': 'Chance to slow the enemy you hit for 3 s.',
        'ON_HIT_FIRE': 'Chance to set the enemy you hit on fire for 3 s.',
        'ON_HIT_WITHER': 'Chance to wither the enemy you hit for 3 s.',
        'MINION_POWER': 'Your minions have more health and deal more damage.',
        'KNOCKBACK': 'Hits push you back less.',
        'SPEED': 'You move faster.',
        'MAX_HEALTH': 'Raises your max health.',
        'ATTACK_SPEED': 'You attack faster.',
        'LUCK': 'You have more luck: better loot and better fishing.',
    },
    'pt_br': {
        'LIFESTEAL': 'Você se cura com parte do dano que causa.',
        'CRIT': 'Chance de seus golpes serem críticos (×1.5 de dano).',
        'EXECUTE': 'Você causa mais dano a inimigos com menos de 35% de vida.',
        'THORNS': 'Quem te ataca corpo a corpo recebe parte desse dano.',
        'DAMAGE_REDUCTION': 'Você recebe menos dano (no máximo 40% no total).',
        'DODGE': 'Chance de esquivar totalmente o ataque de uma criatura.',
        'MANA_REGEN': 'Você recupera mais mana a cada segundo.',
        'RESOURCE_GAIN': 'O recurso da sua classe carrega mais rápido.',
        'REGEN': 'Você recupera vida a cada 3 s.',
        'ON_HIT_POISON': 'Chance de envenenar por 3 s o inimigo que você golpeia.',
        'ON_HIT_SLOW': 'Chance de deixar lento por 3 s o inimigo que você golpeia.',
        'ON_HIT_FIRE': 'Chance de incendiar por 3 s o inimigo que você golpeia.',
        'ON_HIT_WITHER': 'Chance de definhar por 3 s o inimigo que você golpeia.',
        'MINION_POWER': 'Seus servos têm mais vida e causam mais dano.',
        'KNOCKBACK': 'Os golpes te empurram menos.',
        'SPEED': 'Você se move mais rápido.',
        'MAX_HEALTH': 'Aumenta sua vida máxima.',
        'ATTACK_SPEED': 'Você ataca mais rápido.',
        'LUCK': 'Você tem mais sorte: melhor saque e melhor pesca.',
    },
    'fr_fr': {
        'LIFESTEAL': 'Vous vous soignez d’une partie des dégâts infligés.',
        'CRIT': 'Chance que vos coups soient critiques (×1.5 dégâts).',
        'EXECUTE': 'Vous infligez plus de dégâts aux ennemis sous 35% de santé.',
        'THORNS': 'Qui vous frappe au corps à corps subit une partie de ces dégâts.',
        'DAMAGE_REDUCTION': 'Vous subissez moins de dégâts (40% au plus au total).',
        'DODGE': 'Chance d’esquiver entièrement l’attaque d’une créature.',
        'MANA_REGEN': 'Vous récupérez plus de mana chaque seconde.',
        'RESOURCE_GAIN': 'La ressource de votre rôle se charge plus vite.',
        'REGEN': 'Vous récupérez de la santé toutes les 3 s.',
        'ON_HIT_POISON': 'Chance d’empoisonner pendant 3 s l’ennemi que vous frappez.',
        'ON_HIT_SLOW': 'Chance de ralentir pendant 3 s l’ennemi que vous frappez.',
        'ON_HIT_FIRE': 'Chance d’enflammer pendant 3 s l’ennemi que vous frappez.',
        'ON_HIT_WITHER': 'Chance de flétrir pendant 3 s l’ennemi que vous frappez.',
        'MINION_POWER': 'Vos serviteurs ont plus de santé et infligent plus de dégâts.',
        'KNOCKBACK': 'Les coups vous repoussent moins.',
        'SPEED': 'Vous vous déplacez plus vite.',
        'MAX_HEALTH': 'Augmente votre santé maximale.',
        'ATTACK_SPEED': 'Vous attaquez plus vite.',
        'LUCK': 'Vous avez plus de chance : meilleur butin et meilleure pêche.',
    },
    'de_de': {
        'LIFESTEAL': 'Du heilst dich um einen Teil des Schadens, den du verursachst.',
        'CRIT': 'Chance, dass deine Treffer kritisch sind (×1.5 Schaden).',
        'EXECUTE': 'Du verursachst mehr Schaden an Feinden unter 35% Leben.',
        'THORNS': 'Wer dich im Nahkampf trifft, erleidet einen Teil dieses Schadens.',
        'DAMAGE_REDUCTION': 'Du erleidest weniger Schaden (insgesamt höchstens 40%).',
        'DODGE': 'Chance, dem Angriff einer Kreatur ganz auszuweichen.',
        'MANA_REGEN': 'Du erhältst jede Sekunde zusätzliches Mana.',
        'RESOURCE_GAIN': 'Deine Rollenressource lädt schneller auf.',
        'REGEN': 'Du regenerierst alle 3 s Leben.',
        'ON_HIT_POISON': 'Chance, den getroffenen Feind 3 s lang zu vergiften.',
        'ON_HIT_SLOW': 'Chance, den getroffenen Feind 3 s lang zu verlangsamen.',
        'ON_HIT_FIRE': 'Chance, den getroffenen Feind 3 s lang in Brand zu setzen.',
        'ON_HIT_WITHER': 'Chance, den getroffenen Feind 3 s lang verdorren zu lassen.',
        'MINION_POWER': 'Deine Diener haben mehr Leben und verursachen mehr Schaden.',
        'KNOCKBACK': 'Treffer stoßen dich weniger zurück.',
        'SPEED': 'Du bewegst dich schneller.',
        'MAX_HEALTH': 'Erhöht dein maximales Leben.',
        'ATTACK_SPEED': 'Du greifst schneller an.',
        'LUCK': 'Du hast mehr Glück: bessere Beute und besseres Angeln.',
    },
}
# Línea de rangos: «Rango 1 / 2 / 3: +2 % / +4 % / +6 %.»
RANKS = {'es': 'Rango 1 / 2 / 3:', 'en': 'Rank 1 / 2 / 3:', 'pt_br': 'Grau 1 / 2 / 3:',
         'fr_fr': 'Rang 1 / 2 / 3 :', 'de_de': 'Rang 1 / 2 / 3:'}

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
        'carnage': ('Carnicería', 'Cada vez que matas a un enemigo, recuperas el 10 % de tu vida máxima y ganas Prisa II durante 5 s.'),
        'bulwark': ('Baluarte', 'Si tu vida baja del 30 %, ganas 8 de absorción y Resistencia I durante 6 s. Se recarga en 60 s.'),
        'overload': ('Sobrecarga', 'Cada 4.ª habilidad que lanzas no gasta maná y su espera se reduce a la mitad.'),
        'detonation': ('Detonación', 'Cada enemigo que matas explota y hace 4 de daño a 3 bloques a la redonda. No rompe bloques ni daña a tus aliados.'),
        'contagion': ('Contagio', 'Cada vez que matas a un enemigo, su veneno, lentitud, marchitamiento y fuego pasan a 2 enemigos cercanos.'),
        'legion': ('Legión', 'Puedes tener 1 esbirro más, y tus esbirros te curan con el 10 % del daño que hacen.'),
        'specter': ('Espectro', '+5 % de esquiva. Después de esquivar, tu siguiente golpe en menos de 3 s es un crítico seguro.'),
        'fortune': ('Fortuna', 'Al matar criaturas tienes un 25 % de probabilidad de botín extra, y al matar élites, un 10 % de conseguir una esmeralda.'),
        'tempest': ('Tempestad', 'Tus críticos (y el 15 % de tus demás golpes) lanzan una chispa que hace 3 de daño a otro enemigo cercano (espera de 1 s).'),
        'sanctuary': ('Santuario', 'Cada 5 s, tú y tus aliados del grupo a menos de 8 bloques recuperan 2 de vida.'),
    },
    'en': {
        'carnage': ('Carnage', 'Each time you kill an enemy, you heal 10% of your max health and gain Haste II for 5 s.'),
        'bulwark': ('Bulwark', 'If your health drops below 30%, you gain 8 absorption and Resistance I for 6 s. Recharges in 60 s.'),
        'overload': ('Overload', 'Every 4th ability you cast costs no mana and has its cooldown halved.'),
        'detonation': ('Detonation', 'Every enemy you kill explodes, dealing 4 damage within 3 blocks. It never breaks blocks or hurts your allies.'),
        'contagion': ('Contagion', 'Each time you kill an enemy, its poison, slowness, wither and fire spread to 2 nearby enemies.'),
        'legion': ('Legion', 'You can have 1 more minion, and your minions heal you for 10% of the damage they deal.'),
        'specter': ('Specter', '+5% dodge. After you dodge, your next hit within 3 s is a guaranteed critical.'),
        'fortune': ('Fortune', 'Killing creatures has a 25% chance to give extra loot, and killing elites a 10% chance to give an emerald.'),
        'tempest': ('Tempest', 'Your critical hits (and 15% of your other hits) throw a spark that deals 3 damage to another nearby enemy (1 s cooldown).'),
        'sanctuary': ('Sanctuary', 'Every 5 s, you and party allies within 8 blocks heal 2 health.'),
    },
}
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

# Nombre de senda, S4/S5x/S5y/S6/S7 y frase; el orden de las filas originales no cambia.
LOCAL_ROWS = {locale: {} for locale in LOCALES}
for row in """
berserker_a¦Açougueiro/Sede carmesim/Fio voraz/Finalização brutal/Fome vermelha/Fúria insaciável/Fortalece ao ferir e finalizar inimigos¦Boucher/Soif carmin/Tranchant vorace/Coup final brutal/Faim rouge/Furie insatiable/Se renforce en blessant et achevant ses ennemis¦Schlächter/Purpurdurst/Gierige Schneide/Brutaler Abschluss/Roter Hunger/Unstillbare Wut/Wird durch Verwunden und Besiegen stärker
berserker_b¦Colosso/Pele de ferro/Muro espinhoso/Passo inamovível/Fortaleza brutal/Último bastião/Transforma dano recebido em contra-ataque¦Colosse/Peau de fer/Mur épineux/Pas inamovible/Vigueur brutale/Dernier bastion/Transforme les dégâts subis en contre-attaque¦Koloss/Eisenhaut/Dornenwand/Unverrückbarer Schritt/Brutale Standhaftigkeit/Letzte Bastion/Verwandelt erlittenen Schaden in Gegenangriffe
berserker_c¦Redemoinho/Ritmo selvagem/Corrida sangrenta/Pulso vital/Turbilhão selvagem/Tempestade de machados/Pressão ofensiva constante¦Tourbillon/Rythme sauvage/Ruée sanglante/Pulsation vitale/Tourbillon sauvage/Tempête de haches/Pression offensive constante¦Wirbelwind/Wilder Rhythmus/Blutiger Ansturm/Lebenspuls/Wilder Wirbelwind/Axtsturm/Unablässiger Angriffsdruck
mage_a¦Arcanista/Ruptura arcana/Decreto mortal/Selo murcho/Colapso arcano/Tempestade arcana/Dano arcano concentrado¦Arcaniste/Rupture arcanique/Décret mortel/Sceau flétri/Effondrement arcanique/Tempête arcanique/Dégâts arcaniques concentrés¦Arkanist/Arkaner Bruch/Tödlicher Erlass/Verdorrtes Siegel/Arkaner Kollaps/Arkaner Sturm/Gebündelter arkaner Schaden
mage_b¦Erudito/Fonte interior/Fluxo infinito/Manto arcano/Manancial místico/Fluxo perpétuo/Combate prolongado com reservas¦Érudit/Source intérieure/Flux infini/Manteau arcanique/Source mystique/Flux perpétuel/Longs combats grâce aux réserves¦Gelehrter/Innere Quelle/Endloser Fluss/Arkaner Mantel/Mystischer Quell/Ewiger Fluss/Lange Kämpfe mit großen Reserven
mage_c¦Cronomante/Corrente gélida/Véu espectral/Reserva oculta/Prisão temporal/Reflexo arcano/Controla o ritmo e evita golpes¦Chronomancien/Chaîne glacée/Voile spectral/Réserve cachée/Prison temporelle/Réflexe arcanique/Contrôle le rythme et évite les coups¦Chronomant/Frostkette/Geisterschleier/Verborgene Reserve/Zeitgefängnis/Arkaner Reflex/Bestimmt das Tempo und vermeidet Treffer
assassin_a¦Carrasco/Marca mortal/Corte perfeito/Toxina letal/Sentença final/Morte silenciosa/Golpes precisos e letais¦Bourreau/Marque mortelle/Entaille parfaite/Toxine létale/Sentence finale/Mort silencieuse/Frappes précises et mortelles¦Henker/Todesmal/Vollkommener Schnitt/Tödliches Toxin/Letztes Urteil/Lautloser Tod/Präzise tödliche Treffer
assassin_b¦Fantasma/Passo umbrio/Vento sombrio/Lâmina entorpecente/Deslocamento sombrio/Ruptura sombria/Velocidade e enfraquecimento¦Fantôme/Pas ténébreux/Vent obscur/Lame entravante/Déplacement d'ombre/Rupture d'ombre/Vitesse et affaiblissement¦Phantom/Dunkler Schritt/Dunkler Wind/Hemmende Klinge/Schattenwechsel/Schattenbruch/Geschwindigkeit und Schwächung
assassin_c¦Sombra carmesim/Ferida profunda/Sede do vazio/Dança mortal/Ferida abissal/Praga silenciosa/Desgaste e recuperação¦Ombre carmin/Blessure profonde/Soif du vide/Danse mortelle/Blessure abyssale/Fléau silencieux/Usure et récupération¦Purpurschatten/Tiefe Wunde/Leerendurst/Todestanz/Abgrundwunde/Stille Seuche/Zermürbung und Erholung
herbalist_a¦Curandeiro/Seiva vital/Casca antiga/Pele vegetal/Seiva eterna/Jardim vital/Recuperação e fortaleza natural¦Guérisseur/Sève vitale/Écorce ancienne/Peau végétale/Sève éternelle/Jardin vital/Récupération et vigueur naturelle¦Heiler/Lebenssaft/Alte Rinde/Blatthaut/Ewiger Saft/Lebensgarten/Erholung und natürliche Stärke
herbalist_b¦Envenenador/Extrato letal/Pólen paralisante/Destilado mortal/Veneno concentrado/Floração tóxica/Venenos e enfraquecimento¦Empoisonneur/Extrait létal/Pollen paralysant/Distillat mortel/Venin concentré/Floraison toxique/Poisons et affaiblissement¦Giftmischer/Tödlicher Extrakt/Lähmender Pollen/Tödliches Destillat/Konzentriertes Gift/Giftblüte/Gifte und Schwächung
herbalist_c¦Alquimista/Fluxo alquímico/Reserva espiritual/Colheita afortunada/Fluxo refinado/Colheita dourada/Recursos e fortuna¦Alchimiste/Flux alchimique/Réserve spirituelle/Récolte chanceuse/Flux raffiné/Moisson dorée/Ressources et fortune¦Alchemist/Alchemistischer Fluss/Geisterreserve/Glücksernte/Verfeinerter Fluss/Goldene Ernte/Ressourcen und Glück
engineer_a¦Artilheiro/Calibração letal/Protocolo de execução/Núcleo de impacto/Mira aperfeiçoada/Núcleo explosivo/Precisão e dano concentrado¦Artilleur/Calibrage létal/Protocole d'exécution/Noyau d'impact/Visée perfectionnée/Noyau explosif/Précision et dégâts concentrés¦Schütze/Tödliche Kalibrierung/Hinrichtungsprotokoll/Aufprallkern/Verbesserte Visierung/Explosiver Kern/Präzision und gebündelter Schaden
engineer_b¦Encouraçado/Blindagem reforçada/Âncora magnética/Chassi reforçado/Blindagem integral/Fortaleza industrial/Blindagem e estabilidade¦Cuirassé/Blindage renforcé/Ancrage magnétique/Châssis renforcé/Blindage intégral/Forteresse industrielle/Blindage et stabilité¦Gepanzerter/Verstärkte Panzerung/Magnetanker/Verstärktes Fahrwerk/Vollpanzerung/Industriefestung/Panzerung und Stabilität
engineer_c¦Acelerado/Engrenagens rápidas/Propulsão/Reator interno/Engrenagens supremas/Reator central/Velocidade operacional e energia¦Surcadencé/Engrenages rapides/Propulsion/Réacteur interne/Engrenages suprêmes/Réacteur central/Vitesse opérationnelle et énergie¦Übertakteter/Schnelle Zahnräder/Antrieb/Innerer Reaktor/Überlegene Zahnräder/Zentralreaktor/Arbeitstempo und Energie
lancer_a¦Dragão/Ponta mortal/Carga impetuosa/Ponta entorpecente/Estocada perfeita/Lança relâmpago/Mobilidade e ataques precisos¦Dragon/Pointe mortelle/Charge impétueuse/Pointe entravante/Estoc parfait/Lance foudroyante/Mobilité et attaques précises¦Dragoner/Tödliche Spitze/Ungestümer Ansturm/Hemmende Spitze/Vollkommener Stoß/Blitzlanze/Beweglichkeit und präzise Angriffe
lancer_b¦Falange/Espinho defensivo/Postura firme/Armadura de guerra/Espinho de aço/Formação imbatível/Resiste e pune quem se aproxima¦Phalange/Pointe défensive/Posture ferme/Armure de guerre/Pointe d'acier/Formation invincible/Résiste et punit ceux qui s'approchent¦Phalanx/Abwehrstachel/Fester Stand/Kriegsrüstung/Stahlstachel/Unbesiegbare Formation/Hält stand und bestraft nahe Gegner
lancer_c¦Empalador/Sede do guerreiro/Couraça vital/Estocada final/Sede do empalador/Colheita de guerra/Recupera vida e finaliza¦Empaleur/Soif guerrière/Cuirasse vitale/Estoc final/Soif de l'empaleur/Moisson de guerre/Récupère de la santé et achève¦Pfähler/Kriegerdurst/Lebensharnisch/Letzter Stoß/Pfählerdurst/Kriegsernte/Heilt und besiegt Feinde
swordsman_a¦Duelista/Corte magistral/Dança de aço/Passo do duelista/Corte supremo/Contra-ataque perfeito/Duelos, críticos e golpes seguidos¦Duelliste/Entaille magistrale/Danse d'acier/Pas du duelliste/Entaille suprême/Riposte parfaite/Duels, critiques et coups enchaînés¦Duellant/Meisterhafter Schnitt/Stahltanz/Duellantenschritt/Höchster Schnitt/Vollkommene Riposte/Duelle, kritische und verkettete Treffer
swordsman_b¦Kensei/Fome de aço/Execução marcial/Lâmina entorpecente/Fome de sangue/Dança carmesim/Pressão, recuperação e finalizações¦Kensei/Faim d'acier/Exécution martiale/Lame entravante/Faim de sang/Danse carmin/Pression, récupération et coups finaux¦Kensei/Stahlhunger/Kriegerische Hinrichtung/Hemmende Klinge/Bluthunger/Purpurtanz/Druck, Erholung und Abschlüsse
swordsman_c¦Guardião de aço/Guarda de aço/Resposta espinhosa/Vitalidade marcial/Guarda absoluta/Juramento de aço/Resiste ao desgaste¦Gardien d'acier/Garde d'acier/Riposte épineuse/Vitalité martiale/Garde absolue/Serment d'acier/Résiste à l'usure¦Stahlwächter/Stahlwache/Dornenriposte/Kriegerische Vitalität/Absolute Wache/Stahlschwur/Widersteht der Zermürbung
paladin_a¦Templário/Sentença divina/Fio radiante/Chama purificadora/Veredito celeste/Julgamento radiante/Golpes sagrados devastadores¦Templier/Sentence divine/Tranchant radieux/Flamme purificatrice/Verdict céleste/Jugement radieux/Frappes sacrées dévastatrices¦Templer/Göttliches Urteil/Strahlende Schneide/Reinigende Flamme/Himmlischer Richterspruch/Strahlendes Urteil/Verheerende heilige Treffer
paladin_b¦Guardião/Escudo consagrado/Espinhos sagrados/Graça restauradora/Escudo divino/Fortaleza consagrada/Protege o grupo e mantém a linha¦Gardien/Bouclier consacré/Épines sacrées/Grâce réparatrice/Bouclier divin/Forteresse consacrée/Protège le groupe et tient la ligne¦Wächter/Geweihter Schild/Heilige Dornen/Heilende Gnade/Göttlicher Schild/Geweihte Festung/Schützt die Gruppe und hält die Stellung
paladin_c¦Cruzado/Golpe radiante/Cadência sagrada/Fé inabalável/Golpe celeste/Graça compartilhada/Ataques rápidos e apoio¦Croisé/Frappe radieuse/Cadence sacrée/Foi inébranlable/Frappe céleste/Grâce partagée/Attaques rapides et soutien¦Kreuzritter/Strahlender Schlag/Heiliger Takt/Unerschütterlicher Glaube/Himmelsschlag/Geteilte Gnade/Schnelle Angriffe und Unterstützung
necromancer_a¦Senhor dos servos/Legião murcha/Reserva funerária/Mana sepulcral/Legião ascendente/Exército sepulcral/Seus servos são sua força¦Seigneur des serviteurs/Légion flétrie/Réserve funéraire/Mana sépulcral/Légion montante/Armée sépulcrale/Les serviteurs font sa force¦Dienerherr/Verdorrte Legion/Grabesreserve/Grabesmana/Aufsteigende Legion/Grabesheer/Seine Diener sind seine Stärke
necromancer_b¦Hemomante/Dreno funesto/Praga necrótica/Carne imortal/Dreno abissal/Maldição funerária/Drena vitalidade¦Hémomancien/Drain funeste/Fléau nécrotique/Chair immortelle/Drain abyssal/Malédiction funéraire/Draine la vitalité¦Hämomant/Unheilvoller Entzug/Nekrotische Seuche/Unsterbliches Fleisch/Abgrundentzug/Grabesfluch/Entzieht Lebenskraft
necromancer_c¦Praga/Praga venenosa/Marcha entorpecente/Decreto da morte/Praga eterna/Sepulcro profanado/Espalha doenças¦Peste/Fléau venimeux/Marche entravante/Décret de mort/Fléau éternel/Tombeau profané/Propage les maladies¦Seuche/Giftseuche/Hemmender Marsch/Todeserlass/Ewige Seuche/Entweihtes Grab/Verbreitet Krankheiten
druid_a¦Guardião selvagem/Raízes vigorosas/Casca protetora/Seiva renovadora/Coração de carvalho/Bosque protetor/Fortaleza e regeneração¦Gardien sauvage/Racines vigoureuses/Écorce protectrice/Sève rénovatrice/Cœur de chêne/Bosquet protecteur/Vigueur et régénération¦Wilder Wächter/Kräftige Wurzeln/Schützende Rinde/Erneuernder Saft/Eichenherz/Schützender Hain/Stärke und Regeneration
druid_b¦Caçador lunar/Instinto predador/Corrida feral/Presa tóxica/Instinto primordial/Caçada selvagem/Caça presas com críticos¦Chasseur lunaire/Instinct prédateur/Ruée sauvage/Croc toxique/Instinct primordial/Chasse sauvage/Chasse ses proies avec des critiques¦Mondjäger/Raubtierinstinkt/Wilder Ansturm/Giftzahn/Urinstinkt/Wilde Jagd/Jagt Beute mit kritischen Treffern
druid_c¦Guarda-espinhos/Sarças defensivas/Raízes inamovíveis/Pólen entorpecente/Sarças letais/Matagal invasor/Controla o espaço ao redor¦Gardien des ronces/Ronces défensives/Racines inamovibles/Pollen entravant/Ronces létales/Fourré envahissant/Contrôle l'espace alentour¦Dornenwächter/Abwehrranken/Unverrückbare Wurzeln/Hemmender Pollen/Tödliche Ranken/Wucherndes Dickicht/Beherrscht den Raum ringsum
merchant_a¦Tesoureiro/Capital arcano/Reserva comercial/Olhar do mercador/Capital composto/Mercado dourado/Aproveita os recursos ao máximo¦Trésorier/Capital arcanique/Réserve commerciale/Œil du marchand/Capital composé/Marché doré/Tire le meilleur des ressources¦Schatzmeister/Arkanes Kapital/Handelsreserve/Händlerauge/Zinskapital/Goldener Markt/Nutzt Ressourcen optimal
merchant_b¦Mercenário/Aposta mortal/Sangue arriscado/Cobrança final/Aposta mestra/Falência explosiva/Críticos e finalizações oportunistas¦Mercenaire/Pari mortel/Sang risqué/Recouvrement final/Pari magistral/Faillite explosive/Critiques et coups finaux opportunistes¦Söldner/Tödliche Wette/Risikoblut/Letzte Eintreibung/Meisterwette/Explosiver Bankrott/Gelegene kritische Treffer und Abschlüsse
merchant_c¦Contrabandista/Passo furtivo/Fuga rápida/Pele do negociante/Passo invisível/Fuga calculada/Evasão e mobilidade¦Contrebandier/Pas furtif/Fuite rapide/Peau du négociant/Pas invisible/Fuite calculée/Esquive et mobilité¦Schmuggler/Verdeckter Schritt/Schnelle Flucht/Händlerhaut/Unsichtbarer Schritt/Berechnete Flucht/Ausweichen und Beweglichkeit
monk_a¦Punho de ferro/Golpe transcendente/Cadência interior/Passo leve/Punho absoluto/Punhos do trovão/Críticos e velocidade¦Poing de fer/Frappe transcendante/Cadence intérieure/Pas léger/Poing absolu/Poings du tonnerre/Critiques et vitesse¦Eisenfaust/Erhabener Schlag/Innerer Takt/Leichter Schritt/Absolute Faust/Donnerfäuste/Kritische Treffer und Geschwindigkeit
monk_b¦Lótus/Respiração profunda/Reflexo sereno/Foco interior/Respiração eterna/Serenidade compartilhada/Foco e combates longos¦Lotus/Souffle profond/Réflexe serein/Concentration intérieure/Souffle éternel/Sérénité partagée/Concentration et longs combats¦Lotus/Tiefer Atem/Ruhiger Reflex/Innerer Fokus/Ewiger Atem/Geteilte Gelassenheit/Fokus und lange Kämpfe
monk_c¦Punho do dragão/Dreno marcial/Impacto sísmico/Golpe final/Dreno absoluto/Caminho do destruidor/Dreno, impacto e finalizações¦Poing du dragon/Drain martial/Impact sismique/Coup final/Drain absolu/Voie du destructeur/Drain, impact et coups finaux¦Drachenfaust/Kriegerischer Entzug/Seismischer Einschlag/Letzter Schlag/Absoluter Entzug/Weg des Zerstörers/Entzug, Einschlag und Abschlüsse
elementalist_a¦Catalisador/Núcleo ígneo/Colapso elemental/Sentença ardente/Chama primordial/Cataclismo ígneo/Devastação pelo fogo¦Catalyseur/Noyau igné/Effondrement élémentaire/Sentence brûlante/Flamme primordiale/Cataclysme igné/Dévastation par le feu¦Katalysator/Feuerkern/Elementarkollaps/Sengendes Urteil/Urflamme/Feuerkataklysmus/Verheerung durch Feuer
elementalist_b¦Condutor/Corrente arcana/Surto elemental/Manto energético/Corrente primordial/Coração elemental/Sustenta o poder elemental¦Conducteur/Courant arcanique/Afflux élémentaire/Manteau énergétique/Courant primordial/Cœur élémentaire/Entretient la puissance élémentaire¦Leiter/Arkaner Strom/Elementarschub/Energiemantel/Urstrom/Elementarherz/Erhält die Elementarkraft
elementalist_c¦Tempestade/Toque glacial/Passo boreal/Vento elemental/Toque glacial supremo/Tempestade cristalina/Controle e distância¦Tempête/Toucher glacial/Pas boréal/Vent élémentaire/Toucher glacial suprême/Tempête cristalline/Contrôle et distance¦Sturm/Frostberührung/Nordschritt/Elementarwind/Höchste Frostberührung/Kristallsturm/Kontrolle und Abstand
gunslinger_a¦Pistoleiro/Mira letal/Disparo final/Cadência precisa/Mira absoluta/Munição fulminante/Disparos precisos¦Pistolero/Visée létale/Tir final/Cadence précise/Visée absolue/Munitions foudroyantes/Tirs précis¦Revolverheld/Tödliche Visierung/Letzter Schuss/Präziser Takt/Absolute Visierung/Blitzmunition/Präzise Schüsse
gunslinger_b¦Atirador arcano/Câmara energética/Reator de munição/Deslocamento tático/Câmara sobrecarregada/Arsenal inesgotável/Energia e pressão¦Tireur arcanique/Chambre énergétique/Réacteur de munitions/Déplacement tactique/Chambre surchargée/Arsenal inépuisable/Énergie et pression¦Arkaner Schütze/Energiekammer/Munitionsreaktor/Taktischer Wechsel/Überladene Kammer/Endloses Arsenal/Energie und Druck
gunslinger_c¦Fora da lei/Disparo drenante/Corrida balística/Disparo entorpecente/Disparo voraz/Bala fantasma/Mobilidade e desgaste¦Hors-la-loi/Tir drainant/Ruée balistique/Tir entravant/Tir vorace/Balle fantôme/Mobilité et usure¦Gesetzloser/Entzugsschuss/Ballistischer Ansturm/Hemmender Schuss/Gieriger Schuss/Geisterkugel/Beweglichkeit und Zermürbung
weapon_summoner_a¦Lâminas dançantes/Fio manifesto/Impacto espectral/Dreno do arsenal/Arsenal liberado/Tempestade de lâminas/Pressão com armas conjuradas¦Lames dansantes/Tranchant manifesté/Impact spectral/Drain d'arsenal/Arsenal déchaîné/Tempête de lames/Pression par les armes invoquées¦Tanzende Klingen/Manifestierte Schneide/Geistereinschlag/Arsenalentzug/Entfesseltes Arsenal/Klingensturm/Druck durch beschworene Waffen
weapon_summoner_b¦Bastião de aço/Armadura manifesta/Reflexo de aço/Núcleo defensivo/Armadura perpétua/Forja inabalável/Armamentos protetores¦Bastion d'acier/Armure manifestée/Reflet d'acier/Noyau défensif/Armure perpétuelle/Forge inébranlable/Armements protecteurs¦Stahlbastion/Manifestierte Rüstung/Stahlreflex/Abwehrkern/Ewige Rüstung/Unerschütterliche Schmiede/Schützende Bewaffnung
weapon_summoner_c¦Forja rúnica/Fluxo de armas/Reserva manifesta/Carrasco espectral/Fluxo de armas supremo/Núcleo de invocação/Recursos e finalizações¦Forge runique/Flux d'armes/Réserve manifestée/Bourreau spectral/Flux d'armes suprême/Noyau d'invocation/Ressources et coups finaux¦Runenschmiede/Waffenfluss/Manifestierte Reserve/Geisterhenker/Höchster Waffenfluss/Beschwörungskern/Ressourcen und Abschlüsse
hunter_a¦Franco-atirador/Instinto letal/Disparo certeiro/Armadilha venenosa/Instinto furtivo/Marca do rastreador/Pontos fracos e execução¦Tireur d'élite/Instinct létal/Tir juste/Piège venimeux/Instinct de traque/Marque du traqueur/Points faibles et exécution¦Scharfschütze/Tödlicher Instinkt/Gezielter Schuss/Giftfalle/Pirschinstinkt/Fährtenlesermal/Schwachstellen und Hinrichtung
hunter_b¦Armadilheiro/Passo silencioso/Corrida do vento/Flecha entorpecente/Passo do rastreador/Saque do explorador/Mobilidade e evasão¦Trappeur/Pas silencieux/Course du vent/Flèche entravante/Pas du traqueur/Butin de l'explorateur/Mobilité et esquive¦Fallensteller/Lautloser Schritt/Windlauf/Hemmender Pfeil/Fährtenleserschritt/Entdeckerbeute/Beweglichkeit und Ausweichen
hunter_c¦Fera/Instinto sanguinário/Constituição selvagem/Impacto de caça/Instinto de sobrevivência/Troféu supremo/Vitalidade e impacto¦Bête/Instinct sanguinaire/Constitution sauvage/Impact de chasse/Instinct de survie/Trophée suprême/Vitalité et impact¦Bestie/Blutdürstiger Instinkt/Wilde Konstitution/Jagdeinschlag/Überlebensinstinkt/Höchste Trophäe/Lebenskraft und Einschlag
bard_a¦Menestrel/Ressonância vital/Melodia arcana/Nota afortunada/Ressonância superior/Sinfonia vital/Energia e fortuna¦Ménestrel/Résonance vitale/Mélodie arcanique/Note chanceuse/Résonance supérieure/Symphonie vitale/Énergie et fortune¦Spielmann/Lebensresonanz/Arkane Melodie/Glücksnote/Höhere Resonanz/Lebenssinfonie/Energie und Glück
bard_b¦Trovador de guerra/Acorde crítico/Ritmo acelerado/Nota ardente/Acorde perfeito/Sinfonia do trovão/Cadência e ataques flamejantes¦Troubadour de guerre/Accord critique/Rythme accéléré/Note ardente/Accord parfait/Symphonie du tonnerre/Cadence et attaques enflammées¦Kriegstroubadour/Kritischer Akkord/Beschleunigter Rhythmus/Feuernote/Vollkommener Akkord/Donnersinfonie/Takt und brennende Angriffe
bard_c¦Cantor sagrado/Melodia restauradora/Hino guardião/Voz vital/Melodia eterna/Última estrofe/Sustenta o grupo¦Chantre sacré/Mélodie réparatrice/Hymne gardien/Voix vitale/Mélodie éternelle/Dernière strophe/Soutient le groupe¦Heiliger Sänger/Heilende Melodie/Wächterhymne/Lebensstimme/Ewige Melodie/Letzte Strophe/Unterstützt die Gruppe
warlock_a¦Amaldiçoado/Marca murchante/Toxina sombria/Veredito sombrio/Marca abissal/Maldição infinita/Maldições e execução¦Maudit/Marque flétrissante/Toxine obscure/Verdict sinistre/Marque abyssale/Malédiction infinie/Malédictions et exécution¦Verfluchter/Verdorrendes Mal/Dunkles Toxin/Düsterer Richterspruch/Abgrundmal/Endloser Fluch/Flüche und Hinrichtung
warlock_b¦Sangue maldito/Dreno sombrio/Carne profana/Renovação umbria/Dreno profano/Banquete sombrio/Vitalidade como arma¦Sang maudit/Drain obscur/Chair impie/Renouveau ténébreux/Drain profane/Festin sinistre/La vitalité comme arme¦Verfluchtes Blut/Dunkler Entzug/Unheiliges Fleisch/Dunkle Erneuerung/Unheiliger Entzug/Düsteres Festmahl/Lebenskraft als Waffe
warlock_c¦Abismo/Golpe do abismo/Reserva abissal/Canalização sombria/Golpe do vazio/Abismo inesgotável/Concentra recursos¦Abîme/Frappe abyssale/Réserve abyssale/Canalisation obscure/Frappe du vide/Abîme inépuisable/Concentre les ressources¦Abgrund/Abgrundschlag/Abgrundreserve/Dunkles Kanalisieren/Leerenschlag/Bodenloser Abgrund/Bündelt Ressourcen
tamer_a¦Matilha/Pele de fera/Constituição feral/Rugido de guerra/Pele primordial/Matilha suprema/Linha de frente com suas feras¦Meute/Peau de bête/Constitution sauvage/Rugissement de guerre/Peau primordiale/Meute suprême/En première ligne avec ses bêtes¦Rudel/Bestienhaut/Wilde Konstitution/Kriegsbrüllen/Urhaut/Höchstes Rudel/Frontlinie an der Seite seiner Bestien
tamer_b¦Feral/Instinto alfa/Ataque coordenado/Presa voraz/Instinto alfa supremo/Caçada dominante/Ataques coordenados¦Sauvage/Instinct alpha/Attaque coordonnée/Croc vorace/Instinct alpha suprême/Chasse dominante/Attaques coordonnées¦Wild/Alphainstinkt/Koordinierter Angriff/Gieriger Fangzahn/Höchster Alphainstinkt/Überlegene Jagd/Koordinierte Angriffe
tamer_c¦Pastor/Vínculo vital/Instinto afortunado/Reserva animal/Vínculo eterno/Refúgio da matilha/Sobrevivência e recursos¦Berger/Lien vital/Instinct chanceux/Réserve animale/Lien éternel/Refuge de la meute/Survie et ressources¦Hirte/Lebensband/Glücksinstinkt/Tierreserve/Ewiges Band/Rudelzuflucht/Überleben und Ressourcen
shapeshifter_a¦Predador/Instinto letal/Fome selvagem/Corrida feral/Instinto supremo/Reflexo predador/Persegue e elimina¦Prédateur/Instinct létal/Faim sauvage/Ruée sauvage/Instinct suprême/Réflexe prédateur/Traque et élimine¦Raubtier/Tödlicher Instinkt/Wilder Hunger/Wilder Ansturm/Höchster Instinkt/Raubtierreflex/Verfolgt und besiegt
shapeshifter_b¦Mímico/Reflexos selvagens/Instinto adaptativo/Fluxo metamórfico/Reflexos primordiais/Metamorfose afortunada/Mobilidade e energia¦Mime/Réflexes sauvages/Instinct adaptatif/Flux métamorphique/Réflexes primordiaux/Métamorphose chanceuse/Mobilité et énergie¦Mimiker/Wilde Reflexe/Anpassungsinstinkt/Metamorpher Fluss/Urreflexe/Glückswandlung/Beweglichkeit und Energie
shapeshifter_c¦Colosso bestial/Constituição monstruosa/Pele encouraçada/Espinhos naturais/Constituição primordial/Forma imparável/Absorve impactos¦Colosse bestial/Constitution monstrueuse/Peau cuirassée/Épines naturelles/Constitution primordiale/Forme imparable/Absorbe les impacts¦Bestienkoloss/Monströse Konstitution/Gepanzerte Haut/Naturdornen/Urkonstitution/Unaufhaltsame Gestalt/Absorbiert Treffer
""".strip().splitlines():
    path, *columns = row.split('¦')
    for locale, column in zip(LOCALES, columns, strict=True):
        values = column.split('/')
        if len(values) != 7 or path in LOCAL_ROWS[locale]:
            raise ValueError(f'Textos de senda inválidos: {locale}/{path}')
        LOCAL_ROWS[locale][path] = values


def java_effects():
    """{rol: [[S1,S2,S3,S4,S5x,S5y,S6] por senda]} leído de RoleTree.java."""
    with open(JAVA, encoding='utf-8') as source:
        src = source.read()
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
    """Cifra con signo; en español, coma decimal y espacio antes del %. El resto, punto (lo exigen las pruebas)."""
    pct = EFFECTS[effect][1]
    text = '+' + num(value * 100 if pct else value)
    if lang == 'es':
        text = text.replace('.', ',')
    return text + ((' %' if lang == 'es' else '%') if pct else '')


def desc(effect, mult, lang):
    """Frase llana del efecto y sus valores en los rangos 1, 2 y 3 ({@code mult} 1,5 en S6)."""
    v = EFFECTS[effect][0] * mult
    values = ' / '.join(amount(effect, v * r, lang) for r in (1, 2, 3))
    return f'{SENTENCE[lang][effect]} {RANKS[lang]} {values}.'


def keystone_templates():
    """{rol: [plantilla A, B, C]} leído de KeystoneRules.java."""
    with open(KS_JAVA, encoding='utf-8') as source:
        src = source.read()
    out = {m.group(1): [KS_CAMEL[x] for x in m.groups()[1:]]
           for m in re.finditer(r'entry\("(\w+)", Template\.(\w+), Template\.(\w+), Template\.(\w+)\)', src)}
    assert len(out) == 20, 'KeystoneRules: faltan roles'
    return out


def build(lang, data):
    if lang in LOCALES:
        return build_local(lang)
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
        out[key + '.desc'] = TEMPLATE[code][tpl][1]

        names = es_names if code == 'es' else en_names
        effs = effects[role][path - 1]
        base = f'{role}_{LETTERS[path - 1]}'
        out[f'path.aptitudes.{base}.desc'] = es_flavor if code == 'es' else en_flavor
        for i in range(3):
            out[f'node.aptitudes.{base}{i + 1}.desc'] = desc(effs[i], 1.0, code)
        for i, suffix in enumerate(['4', '5x', '5y', '6']):
            key = f'node.aptitudes.{base}{suffix}'
            out[key] = names[i]
            out[key + '.desc'] = desc(effs[3 + i], 1.5 if suffix == '6' else 1.0, code)
    out.update(UI[lang])
    return out


def build_local(lang):
    effects, templates = java_effects(), keystone_templates()
    expected = {f'{role}_{LETTERS[path - 1]}' for role, path, *_ in ROWS}
    if set(LOCAL_ROWS[lang]) != expected:
        raise ValueError(f'Cobertura de nombres de senda incorrecta: {lang}')
    out = dict(UI[lang])
    for template, (name, text) in TEMPLATE[lang].items():
        out[f'keystone.aptitudes.{template}'] = name
        out[f'keystone.aptitudes.{template}.desc'] = text
        out[f'subtitles.aptitudes.keystone_{template}'] = SUBTITLES[lang][template]
    for role, path, *_ in ROWS:
        base = f'{role}_{LETTERS[path - 1]}'
        title, *rest = LOCAL_ROWS[lang][base]
        names, keystone, flavor = rest[:4], rest[4], rest[5]
        out[f'path.aptitudes.{base}'] = title
        out[f'path.aptitudes.{base}.desc'] = flavor
        for i, rank in enumerate(('I', 'II', 'III')):
            key = f'node.aptitudes.{base}{i + 1}'
            out[key] = f'{title} {rank}'
            out[key + '.desc'] = desc(effects[role][path - 1][i], 1.0, lang)
        for i, suffix in enumerate(('4', '5x', '5y', '6')):
            key = f'node.aptitudes.{base}{suffix}'
            out[key] = names[i]
            out[key + '.desc'] = desc(effects[role][path - 1][3 + i], 1.5 if suffix == '6' else 1.0, lang)
        key = f'node.aptitudes.{base}7'
        name, text = TEMPLATE[lang][templates[role][path - 1]]
        out[key] = keystone
        out[key + '.desc'] = text
    return out


# Rótulos generados de la vista de sendas.
for locale, labels in {
    'pt_br': (
        'Você já segue outro caminho nesta classe (os caminhos são exclusivos)',
        'Você já escolheu o outro ramo desta bifurcação',
        'Caminhos exclusivos: em %s você mantém o caminho %s e recupera %s pontos dos demais.',
        'Caminhos', '%s · Caminhos', 'Nível da classe %s/%s', 'Pontos livres: %s',
        'Escolha um caminho: ele é exclusivo (bloqueia os outros dois)', 'Seu caminho',
        'Bloqueado: você já segue %s', 'Bifurcação: escolha um', 'Pedra angular (nível 50)',
        'Aprender', 'Árvore', 'Escolher esta opção?',
        'Aprender %s escolhe o caminho %s e bloqueia os outros dois desta classe. Só é possível mudar reiniciando a árvore.',
        'Aprender %s bloqueia o outro ramo da bifurcação. Só é possível mudar reiniciando a árvore.'),
    'fr_fr': (
        'Vous suivez déjà une autre voie pour ce rôle (les voies sont exclusives)',
        'Vous avez déjà choisi l’autre branche de cette bifurcation',
        'Voies exclusives : pour %s, vous gardez la voie %s et récupérez %s points des autres.',
        'Voies', '%s · Voies', 'Niveau du rôle %s/%s', 'Points libres : %s',
        'Choisissez une voie : elle est exclusive (verrouille les deux autres)', 'Votre voie',
        'Verrouillé : vous suivez déjà %s', 'Bifurcation : choisissez une branche', 'Pierre angulaire (niveau 50)',
        'Apprendre', 'Arbre', 'Choisir cette option ?',
        'Apprendre %s choisit la voie %s et verrouille les deux autres pour ce rôle. Seule une réinitialisation de l’arbre permet de changer.',
        'Apprendre %s verrouille l’autre branche de la bifurcation. Seule une réinitialisation de l’arbre permet de changer.'),
    'de_de': (
        'Du folgst mit dieser Rolle bereits einem anderen Pfad (Pfade schließen einander aus)',
        'Du hast bereits den anderen Zweig dieser Verzweigung gewählt',
        'Exklusive Pfade: Bei %s behältst du den Pfad %s und erhältst %s Punkte der anderen zurück.',
        'Pfade', '%s · Pfade', 'Rollenstufe %s/%s', 'Freie Punkte: %s',
        'Wähle einen exklusiven Pfad (sperrt die beiden anderen)', 'Dein Pfad',
        'Gesperrt: Du folgst bereits %s', 'Verzweigung: einen Zweig wählen', 'Schlüsselstein (Stufe 50)',
        'Erlernen', 'Baum', 'Diese Option wählen?',
        'Durch das Erlernen von %s wählst du den Pfad %s und sperrst die beiden anderen dieser Rolle. Ein Wechsel erfordert das Zurücksetzen des Baums.',
        'Durch das Erlernen von %s sperrst du den anderen Zweig. Ein Wechsel erfordert das Zurücksetzen des Baums.'),
}.items():
    UI[locale] = dict(zip(UI['en_us'], labels, strict=True))

for index, locale in enumerate(LOCALES):
    TEMPLATE[locale] = {key: (UI_TRANSLATIONS[f'keystone.aptitudes.{key}'][index],
                              UI_TRANSLATIONS[f'keystone.aptitudes.{key}.desc'][index])
                        for key in TEMPLATE['en']}
    SUBTITLES[locale] = {key: UI_TRANSLATIONS[f'subtitles.aptitudes.keystone_{key}'][index]
                          for key in TEMPLATE['en']}


def update(lang, data):
    new = build(lang, data)
    guide = 'guide.aptitudes.tree.body'
    if lang in GUIDE:
        old, repl = GUIDE[lang]
        if old in data[guide]:
            data[guide] = data[guide].replace(old, repl)
    data.pop('screen.aptitudes.paths.keystone_soon', None)  # v46.0: aviso provisional, ya no se usa
    # Actualiza en su sitio las claves existentes y añade al final solo las nuevas.
    data.update(new)
    return data


if __name__ == '__main__':
    from generator_cli import run
    raise SystemExit(run(update, __doc__))
