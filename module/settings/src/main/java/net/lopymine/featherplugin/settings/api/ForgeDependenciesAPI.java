package net.lopymine.featherplugin.settings.api;

import net.lopymine.featherplugin.settings.FeatherPluginSettings;

public class ForgeDependenciesAPI {

	public static String getForgeVersion(String minecraft) {
		try {
			return JsonHelper.get("https://maven.minecraftforge.net/api/maven/latest/version/releases/net/minecraftforge/forge?filter=%s-".formatted(minecraft))
					.getAsJsonObject()
					.get("version")
					.getAsString();
		} catch (Exception e) {
			FeatherPluginSettings.LOGGER.log("Failed to find forge version!");
			e.printStackTrace(System.out);
			return "unknown";
		}
	}

}
