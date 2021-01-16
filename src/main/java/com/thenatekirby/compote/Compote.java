package com.thenatekirby.compote;

import com.thenatekirby.babel.core.MutableResourceLocation;
import com.thenatekirby.babel.util.RegistrationUtil;
import com.thenatekirby.compote.registration.CompoteRegistration;
import net.minecraft.block.Block;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// ====---------------------------------------------------------------------------====

@Mod("compote")
public class Compote {
    public static final String MOD_ID = "compote";
    public static final MutableResourceLocation MOD = new MutableResourceLocation(MOD_ID);

    private static final Logger LOGGER = LogManager.getLogger();
    public static Logger getLogger() {
        return LOGGER;
    }

    public Compote() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CompoteConfig.COMMON_CONFIG);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);

        CompoteRegistration.register();
        CompoteConfig.loadConfig(CompoteConfig.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("compote-common.toml"));
    }

    private void setup(final FMLCommonSetupEvent event) {
        VanillaComposterIntegration.registerCompoteAsPointOfInterest();
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        VanillaComposterIntegration.addRecipesToComposterChances(event.getServer().getRecipeManager());
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
            boolean result = RegistrationUtil.overrideExistingBlock(new CompoteComposterBlock(), MOD_ID);
            if (!result) {
                LOGGER.fatal("Unable to override vanilla composter, aborting.");
            }
        }
    }
}
