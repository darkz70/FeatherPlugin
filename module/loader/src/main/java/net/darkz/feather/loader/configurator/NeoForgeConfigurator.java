package net.darkz.feather.loader.configurator;

import net.darkz.feather.common.FeatherExtension;
import org.gradle.api.Project;

public class NeoForgeConfigurator implements LoaderConfigurator {

    private static final String MODDEV_PLUGIN_ID = "net.neoforged.moddev";

    @Override
    public void configure(Project project, FeatherExtension ext) {
        String neoForge = ext.getLoaderVersion().getOrElse("");
        requireProperty(ext, "loaderVersion", neoForge);

        project.getPluginManager().apply(MODDEV_PLUGIN_ID);

        project.afterEvaluate(p -> {
            Object neoExt = p.getExtensions().findByName("neoForge");
            if (neoExt == null) {
                p.getLogger().warn("[FeatherPlugin/NeoForge] 'neoForge' extension not found.");
                return;
            }
            try {
                // Устанавливаем версию NeoForge
                neoExt.getClass()
                      .getMethod("setVersion", String.class)
                      .invoke(neoExt, neoForge);

                // Отключаем parchment
                try {
                    Object parchment = neoExt.getClass()
                            .getMethod("getParchment").invoke(neoExt);
                    if (parchment != null) {
                        // Пробуем setEnabled(false)
                        try {
                            parchment.getClass()
                                     .getMethod("setEnabled", boolean.class)
                                     .invoke(parchment, false);
                        } catch (NoSuchMethodException ignored) {}

                        // Пробуем getEnabled().set(false)
                        try {
                            Object enabled = parchment.getClass()
                                    .getMethod("getEnabled").invoke(parchment);
                            enabled.getClass()
                                   .getMethod("set", Object.class)
                                   .invoke(enabled, false);
                        } catch (Exception ignored) {}
                    }
                } catch (Exception e) {
                    p.getLogger().info("[FeatherPlugin/NeoForge] Parchment disable skipped: " + e.getMessage());
                }

            } catch (Exception e) {
                p.getLogger().warn("[FeatherPlugin/NeoForge] Could not configure: " + e.getMessage());
            }

            // Lombok
            String lombok = ext.getLombok().getOrElse("");
            if (!lombok.isEmpty() && !lombok.equals("unknown")) {
                p.getDependencies().add("compileOnly", "org.projectlombok:lombok:" + lombok);
                p.getDependencies().add("annotationProcessor", "org.projectlombok:lombok:" + lombok);
            }

            project.getLogger().lifecycle("[FeatherPlugin/NeoForge] neoforge=" + neoForge);
        });
    }
}
EOF
echo "done NeoForgeConfigurator"

# FeatherPluginSettings - исправляем регистрацию версий Stonecutter
cat << 'EOF' > /mnt/user-data/outputs/FeatherPlugin-fixed/settings/FeatherPluginSettings.java
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
                log.warn("[FeatherPlugin/Settings] 'stonecutter' extension not found. " +
                         "Apply 'dev.kikugie.stonecutter' before 'feather-settings'.");
                return;
            }

            try {
                Object shared = scSettings.getClass()
                        .getMethod("getShared").invoke(scSettings);

                // Ищем метод vers в Stonecutter 0.9
                Method versMethod = null;
                for (Method m : shared.getClass().getMethods()) {
                    if (m.getName().equals("vers") && m.getParameterCount() == 2) {
                        versMethod = m;
                        break;
                    }
                }

                // Если не нашли vers(2) - ищем vers(1)
                if (versMethod == null) {
                    for (Method m : shared.getClass().getMethods()) {
                        if (m.getName().equals("vers") && m.getParameterCount() == 1) {
                            versMethod = m;
                            break;
                        }
                    }
                }

                if (versMethod == null) {
                    log.warn("[FeatherPlugin/Settings] 'vers' method not found in Stonecutter shared. " +
                             "Register versions manually in settings.gradle.");
                    return;
                }

                for (FeatherVersionsExtension.Entry entry : ext.getEntries()) {
                    for (String loader : entry.loaders()) {
                        ModLoader.fromProperty(loader); // валидация
                        String versionName = entry.mcVersion() + "-" + loader;

                        if (versMethod.getParameterCount() == 2) {
                            versMethod.invoke(shared, versionName, versionName);
                        } else {
                            versMethod.invoke(shared, versionName);
                        }
                        log.lifecycle("[FeatherPlugin/Settings] Registered: " + versionName);
                    }
                }

                log.lifecycle("[FeatherPlugin/Settings] Done. " +
                              ext.totalLoaderCount() + " variant(s) registered.");
            } catch (Exception e) {
                log.warn("[FeatherPlugin/Settings] Error: " + e.getMessage(), e);
            }
        });
    }
}
