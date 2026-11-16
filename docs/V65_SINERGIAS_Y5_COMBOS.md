# v65 — Sinergia Y5: Combos ↔ Piedras Angulares

**Estado:** Base implementada; integración en sistema de combos pendiente.

## Y5: Combos ↔ Piedras Angulares

**Concepto:** El remate de combo dispara una piedra angular (con espera configurable).

### Detalles

- **Trigger:** Cuando un combo llega a máximo (remate exitoso)
- **Acción:** Dispara la piedra angular equipada del jugador
- **Espera:** 20 segundos de cooldown (configurable)
- **Requisito:** Debe haber piedra angular equipada
- **Efecto:** La piedra angular se ejecuta como una acción gratuita

### Implementación actual

**Archivo:** `src/main/java/dev/aptitudes/ability/SynergyRules.java`

Métodos públicos (puros):
```java
boolean canTriggerKeystoneOnCombo(player, comboFinisher)
    → false (placeholder; verifica cooldown)

int keystoneComboTrigerCooldown()
    → 400 ticks (20 segundos por defecto)
```

### Lógica pendiente (TODO)

**Detección de remate exitoso:**
1. Monitorear `ComboCounter` cuando alcanza máximo
2. Verificar si jugador tiene piedra angular equipada
3. Verificar si cooldown de Y5 ha expirado
4. Si todo OK: ejecutar piedra angular
5. Establecer nuevo cooldown

```java
// Pseudocódigo en Combos.onStrike()
ComboCounter counter = COUNTERS.get(player);
if (counter.isMaxed(now)) {
    if (SynergyRules.canTriggerKeystoneOnCombo(player, abilityId)) {
        Keystones.triggerFromCombo(player);  // TODO: crear método
        // Establecer cooldown de Y5
    }
}
```

### Integración

**Dónde integrar:**
- `ability/Combos.java` (onStrike): detectar remate máximo
- `ability/Keystones.java`: nuevo método `triggerFromCombo()`
- Almacenar cooldown de Y5 en `KEYSTONE_COMBO_COOLDOWN` (mapa UUID → tick)

### Ejemplos de flujo

| Evento | Estado | Resultado |
|---|---|---|
| Combo golpe 1 | Contador = 1 | Sin acción |
| Combo golpe 2 | Contador = 2 | Sin acción |
| Combo golpe 3 (máximo) | Contador máx, cooldown 0 | ✅ Piedra angular dispara |
| Inmediatamente otro | Contador máx, cooldown 400 | ❌ En espera (cooldown activo) |
| Después de 20s | Contador máx, cooldown 0 | ✅ Piedra angular dispara |

### Consideraciones de balance

- **Cooldown global:** 20s (ajustable en config)
- **Energía:** La piedra angular consume energía normal (no gratuita)
- **Stacking:** No se apila con múltiples remates (cooldown evita spam)
- **Requisito:** Solo funciona si hay piedra angular equipada

### Pruebas

**Archivo:** `src/test/java/dev/aptitudes/ability/SynergyRulesTest.java`

- `canTriggerKeystoneOnComboReturnsFalseByDefault()` - placeholder
- `keystoneComboTrigerCooldownReturnsValidTicks()` - default 20s (400 ticks)

### Notas

- Cooldown separado por jugador para evitar conflicts
- Compatible con todos los tipos de piedras angulares
- No afecta a usos manuales de piedras angulares
- La piedra angular se ejecuta como si el jugador la hubiera activado manualmente

### Pendientes (C5 finalización)

- [ ] Implementar detección de remate máximo en Combos
- [ ] Crear método `Keystones.triggerFromCombo()`
- [ ] Almacenar cooldown Y5 por jugador
- [ ] Pruebas en partida: verificar disparo automático de piedra
- [ ] Balancear cooldown según feedback
