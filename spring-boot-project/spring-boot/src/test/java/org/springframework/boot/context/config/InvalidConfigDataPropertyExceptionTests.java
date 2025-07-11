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

import org.junit.jupiter.api.Test;

import org.springframework.boot.context.config.ConfigDataEnvironmentContributor.Kind;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.origin.MockOrigin;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.mock.env.MockPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests for {@link InvalidConfigDataPropertyException}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
class InvalidConfigDataPropertyExceptionTests {

	private final ConfigDataResource resource = new TestConfigDataResource();

	private final ConfigurationPropertyName replacement = ConfigurationPropertyName.of("replacement");

	private final ConfigurationPropertyName invalid = ConfigurationPropertyName.of("invalid");

	private final ConfigurationProperty property = new ConfigurationProperty(this.invalid, "bad",
			MockOrigin.of("origin"));

	private final ConversionService conversionService = DefaultConversionService.getSharedInstance();

	@Test
	void createHasCorrectMessage() {
		assertThat(new InvalidConfigDataPropertyException(this.property, false, this.replacement, this.resource))
			.hasMessage(
					"Property 'invalid' imported from location 'test' is invalid and should be replaced with 'replacement' [origin: origin]");
	}

	@Test
	void createWhenNoLocationHasCorrectMessage() {
		assertThat(new InvalidConfigDataPropertyException(this.property, false, this.replacement, null))
			.hasMessage("Property 'invalid' is invalid and should be replaced with 'replacement' [origin: origin]");
	}

	@Test
	void createWhenNoReplacementHasCorrectMessage() {
		assertThat(new InvalidConfigDataPropertyException(this.property, false, null, this.resource))
			.hasMessage("Property 'invalid' imported from location 'test' is invalid [origin: origin]");
	}

	@Test
	void createWhenNoOriginHasCorrectMessage() {
		ConfigurationProperty property = new ConfigurationProperty(this.invalid, "bad", null);
		assertThat(new InvalidConfigDataPropertyException(property, false, this.replacement, this.resource)).hasMessage(
				"Property 'invalid' imported from location 'test' is invalid and should be replaced with 'replacement'");
	}

	@Test
	void createWhenProfileSpecificHasCorrectMessage() {
		ConfigurationProperty property = new ConfigurationProperty(this.invalid, "bad", null);
		assertThat(new InvalidConfigDataPropertyException(property, true, null, this.resource))
			.hasMessage("Property 'invalid' imported from location 'test' is invalid in a profile specific resource");
	}

	@Test
	void getPropertyReturnsProperty() {
		InvalidConfigDataPropertyException exception = new InvalidConfigDataPropertyException(this.property, false,
				this.replacement, this.resource);
		assertThat(exception.getProperty()).isEqualTo(this.property);
	}

	@Test
	void getLocationReturnsLocation() {
		InvalidConfigDataPropertyException exception = new InvalidConfigDataPropertyException(this.property, false,
				this.replacement, this.resource);
		assertThat(exception.getLocation()).isEqualTo(this.resource);
	}

	@Test
	void getReplacementReturnsReplacement() {
		InvalidConfigDataPropertyException exception = new InvalidConfigDataPropertyException(this.property, false,
				this.replacement, this.resource);
		assertThat(exception.getReplacement()).isEqualTo(this.replacement);
	}

	@Test
	void throwOrWarnWhenHasInvalidPropertyThrowsException() {
		MockPropertySource propertySource = new MockPropertySource();
		propertySource.setProperty("spring.profiles", "a");
		ConfigDataEnvironmentContributor contributor = ConfigDataEnvironmentContributor.ofExisting(propertySource,
				this.conversionService);
		assertThatExceptionOfType(InvalidConfigDataPropertyException.class)
			.isThrownBy(() -> InvalidConfigDataPropertyException.throwIfPropertyFound(contributor))
			.withMessageStartingWith("Property 'spring.profiles' is invalid and should be replaced with "
					+ "'spring.config.activate.on-profile'");
	}

	@Test
	void throwOrWarnWhenWhenHasInvalidProfileSpecificPropertyThrowsException() {
		throwOrWarnWhenWhenHasInvalidProfileSpecificPropertyThrowsException("spring.profiles.include");
		throwOrWarnWhenWhenHasInvalidProfileSpecificPropertyThrowsException("spring.profiles.active");
		throwOrWarnWhenWhenHasInvalidProfileSpecificPropertyThrowsException("spring.profiles.default");
	}

	@Test
	void throwOrWarnWhenWhenHasInvalidProfileSpecificPropertyOnIgnoringProfilesContributorDoesNotThrowException() {
		ConfigDataEnvironmentContributor contributor = createInvalidProfileSpecificPropertyContributor(
				"spring.profiles.active", ConfigData.Option.IGNORE_PROFILES);
		assertThatNoException().isThrownBy(() -> InvalidConfigDataPropertyException.throwIfPropertyFound(contributor));
	}

	private void throwOrWarnWhenWhenHasInvalidProfileSpecificPropertyThrowsException(String name) {
		ConfigDataEnvironmentContributor contributor = createInvalidProfileSpecificPropertyContributor(name);
		assertThatExceptionOfType(InvalidConfigDataPropertyException.class)
			.isThrownBy(() -> InvalidConfigDataPropertyException.throwIfPropertyFound(contributor))
			.withMessageStartingWith("Property '" + name + "' is invalid in a profile specific resource");
	}

	private ConfigDataEnvironmentContributor createInvalidProfileSpecificPropertyContributor(String name,
			ConfigData.Option... configDataOptions) {
		MockPropertySource propertySource = new MockPropertySource();
		propertySource.setProperty(name, "a");
		return new ConfigDataEnvironmentContributor(Kind.BOUND_IMPORT, null, null, true, propertySource,
				ConfigurationPropertySource.from(propertySource), null, ConfigData.Options.of(configDataOptions), null,
				this.conversionService);
	}

	@Test
	void throwOrWarnWhenHasNoInvalidPropertyDoesNothing() {
		ConfigDataEnvironmentContributor contributor = ConfigDataEnvironmentContributor
			.ofExisting(new MockPropertySource(), this.conversionService);
		InvalidConfigDataPropertyException.throwIfPropertyFound(contributor);
	}

	private static final class TestConfigDataResource extends ConfigDataResource {

		@Override
		public String toString() {
			return "test";
		}

	}

}
