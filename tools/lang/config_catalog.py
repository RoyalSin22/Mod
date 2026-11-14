"""
Traducciones (pt_br, fr_fr, de_de) de las descripciones de las opciones de configuración (v67): un tooltip por campo de
AptitudesConfig, clave config.aptitudes.field.<campo>. El inglés y el español se escriben en en_us/es_es. Sin signos de
porcentaje (los textos de Minecraft los tratan como formato). Las cifras deben coincidir con las del inglés.
Columnas: campo¦portugués¦francés¦alemán. Uso: lo lee gen_interface_lang.py.
"""
TEXT = {}


def rows(block):
    for row in block.strip().splitlines():
        field, pt, fr, de = row.split('¦')
        if field in TEXT or not (pt.strip() and fr.strip() and de.strip()):
            raise ValueError(f'Fila de configuración inválida o duplicada: {field}')
        TEXT[field] = (pt, fr, de)


rows("""
maxSkillLevel¦Nível máximo que uma habilidade pode alcançar.¦Niveau maximal qu’une compétence peut atteindre.¦Höchste Stufe, die eine Fertigkeit erreichen kann.
xpBase¦XP base de um nível de habilidade: xpBase × (nível+1)^xpExponent.¦XP de base d’un niveau de compétence : xpBase × (niveau+1)^xpExponent.¦Basis-EP einer Fertigkeitsstufe: xpBase × (Stufe+1)^xpExponent.
xpExponent¦Expoente da curva de XP das habilidades: quanto maior, mais caros os níveis.¦Exposant de la courbe d’XP des compétences : plus il est élevé, plus les niveaux coûtent cher.¦Exponent der EP-Kurve der Fertigkeiten: je höher, desto teurer die Stufen.
pointsPerSkillLevel¦Pontos de atributo por nível de habilidade. 0: os atributos só sobem na Fogueira.¦Points d’attribut par niveau de compétence. 0 : les attributs ne montent qu’au Feu de camp.¦Attributspunkte pro Fertigkeitsstufe. 0: Attribute steigen nur am Leuchtfeuer.
startingPoints¦Pontos de atributo com que cada jogador começa.¦Points d’attribut avec lesquels chaque joueur commence.¦Attributspunkte, mit denen jeder Spieler beginnt.
maxPointsPerAttribute¦Máximo de pontos que se pode colocar em um atributo.¦Maximum de points que l’on peut placer dans un attribut.¦Höchstzahl an Punkten, die in ein Attribut investiert werden können.
allowSelfRespec¦false: só um operador (nível 2) pode reiniciar atributos ou árvores com os comandos de reinício.¦false : seul un opérateur (niveau 2) peut réinitialiser attributs ou arbres avec les commandes de réinitialisation.¦false: Nur ein Operator (Stufe 2) kann Attribute oder Bäume mit den Zurücksetzen-Befehlen zurücksetzen.
treeRespecEnabled¦Permite reiniciar a árvore de uma classe com o botão Reiniciar, em troca de níveis de experiência.¦Permet de réinitialiser l’arbre d’une classe avec le bouton Réinitialiser, contre des niveaux d’expérience.¦Erlaubt es, den Baum einer Klasse mit der Schaltfläche Zurücksetzen gegen Erfahrungsstufen zurückzusetzen.
treeRespecLevelsBase¦Níveis de experiência fixos cobrados para reiniciar uma árvore com pontos gastos.¦Niveaux d’expérience fixes facturés pour réinitialiser un arbre avec des points dépensés.¦Feste Erfahrungsstufen, die das Zurücksetzen eines Baums mit ausgegebenen Punkten kostet.
treeRespecLevelsPerPoint¦Níveis de experiência extras por cada ponto gasto na classe (arredondado para cima).¦Niveaux d’expérience supplémentaires par point dépensé dans la classe (arrondi au supérieur).¦Zusätzliche Erfahrungsstufen pro in der Klasse ausgegebenem Punkt (aufgerundet).
treeRespecLevelsMax¦Teto do custo de reiniciar a árvore, em níveis. 0 torna o reinício gratuito.¦Plafond du coût de réinitialisation de l’arbre, en niveaux. 0 la rend gratuite.¦Obergrenze der Kosten für das Zurücksetzen des Baums, in Stufen. 0 macht es kostenlos.
awardXpInCreative¦Concede XP de habilidade a jogadores no Criativo.¦Accorde de l’XP de compétence aux joueurs en mode Créatif.¦Gewährt Fertigkeits-EP an Spieler im Kreativmodus.
awardXpForPlayerKills¦A XP de Combate também conta ao derrotar outros jogadores.¦L’XP de Combat compte aussi quand vous battez d’autres joueurs.¦Nahkampf-EP zählen auch, wenn du andere Spieler besiegst.
showXpInActionBar¦Mostra a XP ganha na barra de ação.¦Affiche l’XP gagnée dans la barre d’action.¦Zeigt die erhaltenen EP in der Aktionsleiste an.
levelUpMessages¦Avisa no chat quando uma habilidade sobe de nível.¦Annonce dans le chat quand une compétence monte de niveau.¦Meldet im Chat, wenn eine Fertigkeit eine Stufe aufsteigt.
miningXpStone¦XP de Mineração por bloco de pedra.¦XP de Minage par bloc de pierre.¦Bergbau-EP pro Steinblock.
miningXpOre¦XP de Mineração por bloco de minério.¦XP de Minage par bloc de minerai.¦Bergbau-EP pro Erzblock.
excavationXpBlock¦XP de Escavação por bloco cavado.¦XP d’Excavation par bloc creusé.¦Graben-EP pro gegrabenem Block.
woodcuttingXpLog¦XP de Corte de madeira por tronco.¦XP de Bûcheronnage par bûche.¦Holzfällen-EP pro Baumstamm.
farmingXpCrop¦XP de Agricultura por cultivo colhido.¦XP d’Agriculture par culture récoltée.¦Landwirtschafts-EP pro geernteter Pflanze.
combatXpPerMaxHealth¦XP de Combate por cada ponto de vida máxima da vítima.¦XP de Combat par point de santé maximale de la victime.¦Nahkampf-EP pro Punkt maximalen Lebens des Opfers.
archeryXpPerMaxHealth¦XP de Arco e flecha por cada ponto de vida máxima da vítima.¦XP de Tir à l’arc par point de santé maximale de la victime.¦Bogenschießen-EP pro Punkt maximalen Lebens des Opfers.
defenseXpPerDamage¦XP de Defesa por cada ponto de dano recebido.¦XP de Défense par point de dégâts subi.¦Verteidigungs-EP pro erlittenem Schadenspunkt.
agilityXpPer10Blocks¦XP de Agilidade a cada 10 blocos percorridos.¦XP d’Agilité tous les 10 blocs parcourus.¦Beweglichkeits-EP pro 10 zurückgelegte Blöcke.
fishingXpCatch¦XP de Pesca por peixe capturado (lixo e tesouros não contam).¦XP de Pêche par poisson capturé (déchets et trésors ne comptent pas).¦Angeln-EP pro gefangenem Fisch (Müll und Schätze zählen nicht).
enchantingXpPerEnchant¦XP de Encantamento por item encantado na mesa (a bigorna não conta).¦XP d’Enchantement par objet enchanté à la table (l’enclume ne compte pas).¦Verzaubern-EP pro am Tisch verzaubertem Gegenstand (der Amboss zählt nicht).
xpDiminishingEnabled¦Ativa o rendimento decrescente de XP por fonte repetida.¦Active le rendement décroissant de l’XP pour une source répétée.¦Aktiviert abnehmende EP-Erträge bei wiederholter Quelle.
xpDiminishingWindowSeconds¦Duração em segundos da janela deslizante que conta os eventos de XP repetidos.¦Durée en secondes de la fenêtre glissante qui compte les événements d’XP répétés.¦Dauer in Sekunden des gleitenden Fensters, das wiederholte EP-Ereignisse zählt.
xpDiminishingThreshold¦Eventos da mesma fonte que dão XP completa dentro da janela.¦Événements de la même source qui donnent l’XP complète dans la fenêtre.¦Ereignisse derselben Quelle, die innerhalb des Fensters volle EP geben.
xpDiminishingFloor¦Fração mínima de XP por evento quando a fonte está esgotada.¦Fraction minimale d’XP par événement quand la source est épuisée.¦Minimaler EP-Anteil pro Ereignis, wenn die Quelle erschöpft ist.
breakSpeedPerLevel¦Velocidade de quebra de blocos ganha por nível de habilidade de coleta.¦Vitesse de destruction des blocs gagnée par niveau de compétence de récolte.¦Abbaugeschwindigkeit, die pro Stufe einer Sammelfertigkeit hinzukommt.
farmingExtraDropChancePerLevel¦Chance por nível de Agricultura de um cultivo extra.¦Probabilité par niveau d’Agriculture d’une récolte supplémentaire.¦Chance pro Landwirtschaftsstufe auf eine zusätzliche Ernte.
combatDamagePerLevel¦Dano corpo a corpo ganho por nível de Combate.¦Dégâts de mêlée gagnés par niveau de Combat.¦Nahkampfschaden, der pro Nahkampfstufe hinzukommt.
archeryDamagePerLevel¦Dano à distância ganho por nível de Arco e flecha.¦Dégâts à distance gagnés par niveau de Tir à l’arc.¦Fernkampfschaden, der pro Bogenschießen-Stufe hinzukommt.
defenseToughnessPerLevel¦Resistência da armadura ganha por nível de Defesa.¦Robustesse d’armure gagnée par niveau de Défense.¦Rüstungshärte, die pro Verteidigungsstufe hinzukommt.
agilitySpeedPerLevel¦Velocidade de movimento ganha por nível de Agilidade.¦Vitesse de déplacement gagnée par niveau d’Agilité.¦Bewegungstempo, das pro Beweglichkeitsstufe hinzukommt.
agilitySafeFallPerLevel¦Distância de queda segura ganha por nível de Agilidade.¦Distance de chute sans danger gagnée par niveau d’Agilité.¦Sichere Fallhöhe, die pro Beweglichkeitsstufe hinzukommt.
miningArmorPerLevel¦Armadura ganha por nível de Mineração.¦Armure gagnée par niveau de Minage.¦Rüstung, die pro Bergbaustufe hinzukommt.
excavationKnockbackPerLevel¦Resistência a empurrão ganha por nível de Escavação.¦Résistance au recul gagnée par niveau d’Excavation.¦Rückstoßresistenz, die pro Grabenstufe hinzukommt.
woodcuttingHealthPerLevel¦Vida máxima ganha por nível de Corte de madeira.¦Santé maximale gagnée par niveau de Bûcheronnage.¦Maximales Leben, das pro Holzfällenstufe hinzukommt.
farmingLuckPerLevel¦Sorte ganha por nível de Agricultura.¦Chance gagnée par niveau d’Agriculture.¦Glück, das pro Landwirtschaftsstufe hinzukommt.
defenseArmorPerLevel¦Armadura ganha por nível de Defesa.¦Armure gagnée par niveau de Défense.¦Rüstung, die pro Verteidigungsstufe hinzukommt.
defenseExplosionPerLevel¦Redução de dano de explosões ganha por nível de Defesa.¦Réduction des dégâts d’explosion gagnée par niveau de Défense.¦Verringerung des Explosionsschadens, die pro Verteidigungsstufe hinzukommt.
fishingLuckPerLevel¦Sorte por nível de Pesca; melhora o saque de pesca do jogo base.¦Chance par niveau de Pêche ; améliore le butin de pêche du jeu de base.¦Glück pro Angeln-Stufe; verbessert die Beute des Grundspiels beim Angeln.
fishingDoubleCatchPerLevel¦Chance por nível de Pesca de capturar dois itens de uma vez.¦Probabilité par niveau de Pêche de capturer deux objets à la fois.¦Chance pro Angeln-Stufe, zwei Gegenstände auf einmal zu fangen.
enchantingRefundChancePerLevel¦Chance por nível de Encantamento de recuperar um nível ao encantar.¦Probabilité par niveau d’Enchantement de récupérer un niveau en enchantant.¦Chance pro Verzaubern-Stufe, beim Verzaubern eine Stufe zurückzubekommen.
enchantingManaPerLevel¦Mana máxima ganha por nível de Encantamento.¦Mana maximal gagné par niveau d’Enchantement.¦Maximales Mana, das pro Verzaubern-Stufe hinzukommt.
vigorHealthPerPoint¦Vida máxima por ponto de Vigor.¦Santé maximale par point de Vigueur.¦Maximales Leben pro Punkt Vitalität.
mindManaPerPoint¦Mana máxima por ponto de Mente.¦Mana maximal par point d’Esprit.¦Maximales Mana pro Punkt Geist.
enduranceArmorPerPoint¦Armadura por ponto de Resistência.¦Armure par point d’Endurance.¦Rüstung pro Punkt Ausdauer.
strengthDamagePerPoint¦Dano corpo a corpo por ponto de Força.¦Dégâts de mêlée par point de Force.¦Nahkampfschaden pro Punkt Stärke.
dexterityAttackSpeedPerPoint¦Velocidade de ataque por ponto de Destreza.¦Vitesse d’attaque par point de Dextérité.¦Angriffsgeschwindigkeit pro Punkt Geschicklichkeit.
intelligenceSpellDamagePerPoint¦Dano de feitiços por ponto de Inteligência.¦Dégâts des sorts par point d’Intelligence.¦Zauberschaden pro Punkt Intelligenz.
faithCooldownPerPoint¦Redução de recarga por ponto de Fé.¦Réduction de recharge par point de Foi.¦Verringerung der Abklingzeit pro Punkt Glaube.
luckPerPoint¦Sorte por ponto do atributo Sorte.¦Chance par point de l’attribut Chance.¦Glück pro Punkt des Attributs Glück.
maxRoleLevel¦Nível máximo que uma classe pode alcançar.¦Niveau maximal qu’une classe peut atteindre.¦Höchste Stufe, die eine Klasse erreichen kann.
roleXpBase¦XP base de um nível de classe, antes da curva da classe.¦XP de base d’un niveau de classe, avant la courbe de la classe.¦Basis-EP einer Klassenstufe, vor der Klassenkurve.
roleXpExponent¦Expoente da curva de XP de classe: quanto maior, mais caros os níveis.¦Exposant de la courbe d’XP de classe : plus il est élevé, plus les niveaux coûtent cher.¦Exponent der Klassen-EP-Kurve: je höher, desto teurer die Stufen.
secondaryRoleXpMultiplier¦Fração da XP de classe que as classes secundárias recebem.¦Fraction de l’XP de classe que reçoivent les classes secondaires.¦Anteil der Klassen-EP, den Nebenklassen erhalten.
rolePointsEveryLevels¦A cada quantos níveis de classe se ganha um ponto de árvore.¦Tous les combien de niveaux de classe on gagne un point d’arbre.¦Nach wie vielen Klassenstufen man einen Baumpunkt erhält.
combosEnabled¦Ativa o sistema de combos de combate.¦Active le système de combos de combat.¦Aktiviert das Kampf-Combosystem.
comboWindowSeconds¦Máximo de segundos permitido entre golpes para manter o combo.¦Nombre maximal de secondes autorisé entre deux coups pour garder le combo.¦Höchstzahl an Sekunden zwischen Treffern, um die Combo zu halten.
comboDamagePerHit¦Fração de dano extra por golpe de combo.¦Fraction de dégâts supplémentaires par coup de combo.¦Zusätzlicher Schadensanteil pro Combotreffer.
comboMaxBonus¦Teto do bônus de combo (dentro do teto global maxDamageMultiplier).¦Plafond du bonus de combo (dans le plafond global maxDamageMultiplier).¦Obergrenze des Combo-Bonus (innerhalb der globalen Grenze maxDamageMultiplier).
tranceGainCritical¦Sintonia que um golpe crítico em uma criatura dá (em 100).¦Accord que donne un coup critique sur une créature (sur 100).¦Einstimmung, die ein kritischer Treffer auf eine Kreatur gibt (von 100).
tranceGainSkill¦Sintonia ganha ao usar uma habilidade.¦Accord gagné en utilisant une capacité.¦Einstimmung, die man beim Einsatz einer Fähigkeit gewinnt.
tranceDurationSeconds¦Segundos que o transe dura quando a sintonia enche.¦Secondes que dure la transe quand l’accord est plein.¦Sekunden, die der Trancezustand anhält, wenn die Einstimmung voll ist.
tranceDamageMultiplier¦Multiplicador de dano causado durante o transe (1.1 significa dez a mais por cento).¦Multiplicateur des dégâts infligés pendant la transe (1.1 signifie dix de plus pour cent).¦Schadensmultiplikator im Trancezustand (1.1 bedeutet zehn Prozent mehr).
soulStainLossPercent¦Percentual da mancha de XP perdido ao morrer de novo antes de recuperá-la.¦Pourcentage de la tache d’XP perdu en mourant de nouveau avant de la récupérer.¦Prozentsatz des EP-Flecks, der verloren geht, wenn man erneut stirbt, bevor man ihn zurückholt.
expeditionZoneRadius¦Raio da zona de uma expedição, em blocos.¦Rayon de la zone d’une expédition, en blocs.¦Radius der Zone einer Expedition, in Blöcken.
expeditionTimeLimitMinutes¦Minutos até uma expedição expirar.¦Minutes avant l’expiration d’une expédition.¦Minuten, bis eine Expedition abläuft.
expeditionRewardEmeralds¦Esmeraldas de recompensa por completar uma expedição.¦Émeraudes de récompense pour avoir terminé une expédition.¦Smaragde als Belohnung für den Abschluss einer Expedition.
expeditionRewardSoulShards¦Fragmentos de alma de recompensa por completar uma expedição.¦Fragments d’âme de récompense pour avoir terminé une expédition.¦Seelensplitter als Belohnung für den Abschluss einer Expedition.
comboFinisherHits¦Golpes necessários para que o seguinte seja um golpe final.¦Coups nécessaires pour que le suivant soit un coup de grâce.¦Treffer, die nötig sind, damit der nächste ein Finisher ist.
comboFinisherCooldownSeconds¦Segundos mínimos entre golpes finais.¦Secondes minimales entre deux coups de grâce.¦Mindestsekunden zwischen Finishern.
partyMaxSize¦Máximo de membros por grupo, incluindo o líder.¦Nombre maximal de membres par groupe, chef compris.¦Höchstzahl an Mitgliedern pro Gruppe, den Anführer eingeschlossen.
partyMaxXpShare¦Máximo de XP que o líder pode compartilhar com o grupo (fração).¦Maximum d’XP que le chef peut partager avec le groupe (fraction).¦Höchstanteil an EP, den der Anführer mit der Gruppe teilen kann (Bruchteil).
partyXpBonusPerMember¦Fração de XP extra por cada membro conectado além do líder.¦Fraction d’XP supplémentaire par membre connecté en plus du chef.¦Zusätzlicher EP-Anteil pro verbundenem Mitglied außer dem Anführer.
partyXpBonusMax¦Teto do bônus de XP de grupo.¦Plafond du bonus d’XP de groupe.¦Obergrenze des Gruppen-EP-Bonus.
eclipseEnabled¦Ativa o evento do Eclipse. Se for desativado durante um eclipse, ele termina sem recompensas.¦Active l’événement de l’Éclipse. S’il est désactivé pendant une éclipse, elle se termine sans récompenses.¦Aktiviert das Finsternis-Ereignis. Wird es während einer Finsternis deaktiviert, endet sie ohne Belohnungen.
eclipseIntervalDays¦A cada quantos dias de jogo ocorre um eclipse.¦Tous les combien de jours de jeu une éclipse a lieu.¦Nach wie vielen Spieltagen eine Finsternis stattfindet.
eclipseFirstDay¦Primeiro dia em que pode haver um eclipse.¦Premier jour où une éclipse peut avoir lieu.¦Erster Tag, an dem eine Finsternis stattfinden kann.
eclipseWaveIntervalSeconds¦Segundos entre as ondas do eclipse.¦Secondes entre les vagues de l’éclipse.¦Sekunden zwischen den Wellen der Finsternis.
eclipseMaxMobsPerPlayer¦Máximo de criaturas do eclipse vivas a 48 blocos de cada jogador. 0: sem ondas.¦Maximum de créatures de l’éclipse vivantes à 48 blocs de chaque joueur. 0 : pas de vagues.¦Höchstzahl lebender Finsternis-Kreaturen im Umkreis von 48 Blöcken um jeden Spieler. 0: keine Wellen.
eclipseMobHealthMultiplier¦Multiplicador de vida das criaturas do eclipse (antes da escala de elite).¦Multiplicateur de santé des créatures de l’éclipse (avant l’échelle d’élite).¦Lebensmultiplikator der Finsternis-Kreaturen (vor der Elite-Skalierung).
eclipseMobDamageMultiplier¦Multiplicador de dano corpo a corpo das criaturas do eclipse.¦Multiplicateur de dégâts de mêlée des créatures de l’éclipse.¦Nahkampfschadensmultiplikator der Finsternis-Kreaturen.
eclipseEliteChance¦Chance de uma criatura do eclipse ser elite.¦Probabilité qu’une créature de l’éclipse soit d’élite.¦Chance, dass eine Finsternis-Kreatur eine Elite ist.
eclipseChampionChance¦Chance de campeão (verificada antes da de elite).¦Probabilité de champion (vérifiée avant celle d’élite).¦Chance auf einen Champion (wird vor der Elite-Chance geprüft).
eclipseHeraldEnabled¦Permite que o Arauto do Eclipse apareça ao meio-dia.¦Permet au Héraut de l’Éclipse d’apparaître à midi.¦Erlaubt dem Herold der Finsternis, mittags zu erscheinen.
eclipseSurvivorRoleXp¦XP da classe principal para quem não morre durante o eclipse.¦XP de la classe principale pour qui ne meurt pas pendant l’éclipse.¦EP der Hauptklasse für alle, die während der Finsternis nicht sterben.
eclipseSurvivorCombatXp¦XP de Combate para quem não morre durante o eclipse.¦XP de Combat pour qui ne meurt pas pendant l’éclipse.¦Nahkampf-EP für alle, die während der Finsternis nicht sterben.
eclipseTotalityBonus¦Vida e dano extras na Totalidade (0 desativa).¦Santé et dégâts supplémentaires pendant la Totalité (0 désactive).¦Zusätzliches Leben und zusätzlicher Schaden in der Totalität (0 schaltet es aus).
eclipseMobArmor¦Pontos de armadura extras de cada criatura do eclipse (0: nenhum).¦Points d’armure supplémentaires de chaque créature de l’éclipse (0 : aucun).¦Zusätzliche Rüstungspunkte jeder Finsternis-Kreatur (0: keine).
eclipseArmorChance¦Chance de cada peça de armadura visível em zumbis, esqueletos e semelhantes do eclipse.¦Probabilité de chaque pièce d’armure visible sur les zombies, squelettes et assimilés de l’éclipse.¦Chance auf jedes sichtbare Rüstungsteil bei Zombies, Skeletten und Ähnlichem der Finsternis.
eclipsePenumbraWaveSize¦Reforços solicitados por jogador na Penumbra; 0 desativa as ondas desta fase.¦Renforts demandés par joueur pendant la Pénombre ; 0 désactive les vagues de cette phase.¦Pro Spieler angeforderte Verstärkung in der Halbschatten-Phase; 0 deaktiviert die Wellen dieser Phase.
eclipseTotalityWaveSize¦Reforços por jogador na Totalidade; 0 desativa as ondas desta fase.¦Renforts par joueur pendant la Totalité ; 0 désactive les vagues de cette phase.¦Verstärkung pro Spieler in der Totalität; 0 deaktiviert die Wellen dieser Phase.
eclipseZoneMobCap¦Teto local extra de criaturas do eclipse na área de 48 blocos ao redor de cada jogador.¦Plafond local supplémentaire de créatures de l’éclipse dans la zone de 48 blocs autour de chaque joueur.¦Zusätzliche lokale Obergrenze an Finsternis-Kreaturen im Bereich von 48 Blöcken um jeden Spieler.
eclipseWorldMobCap¦Teto de novas aparições contando as criaturas do eclipse carregadas no Mundo Normal, Arautos incluídos.¦Plafond des nouvelles apparitions en comptant les créatures de l’éclipse chargées dans le Monde normal, Hérauts compris.¦Obergrenze für neue Spawns, wobei geladene Finsternis-Kreaturen in der Oberwelt mitgezählt werden, Herolde eingeschlossen.
eclipseMinElitesPerWave¦Elites garantidos por jogador e onda, depois dos campeões.¦Élites garanties par joueur et par vague, après les champions.¦Garantierte Eliten pro Spieler und Welle, nach den Champions.
eclipseMinChampionsTotality¦Campeões garantidos no início de cada onda da Totalidade.¦Champions garantis au début de chaque vague de la Totalité.¦Garantierte Champions zu Beginn jeder Welle der Totalität.
eclipseGuaranteedEliteArmor¦Os elites e campeões das ondas usam corpo de zumbi ou esqueleto com equipamento completo visível.¦Les élites et champions des vagues utilisent un corps de zombie ou de squelette avec un équipement complet visible.¦Eliten und Champions der Wellen haben einen Zombie- oder Skelettkörper mit vollständig sichtbarer Ausrüstung.
worldEventWeightEclipse¦Peso do Eclipse nos eventos rotativos do mundo.¦Poids de l’Éclipse dans les événements tournants du monde.¦Gewicht der Finsternis bei den rotierenden Weltereignissen.
worldEventWeightSoulFog¦Peso da Névoa das Almas nos eventos rotativos (0: nunca).¦Poids de la Brume des âmes dans les événements tournants (0 : jamais).¦Gewicht des Seelennebels bei den rotierenden Ereignissen (0: nie).
worldEventWeightEmberRain¦Peso da Chuva de Brasas nos eventos rotativos (0: nunca).¦Poids de la Pluie de braises dans les événements tournants (0 : jamais).¦Gewicht des Glutregens bei den rotierenden Ereignissen (0: nie).
emberRainLargeChance¦Chance de meteorito grande na Chuva de Brasas.¦Probabilité de météorite de grande taille dans la Pluie de braises.¦Chance auf einen großen Meteoriten im Glutregen.
emberRainHugeChance¦Chance de meteorito enorme (verificada antes da do grande).¦Probabilité de météorite énorme (vérifiée avant celle de la grande).¦Chance auf einen riesigen Meteoriten (wird vor der des großen geprüft).
emberRainMobChance¦Chance de um impacto trazer criaturas do Nether (0: nenhuma).¦Probabilité qu’un impact amène des créatures du Nether (0 : aucune).¦Chance, dass ein Einschlag Kreaturen aus dem Nether mitbringt (0: keine).
extraSkillDamageScale¦Multiplica o dano das novas habilidades N1–N10 das classes. 1: os valores indicados.¦Multiplie les dégâts des nouvelles capacités N1–N10 des classes. 1 : les valeurs indiquées.¦Multipliziert den Schaden der neuen Klassenfähigkeiten N1–N10. 1: die angegebenen Werte.
""")

rows("""
keystoneCarnageHeal¦Carnificina: fração da vida máxima curada ao matar.¦Carnage : fraction de la santé maximale soignée en tuant.¦Blutbad: Anteil des maximalen Lebens, der beim Töten geheilt wird.
keystoneCarnageHasteSeconds¦Carnificina: segundos de Pressa II.¦Carnage : secondes de Célérité II.¦Blutbad: Sekunden Eile II.
keystoneBulwarkThreshold¦Baluarte: fração de vida que o ativa.¦Rempart : fraction de santé qui le déclenche.¦Bollwerk: Lebensanteil, der es auslöst.
keystoneBulwarkAbsorption¦Baluarte: pontos de absorção (4 por nível de Absorção).¦Rempart : points d’absorption (4 par niveau d’Absorption).¦Bollwerk: Absorptionspunkte (4 pro Absorptionsstufe).
keystoneBulwarkSeconds¦Baluarte: duração em segundos.¦Rempart : durée en secondes.¦Bollwerk: Dauer in Sekunden.
keystoneBulwarkCooldownSeconds¦Baluarte: recarga em segundos.¦Rempart : recharge en secondes.¦Bollwerk: Abklingzeit in Sekunden.
keystoneOverloadEvery¦Sobrecarga: a cada quantas habilidades vem uma gratuita.¦Surcharge : toutes les combien de capacités on en reçoit une gratuite.¦Überladung: nach wie vielen Fähigkeiten eine kostenlose kommt.
keystoneDetonationDamage¦Detonação: dano (raio 3).¦Détonation : dégâts (rayon 3).¦Detonation: Schaden (Radius 3).
keystoneContagionTargets¦Contágio: inimigos que recebem os efeitos (raio 4).¦Contagion : ennemis qui reçoivent les effets (rayon 4).¦Ansteckung: Feinde, die die Effekte erhalten (Radius 4).
keystoneLegionLifesteal¦Legião: fração do dano dos seus lacaios que cura você.¦Légion : fraction des dégâts de vos sbires qui vous soigne.¦Legion: Anteil des Schadens deiner Diener, der dich heilt.
keystoneSpecterSeconds¦Espectro: segundos de crítico garantido após esquivar.¦Spectre : secondes de critique garanti après une esquive.¦Gespenst: Sekunden garantierter kritischer Treffer nach einem Ausweichen.
keystoneSpecterDodge¦Espectro: esquiva extra.¦Spectre : esquive supplémentaire.¦Gespenst: zusätzliche Ausweichchance.
keystoneFortuneChance¦Fortuna: chance de uma rolagem de saque extra.¦Fortune : probabilité d’un tirage de butin supplémentaire.¦Glück: Chance auf einen zusätzlichen Beutewurf.
keystoneFortuneEliteEmerald¦Fortuna: chance de esmeralda ao matar elites.¦Fortune : probabilité d’une émeraude en tuant des élites.¦Glück: Chance auf einen Smaragd beim Töten von Eliten.
keystoneTempestDamage¦Tempestade: dano da faísca (raio 5).¦Tempête : dégâts de l’étincelle (rayon 5).¦Sturm: Schaden des Funkens (Radius 5).
keystoneTempestCooldownSeconds¦Tempestade: recarga em segundos.¦Tempête : recharge en secondes.¦Sturm: Abklingzeit in Sekunden.
keystoneTempestChance¦Tempestade: chance em golpes diretos sem crítico.¦Tempête : probabilité sur les coups directs sans critique.¦Sturm: Chance bei direkten Treffern ohne Kritischen.
keystoneSanctuaryHeal¦Santuário: vida curada (raio 8).¦Sanctuaire : santé soignée (rayon 8).¦Zuflucht: geheiltes Leben (Radius 8).
keystoneSanctuaryIntervalSeconds¦Santuário: segundos entre curas.¦Sanctuaire : secondes entre deux soins.¦Zuflucht: Sekunden zwischen den Heilungen.
runeFlameSeconds¦Segundos de fogo da runa da Chama.¦Secondes de feu de la rune de flamme.¦Sekunden Feuer der Flammenrune.
runeLeechAmount¦Vida devolvida pelo Roubo de Vida.¦Santé rendue par le Vol de vie.¦Vom Lebensraub zurückgegebenes Leben.
runeLeechCooldownSeconds¦Recarga do Roubo de Vida em segundos.¦Recharge du Vol de vie en secondes.¦Abklingzeit des Lebensraubs in Sekunden.
runeSwiftAttackSpeed¦Velocidade de ataque da Agilidade (ao engastar).¦Vitesse d’attaque de la Vélocité (une fois sertie).¦Angriffsgeschwindigkeit der Flinkheit (beim Einsetzen).
runeSwiftFocusTicks¦Ticks a menos de recarga dos focos com Agilidade (mínimo 4 ticks).¦Ticks en moins de recharge des focalisateurs avec Vélocité (minimum 4 ticks).¦Ticks weniger Abklingzeit bei Foki mit Flinkheit (mindestens 4 Ticks).
runeForceDamage¦Dano da Força (ao engastar no corpo a corpo; a cada disparo nos focos).¦Dégâts de la Force (une fois sertie en mêlée ; à chaque tir pour les focalisateurs).¦Schaden der Kraft (beim Einsetzen im Nahkampf; bei jedem Schuss in Foki).
runeEclipseBonus¦Dano extra da runa do Eclipse contra elites, campeões e chefes.¦Dégâts supplémentaires de la rune d’éclipse contre les élites, champions et boss.¦Zusätzlicher Schaden der Finsternisrune gegen Eliten, Champions und Bosse.
runeFrostSeconds¦Segundos de Lentidão da Geada.¦Secondes de Lenteur du Givre.¦Sekunden Langsamkeit des Frosts.
runeFrostLevel¦Nível de Lentidão da Geada (0 é I, 1 é II).¦Niveau de Lenteur du Givre (0 correspond à I, 1 à II).¦Langsamkeitsstufe des Frosts (0 ist I, 1 ist II).
runeThunderChance¦Chance de descarga do Trovão.¦Probabilité de décharge du Tonnerre.¦Chance auf eine Entladung des Donners.
runeThunderDamage¦Dano da descarga do Trovão a cada alvo extra.¦Dégâts de la décharge du Tonnerre à chaque cible supplémentaire.¦Schaden der Donnerentladung an jedem zusätzlichen Ziel.
runeThunderTargets¦Monstros para os quais a descarga do Trovão salta (4 blocos).¦Monstres sur lesquels la décharge du Tonnerre rebondit (4 blocs).¦Monster, auf die die Donnerentladung überspringt (4 Blöcke).
runeThunderCooldownSeconds¦Segundos entre descargas do Trovão.¦Secondes entre deux décharges du Tonnerre.¦Sekunden zwischen Donnerentladungen.
runeGuardAbsorption¦Absorção que a Guarda dá ao golpear.¦Absorption que la Garde donne en frappant.¦Absorption, die die Wacht beim Treffen gibt.
runeGuardMaxAbsorption¦Teto de absorção da Guarda (não reduz uma absorção maior).¦Plafond d’absorption de la Garde (ne réduit pas une absorption plus grande).¦Absorptionsobergrenze der Wacht (senkt keine größere Absorption).
runeGuardCooldownSeconds¦Recarga da Guarda em segundos.¦Recharge de la Garde en secondes.¦Abklingzeit der Wacht in Sekunden.
runeRemovalEmeraldCost¦Esmeraldas para remover a última runa (grátis no Criativo).¦Émeraudes pour retirer la dernière rune (gratuit en Créatif).¦Smaragde, um die letzte Rune zu entfernen (im Kreativmodus kostenlos).
masterBonusPoints¦Pontos de árvore extras ao evoluir uma classe para Mestre.¦Points d’arbre supplémentaires en faisant évoluer une classe au stade Maître.¦Zusätzliche Baumpunkte, wenn sich eine Klasse zum Meister entwickelt.
ultimateBonusPoints¦Pontos de árvore extras ao evoluir uma classe para Definitivo.¦Points d’arbre supplémentaires en faisant évoluer une classe au stade Ultime.¦Zusätzliche Baumpunkte, wenn sich eine Klasse zu Ultimativ entwickelt.
roleChangeCooldownMinutes¦Minutos de espera para trocar de classe principal de novo (0: nenhum).¦Minutes d’attente avant de changer de nouveau de classe principale (0 : aucune).¦Wartezeit in Minuten, bevor die Hauptklasse erneut gewechselt werden kann (0: keine).
attributeScaling¦Como os atributos escalam: LINEAR, SOFTCAP ou SATURATING.¦Comment les attributs évoluent : LINEAR, SOFTCAP ou SATURATING.¦Wie Attribute skalieren: LINEAR, SOFTCAP oder SATURATING.
maxDamageMultiplier¦Teto global do multiplicador de dano total acumulado de todas as fontes.¦Plafond global du multiplicateur de dégâts total cumulé de toutes les sources.¦Globale Obergrenze des aus allen Quellen gestapelten Gesamtschadensmultiplikators.
attributeSoftCapFirst¦SOFTCAP: pontos de atributo a partir dos quais o rendimento cai.¦SOFTCAP : points d’attribut à partir desquels le rendement baisse.¦SOFTCAP: Attributspunkte, ab denen der Ertrag sinkt.
attributeSoftCapSecond¦SOFTCAP: pontos de atributo a partir dos quais o rendimento volta a cair.¦SOFTCAP : points d’attribut à partir desquels le rendement baisse de nouveau.¦SOFTCAP: Attributspunkte, ab denen der Ertrag erneut sinkt.
attributeSaturationPoints¦SATURATING: pontos com que um atributo se aproxima do máximo.¦SATURATING : points avec lesquels un attribut approche de son maximum.¦SATURATING: Punkte, mit denen sich ein Attribut seinem Maximum nähert.
legendaryExclusive¦Uma habilidade lendária só pode pertencer a um jogador por vez.¦Une capacité légendaire ne peut appartenir qu’à un joueur à la fois.¦Eine legendäre Fähigkeit kann nur einem Spieler gleichzeitig gehören.
eliteChance¦Chance de uma criatura natural aparecer como elite.¦Probabilité qu’une créature naturelle apparaisse en tant qu’élite.¦Chance, dass eine natürliche Kreatur als Elite erscheint.
roleChangeRequiresRitual¦Trocar de classe exige o ritual de renascimento.¦Changer de classe exige le rituel de renaissance.¦Der Klassenwechsel erfordert das Wiedergeburtsritual.
roleXpPerAbilityUse¦XP de classe por usar uma habilidade.¦XP de classe pour l’utilisation d’une capacité.¦Klassen-EP für den Einsatz einer Fähigkeit.
roleXpPerDamage¦XP de classe por cada ponto de dano causado.¦XP de classe par point de dégâts infligé.¦Klassen-EP pro verursachtem Schadenspunkt.
roleXpPerAction¦XP de classe por uma ação própria da classe.¦XP de classe pour une action propre à la classe.¦Klassen-EP für eine klassenspezifische Aktion.
baseMaxEnergy¦Energia máxima base das habilidades ativas.¦Énergie maximale de base des capacités actives.¦Basis-Höchstenergie der aktiven Fähigkeiten.
baseEnergyRegenPerSecond¦Regeneração de energia base por segundo.¦Régénération d’énergie de base par seconde.¦Basis-Energieregeneration pro Sekunde.
levelCostBase¦Custo base em níveis de experiência de um nível de alma.¦Coût de base en niveaux d’expérience d’un niveau d’âme.¦Basiskosten eines Seelenlevels in Erfahrungsstufen.
levelCostDivisor¦Divisor que freia como o custo de um nível de alma cresce.¦Diviseur qui freine la croissance du coût d’un niveau d’âme.¦Teiler, der bremst, wie stark die Kosten eines Seelenlevels wachsen.
levelCostMax¦Custo máximo em níveis de experiência de um nível de alma.¦Coût maximal en niveaux d’expérience d’un niveau d’âme.¦Höchste Kosten eines Seelenlevels in Erfahrungsstufen.
soulLevelsPerPoint¦Níveis de alma necessários para ganhar um ponto de atributo.¦Niveaux d’âme nécessaires pour gagner un point d’attribut.¦Seelenlevel, die für einen Attributspunkt nötig sind.
flaskUpgradeMaxRank¦Maior grau que se pode comprar na Fogueira para um frasco. 0 desativa as compras.¦Rang maximal que l’on peut acheter au Feu de camp pour une fiole. 0 désactive les achats.¦Höchster Rang, den man am Leuchtfeuer für eine Flasche kaufen kann. 0 deaktiviert Käufe.
lifeVesselMax¦Vasos de vida que contam por jogador (+1 coração cada). 0 desativa o uso.¦Fioles de vie comptées par joueur (+1 cœur chacune). 0 désactive leur usage.¦Lebensgefäße, die pro Spieler zählen (je +1 Herz). 0 deaktiviert die Nutzung.
primaryAffinityBonus¦Bônus quando um atributo coincide com a sua classe principal.¦Bonus quand un attribut correspond à votre classe principale.¦Bonus, wenn ein Attribut zu deiner Hauptklasse passt.
secondaryAffinityBonus¦Bônus quando um atributo coincide com uma classe secundária.¦Bonus quand un attribut correspond à une classe secondaire.¦Bonus, wenn ein Attribut zu einer Nebenklasse passt.
soulsBloodstain¦Ao morrer, sua XP fica em uma mancha recuperável onde você caiu.¦En mourant, votre XP reste dans une tache récupérable là où vous êtes tombé.¦Wenn du stirbst, bleiben deine EP in einem zurückholbaren Fleck dort, wo du gefallen bist.
damageNumbers¦Mostra números de dano flutuantes.¦Affiche des nombres de dégâts flottants.¦Zeigt schwebende Schadenszahlen an.
levelUpTitles¦Mostra um título na tela ao subir de nível.¦Affiche un titre à l’écran en montant de niveau.¦Zeigt beim Stufenaufstieg einen Titel auf dem Bildschirm an.
morphExplosionsBreakBlocks¦As explosões da forma Creeper do Metamorfo quebram blocos.¦Les explosions de la forme Creeper du Métamorphe détruisent des blocs.¦Die Explosionen der Creeper-Gestalt des Gestaltwandlers zerstören Blöcke.
uniqueGriefing¦As habilidades únicas com fogo podem incendiar blocos.¦Les capacités uniques avec du feu peuvent enflammer des blocs.¦Einzigartige Fähigkeiten mit Feuer können Blöcke entzünden.
uniqueReleaseAfterDays¦Uma habilidade única é liberada se o dono não se conectar por esta quantidade de dias (0: nunca).¦Une capacité unique est libérée si son propriétaire ne se connecte pas pendant ce nombre de jours (0 : jamais).¦Eine einzigartige Fähigkeit wird freigegeben, wenn ihr Besitzer so viele Tage nicht beitritt (0: nie).
alchemyHerbalistEnabled¦Ativa as vantagens e a XP de destilação do Boticário; não remove poções nem receitas.¦Active les avantages et l’XP de distillation de l’Apothicaire ; ne supprime ni potions ni recettes.¦Aktiviert die Brau-Vorteile und -EP des Apothekers; entfernt weder Tränke noch Rezepte.
alchemyHerbalistRadius¦Raio em que um Boticário próximo é detectado. 0: só ajuda quem abriu o suporte.¦Rayon dans lequel un Apothicaire proche est détecté. 0 : seul celui qui a ouvert le support aide.¦Radius, in dem ein Apotheker in der Nähe erkannt wird. 0: Nur wer den Ständer geöffnet hat, hilft.
alchemyBrewingSpeedBonus¦Fração extra de velocidade de destilação (0.5 é metade mais rápido; 0 mantém o tempo base).¦Fraction supplémentaire de vitesse de distillation (0.5 correspond à moitié plus vite ; 0 garde le temps de base).¦Zusätzlicher Anteil der Braugeschwindigkeit (0.5 ist die Hälfte schneller; 0 behält die Grundzeit).
alchemyDurationBonus¦Duração extra das poções em relação à receita original, sem aumentar a potência.¦Durée supplémentaire des potions par rapport à la recette d’origine, sans augmenter la puissance.¦Zusätzliche Wirkdauer der Tränke gegenüber dem Originalrezept, ohne die Stärke zu erhöhen.
alchemyRoleXpPerBrew¦XP base por preparo com efeitos (não por frasco). 0 desativa a XP.¦XP de base par préparation avec effets (pas par flacon). 0 désactive l’XP.¦Basis-EP pro Gebräu mit Effekten (nicht pro Flasche). 0 deaktiviert die EP.
alchemyXpWindowSeconds¦Janela pessoal de rendimento decrescente da XP de destilação, em segundos.¦Fenêtre personnelle de rendement décroissant de l’XP de distillation, en secondes.¦Persönliches Fenster abnehmender Erträge für Brau-EP, in Sekunden.
alchemyXpFullRewardBrews¦Preparos que dão XP completa por janela; depois cai a zero.¦Préparations qui donnent l’XP complète par fenêtre ; ensuite elle tombe à zéro.¦Gebräue, die pro Fenster volle EP geben; danach sinken sie auf null.
masteryEnabled¦Ativa o ganho e os efeitos de maestria; desativá-lo mantém o histórico.¦Active le gain et les effets de maîtrise ; le désactiver conserve l’historique.¦Aktiviert Gewinn und Wirkung der Meisterschaft; Deaktivieren behält den Verlauf.
masteryXpBase¦XP necessária para o primeiro ponto de maestria.¦XP nécessaire pour le premier point de maîtrise.¦EP, die für den ersten Meisterschaftspunkt nötig sind.
masteryXpStep¦Aumento de custo por cada ponto seguinte; 0 mantém o custo constante.¦Augmentation du coût par point suivant ; 0 garde le coût constant.¦Kostenanstieg pro folgendem Punkt; 0 hält die Kosten konstant.
masteryMaxBonus¦Teto do bônus de maestria (cada ponto dá 0.005).¦Plafond du bonus de maîtrise (chaque point donne 0.005).¦Obergrenze des Meisterschaftsbonus (jeder Punkt gibt 0.005).
threatEnabled¦Ativa a escala de ameaça, o saque e o aviso. Desativá-la remove a vida adicionada em 20 ticks.¦Active l’échelle de menace, le butin et l’avertissement. La désactiver retire la santé ajoutée en 20 ticks.¦Aktiviert Bedrohungsskalierung, Beute und Warnung. Deaktivieren entfernt das hinzugefügte Leben innerhalb von 20 Ticks.
threatRingSize¦Largura horizontal de cada anel de ameaça a partir do ponto de nascimento do mundo. A zona inicial é o nível 0.¦Largeur horizontale de chaque anneau de menace depuis le point d’apparition du monde. La zone de départ est le niveau 0.¦Horizontale Breite jedes Bedrohungsrings ab dem Weltspawn. Die Startzone ist Stufe 0.
threatMaxRing¦Anel máximo (I–V); distâncias maiores mantêm esse nível.¦Anneau maximal (I–V) ; les distances plus grandes gardent ce niveau.¦Höchster Ring (I–V); größere Entfernungen behalten diese Stufe.
threatHealthPerRing¦Vida base extra por anel (com teto interno de +200).¦Santé de base supplémentaire par anneau (avec un plafond interne de +200).¦Zusätzliches Basisleben pro Ring (mit interner Grenze von +200).
threatDamagePerRing¦Dano extra por anel (golpes, projéteis e explosões; com teto do dobro).¦Dégâts supplémentaires par anneau (coups, projectiles et explosions ; plafonnés au double).¦Zusätzlicher Schaden pro Ring (Treffer, Geschosse und Explosionen; auf das Doppelte begrenzt).
threatLootPerRing¦Chance por anel de uma rolagem de saque extra; teto de 100.¦Probabilité par anneau d’un tirage de butin supplémentaire ; plafond de 100.¦Chance pro Ring auf einen zusätzlichen Beutewurf; Obergrenze von 100.
threatEliteChancePerRing¦Aumento relativo por anel de eliteChance: base × (1 + anel × valor).¦Augmentation relative par anneau de eliteChance : base × (1 + anneau × valeur).¦Relativer Anstieg von eliteChance pro Ring: Basis × (1 + Ring × Wert).
""")

rows("""
fogCooldownDays¦Dias de espera entre duas Névoas das Almas.¦Jours d’attente entre deux Brumes des âmes.¦Tage Wartezeit zwischen zwei Seelennebeln.
fogDurationTicks¦Duração da Névoa das Almas em ticks (24000 é um dia de Minecraft).¦Durée de la Brume des âmes en ticks (24000 correspond à un jour Minecraft).¦Dauer des Seelennebels in Ticks (24000 ist ein Minecraft-Tag).
emberRainCooldownDays¦Dias de espera entre duas Chuvas de Brasas provocadas por um portal.¦Jours d’attente entre deux Pluies de braises provoquées par un portail.¦Tage Wartezeit zwischen zwei durch ein Portal ausgelösten Glutregen.
""")
