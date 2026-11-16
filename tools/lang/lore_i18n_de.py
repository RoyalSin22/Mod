"""Textos narrativos del Grimorio en alemán (de_de). Traducción del inglés de lore_catalog.py.

Glosario (coherente con el resto del mod): Essenz, Gilden, Schleier, Leuchtfeuer, Eintracht, Konklave, Schwelle, Erste Ära,
Finsternis (Eclipse), Zerbrochenes Beinhaus, Verschleierter Wanderer, Uralter Automat, Wächter (Custodian), Eisenschlacker,
Oberwelt. Para los guardianes humanos genéricos se usa «Hüter», que distingue de «Wächter» (el Custodio y los
Wächter der Siegel). Cada entrada conserva los mismos párrafos, en el mismo orden, que el texto inglés. Las criaturas
no llevan título.
"""

TEXTS = {}


def entry(entry_id, title, body):
    TEXTS[entry_id] = (title, body.strip())


entry('lore.cosmology.essence_within', 'Die Essenz, die in allen wohnt', """
Bevor die Gilden ihre Festungen errichteten, bevor die Meister ihre Lehren niederschrieben, besaßen die Sterblichen bereits das, was später Essenz genannt werden sollte.

Sie war kein Geschenk der Götter und auch keine Kraft, die wenigen Auserwählten vorbehalten war.

Sie wohnte von Natur aus in den Lebewesen.

Manche lernten, ihren Körper zu stärken. Andere entdeckten Fähigkeiten, die die Grenzen ihrer Natur zu übersteigen schienen. Viele begriffen nie, was diese Möglichkeiten weckte.

Die ersten Meister beobachteten, dass beständige Übung die Fähigkeiten ihrer Lehrlinge verwandelte.

So begann das Studium der Aptitudes.

Die Gilden haben diese Macht nicht erschaffen.

Sie lernten, sie zu entwickeln.
""")

entry('lore.guild.twenty_paths', 'Die zwanzig Pfade', """
Über Generationen gaben Meister ihre Techniken weiter, ohne einer gemeinsamen Tradition anzugehören.

Jede Gemeinschaft entwickelte eigene Arten zu kämpfen, zu überleben, zu bauen und die Welt zu verstehen.

Mit der Zeit entstanden große Schulen.

Zwanzig dieser Traditionen erlangten außerordentlichen Einfluss und wurden von den Gilden der Ersten Ära anerkannt.

Ihre Lehren waren nicht identisch.

Einige traten für die Meisterschaft der Waffen ein; andere suchten die arkanen Kräfte, das Verhalten der Kreaturen oder die Möglichkeiten des Wissens zu ergründen.

Doch alle teilten einen Grundsatz: Macht muss durch Erfahrung gepflegt werden.

Ihre Namen überdauerten selbst das Verschwinden ihrer Festungen.
""")

entry('lore.doctrine.three_doctrines', 'Die drei Lehren', """
Die alten Meister entdeckten, dass dieselbe Disziplin zu ganz unterschiedlichen Formen der Meisterschaft führen konnte.

Wer eine gemeinsame Grundausbildung teilte, begann sich abzuheben, je weiter er seine Fähigkeiten entwickelte.

Manche zogen Ausdauer vor. Andere vervollkommneten Präzision, Anpassung oder Methoden, die ihre Gefährten kaum verstanden.

Der Rat der Drei Wege, von der Eintracht einberufen, erkannte für jede Gilde drei große Lehren an.

Keine wurde für überlegen erklärt.

Jede verlangte jahrelange Hingabe und ein besonderes Verständnis der ursprünglichen Lehren.

Einen Pfad zu wählen hieß, die anderen aufzugeben: Wahre Meisterschaft verlangt Tiefe.
""")

entry('lore.chronology.council_three_ways', 'Der Rat der Drei Wege', """
In den Tagen größten Wohlstands der Ersten Ära versammelten sich die Vertreter der großen Gilden, um ihre Lehrmethoden zu vergleichen.

Die Vielfalt der Techniken war so gewachsen, dass selbst Meister derselben Disziplin über den rechten Weg stritten.

Der Rat löste diese Unterschiede nicht, indem er Sieger ausrief.

Er erkannte an, dass eine Tradition mehrere gleichermaßen berechtigte Antworten enthalten konnte.

Die Lehren wurden geordnet, ihre Unterweisungen aufgezeichnet und ihre Techniken an neue Generationen weitergegeben.

Lange Zeit erlaubte diese Übereinkunft den Gilden, Wissen zu teilen, ohne ihre Unterschiede aufzugeben.

Der Name des Rates überdauerte die Einrichtungen, die ihn einberufen hatten.
""")

entry('lore.faction.concordance', 'Die Eintracht der Gilden', """
Die Erste Ära kannte eine Zeit, in der die zwanzig Traditionen unter einer gemeinsamen Organisation zusammenarbeiteten.

Man nannte sie die Eintracht.

Ihre Vertreter teilten Ausbildungsmethoden, Werkzeuge und Entdeckungen. Die Wege zwischen den Siedlungen erlaubten Meistern zu reisen und Lehrlingen, andere Techniken als die ihrer Heimat zu erlernen.

Die Eintracht vermochte nie, alle Rivalitäten zu beseitigen.

Sie versuchte es auch nicht.

Ihr Zweck war es, zu verhindern, dass Unterschiede das Wissen zerstörten, das jede Gilde angesammelt hatte.

Doch es gab Fragen, für die keine Übereinkunft genügte.

Die gefährlichsten begannen, als Forscher beschlossen, zu erkunden, was nach dem Tod zurückblieb.
""")

entry('lore.cosmology.echoes', 'Die Spur der Gefallenen', """
Die ersten Gelehrten des Todes beobachteten, dass bestimmte Erscheinungen den letzten Atemzug überdauerten.

Es waren keine lebenden Körper.

Und man konnte sie auch nicht mit Sicherheit als vollständige Geister beschreiben.

Manche Forscher nannten sie Echos.

Ihr Ursprung und ihr Fortbestehen verblüfften die Meister der Ersten Ära.

Ein Teil der Gilden vertrat, man solle sie nur studieren.

Andere sahen in ihnen die Gelegenheit, zu verstehen, was das Leben vom Tod trennte.

Dieser Unterschied schien unbedeutend.

Mit der Zeit wurde er zu einem der großen Zerwürfnisse der Zivilisation.
""")

entry('lore.cosmology.veil_frontier', 'Die unsichtbare Grenze', """
Forscher entdeckten, dass die Dimensionen nicht völlig voneinander abgeschlossen waren.

Es gab Phänomene, die bestimmte Grenzen überschreiten konnten, auch wenn die meisten Lebewesen sie nie wahrnahmen.

Sie nannten sie gemeinsam den Schleier.

Zuerst glaubten sie, alle seine Regionen gehorchten denselben Gesetzen.

Später entdeckten sie, dass manche Grenzen außerordentlichen Widerstand leisteten.

Sie schienen weder eine Stadt noch ein Grab noch ein bekanntes Königreich zu schützen.

Sie versperrten einfach den Weg.

Niemand wusste, was dahinter lag.

Und eine Zeit lang hielt es niemand für nötig, es herauszufinden.
""")

entry('lore.faction.permanence', 'Die Forscher der Beständigkeit', """
Die Sterblichkeit war über Generationen als unausweichlicher Zustand hingenommen worden.

Doch die Entdeckungen über das Fortbestehen der Essenz warfen neue Fragen auf.

Konnte ein Bewusstsein nach dem Tod bewahrt werden?

Konnte das Wissen eines Menschen erhalten bleiben, selbst wenn sein Körper nicht mehr war?

Die Forscher des Konklaves suchten Antworten.

Nicht alle wollten Unsterblichkeit erlangen.

Manche wollten das Schicksal der Erinnerung verstehen. Andere hofften, zu verhindern, dass ganze Generationen von Wissen verloren gingen.

Als sie Signale aus einer unzugänglichen Region des Schleiers entdeckten, glaubten sie, ein Zeichen für das Gesuchte gefunden zu haben.

Sie wussten noch nicht, was auf der anderen Seite wartete.
""")

entry('lore.chronology.first_answer', 'Die erste Antwort', """
Die Instrumente zeichneten ein Signal auf, das sie sich nicht erklären konnten.

Die Forscher wiederholten das Experiment.

Das Signal kehrte zurück.

Sie begannen, Fragen zu stellen, und entdeckten Muster, die darauf zu antworten schienen.

Bald trafen Antworten ein, die unbekanntes Wissen enthielten.

Einige Mitglieder des Konklaves behaupteten, ein Bewusstsein gefunden zu haben, das ohne Körper überleben konnte.

Andere vermuteten, dass diese Präsenz nie menschlich gewesen war.

Die Experimente gingen weiter.

Kein erhaltenes Dokument deutet darauf hin, dass die Wesenheit den Bau eines Kommunikationsmechanismus verlangt hätte.

Doch die Antworten führten die Forscher allmählich zu dieser Möglichkeit.

Und sie beschlossen, ihnen zu folgen.
""")

entry('lore.artifact.sealed_artifact', 'Das versiegelte Artefakt', """
Die Forscher bauten einen Gegenstand, der ein Signal halten und durch Essenz verstärken konnte.

Sein ursprünglicher Zweck war es, eine intensivere Verbindung zur unbekannten Region des Schleiers herzustellen.

Das Experiment übertraf alle Erwartungen.

Als die Verbindung eine nie dagewesene Stärke erreichte, gelang es einem fremden Willen, Einfluss auf die Welt auszuüben.

Das Artefakt überstand die Katastrophe.

Die letzten Hüter der Ersten Ära wagten nicht, es zu zerstören.

Sie sperrten es hinter vier Kammern ein und vertrauten seinen Schutz Maschinen an, die auch dann weiter gehorchen würden, wenn ihre Schöpfer längst fort wären.
""")

entry('lore.event.first_eclipse', 'Die Nacht des ersten Falls', """
Die alten Berichte beschreiben einen verdunkelten Himmel und Kreaturen, die aufhörten, ihren eigenen Instinkten zu folgen.

Die Wege wurden unsicher.

Die Verteidigungen, die gewöhnliche Invasionen aufhalten sollten, erwiesen sich als unzureichend.

Die Gilden mobilisierten ihre Meister und Kämpfer.

Die Forscher versuchten, die Verbindung zu schließen, die sie geöffnet hatten.

Das Phänomen endete, bevor das, was hinter dem Schleier verblieb, ihn überschreiten konnte.

Doch die Erste Ära erlangte ihren früheren Glanz nie ganz zurück.

Die Streitigkeiten verschärften sich, Routen wurden aufgegeben und Archive zerstreut.

Über Generationen würden die Überlebenden sich an jenes Ereignis unter einem einzigen Namen erinnern:

Die Erste Finsternis.
""")

entry('lore.temple.last_mandate', 'Das letzte Mandat', """
Das Heiligtum wurde nicht erbaut, um denen Belohnungen zu gewähren, die sich als stark genug erwiesen.

Es wurde vorbereitet, damit das gefährlichste Wissen der Ersten Ära nie wieder genutzt werde.

Die Zugangsmechanismen wurden gesichert.

Ein Automat erhielt den Befehl, die Gänge zu bewachen.

Der Wächter blieb bei den inneren Kammern, ausgestattet mit den vier Schlüsseln, die deren Schlösser beherrschten.

Keine Maschine sollte die Absichten der Besucher deuten.

Sie sollten gehorchen.

Die letzten menschlichen Hüter verschwanden, doch ihre Befehle wurden weiter ausgeführt.
""")

entry('lore.event.current_eclipse', 'Der Blick des Gefangenen', """
Die Flamme verzehrte die Reliquie, und ein Signal durchquerte den Schleier.

Die Welt schien für einen Augenblick stillzustehen.

Dann kam die Dunkelheit.

Kreaturen begannen, sich außergewöhnlich zu verhalten. Manche verfolgten Abenteurer mit ungewohnter Beharrlichkeit; andere schienen Hindernisse zu überwinden, die sie einst aufgehalten hätten.

Es stieg kein Heer aus einer anderen Ebene herab.

Die Präsenz bediente sich dessen, was bereits in der Welt wohnte.

Zum ersten Mal seit den Ereignissen der Ersten Ära hatte der eingesperrte Wille eine neue Gelegenheit gefunden, seinen Einfluss auszudehnen.
""")

entry('lore.event.silent_fracture', 'Der stille Bruch', """
Der Himmel gewann sein Aussehen zurück.

Die Verfolger beendeten ihre Jagd, und die Leuchtfeuer brannten wie zuvor.

Die Überlebenden glaubten, die Bedrohung sei verschwunden.

Doch die Einschließung war nicht in ihren ursprünglichen Zustand zurückgekehrt.

In einer Region des Schleiers blieb eine Veränderung bestehen, die es vor dem Ereignis nicht gegeben hatte.

Das Gefängnis blieb verschlossen.

Sein Insasse blieb abgeschnitten.

Doch eine der Grenzen, die seit der Großen Trennung gehalten hatten, war geschwächt.

Die Finsternis war vorüber.

Ihre Folgen nicht.
""")

entry('lore.arc2.unfinished_oath', 'Der unvollendete Eid', """
Die Wächter der Siegel hielten daran fest, dass die Einschließung bewahrt werden müsse.

Die Jünger der Schwelle vertraten, dass sich kein Gefängnis allein aus der Furcht erklären lasse, die sein Insasse einflößte.

Die Ersten hatten die Folgen des Kontakts miterlebt.

Die Zweiten hatten Hinweise gefunden, dass die Einschließung aus der natürlichen Trennung der Welten entstanden war.

Beide Traditionen bewahrten wichtige Wahrheiten.

Keine kannte die ganze Geschichte.

Der Bruch bestand weiter.

Es gab keine erprobte Methode, ihn zu heilen, aber auch keinen hinreichenden Grund, ihn zu öffnen.

Die letzten Dokumente hinterließen eine Warnung:

Bevor über das Schicksal dessen entschieden wird, was eingeschlossen bleibt, muss verstanden werden, warum das Universum es von den übrigen Welten abgesondert hat.
""")

entry('lore.bestiary.errante_velado', None, """
Nicht alle Toten kehren in ihrer alten Gestalt zurück.

Manche scheinen Bruchstücke dessen mit sich zu schleppen, was sie waren, als wäre ihr Dasein zwischen zwei Zuständen gefangen.

Der Verschleierte Wanderer trägt eine beunruhigende Präsenz in sich, verborgen unter den Resten verrotteter Gewänder und einem geisterhaften Schleier.

Die Gelehrten der Ersten Ära hätten in ihm Zeichen einer unvollständigen Manifestation der Essenz erkannt.

Wir wissen noch nicht, ob er Erinnerungen an sein früheres Leben bewahrt.

Wir wissen nur, dass etwas bleibt.
""")

entry('lore.bestiary.osario_quebrado', None, """
Die alten Forscher beobachteten, dass bestimmte Überreste außergewöhnliche Erscheinungen bewahren konnten, selbst nachdem jedes erkennbare Lebenszeichen verschwunden war.

Das Zerbrochene Beinhaus stellt eines dieser Phänomene dar.

Seine Existenz wirft eine Frage auf, die die Meister der Ersten Ära verblüffte:

Kann Essenz materielle Überreste zusammenhalten, ohne ein Bewusstsein, das sie lenkt?

Die Aufzeichnungen bieten keine abschließende Antwort.
""")

entry('lore.bestiary.ancient_automaton', None, """
Die Ingenieure der Ersten Ära bauten Maschinen, die Befehle noch lange ausführen konnten, nachdem ihre Bediener die Anlagen verlassen hatten.

Der Uralte Automat ist ein Zeugnis dieses Wissens.

Er kämpft weder aus Hass noch aus Ehrgeiz.

Er bleibt dort, wo seine Erbauer ihn zurückließen, und erfüllt eine Aufgabe, deren Bedeutung im Lauf der Jahrhunderte vielleicht verloren ging.

Seine Niederlage bedeutet nicht notwendigerweise das Ende einer bösen Bedrohung.

Sie kann bedeuten, dass eine alte Verteidigung aufgehört hat zu funktionieren.
""")

entry('lore.bestiary.custodio_cuatro_llaves', None, """
Der Wächter wurde erdacht, um die Mechanismen eines außergewöhnlichen Heiligtums zu schützen.

Die vier an seine Struktur gebundenen Schlüssel beherrschten den Zugang zu den inneren Kammern.

Er beurteilt die Absichten derer nicht, die sich nähern.

Er unterscheidet nicht zwischen einem Dieb und einem Abenteurer, der Antworten sucht.

Seine Aufgabe ist es, den Weg zu versperren.

Über Generationen führte er diesen Befehl aus.

Wer ihn besiegt, wird entdecken, dass manche Türen aus ganz anderen Gründen verschlossen wurden, als einen Schatz zu hüten.
""")

entry('lore.bestiary.escorificador_hierro', None, """
Der Eisenschlacker gehört zu den Bedrohungen, die die Einfälle aus dem Nether begleiten, und zu jenen, die in dessen Gebieten hausen.

Seine Anwesenheit in der Oberwelt erinnert daran, dass dimensionale Grenzen durch außergewöhnliche Verbindungen verändert werden können.

Nicht jeder Bruch im Schleier führt zu Orten, die nie ein Forscher erreichte.

Manche öffnen Wege in Regionen, die Abenteurer bereits kennen, auch wenn deren Bewohner nicht in die Welt gehören, die sie durchquert haben.
""")

entry('lore.event.soul_fog_chronicle', 'Seelennebel', """
Wenn der Nebel die Wege bedeckt, erinnern sich die Dorfbewohner an alte Erzählungen von Kreaturen, die aus dem Tod zurückkehren.

Manche behaupten, das Phänomen habe nach dem Fall der Ersten Ära begonnen.

Andere bestehen darauf, es habe schon vor den Gilden existiert.

Kein Zeugnis hat seinen Ursprung vollständig erklärt.

Forscher fanden Hinweise auf Erscheinungen, die mit den Echos der Essenz zusammenhängen, doch ihre Dokumente blieben unvollständig.

Der Nebel kündigt nicht zwingend das Kommen dessen an, was die Gilden fürchteten.

Er kann eine andere Veränderung der Grenzen darstellen, die Materie von bestimmten Restmanifestationen trennen.
""")

entry('lore.event.ember_rain_chronicle', 'Glutregen', """
Portale erlauben es, Entfernungen zu überbrücken, die die gewöhnlichen Gesetze der Welt getrennt halten würden.

Doch manche Verbindungen können das Gleichgewicht zwischen den Dimensionen zeitweise stören.

Der Glutregen ist eine Erscheinung dieser Gefahr.

Kreaturen aus dem Nether brechen in die Oberwelt ein und bringen Bedrohungen mit, auf die viele Siedlungen nicht vorbereitet sind.

Das Phänomen erinnert an eine vergessene Lehre der Forscher des Schleiers:

Einen Weg zwischen zwei Welten zu öffnen heißt nicht, zu beherrschen, was ihn beschreiten kann.
""")

entry('lore.event.eclipse_chronicle', 'Finsternis', """
Während der Ersten Finsternis entdeckten die Meister der Ersten Ära, dass ein äußerer Wille Einfluss auf die Kreaturen der Welt ausüben konnte.

Lange danach erlaubt das Opfer einer Gabe, diese Verbindung erneut herzustellen.

Die betroffenen Champions und Elitegegner erhalten eine außerordentliche Fähigkeit, ihre Ziele aufzuspüren.

Das Phänomen ist nicht die Befreiung des Gefangenen.

Es ist eine vorübergehende Erscheinung seines Willens.

Und während die Abenteurer ums Überleben kämpfen, bleibt etwas hinter den Grenzen des Schleiers verborgen.
""")
