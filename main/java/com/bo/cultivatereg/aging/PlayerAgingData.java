package com.bo.cultivatereg.aging;

import com.bo.cultivatereg.cultivation.Realm;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

/**
 * Stores the client facing representation of a player's age.
 *
 * <p>The original project stores much more information, but all of the
 * systems that interact with the HUD only require a small, serialisable bag
 * of data.  The capability keeps track of both the chronological and
 * biological age as well as a life expectancy value which is used on the
 * overlay to draw progress bars.  The data object is intentionally mutable so
 * that it can easily be copied when network packets arrive or when the
 * capability is cloned.</p>
 */
public class PlayerAgingData {
    private int chronologicalYears;
    private int chronologicalMonths;
    private int biologicalYears;
    private int biologicalMonths;
    private int lifeExpectancyYears;
    private int lifeExpectancyMonths;
    private float progressToNextStage;
    private Realm realm = Realm.MORTAL;
    private int maxLifespanDays;
    private float agingMultiplier = 1.0F;
    private int graceDays;

    public PlayerAgingData() {
    }

    public PlayerAgingData(PlayerAgingData other) {
        copyFrom(other);
    }

    public int getChronologicalYears() {
        return chronologicalYears;
    }

    public void setChronologicalYears(int chronologicalYears) {
        this.chronologicalYears = Math.max(chronologicalYears, 0);
    }

    public int getChronologicalMonths() {
        return chronologicalMonths;
    }

    public void setChronologicalMonths(int chronologicalMonths) {
        this.chronologicalMonths = clampMonthValue(chronologicalMonths);
    }

    public int getBiologicalYears() {
        return biologicalYears;
    }

    public void setBiologicalYears(int biologicalYears) {
        this.biologicalYears = Math.max(biologicalYears, 0);
    }

    public int getBiologicalMonths() {
        return biologicalMonths;
    }

    public void setBiologicalMonths(int biologicalMonths) {
        this.biologicalMonths = clampMonthValue(biologicalMonths);
    }

    public int getLifeExpectancyYears() {
        return lifeExpectancyYears;
    }

    public void setLifeExpectancyYears(int lifeExpectancyYears) {
        this.lifeExpectancyYears = Math.max(lifeExpectancyYears, 0);
    }

    public int getLifeExpectancyMonths() {
        return lifeExpectancyMonths;
    }

    public void setLifeExpectancyMonths(int lifeExpectancyMonths) {
        this.lifeExpectancyMonths = clampMonthValue(lifeExpectancyMonths);
    }

    public float getProgressToNextStage() {
        return progressToNextStage;
    }

    public void setProgressToNextStage(float progressToNextStage) {
        this.progressToNextStage = Math.max(0.0F, Math.min(1.0F, progressToNextStage));
    }

    public void copyFrom(PlayerAgingData other) {
        if (other == null) {
            return;
        }

        chronologicalYears = other.chronologicalYears;
        chronologicalMonths = other.chronologicalMonths;
        biologicalYears = other.biologicalYears;
        biologicalMonths = other.biologicalMonths;
        lifeExpectancyYears = other.lifeExpectancyYears;
        lifeExpectancyMonths = other.lifeExpectancyMonths;
        progressToNextStage = other.progressToNextStage;
        realm = other.realm;
        maxLifespanDays = other.maxLifespanDays;
        agingMultiplier = other.agingMultiplier;
        graceDays = other.graceDays;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ChronologicalYears", chronologicalYears);
        tag.putInt("ChronologicalMonths", chronologicalMonths);
        tag.putInt("BiologicalYears", biologicalYears);
        tag.putInt("BiologicalMonths", biologicalMonths);
        tag.putInt("LifeExpectancyYears", lifeExpectancyYears);
        tag.putInt("LifeExpectancyMonths", lifeExpectancyMonths);
        tag.putFloat("Progress", progressToNextStage);
        tag.putInt("Realm", realm.ordinal());
        tag.putInt("MaxLifespanDays", maxLifespanDays);
        tag.putFloat("AgingMultiplier", agingMultiplier);
        tag.putInt("GraceDays", graceDays);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag == null) {
            return;
        }

        chronologicalYears = tag.getInt("ChronologicalYears");
        chronologicalMonths = clampMonthValue(tag.getInt("ChronologicalMonths"));
        biologicalYears = tag.getInt("BiologicalYears");
        biologicalMonths = clampMonthValue(tag.getInt("BiologicalMonths"));
        lifeExpectancyYears = tag.getInt("LifeExpectancyYears");
        lifeExpectancyMonths = clampMonthValue(tag.getInt("LifeExpectancyMonths"));
        progressToNextStage = Math.max(0.0F, Math.min(1.0F, tag.getFloat("Progress")));
        int realmIdx = Mth.clamp(tag.getInt("Realm"), 0, Realm.values().length - 1);
        realm = Realm.values()[realmIdx];
        maxLifespanDays = Math.max(0, tag.getInt("MaxLifespanDays"));
        agingMultiplier = Math.max(0.0F, tag.getFloat("AgingMultiplier"));
        graceDays = Math.max(0, tag.getInt("GraceDays"));
    }

    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(chronologicalYears);
        buf.writeVarInt(chronologicalMonths);
        buf.writeVarInt(biologicalYears);
        buf.writeVarInt(biologicalMonths);
        buf.writeVarInt(lifeExpectancyYears);
        buf.writeVarInt(lifeExpectancyMonths);
        buf.writeFloat(progressToNextStage);
        buf.writeVarInt(realm.ordinal());
        buf.writeVarInt(maxLifespanDays);
        buf.writeFloat(agingMultiplier);
        buf.writeVarInt(graceDays);
    }

    public static PlayerAgingData readFromBuffer(FriendlyByteBuf buf) {
        PlayerAgingData data = new PlayerAgingData();
        data.chronologicalYears = buf.readVarInt();
        data.chronologicalMonths = clampMonthValue(buf.readVarInt());
        data.biologicalYears = buf.readVarInt();
        data.biologicalMonths = clampMonthValue(buf.readVarInt());
        data.lifeExpectancyYears = buf.readVarInt();
        data.lifeExpectancyMonths = clampMonthValue(buf.readVarInt());
        data.progressToNextStage = Math.max(0.0F, Math.min(1.0F, buf.readFloat()));
        int realmIdx = Mth.clamp(buf.readVarInt(), 0, Realm.values().length - 1);
        data.realm = Realm.values()[realmIdx];
        data.maxLifespanDays = Math.max(0, buf.readVarInt());
        data.agingMultiplier = Math.max(0.0F, buf.readFloat());
        data.graceDays = Math.max(0, buf.readVarInt());
        return data;
    }

    public Realm getRealm() {
        return realm;
    }

    public void setRealm(Realm realm) {
        this.realm = realm == null ? Realm.MORTAL : realm;
    }

    public int getMaxLifespanDays() {
        return maxLifespanDays;
    }

    public void setMaxLifespanDays(int maxLifespanDays) {
        this.maxLifespanDays = Math.max(0, maxLifespanDays);
    }

    public float getAgingMultiplier() {
        return agingMultiplier;
    }

    public void setAgingMultiplier(float agingMultiplier) {
        this.agingMultiplier = Math.max(0.0F, agingMultiplier);
    }

    public int getGraceDays() {
        return graceDays;
    }

    public void setGraceDays(int graceDays) {
        this.graceDays = Math.max(0, graceDays);
    }

    public int getBiologicalDays() {
        int years = Math.max(0, biologicalYears);
        int months = Math.max(0, biologicalMonths);
        return years * 360 + months * 30;
    }

    public AgeBracket getAgeBracket() {
        if (maxLifespanDays <= 0) {
            return AgeBracket.YOUTH;
        }
        float ratio = getBiologicalDays() / (float) maxLifespanDays;
        if (ratio < 0.20F) return AgeBracket.YOUTH;
        if (ratio < 0.45F) return AgeBracket.ADULT;
        if (ratio < 0.70F) return AgeBracket.MIDDLE;
        if (ratio < 0.95F) return AgeBracket.ELDER;
        if (ratio < 1.20F) return AgeBracket.ANCIENT;
        return AgeBracket.TRANSCENDED;
    }

    private static int clampMonthValue(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 11) {
            return value % 12;
        }
        return value;
    }
}
