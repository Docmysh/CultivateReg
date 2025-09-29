package com.bo.cultivatereg.world;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.entity.HomelessManEntity;
import com.bo.cultivatereg.registry.ModBlocks;
import com.bo.cultivatereg.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


import java.util.function.Predicate;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID)
public class HomelessManSpawner {
    private static final int CHECK_INTERVAL = 200;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!(event.level instanceof ServerLevel level) || event.phase != TickEvent.Phase.END) {
            return;
        }

        if (level.getGameTime() % CHECK_INTERVAL != 0) {
            return;
        }

        for (Player player : level.players()) {
            attemptVillageSpawn(level, player.blockPosition());
        }
    }

    private static void attemptVillageSpawn(ServerLevel level, BlockPos near) {
        PoiManager manager = level.getPoiManager();
        Predicate<Holder<PoiType>> predicate = holder -> holder.is(PoiTypes.MEETING);
        try (Stream<PoiRecord> stream = manager.getInRange(predicate, near, 64, PoiManager.Occupancy.ANY)) {
            stream.forEach(record -> trySpawnAt(level, record.getPos()));
        }
    }

    private static void trySpawnAt(ServerLevel level, BlockPos poiPos) {
        HomelessManVillageData data = HomelessManVillageData.get(level);
        if (data.isBanished(poiPos)) {
            return;
        }

        if (data.hasBanishedNearby(poiPos, 64)) {
            return;
        }

        if (!data.canSpawn(poiPos)) {
            // already spawned and still present
            return;
        }

        if (data.hasSpawnedNearby(poiPos, 64)) {
            return;
        }

        if (!level.getEntitiesOfClass(HomelessManEntity.class, new net.minecraft.world.phys.AABB(poiPos).inflate(32.0D)).isEmpty()) {
            return;
        }

        BlockPos spawnPos = findGround(level, poiPos);
        if (spawnPos == null) {
            return;
        }

        if (!level.getBlockState(spawnPos).canBeReplaced()) {
            return;
        }

        BlockPos below = spawnPos.below();
        BlockState ground = level.getBlockState(below);
        if (!ground.isFaceSturdy(level, below, Direction.UP)) {
            return;
        }

        level.setBlock(spawnPos, ModBlocks.DIRTY_TRASH_CAN.get().defaultBlockState(), 3);
        HomelessManEntity entity = ModEntities.HOMELESS_MAN.get().create(level);
        if (entity == null) {
            level.removeBlock(spawnPos, false);
            return;
        }
        RandomSource random = level.getRandom();
        BlockPos standPos = findStandPosition(level, spawnPos, random);
        entity.moveTo(standPos.getX() + 2D, standPos.getY(), standPos.getZ() + 0.5D,
                getFacingAngleTowards(standPos, spawnPos), 0.0F);
        entity.setTrashCanPos(spawnPos);
        entity.setVillageCenter(poiPos);
        level.addFreshEntity(entity);
        data.markSpawned(poiPos);
    }

    private static BlockPos findGround(ServerLevel level, BlockPos origin) {
        RandomSource random = level.getRandom();
        for (int attempts = 0; attempts < 10; attempts++) {
            int dx = random.nextInt(11) - 5;
            int dz = random.nextInt(11) - 5;
            BlockPos candidate = origin.offset(dx, 0, dz);
            BlockPos top = findSurface(level, candidate);
            if (top != null) {
                return top.above();
            }
        }
        return null;
    }

    private static BlockPos findSurface(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), level.getMaxBuildHeight(), pos.getZ());
        while (mutable.getY() > level.getMinBuildHeight()) {
            mutable.move(Direction.DOWN);
            BlockState state = level.getBlockState(mutable);
            if (!state.isAir()) {
                if (state.isFaceSturdy(level, mutable, Direction.UP)) {
                    return mutable.immutable();
                }
            }
        }
        return null;
    }
    private static BlockPos findStandPosition(ServerLevel level, BlockPos trashPos, RandomSource random) {
        for (Direction direction : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
            BlockPos groundPos = trashPos.relative(direction);
            BlockPos feetPos = groundPos.above();
            if (!level.isEmptyBlock(feetPos) || !level.isEmptyBlock(feetPos.above())) {
                continue;
            }

            BlockState groundState = level.getBlockState(groundPos);
            if (groundState.isFaceSturdy(level, groundPos, Direction.UP)) {
                return feetPos;
            }
        }

        return trashPos.above();
    }

    private static float getFacingAngleTowards(BlockPos from, BlockPos to) {
        double dx = (to.getX() + 0.5D) - (from.getX() + 0.5D);
        double dz = (to.getZ() + 0.5D) - (from.getZ() + 0.5D);
        return (float) (Math.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
    }
}