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
