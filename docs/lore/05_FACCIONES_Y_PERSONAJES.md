# 05 · Facciones históricas y personajes

Estado: todo **PROPUESTA**. Ninguna facción es una organización activa, ni tiene NPC, reputación o mecánica.
(El sistema de reputación que existe, de los **Contratos del gremio**, `contract/`, no se relaciona con estas
facciones y no debe reutilizarse para ellas sin autorización.)

## Facciones

| Id previsto | Nombre | Qué es | Postura | Notas |
|---|---|---|---|---|
| `lore.faction.concordance` | Concordia de los Gremios | Coordinación entre las veinte disciplinas: conservar conocimiento, enseñar, cooperar | Institucional | Se debilita en el Cisma y pierde capacidad tras el Primer Eclipse |
| `lore.faction.permanence` | Cónclave de la Permanencia | Investigadores de la conservación de la conciencia tras la muerte | Investigación | Sus experimentos llevan al contacto con la Conciencia; **no todos** son maliciosos |
| `lore.faction.four_seals` | Orden de los Cuatro Sellos | Restringir investigaciones dimensionales peligrosas | Contención | Participa en el sellado del artefacto y en preservar el templo |
| `lore.faction.threshold_witnesses` | Testigos del Umbral | Detectan indicios de una inteligencia tras el Velo | Escepticismo | Parte de sus registros contradice al Cónclave |
| `lore.faction.seal_wardens` | Vigilantes de los Sellos (antes «Herederos de los Sellos») | Tradición que conserva el confinamiento y evita nuevos contactos | Contención | Renombrada por la exclusión de «herederos» |
| `lore.faction.threshold_disciples` | Discípulos del Umbral | Cuestionan si el confinamiento natural implica que deba mantenerse | Cuestionamiento | **No** son seguidores de la entidad |

### Reglas de redacción

1. Vigilantes de los Sellos y Discípulos del Umbral tienen conocimientos **parcialmente correctos y limitados**;
   ninguno posee toda la verdad.
2. Los «Cuatro Sellos» **no** son un objeto del juego. El número cuatro puede tener un vínculo simbólico con las
   cuatro llaves del templo; **no está confirmado ni debe afirmarse** hasta decidirlo (D-08).
3. La Concordia, el Cónclave y la Orden pertenecen a la Primera Era; las dos tradiciones posteriores (Vigilantes
   y Discípulos) pueden ser lo que sobrevivió al Olvido.

## Personajes históricos (solo autores atribuidos)

| Id previsto | Nombre | Concepto | Uso permitido |
|---|---|---|---|
| `lore.figure.edran` | Maestre Edran | Políticas de contención y preservación de los sellos | Autor de documentos |
| `lore.figure.lysara` | Lysara | Estudio de la Gran Separación y del origen natural del confinamiento | Autora de documentos |
| `lore.figure.vaelor` | Vaelor | Colaboró con varias organizaciones; sus registros cuestionan versiones oficiales | Autor de documentos |

**No implementar** NPC, entidades, modelos ni diálogos. Los nombres de estos personajes son provisionales y no
pasan a clave pública hasta aprobarse (basta el slug descriptivo).

### Atribución propuesta de documentos del segundo arco (PROPUESTA)

| Estructura conceptual | Voz dominante | Idea |
|---|---|---|
| Archivo de los Sellos | Edran | Cómo se diseñó la contención |
| Observatorio del Umbral | Lysara | Pruebas de que la prisión surgió con la Gran Separación |
| Cripta del Último Testigo | Vaelor | Documentos que contradicen versiones oficiales |
| Santuario de las Dos Verdades | Edran y Vaelor enfrentados | Contraste de los registros de ambas facciones |
