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

package org.springframework.boot.jetty.servlet;

import jakarta.servlet.ServletException;
import org.eclipse.jetty.ee10.webapp.AbstractConfiguration;
import org.eclipse.jetty.ee10.webapp.Configuration;
import org.eclipse.jetty.ee10.webapp.WebAppContext;

import org.springframework.boot.web.server.servlet.ServletContextInitializers;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.util.Assert;

/**
 * Jetty {@link Configuration} that calls {@link ServletContextInitializer}s.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
class ServletContextInitializerConfiguration extends AbstractConfiguration {

	private final ServletContextInitializers initializers;

	/**
	 * Create a new {@link ServletContextInitializerConfiguration}.
	 * @param initializers the initializers that should be invoked
	 */
	ServletContextInitializerConfiguration(ServletContextInitializers initializers) {
		super(new AbstractConfiguration.Builder());
		Assert.notNull(initializers, "'initializers' must not be null");
		this.initializers = initializers;
	}

	@Override
	public void configure(WebAppContext context) throws Exception {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(context.getClassLoader());
		try {
			callInitializers(context);
		}
		finally {
			Thread.currentThread().setContextClassLoader(classLoader);
		}
	}

	private void callInitializers(WebAppContext context) throws ServletException {
		try {
			context.getContext().setExtendedListenerTypes(true);
			for (ServletContextInitializer initializer : this.initializers) {
				initializer.onStartup(context.getServletContext());
			}
		}
		finally {
			context.getContext().setExtendedListenerTypes(false);
		}
	}

}
