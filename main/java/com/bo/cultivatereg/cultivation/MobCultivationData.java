package com.bo.cultivatereg.cultivation;

public interface MobCultivationData {
    boolean hasCultivation();
    void setHasCultivation(boolean v);

    Realm getRealm();
    void setRealm(Realm r);

    int getStage();          // 1..9
    void setStage(int s);
}
