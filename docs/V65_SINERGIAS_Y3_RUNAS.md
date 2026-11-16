# v65 — Sinergia Y3: Elementos ↔ Runas

**Estado:** Base implementada; integración en ElementFusions pendiente.

## Y3: Elementos ↔ Runas

**Concepto:** Una runa del mismo elemento que el activo del Elementalista potencia la fusión (+20% daño).

### Mapeo: Runa ↔ Elemento

| Runa | Elemento | Efecto |
|---|---|---|
| **FLAME** | FIRE | Activos de fuego +20% |
| **FROST** | ICE | Activos de hielo +20% |
| **THUNDER** | LIGHTNING | Activos de rayo +20% |
| Sin mapeo | N/A | LEECH, SWIFT, FORCE, ECLIPSE, GUARD |

### Detalles

- **Bonificador:** ×1.20 (20% más daño de fusión)
- **Condición:** Elementalista debe tener una runa del elemento correspondiente
- **Aplicación:** Se suma al daño base de la fusión
- **Limitación:** Solo aplica cuando hay coincidencia exacta runa ↔ elemento

### Implementación actual

**Archivo:** `src/main/java/dev/aptitudes/ability/SynergyRules.java`

Método público (puro):
```java
float elementRuneSynergyBonus(ServerPlayerEntity player, Element activeElement, ItemStack weapon)
    → float (1.20 si hay sinergia, 1.0 si no)
```

Mapeo (lazy-initialized):
```java
getRuneElementMap()
    "flame" → Element.FIRE
    "frost" → Element.ICE
    "thunder" → Element.LIGHTNING
```

### Lógica pendiente (TODO)

**Calcular sinergia:** Necesita:
1. Obtener runas del arma: `RuneSocketing.runes(weapon)` → `List<String>`
2. Para cada runa: mapear a elemento con `getRuneElementMap()`
3. Comparar con `activeElement`
4. Retornar 1.20 si hay coincidencia, 1.0 si no

```java
// Pseudocódigo
List<String> runeIds = RuneSocketing.runes(weapon);
Map<String, Element> map = getRuneElementMap();
for (String runeId : runeIds) {
    Element runeElement = map.get(runeId);
    if (runeElement != null && runeElement == activeElement) {
        return 1.20f;
    }
}
return 1.0f;
```

### Integración

**Dónde integrar:** `ElementFusions.java` o `ability/ElementalistKit.java`
- Cuando se ejecuta una fusión: verificar si hay sinergia
- Aplicar bonificador: `daño *= elementRuneSynergyBonus(player, element, weapon)`

### Ejemplos

| Elementalista | Elemento activo | Arma con runas | Sinergia | Resultado |
|---|---|---|---|---|
| Lv5 | FIRE | FLAME + FROST | ✅ FLAME | ×1.20 |
| Lv10 | FIRE | LEECH | ❌ No mapeada | ×1.0 |
| Lv15 | ICE | FROST | ✅ | ×1.20 |
| Lv20 | LIGHTNING | THUNDER + ECLIPSE | ✅ THUNDER | ×1.20 |

### Pruebas

**Archivo:** `src/test/java/dev/aptitudes/ability/SynergyRulesTest.java`

- `elementRuneSynergyBonusReturnsValidBonus()` - placeholder, default 1.0
- `elementRuneSynergyBonusIsNonNegative()` - bonus ≥ 1.0

### Notas

- Lazy-initialized para evitar issues de bootstrapping en tests
- Sin límite de runas: si hay varias del mismo elemento, aplica una sola vez
- Compatible con múltiples runas (solo detecta coincidencia)
- Bonificador no acumulativo (máx ×1.20)

### Pendientes (C5 continuación)

- [ ] Implementar búsqueda en arma actual del jugador
- [ ] Integrar en ElementFusions al ejecutar fusión
- [ ] Pruebas en partida: verificar +20% daño con runa correcta
- [ ] Extender mapeo si hay runas/elementos nuevos en futuras versiones
