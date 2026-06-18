package net.darkz70.featherplugin.core.loader;

import java.util.*;
import net.darkz70.featherplugin.core.data.FeatherProjectConfigurationData;
import net.darkz70.featherplugin.core.extension.FeatherCoreDependenciesExtension;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCopyDetails;
import org.jetbrains.annotations.NotNull;

public interface LoaderManager {

	static LoaderManager of(String loader) {
		if (loader.equals("forge")) {
			return ForgeLoaderManager.getInstance();
		} else if (loader.contains("neoforge")) {
			return NeoForgeLoaderManager.getInstance();
		} else if (loader.contains("fabric")) {
			return FabricLoaderManager.getInstance();
		} else {
			throw new RuntimeException("Unsupported loader \"%s\"!".formatted(loader));
		}
	}

	void applyPlugins(@NotNull FeatherProjectConfigurationData data);

	void applyDependencies(@NotNull FeatherProjectConfigurationData data, FeatherCoreDependenciesExtension extension);

	void configureExtensions(@NotNull FeatherProjectConfigurationData data);

	boolean excludeUselessFiles(FileCopyDetails details);

	String getModDependenciesImplementationMethod(FeatherProjectConfigurationData data);

	String getJarTaskName(FeatherProjectConfigurationData data);

	String getAWFileExtension(FeatherProjectConfigurationData data);

	Map<String, String> getLoaderConfigurations(List<String> configurations, FeatherProjectConfigurationData data);

	default Configuration registerCustomConfiguration(@NotNull FeatherProjectConfigurationData data, String name, String originalName, String loaderName) {
		return data.project().getConfigurations().create(name);
	}
}
