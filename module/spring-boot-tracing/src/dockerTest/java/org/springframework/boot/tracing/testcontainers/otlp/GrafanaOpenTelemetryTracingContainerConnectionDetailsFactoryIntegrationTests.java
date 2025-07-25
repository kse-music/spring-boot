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

package org.springframework.boot.tracing.testcontainers.otlp;

import org.junit.jupiter.api.Test;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.testsupport.container.TestImage;
import org.springframework.boot.tracing.autoconfigure.otlp.OtlpTracingAutoConfiguration;
import org.springframework.boot.tracing.autoconfigure.otlp.OtlpTracingConnectionDetails;
import org.springframework.boot.tracing.autoconfigure.otlp.Transport;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GrafanaOpenTelemetryTracingContainerConnectionDetailsFactory}.
 *
 * @author Eddú Meléndez
 */
@SpringJUnitConfig
@Testcontainers(disabledWithoutDocker = true)
class GrafanaOpenTelemetryTracingContainerConnectionDetailsFactoryIntegrationTests {

	@Container
	@ServiceConnection
	static final LgtmStackContainer container = TestImage.container(LgtmStackContainer.class);

	@Autowired
	private OtlpTracingConnectionDetails connectionDetails;

	@Test
	void connectionCanBeMadeToOpenTelemetryContainer() {
		assertThat(this.connectionDetails.getUrl(Transport.HTTP))
			.isEqualTo("%s/v1/traces".formatted(container.getOtlpHttpUrl()));
		assertThat(this.connectionDetails.getUrl(Transport.GRPC))
			.isEqualTo("%s/v1/traces".formatted(container.getOtlpGrpcUrl()));
	}

	@Configuration(proxyBeanMethods = false)
	@ImportAutoConfiguration(OtlpTracingAutoConfiguration.class)
	static class TestConfiguration {

	}

}
