package net.darkz.feather.loader.configurator;

import net.darkz.feather.common.FeatherExtension;
import org.gradle.api.Project;

/**
 * Strategy interface: each loader has its own implementation that applies
 * and configures the relevant Gradle plugin (Loom / ForgeGradle / ModDevGradle).
 */
public interface LoaderConfigurator {

    /**
     * Apply and configure the loader-specific Gradle plugin.
     *
     * @param project the Gradle project
     * @param ext     the resolved {@link FeatherExtension} block
     */
    void configure(Project project, FeatherExtension ext);

    // ──────────────────────────────────────────────────────────────
    // Shared helpers
    // ──────────────────────────────────────────────────────────────

    /**
     * Checks that a required extension property has a value and throws a
     * meaningful error if it is missing.
     */
    default void requireProperty(FeatherExtension ext, String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "[FeatherPlugin] 'feather." + name + "' must be set when using loader=" +
                    ext.resolvedLoader().id
            );
        }
    }
}
