# v65 — Sinergia Y2: Resonancia de Grupo

**Estado:** Base implementada; lógica de detección de keystones compartidas pendiente.

## Y2: Resonancia de Grupo

**Concepto:** +15% XP si dos o más miembros del grupo comparten la misma plantilla de piedra angular.

### Detalles

- **Bonificador:** ×1.15 (15% más XP)
- **Condición:** Mínimo dos miembros deben tener la misma `KeystoneRules.Template`
- **Aplicación:** Se suma al bonificador de tamaño del grupo existente
- **Alcance:** Todos los miembros en línea reciben el bonus

### Implementación actual

**Archivo:** `src/main/java/dev/aptitudes/ability/SynergyRules.java`

Método público (puro):
- `groupResonanceBonus(player)` → float (1.15 si hay resonancia, 1.0 si no)

**Integración:** `PartyManager.awardCombatXp()` y `awardCombatRoleXp()`

```java
float resonanceBonus = SynergyRules.groupResonanceBonus(killer);
bonus *= resonanceBonus;  // Aplica al bonificador de grupo existente
```

### Lógica pendiente (TODO)

**Calcular resonancia:** Necesita:
1. Obtener miembros en línea del grupo
2. Para cada miembro: obtener `Keystones.active(player)` → `Set<Template>`
3. Verificar si hay templates compartidas entre al menos 2 miembros
4. Retornar 1.15 si sí, 1.0 si no

```java
// Pseudocódigo
Set<Template> shared = new HashSet<>();
for (ServerPlayerEntity member : members) {
    Set<Template> memberKeystones = Keystones.active(member);
    if (shared.isEmpty()) {
        shared.addAll(memberKeystones);
    } else {
        shared.retainAll(memberKeystones);  // Intersección
    }
}
return shared.isEmpty() ? 1.0f : 1.15f;
```

### Pruebas

**Archivo:** `src/test/java/dev/aptitudes/ability/SynergyRulesTest.java`

- `groupResonanceBonusReturnsValidBonus()` - placeholder
- `groupResonanceBonusIsNonNegative()` - bonus ≥ 1.0

### Ejemplos de resonancia

| Grupo | Miembro 1 | Miembro 2 | Resonancia | Bonus |
|---|---|---|---|---|
| 2 jugadores | Overload | Overload | ✅ | ×1.15 |
| 2 jugadores | Overload | Specter | ❌ | ×1.0 |
| 3 jugadores | Overload | Overload | ✅ | ×1.15 |
| 3 jugadores | Overload | Specter | Overload | ✅ | ×1.15 |

### Notas

- La resonancia es acumulativa con el bonus de tamaño del grupo
- Ejemplo: grupo de 3 (bonus ×1.5) + resonancia (×1.15) = ×1.73 total
- Sin resonancia: bonus ×1.0 (nada que ocultar)
- Dinámico: detecta cambios de keystones en tiempo real

### Pendientes (C5 continuación)

- [ ] Implementar lógica de detección de keystones compartidas
- [ ] Prueba unitaria: casos con resonancia/sin resonancia
- [ ] Prueba en partida: verificar XP con 2+ miembros
- [ ] Textos de descripción en 6 idiomas (config, descripción de sinergia)
