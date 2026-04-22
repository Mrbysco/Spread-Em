package com.mrbysco.spreadem.handlers;

import com.mrbysco.spreadem.SpreadEm;
import com.mrbysco.spreadem.data.SpawnData;
import com.mrbysco.spreadem.util.SpreadUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelData;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Set;

public class SpreadHandler {
	@SubscribeEvent
	public void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		if (player instanceof ServerPlayer serverPlayer) {
			ServerLevel serverLevel = serverPlayer.level();
			SpawnData spawnData = SpawnData.get(serverLevel);

			if (!spawnData.isPlayerKnown(player.getUUID())) {
				BlockPos generatedPos = SpreadUtil.generateSpawnPosition(serverPlayer);
				if (generatedPos == null) {
					SpreadEm.LOGGER.error("Failed to generate a spawn position for player {}.", player.getName().getString());
					return;
				}

				LevelData.RespawnData respawnData = serverLevel.getRespawnData();
				serverPlayer.setRespawnPosition(
						new ServerPlayer.RespawnConfig(new LevelData.RespawnData(
								GlobalPos.of(serverLevel.dimension(), generatedPos), respawnData.yaw(), respawnData.pitch()
						), true), false);

				spawnData.addPlayer(player.getUUID(), generatedPos);
				spawnData.setDirty();

				BlockPos alteredSpawn = SpreadUtil.getFudgedSpawnPos(serverPlayer, generatedPos);
				serverPlayer.teleportTo(serverLevel, alteredSpawn.getX(), alteredSpawn.getY(), alteredSpawn.getZ(), Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot(), false);
			}
		}
	}

	@SubscribeEvent
	public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		Player player = event.getEntity();
		if (player instanceof ServerPlayer serverPlayer) {
			ServerLevel serverLevel = serverPlayer.level();
			SpawnData spawnData = SpawnData.get(serverLevel);

			BlockPos position = spawnData.getSpawnPosition(player.getUUID());
			ServerPlayer.RespawnConfig respawnConfig = serverPlayer.getRespawnConfig();
			if (respawnConfig == null) {
				if (position != null) {
					LevelData.RespawnData respawnData = respawnConfig.respawnData();
					serverPlayer.setRespawnPosition(
							new ServerPlayer.RespawnConfig(new LevelData.RespawnData(
									GlobalPos.of(serverLevel.dimension(), position), respawnData.yaw(), respawnData.pitch()
							), true), false);
				}
			}

			if (position != null) {
				BlockPos alteredSpawn = SpreadUtil.getFudgedSpawnPos(serverPlayer, position);
				serverPlayer.teleportTo(serverLevel, alteredSpawn.getX(), alteredSpawn.getY(), alteredSpawn.getZ(), Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot(), false);
			}
		}
	}
}
