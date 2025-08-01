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

package org.springframework.boot.diagnostics;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.io.support.SpringFactoriesLoader.ArgumentResolver;
import org.springframework.core.io.support.SpringFactoriesLoader.FailureHandler;
import org.springframework.core.log.LogMessage;

/**
 * Utility to trigger {@link FailureAnalyzer} and {@link FailureAnalysisReporter}
 * instances loaded from {@code spring.factories}.
 * <p>
 * A {@code FailureAnalyzer} that requires access to the {@link BeanFactory} or
 * {@link Environment} in order to perform its analysis can implement a constructor that
 * accepts arguments of one or both of these types.
 *
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Scott Frederick
 */
final class FailureAnalyzers implements SpringBootExceptionReporter {

	private static final Log logger = LogFactory.getLog(FailureAnalyzers.class);

	private final SpringFactoriesLoader springFactoriesLoader;

	private final List<FailureAnalyzer> analyzers;

	public FailureAnalyzers(@Nullable ConfigurableApplicationContext context) {
		this(context,
				SpringFactoriesLoader.forDefaultResourceLocation((context != null) ? context.getClassLoader() : null));
	}

	FailureAnalyzers(@Nullable ConfigurableApplicationContext context, SpringFactoriesLoader springFactoriesLoader) {
		this.springFactoriesLoader = springFactoriesLoader;
		this.analyzers = loadFailureAnalyzers(context, this.springFactoriesLoader);
	}

	private static List<FailureAnalyzer> loadFailureAnalyzers(@Nullable ConfigurableApplicationContext context,
			SpringFactoriesLoader springFactoriesLoader) {
		return springFactoriesLoader.load(FailureAnalyzer.class, getArgumentResolver(context),
				FailureHandler.logging(logger));
	}

	private static @Nullable ArgumentResolver getArgumentResolver(@Nullable ConfigurableApplicationContext context) {
		if (context == null) {
			return null;
		}
		ArgumentResolver argumentResolver = ArgumentResolver.of(BeanFactory.class, context.getBeanFactory());
		argumentResolver = argumentResolver.and(Environment.class, context.getEnvironment());
		return argumentResolver;
	}

	@Override
	public boolean reportException(Throwable failure) {
		FailureAnalysis analysis = analyze(failure, this.analyzers);
		return report(analysis);
	}

	private @Nullable FailureAnalysis analyze(Throwable failure, List<FailureAnalyzer> analyzers) {
		for (FailureAnalyzer analyzer : analyzers) {
			try {
				FailureAnalysis analysis = analyzer.analyze(failure);
				if (analysis != null) {
					return analysis;
				}
			}
			catch (Throwable ex) {
				logger.trace(LogMessage.format("FailureAnalyzer %s failed", analyzer), ex);
			}
		}
		return null;
	}

	private boolean report(@Nullable FailureAnalysis analysis) {
		List<FailureAnalysisReporter> reporters = this.springFactoriesLoader.load(FailureAnalysisReporter.class);
		if (analysis == null || reporters.isEmpty()) {
			return false;
		}
		for (FailureAnalysisReporter reporter : reporters) {
			reporter.report(analysis);
		}
		return true;
	}

}
