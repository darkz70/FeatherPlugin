package net.lopymine.featherplugin.core.loader;

import java.util.*;
import net.lopymine.featherplugin.core.data.FeatherProjectConfigurationData;
import net.lopymine.featherplugin.core.extension.FeatherCoreDependenciesExtension;
import net.lopymine.featherplugin.core.manager.neoforge.NeoForgeManager;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import org.gradle.api.Project;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.plugins.PluginContainer;
import org.jetbrains.annotations.NotNull;

public class NeoForgeLoaderManager implements LoaderManager {

	private static final NeoForgeLoaderManager INSTANCE = new NeoForgeLoaderManager();

	public static NeoForgeLoaderManager getInstance() {
		return INSTANCE;
	}

	@Override
	public void applyPlugins(@NotNull FeatherProjectConfigurationData data) {
		Project project = data.project();
		PluginContainer plugins = project.getPlugins();
		plugins.apply("net.neoforged.moddev");
	}

	@Override
	public void applyDependencies(@NotNull FeatherProjectConfigurationData data, FeatherCoreDependenciesExtension dependencies) {
		NeoForgeExtension extension = data.project().getExtensions().getByType(NeoForgeExtension.class);
		extension.setVersion(dependencies.getNeoForge());
		NeoForgeManager.apply(data, extension, dependencies, "neoforge");
	}

	@Override
	public void configureExtensions(@NotNull FeatherProjectConfigurationData data) {
		data.project().afterEvaluate((project) -> {
			project.getTasks().named("createMinecraftArtifacts").configure((task) -> {
				task.dependsOn(":%s:stonecutterGenerate".formatted(data.projectName()));
			});
		});
	}

	@Override
	public String getModDependenciesImplementationMethod(FeatherProjectConfigurationData data) {
		return "implementation";
	}

	@Override
	public String getJarTaskName(FeatherProjectConfigurationData data) {
		return "jar";
	}

	@Override
	public String getAWFileExtension(FeatherProjectConfigurationData data) {
		return "cfg";
	}

	@Override
	public boolean excludeUselessFiles(FileCopyDetails details) {
		boolean excluded = false;
		for (String file : List.of("fabric.mod.json", "mods.toml")) {
			if (details.getName().equals(file)) {
				details.exclude();
				excluded = true;
			}
		}
		return excluded;
	}

	@Override
	public Map<String, String> getLoaderConfigurations(List<String> configurations, FeatherProjectConfigurationData data) {
		Map<String, String> map = new HashMap<>();
		for (String s : configurations) {
			map.put(s, s.equals("include") ? "jarJar" : s);
		}
		return map;
	}
}
