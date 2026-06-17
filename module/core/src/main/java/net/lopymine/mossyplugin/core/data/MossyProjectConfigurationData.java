package net.darkz.feather.core.data;

import net.darkz.feather.common.MossyUtils;
import net.darkz.feather.core.FeatherPluginCore;
import net.darkz.feather.core.loader.LoaderManager;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public record MossyProjectConfigurationData(
		FeatherPluginCore plugin,
		String projectName,
		String loaderName,
		String minecraftVersion,
		String comparableMinecraftVersion,
		LoaderManager loaderManager,
		Project project
) {

	public static MossyProjectConfigurationData create(@NotNull Project project, FeatherPluginCore plugin) {
		String projectName = project.getName();
		String loaderName = MossyUtils.substringBefore(projectName, "-");
		String minecraftVersion = MossyUtils.substringSince(projectName, "-");
		String comparableMinecraftVersion = MossyUtils.substringBefore(minecraftVersion, "-");
		LoaderManager loaderManager = LoaderManager.of(loaderName);
		return new MossyProjectConfigurationData(plugin, projectName, loaderName, minecraftVersion, comparableMinecraftVersion, loaderManager, project);
	}
}
