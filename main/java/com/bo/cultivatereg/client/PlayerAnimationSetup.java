package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * This class registers our mod's animation layer with the Player Animator library.
 * It follows the official example provided by the library's author.
 */
@Mod.EventBusSubscriber(modid = CultivateReg.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PlayerAnimationSetup {

    // A unique ID for our animation layer.
    public static final ResourceLocation ANIMATION_LAYER_ID = ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "animation");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Register a factory for our animation layer.
        // This will be called for each player, allowing us to add our custom animation layer to them.
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                ANIMATION_LAYER_ID,
                1000, // A high priority, like in our previous attempts
                PlayerAnimationSetup::registerPlayerAnimation);
    }

    // This method creates the actual animation layer for a player.
    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        // We create a ModifierLayer, which is a versatile layer that lets us play, stop, and modify animations.
        return new ModifierLayer<>();
    }
}
