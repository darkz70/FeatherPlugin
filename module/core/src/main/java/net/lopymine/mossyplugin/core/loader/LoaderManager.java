package net.darkz.feather.core.loader;

import net.darkz.feather.core.data.MossyProjectConfigurationData;
import net.darkz.feather.core.extension.MossyCoreDependenciesExtension;
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

	void applyPlugins(@NotNull MossyProjectConfigurationData data);

	void applyDependencies(@NotNull MossyProjectConfigurationData data, MossyCoreDependenciesExtension extension);

	void configureExtensions(@NotNull MossyProjectConfigurationData data);

	boolean excludeUselessFiles(FileCopyDetails details);

	String getModDependenciesImplementationMethod(MossyProjectConfigurationData data);

	String getJarTaskName(MossyProjectConfigurationData data);

	String getAWFileExtension(MossyProjectConfigurationData data);
}
