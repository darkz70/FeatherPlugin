package net.darkz.feather.settings;

import net.darkz.feather.common.ModLoader;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FeatherPluginSettings implements Plugin<Settings> {

    private static final Logger log = Logging.getLogger(FeatherPluginSettings.class);

    @Override
    public void apply(Settings settings) {
        FeatherVersionsExtension ext = settings.getExtensions()
                .create("featherVersions", FeatherVersionsExtension.class, settings);

        // We use a different approach: instead of calling Stonecutter, 
        // we provide the data that Stonecutter will use.
        // However, Stonecutter 0.9 evaluates its block immediately.
        // Let's try to register them directly when the DSL is called.
        
        ext.setOnUpdate(() -> {
            Object scSettings = settings.getExtensions().findByName("stonecutter");
            if (scSettings == null) return;
            try {
                Object shared = scSettings.getClass().getMethod("getShared").invoke(scSettings);
                Method versMethod = null;
                for (Method m : shared.getClass().getMethods()) {
                    if (m.getName().equals("vers")) {
                        versMethod = m;
                        break;
                    }
                }
                if (versMethod == null) return;

                FeatherVersionsExtension.Entry last = ext.getEntries().get(ext.getEntries().size() - 1);
                for (String loader : last.loaders()) {
                    String versionName = loader + "-" + last.mcVersion();
                    if (versMethod.getParameterCount() == 2) {
                        versMethod.invoke(shared, versionName, versionName);
                    } else {
                        versMethod.invoke(shared, versionName);
                    }
                    log.lifecycle("[FeatherPlugin/Settings] Registered version: " + versionName);
                }
            } catch (Exception e) {
                // Ignore errors during DSL evaluation
            }
        });
        
        settings.getGradle().settingsEvaluated(s -> {
            // Task Aggregation
            Map<String, List<String>> loaderVariants = new TreeMap<>();
            for (FeatherVersionsExtension.Entry entry : ext.getEntries()) {
                for (String loader : entry.loaders()) {
                    loaderVariants.computeIfAbsent(loader, k -> new ArrayList<>()).add(loader + "-" + entry.mcVersion());
                }
            }
            
            settings.getGradle().rootProject(root -> {
                loaderVariants.forEach((loader, variants) -> {
                    root.getTasks().register("build+" + loader + "+All", t -> {
                        t.setGroup("build");
                        for (String v : variants) t.dependsOn(":" + v + ":build");
                    });
                    root.getTasks().register("publish+" + loader + "+All", t -> {
                        t.setGroup("publishing");
                        for (String v : variants) t.dependsOn(":" + v + ":publish");
                    });
                });
            });
        });
    }
}
