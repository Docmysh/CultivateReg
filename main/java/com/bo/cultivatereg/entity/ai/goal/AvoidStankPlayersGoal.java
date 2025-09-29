package com.bo.cultivatereg.entity.ai.goal;

import com.bo.cultivatereg.registry.ModEffects;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;

public class AvoidStankPlayersGoal<T extends PathfinderMob> extends AvoidEntityGoal<Player> {
    public AvoidStankPlayersGoal(T mob, double walkSpeedModifier, double sprintSpeedModifier) {
        super(mob, Player.class, 8.0F, walkSpeedModifier, sprintSpeedModifier,
                player -> player.hasEffect(ModEffects.STANK.get()));
    }
}