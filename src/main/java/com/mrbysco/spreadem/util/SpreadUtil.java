package com.mrbysco.spreadem.util;

import com.mrbysco.spreadem.SpreadEm;
import com.mrbysco.spreadem.config.SpreadConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;

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

				if (!serverLevel.isLoaded(position))
					serverLevel.getChunkAt(position); //Load the chunk

				pos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, position);

				tries--;
			}

			if (tries == 0) {
				spawnPos = fallBackPos;
				SpreadEm.LOGGER.warn("Could not find a suitable spawn position, falling back to world spawn");
			} else {
				spawnPos = pos;
			}

			if (spawnPos.getY() <= serverLevel.getMinBuildHeight()) {
				spawnPos = fallBackPos; //If the spawn position is below the minimum build height, fall back to world spawn
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

	public static BlockPos getFudgedSpawnPos(ServerPlayer serverPlayer, BlockPos blockpos) {
		ServerLevel serverLevel = serverPlayer.serverLevel();
		if (serverLevel.dimensionType().hasSkyLight() && serverLevel.getServer().getWorldData().getGameType() != GameType.ADVENTURE) {
			int spawnRadius = Math.max(0, serverPlayer.server.getSpawnRadius(serverLevel));
			int distToBorder = Mth.floor(serverLevel.getWorldBorder().getDistanceToBorder((double) blockpos.getX(), (double) blockpos.getZ()));
			if (distToBorder < spawnRadius) {
				spawnRadius = distToBorder;
			}

			if (distToBorder <= 1) {
				spawnRadius = 1;
			}

			long k = (long) (spawnRadius * 2L + 1);
			long l = k * k;
			int spawnArea = l > 2147483647L ? Integer.MAX_VALUE : (int) l;
			int j1 = spawnArea <= 16 ? spawnArea - 1 : 17;
			int k1 = RandomSource.create().nextInt(spawnArea);

			for (int l1 = 0; l1 < spawnArea; ++l1) {
				int i2 = (k1 + j1 * l1) % spawnArea;
				int j2 = i2 % (spawnRadius * 2 + 1);
				int k2 = i2 / (spawnRadius * 2 + 1);
				BlockPos overworldPos = getOverworldRespawnPos(serverLevel, blockpos.getX() + j2 - spawnRadius, blockpos.getZ() + k2 - spawnRadius);
				if (overworldPos != null) {
					return overworldPos;
				}
			}
		}
		return blockpos;
	}

	@Nullable
	protected static BlockPos getOverworldRespawnPos(ServerLevel serverLevel, int pX, int pZ) {
		boolean hasCeiling = serverLevel.dimensionType().hasCeiling();
		LevelChunk levelchunk = serverLevel.getChunk(SectionPos.blockToSectionCoord(pX), SectionPos.blockToSectionCoord(pZ));
		int i = hasCeiling ?
				serverLevel.getChunkSource().getGenerator().getSpawnHeight(serverLevel) :
				levelchunk.getHeight(Heightmap.Types.MOTION_BLOCKING, pX & 15, pZ & 15);
		if (i >= serverLevel.getMinBuildHeight()) {
			int j = levelchunk.getHeight(Heightmap.Types.WORLD_SURFACE, pX & 15, pZ & 15);
			if (j > i || j <= levelchunk.getHeight(Heightmap.Types.OCEAN_FLOOR, pX & 15, pZ & 15)) {
				BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

				for (int k = i + 1; k >= serverLevel.getMinBuildHeight(); --k) {
					blockpos$mutableblockpos.set(pX, k, pZ);
					BlockState blockstate = serverLevel.getBlockState(blockpos$mutableblockpos);
					if (!blockstate.getFluidState().isEmpty()) {
						break;
					}

					if (Block.isFaceFull(blockstate.getCollisionShape(serverLevel, blockpos$mutableblockpos), Direction.UP)) {
						return blockpos$mutableblockpos.above().immutable();
					}
				}

			}
		}
		return null;
	}
}
