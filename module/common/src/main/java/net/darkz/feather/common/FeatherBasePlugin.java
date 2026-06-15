package net.darkz.feather.common;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

/**
 * Convenience base class for every FeatherPlugin module.
 * Registers the shared {@link FeatherExtension} if not already present
 * and provides a typed logger.
 */
public abstract class FeatherBasePlugin implements Plugin<Project> {

    protected Logger log;

    @Override
    public final void apply(Project project) {
        this.log = project.getLogger();
        ensureExtension(project);
        applyPlugin(project);
    }

    /**
     * Sub-classes implement this instead of {@link #apply(Project)}.
     *
     * @param project the target project
     */
    protected abstract void applyPlugin(Project project);

    // ──────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────

    protected static FeatherExtension ensureExtension(Project project) {
        if (project.getExtensions().findByName(FeatherExtension.NAME) == null) {
            return project.getExtensions().create(
                    FeatherExtension.NAME,
                    FeatherExtension.class,
                    project
            );
        }
        return extension(project);
    }

    protected static FeatherExtension extension(Project project) {
        return project.getExtensions().getByType(FeatherExtension.class);
    }

    protected void info(String msg) {
        log.lifecycle("[FeatherPlugin] " + msg);
    }

    protected void warn(String msg) {
        log.warn("[FeatherPlugin] " + msg);
    }
}
