/*
 * Copyright 2012-2024 the original author or authors.
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

package org.springframework.boot.web.client;

import java.io.File;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import org.springframework.boot.testsupport.classpath.ClassPathExclusions;
import org.springframework.boot.testsupport.classpath.ClassPathOverrides;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ClientHttpRequestFactories} when OkHttp 3 is the predominant HTTP
 * client.
 *
 * @author Andy Wilkinson
 * @deprecated since 3.2.0 for removal in 3.4.0
 */
@ClassPathOverrides("com.squareup.okhttp3:okhttp:3.14.9")
@ClassPathExclusions({ "httpclient5-*.jar", "jetty-client-*.jar" })
@Deprecated(since = "3.2.0", forRemoval = true)
@SuppressWarnings("removal")
class ClientHttpRequestFactoriesOkHttp3Tests
		extends AbstractClientHttpRequestFactoriesTests<OkHttp3ClientHttpRequestFactory> {

	ClientHttpRequestFactoriesOkHttp3Tests() {
		super(OkHttp3ClientHttpRequestFactory.class);
	}

	@Test
	void okHttp3IsBeingUsed() {
		assertThat(new File(OkHttpClient.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getName())
			.startsWith("okhttp-3.");
	}

	@Override
	protected long connectTimeout(OkHttp3ClientHttpRequestFactory requestFactory) {
		return ((OkHttpClient) ReflectionTestUtils.getField(requestFactory, "client")).connectTimeoutMillis();
	}

	@Override
	protected long readTimeout(OkHttp3ClientHttpRequestFactory requestFactory) {
		return ((OkHttpClient) ReflectionTestUtils.getField(requestFactory, "client")).readTimeoutMillis();
	}

	@Override
	protected boolean supportsSettingConnectTimeout() {
		return true;
	}

	@Override
	protected boolean supportsSettingReadTimeout() {
		return true;
	}

}
