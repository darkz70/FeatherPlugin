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

        // Apply loom (idempotent – may already be applied eagerly by FeatherPluginLoader)
        project.getPluginManager().apply(LOOM_PLUGIN_ID);

        // Add minecraft and mappings dependencies EAGERLY (not in afterEvaluate).
        // fabric-loom registers its own afterEvaluate listener that calls
        // "setupMinecraft" which requires the 'minecraft' configuration to be
        // populated before that listener fires. Adding them here ensures they
        // are present when loom's listener runs.
        project.getDependencies().add("minecraft", "com.mojang:minecraft:" + mc);

        // Mojang mappings
        Object loom = project.getExtensions().findByName("loom");
        if (loom != null) {
            try {
                Object mojangMappings = loom.getClass()
                        .getMethod("officialMojangMappings")
                        .invoke(loom);
                project.getDependencies().add("mappings", mojangMappings);
            } catch (Exception e) {
                project.getLogger().warn("[FeatherPlugin/Fabric] Mojang mappings error: " + e.getMessage());
                // Fallback to Yarn
                project.getDependencies().add("mappings",
                        project.getDependencies().create("net.fabricmc:yarn:" + mc + "+build.1:v2"));
            }
        }

        project.getDependencies().add("modImplementation", "net.fabricmc:fabric-loader:" + loader);

        // Fabric API
        String fabricApi = ext.getFabricApi().getOrElse("");
        if (!fabricApi.isEmpty() && !fabricApi.equals("unknown")) {
            project.getDependencies().add("modImplementation",
                    "net.fabricmc.fabric-api:fabric-api:" + fabricApi);
        }

        // Lombok
        String lombok = ext.getLombok().getOrElse("");
        if (!lombok.isEmpty() && !lombok.equals("unknown")) {
            project.getDependencies().add("compileOnly", "org.projectlombok:lombok:" + lombok);
            project.getDependencies().add("annotationProcessor", "org.projectlombok:lombok:" + lombok);
        }

        project.getLogger().lifecycle(
                "[FeatherPlugin/Fabric] minecraft=" + mc + " fabricLoader=" + loader);
    }
}
