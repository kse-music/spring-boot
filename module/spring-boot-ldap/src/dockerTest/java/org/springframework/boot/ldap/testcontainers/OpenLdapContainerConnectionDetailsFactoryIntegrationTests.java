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

package org.springframework.boot.ldap.testcontainers;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.ldap.autoconfigure.LdapAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.testsupport.container.OpenLdapContainer;
import org.springframework.boot.testsupport.container.TestImage;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OpenLdapContainerConnectionDetailsFactory}.
 *
 * @author Philipp Kessler
 */
@SpringJUnitConfig
@Testcontainers(disabledWithoutDocker = true)
class OpenLdapContainerConnectionDetailsFactoryIntegrationTests {

	@Container
	@ServiceConnection
	static final OpenLdapContainer openLdap = TestImage.container(OpenLdapContainer.class).withEnv("LDAP_TLS", "false");

	@Autowired
	private LdapTemplate ldapTemplate;

	@Test
	void connectionCanBeMadeToLdapContainer() {
		List<String> cn = this.ldapTemplate.search(LdapQueryBuilder.query().where("objectclass").is("dcObject"),
				(AttributesMapper<String>) (attributes) -> attributes.get("dc").get().toString());
		assertThat(cn).singleElement().isEqualTo("example");
	}

	@Configuration(proxyBeanMethods = false)
	@ImportAutoConfiguration({ LdapAutoConfiguration.class })
	static class TestConfiguration {

	}

}
