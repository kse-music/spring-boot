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

package org.springframework.boot.r2dbc.autoconfigure.observation;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for R2DBC observability.
 *
 * @author Moritz Halbritter
 * @since 4.0.0
 */
@ConfigurationProperties("management.observations.r2dbc")
public class R2dbcObservationProperties {

	/**
	 * Whether to tag actual query parameter values.
	 */
	private boolean includeParameterValues;

	public boolean isIncludeParameterValues() {
		return this.includeParameterValues;
	}

	public void setIncludeParameterValues(boolean includeParameterValues) {
		this.includeParameterValues = includeParameterValues;
	}

}
