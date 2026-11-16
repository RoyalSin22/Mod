"""Textos narrativos del Grimorio en francés (fr_fr). Traducción del inglés de lore_catalog.py.

Glosario (coherente con el resto del mod): Essence, guildes, Voile, feu de camp, Concorde, Conclave, Seuil, Première Ère,
Éclipse, Ossuaire brisé, Errant voilé, Automate ancien, Gardien (Custodian), Scorificateur de fer, monde principal
(Overworld). Cada entrada conserva los mismos párrafos, en el mismo orden, que el texto inglés. Las criaturas no
llevan título.
"""

TEXTS = {}


def entry(entry_id, title, body):
    TEXTS[entry_id] = (title, body.strip())


entry('lore.cosmology.essence_within', "L'Essence qui habite en chacun", """
Avant que les guildes n'érigent leurs forteresses, avant que les maîtres ne consignent leurs enseignements, les mortels possédaient déjà ce que l'on appellerait plus tard l'Essence.

Ce n'était pas un don des dieux, ni une force réservée à quelques élus.

Elle habitait naturellement les êtres vivants.

Certains apprirent à renforcer leur corps. D'autres découvrirent des capacités qui semblaient dépasser les limites de leur nature. Beaucoup ne comprirent jamais ce qui éveillait ces possibilités.

Les premiers maîtres observèrent que la pratique constante transformait les capacités de leurs apprentis.

Ainsi commença l'étude des Aptitudes.

Les guildes n'ont pas créé ce pouvoir.

Elles ont appris à le développer.
""")

entry('lore.guild.twenty_paths', 'Les Vingt Voies', """
Pendant des générations, les maîtres transmirent leurs techniques sans appartenir à une tradition commune.

Chaque communauté développa des manières différentes de combattre, de survivre, de bâtir et de comprendre le monde.

Avec le temps, de grandes écoles émergèrent.

Vingt de ces traditions atteignirent une influence extraordinaire et furent reconnues par les guildes de la Première Ère.

Leurs enseignements n'étaient pas identiques.

Certaines prônaient la maîtrise des armes ; d'autres cherchaient à comprendre les forces arcaniques, le comportement des créatures ou les possibilités du savoir.

Mais toutes partageaient un principe : le pouvoir doit être cultivé par l'expérience.

Leurs noms survécurent même après la disparition de leurs forteresses.
""")

entry('lore.doctrine.three_doctrines', 'Les Trois Doctrines', """
Les anciens maîtres découvrirent qu'une même discipline pouvait mener à des formes de maîtrise très différentes.

Ceux qui partageaient une formation initiale commencèrent à se distinguer à mesure qu'ils développaient leurs capacités.

Certains préféraient l'endurance. D'autres perfectionnaient la précision, l'adaptation ou des méthodes que leurs compagnons comprenaient à peine.

Le Conseil des Trois Voies, convoqué par la Concorde, reconnut trois grandes doctrines pour chaque guilde.

Aucune ne fut déclarée supérieure.

Chacune exigeait des années de dévouement et une compréhension particulière des enseignements originels.

Choisir une voie signifiait renoncer aux autres : la véritable maîtrise exige de la profondeur.
""")

entry('lore.chronology.council_three_ways', 'Le Conseil des Trois Voies', """
Aux jours de la plus grande prospérité de la Première Ère, les représentants des grandes guildes se réunirent pour comparer leurs méthodes d'enseignement.

La diversité des techniques avait tant grandi que même les maîtres d'une même discipline se disputaient sur la bonne manière de faire.

Le Conseil ne trancha pas ces différences en proclamant des vainqueurs.

Il reconnut qu'une même tradition pouvait contenir plusieurs réponses également légitimes.

Les doctrines furent ordonnées, leurs enseignements consignés et leurs techniques transmises aux nouvelles générations.

Pendant longtemps, cet accord permit aux guildes de partager leur savoir sans renoncer à leurs différences.

Le nom du Conseil survécut aux institutions qui l'avaient convoqué.
""")

entry('lore.faction.concordance', 'La Concorde des guildes', """
La Première Ère connut une époque où les vingt traditions œuvraient ensemble au sein d'une organisation commune.

On l'appela la Concorde.

Ses représentants partageaient méthodes de formation, outils et découvertes. Les routes entre les établissements permettaient aux maîtres de voyager et aux apprentis d'apprendre des techniques différentes de celles de leur terre natale.

La Concorde ne parvint jamais à éliminer toutes les rivalités.

Elle ne l'essaya pas non plus.

Son but était d'empêcher les différences de détruire le savoir que chaque guilde avait accumulé.

Mais il existait des questions pour lesquelles aucun accord ne suffisait.

Les plus dangereuses commencèrent lorsque des chercheurs décidèrent d'étudier ce qui subsistait après la mort.
""")

entry('lore.cosmology.echoes', "L'Empreinte des déchus", """
Les premiers érudits de la mort observèrent que certaines manifestations persistaient après le dernier souffle.

Ce n'étaient pas des corps vivants.

On ne pouvait pas non plus les décrire avec certitude comme des esprits complets.

Certains chercheurs les appelèrent des Échos.

Leur origine et leur persistance déconcertaient les maîtres de la Première Ère.

Une partie des guildes soutenait qu'il fallait seulement les étudier.

D'autres y voyaient l'occasion de comprendre ce qui séparait la vie de la mort.

Cette différence semblait insignifiante.

Avec le temps, elle devint l'un des grands désaccords de la civilisation.
""")

entry('lore.cosmology.veil_frontier', 'La Frontière invisible', """
Les chercheurs découvrirent que les dimensions n'étaient pas totalement isolées.

Certains phénomènes pouvaient franchir certaines frontières, bien que la plupart des êtres vivants ne les perçoivent jamais.

Ils les nommèrent ensemble le Voile.

Au début, ils crurent que toutes ses régions obéissaient aux mêmes lois.

Plus tard, ils découvrirent que certaines frontières offraient une résistance extraordinaire.

Elles ne semblaient protéger ni une cité, ni une tombe, ni aucun royaume connu.

Elles barraient simplement le chemin.

Personne ne savait ce qui se trouvait derrière.

Et, pendant un temps, personne ne jugea nécessaire de le découvrir.
""")

entry('lore.faction.permanence', 'Les Chercheurs de la Permanence', """
La mortalité était acceptée depuis des générations comme une condition inévitable.

Mais les découvertes sur la persistance de l'Essence soulevèrent de nouvelles questions.

Une conscience pouvait-elle être préservée après la mort ?

Le savoir de quelqu'un pouvait-il être conservé même lorsque son corps n'existait plus ?

Les chercheurs du Conclave cherchèrent des réponses.

Tous ne souhaitaient pas atteindre l'immortalité.

Certains voulaient comprendre le sort de la mémoire. D'autres espéraient éviter que des générations entières de savoir ne se perdent.

Lorsqu'ils détectèrent des signaux venant d'une région inaccessible du Voile, ils crurent avoir trouvé un indice de ce qu'ils cherchaient.

Ils ignoraient encore ce qui attendait de l'autre côté.
""")

entry('lore.chronology.first_answer', 'La Première Réponse', """
Les instruments enregistrèrent un signal qu'ils ne surent pas expliquer.

Les chercheurs répétèrent l'expérience.

Le signal revint.

Ils se mirent à poser des questions et découvrirent des motifs qui semblaient y répondre.

Bientôt, des réponses arrivèrent, porteuses d'un savoir inconnu.

Certains membres du Conclave affirmèrent avoir trouvé une conscience capable de survivre sans corps.

D'autres soupçonnèrent que cette présence n'avait jamais été humaine.

Les expériences se poursuivirent.

Aucun document conservé n'indique que l'entité ait exigé la construction d'un mécanisme de communication.

Mais les réponses guidèrent peu à peu les chercheurs vers cette possibilité.

Et ils choisirent de les suivre.
""")

entry('lore.artifact.sealed_artifact', "L'Artefact scellé", """
Les chercheurs construisirent un objet capable de retenir un signal et de l'amplifier grâce à l'Essence.

Son but initial était d'établir une communication plus intense avec la région inconnue du Voile.

L'expérience dépassa toutes les attentes.

Lorsque la connexion atteignit une force sans précédent, une volonté étrangère parvint à exercer une influence sur le monde.

L'artefact survécut au désastre.

Les derniers gardiens de la Première Ère n'osèrent pas le détruire.

Ils l'enfermèrent derrière quatre chambres et confièrent sa protection à des machines qui continueraient d'obéir même après la disparition de leurs créateurs.
""")

entry('lore.event.first_eclipse', 'La Nuit de la première chute', """
Les récits anciens décrivent un ciel assombri et des créatures qui cessaient d'obéir à leurs propres instincts.

Les routes devinrent peu sûres.

Les défenses bâties pour arrêter les invasions ordinaires se révélèrent insuffisantes.

Les guildes mobilisèrent leurs maîtres et leurs combattants.

Les chercheurs tentèrent de refermer la connexion qu'ils avaient ouverte.

Le phénomène prit fin avant que ce qui restait derrière le Voile ne parvienne à le franchir.

Mais la Première Ère ne retrouva jamais tout à fait sa splendeur d'antan.

Les querelles s'intensifièrent, les routes furent abandonnées et les archives dispersées.

Pendant des générations, les survivants garderaient le souvenir de cet événement sous un seul nom :

La Première Éclipse.
""")

entry('lore.temple.last_mandate', 'Le Dernier Mandat', """
Le sanctuaire n'avait pas été bâti pour accorder des récompenses à ceux qui se montreraient assez forts.

Il avait été préparé pour empêcher que le savoir le plus dangereux de la Première Ère soit de nouveau utilisé.

Les mécanismes d'accès furent sécurisés.

On ordonna à un Automate de garder les couloirs.

Le Gardien demeura auprès des chambres intérieures, muni des quatre clés qui commandaient leurs serrures.

Aucune machine ne devait interpréter les intentions des visiteurs.

Elles devaient obéir.

Les derniers gardiens humains disparurent, mais leurs ordres continuèrent d'être exécutés.
""")

entry('lore.event.current_eclipse', 'Le Regard du Prisonnier', """
La flamme consuma la relique et un signal traversa le Voile.

Le monde sembla s'arrêter un instant.

Puis vinrent les ténèbres.

Les créatures se mirent à se comporter de façon extraordinaire. Certaines poursuivaient les aventuriers avec une persévérance inhabituelle ; d'autres semblaient franchir des obstacles qui les auraient jadis arrêtées.

Aucune armée ne descendait d'un autre plan.

La présence se servait de ce qui habitait déjà le monde.

Pour la première fois depuis les événements de la Première Ère, la volonté emprisonnée avait trouvé une nouvelle occasion d'étendre son influence.
""")

entry('lore.event.silent_fracture', 'La Fracture silencieuse', """
Le ciel retrouva son apparence.

Les poursuivants mirent fin à leur chasse et les feux de camp brûlèrent comme avant.

Les survivants crurent que la menace avait disparu.

Mais le confinement n'était pas revenu à son état d'origine.

Dans une région du Voile, une altération persistait qui n'existait pas avant l'événement.

La prison restait close.

Son occupant restait isolé.

Pourtant, l'une des frontières qui tenaient depuis la Grande Séparation s'était affaiblie.

L'Éclipse avait pris fin.

Ses conséquences, non.
""")

entry('lore.arc2.unfinished_oath', 'Le Serment inachevé', """
Les Gardiens des Sceaux soutenaient qu'il fallait préserver le confinement.

Les Disciples du Seuil maintenaient qu'aucune prison ne pouvait se comprendre uniquement par la peur qu'inspirait son occupant.

Les premiers avaient été témoins des conséquences du contact.

Les seconds avaient trouvé des indices que le confinement était né de la séparation naturelle des mondes.

Les deux traditions détenaient d'importantes vérités.

Aucune ne connaissait toute l'histoire.

La Fracture existait toujours.

Il n'y avait aucune méthode éprouvée pour la réparer, mais pas de raison suffisante de l'ouvrir non plus.

Les derniers documents laissèrent un avertissement :

Avant de décider du sort de ce qui reste confiné, il faudra comprendre pourquoi l'univers l'a séparé du reste de ses mondes.
""")

entry('lore.bestiary.errante_velado', None, """
Tous les morts ne reviennent pas sous leur ancienne forme.

Certains semblent traîner des fragments de ce qu'ils furent, comme si leur existence avait été prise au piège entre deux états.

L'Errant voilé dégage une présence troublante, dissimulée sous les restes de vêtements décomposés et un voile spectral.

Les érudits de la Première Ère y auraient reconnu les signes d'une manifestation incomplète de l'Essence.

Nous ignorons encore s'il garde des souvenirs de sa vie passée.

Nous savons seulement que quelque chose demeure.
""")

entry('lore.bestiary.osario_quebrado', None, """
Les anciens chercheurs observèrent que certains restes pouvaient conserver des manifestations extraordinaires même après la disparition de tout signe de vie reconnaissable.

L'Ossuaire brisé représente l'un de ces phénomènes.

Son existence soulève une question qui déconcertait les maîtres de la Première Ère :

L'Essence peut-elle maintenir ensemble des restes matériels sans conscience pour les gouverner ?

Les archives n'offrent aucune réponse définitive.
""")

entry('lore.bestiary.ancient_automaton', None, """
Les ingénieurs de la Première Ère construisirent des machines capables d'exécuter des ordres longtemps après que leurs opérateurs avaient abandonné les installations.

L'Automate ancien témoigne de ce savoir.

Il ne combat ni par haine ni par ambition.

Il reste là où ses bâtisseurs l'ont laissé, remplissant une fonction dont l'importance s'est peut-être perdue au fil des siècles.

Sa défaite ne signifie pas nécessairement la fin d'une menace maléfique.

Elle peut signifier qu'une antique défense a cessé de fonctionner.
""")

entry('lore.bestiary.custodio_cuatro_llaves', None, """
Le Gardien fut conçu pour protéger les mécanismes d'un sanctuaire exceptionnel.

Les quatre clés liées à sa structure commandaient l'accès aux chambres intérieures.

Il ne juge pas les intentions de ceux qui s'approchent.

Il ne distingue pas un voleur d'un aventurier en quête de réponses.

Sa fonction est de barrer le chemin.

Pendant des générations, il a exécuté cet ordre.

Celui qui parviendra à le vaincre découvrira que certaines portes avaient été fermées pour des raisons très différentes de la garde d'un trésor.
""")

entry('lore.bestiary.escorificador_hierro', None, """
Le Scorificateur de fer fait partie des menaces qui accompagnent les incursions venues du Nether et de celles qui peuplent ses territoires.

Sa présence dans le monde principal rappelle que les frontières dimensionnelles peuvent être altérées par des connexions extraordinaires.

Toute fracture du Voile ne mène pas aux lieux qu'aucun chercheur n'a jamais atteints.

Certaines ouvrent des chemins vers des régions que les aventuriers connaissent déjà, bien que leurs habitants n'appartiennent pas au monde qu'ils ont traversé.
""")

entry('lore.event.soul_fog_chronicle', 'Brume des âmes', """
Quand la brume recouvre les routes, les villageois se rappellent d'anciens récits de créatures revenues d'entre les morts.

Certains affirment que le phénomène a commencé après la chute de la Première Ère.

D'autres soutiennent qu'il existait avant les guildes.

Aucun témoignage n'a entièrement expliqué son origine.

Les chercheurs trouvèrent des indices de manifestations liées aux Échos de l'Essence, mais leurs documents sont restés incomplets.

La brume n'annonce pas nécessairement l'arrivée de ce que redoutaient les guildes.

Elle peut représenter une altération différente des frontières qui séparent la matière de certaines manifestations résiduelles.
""")

entry('lore.event.ember_rain_chronicle', 'Pluie de braises', """
Les portails permettent de franchir des distances que les lois ordinaires du monde garderaient séparées.

Mais certaines connexions peuvent perturber temporairement l'équilibre entre les dimensions.

La Pluie de braises est une manifestation de ce danger.

Des créatures du Nether surgissent dans le monde principal, apportant des menaces auxquelles bien des établissements ne sont pas préparés.

Le phénomène rappelle un enseignement oublié des chercheurs du Voile :

Ouvrir un chemin entre deux mondes ne revient pas à contrôler ce qui peut l'emprunter.
""")

entry('lore.event.eclipse_chronicle', 'Éclipse', """
Pendant la Première Éclipse, les maîtres de la Première Ère découvrirent qu'une volonté extérieure pouvait exercer une influence sur les créatures du monde.

Longtemps après, le sacrifice d'une offrande permet d'établir de nouveau cette connexion.

Les champions et les élites touchés acquièrent une extraordinaire capacité à traquer leurs cibles.

Le phénomène n'est pas la libération du Prisonnier.

C'est une manifestation temporaire de sa volonté.

Et, tandis que les aventuriers luttent pour survivre, quelque chose demeure caché derrière les frontières du Voile.
""")
