# v65 C5 — Plan de Pruebas en Partida

**Estado:** Código integrado y compilando ✅  
**Compilación:** `./gradlew build` → SUCCESS  
**Rama:** `claude/c5-y1-y4-synergies` (9 commits)

---

## Requisitos previos

1. Ejecutar mod en mundo Supervivencia o Creative
2. Tener acceso a múltiples roles (principal + secundario)
3. Tener armas, armaduras y runas disponibles
4. Estar en grupo (para Y2)

---

## Pruebas por sinergia

### Y1: Senda ↔ Activas

**Objetivo:** Verificar que bonificadores de senda se aplican a habilidades.

#### Caso 1: Radio (+20% con SPEED)
```
1. Elegir rol con senda que contenga PathEffect.SPEED
2. Activar habilidad de "movimiento" (buscar por nombre: dash, move, rush)
3. Medir: radio visual debe ser +20%
4. Verificar: ✓ si área aumenta; ✗ si no cambia
```

#### Caso 2: Daño (+15% con CRIT/EXECUTE)
```
1. Elegir rol con senda CRIT/EXECUTE
2. Activar habilidad ofensiva (nova, blast, strike)
3. Medir: daño contra mob debe ser +15%
4. Verificar: ✓ si mob recibe más daño; ✗ si es igual
```

#### Caso 3: Coste energético (−10% con MANA_REGEN + RESOURCE_GAIN)
```
1. Equipar senda con MANA_REGEN y RESOURCE_GAIN
2. Usar habilidad, anotar coste en HUD
3. Cambiar a otra senda sin esos efectos
4. Usar misma habilidad, comparar coste
5. Verificar: ✓ si cuesta −10%; ✗ si es igual
```

---

### Y2: Resonancia grupo (+15% XP)

**Objetivo:** Verificar que grupo comparte bonus XP si tienen keystones iguales.

```
1. Formar grupo de 2+ jugadores
2. Equipar misma piedra angular en ambos (ej: OVERLOAD en ambos)
3. Matar mobs juntos (en grupo)
4. Anotar XP recibido en ambos
5. Cambiar piedra angular en uno de ellos
6. Matar otro mob juntos
7. Comparar XP

Verificar: ✓ si +15% con keystones iguales; ✗ si es igual
```

---

### Y3: Elementos ↔ Runas (+20% daño fusión)

**Objetivo:** Verificar que runa del elemento correcto potencia fusiones.

```
1. Jugar con Elementalista
2. Equipar runa FLAME en arma
3. Activar habilidad de FIRE (fusión)
4. Medir daño contra mob
5. Cambiar a runa diferente (FROST)
6. Activar misma habilidad
7. Comparar daño

Mapeos a probar:
- FLAME (fuego) → Element.FIRE
- FROST (hielo) → Element.ICE
- THUNDER (rayo) → Element.LIGHTNING

Verificar: ✓ si +20% con runa correcta; ✗ si es igual
```

---

### Y4: Armadura ↔ Senda (+10% defensa)

**Objetivo:** Verificar que armadura + senda = bonificador.

```
1. Elegir rol (ej: Paladín)
2. Equipar senda (ej: Defensa)
3. SIN armadura del rol: recibir daño, anotar en vida
4. CON armadura completa del rol: recibir daño igual
5. Medir: resistencia a daño debe ser +10%

Roles con sets:
- Berserker, Mago, Asesino, Herbolista, Ingeniero, Lancero, Espadachín,
  Paladín, Nigromante, Druida, Mercader, Monje, Elementalista, Cambiaformas

Verificar: ✓ si −10% daño recibido; ✗ si es igual
```

---

### Y5: Combos ↔ Keystones

**Objetivo:** Verificar que remate de combo dispara piedra angular.

```
NOTA: Actualmente en fase TODO - triggerFromCombo() no implementado.
Se verifica solo que el código no crashea y establece cooldown.

1. Hacer combo hasta máximo (5+ golpes)
2. Ejecutar remate exitoso
3. Verificar: ✓ si no hay error en consola; ✗ si hay crash
4. Esperar 20s (cooldown)
5. Hacer otro combo

TODO: Implementar disparo real de keystones
- Método: Keystones.triggerFromCombo(player, template)
- Intención: ejecutar piedra angular sin coste de energía
```

---

## Checklist de pruebas

### Compilación
- [x] `./gradlew build` → SUCCESS
- [x] 794 JUnit tests → PASS
- [x] Sin errores de compilación

### Integración
- [x] Y1 integrado en AbilityManager.contextForAbility()
- [x] Y2 integrado en PartyManager.awardCombatXp()
- [x] Y3 integrado (método disponible, sin trigger real)
- [x] Y4 integrado en RolePassives.applyArmorSynergyBonus()
- [x] Y5 integrado en Combos.finisher()

### En partida (pendiente)
- [ ] Y1: Radio +20% observable
- [ ] Y1: Daño +15% observable
- [ ] Y1: Energía −10% observable
- [ ] Y2: XP +15% con keystones iguales
- [ ] Y3: Daño +20% con runa correcta
- [ ] Y4: Defensa +10% con armadura
- [ ] Y5: Remate no crashea (TODO: disparo real)

---

## Cómo ejecutar pruebas

### En Windows/Mac
```bash
./gradlew runClient
# → Esperar a que abra Minecraft
# → Crear mundo Supervivencia
# → Activar modo creativo si es necesario
# → Seguir casos arriba
```

### En Linux (sin pantalla)
```bash
# Instalar Xvfb o usar servidor remoto
Xvfb :99 -screen 0 1024x768x24 &
export DISPLAY=:99
./gradlew runClient &
# → Usar herramientas de captura de pantalla o logs
```

---

## Logs a revisar

Abre `logs/latest.log` después de probar:

```
[Aptitudes] Y1: contextForAbility() called with role=...
[Aptitudes] Y2: groupResonanceBonus() = 1.15
[Aptitudes] Y3: elementRuneSynergyBonus() = 1.20
[Aptitudes] Y4: applyArmorSynergyBonus() applied
[Aptitudes] Y5: tryTriggerKeystoneOnCombo() cooldown set
```

---

## Notas importantes

1. **Y1:** la categoría de cada activa sale de la tabla `Y1Rules` (80 base y 100 nuevas por id, y las evolucionadas por plantilla) y los efectos, de los nodos con rango de la senda elegida. Una activa fuera de la tabla no recibe bonus; el daño y el radio también valen para los golpes diferidos de la misma habilidad.

2. **Y2 sincronización:** El bonus se aplica al cobrar XP de combate. Debe estar en grupo ANTES de matar.

3. **Y3 armas:** Solo funcionan con el arma equipada al disparar la habilidad. Cambiar arma antes de disparar no activará sinergia.

4. **Y4 armadura:** Debe ser set completo (4 piezas del mismo rol). Mezclar sets no activa.

5. **Y5 cooldown:** 20s globales. Después de primer remate, esperar antes de poder dispara keystones otra vez.

---

## Próximos pasos post-testing

1. Si todo ✓: Merge en main
2. Si hay issues: Crear PR con fixes específicos
3. Implementar Y5 triggerFromCombo() si no se dispararan keystones
4. Agregar textos en 6 idiomas
5. Crear logros/estadísticas para sinergias
