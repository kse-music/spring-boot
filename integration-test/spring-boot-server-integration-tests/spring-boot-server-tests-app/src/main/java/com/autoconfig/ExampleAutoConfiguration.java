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

package com.autoconfig;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWarDeployment;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

@AutoConfiguration
public final class ExampleAutoConfiguration {

	@Bean
	@ConditionalOnWarDeployment
	ServletRegistrationBean<TestServlet> onWarTestServlet() {
		ServletRegistrationBean<TestServlet> registration = new ServletRegistrationBean<>(new TestServlet());
		registration.addUrlMappings("/conditionalOnWar");
		return registration;
	}

	@Bean
	ServletRegistrationBean<TestServlet> testServlet() {
		ServletRegistrationBean<TestServlet> registration = new ServletRegistrationBean<>(new TestServlet());
		registration.addUrlMappings("/always");
		return registration;
	}

	static class TestServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
			resp.getWriter().println("{\"hello\":\"world\"}");
			resp.flushBuffer();
		}

	}

}
