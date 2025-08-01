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

package org.springframework.boot.context.config;

import org.jspecify.annotations.Nullable;

/**
 * Abstract base class for configuration data exceptions.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public abstract class ConfigDataException extends RuntimeException {

	/**
	 * Create a new {@link ConfigDataException} instance.
	 * @param message the exception message
	 * @param cause the exception cause
	 */
	protected ConfigDataException(String message, @Nullable Throwable cause) {
		super(message, cause);
	}

}
