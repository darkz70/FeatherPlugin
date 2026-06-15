package net.darkz.feather.loader.configurator;

import net.darkz.feather.common.FeatherExtension;
import org.gradle.api.Project;

/**
 * Configures <a href="https://github.com/neoforged/ModDevGradle">NeoForge ModDevGradle</a>
 * for the target project.
 *
 * <pre>{@code
 * feather {
 *     loader          = "neoforge"
 *     minecraftVersion = "1.21.1"  // used for metadata only; NeoForge embeds MC
 *     loaderVersion   = "21.1.0"   // NeoForge version
 * }
 * }</pre>
 *
 * <p>ModDevGradle 2.x derives the Minecraft version from the NeoForge version
 * so only {@code loaderVersion} is strictly required, but we keep
 * {@code minecraftVersion} for documentation/consistency.
 */
public class NeoForgeConfigurator implements LoaderConfigurator {

    private static final String MODDEV_PLUGIN_ID = "net.neoforged.moddev";

    @Override
    public void configure(Project project, FeatherExtension ext) {
        String mc       = ext.getMinecraftVersion().getOrElse("");
        String neoForge = ext.getLoaderVersion().getOrElse("");
        requireProperty(ext, "loaderVersion", neoForge);

        project.getPluginManager().apply(MODDEV_PLUGIN_ID);

        project.afterEvaluate(p -> {
            // ModDevGradle exposes a 'neoForge { }' extension.
            // We configure it reflectively to avoid a compile-time dep.
            Object neoExt = p.getExtensions().findByName("neoForge");
            if (neoExt == null) {
                p.getLogger().warn("[FeatherPlugin/NeoForge] 'neoForge' extension not found. " +
                                   "Make sure ModDevGradle is on the buildscript classpath.");
                return;
            }
            try {
                // neoForge { version = "21.1.0" }
                neoExt.getClass()
                      .getMethod("setVersion", String.class)
                      .invoke(neoExt, neoForge);
            } catch (Exception e) {
                p.getLogger().warn("[FeatherPlugin/NeoForge] Could not set version: " + e.getMessage());
            }

            project.getLogger().lifecycle(
                    "[FeatherPlugin/NeoForge] neoforge=" + neoForge +
                    (mc.isBlank() ? "" : " mc=" + mc));
        });
    }
}
