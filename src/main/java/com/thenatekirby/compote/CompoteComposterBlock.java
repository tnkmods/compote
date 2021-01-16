package com.thenatekirby.compote;

import com.thenatekirby.babel.integration.Mods;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// ====---------------------------------------------------------------------------====

public class CompoteComposterBlock extends ComposterBlock {
    private static final Block.Properties BLOCK_PROPERTIES = AbstractBlock.Properties.create(Material.WOOD).hardnessAndResistance(0.6F).sound(SoundType.WOOD);

    CompoteComposterBlock() {
        super(BLOCK_PROPERTIES);
        setRegistryName(Mods.MINECRAFT.withPath("composter"));
    }

    // ====---------------------------------------------------------------------------====
    // region Vanilla Overrides

    @Nonnull
    public ActionResultType onBlockActivated(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, PlayerEntity player, @Nonnull Hand hand, BlockRayTraceResult hit) {
        int level = state.get(LEVEL);
        ItemStack itemStack = player.getHeldItem(hand);

        if (itemStack.isEmpty() && player.isCrouching() && level > 0) {
            if (CompoteConfig.rightClickToClear.get()) {
                world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                resetFillState(state, world, pos);
                return ActionResultType.func_233537_a_(world.isRemote);
            }
        }

        if (level < 8 && CHANCES.containsKey(itemStack.getItem())) {
            if (level < 7 && !world.isRemote) {
                BlockState blockstate = attemptCompost(state, world, pos, itemStack);
                world.playEvent(1500, pos, state != blockstate ? 1 : 0);
                if (!player.abilities.isCreativeMode) {
                    itemStack.shrink(1);
                }
            }

            return ActionResultType.func_233537_a_(world.isRemote);

        } else if (level == 8) {
            emptyAndSpawnDrops(state, world, pos);
            return ActionResultType.func_233537_a_(world.isRemote);

        } else {
            return ActionResultType.PASS;
        }
    }

    private static BlockState attemptCompost(BlockState state, IWorld world, BlockPos pos, ItemStack stack) {
        int level = state.get(LEVEL);

        float chance = CHANCES.getFloat(stack.getItem());

        if (level == 0 && CompoteConfig.firstCompostAlwaysSucceeds.get()) {
            return attemptCompostImpl(state, world, pos);
        }

        if (!(world.getRandom().nextDouble() < (double)chance)) {
            return state;

        } else {
            return attemptCompostImpl(state, world, pos);
        }
    }

    private static BlockState attemptCompostImpl(BlockState state, IWorld world, BlockPos pos) {
        int level = state.get(LEVEL);
        int nextLevel = level + 1;
        int effectiveLevel = (nextLevel == CompoteConfig.levelCount.get()) ? 7 : nextLevel;

        BlockState blockstate = state.with(LEVEL, effectiveLevel);
        world.setBlockState(pos, blockstate, 3);

        if (effectiveLevel == 7) {
            world.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), 20);
        }

        return blockstate;
    }

    private static BlockState emptyAndSpawnDrops(BlockState state, World world, BlockPos pos) {
        if (!world.isRemote) {
            double d0 = (double)(world.rand.nextFloat() * 0.7F) + (double)0.15F;
            double d1 = (double)(world.rand.nextFloat() * 0.7F) + (double)0.060000002F + 0.6D;
            double d2 = (double)(world.rand.nextFloat() * 0.7F) + (double)0.15F;

            ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, generateDrops());
            itemEntity.setDefaultPickupDelay();
            world.addEntity(itemEntity);
        }

        BlockState blockstate = resetFillState(state, world, pos);
        world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return blockstate;
    }

    private static BlockState resetFillState(BlockState state, IWorld world, BlockPos pos) {
        BlockState blockstate = state.with(LEVEL, 0);
        world.setBlockState(pos, blockstate, 3);
        return blockstate;
    }

    private static ItemStack generateDrops() {
        return new ItemStack(Items.BONE_MEAL);
    }

    @Nonnull
    public ISidedInventory createInventory(BlockState state, @Nonnull IWorld world, @Nonnull BlockPos pos) {
        int i = state.get(LEVEL);
        if (i == 8) {
            return new CompoteComposterBlock.FullInventory(state, world, pos, generateDrops());
        } else {
            return (i < 7 ? new CompoteComposterBlock.PartialInventory(state, world, pos) : new CompoteComposterBlock.EmptyInventory());
        }
    }

    // endregion
    // ====---------------------------------------------------------------------------====
    // region Helpers

    private static boolean isValidDirectionForInsertion(@Nullable Direction direction) {
        if (CompoteConfig.insertFromAnyDirection.get()) {
            return true;
        }

        return direction == Direction.UP;
    }

    private static boolean isValidDirectionForExtraction(@Nullable Direction direction) {
        if (CompoteConfig.extractFromAnyDirection.get()) {
            return true;
        }

        return direction == Direction.DOWN;
    }

    // endregion
    // ====---------------------------------------------------------------------------====
    // region Inventories

    static class EmptyInventory extends Inventory implements ISidedInventory {
        EmptyInventory() {
            super(0);
        }

        @Nonnull
        public int[] getSlotsForFace(@Nonnull Direction side) {
            return new int[0];
        }

        /**
         * Returns true if automation can insert the given item in the given slot from the given side.
         */
        public boolean canInsertItem(int index, @Nonnull ItemStack itemStackIn, @Nullable Direction direction) {
            return false;
        }

        /**
         * Returns true if automation can extract the given item in the given slot from the given side.
         */
        public boolean canExtractItem(int index, @Nonnull ItemStack stack, @Nonnull Direction direction) {
            return false;
        }
    }

    static class FullInventory extends Inventory implements ISidedInventory {
        private final BlockState state;
        private final IWorld world;
        private final BlockPos pos;
        private boolean extracted;

        FullInventory(BlockState state, IWorld world, BlockPos pos, ItemStack stack) {
            super(stack);
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        /**
         * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
         */
        public int getInventoryStackLimit() {
            return 1;
        }

        @Nonnull
        public int[] getSlotsForFace(@Nonnull Direction side) {
            return isValidDirectionForExtraction(side) ? new int[]{0} : new int[0];
        }

        /**
         * Returns true if automation can insert the given item in the given slot from the given side.
         */
        public boolean canInsertItem(int index, @Nonnull ItemStack itemStackIn, @Nullable Direction direction) {
            return false;
        }

        /**
         * Returns true if automation can extract the given item in the given slot from the given side.
         */
        public boolean canExtractItem(int index, @Nonnull ItemStack stack, @Nonnull Direction direction) {
            return !this.extracted && isValidDirectionForExtraction(direction);// && stack.getItem() == Items.BONE_MEAL;
        }

        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void markDirty() {
            CompoteComposterBlock.resetFillState(this.state, this.world, this.pos);
            this.extracted = true;
        }
    }

    static class PartialInventory extends Inventory implements ISidedInventory {
        private final BlockState state;
        private final IWorld world;
        private final BlockPos pos;
        private boolean inserted;

        PartialInventory(BlockState state, IWorld world, BlockPos pos) {
            super(1);
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        /**
         * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
         */
        public int getInventoryStackLimit() {
            return 1;
        }

        @Nonnull
        public int[] getSlotsForFace(@Nonnull Direction side) {
            return isValidDirectionForInsertion(side) ? new int[]{0} : new int[0];
        }

        /**
         * Returns true if automation can insert the given item in the given slot from the given side.
         */
        public boolean canInsertItem(int index, @Nonnull ItemStack itemStackIn, @Nullable Direction direction) {
            return !this.inserted && isValidDirectionForInsertion(direction) && ComposterBlock.CHANCES.containsKey(itemStackIn.getItem());
        }

        /**
         * Returns true if automation can extract the given item in the given slot from the given side.
         */
        public boolean canExtractItem(int index, @Nonnull ItemStack stack, @Nonnull Direction direction) {
            return false;
        }

        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void markDirty() {
            ItemStack itemStack = this.getStackInSlot(0);

            if (!itemStack.isEmpty()) {
                this.inserted = true;
                BlockState blockstate = CompoteComposterBlock.attemptCompost(this.state, this.world, this.pos, itemStack);
                this.world.playEvent(1500, this.pos, blockstate != this.state ? 1 : 0);
                this.removeStackFromSlot(0);
            }
        }
    }

    // endregion
}
