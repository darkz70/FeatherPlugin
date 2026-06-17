package net.darkz.feather.loader;

import net.darkz.feather.common.FeatherBasePlugin;
import net.darkz.feather.common.FeatherExtension;
import net.darkz.feather.common.ModLoader;
import net.darkz.feather.loader.configurator.FabricConfigurator;
import net.darkz.feather.loader.configurator.ForgeConfigurator;
import net.darkz.feather.loader.configurator.LoaderConfigurator;
import net.darkz.feather.loader.configurator.NeoForgeConfigurator;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class FeatherPluginLoader extends FeatherBasePlugin {

    @Override
    protected void applyPlugin(Project project) {
        FeatherExtension ext = ensureExtension(project);

        // Apply the loader toolchain plugin eagerly so that its DSL extensions
        // (e.g. fabric-loom's include() / minecraft / mappings configurations)
        // are available when the build script's dependencies {} block is evaluated.
        // We detect the loader from the project name (e.g. "fabric-26.1") immediately.
        String projectName = project.getName();
        if (projectName.contains("-")) {
            String loaderStr = projectName.substring(0, projectName.indexOf("-"));
            String mcVersion  = projectName.substring(projectName.indexOf("-") + 1);
            try {
                ModLoader eagerLoader = ModLoader.fromProperty(loaderStr);
                // 1. Apply the loader plugin eagerly so include() and other loom
                //    DSL methods are available at build-script configuration time.
                project.getPluginManager().apply(eagerLoader.getPluginId(mcVersion));

                // 2. Setup Java toolchain if needed (e.g. MC 26.x requires Java 25)
                if (mcVersion.startsWith("26.")) {
                    project.getExtensions().getByType(org.gradle.api.plugins.JavaPluginExtension.class)
                            .getToolchain().getLanguageVersion().set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(25));
                }

                // 3. Populate the extension eagerly so the configurator can use it
                ext.getMinecraftVersion().set(mcVersion);
                ext.getLoaderVersion().set(project.findProperty("build.fabric_loader") != null ? project.findProperty("build.fabric_loader").toString() : "unknown");

                // 4. Immediately configure the loader toolchain to avoid afterEvaluate resolution conflicts
                resolveConfigurator(eagerLoader).configure(project, ext);
            } catch (IllegalArgumentException ignored) {
                // Not a recognized loader prefix, will be applied in afterEvaluate
            }
        }

        project.afterEvaluate(p -> {
            // If the loader wasn't applied eagerly, apply it now
            if (!p.getPlugins().hasPlugin("fabric-loom") && !p.getPlugins().hasPlugin("net.fabricmc.fabric-loom") && !p.getPlugins().hasPlugin("forge") && !p.getPlugins().hasPlugin("net.neoforged.moddev")) {
                ModLoader loader = ext.resolvedLoader();
                info("Applying loader toolchain (fallback): " + loader.name());
                resolveConfigurator(loader).configure(p, ext);
            }


        });
    }

    private LoaderConfigurator resolveConfigurator(ModLoader loader) {
        switch (loader) {
            case FABRIC:   return new FabricConfigurator();
            case FORGE:    return new ForgeConfigurator();
            case NEOFORGE: return new NeoForgeConfigurator();
            default: throw new IllegalArgumentException("[FeatherPlugin] Unsupported loader: " + loader);
        }
    }
}
