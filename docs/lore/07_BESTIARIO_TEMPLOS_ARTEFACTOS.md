# 07 · Bestiario, templos y artefactos

**No se modifica ninguna criatura, jefe, hitbox, modelo, animación, drop, IA ni renderer.** Esta ficha solo
asocia cada criatura con su lugar en la historia.

## Criaturas y jefes conocidos

| Criatura | Id (entrada de bestiario) | Estado en código | Papel narrativo (PROPUESTA) |
|---|---|---|---|
| Errante Velado | `errante_velado` | VERIFICADO (`mob/ErranteEntity`; loot `jiron_velo_espectral` 85 %) | Residuo errante de la Edad del Velo; indicio de Esencia persistente (Ecos) |
| Osario Quebrado | `osario_quebrado` | VERIFICADO (`mob/OsarioEntity`); botín: solo `soul_shard` en bestiario | Restos que vuelven a levantarse; **material propio sin definir** (D-05) |
| Escorificador de Hierro | `escorificador_hierro` | VERIFICADO (`mob/EscorificadorEntity`, Nether/Lluvia de brasas); hitbox pendiente de corregir según el responsable | Criatura del Nether que irrumpe en el Overworld; **no** depende de la Conciencia |
| Coloso Sin Rostro | `faceless_colossus` | VERIFICADO (`boss/FacelessColossusEntity`) | Sin papel fijado (D-07) |
| Guardián Desmontado | `dismantled_guardian` | VERIFICADO (`boss/GuardianEntity`) | Constructo de la Primera Era (PROPUESTA) |
| Autómata Antiguo | `ancient_automaton` | VERIFICADO (`boss/AutomatonEntity`) | **Subjefe del templo** y guardián de mecanismos |
| Custodio de las Cuatro Llaves | **sin entrada de bestiario** (id de entidad `custodio_cuatro_llaves`) | VERIFICADO: es el guardián del sello del templo (`mob/CustodioEntity`; `temple/Temples.spawnGuardian`, v65). Que guarde «cuatro llaves» y cámaras: NO VERIFICADO | Guardián de la cámara principal |
| Élites y campeones | `elite`, `champion` | VERIFICADO (`elite/`) | Criaturas potenciadas durante el Eclipse |
| Heraldo del Eclipse | `boss_eclipse_herald` | VERIFICADO (`boss/BossManager`) | Manifestación del Eclipse; suelta `eclipse_core` |

> Ojo con los nombres: el lore dice «Custodio» para el jefe de las llaves y también habla de «Autómata» y
> «Guardián». En el juego son **tres** entidades distintas (C-07).

### Reglas de desbloqueo del bestiario (VERIFICADO)

Cada entrada se desbloquea **por jugador al derrotar** la criatura (`guide/BestiaryRules`). El lore no cambia
eso: los textos históricos de bestiario seguirán el mismo gatillo.

## Templos

Infraestructura real (**VERIFICADO**, v64, `temple/`): puzle por anillo de Amenaza, guardián, llave del templo
(`TempleKeyItem`), Relicario (`ReliquaryBlockEntity`) y Vasos de vida (+1 corazón).

Secuencia propuesta por el autor y estado:

| # | Paso | Estado |
|---|---|---|
| 1 | Explorar el templo | VERIFICADO (estructura) |
| 2 | Autómata Antiguo como subjefe | Entidad VERIFICADA; su rol de subjefe: NO VERIFICADO |
| 3 | Resolver un puzle | VERIFICADO (puzle por anillo de Amenaza) |
| 4 | Usar el objeto especial | **PENDIENTE** (objeto y receta sin definir) |
| 5 | Abrir la cámara principal | NO VERIFICADO |
| 6 | Combate con el Custodio de las Cuatro Llaves | VERIFICADO como guardián del sello (`Temples.spawnGuardian`); su relación con las cuatro llaves, NO VERIFICADO |
| 7 | Obtener cuatro llaves | NO VERIFICADO (existe una `TempleKeyItem`; no cuatro llaves distintas) |
| 8 | Abrir cuatro cámaras de recompensa, la principal de diamante | NO VERIFICADO |

No se reemplazan templos, llaves, jefes ni estructuras.

## Artefacto central (inspirado conceptualmente en la función narrativa del Behelit)

**No existe en el código** como objeto propio. Existen `eclipse_fragment` (cae de mobs en el Eclipse) y
`eclipse_core` (botín del Heraldo); ninguno es el artefacto del templo.

**Propuesta de identidad original (solo documentación, D-03):**

| Elemento | Propuesta |
|---|---|
| Nombre de trabajo | **Resonador del Umbral** (alternativas: «Aguja del Silencio», «Ancla de Eclipse») |
| Función narrativa | Artefacto de comunicación del Período 7; la conexión externa que la Conciencia necesitaba |
| Apariencia | Disco de metal pálido con un anillo de luz hueco en el centro; no un ojo, no una cara ni un huevo |
| Simbolismo | El anillo vacío evoca al eclipse; **no** reproduce el diseño ni el simbolismo de ninguna obra protegida |
| Uso | Se sacrifica en la Hoguera para el primer Eclipse (**PENDIENTE DE IMPLEMENTAR**) |
| Restricciones | Sin id de registro, sin modelo, sin receta; el nombre es provisional |

Esta propuesta busca **no copiar** objetos, nombres ni diseños protegidos: el único vínculo con el original es la
función narrativa (un objeto que abre una puerta a algo que no debería abrirse), expresada con elementos propios.

## Objetos relacionados que ya existen (VERIFICADO)

`aptitudes:jiron_velo_espectral`, `aptitudes:soul_shard`, `aptitudes:eclipse_fragment`, `aptitudes:eclipse_core`,
`aptitudes:elite_essence`, `aptitudes:champion_heart`. El autor indicó que varios objetos se **rediseñarán**, así
que ningún texto debe depender de su nombre actual (D-05).
