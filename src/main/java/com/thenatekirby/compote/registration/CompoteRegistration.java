package com.thenatekirby.compote.registration;

import com.thenatekirby.babel.registration.DeferredRecipeSerializer;
import com.thenatekirby.compote.Compote;
import com.thenatekirby.compote.recipe.CompoteRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

// ====---------------------------------------------------------------------------====

public class CompoteRegistration {
    private static final DeferredRegister<IRecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Compote.MOD_ID);

    public static final DeferredRecipeSerializer<CompoteRecipe.Serializer> COMPOSTING = DeferredRecipeSerializer.create(CompoteRecipe.RECIPE_TYPE_NAME, CompoteRecipe.Serializer::new, SERIALIZERS);

    public static void register() {
        SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}