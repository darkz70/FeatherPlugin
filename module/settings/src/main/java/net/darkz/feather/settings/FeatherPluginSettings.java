package net.darkz.feather.settings;

import net.darkz.feather.common.ModLoader;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

public class FeatherPluginSettings implements Plugin<Settings> {

    @Override
    public void apply(Settings settings) {
        FeatherVersionsExtension ext = settings.getExtensions()
                .create("featherVersions", FeatherVersionsExtension.class, settings);

        settings.getGradle().settingsEvaluated(s -> {
            if (ext.getEntries().isEmpty()) return;

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
