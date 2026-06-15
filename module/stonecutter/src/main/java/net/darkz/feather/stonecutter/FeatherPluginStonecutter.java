package net.darkz.feather.stonecutter;

import dev.kikugie.stonecutter.StonecutterSettings;
import net.darkz.feather.common.*;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

/**
 * {@code net.darkz.feather.feather-stonecutter} plugin.
 *
 * <p>Wraps <a href="https://stonecutter.kikugie.dev">Stonecutter</a> and
 * automatically injects mod-loader boolean constants so you can write
 * version-and-loader-aware preprocessor comments in your source:
 *
 * <pre>{@code
 * //? if fabric {
 * FabricLoader.getInstance().getObjectShare().put(MY_KEY, value);
 * //?}
 * //? if neoforge {
 * ModList.get().getModContainerById(MOD_ID).ifPresent(c -> ...);
 * //?}
 * }</pre>
 *
 * <p>Apply in {@code settings.gradle}:
 * <pre>{@code
 * plugins {
 *     id 'dev.kikugie.stonecutter'
 *     id 'net.darkz.feather.feather-stonecutter' version '1.0.0'
 * }
 * stonecutter {
 *     centralScript = "build.gradle"
 *     shared {
 *         fun mc(version: String, vararg loaders: String) {
 *             loaders.forEach { versions("${version}-${it}") }
 *         }
 *         mc("1.21.1", "fabric", "quilt", "forge", "neoforge")
 *     }
 *     create(rootProject)
 * }
 * }</pre>
 *
 * In each versioned {@code build.gradle}, call:
 * <pre>{@code
 * feather {
 *     loader = stonecutter.current.project.substringAfterLast('-')
 *     minecraftVersion = stonecutter.current.version
 * }
 * }</pre>
 */
public class FeatherPluginStonecutter extends FeatherBasePlugin {

    @Override
    protected void applyPlugin(Project project) {
        FeatherExtension ext = ensureExtension(project);

        project.afterEvaluate(p -> {
            // Resolve loader constant and inject into Stonecutter if present
            ModLoader loader;
            try {
                loader = ext.resolvedLoader();
            } catch (IllegalArgumentException e) {
                warn("Skipping Stonecutter constants: " + e.getMessage());
                return;
            }

            injectLoaderConstants(p, loader);
        });
    }

    // ──────────────────────────────────────────────────────────────
    // Stonecutter constant injection
    // ──────────────────────────────────────────────────────────────

    private void injectLoaderConstants(Project project, ModLoader loader) {
        Object scExt = project.getExtensions().findByName("stonecutter");
        if (scExt == null) {
            // Stonecutter not applied – silently skip
            return;
        }

        // Use reflection so the stonecutter compile-time dep is optional at runtime
        try {
            Object constants = scExt.getClass().getMethod("getConstants").invoke(scExt);

            for (ModLoader ml : ModLoader.values()) {
                boolean active = ml == loader;
                constants.getClass()
                         .getMethod("put", String.class, Boolean.class)
                         .invoke(constants, ml.id, active);
            }

            // Convenience aliases
            constants.getClass()
                     .getMethod("put", String.class, Boolean.class)
                     .invoke(constants, "fabric_like", loader.isFabricLike());
            constants.getClass()
                     .getMethod("put", String.class, Boolean.class)
                     .invoke(constants, "forge_like",
                             loader.isForge() || loader.isNeoForge());

            info("Stonecutter constants injected for loader=" + loader.id);
        } catch (Exception e) {
            warn("Could not inject Stonecutter constants: " + e.getMessage());
        }
    }
}
