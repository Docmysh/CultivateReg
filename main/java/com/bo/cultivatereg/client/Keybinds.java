package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Keybinds {
    public static KeyMapping MEDITATE_KEY;
    public static KeyMapping MERIDIANS_KEY;
    public static KeyMapping REST_KEY;      // NEW
    public static KeyMapping SHIELD_KEY;    // NEW
    public static KeyMapping FLIGHT_KEY;    // NEW
    public static KeyMapping QI_SIGHT_KEY;  // NEW



    private static final String CAT = "key.categories.cultivatereg";

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent e) {
        MEDITATE_KEY = new KeyMapping("key.cultivatereg.meditate", GLFW.GLFW_KEY_C, CAT);
        MERIDIANS_KEY = new KeyMapping("key.cultivatereg.meridians", GLFW.GLFW_KEY_V, "key.categories.gameplay");

        REST_KEY      = new KeyMapping("key.cultivatereg.rest", GLFW.GLFW_KEY_B, CAT); // NEW
        SHIELD_KEY    = new KeyMapping("key.cultivatereg.qi_shield", GLFW.GLFW_KEY_H, CAT);
        FLIGHT_KEY    = new KeyMapping("key.cultivatereg.qi_flight", GLFW.GLFW_KEY_G, CAT);
        QI_SIGHT_KEY  = new KeyMapping("key.cultivatereg.qi_sight", GLFW.GLFW_KEY_N, CAT); // NEW



        e.register(MEDITATE_KEY);
        e.register(MERIDIANS_KEY);
        e.register(REST_KEY);
        e.register(SHIELD_KEY);
        e.register(FLIGHT_KEY);
        e.register(QI_SIGHT_KEY); // NEW
    }
}
