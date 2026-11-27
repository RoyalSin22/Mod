# Encargo visual V91 — Tres Torres Señoriales

## Objetivo

Rediseñar **primero** la fortaleza de las Tres Torres Señoriales como una ruina medieval gótica
convincente dentro de Minecraft 1.21.1. El autor prefiere el **anillo de murallas rotas**.
El plano actual de 128×128 funciona como base técnica, pero se percibe demasiado simple y vacío.
La próxima implementación debe aprobarse visualmente antes de cambiar la generación.

## Prompt para concepto y prototipo

> Diseña una fortaleza medieval gótica en ruinas, construible con bloques de Minecraft 1.21.1,
> dentro de una huella aproximada de 128×128 bloques. Conserva un anillo irregular de murallas
> rotas con barbacana y entrada principal al sur. Deben destacar tres torres de silueta distinta:
> la Torre del Mariscal, más alta y pesada, que alberga al Coloso Sin Rostro; dos torres de
> vigilancia con un Guardián Desmontado cada una. Evita edificios con forma de caja y superficies
> planas extensas. Usa contrafuertes escalonados, arcos apuntados, ventanas estrechas con tracería,
> cornisas, balcones dañados, tejados parciales, almenas irregulares, bóvedas, escaleras de piedra,
> vigas expuestas, pavimento con patrones, escombros estructurales, vegetación medida y
> puntos de luz integrados. Cada fachada debe tener profundidad y una composición legible.
>
> Distribuye y amuebla con identidad propia: dormitorios con literas y baúles; barbacana con
> rastrillo decorativo y puestos de guardia; estudio con mesas, anaqueles y atriles; sala de
> botín; monasterio o capilla con altar y bancos; ala de pociones y encantamientos con mesas,
> estanterías y material de laboratorio. Sitúa cofres modestos y ocultos por toda la fortaleza.
> El botín principal queda tras el Coloso. El recorrido a pie debe funcionar sin saltos
> obligatorios, incluso donde la ruina abre muros o techos.
>
> Presenta: (1) vista isométrica general con etiquetas y escala; (2) plano por nivel y
> recorrido; (3) cuatro vistas a altura del jugador —entrada, patio, capilla y ala de estudio;
> (4) secciones de las tres torres; (5) paleta exacta de bloques vanilla y propuesta de
> cinco a diez elementos de decoración que requieran modelo propio. Muestra daño coherente:
> un muro derrumbado deja escombros debajo; una bóveda rota conserva apoyos; la vegetación
> no tapa el camino. Evita aspecto de pirámide, castillo intacto, mazmorra de habitaciones
> vacías o render con detalles imposibles de construir.

## Método de producción recomendado

1. Aprobar imágenes de concepto y una lista de motivos arquitectónicos. Las imágenes sirven
   para fijar la dirección artística, no son el plano final.
2. Construir un prototipo en un mundo creativo de **Minecraft 1.21.1 con WorldEdit compatible**.
   Guardar una copia `.schem` de la geometría y capturas desde las cuatro vistas anteriores.
3. Usar Blockbench solo para piezas pequeñas que justifiquen un modelo propio: estatuas sin
   rostro, capiteles, relicarios decorativos o mobiliario. Conservar los `.bbmodel` fuente.
4. Convertir o transcribir el prototipo a un plano puro consultable por columna y chunk.
   La estructura final no debe depender de WorldEdit en tiempo de ejecución.
5. Validar rutas, espacios de jefe, cofres, cimientos y presupuesto de colocación con JUnit,
   luego revisar la estructura en un mundo real antes de integrarla.

## Reja de la Vigilia

El autor quiere retirarla de las estructuras nuevas. En la fortaleza, el control de Guardianes,
Coloso y recompensa debe quedar en el controlador y el recorrido, sin colocar
`vigil_gate`. En el rediseño posterior del templo se sustituirán sus barreras por la
topología del recorrido, la prueba y la Puerta del Custodio. El registro del bloque puede
permanecer **solo como compatibilidad de mundos ya generados**: no debería tener receta,
botín ni aparecer en estructuras nuevas. Su retirada completa del registro se evaluará
cuando se decida si se admite romper esos mundos antiguos.

## Criterios de aceptación visual

- Silueta reconocible desde el exterior y profundidad visible en las cuatro fachadas.
- Cada sala obligatoria tiene mobiliario, iluminación y al menos un elemento arquitectónico
  que exprese su función.
- Las ruinas siguen una causa física legible; no se distribuyen grietas y escombros al azar.
- El jugador puede distinguir entrada, rutas secundarias, tres torres y tesoro final sin mapa.
- Capturas a altura del jugador aprobadas antes de programar la nueva geometría.
- Ninguna decoración obliga a generar enemigos al crear chunks ni añade poderes nuevos.
