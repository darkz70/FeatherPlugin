package net.darkz70.featherplugin.core.extension;

import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.tasks.*;

@Getter
public class FeatherCoreDependenciesExtension {

	@Input
	String minecraft;

	@Input
	String fabricApi;

	@Input
	String fabricLoader;

	@Input
	String neoForge;

	@Input
	String forge;

	@Input
	String parchment;

	@Input
	String lombok;

	@Input
	String disableMixinAp;

	@Nested
	FeatherCoreAdditionalDependencies additional = new FeatherCoreAdditionalDependencies();

	public void additional(Action<FeatherCoreAdditionalDependencies> action) {
		action.execute(this.additional);
	}
}
