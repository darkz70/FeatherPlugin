package net.darkz.feather.settings;

import net.darkz.feather.common.ModLoader;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.lang.reflect.Method;

public class FeatherPluginSettings implements Plugin<Settings> {

    private static final Logger log = Logging.getLogger(FeatherPluginSettings.class);

    @Override
    public void apply(Settings settings) {
        FeatherVersionsExtension ext = settings.getExtensions()
                .create("featherVersions", FeatherVersionsExtension.class, settings);

        settings.getGradle().settingsEvaluated(s -> {
            if (ext.getEntries().isEmpty()) return;

            Object scSettings = settings.getExtensions().findByName("stonecutter");
            if (scSettings == null) {
                log.warn("[FeatherPlugin/Settings] 'stonecutter' extension not found.");
                return;
            }

            try {
                Object shared = scSettings.getClass()
                        .getMethod("getShared").invoke(scSettings);

                // Ищем правильный метод в Stonecutter 0.9
                Method versMethod = null;
                for (Method m : shared.getClass().getMethods()) {
                    if (m.getName().equals("vers")) {
                        versMethod = m;
                        break;
                    }
                }

                if (versMethod == null) {
                    log.warn("[FeatherPlugin/Settings] Could not find 'vers' method. Available methods:");
                    for (Method m : shared.getClass().getMethods()) {
                        if (!m.getDeclaringClass().equals(Object.class)) {
                            log.warn("  - " + m.getName() + "(" + m.getParameterCount() + " params)");
                        }
                    }
                    return;
                }

                for (FeatherVersionsExtension.Entry entry : ext.getEntries()) {
                    for (String loader : entry.loaders()) {
                        ModLoader.fromProperty(loader);
                        String versionName = entry.mcVersion() + "-" + loader;

                        // Stonecutter 0.9: vers(String id, String branch)
                        if (versMethod.getParameterCount() == 2) {
                            versMethod.invoke(shared, versionName, versionName);
                        } else if (versMethod.getParameterCount() == 1) {
                            versMethod.invoke(shared, versionName);
                        }

                        log.lifecycle("[FeatherPlugin/Settings] Registered version: " + versionName);
                    }
                }

                // Устанавливаем active версию (последнюю)
                try {
                    String lastEntry = ext.getEntries().getLast().mcVersion() + "-" +
                            ext.getEntries().getLast().loaders().getLast();
                    for (Method m : scSettings.getClass().getMethods()) {
                        if (m.getName().equals("setActive") && m.getParameterCount() == 1) {
                            m.invoke(scSettings, lastEntry);
                            break;
                        }
                    }
                } catch (Exception ignored) {}

                log.lifecycle("[FeatherPlugin/Settings] Done. " + ext.totalLoaderCount() + " variant(s) registered.");
            } catch (Exception e) {
                log.warn("[FeatherPlugin/Settings] Error: " + e.getMessage(), e);
            }
        });
    }
}
