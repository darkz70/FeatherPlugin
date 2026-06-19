package net.darkz70.featherplugin.core.data;

import net.darkz70.featherplugin.common.FeatherUtils;
import net.darkz70.featherplugin.core.FeatherPluginCore;
import net.darkz70.featherplugin.core.extension.FeatherCoreDependenciesExtension;
import net.darkz70.featherplugin.core.loader.LoaderManager;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public record FeatherProjectConfigurationData(
		FeatherPluginCore plugin,
		String projectName,
		String loaderName,
		String minecraftVersion,
		String comparableMinecraftVersion,
		LoaderManager loaderManager,
		Project project,
		FeatherCoreDependenciesExtension feather
) {

	public static FeatherProjectConfigurationData create(@NotNull Project project, FeatherPluginCore plugin, FeatherCoreDependenciesExtension feather) {
		String projectName = project.getName();
		String loaderName = FeatherUtils.substringBefore(projectName, "-");
		String minecraftVersion = FeatherUtils.substringSince(projectName, "-");
		String comparableMinecraftVersion = FeatherUtils.substringBefore(minecraftVersion, "-");
		LoaderManager loaderManager = LoaderManager.of(loaderName);
		return new FeatherProjectConfigurationData(plugin, projectName, loaderName, minecraftVersion, comparableMinecraftVersion, loaderManager, project, feather);
	}
}
