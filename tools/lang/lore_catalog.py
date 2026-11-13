"""Textos narrativos del Grimorio (Fase 2a), en español e inglés.

Fuente de los metadatos (dependencias, desbloqueo, estado): tools/lore/lore_entries.json.
Los textos de las Colecciones I y II son borradores del autor; aquí están con las
correcciones A-01 a A-10 aplicadas. El texto original sin cambios se conserva en
docs/lore/COLECCION_I_CORPUS.md y COLECCION_II_CORPUS.md.

pt_br, fr_fr y de_de reciben el texto en inglés como respaldo hasta que se traduzcan.
No se editan a mano los JSON de idioma: ejecuta gen_lore_lang.py.
"""

# id -> {'es': (título, cuerpo), 'en': (título, cuerpo)}. Los ids de criaturas no llevan título
# (el nombre sale de la entidad); su título es None.
ENTRIES = {}


def entry(entry_id, es, en):
    if entry_id in ENTRIES:
        raise ValueError(f'Entrada duplicada: {entry_id}')
    ENTRIES[entry_id] = {'es': es, 'en': en}


def text(value):
    return value.strip()


# ---------------------------------------------------------------- Colección I

entry('lore.cosmology.essence_within',
      ('La Esencia que Habita en Todos', text("""
Antes de que los gremios levantaran sus fortalezas, antes de que los maestros escribieran sus enseñanzas, los mortales ya poseían aquello que más tarde recibiría el nombre de Esencia.

No era un regalo de los dioses ni una fuerza reservada a unos pocos elegidos.

Habitaba naturalmente en los seres vivos.

Algunos aprendieron a fortalecer sus cuerpos. Otros descubrieron capacidades que parecían superar los límites de su naturaleza. Muchos jamás comprendieron qué despertaba aquellas posibilidades.

Los primeros maestros observaron que la práctica constante transformaba las capacidades de sus aprendices.

Así comenzó el estudio de las Aptitudes.

Los gremios no crearon aquel poder.

Aprendieron a desarrollarlo.
""")),
      ('The Essence That Dwells in All', text("""
Before the guilds raised their strongholds, before the masters wrote down their teachings, mortals already possessed what would later be called Essence.

It was no gift from the gods, nor a force reserved for a chosen few.

It dwelt naturally in living beings.

Some learned to strengthen their bodies. Others discovered abilities that seemed to exceed the limits of their nature. Many never understood what awakened those possibilities.

The first masters observed that constant practice transformed their apprentices' abilities.

Thus began the study of the Aptitudes.

The guilds did not create that power.

They learned to develop it.
""")))

entry('lore.guild.twenty_paths',
      ('Los Veinte Caminos', text("""
Durante generaciones, los maestros transmitieron sus técnicas sin pertenecer a una tradición común.

Cada comunidad desarrolló maneras diferentes de combatir, sobrevivir, construir y comprender el mundo.

Con el paso del tiempo surgieron grandes escuelas.

Veinte de aquellas tradiciones alcanzaron una influencia extraordinaria y fueron reconocidas por los gremios de la Primera Era.

Sus enseñanzas no eran idénticas.

Algunas defendían el dominio de las armas; otras buscaban comprender las fuerzas arcanas, el comportamiento de las criaturas o las posibilidades del conocimiento.

Sin embargo, todas compartían un principio: el poder debía cultivarse mediante experiencia.

Sus nombres sobrevivieron incluso después de que sus fortalezas desaparecieran.
""")),
      ('The Twenty Paths', text("""
For generations, masters passed down their techniques without belonging to a common tradition.

Each community developed different ways of fighting, surviving, building and understanding the world.

In time, great schools emerged.

Twenty of those traditions reached extraordinary influence and were recognized by the guilds of the First Era.

Their teachings were not identical.

Some championed mastery of arms; others sought to understand arcane forces, the behavior of creatures, or the possibilities of knowledge.

Yet all shared one principle: power must be cultivated through experience.

Their names survived even after their strongholds vanished.
""")))

# A-04 (renunciar a las otras sendas) y A-05 (Concilio convocado por la Concordia).
entry('lore.doctrine.three_doctrines',
      ('Las Tres Doctrinas', text("""
Los antiguos maestros descubrieron que una misma disciplina podía conducir hacia formas muy diferentes de maestría.

Aquellos que compartían un entrenamiento inicial comenzaban a distinguirse conforme desarrollaban sus capacidades.

Unos preferían la resistencia. Otros perfeccionaban la precisión, la adaptación o métodos que sus compañeros apenas comprendían.

El Concilio de las Tres Vías, convocado por la Concordia, reconoció tres grandes doctrinas para cada gremio.

Ninguna fue declarada superior.

Cada una exigía años de dedicación y una comprensión particular de las enseñanzas originales.

Elegir una senda significaba renunciar a las otras: la verdadera maestría requiere profundidad.
""")),
      ('The Three Doctrines', text("""
The ancient masters discovered that a single discipline could lead to very different forms of mastery.

Those who shared an initial training began to set themselves apart as they developed their abilities.

Some preferred endurance. Others perfected precision, adaptation, or methods their companions barely understood.

The Council of the Three Ways, convened by the Concord, recognized three great doctrines for each guild.

None was declared superior.

Each demanded years of dedication and a particular understanding of the original teachings.

Choosing a path meant giving up the others: true mastery requires depth.
""")))

entry('lore.chronology.council_three_ways',
      ('El Concilio de las Tres Vías', text("""
En los días de mayor prosperidad de la Primera Era, los representantes de los grandes gremios se reunieron para comparar sus métodos de enseñanza.

La diversidad de técnicas había crecido tanto que incluso los maestros de una misma disciplina discutían sobre el camino correcto.

El Concilio no resolvió aquellas diferencias proclamando vencedores.

Reconoció que una tradición podía contener varias respuestas igualmente legítimas.

Las doctrinas fueron ordenadas, sus enseñanzas registradas y sus técnicas transmitidas a nuevas generaciones.

Durante mucho tiempo, aquel acuerdo permitió que los gremios compartieran conocimiento sin renunciar a sus diferencias.

El nombre del Concilio sobrevivió más que las instituciones que lo convocaron.
""")),
      ('The Council of the Three Ways', text("""
In the days of the First Era's greatest prosperity, the representatives of the great guilds gathered to compare their teaching methods.

The diversity of techniques had grown so large that even masters of the same discipline argued over the right way.

The Council did not settle those differences by proclaiming victors.

It recognized that one tradition could contain several equally legitimate answers.

The doctrines were ordered, their teachings recorded, and their techniques handed down to new generations.

For a long time, that agreement allowed the guilds to share knowledge without giving up their differences.

The name of the Council outlived the institutions that convened it.
""")))

entry('lore.faction.concordance',
      ('La Concordia de los Gremios', text("""
La Primera Era conoció una época en que las veinte tradiciones colaboraban bajo una organización común.

La llamaban la Concordia.

Sus representantes compartían métodos de entrenamiento, herramientas y descubrimientos. Las rutas entre asentamientos permitían que los maestros viajaran y que los aprendices conocieran técnicas distintas a las de su lugar de origen.

La Concordia nunca consiguió eliminar todas las rivalidades.

Tampoco pretendió hacerlo.

Su propósito era impedir que las diferencias destruyeran el conocimiento que cada gremio había acumulado.

Pero hubo preguntas para las que ningún acuerdo resultó suficiente.

Las más peligrosas comenzaron cuando los investigadores decidieron estudiar aquello que permanecía después de la muerte.
""")),
      ('The Concord of the Guilds', text("""
The First Era knew a time when the twenty traditions worked together under a common organization.

They called it the Concord.

Its representatives shared training methods, tools and discoveries. The routes between settlements let masters travel and let apprentices learn techniques different from those of their homeland.

The Concord never managed to eliminate every rivalry.

Nor did it try.

Its purpose was to keep differences from destroying the knowledge each guild had accumulated.

But there were questions for which no agreement was enough.

The most dangerous began when researchers decided to study what remained after death.
""")))

entry('lore.cosmology.echoes',
      ('La Huella de los Caídos', text("""
Los primeros estudiosos de la muerte observaron que determinadas manifestaciones persistían después del último aliento.

No eran cuerpos vivos.

Tampoco podían describirse con certeza como conciencias completas.

Algunos investigadores las denominaron Ecos.

Su origen y permanencia desconcertaron a los maestros de la Primera Era.

Una parte de los gremios consideró que debían limitarse a estudiarlas.

Otros vieron en ellas la posibilidad de comprender aquello que separaba la vida de la muerte.

Aquella diferencia parecía insignificante.

Con el tiempo se convirtió en uno de los grandes desacuerdos de la civilización.
""")),
      ('The Footprint of the Fallen', text("""
The first scholars of death observed that certain manifestations persisted after the last breath.

They were not living bodies.

Nor could they be described with certainty as complete minds.

Some researchers called them Echoes.

Their origin and persistence baffled the masters of the First Era.

One part of the guilds held that they should only be studied.

Others saw in them the chance to understand what separated life from death.

That difference seemed insignificant.

In time it became one of the great disagreements of the civilization.
""")))

entry('lore.cosmology.veil_frontier',
      ('La Frontera Invisible', text("""
Los investigadores descubrieron que las dimensiones no estaban completamente aisladas.

Existían fenómenos capaces de atravesar determinadas fronteras, aunque la mayoría de los seres vivos jamás los percibiera.

Las llamaron, en conjunto, el Velo.

Al principio creyeron que todas sus regiones respondían a las mismas leyes.

Posteriormente descubrieron que algunas fronteras ofrecían una resistencia extraordinaria.

No parecían proteger una ciudad, una tumba ni un reino conocido.

Simplemente impedían el paso.

Nadie sabía qué se encontraba detrás.

Y, durante un tiempo, nadie consideró necesario descubrirlo.
""")),
      ('The Invisible Frontier', text("""
Researchers discovered that the dimensions were not completely isolated.

There were phenomena able to cross certain frontiers, though most living beings never perceived them.

They called them, together, the Veil.

At first they believed all its regions obeyed the same laws.

Later they discovered that some frontiers offered extraordinary resistance.

They did not seem to protect a city, a tomb or any known kingdom.

They simply barred the way.

No one knew what lay behind them.

And, for a time, no one thought it necessary to find out.
""")))

# A-06: «qué esperaba» en lugar de «alguien esperaba».
entry('lore.faction.permanence',
      ('Los Investigadores de la Permanencia', text("""
La mortalidad había sido aceptada durante generaciones como una condición inevitable.

Pero los descubrimientos sobre la persistencia de la Esencia despertaron nuevas preguntas.

¿Podía una conciencia conservarse después de la muerte?

¿Podía preservarse el conocimiento de alguien incluso cuando su cuerpo desaparecía?

Los investigadores del Cónclave buscaron respuestas.

No todos deseaban alcanzar la inmortalidad.

Algunos pretendían comprender el destino de la memoria. Otros esperaban impedir que generaciones enteras de conocimiento se perdieran.

Cuando detectaron señales procedentes de una región inaccesible del Velo, creyeron haber encontrado un indicio de aquello que buscaban.

Todavía ignoraban qué esperaba al otro lado.
""")),
      ('The Researchers of Permanence', text("""
Mortality had been accepted for generations as an unavoidable condition.

But the discoveries about the persistence of Essence raised new questions.

Could a consciousness be preserved after death?

Could someone's knowledge be preserved even when their body was gone?

The researchers of the Conclave sought answers.

Not all of them wished to reach immortality.

Some meant to understand the fate of memory. Others hoped to keep whole generations of knowledge from being lost.

When they detected signals coming from an inaccessible region of the Veil, they believed they had found a sign of what they sought.

They still did not know what waited on the other side.
""")))

entry('lore.chronology.first_answer',
      ('La Primera Respuesta', text("""
Los instrumentos registraron una señal que no pudieron explicar.

Los investigadores repitieron el experimento.

La señal volvió.

Comenzaron a formular preguntas y descubrieron patrones que parecían responder a ellas.

Pronto llegaron respuestas que contenían conocimientos desconocidos.

Algunos miembros del Cónclave afirmaron que habían encontrado una conciencia capaz de sobrevivir sin un cuerpo.

Otros sospecharon que aquella presencia nunca había sido humana.

Los experimentos continuaron.

Ningún documento conservado indica que la entidad exigiera construir un mecanismo de comunicación.

Pero las respuestas fueron guiando a los investigadores hacia esa posibilidad.

Y ellos decidieron seguirlas.
""")),
      ('The First Answer', text("""
The instruments registered a signal they could not explain.

The researchers repeated the experiment.

The signal returned.

They began to pose questions and discovered patterns that seemed to answer them.

Soon answers arrived containing unknown knowledge.

Some members of the Conclave claimed to have found a consciousness able to survive without a body.

Others suspected that presence had never been human.

The experiments continued.

No surviving document indicates that the entity demanded a communication mechanism be built.

But the answers gradually guided the researchers toward that possibility.

And they chose to follow them.
""")))

entry('lore.artifact.sealed_artifact',
      ('El Artefacto Sellado', text("""
Los investigadores construyeron un objeto capaz de conservar una señal y amplificarla mediante Esencia.

Su propósito original era establecer una comunicación más intensa con la región desconocida del Velo.

El experimento superó todas sus previsiones.

Cuando la conexión alcanzó una fuerza sin precedentes, una voluntad ajena consiguió ejercer influencia sobre el mundo.

El artefacto sobrevivió al desastre.

Los últimos guardianes de la Primera Era no se atrevieron a destruirlo.

Lo encerraron tras cuatro cámaras y confiaron su protección a máquinas que continuarían obedeciendo incluso cuando sus creadores hubieran desaparecido.
""")),
      ('The Sealed Artifact', text("""
The researchers built an object capable of holding a signal and amplifying it through Essence.

Its original purpose was to establish more intense communication with the unknown region of the Veil.

The experiment exceeded all expectations.

When the connection reached unprecedented strength, an alien will managed to exert influence over the world.

The artifact survived the disaster.

The last guardians of the First Era did not dare destroy it.

They shut it away behind four chambers and entrusted its protection to machines that would keep obeying even after their creators were gone.
""")))

entry('lore.event.first_eclipse',
      ('La Noche de la Primera Caída', text("""
Los antiguos relatos describen un cielo oscurecido y criaturas que dejaron de obedecer a sus propios instintos.

Los caminos se volvieron inseguros.

Las defensas construidas para detener invasiones ordinarias resultaron insuficientes.

Los gremios movilizaron a sus maestros y combatientes.

Los investigadores intentaron cerrar la conexión que habían abierto.

El fenómeno terminó antes de que aquello que permanecía detrás del Velo consiguiera atravesarlo.

Pero la Primera Era nunca recuperó completamente su antiguo esplendor.

Las disputas se intensificaron, las rutas fueron abandonadas y los archivos quedaron dispersos.

Durante generaciones, los supervivientes recordarían aquel acontecimiento con un solo nombre:

El Primer Eclipse.
""")),
      ('The Night of the First Fall', text("""
The ancient accounts describe a darkened sky and creatures that stopped obeying their own instincts.

The roads became unsafe.

The defenses built to stop ordinary invasions proved insufficient.

The guilds mobilized their masters and fighters.

The researchers tried to close the connection they had opened.

The phenomenon ended before what remained behind the Veil managed to cross it.

But the First Era never fully recovered its former splendor.

Disputes intensified, routes were abandoned and archives were scattered.

For generations, the survivors would remember that event by a single name:

The First Eclipse.
""")))

entry('lore.temple.last_mandate',
      ('El Último Mandato', text("""
El santuario no fue construido para conceder recompensas a quienes demostraran suficiente fuerza.

Fue preparado para impedir que el conocimiento más peligroso de la Primera Era volviera a utilizarse.

Los mecanismos de acceso fueron asegurados.

Un Autómata recibió instrucciones para proteger los corredores.

El Custodio permaneció junto a las cámaras interiores, equipado con las cuatro llaves que permitían controlar sus cierres.

Ninguna máquina debía interpretar las intenciones de los visitantes.

Debían obedecer.

Los últimos guardianes humanos desaparecieron, pero sus órdenes continuaron ejecutándose.
""")),
      ('The Last Mandate', text("""
The sanctuary was not built to grant rewards to those who proved strong enough.

It was prepared to keep the First Era's most dangerous knowledge from being used again.

The access mechanisms were secured.

An Automaton was instructed to guard the corridors.

The Custodian remained beside the inner chambers, equipped with the four keys that controlled their locks.

No machine was meant to interpret the intentions of visitors.

They were to obey.

The last human guardians vanished, but their orders went on being carried out.
""")))

entry('lore.event.current_eclipse',
      ('La Mirada del Prisionero', text("""
La llama consumió la reliquia y una señal atravesó el Velo.

El mundo pareció detenerse durante un instante.

Después llegó la oscuridad.

Las criaturas comenzaron a comportarse de manera extraordinaria. Algunas perseguían a los aventureros con una persistencia desconocida; otras parecían superar obstáculos que antes las habrían detenido.

No existía un ejército descendiendo de otro plano.

La presencia utilizaba aquello que ya habitaba en el mundo.

Por primera vez desde los acontecimientos de la Primera Era, la voluntad encerrada había encontrado otra oportunidad de extender su influencia.
""")),
      ('The Gaze of the Prisoner', text("""
The flame consumed the relic and a signal crossed the Veil.

The world seemed to stop for an instant.

Then came the darkness.

Creatures began to behave in extraordinary ways. Some pursued adventurers with unfamiliar persistence; others seemed to overcome obstacles that would once have stopped them.

There was no army descending from another plane.

The presence made use of what already dwelled in the world.

For the first time since the events of the First Era, the imprisoned will had found another chance to extend its influence.
""")))

entry('lore.event.silent_fracture',
      ('La Fractura Silenciosa', text("""
El cielo recuperó su apariencia.

Los perseguidores cesaron su búsqueda y las Hogueras volvieron a arder como antes.

Los supervivientes creyeron que la amenaza había desaparecido.

Pero el confinamiento no había recuperado su estado original.

En una región del Velo persistía una alteración que no existía antes del acontecimiento.

La prisión permanecía cerrada.

Su ocupante continuaba aislado.

Sin embargo, una de las fronteras que habían resistido desde la Gran Separación había quedado debilitada.

El Eclipse había terminado.

Sus consecuencias, no.
""")),
      ('The Silent Fracture', text("""
The sky regained its appearance.

The pursuers ended their hunt and the Bonfires burned as before.

The survivors believed the threat had vanished.

But the confinement had not returned to its original state.

In a region of the Veil, an alteration persisted that had not existed before the event.

The prison remained closed.

Its occupant remained isolated.

Yet one of the frontiers that had held since the Great Separation had been weakened.

The Eclipse had ended.

Its consequences had not.
""")))

# A-03: Vigilantes de los Sellos en lugar de Herederos.
entry('lore.arc2.unfinished_oath',
      ('El Juramento Inconcluso', text("""
Los Vigilantes de los Sellos afirmaban que el confinamiento debía preservarse.

Los Discípulos del Umbral sostenían que ninguna prisión podía comprenderse únicamente a través del miedo que provocaba su ocupante.

Los primeros habían presenciado las consecuencias del contacto.

Los segundos habían encontrado indicios de que el confinamiento nació con la separación natural de los mundos.

Ambas tradiciones conservaron verdades importantes.

Ninguna conocía la historia completa.

La Fractura continuaba existiendo.

No había un método demostrado para repararla, pero tampoco una razón suficiente para abrirla.

Los últimos documentos dejaban una advertencia:

Antes de decidir el destino de aquello que permanece encerrado, será necesario comprender por qué el universo lo separó del resto de sus mundos.
""")),
      ('The Unfinished Oath', text("""
The Wardens of the Seals held that the confinement must be preserved.

The Disciples of the Threshold maintained that no prison could be understood solely through the fear its occupant inspired.

The former had witnessed the consequences of the contact.

The latter had found signs that the confinement was born of the natural separation of the worlds.

Both traditions kept important truths.

Neither knew the whole story.

The Fracture still existed.

There was no proven method to repair it, but no sufficient reason to open it either.

The last documents left a warning:

Before deciding the fate of what remains confined, it will be necessary to understand why the universe set it apart from the rest of its worlds.
""")))

# --------------------------------------------------------------- Colección II
# Criaturas: solo cuerpo (título None). A-08: sin notas de trabajo.

entry('lore.bestiary.errante_velado',
      (None, text("""
No todos los muertos regresan conservando su antigua forma.

Algunos parecen arrastrar consigo fragmentos de aquello que fueron, como si su existencia hubiera quedado atrapada entre dos estados.

El Errante Velado manifiesta una presencia inquietante, oculta bajo restos de vestimentas deterioradas y un velo espectral.

Los estudiosos de la Primera Era habrían reconocido en él indicios de una manifestación incompleta de Esencia.

Todavía no sabemos si conserva recuerdos de su vida anterior.

Solo sabemos que algo permanece.
""")),
      (None, text("""
Not all the dead return in their old form.

Some seem to drag along fragments of what they were, as if their existence had been trapped between two states.

The Veiled Wanderer carries an unsettling presence, hidden beneath the remains of decayed garments and a spectral veil.

The scholars of the First Era would have recognized in it signs of an incomplete manifestation of Essence.

We still do not know whether it keeps memories of its former life.

We only know that something remains.
""")))

entry('lore.bestiary.osario_quebrado',
      (None, text("""
Los antiguos investigadores observaron que ciertos restos podían conservar manifestaciones extraordinarias incluso después de que hubiera desaparecido toda señal reconocible de vida.

El Osario Quebrado representa uno de esos fenómenos.

Su existencia plantea una pregunta que desconcertó a los maestros de la Primera Era:

¿Puede la Esencia mantener unidos restos materiales sin que exista una conciencia que los gobierne?

Los registros no ofrecen una respuesta definitiva.
""")),
      (None, text("""
The ancient researchers observed that certain remains could retain extraordinary manifestations even after every recognizable sign of life had vanished.

The Broken Ossuary represents one of those phenomena.

Its existence raises a question that baffled the masters of the First Era:

Can Essence hold material remains together without a consciousness to govern them?

The records offer no definitive answer.
""")))

entry('lore.bestiary.ancient_automaton',
      (None, text("""
Los ingenieros de la Primera Era construyeron máquinas capaces de ejecutar órdenes mucho después de que sus operadores abandonaran las instalaciones.

El Autómata Antiguo es un testimonio de aquellos conocimientos.

No combate por odio ni por ambición.

Permanece donde sus constructores lo dejaron, cumpliendo una función cuya importancia puede haberse perdido con los siglos.

Su derrota no representa necesariamente el fin de una amenaza maligna.

Puede significar que una defensa antigua ha dejado de funcionar.
""")),
      (None, text("""
The engineers of the First Era built machines able to carry out orders long after their operators had abandoned the facilities.

The Ancient Automaton stands as a testament to that knowledge.

It does not fight out of hatred or ambition.

It remains where its builders left it, fulfilling a function whose importance may have been lost over the centuries.

Its defeat does not necessarily mean the end of an evil threat.

It may mean that an ancient defense has ceased to function.
""")))

# La entrada de bestiario del Custodio NO existe todavía en BestiaryRules (D-10).
entry('lore.bestiary.custodio_cuatro_llaves',
      (None, text("""
El Custodio fue concebido para proteger los mecanismos de un santuario excepcional.

Las cuatro llaves vinculadas a su estructura permitían controlar el acceso a las cámaras interiores.

No juzga las intenciones de quienes se aproximan.

No distingue entre un ladrón y un aventurero que busca respuestas.

Su función consiste en impedir el acceso.

Durante generaciones cumplió esa orden.

Quien consiga derrotarlo descubrirá que algunas puertas fueron cerradas por razones muy distintas a la conservación de un tesoro.
""")),
      (None, text("""
The Custodian was conceived to protect the mechanisms of an exceptional sanctuary.

The four keys bound to its structure controlled access to the inner chambers.

It does not judge the intentions of those who approach.

It does not tell a thief from an adventurer seeking answers.

Its function is to bar the way.

For generations it carried out that order.

Whoever manages to defeat it will discover that some doors were closed for reasons very different from guarding a treasure.
""")))

# A-09 (sin «prisión primordial») y A-10 (también habita en el Nether).
entry('lore.bestiary.escorificador_hierro',
      (None, text("""
El Escorificador de Hierro pertenece a las amenazas que acompañan las irrupciones procedentes del Nether y a las que habitan sus territorios.

Su presencia en el Overworld recuerda que las fronteras dimensionales pueden ser alteradas mediante conexiones extraordinarias.

No toda fractura del Velo conduce a los lugares que ningún investigador alcanzó.

Algunas abren caminos hacia regiones que los aventureros ya conocen, aunque sus habitantes no pertenezcan al mundo que han atravesado.
""")),
      (None, text("""
The Iron Slagger belongs to the threats that accompany the incursions from the Nether and to those that dwell in its territories.

Its presence in the Overworld is a reminder that dimensional frontiers can be altered through extraordinary connections.

Not every fracture of the Veil leads to the places no researcher ever reached.

Some open paths to regions adventurers already know, though their inhabitants do not belong to the world they have crossed.
""")))

# Crónicas de eventos (separadas del contador de criaturas). Títulos = nombres oficiales.
# A-09: sin «Conciencia Ancestral» en la Niebla.
entry('lore.event.soul_fog_chronicle',
      ('Niebla de almas', text("""
Cuando la niebla cubre los caminos, los aldeanos recuerdan antiguos relatos sobre criaturas que regresan de la muerte.

Algunos afirman que el fenómeno comenzó después de la caída de la Primera Era.

Otros aseguran que ya existía antes de los gremios.

Ningún testimonio ha logrado explicar por completo su origen.

Los investigadores encontraron indicios de manifestaciones relacionadas con los Ecos de Esencia, pero sus documentos quedaron incompletos.

La niebla no anuncia necesariamente la llegada de aquello que los gremios temieron.

Puede representar una alteración diferente de las fronteras que separan la materia y ciertas manifestaciones residuales.
""")),
      ('Soul Fog', text("""
When the fog covers the roads, villagers recall ancient tales of creatures that return from death.

Some claim the phenomenon began after the fall of the First Era.

Others insist it existed before the guilds.

No testimony has fully explained its origin.

Researchers found signs of manifestations related to the Echoes of Essence, but their documents were left incomplete.

The fog does not necessarily announce the arrival of what the guilds feared.

It may represent a different alteration of the frontiers that separate matter from certain residual manifestations.
""")))

entry('lore.event.ember_rain_chronicle',
      ('Lluvia de brasas', text("""
Los portales permiten atravesar distancias que las leyes ordinarias del mundo mantendrían separadas.

Pero algunas conexiones pueden alterar temporalmente el equilibrio entre dimensiones.

La Lluvia de Brasas es una manifestación de ese peligro.

Criaturas procedentes del Nether irrumpen en el Overworld, llevando consigo amenazas para las que muchos asentamientos no están preparados.

El fenómeno recuerda una enseñanza olvidada de los investigadores del Velo:

Abrir un camino entre dos mundos no significa controlar aquello que puede recorrerlo.
""")),
      ('Ember Rain', text("""
Portals allow crossing distances that the ordinary laws of the world would keep apart.

But some connections can temporarily upset the balance between dimensions.

The Ember Rain is a manifestation of that danger.

Creatures from the Nether burst into the Overworld, bringing threats many settlements are not prepared for.

The phenomenon recalls a forgotten teaching of the researchers of the Veil:

Opening a path between two worlds does not mean controlling what may travel it.
""")))

# A-07: «una ofrenda» en lugar de «una reliquia antigua».
entry('lore.event.eclipse_chronicle',
      ('Eclipse', text("""
Durante el Primer Eclipse, los maestros de la Primera Era descubrieron que una voluntad externa podía ejercer influencia sobre criaturas del mundo.

Mucho tiempo después, el sacrificio de una ofrenda permite que aquella conexión vuelva a establecerse.

Los campeones y élites afectados adquieren una capacidad extraordinaria para buscar a sus objetivos.

El fenómeno no constituye la liberación del Prisionero.

Es una manifestación temporal de su voluntad.

Y, mientras los aventureros intentan sobrevivir, algo permanece oculto detrás de las fronteras del Velo.
""")),
      ('Eclipse', text("""
During the First Eclipse, the masters of the First Era discovered that an external will could exert influence over creatures of the world.

Long afterward, the sacrifice of an offering allows that connection to be established again.

The champions and elites affected gain an extraordinary ability to seek out their targets.

The phenomenon is not the liberation of the Prisoner.

It is a temporary manifestation of its will.

And, while adventurers struggle to survive, something remains hidden behind the frontiers of the Veil.
""")))
