package com.thenatekirby.compote.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thenatekirby.babel.core.ChanceItemStack;
import com.thenatekirby.babel.core.EmptyInventory;
import com.thenatekirby.compote.Compote;
import com.thenatekirby.compote.registration.CompoteRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// ====---------------------------------------------------------------------------====

public class CompoteRecipe implements IRecipe<EmptyInventory> {
    public static final String RECIPE_TYPE_NAME = "composting";
    private static final IRecipeType RECIPE_TYPE = registerRecipeType();

    private static <T extends IRecipe<?>> IRecipeType registerRecipeType() {
        return Registry.register(Registry.RECIPE_TYPE, Compote.MOD.withPath(RECIPE_TYPE_NAME), new IRecipeType<T>() {
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

    // ====---------------------------------------------------------------------------====
    // region Serializer

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CompoteRecipe> {
        private void parseJsonElementInto(@Nonnull JsonElement element, @Nonnull List<ChanceItemStack> list) {
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (int idx = 0; idx < array.size(); idx++) {
                    JsonObject object = array.get(idx).getAsJsonObject();
                    ChanceItemStack chanceItemStack = ChanceItemStack.from(object);
                    if (chanceItemStack != null) {
                        list.add(chanceItemStack);
                    }
                }

            } else if (element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                ChanceItemStack chanceItemStack = ChanceItemStack.from(jsonObject);
                if (chanceItemStack != null) {
                    list.add(chanceItemStack);
                }
            }
        }

        @Override
        @Nonnull
        public CompoteRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
            List<ChanceItemStack> additions = new ArrayList<>();
            List<ChanceItemStack> removals = new ArrayList<>();
            List<ChanceItemStack> changes = new ArrayList<>();
            int priority = 0;

            if (JSONUtils.hasField(json, "add")) {
                JsonElement jsonElement = json.get("add");
                parseJsonElementInto(jsonElement, additions);
            }

            if (JSONUtils.hasField(json, "remove")) {
                JsonElement jsonElement = json.get("remove");
                parseJsonElementInto(jsonElement, removals);
            }

            if (JSONUtils.hasField(json, "change")) {
                JsonElement jsonElement = json.get("change");
                parseJsonElementInto(jsonElement, removals);
            }

            if (JSONUtils.hasField(json, "priority")) {
                priority = json.getAsJsonPrimitive("priority").getAsInt();
            }

            return new CompoteRecipe(recipeId, priority, additions, removals, changes);
        }

        @Nullable
        @Override
        public CompoteRecipe read(@Nonnull ResourceLocation recipeId, PacketBuffer buffer) {
            int priority = buffer.readInt();

            List<ChanceItemStack> additions = new ArrayList<>();
            List<ChanceItemStack> removals = new ArrayList<>();
            List<ChanceItemStack> changes = new ArrayList<>();

            int additionSize = buffer.readVarInt();
            for (int idx = 0; idx < additionSize; idx++) {
                additions.add(ChanceItemStack.read(buffer));
            }

            int removalSize = buffer.readVarInt();
            for (int idx = 0; idx < removalSize; idx++) {
                removals.add(ChanceItemStack.read(buffer));
            }

            int changeSize = buffer.readVarInt();
            for (int idx = 0; idx < changeSize; idx++) {
                changes.add(ChanceItemStack.read(buffer));
            }

            return new CompoteRecipe(recipeId, priority, additions, removals, changes);
        }

        @Override
        public void write(PacketBuffer buffer, CompoteRecipe recipe) {
            buffer.writeInt(recipe.getPriority());

            buffer.writeVarInt(recipe.getAdditions().size());
            for (ChanceItemStack chanceItemStack : recipe.getAdditions()) {
                chanceItemStack.write(buffer);
            }

            buffer.writeVarInt(recipe.getRemovals().size());
            for (ChanceItemStack chanceItemStack : recipe.getRemovals()) {
                chanceItemStack.write(buffer);
            }

            buffer.writeVarInt(recipe.getChanges().size());
            for (ChanceItemStack chanceItemStack : recipe.getChanges()) {
                chanceItemStack.write(buffer);
            }
        }
    }
}