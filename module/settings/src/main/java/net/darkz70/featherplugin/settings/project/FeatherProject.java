package net.darkz70.featherplugin.settings.project;

import net.darkz70.featherplugin.settings.loader.LoaderManager;

public record FeatherProject(
		String projectName,
		String loaderName,
		String minecraftVersion,
		String comparableMinecraftVersion,
		LoaderManager loaderManager
) {

}
