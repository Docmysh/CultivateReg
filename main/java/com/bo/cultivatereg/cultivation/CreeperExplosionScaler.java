package com.bo.cultivatereg.cultivation;

import com.bo.cultivatereg.CultivateReg;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID)
public class CreeperExplosionScaler {

    /** Re-entrancy guard: our custom explode also fires ExplosionEvent.Start. */
    private static boolean IN_CUSTOM = false;

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {
        // Absolute safety net: never let this handler throw.
        try {
            // Server only (client explosions shouldn’t be created anyway, but be safe).
            if (!(event.getLevel() instanceof ServerLevel level)) return;

            // If we're processing our own replacement explode, ignore.
            if (IN_CUSTOM) return;

            // getExplosion() should never be null here, but guard anyway.
            final var explosion = event.getExplosion();
            if (explosion == null) return;

            // The “exploder” can be null for some explosion types. We only care about Creepers.
            final Entity exploder = explosion.getExploder();
            if (!(exploder instanceof Creeper creeper)) return;

            // If creeper already removed for some reason, bail.
            if (!creeper.isAlive() || creeper.isRemoved()) return;

            // ----- YOUR SCALING -----
            // Vanilla: ~3.0F (charged ~6.0F). Clamp aggressively to avoid bad values.
            float base = creeper.isPowered() ? 6.0f : 3.0f;
            float power = Math.max(0.1f, Math.min(12.0f, base));

            // Cancel vanilla explosion & do our own exactly once.
            event.setCanceled(true);

            IN_CUSTOM = true;
            try {
                // Use the creeper as the source, like vanilla.
                level.explode(
                        creeper,
                        creeper.getX(),
                        creeper.getY(),
                        creeper.getZ(),
                        power,
                        Level.ExplosionInteraction.MOB
                );
                // Mirror vanilla: after exploding, the creeper is gone.
                creeper.discard();
            } finally {
                IN_CUSTOM = false;
            }
        } catch (Throwable t) {
            // If anything unexpected happens, DO NOT crash the game.
            // Let vanilla proceed by not cancelling the event.
            // (Optionally log: System.err.println(...) or use a logger.)
        }
    }
}
