package com.thenatekirby.compote.integration.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.gui.drawable.IDrawable;

import javax.annotation.Nonnull;

// ====---------------------------------------------------------------------------====

public class EmptyBackground implements IDrawable {
    private final int width;
    private final int height;

    public EmptyBackground(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(@Nonnull MatrixStack matrixStack, int i, int i1) {
    }
}
