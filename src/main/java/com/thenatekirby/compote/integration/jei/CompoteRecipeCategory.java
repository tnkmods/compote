package com.thenatekirby.compote.integration.jei;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.thenatekirby.compote.Compote;
import com.thenatekirby.compote.Localization;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

// ====---------------------------------------------------------------------------====

public class CompoteRecipeCategory implements IRecipeCategory<JEICompostingRecipe> {
    static final ResourceLocation UID = Compote.MOD.withPath("composting");

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private IGuiHelper guiHelper;
    private IDrawable background;
    private IDrawable icon;

    private final LoadingCache<JEICompostingRecipe, JEICompostingRecipeData> cachedData;

    CompoteRecipeCategory(IGuiHelper guiHelper, int width, int height) {
        this.guiHelper = guiHelper;
        this.background = new EmptyBackground(width, height);
        this.icon = guiHelper.createDrawableIngredient(getIconItemStack());

        this.cachedData = CacheBuilder.newBuilder()
                .maximumSize(20)
                .build(new CacheLoader<JEICompostingRecipe, JEICompostingRecipeData>() {
                    @Override
                    public JEICompostingRecipeData load(@Nonnull JEICompostingRecipe key) {
                        return new JEICompostingRecipeData();
                    }
                });
    }

    // ====---------------------------------------------------------------------------====
    // region Helpers

    private ItemStack getIconItemStack() {
        return new ItemStack(Blocks.COMPOSTER);
    }

    private TranslatableComponent getLocalizedName() {
        return Localization.COMPOSTING;
    }

    // endregion
    // ====---------------------------------------------------------------------------====
    // region IRecipeCategory

    @Nonnull
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public Class<? extends JEICompostingRecipe> getRecipeClass() {
        return JEICompostingRecipe.class;
    }

    @Override
    public Component getTitle() {
        return getLocalizedName();
    }

    @Override
    @Nonnull
    public IDrawable getBackground() {
        return background;
    }

    @Override
    @Nonnull
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(@Nonnull JEICompostingRecipe compoteRecipe, @Nonnull IIngredients ingredients) {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(compoteRecipe.getItemStack());
        itemStacks.add(new ItemStack(Blocks.COMPOSTER));
        ingredients.setInputs(VanillaTypes.ITEM, itemStacks);
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull JEICompostingRecipe compoteRecipe, @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        int y = (background.getHeight() / 2) - 8;
        guiItemStacks.init(0, true, 20, y);

        guiItemStacks.set(ingredients);

        JEICompostingRecipeData data = cachedData.getUnchecked(compoteRecipe);
        data.setRecipeAndSize(compoteRecipe, background.getWidth(), background.getHeight());
    }

    @Override
    public void draw(@Nonnull JEICompostingRecipe recipe, @Nonnull PoseStack matrixStack, double mouseX, double mouseY) {
        JEICompostingRecipeData data = cachedData.getUnchecked(recipe);
        icon.draw(matrixStack, data.getIconX(), data.getIconY());

        drawChance(recipe, matrixStack, data.getChanceText());
    }

    private void drawChance(JEICompostingRecipe recipe, PoseStack matrixStack, String chanceText) {
        Minecraft minecraft = Minecraft.getInstance();

        int textColor = 0xFF888888;
        int width = minecraft.font.width(chanceText);
        int x = (background.getWidth() - width) / 2;
        int y = (background.getHeight() / 2) + 12;
        minecraft.font.draw(matrixStack, chanceText, x, y, textColor);

        cachedData.getUnchecked(recipe).setChanceBounds(x, y, width, 16);
    }

    @Nonnull
    @Override
    public List<Component> getTooltipStrings(@Nonnull JEICompostingRecipe recipe, double mouseX, double mouseY) {
        JEICompostingRecipeData data = cachedData.getUnchecked(recipe);

        List<Component> textComponents = new ArrayList<>();
        if (data.isMouseHoveringChance(mouseX, mouseY)) {
            textComponents.add(Localization.TOOLTIP);
        }

        return textComponents;
    }

    // endregion
}
