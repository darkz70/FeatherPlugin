package net.darkz.feather.settings.project;

import net.darkz.feather.settings.loader.LoaderManager;

public record MossyProject(
		String projectName,
		String loaderName,
		String minecraftVersion,
		String comparableMinecraftVersion,
		LoaderManager loaderManager
) {

}
