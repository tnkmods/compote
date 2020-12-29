package com.thenatekirby.compote.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thenatekirby.compote.util.ChanceItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// ====---------------------------------------------------------------------------====

public class CompoteRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CompoteRecipe> {
    public interface IRecipeProvider {
        CompoteRecipe provide(ResourceLocation recipeId, int priority, List<ChanceItemStack> additions, List<ChanceItemStack> removals, List<ChanceItemStack> changes);
    }

    private final IRecipeProvider provider;

    public CompoteRecipeSerializer(IRecipeProvider provider) {
        this.provider = provider;
    }

    // ====---------------------------------------------------------------------------====
    // Helpers

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

    // ====---------------------------------------------------------------------------====
    // IRecipeSerializer

    @Override
    @Nonnull
    public CompoteRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        List<ChanceItemStack> additions = new ArrayList<>();
        List<ChanceItemStack> removals = new ArrayList<>();
        List<ChanceItemStack> changes = new ArrayList<>();
        int priority = 0;

        if (json.has("add")) {
            JsonElement jsonElement = json.get("add");
            parseJsonElementInto(jsonElement, additions);
        }

        if (json.has("remove")) {
            JsonElement jsonElement = json.get("remove");
            parseJsonElementInto(jsonElement, removals);
        }

        if (json.has("change")) {
            JsonElement jsonElement = json.get("change");
            parseJsonElementInto(jsonElement, removals);
        }

        if (json.has("priority")) {
            priority = json.getAsJsonPrimitive("priority").getAsInt();
        }

        return provider.provide(recipeId, priority, additions, removals, changes);
    }

    @Nullable
    @Override
    public CompoteRecipe read(@Nonnull ResourceLocation recipeId, PacketBuffer buffer) {
        int priority = buffer.readInt();

        List<ChanceItemStack> additions = new ArrayList<>();
        List<ChanceItemStack> removals = new ArrayList<>();
        List<ChanceItemStack> changes = new ArrayList<>();

        int additionSize = buffer.readInt();
        for (int idx = 0; idx < additionSize; idx++) {
            additions.add(ChanceItemStack.read(buffer));
        }

        int removalSize = buffer.readInt();
        for (int idx = 0; idx < removalSize; idx++) {
            removals.add(ChanceItemStack.read(buffer));
        }

        int changeSize = buffer.readInt();
        for (int idx = 0; idx < changeSize; idx++) {
            changes.add(ChanceItemStack.read(buffer));
        }

        return provider.provide(recipeId, priority, additions, removals, changes);
    }

    @Override
    public void write(PacketBuffer buffer, CompoteRecipe recipe) {
        int additionSize = recipe.getAdditions().size();
        int removalSize = recipe.getRemovals().size();
        int changeSize = recipe.getChanges().size();

        buffer.writeInt(recipe.getPriority());

        buffer.writeInt(additionSize);
        for (ChanceItemStack chanceItemStack : recipe.getAdditions()) {
            chanceItemStack.write(buffer);
        }

        buffer.writeInt(removalSize);
        for (ChanceItemStack chanceItemStack : recipe.getRemovals()) {
            chanceItemStack.write(buffer);
        }

        buffer.writeInt(changeSize);
        for (ChanceItemStack chanceItemStack : recipe.getChanges()) {
            chanceItemStack.write(buffer);
        }
    }
}