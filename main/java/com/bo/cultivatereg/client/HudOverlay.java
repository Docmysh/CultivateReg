package com.bo.cultivatereg.client;

import com.bo.cultivatereg.aging.PlayerAgingCapability;
import com.bo.cultivatereg.aging.PlayerAgingData;
import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.cultivation.Realm;
import com.bo.cultivatereg.CultivateReg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID, value = Dist.CLIENT)
public class HudOverlay {

    public static final IGuiOverlay renderOverlay = (gui, g, partialTick, w, h) -> {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
            if (!data.isCultivationUnlocked()) {
                return;
            }
            int x = 8;
            int y = 8;
            String realmLine;
            String qiLine;
            String actionLine;

            // Updated logic for the "Mortal" stage display
            if (data.getRealm() == Realm.MORTAL && !data.hasSensed()) {
                realmLine = "Realm: MORTAL";
                // Changed "Sense" to "Breakthrough Progress"
                qiLine = String.format("Breakthrough: %d/12 Meridians", data.getOpenMeridianCount());
                long ticks = mc.player.tickCount;
                int phase = (int)(ticks % 20L);
                String beat = (phase < 4) ? "●" : "○";
                // Updated instructions to be more accurate
                actionLine = (data.isMeditating() ? "Press V to open Meridians " : "Hold C to Meditate ") + beat;
            } else {
                String stageStr = (data.getRealm() == Realm.MORTAL) ? "" : (" " + data.getStage() + "/9");
                int cap = (data.getRealm() == Realm.MORTAL) ? 0 : data.getRealm().capForStage(data.getStage());
                realmLine = "Realm: " + data.getRealm().name() + stageStr;
                qiLine = "Qi: " + (int)data.getQi() + " / " + cap;

                if (data.isMeditating()) {
                    actionLine = "Meditating (hold C)";
                } else if (data.isResting()) {
                    actionLine = "Resting (press B to stop)";
                } else {
                    actionLine = "Press C to Meditate • Press B to Rest";
                }
            }

            draw(g, mc, realmLine, x, y, 0xFFFFFF);
            y += 10;
            draw(g, mc, qiLine, x, y, 0xA0FFE0);
            y += 10;
            draw(g, mc, actionLine, x, y, 0xFFD080);
            y += 10;

            mc.player.getCapability(PlayerAgingCapability.PLAYER_AGING_CAP).ifPresent(age -> {
                String ageLine = formatAgeLine(age);
                if (!ageLine.isEmpty()) {
                    draw(g, mc, ageLine, x, y, 0xB0C4FF);
                }
            });

            // Spirit bar rendering remains the same
            drawSpiritBar(g, mc, data, w, h);

            // Replace massive heart stacks with a numeric health readout.
            drawHealthNumbers(g, mc, w, h);
        });
    };

    // ---------- Spirit bar rendering ----------

    private static void drawSpiritBar(GuiGraphics g, Minecraft mc, CultivationData data, int w, int h) {
        int cap = spiritCap(data.getRealm(), data.getStage());
        if (cap <= 0) return;

        float cur = data.getSpirit();
        if (cur <= 0.01f && !data.isResting()) return; // hide when empty unless actively resting

        int barW = 90; // visually similar span to hunger icons
        int barH = 6;

        // Position: right side, just above hunger row (mirrors vanilla HUD offsets)
        int x = (w / 2) + 91 - barW; // align to the right HUD cluster
        int y = h - 49;              // just above health/hunger rows

        // Background
        g.fill(x, y, x + barW, y + barH, 0xAA000000);

        // Fill
        int fillW = (int) (barW * Mth.clamp(cur / (float) cap, 0f, 1f));
        if (fillW > 0) {
            g.fill(x + 1, y + 1, x + 1 + fillW, y + barH - 1, 0xFF4FC3F7); // cyan-ish
        }

        // Border
        int border = 0xFF1E88E5;
        g.fill(x, y, x + barW, y + 1, border);
        g.fill(x, y + barH - 1, x + barW, y + barH, border);
        g.fill(x, y, x + 1, y + barH, border);
        g.fill(x + barW - 1, y, x + barW, y + barH, border);

        // Optional numeric readout (small)
        String txt = (int)cur + "/" + cap;
        int tx = x + barW - mc.font.width(txt) - 2;
        int ty = y - 9; // above the bar
        g.drawString(mc.font, txt, tx, ty, 0xAADCF7FF, false);
    }

    private static int spiritCap(Realm realm, int stage) {
        return realm.spiritCapForStage(stage);
    }

    private static void drawHealthNumbers(GuiGraphics g, Minecraft mc, int w, int h) {
        var player = mc.player;
        if (player == null) return;

        int x = (w / 2) - 91; // same anchor as vanilla hearts
        int y = h - 39;

        int health = Mth.ceil(player.getHealth());
        int max = Mth.ceil(player.getMaxHealth());
        int absorption = Mth.ceil(player.getAbsorptionAmount());

        String hp = "HP: " + health + " / " + max;
        if (absorption > 0) {
            hp += " (+" + absorption + ")";
        }

        int color = 0xFFE57373; // default warm red
        if (max > 0) {
            float pct = health / (float) max;
            if (pct >= 0.66f) color = 0xFFA5D6A7; // green
            else if (pct >= 0.33f) color = 0xFFFFF59D; // yellow
        }

        g.drawString(mc.font, hp, x, y, color, true);
    }

    // ---------- Helpers ----------

    private static void draw(GuiGraphics g, Minecraft mc, String s, int x, int y, int color) {
        g.drawString(mc.font, s, x, y, color, true);
    }

    private static String formatAgeLine(PlayerAgingData age) {
        if (age == null) return "";
        Component line = Component.translatable(
                "age.cultivatereg.label",
                age.getBiologicalDays(),
                age.getAgeBracket().displayName()
        );
        return line.getString();
    }
}