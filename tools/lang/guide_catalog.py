"""Traducción inicial del grimorio (T8 fase 2), pendiente de revisión nativa.

Las recetas se consultan en el juego. El bloque de filas de evolución procede
de su generador para que no se duplique como texto mantenido a mano.
"""
from check_lang import LOCALES
from gen_evolved_lang import guide_extra

TRANSLATIONS = {locale: {} for locale in LOCALES}


def chapter(name, titles, bodies):
    for locale, title, body in zip(LOCALES, titles, bodies, strict=True):
        prefix = f'guide.aptitudes.{name}'
        if prefix + '.title' in TRANSLATIONS[locale]:
            raise ValueError(f'Capítulo duplicado: {name}')
        TRANSLATIONS[locale][prefix + '.title'] = title
        TRANSLATIONS[locale][prefix + '.body'] = body.strip()


chapter('start', ('Boas-vindas', 'Bienvenue', 'Willkommen'), (
    """Aptitudes é um sistema RPG completo: suas HABILIDADES evoluem com as ações (minerar, cortar madeira, lutar…), seus ATRIBUTOS são comprados em uma Fogueira com experiência, e você escolhe uma CLASSE principal e uma secundária, cada uma com árvore de talentos, mana e habilidades.

Primeiros passos:
1. Fabrique uma Fogueira e descanse nela.
2. Pressione J e escolha a classe principal.
3. Gaste pontos na árvore (J) e equipe suas ativas.
4. Use V, B (classe principal) e N, M (secundária).""",
    """Aptitudes est un système RPG complet : vos COMPÉTENCES progressent par vos actions (miner, couper du bois, combattre…), vos ATTRIBUTS s'achètent à un feu de camp avec de l'expérience, et vous choisissez un RÔLE principal et un secondaire, chacun avec son arbre de talents, son mana et ses capacités.

Premiers pas :
1. Fabriquez un feu de camp et reposez-vous.
2. Appuyez sur J et choisissez votre rôle principal.
3. Dépensez vos points d'arbre (J) et équipez vos capacités actives.
4. Utilisez V, B (rôle principal) et N, M (secondaire).""",
    """Aptitudes ist ein vollständiges RPG-System: Deine FERTIGKEITEN steigen durch Tätigkeiten (Bergbau, Holzfällen, Kämpfen…), deine ATTRIBUTE kaufst du mit Erfahrung an einem Leuchtfeuer. Du wählst eine HAUPTROLLE und eine NEBENROLLE, jede mit eigenem Talentbaum, Mana und Fähigkeiten.

Erste Schritte:
1. Stelle ein Leuchtfeuer her und raste dort.
2. Drücke J und wähle deine Hauptrolle.
3. Verteile Baumpunkte (J) und rüste aktive Fähigkeiten aus.
4. Nutze V, B (Hauptrolle) und N, M (Nebenrolle)."""))

chapter('keys', ('Teclas', 'Touches', 'Tasten'), (
    """K: atributos e habilidades.
J: classes e árvore de talentos.
V / B: ativas da classe principal.
N / M: ativas da classe secundária.
Z: habilidade característica (Mestre).
R: definitiva (estágio Definitivo).
H: habilidade lendária.
G: trocar elemento (Elementalista).
Y: acessórios.
O: alternar HUD; Shift + O: ajustes do HUD.
Na árvore: arraste para mover, roda para zoom, C para centralizar. Se a janela não couber (escala de interface grande), use a roda fora da árvore ou PgUp/PgDn para rolar.
No grimório: ↑/↓ mudam o capítulo; clique em um item para ver sua receita; Backspace ou botão lateral do mouse voltam; ←/→ alternam a receita.
Tudo pode ser alterado em Opções → Controles.""",
    """K : attributs et compétences.
J : rôles et arbre de talents.
V / B : capacités actives du rôle principal.
N / M : capacités actives du rôle secondaire.
Z : capacité signature (Maître).
R : ultime (stade Ultime).
H : capacité légendaire.
G : changer d'élément (Élémentaliste).
Y : accessoires.
O : afficher/masquer l'ATH ; Maj + O : réglages de l'ATH.
Dans l'arbre : glisser pour déplacer, molette pour zoomer, C pour centrer. Si la fenêtre dépasse l'écran (grande échelle d'interface), défilez avec la molette hors de l'arbre ou Page préc./suiv.
Dans le grimoire : ↑/↓ changent de chapitre ; cliquez sur un objet pour sa recette ; Retour arrière ou bouton latéral de la souris pour revenir ; ←/→ changent de recette.
Tout est modifiable dans Options → Contrôles.""",
    """K: Attribute und Fertigkeiten.
J: Rollen und Talentbaum.
V / B: aktive Fähigkeiten der Hauptrolle.
N / M: aktive Fähigkeiten der Nebenrolle.
Z: Signaturfähigkeit (Meister).
R: ultimative Fähigkeit (Stufe Ultimativ).
H: legendäre Fähigkeit.
G: Element wechseln (Elementarist).
Y: Accessoires.
O: HUD umschalten; Umschalt + O: HUD-Einstellungen.
Im Baum: Ziehen zum Verschieben, Mausrad zum Zoomen, C zum Zentrieren. Passt das Fenster nicht (große GUI-Skalierung), scrolle mit dem Mausrad außerhalb des Baums oder Bild auf/ab.
Im Grimoire: ↑/↓ wechseln das Kapitel; ein Klick auf einen Gegenstand zeigt sein Rezept; Rücktaste oder seitliche Maustaste gehen zurück; ←/→ wechseln das Rezept.
Alles lässt sich unter Optionen → Steuerung ändern."""))

chapter('bonfire', ('A Fogueira', 'Le feu de camp', 'Das Leuchtfeuer'), (
    """Consulte a receita da Fogueira clicando em seu ícone no grimório ou no livro de receitas do jogo.

Descansar (clique direito): restaura toda a vida e o mana, define seu ponto de renascimento, reabastece os Frascos de Estus e de Éter e abre a tela de subida de nível.

Subir de nível: pontos de atributo faltantes são comprados como níveis de alma, pagando níveis comuns de experiência. Cada nível de alma custa 1 + nível/6 níveis de XP (máx. 10), e a cada 3 níveis de alma você ganha 1 ponto.

Mancha de XP: ao morrer, sua XP fica onde você caiu (chamas de alma que só você vê). Volte para recuperá-la antes de morrer novamente.

Ritual: use uma Oferenda de Renovação na Fogueira para poder trocar uma de suas classes.""",
    """Consultez la recette du feu de camp en cliquant sur son icône dans le grimoire ou dans le livre de recettes du jeu.

Repos (clic droit) : restaure toute la santé et le mana, définit votre réapparition, remplit vos Fioles d'Estus et d'éther et ouvre l'écran de montée de niveau.

Monter de niveau : les points d'attribut manquants s'achètent en niveaux d'âme contre des niveaux d'expérience ordinaires. Chaque niveau d'âme coûte 1 + niveau/6 niveaux d'XP (max 10), et tous les 3 niveaux d'âme accordent 1 point.

Trace d'XP : à votre mort, l'XP reste sur place (des flammes d'âme visibles de vous seul). Récupérez-la avant de mourir à nouveau.

Rituel : utilisez une Offrande de renouveau au feu de camp pour pouvoir changer un de vos rôles.""",
    """Das Leuchtfeuerrezept findest du per Klick auf sein Symbol im Grimoire oder im Rezeptbuch des Spiels.

Rasten (Rechtsklick): Stellt Leben und Mana vollständig wieder her, setzt den Wiederbelebungspunkt, füllt Estus- und Ätherflakons auf und öffnet den Stufenaufstieg.

Stufenaufstieg: Fehlende Attributpunkte kaufst du als Seelenstufen mit normalen Erfahrungsstufen. Jede Seelenstufe kostet 1 + Stufe/6 EP-Stufen (max. 10); je 3 Seelenstufen erhältst du 1 Punkt.

EP-Fleck: Beim Tod bleiben deine EP am Todesort (Seelenflammen, die nur du siehst). Hole sie vor dem nächsten Tod zurück.

Ritual: Nutze eine Opfergabe der Erneuerung am Leuchtfeuer, um eine deiner Rollen wechseln zu können."""))

chapter('attributes', ('Atributos', 'Attributs', 'Attribute'), (
    """Oito atributos estilo Souls (tela K):
Vigor: vida máxima.
Mente: mana máximo e regeneração.
Resistência: armadura.
Força: dano corpo a corpo.
Destreza: velocidade de ataque.
Inteligência: dano mágico.
Fé: menor recarga de todas as habilidades (limite global de 60%%).
Sorte: saque e probabilidades.

Cada classe tem 2 atributos afins (★): +50%% de valor se for a classe principal, +25%% se for secundária.""",
    """Huit attributs de style Souls (écran K) :
Vigueur : santé maximale.
Esprit : mana maximal et régénération.
Endurance : armure.
Force : dégâts de mêlée.
Dextérité : vitesse d'attaque.
Intelligence : dégâts magiques.
Foi : recharge réduite de toutes les capacités (limite globale de 60%%).
Chance : butin et probabilités.

Chaque rôle possède 2 attributs d'affinité (★) : +50%% de valeur pour le rôle principal, +25%% pour le secondaire.""",
    """Acht Attribute im Souls-Stil (Bildschirm K):
Vitalität: maximales Leben.
Geist: maximales Mana und Regeneration.
Ausdauer: Rüstung.
Stärke: Nahkampfschaden.
Geschicklichkeit: Angriffsgeschwindigkeit.
Intelligenz: magischer Schaden.
Glaube: kürzere Abklingzeit aller Fähigkeiten (globale Grenze 60%%).
Glück: Beute und Wahrscheinlichkeiten.

Jede Rolle hat 2 verwandte Attribute (★): +50%% Wert bei der Hauptrolle, +25%% bei der Nebenrolle."""))

chapter('skills', ('Habilidades por uso', 'Compétences par usage', 'Fertigkeiten durch Nutzung'), (
    """Elas evoluem ao jogar: Mineração, Escavação, Corte de madeira, Agricultura, Combate, Arco e flecha, Defesa, Agilidade, Pesca e Encantamento.

Cada nível concede um pequeno bônus passivo (velocidade de mineração, armadura, dano de flechas…). A cada 10 níveis você libera um MARCO: visão noturna ao minerar, replantio automático, derrubar árvores inteiras, flechas perfurantes, segunda chance ao morrer… Passe o mouse sobre cada habilidade na tela K para vê-los.

Repetir a MESMA fonte com muita frequência (o mesmo tipo de criatura, o mesmo atacante, pesca contínua) concede cada vez menos XP: fazendas de criaturas e pesca AFK não são eficientes. O jogo normal não é afetado.

COMBOS: golpes corpo a corpo encadeados em criaturas (em até 2.5 s) somam +3%% de dano cada (máx. +15%%). Com 5 golpes, o próximo é uma FINALIZAÇÃO conforme a categoria da classe principal: golpe sísmico (marcial), execução (sombras), corrente arcana (arcana), golpe sagrado que cura (guardiões) ou sobrecarga elétrica (ofícios). Receber um golpe forte quebra o combo.""",
    """Elles progressent en jouant : Minage, Excavation, Bûcheronnage, Agriculture, Combat, Tir à l'arc, Défense, Agilité, Pêche et Enchantement.

Chaque niveau accorde un petit bonus passif (vitesse de minage, armure, dégâts des flèches…). Tous les 10 niveaux, vous débloquez un PALIER : vision nocturne en minant, replantation automatique, abattage d'arbres entiers, flèches perforantes, seconde chance à la mort… Survolez chaque compétence sur l'écran K pour les consulter.

Répéter trop souvent la MÊME source (même type de créature, même attaquant, pêche continue) rapporte de moins en moins d'XP : fermes à créatures et pêche AFK ne sont pas efficaces. Le jeu normal n'est pas affecté.

COMBOS : les coups de mêlée enchaînés sur les créatures (dans les 2.5 s) ajoutent chacun +3%% de dégâts (max +15%%). Après 5 coups, le suivant est un COUP FINAL selon la catégorie du rôle principal : frappe sismique (martial), exécution (ombre), chaîne arcanique (arcanique), frappe sacrée qui soigne (gardiens) ou surcharge électrique (artisanat). Subir un coup puissant brise le combo.""",
    """Sie steigen beim Spielen: Bergbau, Graben, Holzfällen, Landwirtschaft, Nahkampf, Bogenschießen, Verteidigung, Beweglichkeit, Angeln und Verzaubern.

Jede Stufe gewährt einen kleinen passiven Bonus (Abbaugeschwindigkeit, Rüstung, Pfeilschaden…). Alle 10 Stufen schaltest du einen MEILENSTEIN frei: Nachtsicht beim Bergbau, automatisches Nachpflanzen, ganze Bäume fällen, durchdringende Pfeile, eine zweite Chance beim Tod… Fahre auf Bildschirm K über die Fertigkeiten, um die Boni zu sehen.

Wiederholst du zu oft DIESELBE Quelle (denselben Kreaturentyp, denselben Angreifer, ununterbrochenes Angeln), gibt es immer weniger EP: Kreaturenfarmen und AFK-Angeln sind ineffizient. Normales Spielen bleibt unbeeinflusst.

KOMBOS: Verkettete Nahkampftreffer gegen Kreaturen (innerhalb von 2.5 s) geben jeweils +3%% Schaden (max. +15%%). Nach 5 Treffern ist der nächste ein ABSCHLUSS nach der Kategorie deiner Hauptrolle: Erdbebenschlag (kriegerisch), Hinrichtung (Schatten), arkane Kette (arkan), heilender heiliger Schlag (Wächter) oder elektrische Überladung (Handwerk). Ein schwerer erlittener Treffer bricht die Kombo ab."""))

chapter('roles', ('Classes', 'Rôles', 'Rollen'), (
    """Há 20 classes em 5 categorias (Marcial, Arcana, Sombras, Guardiões e Ofícios). Você escolhe uma classe PRINCIPAL e uma SECUNDÁRIA (tela J); a secundária ganha XP pela metade.

Cada classe tem um item característico (usá-lo concede XP), um recurso próprio (Fúria, Concentração, Harmonia…; em 50%% potencializa a próxima habilidade), um conjunto de armadura e uma mecânica única.

Sinergia: duas classes da mesma categoria concedem +15%% de XP de classe; categorias diferentes concedem +15 de mana.

Preencher um espaço vazio é grátis. Para SUBSTITUIR uma classe escolhida, é necessário o ritual da Fogueira com uma Oferenda de Renovação.""",
    """Il existe 20 rôles dans 5 catégories (Martial, Arcanique, Ombre, Gardiens, Artisanat). Vous choisissez un rôle PRINCIPAL et un SECONDAIRE (écran J) ; le secondaire gagne l'XP deux fois moins vite.

Chaque rôle possède son objet caractéristique (l'utiliser donne de l'XP), sa ressource (Fureur, Concentration, Harmonie… ; à 50%%, elle renforce la capacité suivante), un ensemble d'armure et une mécanique unique.

Synergie : deux rôles de même catégorie accordent +15%% d'XP de rôle ; des catégories différentes donnent +15 de mana.

Remplir un emplacement vide est gratuit. Pour REMPLACER un rôle choisi, il faut le rituel du feu de camp avec une Offrande de renouveau.""",
    """Es gibt 20 Rollen in 5 Kategorien (Kriegerisch, Arkan, Schatten, Wächter, Handwerk). Du wählst eine HAUPT- und eine NEBENROLLE (Bildschirm J); die Nebenrolle erhält EP mit halber Geschwindigkeit.

Jede Rolle hat ihren typischen Gegenstand (Nutzung gewährt EP), eine eigene Ressource (Zorn, Fokus, Harmonie…; ab 50%% verstärkt sie die nächste Fähigkeit), ein Rüstungsset und eine eigene Mechanik.

Synergie: Zwei Rollen derselben Kategorie geben +15%% Rollen-EP; verschiedene Kategorien geben +15 Mana.

Ein freier Rollenplatz kann kostenlos belegt werden. Um eine gewählte Rolle zu ERSETZEN, brauchst du das Leuchtfeuerritual mit einer Opfergabe der Erneuerung."""))

chapter('tree', ('Árvore de talentos', 'Arbre de talents', 'Talentbaum'), (
    """A árvore de cada classe combina nós básicos, um nó lendário, filas de evolução e caminhos aprofundados:
• Passivas à esquerda, Caminhos no centro e Ativas à direita.
• Cada nó exige ao menos um grau no pré-requisito; as linhas mostram a rota.
• Três caminhos exclusivos: siga apenas UM por classe. O botão «Caminhos» mostra os sete escalões, a bifurcação no nível 32 e a pedra angular no 50. Veja «Caminhos e pedras angulares».
• Quatro ativas básicas e quatro da evolução: equipe duas por classe com Espaço 1 / Espaço 2.
• Reativas: passivas acionadas ao receber dano, abater ou realizar outras ações.
• Lendária: nó de nível 25, ativado com H.

PONTOS padrão: 1 no nível 1 e 3 a cada 3 níveis de classe; 51 no nível 50. Mestre soma 10 e Definitivo soma 5: até 66. Não é possível aprender tudo.""",
    """L'arbre de chaque rôle combine nœuds de base, nœud légendaire, rangées d'évolution et voies approfondies :
• Passifs à gauche, Voies au centre et Actifs à droite.
• Chaque nœud demande au moins un rang dans son prérequis ; les lignes montrent le chemin.
• Trois voies exclusives : suivez-en UNE par rôle. Le bouton «Voies» affiche les sept paliers, la bifurcation au niveau 32 et la pierre angulaire au 50. Voir «Voies et pierres angulaires».
• Quatre capacités actives de base et quatre d'évolution : équipez-en deux par rôle avec Emplacement 1 / Emplacement 2.
• Réactifs : passifs déclenchés par les dégâts subis, éliminations ou autres actions.
• Légendaire : nœud de niveau 25, activé avec H.

POINTS par défaut : 1 au niveau 1 et 3 tous les 3 niveaux de rôle ; 51 au niveau 50. Maître ajoute 10 et Ultime 5 : jusqu'à 66. Vous ne pouvez pas tout apprendre.""",
    """Der Rollenbaum verbindet Basisknoten, einen legendären Knoten, Entwicklungsreihen und vertiefte Pfade:
• Passive links, Pfade in der Mitte und aktive Fähigkeiten rechts.
• Jeder Knoten erfordert mindestens einen Rang in seinem Vorgänger; die Linien zeigen den Weg.
• Drei exklusive Pfade: Folge nur EINEM pro Rolle. Die Schaltfläche «Pfade» zeigt alle sieben Stufen, die Verzweigung auf Stufe 32 und den Schlüsselstein auf Stufe 50. Siehe «Pfade und Schlüsselsteine».
• Vier aktive Basisfähigkeiten und vier durch Entwicklung: Rüste zwei pro Rolle mit Platz 1 / Platz 2 aus.
• Reaktive: Passive Fähigkeiten, die durch erlittenen Schaden, Tötungen oder andere Aktionen ausgelöst werden.
• Legendär: Knoten auf Stufe 25, ausgelöst mit H.

Standard-PUNKTE: 1 auf Stufe 1 und 3 alle 3 Rollenstufen; 51 auf Stufe 50. Meister fügt 10 und Ultimativ 5 hinzu: bis zu 66. Du kannst nicht alles erlernen."""))
for locale in LOCALES:
    TRANSLATIONS[locale]['guide.aptitudes.tree.body'] += guide_extra(locale)

chapter('evolution', ('Evolução', 'Évolution', 'Entwicklung'), (
    """Cada classe pode evoluir duas vezes:
• MESTRE: nível 20, concluir a prova da classe (abates com sua arma, usos de habilidades…) e um Emblema de Maestria. Libera a característica (Z), +1 grau nos nós e uma arma nova.
• DEFINITIVO: nível 30, ter derrotado um chefe (Wither, Dragão ou Defensor) com a classe ativa e um Núcleo Definitivo. Libera a definitiva (R), mais um grau e a arma definitiva.

O botão Evoluir fica na tela J (a dica mostra o que falta).""",
    """Chaque rôle peut évoluer deux fois :
• MAÎTRE : niveau 20, terminer l'épreuve du rôle (éliminations avec son arme, capacités utilisées…) et un Emblème de maîtrise. Débloque la signature (Z), +1 rang aux nœuds et une nouvelle arme.
• ULTIME : niveau 30, avoir vaincu un boss (Wither, Dragon ou Warden) avec le rôle actif et un Noyau ultime. Débloque l'ultime (R), un rang supplémentaire et l'arme ultime.

Le bouton Évoluer est sur l'écran J (son infobulle indique ce qui manque).""",
    """Jede Rolle kann sich zweimal entwickeln:
• MEISTER: Stufe 20, die Rollenprüfung abschließen (Tötungen mit der Rollenwaffe, Fähigkeitsnutzungen…) und ein Meisterschaftsemblem. Schaltet die Signatur (Z), +1 Rang auf Knoten und eine neue Waffe frei.
• ULTIMATIV: Stufe 30, einen Boss (Wither, Drache oder Warden) mit aktiver Rolle besiegt haben und ein Ultimativer Kern. Schaltet die ultimative Fähigkeit (R), einen weiteren Rang und die ultimative Waffe frei.

Die Schaltfläche Entwickeln findest du auf Bildschirm J (der Tooltip zeigt fehlende Voraussetzungen)."""))

chapter('weapons', ('Armas de evolução', "Armes d'évolution", 'Entwicklungswaffen'), (
    """Ao evoluir para Mestre e Definitivo, você recebe uma arma nova da classe. Elas aparecem na fila superior da árvore, ao lado da característica e da definitiva.

• Classes corpo a corpo: uma lâmina (diamante em Mestre, netherita em Definitivo) que, com a classe ativa, cria um corte de sua cor, carrega o recurso e, na versão definitiva, cura um pouco.
• Classes mágicas ou à distância: um foco de disparo instantâneo (7 ou 10 de dano + 0.2 por nível de classe) que custa mana.

Se perder a arma, selecione-a na árvore e pressione Resgatar (apenas se ela não estiver no inventário ou baú do Ender).""",
    """En évoluant aux stades Maître puis Ultime, vous recevez une nouvelle arme de votre rôle. Elles apparaissent dans la rangée supérieure de l'arbre, près de la signature et de l'ultime.

• Rôles de mêlée : une lame (diamant au stade Maître, netherite au stade Ultime) qui, avec le rôle actif, produit un balayage coloré, charge sa ressource et, pour l'ultime, vous soigne légèrement.
• Rôles magiques ou à distance : un focaliseur à tir instantané (7 ou 10 dégâts + 0.2 par niveau de rôle), qui coûte du mana.

Si vous la perdez, sélectionnez-la dans l'arbre et appuyez sur Réclamer (seulement si elle n'est pas déjà dans votre inventaire ou coffre de l'Ender).""",
    """Bei der Entwicklung zum Meister und zu Ultimativ erhältst du jeweils eine neue Rollenwaffe. Sie erscheinen in der obersten Baumreihe neben Signatur und ultimativer Fähigkeit.

• Nahkampfrollen: Eine Klinge (Diamant als Meister, Netherit als Ultimativ), die bei aktiver Rolle einen farbigen Schwung erzeugt, die Rollenressource lädt und in der ultimativen Ausführung leicht heilt.
• Magische oder Fernkampfrollen: Ein Sofortschuss-Fokus (7 oder 10 Schaden + 0.2 pro Rollenstufe), der Mana kostet.

Verlierst du die Waffe, wähle sie im Baum und drücke Abholen (nur wenn sie nicht schon im Inventar oder in der Endertruhe liegt)."""))

chapter('elements', ('Elementalista', 'Élémentaliste', 'Elementarist'), (
    """O Elementalista domina até 3 elementos (nos níveis 1, 10 e 20) entre 14: Fogo, Gelo, Relâmpago, Terra, Vento, Água, Planta, Magma, Sombra, Luz, Vazio, Veneno, Cristal e Som. A escolha é permanente. G alterna o elemento ativo.

FUSÕES: ao dominar os dois elementos de um par e lançar Explosão Elemental com um deles ativo, a fusão é adicionada:
Fogo+Vento: Tempestade de Fogo · Água+Relâmpago: Eletrocussão · Gelo+Água: Nevasca · Terra+Magma: Erupção · Planta+Veneno: Praga · Luz+Sombra: Eclipse · Vazio+Cristal: Singularidade de Cristal · Som+Vento: Ressonância · Relâmpago+Cristal: Prisma · Fogo+Gelo: Choque Térmico.
Na vista de elementos (J → Elementos), cada elemento mostra suas fusões.""",
    """L'Élémentaliste maîtrise jusqu'à 3 éléments (aux niveaux 1, 10 et 20) parmi 14 : Feu, Glace, Foudre, Terre, Vent, Eau, Plante, Magma, Ombre, Lumière, Vide, Poison, Cristal et Son. Le choix est permanent. G change l'élément actif.

FUSIONS : si vous maîtrisez les deux éléments d'une paire et lancez l'Explosion élémentaire avec l'un d'eux actif, la fusion s'ajoute :
Feu+Vent : Tempête de feu · Eau+Foudre : Électrocution · Glace+Eau : Blizzard · Terre+Magma : Éruption · Plante+Poison : Peste · Lumière+Ombre : Éclipse · Vide+Cristal : Singularité de cristal · Son+Vent : Résonance · Foudre+Cristal : Prisme · Feu+Glace : Choc thermique.
Dans la vue des éléments (J → Éléments), chaque élément indique ses fusions.""",
    """Der Elementarist meistert bis zu 3 Elemente (auf Stufe 1, 10 und 20) aus 14: Feuer, Eis, Blitz, Erde, Wind, Wasser, Pflanze, Magma, Schatten, Licht, Leere, Gift, Kristall und Klang. Die Wahl ist dauerhaft. G wechselt das aktive Element.

FUSIONEN: Meisterst du beide Elemente eines Paares und wirkst den Elementarausbruch mit einem davon aktiv, kommt die Fusion hinzu:
Feuer+Wind: Feuersturm · Wasser+Blitz: Stromschlag · Eis+Wasser: Schneesturm · Erde+Magma: Eruption · Pflanze+Gift: Seuche · Licht+Schatten: Finsternis · Leere+Kristall: Kristallsingularität · Klang+Wind: Resonanz · Blitz+Kristall: Prisma · Feuer+Eis: Temperaturschock.
In der Elementansicht (J → Elemente) zeigt jedes Element seine Fusionen."""))

chapter('armor', ('Armaduras de classe', 'Armures de rôle', 'Rollenrüstung'), (
    """Cada classe tem um conjunto de 4 peças. A fabricação é feita SOMENTE na MESA DA GUILDA (clique direito): consulte a receita de cada peça no grimório, sem confundir com a bancada comum.

Com as 4 peças equipadas e a classe como principal ou secundária, o bônus do conjunto é ativado (veja a dica de cada peça). O peso altera a defesa: pesada (Berserker, Lanceiro, Paladino), média e leve.

As peças também aparecem em baús de torres e acampamentos, podem ser compradas com Vales de Arsenal do aldeão da guilda e são deixadas por campeões.""",
    """Chaque rôle possède un ensemble de 4 pièces. Elles se fabriquent UNIQUEMENT à la TABLE DE GUILDE (clic droit) : consultez la recette de chaque pièce dans le grimoire, sans la confondre avec l'établi ordinaire.

Avec les 4 pièces équipées et ce rôle comme principal ou secondaire, le bonus d'ensemble s'active (voir l'infobulle de chaque pièce). Le poids modifie la défense : lourde (Berserker, Lancier, Paladin), moyenne et légère.

Ces pièces apparaissent aussi dans les coffres des tours et camps, s'achètent avec les Bons d'armurerie du villageois de guilde et sont lâchées par les champions.""",
    """Jede Rolle besitzt ein Set aus 4 Teilen. Herstellung NUR am GILDENTISCH (Rechtsklick): Sieh dir das Rezept jedes Teils im Grimoire an; die normale Werkbank genügt nicht.

Mit allen 4 Teilen und dieser Rolle als Haupt- oder Nebenrolle wird der Setbonus aktiv (siehe Tooltip jedes Teils). Das Gewicht beeinflusst die Verteidigung: schwer (Berserker, Lanzer, Paladin), mittel und leicht.

Die Teile findest du auch in Turm- und Lagertruhen, beim Gildendorfbewohner über Rüstkammergutscheine und als Beute von Champions."""))

chapter('soul_items', ('Itens de alma', "Objets d'âme", 'Seelengegenstände'), (
    """Fragmento de Alma: material básico. Deixado por monstros (veja Saque).
Frasco de Estus: 5 cargas; cura 8 de vida.
Frasco de Éter: 5 cargas; restaura 40%% de mana.
(Os frascos são reabastecidos ao descansar na Fogueira.)
Ferramentas de alma: consulte suas receitas no grimório. Picareta e pá quebram 3×3 ao agachar; o machado derruba a árvore inteira ao agachar; a enxada colhe e replanta 3×3.
Oferenda de Renovação: permite trocar uma classe (ritual da Fogueira).
Vale de Arsenal: concede uma peça do conjunto da classe principal.""",
    """Fragment d'âme : matériau de base. Lâché par les monstres (voir Butin).
Fiole d'Estus : 5 charges ; rend 8 de santé.
Fiole d'éther : 5 charges ; restaure 40%% de mana.
(Les fioles se remplissent en vous reposant au feu de camp.)
Outils d'âme : consultez leurs recettes dans le grimoire. Pioche et pelle cassent sur 3×3 accroupi ; la hache abat l'arbre entier accroupi ; la houe récolte et replante sur 3×3.
Offrande de renouveau : permet de changer un rôle (rituel du feu de camp).
Bon d'armurerie : donne une pièce de l'ensemble de votre rôle principal.""",
    """Seelensplitter: Grundmaterial. Beute von Monstern (siehe Beute).
Estusflakon: 5 Ladungen; heilt 8 Leben.
Ätherflakon: 5 Ladungen; stellt 40%% Mana wieder her.
(Flakons werden beim Rasten am Leuchtfeuer aufgefüllt.)
Seelenwerkzeuge: Rezepte findest du im Grimoire. Spitzhacke und Schaufel bauen beim Schleichen 3×3 ab; die Axt fällt beim Schleichen den ganzen Baum; die Hacke erntet und pflanzt auf 3×3 nach.
Opfergabe der Erneuerung: Erlaubt einen Rollenwechsel (Leuchtfeuerritual).
Rüstkammergutschein: Gibt ein Rüstungsteil deiner Hauptrolle."""))

chapter('accessories', ('Acessórios', 'Accessoires', 'Accessoires'), (
    """Tecla Y: 2 espaços de anel e 1 de amuleto. Mantidos ao morrer.
Anéis: Força (+1.5 de dano), Vitalidade (+4 de vida), Agilidade (+8%% de velocidade), Mana (+30), Vampiro (5%% de roubo de vida), Fortuna (+2 de sorte).
Amuletos: Guardião (+3 de armadura), Fênix (evita a morte a cada 10 min), Arcano (−10%% de recarga), Bosque (cura fora de combate), Troféu do Caçador (+25%% de dano contra elites).

Clique nos ícones dos acessórios para consultar as receitas reais do jogo, os ingredientes e os detalhes.""",
    """Touche Y : 2 emplacements d'anneau et 1 d'amulette. Conservés à la mort.
Anneaux : Force (+1.5 dégâts), Vitalité (+4 de santé), Vélocité (+8%% de vitesse), Mana (+30), Vampire (5%% de vol de vie), Fortune (+2 de chance).
Amulettes : Gardien (+3 d'armure), Phénix (évite la mort toutes les 10 min), Arcanique (−10%% de recharge), Bosquet (soigne hors combat), Trophée du chasseur (+25%% de dégâts aux élites).

Cliquez sur les icônes des accessoires pour consulter les recettes réelles du jeu, les ingrédients et les détails.""",
    """Taste Y: 2 Ringplätze und 1 Amulettplatz. Bleiben beim Tod erhalten.
Ringe: Stärke (+1.5 Schaden), Vitalität (+4 Leben), Flinkheit (+8%% Geschwindigkeit), Mana (+30), Vampir (5%% Lebensraub), Glück (+2 Glück).
Amulette: Wächter (+3 Rüstung), Phönix (rettet alle 10 Min. vor dem Tod), Arkan (−10%% Abklingzeit), Hain (Heilung außerhalb des Kampfes), Jagdtrophäe (+25%% Schaden gegen Elitegegner).

Klicke auf die Accessoire-Symbole, um die tatsächlichen Spielrezepte, Zutaten und Details zu sehen."""))

chapter('elites', ('Elites e campeões', 'Élites et champions', 'Elitegegner und Champions'), (
    """A chance básica de monstros ELITES é de 5%% (nome visível, vida dobrada e 1-2 modificadores: Veloz, Blindado, Vampírico, Brutal, Regenerador, Explosivo, Gélido, Teleportador, Gigante).

Algumas elites são CAMPEÕES (3 modificadores, vida quadruplicada e barra de chefe). Toda torre tem um esqueleto campeão no topo.

Eles deixam Fragmentos de Alma, Essência de Elite e, os campeões, um Coração de Campeão e uma peça de armadura de classe.""",
    """La chance de base d'apparition des monstres ÉLITES est de 5%% (nom visible, santé doublée et 1-2 modificateurs : Rapide, Blindé, Vampirique, Brutal, Régénérant, Explosif, Glacial, Insaisissable, Géant).

Certains élites sont des CHAMPIONS (3 modificateurs, quatre fois la santé et une barre de boss). Chaque tour abrite un squelette champion sur son toit.

Ils lâchent des Fragments d'âme, de l'Essence d'élite et, pour les champions, un Cœur de champion et une pièce d'armure de rôle.""",
    """Die Grundchance für ELITE-Monster beträgt 5%% (sichtbarer Name, doppeltes Leben und 1-2 Modifikatoren: Flink, Gepanzert, Vampirisch, Brutal, Regenerierend, Explosiv, Frostig, Teleportierend, Riesig).

Einige Elitegegner sind CHAMPIONS (3 Modifikatoren, vierfaches Leben und Bossleiste). Jeder Turm hat einen Skelettchampion auf dem Dach.

Sie lassen Seelensplitter und Eliteessenz fallen; Champions außerdem ein Championsherz und ein Rollenrüstungsteil."""))

chapter('structures', ('Estruturas', 'Structures', 'Bauwerke'), (
    """TORRE EM RUÍNAS: 4 andares com baús, geradores de monstros e um campeão no topo, perto do melhor baú (armaduras de classe, ferramentas de alma, acessórios).
SANTUÁRIO DA FOGUEIRA: ruínas com uma Fogueira acesa e um baú (fragmentos, frascos e, às vezes, uma Oferenda).
ACAMPAMENTO: caçadores, bruxos ou bandidos, com guardas e saque temático.

Só são geradas em terreno novo (chunks inexplorados).""",
    """TOUR EN RUINES : 4 étages avec coffres, générateurs de monstres et un champion sur le toit, près du meilleur coffre (armures de rôle, outils d'âme, accessoires).
SANCTUAIRE DU FEU : ruines avec un feu de camp allumé et un coffre (fragments, fioles, parfois une Offrande).
CAMP : chasseurs, sorciers ou bandits, avec gardes et butin thématique.

Ces structures n'apparaissent que dans du nouveau terrain (chunks inexplorés).""",
    """TURMRUINE: 4 Stockwerke mit Truhen, Monsterspawnern und einem Champion auf dem Dach beim besten Schatz (Rollenrüstung, Seelenwerkzeuge, Accessoires).
LEUCHTFEUERSCHREIN: Ruinen mit einem entzündeten Leuchtfeuer und einer Truhe (Splitter, Flakons, manchmal eine Opfergabe).
LAGER: Jäger, Hexenmeister oder Banditen mit Wachen und passender Beute.

Sie entstehen nur in neuem Gelände (unerforschten Chunks)."""))

chapter('guild', ('Aldeão da guilda', 'Villageois de guilde', 'Gildendorfbewohner'), (
    """Coloque uma Mesa da Guilda perto de um aldeão sem profissão para torná-lo Mestre da Guilda. Consulte a receita real no grimório.

Conforme o nível, vende: Fragmentos de Alma, Frascos de Estus e de Éter, materiais de classe, Vales de Arsenal, Oferendas de Renovação, Emblemas de Maestria e Núcleos Definitivos. Compra couro e ossos.

A mesa também é uma oficina: clique direito para fabricar armaduras de classe (só podem ser feitas ali).

RUNAS: segure uma arma de classe em uma mão e uma runa na outra e use a Mesa da Guilda para engastar (duas por arma, diferentes). Há Chama, Roubo de Vida, Agilidade, Força, Geada, Trovão, Guarda e Eclipse. Para remover a última: Shift + clique direito na Mesa, com a arma na mão e a outra vazia; custa esmeraldas e a runa é perdida.""",
    """Placez une Table de guilde près d'un villageois sans métier pour en faire un Maître de guilde. Consultez la recette réelle dans le grimoire.

Selon son niveau, il vend : Fragments d'âme, Fioles d'Estus et d'éther, matériaux de rôle, Bons d'armurerie, Offrandes de renouveau, Emblèmes de maîtrise et Noyaux ultimes. Il achète du cuir et des os.

La table sert aussi d'atelier : clic droit pour fabriquer les armures de rôle (uniquement à cet endroit).

RUNES : tenez une arme de rôle dans une main et une rune dans l'autre, puis utilisez la Table de guilde pour la sertir (deux différentes par arme). Il existe Flamme, Vol de vie, Vélocité, Force, Givre, Tonnerre, Garde et Éclipse. Pour retirer la dernière : Maj + clic droit sur la Table avec l'arme en main et l'autre main vide ; cela coûte des émeraudes et la rune est perdue.""",
    """Stelle einen Gildentisch neben einen arbeitslosen Dorfbewohner, damit er Gildenmeister wird. Das tatsächliche Rezept findest du im Grimoire.

Je nach Stufe verkauft er: Seelensplitter, Estus- und Ätherflakons, Rollenmaterialien, Rüstkammergutscheine, Opfergaben der Erneuerung, Meisterschaftsembleme und Ultimative Kerne. Er kauft Leder und Knochen.

Der Tisch ist auch eine Werkstatt: Rechtsklick zum Herstellen von Rollenrüstung (nur dort möglich).

RUNEN: Halte eine Rollenwaffe in einer Hand und eine Rune in der anderen und nutze den Gildentisch zum Einsetzen (zwei verschiedene pro Waffe). Es gibt Flamme, Lebensraub, Flinkheit, Kraft, Frost, Donner, Wache und Finsternis. Letzte Rune entfernen: Umschalt + Rechtsklick auf den Tisch mit der Waffe in der Hand und leerer anderer Hand; kostet Smaragde und die Rune geht verloren."""))

chapter('recipes', ('Receitas', 'Recettes', 'Rezepte'), (
    """Este capítulo mostra o catálogo de receitas lidas do jogo. Busque um item e clique nele para ver os ingredientes, a disposição e a bancada necessária.

Consulte assim a Fogueira, o Grimório, a Oferenda de Renovação, os Frascos de Estus e de Éter, ferramentas de alma, armaduras de classe, Emblema de Maestria, Núcleo Definitivo, Fragmentos de Alma, Mesa da Guilda e acessórios.

As armaduras de classe são fabricadas na Mesa da Guilda. Para o destilado, veja o capítulo Alquimia. As fichas mostram as receitas reais sincronizadas pelo servidor; não use uma lista de ingredientes fixa.""",
    """Ce chapitre affiche le catalogue des recettes lues dans le jeu. Cherchez un objet et cliquez dessus pour voir les ingrédients, leur disposition et l'atelier nécessaire.

Consultez ainsi le feu de camp, le Grimoire, l'Offrande de renouveau, les Fioles d'Estus et d'éther, les outils d'âme, les armures de rôle, l'Emblème de maîtrise, le Noyau ultime, les Fragments d'âme, la Table de guilde et les accessoires.

Les armures de rôle se fabriquent à la Table de guilde. Pour le brassage, voir le chapitre Alchimie. Les fiches affichent les recettes réelles synchronisées par le serveur, plutôt qu'une liste d'ingrédients figée.""",
    """Dieses Kapitel zeigt den aus dem Spiel gelesenen Rezeptkatalog. Suche einen Gegenstand und klicke ihn an, um Zutaten, Anordnung und benötigte Werkbank zu sehen.

So findest du Leuchtfeuer, Grimoire, Opfergabe der Erneuerung, Estus- und Ätherflakons, Seelenwerkzeuge, Rollenrüstung, Meisterschaftsemblem, Ultimativen Kern, Seelensplitter, Gildentisch und Accessoires.

Rollenrüstung entsteht am Gildentisch. Zum Brauen siehe das Kapitel Alchemie. Die Detailseiten zeigen die tatsächlichen, vom Server synchronisierten Rezepte statt einer festgeschriebenen Zutatenliste."""))

chapter('drops', ('Saque', 'Butin', 'Beute'), (
    """Fragmento de Alma: 5%% dos monstros abatidos por um jogador (+1%% por ponto de sorte); 4 de cada chefe (Wither, Dragão, Defensor, Guardião Ancião).
Elites: 1-2 fragmentos, Essência de Elite (50%%) e mais XP.
Campeões: 4-6 fragmentos, Coração de Campeão, 2-3 Essências e uma peça de armadura de classe.
Baús de torres, santuários e acampamentos: fragmentos, frascos, itens de classe, armaduras, ferramentas de alma, acessórios, Oferendas e Emblemas.
Aldeão da guilda: quase tudo acima por esmeraldas.""",
    """Fragment d'âme : 5%% des monstres tués par un joueur (+1%% par point de chance) ; 4 par boss (Wither, Dragon, Warden, Grand gardien).
Élites : 1-2 fragments, Essence d'élite (50%%) et plus d'XP.
Champions : 4-6 fragments, Cœur de champion, 2-3 Essences et une pièce d'armure de rôle.
Coffres des tours, sanctuaires et camps : fragments, fioles, objets de rôle, armures, outils d'âme, accessoires, Offrandes et Emblèmes.
Villageois de guilde : presque tout cela contre des émeraudes.""",
    """Seelensplitter: 5%% der von Spielern getöteten Monster (+1%% pro Glückspunkt); 4 pro Boss (Wither, Drache, Warden, Großer Wächter).
Elitegegner: 1-2 Splitter, Eliteessenz (50%%) und mehr EP.
Champions: 4-6 Splitter, Championsherz, 2-3 Essenzen und ein Rollenrüstungsteil.
Turm-, Schrein- und Lagertruhen: Splitter, Flakons, Rollengegenstände, Rüstung, Seelenwerkzeuge, Accessoires, Opfergaben und Embleme.
Gildendorfbewohner: fast alles davon gegen Smaragde."""))

chapter('paths', ('Caminhos e pedras angulares', 'Voies et pierres angulaires', 'Pfade und Schlüsselsteine'), (
    """Cada classe tem três caminhos: A, B e C. Aprender o primeiro grau escolhe UM e bloqueia os outros dois. Reinicie a árvore para mudar de caminho.

Em J, o botão «Caminhos» abre a vista completa da classe selecionada: abas A/B/C, níveis, requisitos e confirmação ao escolher. S1–S3 também aparecem na grade da árvore.

SETE ESCALÕES (níveis padrão):
S1: 10; S2: 14; S3: 20; S4: 26.
S5: nível 32, bifurcação entre S5x/S5y: só é possível aprender UM ramo.
S6: nível 40; requer qualquer ramo S5 e repete o efeito de S4 com potência ×1.5.
S7: pedra angular, nível 50, um único grau; requer S6.
S1–S6 permitem três graus, cada um exigindo cinco níveis a mais. Cada grau custa um ponto.

PEDRAS ANGULARES: cada caminho possui um destes dez modelos. Aprender S7 o ativa na classe principal ou secundária, com indicador no HUD. Valores padrão abaixo; o servidor pode alterá-los:

Carnificina: ao abater, restaura 10%% da vida máxima e concede Pressa II por 5 s.

Baluarte: abaixo de 30%% de vida, concede Absorção II (8 pontos) e Resistência I por 6 s; recarga de 60 s.

Tempestade: críticos e 15%% dos golpes diretos lançam uma faísca de 3 de dano em outro inimigo em 5 blocos; recarga de 1 s.

Sobrecarga: a cada quarta habilidade, não custa mana e tem metade da recarga normal.

Espectro: +5%% de esquiva; após esquivar, o próximo golpe em até 3 s é um crítico garantido.

Detonação: ao abater, causa 4 de dano mágico a inimigos em 3 blocos; sem detonações em cadeia, dano a blocos ou aliados.

Contágio: ao abater, espalha veneno, lentidão, decomposição e fogo da vítima para 2 inimigos em 4 blocos.

Santuário: a cada 5 s, cura você e aliados em 8 blocos em 2 pontos de vida.

Fortuna: ao abater, 25%% de chance de uma tirada extra de saque; elites também têm 10%% de chance de deixar uma esmeralda.

Legião: +1 servo máximo; seus servos curam você em 10%% do dano que causam.""",
    """Chaque rôle possède trois voies : A, B et C. Apprendre le premier rang en choisit UNE et verrouille les deux autres. Réinitialisez l'arbre pour changer de voie.

Dans J, le bouton «Voies» ouvre la vue complète du rôle sélectionné : onglets A/B/C, niveaux, prérequis et confirmation du choix. S1–S3 apparaissent aussi dans la grille de l'arbre.

SEPT PALIERS (niveaux par défaut) :
S1 : 10 ; S2 : 14 ; S3 : 20 ; S4 : 26.
S5 : niveau 32, bifurcation S5x/S5y : vous ne pouvez apprendre qu'UNE branche.
S6 : niveau 40 ; nécessite l'une des branches S5 et reprend l'effet de S4 avec une puissance ×1.5.
S7 : pierre angulaire, niveau 50, un seul rang ; nécessite S6.
S1–S6 permettent trois rangs, chacun exigeant cinq niveaux de plus. Chaque rang coûte un point.

PIERRES ANGULAIRES : chaque voie possède un de ces dix modèles. Apprendre S7 l'active sur le rôle principal ou secondaire, avec un indicateur dans l'ATH. Valeurs par défaut ci-dessous ; le serveur peut les modifier :

Carnage : à l'élimination, rend 10%% de santé maximale et accorde Célérité II pendant 5 s.

Rempart : sous 30%% de santé, accorde Absorption II (8 points) et Résistance I pendant 6 s ; recharge de 60 s.

Tempête : les critiques et 15%% des coups directs lancent une étincelle à 3 dégâts sur un autre ennemi à 5 blocs ; recharge de 1 s.

Surcharge : chaque quatrième capacité ne coûte aucun mana et sa recharge est réduite de moitié.

Spectre : +5%% d'esquive ; après une esquive, le prochain coup dans les 3 s est un critique garanti.

Détonation : à l'élimination, inflige 4 dégâts magiques aux ennemis à 3 blocs ; sans explosions en chaîne, dégâts aux blocs ni aux alliés.

Contagion : à l'élimination, propage le poison, la lenteur, le Wither et le feu de la victime à 2 ennemis à 4 blocs.

Sanctuaire : toutes les 5 s, vous rend 2 points de santé, à vous et aux alliés à 8 blocs.

Fortune : à l'élimination, 25%% de chance d'un tirage de butin supplémentaire ; les élites ont aussi 10%% de chance de lâcher une émeraude.

Légion : +1 serviteur maximal ; vos serviteurs vous soignent de 10%% de leurs dégâts.""",
    """Jede Rolle besitzt drei Pfade: A, B und C. Mit dem ersten Rang wählst du EINEN und sperrst die beiden anderen. Setze den Baum zurück, um den Pfad zu wechseln.

In J öffnet «Pfade» die vollständige Pfadansicht der gewählten Rolle: A/B/C-Reiter, Stufen, Voraussetzungen und Wahlbestätigung. S1–S3 stehen auch im Baumraster.

SIEBEN STUFEN (Standardlevel):
S1: 10; S2: 14; S3: 20; S4: 26.
S5: Level 32, Verzweigung S5x/S5y: Du darfst nur EINEN Zweig erlernen.
S6: Level 40; benötigt einen der S5-Zweige und wiederholt S4 mit Stärke ×1.5.
S7: Schlüsselstein, Level 50, ein Rang; benötigt S6.
S1–S6 erlauben drei Ränge, die jeweils fünf weitere Level benötigen. Jeder Rang kostet einen Punkt.

SCHLÜSSELSTEINE: Jeder Pfad hat eine dieser zehn Vorlagen. S7 aktiviert sie für die Haupt- oder Nebenrolle mit HUD-Anzeige. Folgende Standardwerte kann der Server ändern:

Blutbad: Heilt bei Tötung 10%% des maximalen Lebens und gewährt Eile II für 5 s.

Bollwerk: Unter 30%% Leben gibt es Absorption II (8 Punkte) und Resistenz I für 6 s; 60 s Abklingzeit.

Sturm: Kritische Treffer und 15%% der direkten Treffer senden einen Funken mit 3 Schaden zu einem weiteren Feind in 5 Blöcken; 1 s Abklingzeit.

Überladung: Jede vierte Fähigkeit kostet kein Mana und hat halbierte normale Abklingzeit.

Gespenst: +5%% Ausweichen; nach dem Ausweichen ist dein nächster Treffer innerhalb von 3 s garantiert kritisch.

Detonation: Bei Tötung 4 magischer Schaden gegen Feinde in 3 Blöcken; keine Kettenexplosionen, kein Block- oder Verbündetenschaden.

Ansteckung: Bei Tötung werden Gift, Langsamkeit, Ausdörrung und Feuer des Opfers auf 2 Feinde in 4 Blöcken übertragen.

Zuflucht: Heilt dich und Verbündete in 8 Blöcken alle 5 s um 2 Leben.

Glück: Bei Tötung 25%% Chance auf einen weiteren Beutewurf; Elitegegner lassen zudem mit 10%% Chance einen Smaragd fallen.

Legion: +1 maximaler Diener; deine Diener heilen dich um 10%% ihres verursachten Schadens."""))

chapter('achievements', ('Conquistas', 'Succès', 'Fortschritte'), (
    """Abra os progressos do Minecraft e selecione a aba «Aptitudes». São 17 conquistas, concedidas automaticamente pelo servidor quando os requisitos são cumpridos. Passe o mouse sobre um progresso para ler o objetivo.

FOGUEIRA E PROGRESSÃO:
Descansar em uma Fogueira; atingir nível de alma 10; escolher uma classe; evoluir para Mestre e depois Definitivo; atingir nível 50 em uma habilidade por uso.

COMBATE E EQUIPAMENTO:
Executar uma finalização de combo; engastar uma runa; derrotar um chefe próprio e o Arauto do Eclipse. O golpe após uma sequência de cinco golpes realiza a finalização.

EVENTOS:
Concluir uma Névoa das Almas, uma Chuva de Brasas e um Eclipse; cada evento tem sua conquista. No Eclipse, chegar ao fim sem morrer.

GRUPOS E GUILDA:
Formar ou entrar em um grupo; resgatar o primeiro contrato; resgatar 10 contratos; atingir Mestre da Guilda (700 de reputação). Aceitar um contrato não conta como resgatá-lo.

Ao entrar, o servidor verifica conquistas baseadas no estado salvo: classe, evolução, nível de alma, habilidades e contratos. Mundos antigos podem concedê-las sem repetir esse progresso. Conquistas de ação exigem realizar a ação; conquistas já concedidas não se duplicam.""",
    """Ouvrez les progrès de Minecraft et sélectionnez l'onglet «Aptitudes». Il contient 17 succès, accordés automatiquement par le serveur lorsque leurs conditions sont remplies. Survolez un progrès pour lire son objectif.

FEU ET PROGRESSION :
Se reposer à un feu de camp ; atteindre le niveau d'âme 10 ; choisir un rôle ; évoluer aux stades Maître puis Ultime ; atteindre le niveau 50 dans une compétence d'usage.

COMBAT ET ÉQUIPEMENT :
Réussir un coup final de combo ; sertir une rune ; vaincre un boss du mod et le Héraut de l'Éclipse. Le coup suivant une chaîne de cinq réalise le coup final.

ÉVÉNEMENTS :
Terminer une Brume des âmes, une Pluie de braises et une Éclipse ; chaque événement a son succès. Pour l'Éclipse, atteindre la fin sans mourir.

GROUPES ET GUILDE :
Former ou rejoindre un groupe ; réclamer un premier contrat ; en réclamer 10 ; atteindre Maître de guilde (700 de réputation). Accepter un contrat ne compte pas comme le réclamer.

À la connexion, le serveur vérifie les succès fondés sur l'état sauvegardé : rôle, évolution, niveau d'âme, compétences et contrats. Les anciens mondes peuvent les accorder sans refaire cette progression. Les succès d'action demandent l'action ; ceux déjà obtenus ne sont pas dupliqués.""",
    """Öffne die Minecraft-Fortschritte und wähle den Reiter «Aptitudes». Er enthält 17 Fortschritte, die der Server bei erfüllten Voraussetzungen automatisch vergibt. Fahre über einen Fortschritt, um sein Ziel zu lesen.

LEUCHTFEUER UND FORTSCHRITT:
An einem Leuchtfeuer rasten; Seelenstufe 10 erreichen; eine Rolle wählen; zum Meister und dann zu Ultimativ entwickeln; Stufe 50 in einer Nutzungsfertigkeit erreichen.

KAMPF UND AUSRÜSTUNG:
Komboabschluss ausführen; Rune einsetzen; einen eigenen Mod-Boss und den Herold der Finsternis besiegen. Der Treffer nach einer Folge von fünf Treffern löst den Abschluss aus.

EREIGNISSE:
Seelennebel, Glutregen und Finsternis abschließen; jedes Ereignis hat einen eigenen Fortschritt. Bei der Finsternis das Ende ohne Tod erreichen.

GRUPPEN UND GILDE:
Eine Gruppe gründen oder beitreten; den ersten Auftrag abrechnen; 10 Aufträge abrechnen; Gildenmeister erreichen (700 Ruf). Annahme zählt nicht als Abrechnung.

Beim Beitritt prüft der Server Fortschritte anhand gespeicherter Daten: Rolle, Entwicklung, Seelenstufe, Fertigkeiten und Aufträge. Alte Welten können sie ohne erneutes Fortschreiten vergeben. Aktionsfortschritte erfordern die Aktion; bereits vergebene werden nicht verdoppelt."""))

chapter('eclipse', ('Eventos do mundo', 'Événements du monde', 'Weltereignisse'), (
    """Por padrão, a cada 10 dias a partir do dia 10, o Mundo Superior escolhe um evento aleatório: Eclipse, Névoa das Almas ou Chuva de Brasas. Nunca repete o anterior. O sino na véspera anuncia qual virá; /aptitudes event mostra o próximo.

ECLIPSE:
Penumbra, 6:00–9:00: céu vermelho e primeiras ondas.
Totalidade, 9:00–15:00: anel carmesim, cinzas, mais elites e campeões. Às 12:00, o Arauto chega perto de cada grupo de jogadores.
Recuo, 15:00–18:00: as ondas param.

As criaturas têm mais vida, dano e, por padrão, +4 de armadura. Podem vestir cota de malha na Penumbra, ferro na Totalidade e diamante se forem campeões. A Totalidade acrescenta mais 25%% de vida e dano, e esqueletos wither entram nas ondas. As criaturas se dissolvem ao fim do evento.

Elites, campeões e o Arauto deixam Fragmentos do Eclipse. O Arauto deixa o Núcleo do Eclipse, que melhora armas de evolução na Mesa da Guilda. A Runa do Eclipse concede, por padrão, +15%% de dano contra elites e chefes. Chegar ao fim sem morrer concede XP de classe e Combate.

NÉVOA DAS ALMAS (18:00–6:00): neblina cinza e só mortos-vivos: zumbis, esqueletos e variantes. Conforme o bioma, afogados e esqueletos pantanosos perto da água, zumbis-múmia em climas quentes ou errantes nos frios. Deixam Fragmentos de Alma; não aparecem phantoms.

CHUVA DE BRASAS (6:00–18:00): meteoros pequenos, grandes e enormes, visíveis antes do impacto. Os maiores têm explosões mais fortes e mais saque. Não quebram blocos, mas podem causar dano. Deixam minerais e, às vezes, diamantes ou fragmentos de netherita; grandes e enormes também deixam magma por 30 s. Podem trazer cubos de magma, blazes, piglins brutos, hoglins e esqueletos wither.

Os valores dependem da configuração do servidor. Desative o céu vermelho nos ajustes do HUD (Shift + O).""",
    """Par défaut, tous les 10 jours à partir du jour 10, la Surface choisit un événement aléatoire : Éclipse, Brume des âmes ou Pluie de braises. Jamais le même que le précédent. La cloche de la veille annonce lequel arrive ; /aptitudes event affiche le prochain.

ÉCLIPSE :
Pénombre, 6:00–9:00 : ciel rouge et premières vagues.
Totalité, 9:00–15:00 : anneau carmin, cendres, plus d'élites et de champions. À 12:00, le Héraut arrive près de chaque groupe de joueurs.
Retrait, 15:00–18:00 : les vagues s'arrêtent.

Les créatures ont plus de santé, de dégâts et, par défaut, +4 d'armure. Elles peuvent porter des mailles en Pénombre, du fer en Totalité et du diamant pour les champions. La Totalité ajoute encore 25%% de santé et de dégâts ; des wither squelettes rejoignent les vagues. Les créatures se dissolvent à la fin.

Élites, champions et Héraut lâchent des Fragments d'éclipse. Le Héraut donne le Noyau d'éclipse, qui améliore les armes d'évolution à la Table de guilde. La Rune d'éclipse donne par défaut +15%% de dégâts contre élites et boss. Atteindre la fin sans mourir accorde de l'XP de rôle et de Combat.

BRUME DES ÂMES (18:00–6:00) : brouillard gris et seulement des morts-vivants : zombies, squelettes et variantes. Selon le biome, noyés et embourbés près de l'eau, zombies momifiés en climat chaud ou vagabonds en climat froid. Ils lâchent des Fragments d'âme ; aucun phantom n'apparaît.

PLUIE DE BRAISES (6:00–18:00) : météores petits, grands et énormes, visibles avant l'impact. Les plus gros ont des explosions plus puissantes et plus de butin. Ils ne cassent pas les blocs mais peuvent blesser. Ils laissent des minerais, parfois des diamants ou fragments de netherite ; les grands et énormes laissent aussi du magma pendant 30 s. Ils peuvent amener cubes de magma, blazes, piglins barbares, hoglins et wither squelettes.

Les valeurs dépendent de la configuration du serveur. Désactivez le ciel rouge dans les réglages de l'ATH (Maj + O).""",
    """Standardmäßig wählt die Oberwelt ab Tag 10 alle 10 Tage ein zufälliges Ereignis: Finsternis, Seelennebel oder Glutregen. Das vorherige wird nie wiederholt. Die Glocke am Vorabend kündigt das Ereignis an; /aptitudes event zeigt das nächste.

FINSTERNIS:
Halbschatten, 6:00–9:00: Roter Himmel und erste Wellen.
Totalität, 9:00–15:00: Purpurring, Asche, mehr Elitegegner und Champions. Um 12:00 erscheint der Herold nahe jeder Spielergruppe.
Rückzug, 15:00–18:00: Die Wellen enden.

Kreaturen haben mehr Leben, Schaden und standardmäßig +4 Rüstung. Im Halbschatten können sie Kettenrüstung tragen, in der Totalität Eisen und als Champions Diamant. Die Totalität fügt weitere 25%% Leben und Schaden hinzu; Witherskelette verstärken die Wellen. Am Ende lösen sich die Kreaturen auf.

Elitegegner, Champions und Herold lassen Finsternissplitter fallen. Der Herold gibt den Finsterniskern, der Entwicklungswaffen am Gildentisch verbessert. Die Finsternisrune gibt standardmäßig +15%% Schaden gegen Elitegegner und Bosse. Wer das Ende ohne Tod erreicht, erhält Rollen- und Nahkampf-EP.

SEELENNEBEL (18:00–6:00): Grauer Nebel und nur Untote: Zombies, Skelette und Varianten. Je nach Biom Ertrunkene und Sumpfskelette am Wasser, Wüstenzombies in warmem oder Eiswanderer in kaltem Klima. Sie lassen Seelensplitter fallen; keine Phantome erscheinen.

GLUTREGEN (6:00–18:00): Kleine, große und riesige Meteore, vor dem Einschlag sichtbar. Größere Meteore haben stärkere Explosionen und mehr Beute. Sie zerstören keine Blöcke, können aber Schaden zufügen. Sie hinterlassen Erze, teils Diamanten oder Netheritplatten; große und riesige auch Magma für 30 s. Magmawürfel, Lohen, Piglin-Barbaren, Hoglins und Witherskelette können erscheinen.

Die Werte hängen von der Serverkonfiguration ab. Den roten Himmel kannst du in den HUD-Einstellungen ausschalten (Umschalt + O)."""))

chapter('contracts', ('Contratos da guilda', 'Contrats de guilde', 'Gildenaufträge'), (
    """Coloque e use um QUADRO DE CONTRATOS. A cada dia do Minecraft há 3 contratos fixos para você (fechar e reabrir não os muda); a primeira oferta sempre é da maior dificuldade disponível.

Você pode manter 2 contratos de uma vez (3 a partir de Veterano). Cada oferta só pode ser aceita uma vez por dia. Contratos em andamento não expiram.

TIPOS: Caça (abates do grupo também contam), Entrega (tudo de uma vez, do inventário), Pesca, Encantamento, Ofício (XP de habilidade), Forja (na Mesa da Guilda), Evento (sobreviver a Eclipse, Névoa das Almas ou Chuva de Brasas) e Chefe (só Lendário; o grupo também conta).

DIFICULDADE: aumenta com a reputação (Aprendiz → Oficial 100 → Veterano 300 → Mestre 700). Com a classe principal em Mestre, aparecem contratos Elite; em Definitivo, Lendários.

RECOMPENSAS: esmeraldas, XP de classe principal, reputação e, nos mais difíceis, Fragmentos de Alma, Vales de Arsenal ou Emblemas de Maestria.""",
    """Placez un TABLEAU DE CONTRATS et utilisez-le. Chaque jour de Minecraft propose 3 contrats fixes pour vous (fermer puis rouvrir ne les change pas) ; la première offre est toujours de votre difficulté la plus élevée.

Vous pouvez garder 2 contrats simultanés (3 dès Vétéran). Chaque offre ne s'accepte qu'une fois par jour. Les contrats en cours n'expirent pas.

TYPES : Chasse (les éliminations du groupe comptent aussi), Livraison (tout à la fois, depuis l'inventaire), Pêche, Enchantement, Métier (XP de compétence), Forge (à la Table de guilde), Événement (survivre à Éclipse, Brume des âmes ou Pluie de braises) et Boss (Légendaire uniquement ; le groupe compte aussi).

DIFFICULTÉ : augmente avec la réputation (Apprenti → Compagnon 100 → Vétéran 300 → Maître 700). Avec le rôle principal au stade Maître, les contrats Élite apparaissent ; au stade Ultime, les Légendaires.

RÉCOMPENSES : émeraudes, XP du rôle principal, réputation et, pour les plus difficiles, Fragments d'âme, Bons d'armurerie ou Emblèmes de maîtrise.""",
    """Stelle eine AUFTRAGSTAFEL auf und benutze sie. Jeder Minecraft-Tag bietet dir 3 feste Aufträge (Schließen und Öffnen ändern sie nicht); das erste Angebot hat immer deinen höchsten Schwierigkeitsgrad.

Du kannst gleichzeitig 2 Aufträge haben (3 ab Veteran). Jedes Angebot darf nur einmal pro Tag angenommen werden. Laufende Aufträge verfallen nicht.

ARTEN: Jagd (Gruppentötungen zählen mit), Lieferung (alles auf einmal aus dem Inventar), Angeln, Verzaubern, Beruf (Fertigkeits-EP), Schmieden (am Gildentisch), Ereignis (Finsternis, Seelennebel oder Glutregen überleben) und Boss (nur Legendär; Gruppe zählt mit).

SCHWIERIGKEIT: Steigt mit dem Ruf (Lehrling → Geselle 100 → Veteran 300 → Meister 700). Mit der Hauptrolle als Meister erscheinen Eliteaufträge; als Ultimativ legendäre Aufträge.

BELOHNUNGEN: Smaragde, Hauptrollen-EP, Ruf und bei schweren Aufträgen Seelensplitter, Rüstkammergutscheine oder Meisterschaftsembleme."""))

chapter('alchemy', ('Alquimia', 'Alchimie', 'Alchemie'), (
    """Destile em um suporte de poções abastecido. As receitas abaixo vêm do jogo; passe o mouse sobre os frascos e clique nas pétalas para ver a extração.

Mana restaura energia; Frenesi acelera ataques; Véu protege contra criaturas de eventos; Vigor regenera e alimenta.

Um Herborista principal ou secundário em 8 blocos destila 50% mais rápido e prolonga os efeitos em 50%. Abrir o suporte permite afastar-se até 32 blocos, conectado no mesmo mundo. As doses não somam potência.

Só um Herborista recebe XP por lote: quem abriu o suporte tem prioridade, depois o mais próximo. Os três primeiros dão 4 XP base; lotes repetidos em cinco minutos reduzem a XP até zero. Retirar frascos ou reabrir nunca paga novamente. Criativo e espectador não se qualificam. Esses valores são configuráveis.""",
    """Brassez dans un alambic alimenté. Les recettes ci-dessous viennent du jeu ; survolez les fioles et cliquez sur les pétales pour voir leur extraction.

Mana restaure l'énergie ; Frénésie accélère les attaques ; Voile protège des créatures d'événement ; Vigueur régénère et nourrit.

Un Herboriste principal ou secondaire à 8 blocs brasse 50% plus vite et prolonge les effets de 50%. Ouvrir l'alambic lui permet de s'éloigner jusqu'à 32 blocs en restant connecté dans ce monde. Les doses n'ajoutent pas de puissance.

Un seul Herboriste gagne de l'XP par lot : priorité à celui qui a ouvert l'alambic, puis au plus proche. Les trois premiers donnent 4 XP de base ; répéter des lots en cinq minutes réduit l'XP jusqu'à zéro. Prendre les fioles ou rouvrir ne récompense jamais une seconde fois. Créatif et spectateur sont exclus. Ces valeurs sont configurables.""",
    """Braue an einem mit Brennstoff versorgten Braustand. Die Rezepte unten stammen aus dem Spiel; fahre über Flaschen und klicke die Blütenblätter an, um deren Gewinnung zu sehen.

Mana stellt Energie wieder her; Raserei beschleunigt Angriffe; Schleier schützt vor Ereigniskreaturen; Waldkraft regeneriert und sättigt.

Ein Kräuterkundiger als Haupt- oder Nebenrolle in 8 Blöcken braut 50% schneller und verlängert Effekte um 50%. Nach dem Öffnen darf er bis zu 32 Blöcke entfernt sein, solange er in dieser Welt verbunden bleibt. Dosen erhöhen nicht die Stärke.

Nur ein Kräuterkundiger erhält EP pro Braugang: Vorrang hat der Öffner, dann der Nächste. Die ersten drei geben 4 Basis-EP; Wiederholungen innerhalb von fünf Minuten senken sie auf null. Flaschen entnehmen oder erneut öffnen vergibt nie erneut EP. Kreativ- und Zuschauermodus sind ausgeschlossen. Diese Werte sind einstellbar."""))

# v47: las cifras antiguas de daño se conservan; cambia la presión de las oleadas.
for locale, extra in zip(LOCALES, (
    '\n\nASSALTO: por padrão, 4 criaturas por jogador na Penumbra e 8 na Totalidade, a cada 30 s. Se houver espaço e orçamento, cada onda começa com uma elite; na Totalidade, também um campeão. Probabilidade zero desativa a garantia correspondente. As unidades de elite usam zumbis ou esqueletos com armadura completa; campeões vestem diamante. O equipamento não é deixado ao morrer. Limites: 12 criaturas por jogador próximo, 24 por zona e 128 carregadas no mundo, incluindo Arautos; estes podem substituir uma unidade comum no limite. Valores configuráveis.',
    '\n\nASSAUT : par défaut, 4 créatures par joueur en Pénombre et 8 en Totalité, toutes les 30 s. Si la place et le budget le permettent, chaque vague commence par un élite ; en Totalité, aussi un champion. Une probabilité nulle désactive la garantie correspondante. Les unités élites sont des zombies ou squelettes en armure complète ; les champions portent du diamant. Leur équipement ne tombe pas à la mort. Limites : 12 créatures par joueur proche, 24 par zone et 128 chargées dans le monde, Hérauts compris ; ceux-ci peuvent remplacer une unité ordinaire à la limite. Valeurs configurables.',
    '\n\nANGRIFF: Standardmäßig 4 Kreaturen pro Spieler im Halbschatten und 8 in der Totalität, alle 30 s. Bei genug Platz und Budget beginnt jede Welle mit einem Elitegegner; in der Totalität auch mit einem Champion. Chance null deaktiviert die jeweilige Garantie. Eliteeinheiten sind Zombies oder Skelette mit vollständiger Rüstung; Champions tragen Diamant. Ihre Ausrüstung fällt beim Tod nicht ab. Grenzen: 12 Kreaturen je nahem Spieler, 24 pro Gebiet und 128 geladene weltweit, einschließlich Herolden; diese können am Limit eine gewöhnliche Einheit ersetzen. Werte sind einstellbar.',
), strict=True):
    TRANSLATIONS[locale]['guide.aptitudes.eclipse.body'] += extra
