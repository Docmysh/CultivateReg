package com.bo.cultivatereg.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class HomelessManVillageData extends SavedData {
    private static final String DATA_NAME = "cultivatereg_homeless_man";

    private final Map<Long, Status> villages = new HashMap<>();

    public static HomelessManVillageData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(HomelessManVillageData::load, HomelessManVillageData::new, DATA_NAME);
    }

    public HomelessManVillageData() {
    }

    public static HomelessManVillageData load(CompoundTag tag) {
        HomelessManVillageData data = new HomelessManVillageData();
        ListTag list = tag.getList("Villages", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            long pos = entry.getLong("Pos");
            Status status = Status.valueOf(entry.getString("Status"));
            data.villages.put(pos, status);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<Long, Status> entry : this.villages.entrySet()) {
            CompoundTag data = new CompoundTag();
            data.putLong("Pos", entry.getKey());
            data.putString("Status", entry.getValue().name());
            list.add(data);
        }
        tag.put("Villages", list);
        return tag;
    }

    public boolean canSpawn(BlockPos pos) {
        Status status = this.villages.get(pos.asLong());
        return status == null || status == Status.NONE;
    }

    public void markSpawned(BlockPos pos) {
        this.villages.put(pos.asLong(), Status.SPAWNED);
        this.setDirty();
    }

    public void markBanished(BlockPos pos) {
        this.villages.put(pos.asLong(), Status.BANISHED);
        this.setDirty();
    }

    public void markAvailable(BlockPos pos) {
        this.villages.put(pos.asLong(), Status.NONE);
        this.setDirty();
    }

    public boolean isBanished(BlockPos pos) {
        Status status = this.villages.get(pos.asLong());
        return status == Status.BANISHED;
    }

    public boolean hasBanishedNearby(BlockPos pos, int radius) {
        return this.hasStatusWithinRadius(pos, Status.BANISHED, radius);
    }

    public boolean hasSpawnedNearby(BlockPos pos, int radius) {
        return this.hasStatusWithinRadius(pos, Status.SPAWNED, radius);
    }

    private boolean hasStatusWithinRadius(BlockPos pos, Status status, int radius) {
        int radiusSq = radius * radius;
        for (Map.Entry<Long, Status> entry : this.villages.entrySet()) {
            if (entry.getValue() != status) {
                continue;
            }

            BlockPos stored = BlockPos.of(entry.getKey());
            int dx = stored.getX() - pos.getX();
            int dz = stored.getZ() - pos.getZ();
            if (dx * dx + dz * dz <= radiusSq) {
                return true;
            }
        }
        return false;
    }

    private enum Status {
        NONE,
        SPAWNED,
        BANISHED
    }
}