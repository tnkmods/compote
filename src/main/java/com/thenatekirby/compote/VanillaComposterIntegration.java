package com.thenatekirby.compote;

import com.thenatekirby.babel.core.ChanceItemStack;
import com.thenatekirby.babel.util.ReflectionHelper;
import com.thenatekirby.compote.recipe.CompoteRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.coremod.api.ASMAPI;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// ====---------------------------------------------------------------------------====

@SuppressWarnings("WeakerAccess")
public class VanillaComposterIntegration {
    public static void addComposting(@Nonnull ItemStack itemStack, float chance) {
        ComposterBlock.CHANCES.putIfAbsent(itemStack.getItem(), chance);
    }

    public static void addComposting(@Nonnull ChanceItemStack itemStack) {
        addComposting(itemStack.getItemStack(), itemStack.getChance());
    }

    public static void removeComposting(@Nonnull ChanceItemStack itemStack) {
        ComposterBlock.CHANCES.removeFloat(itemStack.getItemStack().getItem());
    }

    public static void addRecipesToComposterChances(@Nonnull RecipeManager recipeManager) {
        List<CompoteRecipe> recipes = new ArrayList<>();
        for (IRecipe<?> iRecipe : recipeManager.getRecipes()) {
            if (iRecipe instanceof CompoteRecipe) {
                recipes.add((CompoteRecipe) iRecipe);
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

    public static void registerCompoteAsPointOfInterest() {
        Map<BlockState, PointOfInterestType> types = ReflectionHelper.getPrivateValue(PointOfInterestType.class, null, ASMAPI.mapField("field_221073_u"));
        Blocks.COMPOSTER.getStateContainer().getValidStates().forEach(state -> types.put(state, PointOfInterestType.FARMER));
    }
}
