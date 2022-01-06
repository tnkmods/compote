package com.thenatekirby.compote;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;

// ====---------------------------------------------------------------------------====

@Mod.EventBusSubscriber
public class CompoteConfig {
    private static final String CATEGORY_GENERAL = "general";

    static ForgeConfigSpec COMMON_CONFIG;

    static ForgeConfigSpec.BooleanValue rightClickToClear;
    static ForgeConfigSpec.IntValue levelCount;
    // public static ForgeConfigSpec.BooleanValue useLootTableDrops;
    static ForgeConfigSpec.BooleanValue firstCompostAlwaysSucceeds;
    static ForgeConfigSpec.BooleanValue insertFromAnyDirection;
    static ForgeConfigSpec.BooleanValue extractFromAnyDirection;

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

        COMMON_BUILDER.comment("General Config");
        COMMON_BUILDER.push(CATEGORY_GENERAL);

        rightClickToClear = COMMON_BUILDER.comment("Shift right clicking with an empty hand will clear the composter of its contents. Default[Vanilla] is false.")
                .define("right_click", false);

        levelCount = COMMON_BUILDER.comment("The number of levels the composter must gain before generating its loot. Default[Vanilla] is 7.")
                .defineInRange("level_count", 7, 1, 7);

//        useLootTableDrops = COMMON_BUILDER.comment("Whether or not to use the customizable loot table for the composter's production, or the default bone meal item. Default[Vanilla] is false.")
//                .define("loot_table_drops", false);

        firstCompostAlwaysSucceeds = COMMON_BUILDER.comment("Whether or not the first piece of compostable material always raises the composter's level. Default[Vanilla] is true.")
                .define("first_compost_always_succeeds", true);

        insertFromAnyDirection = COMMON_BUILDER.comment("Allow items to be inserted from any face of the composter. Vanilla is false, default is true.")
                .define("insert_from_any_direction", true);

        extractFromAnyDirection = COMMON_BUILDER.comment("Allow items to be extracted from any face of the composter. Vanilla is false, default is true.")
                .define("extract_from_any_direction", true);

        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    static void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }
}
