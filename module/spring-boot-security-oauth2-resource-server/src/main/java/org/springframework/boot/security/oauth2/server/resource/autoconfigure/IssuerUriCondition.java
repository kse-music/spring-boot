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

package org.springframework.boot.security.oauth2.server.resource.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.StringUtils;

/**
 * Condition for creating {@link JwtDecoder} by oidc issuer location.
 *
 * @author Artsiom Yudovin
 */
class IssuerUriCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		ConditionMessage.Builder message = ConditionMessage.forCondition("OpenID Connect Issuer URI Condition");
		Environment environment = context.getEnvironment();
		String issuerUri = environment.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
		if (!StringUtils.hasText(issuerUri)) {
			return ConditionOutcome.noMatch(message.didNotFind("issuer-uri property").atAll());
		}
		String jwkSetUri = environment.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri");
		if (StringUtils.hasText(jwkSetUri)) {
			return ConditionOutcome.noMatch(message.found("jwk-set-uri property").items(jwkSetUri));
		}
		return ConditionOutcome.match(message.foundExactly("issuer-uri property"));
	}

}
