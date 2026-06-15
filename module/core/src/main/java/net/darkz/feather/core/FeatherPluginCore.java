package net.darkz.feather.core;

import net.darkz.feather.common.*;
import org.gradle.api.Project;

/**
 * {@code net.darkz.feather.feather-core} plugin.
 *
 * <p>Applies the shared {@link FeatherExtension} DSL and wires up
 * common project metadata (group, version) after the extension has
 * been configured.
 *
 * <p>Apply this in every mod project that uses FeatherPlugin:
 * <pre>{@code
 * plugins {
 *     id 'net.darkz.feather.feather-core' version '1.0.0'
 * }
 * }</pre>
 */
public class FeatherPluginCore extends FeatherBasePlugin {

    @Override
    protected void applyPlugin(Project project) {
        FeatherExtension ext = ensureExtension(project);

        project.afterEvaluate(p -> {
            ModLoader loader = ext.resolvedLoader();
            info("Configured for loader: " + loader.name() +
                 " | MC: " + ext.getMinecraftVersion().getOrElse("?") +
                 " | Loader ver: " + ext.getLoaderVersion().getOrElse("?"));

            // Apply default repos when requested
            if (Boolean.TRUE.equals(ext.getIncludeDefaultRepositories().get())) {
                applyDefaultRepositories(p, loader);
            }

            // Propagate maven group back to Gradle project if set
            if (ext.getMavenGroup().isPresent()) {
                p.setGroup(ext.getMavenGroup().get());
            }
        });
    }

    // ──────────────────────────────────────────────────────────────
    // Repository helpers
    // ──────────────────────────────────────────────────────────────

    private void applyDefaultRepositories(Project project, ModLoader loader) {
        project.getRepositories().maven(r -> {
            r.setName("MavenCentral");
            r.setUrl("https://repo.maven.apache.org/maven2/");
        });

        switch (loader) {
            case FABRIC -> {
                project.getRepositories().maven(r -> {
                    r.setName("FabricMC");
                    r.setUrl("https://maven.fabricmc.net/");
                });
            }
            case QUILT -> {
                project.getRepositories().maven(r -> {
                    r.setName("QuiltMC");
                    r.setUrl("https://maven.quiltmc.org/repository/release/");
                });
                project.getRepositories().maven(r -> {
                    r.setName("FabricMC"); // Quilt still needs Fabric mappings etc.
                    r.setUrl("https://maven.fabricmc.net/");
                });
            }
            case FORGE -> {
                project.getRepositories().maven(r -> {
                    r.setName("MinecraftForge");
                    r.setUrl("https://maven.minecraftforge.net/");
                });
            }
            case NEOFORGE -> {
                project.getRepositories().maven(r -> {
                    r.setName("NeoForgedReleases");
                    r.setUrl("https://maven.neoforged.net/releases/");
                });
            }
        }
    }
}
