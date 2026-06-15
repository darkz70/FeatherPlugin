package net.darkz.feather.loader;

import net.darkz.feather.common.FeatherBasePlugin;
import net.darkz.feather.common.FeatherExtension;
import net.darkz.feather.common.ModLoader;
import net.darkz.feather.loader.configurator.FabricConfigurator;
import net.darkz.feather.loader.configurator.ForgeConfigurator;
import net.darkz.feather.loader.configurator.LoaderConfigurator;
import net.darkz.feather.loader.configurator.NeoForgeConfigurator;
import org.gradle.api.Project;

/**
 * {@code net.darkz.feather.feather-loader} plugin.
 *
 * <p>Detects the value of {@code feather.loader} after project evaluation
 * and delegates to the matching {@link LoaderConfigurator}.
 *
 * <pre>{@code
 * plugins {
 *     id 'net.darkz.feather.feather-loader' version '1.0.0'
 * }
 * feather {
 *     loader = "neoforge"         // or: fabric | forge
 *     minecraftVersion = "1.21.1"
 *     loaderVersion    = "21.1.0"
 * }
 * }</pre>
 */

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
