package com.bo.cultivatereg.cultivation;

public class MobCultivationDataImpl implements MobCultivationData {
    private boolean has = false;
    private Realm realm = Realm.MORTAL;
    private int stage = 1;

    public boolean hasCultivation() { return has; }
    public void setHasCultivation(boolean v) { has = v; }

    public Realm getRealm() { return realm; }
    public void setRealm(Realm r) { realm = (r == null ? Realm.MORTAL : r); }

    public int getStage() { return stage; }
    public void setStage(int s) { stage = Math.max(1, Math.min(9, s)); }
}
