package com.bo.cultivatereg.cultivation.manual;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.cultivation.Realm;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static registry of built-in cultivation manuals.
 */
public final class CultivationManuals {
    private static final Map<ResourceLocation, CultivationManual> REGISTRY = new ConcurrentHashMap<>();

    public static final CultivationManual BASIC_QI_GATHERING = register(
            new CultivationManual(
                    ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "manual/basic_qi_gathering"),
                    "Basic Qi Gathering Manual",
                    "The first step on the path of cultivation is to sense and refine the world's Qi. " +
                            "This manual teaches the methods of breathing, visualization, and meridian " +
                            "opening that allow a mortal body to begin storing Qi within the dantian.",
                    """
                    "Close your eyes and let your breath become the bridge between heaven and earth. Inhale slowly, and imagine the mist of the mountains flowing into your lungs. Guide that mist downward, where it condenses into a single drop of light at your lower abdomen—the dantian. From there, let the light seek out the meridians, the hidden rivers within the flesh. Each channel you open allows Qi to flow more freely, strengthening body and spirit alike. Beware: forcing Qi through sealed meridians will cause backlash and destroy your foundation. Patience is the cultivator’s first weapon."
                    """,
                    "After reading, the player must correctly answer three questions. Passing represents true comprehension " +
                            "of the manual, which then unlocks the ability to open meridians and advance toward the Foundation Establishment stage.",
                    Realm.MORTAL,
                    Realm.QI_GATHERING,
                    3,
                    List.of(
                            new ManualQuestion(
                                    "What is the role of the dantian in Qi Gathering?",
                                    List.of(
                                            "It is the vessel where Qi condenses and is stored.",
                                            "It is the core that immediately transforms Qi into elemental powers.",
                                            "It is a chamber in the heart that circulates blood.",
                                            "It is the gate that summons spirits from the heavens."
                                    ),
                                    0
                            ),
                            new ManualQuestion(
                                    "How should Qi be guided through the body?",
                                    List.of(
                                            "Slowly and patiently through the meridians, never by force.",
                                            "By violently ramming Qi through until the body adapts.",
                                            "By storing it only in the lungs without moving it further.",
                                            "By letting it scatter freely without direction."
                                    ),
                                    0
                            ),
                            new ManualQuestion(
                                    "What danger comes from forcing Qi through unopened meridians?",
                                    List.of(
                                            "Backlash that can damage or destroy the foundation.",
                                            "The cultivator will instantly ascend to immortality.",
                                            "The meridians will vanish and never form again.",
                                            "The dantian will overflow and turn into a spirit beast."
                                    ),
                                    0
                            )
                    )
            )
    );

    private CultivationManuals() {
    }

    private static CultivationManual register(CultivationManual manual) {
        REGISTRY.put(manual.id(), manual);
        return manual;
    }

    public static CultivationManual byId(ResourceLocation id) {
        return REGISTRY.getOrDefault(id, BASIC_QI_GATHERING);
    }

    public static boolean exists(ResourceLocation id) {
        return REGISTRY.containsKey(id);
    }

    public static Map<ResourceLocation, CultivationManual> all() {
        return Map.copyOf(REGISTRY);
    }
}