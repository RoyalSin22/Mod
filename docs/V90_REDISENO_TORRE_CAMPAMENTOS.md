# V90 — Torre menor y campamentos (propuesta B)

## Alcance

Se rediseñan las estructuras nuevas sin borrar las ya generadas. Siguen registrados los identificadores
`tower` y `camp`, los jefes `TOWER_SENTINEL` y `CAMP_CHAMPION`, el índice de estructuras y los comandos
`/aptitudes locate` y `/aptitudes place`. Los filtros de rareza permanecen en 1/240 y 1/200 chunks.
Los nombres de las tablas `tower_floor`, `tower_upper`, `tower_top`, `camp_hunter`, `camp_warlock`
y `camp_bandit` no cambian. Cuatro temáticas nuevas tienen tablas propias de botín modesto.

## Torre

Plano puro `dungeon/TowerCompoundLayout`, radio 10 (21×21), con torre de 9×9 al oeste, patio, caseta,
pozo, establo, sótano y cinco pisos de cinco bloques. Dos contornos: cuadrado con contrafuertes y redondo.
El daño 0–2 abre huecos altos en murallas y muros sin quitar escalera, puerta ni suelo. La entrada está al
sur; la puerta de la torre mira al este. La escalera zigzaguea entre plantas. El sótano usa una escalera
de mano con hueco propio y un cofre bajo una baldosa de lana azul.

| Nivel | Interior y botín | Encuentro |
|---|---|---|
| Patio | Caseta con cofre, pozo, establo y muralla quebrada | Guardias; campeón adicional desde anillo II |
| Sótano | Celdas, barrotes, cofre ordinario y cofre oculto | Exploración |
| P0 | Sala de guardia, cofre `tower_floor` | Élite |
| P1 | Armería, cofre `tower_floor` | Élite |
| P2 | Dormitorio, cofre `tower_floor` | Élite y generador zombi/esqueleto |
| P3 | Taller y estudio, galería, cofre `tower_floor` | Élite y generador araña/esqueleto |
| P4 | Mirador, galería, cofre `tower_upper` | Élite |
| Azotea | Almenas y `tower_top` | Centinela de tres fases |

La paleta depende del anillo de Amenaza y del bioma: piedra y madera local cerca del origen; pizarra
profunda en II–III; piedra negra en IV–V. Los élites llevan armadura visible. Los generadores conservan
su tipo por piso y su configuración moderada actual. Las posiciones de encuentro escalan por anillo.

## Campamentos

Plano puro `dungeon/CampLayout`, radio 14 (máximo 29×29). La semilla elige círculo de tiendas, fortín de
empalizada con puesto de vigía accesible, línea de camino con carros o terraza con rampa. Cada forma admite
los siete tipos: cazadores, brujos, bandidos, mercaderes asaltados, desertores, cultistas del Velo y
buscadores de reliquias. Tela, utilería, enemigo y tabla de botín cambian con el tipo.

El capitán conserva `CAMP_CHAMPION`. A su muerte, una etiqueta persistente identifica el tipo y avanza
el contrato de limpieza correspondiente. `CAMP_CLEAR` se añade al final de `ContractRules.Kind`;
sus siete objetivos numéricos siguen el orden de `CampLayout.Kind`. Los centinelas y el capitán pueden
dar la alarma a menos de 24 bloques con visión; la alarma despierta a los durmientes y activa a todos
los miembros del mismo campamento. Los guardias despiertan de cerca y los patrulleros usan su IA normal.
El cofre oculto queda bajo una baldosa de lana amarilla.

Los encuentros se dimensionan a 4, 6 u 8 miembros más el capitán según anillo 0–I, II–III o IV–V.
En los dos tramos superiores hay respectivamente uno o dos élites de armadura visible. No se introducen
objetos de poder, linajes, razas ni pactos.

## Generación y pruebas

Ambas `Feature` evalúan un plano puro por celda y usan `StructureBuilder` para colocarlo. Rechazan
terreno con fluidos, desnivel superior al permitido o altura insuficiente. Conservan el método de
generación local de las `Feature`; las entidades de encuentro se crean al generar la estructura,
igual que en el diseño anterior. `StructureIndex.record` se llama tras colocarla.

Las pruebas JUnit comprueban determinismo, límites y recorridos a pie para las seis variantes de torre
y las 28 combinaciones de campamento. La comprobación de recorrido trata la pista de lana como bloque
retirable y verifica que el cofre subyacente se puede alcanzar después de abrirla.

## Comprobación manual pendiente

1. Generar y colocar cada forma en varios biomas y anillos. Verificar materiales, iluminación y que no
   invade estructuras próximas ni fluidos.
2. Recorrer ambas formas de torre con daño 0, 1 y 2 en Supervivencia: caseta, sótano, cinco pisos,
   galerías, dos generadores, cofres y Centinela.
3. Recorrer las cuatro formas de campamento, incluida la escalera de la torre de vigía y la rampa.
   Comprobar la alarma con línea de visión y sin ella, guardias, patrullas y durmientes.
4. Aceptar cada contrato, derrotar un capitán del tipo correcto e incorrecto, guardar y recargar el
   mundo, y comprobar progreso y reparto al grupo.
5. Medir coste y aparición efectiva en distintas semillas. La tirada por chunk no cambió, pero la
   huella mayor puede rechazar más terrenos que el diseño anterior.
