# C5 Sinergias v65 — Estado y pendientes

**Estado (revisado el 2026-11-24 contra el código de `claude/practical-feynman-id8op1`):** las cinco sinergias
(Y1–Y5) están implementadas y conectadas. Lo que queda es **validarlas en partida** y algunos extras opcionales.
Este archivo sustituye a la versión anterior, que describía el estado de antes de integrar C5 y ya no era cierta.

Diseño: `docs/V65_SINERGIAS_Y1_Y4.md`, `Y2_RESONANCIA`, `Y3_RUNAS` y `Y5_COMBOS`. Guion de pruebas:
`docs/V65_C5_TESTING_PLAN.md`. Entrada de cambios: `CHANGELOG.md` («v65 C5 — Sinergias»).

## Resuelto (con la evidencia en el código)

| Id | Qué hace | Dónde se aplica | Commits |
|---|---|---|---|
| **Y1** Senda ↔ activas (radio, daño, coste) | El radio se multiplica en `Targeting.hostilesAround` (`AbilityManager.synergyContext(player).radiusBonus()`, `Targeting.java:46`); el daño, en `RolePassives.java:275` (`damageBonus()`); reglas puras en `Y1Rules` y `PathSynergyRules` | `ability/Targeting`, `RolePassives`, `AbilityManager` | `a204ef1`, `ce7204e` |
| **Y2** Resonancia de grupo (XP) | `PartyManager.awardCombatXp` multiplica el bono de grupo por `SynergyRules.groupResonanceBonus(killer)` (`PartyManager.java:131`) | `party/PartyManager`, `ability/SynergyRules` | `27b2c40` |
| **Y3** Elementos ↔ runas | `ElementFusions.apply` calcula `SynergyRules.elementRuneSynergyBonus` y lo aplica al daño de FIRESTORM, ELECTROCUTION, etc. (`ElementFusions.java:79`) | `ability/ElementFusions` | `27b2c40` |
| **Y4** Armadura ↔ senda | Modificador de defensa en `RolePassives`; se retira al quitarse la armadura | `ability/RolePassives`, `SynergyRules` | `efa926b` |
| **Y5** Combo ↔ piedra angular | `Keystones.triggerFromCombo` (`Keystones.java:366`), llamado desde `Combos.java:173` con espera global (`synergyY5KeystoneCooldownSeconds`) | `ability/Keystones`, `Combos` | `7e0ab82` |

Extras que el documento antiguo daba por pendientes y **ya están hechos**:

| Extra | Evidencia |
|---|---|
| Opciones de configuración | `AptitudesConfig` (`enableSynergies`, `synergyBonusY1Radius/Damage/Energy`, `Y2Xp`, `Y3Damage`, `Y4Defense`, `synergyY5KeystoneCooldownSeconds`) con límites en `validated(...)` (líneas 690-696) |
| Textos en los idiomas | Claves `synergy.aptitudes.*` en `en_us.json` (y resto de idiomas); commits `dc32b18` y `6b1b7ab` |
| HUD | `client/hud/SynergyHud`, avisado por `network/SynergyPayload` (`6b8c1f3`, `7e0ab82`) |
| Logro «Sinergista» (las cinco en una sesión) | `achievement/Achievement.SYNERGIST` |
| Comando `/aptitudes synergy status` | `command/SynergyCommand` |
| Tooltip de la senda en cada activa del árbol | `ce7204e` |
| Partículas al activarse | CHANGELOG, entrada «v65 C5» |
| Pruebas de lógica | `ability/SynergyMaskTest`, `ability/Y1RulesTest` |

## Sigue abierto

1. **Validación en partida de Y1–Y5** (obligatoria antes de dar C5 por cerrada). Guion en
   `docs/V65_C5_TESTING_PLAN.md`. El CHANGELOG ya lo declara: «sin pruebas en partida».
2. **Y4 podría aportar más** que la defensa (daño, velocidad). Es una decisión de diseño y de balance; no se
   toca sin pedirla.

## Opcional (polish; ninguno bloquea)

- [ ] Logro «Resonancia perfecta» (grupo de 3+ con la misma piedra angular) y estadística de bonificadores aplicados.
- [ ] Tutorial: explicar las sinergias en la pantalla inicial (`tutorial/TutorialRules`).
- [ ] Revisión de balance de los multiplicadores por defecto (1,20 / 1,15 / 0,90 / 1,15 / 1,20 / 1,10). **Decisión del
  autor** tras probar en partida.
