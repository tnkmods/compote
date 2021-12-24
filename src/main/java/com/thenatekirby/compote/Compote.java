package com.thenatekirby.compote;

import com.thenatekirby.babel.core.BabelMod;
import com.thenatekirby.babel.core.MutableResourceLocation;
import com.thenatekirby.babel.core.lifecycle.IModLifecycleAdapter;
import com.thenatekirby.babel.core.lifecycle.RegistryBuilder;
import com.thenatekirby.babel.util.RegistrationUtil;
import com.thenatekirby.compote.registration.CompoteRegistration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nonnull;

// ====---------------------------------------------------------------------------====

@Mod("compote")
public class Compote extends BabelMod {
    public static final String MOD_ID = "compote";
    public static final MutableResourceLocation MOD = new MutableResourceLocation(MOD_ID);

    public Compote() {
        setModLifecycleAdapter(new IModLifecycleAdapter() {
            @Override
            public void onSetupRegistries(@Nonnull RegistryBuilder builder) {
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
            public void onServerStarting(ServerStartingEvent event) {
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
