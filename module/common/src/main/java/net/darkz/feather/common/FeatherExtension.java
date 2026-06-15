package net.darkz.feather.common;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

/**
 * The {@code feather { }} DSL block available to every project that applies
 * any FeatherPlugin sub-plugin.
 *
 * <pre>{@code
 * feather {
 *     loader = "neoforge"         // fabric | quilt | forge | neoforge
 *     minecraftVersion = "1.21.1"
 *     loaderVersion = "21.1.0"
 *     modId = "my_mod"
 * }
 * }</pre>
 */
public abstract class FeatherExtension {

    public static final String NAME = "feather";

    /** One of: fabric, quilt, forge, neoforge */
    public abstract Property<String> getLoader();

    /** Minecraft version string, e.g. {@code "1.21.1"} */
    public abstract Property<String> getMinecraftVersion();

    /**
     * Loader-specific version:
     * <ul>
     *   <li>Fabric / Quilt – loader version (e.g. {@code "0.16.2"})</li>
     *   <li>Forge   – Forge version suffix (e.g. {@code "51.0.33"})</li>
     *   <li>NeoForge – NeoForge version (e.g. {@code "21.1.0"})</li>
     * </ul>
     */
    public abstract Property<String> getLoaderVersion();

    /** Mod id as declared in the mod metadata file. */
    public abstract Property<String> getModId();

    /** Maven group used when publishing. Defaults to {@code project.group}. */
    public abstract Property<String> getMavenGroup();

    /**
     * Extra repositories to add when {@code includeDefaultRepositories} is {@code true}.
     * Filled automatically based on {@link #getLoader()}.
     */
    public abstract SetProperty<String> getExtraRepositories();

    /** When {@code true} (default) the plugin registers all loader-specific maven repos. */
    public abstract Property<Boolean> getIncludeDefaultRepositories();

    @Inject
    public FeatherExtension(Project project) {
        getIncludeDefaultRepositories().convention(true);
        getMavenGroup().convention(project.provider(() -> project.getGroup().toString()));
    }

    /** Convenience: resolve the enum from the string property. */
    public ModLoader resolvedLoader() {
        return ModLoader.fromProperty(getLoader().getOrElse("fabric"));
    }
}
