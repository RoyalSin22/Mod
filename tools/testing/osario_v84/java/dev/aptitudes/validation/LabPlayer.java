package dev.aptitudes.validation;

import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;

import java.util.UUID;

final class LabPlayer {
	private LabPlayer() {}

	/** Conexión local en memoria que conserva las reglas de supervivencia de ServerPlayerEntity. */
	static ServerPlayerEntity connect(ServerWorld world) {
		UUID id = UUID.randomUUID();
		var profile = new GameProfile(id, "qa_" + id.toString().substring(0, 8));
		var data = ConnectedClientData.createDefault(profile, false);
		var player = new ServerPlayerEntity(world.getServer(), world, profile, data.syncedOptions());
		var connection = new ClientConnection(NetworkSide.SERVERBOUND);
		new EmbeddedChannel(new ChannelHandler[] {connection});
		world.getServer().getPlayerManager().onPlayerConnect(connection, player, data);
		player.changeGameMode(GameMode.SURVIVAL);
		return player;
	}
}
