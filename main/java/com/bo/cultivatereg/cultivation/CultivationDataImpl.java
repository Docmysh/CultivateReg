package com.bo.cultivatereg.cultivation;

import com.bo.cultivatereg.cultivation.manual.CultivationManuals;
import net.minecraft.resources.ResourceLocation;

public class CultivationDataImpl implements CultivationData {
    private Realm realm = Realm.MORTAL;
    private int stage = 1;                 // 1..9 for non-mortal
    private float qi = 0f;
    private boolean meditating = false;
    private boolean sensed = false;
    private float senseProgress = 0f;      // ticks while meditating in MORTAL

    // Combat/utility pools & states
    private float spirit = 0f;             // combat mana (for shield/flight/etc)
    private boolean resting = false;
    private boolean shielding = false;
    private boolean flying = false;

    // NEW: client-visible “Qi Sight” toggle (for mob nameplates, etc.)
    private boolean qiSight = false;
    // Meridians
    private final boolean[] meridianOpen = new boolean[MERIDIANS];
    private final int[] meridianProg = new int[MERIDIANS];

    // Manuals
    private ResourceLocation manualId = CultivationManuals.BASIC_QI_GATHERING.id();
    private int manualQuizProgress = 0;
    private boolean manualQuizPassed = false;

    @Override public boolean isMeridianOpen(int i) { return i>=0 && i<MERIDIANS && meridianOpen[i]; }
    @Override public void setMeridianOpen(int i, boolean v) { if (i>=0 && i<MERIDIANS) meridianOpen[i]=v; }
    @Override public int  getMeridianProgress(int i) { return (i>=0 && i<MERIDIANS) ? meridianProg[i] : 0; }
    @Override public void setMeridianProgress(int i, int p) {
        if (i>=0 && i<MERIDIANS) meridianProg[i] = Math.max(0, Math.min(100, p));
    }


    // ---- Realm / Stage / Qi ----
    @Override public Realm getRealm() { return realm; }
    @Override public void setRealm(Realm r) { this.realm = r; }

    @Override public int getStage() { return stage; }
    @Override public void setStage(int s) { this.stage = Math.max(1, Math.min(9, s)); }

    @Override public float getQi() { return qi; }
    @Override public void setQi(float v) { this.qi = Math.max(0f, v); }
    @Override public void addQi(float delta) { this.qi = Math.max(0f, this.qi + delta); }

    // ---- Meditation / Sense ----
    @Override public boolean isMeditating() { return meditating; }
    @Override public void setMeditating(boolean v) { this.meditating = v; }

    @Override public boolean hasSensed() { return sensed; }
    @Override public void setSensed(boolean v) { this.sensed = v; }

    @Override public float getSenseProgress() { return senseProgress; }
    @Override public void setSenseProgress(float v) { this.senseProgress = Math.max(0f, v); }

    // ---- Spirit / Rest ----
    @Override public float getSpirit() { return spirit; }
    @Override public void setSpirit(float v) { this.spirit = Math.max(0f, v); }
    @Override public void addSpirit(float delta) { this.spirit = Math.max(0f, this.spirit + delta); }

    @Override public boolean isResting() { return resting; }
    @Override public void setResting(boolean v) { this.resting = v; }

    // ---- Shield / Flight ----
    @Override public boolean isShielding() { return shielding; }
    @Override public void setShielding(boolean v) { this.shielding = v; }

    @Override public boolean isFlying() { return flying; }
    @Override public void setFlying(boolean v) { this.flying = v; }

    // ---- NEW: Qi Sight toggle ----
    @Override public boolean isQiSight() { return qiSight; }
    @Override public void setQiSight(boolean v) { this.qiSight = v; }

    // ---- Manuals ----
    @Override public ResourceLocation getManualId() { return manualId; }
    @Override public void setManualId(ResourceLocation id) {
        ResourceLocation resolved = (id != null && CultivationManuals.exists(id))
                ? id
                : CultivationManuals.BASIC_QI_GATHERING.id();
        this.manualId = resolved;
    }
    @Override public int getManualQuizProgress() { return manualQuizProgress; }
    @Override public void setManualQuizProgress(int value) { this.manualQuizProgress = Math.max(0, value); }
    @Override public boolean isManualQuizPassed() { return manualQuizPassed; }
    @Override public void setManualQuizPassed(boolean value) { this.manualQuizPassed = value; }
}

