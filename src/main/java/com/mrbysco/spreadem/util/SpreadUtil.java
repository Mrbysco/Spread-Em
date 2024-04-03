package com.mrbysco.spreadem.util;

import com.mrbysco.spreadem.SpreadEm;
import com.mrbysco.spreadem.config.SpreadConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.Tags;

public class SpreadUtil {

	public static BlockPos generateSpawnPosition(ServerPlayer serverPlayer) {
		ServerLevel serverLevel = serverPlayer.serverLevel();
		RandomSource random = serverPlayer.getRandom();
		BlockPos spawnPos = serverPlayer.getRespawnPosition();
		if (spawnPos == null) {
			BlockPos fallBackPos = serverLevel.getSharedSpawnPos();
			//Generate a spawn position
			int tries = 10;
			BlockPos pos = null;
			while ((pos == null || isBlackListed(serverLevel.getBiome(pos))) && tries != 0) {
				int xPos = random.nextInt(SpreadConfig.COMMON.spreadDistance.get());
				if (random.nextBoolean()) xPos = -xPos;
				int YPos = random.nextInt(SpreadConfig.COMMON.spreadDistance.get());
				if (random.nextBoolean()) YPos = -YPos;

				BlockPos position;
				if (pos != null) {
					position = new BlockPos(pos.getX() + xPos, 0, pos.getZ() + YPos);
				} else {
					position = new BlockPos(xPos, 0, YPos);
				}
				pos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, position);
				tries--;
			}

			if (tries == 0) {
				spawnPos = fallBackPos;
				SpreadEm.LOGGER.warn("Could not find a suitable spawn position, falling back to world spawn");
			} else {
				spawnPos = pos;
			}
		}
		return spawnPos;
	}

	private static boolean isBlackListed(Holder<Biome> biomeHolder) {
		if (SpreadConfig.COMMON.blacklistOceans.get() && biomeHolder.is(Tags.Biomes.IS_WATER)) return true;

		if (SpreadConfig.COMMON.biomeBlacklist.get().isEmpty()) return false;
		for (String biome : SpreadConfig.COMMON.biomeBlacklist.get()) {
			ResourceLocation location = biomeHolder.unwrapKey().map(ResourceKey::location).orElse(null);
			if (location != null && location.toString().equals(biome)) {
				return true;
			}
		}
		return false;
	}
}
