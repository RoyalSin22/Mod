# Compatibilidad con otros mods

Decisiones del autor (v66) sobre con qué mods debe convivir Aptitudes (Fabric, Minecraft 1.21.1). Es una lista de objetivos
de prueba: **ninguno está validado todavía** y la disponibilidad de cada mod para 1.21.1 debe comprobarse antes de probar.

| Categoría | Mods | Decisión |
|---|---|---|
| Optimización | Sodium, Lithium, Iris | Soportados. Hay que comprobar los HUD y los jefes con GeckoLib con Sodium e Iris activos |
| Minimapa | Xaero's Minimap y World Map, JourneyMap | Soportados. Comprobar que no se solapan con la barra de habilidades ni con la esfera de maná |
| Jefes | Bosses of Mass Destruction | Soportado. Comprobar la Amenaza, los afijos de élite y el filtro de objetivos |
| Interfaz y recetas | JEI, REI, Mod Menu | Soportados. La Mesa del gremio usa un tipo de receta propio (`aptitudes:guild_crafting`) |
| Biblioteca | GeckoLib | Obligatoria (4.7.7). Si otro mod exige otra versión, hay conflicto |
| Combate | Better Combat | **No se soporta**: cambiaría todo el estilo de combate |
| Accesorios | Trinkets o Accessories | A estudiar: o bien compatibilizar nuestras ranuras de accesorios con esos mods, o bien hacer que los accesorios de Aptitudes funcionen de forma dinámica con ambos sistemas |

## Pendiente

- Revisión estática de los 10 archivos con `@Mixin` (en especial `MobEntityMixin` y `Eclipse*Mixin`).
- Prueba por lotes: optimización, minimapa, jefes, interfaz y recetas.
- Diseñar la integración de accesorios (ver arriba) antes de tocar `accessory/`.
