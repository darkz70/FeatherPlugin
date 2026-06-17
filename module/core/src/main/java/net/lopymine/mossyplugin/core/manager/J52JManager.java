package net.darkz.feather.core.manager;

import dev.kikugie.fletching_table.extension.FletchingTableExtension;
import lombok.experimental.ExtensionMethod;
import net.darkz.feather.core.FeatherPluginCore;
import net.darkz.feather.core.data.MossyProjectConfigurationData;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

@ExtensionMethod(FeatherPluginCore.class)
public class J52JManager {

	public static void apply(@NotNull MossyProjectConfigurationData data) {
		Project project = data.project();
		project.getExtensions().configure(FletchingTableExtension.class, (extension) -> {
			var main = extension.getJ52j().register("main");
			main.configure((container) -> container.extension("json", "**/*.json5"));
		});
	}

}
