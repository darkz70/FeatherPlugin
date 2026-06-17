package net.darkz.feather.stonecutter;

import net.darkz.feather.common.FeatherBasePlugin;
import net.darkz.feather.common.FeatherExtension;
import net.darkz.feather.common.ModLoader;
import org.gradle.api.Project;

public class FeatherPluginStonecutter extends FeatherBasePlugin {

    @Override
    protected void applyPlugin(Project project) {
        FeatherExtension ext = ensureExtension(project);

        project.afterEvaluate(p -> {
            ModLoader loader;
            try {
                loader = ext.resolvedLoader();
            } catch (IllegalArgumentException e) {
                warn("Skipping Stonecutter constants: " + e.getMessage());
                return;
            }
            injectLoaderConstants(p, loader);
        });
    }

    private void injectLoaderConstants(Project project, ModLoader loader) {
        Object scExt = project.getExtensions().findByName("stonecutter");
        if (scExt == null) return;

        try {
            Object constants = scExt.getClass().getMethod("getConstants").invoke(scExt);
            java.lang.reflect.Method putMethod = null;
            try {
                putMethod = constants.getClass().getMethod("put", String.class, Object.class);
            } catch (NoSuchMethodException e) {
                putMethod = constants.getClass().getMethod("put", String.class, Boolean.class);
            }

            for (ModLoader ml : ModLoader.values()) {
                putMethod.invoke(constants, ml.id, ml == loader);
            }

            putMethod.invoke(constants, "fabric_like", loader.isFabricLike());
            putMethod.invoke(constants, "forge_like", loader.isForge() || loader.isNeoForge());

            info("Stonecutter constants injected for loader=" + loader.id);
        } catch (Exception e) {
            warn("Could not inject Stonecutter constants: " + e.getMessage());
        }
    }
}
