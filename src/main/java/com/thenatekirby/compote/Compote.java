package com.thenatekirby.compote;

import com.thenatekirby.babel.core.MutableResourceLocation;
import com.thenatekirby.babel.registration.BabelMod;
import com.thenatekirby.babel.util.RegistrationUtil;
import com.thenatekirby.compote.registration.CompoteRegistration;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

// ====---------------------------------------------------------------------------====

@Mod("compote")
public class Compote extends BabelMod {
    public static final String MOD_ID = "compote";
    public static final MutableResourceLocation MOD = new MutableResourceLocation(MOD_ID);

    public Compote() {
        setLifecycleAdapter(new ILifecycleAdapter() {
            @Override
            public void onSetupRegistries(BabelRegistryBuilder builder) {
                builder.addRecipeSerializers(CompoteRegistration.SERIALIZERS);
            }

            @Override
            public void onRegisterConfig() {
                ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CompoteConfig.COMMON_CONFIG);
            }

            @Override
            public void onLoadConfig() {
                CompoteConfig.loadConfig(CompoteConfig.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("compote-common.toml"));
            }

            @Override
            public void onServerStarting(FMLServerStartingEvent event) {
                VanillaComposterIntegration.addRecipesToComposterChances(event.getServer().getRecipeManager());
            }
        });
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
            CompoteComposterBlock block = new CompoteComposterBlock();
            boolean result = RegistrationUtil.overrideExistingBlock(block, MOD_ID);
            if (!result) {
                getLogger().fatal("Unable to override vanilla composter, aborting.");
            }

            RegistrationUtil.overrideBlockstates(Blocks.COMPOSTER, block);
        }
    }
}
