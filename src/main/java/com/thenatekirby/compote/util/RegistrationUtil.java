package com.thenatekirby.compote.util;

import com.thenatekirby.compote.Compote;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class RegistrationUtil {
    public static boolean overrideExistingBlock(@Nonnull Block block, String modid) {
        Block existing = ForgeRegistries.BLOCKS.getValue(block.getRegistryName());
        if (existing == null) {
            Compote.getLogger().error("Unable to find block {}", block.getRegistryName());
            return false;
        }

        ForgeRegistries.BLOCKS.register(block);
        ForgeRegistries.ITEMS.register(new BlockItem(block, new Item.Properties().group(Objects.requireNonNull(existing.asItem().getGroup()))) {
            @Override
            public String getCreatorModId(ItemStack itemStack) {
                return modid;
            }

        }.setRegistryName(Objects.requireNonNull(block.getRegistryName())));
        return true;
    }
}
