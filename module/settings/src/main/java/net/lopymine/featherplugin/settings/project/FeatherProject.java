package net.lopymine.featherplugin.settings.project;

import net.lopymine.featherplugin.settings.loader.LoaderManager;

public record FeatherProject(
		String projectName,
		String loaderName,
		String minecraftVersion,
		String comparableMinecraftVersion,
		LoaderManager loaderManager
) {

}
