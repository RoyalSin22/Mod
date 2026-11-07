"""Traducción inicial de nodos base. Columnas: inglés, pt_br, fr_fr, de_de.

Los textos repetidos comparten traducción. Una frase nueva exige una fila propia;
no se traducen fragmentos por sustitución ni se acepta inglés como respaldo.
"""
from check_lang import LOCALES

TEXT = {}


def add(rows):
    for row in rows.strip().splitlines():
        source, *values = row.split('¦')
        if len(values) != 3 or source in TEXT:
            raise ValueError(f'Texto base duplicado o inválido: {source}')
        TEXT[source] = dict(zip(LOCALES, values, strict=True))


add("""
Tough Skin¦Pele resistente¦Peau résistante¦Zähe Haut
Rage¦Fúria¦Rage¦Raserei
Bloodlust¦Sede de sangue¦Soif de sang¦Blutdurst
War Cry¦Grito de guerra¦Cri de guerre¦Kriegsschrei
Arcane Affinity¦Afinidade arcana¦Affinité arcanique¦Arkane Affinität
Arcane Bolt¦Projétil arcano¦Projectile arcanique¦Arkanes Geschoss
Clear Mind¦Mente clara¦Esprit clair¦Klarer Geist
Blink¦Lampejo¦Transfert¦Blinzeln
Shadow¦Sombra¦Ombre¦Schatten
Vanish¦Desaparecer¦Disparition¦Verschwinden
Backstab¦Apunhalada pelas costas¦Attaque sournoise¦Hinterhältiger Stich
Shadow Step¦Passo sombrio¦Pas de l'ombre¦Schattenschritt
Vital Sap¦Seiva vital¦Sève vitale¦Lebenssaft
Balm¦Bálsamo¦Baume¦Balsam
Herbal Immunity¦Imunidade herbal¦Immunité végétale¦Kräuterimmunität
Poison Thorns¦Espinhos venenosos¦Épines empoisonnées¦Giftdornen
Engineering¦Engenharia¦Ingénierie¦Ingenieurskunst
Turret¦Torreta¦Tourelle¦Geschützturm
Battery¦Bateria¦Batterie¦Batterie
Thrusters¦Propulsores¦Propulseurs¦Schubdüsen
Reach¦Alcance¦Allonge¦Reichweite
Lunge¦Investida¦Fente¦Ausfall
Charge¦Carga¦Charge¦Ansturm
Sweep¦Varredura¦Balayage¦Rundumschlag
Keen Edge¦Fio afiado¦Tranchant affûté¦Scharfe Schneide
Blade Dance¦Dança das lâminas¦Danse des lames¦Klingentanz
Parry¦Aparar¦Parade¦Parade
Riposte¦Contra-ataque¦Riposte¦Riposte
Holy Smite¦Golpe sagrado¦Châtiment sacré¦Heiliger Schlag
Divine Shield¦Escudo divino¦Bouclier divin¦Göttlicher Schild
Protective Aura¦Aura protetora¦Aura protectrice¦Schutzaura
Judgement¦Julgamento¦Jugement¦Urteil
Dark Communion¦Comunhão sombria¦Communion obscure¦Dunkle Kommunion
Raise Minions¦Erguer servos¦Relever les serviteurs¦Diener erwecken
Life Drain¦Drenar vida¦Drain de vie¦Lebensentzug
Death Touch¦Toque da morte¦Toucher mortel¦Todesberührung
Forest Child¦Adepto da floresta¦Adepte de la forêt¦Waldkundiger
Entangling Roots¦Raízes enredantes¦Racines entravantes¦Fesselnde Wurzeln
Growth¦Crescimento¦Croissance¦Wachstum
Bear Form¦Forma de urso¦Forme d'ours¦Bärengestalt
Haggling¦Barganha¦Marchandage¦Feilschen
Appraiser's Eye¦Olhar do avaliador¦Œil de l'expert¦Gutachterblick
Deep Pockets¦Bolsos fundos¦Poches profondes¦Tiefe Taschen
Bribe¦Suborno¦Pot-de-vin¦Bestechung
Iron Fists¦Punhos de ferro¦Poings de fer¦Eisenfäuste
Chi Palm¦Palma de chi¦Paume de chi¦Chi-Handfläche
Evasion¦Evasão¦Évasion¦Ausweichen
Meditation¦Meditação¦Méditation¦Meditation
Elemental Affinity¦Afinidade elemental¦Affinité élémentaire¦Elementaraffinität
Elemental Bolt¦Projétil elemental¦Projectile élémentaire¦Elementargeschoss
Elemental Dominion¦Domínio elemental¦Domination élémentaire¦Elementarherrschaft
Elemental Burst¦Explosão elemental¦Explosion élémentaire¦Elementarausbruch
Mimicry¦Mimetismo¦Mimétisme¦Mimikry
Transform¦Transformar¦Transformation¦Verwandlung
Trickster¦Trapaceiro¦Filou¦Trickser
Form Power¦Poder da forma¦Pouvoir de forme¦Gestaltkraft
Hot Blood¦Sangue quente¦Sang chaud¦Heißes Blut
Iron Arm¦Braço de ferro¦Bras de fer¦Eisenarm
Haste¦Aceleração¦Accélération¦Beschleunigung
Efficiency¦Eficiência¦Efficacité¦Effizienz
Mastery¦Maestria¦Maîtrise¦Meisterschaft
Arcane Reserve¦Reserva arcana¦Réserve arcanique¦Arkane Reserve
Ethereal Step¦Passo etéreo¦Pas éthéré¦Ätherischer Schritt
Light Feet¦Pés leves¦Pieds légers¦Leichte Füße
Poisoned Edge¦Fio envenenado¦Tranchant empoisonné¦Giftige Schneide
Deep Roots¦Raízes profundas¦Racines profondes¦Tiefe Wurzeln
Clover¦Trevo¦Trèfle¦Klee
Reinforced Plates¦Placas reforçadas¦Plaques renforcées¦Verstärkte Platten
Drill¦Broca¦Foreuse¦Bohrer
Long Shaft¦Haste longa¦Longue hampe¦Langer Schaft
Firm Stance¦Postura firme¦Posture ferme¦Fester Stand
Quick Wrist¦Pulso rápido¦Poignet vif¦Flinkes Handgelenk
Tempered Edge¦Fio temperado¦Tranchant trempé¦Gehärtete Schneide
Unbreakable Faith¦Fé inabalável¦Foi inébranlable¦Unerschütterlicher Glaube
Blessing¦Bênção¦Bénédiction¦Segen
Soul Well¦Poço de almas¦Puits d'âmes¦Seelenbrunnen
Rotten Flesh¦Carne podre¦Chair putréfiée¦Verrottetes Fleisch
Bark¦Casca¦Écorce¦Rinde
Deer Stride¦Passo do cervo¦Pas du cerf¦Hirschschritt
Lucky Coin¦Moeda da sorte¦Pièce porte-bonheur¦Glücksmünze
Ledger¦Livro de contas¦Livre de comptes¦Kassenbuch
Swift Fist¦Punho veloz¦Poing vif¦Flinke Faust
Wind Step¦Passo do vento¦Pas du vent¦Windschritt
Elemental Core¦Núcleo elemental¦Noyau élémentaire¦Elementarkern
Stone Skin¦Pele de pedra¦Peau de pierre¦Steinhaut
Wild Essence¦Essência selvagem¦Essence sauvage¦Wilde Essenz
Nimble Paws¦Patas ágeis¦Pattes agiles¦Flinke Pfoten
Frenzy¦Frenesi¦Frénésie¦Raserei
Unbreakable¦Inabalável¦Inébranlable¦Unbeugsam
Carnage¦Carnificina¦Carnage¦Blutbad
Leap Slam¦Salto esmagador¦Saut fracassant¦Sprungschlag
Whirlwind¦Redemoinho¦Tourbillon¦Wirbelwind
Arcane Charges¦Cargas arcanas¦Charges arcaniques¦Arkane Ladungen
Mana Shield¦Escudo de mana¦Bouclier de mana¦Manaschild
Spell Echo¦Eco mágico¦Écho magique¦Zauberecho
Arcane Missiles¦Mísseis arcanos¦Projectiles arcaniques¦Arkane Raketen
Lethal Tempo¦Ritmo letal¦Tempo mortel¦Tödlicher Rhythmus
Smoke Escape¦Fuga de fumaça¦Fuite enfumée¦Rauchflucht
Fan of Knives¦Leque de facas¦Éventail de couteaux¦Messerfächer
Shuriken¦Shuriken¦Shuriken¦Shuriken
Bloom¦Florescimento¦Floraison¦Blüte
Antidote¦Antídoto¦Antidote¦Gegengift
Thorny Skin¦Pele espinhosa¦Peau épineuse¦Dornenhaut
Healing Rain¦Chuva curativa¦Pluie curative¦Heilregen
Spore Cloud¦Nuvem de esporos¦Nuage de spores¦Sporenwolke
Overdrive¦Sobrecarga¦Surrégime¦Übersteuerung
Auto Shield¦Escudo automático¦Bouclier automatique¦Automatischer Schild
Field Repair¦Reparo em campo¦Réparation de terrain¦Feldreparatur
Grapple¦Gancho¦Grappin¦Greifhaken
Pulse Grenade¦Granada de pulso¦Grenade à impulsion¦Impulsgranate
Impetus¦Ímpeto¦Élan¦Schwung
Brace¦Firmar posição¦Se préparer¦Abstützen
Dragoon Fall¦Queda do dragão¦Chute du dragon¦Drachensturz
Dragon Leap¦Salto do dragão¦Saut du dragon¦Drachensprung
Piercing Thrust¦Estocada perfurante¦Estoc perforant¦Durchbohrender Stoß
Kata¦Kata¦Kata¦Kata
Reflexes¦Reflexos¦Réflexes¦Reflexe
Flurry¦Rajada de golpes¦Rafale de coups¦Schlaghagel
Iaido¦Iaido¦Iaido¦Iaido
Thousand Cuts¦Mil cortes¦Mille entailles¦Tausend Schnitte
Holy Light¦Luz sagrada¦Lumière sacrée¦Heiliges Licht
Intercession¦Intercessão¦Intercession¦Fürsprache
Purifying Light¦Luz purificadora¦Lumière purificatrice¦Reinigendes Licht
Consecration¦Consagração¦Consécration¦Weihe
Hammer of Justice¦Martelo da justiça¦Marteau de justice¦Hammer der Gerechtigkeit
Soul Harvest¦Colheita de almas¦Moisson d'âmes¦Seelenernte
Bone Shield¦Escudo de ossos¦Bouclier d'os¦Knochenschild
Corpse Burst¦Explosão de cadáver¦Explosion de cadavre¦Leichenexplosion
Bone Spear¦Lança de ossos¦Lance d'os¦Knochenspeer
Soul Siphon¦Sifão de almas¦Siphon d'âmes¦Seelensiphon
Natural Cycle¦Ciclo natural¦Cycle naturel¦Naturkreislauf
Barkskin¦Pele de casca¦Peau d'écorce¦Rindenhaut
Bramble¦Sarça¦Ronces¦Brombeerranke
Pack¦Matilha¦Meute¦Rudel
Thorn Storm¦Tempestade de espinhos¦Tempête d'épines¦Dornensturm
Compound Interest¦Juros compostos¦Intérêts composés¦Zinseszins
Insurance¦Seguro¦Assurance¦Versicherung
Bounty¦Recompensa¦Prime¦Kopfgeld
Coin Toss¦Lançamento de moeda¦Jet de pièce¦Münzwurf
Golden Guard¦Guarda dourada¦Garde dorée¦Goldene Wache
Stances¦Posturas¦Postures¦Haltungen
Deflect¦Desviar¦Déviation¦Ablenken
Inner Peace¦Paz interior¦Paix intérieure¦Innere Ruhe
Flying Kick¦Voadora¦Coup de pied volant¦Sprungtritt
Hundred Fists¦Cem punhos¦Cent poings¦Hundert Fäuste
Elemental Reaction¦Reação elemental¦Réaction élémentaire¦Elementarreaktion
Elemental Ward¦Proteção elemental¦Protection élémentaire¦Elementarschutz
Resonant Kill¦Abate ressonante¦Élimination résonante¦Resonante Tötung
Elemental Barrage¦Bombardeio elemental¦Barrage élémentaire¦Elementarsalve
Elemental Aura¦Aura elemental¦Aura élémentaire¦Elementaraura
Instinct¦Instinto¦Instinct¦Instinkt
Slippery¦Escorregadio¦Insaisissable¦Schlüpfrig
Devour¦Devorar¦Dévorer¦Verschlingen
Pounce¦Bote¦Bond¦Anspringen
Beast Howl¦Uivo bestial¦Hurlement bestial¦Bestienheulen
Time Warp¦Distorção temporal¦Distorsion temporelle¦Zeitverzerrung
Forged Arm¦Braço forjado¦Bras forgé¦Geschmiedeter Arm
Blade Plating¦Blindagem de lâminas¦Blindage de lames¦Klingenpanzerung
Stride¦Passada¦Foulée¦Schritt
Keen Eye¦Olhar aguçado¦Œil perçant¦Scharfer Blick
Minstrel's Stamina¦Vigor do menestrel¦Endurance du ménestrel¦Ausdauer des Spielmanns
Bard's Lungs¦Pulmões do bardo¦Poumons du barde¦Bardenlunge
Dark Well¦Poço sombrio¦Puits obscur¦Dunkler Brunnen
Hardened Flesh¦Carne endurecida¦Chair endurcie¦Gehärtetes Fleisch
Weathered Hide¦Pele curtida¦Peau aguerrie¦Wettergegerbte Haut
Thick Leather¦Couro grosso¦Cuir épais¦Dickes Leder
Quickdraw¦Saque rápido¦Dégainé rapide¦Schnellziehen
Arcane Shot¦Disparo arcano¦Tir arcanique¦Arkaner Schuss
Eagle Eye¦Olho de águia¦Œil d'aigle¦Adlerauge
Fan the Hammer¦Disparo em leque¦Tir en éventail¦Schnellfeuer
Arcane Cylinder¦Tambor arcano¦Barillet arcanique¦Arkane Trommel
Dodge Roll¦Rolamento evasivo¦Roulade d'esquive¦Ausweichrolle
Reload¦Recarregar¦Rechargement¦Nachladen
Ricochet¦Ricochete¦Ricochet¦Abpraller
Deadeye¦Mira mortal¦Œil mortel¦Todesblick
Armory¦Arsenal¦Armurerie¦Rüstkammer
Blade Volley¦Salva de lâminas¦Salve de lames¦Klingensalve
Blade Guard¦Guarda de lâminas¦Garde de lames¦Klingenwache
Sword Rain¦Chuva de espadas¦Pluie d'épées¦Schwertregen
Floating Arsenal¦Arsenal flutuante¦Arsenal flottant¦Schwebendes Arsenal
Intercepting Blade¦Lâmina interceptora¦Lame d'interception¦Abfangklinge
Reforge¦Reforjar¦Reforge¦Neuschmieden
Spectral Greatsword¦Espadão espectral¦Espadon spectral¦Geistergroßschwert
Blade Prison¦Prisão de lâminas¦Prison de lames¦Klingengefängnis
Predator¦Predador¦Prédateur¦Raubtier
Hunter's Mark¦Marca do caçador¦Marque du chasseur¦Jägermal
Tracker¦Rastreador¦Traqueur¦Fährtenleser
Bear Trap¦Armadilha de urso¦Piège à ours¦Bärenfalle
The Hunt¦A caçada¦La chasse¦Die Jagd
Camouflage¦Camuflagem¦Camouflage¦Tarnung
Trophy¦Troféu¦Trophée¦Trophäe
Arrow Volley¦Salva de flechas¦Salve de flèches¦Pfeilsalve
Falcon Strike¦Ataque do falcão¦Frappe du faucon¦Falkenschlag
Inspiring Presence¦Presença inspiradora¦Présence inspirante¦Inspirierende Gegenwart
Song of Valor¦Canção da bravura¦Chant de vaillance¦Lied der Tapferkeit
Soothing Melody¦Melodia calmante¦Mélodie apaisante¦Beruhigende Melodie
Dissonance¦Dissonância¦Dissonance¦Dissonanz
Crescendo¦Crescendo¦Crescendo¦Crescendo
Encore¦Bis¦Rappel¦Zugabe
Lullaby¦Canção de ninar¦Berceuse¦Wiegenlied
Ballad of Shielding¦Balada protetora¦Ballade protectrice¦Schutzballade
Requiem¦Réquiem¦Requiem¦Requiem
Dark Bargain¦Poder sombrio¦Pouvoir ténébreux¦Dunkle Kraft
Curse of Agony¦Maldição da agonia¦Malédiction d'agonie¦Fluch der Qual
Soul Link¦Elo de almas¦Lien d'âmes¦Seelenverbindung
Abyssal Bolt¦Projétil abissal¦Projectile abyssal¦Abgrundgeschoss
Corruption¦Corrupção¦Corruption¦Verderbnis
Demonic Ward¦Proteção demoníaca¦Protection démoniaque¦Dämonischer Schutz
Soul Feast¦Banquete de almas¦Festin d'âmes¦Seelenmahl
Summon Imps¦Invocar diabretes¦Invoquer des diablotins¦Wichtel beschwören
Doom¦Condenação¦Condamnation¦Verdammnis
Beast Bond¦Vínculo bestial¦Lien bestial¦Bestienbund
Sic 'Em¦Peguem!¦À l'attaque !¦Fass!
Pack Leader¦Líder da matilha¦Chef de meute¦Rudelführer
Vital Bond¦Vínculo vital¦Lien vital¦Lebensband
Companion¦Companheiro¦Compagnon¦Gefährte
Protective Instinct¦Instinto protetor¦Instinct protecteur¦Schutzinstinkt
Feral Feast¦Banquete selvagem¦Festin sauvage¦Wildes Festmahl
Stampede¦Estampida¦Ruée¦Stampede
Alpha Roar¦Rugido alfa¦Rugissement alpha¦Alphabrüllen
★ Wheel of Adaptation¦★ Roda da adaptação¦★ Roue d'adaptation¦★ Rad der Anpassung
★ Crown of Greed¦★ Coroa da ganância¦★ Couronne d'avidité¦★ Krone der Gier
★ Void Archive¦★ Arquivo do vazio¦★ Archives du vide¦★ Archiv der Leere
★ Fungal Plague¦★ Praga fúngica¦★ Peste fongique¦★ Pilzseuche
★ Parasite¦★ Parasita¦★ Parasite¦★ Parasit
★ Hellfire¦★ Fogo infernal¦★ Feu infernal¦★ Höllenfeuer
★ Chosen King¦★ Rei escolhido¦★ Roi élu¦★ Auserwählter König
★ Lord of Spacetime¦★ Senhor do espaço-tempo¦★ Seigneur de l'espace-temps¦★ Herr der Raumzeit
★ Domain of Negation¦★ Domínio da negação¦★ Domaine de négation¦★ Sphäre der Verneinung
★ Geneticist¦★ Mestre da mutação¦★ Maître des mutations¦★ Meister der Wandlung
★ Perfect Mirror¦★ Espelho perfeito¦★ Miroir parfait¦★ Vollkommener Spiegel
★ Exploiter¦★ Explorador de riquezas¦★ Prospecteur¦★ Schatzsucher
★ Pendragon¦★ Pendragon¦★ Pendragon¦★ Pendragon
★ Lord of the Skies¦★ Senhor dos céus¦★ Seigneur des cieux¦★ Herr der Lüfte
★ Eternal Stillness¦★ Quietude eterna¦★ Quiétude éternelle¦★ Ewige Stille
★ Harbinger of Death¦★ Arauto da morte¦★ Héraut de la mort¦★ Todesbote
★ Skypiercer¦★ Perfurador dos céus¦★ Perce-ciel¦★ Himmelsdurchbohrer
★ World Tree¦★ Árvore do mundo¦★ Arbre-monde¦★ Weltenbaum
★ The Last Bullet¦★ A última bala¦★ La dernière balle¦★ Die letzte Kugel
★ Gate of Arms¦★ Portal das armas¦★ Porte des armes¦★ Tor der Waffen
""")

add("""
+10% knockback resistance and +1 armor toughness per rank.¦+10% de resistência a repulsão e +1 de tenacidade por grau.¦+10% de résistance au recul et +1 de robustesse par rang.¦+10% Rückstoßresistenz und +1 Rüstungshärte pro Rang.
Strength and Speed for 8/10/12 s (Strength II at rank 3).¦Força e Velocidade por 8/10/12 s (Força II no grau 3).¦Force et Vitesse pendant 8/10/12 s (Force II au rang 3).¦Stärke und Schnelligkeit für 8/10/12 s (Stärke II auf Rang 3).
Up to +15/30/45% melee damage based on missing health.¦Até +15/30/45% de dano corpo a corpo conforme a vida perdida.¦Jusqu'à +15/30/45% de dégâts de mêlée selon la santé manquante.¦Bis zu +15/30/45% Nahkampfschaden abhängig vom fehlenden Leben.
Knocks back, weakens and slows enemies within 5/6/7 blocks.¦Empurra, enfraquece e desacelera inimigos em 5/6/7 blocos.¦Repousse, affaiblit et ralentit les ennemis à 5/6/7 blocs.¦Stößt Feinde in 5/6/7 Blöcken zurück, schwächt und verlangsamt sie.
+25 max mana and +0.5 regen per second, per rank.¦+25 de mana máximo e +0.5 de regeneração por segundo, por grau.¦+25 de mana maximal et +0.5 de régénération par seconde, par rang.¦+25 maximales Mana und +0.5 Regeneration pro Sekunde und Rang.
A bolt of arcane energy up to 24 blocks: 6/8/10 damage, splashes nearby enemies and marks the target with a rune (Glowing).¦Projétil arcano de até 24 blocos: 6/8/10 de dano, atinge inimigos próximos e marca o alvo com uma runa (Brilho).¦Projectile arcanique jusqu'à 24 blocs : 6/8/10 dégâts, touche les ennemis proches et marque la cible d'une rune (Surbrillance).¦Arkanes Geschoss bis zu 24 Blöcke: 6/8/10 Schaden, trifft nahe Feinde und markiert das Ziel mit einer Rune (Leuchten).
-10/20/30% cooldown on all your abilities.¦-10/20/30% de recarga de todas as suas habilidades.¦-10/20/30% de recharge pour toutes vos capacités.¦-10/20/30% Abklingzeit für alle deine Fähigkeiten.
Teleports you up to 8/10/12 blocks where you look.¦Teleporta até 8/10/12 blocos na direção do olhar.¦Vous téléporte jusqu'à 8/10/12 blocs dans la direction regardée.¦Teleportiert dich bis zu 8/10/12 Blöcke in Blickrichtung.
Faster sneaking. From rank 2, invisible while sneaking.¦Agacha mais rápido. A partir do grau 2, fica invisível ao agachar.¦Déplacement accroupi plus rapide. Dès le rang 2, invisible en étant accroupi.¦Schnelleres Schleichen. Ab Rang 2 bist du beim Schleichen unsichtbar.
Invisibility and Speed for 6/8/10 s; mobs chasing you lose track.¦Invisibilidade e Velocidade por 6/8/10 s; criaturas que perseguem você perdem seu rastro.¦Invisibilité et Vitesse pendant 6/8/10 s ; les créatures qui vous poursuivent perdent votre trace.¦Unsichtbarkeit und Schnelligkeit für 6/8/10 s; Verfolger verlieren deine Spur.
+25/50/75% damage when hitting from behind.¦+25/50/75% de dano ao atacar por trás.¦+25/50/75% de dégâts en frappant par-derrière.¦+25/50/75% Schaden bei Treffern von hinten.
Teleports you behind the target you look at (16/20/24 blocks).¦Teleporta para trás do alvo observado (16/20/24 blocos).¦Vous téléporte derrière la cible regardée (16/20/24 blocs).¦Teleportiert dich hinter das anvisierte Ziel (16/20/24 Blöcke).
Regain 1 health every 9/6/3 s.¦Recupera 1 de vida a cada 9/6/3 s.¦Récupère 1 de santé toutes les 9/6/3 s.¦Stellt alle 9/6/3 s 1 Leben wieder her.
Heals 4/6/8 health and grants Regeneration to you and players within 6 blocks.¦Cura 4/6/8 de vida e concede Regeneração a você e aos jogadores em 6 blocos.¦Rend 4/6/8 de santé et accorde Régénération à vous et aux joueurs à 6 blocs.¦Heilt dich und Spieler in 6 Blöcken um 4/6/8 Leben und gewährt Regeneration.
Immune to poison (r1), hunger (r2) and wither (r3).¦Imune a veneno (r1), fome (r2) e decomposição (r3).¦Immunité au poison (r1), à la faim (r2) et au Wither (r3).¦Immun gegen Gift (r1), Hunger (r2) und Ausdörrung (r3).
Poisons and slows enemies within 5/6/7 blocks.¦Envenena e desacelera inimigos em 5/6/7 blocos.¦Empoisonne et ralentit les ennemis à 5/6/7 blocs.¦Vergiftet und verlangsamt Feinde in 5/6/7 Blöcken.
Per rank: +1 block interaction range, +10% break speed and +1 armor.¦Por grau: +1 bloco de alcance de interação, +10% de velocidade de quebra e +1 de armadura.¦Par rang : +1 bloc de portée d'interaction, +10% de vitesse de minage et +1 d'armure.¦Pro Rang: +1 Block Interaktionsreichweite, +10% Abbaugeschwindigkeit und +1 Rüstung.
Deploys an allied iron golem for 20/25/30 s. Drops no iron.¦Cria um golem de ferro aliado por 20/25/30 s. Não deixa ferro.¦Déploie un golem de fer allié pour 20/25/30 s. Ne lâche pas de fer.¦Stellt einen verbündeten Eisengolem für 20/25/30 s auf. Lässt kein Eisen fallen.
Per rank: +25 max mana, +0.5 regen/s, higher jumps and +2 blocks of safe fall.¦Por grau: +25 de mana máximo, +0.5 de regeneração/s, saltos mais altos e +2 blocos de queda segura.¦Par rang : +25 de mana maximal, +0.5 de régénération/s, sauts plus hauts et +2 blocs de chute sûre.¦Pro Rang: +25 maximales Mana, +0.5 Regeneration/s, höhere Sprünge und +2 Blöcke sichere Fallhöhe.
Boost forward and upward, with a short slow fall.¦Impulso para a frente e para cima, com breve Queda Lenta.¦Propulsion vers l'avant et le haut, avec une brève Chute lente.¦Schub nach vorn und oben mit kurzem Sanften Fall.
+0.5 blocks of attack reach per rank.¦+0.5 bloco de alcance de ataque por grau.¦+0.5 bloc de portée d'attaque par rang.¦+0.5 Blöcke Angriffsreichweite pro Rang.
Dash forward dealing 6/8/10 damage to enemies in your path.¦Avança causando 6/8/10 de dano aos inimigos no caminho.¦Fonce en infligeant 6/8/10 dégâts aux ennemis sur le passage.¦Stürmt vor und verursacht 6/8/10 Schaden an Feinden auf dem Weg.
+15/30/45% damage when attacking while sprinting.¦+15/30/45% de dano ao atacar correndo.¦+15/30/45% de dégâts en attaquant pendant un sprint.¦+15/30/45% Schaden beim Angriff während des Sprintens.
Circular strike: 5/7/9 damage to enemies around you.¦Golpe circular: 5/7/9 de dano aos inimigos ao redor.¦Frappe circulaire : 5/7/9 dégâts aux ennemis autour de vous.¦Rundumschlag: 5/7/9 Schaden an Feinden um dich herum.
+7/14/21% damage with swords.¦+7/14/21% de dano com espadas.¦+7/14/21% de dégâts avec les épées.¦+7/14/21% Schwertschaden.
Haste I/II/III and Speed for 8/10/12 s.¦Pressa I/II/III e Velocidade por 8/10/12 s.¦Célérité I/II/III et Vitesse pendant 8/10/12 s.¦Eile I/II/III und Schnelligkeit für 8/10/12 s.
5/10/15% chance to negate a melee hit.¦5/10/15% de chance de anular um golpe corpo a corpo.¦5/10/15% de chance d'annuler un coup de mêlée.¦5/10/15% Chance, einen Nahkampftreffer abzuwehren.
For 2/3/4 s, the next hit you take is negated and returned at 150/200/250% damage.¦Por 2/3/4 s, o próximo golpe recebido é anulado e devolvido com 150/200/250% de dano.¦Pendant 2/3/4 s, le prochain coup reçu est annulé et renvoyé avec 150/200/250% de dégâts.¦Für 2/3/4 s wird der nächste Treffer abgewehrt und mit 150/200/250% Schaden zurückgegeben.
+20/40/60% damage against undead.¦+20/40/60% de dano contra mortos-vivos.¦+20/40/60% de dégâts contre les morts-vivants.¦+20/40/60% Schaden gegen Untote.
Absorption I/II/III for 8 s and Resistance for 4/5/6 s.¦Absorção I/II/III por 8 s e Resistência por 4/5/6 s.¦Absorption I/II/III pendant 8 s et Résistance pendant 4/5/6 s.¦Absorption I/II/III für 8 s und Resistenz für 4/5/6 s.
Resistance for you and players within 4/6/8 blocks. Rank 3 also grants Regeneration.¦Resistência para você e jogadores em 4/6/8 blocos. No grau 3, também Regeneração.¦Résistance pour vous et les joueurs à 4/6/8 blocs. Le rang 3 accorde aussi Régénération.¦Resistenz für dich und Spieler in 4/6/8 Blöcken. Rang 3 gewährt auch Regeneration.
Damages nearby enemies (6/8/10, double vs undead) and heals allies.¦Causa dano aos inimigos próximos (6/8/10, dobro contra mortos-vivos) e cura aliados.¦Blesse les ennemis proches (6/8/10, double contre les morts-vivants) et soigne les alliés.¦Verletzt nahe Feinde (6/8/10, doppelt gegen Untote) und heilt Verbündete.
Each creature dying within 16 blocks grants 2/4/6 mana and 1 health.¦Cada criatura que morre em 16 blocos concede 2/4/6 de mana e 1 de vida.¦Chaque créature mourant à 16 blocs accorde 2/4/6 de mana et 1 de santé.¦Jede Kreatur, die in 16 Blöcken stirbt, gewährt 2/4/6 Mana und 1 Leben.
Summons 1 zombie / zombie + skeleton / 2 zombies + skeleton allies for 45 s (max 4). They gain health, damage and armor with your level.¦Invoca 1 zumbi / zumbi + esqueleto / 2 zumbis + esqueleto aliados por 45 s (máx. 4). Vida, dano e armadura aumentam com seu nível.¦Invoque 1 zombie / zombie + squelette / 2 zombies + squelette alliés pour 45 s (max 4). Santé, dégâts et armure augmentent avec votre niveau.¦Beschwört 1 Zombie / Zombie + Skelett / 2 Zombies + Skelett für 45 s (max. 4). Leben, Schaden und Rüstung steigen mit deiner Stufe.
Heal 5/10/15% of the damage you or your minions deal.¦Cura 5/10/15% do dano causado por você ou seus servos.¦Vous soigne de 5/10/15% des dégâts infligés par vous ou vos serviteurs.¦Heilt dich um 5/10/15% des von dir oder deinen Dienern verursachten Schadens.
Drains enemies within 5 blocks (4/5/6 damage + Wither) and heals you for half the total.¦Drena inimigos em 5 blocos (4/5/6 de dano + Decomposição) e cura você em metade do total.¦Draine les ennemis à 5 blocs (4/5/6 dégâts + Wither) et vous soigne de la moitié du total.¦Entzieht Feinden in 5 Blöcken Leben (4/5/6 Schaden + Ausdörrung) und heilt dich um die Hälfte der Summe.
In forests, jungles and taigas: Speed; Regeneration from rank 2 and Strength at rank 3.¦Em florestas, selvas e taigas: Velocidade; Regeneração a partir do grau 2 e Força no grau 3.¦En forêt, jungle et taïga : Vitesse ; Régénération dès le rang 2 et Force au rang 3.¦In Wäldern, Dschungeln und Taigas: Schnelligkeit; Regeneration ab Rang 2 und Stärke auf Rang 3.
Roots and weakens enemies within 6/7/8 blocks for 3/4/5 s.¦Imobiliza e enfraquece inimigos em 6/7/8 blocos por 3/4/5 s.¦Immobilise et affaiblit les ennemis à 6/7/8 blocs pendant 3/4/5 s.¦Fesselt und schwächt Feinde in 6/7/8 Blöcken für 3/4/5 s.
Every 2 s nearby crops and saplings grow on their own (more radius and chance per rank).¦A cada 2 s, plantações e mudas próximas crescem sozinhas (mais raio e chance por grau).¦Toutes les 2 s, cultures et pousses proches grandissent seules (rayon et chance accrus par rang).¦Alle 2 s wachsen nahe Pflanzen und Setzlinge von selbst (mehr Radius und Chance pro Rang).
For 10/12/14 s: Strength, Resistance and Health Boost, but slower. Heals on transforming.¦Por 10/12/14 s: Força, Resistência e Vida Extra, mas fica mais lento. Cura ao transformar.¦Pendant 10/12/14 s : Force, Résistance et Bonus de santé, mais plus lent. Soigne à la transformation.¦Für 10/12/14 s: Stärke, Resistenz und Lebensenergie, aber langsamer. Heilt bei der Verwandlung.
Permanent Hero of the Village I/II/III: villager discounts.¦Herói da Vila I/II/III permanente: descontos com aldeões.¦Héros du village I/II/III permanent : réductions des villageois.¦Dauerhaft Held des Dorfes I/II/III: Rabatte bei Dorfbewohnern.
Luck I/II/III for 60 s and reveals nearby creatures for 10 s.¦Sorte I/II/III por 60 s e revela criaturas próximas por 10 s.¦Chance I/II/III pendant 60 s et révèle les créatures proches pendant 10 s.¦Glück I/II/III für 60 s und enthüllt nahe Kreaturen für 10 s.
10/20/30% chance of an extra emerald on each trade.¦10/20/30% de chance de uma esmeralda extra por troca.¦10/20/30% de chance d'une émeraude supplémentaire par échange.¦10/20/30% Chance auf einen zusätzlichen Smaragd pro Handel.
Costs 1 emerald. Enemies within 8/10/12 blocks stop attacking players for 7/9/11 s.¦Custa 1 esmeralda. Inimigos em 8/10/12 blocos param de atacar jogadores por 7/9/11 s.¦Coûte 1 émeraude. Les ennemis à 8/10/12 blocs cessent d'attaquer les joueurs pendant 7/9/11 s.¦Kostet 1 Smaragd. Feinde in 8/10/12 Blöcken greifen Spieler für 7/9/11 s nicht mehr an.
+1.5/3/4.5 damage when hitting bare-handed.¦+1.5/3/4.5 de dano ao golpear de mãos vazias.¦+1.5/3/4.5 dégâts à mains nues.¦+1.5/3/4.5 Schaden bei Treffern mit bloßer Hand.
Strike up to 5 blocks: 6/8/10 damage and a big knockback.¦Golpe de até 5 blocos: 6/8/10 de dano e forte repulsão.¦Frappe jusqu'à 5 blocs : 6/8/10 dégâts et fort recul.¦Schlag bis zu 5 Blöcke: 6/8/10 Schaden und starker Rückstoß.
7/14/21% chance to avoid any attack from a creature.¦7/14/21% de chance de evitar qualquer ataque de criatura.¦7/14/21% de chance d'éviter toute attaque d'une créature.¦7/14/21% Chance, einem beliebigen Kreaturenangriff auszuweichen.
4 s still: Regeneration III and Resistance II. Restores 30/40/50 mana. Costs no mana.¦4 s parado: Regeneração III e Resistência II. Restaura 30/40/50 de mana. Não custa mana.¦4 s immobile : Régénération III et Résistance II. Rend 30/40/50 de mana. Ne coûte aucun mana.¦4 s stillstehen: Regeneration III und Resistenz II. Stellt 30/40/50 Mana wieder her. Kostet kein Mana.
Enables the passive of your active element; its strength depends on rank.¦Ativa a passiva do elemento atual; a potência depende do grau.¦Active le passif de votre élément actif ; sa puissance dépend du rang.¦Aktiviert den passiven Effekt des aktuellen Elements; die Stärke hängt vom Rang ab.
Strike up to 24 blocks (6/7/8 damage) with the active element's effect.¦Golpe de até 24 blocos (6/7/8 de dano) com o efeito do elemento ativo.¦Frappe jusqu'à 24 blocs (6/7/8 dégâts) avec l'effet de l'élément actif.¦Treffer bis zu 24 Blöcke (6/7/8 Schaden) mit dem Effekt des aktiven Elements.
Your other mastered elements' passives are also active, at this rank.¦As passivas dos outros elementos dominados também ficam ativas, neste grau.¦Les passifs de vos autres éléments maîtrisés s'activent aussi, à ce rang.¦Die passiven Effekte deiner anderen gemeisterten Elemente sind auf diesem Rang ebenfalls aktiv.
Blast within 5/6/7 blocks with the active element's effect.¦Explosão em 5/6/7 blocos com o efeito do elemento ativo.¦Explosion à 5/6/7 blocs avec l'effet de l'élément actif.¦Explosion in 5/6/7 Blöcken mit dem Effekt des aktiven Elements.
Reduces mana drain while transformed (3 → 2.5 → 2 → 1.5 per second).¦Reduz o consumo de mana transformado (3 → 2.5 → 2 → 1.5 por segundo).¦Réduit le mana consommé en transformation (3 → 2.5 → 2 → 1.5 par seconde).¦Verringert den Manaverbrauch in verwandelter Gestalt (3 → 2.5 → 2 → 1.5 pro Sekunde).
Turns you into the chosen form (or back to human). Others see you as the mob; your hitbox doesn't change.¦Transforma na forma escolhida (ou volta à forma humana). Os outros veem você como a criatura.¦Prend la forme choisie (ou redevient humain). Les autres vous voient comme la créature.¦Verwandelt dich in die gewählte Gestalt (oder zurück zum Menschen). Andere sehen dich als die Kreatur.
R1: monsters ignore you while you look like one of them. R2: you emit the mob's sounds (other players hear them too).¦R1: monstros ignoram você enquanto parecer um deles. R2: emite os sons da criatura (outros jogadores também ouvem).¦R1 : les monstres vous ignorent si vous leur ressemblez. R2 : vous émettez les sons de la créature (audibles aussi par les autres joueurs).¦R1: Monster ignorieren dich, wenn du wie sie aussiehst. R2: Du gibst die Laute der Kreatur von dir (auch andere Spieler hören sie).
Uses the current form's power: explosion (Creeper), teleport (Enderman), venom bite (Spider), fireballs (Blaze), flight (Bat), super jump (Slime), egg barrage (Chicken), ground slam (Golem), curse (Witch), escape (Villager).¦Usa o poder da forma: explosão (Creeper), teleporte (Enderman), mordida venenosa (Aranha), bolas de fogo (Blaze), voo (Morcego), supersalto (Slime), rajada de ovos (Galinha), impacto no chão (Golem), maldição (Bruxa), fuga (Aldeão).¦Utilise le pouvoir de la forme : explosion (Creeper), téléportation (Enderman), morsure venimeuse (Araignée), boules de feu (Blaze), vol (Chauve-souris), super saut (Slime), barrage d'œufs (Poule), frappe au sol (Golem), malédiction (Sorcière), fuite (Villageois).¦Nutzt die Gestaltkraft: Explosion (Creeper), Teleportation (Enderman), Giftbiss (Spinne), Feuerbälle (Lohe), Flug (Fledermaus), Supersprung (Schleim), Eiersalve (Huhn), Bodenschlag (Golem), Fluch (Hexe), Flucht (Dorfbewohner).
+2 max health per rank.¦+2 de vida máxima por grau.¦+2 de santé maximale par rang.¦+2 maximales Leben pro Rang.
+0.5 attack damage per rank.¦+0.5 de dano de ataque por grau.¦+0.5 de dégâts d'attaque par rang.¦+0.5 Angriffsschaden pro Rang.
−10% cooldown per rank on this role's actives.¦−10% de recarga por grau nas ativas desta classe.¦−10% de recharge par rang sur les capacités actives de ce rôle.¦−10% Abklingzeit pro Rang für die aktiven Fähigkeiten dieser Rolle.
−10% mana cost per rank on this role's actives.¦−10% de custo de mana por grau nas ativas desta classe.¦−10% de coût en mana par rang sur les capacités actives de ce rôle.¦−10% Manakosten pro Rang für die aktiven Fähigkeiten dieser Rolle.
+1 effective rank per rank on this role's actives (more power).¦+1 grau efetivo por grau nas ativas desta classe (mais potência).¦+1 rang effectif par rang sur les capacités actives de ce rôle (plus de puissance).¦+1 effektiver Rang pro Rang für die aktiven Fähigkeiten dieser Rolle (mehr Stärke).
+15 max mana per rank.¦+15 de mana máximo por grau.¦+15 de mana maximal par rang.¦+15 maximales Mana pro Rang.
+3% speed per rank.¦+3% de velocidade por grau.¦+3% de vitesse par rang.¦+3% Geschwindigkeit pro Rang.
+4% speed per rank.¦+4% de velocidade por grau.¦+4% de vitesse par rang.¦+4% Geschwindigkeit pro Rang.
+0.4 attack damage per rank.¦+0.4 de dano de ataque por grau.¦+0.4 de dégâts d'attaque par rang.¦+0.4 Angriffsschaden pro Rang.
+0.5 luck per rank.¦+0.5 de sorte por grau.¦+0.5 de chance par rang.¦+0.5 Glück pro Rang.
+1 armor per rank.¦+1 de armadura por grau.¦+1 d'armure par rang.¦+1 Rüstung pro Rang.
+10% mining speed per rank.¦+10% de velocidade de mineração por grau.¦+10% de vitesse de minage par rang.¦+10% Abbaugeschwindigkeit pro Rang.
+0.3 blocks of reach per rank.¦+0.3 bloco de alcance por grau.¦+0.3 bloc d'allonge par rang.¦+0.3 Blöcke Reichweite pro Rang.
+10% knockback resistance per rank.¦+10% de resistência a repulsão por grau.¦+10% de résistance au recul par rang.¦+10% Rückstoßresistenz pro Rang.
+5% attack speed per rank.¦+5% de velocidade de ataque por grau.¦+5% de vitesse d'attaque par rang.¦+5% Angriffsgeschwindigkeit pro Rang.
+1 armor toughness per rank.¦+1 de tenacidade por grau.¦+1 de robustesse par rang.¦+1 Rüstungshärte pro Rang.
+1 max health per rank.¦+1 de vida máxima por grau.¦+1 de santé maximale par rang.¦+1 maximales Leben pro Rang.
+1 luck per rank.¦+1 de sorte por grau.¦+1 de chance par rang.¦+1 Glück pro Rang.
+10 max mana per rank.¦+10 de mana máximo por grau.¦+10 de mana maximal par rang.¦+10 maximales Mana pro Rang.
""")

add("""
MECHANIC: each melee hit within 3 s stacks +4%% damage (max 3/4/5 stacks).¦MECÂNICA: cada golpe corpo a corpo em 3 s acumula +4%% de dano (máx. 3/4/5 cargas).¦MÉCANIQUE : chaque coup de mêlée en 3 s cumule +4%% de dégâts (max 3/4/5 cumuls).¦MECHANIK: Jeder Nahkampftreffer innerhalb von 3 s stapelt +4%% Schaden (max. 3/4/5 Stapel).
Dropping below 30%% health grants Resistance for 4/5/6 s (II at rank 3). Every 20 s.¦Abaixo de 30%% de vida, concede Resistência por 4/5/6 s (II no grau 3). A cada 20 s.¦Sous 30%% de santé, accorde Résistance pendant 4/5/6 s (II au rang 3). Toutes les 20 s.¦Unter 30%% Leben erhältst du Resistenz für 4/5/6 s (II auf Rang 3). Alle 20 s.
Each kill heals 2/3/4 health.¦Cada abate cura 2/3/4 de vida.¦Chaque élimination rend 2/3/4 de santé.¦Jede Tötung heilt 2/3/4 Leben.
Leap forward and slam on landing, hitting everything around you (6/8/10 damage) and launching them.¦Salta para a frente e golpeia ao pousar, atingindo ao redor (6/8/10 de dano) e lançando os inimigos ao ar.¦Bondit en avant et frappe à l'atterrissage, touchant autour de vous (6/8/10 dégâts) et projetant les ennemis.¦Springt vor und schlägt bei der Landung ringsum zu (6/8/10 Schaden), wirft Gegner in die Luft.
Spin 3 times in 1.5 s hitting everything nearby (3/4/5 per spin).¦Gira 3 vezes em 1.5 s, atingindo ao redor (3/4/5 por giro).¦Tourne 3 fois en 1.5 s et frappe autour de vous (3/4/5 par tour).¦Dreht sich 3-mal in 1.5 s und trifft alles in der Nähe (3/4/5 pro Drehung).
MECHANIC: each Mage spell grants a charge; at 5/4/3 charges, the next spell refunds its mana cost.¦MECÂNICA: cada magia de Mago concede uma carga; com 5/4/3 cargas, a próxima devolve seu custo de mana.¦MÉCANIQUE : chaque sort de Mage donne une charge ; à 5/4/3 charges, le suivant rembourse son coût en mana.¦MECHANIK: Jeder Magierzauber gibt eine Ladung; bei 5/4/3 Ladungen erstattet der nächste seine Manakosten.
10/20/30%% of each hit is absorbed by your mana (2 mana per health point).¦10/20/30%% de cada golpe é absorvido pelo mana (2 de mana por ponto de vida).¦10/20/30%% de chaque coup est absorbé par votre mana (2 mana par point de santé).¦10/20/30%% jedes Treffers wird von deinem Mana absorbiert (2 Mana pro Lebenspunkt).
Killing with spells or ranged attacks restores 8/16/24 mana.¦Abates com magias ou ataques à distância restauram 8/16/24 de mana.¦Les éliminations par sort ou attaque à distance rendent 8/16/24 de mana.¦Tötungen durch Zauber oder Fernangriffe stellen 8/16/24 Mana wieder her.
Fire 4/5/6 homing missiles at the nearest enemies (3 damage each).¦Dispara 4/5/6 projéteis teleguiados contra os inimigos mais próximos (3 de dano cada).¦Tire 4/5/6 projectiles guidés sur les ennemis les plus proches (3 dégâts chacun).¦Feuert 4/5/6 zielsuchende Geschosse auf die nächsten Feinde (je 3 Schaden).
MECHANIC: the 3rd consecutive hit on the same target is a finisher: +50/75/100%% damage and Wither.¦MECÂNICA: o 3º golpe seguido no mesmo alvo é uma finalização: +50/75/100%% de dano e Decomposição.¦MÉCANIQUE : le 3e coup consécutif sur la même cible est un coup final : +50/75/100%% de dégâts et Wither.¦MECHANIK: Der 3. Treffer in Folge auf dasselbe Ziel ist ein Abschluss: +50/75/100%% Schaden und Ausdörrung.
Dropping below 30%% health grants Invisibility and Speed II for 3 s (every 40/35/30 s).¦Abaixo de 30%% de vida, concede Invisibilidade e Velocidade II por 3 s (a cada 40/35/30 s).¦Sous 30%% de santé, accorde Invisibilité et Vitesse II pendant 3 s (toutes les 40/35/30 s).¦Unter 30%% Leben erhältst du Unsichtbarkeit und Schnelligkeit II für 3 s (alle 40/35/30 s).
Each kill: Speed for 3 s and Invisibility for 1/2/3 s.¦Cada abate: Velocidade por 3 s e Invisibilidade por 1/2/3 s.¦Chaque élimination : Vitesse pendant 3 s et Invisibilité pendant 1/2/3 s.¦Jede Tötung: Schnelligkeit für 3 s und Unsichtbarkeit für 1/2/3 s.
Throw blades at all enemies within 5 blocks: 4/5/6 damage and Poison.¦Lança lâminas contra todos os inimigos em 5 blocos: 4/5/6 de dano e Veneno.¦Lance des lames sur tous les ennemis à 5 blocs : 4/5/6 dégâts et Poison.¦Wirft Klingen auf alle Feinde in 5 Blöcken: 4/5/6 Schaden und Gift.
Ranged strike: 7/9/11 damage, Glowing and Slowness II.¦Ataque à distância: 7/9/11 de dano, Brilho e Lentidão II.¦Frappe à distance : 7/9/11 dégâts, Surbrillance et Lenteur II.¦Fernangriff: 7/9/11 Schaden, Leuchten und Langsamkeit II.
MECHANIC: after standing still 3 s, you and allies within 3/4/5 blocks gain Regeneration.¦MECÂNICA: após ficar parado por 3 s, você e aliados em 3/4/5 blocos ganham Regeneração.¦MÉCANIQUE : après 3 s immobile, vous et les alliés à 3/4/5 blocs gagnez Régénération.¦MECHANIK: Nach 3 s Stillstand erhalten du und Verbündete in 3/4/5 Blöcken Regeneration.
Every second, 15/30/45%% chance to remove a negative effect.¦A cada segundo, 15/30/45%% de chance de remover um efeito negativo.¦Chaque seconde, 15/30/45%% de chance de retirer un effet négatif.¦Jede Sekunde 15/30/45%% Chance, einen negativen Effekt zu entfernen.
Melee attackers are poisoned for 2/3/4 s.¦Atacantes corpo a corpo são envenenados por 2/3/4 s.¦Les attaquants en mêlée sont empoisonnés pendant 2/3/4 s.¦Nahkampfangreifer werden für 2/3/4 s vergiftet.
5 s of rain healing 1.5/2/2.5 per second to players within 6/7/8 blocks.¦Chuva por 5 s que cura 1.5/2/2.5 por segundo em jogadores em 6/7/8 blocos.¦Pluie de 5 s rendant 1.5/2/2.5 par seconde aux joueurs à 6/7/8 blocs.¦5 s Regen, der Spieler in 6/7/8 Blöcken um 1.5/2/2.5 pro Sekunde heilt.
A 6 s cloud where you look: Poison and Weakness to enemies.¦Nuvem de 6 s no local observado: Veneno e Fraqueza nos inimigos.¦Nuage de 6 s au point regardé : Poison et Faiblesse aux ennemis.¦6 s Wolke am anvisierten Ort: Gift und Schwäche für Feinde.
MECHANIC: each Engineer ability adds 25 heat; at 100 you enter Overdrive for 6/8/10 s (+20%% damage, Haste II), then slow down.¦MECÂNICA: cada habilidade de Engenheiro soma 25 de calor; em 100, ativa Sobrecarga por 6/8/10 s (+20%% de dano, Pressa II), depois fica lento.¦MÉCANIQUE : chaque capacité d'Ingénieur ajoute 25 de chaleur ; à 100, Surrégime pendant 6/8/10 s (+20%% de dégâts, Célérité II), puis ralentissement.¦MECHANIK: Jede Ingenieursfähigkeit gibt 25 Hitze; bei 100 folgt Übersteuerung für 6/8/10 s (+20%% Schaden, Eile II), danach Verlangsamung.
Hits of 5 or more grant Absorption for 5 s (I/II/III). Every 20 s.¦Golpes de 5 ou mais concedem Absorção por 5 s (I/II/III). A cada 20 s.¦Les coups de 5 ou plus accordent Absorption pendant 5 s (I/II/III). Toutes les 20 s.¦Treffer ab 5 gewähren Absorption für 5 s (I/II/III). Alle 20 s.
Every 20 s, repair 2/4/6 durability on all your gear.¦A cada 20 s, repara 2/4/6 de durabilidade em todo o equipamento.¦Toutes les 20 s, répare 2/4/6 de durabilité sur tout votre équipement.¦Repariert alle 20 s 2/4/6 Haltbarkeit an deiner gesamten Ausrüstung.
Pull yourself to the block you look at (up to 20/24/28 blocks) without fall damage.¦Puxa você até o bloco observado (até 20/24/28 blocos), sem dano de queda.¦Vous tire vers le bloc regardé (jusqu'à 20/24/28 blocs), sans dégâts de chute.¦Zieht dich zum anvisierten Block (bis zu 20/24/28 Blöcke), ohne Fallschaden.
Explodes after 1 s where you look: 8/10/12 damage and knockback, no block damage.¦Explode após 1 s no local observado: 8/10/12 de dano e repulsão, sem destruir blocos.¦Explose après 1 s au point regardé : 8/10/12 dégâts et recul, sans dégâts aux blocs.¦Explodiert nach 1 s am anvisierten Ort: 8/10/12 Schaden und Rückstoß, ohne Blockschaden.
MECHANIC: sprinting for 2 s charges your next hit: +30/45/60%% damage and knockback.¦MECÂNICA: correr por 2 s carrega o próximo golpe: +30/45/60%% de dano e repulsão.¦MÉCANIQUE : sprinter 2 s charge votre prochain coup : +30/45/60%% de dégâts et recul.¦MECHANIK: 2 s Sprinten lädt deinen nächsten Treffer auf: +30/45/60%% Schaden und Rückstoß.
While sneaking you take 10/20/30%% less damage.¦Ao agachar, recebe 10/20/30%% menos dano.¦Accroupi, vous subissez 10/20/30%% de dégâts en moins.¦Beim Schleichen erleidest du 10/20/30%% weniger Schaden.
−30/60/90%% fall damage.¦−30/60/90%% de dano de queda.¦−30/60/90%% de dégâts de chute.¦−30/60/90%% Fallschaden.
Leap high and dive down: 7/9/11 damage and Slowness III around you.¦Salta alto e mergulha: 7/9/11 de dano e Lentidão III ao redor.¦Bondit haut et plonge : 7/9/11 dégâts et Lenteur III autour de vous.¦Springt hoch und stürzt hinab: 7/9/11 Schaden und Langsamkeit III ringsum.
Pierce all enemies in an 8-block line: 8/10/12 damage.¦Perfura todos os inimigos em uma linha de 8 blocos: 8/10/12 de dano.¦Transperce tous les ennemis sur une ligne de 8 blocs : 8/10/12 dégâts.¦Durchbohrt alle Feinde auf einer 8 Blöcke langen Linie: 8/10/12 Schaden.
MECHANIC: with a sword, the 3rd chained hit (1.5 s) sweeps nearby enemies for 40/60/80%% of the damage.¦MECÂNICA: com espada, o 3º golpe encadeado (1.5 s) varre inimigos próximos com 40/60/80%% do dano.¦MÉCANIQUE : à l'épée, le 3e coup enchaîné (1.5 s) balaie les ennemis proches pour 40/60/80%% des dégâts.¦MECHANIK: Mit einem Schwert trifft der 3. verkettete Schlag (1.5 s) nahe Feinde mit 40/60/80%% des Schadens.
When hit, 5/10/15%% chance to gain Speed II and make your next hit deal +50%%.¦Ao receber um golpe, 5/10/15%% de chance de ganhar Velocidade II e causar +50%% no próximo golpe.¦En étant touché, 5/10/15%% de chance de gagner Vitesse II et +50%% sur le prochain coup.¦Bei einem Treffer 5/10/15%% Chance auf Schnelligkeit II und +50%% beim nächsten Schlag.
Each kill: Haste for 4 s (Haste II at rank 3).¦Cada abate: Pressa por 4 s (Pressa II no grau 3).¦Chaque élimination : Célérité pendant 4 s (Célérité II au rang 3).¦Jede Tötung: Eile für 4 s (Eile II auf Rang 3).
Dash 6 blocks like lightning, cutting everything you pass: 7/9/11 damage.¦Avança 6 blocos como um raio, cortando tudo no caminho: 7/9/11 de dano.¦Fonce de 6 blocs comme l'éclair en tranchant sur le passage : 7/9/11 dégâts.¦Stürmt blitzschnell 6 Blöcke vor und schneidet alles auf dem Weg: 7/9/11 Schaden.
5 rapid cuts on the target (3/4/5 each).¦5 cortes rápidos no alvo (3/4/5 cada).¦5 entailles rapides sur la cible (3/4/5 chacune).¦5 schnelle Schnitte gegen das Ziel (je 3/4/5).
MECHANIC: your hits charge light (double vs undead); at 5 charges it bursts: 4/5/6 area damage and heals you 3/4/5.¦MECÂNICA: golpes carregam luz (dobro contra mortos-vivos); com 5 cargas, explode: 4/5/6 de dano em área e cura você em 3/4/5.¦MÉCANIQUE : vos coups chargent la lumière (double contre les morts-vivants) ; à 5 charges, explosion : 4/5/6 dégâts de zone et vous soigne de 3/4/5.¦MECHANIK: Treffer laden Licht auf (doppelt gegen Untote); bei 5 Ladungen Explosion: 4/5/6 Flächenschaden und heilt dich um 3/4/5.
If an ally within 8 blocks drops below 30%% health, heal them 4/6/8 (every 30 s).¦Se um aliado em 8 blocos cair abaixo de 30%% de vida, cura 4/6/8 (a cada 30 s).¦Si un allié à 8 blocs tombe sous 30%% de santé, lui rend 4/6/8 (toutes les 30 s).¦Fällt ein Verbündeter in 8 Blöcken unter 30%% Leben, heilt ihn um 4/6/8 (alle 30 s).
When hit, 10/20/30%% chance to remove Poison and Wither and heal 1.¦Ao receber um golpe, 10/20/30%% de chance de remover Veneno e Decomposição e curar 1.¦En étant touché, 10/20/30%% de chance de retirer Poison et Wither et de rendre 1 de santé.¦Bei einem Treffer 10/20/30%% Chance, Gift und Ausdörrung zu entfernen und 1 Leben zu heilen.
Holy ground for 6 s: damages enemies (double vs undead) and heals players.¦Solo sagrado por 6 s: causa dano aos inimigos (dobro contra mortos-vivos) e cura jogadores.¦Sol sacré pendant 6 s : blesse les ennemis (double contre les morts-vivants) et soigne les joueurs.¦Heiliger Boden für 6 s: Verletzt Feinde (doppelt gegen Untote) und heilt Spieler.
Stuns the target for 3 s and deals 8/10/12 damage.¦Atordoa o alvo por 3 s e causa 8/10/12 de dano.¦Étourdit la cible pendant 3 s et inflige 8/10/12 dégâts.¦Betäubt das Ziel für 3 s und verursacht 8/10/12 Schaden.
MECHANIC: each kill grants a soul (max 3/4/5): +3%% damage per soul. They fade one by one every 15 s.¦MECÂNICA: cada abate concede uma alma (máx. 3/4/5): +3%% de dano por alma. Desaparecem uma a uma a cada 15 s.¦MÉCANIQUE : chaque élimination donne une âme (max 3/4/5) : +3%% de dégâts par âme. Elles disparaissent une à une toutes les 15 s.¦MECHANIK: Jede Tötung gewährt eine Seele (max. 3/4/5): +3%% Schaden pro Seele. Sie verschwinden einzeln alle 15 s.
""")

add("""
Dropping below 50%% health grants Absorption I/II/III for 5 s (every 30 s).¦Abaixo de 50%% de vida, concede Absorção I/II/III por 5 s (a cada 30 s).¦Sous 50%% de santé, accorde Absorption I/II/III pendant 5 s (toutes les 30 s).¦Unter 50%% Leben erhältst du Absorption I/II/III für 5 s (alle 30 s).
When a minion dies it explodes: 3/4/5 damage to nearby enemies.¦Ao morrer, um servo explode: 3/4/5 de dano a inimigos próximos.¦À sa mort, un serviteur explose : 3/4/5 dégâts aux ennemis proches.¦Ein Diener explodiert beim Tod: 3/4/5 Schaden an nahen Feinden.
A spear piercing a 16-block line: 7/9/11 damage and Wither.¦Lança que perfura uma linha de 16 blocos: 7/9/11 de dano e Decomposição.¦Lance transperçant une ligne de 16 blocs : 7/9/11 dégâts et Wither.¦Speer durch eine 16 Blöcke lange Linie: 7/9/11 Schaden und Ausdörrung.
Drains enemies within 6/7/8 blocks (3/4/5 each) and heals you for half.¦Drena inimigos em 6/7/8 blocos (3/4/5 cada) e cura você em metade.¦Draine les ennemis à 6/7/8 blocs (3/4/5 chacun) et vous soigne de la moitié.¦Entzieht Feinden in 6/7/8 Blöcken Leben (je 3/4/5) und heilt dich um die Hälfte.
MECHANIC: by day you heal 0.5/1/1.5 every 3 s; by night you deal +10/15/20%% damage.¦MECÂNICA: de dia, cura 0.5/1/1.5 a cada 3 s; à noite, causa +10/15/20%% de dano.¦MÉCANIQUE : le jour, rend 0.5/1/1.5 toutes les 3 s ; la nuit, inflige +10/15/20%% de dégâts.¦MECHANIK: Tagsüber heilst du alle 3 s 0.5/1/1.5; nachts verursachst du +10/15/20%% Schaden.
Melee hits: 10/20/30%% chance of Resistance II for 3 s (every 15 s).¦Golpes corpo a corpo: 10/20/30%% de chance de Resistência II por 3 s (a cada 15 s).¦Coups de mêlée : 10/20/30%% de chance de Résistance II pendant 3 s (toutes les 15 s).¦Nahkampftreffer: 10/20/30%% Chance auf Resistenz II für 3 s (alle 15 s).
Melee attackers take 1/2/3 damage.¦Atacantes corpo a corpo recebem 1/2/3 de dano.¦Les attaquants en mêlée subissent 1/2/3 dégâts.¦Nahkampfangreifer erleiden 1/2/3 Schaden.
Summon 2 allied wolves (3 at rank 3) for 40 s.¦Invoca 2 lobos aliados (3 no grau 3) por 40 s.¦Invoque 2 loups alliés (3 au rang 3) pour 40 s.¦Beschwört 2 verbündete Wölfe (3 auf Rang 3) für 40 s.
Thorns within 6 blocks: 4/5/6 damage and Slowness II.¦Espinhos em 6 blocos: 4/5/6 de dano e Lentidão II.¦Épines à 6 blocs : 4/5/6 dégâts et Lenteur II.¦Dornen in 6 Blöcken: 4/5/6 Schaden und Langsamkeit II.
MECHANIC: every 10 emeralds in your inventory grant +1 luck and +2%% damage (max 3/4/5).¦MECÂNICA: cada 10 esmeraldas no inventário concedem +1 de sorte e +2%% de dano (máx. 3/4/5).¦MÉCANIQUE : chaque lot de 10 émeraudes dans l'inventaire donne +1 de chance et +2%% de dégâts (max 3/4/5).¦MECHANIK: Je 10 Smaragde im Inventar gewähren +1 Glück und +2%% Schaden (max. 3/4/5).
Hits of 6 or more: pay 1 emerald to take 30/40/50%% less.¦Golpes de 6 ou mais: paga 1 esmeralda para receber 30/40/50%% menos.¦Coups de 6 ou plus : payez 1 émeraude pour subir 30/40/50%% de moins.¦Treffer ab 6: Zahle 1 Smaragd, um 30/40/50%% weniger zu erleiden.
Monsters you kill drop an emerald (5/10/15%%).¦Monstros abatidos deixam uma esmeralda (5/10/15%%).¦Les monstres tués lâchent une émeraude (5/10/15%%).¦Getötete Monster lassen einen Smaragd fallen (5/10/15%%).
Spend 1 emerald: a 9/12/15 damage shot.¦Gasta 1 esmeralda: disparo de 9/12/15 de dano.¦Dépense 1 émeraude : tir de 9/12/15 dégâts.¦Verbraucht 1 Smaragd: Schuss mit 9/12/15 Schaden.
Spend 2 emeralds: Absorption II and Resistance I.¦Gasta 2 esmeraldas: Absorção II e Resistência I.¦Dépense 2 émeraudes : Absorption II et Résistance I.¦Verbraucht 2 Smaragde: Absorption II und Resistenz I.
MECHANIC: sprinting, Tiger stance (+10/15/20%% damage); sneaking, Crane stance (−10/15/20%% damage taken).¦MECÂNICA: correndo, postura do Tigre (+10/15/20%% de dano); agachado, postura da Garça (−10/15/20%% de dano recebido).¦MÉCANIQUE : sprint, posture du Tigre (+10/15/20%% de dégâts) ; accroupi, posture de la Grue (−10/15/20%% de dégâts subis).¦MECHANIK: Sprinten, Tigerhaltung (+10/15/20%% Schaden); Schleichen, Kranichhaltung (−10/15/20%% erlittener Schaden).
15/30/45%% chance to deflect projectiles.¦15/30/45%% de chance de desviar projéteis.¦15/30/45%% de chance de dévier les projectiles.¦15/30/45%% Chance, Geschosse abzulenken.
Each kill: +2 hunger and heal 1/2/3.¦Cada abate: +2 de fome e cura 1/2/3.¦Chaque élimination : +2 de faim et rend 1/2/3 de santé.¦Jede Tötung: +2 Hunger und heilt 1/2/3.
Launch forward; the first enemy takes 7/9/11 and is knocked flying.¦Avança; o primeiro inimigo recebe 7/9/11 e é lançado ao ar.¦Fonce ; le premier ennemi subit 7/9/11 et est projeté en l'air.¦Stürmt vor; der erste Feind erleidet 7/9/11 und wird hochgeschleudert.
8 lightning-fast punches on the target (2/2.5/3 each).¦8 socos velozes no alvo (2/2.5/3 cada).¦8 coups de poing fulgurants sur la cible (2/2.5/3 chacun).¦8 blitzschnelle Faustschläge gegen das Ziel (je 2/2.5/3).
MECHANIC: switching element (G) raises your magic damage by 30/45/60%% for 5 s.¦MECÂNICA: trocar de elemento (G) aumenta o dano mágico em 30/45/60%% por 5 s.¦MÉCANIQUE : changer d'élément (G) augmente les dégâts magiques de 30/45/60%% pendant 5 s.¦MECHANIK: Elementwechsel (G) erhöht deinen magischen Schaden für 5 s um 30/45/60%%.
−20/40/60%% fire, ice and lightning damage.¦−20/40/60%% de dano de fogo, gelo e relâmpago.¦−20/40/60%% de dégâts de feu, glace et foudre.¦−20/40/60%% Feuer-, Eis- und Blitzschaden.
Each kill restores 8/16/24 mana.¦Cada abate restaura 8/16/24 de mana.¦Chaque élimination rend 8/16/24 de mana.¦Jede Tötung stellt 8/16/24 Mana wieder her.
3 projectiles of your active element, one every 0.5 s.¦3 projéteis do elemento ativo, um a cada 0.5 s.¦3 projectiles de l'élément actif, un toutes les 0.5 s.¦3 Geschosse des aktiven Elements, eines alle 0.5 s.
4 pulses of your active element's burst over 6 s.¦4 pulsos da explosão do elemento ativo em 6 s.¦4 impulsions de l'explosion de l'élément actif en 6 s.¦4 Impulse des Ausbruchs de deinem aktiven Element über 6 s.
MECHANIC: while transformed, every 10 s you gain +5%% damage (max 3/4/5); lost when you revert.¦MECÂNICA: transformado, ganha +5%% de dano a cada 10 s (máx. 3/4/5); perde ao reverter.¦MÉCANIQUE : transformé, gagne +5%% de dégâts toutes les 10 s (max 3/4/5) ; perdus au retour.¦MECHANIK: Verwandelt erhältst du alle 10 s +5%% Schaden (max. 3/4/5); beim Zurückverwandeln verloren.
When hit, 10/20/30%% chance of Invisibility for 2 s (every 20 s).¦Ao receber um golpe, 10/20/30%% de chance de Invisibilidade por 2 s (a cada 20 s).¦En étant touché, 10/20/30%% de chance d'Invisibilité pendant 2 s (toutes les 20 s).¦Bei einem Treffer 10/20/30%% Chance auf Unsichtbarkeit für 2 s (alle 20 s).
Each kill: heal 1/2/3 and +1 hunger.¦Cada abate: cura 1/2/3 e +1 de fome.¦Chaque élimination : rend 1/2/3 de santé et +1 de faim.¦Jede Tötung: heilt 1/2/3 und +1 Hunger.
Leap at the target and deal 6/8/10 damage on arrival.¦Salta até o alvo e causa 6/8/10 de dano ao chegar.¦Bondit sur la cible et inflige 6/8/10 dégâts à l'arrivée.¦Springt zum Ziel und verursacht bei der Ankunft 6/8/10 Schaden.
Enemies within 6 blocks take 4/5/6, Weakness and knockback.¦Inimigos em 6 blocos recebem 4/5/6, Fraqueza e repulsão.¦Les ennemis à 6 blocs subissent 4/5/6, Faiblesse et recul.¦Feinde in 6 Blöcken erleiden 4/5/6, Schwäche und Rückstoß.
Slow time around you (5/6/7 blocks): 3/4/5 damage, Slowness IV and Mining Fatigue to enemies; you gain Speed II and Haste II.¦Desacelera o tempo ao redor (5/6/7 blocos): 3/4/5 de dano, Lentidão IV e Fadiga aos inimigos; você ganha Velocidade II e Pressa II.¦Ralentit le temps autour de vous (5/6/7 blocs) : 3/4/5 dégâts, Lenteur IV et Fatigue aux ennemis ; vous gagnez Vitesse II et Célérité II.¦Verlangsamt die Zeit um dich (5/6/7 Blöcke): 3/4/5 Schaden, Langsamkeit IV und Abbaulähmung für Feinde; du erhältst Schnelligkeit II und Eile II.
+3%% speed per rank.¦+3%% de velocidade por grau.¦+3%% de vitesse par rang.¦+3%% Geschwindigkeit pro Rang.
+0.4 damage per rank.¦+0.4 de dano por grau.¦+0.4 de dégâts par rang.¦+0.4 Schaden pro Rang.
+4%% speed per rank.¦+4%% de velocidade por grau.¦+4%% de vitesse par rang.¦+4%% Geschwindigkeit pro Rang.
−10%% cooldown on your firearms and +4%% ranged damage per rank.¦−10%% de recarga das armas de fogo e +4%% de dano à distância por grau.¦−10%% de recharge des armes à feu et +4%% de dégâts à distance par rang.¦−10%% Abklingzeit deiner Schusswaffen und +4%% Fernkampfschaden pro Rang.
32-block shot: 8/10/12 damage and knockback.¦Disparo de 32 blocos: 8/10/12 de dano e repulsão.¦Tir de 32 blocs : 8/10/12 dégâts et recul.¦Schuss über 32 Blöcke: 8/10/12 Schaden und Rückstoß.
8/16/24%% chance of a ranged critical (+50%%).¦8/16/24%% de chance de crítico à distância (+50%%).¦8/16/24%% de chance de critique à distance (+50%%).¦8/16/24%% Chance auf einen kritischen Fernkampftreffer (+50%%).
6 shots in 1.2 s at nearby enemies (3/4/5 each).¦6 disparos em 1.2 s nos inimigos próximos (3/4/5 cada).¦6 tirs en 1.2 s sur les ennemis proches (3/4/5 chacun).¦6 Schüsse in 1.2 s auf nahe Feinde (je 3/4/5).
MECHANIC: every ranged hit loads a round; the 6th is explosive: +50/75/100%% damage plus area damage.¦MECÂNICA: cada golpe à distância carrega uma bala; a 6ª explode: +50/75/100%% de dano e dano em área.¦MÉCANIQUE : chaque coup à distance charge une balle ; la 6e explose : +50/75/100%% de dégâts et dégâts de zone.¦MECHANIK: Jeder Fernkampftreffer lädt eine Kugel; die 6. explodiert: +50/75/100%% Schaden plus Flächenschaden.
When hit, 10/20/30%% chance to roll back with Speed II (every 8 s).¦Ao receber um golpe, 10/20/30%% de chance de rolar para trás com Velocidade II (a cada 8 s).¦En étant touché, 10/20/30%% de chance de rouler en arrière avec Vitesse II (toutes les 8 s).¦Bei einem Treffer 10/20/30%% Chance, mit Schnelligkeit II zurückzurollen (alle 8 s).
A bullet bouncing between 3/4/5 enemies (6/7/8 each).¦Bala que ricocheteia entre 3/4/5 inimigos (6/7/8 cada).¦Balle rebondissant entre 3/4/5 ennemis (6/7/8 chacun).¦Kugel, die zwischen 3/4/5 Feinden abprallt (je 6/7/8).
Mark up to 5/6/7 enemies and after 1.5 s shoot them all (11/14/17).¦Marca até 5/6/7 inimigos e dispara em todos após 1.5 s (11/14/17).¦Marque jusqu'à 5/6/7 ennemis et les touche tous après 1.5 s (11/14/17).¦Markiert bis zu 5/6/7 Feinde und beschießt sie alle nach 1.5 s (11/14/17).
+5%% melee damage per rank.¦+5%% de dano corpo a corpo por grau.¦+5%% de dégâts de mêlée par rang.¦+5%% Nahkampfschaden pro Rang.
Fire all your floating blades plus 3 new ones at the target (5/6/7 each).¦Dispara todas as lâminas flutuantes mais 3 novas contra o alvo (5/6/7 cada).¦Lance toutes vos lames flottantes et 3 nouvelles sur la cible (5/6/7 chacune).¦Feuert alle schwebenden Klingen und 3 neue auf das Ziel (je 5/6/7).
Each floating blade reduces damage taken by 1.5/3/4.5%% (max 40%%).¦Cada lâmina flutuante reduz o dano recebido em 1.5/3/4.5%% (máx. 40%%).¦Chaque lame flottante réduit les dégâts subis de 1.5/3/4.5%% (max 40%%).¦Jede schwebende Klinge senkt erlittenen Schaden um 1.5/3/4.5%% (max. 40%%).
8 swords fall where you look (4/5/6 each).¦8 espadas caem no local observado (4/5/6 cada).¦8 épées tombent au point regardé (4/5/6 chacune).¦8 Schwerter fallen am anvisierten Ort (je 4/5/6).
MECHANIC: every 4 s you summon a blade that orbits you (max 3/4/5); each melee hit fires one at the target.¦MECÂNICA: a cada 4 s invoca uma lâmina que orbita você (máx. 3/4/5); cada golpe corpo a corpo lança uma no alvo.¦MÉCANIQUE : toutes les 4 s, une lame orbite autour de vous (max 3/4/5) ; chaque coup de mêlée en lance une sur la cible.¦MECHANIK: Alle 4 s beschwörst du eine kreisende Klinge (max. 3/4/5); jeder Nahkampftreffer feuert eine aufs Ziel.
Melee hits: 10/20/30%% chance a blade parries (−50%% damage).¦Golpes corpo a corpo: 10/20/30%% de chance de uma lâmina aparar (−50%% de dano).¦Coups de mêlée : 10/20/30%% de chance qu'une lame pare (−50%% de dégâts).¦Nahkampftreffer: 10/20/30%% Chance, dass eine Klinge pariert (−50%% Schaden).
Each kill summons a floating blade.¦Cada abate invoca uma lâmina flutuante.¦Chaque élimination invoque une lame flottante.¦Jede Tötung beschwört eine schwebende Klinge.
A giant 7-block slash: 10/12/14 damage, launching enemies.¦Corte gigante de 7 blocos: 10/12/14 de dano, lançando inimigos ao ar.¦Entaille géante de 7 blocs : 10/12/14 dégâts, projetant les ennemis.¦Riesiger 7-Blöcke-Hieb: 10/12/14 Schaden, schleudert Feinde hoch.
Roots the target for 3 s while 6 blades cut it (3/4/5 each).¦Imobiliza o alvo por 3 s enquanto 6 lâminas cortam (3/4/5 cada).¦Immobilise la cible 3 s tandis que 6 lames la tailladent (3/4/5 chacune).¦Fesselt das Ziel für 3 s, während 6 Klingen es schneiden (je 3/4/5).
+5%% ranged damage per rank.¦+5%% de dano à distância por grau.¦+5%% de dégâts à distance par rang.¦+5%% Fernkampfschaden pro Rang.
Marks the target for 10 s: it takes +15/25/35%% of your damage.¦Marca o alvo por 10 s: recebe +15/25/35%% do seu dano.¦Marque la cible 10 s : elle subit +15/25/35%% de vos dégâts.¦Markiert das Ziel für 10 s: Es erleidet +15/25/35%% deines Schadens.
Every 10 s, hostiles within 16/20/24 blocks glow for 3 s.¦A cada 10 s, hostis em 16/20/24 blocos brilham por 3 s.¦Toutes les 10 s, les hostiles à 16/20/24 blocs brillent pendant 3 s.¦Alle 10 s leuchten Feinde in 16/20/24 Blöcken für 3 s.
Place a trap (30 s): the first enemy is rooted for 3 s and takes 6/8/10.¦Coloca uma armadilha (30 s): o primeiro inimigo fica preso por 3 s e recebe 6/8/10.¦Pose un piège (30 s) : le premier ennemi est immobilisé 3 s et subit 6/8/10.¦Stellt eine Falle (30 s): Der erste Feind wird 3 s gefesselt und erleidet 6/8/10.
MECHANIC: hitting the same prey repeatedly stacks +6%% damage against it (max 3/4/5).¦MECÂNICA: atingir a mesma presa repetidamente acumula +6%% de dano contra ela (máx. 3/4/5).¦MÉCANIQUE : frapper la même proie cumule +6%% de dégâts contre elle (max 3/4/5).¦MECHANIK: Wiederholte Treffer auf dieselbe Beute stapeln +6%% Schaden gegen sie (max. 3/4/5).
Sneaking still for 2 s makes you invisible.¦Ficar agachado e parado por 2 s torna você invisível.¦Rester accroupi immobile 2 s vous rend invisible.¦2 s regungsloses Schleichen macht dich unsichtbar.
Each kill gives you 2/3/4 arrows.¦Cada abate concede 2/3/4 flechas.¦Chaque élimination donne 2/3/4 flèches.¦Jede Tötung gibt dir 2/3/4 Pfeile.
10 arrows rain where you look (3/4/5 each).¦10 flechas caem no local observado (3/4/5 cada).¦10 flèches pleuvent au point regardé (3/4/5 chacune).¦10 Pfeile regnen am anvisierten Ort herab (je 3/4/5).
Your falcon strikes your marked prey (or target) 4 times: 4/5/6 each.¦Seu falcão ataca a presa marcada (ou alvo) 4 vezes: 4/5/6 cada.¦Votre faucon frappe la proie marquée (ou cible) 4 fois : 4/5/6 chacune.¦Dein Falke trifft die markierte Beute (oder das Ziel) 4-mal: je 4/5/6.
Allies within 8 blocks: Speed (rank 2: Luck; rank 3: Haste).¦Aliados em 8 blocos: Velocidade (grau 2: Sorte; grau 3: Pressa).¦Alliés à 8 blocs : Vitesse (rang 2 : Chance ; rang 3 : Célérité).¦Verbündete in 8 Blöcken: Schnelligkeit (Rang 2: Glück; Rang 3: Eile).
Allies within 10 blocks: Strength for 10 s (II at rank 3) and heal 2.¦Aliados em 10 blocos: Força por 10 s (II no grau 3) e cura 2.¦Alliés à 10 blocs : Force pendant 10 s (II au rang 3) et rend 2 de santé.¦Verbündete in 10 Blöcken: Stärke für 10 s (II auf Rang 3) und heilt 2.
Every 5 s, allies within 8 blocks heal 0.5/1/1.5.¦A cada 5 s, aliados em 8 blocos curam 0.5/1/1.5.¦Toutes les 5 s, les alliés à 8 blocs récupèrent 0.5/1/1.5.¦Alle 5 s heilen Verbündete in 8 Blöcken um 0.5/1/1.5.
Enemies within 8 blocks: 4/5/6 damage, Nausea, Weakness and knockback.¦Inimigos em 8 blocos: 4/5/6 de dano, Náusea, Fraqueza e repulsão.¦Ennemis à 8 blocs : 4/5/6 dégâts, Nausée, Faiblesse et recul.¦Feinde in 8 Blöcken: 4/5/6 Schaden, Übelkeit, Schwäche und Rückstoß.
MECHANIC: each song adds a note; at 4, a finale: Regeneration II and Resistance to allies (5/6/7 s).¦MECÂNICA: cada canção soma uma nota; com 4, finalização: Regeneração II e Resistência aos aliados (5/6/7 s).¦MÉCANIQUE : chaque chant ajoute une note ; à 4, finale : Régénération II et Résistance aux alliés (5/6/7 s).¦MECHANIK: Jedes Lied gibt eine Note; bei 4 ein Finale: Regeneration II und Resistenz für Verbündete (5/6/7 s).
10/20/30%% chance a song goes off without cooldown.¦10/20/30%% de chance de uma canção não ter recarga.¦10/20/30%% de chance qu'un chant ne déclenche pas de recharge.¦10/20/30%% Chance, dass ein Lied keine Abklingzeit auslöst.
Melee hits: 10/20/30%% chance to lull the attacker (Slowness II and Weakness).¦Golpes corpo a corpo: 10/20/30%% de chance de embalar o atacante (Lentidão II e Fraqueza).¦Coups de mêlée : 10/20/30%% de chance d'assoupir l'attaquant (Lenteur II et Faiblesse).¦Nahkampftreffer: 10/20/30%% Chance, den Angreifer einzulullen (Langsamkeit II und Schwäche).
Allies within 10 blocks: Absorption II for 10 s and Resistance for 5 s.¦Aliados em 10 blocos: Absorção II por 10 s e Resistência por 5 s.¦Alliés à 10 blocs : Absorption II pendant 10 s et Résistance pendant 5 s.¦Verbündete in 10 Blöcken: Absorption II für 10 s und Resistenz für 5 s.
Enemies within 10 blocks: 6/8/10 damage and Wither; allies: Speed II.¦Inimigos em 10 blocos: 6/8/10 de dano e Decomposição; aliados: Velocidade II.¦Ennemis à 10 blocs : 6/8/10 dégâts et Wither ; alliés : Vitesse II.¦Feinde in 10 Blöcken: 6/8/10 Schaden und Ausdörrung; Verbündete: Schnelligkeit II.
+5%% magic damage per rank; +10%% more below half health.¦+5%% de dano mágico por grau; +10%% extra abaixo da metade da vida.¦+5%% de dégâts magiques par rang ; +10%% de plus sous la moitié de santé.¦+5%% magischer Schaden pro Rang; weitere +10%% unter halbem Leben.
The target takes 1.5/2/2.5 damage per second for 8 s and Weakness.¦O alvo recebe 1.5/2/2.5 de dano por segundo por 8 s e Fraqueza.¦La cible subit 1.5/2/2.5 dégâts par seconde pendant 8 s et Faiblesse.¦Das Ziel erleidet für 8 s 1.5/2/2.5 Schaden pro Sekunde und Schwäche.
Your magic damage heals you 4/8/12%%.¦Seu dano mágico cura você em 4/8/12%%.¦Vos dégâts magiques vous soignent de 4/8/12%%.¦Dein magischer Schaden heilt dich um 4/8/12%%.
24-block bolt: 9/11/13 damage and Wither.¦Projétil de 24 blocos: 9/11/13 de dano e Decomposição.¦Projectile de 24 blocs : 9/11/13 dégâts et Wither.¦Geschoss über 24 Blöcke: 9/11/13 Schaden und Ausdörrung.
MECHANIC: each Warlock spell costs 1 health and builds Corruption (+6%% magic damage, max 3/4/5) for 10 s.¦MECÂNICA: cada magia de Bruxo custa 1 de vida e acumula Corrupção (+6%% de dano mágico, máx. 3/4/5) por 10 s.¦MÉCANIQUE : chaque sort de Sorcier coûte 1 de santé et cumule Corruption (+6%% de dégâts magiques, max 3/4/5) pendant 10 s.¦MECHANIK: Jeder Hexenmeisterzauber kostet 1 Leben und sammelt Verderbnis (+6%% magischer Schaden, max. 3/4/5) für 10 s.
When hit, 10/20/30%% chance to curse the attacker (Wither) and heal 1.¦Ao receber um golpe, 10/20/30%% de chance de amaldiçoar o atacante (Decomposição) e curar 1.¦En étant touché, 10/20/30%% de chance de maudire l'attaquant (Wither) et de rendre 1 de santé.¦Bei einem Treffer 10/20/30%% Chance, den Angreifer zu verfluchen (Ausdörrung) und 1 Leben zu heilen.
Each kill heals 2/4/6 and restores 10 mana.¦Cada abate cura 2/4/6 e restaura 10 de mana.¦Chaque élimination rend 2/4/6 de santé et 10 de mana.¦Jede Tötung heilt 2/4/6 und stellt 10 Mana wieder her.
Summon 2 imps (3 at rank 3) that fight for you.¦Invoca 2 diabretes (3 no grau 3) que lutam por você.¦Invoque 2 diablotins (3 au rang 3) qui combattent pour vous.¦Beschwört 2 Wichtel (3 auf Rang 3), die für dich kämpfen.
Doom the target: after 5 s it takes 16/20/24 damage.¦Condena o alvo: após 5 s, recebe 16/20/24 de dano.¦Condamne la cible : après 5 s, elle subit 16/20/24 dégâts.¦Verdammt das Ziel: Nach 5 s erleidet es 16/20/24 Schaden.
Your nearby pets have Regeneration (rank 2: Strength; rank 3: Resistance).¦Seus animais próximos têm Regeneração (grau 2: Força; grau 3: Resistência).¦Vos animaux proches ont Régénération (rang 2 : Force ; rang 3 : Résistance).¦Deine nahen Tiere haben Regeneration (Rang 2: Stärke; Rang 3: Resistenz).
Your pets attack the target with Speed II and Strength.¦Seus animais atacam o alvo com Velocidade II e Força.¦Vos animaux attaquent la cible avec Vitesse II et Force.¦Deine Tiere greifen das Ziel mit Schnelligkeit II und Stärke an.
+4%% damage per rank for each nearby pet (max 3).¦+4%% de dano por grau por animal próximo (máx. 3).¦+4%% de dégâts par rang pour chaque animal proche (max 3).¦+4%% Schaden pro Rang je nahem Tier (max. 3).
Fully heal your pets (plus Absorption); you heal 2 per pet.¦Cura totalmente seus animais (mais Absorção); você cura 2 por animal.¦Soigne entièrement vos animaux (avec Absorption) ; vous récupérez 2 par animal.¦Heilt deine Tiere vollständig (plus Absorption); du heilst 2 pro Tier.
MECHANIC: if one of your pets is attacking your target, you deal +10/15/20%% damage to it.¦MECÂNICA: se um animal seu ataca seu alvo, você causa +10/15/20%% de dano nele.¦MÉCANIQUE : si un de vos animaux attaque votre cible, vous lui infligez +10/15/20%% de dégâts.¦MECHANIK: Greift eines deiner Tiere dein Ziel an, verursachst du +10/15/20%% Schaden daran.
If something hits you, your pets attack it with Speed (rank 2+: Strength).¦Se algo atingir você, seus animais atacam com Velocidade (grau 2+: Força).¦Si quelque chose vous frappe, vos animaux l'attaquent avec Vitesse (rang 2+ : Force).¦Trifft dich etwas, greifen deine Tiere es mit Schnelligkeit an (Rang 2+: Stärke).
Each kill heals your nearby pets 2/4/6.¦Cada abate cura seus animais próximos em 2/4/6.¦Chaque élimination soigne vos animaux proches de 2/4/6.¦Jede Tötung heilt deine nahen Tiere um 2/4/6.
A stampede along 8 blocks: 7/9/11 damage, launching enemies.¦Estampida ao longo de 8 blocos: 7/9/11 de dano, lançando inimigos ao ar.¦Ruée sur 8 blocs : 7/9/11 dégâts, projetant les ennemis.¦Stampede über 8 Blöcke: 7/9/11 Schaden, schleudert Feinde hoch.
Your pets gain Strength II and Resistance II for 10 s and weaken nearby enemies.¦Seus animais ganham Força II e Resistência II por 10 s e enfraquecem inimigos próximos.¦Vos animaux gagnent Force II et Résistance II pendant 10 s et affaiblissent les ennemis proches.¦Deine Tiere erhalten Stärke II und Resistenz II für 10 s und schwächen nahe Feinde.
""")


def legendary(rows):
    suffixes = (' LEGENDARY: activate it with the H key.',
                ' LENDÁRIA: ative com a tecla H.',
                ' LÉGENDAIRE : activez avec la touche H.',
                ' LEGENDÄR: Mit Taste H aktivieren.')
    add('\n'.join('¦'.join(value + suffix for value, suffix in zip(row.split('¦'), suffixes, strict=True))
                  for row in rows.strip().splitlines()))


legendary("""
Passive: each hit of the same damage type makes you 10%% more resistant to it for 60 s (max 80%%). Active: 10 s immunity to the last damage type taken and Regeneration II.¦Passiva: cada golpe do mesmo tipo de dano aumenta a resistência a ele em 10%% por 60 s (máx. 80%%). Ativa: 10 s de imunidade ao último tipo de dano recebido e Regeneração II.¦Passif : chaque coup du même type augmente votre résistance à ce type de 10%% pendant 60 s (max 80%%). Actif : 10 s d'immunité au dernier type subi et Régénération II.¦Passiv: Jeder Treffer desselben Schadenstyps gibt 10%% mehr Resistenz dagegen für 60 s (max. 80%%). Aktiv: 10 s Immunität gegen den zuletzt erlittenen Schadenstyp und Regeneration II.
Passive: 15%% extra emerald on kills. Active: steals every beneficial effect from anyone within 8 blocks.¦Passiva: 15%% de esmeralda extra por abate. Ativa: rouba todos os efeitos benéficos de quem estiver em 8 blocos.¦Passif : 15%% d'émeraude supplémentaire par élimination. Actif : vole tous les effets bénéfiques de chacun à 8 blocs.¦Passiv: 15%% zusätzlicher Smaragd bei Tötungen. Aktiv: Stiehlt alle positiven Effekte von allen in 8 Blöcken.
Passive: memorizes the last role ability used near you (16 blocks) by any player. Active: you cast it yourself at rank 3.¦Passiva: memoriza a última habilidade de classe usada por qualquer jogador em 16 blocos. Ativa: você a lança no grau 3.¦Passif : mémorise la dernière capacité de rôle utilisée par un joueur à 16 blocs. Actif : vous la lancez au rang 3.¦Passiv: Merkt sich die letzte Rollenfähigkeit eines Spielers in 16 Blöcken. Aktiv: Du wirkst sie selbst auf Rang 3.
Passive: your hits infect (poison) and it spreads on death. Active: 6 s spore cloud (poison II, blindness).¦Passiva: golpes infectam (veneno), espalhando ao morrer. Ativa: nuvem de esporos de 6 s (veneno II, cegueira).¦Passif : vos coups infectent (poison), propagé à la mort. Actif : nuage de spores de 6 s (poison II, cécité).¦Passiv: Treffer infizieren (Gift), das sich beim Tod ausbreitet. Aktiv: 6 s Sporenwolke (Gift II, Blindheit).
Passive: 10%% lifesteal. Active: latch onto a target, draining 2 health per second for 5 s while invisible.¦Passiva: 10%% de roubo de vida. Ativa: prende-se a um alvo, drenando 2 de vida por segundo por 5 s enquanto invisível.¦Passif : 10%% de vol de vie. Actif : s'accroche à une cible et draine 2 de santé par seconde pendant 5 s en restant invisible.¦Passiv: 10%% Lebensraub. Aktiv: Heftet sich unsichtbar an ein Ziel und entzieht 5 s lang 2 Leben pro Sekunde.
Passive: fire immune; your hits burn. Active: infernal nova (10 area damage and ground fire).¦Passiva: imune a fogo; golpes queimam. Ativa: nova infernal (10 de dano em área e fogo no chão).¦Passif : immunité au feu ; vos coups brûlent. Actif : nova infernale (10 dégâts de zone et feu au sol).¦Passiv: Feuerimmun; deine Treffer entzünden. Aktiv: Höllennova (10 Flächenschaden und Bodenfeuer).
Passive: +4 hearts; Strength and Regeneration to allies within 12 blocks. Active: Royal Decree (enemies within 16 blocks are nearly frozen and weakened).¦Passiva: +4 corações; Força e Regeneração aos aliados em 12 blocos. Ativa: Decreto Real (inimigos em 16 blocos quase parados e enfraquecidos).¦Passif : +4 cœurs ; Force et Régénération aux alliés à 12 blocs. Actif : Décret royal (ennemis à 16 blocs presque immobilisés et affaiblis).¦Passiv: +4 Herzen; Stärke und Regeneration für Verbündete in 12 Blöcken. Aktiv: Königlicher Erlass (Feinde in 16 Blöcken fast bewegungslos und geschwächt).
Passive: your position and health are recorded every second. Active: rewind 5 seconds (position and health).¦Passiva: posição e vida registradas a cada segundo. Ativa: retrocede 5 segundos (posição e vida).¦Passif : position et santé enregistrées chaque seconde. Actif : revient 5 secondes en arrière (position et santé).¦Passiv: Position und Leben werden jede Sekunde erfasst. Aktiv: 5 Sekunden zurückspulen (Position und Leben).
Passive: immune to negative effects. Active: 8 s zone where other players CANNOT use abilities and mobs lose their effects.¦Passiva: imune a efeitos negativos. Ativa: zona de 8 s onde outros jogadores NÃO podem usar habilidades e criaturas perdem seus efeitos.¦Passif : immunité aux effets négatifs. Actif : zone de 8 s où les autres joueurs NE PEUVENT PAS utiliser de capacités et les créatures perdent leurs effets.¦Passiv: Immun gegen negative Effekte. Aktiv: 8 s Zone, in der andere Spieler KEINE Fähigkeiten nutzen können und Kreaturen ihre Effekte verlieren.
Passive: nearby babies grow 10× faster. Active: mutates nearby animals into other species.¦Passiva: filhotes próximos crescem 10× mais rápido. Ativa: transforma animais próximos em outras espécies.¦Passif : les bébés proches grandissent 10× plus vite. Actif : transforme les animaux proches en d'autres espèces.¦Passiv: Nahe Jungtiere wachsen 10× schneller. Aktiv: Verwandelt nahe Tiere in andere Arten.
Active: copy the mob you look at (any mob): become it for 30 s and gain half its max health.¦Ativa: copia a criatura observada (qualquer uma): transforma por 30 s e ganha metade da vida máxima dela.¦Actif : copie la créature regardée (n'importe laquelle) : la devient pendant 30 s et gagne la moitié de sa santé maximale.¦Aktiv: Kopiert die anvisierte Kreatur (jede Art): Verwandelt dich für 30 s und gibt die Hälfte ihres maximalen Lebens.
Passive: 25%% double ore. Active: 15 s of double loot from creatures you kill and Luck III.¦Passiva: 25%% de minério duplo. Ativa: 15 s de saque duplo de criaturas abatidas e Sorte III.¦Passif : 25%% de minerai doublé. Actif : 15 s de butin doublé des créatures tuées et Chance III.¦Passiv: 25%% doppeltes Erz. Aktiv: 15 s doppelte Beute von getöteten Kreaturen und Glück III.
Passive: with a sword, +4 armor and +10%% damage. Active: Excalibur, a 30-block beam of light (16 damage, double vs undead).¦Passiva: com espada, +4 de armadura e +10%% de dano. Ativa: Excalibur, raio de luz de 30 blocos (16 de dano, dobro contra mortos-vivos).¦Passif : avec une épée, +4 d'armure et +10%% de dégâts. Actif : Excalibur, rayon de lumière de 30 blocs (16 dégâts, double contre les morts-vivants).¦Passiv: Mit Schwert +4 Rüstung und +10%% Schaden. Aktiv: Excalibur, Lichtstrahl über 30 Blöcke (16 Schaden, doppelt gegen Untote).
Passive: immune to fall damage. Active: 12 s of free flight.¦Passiva: imune a dano de queda. Ativa: 12 s de voo livre.¦Passif : immunité aux dégâts de chute. Actif : 12 s de vol libre.¦Passiv: Immun gegen Fallschaden. Aktiv: 12 s freier Flug.
Passive: Resistance while crouching still. Active: stops time for 5 s within 12 blocks (mobs and projectiles frozen).¦Passiva: Resistência ao ficar agachado e parado. Ativa: para o tempo por 5 s em 12 blocos (criaturas e projéteis congelados).¦Passif : Résistance accroupi immobile. Actif : arrête le temps 5 s à 12 blocs (créatures et projectiles figés).¦Passiv: Resistenz beim regungslosen Schleichen. Aktiv: Hält die Zeit für 5 s in 12 Blöcken an (Kreaturen und Geschosse eingefroren).
Passive: undead won't attack you. Active: sentence a target; after 3 s it loses 50%% of its max health (10%% for bosses).¦Passiva: mortos-vivos não atacam você. Ativa: sentencia um alvo; após 3 s, perde 50%% da vida máxima (10%% para chefes).¦Passif : les morts-vivants ne vous attaquent pas. Actif : condamne une cible ; après 3 s, elle perd 50%% de santé maximale (10%% pour les boss).¦Passiv: Untote greifen dich nicht an. Aktiv: Verurteilt ein Ziel; nach 3 s verliert es 50%% seines maximalen Lebens (10%% bei Bossen).
Passive: +1 reach. Active: a celestial spear falls where you look: 14 damage within 4 blocks, launching enemies.¦Passiva: +1 de alcance. Ativa: lança celestial cai no local observado: 14 de dano em 4 blocos, lançando inimigos ao ar.¦Passif : +1 d'allonge. Actif : une lance céleste tombe au point regardé : 14 dégâts à 4 blocs, projetant les ennemis.¦Passiv: +1 Reichweite. Aktiv: Himmelsspeer fällt am anvisierten Ort: 14 Schaden in 4 Blöcken, schleudert Feinde hoch.
Passive: Regeneration on dirt, grass or leaves. Active: a sacred grove for 8 s (8 blocks): players heal 2/s and enemies are nearly rooted.¦Passiva: Regeneração em terra, grama ou folhas. Ativa: bosque sagrado por 8 s (8 blocos): jogadores curam 2/s e inimigos quase parados.¦Passif : Régénération sur terre, herbe ou feuilles. Actif : bosquet sacré pendant 8 s (8 blocs) : joueurs soignés de 2/s et ennemis presque immobilisés.¦Passiv: Regeneration auf Erde, Gras oder Laub. Aktiv: Heiliger Hain für 8 s (8 Blöcke): Spieler heilen 2/s und Feinde sind fast gefesselt.
Passive: +10%% ranged damage. Active: a 40-block shot: 30 damage (60 if the target is below half health).¦Passiva: +10%% de dano à distância. Ativa: disparo de 40 blocos: 30 de dano (60 se o alvo estiver abaixo da metade da vida).¦Passif : +10%% de dégâts à distance. Actif : tir de 40 blocs : 30 dégâts (60 si la cible a moins de la moitié de santé).¦Passiv: +10%% Fernkampfschaden. Aktiv: Schuss über 40 Blöcke: 30 Schaden (60 bei einem Ziel unter halbem Leben).
Passive: +1 max floating blade. Active: 30 swords rain within 10 blocks around you for 3 s (4 damage each).¦Passiva: +1 lâmina flutuante máxima. Ativa: 30 espadas caem em 10 blocos ao redor por 3 s (4 de dano cada).¦Passif : +1 lame flottante maximale. Actif : 30 épées pleuvent à 10 blocs autour de vous pendant 3 s (4 dégâts chacune).¦Passiv: +1 maximale schwebende Klinge. Aktiv: 30 Schwerter regnen 3 s lang in 10 Blöcken um dich (je 4 Schaden).
""")
