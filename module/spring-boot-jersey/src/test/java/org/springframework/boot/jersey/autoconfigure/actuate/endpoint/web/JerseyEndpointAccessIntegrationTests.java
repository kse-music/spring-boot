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

package org.springframework.boot.jersey.autoconfigure.actuate.endpoint.web;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jersey.autoconfigure.JerseyAutoConfiguration;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.tomcat.autoconfigure.servlet.TomcatServletWebServerAutoConfiguration;
import org.springframework.boot.web.server.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for controlling access to endpoints exposed by Jersey.
 *
 * @author Andy Wilkinson
 */
class JerseyEndpointAccessIntegrationTests {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner(
			AnnotationConfigServletWebServerApplicationContext::new)
		.withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class, JerseyAutoConfiguration.class,
				EndpointAutoConfiguration.class, TomcatServletWebServerAutoConfiguration.class,
				WebEndpointAutoConfiguration.class, ManagementContextAutoConfiguration.class,
				BeansEndpointAutoConfiguration.class))
		.withUserConfiguration(CustomServletEndpoint.class)
		.withPropertyValues("server.port:0");

	@Test
	void accessIsUnrestrictedByDefault() {
		this.contextRunner.withPropertyValues("management.endpoints.web.exposure.include=*").run((context) -> {
			WebTestClient client = createClient(context);
			assertThat(isAccessible(client, HttpMethod.GET, "beans")).isTrue();
			assertThat(isAccessible(client, HttpMethod.GET, "customservlet")).isTrue();
			assertThat(isAccessible(client, HttpMethod.POST, "customservlet")).isTrue();
		});
	}

	@Test
	void accessCanBeReadOnlyByDefault() {
		this.contextRunner
			.withPropertyValues("management.endpoints.web.exposure.include=*",
					"management.endpoints.access.default=READ_ONLY")
			.run((context) -> {
				WebTestClient client = createClient(context);
				assertThat(isAccessible(client, HttpMethod.GET, "beans")).isTrue();
				assertThat(isAccessible(client, HttpMethod.GET, "customservlet")).isTrue();
				assertThat(isAccessible(client, HttpMethod.POST, "customservlet")).isFalse();
			});
	}

	@Test
	void accessCanBeNoneByDefault() {
		this.contextRunner
			.withPropertyValues("management.endpoints.web.exposure.include=*",
					"management.endpoints.access.default=NONE")
			.run((context) -> {
				WebTestClient client = createClient(context);
				assertThat(isAccessible(client, HttpMethod.GET, "beans")).isFalse();
				assertThat(isAccessible(client, HttpMethod.GET, "customservlet")).isFalse();
				assertThat(isAccessible(client, HttpMethod.POST, "customservlet")).isFalse();
			});
	}

	@Test
	void accessForOneEndpointCanOverrideTheDefaultAccess() {
		this.contextRunner
			.withPropertyValues("management.endpoints.web.exposure.include=*",
					"management.endpoints.access.default=NONE", "management.endpoint.customservlet.access=READ_ONLY")
			.run((context) -> {
				WebTestClient client = createClient(context);
				assertThat(isAccessible(client, HttpMethod.GET, "beans")).isFalse();
				assertThat(isAccessible(client, HttpMethod.GET, "customservlet")).isTrue();
				assertThat(isAccessible(client, HttpMethod.POST, "customservlet")).isFalse();
			});
	}

	@Test
	void accessCanBeCappedAtReadOnly() {
		this.contextRunner
			.withPropertyValues("management.endpoints.web.exposure.include=*",
					"management.endpoints.access.default=UNRESTRICTED",
					"management.endpoints.access.max-permitted=READ_ONLY")
			.run((context) -> {
				WebTestClient client = createClient(context);
				assertThat(isAccessible(client, HttpMethod.GET, "beans")).isTrue();
				assertThat(isAccessible(client, HttpMethod.GET, "customservlet")).isTrue();
				assertThat(isAccessible(client, HttpMethod.POST, "customservlet")).isFalse();
			});
	}

	@Test
	void accessCanBeCappedAtNone() {
		this.contextRunner.withPropertyValues("management.endpoints.web.exposure.include=*",
				"management.endpoints.access.default=UNRESTRICTED", "management.endpoints.access.max-permitted=NONE")
			.run((context) -> {
				WebTestClient client = createClient(context);
				assertThat(isAccessible(client, HttpMethod.GET, "beans")).isFalse();
				assertThat(isAccessible(client, HttpMethod.GET, "customservlet")).isFalse();
				assertThat(isAccessible(client, HttpMethod.POST, "customservlet")).isFalse();
			});
	}

	private WebTestClient createClient(AssertableWebApplicationContext context) {
		int port = context.getSourceApplicationContext(ServletWebServerApplicationContext.class)
			.getWebServer()
			.getPort();
		ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
			.codecs((configurer) -> configurer.defaultCodecs().maxInMemorySize(-1))
			.build();
		return WebTestClient.bindToServer()
			.baseUrl("http://localhost:" + port)
			.exchangeStrategies(exchangeStrategies)
			.responseTimeout(Duration.ofMinutes(5))
			.build();
	}

	private boolean isAccessible(WebTestClient client, HttpMethod method, String path) {
		path = "/actuator/" + path;
		EntityExchangeResult<byte[]> result = client.method(method).uri(path).exchange().expectBody().returnResult();
		if (result.getStatus() == HttpStatus.OK) {
			return true;
		}
		if (result.getStatus() == HttpStatus.NOT_FOUND || result.getStatus() == HttpStatus.METHOD_NOT_ALLOWED) {
			return false;
		}
		throw new IllegalStateException(
				String.format("Unexpected %s HTTP status for endpoint %s", result.getStatus(), path));
	}

	@org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpoint(id = "customservlet")
	@SuppressWarnings({ "deprecation", "removal" })
	static class CustomServletEndpoint
			implements Supplier<org.springframework.boot.actuate.endpoint.web.EndpointServlet> {

		@Override
		public org.springframework.boot.actuate.endpoint.web.EndpointServlet get() {
			return new org.springframework.boot.actuate.endpoint.web.EndpointServlet(new HttpServlet() {

				@Override
				protected void doGet(HttpServletRequest req, HttpServletResponse resp)
						throws ServletException, IOException {
				}

				@Override
				protected void doPost(HttpServletRequest req, HttpServletResponse resp)
						throws ServletException, IOException {
				}

			});
		}

	}

}
