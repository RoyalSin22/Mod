# v65 — Sinergias Y1 (Senda ↔ Activas) y Y4 (Armadura ↔ Senda)

**Estado:** Base de código creada; mapeos específicos pendientes.

## Y1: Senda ↔ Activas

La senda elegida modifica una activa del rol (radio, efecto de estado, carga extra).

### Mapeo (propuesta)

Cada **senda** puede modificar **una o más activas** del rol con un bonificador:

| Senda | Activa afectada | Modificador | Tipo | Valor |
|---|---|---|---|---|
| Velocidad (S5a) | Dash/Embestida | Radio +20% | Radio | ×1.2 |
| Fuerza (S5b) | Nova/Explosión | Daño +15% | Daño | ×1.15 |
| Defensa (S6) | Escudo | Absorción +10% | Absorción | ×1.10 |
| Eficiencia (S4) | Todas | Coste energético −10% | Energía | ×0.9 |

### Implementación

**Archivo:** `src/main/java/dev/aptitudes/ability/SynergyRules.java`

Métodos públicos (puros):
- `radiusModifierForPath(player, pathId)` → float (bonificador de radio)
- `damageModifierForPath(player, pathId)` → float (bonificador de daño)
- `energyCostModifierForPath(player, pathId)` → float (bonificador de coste)

**Integración:** `AbilityManager.invoke()` → aplicar bonificadores antes de calcular daño/radio.

---

## Y4: Armadura ↔ Senda

Bonificación extra si la armadura coincide con la senda elegida.

### Mapeo (propuesta)

| Rol | Senda | Armadura | Bonificador |
|---|---|---|---|
| Paladin | Defensa (S6) | Paladín | +10% defensa |
| Guerrero | Fuerza (S5b) | Guerrero | +10% daño |
| Mago | Eficiencia (S4) | Mago | +10% maná |

### Implementación

**Archivo:** `src/main/java/dev/aptitudes/ability/SynergyRules.java`

Método público (puro):
- `armorSynergyBonus(player, roleId, pathId)` → float (bonificador 1.0 = sin sinergias, 1.10 = +10%)

**Integración:** `RolePassives.applyAttributes()` → verificar armadura + senda → aplicar bonificador.

---

## Pendientes (C5 continuación)

- [ ] Mapeo de sendas ↔ activas para cada rol (diseño)
- [ ] Mapeo de armaduras ↔ sendas (diseño)
- [ ] Integración en AbilityManager.invoke()
- [ ] Integración en RolePassives.applyAttributes()
- [ ] Textos de descripciones en 6 idiomas
- [ ] Pruebas con casos específicos (Guerrero + Defensa, Mago + Eficiencia)

## Notas

- Los bonificadores deben ser acotados (1.0 ≤ x ≤ 1.5)
- Sin modificadores negativos (no "penalizar")
- Sinergias apilables (múltiples sendas pueden modificar la misma activa)
