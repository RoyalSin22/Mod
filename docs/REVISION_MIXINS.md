# Revisión estática de mixins (2026-11-13, v67)

Comprobado contra el jar de Minecraft 1.21.1 con Yarn `1.21.1+build.3` (`javap` sobre la caché de Loom). Las dos
configuraciones (`aptitudes.mixins.json` y `aptitudes.client.mixins.json`) tienen `required: true` y
`defaultRequire = 1`, así que un objetivo inexistente impediría arrancar, salvo en los inyectores con `require = 0`.

## Resultado: 10 mixins, sin errores

| Mixin | Objetivo (firma Yarn verificada) | Tipo | `require` | Revisión |
|---|---|---|---|---|
| `LivingEntityMixin` | `LivingEntity.damage(DamageSource, float)` | `@ModifyVariable` HEAD, `float` ordinal 0 | 1 | Los cuatro manejadores (`RolePassives`, `UniqueEffects`, `Elites`, `Accessories`) salen si el mundo es del cliente |
| `MerchantEntityMixin` | `MerchantEntity.trade(TradeOffer)` | `@Inject` TAIL | 1 | Solo en el servidor y con un jugador de servidor como cliente del trato |
| `MobEntityMixin` | `MobEntity.setTarget(LivingEntity)` | `@Inject` HEAD cancelable | 1 | Única regla en `AbilityState.shouldBlockTarget`; los mobs con IA Brain los cubre `BrainTargetFilter` |
| `PersistentProjectileAccessor` | `PersistentProjectileEntity.setPierceLevel(byte)` (privado) | `@Invoker` | — | Firma `byte` correcta |
| `PlayerEntityMixin` | `getBlockBreakingSpeed(BlockState)`; `getBaseDimensions(EntityPose)` | `@Inject` RETURN / HEAD | 1 / 0 | Tipos de retorno (`Float`, `EntityDimensions`) correctos |
| `CameraMixin` | `Camera.update(BlockView, Entity, boolean, boolean, float)` | `@Inject` TAIL | 0 | `@Shadow` verificados: `setRotation(float, float)` protegido, `getYaw()`, `getPitch()` |
| `EntityRenderDispatcherMixin` | `EntityRenderDispatcher.render(E, double, double, double, float, float, MatrixStack, VertexConsumerProvider, int)` | `@Inject` HEAD cancelable | 1 | Sin recursión: la forma nunca es un jugador (`EntityType.PLAYER.create` devuelve `null`) |
| `EclipseFogMixin` | `BackgroundRenderer.render(...)` → `RenderSystem.clearColor(FFFF)`; `applyFogColor()` | `@ModifyArgs` / `@Inject` TAIL | 0 | La llamada `INVOKE` existe en el bytecode |
| `EclipseSkyMixin` | `ClientWorld.getSkyColor(Vec3d, float)`; `getCloudsColor(float)` | `@Inject` RETURN | 0 | Firmas correctas |
| `EclipseSunMixin` | `WorldRenderer.renderSky(...)` → `RenderSystem.setShaderTexture(I, Identifier)` ordinal 0 | `@ModifyArg` índice 1 | 0 | El ordinal 0 carga `SUN` (el 1 es `MOON_PHASES`) |

## Cambio aplicado

- **`EntityRenderDispatcherMixin`:** si el renderizador de la criatura de una forma lanza una excepción (por ejemplo,
  la de otro mod), antes cerraba el juego. Ahora se registra una vez y se dibuja el jugador normal; ese tipo de
  criatura no se vuelve a intentar en la sesión.

## Riesgos y limitaciones conocidos

- **`require = 0` en los mixins cosméticos** (cámara, Eclipse y caja de colisión de las formas): es intencionado, para
  que un cambio de versión no impida arrancar. A cambio, si un objetivo desaparece, el efecto se pierde sin error;
  revisarlos al portar a otra versión de Minecraft.
- **Shaders (Iris):** con un paquete de shaders, el cielo lo dibuja el shader, así que el tinte del cielo y el sol del
  Eclipse pueden no verse. No hay cierre ni error; solo se pierde el efecto visual.
- **Coste por fotograma:** los mixins de cámara y de dibujo de entidades hacen una comprobación de tipo y una búsqueda
  en un mapa por entidad; no es significativo.
