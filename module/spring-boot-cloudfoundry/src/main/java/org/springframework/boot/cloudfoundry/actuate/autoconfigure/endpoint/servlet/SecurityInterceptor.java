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

package org.springframework.boot.cloudfoundry.actuate.autoconfigure.endpoint.servlet;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.cloudfoundry.actuate.autoconfigure.endpoint.AccessLevel;
import org.springframework.boot.cloudfoundry.actuate.autoconfigure.endpoint.CloudFoundryAuthorizationException;
import org.springframework.boot.cloudfoundry.actuate.autoconfigure.endpoint.CloudFoundryAuthorizationException.Reason;
import org.springframework.boot.cloudfoundry.actuate.autoconfigure.endpoint.SecurityResponse;
import org.springframework.boot.cloudfoundry.actuate.autoconfigure.endpoint.Token;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsUtils;

/**
 * Security interceptor to validate the cloud foundry token.
 *
 * @author Madhura Bhave
 */
class SecurityInterceptor {

	private static final Log logger = LogFactory.getLog(SecurityInterceptor.class);

	private final @Nullable TokenValidator tokenValidator;

	private final @Nullable SecurityService cloudFoundrySecurityService;

	private final @Nullable String applicationId;

	private static final SecurityResponse SUCCESS = SecurityResponse.success();

	SecurityInterceptor(@Nullable TokenValidator tokenValidator, @Nullable SecurityService cloudFoundrySecurityService,
			@Nullable String applicationId) {
		this.tokenValidator = tokenValidator;
		this.cloudFoundrySecurityService = cloudFoundrySecurityService;
		this.applicationId = applicationId;
	}

	SecurityResponse preHandle(HttpServletRequest request, @Nullable EndpointId endpointId) {
		if (CorsUtils.isPreFlightRequest(request)) {
			return SecurityResponse.success();
		}
		try {
			if (!StringUtils.hasText(this.applicationId)) {
				throw new CloudFoundryAuthorizationException(Reason.SERVICE_UNAVAILABLE,
						"Application id is not available");
			}
			if (this.cloudFoundrySecurityService == null || this.tokenValidator == null) {
				throw new CloudFoundryAuthorizationException(Reason.SERVICE_UNAVAILABLE,
						"Cloud controller URL is not available");
			}
			if (HttpMethod.OPTIONS.matches(request.getMethod())) {
				return SUCCESS;
			}
			check(request, endpointId);
		}
		catch (Exception ex) {
			logger.error(ex);
			if (ex instanceof CloudFoundryAuthorizationException cfException) {
				return new SecurityResponse(cfException.getStatusCode(),
						"{\"security_error\":\"" + cfException.getMessage() + "\"}");
			}
			return new SecurityResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
		return SecurityResponse.success();
	}

	private void check(HttpServletRequest request, @Nullable EndpointId endpointId) {
		Assert.state(this.cloudFoundrySecurityService != null, "'cloudFoundrySecurityService' must not be null");
		Assert.state(this.applicationId != null, "'applicationId' must not be null");
		Assert.state(this.tokenValidator != null, "'tokenValidator' must not be null");
		Token token = getToken(request);
		this.tokenValidator.validate(token);
		AccessLevel accessLevel = this.cloudFoundrySecurityService.getAccessLevel(token.toString(), this.applicationId);
		if (!accessLevel.isAccessAllowed((endpointId != null) ? endpointId.toLowerCaseString() : "")) {
			throw new CloudFoundryAuthorizationException(Reason.ACCESS_DENIED, "Access denied");
		}
		request.setAttribute(AccessLevel.REQUEST_ATTRIBUTE, accessLevel);
	}

	private Token getToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		String bearerPrefix = "bearer ";
		if (authorization == null || !authorization.toLowerCase(Locale.ENGLISH).startsWith(bearerPrefix)) {
			throw new CloudFoundryAuthorizationException(Reason.MISSING_AUTHORIZATION,
					"Authorization header is missing or invalid");
		}
		return new Token(authorization.substring(bearerPrefix.length()));
	}

}
