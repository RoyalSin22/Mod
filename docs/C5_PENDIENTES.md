# C5 Sinergias (v65) — estado y pendientes

**Revisado el 2026-11-24 contra el código.** El archivo anterior describía un estado previo a la integración
(«los bonificadores se calculan pero NO se aplican»); eso ya no es cierto. Qué se hizo y dónde:

## Resuelto (comprobado en el código)

| Id | Qué | Evidencia |
|---|---|---|
| Y1 | Radio y daño de la senda en activas | `ability/Targeting.hostilesAround` (línea 46) aplica `AbilityManager.synergyContext(player).radiusBonus()`; el daño pasa por el contexto de sinergia (`a084987`, `ce7204e`: activas nuevas y evolucionadas, golpes diferidos y tooltip de senda) |
| Y2 | XP de grupo con Resonancia | `party/PartyManager.awardCombatXp` (línea 131) multiplica por `SynergyRules.groupResonanceBonus(killer)` |
| Y3 | Daño de fusión con la runa correcta | `ability/ElementFusions.apply` (línea 79) calcula `SynergyRules.elementRuneSynergyBonus(...)` y lo aplica a Tormenta de fuego, Electrocución, etc. |
| Y4 | Armadura ↔ senda | `ability/RolePassives` (`efa926b` retira el modificador al quitarse la armadura) |
| Y5 | Combos ↔ piedras angulares | `ability/Keystones.triggerFromCombo` (línea 366), llamado desde `ability/Combos` (línea 173) con espera `synergyY5KeystoneCooldownSeconds` (`372bb81`) |
| Config | 8 opciones | `AptitudesConfig` (`enableSynergies`, `synergyBonusY1Radius/Damage/Energy`, `Y2Xp`, `Y3Damage`, `Y4Defense`, `Y5KeystoneCooldownSeconds`), con límites en `validated(...)` (`673aa09`) |
| Idiomas | Textos de sinergias | 13 claves `synergy.aptitudes.*` en en_us, es_es, pt_br, fr_fr y de_de (`dc32b18`, `6b1b7ab`) |
| HUD | Indicadores | `6b8c1f3`, `7e0ab82` |
| Logro | «Sinergista» (5 sinergias en una sesión) | `achievement/Achievement.SYNERGIST` |
| Comando | `/aptitudes synergy status` | `command/SynergyCommand` |

## Sigue abierto (opcional)

- [ ] **Validar en partida** Y1–Y5 (plan en `docs/V65_C5_TESTING_PLAN.md`, sección «En partida»). Sin datos reales.
- [ ] Logro «Resonancia perfecta» (grupo de 3+ con la misma piedra angular): no existe; solo está «Sinergista».
- [ ] Estadística de «total de bonificadores aplicados»: no existe.
- [ ] Tutorial: no menciona las sinergias (`tutorial/` no tiene referencias).
- [ ] Animación de partículas al activarse una sinergia (solo hay indicador de HUD).
- [ ] Balance de los multiplicadores (1,15 → ¿1,10?): decisión del autor tras probar en partida.
- [ ] Y4 podría dar más que +10 % de defensa (daño, velocidad…): idea, no decidida.
