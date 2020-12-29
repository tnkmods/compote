package com.thenatekirby.compote.util;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

// ====---------------------------------------------------------------------------====

public class ChanceItemStack {
    private ItemStack itemStack;
    private float chance;

    public ChanceItemStack(ItemStack itemStack, float chance) {
        this.itemStack = itemStack;
        this.chance = chance;
    }

    @Nullable
    public static ChanceItemStack from(JsonObject jsonObject) {
        ItemStack itemStack;
        float chance;

        if (jsonObject.has("item")) {
            itemStack = ShapedRecipe.deserializeItem(jsonObject);

        } else {
            return null;
        }

        if (jsonObject.has("chance")) {
            chance = jsonObject.getAsJsonPrimitive("chance").getAsFloat();
        } else {
            chance = 1.0f;
        }

        return new ChanceItemStack(itemStack, chance);
    }

    // ====---------------------------------------------------------------------------====
    // Getters

    public ItemStack getItemStack() {
        return itemStack;
    }

    public float getChance() {
        return chance;
    }

    // ====---------------------------------------------------------------------------====
    // Serialization

    public void write(PacketBuffer buffer) {
        buffer.writeItemStack(itemStack);
        buffer.writeFloat(chance);
    }

    public static ChanceItemStack read(PacketBuffer buffer) {
        ItemStack itemStack = buffer.readItemStack();
        float chance = buffer.readFloat();
        return new ChanceItemStack(itemStack, chance);
    }
}