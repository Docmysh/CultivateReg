package com.bo.cultivatereg.entity;

import com.bo.cultivatereg.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HomelessManEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> QUEST_STARTED =
            SynchedEntityData.defineId(HomelessManEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> QUEST_COMPLETE =
            SynchedEntityData.defineId(HomelessManEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double GREETING_RANGE = 6.0D;
    private static final int GREETING_COOLDOWN_TICKS = 200;

    @Nullable
    private BlockPos trashCanPos;
    @Nullable
    private BlockPos villageCenter;

    private final Set<UUID> playersInGreetingRange = new HashSet<>();
    private int greetingCooldown;

    public HomelessManEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.greetingCooldown = 0;
    }

    // -------------------- ATTRIBUTES & GOALS -------------------- //

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    // -------------------- AI & TICKING -------------------- //

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (this.greetingCooldown > 0) {
            this.greetingCooldown--;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            List<ServerPlayer> players = serverLevel.getEntitiesOfClass(ServerPlayer.class,
                    this.getBoundingBox().inflate(GREETING_RANGE), this::isValidGreetingTarget);

            Set<UUID> currentPlayers = new HashSet<>();
            for (ServerPlayer player : players) {
                UUID uuid = player.getUUID();
                currentPlayers.add(uuid);

                if (!this.playersInGreetingRange.contains(uuid) && this.greetingCooldown == 0) {
                    player.sendSystemMessage(Component.translatable("message.cultivatereg.homeless_man.greeting"));
                    this.greetingCooldown = GREETING_COOLDOWN_TICKS;
                }
            }

            this.playersInGreetingRange.clear();
            this.playersInGreetingRange.addAll(currentPlayers);
        }
    }

    private boolean isValidGreetingTarget(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator() && !player.isRemoved() && !player.isInvisibleTo(this);
    }

    // -------------------- INTERACTION -------------------- //

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }

        if (!this.isQuestStarted()) {
            this.setQuestStarted(true);
            player.displayClientMessage(Component.translatable("message.cultivatereg.homeless_man.quest_start"), true);
            return InteractionResult.SUCCESS;
        } else if (!this.isQuestComplete()) {
            // Assuming ModItems.TRASH is your quest item
            if (stack.is(ModItems.booze)) {
                this.setQuestComplete(true);
                player.displayClientMessage(Component.translatable("message.cultivatereg.homeless_man.quest_complete"), true);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable("message.cultivatereg.homeless_man.quest_progress"), true);
            }
        } else {
            player.displayClientMessage(Component.translatable("message.cultivatereg.homeless_man.quest_after"), true);
        }

        return InteractionResult.SUCCESS;
    }

    // -------------------- DATA & NBT -------------------- //

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(QUEST_STARTED, false);
        this.entityData.define(QUEST_COMPLETE, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("QuestStarted", this.isQuestStarted());
        tag.putBoolean("QuestComplete", this.isQuestComplete());
        if (this.trashCanPos != null) {
            tag.put("TrashCanPos", NbtUtils.writeBlockPos(this.trashCanPos));
        }
        if (this.villageCenter != null) {
            tag.put("VillageCenter", NbtUtils.writeBlockPos(this.villageCenter));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setQuestStarted(tag.getBoolean("QuestStarted"));
        this.setQuestComplete(tag.getBoolean("QuestComplete"));
        if (tag.contains("TrashCanPos")) {
            this.trashCanPos = NbtUtils.readBlockPos(tag.getCompound("TrashCanPos"));
        }
        if (tag.contains("VillageCenter")) {
            this.villageCenter = NbtUtils.readBlockPos(tag.getCompound("VillageCenter"));
        }
    }

    public boolean isQuestStarted() {
        return this.entityData.get(QUEST_STARTED);
    }

    public void setQuestStarted(boolean started) {
        this.entityData.set(QUEST_STARTED, started);
    }

    public boolean isQuestComplete() {
        return this.entityData.get(QUEST_COMPLETE);
    }

    public void setQuestComplete(boolean complete) {
        this.entityData.set(QUEST_COMPLETE, complete);
    }

    // -------------------- SOUNDS & PHYSICS -------------------- //

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        SoundType soundType = state.getSoundType(this.level(), pos, this);
        this.playSound(soundType.getStepSound(), soundType.getVolume() * 0.15F, soundType.getPitch());
    }

    @Override
    public boolean isPushable() {
        return true;
    }
}