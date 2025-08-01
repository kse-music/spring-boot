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

package org.springframework.boot.build.starters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import org.springframework.core.CollectionFactory;

/**
 * A {@link Task} for generating metadata that describes a starter.
 *
 * @author Andy Wilkinson
 */
public abstract class StarterMetadata extends DefaultTask {

	private Configuration dependencies;

	public StarterMetadata() {
		Project project = getProject();
		getStarterName().convention(project.provider(project::getName));
		getStarterDescription().convention(project.provider(project::getDescription));
	}

	@Input
	public abstract Property<String> getStarterName();

	@Input
	public abstract Property<String> getStarterDescription();

	@Classpath
	public FileCollection getDependencies() {
		return this.dependencies;
	}

	public void setDependencies(Configuration dependencies) {
		this.dependencies = dependencies;
	}

	@OutputFile
	public abstract RegularFileProperty getDestination();

	@TaskAction
	void generateMetadata() throws IOException {
		Properties properties = CollectionFactory.createSortedProperties(true);
		properties.setProperty("name", getStarterName().get());
		properties.setProperty("description", getStarterDescription().get());
		properties.setProperty("dependencies",
				String.join(",",
						this.dependencies.getResolvedConfiguration()
							.getResolvedArtifacts()
							.stream()
							.map(ResolvedArtifact::getName)
							.collect(Collectors.toSet())));
		File destination = getDestination().getAsFile().get();
		destination.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(destination)) {
			properties.store(writer, null);
		}
	}

}
