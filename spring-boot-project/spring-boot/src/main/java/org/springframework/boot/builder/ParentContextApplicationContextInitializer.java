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

package org.springframework.boot.builder;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

/**
 * {@link ApplicationContextInitializer} for setting the parent context. Also publishes
 * {@link ParentContextAvailableEvent} when the context is refreshed to signal to other
 * listeners that the context is available and has a parent.
 *
 * @author Dave Syer
 * @since 1.0.0
 */
public class ParentContextApplicationContextInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

	private int order = Ordered.HIGHEST_PRECEDENCE;

	private final ApplicationContext parent;

	public ParentContextApplicationContextInitializer(ApplicationContext parent) {
		this.parent = parent;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		if (applicationContext != this.parent) {
			applicationContext.setParent(this.parent);
			applicationContext.addApplicationListener(EventPublisher.INSTANCE);
		}
	}

	private static final class EventPublisher implements ApplicationListener<ContextRefreshedEvent>, Ordered {

		private static final EventPublisher INSTANCE = new EventPublisher();

		@Override
		public int getOrder() {
			return Ordered.HIGHEST_PRECEDENCE;
		}

		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			ApplicationContext context = event.getApplicationContext();
			if (context instanceof ConfigurableApplicationContext configurableApplicationContext
					&& context == event.getSource()) {
				context.publishEvent(new ParentContextAvailableEvent(configurableApplicationContext));
			}
		}

	}

	/**
	 * {@link ApplicationEvent} fired when a parent context is available.
	 */
	public static class ParentContextAvailableEvent extends ApplicationEvent {

		public ParentContextAvailableEvent(ConfigurableApplicationContext applicationContext) {
			super(applicationContext);
		}

		public ConfigurableApplicationContext getApplicationContext() {
			return (ConfigurableApplicationContext) getSource();
		}

	}

}
