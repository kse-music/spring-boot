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

package org.springframework.boot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Class used by {@link SpringApplication} to print the application banner.
 *
 * @author Phillip Webb
 */
class SpringApplicationBannerPrinter {

	static final String BANNER_LOCATION_PROPERTY = "spring.banner.location";

	static final String DEFAULT_BANNER_LOCATION = "banner.txt";

	private static final Banner DEFAULT_BANNER = new SpringBootBanner();

	private final ResourceLoader resourceLoader;

	private final @Nullable Banner fallbackBanner;

	SpringApplicationBannerPrinter(ResourceLoader resourceLoader, @Nullable Banner fallbackBanner) {
		this.resourceLoader = resourceLoader;
		this.fallbackBanner = fallbackBanner;
	}

	Banner print(Environment environment, @Nullable Class<?> sourceClass, Log logger) {
		Banner banner = getBanner(environment);
		try {
			logger.info(createStringFromBanner(banner, environment, sourceClass));
		}
		catch (UnsupportedEncodingException ex) {
			logger.warn("Failed to create String for banner", ex);
		}
		return new PrintedBanner(banner, sourceClass);
	}

	Banner print(Environment environment, @Nullable Class<?> sourceClass, PrintStream out) {
		Banner banner = getBanner(environment);
		banner.printBanner(environment, sourceClass, out);
		return new PrintedBanner(banner, sourceClass);
	}

	private Banner getBanner(Environment environment) {
		Banner textBanner = getTextBanner(environment);
		if (textBanner != null) {
			return textBanner;
		}
		if (this.fallbackBanner != null) {
			return this.fallbackBanner;
		}
		return DEFAULT_BANNER;
	}

	private @Nullable Banner getTextBanner(Environment environment) {
		String location = environment.getProperty(BANNER_LOCATION_PROPERTY, DEFAULT_BANNER_LOCATION);
		Resource resource = this.resourceLoader.getResource(location);
		try {
			if (resource.exists() && !resource.getURL().toExternalForm().contains("liquibase-core")) {
				return new ResourceBanner(resource);
			}
		}
		catch (IOException ex) {
			// Ignore
		}
		return null;
	}

	private String createStringFromBanner(Banner banner, Environment environment,
			@Nullable Class<?> mainApplicationClass) throws UnsupportedEncodingException {
		String charset = environment.getProperty("spring.banner.charset", StandardCharsets.UTF_8.name());
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (PrintStream out = new PrintStream(byteArrayOutputStream, false, charset)) {
			banner.printBanner(environment, mainApplicationClass, out);
		}
		return byteArrayOutputStream.toString(charset);
	}

	/**
	 * Decorator that allows a {@link Banner} to be printed again without needing to
	 * specify the source class.
	 */
	private static class PrintedBanner implements Banner {

		private final Banner banner;

		private final @Nullable Class<?> sourceClass;

		PrintedBanner(Banner banner, @Nullable Class<?> sourceClass) {
			this.banner = banner;
			this.sourceClass = sourceClass;
		}

		@Override
		public void printBanner(Environment environment, @Nullable Class<?> sourceClass, PrintStream out) {
			sourceClass = (sourceClass != null) ? sourceClass : this.sourceClass;
			this.banner.printBanner(environment, sourceClass, out);
		}

	}

	static class SpringApplicationBannerPrinterRuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
			hints.resources().registerPattern(DEFAULT_BANNER_LOCATION);
		}

	}

}
