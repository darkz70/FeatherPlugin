package net.darkz.feather.loader.configurator;

import net.darkz.feather.common.FeatherExtension;
import org.gradle.api.Project;

/**
 * Configures <a href="https://github.com/FabricMC/fabric-loom">Fabric Loom</a>
 * for the target project.
 *
 * <p>Expected {@code feather} block:
 * <pre>{@code
 * feather {
 *     loader          = "fabric"
 *     minecraftVersion = "1.21.1"
 *     loaderVersion   = "0.16.2"   // Fabric Loader version
 * }
 * }</pre>
 */
public class FabricConfigurator implements LoaderConfigurator {

    private static final String LOOM_PLUGIN_ID = "fabric-loom";

    @Override
    public void configure(Project project, FeatherExtension ext) {
        String mc     = ext.getMinecraftVersion().getOrElse("");
        String loader = ext.getLoaderVersion().getOrElse("");
        requireProperty(ext, "minecraftVersion", mc);
        requireProperty(ext, "loaderVersion",    loader);

        // Apply Fabric Loom (must be in the project's buildscript classpath)
        project.getPluginManager().apply(LOOM_PLUGIN_ID);

        // Wire Minecraft & Fabric Loader versions into the standard dependencies
        project.afterEvaluate(p -> {
            p.getDependencies().add("minecraft", "com.mojang:minecraft:" + mc);
            p.getDependencies().add("mappings",  p.getDependencies()
                    .create("net.fabricmc:yarn:" + mc + "+build.1:v2"));
            p.getDependencies().add("modImplementation",
                    "net.fabricmc:fabric-loader:" + loader);

            project.getLogger().lifecycle(
                    "[FeatherPlugin/Fabric] minecraft=" + mc +
                    " fabricLoader=" + loader);
        });
    }
}
