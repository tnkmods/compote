package com.thenatekirby.compote;

import com.thenatekirby.babel.core.ChanceItemStack;
import com.thenatekirby.compote.recipe.CompoteRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.ComposterBlock;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

// ====---------------------------------------------------------------------------====

@SuppressWarnings("WeakerAccess")
public class VanillaComposterIntegration {
    public static void addComposting(@Nonnull ItemStack itemStack, float chance) {
        ComposterBlock.COMPOSTABLES.putIfAbsent(itemStack.getItem(), chance);
    }

    public static void addComposting(@Nonnull ChanceItemStack itemStack) {
        addComposting(itemStack.getItemStack(), itemStack.getChance());
    }

    public static void removeComposting(@Nonnull ChanceItemStack itemStack) {
        ComposterBlock.COMPOSTABLES.removeFloat(itemStack.getItemStack().getItem());
    }

    public static void addRecipesToComposterChances(@Nonnull RecipeManager recipeManager) {
        List<CompoteRecipe> recipes = new ArrayList<>();
        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            if (recipe instanceof CompoteRecipe) {
                recipes.add((CompoteRecipe) recipe);
            }
        }

        Compote.getLogger().info("Compote is caching compote:composting recipes with vanilla composter chances");

        for (CompoteRecipe recipe: recipes) {
            for (ChanceItemStack addition : recipe.getAdditions()) {
                addComposting(addition);
            }

            for (ChanceItemStack removal : recipe.getRemovals()) {
                removeComposting(removal);
            }

            for (ChanceItemStack change : recipe.getChanges()) {
                removeComposting(change);
                addComposting(change);
            }
        }
    }
}
