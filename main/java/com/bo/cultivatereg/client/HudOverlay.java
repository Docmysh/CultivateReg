package com.bo.cultivatereg.client;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.cultivation.Realm;
import com.bo.cultivatereg.CultivateReg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
            int x = 8, y = 8;
            String realmLine;
            String qiLine;
            String actionLine;

            if (data.getRealm() == Realm.MORTAL && !data.hasSensed()) {
                realmLine = "Realm: MORTAL";
                qiLine = String.format("Sense: %.0f / 100", data.getSenseProgress());
                long ticks = mc.player.tickCount;
                int phase = (int)(ticks % 20L);
                String beat = (phase < 4) ? "●" : "○";
                actionLine = (data.isMeditating() ? "Tap C + tap V to open meridians " : "Hold C to start sensing ") + beat;
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

            draw(g, mc, realmLine, x, y,     0xFFFFFF);
            draw(g, mc, qiLine,    x, y+10,  0xA0FFE0);
            draw(g, mc, actionLine,x, y+20,  0xFFD080);

            // --- NEW: Spirit (mana) bar above the hunger bar ---
            drawSpiritBar(g, mc, data, w, h);
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
        stage = Mth.clamp(stage, 1, 9);
        return switch (realm) {
            case MORTAL -> 0;
            case QI_GATHERING -> 100 + 15 * (stage - 1);     // 100..220
            case FOUNDATION   -> 200 + 25 * (stage - 1);     // 200..400
            case CORE_FORMATION -> 400 + 50 * (stage - 1);   // 400..800
        };
    }

    // ---------- Helpers ----------

    private static void draw(GuiGraphics g, Minecraft mc, String s, int x, int y, int color) {
        g.drawString(mc.font, s, x, y, color, true);
    }
}
