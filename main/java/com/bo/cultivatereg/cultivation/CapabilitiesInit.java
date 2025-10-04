package com.bo.cultivatereg.cultivation;

import com.bo.cultivatereg.CultivateReg;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CapabilitiesInit {
    private CapabilitiesInit() {}

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent e) {
        e.register(CultivationData.class);     // player cap
        e.register(MobCultivationData.class);  // mob cap
        e.register(com.bo.cultivatereg.aging.PlayerAgingData.class);
    }
}
