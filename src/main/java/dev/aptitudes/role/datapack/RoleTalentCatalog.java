package dev.aptitudes.role.datapack;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.network.TalentCatalogPayload;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleManager.UpgradeResult;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import dev.aptitudes.role.PathEffect;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Catálogo del piloto. Solo publica revisiones completas de los datapacks del servidor. */
public final class RoleTalentCatalog {
	public static final int MAX_NODES = TalentPackRules.MAX_NODES;
	public static final int MAX_ID_LENGTH = TalentPackRules.MAX_ID_LENGTH;
	private static final String DIRECTORY = "aptitudes/talents";
	private static final String SUFFIX = ".json";
	private static final Snapshot EMPTY = new Snapshot(0, Map.of());
	private static final Map<MinecraftServer, Snapshot> CATALOGS = new ConcurrentHashMap<>();

	public record Talent(Role role, String prerequisite, int level, int ranks, PathEffect effect) {}

	public record Snapshot(long revision, Map<String, Talent> talents) {
		public Snapshot {
			talents = Map.copyOf(talents);
		}
	}

	private RoleTalentCatalog() {}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> reload(server, server.getResourceManager()));
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
			if (success) reload(server, resources);
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(CATALOGS::remove);
	}

	public static Snapshot snapshot(MinecraftServer server) {
		return CATALOGS.getOrDefault(server, EMPTY);
	}

	private static void reload(MinecraftServer server, ResourceManager resources) {
		Snapshot previous = snapshot(server);
		Map<String, Talent> talents;
		try {
			talents = read(resources);
		} catch (IOException | IllegalArgumentException e) {
			Aptitudes.LOGGER.warn("Catálogo de talentos rechazado. Se conserva la revisión {}: {}",
					previous.revision(), e.getMessage());
			return;
		}
		Snapshot next = previous;
		if (!talents.equals(previous.talents())) {
			next = new Snapshot(previous.revision() + 1, talents);
			CATALOGS.put(server, next);
			server.getPlayerManager().getPlayerList().forEach(RoleManager::refresh);
		}
		Aptitudes.LOGGER.info("Catálogo de talentos por datapack, revisión {}: {} nodos",
				next.revision(), next.talents().size());
	}

	public static void reconcile(MinecraftServer server, RoleData data) {
		if (data.datapackRanks().isEmpty()) return;
		Map<String, TalentProgressRules.Requirement> requirements = new LinkedHashMap<>();
		snapshot(server).talents().forEach((id, node) -> {
			String role = node.role().key();
			String pre = node.prerequisite();
			if (pre.indexOf(':') >= 0) pre = TalentProgressRules.key(role, pre);
			requirements.put(TalentProgressRules.key(role, id), new TalentProgressRules.Requirement(
					data.level(node.role()), node.level(), node.ranks(), pre));
		});
		data.setDatapackRanks(TalentProgressRules.reconcile(data.datapackRanks(), requirements, data::rank));
	}

	public static void applySpeed(ServerPlayerEntity player, RoleData data) {
		int ranks = 0;
		if (!data.datapackRanks().isEmpty()) {
			for (var entry : snapshot(player.getServer()).talents().entrySet()) {
				Talent node = entry.getValue();
				if (node.effect() == PathEffect.SPEED && data.isActive(node.role())) {
					ranks += data.datapackRank(node.role(), entry.getKey());
				}
			}
		}
		PathEffect effect = PathEffect.SPEED;
		AttributeApplier.set(player, effect.attribute(), Aptitudes.id("datapack/speed"),
				TalentProgressRules.speedRanks(ranks) * effect.perRank(), effect.operation());
	}

	public static UpgradeResult purchase(ServerPlayerEntity player, String id, long revision, int expectedRank) {
		if (!player.isAlive() || player.isSpectator()) return UpgradeResult.DATAPACK_UNAVAILABLE;
		Snapshot catalog = snapshot(player.getServer());
		Talent node = catalog.talents().get(id);
		if (catalog.revision() != revision) return UpgradeResult.DATAPACK_STALE;
		if (node == null) return UpgradeResult.UNKNOWN_NODE;
		RoleData data = RoleManager.get(player);
		Role role = node.role();
		if (!data.isActive(role)) return UpgradeResult.ROLE_NOT_ACTIVE;
		int rank = data.datapackRank(role, id);
		if (!TalentProgressRules.matches(catalog.revision(), revision, rank, expectedRank)) {
			return UpgradeResult.DATAPACK_STALE;
		}
		if (rank >= node.ranks()) return UpgradeResult.MAX_RANK;
		if (data.level(role) < TalentProgressRules.levelForRank(node.level(), rank + 1)) {
			return UpgradeResult.LEVEL_TOO_LOW;
		}
		String pre = node.prerequisite();
		int prerequisiteRank = pre.indexOf(':') >= 0 ? data.datapackRank(role, pre) : data.rank(pre);
		if (prerequisiteRank <= 0) return UpgradeResult.MISSING_PREREQUISITE;
		if (RoleManager.availablePoints(data, role) <= 0) return UpgradeResult.NO_POINTS;
		data.learnDatapackRank(role, id, rank + 1);
		RoleManager.refresh(player);
		return UpgradeResult.OK;
	}

	public static void sync(ServerPlayerEntity player, RoleData data) {
		if (!ServerPlayNetworking.canSend(player, TalentCatalogPayload.ID)) return;
		Snapshot snapshot = snapshot(player.getServer());
		Map<String, TalentCatalogPayload.Entry> entries = new LinkedHashMap<>();
		snapshot.talents().forEach((id, node) -> entries.put(id, new TalentCatalogPayload.Entry(
				node.role(), node.prerequisite(), node.level(), node.ranks(), data.datapackRank(node.role(), id))));
		ServerPlayNetworking.send(player, new TalentCatalogPayload(snapshot.revision(), entries));
	}

	private static Map<String, Talent> read(ResourceManager resources) throws IOException {
		Map<Identifier, Resource> files = resources.findResources(DIRECTORY, id -> id.getPath().endsWith(SUFFIX));
		if (files.size() > TalentPackRules.MAX_NODES) {
			throw new IllegalArgumentException("Máximo " + TalentPackRules.MAX_NODES + " archivos de talento");
		}
		List<TalentPackRules.Node> definitions = new ArrayList<>(files.size());
		for (var entry : files.entrySet()) {
			Identifier file = entry.getKey();
			Resource resource = entry.getValue();
			String path = file.getPath();
			String id = file.getNamespace() + ":" + path.substring(DIRECTORY.length() + 1, path.length() - SUFFIX.length());
			try (var stream = resource.getInputStream()) {
				byte[] bytes = stream.readNBytes(TalentPackRules.MAX_FILE_BYTES + 1);
				if (bytes.length > TalentPackRules.MAX_FILE_BYTES) throw new IOException("Archivo de más de 8192 bytes");
				String json = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
				definitions.add(TalentPackRules.parse(id, json));
			} catch (IOException | IllegalArgumentException e) {
				throw new IOException(file + " (" + resource.getPackId() + "): " + e.getMessage(), e);
			}
		}
		TalentPackRules.validate(definitions, RoleTalentCatalog::roleExists, RoleTalentCatalog::builtinAnchor);
		Map<String, Talent> talents = new LinkedHashMap<>();
		for (TalentPackRules.Node node : definitions) {
			PathEffect effect = switch (node.template()) {
				case SPEED -> PathEffect.SPEED;
			};
			talents.put(node.id(), new Talent(Role.byKey(node.role()).orElseThrow(), node.prerequisite(),
					node.level(), node.ranks(), effect));
		}
		return talents;
	}

	private static boolean roleExists(String key) {
		return Role.byKey(key).filter(role -> role.key().equals(key)).isPresent();
	}

	private static TalentPackRules.Anchor builtinAnchor(String id) {
		RoleNode node = RoleTree.get(id);
		if (node == null || node.evolved() || node.kind() == RoleNode.Kind.PATH
				|| node.kind() == RoleNode.Kind.LEGENDARY) return null;
		return new TalentPackRules.Anchor(node.role().key(), node.requiredLevel());
	}
}
