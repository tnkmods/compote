package com.thenatekirby.compote;


import com.thenatekirby.babel.core.api.IBlockReplacement;
import com.thenatekirby.babel.entity.PlayerStatus;
import com.thenatekirby.babel.integration.Mods;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// ====---------------------------------------------------------------------------====

public class CompoteComposterBlock extends ComposterBlock implements IBlockReplacement {
    private static final Block.Properties BLOCK_PROPERTIES = Block.Properties.of(Material.WOOD).strength(0.6F).sound(SoundType.WOOD);

    CompoteComposterBlock() {
        super(BLOCK_PROPERTIES);
        setRegistryName(Mods.MINECRAFT.withPath("composter"));
    }

    // ====---------------------------------------------------------------------------====
    // region Vanilla Overrides

    @Nonnull
    @Override
    public InteractionResult use(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hitResult) {
        int fillLevel = state.getValue(LEVEL);
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.isEmpty() && player.isCrouching() && fillLevel > 0) {
            if (CompoteConfig.rightClickToClear.get()) {
                level.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                resetFillState(state, level, pos);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        if (fillLevel < 8 && COMPOSTABLES.containsKey(itemStack.getItem())) {
            if (fillLevel < 7 && !level.isClientSide) {
                BlockState blockstate = attemptCompost(state, level, pos, itemStack);
                level.levelEvent(1500, pos, state != blockstate ? 1 : 0);

                if (PlayerStatus.of(player).isCreative()) {
                    itemStack.shrink(1);
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);

        } else if (fillLevel == 8) {
            emptyAndSpawnDrops(state, level, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);

        } else {
            return InteractionResult.PASS;
        }
    }

    private static BlockState attemptCompost(BlockState state, LevelAccessor world, BlockPos pos, ItemStack stack) {
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

    private static BlockState attemptCompostImpl(BlockState state, LevelAccessor level, BlockPos pos) {
        int fillLevel = state.getValue(LEVEL);
        int nextLevel = fillLevel + 1;
        int effectiveLevel = (nextLevel == CompoteConfig.levelCount.get()) ? 7 : nextLevel;

        BlockState blockstate = state.setValue(LEVEL, effectiveLevel);
        level.setBlock(pos, blockstate, 3);

        if (effectiveLevel == 7) {
            level.scheduleTick(pos, state.getBlock(), 20);
        }

        return blockstate;
    }

    private static BlockState emptyAndSpawnDrops(BlockState state, Level level, BlockPos pos) {
        if (!level.isClientSide) {
            double d0 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;
            double d1 = (double)(level.random.nextFloat() * 0.7F) + (double)0.060000002F + 0.6D;
            double d2 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;

            ItemEntity itemEntity = new ItemEntity(level, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, generateDrops());
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }

        BlockState blockstate = resetFillState(state, level, pos);
        level.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        return blockstate;
    }

    private static BlockState resetFillState(BlockState state, LevelAccessor world, BlockPos pos) {
        BlockState blockstate = state.setValue(LEVEL, 0);
        world.setBlock(pos, blockstate, 3);
        return blockstate;
    }

    private static ItemStack generateDrops() {
        return new ItemStack(Items.BONE_MEAL);
    }

    @Nonnull
    public WorldlyContainer getContainer(BlockState state, @Nonnull LevelAccessor level, @Nonnull BlockPos pos) {
        int fillLevel = state.getValue(LEVEL);
        if (fillLevel == 8) {
            return new CompoteComposterBlock.FullInventory(state, level, pos, generateDrops());
        } else {
            return (fillLevel < 7 ? new CompoteComposterBlock.PartialInventory(state, level, pos) : new CompoteComposterBlock.EmptyInventory());
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

    private StateDefinition<Block, BlockState> container;

    @Override
    public void overrideDefaultState(BlockState state) {
        registerDefaultState(state);
    }

    @Override
    public void overrideStateContainer(StateDefinition<Block, BlockState> container) {
        this.container = container;
    }

    @Override
    @Nonnull
    public StateDefinition<Block, BlockState> getStateDefinition() {
        if (container != null) {
            return container;
        }

        return super.getStateDefinition();
    }

    // endregion
    // ====---------------------------------------------------------------------------====
    // region Inventories

    static class EmptyInventory extends SimpleContainer implements WorldlyContainer {
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

    static class FullInventory extends SimpleContainer implements WorldlyContainer {
        private final BlockState state;
        private final LevelAccessor world;
        private final BlockPos pos;
        private boolean extracted;

        FullInventory(BlockState state, LevelAccessor world, BlockPos pos, ItemStack stack) {
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

    static class PartialInventory extends SimpleContainer implements WorldlyContainer {
        private final BlockState state;
        private final LevelAccessor world;
        private final BlockPos pos;
        private boolean inserted;

        PartialInventory(BlockState state, LevelAccessor world, BlockPos pos) {
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
