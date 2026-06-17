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
            for (java.lang.reflect.Method m : constants.getClass().getMethods()) {
                if (m.getName().equals("put") && m.getParameterCount() == 2 && m.getParameterTypes()[0].equals(String.class)) {
                    putMethod = m;
                    break;
                }
            }
            
            if (putMethod == null) {
                warn("Available methods on constants class " + constants.getClass().getName() + ":");
                for (java.lang.reflect.Method m : constants.getClass().getMethods()) {
                    warn("  " + m.getName() + "(" + java.util.Arrays.toString(m.getParameterTypes()) + ")");
                }
                throw new NoSuchMethodException("Could not find put(String, ...) method");
            }

            for (ModLoader ml : ModLoader.values()) {
                try {
                    putMethod.invoke(constants, ml.id, ml == loader);
                } catch (Exception e) {
                    putMethod.invoke(constants, ml.id, (Object) (ml == loader));
                }
            }

            try {
                putMethod.invoke(constants, "fabric_like", loader.isFabricLike());
            } catch (Exception e) {
                putMethod.invoke(constants, "fabric_like", (Object) loader.isFabricLike());
            }

            try {
                putMethod.invoke(constants, "forge_like", loader.isForge() || loader.isNeoForge());
            } catch (Exception e) {
                putMethod.invoke(constants, "forge_like", (Object) (loader.isForge() || loader.isNeoForge()));
            }

            info("Stonecutter constants injected for loader=" + loader.id);
        } catch (Exception e) {
            warn("Could not inject Stonecutter constants: " + e.getMessage());
        }
    }
}
