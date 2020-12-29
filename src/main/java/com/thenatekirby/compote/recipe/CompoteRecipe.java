package com.thenatekirby.compote.recipe;

import com.thenatekirby.compote.Compote;
import com.thenatekirby.compote.registration.CompoteRegistration;
import com.thenatekirby.compote.util.EmptyInventory;
import com.thenatekirby.compote.util.ChanceItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

// ====---------------------------------------------------------------------------====

public class CompoteRecipe implements IRecipe<EmptyInventory> {
    public static final String RECIPE_TYPE_NAME = "composting";
    private static final IRecipeType RECIPE_TYPE = registerRecipeType();

    private static <T extends IRecipe<?>> IRecipeType registerRecipeType() {
        return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(Compote.MOD_ID, RECIPE_TYPE_NAME), new IRecipeType<T>() {
            @Override
            public String toString() {
                return RECIPE_TYPE_NAME;
            }
        });
    }

    @Nonnull
    private final ResourceLocation recipeId;

    private final int priority;

    @Nonnull
    private final List<ChanceItemStack> additions;

    @Nonnull
    private final List<ChanceItemStack> removals;

    @Nonnull
    private final List<ChanceItemStack> changes;

    public CompoteRecipe(@Nonnull ResourceLocation recipeId, int priority, @Nonnull List<ChanceItemStack> additions, @Nonnull List<ChanceItemStack> removals, @Nonnull List<ChanceItemStack> changes) {
        this.recipeId = recipeId;
        this.priority = priority;
        this.additions = additions;
        this.removals = removals;
        this.changes = changes;
    }

    // ====---------------------------------------------------------------------------====
    // Getters

    int getPriority() {
        return priority;
    }

    @Nonnull
    public List<ChanceItemStack> getAdditions() {
        return additions;
    }

    @Nonnull
    public List<ChanceItemStack> getRemovals() {
        return removals;
    }

    @Nonnull
    public List<ChanceItemStack> getChanges() {
        return changes;
    }

    // ====---------------------------------------------------------------------------====
    // IRecipe

    @Override
    public boolean matches(@Nonnull EmptyInventory inv, @Nonnull World worldIn) {
        return false;
    }

    @Override
    @Nonnull
    public ItemStack getCraftingResult(@Nonnull EmptyInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Override
    @Nonnull
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return recipeId;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return CompoteRegistration.COMPOSTING.getAsRecipeSerializer();
    }

    @Nonnull
    @Override
    public IRecipeType<?> getType() {
        return RECIPE_TYPE;
    }
}