package dev.chickenthedev.stratagems;

import dev.chickenthedev.stratagems.platform.Services;
import dev.chickenthedev.stratagems.platform.services.IRegistryHelper;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can
// only import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that
// provide common compatible binaries. This means common code can not directly use loader specific concepts such as
// NeoForge events however it will be compatible with all supported mod loaders.
public class StratagemsMod {

    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects.
    public static void init(IRegistryHelper registries) {

        Constants.LOG.info("Arming {} on {} ({} environment)", Constants.MOD_NAME,
                Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());

        StratagemConfig.load();
        ModItems.register(registries);
        ModEntities.register(registries);

        Constants.LOG.info("Registered {} stratagems. For Super Earth!", StratagemRegistry.all().size());
    }
}
