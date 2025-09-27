package com.bo.cultivatereg.command;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.cultivation.*;
import com.bo.cultivatereg.network.Net;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Arrays;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID)
public class CultivationCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent e) {
        var d = e.getDispatcher();

        d.register(Commands.literal("cultivation")
                .then(Commands.literal("info").executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
                        msg(ctx.getSource(), "Realm=%s, Stage=%d, Qi=%.1f, Cap=%d, Meditating=%s, Sensed=%s, SenseProg=%.0f/100",
                                data.getRealm().name(), data.getStage(), data.getQi(),
                                data.getRealm()==Realm.MORTAL?0:data.getRealm().capForStage(data.getStage()),
                                data.isMeditating(), data.hasSensed(), data.getSenseProgress());
                    });
                    return 1;
                }))
                .then(Commands.literal("sense").executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
                        if (!data.hasSensed()) {
                            data.setSensed(true);
                            data.setRealm(Realm.QI_GATHERING);
                            data.setStage(1);
                            data.setQi(0f);
                            data.setSenseProgress(0f);
                            Net.sync(sp, data);
                        }
                    });
                    return 1;
                }))
                .then(Commands.literal("start").executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> { data.setMeditating(true); Net.sync(sp, data); });
                    return 1;
                }))
                .then(Commands.literal("stop").executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> { data.setMeditating(false); Net.sync(sp, data); });
                    return 1;
                }))
                .then(Commands.literal("addqi")
                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0f)).executes(ctx -> {
                            float amt = FloatArgumentType.getFloat(ctx, "amount");
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> { data.addQi(amt); Net.sync(sp, data); });
                            return 1;
                        })))
                .then(Commands.literal("setrealm")
                        .then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name").toUpperCase();
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
                                try {
                                    Realm r = Realm.valueOf(name);
                                    data.setRealm(r);
                                    data.setStage(r==Realm.MORTAL?1:1);
                                    data.setQi(0f);
                                    data.setSensed(r!=Realm.MORTAL);
                                    data.setSenseProgress(0f);
                                    Net.sync(sp, data);
                                } catch (IllegalArgumentException ex) {
                                    String valid = String.join(", ", Arrays.stream(Realm.values()).map(Enum::name).toList());
                                    msg(ctx.getSource(), "Unknown realm. Use: %s", valid);
                                }
                            });
                            return 1;
                        })))
                .then(Commands.literal("setstage")
                        .then(Commands.argument("stage", IntegerArgumentType.integer(1,9)).executes(ctx -> {
                            int st = IntegerArgumentType.getInteger(ctx, "stage");
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
                                if (data.getRealm()==Realm.MORTAL) {
                                    msg(ctx.getSource(),"MORTAL has no stages");
                                } else {
                                    data.setStage(st);
                                    data.setQi(0f);
                                    Net.sync(sp, data);
                                }
                            });
                            return 1;
                        })))
        );
    }

    private static void msg(CommandSourceStack src, String fmt, Object... args) {
        src.sendSuccess(() -> Component.literal(String.format(fmt, args)), false);
    }
}
