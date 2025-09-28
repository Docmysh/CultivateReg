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
                    Realm.MORTAL,
                    Realm.QI_GATHERING,
                    3,
                    List.of(
                            new ManualQuestion(
                                    "What is the first step before drawing Qi?",
                                    List.of(
                                            "Calm the mind and sense the flow",
                                            "Sprint around the sect",
                                            "Consume spirit stones"
                                    ),
                                    0
                            ),
                            new ManualQuestion(
                                    "Which meridians should be opened first?",
                                    List.of(
                                            "Any that resonate during meditation",
                                            "Only the heart meridian",
                                            "None, meridians open themselves"
                                    ),
                                    0
                            ),
                            new ManualQuestion(
                                    "How much Qi can this manual safely refine?",
                                    List.of(
                                            "Up to the third stage of Qi Gathering",
                                            "Until Foundation Establishment",
                                            "There is no limit"
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