package net.darkz.feather.core.manager;

import dev.kikugie.stonecutter.build.StonecutterBuildExtension;
import java.util.Map;
import lombok.experimental.ExtensionMethod;
import net.darkz.feather.core.FeatherPluginCore;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

@ExtensionMethod(FeatherPluginCore.class)
public class StonecutterManager {

	public static void apply(@NotNull Project project, FeatherPluginCore plugin) {
		StonecutterBuildExtension stonecutter = project.getStonecutter();

		String mcVersion = plugin.getProjectMultiVersion().projectVersion();
		Map<String, String> properties = project.getMossyProperties("data");
		properties.putAll(project.getMossyProperties("build"));
		Map<String, String> dependencies = project.getMossyProperties("dep");
		properties.putAll(dependencies);
		properties.put("java", String.valueOf(plugin.getJavaVersionIndex()));
		properties.put("minecraft", mcVersion);
		properties.put("fabric_api_id", project.getStonecutter().compare("1.19.1", mcVersion) >= 0 ? "fabric" : "fabric-api");
		properties.put("mod_version", project.getVersion().toString());

		properties.forEach((key, value) -> {
			stonecutter.getSwaps().put(key, getFormatted(value));
		});

		dependencies.forEach((modId, version) -> {
			stonecutter.getConstants().put(modId, !version.equals("unknown"));
		});
	}

	private static @NotNull String getFormatted(String modVersion) {
		return "\"%s\";".formatted(modVersion);
	}
}
