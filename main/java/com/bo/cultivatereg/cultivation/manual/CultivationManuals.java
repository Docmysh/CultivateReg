package com.bo.cultivatereg.cultivation.manual;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.cultivation.Realm;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static registry of built-in cultivation manuals.
 */
public final class CultivationManuals {
    private static final Map<ResourceLocation, CultivationManual> REGISTRY = new ConcurrentHashMap<>();

    public static final CultivationManual FILTHY_BEGGAR_PRIMER = register(
            new CultivationManual(
                    ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "manual/filthy_beggar_primer"),
                    "I Wanted to Cultivate to Immortality, Yet Somehow Ended Up With Nothing But a Filthy Book From Some Homeless Guy",
                    "A stained primer that crudely sketches how mortals might pry open their first meridians.",
                    """
                    The Twelve Locks

                    There are twelve locks, each deeper than the last, each refusing entry until something is given.
                    Not all are needed—only three. Is there a need for more?

                    The first three will open with what drops from the broken.
                    The ones who arise at night.
                    Three stones. One for each gate.

                    The next three are bound in violet.
                    They do not rise, but hide.
                    Purple. Pulled from the below.
                    Three is enough.

                    Then comes the rest.
                    They do not like to be seen.
                    You will need the pearls of the tall ones, afraid of water.
                    Gather three. Don’t ask. Don’t look them in the eye.

                    The final three doors are old and cruel.
                    Three skulls, black as dried oil.
                    Put together again. Three times three. Nine in total.
                    Each of the three roar once more.
                    Now you must put them to rest.

                    Only then will the path open all the way through.
                    Only then will breath not escape you.

                    And when the locks are done, when you think the way is clear—
                    remember where I pissed.
                    Dive through the filth where the stream runs yellow.
                    There, beneath the stench, the last key waits.
                    """,
                    "Reading this foul manual unlocks your ability to perceive cultivation and begin opening meridians, though its guidance is maddeningly vague.",
                    Realm.MORTAL,
                    Realm.QI_GATHERING,
                    3,
                    List.of()
            )
    );

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
                            "of the manual, which then unlocks the ability to open meridians and advance toward the Qi Gathering realm.",
                    Realm.MORTAL,
                    Realm.QI_GATHERING,
                    3,
                    List.of(
                            new ManualQuestion(
                                    "What is the role of the dantian in Qi Gathering?",
                                    List.of(
                                            "Condense and store Qi",
                                            "Fuck if I know",
                                            "TLDR",
                                            "Cultivation bs"
                                    ),
                                    Set.of(0, 3)
                            ),
                            new ManualQuestion(
                                    "How should Qi be guided through the body?",
                                    List.of(
                                            "Slowly through the meridians.",
                                            "TLDR",
                                            "Js do it",
                                            "Cultivation bs?"
                                    ),
                                    Set.of(0, 3)
                            ),
                            new ManualQuestion(
                                    "What danger comes from forcing Qi through unopened meridians?",
                                    List.of(
                                            "A destroyed foundation.",
                                            "Clearly no danger",
                                            "TLDR",
                                            "Nothing cause cultivation bs"
                                    ),
                                    Set.of(0, 3)
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
        return REGISTRY.getOrDefault(id, FILTHY_BEGGAR_PRIMER);
    }

    public static boolean exists(ResourceLocation id) {
        return REGISTRY.containsKey(id);
    }

    public static Map<ResourceLocation, CultivationManual> all() {
        return Map.copyOf(REGISTRY);
    }
}