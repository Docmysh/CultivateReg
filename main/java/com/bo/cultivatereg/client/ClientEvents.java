package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.network.Net;
import com.bo.cultivatereg.network.StartFlightPacket;
import com.bo.cultivatereg.network.StartMeditatePacket;
import com.bo.cultivatereg.network.StartRestPacket;
import com.bo.cultivatereg.network.StartShieldPacket;
import com.bo.cultivatereg.network.StopFlightPacket;
import com.bo.cultivatereg.network.StopMeditatePacket;
import com.bo.cultivatereg.network.StopRestPacket;
import com.bo.cultivatereg.network.StopShieldPacket;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    private static boolean prevMeditate = false;
    private static boolean prevMeridians = false;
    private static boolean prevRest = false;
    private static boolean prevShield = false;
    private static boolean prevFlight = false;
    private static boolean prevQiSight = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // NEW: Cancel meditation/resting if the player moves.
        handleMovementCancellation(player);

        handleAnimation(player);
        handleKeybinds(player);
    }

    private static void handleMovementCancellation(LocalPlayer player) {
        player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
            if ((data.isMeditating() || data.isResting()) && player.input != null) {
                boolean isTryingToMove = player.input.up || player.input.down || player.input.left || player.input.right || player.input.jumping || player.input.shiftKeyDown;
                if (isTryingToMove) {
                    if (data.isMeditating()) {
                        Net.CHANNEL.sendToServer(new StopMeditatePacket());
                    }
                    if (data.isResting()) {
                        Net.CHANNEL.sendToServer(new StopRestPacket());
                    }
                }
            }
        });
    }

    private static void handleAnimation(AbstractClientPlayer player) {
        @SuppressWarnings("unchecked")
        var animationLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                .getPlayerAssociatedData(player)
                .get(PlayerAnimationSetup.ANIMATION_LAYER_ID);
        if (animationLayer == null) return;

        player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
            boolean shouldBePosing = data.isMeditating() || data.isResting();
            boolean isAnimating = animationLayer.getAnimation() != null;

            if (shouldBePosing && !isAnimating) {
                String key = data.isMeditating() ? "meditation" : "meditation"; // Fallback to meditation anim for now
                var animationData = PlayerAnimationRegistry.getAnimation(
                        ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, key));
                if (animationData != null) {
                    animationLayer.setAnimation(new KeyframeAnimationPlayer(animationData));
                }
            } else if (!shouldBePosing && isAnimating) {
                animationLayer.setAnimation(null);
            }
        });
    }

    private static void handleKeybinds(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (Keybinds.MEDITATE_KEY == null) return;

        CultivationData data = player.getCapability(CultivationCapability.CULTIVATION_CAP).orElse(null);
        boolean unlocked = data != null && data.isCultivationUnlocked();
        boolean qiSightNow = Keybinds.QI_SIGHT_KEY.isDown();
        boolean qiSightNow = Keybinds.QI_SIGHT_KEY.isDown();
        if (qiSightNow && !prevQiSight) {
            if (!unlocked) {
                player.displayClientMessage(Component.translatable("message.cultivatereg.cultivation.locked"), true);
            } else if (data != null && !data.hasSensed()) {
                player.displayClientMessage(Component.translatable("message.cultivatereg.cultivation.no_sense"), true);
            } else {
                boolean on = ClientState.toggleQiSight();
                player.displayClientMessage(Component.literal(on ? "Qi Sight: ON" : "Qi Sight: OFF"), true);
            }
        }
        prevQiSight = qiSightNow;

        boolean shieldNow = Keybinds.SHIELD_KEY.isDown();
        if (shieldNow && !prevShield) {
            if (!unlocked) {
                player.displayClientMessage(Component.translatable("message.cultivatereg.cultivation.locked"), true);
            } else if (data != null) {
                if (data.isShielding()) Net.CHANNEL.sendToServer(new StopShieldPacket());
                else Net.CHANNEL.sendToServer(new StartShieldPacket());
            }
        }
        prevShield = shieldNow;

        boolean flightNow = Keybinds.FLIGHT_KEY.isDown();
        if (flightNow && !prevFlight) {
            player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
                if (d.isFlying()) Net.CHANNEL.sendToServer(new StopFlightPacket());
                else Net.CHANNEL.sendToServer(new StartFlightPacket());
            });
        }
        prevFlight = flightNow;

        boolean meditateNow = Keybinds.MEDITATE_KEY.isDown();
        if (meditateNow && !prevMeditate) {
            if (!unlocked) {
                player.displayClientMessage(Component.translatable("message.cultivatereg.cultivation.locked"), true);
            } else if (data != null) {
                if (data.isShielding() || data.isFlying()) {
                    player.displayClientMessage(Component.literal("Cannot meditate while shielding or flying."), true);
                } else if (data.isMeditating()) {
                    Net.CHANNEL.sendToServer(new StopMeditatePacket());
                } else {
                    Net.CHANNEL.sendToServer(new StartMeditatePacket());
                }
            }
        }
        prevMeditate = meditateNow;

        boolean restNow = Keybinds.REST_KEY.isDown();
        if (restNow && !prevRest) {
            if (!unlocked) {
                player.displayClientMessage(Component.translatable("message.cultivatereg.cultivation.locked"), true);
            } else if (data != null) {
                if (data.isShielding() || data.isFlying()) {
                    player.displayClientMessage(Component.literal("Cannot rest while shielding or flying."), true);
                } else if (data.isResting()) {
                    Net.CHANNEL.sendToServer(new StopRestPacket());
                } else {
                    Net.CHANNEL.sendToServer(new StartRestPacket());
                }
            }
        }
        prevRest = restNow;

        boolean meridiansNow = Keybinds.MERIDIANS_KEY.isDown();
        if (meridiansNow && !prevMeridians) {
            if (!unlocked) {
                player.displayClientMessage(Component.translatable("message.cultivatereg.cultivation.locked"), true);
            } else if (data != null) {
                if (data.isMeditating()) {
                    mc.setScreen(new com.bo.cultivatereg.client.gui.MeridiansScreen());
                } else {
                    player.displayClientMessage(Component.literal("Meditate (C) to open meridians."), true);
                }
            }
        }
        prevMeridians = meridiansNow;
    }
}

