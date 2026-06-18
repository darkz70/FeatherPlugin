package net.darkz70.featherplugin.settings.manager;

import dev.kikugie.stonecutter.settings.StonecutterSettingsExtension;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import net.darkz70.featherplugin.settings.FeatherPluginSettings;
import net.darkz70.featherplugin.settings.project.FeatherProject;
import org.gradle.api.initialization.Settings;
import org.jetbrains.annotations.*;

public class AccessWidenerManager {

	public static void apply(@NotNull Settings settings, List<FeatherProject> projects) {
		StonecutterSettingsExtension stonecutter = settings.getExtensions().getByType(StonecutterSettingsExtension.class);
		Path path = settings.getRootDir().toPath();
		for (FeatherProject project : projects) {
			createExampleAccessWidener(path, project, stonecutter);
		}
	}

	public static void createExampleAccessWidener(Path rootProject, FeatherProject project, StonecutterSettingsExtension stonecutter) {
		File awFile = createAWFile(rootProject, project, stonecutter);
		if (awFile == null) {
			return;
		}

		try (FileWriter writer = new FileWriter(awFile)) {
			project.loaderManager().fillAWWillExampleText(writer, project.comparableMinecraftVersion(), stonecutter);
		} catch (Exception e) {
			FeatherPluginSettings.LOGGER.log("Failed to create AW for \"%s\"!".formatted(project.projectName()));
			e.printStackTrace(System.out);
			return;
		}

		FeatherPluginSettings.LOGGER.log("Successfully created AW for " + project.projectName());
	}

	@Nullable
	private static File createAWFile(Path rootProject, FeatherProject project, StonecutterSettingsExtension stonecutter) {
		String projectName = project.projectName();

		Path awsFolder = rootProject.resolve("src/main/resources/aws/");
		File awsFolderFile = awsFolder.toFile();
		if (!awsFolderFile.exists() && !awsFolderFile.mkdirs()) {
			FeatherPluginSettings.LOGGER.log("Failed to get or create AW folder for " + projectName);
			return null;
		}

		File versionedAWFile = awsFolder.resolve("%s.%s".formatted(projectName, project.loaderManager().getAWExtension(project.comparableMinecraftVersion(), stonecutter))).toFile();
		if (versionedAWFile.exists()) {
			return null;
		}

		try {
			if (!versionedAWFile.createNewFile()) {
				FeatherPluginSettings.LOGGER.log("[1] Failed to create AW file for " + projectName);
				return null;
			}
		} catch (Exception e) {
			FeatherPluginSettings.LOGGER.log("[2] Failed to create AW file for " + projectName);
			e.printStackTrace(System.out);
			return null;
		}

		return versionedAWFile;
	}

}
