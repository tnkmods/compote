package com.thenatekirby.compote.integration.jei;

import com.thenatekirby.babel.core.ChanceItemStack;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ComposterBlock;

import java.util.ArrayList;
import java.util.List;

// ====---------------------------------------------------------------------------====

@SuppressWarnings("rawtypes")
class CompostingRecipeBuilder {
    static List<JEICompostingRecipe> getCompostingRecipes() {
        List<JEICompostingRecipe> recipes = new ArrayList<>();
        Object2FloatMap chances = ComposterBlock.COMPOSTABLES;

        for (Object item: chances.keySet()) {
            ItemStack itemStack = new ItemStack((Item) item);
            ChanceItemStack chanceItemStack = new ChanceItemStack(itemStack, chances.getFloat(item));
            if (!chanceItemStack.getItemStack().isEmpty()) {
                JEICompostingRecipe recipe = new JEICompostingRecipe(chanceItemStack);
                recipes.add(recipe);
            }
        }

        return recipes;
    }
}
