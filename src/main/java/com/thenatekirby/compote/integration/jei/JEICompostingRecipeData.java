package com.thenatekirby.compote.integration.jei;

// ====---------------------------------------------------------------------------====

class JEICompostingRecipeData {
    private int iconX;
    private int iconY;
    private String chanceText;

    private int chanceX;
    private int chanceY;
    private int chanceWidth;
    private int chanceHeight;

    void setRecipeAndSize(JEICompostingRecipe recipe, int width, int height) {
        iconX = (width - 16) / 2;
        iconY = (height - 16) / 2;

        int chance = (int) (recipe.getChance() * 100.0F);
        int displayChance = Math.min(100, Math.max(0, chance));
        chanceText = displayChance + "%";
    }

    void setChanceBounds(int x, int y, int width, int height) {
        this.chanceX = x;
        this.chanceY = y;
        this.chanceWidth = width;
        this.chanceHeight = height;
    }

    int getIconX() {
        return iconX;
    }

    int getIconY() {
        return iconY;
    }

    String getChanceText() {
        return chanceText;
    }

    boolean isMouseHoveringChance(double mouseX, double mouseY) {
        return (mouseX >= chanceX && mouseX <= chanceX + chanceWidth) && (mouseY >= chanceY && mouseY <= chanceY + chanceHeight);
    }
}
