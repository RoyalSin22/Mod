package dev.aptitudes.network;

import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.data.PlayerAttribute;
import dev.aptitudes.logic.ProgressManager;
import dev.aptitudes.role.Element;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
	/** Límite por paquete para que un cliente modificado no pueda forzar bucles grandes. */
	private static final int MAX_AMOUNT_PER_REQUEST = 100;

	private ModNetworking() {
	}

	public static void registerCommon() {
		PayloadTypeRegistry.playS2C().register(SyncProgressPayload.ID, SyncProgressPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(RoleSyncPayload.ID, RoleSyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(EnergyPayload.ID, EnergyPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CooldownPayload.ID, CooldownPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(MorphPayload.ID, MorphPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(FormSyncPayload.ID, FormSyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(UniqueSyncPayload.ID, UniqueSyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(StatsPayload.ID, StatsPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(OpenBonfirePayload.ID, OpenBonfirePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(OpenRolesPayload.ID, OpenRolesPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(MechanicPayload.ID, MechanicPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ResourcePayload.ID, ResourcePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ScreenFxPayload.ID, ScreenFxPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(DamageNumberPayload.ID, DamageNumberPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SkillXpPayload.ID, SkillXpPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(LevelUpPayload.ID, LevelUpPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(OpenAccessoriesPayload.ID, OpenAccessoriesPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestStatsPayload.ID, RequestStatsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(AllocatePointsPayload.ID, AllocatePointsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(UseAbilityPayload.ID, UseAbilityPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RoleActionPayload.ID, RoleActionPayload.CODEC);

		// En 1.21 los handlers se ejecutan ya en el hilo del servidor.
		ServerPlayNetworking.registerGlobalReceiver(AllocatePointsPayload.ID, (payload, context) -> {
			PlayerAttribute[] attrs = PlayerAttribute.values();
			int index = payload.attributeIndex();
			if (index < 0 || index >= attrs.length) return;
			int amount = Math.max(1, Math.min(MAX_AMOUNT_PER_REQUEST, payload.amount()));
			ProgressManager.allocate(context.player(), attrs[index], amount);
		});

		ServerPlayNetworking.registerGlobalReceiver(LevelUpPayload.ID,
				(payload, context) -> dev.aptitudes.souls.SoulsManager.levelUp(context.player(), payload.allocations()));

		ServerPlayNetworking.registerGlobalReceiver(RequestStatsPayload.ID,
				(payload, context) -> context.responseSender().sendPacket(
						dev.aptitudes.logic.StatPreview.build(context.player())));

		ServerPlayNetworking.registerGlobalReceiver(UseAbilityPayload.ID,
				(payload, context) -> AbilityManager.use(context.player(), payload.slot()));

		ServerPlayNetworking.registerGlobalReceiver(OpenAccessoriesPayload.ID,
				(payload, context) -> dev.aptitudes.accessory.Accessories.open(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(RoleActionPayload.ID, (payload, context) -> {
			var player = context.player();
			switch (payload.action()) {
				case RoleActionPayload.SET_PRIMARY, RoleActionPayload.SET_SECONDARY -> {
					Role role = Role.byIndex(payload.roleIndex());
					if (role == null) return;
					RoleManager.ChangeResult result =
							RoleManager.setRole(player, role, payload.action() == RoleActionPayload.SET_PRIMARY);
					if (result == RoleManager.ChangeResult.NEEDS_RITUAL) {
						player.sendMessage(Text.translatable("aptitudes.role.needs_ritual").formatted(Formatting.RED), true);
					}
				}
				case RoleActionPayload.UPGRADE_NODE -> {
					RoleManager.UpgradeResult result = RoleManager.upgrade(player, payload.nodeId());
					if (result != RoleManager.UpgradeResult.OK) {
						player.sendMessage(Text.translatable("aptitudes.role.upgrade."
								+ result.name().toLowerCase(java.util.Locale.ROOT)).formatted(Formatting.RED), true);
					}
				}
				case RoleActionPayload.CHOOSE_ELEMENT -> {
					Element element = Element.byIndex(payload.roleIndex());
					if (element == null) return;
					RoleManager.ElementResult result = RoleManager.chooseElement(player, element);
					if (result != RoleManager.ElementResult.OK) {
						player.sendMessage(Text.translatable("aptitudes.element."
								+ result.name().toLowerCase(java.util.Locale.ROOT)).formatted(Formatting.RED), true);
					}
				}
				case RoleActionPayload.CYCLE_ELEMENT -> RoleManager.cycleElement(player);
				case RoleActionPayload.EQUIP_1, RoleActionPayload.EQUIP_2 ->
						RoleManager.equip(player, payload.nodeId(), payload.action() == RoleActionPayload.EQUIP_1 ? 0 : 1);
				case RoleActionPayload.SELECT_FORM -> {
					dev.aptitudes.morph.MorphForm form = dev.aptitudes.morph.MorphForm.byIndex(payload.roleIndex());
					if (form != null) dev.aptitudes.morph.MorphManager.select(player, form);
				}
				case RoleActionPayload.EVOLVE -> {
					Role role = Role.byIndex(payload.roleIndex());
					if (role == null) return;
					RoleManager.EvolveResult result = RoleManager.evolve(player, role);
					if (result != RoleManager.EvolveResult.OK) {
						player.sendMessage(Text.translatable("aptitudes.evolve."
								+ result.name().toLowerCase(java.util.Locale.ROOT)).formatted(Formatting.RED), true);
					}
				}
				default -> {
					// acción desconocida: se ignora (cliente modificado o versión distinta)
				}
			}
		});
	}
}
