package net.darkz.feather.core.manager;

import lombok.experimental.ExtensionMethod;
import net.darkz.feather.core.FeatherPluginCore;
import org.gradle.api.*;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;

@ExtensionMethod(FeatherPluginCore.class)
public class JavaManager {

	public static void apply(@NotNull Project project, FeatherPluginCore mossyPlugin) {
		int javaVersionIndex = mossyPlugin.getJavaVersionIndex();
		JavaVersion javaVersion = mossyPlugin.getJavaVersion();

		TaskCollection<JavaCompile> collection = project.getTasks().withType(JavaCompile.class);
		for (JavaCompile javaCompile : collection) {
			javaCompile.getOptions().getRelease().set(javaVersionIndex);
		}

		JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
		javaExtension.setSourceCompatibility(javaVersion);
		javaExtension.setTargetCompatibility(javaVersion);
	}
}
