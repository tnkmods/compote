package com.thenatekirby.compote.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

// ====---------------------------------------------------------------------------====

public class EmptyInventory implements IInventory {
    public static EmptyInventory INSTANCE = new EmptyInventory();

    private EmptyInventory() {
    }

    @Override
    public int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int i, int i1) {
        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int i, @Nonnull ItemStack itemStack) {

    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUsableByPlayer(@Nonnull PlayerEntity playerEntity) {
        return false;
    }

    @Override
    public void clear() {

    }

}