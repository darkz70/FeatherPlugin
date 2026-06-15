package net.darkz.feather.settings;

import dev.kikugie.stonecutter.StonecutterSettings;
import net.darkz.feather.common.ModLoader;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

/**
 * {@code net.darkz.feather.feather-settings} plugin.
 *
 * <p>Applied in {@code settings.gradle} (not {@code build.gradle}).
 * Provides a {@code featherVersions { }} block that generates the
 * Stonecutter version matrix from a compact DSL:
 *
 * <pre>{@code
 * // settings.gradle
 * plugins {
 *     id 'dev.kikugie.stonecutter'
 *     id 'net.darkz.feather.feather-settings' version '1.0.0'
 * }
 *
 * featherVersions {
 *     // each call: mc version + one or more loaders
 *     version("1.21.1", "fabric", "quilt", "neoforge")
 *     version("1.20.1", "fabric", "forge")
 * }
 * }</pre>
 *
 * <p>This translates to Stonecutter sub-project names like
 * {@code 1.21.1-fabric}, {@code 1.21.1-quilt}, {@code 1.20.1-forge}, etc.
 */
public class FeatherPluginSettings implements Plugin<Settings> {

    @Override
    public void apply(Settings settings) {
        FeatherVersionsExtension ext = settings.getExtensions()
                .create("featherVersions", FeatherVersionsExtension.class, settings);

        settings.getGradle().settingsEvaluated(s -> {
            if (ext.getEntries().isEmpty()) return;

            // Attempt to wire into Stonecutter settings if present
            Object scSettings = settings.getExtensions().findByName("stonecutter");
            if (scSettings == null) {
                settings.getLogger().warn(
                        "[FeatherPlugin/Settings] 'stonecutter' extension not found. " +
                        "Apply 'dev.kikugie.stonecutter' before 'feather-settings'.");
                return;
            }

            try {
                Object shared = scSettings.getClass()
                        .getMethod("getShared").invoke(scSettings);

                for (FeatherVersionsExtension.Entry entry : ext.getEntries()) {
                    for (String loader : entry.loaders()) {
                        // Validate loader
                        ModLoader.fromProperty(loader);
                        String versionName = entry.mcVersion() + "-" + loader;
                        shared.getClass()
                              .getMethod("versions", String[].class)
                              .invoke(shared, (Object) new String[]{versionName});
                    }
                }

                settings.getLogger().lifecycle(
                        "[FeatherPlugin/Settings] Registered " + ext.getEntries().size() +
                        " MC version(s) with " + ext.totalLoaderCount() + " loader variant(s).");
            } catch (Exception e) {
                settings.getLogger().warn(
                        "[FeatherPlugin/Settings] Could not configure Stonecutter: " + e.getMessage());
            }
        });
    }
}
