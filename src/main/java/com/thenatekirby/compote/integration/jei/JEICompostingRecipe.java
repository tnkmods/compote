package com.thenatekirby.compote.integration.jei;

import com.thenatekirby.compote.util.ChanceItemStack;
import net.minecraft.item.ItemStack;

// ====---------------------------------------------------------------------------====

class JEICompostingRecipe {
    private final ItemStack itemStack;
    private final float chance;

    JEICompostingRecipe(ChanceItemStack chanceItemStack) {
        this.itemStack = chanceItemStack.getItemStack();
        this.chance = chanceItemStack.getChance();
    }

    ItemStack getItemStack() {
        return itemStack;
    }

    float getChance() {
        return chance;
    }
}
