/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.buildpack.platform.build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.buildpack.platform.docker.type.Binding;
import org.springframework.boot.buildpack.platform.docker.type.ContainerConfig;
import org.springframework.boot.buildpack.platform.docker.type.ImageReference;
import org.springframework.util.StringUtils;

/**
 * An individual build phase executed as part of a {@link Lifecycle} run.
 *
 * @author Phillip Webb
 * @author Scott Frederick
 * @author Jeroen Meijer
 */
class Phase {

	private final String name;

	private boolean daemonAccess;

	private final List<String> args = new ArrayList<>();

	private final List<Binding> bindings = new ArrayList<>();

	private final Map<String, String> env = new LinkedHashMap<>();

	private final List<String> securityOptions = new ArrayList<>();

	private String networkMode;

	private boolean requiresApp;

	/**
	 * Create a new {@link Phase} instance.
	 * @param name the name of the phase
	 * @param verboseLogging if verbose logging is requested
	 */
	Phase(String name, boolean verboseLogging) {
		this.name = name;
		withLogLevelArg(verboseLogging);
	}

	void withApp(String path, Binding binding) {
		withArgs("-app", path);
		withBinding(binding);
		this.requiresApp = true;
	}

	void withBuildCache(String path, Binding binding) {
		withArgs("-cache-dir", path);
		withBinding(binding);
	}

	/**
	 * Update this phase with Docker daemon access.
	 */
	void withDaemonAccess() {
		this.withArgs("-daemon");
		this.daemonAccess = true;
	}

	void withImageName(ImageReference imageName) {
		withArgs(imageName);
	}

	void withLaunchCache(String path, Binding binding) {
		withArgs("-launch-cache", path);
		withBinding(binding);
	}

	void withLayers(String path, Binding binding) {
		withArgs("-layers", path);
		withBinding(binding);
	}

	void withPlatform(String path) {
		withArgs("-platform", path);
	}

	void withProcessType(String type) {
		withArgs("-process-type", type);
	}

	void withRunImage(ImageReference runImage) {
		withArgs("-run-image", runImage);
	}

	void withSkipRestore() {
		withArgs("-skip-restore");
	}

	/**
	 * Update this phase with a debug log level arguments if verbose logging has been
	 * requested.
	 * @param verboseLogging if verbose logging is requested
	 */
	private void withLogLevelArg(boolean verboseLogging) {
		if (verboseLogging) {
			this.args.add("-log-level");
			this.args.add("debug");
		}
	}

	/**
	 * Update this phase with additional run arguments.
	 * @param args the arguments to add
	 */
	void withArgs(Object... args) {
		Arrays.stream(args).map(Object::toString).forEach(this.args::add);
	}

	/**
	 * Update this phase with an addition volume binding.
	 * @param binding the binding
	 */
	void withBinding(Binding binding) {
		this.bindings.add(binding);
	}

	/**
	 * Update this phase with an additional environment variable.
	 * @param name the variable name
	 * @param value the variable value
	 */
	void withEnv(String name, String value) {
		this.env.put(name, value);
	}

	/**
	 * Update this phase with the network the build container will connect to.
	 * @param networkMode the network
	 */
	void withNetworkMode(String networkMode) {
		this.networkMode = networkMode;
	}

	/**
	 * Update this phase with a security option.
	 * @param option the security option
	 */
	void withSecurityOption(String option) {
		this.securityOptions.add(option);
	}

	/**
	 * Return the name of the phase.
	 * @return the phase name
	 */
	String getName() {
		return this.name;
	}

	boolean requiresApp() {
		return this.requiresApp;
	}

	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * Apply this phase settings to a {@link ContainerConfig} update.
	 * @param update the update to apply the phase to
	 */
	void apply(ContainerConfig.Update update) {
		if (this.daemonAccess) {
			update.withUser("root");
		}
		update.withCommand("/cnb/lifecycle/" + this.name, StringUtils.toStringArray(this.args));
		update.withLabel("author", "spring-boot");
		this.bindings.forEach(update::withBinding);
		this.env.forEach(update::withEnv);
		if (this.networkMode != null) {
			update.withNetworkMode(this.networkMode);
		}
		this.securityOptions.forEach(update::withSecurityOption);
	}

}
