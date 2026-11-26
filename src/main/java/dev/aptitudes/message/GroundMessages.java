package dev.aptitudes.message;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.InteractionEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

import java.util.UUID;

/**
 * C7 · S5 Mensajes en el suelo. Un jugador deja una frase predefinida ({@link GroundMessageRules#PHRASES}) con
 * {@code /aptitudes message <frase>} y los demás la leen y la valoran con clic derecho. Sin cliente nuevo y sin mixins:
 * cada nota son dos entidades de vanilla, un {@code text_display} (el texto, siempre de cara al lector) y una
 * {@code interaction} (la zona donde se hace clic), unidas por una etiqueta con el UUID de la primera. El estado y las
 * valoraciones se guardan en {@link GroundMessageState}; las entidades se guardan con el mundo.
 */
public final class GroundMessages {
	/** Etiqueta común; la de enlace es {@code TAG + ":" + uuid del text_display}. */
	public static final String TAG = Aptitudes.MOD_ID + ".message";
	private static final String LINK_PREFIX = TAG + ":";

	private GroundMessages() {}

	public enum PlaceResult { OK, DISABLED, BAD_PHRASE, LIMIT, COOLDOWN, FAILED }

	public static void register() {
		UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			if (world.isClient() || hand != Hand.MAIN_HAND || !(player instanceof ServerPlayerEntity sp) || sp.isSpectator()) return ActionResult.PASS;
			UUID id = linkOf(entity);
			if (id == null) return ActionResult.PASS;
			vote(sp, (ServerWorld) world, id);
			return ActionResult.SUCCESS;
		});
		// Entidad etiquetada que el estado no conoce (archivo perdido, nota ya caducada): se retira al cargarla.
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			UUID id = linkOf(entity);
			if (id == null || world.getServer() == null || world.getServer().getOverworld() == null) return;
			if (!GroundMessageState.get(world.getServer().getOverworld()).book().contains(id)) entity.discard();
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 200 == 0) expire(server);
		});
	}

	// ------------------------------------------------------------------ Colocar

	public static PlaceResult place(ServerPlayerEntity player, String phrase) {
		AptitudesConfig cfg = Aptitudes.config();
		if (!cfg.groundMessagesEnabled) return PlaceResult.DISABLED;
		if (!GroundMessageRules.validPhrase(phrase)) return PlaceResult.BAD_PHRASE;
		MinecraftServer server = player.getServer();
		if (server == null || server.getOverworld() == null || !(player.getWorld() instanceof ServerWorld world)) return PlaceResult.FAILED;
		ServerWorld overworld = server.getOverworld();
		GroundMessageState state = GroundMessageState.get(overworld);
		GroundMessageRules.Book book = state.book();
		long now = overworld.getTime();
		if (!GroundMessageRules.underLimit(book.countBy(player.getUuid()), cfg.groundMessagesPerPlayer)) return PlaceResult.LIMIT;
		if (!GroundMessageRules.cooldownReady(book.lastPlacedBy(player.getUuid()), now, cfg.groundMessageCooldownSeconds)) return PlaceResult.COOLDOWN;

		DisplayEntity.TextDisplayEntity text = EntityType.TEXT_DISPLAY.create(world);
		InteractionEntity zone = EntityType.INTERACTION.create(world);
		if (text == null || zone == null) return PlaceResult.FAILED;
		double x = player.getX(), y = player.getY() + 0.9, z = player.getZ();
		UUID id = text.getUuid();
		// El estado se anota ANTES de crear las entidades: ENTITY_LOAD retira las etiquetadas que no conoce.
		book.place(id, player.getUuid(), phrase, now);
		state.markDirty();

		text.setPosition(x, y, z);
		applyText(text, phrase, 0, world);
		zone.setUuid(zoneId(id)); // derivado: así se encuentran ambas entidades sin recorrer el mundo
		zone.setPosition(x, y - 0.5, z);
		NbtCompound zoneTag = new NbtCompound();
		zone.writeNbt(zoneTag);
		zoneTag.putFloat("width", 1.0f);
		zoneTag.putFloat("height", 1.0f);
		zoneTag.putBoolean("response", true);
		zone.readNbt(zoneTag);
		for (Entity e : new Entity[] {text, zone}) {
			e.addCommandTag(TAG);
			e.addCommandTag(LINK_PREFIX + id);
		}
		world.spawnEntity(text);
		world.spawnEntity(zone);
		world.playSound(null, x, y, z, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.8f, 1.2f);
		return PlaceResult.OK;
	}

	/** Escribe la frase y el recuento de valoraciones en el {@code text_display}. */
	private static void applyText(DisplayEntity.TextDisplayEntity text, String phrase, int votes, ServerWorld world) {
		Text content = Text.translatable("message.aptitudes.format", Text.translatable("message.aptitudes.phrase." + phrase), votes)
				.formatted(Formatting.YELLOW);
		NbtCompound tag = new NbtCompound();
		text.writeNbt(tag);
		tag.putString("text", Text.Serialization.toJsonString(content, world.getRegistryManager()));
		tag.putString("billboard", "center");
		tag.putInt("background", 0x66000000);
		tag.putInt("line_width", 160);
		tag.putFloat("view_range", 0.5f);
		tag.putBoolean("shadow", true);
		text.readNbt(tag);
	}

	// ------------------------------------------------------------------ Valorar y retirar

	private static void vote(ServerPlayerEntity player, ServerWorld world, UUID id) {
		MinecraftServer server = player.getServer();
		if (server == null || server.getOverworld() == null) return;
		GroundMessageState state = GroundMessageState.get(server.getOverworld());
		GroundMessageRules.VoteResult result = state.book().vote(id, player.getUuid());
		switch (result) {
			case OK -> {
				state.markDirty();
				GroundMessageRules.Entry entry = state.book().get(id);
				Entity text = findEntity(server, id);
				if (text instanceof DisplayEntity.TextDisplayEntity display && text.getWorld() instanceof ServerWorld textWorld) {
					applyText(display, entry.phrase, entry.votes(), textWorld);
				}
				world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
						SoundCategory.PLAYERS, 0.6f, 1.5f);
				player.sendMessage(Text.translatable("message.aptitudes.voted", entry.votes()).formatted(Formatting.GREEN), true);
			}
			case OWN -> player.sendMessage(Text.translatable("message.aptitudes.own").formatted(Formatting.GRAY), true);
			case ALREADY -> player.sendMessage(Text.translatable("message.aptitudes.already").formatted(Formatting.GRAY), true);
			case UNKNOWN -> { }
		}
	}

	/**
	 * Retira la nota propia más cercana a menos de 6 bloques (un operador puede retirar cualquiera).
	 * @return {@code true} si retiró alguna
	 */
	public static boolean removeNearest(ServerPlayerEntity player, boolean operator) {
		MinecraftServer server = player.getServer();
		if (server == null || server.getOverworld() == null) return false;
		GroundMessageState state = GroundMessageState.get(server.getOverworld());
		UUID best = null;
		double bestDistance = 36.0; // 6 bloques al cuadrado
		for (var e : state.book().entries().entrySet()) {
			if (!operator && !e.getValue().author.equals(player.getUuid())) continue;
			Entity entity = findEntity(server, e.getKey());
			if (entity == null || entity.getWorld() != player.getWorld()) continue;
			double d = entity.squaredDistanceTo(player);
			if (d <= bestDistance) {
				bestDistance = d;
				best = e.getKey();
			}
		}
		if (best == null) return false;
		discard(server, best);
		state.book().remove(best);
		state.markDirty();
		return true;
	}

	private static void expire(MinecraftServer server) {
		if (server.getOverworld() == null) return;
		GroundMessageState state = GroundMessageState.get(server.getOverworld());
		var expired = state.book().expiredIds(server.getOverworld().getTime(), Aptitudes.config().groundMessageLifetimeDays);
		if (expired.isEmpty()) return;
		for (UUID id : expired) {
			discard(server, id);
			state.book().remove(id);
		}
		state.markDirty();
	}

	// ------------------------------------------------------------------ Utilidades

	/** Quita las dos entidades de una nota si están cargadas; si no, las retira {@code ENTITY_LOAD} al cargarlas. */
	private static void discard(MinecraftServer server, UUID id) {
		for (UUID uuid : new UUID[] {id, zoneId(id)}) {
			Entity e = findEntity(server, uuid);
			if (e != null) e.discard();
		}
	}

	/** UUID de la zona de interacción de la nota {@code id}: el mismo con un bit invertido. */
	static UUID zoneId(UUID id) {
		return new UUID(id.getMostSignificantBits() ^ 1L, id.getLeastSignificantBits());
	}

	private static Entity findEntity(MinecraftServer server, UUID id) {
		for (ServerWorld w : server.getWorlds()) {
			Entity e = w.getEntity(id);
			if (e != null) return e;
		}
		return null;
	}

	/** UUID de la nota a la que pertenece la entidad, o {@code null} si no es una nota. */
	static UUID linkOf(Entity entity) {
		if (!entity.getCommandTags().contains(TAG)) return null;
		for (String tag : entity.getCommandTags()) {
			if (tag.startsWith(LINK_PREFIX)) {
				try {
					return UUID.fromString(tag.substring(LINK_PREFIX.length()));
				} catch (IllegalArgumentException ignored) {
					return null;
				}
			}
		}
		return null;
	}
}
