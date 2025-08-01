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

package org.springframework.boot.actuate.endpoint.invoke.convert;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.endpoint.invoke.OperationParameter;
import org.springframework.boot.actuate.endpoint.invoke.ParameterMappingException;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;

/**
 * {@link ParameterValueMapper} backed by a {@link ConversionService}.
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 2.0.0
 */
public class ConversionServiceParameterValueMapper implements ParameterValueMapper {

	private final ConversionService conversionService;

	/**
	 * Create a new {@link ConversionServiceParameterValueMapper} instance.
	 */
	public ConversionServiceParameterValueMapper() {
		this(ApplicationConversionService.getSharedInstance());
	}

	/**
	 * Create a new {@link ConversionServiceParameterValueMapper} instance backed by a
	 * specific conversion service.
	 * @param conversionService the conversion service
	 */
	public ConversionServiceParameterValueMapper(ConversionService conversionService) {
		Assert.notNull(conversionService, "'conversionService' must not be null");
		this.conversionService = conversionService;
	}

	@Override
	public @Nullable Object mapParameterValue(OperationParameter parameter, @Nullable Object value)
			throws ParameterMappingException {
		try {
			return this.conversionService.convert(value, parameter.getType());
		}
		catch (Exception ex) {
			throw new ParameterMappingException(parameter, value, ex);
		}
	}

}
