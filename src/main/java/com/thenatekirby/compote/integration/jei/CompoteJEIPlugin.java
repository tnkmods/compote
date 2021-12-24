package com.thenatekirby.compote.integration.jei;

import com.thenatekirby.compote.Compote;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;

// ====---------------------------------------------------------------------------====

@JeiPlugin
public class CompoteJEIPlugin implements IModPlugin {
    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Compote.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new CompoteRecipeCategory(guiHelper,144, 54));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(CompostingRecipeBuilder.getCompostingRecipes(), CompoteRecipeCategory.UID);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Blocks.COMPOSTER), CompoteRecipeCategory.UID);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    }
}
