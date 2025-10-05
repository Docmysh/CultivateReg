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

    public static final CultivationManual SALTCHEMIST_TIER_I = register(
            new CultivationManual(
                    ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "manual/saltchemist_tier_i"),
                    "Saltchemist Manual – Tier I",
                    "Introduces the Doctrine of Universal Union and teaches how to refine Virtuous Salts " +
                            "to carry a cultivator from Qi Gathering through Core Formation.",
                    """
                    Foundational Principle

                    The Saltchemist follows the Doctrine of Universal Union: that all substances, from the smallest grain of salt
                    to the flesh of the cultivator, are bound by hidden laws. By imbibing the Root Panacea, an ancient elixir
                    older than dynasties, the cultivator transmutes their body into a living crucible. Every breath becomes
                    distillation, every heartbeat a reaction.

                    Through this transformation, one gains dominion over Virtuous Salts—primordial catalysts that can bend
                    essence itself. These salts, when refined, can exalt allies with boons or corrode foes with ruin. Unlike other
                    paths, the Saltchemist requires sharpness of Mind and Intellect, for without understanding of balance,
                    reaction, and measure, the crucible body implodes under its own experiments.

                    Tier I – Crucible Awakening

                    Draw Qi into the Root Panacea to separate the Seven Base Salts. Circulate each salt through the limbs while
                    holding the dantian steady, then dissolve it back into the abdomen to temper marrow and meridians. Repeat the
                    cycle until the Virtuous Salts answer your will. As the crucible body stabilises, condense the salts into a
                    crystalline core within the dantian, fusing Foundation and Core Formation into a single, seamless reaction.
                    """,
                    "To advance, distil three batches of Seven Base Salts without impurity, then submerge the Heart Meridian in
                    brine until the Virtuous Salts resonate with your core.",
                    Realm.QI_GATHERING,
                    Realm.CORE_FORMATION,
                    9,
                    List.of()
            )
    );

    public static final CultivationManual SALTCHEMIST_TIER_II = register(
            new CultivationManual(
                    ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "manual/saltchemist_tier_ii"),
                    "Saltchemist Manual – Tier II",
                    "Expands the crucible body, teaching how to marshal Virtuous Salts to shepherd a Nascent Soul toward Void"
                            + " Refining.",
                    """
                    Tier II – Sublimation of the Nascent Crucible

                    With the core condensed, vent its vapours into the soul. Separate the Virtuous Salts into three arrays: the
                    Tempering Array to protect the nascent infant, the Exalting Array to feed allies and formations, and the
                    Corrosive Array to scour enemies. Alternate the arrays through the meridians so that no reaction stagnates,
                    then compress them into liquid halos around the soul.

                    As the halos spin, fold them back into the void within the dantian. Let the Nascent Soul steep within the
                    tri-fold crucible until it learns to exhale salt-mist on its own. When the mist no longer dissipates, the body
                    is prepared to leap from Soul Transformation into Void Refining without fracture.
                    """,
                    "Balance the Tempering, Exalting, and Corrosive arrays for nine cycles while sustaining an ally with their
                    boons; only then will the Virtuous Salts crystallise enough to breach the void.",
                    Realm.NASCENT_SOUL,
                    Realm.VOID_REFINING,
                    9,
                    List.of()
            )
    );

    public static final CultivationManual SALTCHEMIST_TIER_III = register(
            new CultivationManual(
                    ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "manual/saltchemist_tier_iii"),
                    "Saltchemist Manual – Tier III",
                    "Finalises the Saltchemist path by guiding the crucible body from Void Refining through Tribulation, uniting"
                            + " mortal flesh with cosmic salts.",
                    """
                    Tier III – Universal Union

                    Project the Virtuous Salts beyond the body until they anchor to the stars. Harvest celestial frost, oceanic
                    brine, and volcanic ash to create the Triune Panacea, then dissolve the crucible body within it. Allow every
                    breath to alternate between drawing cosmic salts inward and exhaling mortal impurities outward.

                    When the Triune Panacea balances perfectly, call down heaven's lightning and let it strike the crucible body.
                    Each bolt should be caught, distilled, and stored as a final Virtuous Salt. Once nine bolts are sealed, ignite
                    them all at once and step through the storm. The body that emerges will no longer fear Tribulation.
                    """,
                    "Offer the Triune Panacea to the heavens and endure nine lightning strikes without shattering the crucible
                    body; only then may the Saltchemist tread Integration and stand unbowed before Tribulation.",
                    Realm.VOID_REFINING,
                    Realm.TRIBULATION,
                    9,
                    List.of()
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