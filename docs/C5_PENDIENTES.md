# C5 Sinergias v65 — Pendientes Críticos

**Situación actual:** Base implementada pero **no funcional en partida** - bonificadores se calculan pero NO se aplican.

---

## 🔴 CRÍTICOS (Bloquean funcionamiento)

### Y1: Daño y radio NO se aplican

**Problema:** `contextForAbility()` calcula bonificadores pero no se usan.

**Falta:**
1. Aplicar `radiusBonus` en habilidades con área (Nova, Explosión, etc.)
2. Aplicar `damageBonus` en cálculo de daño

**Dónde:**
- `ability/ElementFusions.java` - aplicar Y1 bonus al daño de fusión
- `ability/CastFx.java` o handlers de AOE - aplicar radio
- `RolePassives.modifyOutgoingDamage()` - aplicar daño general

**Líneas de código:**
```java
// En ElementFusions u donde se calcula daño:
AbilitySynergyContext synergy = AbilityManager.getSynergyContext(player);
float damage = baseDamage * synergy.damageBonus();
```

---

### Y2: XP NO se multiplica con resonancia

**Problema:** `groupResonanceBonus()` está calculado pero nunca se llama.

**Falta:**
Integrar en `PartyManager.awardCombatXp()`:

```java
// En PartyManager.awardCombatXp() (actual: línea 127)
double bonus = PartyRules.bonus(...) 
             * SynergyRules.groupResonanceBonus(killer); // ← AGREGAR
```

**Ubicación exacta:**
`src/main/java/dev/aptitudes/party/PartyManager.java:127`

---

### Y3: Daño de runas NO se aplica

**Problema:** `elementRuneSynergyBonus()` existe pero nunca se invoca.

**Falta:**
Integrar en donde se calcula daño de fusión del Elementalista.

**Dónde buscar:**
- `ability/ElementalistKit.java` o `ElementalistRules.java`
- `ability/ElementFusions.java`
- Método que calcula daño de habilidades FIRE/ICE/LIGHTNING

**Patrón:**
```java
float damage = baseDamage;
ItemStack weapon = player.getMainHandStack();
Element activeElement = RoleManager.get(player).activeElement();
damage *= SynergyRules.elementRuneSynergyBonus(player, activeElement, weapon);
```

---

### Y5: Keystones NO se disparan

**Problema:** Se calcula cooldown pero nunca se dispara la piedra angular.

**Falta:**
Implementar `Keystones.triggerFromCombo()` en `Keystones.java`:

```java
public static void triggerFromCombo(ServerPlayerEntity player) {
    Set<Template> keystones = active(player);
    if (keystones.isEmpty()) return;
    
    Template template = keystones.iterator().next(); // Primera piedra
    
    // Ejecutar: tomar del código de Keystones (Overload, Bulwark, etc.)
    // Aplicar efecto de la template sin coste de energía
    // Emitir sonido/partículas
}
```

**Luego en Combos.java:**
```java
// En tryTriggerKeystoneOnCombo() (línea actual: "TODO: Implementar disparo real")
Keystones.triggerFromCombo(player);
```

---

## 🟡 IMPORTANTES (Mejor experiencia)

### Config: Opciones faltantes

**Falta en `AptitudesConfig`:**
```java
public boolean enableSynergies = true;
public float synergyBonusY1Radius = 1.20f;    // +20%
public float synergyBonusY1Damage = 1.15f;    // +15%
public float synergyBonusY1Energy = 0.90f;    // −10%
public float synergyBonusY2XP = 1.15f;        // +15%
public float synergyBonusY3Damage = 1.20f;    // +20%
public float synergyBonusY4Defense = 1.10f;   // +10%
public int synergyY5KeystoneCooldowSeconds = 20;
```

**Dónde:**
`src/main/java/dev/aptitudes/config/AptitudesConfig.java`

---

### Idiomas: Textos faltantes

**Falta agregar en 6 idiomas:**
- `src/main/resources/assets/aptitudes/lang/en_us.json`
- `src/main/resources/assets/aptitudes/lang/es_es.json`
- `src/main/resources/assets/aptitudes/lang/pt_br.json`
- `src/main/resources/assets/aptitudes/lang/fr_fr.json`
- `src/main/resources/assets/aptitudes/lang/de_de.json`

**Keys:**
```json
"synergy.aptitudes.y1_active": "Senda Synergy: +%d%% radius",
"synergy.aptitudes.y1_damage": "Senda Synergy: +%d%% damage",
"synergy.aptitudes.y2_active": "Group Resonance: +15%% XP",
"synergy.aptitudes.y3_active": "Element-Rune Synergy: +20%% damage",
"synergy.aptitudes.y4_active": "Armor Synergy: +10%% defense",
"synergy.aptitudes.y5_active": "Combo Keystone Triggered!"
```

---

### HUD: Indicadores visuales

**Falta:**
- Indicador en HUD cuando sinergia está activa
- Color/ícono diferente para cada Y
- Mostrar multiplicador actual (1.15, 1.20, etc.)

**Ubicación:**
`client/hud/AbilityHud.java` o crear `SynergyHud.java`

---

### Logros: Estadísticas

**Falta:**
- Achievement: "Sinergista" (usar 5 sinergias diferentes en una sesión)
- Achievement: "Resonancia perfecta" (grupo de 3+ con misma keystone)
- Estadísticas: total de bonificadores aplicados

---

## 🟢 OPCIONALES (Polish)

- [ ] Tooltip en habilidades mostrando bonificador Y1 activo
- [ ] Comando `/aptitudes synergy status` (mostrar active synergies)
- [ ] Animación de partículas cuando sinergia se activa
- [ ] Tutorial: explicar sinergias en pantalla inicial
- [ ] Balance: ajustar multipliers (1.15 → 1.10?, etc.)

---

## 📋 Plan de completitud

### Fase 1: Críticos (2-3 horas)
1. [ ] Y1: Integrar daño y radio en habilidades
2. [ ] Y2: Agregar bonus en PartyManager
3. [ ] Y3: Integrar en ElementFusions
4. [ ] Y5: Implementar triggerFromCombo()

### Fase 2: Importantes (1-2 horas)
5. [ ] Config: agregar campos C5
6. [ ] Idiomas: textos en 6 lenguas
7. [ ] HUD: indicadores visuales
8. [ ] Logros: achievements

### Fase 3: Polish (1 hora)
9. [ ] Tooltips, comandos, animaciones
10. [ ] Testing final en partida

---

## Checklist rápida

```bash
# Compilación actual
✅ ./gradlew build
✅ Todos los tests PASS
⚠️  Pero: bonificadores no se aplican en partida

# Activar Y1-Y5:
❌ Y1 daño/radio → EN PROGRESS
❌ Y2 XP → EN PROGRESS
❌ Y3 fusión → EN PROGRESS  
❌ Y4 defensa → DONE (ver nota abajo)
❌ Y5 keystones → NEAR (falta método)

# Nota Y4
✅ Integrado en RolePassives
✅ Aplica +10% KBR
⚠️  Pero: podría aplicar más bonificadores (daño, velocidad, etc.)
```

---

## Próximo paso recomendado

**Empezar por Y1** (más impactante, visible):
1. Buscar donde se calcula daño en habilidades
2. Pasar `AbilitySynergyContext` desde `AbilityManager`
3. Aplicar `damageBonus()` multiplicador
4. Compilar + testing rápido

**Tiempo estimado:** 30-45 minutos
