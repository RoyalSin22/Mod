"""Traducción inicial de interfaz y objetos (T8, fase 1). Orden: pt_br, fr_fr, de_de.

Los textos del grimorio y los nodos se incorporan en sus fases; las filas generadas
de interfaz se mantienen en UI de gen_evolved_lang y gen_path_lang.
"""

LOCALES = ('pt_br', 'fr_fr', 'de_de')
TRANSLATIONS = {}


def add(prefix, rows):
    for row in rows.strip().splitlines():
        if not row.strip():
            continue
        key, *values = row.split('¦')
        key = prefix + key
        if len(values) != 3 or key in TRANSLATIONS:
            raise ValueError(f'Fila inválida o duplicada: {key}')
        TRANSLATIONS[key] = tuple(v.replace('\\n', '\n') for v in values)


add('', """
key.aptitudes.open¦Abrir habilidades e atributos¦Ouvrir les compétences et attributs¦Fertigkeiten und Attribute öffnen
category.aptitudes¦Aptitudes¦Aptitudes¦Aptitudes
screen.aptitudes.title¦Habilidades e atributos¦Compétences et attributs¦Fertigkeiten und Attribute
screen.aptitudes.skills¦Habilidades¦Compétences¦Fertigkeiten
screen.aptitudes.attributes¦Atributos¦Attributs¦Attribute
screen.aptitudes.points¦Pontos disponíveis: %s¦Points disponibles : %s¦Verfügbare Punkte: %s
screen.aptitudes.level¦Nv. %s¦Niv. %s¦Stufe %s
screen.aptitudes.max¦MÁX¦MAX¦MAX
screen.aptitudes.xp¦XP: %s / %s¦XP : %s / %s¦EP: %s / %s
screen.aptitudes.waiting¦Aguardando dados do servidor...¦En attente des données du serveur...¦Warte auf Serverdaten...
screen.aptitudes.shift_hint¦Shift + clique: atribuir 5 pontos¦Maj + clic : attribuer 5 points¦Umschalt + Klick: 5 Punkte verteilen
itemGroup.aptitudes.items¦Aptitudes¦Aptitudes¦Aptitudes
""")

add('skill.aptitudes.', """
mining¦Mineração¦Minage¦Bergbau
mining.desc¦Evolui ao minerar pedras e minérios. Passivas: mineração mais rápida e ARMADURA.¦Progresse en minant la roche et les minerais. Passifs : minage plus rapide et ARMURE.¦Steigt durch den Abbau von Stein und Erzen. Passive Boni: schnellerer Abbau und RÜSTUNG.
excavation¦Escavação¦Excavation¦Graben
excavation.desc¦Evolui ao escavar. Passivas: escavação mais rápida e RESISTÊNCIA A RECUO.¦Progresse en creusant. Passifs : excavation plus rapide et RÉSISTANCE AU RECUL.¦Steigt durch Graben. Passive Boni: schnelleres Graben und RÜCKSTOSSRESISTENZ.
woodcutting¦Corte de madeira¦Bûcheronnage¦Holzfällen
woodcutting.desc¦Evolui ao cortar troncos. Passivas: corte mais rápido e VIDA MÁXIMA.¦Progresse en coupant des bûches. Passifs : coupe plus rapide et SANTÉ MAXIMALE.¦Steigt durch das Fällen von Stämmen. Passive Boni: schnelleres Fällen und MAXIMALES LEBEN.
farming¦Agricultura¦Agriculture¦Landwirtschaft
farming.desc¦Evolui ao colher plantações. Passivas: colheita dupla e SORTE.¦Progresse en récoltant les cultures. Passifs : récolte double et CHANCE.¦Steigt durch das Ernten von Pflanzen. Passive Boni: doppelte Ernte und GLÜCK.
combat¦Combate¦Combat¦Nahkampf
combat.desc¦Evolui com abates corpo a corpo. Passiva: mais dano corpo a corpo (%).¦Progresse avec les éliminations au corps à corps. Passif : dégâts de mêlée accrus (%).¦Steigt durch Nahkampftötungen. Passiver Bonus: mehr Nahkampfschaden (%).
archery¦Arco e flecha¦Tir à l'arc¦Bogenschießen
archery.desc¦Evolui com abates por projéteis. Passiva: mais dano de flechas.¦Progresse avec les éliminations par projectile. Passif : dégâts des flèches accrus.¦Steigt durch Tötungen mit Projektilen. Passiver Bonus: mehr Pfeilschaden.
defense¦Defesa¦Défense¦Verteidigung
defense.desc¦Evolui ao receber golpes. Passivas: ARMADURA, ROBUSTEZ e resistência a explosões.¦Progresse en subissant des coups. Passifs : ARMURE, ROBUSTESSE et résistance aux explosions.¦Steigt durch erlittene Treffer. Passive Boni: RÜSTUNG, RÜSTUNGSHÄRTE und Explosionsresistenz.
agility¦Agilidade¦Agilité¦Beweglichkeit
agility.desc¦Evolui ao correr. Passivas: VELOCIDADE e queda segura.¦Progresse en sprintant. Passifs : VITESSE et chute amortie.¦Steigt durch Sprinten. Passive Boni: GESCHWINDIGKEIT und sichere Fallhöhe.
fishing¦Pesca¦Pêche¦Angeln
fishing.desc¦Evolui ao pescar. Passivas: SORTE e pesca dupla.¦Progresse en pêchant. Passifs : CHANCE et prise double.¦Steigt durch das Fangen von Fischen. Passive Boni: GLÜCK und doppelter Fang.
enchanting¦Encantamento¦Enchantement¦Verzaubern
enchanting.desc¦Evolui ao encantar na mesa de encantamentos. Passivas: restituição de níveis e MANA máximo.¦Progresse en enchantant à la table d'enchantement. Passifs : remboursement de niveaux et MANA maximal.¦Steigt durch Verzaubern am Zaubertisch. Passive Boni: Stufenerstattung und maximales MANA.
""")

add('attribute.aptitudes.', """
strength¦Força¦Force¦Stärke
strength.desc¦Aumenta o dano corpo a corpo.¦Augmente les dégâts de mêlée.¦Erhöht den Nahkampfschaden.
dexterity¦Destreza¦Dextérité¦Geschicklichkeit
dexterity.desc¦Aumenta a velocidade de ataque.¦Augmente la vitesse d'attaque.¦Erhöht die Angriffsgeschwindigkeit.
endurance¦Resistência¦Endurance¦Ausdauer
endurance.desc¦Aumenta a armadura.¦Augmente l'armure.¦Erhöht die Rüstung.
luck¦Sorte¦Chance¦Glück
luck.desc¦Melhora o saque (baús e pesca).¦Améliore le butin (coffres et pêche).¦Verbessert die Beute (Truhen und Angeln).
""")

add('aptitudes.', """
levelup¦%s alcançou o nível %s! (+%s pontos de atributo)¦%s atteint le niveau %s ! (+%s points d'attribut)¦%s hat Stufe %s erreicht! (+%s Attributpunkte)
xp_gain¦+%s XP de %s (%s/%s)¦+%s XP de %s (%s/%s)¦+%s EP für %s (%s/%s)
allocations_reset¦Seus pontos de atributo foram restituídos porque os pontos disponíveis mudaram.¦Vos points d'attribut ont été remboursés, car vos points disponibles ont changé.¦Deine Attributpunkte wurden erstattet, weil sich deine verfügbaren Punkte geändert haben.
""")

add('aptitudes.cmd.', """
header¦— Aptitudes de %s —¦— Aptitudes de %s —¦— Aptitudes von %s —
skill_line¦%s: nível %s (%s/%s XP)¦%s : niveau %s (%s/%s XP)¦%s: Stufe %s (%s/%s EP)
attribute_line¦%s: %s/%s¦%s : %s/%s¦%s: %s/%s
points¦Pontos não gastos: %s¦Points inutilisés : %s¦Unverteilte Punkte: %s
allocated¦%s ponto(s) atribuído(s) a %s.¦%s point(s) attribué(s) à %s.¦%s Punkt(e) für %s verteilt.
allocate_fail¦Não foi possível atribuir pontos (sem pontos disponíveis ou atributo no máximo).¦Impossible d'attribuer des points (aucun point disponible ou attribut au maximum).¦Punkte konnten nicht verteilt werden (keine Punkte verfügbar oder Attribut am Maximum).
respec_done¦Pontos de atributo restituídos.¦Points d'attribut remboursés.¦Attributpunkte erstattet.
respec_disabled¦A redistribuição está desativada neste servidor.¦La réattribution est désactivée sur ce serveur.¦Die Neuverteilung ist auf diesem Server deaktiviert.
unknown_skill¦Habilidade desconhecida: %s¦Compétence inconnue : %s¦Unbekannte Fertigkeit: %s
unknown_attribute¦Atributo desconhecido: %s¦Attribut inconnu : %s¦Unbekanntes Attribut: %s
setlevel¦O nível de %s de %s foi definido como %s.¦Le niveau de %s de %s est défini à %s.¦%s von %s auf Stufe %s gesetzt.
addxp¦%s XP de %s concedidos a %s.¦%s XP de %s accordés à %s.¦%s EP für %s an %s vergeben.
addpoints¦%s pontos extras concedidos a %s.¦%s points bonus accordés à %s.¦%s Bonuspunkte an %s vergeben.
reset¦O progresso de %s foi reiniciado.¦La progression de %s a été réinitialisée.¦Der Fortschritt von %s wurde zurückgesetzt.
reload¦Configuração do Aptitudes recarregada.¦Configuration d'Aptitudes rechargée.¦Aptitudes-Konfiguration neu geladen.
""")

add('key.aptitudes.', """
roles¦Abrir classes¦Ouvrir les rôles¦Rollen öffnen
ability_1¦Habilidade 1 (classe principal)¦Capacité 1 (rôle principal)¦Fähigkeit 1 (Hauptrolle)
ability_2¦Habilidade 2 (classe principal)¦Capacité 2 (rôle principal)¦Fähigkeit 2 (Hauptrolle)
ability_3¦Habilidade 3 (classe secundária)¦Capacité 3 (rôle secondaire)¦Fähigkeit 3 (Nebenrolle)
ability_4¦Habilidade 4 (classe secundária)¦Capacité 4 (rôle secondaire)¦Fähigkeit 4 (Nebenrolle)
""")

add('screen.aptitudes.', """
roles.title¦Classes¦Rôles¦Rollen
roles.set_primary¦Definir principal¦Choisir comme principal¦Als Hauptrolle
roles.set_secondary¦Definir secundária¦Choisir comme secondaire¦Als Nebenrolle
roles.change_cd¦Troca em %s min¦Changement dans %s min¦Wechsel in %s Min.
roles.inactive_hint¦Escolha esta classe como principal ou secundária para usar sua árvore.¦Choisissez ce rôle comme principal ou secondaire pour utiliser son arbre.¦Wähle diese Rolle als Haupt- oder Nebenrolle, um ihren Baum zu nutzen.
roles.active¦Ativa¦Actif¦Aktiv
roles.passive¦Passiva¦Passif¦Passiv
roles.requires¦Próximo grau: nível %s¦Rang suivant : niveau %s¦Nächster Rang: Stufe %s
roles.active_info¦Mana: %s · Recarga: %s s · Espaço %s¦Mana : %s · Recharge : %s s · Emplacement %s¦Mana: %s · Abklingzeit: %s s · Platz %s
roles.needs¦Requer: %s¦Nécessite : %s¦Benötigt: %s
elements.header¦Elementos: %s/%s (máx. %s)¦Éléments : %s/%s (max %s)¦Elemente: %s/%s (max. %s)
elements.choose¦Escolha um elemento!¦Choisissez un élément !¦Wähle ein Element!
elements.next¦Próximo no nível %s¦Suivant au niveau %s¦Nächstes auf Stufe %s
evolve¦Evoluir¦Évoluer¦Entwickeln
evolve.maxed¦Esta classe alcançou sua forma definitiva.¦Ce rôle a atteint sa forme ultime.¦Diese Rolle hat ihre ultimative Form erreicht.
evolve.to¦Evoluir para: %s¦Évoluer en : %s¦Entwickeln zu: %s
evolve.active¦Classe principal ou secundária¦Rôle principal ou secondaire¦Haupt- oder Nebenrolle
evolve.boss¦Derrote o Wither, o Dragão ou o Defensor com esta classe ativa¦Vaincre le Wither, le Dragon ou le Warden avec ce rôle actif¦Besiege Wither, Drache oder Warden mit dieser aktiven Rolle
evolve.item¦Consome: %s¦Consomme : %s¦Verbraucht: %s
synergy.specialization¦Especialização: +15% XP de classe¦Spécialisation : +15% d'XP de rôle¦Spezialisierung: +15% Rollen-EP
synergy.versatility¦Versatilidade: +15 de mana máximo¦Polyvalence : +15 de mana maximal¦Vielseitigkeit: +15 maximales Mana
forms.header¦Formas (clique para escolher)¦Formes (cliquer pour choisir)¦Formen (zum Wählen anklicken)
forms.selected¦Selecionada¦Sélectionnée¦Ausgewählt
forms.unlocked¦Desbloqueada¦Débloquée¦Freigeschaltet
forms.kills¦Derrotados: %s/%s¦Vaincus : %s/%s¦Besiegt: %s/%s
uniques.title¦Habilidades únicas¦Capacités uniques¦Einzigartige Fähigkeiten
uniques.rule¦Uma por servidor · uma por jogador¦Une par serveur · une par joueur¦Eine pro Server · eine pro Spieler
uniques.cost¦Mana: %s · Recarga: %s s · Tecla H¦Mana : %s · Recharge : %s s · Touche H¦Mana: %s · Abklingzeit: %s s · Taste H
confirm¦Confirmar¦Confirmer¦Bestätigen
reset¦Redefinir¦Réinitialiser¦Zurücksetzen
tab.attributes¦Atributos¦Attributs¦Attribute
tab.skills¦Habilidades¦Compétences¦Fertigkeiten
tab.roles¦Classes¦Rôles¦Rollen
tab.uniques¦Únicas¦Uniques¦Einzigartige
hint.attributes¦↑↓ Selecionar  ←→ Ajustar  Shift ×5  Enter Confirmar  Tab Página¦↑↓ Sélection  ←→ Ajuster  Maj ×5  Entrée Confirmer  Tab Page¦↑↓ Auswahl  ←→ Anpassen  Umschalt ×5  Eingabe Bestätigen  Tab Seite
hint.tabs¦Tab: próxima página · Shift+Tab: anterior¦Tab : page suivante · Maj+Tab : précédente¦Tab: nächste Seite · Umschalt+Tab: vorherige
attribute_points¦Pontos de atributo¦Points d'attribut¦Attributpunkte
base_stats¦Estatísticas básicas¦Statistiques de base¦Grundwerte
details¦Detalhes¦Détails¦Details
points_invested¦%s pontos investidos¦%s points investis¦%s investierte Punkte
per_point¦Por ponto¦Par point¦Pro Punkt
limit¦Limite¦Limite¦Grenze
roles.upgrade¦Melhorar¦Améliorer¦Verbessern
roles.tree¦Árvore¦Arbre¦Baum
roles.elements¦Elementos¦Éléments¦Elemente
roles.forms¦Formas¦Formes¦Formen
node.core¦Núcleo da classe¦Cœur du rôle¦Rollenkern
node.signature¦Característica (Z)¦Signature (Z)¦Signatur (Z)
node.ultimate¦Definitiva (R)¦Ultime (R)¦Ultimative (R)
node.signature_req¦Desbloqueada no estágio Mestre · 60 de mana · 120 s¦Débloquée au stade Maître · 60 de mana · 120 s¦Ab Stufe Meister · 60 Mana · 120 s
node.ultimate_req¦Desbloqueada no estágio Definitivo · 80 de mana · 180 s¦Débloquée au stade Ultime · 80 de mana · 180 s¦Ab Stufe Ultimativ · 80 Mana · 180 s
node.rank¦Grau %s/%s (+%s de evolução)¦Rang %s/%s (+%s d'évolution)¦Rang %s/%s (+%s Entwicklung)
node.cost¦%s de mana, %s s¦%s de mana, %s s¦%s Mana, %s s
elements.active¦Ativo¦Actif¦Aktiv
elements.mastered¦Dominado¦Maîtrisé¦Gemeistert
elements.available¦Clique para dominar¦Cliquer pour maîtriser¦Zum Meistern anklicken
soul_level¦Nível de alma¦Niveau d'âme¦Seelenstufe
xp_cost¦Custo (níveis de XP)¦Coût (niveaux d'XP)¦Kosten (EP-Stufen)
need_bonfire¦Suba de nível em uma Fogueira¦Montez de niveau à un feu de camp¦Steige an einem Leuchtfeuer auf
level_up¦Subir de nível¦Monter de niveau¦Stufe erhöhen
node.stat¦Treinamento¦Entraînement¦Training
node.upgrade¦Melhorar¦Améliorer¦Verbessern
skills_passives¦Habilidades e suas passivas (evoluem com o uso)¦Compétences et passifs (progression par l'usage)¦Fertigkeiten und passive Boni (steigen durch Nutzung)
passives¦Passivas:¦Passifs :¦Passive Boni:
affinity¦★ Afinidade de classe: +%s%%¦★ Affinité de rôle : +%s%%¦★ Rollenaffinität: +%s%%
node.path¦Caminho¦Voie¦Pfad
path.or¦ou¦ou¦oder
milestones¦Marcos:¦Paliers :¦Meilensteine:
milestone_line¦Nv. %s · %s: %s¦Niv. %s · %s : %s¦St. %s · %s: %s
equip¦Espaço %s¦Emplacement %s¦Platz %s
node.mechanic¦Mecânica da classe¦Mécanique du rôle¦Rollenmechanik
node.reactive¦Passiva reativa¦Passif réactif¦Reaktive passive Fähigkeit
node.needs¦Requer: %s¦Nécessite : %s¦Benötigt: %s
node.legend_taken¦Obtida por %s (lendárias exclusivas)¦Détenue par %s (légendaires exclusives)¦Vergeben an %s (exklusive legendäre Fähigkeiten)
roles.rebirths¦Trocas de classe: %s¦Changements de rôle : %s¦Rollenwechsel: %s
roles.need_ritual¦Trocar classe: ritual na Fogueira¦Changer de rôle : rituel au feu de camp¦Rolle wechseln: Leuchtfeuerritual
tree.hint¦Arrastar ou roda: mover · Ctrl+roda: zoom · C: centralizar¦Glisser ou molette : déplacer · Ctrl+molette : zoom · C : centrer¦Ziehen oder Mausrad: verschieben · Strg+Mausrad: Zoom · C: zentrieren
tree.hint_scroll¦Roda: mover · Ctrl+roda: zoom · PgUp/PgDn: janela¦Molette : déplacer · Ctrl+molette : zoom · Page préc./suiv. : fenêtre¦Mausrad: verschieben · Strg+Mausrad: Zoom · Bild auf/ab: Fenster
tree.section_veteran¦Veterano¦Vétéran¦Veteran
tree.section_ascent¦Ascensão¦Ascension¦Aufstieg
tree.section_mastery¦Maestria¦Maîtrise¦Meisterschaft
hud_config¦Ajustes do HUD¦Réglages de l'ATH¦HUD-Einstellungen
hud.scale¦Escala: %s%%¦Échelle : %s%%¦Skalierung: %s%%
hud.souls_bars¦Barras de estado¦Barres d'état¦Statusleisten
hud.mechanic¦Mecânica da classe¦Mécanique du rôle¦Rollenmechanik
hud.damage_numbers¦Números de dano¦Nombres de dégâts¦Schadenszahlen
hud.screen_effects¦Efeitos de tela¦Effets d'écran¦Bildschirmeffekte
hud.cast_animations¦Animações 3D¦Animations 3D¦3D-Animationen
hud.visible¦HUD visível¦ATH visible¦HUD sichtbar
hud.reset¦Redefinir¦Réinitialiser¦Zurücksetzen
hud.drag¦Arraste a barra de habilidades para movê-la¦Glissez la barre de capacités pour la déplacer¦Ziehe die Fähigkeitsleiste, um sie zu verschieben
tree.passives¦PASSIVAS¦PASSIFS¦PASSIVE
tree.paths¦CAMINHOS · ESCOLHA 1¦VOIES · CHOISIR 1¦PFADE · WÄHLE 1
tree.actives¦ATIVAS¦ACTIFS¦AKTIVE
node.legendary¦Lendária¦Légendaire¦Legendär
accessories¦Acessórios¦Accessoires¦Accessoires
accessories.ring¦Anéis¦Anneaux¦Ringe
accessories.amulet¦Amuleto¦Amulette¦Amulett
guide¦Grimório de Aptitudes¦Grimoire d'Aptitudes¦Aptitudes-Grimoire
guide.scroll¦Roda: %s / %s¦Molette : %s / %s¦Mausrad: %s / %s
guide.chapter¦Capítulo %s de %s¦Chapitre %s sur %s¦Kapitel %s von %s
guide.click_recipe¦Clique: receita e detalhes¦Clic : recette et détails¦Klick: Rezept und Details
guide.click_details¦Clique: detalhes¦Clic : détails¦Klick: Details
guide.recipes_count¦%s receitas¦%s recettes¦%s Rezepte
guide.search¦Buscar…¦Rechercher…¦Suchen…
guide.no_recipes_synced¦As receitas ainda não estão disponíveis. Entre em um mundo (ou servidor) com o mod para vê-las.¦Les recettes ne sont pas encore disponibles. Rejoignez un monde (ou serveur) avec le mod pour les voir.¦Rezepte sind noch nicht verfügbar. Betritt eine Welt (oder einen Server) mit dem Mod, um sie zu sehen.
guide.back¦‹ Voltar¦‹ Retour¦‹ Zurück
guide.no_recipe¦Não tem receita de fabricação: é obtido como saque, de elites e campeões, do aldeão da guilda ou ao evoluir sua classe (veja o capítulo correspondente).¦Cet objet n'a pas de recette : il s'obtient en butin, sur les élites et champions, auprès du villageois de guilde ou en faisant évoluer votre rôle (voir le chapitre associé).¦Kein Herstellungsrezept: Erhältlich als Beute, von Elitegegnern und Champions, beim Gildendorfbewohner oder durch Rollenentwicklung (siehe zugehöriges Kapitel).
guide.shapeless¦Fabricação (qualquer disposição)¦Fabrication (disposition libre)¦Herstellung (beliebige Anordnung)
guide.shaped¦Fabricação (formato exato)¦Fabrication (forme exacte)¦Herstellung (genaue Anordnung)
guide.crafting_table¦Bancada de trabalho¦Établi¦Werkbank
guide.guild_desk¦Mesa da guilda (vila da guilda)¦Table de guilde (village de guilde)¦Gildentisch (Gildendorf)
guide.used_in¦Usado em:¦Utilisé dans :¦Verwendet für:
elements.fusion¦%s (com %s)¦%s (avec %s)¦%s (mit %s)
node.weapon¦Arma de evolução¦Arme d'évolution¦Entwicklungswaffe
node.weapon_blade¦Lâmina de classe: com a classe ativa, cria um corte de sua cor, carrega seu recurso e (Definitivo) cura um pouco ao acertar.¦Lame de rôle : avec le rôle actif, produit un balayage de sa couleur, charge sa ressource et (Ultime) soigne légèrement à l'impact.¦Rollenklinge: Mit aktiver Rolle erzeugt sie einen farbigen Schwung, lädt die Ressource und heilt (Ultimativ) bei Treffern leicht.
node.weapon_focus¦Foco de classe: disparo instantâneo da cor da classe que custa mana; o dano aumenta com o nível da classe.¦Focaliseur de rôle : tir instantané de la couleur du rôle coûtant du mana ; dégâts accrus avec le niveau du rôle.¦Rollenfokus: Sofortschuss in der Rollenfarbe, der Mana kostet; mehr Schaden mit der Rollenstufe.
node.weapon_master_req¦Obtida ao evoluir para Mestre¦Obtenue en évoluant au stade Maître¦Erhalten bei der Entwicklung zum Meister
node.weapon_ultimate_req¦Obtida ao evoluir para Definitivo¦Obtenue en évoluant au stade Ultime¦Erhalten bei der Entwicklung zu Ultimativ
weapon.claim¦Resgatar¦Réclamer¦Abholen
hud.eclipse_sky¦Céu do Eclipse¦Ciel de l'Éclipse¦Finsternishimmel
tab.config¦⚙¦⚙¦⚙
config¦Ajustes do Aptitudes¦Réglages d'Aptitudes¦Aptitudes-Einstellungen
config.search¦Buscar opção…¦Rechercher une option…¦Option suchen…
config.client¦HUD e efeitos…¦ATH et effets…¦HUD und Effekte…
config.local¦Valores locais¦Valeurs locales¦Lokale Werte
config.editable¦Servidor · você pode editar¦Serveur · modification autorisée¦Server · bearbeitbar
config.readonly¦Servidor · somente leitura¦Serveur · lecture seule¦Server · schreibgeschützt
config.none¦Nenhuma opção encontrada.¦Aucune option correspondante.¦Keine passende Option.
config.hint¦✔ ou Enter aplica o valor. Cada opção está descrita em docs/CONFIGURATION.md¦✔ ou Entrée applique la valeur. Chaque option est décrite dans docs/CONFIGURATION.md¦✔ oder Eingabe übernimmt den Wert. Alle Optionen stehen in docs/CONFIGURATION.md
hud.tutorial¦Tutorial¦Tutoriel¦Tutorial
""")

add('aptitudes.cmd.', """
unknown_role¦Classe desconhecida: %s¦Rôle inconnu : %s¦Unbekannte Rolle: %s
roles_header¦— Classes de %s —¦— Rôles de %s —¦— Rollen von %s —
role_line¦%s: nível %s (%s/%s XP), %s ponto(s) livre(s) %s¦%s : niveau %s (%s/%s XP), %s point(s) libre(s) %s¦%s: Stufe %s (%s/%s EP), %s freie(r) Punkt(e) %s
primary_set¦%s agora é sua classe principal.¦%s est désormais votre rôle principal.¦%s ist jetzt deine Hauptrolle.
secondary_set¦%s agora é sua classe secundária.¦%s est désormais votre rôle secondaire.¦%s ist jetzt deine Nebenrolle.
role_same¦Essa classe já ocupa esse espaço.¦Ce rôle occupe déjà cet emplacement.¦Diese Rolle belegt diesen Platz bereits.
learned¦%s melhorado para o grau %s.¦%s amélioré au rang %s.¦%s auf Rang %s verbessert.
cooldown_cleared¦Recarga de troca de classe removida para %s.¦Délai de changement de rôle effacé pour %s.¦Rollenwechsel-Abklingzeit für %s aufgehoben.
settier¦%s de %s definido como %s.¦%s de %s défini à %s.¦%s von %s auf %s gesetzt.
unknown_unique¦Habilidade única desconhecida: %s¦Capacité unique inconnue : %s¦Unbekannte einzigartige Fähigkeit: %s
unique_header¦— Habilidades únicas —¦— Capacités uniques —¦— Einzigartige Fähigkeiten —
unique_line¦%s: %s¦%s : %s¦%s: %s
unique_revoked¦%s está disponível novamente.¦%s est de nouveau disponible.¦%s ist wieder verfügbar.
""")

add('key.aptitudes.', """
cycle_element¦Alternar elemento (Elementalista)¦Changer d'élément (Élémentaliste)¦Element wechseln (Elementarist)
ability_5¦Habilidade definitiva¦Capacité ultime¦Ultimative Fähigkeit
ability_6¦Habilidade única¦Capacité unique¦Einzigartige Fähigkeit
ability_7¦Habilidade característica¦Capacité signature¦Signaturfähigkeit
uniques¦Abrir habilidades únicas¦Ouvrir les capacités uniques¦Einzigartige Fähigkeiten öffnen
toggle_hud¦Alternar HUD (Shift: ajustes)¦Afficher/masquer l'ATH (Maj : réglages)¦HUD umschalten (Umschalt: Einstellungen)
accessories¦Acessórios¦Accessoires¦Accessoires
party¦Abrir menu do grupo¦Ouvrir le menu de groupe¦Gruppenmenü öffnen
""")

add('attribute.aptitudes.', """
vigor¦Vigor¦Vigueur¦Vitalität
vigor.desc¦Aumenta a vida máxima.¦Augmente la santé maximale.¦Erhöht das maximale Leben.
mind¦Mente¦Esprit¦Geist
mind.desc¦Aumenta o mana máximo e sua regeneração.¦Augmente le mana maximal et sa régénération.¦Erhöht das maximale Mana und seine Regeneration.
intelligence¦Inteligência¦Intelligence¦Intelligenz
intelligence.desc¦Aumenta o dano de feitiços, explosões e habilidades à distância.¦Augmente les dégâts des sorts, explosions et capacités à distance.¦Erhöht den Schaden von Zaubern, Explosionen und Fernkampffähigkeiten.
faith¦Fé¦Foi¦Glaube
faith.desc¦Reduz a recarga de todas as suas habilidades.¦Réduit le temps de recharge de toutes vos capacités.¦Verringert die Abklingzeit aller deiner Fähigkeiten.
""")

add('category.aptitudes.', """
martial¦Marcial¦Martial¦Kriegerisch
martial.short¦MAR¦MAR¦KRI
martial.desc¦Combate corpo a corpo: Berserker, Lanceiro, Espadachim e Monge.¦Corps à corps : Berserker, Lancier, Épéiste et Moine.¦Nahkampf: Berserker, Lanzer, Schwertkämpfer und Mönch.
arcane¦Arcana¦Arcanique¦Arkan
arcane.short¦ARC¦ARC¦ARK
arcane.desc¦Magia e poderes sombrios: Mago, Necromante e Elementalista.¦Magie et pouvoirs obscurs : Mage, Nécromancien et Élémentaliste.¦Magie und dunkle Kräfte: Magier, Nekromant und Elementarist.
shadow¦Sombras¦Ombre¦Schatten
shadow.short¦SOM¦OMB¦SCH
shadow.desc¦Furtividade e engano: Assassino e Metamorfo.¦Furtivité et tromperie : Assassin et Métamorphe.¦Heimlichkeit und Täuschung: Assassine und Gestaltwandler.
guardian¦Guardiões¦Gardiens¦Wächter
guardian.short¦GUA¦GAR¦WÄC
guardian.desc¦Proteção e vida: Herborista, Paladino e Druida.¦Protection et vie : Herboriste, Paladin et Druide.¦Schutz und Leben: Kräuterkundiger, Paladin und Druide.
craft¦Ofícios¦Artisanat¦Handwerk
craft.short¦OFI¦ART¦HAN
craft.desc¦Engenhosidade e comércio: Engenheiro e Mercador.¦Ingéniosité et commerce : Ingénieur et Marchand.¦Erfindungsgeist und Handel: Ingenieur und Händler.
""")

# Los nombres de las piezas comparten el mismo tema de cada conjunto.
ARMOR_THEMES = {
    'berserker': ('da Fúria', 'de la fureur', 'der Raserei'),
    'mage': ('do Arquimago', 'de l’archimage', 'des Erzmagiers'),
    'assassin': ('das Sombras', 'des ombres', 'der Schatten'),
    'herbalist': ('do Boticário', 'd’apothicaire', 'des Apothekers'),
    'engineer': ('do Exoesqueleto de Cobre', 'd’exoarmure en cuivre', 'des Kupferexoskeletts'),
    'lancer': ('do Dragoneiro', 'de dragon', 'des Dragoners'),
    'swordsman': ('do Duelista', 'du duelliste', 'des Duellanten'),
    'paladin': ('da Consagração', 'de la consécration', 'der Weihe'),
    'necromancer': ('do Ossuário', 'de l’ossuaire', 'des Beinhauses'),
    'druid': ('de Casca', 'd’écorce', 'der Borke'),
    'merchant': ('da Guilda', 'de la guilde', 'der Gilde'),
    'monk': ('da Serenidade', 'de la sérénité', 'der Gelassenheit'),
    'elementalist': ('dos Elementos', 'des éléments', 'der Elemente'),
    'shapeshifter': ('da Metamorfose', 'de métamorphose', 'des Wandels'),
    'gunslinger': ('do Pistoleiro', 'du pistolero', 'des Revolverhelden'),
    'weapon_summoner': ('do Arsenal', 'de l’arsenal', 'des Arsenals'),
    'hunter': ('do Caçador', 'du chasseur', 'des Jägers'),
    'bard': ('do Trovador', 'du troubadour', 'des Troubadours'),
    'warlock': ('da Maldição', 'de la malédiction', 'des Fluchs'),
    'tamer': ('do Domador', 'du dompteur', 'des Tierbändigers'),
}
for role, suffixes in ARMOR_THEMES.items():
    for piece, names in {
        'helmet': ('Elmo', 'Casque', 'Helm'), 'chestplate': ('Peitoral', 'Plastron', 'Brustpanzer'),
        'leggings': ('Calças', 'Jambières', 'Beinschutz'), 'boots': ('Botas', 'Bottes', 'Stiefel'),
    }.items():
        TRANSLATIONS[f'item.aptitudes.{role}_{piece}'] = tuple(f'{name} {suffix}' for name, suffix in zip(names, suffixes, strict=True))

add('item.aptitudes.', """
berserker_axe¦Machado do Berserker¦Hache de berserker¦Berserkeraxt
arcane_staff¦Cajado Arcano¦Bâton arcanique¦Arkaner Stab
dagger¦Adaga¦Dague¦Dolch
herb_pouch¦Saquinho de Ervas¦Sachet d'herbes¦Kräuterbeutel
wrench¦Chave Inglesa¦Clé à molette¦Schraubenschlüssel
plasma_gun¦Arma de Plasma¦Fusil à plasma¦Plasmagewehr
spear¦Lança¦Lance¦Speer
katana¦Katana¦Katana¦Katana
hitscan.tooltip¦Clique direito: disparo instantâneo (%s de mana). Melhora com o nível de %s.¦Clic droit : tir instantané (%s de mana). Évolue avec le niveau de %s.¦Rechtsklick: Sofortschuss (%s Mana). Skaliert mit der Stufe von %s.
herb_pouch.tooltip¦Clique direito: cura e remove veneno. Melhora com o nível de Herborista.¦Clic droit : soigne et dissipe le poison. Évolue avec le niveau d'Herboriste.¦Rechtsklick: Heilt und entfernt Gift. Skaliert mit der Kräuterkundigenstufe.
wrench.tooltip¦Clique direito em um bloco direcional: gira 90°.¦Clic droit sur un bloc orientable : le tourne de 90°.¦Rechtsklick auf einen ausrichtbaren Block: Dreht ihn um 90°.
holy_hammer¦Martelo Sagrado¦Marteau sacré¦Heiliger Hammer
necro_tome¦Tomo Necromântico¦Grimoire nécromantique¦Nekromantischer Foliant
oak_staff¦Cajado de Carvalho¦Bâton de chêne¦Eichenstab
guild_seal¦Selo da Guilda¦Sceau de guilde¦Gildensiegel
hand_wraps¦Bandagens de Combate¦Bandages de combat¦Handbandagen
elemental_orb¦Orbe Elemental¦Orbe élémentaire¦Elementarkugel
oak_staff.tooltip¦Clique direito em um bloco: fertiliza uma área de 3×3 (%s de mana).¦Clic droit sur un bloc : fertilise une zone de 3×3 (%s de mana).¦Rechtsklick auf einen Block: Düngt einen Bereich von 3×3 (%s Mana).
guild_seal.tooltip¦Clique direito em um aldeão: reabastece as trocas (somente Mercador, recarga de 5 min).¦Clic droit sur un villageois : réapprovisionne ses échanges (Marchand uniquement, recharge de 5 min).¦Rechtsklick auf einen Dorfbewohner: Füllt seine Angebote auf (nur Händler, 5 Min. Abklingzeit).
guild_seal.not_merchant¦Somente um Mercador ativo pode usar o selo.¦Seul un Marchand actif peut utiliser le sceau.¦Nur ein aktiver Händler kann das Siegel nutzen.
elemental_orb.tooltip¦Clique direito: alterna para o próximo elemento dominado (como G).¦Clic droit : passe à l'élément maîtrisé suivant (comme G).¦Rechtsklick: Wechselt zum nächsten gemeisterten Element (wie G).
mastery_emblem¦Emblema de Maestria¦Emblème de maîtrise¦Meisterschaftsemblem
ultimate_core¦Núcleo Definitivo¦Noyau ultime¦Ultimativer Kern
mimic_mask¦Máscara de Mil Faces¦Masque aux mille visages¦Maske der tausend Gesichter
mimic_mask.tooltip¦Clique direito: transforma / retorna à forma humana (Metamorfo).¦Clic droit : transformation / retour à la forme humaine (Métamorphe).¦Rechtsklick: Verwandeln / menschliche Form annehmen (Gestaltwandler).
mimic_mask.not_shapeshifter¦Você precisa ter a classe Metamorfo ativa.¦Le rôle de Métamorphe doit être actif.¦Du musst die Rolle Gestaltwandler aktiviert haben.
armor.role¦Conjunto de %s¦Ensemble de %s¦%s-Set
armor.set_header¦Conjunto completo (4 peças):¦Ensemble complet (4 pièces) :¦Vollständiges Set (4 Teile):
armor.requires¦Requer a classe como principal ou secundária¦Nécessite le rôle comme principal ou secondaire¦Benötigt die Rolle als Haupt- oder Nebenrolle
soul_shard¦Fragmento de Alma¦Fragment d'âme¦Seelensplitter
estus_flask¦Frasco de Estus¦Fiole d'Estus¦Estusflakon
ether_flask¦Frasco de Éter¦Fiole d'éther¦Ätherflakon
estus_flask.tooltip¦Cura 8 de vida e concede Regeneração breve¦Rend 8 points de santé et une brève Régénération¦Heilt 8 Leben und gewährt kurz Regeneration
ether_flask.tooltip¦Restaura 40%% de mana¦Restaure 40%% de mana¦Stellt 40%% Mana wieder her
flask.charges¦Cargas: %s/%s¦Charges : %s/%s¦Ladungen: %s/%s
flask.refill¦Descanse em uma Fogueira para reabastecer¦Reposez-vous à un feu de camp pour remplir¦Raste an einem Leuchtfeuer zum Auffüllen
flask.empty¦O frasco está vazio. Descanse em uma Fogueira.¦La fiole est vide. Reposez-vous à un feu de camp.¦Der Flakon ist leer. Raste an einem Leuchtfeuer.
soul_pickaxe¦Picareta de Alma¦Pioche d'âme¦Seelenspitzhacke
soul_shovel¦Pá de Alma¦Pelle d'âme¦Seelenschaufel
soul_axe¦Machado de Alma¦Hache d'âme¦Seelenaxt
soul_hoe¦Enxada de Alma¦Houe d'âme¦Seelenhacke
soul_pickaxe.tooltip¦Agachado: minera uma área de 3×3¦Accroupi : mine une zone de 3×3¦Schleichen: Baut einen Bereich von 3×3 ab
soul_shovel.tooltip¦Agachado: escava uma área de 3×3¦Accroupi : creuse une zone de 3×3¦Schleichen: Gräbt einen Bereich von 3×3 aus
soul_axe.tooltip¦Agachado: derruba a árvore inteira¦Accroupi : abat l'arbre entier¦Schleichen: Fällt den gesamten Baum
soul_hoe.tooltip¦Colhe e replanta plantações maduras em 3×3¦Récolte et replante les cultures mûres sur 3×3¦Erntet und pflanzt reife Feldfrüchte auf 3×3 neu
rebirth_offering¦Oferenda de Renovação¦Offrande de renouveau¦Opfergabe der Erneuerung
rebirth_offering.tooltip¦Queime-a em uma Fogueira (clique direito) para trocar uma de suas classes.¦Brûlez-la à un feu de camp (clic droit) pour changer un de vos rôles.¦Verbrenne sie an einem Leuchtfeuer (Rechtsklick), um eine deiner Rollen zu wechseln.
arcane_revolver¦Revólver Arcano¦Revolver arcanique¦Arkaner Revolver
weapon_sigil¦Sigilo de Armas¦Sceau d'armes¦Waffensigil
hunting_knife¦Faca de Caça¦Couteau de chasse¦Jagdmesser
lute¦Alaúde¦Luth¦Laute
lute.tooltip¦Clique direito: aliados próximos ganham Velocidade. Carrega Harmonia.¦Clic droit : les alliés proches gagnent Vitesse. Charge l'Harmonie.¦Rechtsklick: Nahe Verbündete erhalten Schnelligkeit. Lädt Harmonie auf.
pact_talisman¦Talismã Amaldiçoado¦Talisman maudit¦Verfluchter Talisman
beast_whistle¦Apito de Feras¦Sifflet des bêtes¦Bestienpfeife
beast_whistle.tooltip¦Clique direito: chama, cura e acelera seus animais.¦Clic droit : appelle, soigne et accélère vos animaux.¦Rechtsklick: Ruft und heilt deine Tiere und beschleunigt sie.
armory_voucher¦Vale de Arsenal¦Bon d'armurerie¦Rüstkammergutschein
armory_voucher.tooltip¦Use para receber uma peça do conjunto da sua classe principal.¦Utilisez-le pour recevoir une pièce de l'armure de votre rôle principal.¦Benutze ihn, um ein Rüstungsteil deiner Hauptrolle zu erhalten.
armory_voucher.no_role¦Você precisa de uma classe principal para resgatar o vale.¦Vous devez avoir un rôle principal pour échanger ce bon.¦Du benötigst eine Hauptrolle, um den Gutschein einzulösen.
armory_voucher.received¦Você recebe: %s¦Vous recevez : %s¦Du erhältst: %s
accessory.ring¦Anel¦Anneau¦Ring
accessory.amulet¦Amuleto¦Amulette¦Amulett
accessory.hint¦Equipe com a tecla Y (Acessórios).¦Équipez-le avec la touche Y (Accessoires).¦Mit Taste Y ausrüsten (Accessoires).
elite_essence¦Essência de Elite¦Essence d'élite¦Eliteessenz
champion_heart¦Coração de Campeão¦Cœur de champion¦Championsherz
faceless_colossus_seal¦Selo do Colosso Sem Rosto¦Sceau du Colosse sans Visage¦Siegel des Gesichtslosen Kolosses
faceless_colossus_seal.tooltip¦Invoca o Colosso Sem Rosto. Custa 4 Fragmentos de alma e um Coração de campeão. Fica disponível de novo 20 minutos após a última derrota.¦Invoque le Colosse sans Visage. Coûte 4 Fragments d'âme et un Cœur de champion. Disponible à nouveau 20 minutes après sa dernière défaite.¦Beschwört den Gesichtslosen Koloss. Kostet 4 Seelensplitter und ein Champion-Herz. Wieder verfügbar 20 Minuten nach der letzten Niederlage.
dismantled_guardian_seal¦Selo do Guardião Desmontado¦Sceau du Gardien Démantelé¦Siegel des Zerlegten Wächters
dismantled_guardian_seal.tooltip¦Invoca o Guardião Desmontado. Custa 4 Fragmentos de alma e uma Essência de elite. Fica disponível de novo 20 minutos após a última derrota.¦Invoque le Gardien démantelé. Coûte 4 Fragments d'âme et une Essence d'élite. Disponible à nouveau 20 minutes après sa dernière défaite.¦Beschwört den Zerlegten Wächter. Kostet 4 Seelensplitter und eine Elitenessenz. Wieder verfügbar 20 Minuten nach der letzten Niederlage.
ancient_automaton_seal¦Selo do Autômato Antigo¦Sceau de l'Automate Ancien¦Siegel des Uralten Automaten
ancient_automaton_seal.tooltip¦Invoca o Autômato Antigo. Custa 4 Fragmentos de alma e uma Essência de elite. Fica disponível de novo 20 minutos após a última derrota.¦Invoque l'Automate ancien. Coûte 4 Fragments d'âme et une Essence d'élite. Disponible à nouveau 20 minutes après sa dernière défaite.¦Beschwört den Uralten Automaten. Kostet 4 Seelensplitter und eine Elitenessenz. Wieder verfügbar 20 Minuten nach der letzten Niederlage.
ring_of_strength¦Anel da Força¦Anneau de force¦Ring der Stärke
ring_of_strength.desc¦+1.5 de dano¦+1.5 dégâts¦+1.5 Schaden
ring_of_vitality¦Anel da Vitalidade¦Anneau de vitalité¦Ring der Vitalität
ring_of_vitality.desc¦+4 de vida máxima¦+4 de santé maximale¦+4 maximales Leben
ring_of_swiftness¦Anel da Agilidade¦Anneau de vélocité¦Ring der Flinkheit
ring_of_swiftness.desc¦+8%% de velocidade¦+8%% de vitesse¦+8%% Geschwindigkeit
ring_of_mana¦Anel do Mana¦Anneau de mana¦Ring des Manas
ring_of_mana.desc¦+30 de mana máximo¦+30 de mana maximal¦+30 maximales Mana
ring_of_the_vampire¦Anel do Vampiro¦Anneau du vampire¦Ring des Vampirs
ring_of_the_vampire.desc¦5%% de roubo de vida¦5%% de vol de vie¦5%% Lebensraub
ring_of_fortune¦Anel da Fortuna¦Anneau de fortune¦Ring des Glücks
ring_of_fortune.desc¦+2 de sorte¦+2 de chance¦+2 Glück
amulet_of_the_guardian¦Amuleto do Guardião¦Amulette du gardien¦Amulett des Wächters
amulet_of_the_guardian.desc¦+3 de armadura¦+3 d'armure¦+3 Rüstung
amulet_of_the_phoenix¦Amuleto da Fênix¦Amulette du phénix¦Amulett des Phönix
amulet_of_the_phoenix.desc¦Salva da morte com metade da vida (a cada 10 min)¦Vous sauve de la mort avec la moitié de votre santé (toutes les 10 min)¦Rettet dich vor dem Tod mit halbem Leben (alle 10 Min.)
arcane_amulet¦Amuleto Arcano¦Amulette arcanique¦Arkanes Amulett
arcane_amulet.desc¦−10%% de recarga das habilidades¦−10%% de recharge des capacités¦−10%% Fähigkeitsabklingzeit
amulet_of_the_grove¦Amuleto do Bosque¦Amulette du bosquet¦Amulett des Hains
amulet_of_the_grove.desc¦Regenera 1 de vida por segundo após 5 s sem receber dano¦Régénère 1 point de santé par seconde après 5 s sans subir de dégâts¦Regeneriert 1 Leben pro Sekunde nach 5 s ohne erlittenen Schaden
hunters_trophy¦Troféu do Caçador¦Trophée du chasseur¦Jagdtrophäe
hunters_trophy.desc¦+25%% de dano contra elites e campeões¦+25%% de dégâts contre les élites et champions¦+25%% Schaden gegen Elitegegner und Champions
guide_book¦Grimório de Aptitudes¦Grimoire d'Aptitudes¦Aptitudes-Grimoire
guide_book.tooltip¦Clique direito: guia do mod (Fogueira, classes, receitas, saque…).¦Clic droit : guide du mod (feu de camp, rôles, recettes, butin…).¦Rechtsklick: Mod-Anleitung (Leuchtfeuer, Rollen, Rezepte, Beute…).
role_weapon.role¦Arma de classe: %s¦Arme de rôle : %s¦Rollenwaffe: %s
role_weapon.blade_master¦Com a classe ativa: corte colorido e +3 de recurso por golpe.¦Avec le rôle actif : balayage coloré et +3 de ressource par coup.¦Mit aktiver Rolle: farbiger Schwung und +3 Ressource pro Treffer.
role_weapon.blade_ultimate¦Com a classe ativa: corte colorido, +3 de recurso e cura de 0.5 por golpe.¦Avec le rôle actif : balayage coloré, +3 de ressource et soin de 0.5 par coup.¦Mit aktiver Rolle: farbiger Schwung, +3 Ressource und 0.5 Heilung pro Treffer.
role_weapon.focus¦Clique direito: disparo de %s de dano (%s de mana). Requer a classe ativa.¦Clic droit : tir à %s dégâts (%s de mana). Nécessite le rôle actif.¦Rechtsklick: Schuss mit %s Schaden (%s Mana). Erfordert die aktive Rolle.
role_weapon.needs_role¦Você precisa de %s como classe principal ou secundária.¦%s doit être votre rôle principal ou secondaire.¦Du benötigst %s als Haupt- oder Nebenrolle.
rune_flame¦Runa da Chama¦Rune de flamme¦Flammenrune
rune_leech¦Runa de Roubo de Vida¦Rune de vol de vie¦Lebensraubrune
rune_swift¦Runa da Agilidade¦Rune de vélocité¦Flinkheitsrune
rune_force¦Runa da Força¦Rune de force¦Kraftrune
rune.flame¦Chama¦Flamme¦Flamme
rune.leech¦Roubo de vida¦Vol de vie¦Lebensraub
rune.swift¦Agilidade¦Vélocité¦Flinkheit
rune.force¦Força¦Force¦Kraft
rune.flame.desc¦Ao acertar: queima o alvo por %s s.¦À l'impact : brûle la cible pendant %s s.¦Bei Treffer: Verbrennt das Ziel für %s s.
rune.leech.desc¦Ao acertar: restaura %s de vida (%s s de recarga).¦À l'impact : rend %s de santé (recharge de %s s).¦Bei Treffer: Stellt %s Leben wieder her (%s s Abklingzeit).
rune.swift.desc¦+%s%% de velocidade de ataque; focos: %s ticks a menos de recarga.¦+%s%% de vitesse d'attaque ; focaliseurs : %s ticks de recharge en moins.¦+%s%% Angriffsgeschwindigkeit; Fokusse: %s Ticks kürzere Abklingzeit.
rune.force.desc¦+%s de dano.¦+%s dégâts.¦+%s Schaden.
tower_core¦Núcleo do Sentinela da Torre¦Noyau de la Sentinelle de la tour¦Kern des Turmwächters
shrine_sigil¦Sigilo do Oráculo do Santuário¦Sceau de l'Oracle du sanctuaire¦Siegel des Schreinorakels
warlord_medallion¦Medalhão do Campeão do Acampamento¦Médaillon du Champion du camp¦Medaillon des Lagerchampions
eclipse_fragment¦Fragmento do Eclipse¦Fragment d'éclipse¦Finsternissplitter
eclipse_core¦Núcleo do Eclipse¦Noyau d'éclipse¦Finsterniskern
rune_eclipse¦Runa do Eclipse¦Rune d'éclipse¦Finsternisrune
rune.eclipse¦Eclipse¦Éclipse¦Finsternis
rune.eclipse.desc¦+%s%% de dano contra elites, campeões e chefes.¦+%s%% de dégâts contre les élites, champions et boss.¦+%s%% Schaden gegen Elitegegner, Champions und Bosse.
rune_frost¦Runa da Geada¦Rune de givre¦Frostrune
rune_thunder¦Runa do Trovão¦Rune de tonnerre¦Donnerrune
rune_guard¦Runa da Guarda¦Rune de garde¦Wachtrune
rune.frost¦Geada¦Givre¦Frost
rune.thunder¦Trovão¦Tonnerre¦Donner
rune.guard¦Guarda¦Garde¦Wache
rune.frost.desc¦Ao acertar: Lentidão %s por %s s.¦À l'impact : Lenteur %s pendant %s s.¦Bei Treffer: Langsamkeit %s für %s s.
rune.thunder.desc¦Ao acertar: %s%% de chance de uma descarga atingir %s monstros próximos (%s de dano; %s s de recarga).¦À l'impact : %s%% de chance qu'une décharge saute sur %s monstres proches (%s dégâts ; recharge de %s s).¦Bei Treffer: %s%% Chance auf eine Entladung, die auf %s nahe Monster überspringt (%s Schaden; %s s Abklingzeit).
rune.guard.desc¦Ao acertar: +%s de absorção (máx. %s; %s s de recarga).¦À l'impact : +%s d'absorption (max %s ; recharge de %s s).¦Bei Treffer: +%s Absorption (max. %s; %s s Abklingzeit).
herb_petals¦Pétalas de Ervas¦Pétales d'herbes¦Kräuterblütenblätter
""")

for suffix, names in {
    'mana': ('Mana', 'mana', 'Manas'),
    'frenzy': ('Frenesi', 'frénésie', 'Raserei'),
    'eclipse_veil': ('Véu do Eclipse', 'voile de l’éclipse', 'Finsternisschleiers'),
    'forest_vigor': ('Vigor da Floresta', 'vigueur sylvestre', 'Waldkraft'),
}.items():
    for kind, prefixes in {
        'potion': ('Poção de ', 'Potion de ', 'Trank '),
        'splash_potion': ('Poção Arremessável de ', 'Potion jetable de ', 'Wurftrank '),
        'lingering_potion': ('Poção Prolongada de ', 'Potion persistante de ', 'Verweiltrank '),
        'tipped_arrow': ('Flecha de ', 'Flèche de ', 'Pfeil '),
    }.items():
        german_article = 'des ' if suffix in ('mana', 'eclipse_veil') else 'der '
        portuguese = prefixes[0] + names[0]
        french = prefixes[1] + names[1]
        if suffix == 'eclipse_veil':
            portuguese = prefixes[0].removesuffix('de ') + 'do ' + names[0]
            french = prefixes[1].removesuffix('de ') + 'du ' + names[1]
        TRANSLATIONS[f'item.minecraft.{kind}.effect.aptitudes_{suffix}'] = (
            portuguese, french, prefixes[2] + german_article + names[2])

add('role.aptitudes.', """
berserker¦Berserker¦Berserker¦Berserker
berserker.desc¦Guerreiro de machado que se torna mais letal quanto mais ferido está. XP: acertar com machados.¦Guerrier à la hache, d'autant plus meurtrier qu'il est blessé. XP : frapper avec des haches.¦Axtkrieger, der mit seinen Verletzungen gefährlicher wird. EP: Treffer mit Äxten.
mage¦Arcanista¦Arcaniste¦Arkanist
mage.desc¦Magia arcana e elemental: runas, distorção do espaço e do tempo, escudos e projéteis arcanos, além de fogo, raio e gelo.¦Magie arcanique et élémentaire : runes, distorsion de l'espace et du temps, boucliers et projectiles arcaniques, plus feu, foudre et givre.¦Arkane und elementare Magie: Runen, Raum- und Zeitverzerrung, Schilde und arkane Geschosse, dazu Feuer, Blitz und Frost.
assassin¦Assassino¦Assassin¦Assassine
assassin.desc¦Furtividade, invisibilidade e ataques pelas costas. XP: acertar com a adaga.¦Furtivité, invisibilité et attaques dans le dos. XP : frapper avec la dague.¦Heimlichkeit, Unsichtbarkeit und Angriffe in den Rücken. EP: Treffer mit dem Dolch.
herbalist¦Boticário¦Apothicaire¦Apotheker
herbalist.desc¦Cura, regeneração e venenos. XP: colher plantações, flores e bagas; usar ervas.¦Soins, régénération et poisons. XP : récolter cultures, fleurs et baies ; utiliser des herbes.¦Heilung, Regeneration und Gifte. EP: Pflanzen, Blumen und Beeren ernten; Kräuter nutzen.
lancer¦Lanceiro¦Lancier¦Lanzer
lancer.desc¦Alcance e investidas devastadoras. XP: acertar com lança ou tridente.¦Allonge et charges dévastatrices. XP : frapper avec une lance ou un trident.¦Reichweite und verheerende Anstürme. EP: Treffer mit Speer oder Dreizack.
swordsman¦Duelista¦Duelliste¦Duellant
swordsman.desc¦Mestre da espada: lâmina, aparos e contra-ataques. XP: acertar com espadas.¦Maître de l'épée : tranchant, parades et ripostes. XP : frapper avec des épées.¦Schwertmeister: Klingen, Paraden und Gegenangriffe. EP: Treffer mit Schwertern.
engineer¦Engenheiro¦Ingénieur¦Ingenieur
engineer.desc¦Tecnologia prática e avançada: alcance, blindagem, baterias, propulsores e torretas. XP: redstone, máquinas, chave inglesa e arma de plasma.¦Technologie pratique et avancée : portée, blindage, batteries, propulseurs et tourelles. XP : redstone, machines, clé à molette et fusil à plasma.¦Praktische und fortschrittliche Technik: Reichweite, Panzerung, Batterien, Antriebe und Geschütztürme. EP: Redstone, Maschinen, Schraubenschlüssel und Plasmagewehr.
paladin¦Paladino¦Paladin¦Paladin
paladin.desc¦Guerreiro sagrado: pune mortos-vivos e protege aliados. XP: martelo sagrado, dano a mortos-vivos, bloqueio com escudo.¦Guerrier sacré : châtie les morts-vivants et protège les alliés. XP : marteau sacré, dégâts aux morts-vivants, blocages au bouclier.¦Heiliger Krieger: Bestraft Untote und schützt Verbündete. EP: Heiliger Hammer, Schaden gegen Untote, Blocken mit Schild.
necromancer¦Necromante¦Nécromancien¦Nekromant
necromancer.desc¦Invoca servos aliados que crescem com seu nível e drena vida. A evolução libera artes de sangue: pagar habilidades com vida, causar sangramento e, como Soberano Carmesim, evitar a morte. XP: tomo, drenagem e dano dos servos.¦Invoque des serviteurs alliés qui progressent avec votre niveau et vole la vie. L'évolution ouvre les arts du sang : payer les capacités en santé, faire saigner et, en Souverain carmin, tromper la mort. XP : grimoire, drain et dégâts des serviteurs.¦Beschwört verbündete Diener, die mit deiner Stufe wachsen, und entzieht Leben. Entwicklung schaltet Blutkünste frei: Fähigkeiten mit Leben bezahlen, Blutungen verursachen und als Purpurfürst dem Tod entgehen. EP: Foliant, Lebensentzug und Dienerschaden.
druid¦Druida¦Druide¦Druide
druid.desc¦Guardião da floresta: crescimento, raízes e forma de urso. XP: plantar mudas, reproduzir animais, usar o cajado.¦Gardien des forêts : croissance, racines et forme d'ours. XP : planter des pousses, élever des animaux, utiliser le bâton.¦Waldhüter: Wachstum, Wurzeln und Bärengestalt. EP: Setzlinge pflanzen, Tiere züchten, den Stab nutzen.
merchant¦Mercador tático¦Marchand tactique¦Taktischer Händler
merchant.desc¦Comércio e astúcia: descontos, lucro extra e subornos. XP: negociar com aldeões.¦Commerce et ruse : remises, profits supplémentaires et pots-de-vin. XP : commercer avec les villageois.¦Handel und List: Rabatte, Zusatzgewinne und Bestechung. EP: Handel mit Dorfbewohnern.
monk¦Monge¦Moine¦Mönch
monk.desc¦Artes marciais desarmadas: punhos de ferro, esquiva e meditação. XP: acertar com as mãos vazias.¦Arts martiaux à mains nues : poings de fer, esquive et méditation. XP : frapper à mains nues.¦Unbewaffnete Kampfkunst: Eisenfäuste, Ausweichen und Meditation. EP: Treffer mit bloßen Händen.
elementalist¦Xamã¦Chaman¦Schamane
elementalist.desc¦Domina até 3 de 9 elementos (nos níveis 1, 10 e 20). As habilidades mudam com o elemento ativo (tecla G).¦Maîtrise jusqu'à 3 éléments sur 9 (aux niveaux 1, 10 et 20). Les capacités changent avec l'élément actif (touche G).¦Meistert bis zu 3 von 9 Elementen (auf Stufe 1, 10 und 20). Fähigkeiten ändern sich mit dem aktiven Element (Taste G).
shapeshifter¦Metamorfo¦Métamorphe¦Gestaltwandler
shapeshifter.desc¦Transforma-se em criaturas (desbloqueadas ao derrotá-las) e usa seus poderes. Outros jogadores veem você como a criatura. XP: usar transformações e poderes.¦Se transforme en créatures (débloquées en les vainquant) et utilise leurs pouvoirs. Les autres joueurs voient la créature. XP : transformations et pouvoirs.¦Verwandelt sich in Kreaturen (durch Besiegen freigeschaltet) und nutzt deren Kräfte. Andere Spieler sehen dich als diese Kreatur. EP: Verwandlungen und Kräfte nutzen.
gunslinger¦Mago Pistoleiro¦Mage pistolero¦Revolvermagier
gunslinger.desc¦Revólver arcano, disparos em cadeia e balas explosivas. XP: disparar o Revólver Arcano e usar suas habilidades.¦Revolver arcanique, tirs en chaîne et balles explosives. XP : tirer au Revolver arcanique et utiliser ses capacités.¦Arkaner Revolver, Kettenschüsse und explosive Geschosse. EP: Schüsse mit dem Arkanen Revolver und Fähigkeiten.
weapon_summoner¦Invocador de Armas¦Invocateur d'armes¦Waffenbeschwörer
weapon_summoner.desc¦Um arsenal de lâminas espectrais orbita você e ataca seus inimigos. XP: Sigilo de Armas e habilidades.¦Un arsenal de lames spectrales gravite autour de vous et frappe vos ennemis. XP : Sceau d'armes et capacités.¦Ein Arsenal spektraler Klingen umkreist dich und beschießt deine Feinde. EP: Waffensigil und Fähigkeiten.
hunter¦Caçador¦Chasseur¦Jäger
hunter.desc¦Marca presas, coloca armadilhas, dispara chuvas de flechas e caça com um falcão. XP: flechas e Faca de Caça.¦Marque ses proies, pose des pièges, fait pleuvoir les flèches et chasse avec un faucon. XP : flèches et Couteau de chasse.¦Markiert Beute, legt Fallen, lässt Pfeile regnen und jagt mit einem Falken. EP: Pfeile und Jagdmesser.
bard¦Bardo¦Barde¦Barde
bard.desc¦Canções de apoio em área para aliados e notas que atordoam inimigos. XP: tocar Alaúde e canções.¦Chants de soutien de zone pour les alliés et notes étourdissant les ennemis. XP : jouer du Luth et chanter.¦Unterstützende Flächenlieder für Verbündete und betäubende Töne für Feinde. EP: Laute spielen und Lieder nutzen.
warlock¦Bruxo abissal¦Sorcier abyssal¦Abyssaler Hexenmeister
warlock.desc¦Feitiçaria sombria: maldições, projéteis abissais e diabretes, alimentados pela sua vida. XP: Talismã Amaldiçoado e feitiços.¦Sorcellerie obscure : malédictions, projectiles abyssaux et diablotins, alimentés par votre vie. XP : Talisman maudit et sorts.¦Dunkle Zauberei: Flüche, Abgrundgeschosse und Wichte, gespeist durch dein Leben. EP: Verfluchter Talisman und Zauber.
tamer¦Domador¦Dompteur¦Tierbändiger
tamer.desc¦Fortalece animais domesticados e luta ao lado deles. XP: dano dos animais, Apito e habilidades.¦Renforce les animaux apprivoisés et combat à leurs côtés. XP : dégâts des animaux, Sifflet et capacités.¦Stärkt gezähmte Tiere und kämpft an ihrer Seite. EP: Tierschaden, Pfeife und Fähigkeiten.
""")

add('element.aptitudes.', """
fire¦Fogo¦Feu¦Feuer
fire.desc¦Queima inimigos. Passiva: resistência ao fogo.¦Brûle les ennemis. Passif : résistance au feu.¦Verbrennt Feinde. Passiv: Feuerresistenz.
ice¦Gelo¦Glace¦Eis
ice.desc¦Desacelera e congela. Passiva: imunidade ao congelamento (neve fofa).¦Ralentit et gèle. Passif : immunité au gel (neige poudreuse).¦Verlangsamt und gefriert. Passiv: immun gegen Erfrieren (Pulverschnee).
lightning¦Relâmpago¦Foudre¦Blitz
lightning.desc¦Dano em cadeia e raios. Passiva: +3% de velocidade por grau.¦Dégâts en chaîne et éclairs. Passif : +3% de vitesse par rang.¦Kettenschaden und Blitze. Passiv: +3% Geschwindigkeit pro Rang.
earth¦Terra¦Terre¦Erde
earth.desc¦Lança inimigos no ar. Passiva: +1 de armadura por grau.¦Projette les ennemis en l'air. Passif : +1 d'armure par rang.¦Schleudert Feinde in die Luft. Passiv: +1 Rüstung pro Rang.
wind¦Vento¦Vent¦Wind
wind.desc¦Recuo forte e levitação. Passiva: +3 blocos de queda segura por grau.¦Fort recul et lévitation. Passif : +3 blocs de chute amortie par rang.¦Starker Rückstoß und Schweben. Passiv: +3 Blöcke sichere Fallhöhe pro Rang.
water¦Água¦Eau¦Wasser
water.desc¦Dano curativo e curas em área. Passiva: respiração aquática (e nado rápido a partir do grau 2).¦Dégâts curatifs et soins de zone. Passif : respiration aquatique (et nage rapide dès le rang 2).¦Heilender Schaden und Flächenheilung. Passiv: Wasseratmung (und schnelles Schwimmen ab Rang 2).
plant¦Planta¦Plante¦Pflanze
plant.desc¦Veneno e raízes. Passiva: regeneração sobre terra, grama ou folhas.¦Poison et racines. Passif : régénération sur terre, herbe ou feuilles.¦Gift und Wurzeln. Passiv: Regeneration auf Erde, Gras oder Laub.
magma¦Magma¦Magma¦Magma
magma.desc¦Dano de fogo intenso com recuo. Passiva: imunidade a blocos de magma, +1 de robustez por grau.¦Lourds dégâts de feu avec recul. Passif : immunité aux blocs de magma, +1 de robustesse par rang.¦Hoher Feuerschaden mit Rückstoß. Passiv: immun gegen Magmablöcke, +1 Rüstungshärte pro Rang.
shadow¦Sombra¦Ombre¦Schatten
shadow.desc¦Decomposição, cegueira e invisibilidade. Passiva: visão noturna no escuro.¦Wither, cécité et invisibilité. Passif : vision nocturne dans l'obscurité.¦Ausdörrung, Blindheit und Unsichtbarkeit. Passiv: Nachtsicht im Dunkeln.
light¦Luz¦Lumière¦Licht
light.desc¦Dano dobrado contra mortos-vivos e marca inimigos; a explosão cura aliados. Passiva: imunidade a Cegueira e Escuridão.¦Double les dégâts aux morts-vivants et marque les ennemis ; l'explosion soigne les alliés. Passif : immunité à Cécité et Ténèbres.¦Doppelter Schaden gegen Untote und markiert Feinde; der Ausbruch heilt Verbündete. Passiv: immun gegen Blindheit und Dunkelheit.
void¦Vazio¦Vide¦Leere
void.desc¦Atrai inimigos. Passiva: gravidade reduzida (saltos maiores, quedas mais lentas).¦Attire les ennemis vers vous. Passif : gravité réduite (sauts plus hauts, chutes plus lentes).¦Zieht Feinde zu dir. Passiv: verringerte Schwerkraft (höhere Sprünge, langsameres Fallen).
poison¦Veneno¦Poison¦Gift
poison.desc¦Veneno, náusea e fraqueza. Passiva: imunidade a veneno.¦Poison, nausée et faiblesse. Passif : immunité au poison.¦Gift, Übelkeit und Schwäche. Passiv: immun gegen Gift.
crystal¦Cristal¦Cristal¦Kristall
crystal.desc¦Estilhaços que causam mais dano e lentidão; a explosão concede Absorção. Passiva: resistência a recuo.¦Éclats plus puissants qui ralentissent ; l'explosion vous accorde Absorption. Passif : résistance au recul.¦Splitter, die stärker treffen und verlangsamen; der Ausbruch gewährt Absorption. Passiv: Rückstoßresistenz.
sound¦Som¦Son¦Klang
sound.desc¦Ondas sonoras que empurram e atordoam. Passiva: ecolocalização (hostis próximos brilham a cada 4 s).¦Ondes sonores qui repoussent et étourdissent. Passif : écholocalisation (les hostiles proches brillent toutes les 4 s).¦Schallwellen, die zurückstoßen und betäuben. Passiv: Echoortung (nahe feindliche Kreaturen leuchten alle 4 s).
""")

add('', """
entity.aptitudes.turret¦Torreta¦Tourelle¦Geschützturm
entity.aptitudes.minion¦Servo de %s¦Serviteur de %s¦Diener von %s
entity.aptitudes.spirit_wolf¦Lobo Espiritual¦Loup spirituel¦Geisterwolf
block.aptitudes.bonfire¦Fogueira¦Feu de camp¦Leuchtfeuer
block.aptitudes.guild_desk¦Mesa da Guilda¦Table de guilde¦Gildentisch
entity.minecraft.villager.guildmaster¦Mestre da Guilda¦Maître de guilde¦Gildenmeister
entity.minecraft.villager.aptitudes.guildmaster¦Mestre da Guilda¦Maître de guilde¦Gildenmeister
container.aptitudes.guild_desk¦Mesa da Guilda¦Table de guilde¦Gildentisch
container.aptitudes.guild_desk.result¦Resultado¦Résultat¦Ergebnis
block.aptitudes.contract_board¦Quadro de Contratos¦Tableau de contrats¦Auftragstafel
container.aptitudes.contract_board¦Quadro de Contratos¦Tableau de contrats¦Auftragstafel
effect.aptitudes.mana¦Recuperação de Mana¦Récupération de mana¦Manawiederherstellung
effect.aptitudes.frenzy¦Frenesi¦Frénésie¦Raserei
effect.aptitudes.eclipse_veil¦Véu do Eclipse¦Voile de l'éclipse¦Finsternisschleier
""")

add('stat.aptitudes.', """
health¦Vida¦Santé¦Leben
damage¦Dano¦Dégâts¦Schaden
attack_speed¦Velocidade de ataque¦Vitesse d'attaque¦Angriffstempo
armor¦Armadura¦Armure¦Rüstung
movement_speed¦Velocidade¦Vitesse¦Bewegungstempo
knockback_resistance¦Res. a recuo¦Rés. au recul¦Rückstoßres.
toughness¦Robustez¦Robustesse¦Rüstungshärte
luck¦Sorte¦Chance¦Glück
mana¦Mana máximo¦Mana maximal¦Max. Mana
spell_damage¦Dano mágico %%¦Dégâts magiques %%¦Zauberschaden %%
cooldown_reduction¦Red. de recarga %%¦Réduc. de recharge %%¦Abklingzeitverk. %%
""")

add('resource.aptitudes.', """
fury¦Fúria¦Fureur¦Zorn
focus¦Concentração¦Concentration¦Fokus
combo¦Combo¦Combo¦Kombo
sap¦Seiva¦Sève¦Saft
charge¦Carga¦Charge¦Ladung
momentum¦Ímpeto¦Élan¦Schwung
rhythm¦Ritmo¦Rythme¦Rhythmus
devotion¦Devoção¦Dévotion¦Hingabe
blood¦Sangue¦Sang¦Blut
verdance¦Verdejância¦Verdure¦Grünkraft
gold¦Ouro¦Or¦Gold
chi¦Chi¦Chi¦Chi
resonance¦Ressonância¦Résonance¦Resonanz
essence¦Essência¦Essence¦Essenz
cylinder¦Tambor¦Barillet¦Trommel
arsenal¦Arsenal¦Arsenal¦Arsenal
tracking¦Rastreamento¦Pistage¦Fährtensuche
harmony¦Harmonia¦Harmonie¦Harmonie
corruption¦Corrupção¦Corruption¦Verderbnis
bond¦Laço¦Lien¦Bindung
""")

add('skillbonus.aptitudes.', """
break_speed¦+%s%% de velocidade de quebra¦+%s%% de vitesse de minage¦+%s%% Abbaugeschwindigkeit
armor¦+%s de armadura¦+%s d'armure¦+%s Rüstung
knockback_resistance¦+%s%% de res. a recuo¦+%s%% de rés. au recul¦+%s%% Rückstoßres.
max_health¦+%s de vida máxima¦+%s de santé maximale¦+%s maximales Leben
double_harvest¦%s%% de colheita dupla¦%s%% de récolte double¦%s%% doppelte Ernte
luck¦+%s de sorte¦+%s de chance¦+%s Glück
double_catch¦%s%% de pesca dupla¦%s%% de prise double¦%s%% doppelter Fang
enchant_refund¦%s%% de restituição de níveis¦%s%% de remboursement de niveaux¦%s%% Stufenerstattung
max_mana¦+%s de mana máximo¦+%s de mana maximal¦+%s maximales Mana
melee_damage¦+%s%% de dano corpo a corpo¦+%s%% de dégâts de mêlée¦+%s%% Nahkampfschaden
arrow_damage¦+%s%% de dano de flechas¦+%s%% de dégâts des flèches¦+%s%% Pfeilschaden
toughness¦+%s de robustez¦+%s de robustesse¦+%s Rüstungshärte
explosion_resistance¦+%s%% de res. a explosões¦+%s%% de rés. aux explosions¦+%s%% Explosionsres.
movement_speed¦+%s%% de velocidade¦+%s%% de vitesse¦+%s%% Geschwindigkeit
safe_fall¦+%s blocos de queda segura¦+%s blocs de chute amortie¦+%s Blöcke sichere Fallhöhe
""")

add('hud.aptitudes.', """
mana¦Mana %s/%s¦Mana %s/%s¦Mana %s/%s
element¦Elemento: %s¦Élément : %s¦Element: %s
combo.finisher¦FINALIZAÇÃO!¦COUP FINAL !¦ABSCHLUSS!
combo.ready¦finalização pronta¦coup final prêt¦Abschluss bereit
""")

add('title.aptitudes.', """
berserker.1¦Senhor da Guerra¦Seigneur de guerre¦Kriegsherr
berserker.2¦Avatar da Ira¦Avatar de la colère¦Avatar des Zorns
mage.1¦Arquimago¦Archimage¦Erzmagier
mage.2¦Soberano Arcano¦Souverain arcanique¦Arkaner Herrscher
assassin.1¦Mestre das Sombras¦Maître des ombres¦Schattenmeister
assassin.2¦Ceifador Silencioso¦Faucheur silencieux¦Stiller Schnitter
herbalist.1¦Alquimista Verde¦Alchimiste vert¦Grüner Alchemist
herbalist.2¦Guardião do Éden¦Gardien de l'Éden¦Wächter Edens
engineer.1¦Mestre Engenheiro¦Maître ingénieur¦Meisteringenieur
engineer.2¦Arquiteto do Amanhã¦Architecte de demain¦Architekt von morgen
lancer.1¦Lanceiro Real¦Lancier royal¦Königlicher Lanzer
lancer.2¦Lança Celeste¦Lance céleste¦Himmelslanze
swordsman.1¦Mestre das Lâminas¦Maître des lames¦Klingenmeister
swordsman.2¦Santo da Espada¦Saint de l'épée¦Schwertheiliger
paladin.1¦Cruzado¦Croisé¦Kreuzritter
paladin.2¦Rei Eterno¦Roi éternel¦Ewiger König
necromancer.1¦Nobre do Sangue¦Noble du sang¦Blutadliger
necromancer.2¦Soberano Carmesim¦Souverain carmin¦Purpurfürst
druid.1¦Arquidruida¦Archidruide¦Erzdruide
druid.2¦Espírito da Floresta¦Esprit de la forêt¦Geist des Waldes
merchant.1¦Magnata¦Magnat¦Magnat
merchant.2¦Senhor da Fortuna¦Seigneur de la fortune¦Herr des Glücks
monk.1¦Grão-Mestre¦Grand maître¦Großmeister
monk.2¦Iluminado¦Éveillé¦Erleuchteter
elementalist.1¦Arquimago Elemental¦Archimage élémentaire¦Elementarer Erzmagier
elementalist.2¦Senhor dos Elementos¦Seigneur des éléments¦Herr der Elemente
shapeshifter.1¦Metamorfo¦Métamorphe¦Metamorph
shapeshifter.2¦Quimera Primordial¦Chimère primordiale¦Urchimäre
gunslinger.1¦Pistoleiro Rúnico¦Pistolero runique¦Runenrevolverheld
gunslinger.2¦Lenda do Oeste Arcano¦Légende de l'Ouest arcanique¦Legende des arkanen Westens
weapon_summoner.1¦Senhor das Lâminas¦Seigneur des lames¦Herr der Klingen
weapon_summoner.2¦Arsenal Vivo¦Arsenal vivant¦Lebendes Arsenal
hunter.1¦Mestre Caçador¦Maître chasseur¦Meisterjäger
hunter.2¦Senhor da Caça¦Seigneur de la chasse¦Herr der Jagd
bard.1¦Mestre de Capela¦Maître de chapelle¦Kapellmeister
bard.2¦Voz dos Deuses¦Voix des dieux¦Stimme der Götter
warlock.1¦Arquibruxo¦Archisorcier¦Erzhexenmeister
warlock.2¦Arauto do Abismo¦Héraut de l'abîme¦Herold des Abgrunds
tamer.1¦Senhor das Feras¦Seigneur des bêtes¦Herr der Bestien
tamer.2¦Rei da Matilha¦Roi de la meute¦König des Rudels
skill_up¦%s nível %s!¦%s niveau %s !¦%s Stufe %s!
skill_up_sub¦Sua habilidade melhora¦Votre compétence progresse¦Deine Fertigkeit verbessert sich
milestone¦✦ Marco: %s ✦¦✦ Palier : %s ✦¦✦ Meilenstein: %s ✦
""")

add('item.aptitudes.', """
berserker_weapon_master¦Machado do Senhor da Guerra¦Hache du seigneur de guerre¦Axt des Kriegsherrn
berserker_weapon_ultimate¦Machado do Avatar da Ira¦Hache de l'avatar de la colère¦Axt des Zornavatars
assassin_weapon_master¦Adaga do Mestre das Sombras¦Dague du maître des ombres¦Dolch des Schattenmeisters
assassin_weapon_ultimate¦Adaga do Ceifador Silencioso¦Dague du faucheur silencieux¦Dolch des stillen Schnitters
herbalist_weapon_master¦Foice do Alquimista Verde¦Faucille de l'alchimiste vert¦Sichel des grünen Alchemisten
herbalist_weapon_ultimate¦Foice do Guardião do Éden¦Faucille du gardien de l'Éden¦Sichel des Wächters Edens
lancer_weapon_master¦Lança do Lanceiro Real¦Lance du lancier royal¦Lanze des königlichen Lanzers
lancer_weapon_ultimate¦Lança da Lança Celeste¦Lance de la lance céleste¦Lanze der Himmelslanze
swordsman_weapon_master¦Katana do Mestre das Lâminas¦Katana du maître des lames¦Katana des Klingenmeisters
swordsman_weapon_ultimate¦Katana do Santo da Espada¦Katana du saint de l'épée¦Katana des Schwertheiligen
paladin_weapon_master¦Martelo do Cruzado¦Marteau du croisé¦Hammer des Kreuzritters
paladin_weapon_ultimate¦Martelo do Rei Eterno¦Marteau du roi éternel¦Hammer des ewigen Königs
merchant_weapon_master¦Florete do Magnata¦Rapière du magnat¦Rapier des Magnaten
merchant_weapon_ultimate¦Florete do Senhor da Fortuna¦Rapière du seigneur de la fortune¦Rapier des Glücksherrn
monk_weapon_master¦Cajado do Grão-Mestre¦Bâton du grand maître¦Stab des Großmeisters
monk_weapon_ultimate¦Cajado do Iluminado¦Bâton de l'éveillé¦Stab des Erleuchteten
shapeshifter_weapon_master¦Garras do Metamorfo¦Griffes du métamorphe¦Klauen des Metamorphs
shapeshifter_weapon_ultimate¦Garras da Quimera Primordial¦Griffes de la chimère primordiale¦Klauen der Urchimäre
weapon_summoner_weapon_master¦Lâmina do Senhor das Lâminas¦Lame du seigneur des lames¦Klinge des Klingenherrn
weapon_summoner_weapon_ultimate¦Lâmina do Arsenal Vivo¦Lame de l'arsenal vivant¦Klinge des lebenden Arsenals
hunter_weapon_master¦Facão do Mestre Caçador¦Machette du maître chasseur¦Machete des Meisterjägers
hunter_weapon_ultimate¦Facão do Senhor da Caça¦Machette du seigneur de la chasse¦Machete des Jagdherrn
tamer_weapon_master¦Chicote do Senhor das Feras¦Fouet du seigneur des bêtes¦Peitsche des Bestienherrn
tamer_weapon_ultimate¦Chicote do Rei da Matilha¦Fouet du roi de la meute¦Peitsche des Rudelkönigs
mage_weapon_master¦Varinha do Arquimago¦Baguette de l'archimage¦Zauberstab des Erzmagiers
mage_weapon_ultimate¦Varinha do Soberano Arcano¦Baguette du souverain arcanique¦Zauberstab des arkanen Herrschers
engineer_weapon_master¦Canhão do Mestre Engenheiro¦Canon du maître ingénieur¦Kanone des Meisteringenieurs
engineer_weapon_ultimate¦Canhão do Arquiteto do Amanhã¦Canon de l'architecte de demain¦Kanone des Architekten von morgen
necromancer_weapon_master¦Cetro do Nobre do Sangue¦Sceptre du noble du sang¦Zepter des Blutadligen
necromancer_weapon_ultimate¦Cetro do Soberano Carmesim¦Sceptre du souverain carmin¦Zepter des Purpurfürsten
druid_weapon_master¦Cajado do Arquidruida¦Bâton de l'archidruide¦Stab des Erzdruiden
druid_weapon_ultimate¦Cajado do Espírito da Floresta¦Bâton de l'esprit de la forêt¦Stab des Waldgeistes
elementalist_weapon_master¦Orbe do Arquimago Elemental¦Orbe de l'archimage élémentaire¦Kugel des elementaren Erzmagiers
elementalist_weapon_ultimate¦Orbe do Senhor dos Elementos¦Orbe du seigneur des éléments¦Kugel des Elementeherrn
gunslinger_weapon_master¦Revólver do Pistoleiro Rúnico¦Revolver du pistolero runique¦Revolver des Runenrevolverhelden
gunslinger_weapon_ultimate¦Revólver da Lenda do Oeste Arcano¦Revolver de la légende de l'Ouest arcanique¦Revolver der Legende des arkanen Westens
bard_weapon_master¦Harpa do Mestre de Capela¦Harpe du maître de chapelle¦Harfe des Kapellmeisters
bard_weapon_ultimate¦Harpa da Voz dos Deuses¦Harpe de la voix des dieux¦Harfe der Götterstimme
warlock_weapon_master¦Crânio do Arquibruxo¦Crâne de l'archisorcier¦Schädel des Erzhexenmeisters
warlock_weapon_ultimate¦Crânio do Arauto do Abismo¦Crâne du héraut de l'abîme¦Schädel des Abgrundherolds
""")

add('trial.aptitudes.', """
weapon_kills¦Abates com a arma da classe: %s/%s¦Éliminations avec l'arme du rôle : %s/%s¦Tötungen mit der Rollenwaffe: %s/%s
undead_kills¦Mortos-vivos derrotados: %s/%s¦Morts-vivants vaincus : %s/%s¦Getötete Untote: %s/%s
souls¦Almas colhidas (mortes próximas): %s/%s¦Âmes récoltées (morts proches) : %s/%s¦Geerntete Seelen (nahe Tode): %s/%s
ability_uses¦Usos de habilidades da classe: %s/%s¦Utilisations de capacités du rôle : %s/%s¦Nutzungen von Rollenfähigkeiten: %s/%s
role_actions¦Ações da classe: %s/%s¦Actions du rôle : %s/%s¦Rollenaktionen: %s/%s
""")

add('form.aptitudes.', """
creeper¦Creeper¦Creeper¦Creeper
creeper.desc¦Poder: explode sem morrer (quebra blocos se o servidor permitir). Desbloqueio: derrotar 10.¦Pouvoir : exploser sans mourir (détruit les blocs si le serveur l'autorise). Déblocage : en vaincre 10.¦Kraft: Explodieren ohne zu sterben (zerstört Blöcke, falls erlaubt). Freischalten: 10 besiegen.
enderman¦Enderman¦Enderman¦Enderman
enderman.desc¦Visão noturna. Poder: teleporte de 20-28 blocos. Desbloqueio: derrotar 10.¦Vision nocturne. Pouvoir : téléportation de 20-28 blocs. Déblocage : en vaincre 10.¦Nachtsicht. Kraft: 20-28 Blöcke teleportieren. Freischalten: 10 besiegen.
spider¦Aranha¦Araignée¦Spinne
spider.desc¦Visão noturna. Poder: mordida venenosa. Desbloqueio: derrotar 10.¦Vision nocturne. Pouvoir : morsure venimeuse. Déblocage : en vaincre 10.¦Nachtsicht. Kraft: Giftbiss. Freischalten: 10 besiegen.
blaze¦Blaze¦Blaze¦Lohe
blaze.desc¦Resistência ao fogo. Poder: 3 bolas de fogo. Desbloqueio: derrotar 10.¦Résistance au feu. Pouvoir : 3 boules de feu. Déblocage : en vaincre 10.¦Feuerresistenz. Kraft: 3 Feuerbälle. Freischalten: 10 besiegen.
bat¦Morcego¦Chauve-souris¦Fledermaus
bat.desc¦Visão noturna; queda lenta ao agachar. Poder: voo curto guiado pelo olhar. Desbloqueio: derrotar 5.¦Vision nocturne ; chute lente accroupi. Pouvoir : vol bref dirigé par le regard. Déblocage : en vaincre 5.¦Nachtsicht; langsamer Fall beim Schleichen. Kraft: kurzer Flug in Blickrichtung. Freischalten: 5 besiegen.
slime¦Slime¦Slime¦Schleim
slime.desc¦Sem dano de queda. Poder: supersalto. Desbloqueio: derrotar 10.¦Aucun dégât de chute. Pouvoir : super saut. Déblocage : en vaincre 10.¦Kein Fallschaden. Kraft: Supersprung. Freischalten: 10 besiegen.
chicken¦Galinha¦Poule¦Huhn
chicken.desc¦Queda lenta. Poder: rajada de ovos (pegadinha). Desbloqueio: derrotar 10.¦Chute lente. Pouvoir : barrage d'œufs (farce). Déblocage : en vaincre 10.¦Langsamer Fall. Kraft: Eiersalve (Streich). Freischalten: 10 besiegen.
iron_golem¦Golem de Ferro¦Golem de fer¦Eisengolem
iron_golem.desc¦Resistência, mas menor velocidade. Poder: golpe no chão. Desbloqueio: derrotar 3.¦Résistance, mais plus lent. Pouvoir : frappe au sol. Déblocage : en vaincre 3.¦Resistenz, aber langsamer. Kraft: Bodenschlag. Freischalten: 3 besiegen.
witch¦Bruxa¦Sorcière¦Hexe
witch.desc¦Imunidade a veneno. Poder: maldição (veneno, lentidão, fraqueza). Desbloqueio: derrotar 10.¦Immunité au poison. Pouvoir : malédiction (poison, lenteur, faiblesse). Déblocage : en vaincre 10.¦Immun gegen Gift. Kraft: Fluch (Gift, Langsamkeit, Schwäche). Freischalten: 10 besiegen.
villager¦Aldeão¦Villageois¦Dorfbewohner
villager.desc¦Disfarce perfeito para pegadinhas. Poder: fuga (Velocidade III). Desbloqueio: Metamorfo nível 5.¦Déguisement idéal pour les farces. Pouvoir : fuite (Vitesse III). Déblocage : Métamorphe niveau 5.¦Perfekte Tarnung für Streiche. Kraft: Flucht (Schnelligkeit III). Freischalten: Gestaltwandler Stufe 5.
warden¦Defensor¦Warden¦Warden
warden.desc¦Somente com a habilidade definitiva.¦Uniquement avec la capacité ultime.¦Nur mit der ultimativen Fähigkeit.
""")

add('aptitudes.role.', """
levelup¦%s alcançou o nível %s! (+%s ponto(s) de árvore)¦%s atteint le niveau %s ! (+%s point(s) d'arbre)¦%s hat Stufe %s erreicht! (+%s Baumpunkt(e))
tree_reset¦Seus pontos da árvore de %s foram restituídos porque as regras mudaram.¦Vos points d'arbre de %s ont été remboursés, car les règles ont changé.¦Deine Baumpunkte für %s wurden erstattet, weil sich die Regeln geändert haben.
change_cooldown¦Você pode trocar de classe em %s min.¦Vous pourrez changer de rôle dans %s min.¦Du kannst in %s Min. die Rolle wechseln.
primary_short¦[Principal]¦[Principal]¦[Hauptrolle]
secondary_short¦[Secundária]¦[Secondaire]¦[Nebenrolle]
upgrade.unknown_node¦Nó desconhecido.¦Nœud inconnu.¦Unbekannter Knoten.
upgrade.role_not_active¦Essa classe não é sua principal nem secundária.¦Ce rôle n'est ni votre principal ni votre secondaire.¦Diese Rolle ist weder deine Haupt- noch deine Nebenrolle.
upgrade.max_rank¦Esse nó já está no grau máximo.¦Ce nœud est déjà au rang maximal.¦Dieser Knoten hat bereits den höchsten Rang.
upgrade.level_too_low¦O nível da sua classe é baixo demais para o próximo grau.¦Le niveau du rôle est trop bas pour le rang suivant.¦Deine Rollenstufe ist für den nächsten Rang zu niedrig.
upgrade.missing_prerequisite¦Aprenda o nó anterior primeiro.¦Apprenez d'abord le nœud précédent.¦Erlerne zuerst den vorherigen Knoten.
upgrade.no_points¦Não restam pontos de árvore nessa classe.¦Aucun point d'arbre restant pour ce rôle.¦Keine Baumpunkte für diese Rolle übrig.
equipped¦%s equipado no espaço %s¦%s équipé dans l'emplacement %s¦%s auf Platz %s ausgerüstet
needs_ritual¦Para substituir uma classe escolhida, queime uma Oferenda de Renovação em uma Fogueira.¦Pour remplacer un rôle choisi, brûlez une Offrande de renouveau à un feu de camp.¦Verbrenne eine Opfergabe der Erneuerung an einem Leuchtfeuer, um eine gewählte Rolle zu ersetzen.
rebirth_granted¦A Fogueira aceita sua oferenda. Você pode trocar uma de suas classes.¦Le feu de camp accepte votre offrande. Vous pouvez changer un de vos rôles.¦Das Leuchtfeuer nimmt deine Opfergabe an. Du darfst eine deiner Rollen wechseln.
upgrade.legend_taken¦Outro jogador do servidor já possui esta lendária.¦Un autre joueur du serveur possède déjà cette légendaire.¦Ein anderer Spieler auf dem Server besitzt diese legendäre Fähigkeit bereits.
upgrade.legend_limit¦Você já possui outra lendária (uma por jogador).¦Vous possédez déjà une autre légendaire (une par joueur).¦Du besitzt bereits eine andere legendäre Fähigkeit (eine pro Spieler).
""")

add('aptitudes.ability.', """
no_primary¦Você não tem classe principal. Pressione J para escolher.¦Vous n'avez pas de rôle principal. Appuyez sur J pour en choisir un.¦Du hast keine Hauptrolle. Drücke J, um eine zu wählen.
no_secondary¦Você não tem classe secundária. Pressione J para escolher.¦Vous n'avez pas de rôle secondaire. Appuyez sur J pour en choisir un.¦Du hast keine Nebenrolle. Drücke J, um eine zu wählen.
not_learned¦Você ainda não aprendeu essa habilidade.¦Vous n'avez pas encore appris cette capacité.¦Du hast diese Fähigkeit noch nicht erlernt.
cooldown¦%s estará pronta em %s s.¦%s sera prête dans %s s.¦%s ist in %s s bereit.
no_energy¦Mana insuficiente (requer %s).¦Mana insuffisant (%s requis).¦Nicht genug Mana (%s benötigt).
no_effect¦Sem alvo ou espaço para usar.¦Aucune cible ou pas assez de place.¦Kein Ziel oder nicht genug Platz.
no_ultimate¦Você precisa de uma classe no estágio Definitivo para usar uma habilidade definitiva.¦Un rôle au stade Ultime est nécessaire pour utiliser une capacité ultime.¦Du benötigst eine Rolle auf Stufe Ultimativ für eine ultimative Fähigkeit.
no_blood¦Vida insuficiente para magia de sangue (custo: %s).¦Santé insuffisante pour la magie du sang (coût : %s).¦Nicht genug Leben für Blutmagie (Kosten: %s).
no_signature¦Você precisa de uma classe Mestre (com uma característica ativa) para usar Z.¦Un rôle au stade Maître (avec une signature active) est nécessaire pour utiliser Z.¦Du benötigst eine Meisterrolle (mit aktiver Signatur), um Z zu nutzen.
""")

add('aptitudes.element.', """
switched¦Elemento ativo: %s¦Élément actif : %s¦Aktives Element: %s
not_elementalist¦Elementalista deve ser sua classe principal ou secundária.¦Élémentaliste doit être votre rôle principal ou secondaire.¦Elementarist muss deine Haupt- oder Nebenrolle sein.
already_mastered¦Você já domina esse elemento.¦Vous maîtrisez déjà cet élément.¦Du meisterst dieses Element bereits.
no_slot¦Você ainda não pode dominar outro elemento (níveis 1, 10 e 20).¦Vous ne pouvez pas encore maîtriser un autre élément (niveaux 1, 10 et 20).¦Du kannst noch kein weiteres Element meistern (Stufen 1, 10 und 20).
""")

add('aptitudes.evolve.', """
role_not_active¦Você só pode evoluir sua classe principal ou secundária.¦Vous ne pouvez faire évoluer que votre rôle principal ou secondaire.¦Du kannst nur deine Haupt- oder Nebenrolle entwickeln.
max_tier¦Esta classe já está na forma definitiva.¦Ce rôle est déjà sous sa forme ultime.¦Diese Rolle hat bereits ihre ultimative Form.
level_too_low¦O nível da sua classe é baixo demais para evoluir.¦Le niveau du rôle est trop bas pour évoluer.¦Deine Rollenstufe ist zum Entwickeln zu niedrig.
trial_incomplete¦Você ainda não concluiu a prova de maestria.¦Vous n'avez pas encore terminé l'épreuve de maîtrise.¦Du hast die Meisterprüfung noch nicht abgeschlossen.
no_boss¦Você deve derrotar um chefe (Wither, Dragão ou Defensor) com esta classe ativa.¦Vous devez vaincre un boss (Wither, Dragon ou Warden) avec ce rôle actif.¦Du musst einen Boss (Wither, Drache oder Warden) mit dieser aktiven Rolle besiegen.
missing_item¦Você precisa do item de evolução no inventário.¦L'objet d'évolution doit être dans votre inventaire.¦Du benötigst den Entwicklungsgegenstand im Inventar.
trial_done¦Prova de maestria de %s concluída!¦Épreuve de maîtrise de %s terminée !¦Meisterprüfung für %s abgeschlossen!
boss_done¦Feito de chefe registrado para %s!¦Exploit de boss enregistré pour %s !¦Bossleistung für %s vermerkt!
broadcast¦%s evoluiu para %s!¦%s a évolué en %s !¦%s hat sich zu %s entwickelt!
""")

add('aptitudes.', """
blood.immortality¦Imortalidade Carmesim! O sangue impede sua morte.¦Immortalité carmin ! Le sang vous refuse la mort.¦Purpurunsterblichkeit! Das Blut verweigert dir den Tod.
casino.jackpot¦🎰 PRÊMIO MÁXIMO! Esmeraldas, Sorte e Herói da Vila.¦🎰 GROS LOT ! Émeraudes, Chance et Héros du village.¦🎰 JACKPOT! Smaragde, Glück und Held des Dorfes.
casino.power¦🎰 A fortuna concede poder.¦🎰 La fortune vous accorde du pouvoir.¦🎰 Das Glück verleiht dir Macht.
casino.heal¦🎰 Cura completa.¦🎰 Soin complet.¦🎰 Vollständige Heilung.
casino.gold¦🎰 Chuva de ouro!¦🎰 Pluie d'or !¦🎰 Goldregen!
casino.curse¦🎰 O azar recai sobre seus inimigos.¦🎰 La malchance frappe vos ennemis.¦🎰 Das Pech trifft deine Feinde.
casino.house¦🎰 A casa vence…¦🎰 La maison gagne…¦🎰 Die Bank gewinnt…
morph.unlocked¦Forma desbloqueada: %s!¦Forme débloquée : %s !¦Form freigeschaltet: %s!
morph.locked¦Você ainda não desbloqueou a forma %s.¦Vous n'avez pas encore débloqué la forme %s.¦Du hast die Form %s noch nicht freigeschaltet.
morph.exhausted¦Sem mana: você retorna à forma humana.¦Plus de mana : vous reprenez forme humaine.¦Kein Mana mehr: Du nimmst wieder menschliche Form an.
signature.beacon_set¦Sinalizador definido. Use a habilidade novamente para voltar aqui.¦Balise placée. Utilisez à nouveau la capacité pour revenir ici.¦Signalpunkt gesetzt. Nutze die Fähigkeit erneut, um hierher zurückzukehren.
signature.forest_rebirth¦A floresta traz você de volta à vida!¦La forêt vous ramène à la vie !¦Der Wald bringt dich ins Leben zurück!
levelup_skill¦%s alcançou o nível %s!¦%s atteint le niveau %s !¦%s hat Stufe %s erreicht!
resource.empowered¦%s liberado: habilidade potencializada!¦%s libéré : capacité renforcée !¦%s freigesetzt: verstärkte Fähigkeit!
hud.shown¦HUD exibido¦ATH affiché¦HUD eingeblendet
hud.hidden¦HUD oculto (pressione O para exibir)¦ATH masqué (appuyez sur O pour l'afficher)¦HUD ausgeblendet (O zum Einblenden)
""")

add('aptitudes.unique.', """
granted¦✦ %s obteve a habilidade única %s. Ninguém mais pode tê-la!¦✦ %s a obtenu la capacité unique %s. Personne d'autre ne peut la posséder !¦✦ %s hat die einzigartige Fähigkeit %s erhalten. Niemand sonst kann sie besitzen!
taken¦Você concluiu o feito de %s, mas a habilidade já pertence a %s.¦Vous avez accompli l'exploit pour %s, mais la capacité appartient déjà à %s.¦Du hast die Leistung für %s erbracht, aber die Fähigkeit gehört bereits %s.
already_own¦Você concluiu o feito de %s, mas já possui uma habilidade única.%s¦Vous avez accompli l'exploit pour %s, mais vous possédez déjà une capacité unique.%s¦Du hast die Leistung für %s erbracht, besitzt aber bereits eine einzigartige Fähigkeit.%s
released¦A habilidade única %s está disponível novamente (%s ficou inativo por tempo demais).¦La capacité unique %s est de nouveau disponible (%s est resté inactif trop longtemps).¦Die einzigartige Fähigkeit %s ist wieder frei (%s war zu lange inaktiv).
none¦Você não tem lendária: aprenda no fim da árvore da sua classe (nível 25).¦Vous n'avez pas de légendaire : apprenez-la au bout de l'arbre de votre rôle (niveau 25).¦Du hast keine legendäre Fähigkeit: Erlerne sie am Ende deines Rollenbaums (Stufe 25).
negated¦Você está em um Domínio de Negação!¦Vous êtes dans un Domaine de négation !¦Du befindest dich in einer Zone der Negation!
free¦Disponível¦Libre¦Frei
""")

add('aptitudes.souls.', """
rested¦Você descansa na Fogueira. Vida e mana restaurados; renascimento definido.¦Vous vous reposez au feu de camp. Santé et mana restaurés ; réapparition définie.¦Du rastest am Leuchtfeuer. Leben und Mana wiederhergestellt; Wiederbelebungspunkt gesetzt.
need_bonfire¦Você deve estar ao lado da Fogueira onde descansou.¦Vous devez être près du feu de camp où vous vous êtes reposé.¦Du musst neben dem Leuchtfeuer stehen, an dem du gerastet hast.
not_enough¦Você precisa de %s níveis de XP (tem %s).¦Il vous faut %s niveaux d'XP (vous en avez %s).¦Du benötigst %s EP-Stufen (du hast %s).
leveled¦Nível de alma %s (−%s níveis de XP). A cada 3 níveis de alma = 1 ponto de atributo.¦Niveau d'âme %s (−%s niveaux d'XP). Tous les 3 niveaux d'âme = 1 point d'attribut.¦Seelenstufe %s (−%s EP-Stufen). Je 3 Seelenstufen = 1 Attributpunkt.
stain_left¦Sua XP (%s pontos) permanece onde você caiu: %s, %s, %s. Recupere antes de morrer novamente.¦Votre XP (%s points) reste là où vous êtes tombé : %s, %s, %s. Récupérez-la avant de mourir à nouveau.¦Deine EP (%s Punkte) bleiben am Todesort: %s, %s, %s. Hole sie vor dem nächsten Tod zurück.
stain_lost¦Você morreu antes de recuperar: %s pontos de XP perdidos.¦Vous êtes mort avant de la récupérer : %s points d'XP perdus.¦Du bist vor der Rückholung gestorben: %s EP-Punkte verloren.
recovered¦XP recuperada: +%s pontos¦XP récupérée : +%s points¦EP zurückgeholt: +%s Punkte
""")

add('ultimate.aptitudes.', """
berserker¦Ira Implacável¦Colère implacable¦Unaufhaltsamer Zorn
berserker.desc¦8 s sem poder morrer (a vida não cai abaixo de 1), com Força II, Velocidade II e Resistência.¦8 s sans pouvoir mourir (la santé ne descend jamais sous 1), avec Force II, Vitesse II et Résistance.¦8 s lang unsterblich (Leben fällt nie unter 1), mit Stärke II, Schnelligkeit II und Resistenz.
mage¦Singularidade¦Singularité¦Singularität
mage.desc¦Um buraco negro no ponto-alvo atrai e fere inimigos por 5 s.¦Un trou noir au point visé attire et blesse les ennemis pendant 5 s.¦Ein schwarzes Loch am Zielpunkt zieht Feinde 5 s lang an und verletzt sie.
assassin¦Execução Sombria¦Exécution des ombres¦Schattenhinrichtung
assassin.desc¦Salta para trás de até 5 inimigos próximos; os que têm menos de 30% de vida morrem instantaneamente.¦Saute derrière jusqu'à 5 ennemis proches ; ceux sous 30% de santé meurent instantanément.¦Springe hinter bis zu 5 nahe Feinde; Ziele mit weniger als 30% Leben sterben sofort.
herbalist¦Jardim do Éden¦Jardin d'Éden¦Garten Eden
herbalist.desc¦Área de 8 blocos por 10 s: aliados regeneram e são purificados; inimigos são envenenados.¦Zone de 8 blocs pendant 10 s : les alliés régénèrent et sont purifiés ; les ennemis sont empoisonnés.¦Bereich von 8 Blöcken für 10 s: Verbündete regenerieren und werden gereinigt; Feinde werden vergiftet.
engineer¦Ataque Orbital¦Frappe orbitale¦Orbitalschlag
engineer.desc¦12 impactos em 3 s ao redor do ponto-alvo. Não quebram blocos e só ferem e empurram inimigos.¦12 impacts en 3 s autour du point visé. Ils ne détruisent pas les blocs et ne blessent et repoussent que les ennemis.¦12 Einschläge in 3 s um den Zielpunkt. Sie zerstören keine Blöcke und verletzen und stoßen nur Feinde zurück.
lancer¦Lança Celestial¦Lance céleste¦Himmlische Lanze
lancer.desc¦Salto enorme e mergulho com uma onda de choque devastadora.¦Bond immense puis plongeon avec une onde de choc dévastatrice.¦Gewaltiger Sprung und Sturzangriff mit verheerender Schockwelle.
swordsman¦Mil Cortes¦Mille entailles¦Tausend Schnitte
swordsman.desc¦15 cortes em 3 s no inimigo mais próximo, ignorando invulnerabilidade entre golpes.¦15 coups en 3 s sur l'ennemi le plus proche, ignorant l'invulnérabilité entre les coups.¦15 Hiebe in 3 s gegen den nächsten Feind, ignorieren die Trefferunverwundbarkeit.
paladin¦Reino Eterno¦Royaume éternel¦Ewiges Königreich
paladin.desc¦Área de 8 blocos por 8 s: aliados não podem morrer e mortos-vivos queimam.¦Zone de 8 blocs pendant 8 s : les alliés ne peuvent pas mourir et les morts-vivants brûlent.¦Bereich von 8 Blöcken für 8 s: Verbündete können nicht sterben und Untote brennen.
necromancer¦Maré Carmesim¦Marée carmin¦Purpurflut
necromancer.desc¦Drena inimigos em 10 blocos, cura você, completa seu exército de servos e o enfurece.¦Draine les ennemis dans un rayon de 10 blocs, vous soigne, complète votre armée de serviteurs et la déchaîne.¦Entzieht Feinden im Umkreis von 10 Blöcken Leben, heilt dich, füllt deine Dienerarmee auf und versetzt sie in Raserei.
druid¦Fúria da Natureza¦Fureur de la nature¦Zorn der Natur
druid.desc¦Invoca 3 lobos espirituais aliados (30 s) e enraíza inimigos próximos.¦Invoque 3 loups spirituels alliés (30 s) et enracine les ennemis proches.¦Beschwört 3 verbündete Geisterwölfe (30 s) und verwurzelt nahe Feinde.
merchant¦Cassino da Fortuna¦Casino de la fortune¦Glückskasino
merchant.desc¦Roleta: prêmio máximo, poder, cura completa, chuva de ouro, maldição nos inimigos… ou a casa vence.¦Roulette : gros lot, puissance, soin complet, pluie d'or, malédiction des ennemis… ou la maison gagne.¦Roulette: Jackpot, Macht, volle Heilung, Goldregen, Fluch auf Feinde… oder die Bank gewinnt.
monk¦Nirvana¦Nirvana¦Nirwana
monk.desc¦10 s intocável por criaturas, com Velocidade III, Força II e Supersalto.¦10 s intouchable par les créatures, avec Vitesse III, Force II et Sauts améliorés.¦10 s lang unantastbar für Kreaturen, mit Schnelligkeit III, Stärke II und Sprungkraft.
elementalist¦Convergência Elemental¦Convergence élémentaire¦Elementare Konvergenz
elementalist.desc¦Libera a explosão de todos os elementos dominados de uma só vez.¦Déchaîne l'explosion de tous vos éléments maîtrisés à la fois.¦Entfesselt den Ausbruch aller gemeisterten Elemente zugleich.
shapeshifter¦Forma Primordial¦Forme primordiale¦Urgestalt
shapeshifter.desc¦Torna-se um Defensor por 20 s: +20 de vida, Força II, Resistência II, Escuridão em jogadores próximos e Estrondo Sônico como poder.¦Devient un Warden pendant 20 s : +20 de santé, Force II, Résistance II, Ténèbres aux joueurs proches et Bang sonique comme pouvoir.¦Werde für 20 s zum Warden: +20 Leben, Stärke II, Resistenz II, Dunkelheit für nahe Spieler und Schallknall als Kraft.
gunslinger¦Duelo ao Meio-Dia¦Duel à midi¦Zwölf Uhr mittags
gunslinger.desc¦Marca todos os inimigos em 32 blocos e dispara em todos após 1 s: 15/18/21 de dano.¦Marque tous les ennemis dans un rayon de 32 blocs et leur tire dessus après 1 s : 15/18/21 dégâts.¦Markiert alle Feinde im Umkreis von 32 Blöcken und beschießt sie nach 1 s: 15/18/21 Schaden.
weapon_summoner¦Arsenal Infinito¦Arsenal infini¦Unendliches Arsenal
weapon_summoner.desc¦Por 5 s, a cada meio segundo uma lâmina atinge cada inimigo em 16 blocos.¦Pendant 5 s, une lame frappe chaque ennemi dans un rayon de 16 blocs toutes les demi-secondes.¦5 s lang trifft jede halbe Sekunde eine Klinge jeden Feind im Umkreis von 16 Blöcken.
hunter¦Caçada Selvagem¦Chasse sauvage¦Wilde Jagd
hunter.desc¦Marca todos os inimigos em 24 blocos (+35%% do seu dano) e os atinge com 3 salvas de flechas.¦Marque tous les ennemis dans un rayon de 24 blocs (+35%% de vos dégâts) et les frappe de 3 volées de flèches.¦Markiert alle Feinde im Umkreis von 24 Blöcken (+35%% deines Schadens) und trifft sie mit 3 Pfeilsalven.
bard¦Hino dos Heróis¦Hymne des héros¦Heldenhymne
bard.desc¦Aliados em 20 blocos: vida completa, Força II, Resistência II e Absorção IV por 15 s.¦Alliés dans un rayon de 20 blocs : santé complète, Force II, Résistance II et Absorption IV pendant 15 s.¦Verbündete im Umkreis von 20 Blöcken: volles Leben, Stärke II, Resistenz II und Absorption IV für 15 s.
warlock¦Avatar Demoníaco¦Avatar démoniaque¦Dämonischer Avatar
warlock.desc¦15 s: Força II, Resistência, imunidade ao fogo, Velocidade e 20%% de roubo de vida.¦15 s : Force II, Résistance, immunité au feu, Vitesse et 20%% de vol de vie.¦15 s: Stärke II, Resistenz, Feuerimmunität, Schnelligkeit und 20%% Lebensraub.
tamer¦Rei das Feras¦Roi des bêtes¦König der Bestien
tamer.desc¦Invoca um golem de ferro e 3 lobos aliados por 30 s.¦Invoque un golem de fer et 3 loups alliés pendant 30 s.¦Beschwört einen Eisengolem und 3 verbündete Wölfe für 30 s.
""")

add('signature.aptitudes.', """
berserker¦Rugido do Julgamento¦Rugissement du jugement¦Urteilsgebrüll
berserker.desc¦Criaturas em 8 blocos perdem METADE da vida atual; você ganha Força III e Velocidade II (15 s).¦Les créatures dans un rayon de 8 blocs perdent la MOITIÉ de leur santé actuelle ; vous gagnez Force III et Vitesse II (15 s).¦Kreaturen im Umkreis von 8 Blöcken verlieren die HÄLFTE ihres aktuellen Lebens; du erhältst Stärke III und Schnelligkeit II (15 s).
mage¦Chuva de Runas¦Pluie de runes¦Runenregen
mage.desc¦8 runas arcanas caem no ponto-alvo: dano mágico em área e recuo. Sem fogo ou explosões.¦8 runes arcaniques tombent au point visé : dégâts magiques de zone et recul. Sans feu ni explosions.¦8 arkane Runen fallen auf den Zielpunkt: magischer Flächenschaden und Rückstoß. Kein Feuer und keine Explosionen.
assassin¦Marca da Morte¦Marque de mort¦Todesmal
assassin.desc¦Marca o alvo: ele recebe o TRIPLO de dano de você por 10 s.¦Marque la cible : elle subit le TRIPLE de vos dégâts pendant 10 s.¦Markiert das Ziel: Es erleidet 10 s lang DREIFACHEN Schaden von dir.
herbalist¦Elixir da Vida¦Élixir de vie¦Lebenselixier
herbalist.desc¦Purifica aliados próximos e concede Regeneração III e vida extra.¦Purifie les alliés proches et accorde Régénération III et de la santé supplémentaire.¦Reinigt nahe Verbündete und gewährt Regeneration III und zusätzliches Leben.
engineer¦Sinalizador Tático¦Balise tactique¦Taktisches Signal
engineer.desc¦1º uso: define um ponto de teleporte. 2º uso: retorna com blindagem (Absorção IV + Resistência II).¦1re utilisation : place un point de téléportation. 2e : y revient avec blindage (Absorption IV + Résistance II).¦1. Nutzung: Setzt einen Teleportpunkt. 2. Nutzung: Rückkehr mit Panzerung (Absorption IV + Resistenz II).
lancer¦Investida Imparável¦Charge irrésistible¦Unaufhaltsamer Ansturm
lancer.desc¦3 s de investidas contínuas que atropelam inimigos; intocável por criaturas durante o efeito.¦3 s de charges continues piétinant les ennemis ; intouchable par les créatures pendant ce temps.¦3 s lang ununterbrochene Anstürme, die Feinde niedertrampeln; währenddessen unantastbar für Kreaturen.
swordsman¦Corte Dimensional¦Entaille dimensionnelle¦Dimensionsschnitt
swordsman.desc¦Um corte de 14 blocos atravessa todos os inimigos: 20 de dano.¦Une entaille sur 14 blocs traverse tous les ennemis : 20 dégâts.¦Ein 14 Blöcke langer Hieb durch alle Feinde: 20 Schaden.
paladin¦Bastião Sagrado¦Bastion sacré¦Heilige Bastion
paladin.desc¦10 s sem receber NENHUM dano, +50 de mana, Vida Extra III, Absorção III, Resistência II e cura completa.¦10 s sans subir AUCUN dégât, +50 de mana, Bonus de santé III, Absorption III, Résistance II et soin complet.¦10 s lang KEIN Schaden, +50 Mana, Lebensenergie III, Absorption III, Resistenz II und volle Heilung.
necromancer¦Exército de Sangue¦Armée de sang¦Blutarmee
necromancer.desc¦Ergue 4 servos com Força II e Resistência II. Passiva (Mestre): seus servos são mais resistentes e o dano deles cura você.¦Relève 4 serviteurs avec Force II et Résistance II. Passif (Maître) : vos serviteurs sont plus robustes et leurs dégâts vous soignent.¦Erweckt 4 Diener mit Stärke II und Resistenz II. Passiv (Meister): Deine Diener sind widerstandsfähiger und ihr Schaden heilt dich.
druid¦Renovação da Floresta (passiva)¦Renouveau de la forêt (passif)¦Waldneubeginn (passiv)
druid.desc¦Ao morrer, você revive como com um Totem da Imortalidade. Uma vez a cada 5 minutos.¦À votre mort, vous ressuscitez comme avec un Totem d'immortalité. Une fois toutes les 5 minutes.¦Beim Tod wirst du wie durch ein Totem der Unsterblichkeit wiederbelebt. Einmal alle 5 Minuten.
merchant¦Senhor do Mercado (passiva)¦Seigneur du marché (passif)¦Marktherr (passiv)
merchant.desc¦Herói da Vila V permanente (descontos enormes) e Sorte.¦Héros du village V permanent (énormes remises) et Chance.¦Dauerhaft Held des Dorfes V (riesige Rabatte) und Glück.
monk¦Bênção do Mestre¦Bénédiction du maître¦Segen des Meisters
monk.desc¦Cura totalmente aliados em 10 blocos e concede Força II, Velocidade II, Resistência II e Regeneração II (20 s).¦Soigne entièrement les alliés dans un rayon de 10 blocs et accorde Force II, Vitesse II, Résistance II et Régénération II (20 s).¦Heilt Verbündete im Umkreis von 10 Blöcken vollständig und gewährt Stärke II, Schnelligkeit II, Resistenz II und Regeneration II (20 s).
elementalist¦Bastião Elemental¦Bastion élémentaire¦Elementarbastion
elementalist.desc¦Ergue uma parede do elemento principal e libera seu domínio por 10 s, ferindo todos os inimigos dentro.¦Érige un mur de votre élément principal et libère son domaine pendant 10 s, blessant tous les ennemis à l'intérieur.¦Errichtet eine Wand deines Hauptelements und entfesselt dessen Domäne für 10 s, die alle Feinde darin verletzt.
shapeshifter¦Caos de Formas¦Chaos des formes¦Gestaltenchaos
shapeshifter.desc¦Pegadinha: transforma jogadores próximos em galinhas por 10 s e faz monstros levitarem.¦Farce : transforme les joueurs proches en poules pendant 10 s et fait léviter les monstres.¦Streich: Verwandelt nahe Spieler für 10 s in Hühner und lässt Monster schweben.
gunslinger¦Rajada Infinita¦Barrage sans fin¦Endloses Sperrfeuer
gunslinger.desc¦20 disparos em 2 s contra inimigos próximos.¦20 tirs en 2 s sur les ennemis proches.¦20 Schüsse in 2 s auf nahe Feinde.
weapon_summoner¦Mil Lâminas¦Mille lames¦Tausend Klingen
weapon_summoner.desc¦Um anel de lâminas em 5 blocos: 12/14/16 de dano e recuo.¦Un cercle de lames sur 5 blocs : 12/14/16 dégâts et recul.¦Ein Klingenring im Umkreis von 5 Blöcken: 12/14/16 Schaden und Rückstoß.
hunter¦Disparo do Caçador¦Tir du chasseur¦Jägerschuss
hunter.desc¦Um disparo que atravessa tudo em 40 blocos: 14/16/18 de dano.¦Un tir qui transperce tout sur 40 blocs : 14/16/18 dégâts.¦Ein Schuss, der alles auf 40 Blöcken durchdringt: 14/16/18 Schaden.
bard¦Sinfonia de Guerra¦Symphonie de guerre¦Kriegssymphonie
bard.desc¦Aliados em 16 blocos: Força II, Velocidade II e Regeneração por 15 s.¦Alliés dans un rayon de 16 blocs : Force II, Vitesse II et Régénération pendant 15 s.¦Verbündete im Umkreis von 16 Blöcken: Stärke II, Schnelligkeit II und Regeneration für 15 s.
warlock¦Portal do Abismo¦Porte de l'abîme¦Abgrundtor
warlock.desc¦Abre um portal na direção do olhar: 8/10/12 de dano, Decomposição II e levitação em inimigos em 8 blocos.¦Ouvre une porte au point visé : 8/10/12 dégâts, Wither II et lévitation aux ennemis dans un rayon de 8 blocs.¦Öffnet ein Tor am Blickziel: 8/10/12 Schaden, Ausdörrung II und Schweben für Feinde im Umkreis von 8 Blöcken.
tamer¦Matilha Espectral¦Meute spectrale¦Spektralrudel
tamer.desc¦Invoca 4 lobos aliados com Força II por 30 s.¦Invoque 4 loups alliés avec Force II pendant 30 s.¦Beschwört 4 verbündete Wölfe mit Stärke II für 30 s.
""")

add('unique.aptitudes.', """
adaptation¦Roda da Adaptação¦Roue d'adaptation¦Rad der Anpassung
adaptation.desc¦Passiva: cada golpe do mesmo tipo de dano aumenta sua resistência em 10% por 60 s (máx. 80%). Ativa: 10 s de imunidade ao último tipo de dano recebido e Regeneração II.¦Passif : chaque coup du même type de dégâts augmente votre résistance de 10% pendant 60 s (max 80%). Actif : 10 s d'immunité au dernier type de dégâts subi et Régénération II.¦Passiv: Jeder Treffer desselben Schadenstyps erhöht deine Resistenz dagegen für 60 s um 10% (max. 80%). Aktiv: 10 s Immunität gegen den zuletzt erlittenen Schadenstyp und Regeneration II.
adaptation.condition¦Receber dano de %s/%s tipos diferentes¦Subir %s/%s types de dégâts différents¦Schaden durch %s/%s verschiedene Typen erleiden
greed¦Coroa da Ganância¦Couronne d'avidité¦Krone der Gier
greed.desc¦Passiva: 15% de esmeralda extra ao abater. Ativa: rouba todos os efeitos benéficos de quem estiver em 8 blocos.¦Passif : 15% d'émeraude supplémentaire à l'élimination. Actif : vole tous les effets bénéfiques de quiconque dans un rayon de 8 blocs.¦Passiv: 15% Chance auf einen zusätzlichen Smaragd bei Tötungen. Aktiv: Stiehlt alle positiven Effekte von allen im Umkreis von 8 Blöcken.
greed.condition¦Carregar %s/%s blocos de esmeralda¦Porter %s/%s blocs d'émeraude¦%s/%s Smaragdblöcke tragen
void_archive¦Arquivo do Vazio¦Archives du vide¦Archiv der Leere
void_archive.desc¦Passiva: memoriza a última habilidade de classe usada por qualquer jogador em 16 blocos. Ativa: você a lança no grau 3.¦Passif : mémorise la dernière capacité de rôle utilisée par un joueur dans un rayon de 16 blocs. Actif : vous la lancez au rang 3.¦Passiv: Merkt sich die letzte Rollenfähigkeit eines Spielers im Umkreis von 16 Blöcken. Aktiv: Du wirkst sie selbst auf Rang 3.
void_archive.condition¦Classes no nível 10+: %s/%s¦Rôles au niveau 10+ : %s/%s¦Rollen auf Stufe 10+: %s/%s
fungal¦Praga Fúngica¦Peste fongique¦Pilzplage
fungal.desc¦Passiva: seus golpes infectam (veneno), espalhando-se na morte. Ativa: nuvem de esporos por 6 s (Veneno II, Cegueira).¦Passif : vos coups infectent (poison), qui se propage à la mort. Actif : nuage de spores pendant 6 s (Poison II, Cécité).¦Passiv: Deine Treffer infizieren (Gift), das sich beim Tod ausbreitet. Aktiv: 6 s Sporenwolke (Gift II, Blindheit).
fungal.condition¦Abates sobre micélio: %s/%s¦Éliminations sur mycélium : %s/%s¦Tötungen auf Myzel: %s/%s
parasite¦Parasita¦Parasite¦Parasit
parasite.desc¦Passiva: 10% de roubo de vida. Ativa: prende-se a um alvo, drenando 2 de vida por segundo por 5 s enquanto fica invisível.¦Passif : 10% de vol de vie. Actif : s'accroche à une cible, drainant 2 points de santé par seconde pendant 5 s en restant invisible.¦Passiv: 10% Lebensraub. Aktiv: Hefte dich unsichtbar an ein Ziel und entziehe ihm 5 s lang 2 Leben pro Sekunde.
parasite.condition¦Abates com 3 corações ou menos: %s/%s¦Éliminations avec 3 cœurs ou moins : %s/%s¦Tötungen mit höchstens 3 Herzen: %s/%s
hellfire¦Fogo Infernal¦Feu infernal¦Höllenfeuer
hellfire.desc¦Passiva: imunidade ao fogo; seus golpes queimam. Ativa: nova infernal (10 de dano em área e fogo no chão).¦Passif : immunité au feu ; vos coups brûlent. Actif : nova infernale (10 dégâts de zone et feu au sol).¦Passiv: Feuerimmunität; deine Treffer entzünden. Aktiv: Höllennova (10 Flächenschaden und Feuer am Boden).
hellfire.condition¦Segundos na lava: %s/%s¦Secondes dans la lave : %s/%s¦Sekunden in Lava: %s/%s
chosen_king¦Rei Escolhido¦Roi élu¦Auserwählter König
chosen_king.desc¦Passiva: +4 corações; Força e Regeneração para aliados em 12 blocos. Ativa: Decreto Real (inimigos em 16 blocos ficam quase imóveis e enfraquecidos).¦Passif : +4 cœurs ; Force et Régénération aux alliés dans un rayon de 12 blocs. Actif : Décret royal (ennemis dans un rayon de 16 blocs presque immobilisés et affaiblis).¦Passiv: +4 Herzen; Stärke und Regeneration für Verbündete im Umkreis von 12 Blöcken. Aktiv: Königliches Dekret (Feinde im Umkreis von 16 Blöcken fast bewegungslos und geschwächt).
chosen_king.condition¦Derrotar o Wither e o Dragão: %s/%s¦Vaincre le Wither et le Dragon : %s/%s¦Wither und Drache besiegen: %s/%s
spacetime¦Senhor do Espaço-Tempo¦Seigneur de l'espace-temps¦Herr der Raumzeit
spacetime.desc¦Passiva: posição e vida registradas a cada segundo. Ativa: retrocede 5 segundos (posição e vida).¦Passif : votre position et votre santé sont enregistrées chaque seconde. Actif : remonte de 5 secondes (position et santé).¦Passiv: Position und Leben werden jede Sekunde gespeichert. Aktiv: 5 Sekunden zurückspulen (Position und Leben).
spacetime.condition¦Pérolas do Ender usadas: %s/%s¦Perles de l'Ender utilisées : %s/%s¦Benutzte Enderperlen: %s/%s
negation¦Domínio de Negação¦Domaine de négation¦Zone der Negation
negation.desc¦Passiva: imunidade a efeitos negativos. Ativa: área de 8 s onde outros jogadores NÃO podem usar habilidades e criaturas perdem seus efeitos.¦Passif : immunité aux effets négatifs. Actif : zone de 8 s où les autres joueurs NE PEUVENT PAS utiliser de capacités et les créatures perdent leurs effets.¦Passiv: immun gegen negative Effekte. Aktiv: 8 s Zone, in der andere Spieler KEINE Fähigkeiten nutzen können und Kreaturen ihre Effekte verlieren.
negation.condition¦Baldes de leite bebidos: %s/%s¦Seaux de lait bus : %s/%s¦Getrunkene Milcheimer: %s/%s
geneticist¦Transmutador Natural¦Alchimiste du vivant¦Naturwandler
geneticist.desc¦Passiva: filhotes próximos crescem 10× mais rápido. Ativa: transforma animais próximos em outras espécies.¦Passif : les jeunes animaux proches grandissent 10× plus vite. Actif : transforme les animaux proches en d'autres espèces.¦Passiv: Nahe Jungtiere wachsen 10× schneller. Aktiv: Verwandelt nahe Tiere in andere Arten.
geneticist.condition¦Animais reproduzidos: %s/%s¦Animaux reproduits : %s/%s¦Gezüchtete Tiere: %s/%s
mirror¦Espelho Perfeito¦Miroir parfait¦Perfekter Spiegel
mirror.desc¦Ativa: copia a criatura que você olha (qualquer criatura): transforma-se nela por 30 s e ganha metade da vida máxima dela.¦Actif : copie la créature regardée (n'importe laquelle) : prenez sa forme pendant 30 s et gagnez la moitié de sa santé maximale.¦Aktiv: Kopiere die anvisierte Kreatur (jede beliebige): Werde für 30 s zu ihr und erhalte die Hälfte ihres maximalen Lebens.
mirror.condition¦Formas desbloqueadas: %s/%s¦Formes débloquées : %s/%s¦Freigeschaltete Formen: %s/%s
exploiter¦Explorador de Recursos¦Exploiteur¦Ausbeuter
exploiter.desc¦Passiva: 25% de minério duplo. Ativa: 15 s de saque dobrado das criaturas abatidas e Sorte III.¦Passif : 25% de minerais doublés. Actif : 15 s de butin doublé sur vos éliminations et Chance III.¦Passiv: 25% doppelte Erze. Aktiv: 15 s doppelte Beute von getöteten Kreaturen und Glück III.
exploiter.condition¦Nível de Mineração: %s/%s¦Niveau de Minage : %s/%s¦Bergbaustufe: %s/%s
pendragon¦Pendragon¦Pendragon¦Pendragon
pendragon.desc¦Passiva: com espada, +4 de armadura e +10% de dano. Ativa: Excalibur, um raio de luz de 30 blocos (16 de dano, dobro contra mortos-vivos).¦Passif : avec une épée, +4 d'armure et +10% de dégâts. Actif : Excalibur, rayon de lumière de 30 blocs (16 dégâts, doublés contre les morts-vivants).¦Passiv: Mit einem Schwert +4 Rüstung und +10% Schaden. Aktiv: Excalibur, ein 30 Blöcke langer Lichtstrahl (16 Schaden, doppelt gegen Untote).
pendragon.condition¦Espadachim ou Paladino Definitivo: %s/%s¦Épéiste ou Paladin Ultime : %s/%s¦Ultimativer Schwertkämpfer oder Paladin: %s/%s
sky_lord¦Senhor dos Céus¦Seigneur des cieux¦Herr der Lüfte
sky_lord.desc¦Passiva: imunidade a dano de queda. Ativa: 12 s de voo livre.¦Passif : immunité aux dégâts de chute. Actif : 12 s de vol libre.¦Passiv: immun gegen Fallschaden. Aktiv: 12 s freier Flug.
sky_lord.condition¦Quilômetros voados com élitros: %s/%s¦Kilomètres parcourus en élytres : %s/%s¦Mit Elytren geflogene Kilometer: %s/%s
stillness¦Quietude Eterna¦Quiétude éternelle¦Ewige Stille
stillness.desc¦Passiva: Resistência ao ficar agachado e imóvel. Ativa: para o tempo por 5 s em 12 blocos (criaturas e projéteis congelados).¦Passif : Résistance en restant accroupi immobile. Actif : arrête le temps pendant 5 s dans un rayon de 12 blocs (créatures et projectiles figés).¦Passiv: Resistenz beim reglosen Schleichen. Aktiv: Hält die Zeit für 5 s im Umkreis von 12 Blöcken an (Kreaturen und Projektile eingefroren).
stillness.condition¦Minutos agachado: %s/%s¦Minutes accroupi : %s/%s¦Geschlichene Minuten: %s/%s
harbinger¦Arauto da Morte¦Héraut de la mort¦Todesbote
harbinger.desc¦Passiva: mortos-vivos não atacam você. Ativa: sentencia um alvo; após 3 s ele perde 50% da vida máxima (10% para chefes).¦Passif : les morts-vivants ne vous attaquent pas. Actif : condamne une cible ; après 3 s, elle perd 50% de sa santé maximale (10% pour les boss).¦Passiv: Untote greifen dich nicht an. Aktiv: Verurteile ein Ziel; nach 3 s verliert es 50% seines maximalen Lebens (10% bei Bossen).
harbinger.condition¦Suas mortes: %s/%s¦Vos morts : %s/%s¦Eigene Tode: %s/%s
skypiercer¦Perfurador dos Céus¦Perce-ciel¦Himmelsdurchbohrer
skypiercer.desc¦Passiva: +1 de alcance. Ativa: uma lança celestial cai onde você olha: 14 de dano em 4 blocos, lançando inimigos.¦Passif : +1 d'allonge. Actif : une lance céleste tombe au point visé : 14 dégâts dans un rayon de 4 blocs, projetant les ennemis.¦Passiv: +1 Reichweite. Aktiv: Eine himmlische Lanze fällt auf dein Blickziel: 14 Schaden im Umkreis von 4 Blöcken und schleudert Feinde hoch.
skypiercer.condition¦¦¦
world_tree¦Árvore do Mundo¦Arbre-monde¦Weltenbaum
world_tree.desc¦Passiva: Regeneração sobre terra, grama ou folhas. Ativa: bosque sagrado por 8 s (8 blocos): jogadores curam 2/s e inimigos quase se enraízam.¦Passif : Régénération sur terre, herbe ou feuilles. Actif : bosquet sacré pendant 8 s (8 blocs) : les joueurs récupèrent 2/s et les ennemis sont presque immobilisés.¦Passiv: Regeneration auf Erde, Gras oder Laub. Aktiv: Heiliger Hain für 8 s (8 Blöcke): Spieler heilen 2/s und Feinde sind fast verwurzelt.
world_tree.condition¦¦¦
last_bullet¦A Última Bala¦La dernière balle¦Die letzte Kugel
last_bullet.desc¦Passiva: +10%% de dano à distância. Ativa: disparo de 40 blocos: 30 de dano (60 se o alvo estiver abaixo de metade da vida).¦Passif : +10%% de dégâts à distance. Actif : tir de 40 blocs : 30 dégâts (60 si la cible est sous la moitié de sa santé).¦Passiv: +10%% Fernkampfschaden. Aktiv: Ein Schuss über 40 Blöcke: 30 Schaden (60 bei weniger als halbem Leben des Ziels).
last_bullet.condition¦¦¦
gate_of_arms¦Portal de Armas¦Porte des armes¦Waffentor
gate_of_arms.desc¦Passiva: +1 lâmina flutuante máxima. Ativa: 30 espadas caem em 10 blocos ao seu redor por 3 s (4 de dano cada).¦Passif : +1 lame flottante maximale. Actif : 30 épées pleuvent dans un rayon de 10 blocs pendant 3 s (4 dégâts chacune).¦Passiv: +1 maximale schwebende Klinge. Aktiv: Ein 3 s langer Schwertregen mit 30 Schwertern im Umkreis von 10 Blöcken (je 4 Schaden).
gate_of_arms.condition¦¦¦
""")

add('path_effect.aptitudes.', """
lifesteal¦Roubo de vida¦Vol de vie¦Lebensraub
crit¦Acerto crítico (×1.5)¦Coup critique (×1.5)¦Kritischer Treffer (×1.5)
execute¦Execução (+dano contra inimigos abaixo de 35% de vida)¦Exécution (+dégâts contre les ennemis sous 35% de santé)¦Hinrichtung (+Schaden gegen Feinde unter 35% Leben)
thorns¦Espinhos (reflete dano corpo a corpo)¦Épines (renvoie les dégâts de mêlée)¦Dornen (wirft Nahkampfschaden zurück)
damage_reduction¦Redução de dano¦Réduction des dégâts¦Schadensverringerung
dodge¦Esquiva¦Esquive¦Ausweichen
mana_regen¦Regeneração de mana (por segundo)¦Régénération de mana (par seconde)¦Manaregeneration (pro Sekunde)
resource_gain¦Ganho de recurso de classe¦Gain de ressource de rôle¦Rollenressourcengewinn
regen¦Regeneração de vida (a cada 3 s)¦Régénération de santé (toutes les 3 s)¦Lebensregeneration (alle 3 s)
on_hit_poison¦Chance de envenenar¦Chance d'empoisonner¦Vergiftungschance
on_hit_slow¦Chance de desacelerar¦Chance de ralentir¦Verlangsamungschance
on_hit_fire¦Chance de queimar¦Chance de brûler¦Entzündungschance
on_hit_wither¦Chance de causar decomposição¦Chance d'infliger Wither¦Ausdörrungschance
minion_power¦Poder dos servos¦Puissance des serviteurs¦Dienerstärke
knockback¦Resistência a recuo¦Résistance au recul¦Rückstoßresistenz
speed¦Velocidade¦Vitesse¦Geschwindigkeit
max_health¦Vida máxima¦Santé maximale¦Maximales Leben
attack_speed¦Velocidade de ataque¦Vitesse d'attaque¦Angriffsgeschwindigkeit
luck¦Sorte¦Chance¦Glück
""")

add('setbonus.aptitudes.', """
berserker¦Couraça Furiosa¦Cuirasse de fureur¦Zornharnisch
berserker.desc¦+10%% de dano; Força I abaixo de 50%% de vida¦+10%% de dégâts ; Force I sous 50%% de santé¦+10%% Schaden; Stärke I unter 50%% Leben
mage¦Vestes do Arquimago¦Robes d'archimage¦Erzmagierrobe
mage.desc¦−25%% de custo de mana; +15%% de dano mágico¦−25%% de coût en mana ; +15%% de dégâts magiques¦−25%% Manakosten; +15%% magischer Schaden
assassin¦Manto das Sombras¦Voile d'ombre¦Schattenhülle
assassin.desc¦+10%% de velocidade; +30%% de dano contra alvos que não atacam você¦+10%% de vitesse ; +30%% de dégâts aux cibles qui ne vous attaquent pas¦+10%% Geschwindigkeit; +30%% Schaden gegen Ziele, die dich nicht angreifen
herbalist¦Traje do Boticário¦Tenue d'apothicaire¦Apothekergewand
herbalist.desc¦Imunidade a veneno e fome; cura 1 de vida a cada 5 s¦Immunité au poison et à la faim ; soigne 1 PV toutes les 5 s¦Immun gegen Gift und Hunger; heilt alle 5 s 1 Leben
engineer¦Exoesqueleto de Cobre¦Exoarmure en cuivre¦Kupferexoskelett
engineer.desc¦−40%% de dano de explosões; −15%% de recarga¦−40%% de dégâts d'explosion ; −15%% de recharge¦−40%% Explosionsschaden; −15%% Abklingzeit
lancer¦Armadura do Dragoneiro¦Armure de dragon¦Dragonerrüstung
lancer.desc¦+0.75 de alcance; +20%% de dano ao correr ou cavalgar¦+0.75 d'allonge ; +20%% de dégâts en sprintant ou à cheval¦+0.75 Reichweite; +20%% Schaden beim Sprinten oder Reiten
swordsman¦Armadura do Duelista¦Armure du duelliste¦Duellantenrüstung
swordsman.desc¦+10%% de velocidade de ataque; +15%% de dano com espadas¦+10%% de vitesse d'attaque ; +15%% de dégâts à l'épée¦+10%% Angriffsgeschwindigkeit; +15%% Schwertschaden
paladin¦Placas Consagradas¦Plaques consacrées¦Geweihte Plattenrüstung
paladin.desc¦+4 de vida máxima; −30%% de dano de mortos-vivos e +25%% contra eles¦+4 de santé maximale ; −30%% de dégâts des morts-vivants et +25%% contre eux¦+4 maximales Leben; −30%% Schaden durch Untote und +25%% gegen sie
necromancer¦Manto do Ossuário¦Voile d'ossuaire¦Beinhaushülle
necromancer.desc¦10%% de roubo de vida; imunidade a Decomposição¦10%% de vol de vie ; immunité à Wither¦10%% Lebensraub; immun gegen Ausdörrung
druid¦Casca Anciã¦Écorce ancienne¦Uralte Borke
druid.desc¦Regeneração à luz do sol; imunidade a cactos e arbustos¦Régénération au soleil ; immunité aux cactus et buissons¦Regeneration im Sonnenlicht; immun gegen Kakteen und Büsche
merchant¦Traje da Guilda¦Tenue de guilde¦Gildengewand
merchant.desc¦+2 de sorte; Herói da Vila permanente¦+2 de chance ; Héros du village permanent¦+2 Glück; dauerhaft Held des Dorfes
monk¦Quimono do Punho Sereno¦Gi du poing serein¦Gi der stillen Faust
monk.desc¦+30%% de dano desarmado; Supersalto I¦+30%% de dégâts à mains nues ; Sauts améliorés I¦+30%% unbewaffneter Schaden; Sprungkraft I
elementalist¦Manto dos Quatro Ventos¦Manteau des quatre vents¦Mantel der vier Winde
elementalist.desc¦Resistência ao fogo; sem congelamento; +15%% de dano mágico¦Résistance au feu ; aucun gel ; +15%% de dégâts magiques¦Feuerresistenz; kein Erfrieren; +15%% magischer Schaden
shapeshifter¦Pele Mutável¦Peau changeante¦Wandelhaut
shapeshifter.desc¦−15%% de recarga; +10%% de velocidade; visão noturna¦−15%% de recharge ; +10%% de vitesse ; vision nocturne¦−15%% Abklingzeit; +10%% Geschwindigkeit; Nachtsicht
gunslinger¦Sobretudo do Pistoleiro¦Cache-poussière du pistolero¦Revolverheldenmantel
gunslinger.desc¦+10%% de velocidade; +15%% de dano à distância¦+10%% de vitesse ; +15%% de dégâts à distance¦+10%% Geschwindigkeit; +15%% Fernkampfschaden
weapon_summoner¦Placas do Arsenal¦Plaques de l'arsenal¦Arsenalplattenrüstung
weapon_summoner.desc¦+3 de armadura; +15%% de dano corpo a corpo¦+3 d'armure ; +15%% de dégâts de mêlée¦+3 Rüstung; +15%% Nahkampfschaden
hunter¦Traje do Rastreador¦Tenue du pisteur¦Fährtensucherkleidung
hunter.desc¦+10%% de velocidade; visão noturna¦+10%% de vitesse ; vision nocturne¦+10%% Geschwindigkeit; Nachtsicht
bard¦Traje do Trovador¦Tenue du troubadour¦Troubadourgewand
bard.desc¦+4 de vida máxima; sorte para aliados próximos¦+4 de santé maximale ; chance aux alliés proches¦+4 maximales Leben; Glück für nahe Verbündete
warlock¦Vestes Amaldiçoadas¦Vêtements maudits¦Verfluchte Gewänder
warlock.desc¦8%% de roubo de vida; imunidade a Decomposição¦8%% de vol de vie ; immunité à Wither¦8%% Lebensraub; immun gegen Ausdörrung
tamer¦Peles do Domador¦Fourrures du dompteur¦Tierbändigerfelle
tamer.desc¦+4 de vida máxima; seus animais ganham Resistência¦+4 de santé maximale ; vos animaux gagnent Résistance¦+4 maximales Leben; deine Tiere erhalten Resistenz
""")

add('milestone.aptitudes.', """
mining.10¦Olhos de Mineiro¦Yeux de mineur¦Bergmannsaugen
mining.10.desc¦Visão noturna abaixo de Y=40¦Vision nocturne sous Y=40¦Nachtsicht unter Y=40
mining.20¦Picareta Ágil¦Pioche agile¦Flinke Spitzhacke
mining.20.desc¦Pressa com picareta (Pressa II no 50)¦Célérité avec une pioche (Célérité II au 50)¦Eile mit Spitzhacke (Eile II auf 50)
mining.30¦Veio Rico¦Filon riche¦Reiche Ader
mining.30.desc¦10%% de minérios dobrados¦10%% de minerais doublés¦10%% doppelte Erze
mining.40¦Sentido de Minério¦Sens du minerai¦Erzgespür
mining.40.desc¦Com picareta, destaca minérios próximos a cada 3 s¦Avec une pioche, révèle les minerais proches toutes les 3 s¦Mit Spitzhacke: hebt alle 3 s nahe Erze hervor
mining.50¦Mestre Mineiro¦Maître mineur¦Meisterbergmann
mining.50.desc¦Pressa II com picareta¦Célérité II avec une pioche¦Eile II mit Spitzhacke
excavation.10¦Pá Veloz¦Pelle rapide¦Schnelle Schaufel
excavation.10.desc¦Pressa com pá¦Célérité avec une pelle¦Eile mit Schaufel
excavation.20¦Caçador de Tesouros¦Chasseur de trésors¦Schatzsucher
excavation.20.desc¦3%% de tesouro ao escavar¦3%% de trésor en creusant¦3%% Schatzchance beim Graben
excavation.30¦Pés de Toupeira¦Pattes de taupe¦Maulwurfsfüße
excavation.30.desc¦−50%% de dano de queda¦−50%% de dégâts de chute¦−50%% Fallschaden
excavation.40¦Escavador Perito¦Terrassier expert¦Erfahrener Gräber
excavation.40.desc¦Pressa II com pá¦Célérité II avec une pelle¦Eile II mit Schaufel
excavation.50¦Arqueólogo¦Archéologue¦Archäologe
excavation.50.desc¦6%% de tesouro, incluindo diamantes¦6%% de trésor, dont des diamants¦6%% Schatzchance, einschließlich Diamanten
woodcutting.10¦Machado Veloz¦Hache rapide¦Schnelle Axt
woodcutting.10.desc¦Pressa com machado¦Célérité avec une hache¦Eile mit Axt
woodcutting.20¦Coletor¦Cueilleur¦Sammler
woodcutting.20.desc¦10%% de maçã ou muda ao cortar troncos¦10%% de pomme ou pousse en coupant des bûches¦10%% Chance auf Apfel oder Setzling aus Stämmen
woodcutting.30¦Lenhador¦Bûcheron¦Holzfäller
woodcutting.30.desc¦Agache com machado: derruba a árvore (32 troncos)¦Accroupi avec une hache : abat l'arbre (32 bûches)¦Mit Axt schleichen: fällt den Baum (32 Stämme)
woodcutting.40¦Machado Perito¦Hache experte¦Meisteraxt
woodcutting.40.desc¦Pressa II com machado¦Célérité II avec une hache¦Eile II mit Axt
woodcutting.50¦Lenhador Lendário¦Bûcheron légendaire¦Legendärer Holzfäller
woodcutting.50.desc¦Derruba até 96 troncos¦Abat jusqu'à 96 bûches¦Fällt bis zu 96 Stämme
farming.10¦Bem Alimentado¦Bien nourri¦Gut genährt
farming.10.desc¦Saturação a cada minuto¦Saturation chaque minute¦Sättigung jede Minute
farming.20¦Replantio¦Replantation¦Nachpflanzen
farming.20.desc¦Plantações colhidas se replantam sozinhas¦Les cultures récoltées se replantent¦Geerntete Pflanzen werden nachgepflanzt
farming.30¦Mão Verde¦Main verte¦Grüner Daumen
farming.30.desc¦Plantações próximas crescem mais rápido¦Les cultures proches poussent plus vite¦Nahe Pflanzen wachsen schneller
farming.40¦Colheita Farta¦Récolte abondante¦Reiche Ernte
farming.40.desc¦10%% de colheita tripla¦10%% de récolte triple¦10%% dreifache Ernte
farming.50¦Afinidade da Terra¦Accord de la terre¦Erdverbundenheit
farming.50.desc¦Regeneração sobre terra arada¦Régénération sur terre labourée¦Regeneration auf Ackerboden
combat.10¦Golpe Firme¦Frappe ferme¦Fester Schlag
combat.10.desc¦+1 de dano¦+1 dégât¦+1 Schaden
combat.20¦Crítico Vital¦Critique vital¦Lebenskrit
combat.20.desc¦Críticos curam 1 coração¦Les critiques soignent 1 cœur¦Kritische Treffer heilen 1 Herz
combat.30¦Último Suspiro¦Dernier souffle¦Letzter Atemzug
combat.30.desc¦Força I com pouca vida¦Force I à faible santé¦Stärke I bei wenig Leben
combat.40¦Veterano¦Vétéran¦Veteran
combat.40.desc¦+2 de dano extra¦+2 dégâts supplémentaires¦+2 zusätzlicher Schaden
combat.50¦Frenesi¦Frénésie¦Raserei
combat.50.desc¦Abater monstros concede Velocidade e Regeneração¦Tuer des monstres accorde Vitesse et Régénération¦Monstertötungen gewähren Schnelligkeit und Regeneration
archery.10¦Olho de Falcão¦Œil de faucon¦Falkenauge
archery.10.desc¦Flechas aplicam Brilho¦Les flèches appliquent Surbrillance¦Pfeile verleihen Leuchten
archery.20¦Aljava Infinita¦Carquois sans fond¦Bodenloser Köcher
archery.20.desc¦25%% de chance de conservar a flecha¦25%% de chance de conserver la flèche¦25%% Chance, den Pfeil zu behalten
archery.30¦Força da Puxada¦Puissance de traction¦Zugkraft
archery.30.desc¦+30%% de velocidade de flechas¦+30%% de vitesse des flèches¦+30%% Pfeilgeschwindigkeit
archery.40¦Flecha Pesada¦Flèche lourde¦Schwerer Pfeil
archery.40.desc¦Flechas desaceleram¦Les flèches ralentissent¦Pfeile verlangsamen
archery.50¦Perfuração¦Perforation¦Durchschuss
archery.50.desc¦Flechas atravessam 2 inimigos¦Les flèches traversent 2 ennemis¦Pfeile durchdringen 2 Feinde
defense.10¦Pele à Prova de Fogo¦Peau ignifuge¦Feuerfeste Haut
defense.10.desc¦−20%% de dano de fogo¦−20%% de dégâts de feu¦−20%% Feuerschaden
defense.20¦Barreira¦Barrière¦Barriere
defense.20.desc¦Absorção a cada minuto¦Absorption chaque minute¦Absorption jede Minute
defense.30¦Espinhos¦Épines¦Dornen
defense.30.desc¦Reflete 10%% do dano corpo a corpo¦Renvoie 10%% des dégâts de mêlée¦Wirft 10%% des Nahkampfschadens zurück
defense.40¦Bastião¦Bastion¦Bastion
defense.40.desc¦Resistência I com pouca vida¦Résistance I à faible santé¦Resistenz I bei wenig Leben
defense.50¦Segunda Chance¦Seconde chance¦Zweite Chance
defense.50.desc¦Sobrevive a um golpe fatal (a cada 10 min)¦Survit à un coup fatal (toutes les 10 min)¦Überlebe einen tödlichen Treffer (alle 10 Min.)
agility.10¦Saltitante¦Rebondissant¦Sprunghaft
agility.10.desc¦Supersalto I permanente¦Sauts améliorés I permanents¦Dauerhaft Sprungkraft I
agility.20¦Pouso Suave¦Atterrissage en douceur¦Sanfte Landung
agility.20.desc¦−50%% de dano de queda¦−50%% de dégâts de chute¦−50%% Fallschaden
agility.30¦Velocista¦Sprinteur¦Sprinter
agility.30.desc¦Velocidade I ao correr¦Vitesse I en sprintant¦Schnelligkeit I beim Sprinten
agility.40¦Passo Seguro¦Pas assuré¦Sicherer Tritt
agility.40.desc¦Sobe blocos sem pular¦Monte les blocs sans sauter¦Steigt ohne Sprung auf Blöcke
agility.50¦Pena¦Plume¦Feder
agility.50.desc¦Imunidade a dano de queda¦Immunité aux dégâts de chute¦Immun gegen Fallschaden
fishing.10¦Paciência¦Patience¦Geduld
fishing.10.desc¦+2-4 XP vanilla por peixe¦+2-4 XP vanilla par poisson¦+2-4 Vanilla-EP pro Fisch
fishing.20¦Nadadeiras¦Nageoires¦Flossen
fishing.20.desc¦Graça do Golfinho ao nadar¦Grâce du dauphin en nageant¦Gunst des Delfins beim Schwimmen
fishing.30¦Tesouro Submerso¦Trésor englouti¦Versunkener Schatz
fishing.30.desc¦5%% de tesouro marinho por peixe¦5%% de trésor marin par poisson¦5%% Meeresschatz pro Fisch
fishing.40¦Afinidade do Mar¦Accord de la mer¦Meeresverbundenheit
fishing.40.desc¦Poder do Aqueduto na água¦Force de conduit dans l'eau¦Meereskraft im Wasser
fishing.50¦Senhor das Marés¦Seigneur des marées¦Herr der Gezeiten
fishing.50.desc¦10%% de tesouro, pode incluir um Coração do Mar¦10%% de trésor, peut contenir un Cœur de la mer¦10%% Schatzchance, kann ein Herz des Meeres enthalten
enchanting.10¦Economia de Lápis-Lazúli¦Économie de lapis¦Lapissparer
enchanting.10.desc¦25%% de chance de recuperar 1 lápis-lazúli ao encantar¦25%% de chance de récupérer 1 lapis en enchantant¦25%% Chance auf 1 Lapis zurück beim Verzaubern
enchanting.20¦Mente Arcana¦Esprit arcanique¦Arkaner Geist
enchanting.20.desc¦+10 de mana máximo¦+10 de mana maximal¦+10 maximales Mana
enchanting.30¦Saber Engarrafado¦Savoir en bouteille¦Wissen in Flaschen
enchanting.30.desc¦20%% de chance de 2 frascos de experiência ao encantar¦20%% de chance d'obtenir 2 fioles d'expérience en enchantant¦20%% Chance auf 2 Erfahrungsfläschchen beim Verzaubern
enchanting.40¦Fluxo Arcano¦Flux arcanique¦Arkaner Fluss
enchanting.40.desc¦−10%% de recarga de habilidades¦−10%% de recharge des capacités¦−10%% Fähigkeitsabklingzeit
enchanting.50¦Arquiencantador¦Archienchanteur¦Erzverzauberer
enchanting.50.desc¦Cada encantamento restitui 1 nível¦Chaque enchantement rembourse 1 niveau¦Jede Verzauberung erstattet 1 Stufe
""")

add('aptitudes.', """
milestone.unlocked¦✦ Marco de %s (nível %s): %s¦✦ Palier de %s (niveau %s) : %s¦✦ Meilenstein für %s (Stufe %s): %s
milestone.second_chance¦Segunda chance! Você sobreviveu a um golpe fatal¦Seconde chance ! Vous avez survécu à un coup fatal¦Zweite Chance! Du hast einen tödlichen Treffer überlebt
toast.level¦Nv. %s¦Niv. %s¦St. %s
set.activated¦✦ Bônus de conjunto ativo: %s¦✦ Bonus d'ensemble actif : %s¦✦ Setbonus aktiv: %s
mech.counter_ready¦Reflexos! Seu próximo golpe causa +50%%¦Réflexes ! Votre prochain coup inflige +50%%¦Reflexe! Dein nächster Treffer verursacht +50%%
mech.free_cast¦Cargas arcanas: feitiço grátis¦Charges arcaniques : sort gratuit¦Arkane Ladungen: kostenloser Zauber
mech.overdrive¦SOBRECARGA!¦SURRÉGIME !¦ÜBERLASTUNG!
mech.encore¦Bis! A canção é lançada sem recarga¦Encore ! Le chant se déclenche sans recharge¦Zugabe! Das Lied wird ohne Abklingzeit ausgelöst
mech.crescendo¦Crescendo! Um final para seus aliados¦Crescendo ! Un final pour vos alliés¦Crescendo! Ein Finale für deine Verbündeten
""")

add('mech.aptitudes.', """
overdrive_on¦Sobrecarga %ss¦Surrégime %ss¦Überlastung %ss
day¦Dia: cura¦Jour : soin¦Tag: Heilung
night¦Noite: +dano¦Nuit : +dégâts¦Nacht: +Schaden
tiger¦Tigre¦Tigre¦Tiger
crane¦Garça¦Grue¦Kranich
neutral¦Sem postura¦Aucune posture¦Keine Haltung
reaction_on¦Reação %ss¦Réaction %ss¦Reaktion %ss
reaction_ready¦Troque de elemento¦Changez d'élément¦Element wechseln
""")

add('fusion.aptitudes.', """
firestorm¦Tempestade de Fogo¦Tempête de feu¦Feuersturm
firestorm.desc¦Fogo + Vento: a explosão incendeia e causa dano extra.¦Feu + Vent : l'explosion enflamme et inflige des dégâts supplémentaires.¦Feuer + Wind: Der Ausbruch entzündet und verursacht zusätzlichen Schaden.
electrocution¦Eletrocussão¦Électrocution¦Stromschlag
electrocution.desc¦Água + Relâmpago: dano extra e atordoamento de 2 s.¦Eau + Foudre : dégâts supplémentaires et étourdissement de 2 s.¦Wasser + Blitz: zusätzlicher Schaden und 2 s Betäubung.
blizzard¦Nevasca¦Blizzard¦Schneesturm
blizzard.desc¦Gelo + Água: congela e desacelera intensamente.¦Glace + Eau : gèle et ralentit fortement.¦Eis + Wasser: gefriert und verlangsamt stark.
eruption¦Erupção¦Éruption¦Eruption
eruption.desc¦Terra + Magma: lança inimigos no ar, queimando.¦Terre + Magma : projette les ennemis en l'air en les brûlant.¦Erde + Magma: schleudert Feinde brennend in die Luft.
plague¦Praga¦Peste¦Seuche
plague.desc¦Planta + Sombra: Veneno II e Decomposição.¦Plante + Ombre : Poison II et Wither.¦Pflanze + Schatten: Gift II und Verwesung.
eclipse¦Eclipse¦Éclipse¦Finsternis
eclipse.desc¦Luz + Sombra: cega inimigos e cura 4 de vida.¦Lumière + Ombre : aveugle les ennemis et vous soigne de 4.¦Licht + Schatten: blendet Feinde und heilt dich um 4.
crystal_singularity¦Singularidade de Cristal¦Singularité de cristal¦Kristallsingularität
crystal_singularity.desc¦Vazio + Cristal: atrai inimigos e causa dano.¦Vide + Cristal : attire les ennemis vers vous et les blesse.¦Leere + Kristall: zieht Feinde zu dir und verletzt sie.
resonance¦Ressonância¦Résonance¦Resonanz
resonance.desc¦Som + Vento: grande recuo e náusea.¦Son + Vent : fort recul et nausée.¦Klang + Wind: starker Rückstoß und Übelkeit.
prism¦Prisma¦Prisme¦Prisma
prism.desc¦Relâmpago + Cristal: raios extras nos 3 inimigos mais próximos.¦Foudre + Cristal : rayons supplémentaires sur les 3 ennemis les plus proches.¦Blitz + Kristall: zusätzliche Strahlen auf die 3 nächsten Feinde.
thermal_shock¦Choque Térmico¦Choc thermique¦Temperaturschock
thermal_shock.desc¦Fogo + Gelo: dano extra pela mudança súbita de temperatura.¦Feu + Glace : dégâts supplémentaires dus au changement brutal de température.¦Feuer + Eis: zusätzlicher Schaden durch den plötzlichen Temperaturwechsel.
vapor¦Vapor¦Vapeur¦Dampf
vapor.desc¦Fogo + Água: cegueira e lentidão em área.¦Feu + Eau : cécité et lenteur dans la zone.¦Feuer + Wasser: Blindheit und Verlangsamung im Gebiet.
green_tide¦Maré verde¦Marée verte¦Grüne Flut
green_tide.desc¦Água + Planta: cura e regenera os aliados próximos.¦Eau + Plante : soigne et régénère les alliés proches.¦Wasser + Pflanze: heilt und regeneriert nahe Verbündete.
""")

add('party.', """
error.cannot_invite¦Você não pode convidar esse jogador. Somente o líder pode convidar e o grupo deve ter espaço.¦Vous ne pouvez pas inviter ce joueur. Seul le chef peut inviter et le groupe doit avoir de la place.¦Du kannst diesen Spieler nicht einladen. Nur der Anführer kann einladen und die Gruppe muss Platz haben.
error.invite_expired¦Esse convite de grupo expirou.¦Cette invitation de groupe a expiré.¦Diese Gruppeneinladung ist abgelaufen.
error.party_full¦O grupo não tem mais espaço ou você já está em um grupo.¦Le groupe est complet ou vous êtes déjà dans un groupe.¦Die Gruppe ist voll oder du bist bereits in einer Gruppe.
error.no_invite¦Você não tem convite de grupo pendente.¦Vous n'avez aucune invitation de groupe en attente.¦Du hast keine offene Gruppeneinladung.
error.not_member¦Você não está em um grupo.¦Vous n'êtes pas dans un groupe.¦Du bist in keiner Gruppe.
error.cannot_kick¦Somente o líder pode remover outro membro do grupo.¦Seul le chef peut retirer un autre membre du groupe.¦Nur der Anführer kann andere Gruppenmitglieder entfernen.
error.leader_only¦Somente o líder do grupo pode alterar este ajuste.¦Seul le chef du groupe peut modifier ce réglage.¦Nur der Gruppenanführer kann diese Einstellung ändern.
invite.accept¦[Aceitar]¦[Accepter]¦[Annehmen]
invite.reject¦[Recusar]¦[Refuser]¦[Ablehnen]
invite.message¦%s convidou você para um grupo. %s %s¦%s vous invite dans un groupe. %s %s¦%s hat dich in eine Gruppe eingeladen. %s %s
invite.sent¦Convite de grupo enviado a %s.¦Invitation de groupe envoyée à %s.¦Gruppeneinladung an %s gesendet.
invite.rejected¦Convite de grupo recusado.¦Invitation de groupe refusée.¦Gruppeneinladung abgelehnt.
invite.rejected_by¦%s recusou o convite de grupo.¦%s a refusé l'invitation de groupe.¦%s hat die Gruppeneinladung abgelehnt.
joined.other¦%s entrou no grupo.¦%s a rejoint le groupe.¦%s ist der Gruppe beigetreten.
joined.self¦Você entrou no grupo.¦Vous avez rejoint le groupe.¦Du bist der Gruppe beigetreten.
disbanded¦O grupo foi desfeito pelo líder.¦Le groupe a été dissous par son chef.¦Der Anführer hat die Gruppe aufgelöst.
left.other¦%s saiu do grupo.¦%s a quitté le groupe.¦%s hat die Gruppe verlassen.
left.self¦Você saiu do grupo.¦Vous avez quitté le groupe.¦Du hast die Gruppe verlassen.
kicked.self¦O líder removeu você do grupo.¦Le chef vous a retiré du groupe.¦Der Anführer hat dich aus der Gruppe entfernt.
kicked.other¦%s removido do grupo.¦%s a été retiré du groupe.¦%s aus der Gruppe entfernt.
pvp.enabled¦Fogo amigo ativado.¦Tir allié activé.¦Freundbeschuss aktiviert.
pvp.disabled¦Fogo amigo desativado.¦Tir allié désactivé.¦Freundbeschuss deaktiviert.
share.changed¦XP compartilhada definida como %s%%.¦Partage d'XP défini à %s%%.¦Geteilte EP auf %s%% gesetzt.
list.none¦Você não está em um grupo.¦Vous n'êtes pas dans un groupe.¦Du bist in keiner Gruppe.
list.header¦Grupo · XP compartilhada %s%% · %s¦Groupe · XP partagée %s%% · %s¦Gruppe · geteilte EP %s%% · %s
gui.title¦Grupo¦Groupe¦Gruppe
gui.pvp¦Fogo amigo¦Tir allié¦Freundbeschuss
gui.leave¦Sair¦Quitter¦Verlassen
gui.invite¦Convidar¦Inviter¦Einladen
gui.loading¦Carregando dados do grupo…¦Chargement des données du groupe…¦Gruppendaten werden geladen…
gui.share¦XP compartilhada: %s%%¦XP partagée : %s%%¦Geteilte EP: %s%%
gui.pvp_enabled¦Fogo amigo: ativado¦Tir allié : activé¦Freundbeschuss: an
gui.pvp_disabled¦Fogo amigo: desativado¦Tir allié : désactivé¦Freundbeschuss: aus
gui.members¦Membros¦Membres¦Mitglieder
gui.players¦Jogadores conectados¦Joueurs connectés¦Verbundene Spieler
gui.scroll¦Rolar · %s / %s¦Défilement · %s / %s¦Scrollen · %s / %s
gui.none¦Você ainda não está em um grupo. Convide um jogador conectado para criar um.¦Vous n'êtes pas encore dans un groupe. Invitez un joueur connecté pour en créer un.¦Noch in keiner Gruppe. Lade einen verbundenen Spieler ein, um eine zu gründen.
hud.other_dimension¦Outra dimensão¦Autre dimension¦Andere Dimension
hud.offline¦Desconectado¦Déconnecté¦Offline
""")

add('contract.aptitudes.', """
loading¦Carregando contratos…¦Chargement des contrats…¦Aufträge werden geladen…
header¦Grau: %s · Reputação %s · Ativos %s/%s¦Rang : %s · Réputation %s · Actifs %s/%s¦Rang: %s · Ruf %s · Aktiv %s/%s
offers¦Ofertas de hoje¦Offres du jour¦Heutige Angebote
active¦Em andamento¦En cours¦In Arbeit
none¦Você não tem contratos em andamento.¦Vous n'avez aucun contrat en cours.¦Du hast keine laufenden Aufträge.
line¦[%s] %s: %s ×%s¦[%s] %s : %s ×%s¦[%s] %s: %s ×%s
line_simple¦[%s] %s ×%s¦[%s] %s ×%s¦[%s] %s ×%s
rewards¦%s esmeraldas · %s XP de classe · +%s reputação¦%s émeraudes · %s XP de rôle · +%s réputation¦%s Smaragde · %s Rollen-EP · +%s Ruf
reward.fragments¦ · %s fragmentos de alma¦ · %s fragments d'âme¦ · %s Seelensplitter
reward.voucher¦ · vale de arsenal¦ · bon d'armurerie¦ · Rüstkammergutschein
reward.emblem¦ · emblema de maestria¦ · emblème de maîtrise¦ · Meisterschaftsemblem
action.accept¦Aceitar¦Accepter¦Annehmen
action.abandon¦Abandonar¦Abandonner¦Aufgeben
action.deliver¦Entregar¦Livrer¦Abgeben
action.claim¦Resgatar¦Réclamer¦Abholen
accepted¦Contrato aceito.¦Contrat accepté.¦Auftrag angenommen.
cannot_accept¦Não é possível aceitar esse contrato (já escolhido hoje ou sem espaços livres).¦Impossible d'accepter ce contrat (déjà pris aujourd'hui ou aucun emplacement libre).¦Dieser Auftrag kann nicht angenommen werden (heute schon gewählt oder kein freier Platz).
abandoned¦Contrato abandonado.¦Contrat abandonné.¦Auftrag aufgegeben.
delivered¦Entrega concluída.¦Livraison terminée.¦Lieferung abgeschlossen.
delivery_missing¦Ainda faltam %s × %s para entregar.¦Il vous manque encore %s × %s à livrer.¦Es fehlen noch %s × %s zur Abgabe.
ready¦Contrato concluído! Resgate no Quadro de Contratos.¦Contrat terminé ! Réclamez-le à un Tableau de contrats.¦Auftrag erfüllt! Hole die Belohnung an einer Auftragstafel ab.
claimed¦Contrato resgatado: +%s reputação.¦Contrat réclamé : +%s réputation.¦Auftrag abgerechnet: +%s Ruf.
rank.0¦Aprendiz¦Apprenti¦Lehrling
rank.1¦Oficial¦Compagnon¦Geselle
rank.2¦Veterano¦Vétéran¦Veteran
rank.3¦Mestre¦Maître¦Meister
difficulty.easy¦Fácil¦Facile¦Einfach
difficulty.normal¦Normal¦Normal¦Normal
difficulty.hard¦Difícil¦Difficile¦Schwer
difficulty.elite¦Elite¦Élite¦Elite
difficulty.legendary¦Lendário¦Légendaire¦Legendär
kind.hunt¦Caça¦Chasse¦Jagd
kind.delivery¦Entrega¦Livraison¦Lieferung
kind.fishing¦Pesca¦Pêche¦Angeln
kind.enchanting¦Encantamento¦Enchantement¦Verzaubern
kind.profession¦Ofício (XP)¦Métier (XP)¦Beruf (EP)
kind.forge¦Forjar na Mesa da Guilda¦Forger à la Table de guilde¦Am Gildentisch schmieden
kind.event¦Sobreviver¦Survivre¦Überleben
kind.boss¦Derrotar¦Vaincre¦Besiegen
""")

add('advancement.aptitudes.', """
root.title¦Aptitudes¦Aptitudes¦Aptitudes
root.description¦Habilidades, classes e almas: seu caminho começa no grimório¦Compétences, rôles et âmes : votre voie commence avec le grimoire¦Fertigkeiten, Rollen und Seelen: Dein Pfad beginnt mit dem Grimoire
rest_bonfire.title¦Chama Acesa¦Flamme allumée¦Entfacht
rest_bonfire.description¦Descanse em uma Fogueira¦Reposez-vous à un feu de camp¦Raste an einem Leuchtfeuer
soul_level.title¦Alma Temperada¦Âme trempée¦Gestählte Seele
soul_level.description¦Alcance o nível de alma 10 em uma Fogueira¦Atteignez le niveau d'âme 10 à un feu de camp¦Erreiche Seelenstufe 10 an einem Leuchtfeuer
choose_role.title¦Uma Vocação¦Une vocation¦Eine Berufung
choose_role.description¦Escolha uma classe¦Choisissez un rôle¦Wähle eine Rolle
combo_finisher.title¦Corrente Inquebrável¦Chaîne ininterrompue¦Ungebrochene Kette
combo_finisher.description¦Acerte uma finalização de combo¦Portez un coup final de combo¦Führe einen Komboabschluss aus
evolve_master.title¦Mestre do Ofício¦Maître du métier¦Meister des Fachs
evolve_master.description¦Evolua uma classe para Mestre¦Faites évoluer un rôle au stade Maître¦Entwickle eine Rolle zum Meister
evolve_ultimate.title¦Além da Maestria¦Au-delà de la maîtrise¦Jenseits der Meisterschaft
evolve_ultimate.description¦Evolua uma classe para Definitivo¦Faites évoluer un rôle au stade Ultime¦Entwickle eine Rolle zu Ultimativ
skill_mastery.title¦Ofício de uma Vida¦Métier d'une vie¦Lebenswerk
skill_mastery.description¦Alcance o nível 50 em qualquer habilidade¦Atteignez le niveau 50 dans une compétence¦Erreiche Stufe 50 in einer Fertigkeit
socket_rune.title¦Ferreiro de Runas¦Forgerune¦Runenschmied
socket_rune.description¦Engaste uma runa em uma arma de classe na Mesa da Guilda¦Sertissez une rune dans une arme de rôle à la Table de guilde¦Setze eine Rune am Gildentisch in eine Rollenwaffe ein
slay_boss.title¦Flagelo dos Ermos¦Fléau des terres sauvages¦Schrecken der Wildnis
slay_boss.description¦Derrote um chefe de torre, santuário ou acampamento¦Vainquez un boss de tour, de sanctuaire ou de camp¦Besiege einen Turm-, Schrein- oder Lagerboss
slay_herald.title¦Amanhecer Após a Escuridão¦Aube après les ténèbres¦Morgen nach der Dunkelheit
slay_herald.description¦Derrote o Arauto do Eclipse¦Vainquez le Héraut de l'Éclipse¦Besiege den Herold der Finsternis
survive_soul_fog.title¦Através da Névoa¦À travers la brume¦Durch den Nebel
survive_soul_fog.description¦Sobreviva à Névoa das Almas até ela se dissipar¦Survivez à une Brume des âmes jusqu'à sa dissipation¦Überlebe einen Seelennebel, bis er sich lichtet
survive_ember_rain.title¦Sob Céus Ardentes¦Sous des cieux ardents¦Unter brennendem Himmel
survive_ember_rain.description¦Sobreviva a uma Chuva de Brasas¦Survivez à une Pluie de braises¦Überlebe einen Glutregen
survive_eclipse.title¦Testemunha do Eclipse¦Témoin de l'Éclipse¦Zeuge der Finsternis
survive_eclipse.description¦Sobreviva a um Eclipse sem cair¦Survivez à une Éclipse sans tomber¦Überlebe eine Finsternis, ohne zu fallen
join_party.title¦A União Faz a Força¦L'union fait la force¦Gemeinsam stark
join_party.description¦Forme um grupo com outro jogador¦Formez un groupe avec un autre joueur¦Gründe mit einem anderen Spieler eine Gruppe
first_contract.title¦Assinado e Selado¦Signé et scellé¦Unterzeichnet und besiegelt
first_contract.description¦Resgate seu primeiro contrato da guilda¦Réclamez votre premier contrat de guilde¦Rechne deinen ersten Gildenauftrag ab
contract_veteran.title¦Veterano da Guilda¦Vétéran de la guilde¦Gildenveteran
contract_veteran.description¦Resgate 10 contratos da guilda¦Réclamez 10 contrats de guilde¦Rechne 10 Gildenaufträge ab
guild_master.title¦Mestre da Guilda¦Maître de guilde¦Gildenmeister
guild_master.description¦Alcance o grau Mestre na guilda (700 de reputação)¦Atteignez le rang Maître dans la guilde (700 de réputation)¦Erreiche den Gildenrang Meister (700 Ruf)
""")

add('aptitudes.', """
accessory.phoenix¦O Amuleto da Fênix traz você de volta à vida!¦L'Amulette du phénix vous ramène à la vie !¦Das Amulett des Phönix bringt dich ins Leben zurück!
fusion.triggered¦Fusão: %s!¦Fusion : %s !¦Fusion: %s!
weapon.unlocked¦Arma desbloqueada: %s¦Arme débloquée : %s¦Waffe freigeschaltet: %s
weapon.locked¦Você ainda não alcançou essa evolução com esta classe ativa.¦Vous n'avez pas encore atteint cette évolution avec ce rôle actif.¦Du hast diese Entwicklung mit dieser aktiven Rolle noch nicht erreicht.
weapon.already¦Você já possui essa arma (inventário ou baú do Ender).¦Vous possédez déjà cette arme (inventaire ou coffre de l'Ender).¦Du besitzt diese Waffe bereits (Inventar oder Endertruhe).
rune.sockets¦Runas: %s/%s¦Runes : %s/%s¦Runen: %s/%s
rune.hint¦Leve a arma e uma runa à Mesa da Guilda para engastar.¦Apportez l'arme et une rune à la Table de guilde pour la sertir.¦Bringe die Waffe und eine Rune zum Gildentisch, um sie einzusetzen.
rune.socketed¦%s engastada no espaço %s/%s.¦%s sertie dans l'emplacement %s/%s.¦%s in Platz %s/%s eingesetzt.
rune.full¦Os dois espaços desta arma já estão ocupados.¦Les deux emplacements de cette arme sont déjà remplis.¦Beide Sockel dieser Waffe sind bereits belegt.
rune.duplicate¦Esta arma já possui essa runa.¦Cette arme possède déjà cette rune.¦Diese Waffe enthält diese Rune bereits.
rune.none¦Esta arma não possui runas.¦Cette arme n'a aucune rune.¦Diese Waffe enthält keine Runen.
rune.remove_cost¦Você precisa de %s esmeraldas para remover uma runa.¦Il faut %s émeraudes pour retirer une rune.¦Du benötigst %s Smaragde, um eine Rune zu entfernen.
rune.removed¦Você removeu %s por %s esmeraldas. A runa foi perdida.¦Vous avez retiré %s pour %s émeraudes. La rune est perdue.¦Du hast %s für %s Smaragde entfernt. Die Rune ist verloren.
rune.remove_hint¦Shift + clique direito na Mesa da Guilda (outra mão vazia): remove a última runa por esmeraldas.¦Maj + clic droit sur la Table de guilde (autre main vide) : retire la dernière rune contre des émeraudes.¦Umschalt + Rechtsklick auf den Gildentisch (andere Hand leer): Entfernt die letzte Rune gegen Smaragde.
config.no_permission¦Somente um operador pode alterar a configuração do servidor.¦Seul un opérateur peut modifier la configuration du serveur.¦Nur ein Operator kann die Serverkonfiguration ändern.
config.unknown¦Opção desconhecida: %s¦Option inconnue : %s¦Unbekannte Option: %s
config.invalid¦Valor inválido para %s: "%s"¦Valeur invalide pour %s : "%s"¦Ungültiger Wert für %s: "%s"
config.changed¦Configuração: %s = %s¦Configuration : %s = %s¦Konfiguration: %s = %s
""")

add('elite.aptitudes.', """
elite_name¦✦ %s¦✦ %s¦✦ %s
champion_name¦♛ Campeão: %s¦♛ Champion : %s¦♛ Champion: %s
champion_slain¦%s derrotou %s¦%s a vaincu %s¦%s hat %s bezwungen
fast¦Veloz¦Rapide¦Flink
armored¦Blindado¦Blindé¦Gepanzert
vampiric¦Vampírico¦Vampirique¦Vampirisch
brutal¦Brutal¦Brutal¦Brutal
regenerating¦Regenerador¦Régénérant¦Regenerierend
explosive¦Explosivo¦Explosif¦Explosiv
frost¦Gélido¦Glacial¦Frostig
blinking¦Teleportador¦Insaisissable¦Teleportierend
giant¦Gigante¦Géant¦Riesig
""")

add('boss.aptitudes.', """
tower_sentinel¦Sentinela da Torre¦Sentinelle de la tour¦Turmwächter
shrine_oracle¦Oráculo do Santuário¦Oracle du sanctuaire¦Schreinorakel
camp_champion¦Campeão do Acampamento¦Champion du camp¦Lagerchampion
phase_two¦%s entra na fase II!¦%s entre en phase II !¦%s tritt in Phase II ein!
phase_three¦%s entra na fase final!¦%s entre dans sa phase finale !¦%s tritt in die letzte Phase ein!
defeated¦%s derrotou %s!¦%s a vaincu %s !¦%s hat %s besiegt!
weapon_cores¦Núcleos de despertar: %s/%s (+%s de dano)¦Noyaux d'éveil : %s/%s (+%s dégâts)¦Erweckungskerne: %s/%s (+%s Schaden)
weapon_upgraded¦%s melhorada com +%s de dano.¦%s améliorée de +%s dégâts.¦%s um +%s Schaden verbessert.
weapon_full¦Esta arma de evolução já possui os três núcleos.¦Cette arme d'évolution possède déjà les trois noyaux.¦Diese Entwicklungswaffe enthält bereits alle drei Kerne.
weapon_duplicate¦Esta arma já contém esse núcleo.¦Cette arme contient déjà ce noyau.¦Diese Waffe enthält diesen Kern bereits.
core_hint¦Use na Mesa da Guilda com uma arma de evolução na outra mão.¦Utilisez à la Table de guilde avec une arme d'évolution dans l'autre main.¦Am Gildentisch mit einer Entwicklungswaffe in der anderen Hand benutzen.
eclipse_herald¦Arauto do Eclipse¦Héraut de l'Éclipse¦Herold der Finsternis
""")

add('eclipse.aptitudes.', """
announce¦Amanhã o sol nascerá escurecido… Prepare-se.¦Demain, le soleil se lèvera obscurci… Préparez-vous.¦Morgen geht die Sonne verdunkelt auf… Sei bereit.
start¦☀ O Eclipse começou. A luz se apaga e algo desperta.¦☀ L'Éclipse a commencé. La lumière s'éteint et quelque chose s'éveille.¦☀ Die Finsternis hat begonnen. Das Licht schwindet und etwas regt sich.
phase.penumbra¦A penumbra cobre o mundo.¦La pénombre couvre le monde.¦Halbschatten bedeckt die Welt.
phase.totality¦Totalidade! O sol é um anel de sangue e as hordas avançam.¦Totalité ! Le soleil est un anneau de sang et les hordes avancent.¦Totalität! Die Sonne ist ein Ring aus Blut und die Horden rücken vor.
phase.retreat¦O anel se rompe: o Eclipse recua.¦L'anneau se brise : l'Éclipse se retire.¦Der Ring zerbricht: Die Finsternis weicht zurück.
herald¦O Arauto do Eclipse desceu.¦Le Héraut de l'Éclipse est descendu.¦Der Herold der Finsternis ist herabgestiegen.
end¦O Eclipse terminou. A luz retorna.¦L'Éclipse est terminée. La lumière revient.¦Die Finsternis ist vorbei. Das Licht kehrt zurück.
stopped¦O Eclipse foi interrompido.¦L'Éclipse a été arrêtée.¦Die Finsternis wurde beendet.
survivor¦Você sobreviveu ao Eclipse: recebe experiência de classe e Combate.¦Vous avez survécu à l'Éclipse : vous gagnez de l'expérience de rôle et de Combat.¦Du hast die Finsternis überlebt: Du erhältst Rollen- und Nahkampf-EP.
phase_name.none¦sem eclipse¦aucune éclipse¦keine Finsternis
phase_name.penumbra¦Penumbra¦Pénombre¦Halbschatten
phase_name.totality¦Totalidade¦Totalité¦Totalität
phase_name.retreat¦Recuo¦Retrait¦Rückzug
hud¦Eclipse · %s¦Éclipse · %s¦Finsternis · %s
status.active¦Há um Eclipse em andamento (fase: %s).¦Une Éclipse est en cours (phase : %s).¦Eine Finsternis läuft (Phase: %s).
status.next¦Próximo Eclipse em %s dias.¦Prochaine Éclipse dans %s jours.¦Nächste Finsternis in %s Tagen.
status.disabled¦O Eclipse está desativado na configuração (%s).¦L'Éclipse est désactivée dans la configuration (%s).¦Die Finsternis ist in der Konfiguration deaktiviert (%s).
command.started¦Eclipse iniciado.¦Éclipse démarrée.¦Finsternis gestartet.
command.stopped¦Eclipse interrompido.¦Éclipse arrêtée.¦Finsternis beendet.
command.already_active¦Já há um Eclipse em andamento.¦Une Éclipse est déjà en cours.¦Es läuft bereits eine Finsternis.
command.not_active¦Não há Eclipse em andamento.¦Aucune Éclipse en cours.¦Es läuft keine Finsternis.
""")

add('event.aptitudes.', """
eclipse.name¦O Eclipse¦L'Éclipse¦Die Finsternis
soul_fog.name¦Névoa das Almas¦Brume des âmes¦Seelennebel
soul_fog.announce¦Esta noite a névoa trará aqueles que não descansam…¦Cette nuit, la brume amènera ceux qui ne reposent pas…¦Heute Nacht bringt der Nebel jene, die nicht ruhen…
soul_fog.start¦A Névoa das Almas cobre o mundo. Algo se move na neblina.¦La Brume des âmes couvre le monde. Quelque chose bouge dans la brume.¦Seelennebel bedeckt die Welt. Etwas bewegt sich im Dunst.
soul_fog.end¦A névoa se dissipa ao amanhecer.¦La brume se lève avec l'aube.¦Mit dem Morgengrauen lichtet sich der Nebel.
soul_fog.stopped¦A Névoa das Almas foi interrompida.¦La Brume des âmes a été arrêtée.¦Der Seelennebel wurde beendet.
ember_rain.name¦Chuva de Brasas¦Pluie de braises¦Glutregen
ember_rain.announce¦Amanhã o céu vai arder. Busque abrigo… ou minério.¦Demain, le ciel brûlera. Cherchez un abri… ou du minerai.¦Morgen brennt der Himmel. Suche Schutz… oder Erz.
ember_rain.start¦Chuva de Brasas! Meteoros estão caindo: afaste-se dos rastros de fogo.¦Pluie de braises ! Des météores tombent : éloignez-vous des traînées de feu.¦Glutregen! Meteore fallen: Halte dich von den Feuerschweifen fern.
ember_rain.end¦O céu se acalma: a Chuva de Brasas terminou.¦Le ciel s'apaise : la Pluie de braises est terminée.¦Der Himmel beruhigt sich: Der Glutregen ist vorbei.
ember_rain.stopped¦A Chuva de Brasas foi interrompida.¦La Pluie de braises a été arrêtée.¦Der Glutregen wurde beendet.
status.active¦Evento em andamento: %s.¦Événement en cours : %s.¦Laufendes Ereignis: %s.
status.next¦Próximo evento: %s, em %s dias.¦Prochain événement : %s, dans %s jours.¦Nächstes Ereignis: %s, in %s Tagen.
status.none¦Nenhum evento está ativado na configuração.¦Aucun événement n'est activé dans la configuration.¦In der Konfiguration sind keine Ereignisse aktiviert.
command.unknown¦Evento desconhecido: %s (eclipse, soul_fog, ember_rain).¦Événement inconnu : %s (eclipse, soul_fog, ember_rain).¦Unbekanntes Ereignis: %s (eclipse, soul_fog, ember_rain).
command.busy¦Já há um evento em andamento.¦Un événement est déjà en cours.¦Es läuft bereits ein Ereignis.
command.started¦Evento iniciado: %s.¦Événement démarré : %s.¦Ereignis gestartet: %s.
command.stopped¦Evento interrompido.¦Événement arrêté.¦Ereignis beendet.
command.none¦Não há evento em andamento.¦Aucun événement en cours.¦Es läuft kein Ereignis.
""")

add('subtitles.aptitudes.', """
eclipse_announce¦Sino distante do Eclipse¦Cloche lointaine de l'Éclipse¦Ferne Finsternisglocke
eclipse_start¦O sol se apaga¦Le soleil s'efface¦Die Sonne verblasst
eclipse_totality¦Totalidade do Eclipse¦Totalité de l'Éclipse¦Totale Finsternis
herald_spawn¦O Arauto chega¦Le Héraut arrive¦Der Herold erscheint
combo_finisher¦Finalização de combo¦Coup final de combo¦Komboabschluss
rune_socket¦Runa engastada¦Rune sertie¦Rune eingesetzt
rune_remove¦Runa removida¦Rune retirée¦Rune entfernt
ui_metal_select¦Clique metálico¦Clic métallique¦Metallklick
ui_metal_unlock¦Desbloqueio metálico¦Déverrouillage métallique¦Metallfreischaltung
ui_metal_denied¦Metal bloqueado¦Métal verrouillé¦Metall gesperrt
keystone_carnage¦Carnificina: batimento cardíaco¦Carnage : battement de cœur¦Blutbad: Herzschlag
keystone_bulwark¦Baluarte: escudo¦Rempart : bouclier¦Bollwerk: Schild
keystone_overload¦Sobrecarga: descarga¦Surcharge : décharge¦Überladung: Entladung
keystone_detonation¦Detonação: explosão¦Détonation : explosion¦Detonation: Explosion
keystone_contagion¦Contágio: borbulhar¦Contagion : bouillonnement¦Ansteckung: Blubbern
keystone_legion¦Legião: coro¦Légion : chœur¦Legion: Chor
keystone_specter¦Espectro: sussurro¦Spectre : murmure¦Gespenst: Flüstern
keystone_fortune¦Fortuna: moedas¦Fortune : pièces¦Glück: Münzen
keystone_tempest¦Tempestade: faísca¦Tempête : étincelle¦Sturm: Funke
keystone_sanctuary¦Santuário: cura¦Sanctuaire : soin¦Zuflucht: Heilung
""")

add('keystone.aptitudes.', """
carnage¦Carnificina¦Carnage¦Blutbad
carnage.desc¦Cada vez que você abate um inimigo, recupera 10% da vida máxima e recebe Pressa II por 5 s.¦Chaque fois que vous tuez un ennemi, vous récupérez 10% de votre santé maximale et gagnez Célérité II pendant 5 s.¦Jedes Mal, wenn du einen Feind tötest, heilst du 10% deines maximalen Lebens und erhältst Eile II für 5 s.
bulwark¦Baluarte¦Rempart¦Bollwerk
bulwark.desc¦Se sua vida cair abaixo de 30%, você ganha 8 de absorção e Resistência I por 6 s. Recarrega em 60 s.¦Si votre santé passe sous 30%, vous gagnez 8 d'absorption et Résistance I pendant 6 s. Se recharge en 60 s.¦Fällt dein Leben unter 30%, erhältst du 8 Absorption und Resistenz I für 6 s. Lädt in 60 s wieder auf.
overload¦Sobrecarga¦Surcharge¦Überladung
overload.desc¦A cada 4 habilidades que você usa, uma não custa mana e tem a recarga reduzida pela metade.¦Une capacité sur 4 que vous lancez ne coûte aucun mana et voit sa recharge divisée par deux.¦Jede 4. Fähigkeit, die du wirkst, kostet kein Mana und hat halbierte Abklingzeit.
detonation¦Detonação¦Détonation¦Detonation
detonation.desc¦Cada inimigo que você abate explode e causa 4 de dano em até 3 blocos. Nunca quebra blocos nem fere seus aliados.¦Chaque ennemi que vous tuez explose et inflige 4 dégâts dans un rayon de 3 blocs. Ne casse jamais de blocs et ne blesse pas vos alliés.¦Jeder Feind, den du tötest, explodiert und verursacht 4 Schaden im Umkreis von 3 Blöcken. Zerstört nie Blöcke und trifft nie Verbündete.
contagion¦Contágio¦Contagion¦Ansteckung
contagion.desc¦Cada vez que você abate um inimigo, o veneno, a lentidão, o definhamento e o fogo dele passam para 2 inimigos próximos.¦Chaque fois que vous tuez un ennemi, son poison, sa lenteur, son flétrissement et son feu passent à 2 ennemis proches.¦Jedes Mal, wenn du einen Feind tötest, gehen sein Gift, seine Langsamkeit, seine Ausdörrung und sein Feuer auf 2 nahe Feinde über.
legion¦Legião¦Légion¦Legion
legion.desc¦Você pode ter 1 servo a mais, e seus servos te curam com 10% do dano que causam.¦Vous pouvez avoir 1 serviteur de plus, et vos serviteurs vous soignent de 10% des dégâts qu'ils infligent.¦Du kannst 1 Diener mehr haben, und deine Diener heilen dich um 10% des Schadens, den sie verursachen.
specter¦Espectro¦Spectre¦Gespenst
specter.desc¦+5% de esquiva. Depois de esquivar, seu próximo golpe em até 3 s é um crítico garantido.¦+5% d'esquive. Après une esquive, votre prochain coup dans les 3 s est un critique garanti.¦+5% Ausweichen. Nach dem Ausweichen trifft dein nächster Schlag innerhalb von 3 s garantiert kritisch.
fortune¦Fortuna¦Fortune¦Glück
fortune.desc¦Abater criaturas tem 25% de chance de dar saque extra, e abater elites, 10% de chance de dar uma esmeralda.¦Tuer des créatures a 25% de chance de donner du butin supplémentaire, et tuer des élites 10% de chance de donner une émeraude.¦Das Töten von Kreaturen hat 25% Chance auf zusätzliche Beute, das Töten von Elitegegnern 10% Chance auf einen Smaragd.
tempest¦Tempestade¦Tempête¦Sturm
tempest.desc¦Seus críticos (e 15% dos seus outros golpes) lançam uma faísca que causa 3 de dano a outro inimigo próximo (1 s de recarga).¦Vos coups critiques (et 15% de vos autres coups) lancent une étincelle qui inflige 3 dégâts à un autre ennemi proche (recharge de 1 s).¦Deine kritischen Treffer (und 15% deiner übrigen Treffer) schleudern einen Funken, der einem weiteren nahen Feind 3 Schaden zufügt (1 s Abklingzeit).
sanctuary¦Santuário¦Sanctuaire¦Zuflucht
sanctuary.desc¦A cada 5 s, você e os aliados do grupo a até 8 blocos recuperam 2 de vida.¦Toutes les 5 s, vous et les alliés du groupe à moins de 8 blocs récupérez 2 points de santé.¦Alle 5 s heilen du und Gruppenverbündete im Umkreis von 8 Blöcken 2 Leben.
""")

add('tutorial.aptitudes.', """
step.guide¦Obtenha o Grimório de Aptitudes¦Obtenez le Grimoire d'Aptitudes¦Erhalte das Aptitudes-Grimoire
step.bonfire¦Suba de nível em uma Fogueira¦Montez de niveau à un feu de camp¦Steige an einem Leuchtfeuer auf
step.primary_role¦Escolha sua classe principal¦Choisissez votre rôle principal¦Wähle deine Hauptrolle
step.active¦Use uma habilidade ativa com sucesso¦Utilisez une capacité active avec succès¦Nutze eine aktive Fähigkeit erfolgreich
step.combo¦Acerte um combo de 5 golpes¦Réussissez un combo de 5 coups¦Erziele eine Kombo mit 5 Treffern
step.guild_craft¦Fabrique na Mesa da Guilda¦Fabriquez à la Table de guilde¦Stelle etwas am Gildentisch her
status¦Tutorial %s. Passos: %d/%d; recompensas: %d/%d.¦Tutoriel %s. Étapes : %d/%d ; récompenses : %d/%d.¦Tutorial %s. Schritte: %d/%d; Belohnungen: %d/%d.
state.active¦em andamento¦en cours¦in Arbeit
state.completed¦concluído¦terminé¦abgeschlossen
state.skipped¦ignorado¦ignoré¦übersprungen
next¦Passo %d/%d: %s¦Étape %d/%d : %s¦Schritt %d/%d: %s
pending¦Recompensas pendentes: %d. São entregues em ordem quando os passos anteriores são concluídos e há espaço no inventário.¦Récompenses en attente : %d. Elles sont livrées dans l'ordre une fois les étapes précédentes terminées et de la place disponible dans l'inventaire.¦Ausstehende Belohnungen: %d. Sie werden der Reihe nach vergeben, sobald vorherige Schritte erledigt sind und Platz im Inventar ist.
skipped¦Tutorial ignorado permanentemente. As recompensas pendentes foram perdidas.¦Tutoriel ignoré définitivement. Les récompenses en attente sont perdues.¦Tutorial dauerhaft übersprungen. Ausstehende Belohnungen verfallen.
already_skipped¦O tutorial já foi ignorado.¦Le tutoriel est déjà ignoré.¦Das Tutorial wurde bereits übersprungen.
hud¦Tutorial · %d/%d¦Tutoriel · %d/%d¦Tutorial · %d/%d
hud.pending¦Libere espaço no inventário (%d).¦Libérez de la place dans l'inventaire (%d).¦Schaffe Platz im Inventar (%d).
""")

add('mastery.aptitudes.', """
card¦M%d¦M%d¦M%d
roles¦Classes M %d/%d¦Rôles M %d/%d¦Rollen M %d/%d
roles_hint¦Principal / secundária; bônus no nível máximo (mín. 50).¦Principal / secondaire ; bonus au niveau maximal (min. 50).¦Haupt- / Nebenrolle; Bonus auf Maximalstufe (mind. 50).
skill¦Maestria: %d pontos · +%s%% nas passivas¦Maîtrise : %d points · +%s%% aux passifs¦Meisterschaft: %d Punkte · +%s%% auf passive Boni
role¦%s: M%d · +%s%% de eficiência de energia¦%s : M%d · +%s%% d'efficacité énergétique¦%s: M%d · +%s%% Energieeffizienz
xp¦XP de maestria: %s / %s¦XP de maîtrise : %s / %s¦Meisterschafts-EP: %s / %s
limit¦Limite de pontos de maestria alcançado.¦Limite de points de maîtrise atteinte.¦Meisterschaftspunktelimit erreicht.
""")

add('threat.aptitudes.', """
hud¦Ameaça: %s¦Menace : %s¦Bedrohung: %s
ring.0¦Zona tranquila¦Zone calme¦Ruhige Zone
ring.1¦I¦I¦I
ring.2¦II¦II¦II
ring.3¦III¦III¦III
ring.4¦IV¦IV¦IV
ring.5¦V¦V¦V
""")

add('screen.aptitudes.tree.', """
tab_talents¦Talentos¦Talents¦Talente
rank¦Grau %s/%s¦Rang %s/%s¦Rang %s/%s
either¦%s ou %s¦%s ou %s¦%s oder %s
""")

add('', """
talent.aptitudes_piloto.paso_ligero.name¦Passo leve¦Pas léger¦Leichter Schritt
talent.aptitudes_piloto.paso_firme.name¦Passo firme¦Pas assuré¦Sicherer Schritt
""")

add('forge.aptitudes.', """
reforge¦Reforjar arma¦Reforger l’arme¦Waffe umschmieden
temper¦Temperar arma¦Tremper l’arme¦Waffe härten
confirm¦Confirmar¦Confirmer¦Bestätigen
empty_grid¦Esvazie a grade e solte o item do cursor.¦Videz la grille et déposez l’objet du curseur.¦Leere das Raster und lege den Gegenstand am Mauszeiger ab.
temper_unavailable¦Descanse em uma Fogueira acessível no modo Sobrevivência ou Aventura para temperar uma arma.¦Reposez-vous à un feu de camp accessible en Survie ou en Aventure pour tremper une arme.¦Raste im Überlebens- oder Abenteuermodus an einem erreichbaren Leuchtfeuer, um eine Waffe zu härten.
input.weapon¦Arma¦Arme¦Waffe
input.essence¦Essência¦Essence¦Essenz
input.heart¦Coração¦Cœur¦Herz
input.shards¦Fragmentos¦Fragments¦Splitter
current¦Atual · %s¦Actuel · %s¦Aktuell · %s
result¦Resultado · %s¦Résultat · %s¦Ergebnis · %s
none¦Sem afixo¦Sans affixe¦Ohne Affix
affix¦%1$s %2$s¦%1$s %2$s¦%1$s %2$s
profile.reserve¦Reserva¦Réserve¦Reserve
profile.impetus¦Impulso¦Élan¦Schwung
effect.reserve¦−%s%% custo¦−%s%% coût¦−%s%% Kosten
effect.impetus¦+%s%% recurso¦+%s%% ressource¦+%s%% Ressource
hint.reserve¦As habilidades ativas equipadas desta classe gastam menos energia com a arma na mão principal.¦Les capacités actives équipées de ce rôle coûtent moins d’énergie avec cette arme en main principale.¦Ausgerüstete aktive Fähigkeiten dieser Rolle kosten weniger Energie, solange du die Waffe in der Haupthand hältst.
hint.impetus¦Acertos válidos com esta arma contra criaturas hostis geram mais recurso desta classe.¦Les coups valides de cette arme contre les créatures hostiles donnent plus de ressource à ce rôle.¦Gültige Treffer mit dieser Waffe gegen feindliche Kreaturen gewähren mehr Rollenressource.
status.no_weapon¦Coloque uma arma de evolução.¦Placez une arme d’évolution.¦Lege eine Entwicklungswaffe ein.
status.wrong_role¦Ative a classe da arma e desbloqueie sua evolução.¦Activez le rôle de l’arme et débloquez son évolution.¦Aktiviere die Rolle der Waffe und schalte ihre Entwicklung frei.
status.same_affix¦A arma já tem esse afixo.¦L’arme possède déjà cet affixe.¦Die Waffe hat dieses Affix bereits.
status.needs_reforge¦Reforje a arma na Mesa da Guilda primeiro.¦Reforgez d’abord l’arme à la Table de guilde.¦Schmiede die Waffe zuerst am Gildentisch um.
status.max_rank¦A têmpera já está no grau II.¦La trempe est déjà au rang II.¦Die Waffe ist bereits auf Rang II gehärtet.
status.missing¦Adicione os ingredientes necessários.¦Ajoutez les ingrédients requis.¦Lege die benötigten Zutaten ein.
status.ready¦Pronto para confirmar.¦Prêt à confirmer.¦Bereit zur Bestätigung.
""")

add('hud.aptitudes.boss.', """
phase¦Fase %s¦Phase %s¦Phase %s
health¦%s%%¦%s %%¦%s %%
more¦+%s por perto¦+%s à proximité¦+%s in der Nähe
""")

add('screen.aptitudes.hud.', """
layout¦Disposição¦Disposition¦Anordnung
indicators¦Indicadores¦Indicateurs¦Anzeigen
minimal¦Modo minimalista¦Mode minimaliste¦Minimalmodus
animations¦Animações da interface¦Animations de l’interface¦HUD-Animationen
preset.classic¦Clássico¦Classique¦Klassisch
preset.centered¦Centralizado¦Centré¦Zentriert
preset.compact¦Compacto¦Compact¦Kompakt
""")

add('', """
flask.aptitudes.open¦Frascos¦Fioles¦Phiolen
flask.aptitudes.title¦Melhorar frascos¦Améliorer les fioles¦Phiolen verbessern
flask.aptitudes.input.flask¦Frasco¦Fiole¦Phiole
flask.aptitudes.input.core¦Núcleo¦Noyau¦Bosskern
flask.aptitudes.insert¦Coloque um frasco de Estus ou Éter.¦Insérez une fiole d’Estus ou d’éther.¦Lege eine Estus- oder Ätherphiole ein.
flask.aptitudes.limit¦Limite de melhoria atingido.¦Limite d’amélioration atteinte.¦Aufwertungslimit erreicht.
flask.aptitudes.unavailable¦Descanse em uma fogueira próxima no Sobrevivência para melhorar frascos.¦Reposez-vous à un feu de camp proche en Survie pour améliorer les fioles.¦Raste im Überlebensmodus an einem nahen Leuchtfeuer, um Phiolen zu verbessern.
flask.aptitudes.current¦Atual · Grau %s · %s cargas · %s¦Actuel · Rang %s · %s charges · %s¦Aktuell · Rang %s · %s Ladungen · %s
flask.aptitudes.next¦Melhoria · Grau %s · %s cargas · %s¦Après · Rang %s · %s charges · %s¦Danach · Rang %s · %s Ladungen · %s
item.aptitudes.flask.rank¦Grau de melhoria %s¦Rang d’amélioration %s¦Aufwertungsrang %s
item.aptitudes.flask.estus_recovery¦Cura %s PV¦Rend %s PV¦Heilt %s LP
item.aptitudes.flask.ether_recovery¦Recupera %s%% de mana¦Rend %s %% de mana¦Stellt %s %% Mana wieder her
item.aptitudes.flask.regeneration¦Regeneração breve¦Régénération brève¦Kurze Regeneration
""")

add('', """
block.aptitudes.arena_fog¦Névoa da arena¦Brume d’arène¦Arenanebel
subtitles.aptitudes.arena_theme¦O tema da arena começa¦Le thème de l’arène commence¦Das Arenathema beginnt
""")

# v61 (K3): grimorio, bestiario, buscador, marcadores y progreso.
add('', """
key.aptitudes.guide_lookup¦Ver no grimório (inventário)¦Voir dans le grimoire (inventaire)¦Im Grimoire ansehen (Inventar)
screen.aptitudes.guide.lookup¦%s: ver no grimório¦%s : voir dans le grimoire¦%s: im Grimoire ansehen
screen.aptitudes.guide.tab.bestiary¦Bestiário¦Bestiaire¦Bestiarium
screen.aptitudes.guide.tab.progress¦Progresso¦Progression¦Fortschritt
screen.aptitudes.guide.tab.search¦Buscar no grimório¦Chercher dans le grimoire¦Im Grimoire suchen
screen.aptitudes.guide.tab.bookmarks¦Marcadores¦Signets¦Lesezeichen
screen.aptitudes.guide.tab.tutorial¦Capítulo recomendado · %s¦Chapitre recommandé · %s¦Empfohlenes Kapitel · %s
screen.aptitudes.guide.tutorial¦Tutorial %s/%s: %s¦Tutoriel %s/%s : %s¦Tutorial %s/%s: %s
screen.aptitudes.guide.bookmark.add¦Adicionar marcador¦Ajouter un signet¦Lesezeichen hinzufügen
screen.aptitudes.guide.bookmark.remove¦Remover marcador¦Retirer le signet¦Lesezeichen entfernen
screen.aptitudes.guide.bookmarks.count¦%s/%s salvos¦%s/%s enregistrés¦%s/%s gespeichert
screen.aptitudes.guide.bookmarks.empty¦Ainda não há marcadores. Clique em ☆ em um capítulo, item, criatura ou classe para salvá-lo aqui.¦Aucun signet pour l'instant. Cliquez sur ☆ dans un chapitre, un objet, une créature ou un rôle pour l'enregistrer ici.¦Noch keine Lesezeichen. Klicke auf ☆ bei einem Kapitel, Gegenstand, einer Kreatur oder Rolle, um sie hier zu speichern.
screen.aptitudes.guide.bestiary.count¦%s/%s páginas descobertas¦%s/%s pages découvertes¦%s/%s Seiten entdeckt
screen.aptitudes.guide.progress.total¦Progresso total: %s%%¦Progression totale : %s %%¦Gesamtfortschritt: %s %%
screen.aptitudes.guide.progress.waiting¦Aguardando dados do servidor…¦En attente des données du serveur…¦Warte auf Serverdaten…
screen.aptitudes.guide.progress.achievements¦Conquistas¦Succès¦Erfolge
screen.aptitudes.guide.progress.bestiary¦Páginas do bestiário¦Pages du bestiaire¦Bestiariumsseiten
screen.aptitudes.guide.progress.bosses¦Chefes derrotados¦Boss vaincus¦Besiegte Bosse
screen.aptitudes.guide.progress.steps¦Degraus de caminho¦Paliers de voie¦Pfadstufen
screen.aptitudes.guide.progress.keystones¦Pedras angulares¦Pierres angulaires¦Schlüsselsteine
screen.aptitudes.guide.progress.value¦%s/%s · %s%%¦%s/%s · %s %%¦%s/%s · %s %%
screen.aptitudes.guide.search.empty¦Digite para buscar capítulos, itens, criaturas descobertas e classes.¦Tapez pour chercher des chapitres, objets, créatures découvertes et rôles.¦Tippe, um Kapitel, Gegenstände, entdeckte Kreaturen und Rollen zu suchen.
screen.aptitudes.guide.search.none¦Nenhum resultado.¦Aucun résultat.¦Keine Ergebnisse.
screen.aptitudes.guide.kind.chapter¦Capítulo¦Chapitre¦Kapitel
screen.aptitudes.guide.kind.item¦Item¦Objet¦Gegenstand
screen.aptitudes.guide.kind.beast¦Bestiário¦Bestiaire¦Bestiarium
screen.aptitudes.guide.kind.role¦Caminhos¦Voies¦Pfade
screen.aptitudes.guide.kind.missing¦Indisponível¦Indisponible¦Nicht verfügbar
screen.aptitudes.guide.role.click¦Clique: ver seus caminhos¦Clic : voir ses voies¦Klick: Pfade ansehen
screen.aptitudes.guide.role.paths¦Caminhos · %s¦Voies · %s¦Pfade · %s
screen.aptitudes.guide.role.chosen¦seu caminho¦votre voie¦dein Pfad
screen.aptitudes.guide.role.step¦Degrau %s · nível %s¦Palier %s · niveau %s¦Stufe %s · Level %s
bestiary.aptitudes.unlocked¦Bestiário: nova página — %s¦Bestiaire : nouvelle page — %s¦Bestiarium: neue Seite — %s
bestiary.aptitudes.locked¦???¦???¦???
bestiary.aptitudes.locked.hint¦Derrote esta criatura para desbloquear sua página.¦Vainquez cette créature pour débloquer sa page.¦Besiege diese Kreatur, um ihre Seite freizuschalten.
bestiary.aptitudes.kills¦Derrotadas: %s¦Vaincues : %s¦Besiegt: %s
bestiary.aptitudes.stats¦Vida %s · Armadura %s · Dano %s¦Vie %s · Armure %s · Dégâts %s¦Leben %s · Rüstung %s · Schaden %s
bestiary.aptitudes.boss_stats¦Chefe: atributos de elite reforçados¦Boss : statistiques d'élite renforcées¦Boss: verstärkte Elitewerte
bestiary.aptitudes.drops¦Saque¦Butin¦Beute
bestiary.aptitudes.weak.smite¦Vulnerável a Julgamento¦Vulnérable à Châtiment¦Anfällig für Bann
bestiary.aptitudes.weak.bane¦Vulnerável a Ruína dos Artrópodes¦Vulnérable à Fléau des arthropodes¦Anfällig für Nemesis der Gliederfüßer
bestiary.aptitudes.weak.impaling¦Vulnerável a Penetração¦Vulnérable à Empalement¦Anfällig für Harpune
bestiary.aptitudes.fire_immune¦Imune ao fogo¦Insensible au feu¦Immun gegen Feuer
bestiary.aptitudes.section.creature¦Criaturas¦Créatures¦Kreaturen
bestiary.aptitudes.section.elite¦Elites¦Élites¦Eliten
bestiary.aptitudes.section.boss¦Chefes¦Boss¦Bosse
bestiary.aptitudes.kind.creature¦Criatura¦Créature¦Kreatur
bestiary.aptitudes.kind.elite¦Elite¦Élite¦Elite
bestiary.aptitudes.kind.boss¦Chefe¦Boss¦Boss
bestiary.aptitudes.region.overworld¦Superfície¦Surface¦Oberwelt
bestiary.aptitudes.region.nether¦Nether¦Nether¦Nether
bestiary.aptitudes.region.end¦O End¦L'End¦Das Ende
bestiary.aptitudes.region.any¦Qualquer dimensão¦Toutes les dimensions¦Jede Dimension
bestiary.aptitudes.elite.name¦Elite¦Élite¦Elite
bestiary.aptitudes.champion.name¦Campeão¦Champion¦Champion
bestiary.aptitudes.zombie.tip¦Lutador corpo a corpo lento que geme antes de chegar. Pode chamar outros zumbis e derruba portas de madeira no Difícil. Queima ao sol, a menos que use capacete.¦Combattant lent au corps à corps qui grogne avant d'arriver. Il peut appeler d'autres zombies et enfonce les portes en bois en Difficile. Brûle au soleil sauf s'il porte un casque.¦Langsamer Nahkämpfer, der stöhnt, bevor er auftaucht. Er kann andere Zombies rufen und bricht auf Schwer Holztüren ein. Brennt im Sonnenlicht, außer er trägt einen Helm.
bestiary.aptitudes.husk.tip¦Zumbi do deserto que não queima ao sol. Cada golpe causa Fome: tenha comida nas lutas longas.¦Zombie du désert qui ne brûle pas au soleil. Chaque coup inflige Faim : gardez de la nourriture pour les longs combats.¦Wüstenzombie, der im Tageslicht nicht brennt. Jeder Treffer verursacht Hunger, halte bei langen Kämpfen Nahrung bereit.
bestiary.aptitudes.drowned.tip¦Zumbi de rios e oceanos. Alguns carregam tridentes e os arremessam de longe; lute em terra firme, onde ele perde a vantagem.¦Zombie des rivières et des océans. Certains portent un trident et le lancent à distance ; combattez sur la terre ferme, où il perd son avantage.¦Zombie aus Flüssen und Ozeanen. Manche tragen Dreizacke und werfen sie aus der Ferne; kämpfe an Land, wo er seinen Vorteil verliert.
bestiary.aptitudes.zombie_villager.tip¦Um aldeão perdido para a horda. Luta como um zumbi, mas uma poção arremessável de Fraqueza e uma maçã dourada podem curá-lo.¦Un villageois perdu pour la horde. Il se bat comme un zombie, mais une potion jetable de Faiblesse et une pomme dorée peuvent le guérir.¦Ein an die Horde verlorener Dorfbewohner. Er kämpft wie ein Zombie, doch ein Wurftrank der Schwäche und ein goldener Apfel können ihn heilen.
bestiary.aptitudes.skeleton.tip¦Arqueiro que mantém distância e se move em círculos. Aproxime-se protegido ou levante um escudo; queima ao sol.¦Archer qui garde ses distances et tourne autour de vous. Approchez à couvert ou levez un bouclier ; il brûle au soleil.¦Bogenschütze, der Abstand hält und dich umkreist. Nähere dich in Deckung oder hebe einen Schild; er brennt im Sonnenlicht.
bestiary.aptitudes.stray.tip¦Arqueiro congelado das terras nevadas. Suas flechas causam Lentidão: não troque tiros em campo aberto.¦Archer gelé des terres enneigées. Ses flèches infligent Lenteur : n'échangez pas de tirs à découvert.¦Gefrorener Bogenschütze der Schneelande. Seine Pfeile verursachen Langsamkeit, also liefere dir kein Feuergefecht im Freien.
bestiary.aptitudes.bogged.tip¦Arqueiro coberto de musgo de pântanos e câmaras de desafio. Suas flechas envenenam, mas atira mais devagar que um esqueleto.¦Archer moussu des marais et des chambres d'épreuves. Ses flèches empoisonnent, mais il tire plus lentement qu'un squelette.¦Bemooster Bogenschütze aus Sümpfen und Prüfungskammern. Seine Pfeile vergiften, doch er schießt langsamer als ein Skelett.
bestiary.aptitudes.spider.tip¦Escala paredes e salta sobre você. Neutra sob luz forte e hostil no escuro; tetos baixos impedem seus saltos.¦Escalade les murs et vous bondit dessus. Neutre en pleine lumière, hostile dans l'obscurité ; les plafonds bas bloquent ses sauts.¦Klettert Wände hoch und springt dich an. Bei hellem Licht neutral, in der Dunkelheit feindlich; niedrige Decken stoppen ihre Sprünge.
bestiary.aptitudes.cave_spider.tip¦Aranha pequena das minas abandonadas cuja mordida envenena. Quebre ou ilumine seu gerador e lute em túneis estreitos.¦Petite araignée des puits de mine abandonnés dont la morsure empoisonne. Cassez ou éclairez son générateur et combattez-la dans des tunnels étroits.¦Kleine Spinne verlassener Minen, deren Biss vergiftet. Zerstöre oder beleuchte ihren Spawner und kämpfe in engen Tunneln.
bestiary.aptitudes.creeper.tip¦Perseguidor silencioso que explode de perto. Acerte-o e recue para cancelar o pavio; um carregado por um raio explode muito mais forte.¦Rôdeur silencieux qui explose à bout portant. Frappez-le et reculez pour annuler la mèche ; un creeper chargé par la foudre explose bien plus fort.¦Lautloser Verfolger, der aus nächster Nähe explodiert. Triff ihn und weiche zurück, um die Zündung abzubrechen; ein vom Blitz geladener explodiert viel stärker.
bestiary.aptitudes.enderman.tip¦Neutro até que você o encare ou o ataque. Teletransporta-se, desvia de projéteis e a água o fere: lute sob um teto de dois blocos.¦Neutre tant que vous ne le regardez pas dans les yeux ou ne le frappez pas. Il se téléporte, esquive les projectiles et l'eau le blesse : battez-vous sous un toit de deux blocs.¦Neutral, bis du ihm in die Augen siehst oder ihn schlägst. Er teleportiert sich, weicht Geschossen aus und Wasser schadet ihm: kämpfe unter einem zwei Blöcke hohen Dach.
bestiary.aptitudes.witch.tip¦Arremessa poções nocivas e bebe cura ou resistência quando ferida. Ataque rápido e leve leite para limpar seus efeitos.¦Lance des potions nocives et boit soin ou résistance quand elle est blessée. Attaquez vite et gardez du lait pour dissiper ses effets.¦Wirft schädliche Tränke und trinkt Heilung oder Resistenz, wenn sie verletzt ist. Greife schnell an und halte Milch gegen ihre Effekte bereit.
bestiary.aptitudes.slime.tip¦Divide-se em slimes menores ao morrer; os minúsculos são inofensivos. Aparece em pântanos e em certas áreas profundas.¦Se divise en slimes plus petits à sa mort ; les plus minuscules sont inoffensifs. Apparaît dans les marais et certaines zones profondes.¦Teilt sich beim Tod in kleinere Schleime; die winzigsten sind harmlos. Erscheint in Sümpfen und in bestimmten tiefen Bereichen.
bestiary.aptitudes.phantom.tip¦Persegue quem passa dias sem dormir, mergulhando do céu à noite. Durma para afastá-lo ou golpeie quando descer.¦Hante les joueurs qui n'ont pas dormi depuis des jours et pique du ciel la nuit. Dormez pour l'éloigner ou frappez-le quand il plonge.¦Sucht Spieler heim, die tagelang nicht geschlafen haben, und stürzt nachts vom Himmel. Schlafe, um es fernzuhalten, oder schlage zu, wenn es herabstößt.
bestiary.aptitudes.silverfish.tip¦Esconde-se em pedra infestada e sai em enxame quando perturbado, chamando outros. Nas fortalezas, desconfie de pedras suspeitas.¦Se cache dans la pierre infestée et surgit en essaim quand on la dérange, appelant les autres. Dans les forteresses, méfiez-vous des pierres suspectes.¦Versteckt sich in befallenem Stein und schwärmt aus, wenn man ihn stört, und ruft andere. Sei in Festungen bei verdächtigem Stein vorsichtig.
bestiary.aptitudes.pillager.tip¦Saqueador com besta que patrulha em grupo e lidera invasões. Proteja-se entre as rajadas e ataque enquanto recarrega.¦Pillard à l'arbalète qui patrouille en groupe et mène les raids. Abritez-vous entre les salves et frappez pendant qu'il recharge.¦Plünderer mit Armbrust, der in Gruppen patrouilliert und Überfälle anführt. Suche zwischen den Salven Deckung und schlage beim Nachladen zu.
bestiary.aptitudes.vindicator.tip¦Saqueador com machado e muito dano corpo a corpo. Mantenha-o afastado com escudo ou ataques à distância.¦Pillard armé d'une hache qui inflige de lourds dégâts au corps à corps. Tenez-le à distance avec un bouclier ou des attaques à distance.¦Plünderer mit Axt, der hohen Nahkampfschaden verursacht. Halte ihn mit Schild oder Fernangriffen auf Abstand.
bestiary.aptitudes.evoker.tip¦Feiticeiro que faz brotar presas do chão e invoca espíritos. Derrote-o logo para interromper as invocações.¦Sorcier qui fait jaillir des crocs du sol et invoque des esprits. Battez-le vite pour couper les invocations.¦Zauberer, der Reißzähne aus dem Boden wachsen lässt und Geister beschwört. Besiege ihn schnell, um die Beschwörungen zu beenden.
bestiary.aptitudes.ravager.tip¦Fera de invasão que investe e ruge, derrubando tudo. Ataque pelos lados e bloqueie as mordidas com escudo para atordoá-la.¦Bête de raid qui charge et rugit en repoussant tout. Frappez-la sur les côtés et bloquez ses morsures au bouclier pour l'étourdir.¦Überfallbestie, die anstürmt und brüllt und alles zurückwirft. Greife von der Seite an und blocke ihre Bisse mit dem Schild, um sie zu betäuben.
bestiary.aptitudes.guardian.tip¦Sentinela dos monumentos oceânicos que carrega um raio. Corte sua linha de visão com blocos e cuidado com seus espinhos ao atacar.¦Sentinelle des monuments sous-marins qui charge un rayon. Coupez sa ligne de vue avec des blocs et méfiez-vous de ses épines en attaquant.¦Wächter der Ozeanmonumente, der einen Strahl auflädt. Unterbrich seine Sichtlinie mit Blöcken und achte beim Angriff auf seine Stacheln.
bestiary.aptitudes.elder_guardian.tip¦Antigo guardião do monumento. Amaldiçoa os jogadores próximos com Fadiga de mineração; leve Respiração aquática e boa armadura.¦Ancien gardien du monument. Il maudit les joueurs proches avec Fatigue de minage ; apportez Respiration aquatique et une bonne armure.¦Uralter Hüter des Monuments. Er belegt Spieler in der Nähe mit Abbaulähmung; nimm Unterwasseratmung und gute Rüstung mit.
bestiary.aptitudes.breeze.tip¦Espírito do vento esquivo das câmaras de desafio. Salta sem parar, dispara cargas de vento que empurram e desvia projéteis.¦Esprit du vent insaisissable des chambres d'épreuves. Il bondit sans cesse, tire des charges de vent qui repoussent et dévie les projectiles.¦Flinker Windgeist der Prüfungskammern. Er springt ständig umher, feuert zurückstoßende Windladungen und lenkt Geschosse ab.
bestiary.aptitudes.warden.tip¦Guardião cego da escuridão profunda que caça pelo som e pelo olfato. Seu estrondo sônico atravessa armadura e escudo. Agache-se, distraia-o jogando itens ou fuja.¦Gardien aveugle des profondeurs obscures qui chasse au son et à l'odeur. Son explosion sonique traverse armure et bouclier. Accroupissez-vous, distrayez-le en lançant des objets ou fuyez.¦Blinder Wächter der Tiefen Dunkelheit, der nach Geräusch und Geruch jagt. Sein Schallstoß durchdringt Rüstung und Schild. Schleiche, lenke ihn mit geworfenen Gegenständen ab oder flieh.
bestiary.aptitudes.zombified_piglin.tip¦Neutro até que um seja atacado; então o grupo inteiro revida. Nunca golpeie um em área cheia.¦Neutre jusqu'à ce qu'on en attaque un ; alors tout le groupe riposte. Ne frappez jamais l'un d'eux dans une zone bondée.¦Neutral, bis einer angegriffen wird; dann schlägt die ganze Gruppe zurück. Greife nie einen in einer belebten Gegend an.
bestiary.aptitudes.piglin_brute.tip¦Guarda dos bastiões com machado de ouro, sempre hostil mesmo se você usar ouro. Não reaparece: elimine-o com cuidado.¦Garde des bastions armé d'une hache en or, toujours hostile même si vous portez de l'or. Il ne réapparaît pas : éliminez-le prudemment.¦Bastionwache mit Goldaxt, immer feindlich, auch wenn du Gold trägst. Sie erscheint nicht erneut: räume sie vorsichtig aus.
bestiary.aptitudes.hoglin.tip¦Fera das florestas carmesins que investe e arremessa você para o alto. Foge de fungos distorcidos e vira zoglin na superfície.¦Bête des forêts écarlates qui charge et vous projette en l'air. Elle fuit les champignons biscornus et devient zoglin à la surface.¦Bestie der Karmesinwälder, die anstürmt und dich in die Luft schleudert. Sie flieht vor Wirrpilzen und wird in der Oberwelt zum Zoglin.
bestiary.aptitudes.blaze.tip¦Espírito das fortalezas que flutua e dispara rajadas de bolas de fogo. Resistência ao fogo e bolas de neve facilitam muito.¦Esprit des forteresses qui flotte et tire des salves de boules de feu. Résistance au feu et boules de neige facilitent beaucoup le combat.¦Festungsgeist, der schwebt und Feuerballsalven abfeuert. Feuerresistenz und Schneebälle machen ihn viel leichter.
bestiary.aptitudes.ghast.tip¦Criatura enorme e flutuante cujas bolas de fogo explodem ao impacto. Rebata-as com um golpe ou atire nela com arco.¦Énorme créature flottante dont les boules de feu explosent à l'impact. Renvoyez-les d'un coup ou tirez-lui dessus à l'arc.¦Riesige schwebende Kreatur, deren Feuerbälle beim Aufprall explodieren. Schlage sie zurück oder schieße mit dem Bogen auf ihn.
bestiary.aptitudes.magma_cube.tip¦Cubo ígneo do Nether que se divide como um slime e salta alto. Os grandes batem forte: lute de um ponto elevado.¦Cube ardent du Nether qui se divise comme un slime et saute haut. Les gros frappent fort : battez-vous en hauteur.¦Feuriger Netherwürfel, der sich wie ein Schleim teilt und hoch springt. Die großen schlagen hart zu: kämpfe von erhöhter Position.
bestiary.aptitudes.wither_skeleton.tip¦Espadachim alto das fortalezas cujos golpes causam Definhamento. Um escudo e uma arma rápida ajudam; às vezes deixa cair o crânio.¦Grand épéiste des forteresses dont les coups infligent Wither. Un bouclier et une arme rapide aident ; il lâche parfois son crâne.¦Großer Festungsschwertkämpfer, dessen Hiebe Verdorrung verursachen. Schild und schnelle Waffe helfen; manchmal lässt er seinen Schädel fallen.
bestiary.aptitudes.endermite.tip¦Praga minúscula que pode surgir onde uma pérola do Ender cai. Os endermen a atacam assim que a veem.¦Minuscule nuisible qui peut apparaître là où atterrit une perle de l'Ender. Les endermen l'attaquent dès qu'ils la voient.¦Winziger Schädling, der dort erscheinen kann, wo eine Enderperle landet. Endermen greifen ihn sofort an.
bestiary.aptitudes.shulker.tip¦Torre das cidades do End que se esconde na concha e dispara projéteis teleguiados que causam Levitação. Golpeie quando abrir.¦Tourelle des cités de l'End qui se cache dans sa coquille et tire des projectiles à tête chercheuse qui causent Lévitation. Frappez quand elle s'ouvre.¦Geschützturm der Endstädte, der sich in seiner Schale versteckt und zielsuchende Geschosse mit Schwebeeffekt abfeuert. Schlage zu, wenn er sich öffnet.
bestiary.aptitudes.wither.tip¦Horror de três cabeças invocado: dispara crânios explosivos e resiste a flechas com metade da vida. Lute no subsolo e leve cura.¦Horreur à trois têtes que l'on invoque : il tire des crânes explosifs et résiste aux flèches à mi-vie. Combattez-le sous terre et apportez des soins.¦Beschworener dreiköpfiger Schrecken, der explosive Schädel verschießt und ab halber Lebenskraft Pfeilen widersteht. Kämpfe unter der Erde und nimm Heilung mit.
bestiary.aptitudes.ender_dragon.tip¦Soberana do End, curada pelos cristais nos pilares de obsidiana. Destrua os cristais primeiro e ataque quando pousar no portal.¦Maître de l'End, soigné par les cristaux au sommet des piliers d'obsidienne. Détruisez d'abord les cristaux, puis frappez quand il se pose sur le portail.¦Herrscher des Endes, geheilt von den Kristallen auf den Obsidiansäulen. Zerstöre zuerst die Kristalle und schlage zu, wenn er auf dem Portal landet.
bestiary.aptitudes.elite.tip¦Criatura fortalecida com um ou mais afixos, visíveis em seu nome. Bate mais forte e tem mais vida, mas deixa Essência de elite e fragmentos de alma.¦Créature renforcée par un ou plusieurs affixes visibles dans son nom. Elle frappe plus fort et a plus de vie, mais lâche de l'Essence d'élite et des fragments d'âme.¦Kreatur mit einem oder mehreren Affixen, die in ihrem Namen stehen. Sie schlägt härter zu und hat mehr Leben, lässt aber Eliteessenz und Seelensplitter fallen.
bestiary.aptitudes.champion.tip¦A elite mais forte, com sua própria barra de chefe. Seus afixos se acumulam; ao derrotá-la você ganha um Coração de campeão e às vezes armadura de classe.¦L'élite la plus forte, avec sa propre barre de boss. Ses affixes se cumulent ; la vaincre donne un Cœur de champion et parfois une armure de rôle.¦Die stärkste Elite mit eigener Bossleiste. Ihre Affixe stapeln sich; besiegt gewährt sie ein Championherz und manchmal Rollenrüstung.
bestiary.aptitudes.boss_tower_sentinel.tip¦Guardião do topo das torres. Fica mais forte na fase II; na final marca um círculo ao redor antes de golpear: saia da marca.¦Gardien du sommet des tours. Il se renforce en phase II ; en phase finale, il marque un cercle autour de lui avant de frapper : sortez de la marque.¦Wächter auf der Spitze der Türme. In Phase II wird er stärker; in der letzten Phase markiert er einen Kreis um sich, bevor er zuschlägt: verlasse die Markierung.
bestiary.aptitudes.boss_shrine_oracle.tip¦Feiticeiro dos santuários da Fogueira. Endurece na fase II e na final golpeia em um anel que também desacelera: fique perto, dentro do anel.¦Sorcier des sanctuaires du feu de camp. Il se durcit en phase II et, en phase finale, frappe en anneau en ralentissant : restez près de lui, dans l'anneau.¦Zauberer der Leuchtfeuerschreine. In Phase II wird er zäher, in der letzten Phase schlägt er in einem verlangsamenden Ring zu: bleib nah bei ihm, innerhalb des Rings.
bestiary.aptitudes.boss_camp_champion.tip¦Líder dos acampamentos de saqueadores. Na fase final mira um cone no jogador mais próximo: desvie para o lado ao ver o aviso.¦Chef des camps de pillards. En phase finale, il vise un cône vers le joueur le plus proche : esquivez sur le côté quand l'avertissement apparaît.¦Anführer der Plündererlager. In der letzten Phase zielt er mit einem Kegel auf den nächsten Spieler: weiche seitlich aus, sobald die Warnung erscheint.
bestiary.aptitudes.boss_eclipse_herald.tip¦Surge durante o Eclipse com armadura de netherita. Na fase final libera uma onda ao redor a cada poucos segundos: mova-se e ataque entre os pulsos.¦Apparaît pendant l'Éclipse en armure de netherite. En phase finale, il libère une onde de choc autour de lui toutes les quelques secondes : bougez et frappez entre les impulsions.¦Erscheint während der Finsternis in Netheritrüstung. In der letzten Phase stößt er alle paar Sekunden eine Schockwelle aus: bleib in Bewegung und schlage zwischen den Stößen zu.
""")

# v62 (K4): tooltips de objetos (rol, estadísticas y rareza).
add('', """
item.aptitudes.role_item¦Item de classe: %s¦Objet de rôle : %s¦Rollengegenstand: %s
item.aptitudes.hitscan.stats¦Dano %s · Alcance %s blocos · Recarga %s s¦Dégâts %s · Portée %s blocs · Recharge %s s¦Schaden %s · Reichweite %s Blöcke · Abklingzeit %s s
item.aptitudes.hitscan.lifesteal¦Rouba %s%% do dano como vida¦Vole %s %% des dégâts en vie¦Stiehlt %s %% des Schadens als Leben
item.aptitudes.stats.dps¦≈ %s de dano por segundo¦≈ %s dégâts par seconde¦≈ %s Schaden pro Sekunde
item.aptitudes.rarity.uncommon¦Raridade: incomum¦Rareté : peu commun¦Seltenheit: ungewöhnlich
item.aptitudes.rarity.rare¦Raridade: rara¦Rareté : rare¦Seltenheit: selten
item.aptitudes.rarity.epic¦Raridade: épica¦Rareté : épique¦Seltenheit: episch
""")

# v64 (K6): Templos sellados y Vasos de vida.
add('', """
block.aptitudes.sealed_stone¦Pedra selada¦Pierre scellée¦Versiegelter Stein
block.aptitudes.temple_seal¦Selo do templo¦Sceau du temple¦Tempelsiegel
block.aptitudes.reliquary¦Relicário¦Reliquaire¦Reliquiar
item.aptitudes.temple_key¦Chave do templo¦Clé du temple¦Tempelschlüssel
item.aptitudes.temple_key.tooltip¦Abre o Relicário do templo cujo guardião a deixou cair.¦Ouvre le Reliquaire du temple dont le gardien l'a lâchée.¦Öffnet das Reliquiar des Tempels, dessen Wächter ihn fallen ließ.
item.aptitudes.temple_key.blank¦Uma chave vazia: não pertence a nenhum templo.¦Une clé vide : elle n'appartient à aucun temple.¦Ein leerer Schlüssel: Er gehört zu keinem Tempel.
item.aptitudes.life_vessel¦Vaso de vida¦Vase de vie¦Lebensgefäß
item.aptitudes.life_vessel.tooltip¦Use: +1 coração permanente (cada vaso funciona uma vez).¦Utiliser : +1 cœur permanent (chaque vase ne sert qu'une fois).¦Benutzen: +1 dauerhaftes Herz (jedes Gefäß wirkt nur einmal).
entity.aptitudes.temple_guardian¦Guardião do templo¦Gardien du temple¦Tempelwächter
temple.aptitudes.levers_solved¦As alavancas se encaixam no lugar.¦Les leviers s'enclenchent.¦Die Hebel rasten ein.
temple.aptitudes.plates_reset¦O selo rejeita a ordem. Comece de novo.¦Le sceau refuse cet ordre. Recommencez.¦Das Siegel verwirft die Reihenfolge. Fang von vorn an.
temple.aptitudes.awakened¦O selo se rompe: o guardião do templo desperta!¦Le sceau se brise : le gardien du temple s'éveille !¦Das Siegel bricht: Der Tempelwächter erwacht!
temple.aptitudes.guardian_defeated¦O guardião cai e deixa a chave do templo.¦Le gardien tombe et laisse la clé du temple.¦Der Wächter fällt und hinterlässt den Tempelschlüssel.
temple.aptitudes.seal.levers¦Selo: reproduza com as alavancas as luzes sobre a porta.¦Sceau : reproduisez avec les leviers les lumières au-dessus de la porte.¦Siegel: Stelle mit den Hebeln die Lichter über der Tür nach.
temple.aptitudes.seal.plates¦Selo: pise nas placas na ordem das velas.¦Sceau : marchez sur les plaques dans l'ordre de leurs bougies.¦Siegel: Betritt die Platten in der Reihenfolge ihrer Kerzen.
temple.aptitudes.seal.awakened¦Selo: o guardião vigia o santuário.¦Sceau : le gardien veille sur le sanctuaire.¦Siegel: Der Wächter bewacht das Heiligtum.
temple.aptitudes.seal.cleared¦Selo: este templo está aberto.¦Sceau : ce temple est ouvert.¦Siegel: Dieser Tempel ist offen.
temple.aptitudes.reliquary.sealed¦O Relicário está trancado. O guardião do templo guarda a chave.¦Le Reliquaire est verrouillé. Le gardien du temple en garde la clé.¦Das Reliquiar ist verschlossen. Der Tempelwächter trägt den Schlüssel.
temple.aptitudes.reliquary.wrong_key¦Esta chave é de outro templo.¦Cette clé appartient à un autre temple.¦Dieser Schlüssel gehört zu einem anderen Tempel.
temple.aptitudes.reliquary.opened¦O Relicário se abre.¦Le Reliquaire s'ouvre.¦Das Reliquiar öffnet sich.
temple.aptitudes.reliquary.vessel¦Você recebe um Vaso de vida.¦Vous recevez un Vase de vie.¦Du erhältst ein Lebensgefäß.
temple.aptitudes.reliquary.claimed¦Você já pegou seu vaso deste Relicário.¦Vous avez déjà pris votre vase dans ce Reliquaire.¦Du hast dein Gefäß aus diesem Reliquiar schon genommen.
temple.aptitudes.vessel.used¦Vaso de vida: %s/%s.¦Vase de vie : %s/%s.¦Lebensgefäß: %s/%s.
temple.aptitudes.vessel.limit¦Você já tem o máximo de vasos.¦Vous avez déjà le maximum de vases.¦Du hast bereits die Höchstzahl an Gefäßen.
temple.aptitudes.vessel.duplicate¦Este vaso já foi usado.¦Ce vase a déjà été utilisé.¦Dieses Gefäß wurde bereits benutzt.
temple.aptitudes.vessel.disabled¦Os Vasos de vida estão desativados neste servidor.¦Les Vases de vie sont désactivés sur ce serveur.¦Lebensgefäße sind auf diesem Server deaktiviert.
advancement.aptitudes.temple_opened.title¦Sem selo¦Descellé¦Entsiegelt
advancement.aptitudes.temple_opened.description¦Abra o Relicário de um templo com sua chave.¦Ouvrez le Reliquaire d'un temple avec sa clé.¦Öffne das Reliquiar eines Tempels mit seinem Schlüssel.
advancement.aptitudes.life_vessel.title¦Coração pleno¦Cœur comblé¦Volles Herz
advancement.aptitudes.life_vessel.description¦Use um Vaso de vida.¦Utilisez un Vase de vie.¦Benutze ein Lebensgefäß.
""")

# v63 (C4): jefes animados (GeckoLib).
add('', """
entity.aptitudes.faceless_colossus¦O Colosso Sem Rosto¦Le Colosse sans visage¦Der gesichtslose Koloss
entity.aptitudes.osario_quebrado¦Ossuário Quebrado¦Ossuaire brisé¦Zerbrochenes Beinhaus
item.aptitudes.osario_quebrado_spawn_egg¦Ovo de aparição do Osario Quebrado¦Œuf d'apparition de l'Ossuaire brisé¦Erscheinungsei des Zerbrochenen Beinhauses
bestiary.aptitudes.osario_quebrado.tip¦Osso e terra que não descansam. Arrasta um braço e avança ao te ver.¦Os et terre qui refusent le repos. Il traîne un bras et charge dès qu'il vous voit.¦Knochen und Erde, die nicht ruhen wollen. Er schleift einen Arm und stürmt los, sobald er dich sieht.
contract.aptitudes.kind.nether¦Caça no Nether¦Chasse au Nether¦Jagd im Nether
contract.aptitudes.kind.new_mob¦Criaturas novas¦Créatures nouvelles¦Neue Kreaturen
contract.aptitudes.kind.expedition¦Expedição¦Expédition¦Expedition
entity.aptitudes.escorificador_hierro¦Escorificador de Ferro¦Scorificateur de fer¦Eisenschlacker
item.aptitudes.escorificador_hierro_spawn_egg¦Ovo de aparição do Escorificador de Ferro¦Œuf d'apparition du Scorificateur de fer¦Erscheinungsei des Eisenschlackers
bestiary.aptitudes.escorificador_hierro.tip¦Ferro e rocha com uma grelha incandescente no peito. A grelha acende antes de um golpe esmagador: saia do caminho.¦Fer et roche avec une grille incandescente sur la poitrine. La grille s'allume juste avant un coup écrasant : écartez-vous.¦Eisen und Gestein mit einem glühenden Gitter auf der Brust. Das Gitter leuchtet kurz vor einem zermalmenden Schlag auf: Weich aus.
boss.aptitudes.ruins_blocked¦As ruínas não podem ser fechadas: o terreno ao redor da invocação está ocupado.¦Les ruines ne peuvent pas être scellées : le terrain autour de l'invocation est encombré.¦Die Ruinen können nicht versiegelt werden: Der Boden um die Beschwörung ist blockiert.
elite.aptitudes.thorny¦Espinhoso¦Épineux¦Dornig
elite.aptitudes.searing¦Abrasador¦Brûlant¦Versengend
expedition.aptitudes.modifier.fog¦Névoa¦Brouillard¦Nebel
expedition.aptitudes.modifier.ember_rain¦Chuva de brasas¦Pluie de braises¦Glutregen
expedition.aptitudes.modifier.elite_surge¦Onda de elites¦Déferlante d'élites¦Elitenwelle
expedition.aptitudes.modifier.hunger¦Fome¦Faim¦Hunger
expedition.aptitudes.modifier.boss_stagger¦Chefe atordoado¦Boss étourdi¦Boss betäubt
expedition.aptitudes.modifier.loot_boost¦Saque abundante¦Butin abondant¦Reiche Beute
expedition.aptitudes.modifiers¦Modificadores: %s¦Modificateurs : %s¦Modifikatoren: %s
item.aptitudes.expedition_token¦Ficha de expedição¦Jeton d'expédition¦Expeditionsmarke
expedition.aptitudes.started¦Expedição iniciada com %s módulos. Primeiro: %s.¦Expédition lancée avec %s modules. Premier : %s.¦Expedition mit %s Modulen gestartet. Erstes: %s.
expedition.aptitudes.module_next¦Módulo %s/%s: %s.¦Module %s/%s : %s.¦Modul %s/%s: %s.
expedition.aptitudes.complete¦Expedição concluída: %s esmeraldas e %s fragmentos de alma.¦Expédition terminée : %s émeraudes et %s fragments d'âme.¦Expedition abgeschlossen: %s Smaragde und %s Seelensplitter.
expedition.aptitudes.timeout¦A expedição expirou.¦L'expédition a expiré.¦Die Expedition ist abgelaufen.
expedition.aptitudes.already¦Você já tem uma expedição em andamento.¦Vous avez déjà une expédition en cours.¦Du hast bereits eine laufende Expedition.
expedition.aptitudes.overworld_only¦As expedições só começam no Overworld.¦Les expéditions ne peuvent commencer que dans le monde principal.¦Expeditionen können nur in der Oberwelt beginnen.
expedition.aptitudes.module.combat¦Combate¦Combat¦Kampf
expedition.aptitudes.module.puzzle¦Coleta de almas¦Collecte d'âmes¦Seelensammlung
expedition.aptitudes.module.elite¦Elite¦Élite¦Elite
expedition.aptitudes.module.event¦Evento¦Événement¦Ereignis
expedition.aptitudes.module.boss¦Chefe¦Boss¦Boss
item.aptitudes.automaton_orb¦Esfera do Autômato¦Orbe de l'Automate¦Kugel des Automaten
item.aptitudes.automaton_orb_deflected¦Esfera devolvida do Autômato¦Orbe de l'Automate renvoyé¦Zurückgeworfene Kugel des Automaten
entity.aptitudes.errante_velado¦Andarilho Velado¦Errant voilé¦Verschleierter Wanderer
item.aptitudes.errante_velado_spawn_egg¦Ovo de aparição do Andarilho Velado¦Œuf d'apparition de l'Errant voilé¦Erscheinungsei des Verschleierten Wanderers
bestiary.aptitudes.errante_velado.tip¦Um véu de névoa de almas que flutua até os vivos. Fica suspenso e depois rasga com seu braço longo e com garra.¦Un voile de brume d'âmes qui dérive vers les vivants. Il reste suspendu puis déchire avec son long bras griffu.¦Ein Schleier aus Seelennebel, der auf die Lebenden zutreibt. Er schwebt und reißt dann mit seinem langen, krallenbewehrten Arm zu.
item.aptitudes.jiron_velo_espectral¦Farrapo de Véu Espectral¦Lambeau de voile spectral¦Spektraler Schleierfetzen
subtitles.aptitudes.errante_ambient¦O Errante Velado sussurra¦Le Vagabond voilé murmure¦Verhüllter Wanderer flüstert
subtitles.aptitudes.errante_alert¦O Errante Velado fica alerta¦Le Vagabond voilé se met en alerte¦Verhüllter Wanderer wird aufmerksam
subtitles.aptitudes.errante_prepare¦O véu do Errante se estica¦Le voile du Vagabond se tend¦Schleier des Wanderers spannt sich
subtitles.aptitudes.errante_rend¦O Errante rasga o ar¦Le Vagabond lacère¦Verhüllter Wanderer schlägt
subtitles.aptitudes.errante_hurt¦O Errante Velado sofre dano¦Le Vagabond voilé est blessé¦Verhüllter Wanderer wird verletzt
subtitles.aptitudes.errante_death¦O Errante Velado desaba¦Le Vagabond voilé s’effondre¦Verhüllter Wanderer bricht zusammen
subtitles.aptitudes.errante_despawn¦O Errante Velado se dissipa¦Le Vagabond voilé se dissipe¦Verhüllter Wanderer verblasst
entity.aptitudes.custodio_cuatro_llaves¦O Custódio das Quatro Chaves¦Le Gardien des Quatre Clés¦Der Wächter der vier Schlüssel
item.aptitudes.faceless_colossus_spawn_egg¦Ovo gerador do Colosso Sem Rosto¦Œuf d'apparition du Colosse sans visage¦Spawn-Ei des gesichtslosen Kolosses
bestiary.aptitudes.faceless_colossus.tip¦Guerreiro gigante com espadão. Anuncia um golpe amplo e um golpe descendente com onda: saia do círculo marcado. Com metade da vida sua armadura racha: fica mais rápido, investe de longe e recebe mais dano.¦Guerrier géant armé d'une grande épée. Il annonce un large balayage et un coup plongeant avec onde de choc : sortez du cercle marqué. À mi-vie son armure se fissure : il va plus vite, charge de loin et subit plus de dégâts.¦Riesiger Krieger mit Zweihänder. Er kündigt einen weiten Hieb und einen Abwärtsschlag mit Schockwelle an: Verlasse den markierten Kreis. Bei halber Lebenskraft bricht seine Rüstung: Er wird schneller, stürmt aus der Ferne an und erleidet mehr Schaden.
""")

# v63b-c (jefes): Guardián Desmontado y Autómata Antiguo.
add('', """
entity.aptitudes.dismantled_guardian¦Guardião Desmontado¦Gardien démantelé¦Zerlegter Wächter
entity.aptitudes.ancient_automaton¦Autômato Antigo¦Automate ancien¦Uralter Automat
entity.aptitudes.automaton_orb¦Esfera de energia¦Orbe d'énergie¦Energiekugel
item.aptitudes.dismantled_guardian_spawn_egg¦Ovo de aparição do Guardião Desmontado¦Œuf d'apparition du Gardien démantelé¦Spawn-Ei des zerlegten Wächters
item.aptitudes.ancient_automaton_spawn_egg¦Ovo de aparição do Autômato Antigo¦Œuf d'apparition de l'Automate ancien¦Spawn-Ei des uralten Automaten
piece.aptitudes.helm¦Elmo¦Heaume¦Helm
piece.aptitudes.shield¦Escudo¦Bouclier¦Schild
piece.aptitudes.gauntlets¦Manoplas¦Gantelets¦Handschuhe
guardian.aptitudes.piece_broken¦%s quebrado!¦%s brisé !¦%s zerbrochen!
guardian.aptitudes.core_exposed¦O núcleo está exposto!¦Le noyau est exposé !¦Der Kern liegt frei!
automaton.aptitudes.stunned¦Atordoado! Recebe o dobro de dano.¦Étourdi ! Il subit le double de dégâts.¦Betäubt! Doppelter Schaden.
bestiary.aptitudes.dismantled_guardian.tip¦Armadura vazia cujas peças devem ser quebradas em ordem: elmo, escudo e manoplas. Só a peça ativa recebe dano; quando as três caem, o núcleo fica exposto e recebe 50 % a mais. Seus ataques são anunciados antes de atingir.¦Armure vide dont les pièces doivent être brisées dans l'ordre : heaume, bouclier, gantelets. Seule la pièce active subit des dégâts ; quand les trois tombent, le noyau est exposé et subit 50 % de dégâts en plus. Ses attaques sont annoncées.¦Leere Rüstung, deren Teile in der Reihenfolge Helm, Schild, Handschuhe zerbrochen werden müssen. Nur das aktive Teil nimmt Schaden; sind alle drei gefallen, liegt der Kern frei und erleidet 50 % mehr Schaden. Angriffe werden vorher angekündigt.
bestiary.aptitudes.ancient_automaton.tip¦Construto de pedra e bronze. Suas esferas de energia podem ser rebatidas: a que o atinge o atordoa por 5 s, e enquanto estiver atordoado recebe o dobro de dano. Na fase 2 dispara um raio que para nos blocos sólidos. Não pode ser empurrado.¦Construct de pierre et de bronze. Ses orbes d'énergie peuvent être renvoyées : celle qui l'atteint l'étourdit 5 s, et pendant ce temps il subit le double de dégâts. En phase deux, il tire un rayon arrêté par les blocs solides. Il ne peut pas être repoussé.¦Konstrukt aus Stein und Bronze. Seine Energiekugeln können zurückgeschlagen werden: Die Kugel, die ihn trifft, betäubt ihn für 5 s, und während dieser Zeit erleidet er doppelten Schaden. In Phase zwei feuert er einen Strahl, der an festen Blöcken endet. Er lässt sich nicht wegstoßen.
""")

add('', """
aptitudes.respec.done¦Árvore de %s reiniciada: %s pontos devolvidos por %s níveis de experiência.¦Arbre de %s réinitialisé : %s points rendus pour %s niveaux d’expérience.¦Baum von %s zurückgesetzt: %s Punkte zurück für %s Erfahrungsstufen.
aptitudes.respec.disabled¦O reinício da árvore está desativado neste servidor.¦La réinitialisation de l’arbre est désactivée sur ce serveur.¦Das Zurücksetzen des Baums ist auf diesem Server deaktiviert.
aptitudes.respec.unavailable¦Você não pode reiniciar a árvore agora.¦Vous ne pouvez pas réinitialiser l’arbre pour le moment.¦Du kannst den Baum gerade nicht zurücksetzen.
aptitudes.respec.nothing_to_reset¦A árvore de %s não tem pontos gastos.¦L’arbre de %s n’a aucun point dépensé.¦Der Baum von %s hat keine ausgegebenen Punkte.
aptitudes.respec.not_enough_levels¦Você precisa de %2$s níveis de experiência para reiniciar a árvore de %1$s.¦Il vous faut %2$s niveaux d’expérience pour réinitialiser l’arbre de %1$s.¦Du brauchst %2$s Erfahrungsstufen, um den Baum von %1$s zurückzusetzen.
screen.aptitudes.respec.button¦Reiniciar¦Réinitialiser¦Zurücksetzen
screen.aptitudes.respec.confirm_title¦Reiniciar a árvore?¦Réinitialiser l’arbre ?¦Baum zurücksetzen?
screen.aptitudes.respec.confirm¦Reiniciar a árvore de %1$s devolve %2$s pontos e custa %3$s níveis de experiência. O nível e a evolução da classe não mudam, mas as sendas escolhidas ficam livres.¦Réinitialiser l’arbre de %1$s rend %2$s points et coûte %3$s niveaux d’expérience. Le niveau et l’évolution du rôle ne changent pas, mais les sentiers choisis redeviennent libres.¦Das Zurücksetzen des Baums von %1$s gibt %2$s Punkte zurück und kostet %3$s Erfahrungsstufen. Stufe und Entwicklung der Rolle bleiben, aber die gewählten Pfade werden frei.
screen.aptitudes.respec.cost¦Custa %s níveis de experiência¦Coûte %s niveaux d’expérience¦Kostet %s Erfahrungsstufen
screen.aptitudes.respec.missing¦Você precisa de %s níveis de experiência¦Il vous faut %s niveaux d’expérience¦Du brauchst %s Erfahrungsstufen
screen.aptitudes.respec.nothing¦Não há pontos gastos nesta classe¦Aucun point dépensé dans ce rôle¦Keine Punkte in dieser Rolle ausgegeben
""")
