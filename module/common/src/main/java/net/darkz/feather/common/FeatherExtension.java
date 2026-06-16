package net.darkz.feather.common;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;
import java.util.*;

public abstract class FeatherExtension {

    public static final String NAME = "feather";

    public abstract Property<String> getLoader();
    public abstract Property<String> getMinecraftVersion();
    public abstract Property<String> getLoaderVersion();
    public abstract Property<String> getModId();
    public abstract Property<String> getMavenGroup();
    public abstract SetProperty<String> getExtraRepositories();
    public abstract Property<Boolean> getIncludeDefaultRepositories();

    // Новые поля
    public abstract Property<String> getLombok();
    public abstract Property<String> getFabricApi();

    private final List<String> disabled = new ArrayList<>();
    private final Map<String, String> overrides = new LinkedHashMap<>();

    @Inject
    public FeatherExtension(Project project) {
        getIncludeDefaultRepositories().convention(true);
        getMavenGroup().convention(project.provider(() -> project.getGroup().toString()));
    }

    // DSL метод для блока additional { }
    public void additional(groovy.lang.Closure<?> closure) {
        closure.setDelegate(this);
        closure.setResolveStrategy(groovy.lang.Closure.DELEGATE_FIRST);
        closure.call();
    }

    public void disable(String dep) {
        disabled.add(dep);
    }

    public void override(String configuration, String dep) {
        overrides.put(dep, configuration);
    }

    public boolean isDisabled(String dep) {
        return disabled.contains(dep);
    }

    public String getOverrideConfig(String dep) {
        return overrides.getOrDefault(dep, null);
    }

    public ModLoader resolvedLoader() {
        return ModLoader.fromProperty(getLoader().getOrElse("fabric"));
    }
}
