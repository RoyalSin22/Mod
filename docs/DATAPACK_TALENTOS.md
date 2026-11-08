# Talentos por datapack (v55)

Copia la carpeta [talentos-piloto](examples/talentos-piloto/) a `datapacks` dentro de un mundo de
Minecraft 1.21.1. El ejemplo define Paso ligero y Paso firme para el Espadachín. Al abrir el mundo,
consulta `logs/latest.log` en el cliente integrado o el registro del servidor. Busca
«Catálogo de talentos por datapack, revisión 1: 2 nodos». Si lo instalas con el mundo abierto,
activa `file/talentos-piloto` con `/datapack enable`.

Cada JSON en `data/<namespace>/aptitudes/talents/<ruta>.json` crea el identificador
`<namespace>:<ruta>`. El pack de mayor prioridad define el contenido si varios aportan la misma ruta.
Los identificadores admiten hasta 64 caracteres. Los segmentos empiezan por minúscula, cifra o guion bajo.

| Campo | Valores |
|---|---|
| `role` | Clave de un rol existente, como `swordsman`. |
| `prerequisite` | ID de un talento base del mismo rol o ID de otro talento del catálogo. Cada rama debe remontar a un talento base. Las sendas, evoluciones y legendarias quedan fuera de este piloto. |
| `level` | Entero entre 1 y 50. El primer rango exige al menos el nivel de su prerrequisito. |
| `ranks` | Entero entre 1 y 3. Cada rango posterior exige cinco niveles adicionales y el último debe caber dentro del nivel 50. |
| `template` | `speed`, vinculada al efecto de velocidad que ya usan las Sendas. |

El servidor acepta como máximo 32 talentos, 8 por rol y 8192 bytes por archivo. Valida el catálogo
completo al arrancar y con `/reload`. Una definición inválida deja activa la última revisión válida
de esa sesión. Deshabilitar el pack mediante `/datapack disable` elimina sus nodos si el catálogo
restante sigue siendo válido. La revisión solo cambia cuando cambian las definiciones aceptadas.

## Aprender y recuperar puntos

En **J**, selecciona Espadachín y abre **Talentos del pack**. Aprende primero **Parada**
(ID `parry`). Paso ligero requiere nivel 12. Tras aprenderlo, Paso firme se puede comprar a nivel
16 y mejorar a nivel 21. El rol debe estar equipado como principal o secundario y cada rango
cuesta un punto del mismo fondo que Talentos y Sendas. El pack se instala en el servidor. Los
clientes necesitan la misma versión del mod para abrir la vista y enviar compras.

Los rangos son pasivos. Cada uno suma +2 % de velocidad de movimiento sobre la base del atributo.
El bonus de los dos roles activos se limita en conjunto a +6 %. Cambiar de rol quita su contribución
hasta volver a equiparlo. Los rangos se guardan por jugador y se conservan al morir, reconectar
o reiniciar con un catálogo compatible.

Si retiras una definición, la asignas a otro rol o elevas sus requisitos, el servidor retira los rangos
afectados y devuelve los puntos, incluidas las ramas que dependían de ellos. Con los jugadores
conectados ocurre al recargar. Para los demás ocurre al entrar. La última revisión válida se mantiene
en memoria durante la sesión. Tras reiniciar se valida el catálogo de disco. Si es inválido, no se carga
y se devuelven los puntos de sus ramas al entrar.

Para dar nombre a un talento nuevo, distribuye un paquete de recursos con la clave
`talent.<namespace>.<ruta>.name` en los idiomas correspondientes. Conserva las barras de la ruta.
Sin esa traducción, la vista muestra el identificador del talento. El mod incluye los nombres del ejemplo.
