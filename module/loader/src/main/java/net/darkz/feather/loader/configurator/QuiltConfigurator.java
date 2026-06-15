package net.darkz.feather.loader.configurator;

import net.darkz.feather.common.FeatherExtension;
import org.gradle.api.Project;

/**
 * Configures <a href="https://github.com/QuiltMC/quilt-loom">Quilt Loom</a>
 * for the target project.
 *
 * <p>Quilt Loom is a fork of Fabric Loom – it shares the same DSL so
 * configuration mirrors {@link FabricConfigurator} with Quilt-specific
 * coordinates.
 *
 * <pre>{@code
 * feather {
 *     loader          = "quilt"
 *     minecraftVersion = "1.21.1"
 *     loaderVersion   = "0.26.0"   // Quilt Loader version
 * }
 * }</pre>
 */
public class QuiltConfigurator implements LoaderConfigurator {

    private static final String LOOM_PLUGIN_ID = "org.quiltmc.loom";

    @Override
    public void configure(Project project, FeatherExtension ext) {
        String mc     = ext.getMinecraftVersion().getOrElse("");
        String loader = ext.getLoaderVersion().getOrElse("");
        requireProperty(ext, "minecraftVersion", mc);
        requireProperty(ext, "loaderVersion",    loader);

        project.getPluginManager().apply(LOOM_PLUGIN_ID);

        project.afterEvaluate(p -> {
            p.getDependencies().add("minecraft", "com.mojang:minecraft:" + mc);
            // Quilt uses QSL / Quilt mappings – default to Quilt Mappings
            p.getDependencies().add("mappings",
                    "org.quiltmc:quilt-mappings:" + mc + "+build.1:intermediary-v2");
            p.getDependencies().add("modImplementation",
                    "org.quiltmc:quilt-loader:" + loader);

            project.getLogger().lifecycle(
                    "[FeatherPlugin/Quilt] minecraft=" + mc +
                    " quiltLoader=" + loader);
        });
    }
}
