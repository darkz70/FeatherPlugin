package net.darkz.feather.loader.configurator;

import net.darkz.feather.common.FeatherExtension;
import org.gradle.api.Project;

public class FabricConfigurator implements LoaderConfigurator {

    private static final String LOOM_PLUGIN_ID = "fabric-loom";

    @Override
    public void configure(Project project, FeatherExtension ext) {
        String mc     = ext.getMinecraftVersion().getOrElse("");
        String loader = ext.getLoaderVersion().getOrElse("");
        requireProperty(ext, "minecraftVersion", mc);
        requireProperty(ext, "loaderVersion", loader);

        project.getPluginManager().apply(LOOM_PLUGIN_ID);

        project.afterEvaluate(p -> {
            p.getDependencies().add("minecraft", "com.mojang:minecraft:" + mc);

            // Mojang маппинги вместо Yarn
            Object loom = p.getExtensions().findByName("loom");
            if (loom != null) {
                try {
                    Object mojangMappings = loom.getClass()
                            .getMethod("officialMojangMappings")
                            .invoke(loom);
                    p.getDependencies().add("mappings", mojangMappings);
                } catch (Exception e) {
                    p.getLogger().warn("[FeatherPlugin/Fabric] Mojang mappings error: " + e.getMessage());
                    // Фоллбек на Yarn
                    p.getDependencies().add("mappings",
                            p.getDependencies().create("net.fabricmc:yarn:" + mc + "+build.1:v2"));
                }
            }

            p.getDependencies().add("modImplementation", "net.fabricmc:fabric-loader:" + loader);

            // Fabric API
            String fabricApi = ext.getFabricApi().getOrElse("");
            if (!fabricApi.isEmpty() && !fabricApi.equals("unknown")) {
                p.getDependencies().add("modImplementation",
                        "net.fabricmc.fabric-api:fabric-api:" + fabricApi);
            }

            // Lombok
            String lombok = ext.getLombok().getOrElse("");
            if (!lombok.isEmpty() && !lombok.equals("unknown")) {
                p.getDependencies().add("compileOnly", "org.projectlombok:lombok:" + lombok);
                p.getDependencies().add("annotationProcessor", "org.projectlombok:lombok:" + lombok);
            }

            project.getLogger().lifecycle(
                    "[FeatherPlugin/Fabric] minecraft=" + mc + " fabricLoader=" + loader);
        });
    }
}
