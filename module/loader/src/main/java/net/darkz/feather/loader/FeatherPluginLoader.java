package net.darkz.feather.loader;

import net.darkz.feather.common.FeatherBasePlugin;
import net.darkz.feather.common.FeatherExtension;
import net.darkz.feather.common.ModLoader;
import net.darkz.feather.loader.configurator.FabricConfigurator;
import net.darkz.feather.loader.configurator.ForgeConfigurator;
import net.darkz.feather.loader.configurator.LoaderConfigurator;
import net.darkz.feather.loader.configurator.NeoForgeConfigurator;
import org.gradle.api.Project;

public class FeatherPluginLoader extends FeatherBasePlugin {

    @Override
    protected void applyPlugin(Project project) {
        FeatherExtension ext = ensureExtension(project);

        project.afterEvaluate(p -> {
            ModLoader loader = ext.resolvedLoader();
            info("Applying loader toolchain: " + loader.name());
            resolveConfigurator(loader).configure(p, ext);
        });
    }

    private LoaderConfigurator resolveConfigurator(ModLoader loader) {
        switch (loader) {
            case FABRIC:   return new FabricConfigurator();
            case FORGE:    return new ForgeConfigurator();
            case NEOFORGE: return new NeoForgeConfigurator();
            default: throw new IllegalArgumentException("[FeatherPlugin] Unsupported loader: " + loader);
        }
    }
}
