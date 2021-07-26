package com.thenatekirby.compote;

import com.thenatekirby.babel.api.IBlockReplacement;
import com.thenatekirby.babel.integration.Mods;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// ====---------------------------------------------------------------------------====

public class CompoteComposterBlock extends ComposterBlock implements IBlockReplacement {
    private static final AbstractBlock.Properties BLOCK_PROPERTIES = AbstractBlock.Properties.of(Material.WOOD).strength(0.6F).sound(SoundType.WOOD);

    CompoteComposterBlock() {
        super(BLOCK_PROPERTIES);
        setRegistryName(Mods.MINECRAFT.withPath("composter"));
    }

    // ====---------------------------------------------------------------------------====
    // region Vanilla Overrides

    @Nonnull
    public ActionResultType use(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, PlayerEntity player, @Nonnull Hand hand, BlockRayTraceResult hit) {
        int level = state.getValue(LEVEL);
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.isEmpty() && player.isCrouching() && level > 0) {
            if (CompoteConfig.rightClickToClear.get()) {
                world.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                resetFillState(state, world, pos);
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
        }

        if (level < 8 && COMPOSTABLES.containsKey(itemStack.getItem())) {
            if (level < 7 && !world.isClientSide) {
                BlockState blockstate = attemptCompost(state, world, pos, itemStack);
                world.levelEvent(1500, pos, state != blockstate ? 1 : 0);
                if (!player.abilities.instabuild) {
                    itemStack.shrink(1);
                }
            }

            return ActionResultType.sidedSuccess(world.isClientSide);

        } else if (level == 8) {
            emptyAndSpawnDrops(state, world, pos);
            return ActionResultType.sidedSuccess(world.isClientSide);

        } else {
            return ActionResultType.PASS;
        }
    }

    private static BlockState attemptCompost(BlockState state, IWorld world, BlockPos pos, ItemStack stack) {
        int level = state.getValue(LEVEL);

        float chance = COMPOSTABLES.getFloat(stack.getItem());

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
        int level = state.getValue(LEVEL);
        int nextLevel = level + 1;
        int effectiveLevel = (nextLevel == CompoteConfig.levelCount.get()) ? 7 : nextLevel;

        BlockState blockstate = state.setValue(LEVEL, effectiveLevel);
        world.setBlock(pos, blockstate, 3);

        if (effectiveLevel == 7) {
            world.getBlockTicks().scheduleTick(pos, state.getBlock(), 20);
        }

        return blockstate;
    }

    private static BlockState emptyAndSpawnDrops(BlockState state, World world, BlockPos pos) {
        if (!world.isClientSide) {
            double d0 = (double)(world.random.nextFloat() * 0.7F) + (double)0.15F;
            double d1 = (double)(world.random.nextFloat() * 0.7F) + (double)0.060000002F + 0.6D;
            double d2 = (double)(world.random.nextFloat() * 0.7F) + (double)0.15F;

            ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, generateDrops());
            itemEntity.setDefaultPickUpDelay();
            world.addFreshEntity(itemEntity);
        }

        BlockState blockstate = resetFillState(state, world, pos);
        world.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return blockstate;
    }

    private static BlockState resetFillState(BlockState state, IWorld world, BlockPos pos) {
        BlockState blockstate = state.setValue(LEVEL, 0);
        world.setBlock(pos, blockstate, 3);
        return blockstate;
    }

    private static ItemStack generateDrops() {
        return new ItemStack(Items.BONE_MEAL);
    }

    @Nonnull
    public ISidedInventory getContainer(BlockState state, @Nonnull IWorld world, @Nonnull BlockPos pos) {
        int i = state.getValue(LEVEL);
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

    // ====---------------------------------------------------------------------------====
    // region IBlockReplacement

    private StateContainer<Block, BlockState> container;

    @Override
    public void overrideDefaultState(BlockState state) {
        registerDefaultState(state);
    }

    @Override
    public void overrideStateContainer(StateContainer<Block, BlockState> container) {
        this.container = container;
    }

    @Override
    @Nonnull
    public StateContainer<Block, BlockState> getStateDefinition() {
        if (container != null) {
            return container;
        }

        return super.getStateDefinition();
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
        public boolean canPlaceItemThroughFace(int index, @Nonnull ItemStack itemStackIn, @Nullable Direction direction) {
            return false;
        }

        /**
         * Returns true if automation can extract the given item in the given slot from the given side.
         */
        public boolean canTakeItemThroughFace(int index, @Nonnull ItemStack stack, @Nonnull Direction direction) {
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
        public int getMaxStackSize() {
            return 1;
        }

        @Nonnull
        public int[] getSlotsForFace(@Nonnull Direction side) {
            return isValidDirectionForExtraction(side) ? new int[]{0} : new int[0];
        }

        /**
         * Returns true if automation can insert the given item in the given slot from the given side.
         */
        public boolean canPlaceItemThroughFace(int index, @Nonnull ItemStack itemStackIn, @Nullable Direction direction) {
            return false;
        }

        /**
         * Returns true if automation can extract the given item in the given slot from the given side.
         */
        public boolean canTakeItemThroughFace(int index, @Nonnull ItemStack stack, @Nonnull Direction direction) {
            return !this.extracted && isValidDirectionForExtraction(direction);// && stack.getItem() == Items.BONE_MEAL;
        }

        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void setChanged() {
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
        public int getMaxStackSize() {
            return 1;
        }

        @Nonnull
        public int[] getSlotsForFace(@Nonnull Direction side) {
            return isValidDirectionForInsertion(side) ? new int[]{0} : new int[0];
        }

        /**
         * Returns true if automation can insert the given item in the given slot from the given side.
         */
        public boolean canPlaceItemThroughFace(int index, @Nonnull ItemStack itemStackIn, @Nullable Direction direction) {
            return !this.inserted && isValidDirectionForInsertion(direction) && ComposterBlock.COMPOSTABLES.containsKey(itemStackIn.getItem());
        }

        /**
         * Returns true if automation can extract the given item in the given slot from the given side.
         */
        public boolean canTakeItemThroughFace(int index, @Nonnull ItemStack stack, @Nonnull Direction direction) {
            return false;
        }

        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void setChanged() {
            ItemStack itemStack = this.getItem(0);

            if (!itemStack.isEmpty()) {
                this.inserted = true;
                BlockState blockstate = CompoteComposterBlock.attemptCompost(this.state, this.world, this.pos, itemStack);
                this.world.levelEvent(1500, this.pos, blockstate != this.state ? 1 : 0);
                this.removeItemNoUpdate(0);
            }
        }
    }

    // endregion
}
