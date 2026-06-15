package net.darkz.feather.loader.configurator;

import net.darkz.feather.common.FeatherExtension;
import org.gradle.api.Project;

/**
 * Configures <a href="https://github.com/MinecraftForge/ForgeGradle">ForgeGradle</a>
 * (the legacy Minecraft Forge toolchain) for the target project.
 *
 * <pre>{@code
 * feather {
 *     loader          = "forge"
 *     minecraftVersion = "1.20.1"
 *     loaderVersion   = "47.3.0"   // Forge version (the part after MC version)
 * }
 * }</pre>
 *
 * The full Forge dependency coordinate becomes
 * {@code net.minecraftforge:forge:<mc>-<forge>}.
 */
public class ForgeConfigurator implements LoaderConfigurator {

    private static final String FORGE_GRADLE_PLUGIN_ID = "net.minecraftforge.gradle";

    @Override
    public void configure(Project project, FeatherExtension ext) {
        String mc    = ext.getMinecraftVersion().getOrElse("");
        String forge = ext.getLoaderVersion().getOrElse("");
        requireProperty(ext, "minecraftVersion", mc);
        requireProperty(ext, "loaderVersion",    forge);

        project.getPluginManager().apply(FORGE_GRADLE_PLUGIN_ID);

        project.afterEvaluate(p -> {
            // ForgeGradle exposes a 'minecraft' extension – configure via raw map
            // to avoid a compile-time dependency on ForgeGradle's classes.
            Object forgeExt = p.getExtensions().findByName("minecraft");
            if (forgeExt != null) {
                try {
                    forgeExt.getClass()
                            .getMethod("setMappings", String.class, String.class)
                            .invoke(forgeExt, "official", mc);
                } catch (Exception e) {
                    p.getLogger().warn("[FeatherPlugin/Forge] Could not set mappings: " + e.getMessage());
                }
            }

            String coord = "net.minecraftforge:forge:" + mc + "-" + forge;
            p.getDependencies().add("forge", coord);

            project.getLogger().lifecycle(
                    "[FeatherPlugin/Forge] forge=" + coord);
        });
    }
}
