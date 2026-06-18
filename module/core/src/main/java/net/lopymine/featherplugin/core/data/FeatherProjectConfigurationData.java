package net.lopymine.featherplugin.core.data;

import net.lopymine.featherplugin.common.FeatherUtils;
import net.lopymine.featherplugin.core.FeatherPluginCore;
import net.lopymine.featherplugin.core.loader.LoaderManager;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public record FeatherProjectConfigurationData(
		FeatherPluginCore plugin,
		String projectName,
		String loaderName,
		String minecraftVersion,
		String comparableMinecraftVersion,
		LoaderManager loaderManager,
		Project project
) {

	public static FeatherProjectConfigurationData create(@NotNull Project project, FeatherPluginCore plugin) {
		String projectName = project.getName();
		String loaderName = FeatherUtils.substringBefore(projectName, "-");
		String minecraftVersion = FeatherUtils.substringSince(projectName, "-");
		String comparableMinecraftVersion = FeatherUtils.substringBefore(minecraftVersion, "-");
		LoaderManager loaderManager = LoaderManager.of(loaderName);
		return new FeatherProjectConfigurationData(plugin, projectName, loaderName, minecraftVersion, comparableMinecraftVersion, loaderManager, project);
	}
}
