"""Traducción inicial de los nombres de nodos base. Columnas: inglés, pt_br, fr_fr, de_de.
Las descripciones están en base_desc.py.

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


# v67 (F2): nombres de las habilidades nuevas del Bersérker.
add("""
Scars¦Cicatrizes¦Cicatrices¦Narben
Boiling Blood¦Sangue fervente¦Sang bouillonnant¦Kochendes Blut
Throwing Axe¦Machado de arremesso¦Hache de lancer¦Wurfaxt
Blood Chain¦Corrente de sangue¦Chaîne de sang¦Blutkette
Intimidating Roar¦Rugido intimidador¦Rugissement intimidant¦Einschüchterndes Brüllen
Rending Slashes¦Cortes dilacerantes¦Taillades déchirantes¦Reißende Hiebe
Blood Oath¦Juramento de sangue¦Serment de sang¦Blutschwur
Avatar of War¦Avatar da guerra¦Avatar de la guerre¦Avatar des Krieges
Battle Echo¦Eco de batalha¦Écho de bataille¦Schlachtecho
Ragnarök¦Ragnarök¦Ragnarök¦Ragnarök
""")

# v67 (F3): nombres de las habilidades nuevas de Mago, Asesino, Herbolista e Ingeniero.
add("""
Arcane Plenitude¦Plenitude arcana¦Plénitude arcanique¦Arkane Fülle
Latent Frost¦Gelo latente¦Givre latent¦Latenter Frost
Chain Lightning¦Raio em cadeia¦Foudre en chaîne¦Kettenblitz
Mirror Shield¦Escudo espelhado¦Bouclier miroir¦Spiegelschild
Concatenation¦Concatenação¦Concaténation¦Verkettung
Hailstorm¦Tempestade de granizo¦Tempête de grêle¦Hagelsturm
Emergency Reserve¦Reserva de emergência¦Réserve d’urgence¦Notreserve
Pure Arcane Form¦Forma arcana pura¦Forme arcanique pure¦Reine arkane Gestalt
Crystallized Mana¦Mana cristalizada¦Mana cristallisé¦Kristallisiertes Mana
Stellar Nova¦Nova estelar¦Nova stellaire¦Sternennova
Weak Point¦Ponto fraco¦Point faible¦Schwachstelle
Link¦Elo¦Maillon¦Kettenglied
Blinding Smoke¦Fumaça cegante¦Fumée aveuglante¦Blendender Rauch
Lightning Cut¦Corte relâmpago¦Coupe éclair¦Blitzschnitt
Lingering Shadow¦Sombra persistente¦Ombre persistante¦Beharrlicher Schatten
Shadow Chain¦Corrente de sombras¦Chaîne d’ombres¦Schattenkette
Concentrated Venom¦Veneno concentrado¦Venin concentré¦Konzentriertes Gift
Veil of Death¦Véu da morte¦Voile de mort¦Todesschleier
Silent Harvest¦Colheita silenciosa¦Moisson silencieuse¦Stille Ernte
Eternal Night¦Noite eterna¦Nuit éternelle¦Ewige Nacht
Spring¦Primavera¦Printemps¦Frühling
Pollination¦Polinização¦Pollinisation¦Bestäubung
Explosive Seed¦Semente explosiva¦Graine explosive¦Explosiver Same
Healing Root¦Raiz curativa¦Racine curative¦Heilwurzel
Living Bark¦Casca viva¦Écorce vivante¦Lebende Rinde
Locust Plague¦Praga de gafanhotos¦Plaie de sauterelles¦Heuschreckenplage
Universal Antidote¦Antídoto universal¦Antidote universel¦Universalgegengift
Colossal Bloom¦Floração colossal¦Floraison colossale¦Kolossale Blüte
Deep Root¦Raiz profunda¦Racine profonde¦Tiefe Wurzel
Rain of Living Seeds¦Chuva de sementes vivas¦Pluie de graines vivantes¦Regen lebender Samen
Insulation¦Isolamento¦Isolant¦Isolierung
Recycling¦Reciclagem¦Recyclage¦Recycling
Proximity Mine¦Mina de proximidade¦Mine de proximité¦Näherungsmine
Support Drone¦Drone de apoio¦Drone de soutien¦Unterstützungsdrohne
Residual Heat¦Calor residual¦Chaleur résiduelle¦Restwärme
Protective Dome¦Cúpula protetora¦Dôme protecteur¦Schutzkuppel
Thrift Mode¦Modo econômico¦Mode économie¦Sparmodus
Exosuit¦Exoesqueleto¦Exosquelette¦Exoanzug
Assembly Line¦Linha de montagem¦Chaîne de montage¦Fließband
Tesla Tower¦Torre Tesla¦Tour Tesla¦Tesla-Turm
Keep Your Distance¦Manter a distância¦Garder ses distances¦Abstand halten
Pinned Point¦Ponta cravada¦Pointe plantée¦Haftende Spitze
Javelin¦Dardo¦Javelot¦Wurfspeer
Pike Wall¦Muro de piques¦Mur de piques¦Pikenwall
Skewer¦Espetar¦Embrocher¦Aufspießen
Pike Fan¦Leque de piques¦Éventail de piques¦Pikenfächer
First Contact¦Primeiro contato¦Premier contact¦Erstkontakt
Ancestral Spear¦Lança ancestral¦Lance ancestrale¦Uralter Speer
Double Thrust¦Estocada dupla¦Double estocade¦Doppelstoß
Javelin Barrage¦Rajada de dardos¦Salve de javelots¦Wurfspeersalve
Duel¦Duelo¦Duel¦Duell
Piercing Edge¦Fio perfurante¦Lame perforante¦Durchdringende Klinge
Rising Slash¦Corte ascendente¦Taillade ascendante¦Aufwärtshieb
Whirl¦Moinho¦Moulinet¦Wirbel
Winning Streak¦Sequência de vitórias¦Série de victoires¦Siegesserie
Swordsman’s Leap¦Salto do espadachim¦Bond de l’épéiste¦Schwertkämpfersprung
Will of Steel¦Vontade de aço¦Volonté d’acier¦Stählerner Wille
Flow State¦Estado de fluxo¦État de flux¦Flow-Zustand
Unsheathing¦Desembainhada¦Dégainé¦Ziehen der Klinge
Final Cut¦Corte final¦Taillade finale¦Letzter Schnitt
Faith Against Darkness¦Fé contra a escuridão¦Foi contre les ténèbres¦Glaube gegen die Finsternis
Shared Triumph¦Triunfo compartilhado¦Triomphe partagé¦Geteilter Triumph
Ray of Light¦Raio de luz¦Rayon de lumière¦Lichtstrahl
Shield Bash¦Golpe de escudo¦Coup de bouclier¦Schildschlag
Prayer¦Oração¦Prière¦Gebet
Wall of Light¦Muro de luz¦Mur de lumière¦Lichtwand
Shield Bond¦Vínculo de escudo¦Lien de bouclier¦Schildbund
Holy Banner¦Estandarte sagrado¦Étendard sacré¦Heiliges Banner
Righteous Wrath¦Ira justa¦Juste colère¦Gerechter Zorn
Pillar of Light¦Coluna de luz¦Colonne de lumière¦Lichtsäule
Attack Order¦Ordem de ataque¦Ordre d’attaque¦Angriffsbefehl
Feast¦Banquete¦Festin¦Festmahl
Bone Hand¦Mão de ossos¦Main d’os¦Knochenhand
Sacrifice¦Sacrifício¦Sacrifice¦Opferung
Rotting Aura¦Aura pútrida¦Aura putride¦Faulige Aura
Cursed Ground¦Terra amaldiçoada¦Terre maudite¦Verfluchter Boden
Resurge¦Ressurgir¦Renaissance¦Wiederkehr
Necrotic Frenzy¦Frenesi necrótico¦Frénésie nécrotique¦Nekrotische Raserei
Bone Bond¦Vínculo de ossos¦Lien d’os¦Knochenband
Soul Storm¦Tempestade de almas¦Tempête d’âmes¦Seelensturm
Tall Grass¦Grama alta¦Hautes herbes¦Hohes Gras
Rending Claws¦Garras dilacerantes¦Griffes déchirantes¦Reißende Klauen
Vine Whip¦Chicote de trepadeiras¦Fouet de lianes¦Rankenpeitsche
Regrowth¦Rebrota¦Repousse¦Nachwuchs
Torpor¦Letargia¦Léthargie¦Erstarrung
Bear Roar¦Rugido do urso¦Rugissement de l’ours¦Bärengebrüll
Sturdy Oak¦Carvalho firme¦Chêne robuste¦Standfeste Eiche
Dire Wolf Form¦Forma de lobo atroz¦Forme de loup sinistre¦Schattenwolf-Gestalt
Seasonal Flow¦Fluxo sazonal¦Flux saisonnier¦Jahreszeitenfluss
Forest Awakening¦Despertar da floresta¦Éveil de la forêt¦Erwachen des Waldes
Coin Purse¦Bolsa de moedas¦Porte-monnaie¦Geldbeutel
Dividends¦Dividendos¦Dividendes¦Dividenden
Bouncing Coin¦Moeda quicante¦Pièce rebondissante¦Springende Münze
Auction¦Leilão¦Vente aux enchères¦Versteigerung
Credit¦Crédito¦Crédit¦Kredit
Caravan¦Caravana¦Caravane¦Karawane
General Ledger¦Livro-razão¦Grand livre¦Hauptbuch
Investment¦Investimento¦Investissement¦Investition
Bounty Hunting¦Caça de recompensas¦Chasse aux primes¦Kopfgeldjagd
Grand Liquidation¦Grande liquidação¦Grande liquidation¦Großer Ausverkauf
Bare Hands¦Mãos vazias¦Mains nues¦Leere Hände
Combination¦Combinação¦Enchaînement¦Kombination
Whirling Kick¦Chute giratório¦Coup de pied tournoyant¦Wirbeltritt
Chi Wave¦Onda de chi¦Onde de chi¦Chi-Welle
Serene Body¦Corpo sereno¦Corps serein¦Gelassener Körper
Explosive Palm¦Palma explosiva¦Paume explosive¦Explosive Handfläche
Pressure Point¦Ponto de pressão¦Point de pression¦Druckpunkt
Tiger Stance¦Postura do tigre¦Posture du tigre¦Tigerhaltung
Fist Echo¦Eco do punho¦Écho du poing¦Faustecho
Void Palm¦Palma do vazio¦Paume du vide¦Handfläche der Leere
Turning Tide¦Virada de maré¦Marée changeante¦Gezeitenwechsel
Element Echo¦Eco do elemento¦Écho de l’élément¦Elementecho
Elemental Lance¦Lança elemental¦Lance élémentaire¦Elementarlanze
Elemental Wall¦Muro elemental¦Mur élémentaire¦Elementarwand
Chain Reaction¦Reação em cadeia¦Réaction en chaîne¦Kettenreaktion
Elemental Totem¦Totem elemental¦Totem élémentaire¦Elementartotem
Stable Fusion¦Fusão estável¦Fusion stable¦Stabile Fusion
Elemental Form¦Forma elemental¦Forme élémentaire¦Elementargestalt
Elemental Harmony¦Harmonia elemental¦Harmonie élémentaire¦Elementare Harmonie
Elements Unleashed¦Elementos desencadeados¦Éléments déchaînés¦Entfesselte Elemente
""")

# v67: las descripciones base se escriben en base_desc.py (estilo llano), con sus traducciones.
from base_desc import catalog as _descriptions  # noqa: E402

for _source, _values in _descriptions().items():
    if _source in TEXT:
        raise ValueError(f'Descripción base repetida en el catálogo: {_source}')
    TEXT[_source] = _values
