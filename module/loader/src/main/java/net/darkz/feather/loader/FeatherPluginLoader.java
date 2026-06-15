package net.darkz.feather.loader;

import net.darkz.feather.common.*;
import net.darkz.feather.loader.configurator.*;
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
 *     loader = "neoforge"         // or: fabric | quilt | forge
 *     minecraftVersion = "1.21.1"
 *     loaderVersion    = "21.1.0"
 * }
 * }</pre>
 */
public class FeatherPluginLoader extends FeatherBasePlugin {

    @Override
    protected void applyPlugin(Project project) {
        // Ensure extension exists (core may not have been applied first)
        FeatherExtension ext = ensureExtension(project);

        project.afterEvaluate(p -> {
            ModLoader loader = ext.resolvedLoader();
            info("Applying loader toolchain: " + loader.name());
            resolveConfigurator(loader).configure(p, ext);
        });
    }

    // ──────────────────────────────────────────────────────────────

    private LoaderConfigurator resolveConfigurator(ModLoader loader) {
        return switch (loader) {
            case FABRIC   -> new FabricConfigurator();
            case QUILT    -> new QuiltConfigurator();
            case FORGE    -> new ForgeConfigurator();
            case NEOFORGE -> new NeoForgeConfigurator();
        };
    }
}
