package com.mrbysco.spreadem.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrbysco.spreadem.SpreadEm;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnData extends SavedData {
	private static final Identifier DATA_NAME = Identifier.fromNamespaceAndPath(SpreadEm.MOD_ID, "spawn_data");

	public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
					Codec.unboundedMap(UUIDUtil.STRING_CODEC, BlockPos.CODEC).fieldOf("spawnMap").forGetter(data -> data.spawnMap))
			.apply(inst, SpawnData::new));
	
	private final Map<UUID, BlockPos> spawnMap = new HashMap<>();

	public SpawnData(Map<UUID, BlockPos> spawnMap) {
		if (!spawnMap.isEmpty()) {
			this.spawnMap.putAll(spawnMap);
		}
	}

	public SpawnData() {
		this(new HashMap<>());
	}

	public boolean isPlayerKnown(UUID uuid) {
		return spawnMap.containsKey(uuid);
	}

	public void addPlayer(UUID uuid, BlockPos pos) {
		spawnMap.put(uuid, pos);
		setDirty();
	}

	public BlockPos getSpawnPosition(UUID uuid) {
		return spawnMap.getOrDefault(uuid, null);
	}

	public static SavedDataType<SpawnData> type() {
		return new SavedDataType<>(DATA_NAME, SpawnData::new, CODEC);
	}

	public static SpawnData get(Level level) {
		if (!(level instanceof ServerLevel)) {
			throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
		}
		ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);

		assert overworld != null;
		SavedDataStorage storage = overworld.getDataStorage();
		return storage.computeIfAbsent(type());
	}
}
