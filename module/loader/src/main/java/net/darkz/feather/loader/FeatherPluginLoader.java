package net.darkz.feather.loader;

import net.darkz.feather.common.FeatherBasePlugin;
import net.darkz.feather.common.FeatherExtension;
import net.darkz.feather.common.ModLoader;
import net.darkz.feather.loader.configurator.FabricConfigurator;
import net.darkz.feather.loader.configurator.ForgeConfigurator;
import net.darkz.feather.loader.configurator.LoaderConfigurator;
import net.darkz.feather.loader.configurator.NeoForgeConfigurator;
import org.gradle.api.Project;

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
            try {
                ModLoader eagerLoader = ModLoader.fromProperty(loaderStr);
                project.getPluginManager().apply(eagerLoader.gradlePluginId);
            } catch (IllegalArgumentException ignored) {
                // Not a recognized loader prefix, will be applied in afterEvaluate
            }
        }

        project.afterEvaluate(p -> {
            ModLoader loader = ext.resolvedLoader();
            info("Applying loader toolchain: " + loader.name());
            resolveConfigurator(loader).configure(p, ext);
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
