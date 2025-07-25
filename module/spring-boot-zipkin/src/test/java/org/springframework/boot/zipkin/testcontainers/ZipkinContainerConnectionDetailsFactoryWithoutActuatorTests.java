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

package org.springframework.boot.zipkin.testcontainers;

import org.junit.jupiter.api.Test;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactoryHints;
import org.springframework.boot.testsupport.classpath.ClassPathExclusions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ZipkinContainerConnectionDetailsFactory}.
 *
 * @author Moritz Halbritter
 */
@ClassPathExclusions("spring-boot-actuator-*")
class ZipkinContainerConnectionDetailsFactoryWithoutActuatorTests {

	@Test
	void shouldRegisterHints() {
		RuntimeHints hints = ContainerConnectionDetailsFactoryHints.getRegisteredHints(getClass().getClassLoader());
		assertThat(hints).isNotNull();
	}

}
