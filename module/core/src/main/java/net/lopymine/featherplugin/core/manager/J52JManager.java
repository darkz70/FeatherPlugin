package net.lopymine.featherplugin.core.manager;

import dev.kikugie.fletching_table.extension.FletchingTableExtension;
import lombok.experimental.ExtensionMethod;
import net.lopymine.featherplugin.core.FeatherPluginCore;
import net.lopymine.featherplugin.core.data.FeatherProjectConfigurationData;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

@ExtensionMethod(FeatherPluginCore.class)
public class J52JManager {

	public static void apply(@NotNull FeatherProjectConfigurationData data) {
		Project project = data.project();
		project.getExtensions().configure(FletchingTableExtension.class, (extension) -> {
			var main = extension.getJ52j().register("main");
			main.configure((container) -> container.extension("json", "**/*.json5"));
		});
	}

}
