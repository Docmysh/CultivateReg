package com.bo.cultivatereg.cultivation;

public interface CultivationData {
    Realm getRealm();
    void setRealm(Realm r);

    int getStage();           // 1..9 (ignored for MORTAL)
    void setStage(int s);

    float getQi();
    void setQi(float v);
    void addQi(float delta);

    boolean isMeditating();
    void setMeditating(boolean v);

    boolean hasSensed();      // mortal → first unlock flag
    void setSensed(boolean v);

    float getSenseProgress(); // ticks accumulated while sensing (MORTAL only)
    void setSenseProgress(float v);
    // NEW: combat-Qi "mana" for future spells
    float getSpirit();
    void setSpirit(float v);
    void addSpirit(float delta);

    // NEW: resting state (server source of truth)
    boolean isResting();
    void setResting(boolean v);
    // --- NEW: abilities using Spirit (mana)
    boolean isShielding();
    void setShielding(boolean v);

    boolean isFlying();
    void setFlying(boolean v);

    boolean isQiSight();     // on/off client-visible sense
    void setQiSight(boolean v);
    // --- Meridians (12 total) ---
    int MERIDIANS = 12;
    boolean isMeridianOpen(int idx);
    void setMeridianOpen(int idx, boolean open);
    int getMeridianProgress(int idx);          // 0..100
    void setMeridianProgress(int idx, int pct);
    default int getOpenMeridianCount() {
        int n = 0; for (int i = 0; i < MERIDIANS; i++) if (isMeridianOpen(i)) n++;
        return n;
    }




}
