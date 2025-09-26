// src/main/java/com/bo/cultivatereg/client/ClientEvents.java
package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.network.Net;
import com.bo.cultivatereg.network.SenseAttemptPacket;
import com.bo.cultivatereg.network.StartFlightPacket;
import com.bo.cultivatereg.network.StartMeditatePacket;
import com.bo.cultivatereg.network.StartRestPacket;
import com.bo.cultivatereg.network.StartShieldPacket;
import com.bo.cultivatereg.network.StopFlightPacket;
import com.bo.cultivatereg.network.StopMeditatePacket;
import com.bo.cultivatereg.network.StopRestPacket;
import com.bo.cultivatereg.network.StopShieldPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {
    private static boolean prevC = false;
    private static boolean prevSense = false;
    private static boolean prevRest = false;
    private static boolean prevShield = false;
    private static boolean prevFlight = false;
    private static boolean prevQiSight = false;
    private static boolean prevHeaven = false; // track Heavenly Sword key state

    // when R was pressed to start charging (client-side ticks)
    private static int chargeStartClientTicks = -1;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || Keybinds.MEDITATE_KEY == null) return;



        // --- Qi Sight toggle (client-only) ---
        if (Keybinds.QI_SIGHT_KEY != null) {
            boolean now = Keybinds.QI_SIGHT_KEY.isDown();
            if (now && !prevQiSight) {
                boolean on = ClientState.toggleQiSight();
                mc.player.displayClientMessage(Component.literal(on ? "Qi Sight: ON" : "Qi Sight: OFF"), true);
            }
            prevQiSight = now;
        }

        // --- Shield toggle ---
        if (Keybinds.SHIELD_KEY != null) {
            boolean now = Keybinds.SHIELD_KEY.isDown();
            if (now && !prevShield) {
                mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
                    if (d.isShielding()) Net.CHANNEL.send(PacketDistributor.SERVER.noArg(), new StopShieldPacket());
                    else                 Net.CHANNEL.send(PacketDistributor.SERVER.noArg(), new StartShieldPacket());
                });
            }
            prevShield = now;
        }

        // --- Flight toggle ---
        if (Keybinds.FLIGHT_KEY != null) {
            boolean now = Keybinds.FLIGHT_KEY.isDown();
            if (now && !prevFlight) {
                mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
                    if (d.isFlying()) Net.CHANNEL.send(PacketDistributor.SERVER.noArg(), new StopFlightPacket());
                    else               Net.CHANNEL.send(PacketDistributor.SERVER.noArg(), new StartFlightPacket());
                });
            }
            prevFlight = now;
        }

        // --- Meditate toggle (C) — blocked if shielding/flying ---
        boolean nowC = Keybinds.MEDITATE_KEY.isDown();
        if (nowC && !prevC) {
            mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent((CultivationData d) -> {
                if (d.isShielding() || d.isFlying()) {
                    mc.player.displayClientMessage(Component.literal("Cannot meditate while shielding or flying."), true);
                    return;
                }
                if (d.isMeditating()) Net.CHANNEL.send(PacketDistributor.SERVER.noArg(), new StopMeditatePacket());
                else                  Net.CHANNEL.send(PacketDistributor.SERVER.noArg(), new StartMeditatePacket());
            });
        }
        prevC = nowC;

        // --- Rest toggle (B) — blocked if shielding/flying ---
        if (Keybinds.REST_KEY != null) {
            boolean nowRest = Keybinds.REST_KEY.isDown();
            if (nowRest && !prevRest) {
                mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent((CultivationData d) -> {
                    if (d.isShielding() || d.isFlying()) {
                        mc.player.displayClientMessage(Component.literal("Cannot rest while shielding or flying."), true);
                        return;
                    }
                    if (d.isResting()) Net.CHANNEL.send(PacketDistributor.SERVER.noArg(), new StopRestPacket());
                    else               Net.CHANNEL.send(PacketDistributor.SERVER.noArg(), new StartRestPacket());
                });
            }
            prevRest = nowRest;
        }

        // --- Movement interrupt for rest/meditation ---
        mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent((CultivationData d) -> {
            if ((d.isResting() || d.isMeditating()) && mc.player.input != null) {
                boolean moving = mc.player.input.up || mc.player.input.down || mc.player.input.left || mc.player.input.right
                        || mc.player.input.jumping || mc.player.input.shiftKeyDown;
                if (moving) {
                    if (d.isResting())    Net.CHANNEL.send(PacketDistributor.SERVER.noArg(), new StopRestPacket());
                    if (d.isMeditating()) Net.CHANNEL.send(PacketDistributor.SERVER.noArg(), new StopMeditatePacket());
                }
            }
        });

        // --- Rhythm minigame tap (V) ---
        // --- Meridians GUI (press V) ---
        if (Keybinds.MERIDIANS_KEY != null) {
            boolean now = Keybinds.MERIDIANS_KEY.isDown();
            if (now && !prevSense) {
                mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
                    if (d.isMeditating() && d.getRealm() == com.bo.cultivatereg.cultivation.Realm.MORTAL) {
                        mc.setScreen(new com.bo.cultivatereg.client.gui.MeridiansScreen());
                    } else {
                        mc.player.displayClientMessage(Component.literal("Meditate (C) to open meridians."), true);
                    }
                });
            }
            prevSense = now; // reuse the boolean we already had
        }

    }
    }

