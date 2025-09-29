package com.bo.cultivatereg.entity;

import com.bo.cultivatereg.registry.ModItems;
import com.bo.cultivatereg.world.HomelessManVillageData;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

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
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    // -------------------- AI & TICKING -------------------- //

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (this.greetingCooldown > 0) {
            this.greetingCooldown--;
        }

        this.anchorToTrashCan();

        if (this.level() instanceof ServerLevel serverLevel) {
            List<ServerPlayer> players = serverLevel.getEntitiesOfClass(ServerPlayer.class,
                    this.getBoundingBox().inflate(GREETING_RANGE), this::isValidGreetingTarget);

            Set<UUID> currentPlayers = new HashSet<>();
            for (ServerPlayer player : players) {
                UUID uuid = player.getUUID();
                currentPlayers.add(uuid);

                if (!this.playersInGreetingRange.contains(uuid) && this.greetingCooldown == 0) {
                    player.sendSystemMessage(this.createDialogue("message.cultivatereg.homeless_man.greeting"));
                    this.greetingCooldown = GREETING_COOLDOWN_TICKS;
                }
            }

            this.playersInGreetingRange.clear();
            this.playersInGreetingRange.addAll(currentPlayers);
        }
    }

    private boolean isValidGreetingTarget(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator() && !player.isRemoved() && !player.isInvisible();
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
            player.sendSystemMessage(this.createDialogue("message.cultivatereg.homeless_man.quest_start"));
            return InteractionResult.SUCCESS;
        }

        if (!this.isQuestComplete()) {
            if (stack.is(ModItems.BOOZE.get())) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.rewardAndDepart(player);

                ItemStack filthyManual = new ItemStack(ModItems.FILTHY_CULTIVATION_MANUAL.get());
                boolean added = player.addItem(filthyManual);
                if (!added) {
                    player.drop(filthyManual, false);
                }

                this.setQuestComplete(true);
                player.sendSystemMessage(this.createDialogue("message.cultivatereg.homeless_man.quest_complete"));
                return InteractionResult.SUCCESS;
            }

            player.sendSystemMessage(this.createDialogue("message.cultivatereg.homeless_man.quest_progress"));
            return InteractionResult.SUCCESS;
        }

        player.sendSystemMessage(this.createDialogue("message.cultivatereg.homeless_man.quest_after"));
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

    public void setTrashCanPos(@Nullable BlockPos pos) {
        this.trashCanPos = pos;
        if (pos != null) {
            this.restrictTo(pos.above(), 1);
        } else {
            this.clearRestriction();
        }
    }

    @Nullable
    public BlockPos getTrashCanPos() {
        return this.trashCanPos;
    }

    public void setVillageCenter(@Nullable BlockPos pos) {
        this.villageCenter = pos;
    }

    @Nullable
    public BlockPos getVillageCenter() {
        return this.villageCenter;
    }

    private void anchorToTrashCan() {
        if (this.trashCanPos == null) {
            return;
        }

        Vec3 anchor = Vec3.atCenterOf(this.trashCanPos).add(0.0D, 1.0D, 0.0D);
        double maxDistance = 0.75D;
        if (this.position().distanceToSqr(anchor) > maxDistance * maxDistance) {
            this.teleportTo(anchor.x, anchor.y, anchor.z);
            this.getNavigation().stop();
        }
    }

    private Component createDialogue(String translationKey) {
        Component prefix = Component.literal("Filthy Beggar")
                .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true));
        Component separator = Component.literal(" ⟫ ").withStyle(ChatFormatting.DARK_GRAY);
        Component line = Component.translatable(translationKey).withStyle(ChatFormatting.YELLOW);
        return Component.empty().append(prefix).append(separator).append(line);
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
    private void rewardAndDepart(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverPlayer.displayClientMessage(Component.translatable("message.cultivatereg.homeless_man.quest_complete"), true);

        ItemStack manual = new ItemStack(ModItems.FILTHY_CULTIVATION_MANUAL.get());
        if (!serverPlayer.addItem(manual)) {
            serverPlayer.spawnAtLocation(manual, 0.25F);
        }

        if (this.trashCanPos != null) {
            serverLevel.removeBlock(this.trashCanPos, false);
        }

        if (this.villageCenter != null) {
            HomelessManVillageData.get(serverLevel).markBanished(this.villageCenter);
        }

        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (lightning != null) {
            lightning.moveTo(this.getX(), this.getY(), this.getZ());
            lightning.setVisualOnly(true);
            serverLevel.addFreshEntity(lightning);
        }

        this.discard();
    }
}