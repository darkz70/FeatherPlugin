package net.darkz.feather.loader.configurator;

import net.darkz.feather.common.FeatherExtension;
import org.gradle.api.Project;

public class FabricConfigurator implements LoaderConfigurator {

    @Override
    public void configure(Project project, FeatherExtension ext) {
        String mc     = ext.getMinecraftVersion().getOrElse("");
        String loader = ext.getLoaderVersion().getOrElse("");
        requireProperty(ext, "minecraftVersion", mc);
        requireProperty(ext, "loaderVersion", loader);

        // Apply loom (idempotent – may already be applied eagerly by FeatherPluginLoader)
        project.getPluginManager().apply(ext.resolvedLoader().getPluginId(mc));

        // Add minecraft dependency if not already added eagerly by FeatherPluginLoader.
        // FeatherPluginLoader adds it immediately after applying loom (before loom's
        // afterEvaluate listener fires) using the MC version from the project name.
        // Here we use the authoritative value from feather{} DSL instead.
        boolean minecraftAlreadyAdded = !project.getConfigurations()
                .getByName("minecraft").getDependencies().isEmpty();
        if (!minecraftAlreadyAdded) {
            project.getDependencies().add("minecraft", "com.mojang:minecraft:" + mc);
        }

        // Mojang mappings (only if 'mappings' configuration exists)
        if (project.getConfigurations().findByName("mappings") != null) {
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
        } else {
            project.getLogger().lifecycle("[FeatherPlugin/Fabric] Skipping mappings as 'mappings' configuration is not present (normal for unobfuscated MC 26.1+)");
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
