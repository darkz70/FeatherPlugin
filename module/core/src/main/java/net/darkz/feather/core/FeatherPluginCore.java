package net.darkz.feather.core;

import net.darkz.feather.common.*;
import org.gradle.api.Project;

public class FeatherPluginCore extends FeatherBasePlugin {

    @Override
    protected void applyPlugin(Project project) {
        FeatherExtension ext = ensureExtension(project);

        project.afterEvaluate(p -> {
            // Автоопределение loader и mc из имени subproject если не задано
            String loaderVal = ext.getLoader().getOrElse("");
            String mcVal = ext.getMinecraftVersion().getOrElse("");

            if (loaderVal.isEmpty() || mcVal.isEmpty()) {
                // Пробуем получить из Stonecutter
                Object scExt = p.getExtensions().findByName("stonecutter");
                if (scExt != null) {
                    try {
                        Object current = scExt.getClass().getMethod("getCurrent").invoke(scExt);
                        String projectName = (String) current.getClass()
                                .getMethod("getProject").invoke(current);
                        // Формат: "26.1.2-fabric" или "fabric-26.1.2"
                        if (projectName.contains("-")) {
                            int idx = projectName.lastIndexOf("-");
                            String part1 = projectName.substring(0, idx);
                            String part2 = projectName.substring(idx + 1);
                            // Определяем что loader а что mc
                            boolean part2IsLoader = part2.matches("fabric|neoforge|forge|quilt");
                            if (part2IsLoader) {
                                if (loaderVal.isEmpty()) ext.getLoader().set(part2);
                                if (mcVal.isEmpty()) ext.getMinecraftVersion().set(part1);
                            } else {
                                if (loaderVal.isEmpty()) ext.getLoader().set(part1);
                                if (mcVal.isEmpty()) ext.getMinecraftVersion().set(part2);
                            }
                        }
                    } catch (Exception e) {
                        warn("Could not auto-detect loader from Stonecutter: " + e.getMessage());
                    }
                }
            }

            ModLoader loader = ext.resolvedLoader();
            String mc = ext.getMinecraftVersion().getOrElse("");
            info("Configured for loader: " + loader.name() +
                 " | MC: " + mc +
                 " | Loader ver: " + ext.getLoaderVersion().getOrElse("?"));

            // Setup Java toolchain for 26.x if not already done by loader
            if (mc.startsWith("26.")) {
                try {
                    p.getExtensions().getByType(org.gradle.api.plugins.JavaPluginExtension.class)
                            .getToolchain().getLanguageVersion().set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(25));
                } catch (Exception ignored) {}
            }

            if (Boolean.TRUE.equals(ext.getIncludeDefaultRepositories().get())) {
                applyDefaultRepositories(p, loader);
            }

            if (ext.getMavenGroup().isPresent()) {
                p.setGroup(ext.getMavenGroup().get());
            }
        });
    }

    private void applyDefaultRepositories(Project project, ModLoader loader) {
        project.getRepositories().maven(r -> {
            r.setName("MavenCentral");
            r.setUrl("https://repo.maven.apache.org/maven2/");
        });
        project.getRepositories().maven(r -> {
            r.setName("Modrinth");
            r.setUrl("https://api.modrinth.com/maven");
        });
        project.getRepositories().maven(r -> {
            r.setName("TerraformersMC");
            r.setUrl("https://maven.terraformersmc.com/");
        });
        project.getRepositories().maven(r -> {
            r.setName("isxander");
            r.setUrl("https://maven.isxander.dev/releases");
        });

        switch (loader) {
            case FABRIC -> project.getRepositories().maven(r -> {
                r.setName("FabricMC");
                r.setUrl("https://maven.fabricmc.net/");
            });
            case QUILT -> {
                project.getRepositories().maven(r -> {
                    r.setName("QuiltMC");
                    r.setUrl("https://maven.quiltmc.org/repository/release/");
                });
                project.getRepositories().maven(r -> {
                    r.setName("FabricMC");
                    r.setUrl("https://maven.fabricmc.net/");
                });
            }
            case FORGE -> project.getRepositories().maven(r -> {
                r.setName("MinecraftForge");
                r.setUrl("https://maven.minecraftforge.net/");
            });
            case NEOFORGE -> project.getRepositories().maven(r -> {
                r.setName("NeoForgedReleases");
                r.setUrl("https://maven.neoforged.net/releases/");
            });
        }
    }
}
