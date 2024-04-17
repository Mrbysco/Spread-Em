package com.mrbysco.spreadem.data;

import com.mrbysco.spreadem.SpreadEm;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnData extends SavedData {
	private static final String DATA_NAME = SpreadEm.MOD_ID + "_spawn_data";

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

	public static SpawnData load(CompoundTag nbt) {
		Map<UUID, BlockPos> spawnMap = new HashMap<>();
		CompoundTag spawnMapCompound = nbt.getCompound("SpawnMap");
		for (String key : spawnMapCompound.getAllKeys()) {
			CompoundTag spawnCompound = spawnMapCompound.getCompound(key);
			UUID uuid = spawnCompound.getUUID("UUID");
			BlockPos pos = BlockPos.of(spawnCompound.getLong("Pos"));
			spawnMap.put(uuid, pos);
		}
		return new SpawnData(spawnMap);
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		CompoundTag spawnMapCompound = new CompoundTag();
		for (Map.Entry<UUID, BlockPos> entry : spawnMap.entrySet()) {
			CompoundTag spawnCompound = new CompoundTag();
			spawnCompound.putUUID("UUID", entry.getKey());
			spawnCompound.putLong("Pos", entry.getValue().asLong());
			spawnMapCompound.put(entry.getKey().toString(), spawnCompound);
		}
		compound.put("SpawnMap", spawnMapCompound);

		return compound;
	}

	public static SpawnData get(Level level) {
		if (!(level instanceof ServerLevel)) {
			throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
		}
		ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);

		DimensionDataStorage storage = overworld.getDataStorage();
		return storage.computeIfAbsent(SpawnData::load, SpawnData::new, DATA_NAME);
	}
}
