package net.darkz70.featherplugin.settings.manager;

import dev.kikugie.stonecutter.settings.StonecutterSettingsExtension;
import java.util.*;
import net.darkz70.featherplugin.common.FeatherUtils;
import org.gradle.api.initialization.Settings;
import org.jetbrains.annotations.NotNull;

public class StonecutterManager {

	public static void apply(@NotNull Settings settings, Map<String, List<String>> projects) {
		settings.getPluginManagement().repositories(repos -> {
			repos.maven(maven -> {
				maven.setName("FeatherPluginGitHubPackages");
				maven.setUrl("https://maven.pkg.github.com/darkz70/FeatherPlugin");
				maven.credentials(creds -> {
					String user = System.getenv("GPR_USER");
					String key = System.getenv("GPR_KEY");
					if (user == null) user = System.getenv("GITHUB_ACTOR");
					if (key == null) key = System.getenv("GITHUB_TOKEN");
					creds.setUsername(user);
					creds.setPassword(key);
				});
			});
			repos.maven(maven -> {
				maven.setName("KikuReleases");
				maven.setUrl("https://maven.kikugie.dev/releases");
			});
			repos.maven(maven -> {
				maven.setName("KikuSnapshots");
				maven.setUrl("https://maven.kikugie.dev/snapshots");
			});
			repos.gradlePluginPortal();
		});

		settings.getPlugins().apply("dev.kikugie.stonecutter");
		StonecutterSettingsExtension stonecutter = settings.getExtensions().getByType(StonecutterSettingsExtension.class);
		stonecutter.create(settings.getRootProject(), (builder) -> {
			String propertyLoader = settings.getProviders().gradleProperty("ci_loader").getOrNull();
			projects.forEach((loader, versions) -> {
				if (propertyLoader != null && !propertyLoader.equals(loader)) {
					return;
				}
				String last = versions.get(versions.size() - 1);
				for (String version : versions) {
					String ver = "%s-%s".formatted(loader, version);
					builder.version(ver, FeatherUtils.substringBefore(version, "-"));
					if (version.equals(last)) {
						builder.getVcsVersion().set(ver);
					}
				}
			});
		});
	}

}
