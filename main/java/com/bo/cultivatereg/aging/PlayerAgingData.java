package com.bo.cultivatereg.aging;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

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
    }

    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(chronologicalYears);
        buf.writeVarInt(chronologicalMonths);
        buf.writeVarInt(biologicalYears);
        buf.writeVarInt(biologicalMonths);
        buf.writeVarInt(lifeExpectancyYears);
        buf.writeVarInt(lifeExpectancyMonths);
        buf.writeFloat(progressToNextStage);
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
        return data;
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
