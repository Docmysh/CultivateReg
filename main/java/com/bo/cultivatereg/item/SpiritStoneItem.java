package com.bo.cultivatereg.item;

import net.minecraft.world.item.Item;

public class SpiritStoneItem extends Item {
    private final int color;

    public SpiritStoneItem(int color, Properties properties) {
        super(properties);
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}