package com.mrbysco.spreadem.handlers;

import com.mrbysco.spreadem.data.SpawnData;
import com.mrbysco.spreadem.util.SpreadUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Set;

public class SpreadHandler {
	@SubscribeEvent
	public void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		if (player instanceof ServerPlayer serverPlayer) {
			ServerLevel serverLevel = serverPlayer.serverLevel();
			SpawnData spawnData = SpawnData.get(serverLevel);

			if (!spawnData.isPlayerKnown(player.getUUID())) {
				BlockPos generatedPos = SpreadUtil.generateSpawnPosition(serverPlayer);

				serverPlayer.setRespawnPosition(serverLevel.dimension(), generatedPos, serverLevel.getSharedSpawnAngle(), true, false);
				spawnData.addPlayer(player.getUUID(), generatedPos);
				spawnData.setDirty();

				BlockPos alteredSpawn = SpreadUtil.getFudgedSpawnPos(serverPlayer, generatedPos);
				serverPlayer.teleportTo(serverLevel, alteredSpawn.getX(), alteredSpawn.getY(), alteredSpawn.getZ(), Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot());
			}
		}
	}

	@SubscribeEvent
	public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		Player player = event.getEntity();
		if (player instanceof ServerPlayer serverPlayer) {
			ServerLevel serverLevel = serverPlayer.serverLevel();
			SpawnData spawnData = SpawnData.get(serverLevel);

			BlockPos position = spawnData.getSpawnPosition(player.getUUID());
			BlockPos respawnPos = serverPlayer.getRespawnPosition();
			if (respawnPos == null) {
				if (position != null) {
					respawnPos = position;
					serverPlayer.setRespawnPosition(serverPlayer.getRespawnDimension(), position, serverPlayer.getRespawnAngle(), true, false);
				}
			}

			if (position != null && position.equals(respawnPos)) {
				BlockPos alteredSpawn = SpreadUtil.getFudgedSpawnPos(serverPlayer, position);
				serverPlayer.teleportTo(serverLevel, alteredSpawn.getX(), alteredSpawn.getY(), alteredSpawn.getZ(), Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot());
			}
		}
	}
}
