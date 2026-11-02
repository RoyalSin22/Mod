package dev.aptitudes.client;

import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.client.hud.AbilityHud;
import dev.aptitudes.client.screen.AptitudesScreen;
import dev.aptitudes.client.screen.RolesScreen;
import dev.aptitudes.network.FormSyncPayload;
import dev.aptitudes.network.MorphPayload;
import dev.aptitudes.network.UniqueSyncPayload;
import dev.aptitudes.network.CooldownPayload;
import dev.aptitudes.network.EnergyPayload;
import dev.aptitudes.network.RoleActionPayload;
import dev.aptitudes.network.RoleSyncPayload;
import dev.aptitudes.network.SyncProgressPayload;
import dev.aptitudes.network.UseAbilityPayload;
import dev.aptitudes.sync.ClientCache;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AptitudesClient implements ClientModInitializer {
	private static final String CATEGORY = "category.aptitudes";
	/** Teclas por defecto: V/B (principal), N/M (secundario), R (definitiva), H (única), Z (característica). */
	private static final int[] DEFAULT_ABILITY_KEYS =
			{GLFW.GLFW_KEY_V, GLFW.GLFW_KEY_B, GLFW.GLFW_KEY_N, GLFW.GLFW_KEY_M, GLFW.GLFW_KEY_R, GLFW.GLFW_KEY_H,
				GLFW.GLFW_KEY_Z};

	private static KeyBinding openSkillsKey;
	private static KeyBinding openRolesKey;
	private static KeyBinding cycleElementKey;
	private static KeyBinding toggleHudKey;
	private static KeyBinding accessoriesKey;
	private static final KeyBinding[] abilityKeys = new KeyBinding[AbilityManager.SLOTS];

	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			if (dev.aptitudes.rune.RuneSocketing.eligible(stack)) dev.aptitudes.rune.RuneSocketing.appendTooltip(stack, lines, ClientServerConfig.get());
			dev.aptitudes.boss.BossManager.appendTooltip(stack, lines);
			if (stack.getItem() instanceof dev.aptitudes.boss.BossCoreItem) lines.add(net.minecraft.text.Text.translatable("boss.aptitudes.core_hint"));
		});
		openSkillsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.aptitudes.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY));
		openRolesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.aptitudes.roles", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY));
		cycleElementKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.aptitudes.cycle_element", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));
		ClientConfig.load();
		dev.aptitudes.item.GuideBookItem.opener = () -> net.minecraft.client.MinecraftClient.getInstance()
				.setScreen(new dev.aptitudes.client.screen.GuideScreen());
		toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.aptitudes.toggle_hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, CATEGORY));
		accessoriesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.aptitudes.accessories", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, CATEGORY));
		net.minecraft.client.gui.screen.ingame.HandledScreens.register(dev.aptitudes.accessory.AccessoryScreenHandler.TYPE,
				dev.aptitudes.client.screen.AccessoryScreen::new);
		net.minecraft.client.gui.screen.ingame.HandledScreens.register(dev.aptitudes.world.GuildCrafting.SCREEN,
				dev.aptitudes.client.screen.GuildDeskScreen::new);
		for (int i = 0; i < abilityKeys.length; i++) {
			abilityKeys[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding(
					"key.aptitudes.ability_" + (i + 1), InputUtil.Type.KEYSYM, DEFAULT_ABILITY_KEYS[i], CATEGORY));
		}

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientRoleState.tick();
			ClientEclipse.tick(client);
			ClientMorphs.tick(client);
			while (toggleHudKey.wasPressed()) {
				// Mayús + O: configuración del HUD (posición, escala, qué se muestra).
				if (net.minecraft.client.gui.screen.Screen.hasShiftDown()) {
					if (client.currentScreen == null) client.setScreen(new dev.aptitudes.client.screen.HudConfigScreen());
					continue;
				}
				ClientConfig.get().hudVisible = !ClientConfig.get().hudVisible;
				ClientConfig.save();
				if (client.player != null) {
					client.player.sendMessage(net.minecraft.text.Text.translatable(ClientConfig.get().hudVisible
							? "aptitudes.hud.shown" : "aptitudes.hud.hidden"), true);
				}
			}
			while (accessoriesKey.wasPressed()) {
				if (client.player != null && client.currentScreen == null
						&& ClientPlayNetworking.canSend(dev.aptitudes.network.OpenAccessoriesPayload.ID)) {
					ClientPlayNetworking.send(new dev.aptitudes.network.OpenAccessoriesPayload());
				}
			}
			while (openSkillsKey.wasPressed()) {
				if (client.player != null && client.currentScreen == null) client.setScreen(new AptitudesScreen());
			}
			while (openRolesKey.wasPressed()) {
				if (client.player != null && client.currentScreen == null) client.setScreen(new RolesScreen());
			}
			while (cycleElementKey.wasPressed()) {
				if (client.player != null && ClientPlayNetworking.canSend(RoleActionPayload.ID)) {
					ClientPlayNetworking.send(new RoleActionPayload(RoleActionPayload.CYCLE_ELEMENT, 0, ""));
				}
			}
			for (int i = 0; i < abilityKeys.length; i++) {
				while (abilityKeys[i].wasPressed()) {
					if (client.player != null && ClientPlayNetworking.canSend(UseAbilityPayload.ID)) {
						ClientPlayNetworking.send(new UseAbilityPayload(i));
					}
				}
			}
		});

		// Los receptores se ejecutan en el hilo del cliente (comportamiento por defecto en 1.21).
		ClientPlayNetworking.registerGlobalReceiver(SyncProgressPayload.ID,
				(payload, context) -> ClientCache.update(context.player().getUuid(), payload));
		ClientPlayNetworking.registerGlobalReceiver(RoleSyncPayload.ID,
				(payload, context) -> ClientRoleState.onSync(payload));
		ClientPlayNetworking.registerGlobalReceiver(EnergyPayload.ID,
				(payload, context) -> ClientRoleState.onEnergy(payload));
		ClientPlayNetworking.registerGlobalReceiver(dev.aptitudes.network.ResourcePayload.ID,
				(payload, context) -> ClientRoleState.onResources(payload));
		ClientPlayNetworking.registerGlobalReceiver(CooldownPayload.ID,
				(payload, context) -> ClientRoleState.onCooldown(payload));
		ClientPlayNetworking.registerGlobalReceiver(dev.aptitudes.network.ComboPayload.ID,
				(payload, context) -> dev.aptitudes.client.hud.ComboHud.onCombo(payload));
		ClientPlayNetworking.registerGlobalReceiver(dev.aptitudes.network.EclipsePayload.ID,
				(payload, context) -> ClientEclipse.onPayload(payload));
		// v40: configuración del servidor (valores reales en tooltips y pantalla de configuración).
		ClientPlayNetworking.registerGlobalReceiver(dev.aptitudes.network.ConfigSyncPayload.ID,
				(payload, context) -> ClientServerConfig.onSync(payload));
		ClientPlayNetworking.registerGlobalReceiver(dev.aptitudes.network.OpenConfigPayload.ID,
				(payload, context) -> context.client().setScreen(new dev.aptitudes.client.screen.ConfigScreen(null)));

		ClientPlayNetworking.registerGlobalReceiver(MorphPayload.ID,
				(payload, context) -> ClientMorphs.set(payload.player(), payload.typeId()));
		ClientPlayNetworking.registerGlobalReceiver(FormSyncPayload.ID,
				(payload, context) -> ClientExtraState.onForms(payload));
		ClientPlayNetworking.registerGlobalReceiver(dev.aptitudes.network.StatsPayload.ID,
				(payload, context) -> ClientExtraState.onStats(payload));
		ClientPlayNetworking.registerGlobalReceiver(UniqueSyncPayload.ID,
				(payload, context) -> ClientExtraState.onUniques(payload));

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			ClientCache.clear();
			ClientRoleState.clear();
			ClientMorphs.clear();
			ClientExtraState.clear();
			dev.aptitudes.client.hud.ClientFx.clear();
			AbilityHud.clear();
			ClientEclipse.clear();
			ClientServerConfig.clear();
		});

		HudRenderCallback.EVENT.register(AbilityHud::render);
		HudRenderCallback.EVENT.register(dev.aptitudes.client.hud.ClientFx::renderHud);
		HudRenderCallback.EVENT.register(dev.aptitudes.client.hud.ComboHud::render);
		HudRenderCallback.EVENT.register(dev.aptitudes.client.hud.EclipseHud::render);
		net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_ENTITIES.register(
				dev.aptitudes.client.hud.ClientFx::renderWorld);
		ClientPlayNetworking.registerGlobalReceiver(dev.aptitudes.network.MechanicPayload.ID,
				(payload, context) -> AbilityHud.onMechanics(payload));
		ClientPlayNetworking.registerGlobalReceiver(dev.aptitudes.network.ScreenFxPayload.ID,
				(payload, context) -> dev.aptitudes.client.hud.ClientFx.onScreenFx(payload));
		ClientPlayNetworking.registerGlobalReceiver(dev.aptitudes.network.DamageNumberPayload.ID,
				(payload, context) -> dev.aptitudes.client.hud.ClientFx.onDamageNumber(payload));
		ClientPlayNetworking.registerGlobalReceiver(dev.aptitudes.network.SkillXpPayload.ID,
				(payload, context) -> dev.aptitudes.client.hud.ClientFx.onSkillXp(payload));

		// Hoguera: modelo con planos de fuego recortados.
		net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(
				dev.aptitudes.block.ModBlocks.BONFIRE, net.minecraft.client.render.RenderLayer.getCutout());
		ClientPlayNetworking.registerGlobalReceiver(dev.aptitudes.network.OpenRolesPayload.ID,
				(payload, context) -> context.client().setScreen(new RolesScreen()));
		ClientPlayNetworking.registerGlobalReceiver(dev.aptitudes.network.OpenBonfirePayload.ID,
				(payload, context) -> context.client().setScreen(new AptitudesScreen(true)));
	}

	public static KeyBinding abilityKey(int slot) {
		return abilityKeys[slot];
	}
}
