// src/main/java/com/bo/cultivatereg/client/gui/MeridiansScreen.java
package com.bo.cultivatereg.client.gui;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.cultivation.Realm;
import com.bo.cultivatereg.cultivation.manual.CultivationManuals;
import com.bo.cultivatereg.network.BreakthroughPacket;
import com.bo.cultivatereg.network.Net;
import com.bo.cultivatereg.network.SenseAttemptPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MeridiansScreen extends Screen {
    private static final int R = 56; // ring radius
    private int cx, cy;
    private Button breakthrough;

    public MeridiansScreen() {
        super(Component.literal("Meridians"));
    }

    @Override
    protected void init() {
        cx = this.width / 2;
        cy = this.height / 2;

        // 12 meridian buttons in a ring
        for (int i = 0; i < CultivationData.MERIDIANS; i++) {
            double a = Math.toRadians((360.0 / CultivationData.MERIDIANS) * i - 90);
            int x = cx + (int) (Math.cos(a) * R) - 16;
            int y = cy + (int) (Math.sin(a) * R) - 10;

            final int idx = i;
            this.addRenderableWidget(Button.builder(Component.literal(Integer.toString(i + 1)),
                            b -> guide(idx))
                    .pos(x, y).size(32, 20).build());
        }

        // Breakthrough button
        this.breakthrough = this.addRenderableWidget(
                Button.builder(Component.literal("Breakthrough"), b -> doBreakthrough())
                        .pos(cx - 48, cy + R + 28).size(96, 20).build()
        );
        this.breakthrough.active = false; // enabled dynamically
    }

    /** Send a guide-qi attempt to the server for meridian idx. */
    private void guide(int idx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
            if (!d.isMeditating()) return;
            if (d.isMeridianOpen(idx)) return;
            Net.CHANNEL.sendToServer(new SenseAttemptPacket(idx));
        });
    }

    /** Try to break through from MORTAL to QI_GATHERING (server-validated). */
    private void doBreakthrough() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
            var manual = CultivationManuals.byId(d.getManualId());
            boolean manualOk = d.isManualQuizPassed()
                    && manual.canBreakthroughFrom(d.getRealm())
                    && manual.targetRealm() == Realm.QI_GATHERING;
            if (manualOk && d.isMeditating() && d.getRealm() == Realm.MORTAL && d.getOpenMeridianCount() >= 1) {
                Net.CHANNEL.sendToServer(new BreakthroughPacket());
            }
        });
    }

    /** Enable/disable the breakthrough button based on current capability state. */
    private void updateBreakthroughEnabled() {
        if (breakthrough == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            breakthrough.active = false;
            return;
        }
        mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
            var manual = CultivationManuals.byId(d.getManualId());
            boolean manualOk = d.isManualQuizPassed()
                    && manual.canBreakthroughFrom(d.getRealm())
                    && manual.targetRealm() == Realm.QI_GATHERING;
            boolean ok = d.isMeditating()
                    && d.getRealm() == Realm.MORTAL
                    && d.getOpenMeridianCount() >= 1
                    && manualOk;
            breakthrough.active = ok;
        });
    }

    @Override
    public void tick() {
        super.tick();
        updateBreakthroughEnabled();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        var mc = Minecraft.getInstance();

        // Title
        g.drawCenteredString(this.font, "Meridians", cx, cy - (R + 30), 0xFFFFFF);

        // Live state + labels around the ring
        if (mc.player != null) {
            mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
                for (int i = 0; i < CultivationData.MERIDIANS; i++) {
                    double a = Math.toRadians((360.0 / CultivationData.MERIDIANS) * i - 90);
                    int x = cx + (int) (Math.cos(a) * (R + 26));
                    int y = cy + (int) (Math.sin(a) * (R + 26));
                    String text = d.isMeridianOpen(i) ? "OPEN" : (d.getMeridianProgress(i) + "%");
                    int color = d.isMeridianOpen(i) ? 0x00FF66 : 0xCCCCCC;
                    g.drawCenteredString(this.font, text, x, y, color);
                }

                // Hint text
                int hintY = cy + R + 52;
                String hint = "Open ≥1 while meditating to Breakthrough. Gates at 3/6/9.";
                g.drawCenteredString(this.font, hint, cx, hintY, 0xA0E0FF);
            });
        }

        super.render(g, mx, my, pt);
    }
}
