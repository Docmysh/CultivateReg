package com.bo.cultivatereg.entity;

import com.bo.cultivatereg.registry.ModItems;
import com.bo.cultivatereg.world.HomelessManVillageData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class HomelessManEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> QUEST_STARTED =
            SynchedEntityData.defineId(HomelessManEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> QUEST_COMPLETE =
            SynchedEntityData.defineId(HomelessManEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private BlockPos trashCanPos;
    @Nullable
    private BlockPos villageCenter;

    public HomelessManEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(QUEST_STARTED, Boolean.FALSE);
        this.entityData.define(QUEST_COMPLETE, Boolean.FALSE);
    }

    @Override
    public MobType getMobType() {
        return MobType.HUMAN;
    }

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
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.isQuestStarted()) {
            if (!this.level().isClientSide) {
                this.setQuestStarted(true);
                player.displayClientMessage(Component.translatable("message.cultivatereg.homeless_man.quest_start"), true);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (!this.isQuestComplete()) {
                if (stack.is(ModItems.BOOZE.get())) {
                    if (!this.level().isClientSide) {
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        this.setQuestComplete(true);
                        player.displayClientMessage(Component.translatable("message.cultivatereg.homeless_man.quest_complete"), true);
                        this.gameEvent(GameEvent.ENTITY_INTERACT);
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }

                if (!this.level().isClientSide) {
                    player.displayClientMessage(Component.translatable("message.cultivatereg.homeless_man.need_booze"), true);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (!this.level().isClientSide) {
                player.displayClientMessage(Component.translatable("message.cultivatereg.homeless_man.after_quest"), true);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        @Override
        public void die (DamageSource damageSource){
            Level level = this.level();
            BlockPos center = this.villageCenter;
            super.die(damageSource);
            if (level instanceof ServerLevel serverLevel && center != null) {
                HomelessManVillageData.get(serverLevel).markBanished(center);
            }
        }

        @Override
        public void remove (RemovalReason reason){
            Level level = this.level();
            BlockPos center = this.villageCenter;
            super.remove(reason);
            if (level instanceof ServerLevel serverLevel && center != null && reason == RemovalReason.DISCARDED) {
                HomelessManVillageData.get(serverLevel).markAvailable(center);
            }
        }
        public boolean isQuestStarted () {
            return this.entityData.get(QUEST_STARTED);
        }

        public void setQuestStarted ( boolean started){
            this.entityData.set(QUEST_STARTED, started);
        }

        public boolean isQuestComplete () {
            return this.entityData.get(QUEST_COMPLETE);
        }

        public void setQuestComplete ( boolean complete){
            this.entityData.set(QUEST_COMPLETE, complete);
        }

        @Nullable
        public BlockPos getTrashCanPos () {
            return this.trashCanPos;
        }

        public void setTrashCanPos (@Nullable BlockPos trashCanPos){
            this.trashCanPos = trashCanPos;
        }

        @Nullable
        public BlockPos getVillageCenter () {
            return this.villageCenter;
        }

        public void setVillageCenter (@Nullable BlockPos villageCenter){
            this.villageCenter = villageCenter;
        }

        @Override
        public void addAdditionalSaveData (CompoundTag tag){
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
        public void readAdditionalSaveData (CompoundTag tag){
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

        @Override
        public boolean isPushable () {
            return true;
        }

        @Override
        protected void playStepSound (BlockPos pos, net.minecraft.world.level.block.state.BlockState blockState){
            this.playSound(SoundEvents.VILLAGER_STEP, 0.15F, 1.0F);
        }

        public static AttributeSupplier.Builder createAttributes () {
            return Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 20.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.3D);
        }
    }
}